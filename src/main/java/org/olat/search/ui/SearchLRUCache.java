/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.search.ui;

import org.apache.commons.collections.map.LRUMap;
import org.olat.core.commons.services.search.SearchResults;

class SearchLRUCache extends LRUMap {

	public SearchLRUCache() {
		super(5);
	}
	
	@Override
	public SearchResults get(Object key) {
		SearchEntry searchEntry = (SearchEntry)super.get(key);
		if(searchEntry != null) {
			if(searchEntry.isUpToDate()) {
				return searchEntry.getSearchResults();
			}
			remove(key);
		}
		return null;
	}

	@Override
	public Object put(Object key, Object value) {
		return super.put(key, new SearchEntry((SearchResults)value));
	}

	public class SearchEntry {
		private final SearchResults results;
		private long timestamp = System.currentTimeMillis();
		
		public SearchEntry(SearchResults results) {
			this.results = results;
		}
		
		public SearchResults getSearchResults() {
			return results;
		}
		
		public boolean isUpToDate() {
			return System.currentTimeMillis() - timestamp < 300000;
		}
	}
}
