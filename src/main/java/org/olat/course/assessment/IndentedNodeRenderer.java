/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Description:<BR/>
 * Renders a node in a table using the node icon and indent. Required object is a map
 * that contains the values using the keys defined in AssessmentHelper
 * 
 * <P/>
 * Initial Date:  Nov 23, 2004
 *
 * @author gnaegi 
 */
public class IndentedNodeRenderer implements CustomCellRenderer {

	private static final String INDENT = "&nbsp;&nbsp;";
	//fxdiff VCRP-4: assessment overview with max score
	private boolean indentationEnabled = true;

	/**
	 * 
	 */
	public IndentedNodeRenderer() {
		super();
	}
	
	public boolean isIndentationEnabled() {
		return indentationEnabled;
	}
	
	public void setIndentationEnabled(boolean indentationEnabled) {
		this.indentationEnabled = indentationEnabled;
	}

	/** 
	 * @see org.olat.core.gui.components.table.CustomCellRenderer#render(org.olat.core.gui.render.StringOutput, org.olat.core.gui.render.Renderer, java.lang.Object, java.util.Locale, int, java.lang.String)
	 */
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		Map nodeData = (Map) val;
		Integer indent = (Integer) nodeData.get(AssessmentHelper.KEY_INDENT);
		String type = (String)  nodeData.get(AssessmentHelper.KEY_TYPE);

		String cssClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type).getIconCSSClass();
		String title = (String)  nodeData.get(AssessmentHelper.KEY_TITLE_SHORT);
		String altText = (String)  nodeData.get(AssessmentHelper.KEY_TITLE_LONG);
		
		//fxdiff VCRP-4: assessment overview with max score
		if(isIndentationEnabled()) {
			Integer indentation = (Integer) nodeData.get(AssessmentHelper.KEY_INDENT);
			appendIndent(sb,indentation);
		}
		
		sb.append("<span class=\"b_with_small_icon_left ").append(cssClass);
		if (altText != null) {
			sb.append("\" title= \"").append(StringHelper.escapeHtml(altText));
		}
		sb.append("\">");
		sb.append(StringHelper.escapeHtml(title));
		sb.append("</span>");
	}

	
  private void appendIndent(StringOutput sb, Integer indent) {
  	for (int i = 0; i < indent.intValue(); i++) {
			sb.append(INDENT);
		}
  }

}
