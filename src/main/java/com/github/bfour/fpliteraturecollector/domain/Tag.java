package com.github.bfour.fpliteraturecollector.domain;

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

import java.awt.Color;
import java.util.Date;

import com.github.bfour.fpjcommons.model.Entity;

public class Tag extends Entity {

	private String name;
	private Color colour;

	public Tag(Long ID, Date creationTime, Date lastChangeTime, String name,
			Color colour) {
		super(ID, creationTime, lastChangeTime);
		this.name = name;
		this.colour = colour;
	}

	public Tag(String name, Color colour) {
		super();
		this.name = name;
		this.colour = colour;
	}

	public String getName() {
		return name;
	}

	public Color getColour() {
		return colour;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getID() == null) ? 0 : getID().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Tag))
			return false;
		Tag other = (Tag) obj;
		if (getID() == null) {
			if (other.getID() != null)
				return false;
		} else if (!getID().equals(other.getID()))
			return false;
		return true;
	}

}
