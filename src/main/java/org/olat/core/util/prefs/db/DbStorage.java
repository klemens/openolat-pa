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
* <p>
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.util.prefs.db;

import java.util.Iterator;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesStorage;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

/**
 * Description:<br>
 * <P>
 * Initial Date: 21.06.2006 <br>
 * 
 * @author Felix Jost
 */
public class DbStorage extends LogDelegator implements PreferencesStorage{

	static final String USER_PROPERTY_KEY = "v2guipreferences";
	
	public Preferences getPreferencesFor(Identity identity, boolean useTransientPreferences) {
		if (useTransientPreferences) {
			return createEmptyDbPrefs(identity,true);
		} else {			
			return getPreferencesFor(identity);
		}
	}

	/**
	 * search x-stream serialization in properties table, create new if not found
	 * @param identity
	 * @return
	 */
	private DbPrefs getPreferencesFor(final Identity identity) {
		 Property guiProperty = null; 
		 try { 
			 guiProperty = PropertyManager.getInstance().findProperty(identity, null, null, null, USER_PROPERTY_KEY); 
		 } catch (AssertException e) {
			 // OLAT-6429 detect and delete multiple prefs objects, keep the first one only 
			 List<Property> guiPropertyList = PropertyManager.getInstance().findProperties(identity, null, null, null, USER_PROPERTY_KEY); 
			 if (guiPropertyList != null && guiPropertyList.size() > 0) {
				 logError("Found more than 1 entry for " + USER_PROPERTY_KEY + " in o_property table for user " + identity.getName() + ". Use first of them, deleting the others!", e); 
				 guiProperty = guiPropertyList.get(0);
				 Iterator<Property> iterator = guiPropertyList.iterator();
				 iterator.next();
				 while (iterator.hasNext()) { 
					 Property property = (Property) iterator.next(); 
					 PropertyManager.getInstance().deleteProperty(property); 				 
					 logInfo("Will delete old property: " + property.getTextValue()); 
				 } 
			 }
		 }
		
		if (guiProperty == null) {
			return createEmptyDbPrefs(identity,false);
		} else {
			return getPreferencesForProperty(identity, guiProperty);
		}
	}

	private DbPrefs getPreferencesForProperty(Identity identity, Property guiProperty) {
		DbPrefs prefs;
		try {
			prefs = createDbPrefsFrom(identity, guiProperty, guiProperty.getTextValue());
		} catch (Exception e) {
			prefs = doGuiPrefsMigration( guiProperty, identity);
		}
		return prefs;
	}

	private DbPrefs createEmptyDbPrefs(Identity identity, boolean isTransient) {
		DbPrefs prefs = new DbPrefs();
		prefs.setIdentity(identity);
		prefs.isTransient = isTransient;
		return prefs;
	}

	private DbPrefs createDbPrefsFrom(Identity identity, Property guiProperty, String textValue) {
		DbPrefs prefs = (DbPrefs) XStreamHelper.fromXML(textValue);
		prefs.setIdentity(identity); // reset transient value
		prefs.dbProperty = guiProperty; // set property for later use
		return prefs;
	}

	private DbPrefs doGuiPrefsMigration(Property guiProperty, Identity identity) {
		String migratedTextValue = doCalendarRefactoringMigration(guiProperty.getTextValue());
		// add new migration methode here 
		try {
			return createDbPrefsFrom(identity, guiProperty, migratedTextValue);
		} catch (Exception e) {
			// Migration failed => return empty db-prefs
			return createEmptyDbPrefs(identity,false);
		}
	}

	/**
	 * Migration for 5.1.x to 5.2.0 because the calendar package was changed. 
	 * Rename 'org.olat.core.commons.calendar.model.KalendarConfig' to 'org.olat.commons.calendar.model.KalendarConfig'.
	 * @param textValue
	 * @return Migrated textValue String
	 */
	private String doCalendarRefactoringMigration(String textValue) {
		return textValue.replaceAll("org.olat.core.commons.calendar.model.KalendarConfig", "org.olat.commons.calendar.model.KalendarConfig");
	}

}
