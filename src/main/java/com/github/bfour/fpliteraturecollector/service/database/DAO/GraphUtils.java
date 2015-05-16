package com.github.bfour.fpliteraturecollector.service.database.DAO;

/*
 * -\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\-
 * FP-LiteratureCollector
 * =================================
 * Copyright (C) 2015 Florian Pollak
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

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.bfour.fpjcommons.model.Entity;
import com.github.bfour.fpjcommons.services.DatalayerException;
import com.github.bfour.fpjcommons.services.CRUD.BidirectionalCRUDService;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class GraphUtils {

	private static final Logger LOGGER = Logger.getLogger(GraphUtils.class);

	public static <T extends Entity> void setCollectionPropertyOnVertex(
			Vertex vertex, String edgeName, Collection<T> collectionToBeSet,
			AbstractOrientDBDAO<T> collectionItemDAO,
			BidirectionalCRUDService<T> collectionItemService,
			boolean cascadeDelete) throws DatalayerException {

		// get current items of collection in vertex
		List<T> vertexItems = new LinkedList<T>();
		Iterable<Edge> vertexItemEdges = vertex.getEdges(Direction.OUT,
				edgeName);
		for (Edge vertexItemEdge : vertexItemEdges) {
			vertexItems.add(collectionItemDAO.vertexToEntity(vertexItemEdge
					.getVertex(Direction.IN)));
		}

		// determine items to remove and add in vertex
		List<T> itemsToRemove = new LinkedList<>();
		for (T vertexItem : vertexItems) {
			if (collectionToBeSet == null
					|| !collectionToBeSet.contains(vertexItem))
				itemsToRemove.add(vertexItem);
		}
		List<T> itemsToAdd = new LinkedList<>();
		if (collectionToBeSet != null) {
			for (T collectionToBeSetItem : collectionToBeSet) {
				if (!vertexItems.contains(collectionToBeSetItem))
					itemsToAdd.add(collectionToBeSetItem);
			}
		}
		List<T> itemsToUpdate = new LinkedList<>();
		if (collectionToBeSet != null) {
			for (T collectionToBeSetItem : collectionToBeSet) {
				if (!itemsToAdd.contains(collectionToBeSetItem))
					itemsToUpdate.add(collectionToBeSetItem);
			}
		}

		// go through existing edges and remove if necessary
		vertexItemEdges = vertex.getEdges(Direction.OUT, edgeName);
		for (Edge vertexItemEdge : vertexItemEdges) {
			T p = collectionItemDAO.vertexToEntity(vertexItemEdge
					.getVertex(Direction.IN));
			if (itemsToRemove.contains(p)) {
				LOGGER.debug("removing vertex item edge "
						+ vertexItemEdge.getLabel());
				vertexItemEdge.remove();
				boolean referencedVertexHasMoreEdges = vertexItemEdge
						.getVertex(Direction.IN)
						.getEdges(Direction.IN, edgeName).iterator().hasNext();
				if (cascadeDelete && !referencedVertexHasMoreEdges) {
					LOGGER.debug("cascaded deletion of related items: deleting "
							+ p + " of type " + p.getClass().getName());
					collectionItemDAO.delete(p, false);
					collectionItemService.receiveDelete(p);
				}
			}
		}

		// add items to be added
		for (T itemToAdd : itemsToAdd) {
			Vertex itemVertex = collectionItemDAO.getVertexForEntity(itemToAdd);
			if (itemVertex == null) {
				LOGGER.debug("creating item " + itemToAdd + " of type "
						+ itemToAdd.getClass().getName());
				T createdItem = collectionItemDAO.create(itemToAdd, false);
				collectionItemService.receiveCreate(createdItem);
				itemVertex = collectionItemDAO.getVertexForEntity(createdItem);
			}
			LOGGER.debug("creating edge between given vertex and " + itemToAdd
					+ " (" + itemToAdd.getClass().getName() + ")");
			vertex.addEdge(edgeName, itemVertex);
		}

		// items to be updated
		for (T itemToUpdate : itemsToUpdate) {
			LOGGER.debug("updating item " + itemToUpdate + " ("
					+ itemToUpdate.getClass().getName() + ")");
			collectionItemDAO.update(itemToUpdate, itemToUpdate, false); // TODO
																			// do
																			// we
																			// know
																			// old
																			// entity?
																			// improve
		}

	}

	public static <T extends Entity> List<T> getCollectionFromVertexProperty(
			Vertex v, String edgeName, AbstractOrientDBDAO<T> DAO)
			throws DatalayerException {

		List<T> items = new LinkedList<>();
		Iterable<Edge> edgeIter = v.getEdges(Direction.OUT, edgeName);
		for (Edge itemEdge : edgeIter) {
			Vertex itemVertex = itemEdge.getVertex(Direction.IN);
			T item = DAO.vertexToEntity(itemVertex);
			items.add(item);
		}
		return items;

	}

	public static <T> void setProperty(Vertex v, String name, T value,
			boolean canBeNull) {
		if (value == null && canBeNull)
			v.removeProperty(name);
		else if (value != null)
			v.setProperty(name, value);
		else
			throw new InvalidParameterException("value \"" + name
					+ "\" is null but declared as cannot be null");
	}

}
