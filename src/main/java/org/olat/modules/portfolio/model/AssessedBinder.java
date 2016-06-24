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
package org.olat.modules.portfolio.model;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.portfolio.Binder;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBinder {
	
	private final Identity assessedIdentity;
	private final Binder binder;
	private final AssessmentEntry assessmentEntry;
	
	public AssessedBinder(Identity assessedIdentity, Binder binder, AssessmentEntry assessmentEntry) {
		this.assessedIdentity = assessedIdentity;
		this.binder = binder;
		this.assessmentEntry = assessmentEntry;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public Binder getBinder() {
		return binder;
	}

	public AssessmentEntry getAssessmentEntry() {
		return assessmentEntry;
	}
}
