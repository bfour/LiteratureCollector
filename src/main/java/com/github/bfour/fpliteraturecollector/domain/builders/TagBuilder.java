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

package com.github.bfour.fpliteraturecollector.domain.builders;

import java.awt.Color;

import com.github.bfour.fpliteraturecollector.domain.Tag;
import com.github.bfour.jlib.commons.utils.Getter;

public class TagBuilder extends
		com.github.bfour.jlib.guiextended.tagging.TagBuilder<Tag> {

	private static Getter<com.github.bfour.jlib.guiextended.tagging.Tag, Tag> tagGetter = new Getter<com.github.bfour.jlib.guiextended.tagging.Tag, Tag>() {
		@Override
		public Tag get(com.github.bfour.jlib.guiextended.tagging.Tag input) {
			return new Tag(input.getID(), input.getCreationTime(),
					input.getLastChangeTime(), input.getName(), "",
					input.getColour()); // TODO implement description
		}
	};
	private String name;
	private Color colour;
	private String description;

	public TagBuilder() {
		super(tagGetter);
	}

	public TagBuilder(Tag tag) {

		super(tagGetter, tag);

		setID(tag.getID());
		setCreationTime(tag.getCreationTime());
		setLastChangeTime(tag.getLastChangeTime());

		setName(tag.getName());
		setColour(tag.getColour());
		setDescription(tag.getDescription());

	}

	@Override
	public Tag getObject() {
		return new Tag(getID(), getCreationTime(), getLastChangeTime(),
				getName(), getDescription(), getColour());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Color getColour() {
		return colour;
	}

	@Override
	public void setColour(Color colour) {
		this.colour = colour;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
