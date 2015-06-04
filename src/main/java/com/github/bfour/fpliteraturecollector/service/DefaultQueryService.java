package com.github.bfour.fpliteraturecollector.service;

/*
 * -\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\-
 * FP-LiteratureCollector
 * =================================
 * Copyright (C) 2014 - 2015 Florian Pollak
 * =================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -///////////////////////////////-
 */

import com.github.bfour.fpjcommons.lang.Tuple;
import com.github.bfour.fpjcommons.services.DatalayerException;
import com.github.bfour.fpjcommons.services.ServiceException;
import com.github.bfour.fpjcommons.services.CRUD.DataIterator;
import com.github.bfour.fpjcommons.services.CRUD.EventCreatingCRUDService;
import com.github.bfour.fpliteraturecollector.domain.AtomicRequest;
import com.github.bfour.fpliteraturecollector.domain.Query;
import com.github.bfour.fpliteraturecollector.domain.Query.QueryStatus;
import com.github.bfour.fpliteraturecollector.domain.builders.QueryBuilder;
import com.github.bfour.fpliteraturecollector.service.crawlers.Crawler;
import com.github.bfour.fpliteraturecollector.service.database.DAO.QueryDAO;

public class DefaultQueryService extends EventCreatingCRUDService<Query>
		implements QueryService {

	private static DefaultQueryService instance;
	private AtomicRequestService atomReqServ;
	private QueryDAO DAO;

	private DefaultQueryService(QueryDAO DAO, boolean forceCreateNewInstance,
			AtomicRequestService atomReqServ) {
		super(DAO);
		this.DAO = DAO;
		this.atomReqServ = atomReqServ;
	}

	public static DefaultQueryService getInstance(QueryDAO DAO,
			boolean forceCreateNewInstance, AtomicRequestService atomReqServ) {
		if (instance == null || forceCreateNewInstance)
			instance = new DefaultQueryService(DAO, forceCreateNewInstance,
					atomReqServ);
		return instance;
	}

	@Override
	public synchronized Query create(Query entity) throws ServiceException {
		entity = setStatus(entity);
		checkIntegrity(entity);
		return super.create(entity);
	}

	@Override
	public synchronized Query update(Query oldEntity, Query newEntity)
			throws ServiceException {
		checkIntegrity(newEntity);
		return super.update(oldEntity, newEntity);
	}

	@Override
	public void delete(Query entity) throws ServiceException {
		if (entity.getAtomicRequests() != null)
			for (AtomicRequest ar : entity.getAtomicRequests())
				atomReqServ.delete(ar);
		super.delete(entity);
	}

	@Override
	public synchronized void deleteCascade(Query q) throws ServiceException {
		if (q.getAtomicRequests() != null)
			for (AtomicRequest ar : q.getAtomicRequests())
				atomReqServ.deleteCascade(ar);
		super.delete(q);
	}

	private void checkIntegrity(Query entity) throws ServiceException {
		if (entity.getAtomicRequests() == null)
			throw new ServiceException(
					"atomic request list must not be null (may be empty)");
		if (entity.getStatus() == null)
			throw new ServiceException("status must not be null");
	}

	@Override
	public synchronized Query getByQueuePosition(int position)
			throws ServiceException {
		try {
			Query q = DAO.getByQueuePosition(position);
			if (q == null)
				return null;
			return q;
		} catch (DatalayerException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public synchronized Query queueUp(Query query) throws ServiceException {

		Query predecessor = getByQueuePosition(query.getQueuePosition() - 1);
		if (predecessor == null) {
			// no predecessor, we're already at beginning of queue do nothing
			return query;
		}

		// switch this with predecessor
		QueryBuilder qBuilder = new QueryBuilder(query);
		QueryBuilder predBuilder = new QueryBuilder(predecessor);
		qBuilder.setQueuePosition(predecessor.getQueuePosition());
		predBuilder.setQueuePosition(query.getQueuePosition());

		update(predecessor, predBuilder.getObject());
		return update(query, qBuilder.getObject());

	}

	@Override
	public synchronized Query queueDown(Query query) throws ServiceException {

		Query successor = getByQueuePosition(query.getQueuePosition() + 1);
		if (successor == null) {
			// no successor, we're already at end of queue do nothing
			return query;
		}

		// switch this with successor
		QueryBuilder qBuilder = new QueryBuilder(query);
		QueryBuilder sucBuilder = new QueryBuilder(successor);
		qBuilder.setQueuePosition(successor.getQueuePosition());
		sucBuilder.setQueuePosition(query.getQueuePosition());

		update(successor, sucBuilder.getObject());
		return update(query, qBuilder.getObject());

	}

	@Override
	public synchronized Query queue(Query query) throws ServiceException {
		if (query.getStatus() == QueryStatus.IDLE
				|| query.getStatus() == QueryStatus.FINISHED_WITH_ERROR
				|| query.getStatus() == QueryStatus.QUEUED) {
			Query newQuery = new QueryBuilder(query)
					.setQueuePosition(getMaxQueuePosition() + 1)
					.setStatus(QueryStatus.QUEUED).getObject();
			return update(query, newQuery);
		}
		return query;
	}

	@Override
	public synchronized void queueAll() throws ServiceException {
		for (Query q : getAll())
			queue(q);
	}

	@Override
	public synchronized Query unqueue(Query query) throws ServiceException {
		Query newQuery = new QueryBuilder(query).setQueuePosition(null)
				.getObject();
		newQuery = setInitialStatus(newQuery);
		return update(query, newQuery);
	}

	@Override
	public synchronized void unqueueAll() throws ServiceException {
		for (Query q : getAll())
			unqueue(q);
	}

	@Override
	public boolean hasAnyUnprocessedRequest() throws ServiceException {
		DataIterator<Query> iter = getAllByStream();
		try {
			while (iter.hasNext()) {
				Query query;
				query = iter.next();
				for (AtomicRequest atomReq : query.getAtomicRequests()) {
					if (!atomReq.isProcessed())
						return true;
				}
			}
		} catch (DatalayerException e) {
			throw new ServiceException(e);
		}
		return false;
	}

	@Override
	public synchronized Tuple<Query, AtomicRequest> getFirstUnprocessedRequestInQueueForCrawler(
			Crawler crawler) throws ServiceException {

		// TODO (low) improve performance by using direct SQL max() query
		int pos = 1;
		Query q;
		while ((q = getByQueuePosition(pos)) != null) {
			pos++;
			for (AtomicRequest atomReq : q.getAtomicRequests()) {
				if (!atomReq.getCrawler().equals(crawler))
					continue;
				if (!atomReq.isProcessed())
					return new Tuple<Query, AtomicRequest>(q, atomReq);
			}
		}
		return null;

	}

	private int getMaxQueuePosition() throws ServiceException {
		// TODO (low) improve performance by using direct SQL max() query
		int max = 0;
		for (Query q : getAll()) {
			Integer pos = q.getQueuePosition();
			if (pos != null && pos > max)
				max = pos;
		}
		return max;
	}

	@Override
	public void setAllIdleOrFinished() throws ServiceException {
		for (Query q : getAll()) {
			update(q, setInitialStatus(q));
		}
	}

	private Query setStatus(Query q) {
		if (q.getStatus() != null)
			return q;

		boolean hasError = false;
		for (AtomicRequest atomReq : q.getAtomicRequests()) {
			if (!atomReq.isProcessed())
				return new QueryBuilder(q).setStatus(QueryStatus.IDLE)
						.getObject();
			if (atomReq.getProcessingError() != null)
				hasError = true;
		}

		if (hasError)
			return new QueryBuilder(q).setStatus(
					QueryStatus.FINISHED_WITH_ERROR).getObject();
		else
			return new QueryBuilder(q).setStatus(QueryStatus.FINISHED)
					.getObject();

	}

	private Query setInitialStatus(Query q) {
		if (q.getStatus() != null
				&& (q.getStatus() == QueryStatus.FINISHED || q.getStatus() == QueryStatus.FINISHED_WITH_ERROR)) {
			// return new QueryBuilder(q).setStatus(q.getStatus()).getObject();
			return q;
		}
		return new QueryBuilder(q).setStatus(QueryStatus.IDLE).getObject();
	}

}
