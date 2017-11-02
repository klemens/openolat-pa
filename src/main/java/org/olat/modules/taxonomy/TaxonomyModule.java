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
package org.olat.modules.taxonomy;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.taxonomy.site.TaxonomyContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String TAXONOMY_ENABLED = "taxonomy.enabled";
	private static final String TAXONOMY_TREE_KEY = "taxonomy.tree.key";
	
	@Value("${taxonomy.enabled:true}")
	private boolean enabled;
	private String taxonomyTreeKey;
	
	@Autowired
	public TaxonomyModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator("TaxonomySite",
				new TaxonomyContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Taxonomy",
				new TaxonomyContextEntryControllerCreator());

		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(TAXONOMY_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String taxonomyTreeKeyObj = getStringPropertyValue(TAXONOMY_TREE_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyTreeKeyObj)) {
			taxonomyTreeKey = taxonomyTreeKeyObj;
		}
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(TAXONOMY_ENABLED, Boolean.toString(enabled), true);
	}

	public String getTaxonomyTreeKey() {
		return taxonomyTreeKey;
	}

	public void setTaxonomyTreeKey(String taxonomyTreeKey) {
		this.taxonomyTreeKey = taxonomyTreeKey;
		setStringProperty(TAXONOMY_TREE_KEY, taxonomyTreeKey, true);
	}

	public boolean isManagedTaxonomyLevels() {
		return true;
	}
}
