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

package com.github.bfour.fpliteraturecollector.service.crawlers;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.epop.dataprovider.DataProvider;
import org.epop.dataprovider.DataUnavailableException;

import com.github.bfour.fpjcommons.services.DatalayerException;
import com.github.bfour.fpjsearch.fpjsearch.Searchable;
import com.github.bfour.fpliteraturecollector.domain.AtomicRequest;
import com.github.bfour.fpliteraturecollector.domain.Literature;
import com.github.bfour.fpliteraturecollector.domain.SearchEngine;

public class Crawler implements Searchable {

	private DataProvider provider;
	private List<SearchEngine> engines;

	public Crawler(DataProvider provider, SearchEngine... engines) {
		this.provider = provider;
		this.engines = Arrays.asList(engines);
	}

	public List<Literature> process(AtomicRequest atomReq)
			throws DataUnavailableException, DatalayerException,
			URISyntaxException {
		return provider.runQuery(atomReq.getSearchString(),
				atomReq.getMaxPageTurns(), false); // TODO determine initial
													// wait based on previous
													// activity
	}

	@Override
	public String toString() {
		return CrawlerService.getInstance().getIdentifierForCrawler(this);
	}

	/**
	 * Get the SupportedSearchEngines used by this Crawler. This information may
	 * be used by a scheduler to run crawlers in parallel.
	 * 
	 * @return SupportedSearchEngines used by this Crawler
	 */
	public List<SearchEngine> getSearchEnginesBeingAccessed() {
		return engines;
	}

}
