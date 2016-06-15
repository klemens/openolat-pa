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
package org.olat.modules.portfolio.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.portfolio.model.SharedItemRow;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedItemsDataModel extends DefaultFlexiTableDataModel<SharedItemRow> {
	
	public SharedItemsDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		SharedItemRow itemRow = getObject(row);
		switch(ShareItemCols.values()[col]) {
			case binderName: return itemRow.getBinder();
			case courseName: return itemRow.getCourse();
			case lastModified: return itemRow.getLastModified();
		}
		return null;
	}
	
	@Override
	public SharedItemsDataModel createCopyWithEmptyList() {
		return new SharedItemsDataModel(getTableColumnModel());
	}

	public enum ShareItemCols implements FlexiSortableColumnDef {
		binderName("table.header.key"),
		courseName("table.header.title"),
		lastModified("table.header.open");
		
		private final String i18nKey;
		
		private ShareItemCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
