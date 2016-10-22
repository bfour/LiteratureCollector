/*
 * Copyright 2016 Florian Pollak
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.bfour.fpliteraturecollector.service;

import com.github.bfour.fpjcommons.services.ServiceException;
import com.github.bfour.fpjcommons.services.CRUD.EventCreatingCRUDService;
import com.github.bfour.fpliteraturecollector.domain.Tag;
import com.github.bfour.fpliteraturecollector.service.database.DAO.TagDAO;

public class DefaultTagService extends EventCreatingCRUDService<Tag> implements
		TagService {

	private static DefaultTagService instance;

	private DefaultTagService(TagDAO DAO) {
		super(DAO);
	}

	public static DefaultTagService getInstance(TagDAO DAO,
			boolean forceCreateNewInstance) {
		if (instance == null || forceCreateNewInstance)
			instance = new DefaultTagService(DAO);
		return instance;
	}

	@Override
	public Tag create(Tag entity) throws ServiceException {
		checkIntegrity(entity);
		return super.create(entity);
	}

	@Override
	public Tag update(Tag oldEntity, Tag newEntity) throws ServiceException {
		checkIntegrity(newEntity);
		return super.update(oldEntity, newEntity);
	}

	private void checkIntegrity(Tag tag) throws ServiceException {
		if (tag.getName() == null)
			throw new ServiceException("name of tag must be specified");
		if (tag.getColour() == null)
			throw new ServiceException("colour of tag must be specified");
	}

}
