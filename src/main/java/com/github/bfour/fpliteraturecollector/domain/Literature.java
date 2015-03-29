package com.github.bfour.fpliteraturecollector.domain;

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

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import com.github.bfour.fpjcommons.model.Entity;

public class Literature extends Entity {

	public static enum LiteratureType {
		UNKNOWN, BOOK, DISSERTATION, JOURNAL_PAPER, CONFERENCE_PAPER,
	}

	protected String title;
	protected LiteratureType type;
	protected List<Author> authors;
	protected String DOI;
	protected ISBN ISBN;
	protected Integer year;
	/**
	 * eg. name of journal, name of conference ...
	 */
	protected String publicationContext;
	protected String publisher;
	protected String websiteURL;
	protected String fulltextURL;
	protected Path fulltextFilePath;
	
	protected Integer gScholarNumCitations;

	// TODO: also important: type of publication (journal, proceeding
	// (Konferenzband), book chapter), Verlag, Datum (Jahr)

	public Literature(Long iD, Date creationTime, Date lastChangeTime,
			String title, LiteratureType type, List<Author> authors,
			String DOI, ISBN ISBN, Integer year, String publicationContext, String publisher, String websiteURL, 
			String fulltextURL, Path fulltextFilePath, Integer gScholarNumCitations) {
		super(iD, creationTime, lastChangeTime);
		this.title = title;
		this.type = type;
		this.authors = authors;
		this.DOI = DOI;
		this.ISBN = ISBN;
		this.year = year;
		this.publicationContext = publicationContext;
		this.publisher = publisher;
		this.websiteURL = websiteURL;
		this.fulltextURL = fulltextURL;
		this.fulltextFilePath = fulltextFilePath;
		this.gScholarNumCitations = gScholarNumCitations;
	}

	public Literature(String title, LiteratureType type, List<Author> authors,
			String DOI, ISBN ISBN, Integer year, String publicationContext, String publisher, String websiteURL, 
			String fulltextURL, Path fulltextFilePath, Integer gScholarNumCitations) {
		super();
		this.title = title;
		this.type = type;
		this.authors = authors;
		this.DOI = DOI;
		this.ISBN = ISBN;
		this.year = year;
		this.publicationContext = publicationContext;
		this.publisher = publisher;
		this.websiteURL = websiteURL;
		this.fulltextURL = fulltextURL;
		this.fulltextFilePath = fulltextFilePath;
		this.gScholarNumCitations = gScholarNumCitations;
	}

	public Literature() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public LiteratureType getType() {
		return type;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public String getDOI() {
		return DOI;
	}

	public ISBN getISBN() {
		return ISBN;
	}

	public Integer getYear() {
		return year;
	}

	public String getPublicationContext() {
		return publicationContext;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getWebsiteURL() {
		return websiteURL;
	}

	public String getFulltextURL() {
		return fulltextURL;
	}

	public Path getFulltextFilePath() {
		return fulltextFilePath;
	}

	public Integer getgScholarNumCitations() {
		return gScholarNumCitations;
	}
	
	@Override
	public String toString() {
		return title;
	}

}
