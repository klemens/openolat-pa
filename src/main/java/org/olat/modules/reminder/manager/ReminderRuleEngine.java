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
package org.olat.modules.reminder.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.modules.reminder.FilterRuleSPI;
import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.rule.BusinessGroupRoleRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryRoleRuleSPI;
import org.olat.modules.reminder.rule.UserPropertyRuleSPI;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReminderRuleEngine {
	
	public static final String DATE_RULE_TYPE = DateRuleSPI.class.getSimpleName();
	public static final String USER_PROP_RULE_TYPE = UserPropertyRuleSPI.class.getSimpleName();
	public static final String REPO_ROLE_RULE_TYPE = RepositoryEntryRoleRuleSPI.class.getSimpleName();
	public static final String BUSINESSGROUP_ROLE_RULE_TYPE = BusinessGroupRoleRuleSPI.class.getSimpleName();

	@Autowired
	private DateRuleSPI dateRuleSpi;
	@Autowired
	private UserPropertyRuleSPI userPropertyRuleSpi;
	@Autowired
	private BusinessGroupRoleRuleSPI groupRoleRuleSpi;
	@Autowired
	private RepositoryEntryRoleRuleSPI repoRoleRuleSpi;
	
	@Autowired
	private ReminderDAO reminderDao;
	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private ReminderService reminderManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	
	public List<Identity> evaluate(Reminder reminder, boolean resend) {
		String config = reminder.getConfiguration();
		ReminderRules rules = reminderManager.toRules(config);
		if(rules == null || rules.getRules() == null || rules.getRules().isEmpty()) {
			return Collections.emptyList();	
		}
		
		List<ReminderRule> ruleList = new ArrayList<>(rules.getRules());
		//1. Date rules doesn't need database queries
		boolean allOk = evaluateDateRule(ruleList);
		
		List<Identity> identities;
		if(allOk) {
			identities = getIdentities(reminder.getEntry(), reminder, ruleList, resend);
			
			if(identities.size() > 0 && ruleList.size() > 0) {
				filterByRules(reminder.getEntry(),  identities, ruleList);
			}

		} else {
			identities = Collections.emptyList();	
		}
		return identities;
	}
	
	/**
	 * 
	 * @param reminder
	 */
	protected boolean evaluateDateRule(List<ReminderRule> ruleList) {
		boolean allOk = true;
		
		for(Iterator<ReminderRule> ruleIt=ruleList.iterator(); ruleIt.hasNext(); ) {
			ReminderRule rule = ruleIt.next();
			if(DATE_RULE_TYPE.equals(rule.getType())) {
				allOk &= dateRuleSpi.evaluate(rule);
				ruleIt.remove();
			}
		}

		return allOk;
	}
	
	protected List<Identity> getIdentities(RepositoryEntry entry, Reminder reminder, List<ReminderRule> ruleList, boolean resend) {	
		List<ReminderRule> identitiesProviderRules = new ArrayList<>();

		for(Iterator<ReminderRule> ruleIt=ruleList.iterator(); ruleIt.hasNext(); ) {
			ReminderRule rule = ruleIt.next();
			RuleSPI ruleSpi = reminderModule.getRuleSPIByType(rule.getType());
			if(ruleSpi instanceof IdentitiesProviderRuleSPI) {
				identitiesProviderRules.add(rule);
				ruleIt.remove();
			}
		}
		
		List<Identity> identities;
		if(identitiesProviderRules.isEmpty()) {
			//all members of repository entry
			List<Identity> duplicatedIdentities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.both,
					GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name());
			identities = new ArrayList<>(new HashSet<>(duplicatedIdentities));
		} else {
			identities = null;
			
			for(ReminderRule rule:identitiesProviderRules) {
				RuleSPI ruleSpi = reminderModule.getRuleSPIByType(rule.getType());
				IdentitiesProviderRuleSPI identitiesProviderRuleSpi = (IdentitiesProviderRuleSPI)ruleSpi;
				List<Identity> members = identitiesProviderRuleSpi.evaluate(entry, rule);
				if(identities == null) {
					identities = members;
				} else {
					identities.retainAll(members);
				}
			}
		}
		
		//filter by user property
		filterIdentitiesByProperty(identities, ruleList);
		// deduplicated the list
		identities = new ArrayList<>(new HashSet<>(identities));
		if(!resend) {
			List<Long> alreadySendKeys = reminderDao.getReminderRecipientKeys(reminder);
			Set<Long> alreadySendKeySet = new HashSet<>(alreadySendKeys);
			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				if(alreadySendKeySet.contains(identityIt.next().getKey())) {
					identityIt.remove();
				}
			}
		}
		return identities;
	}
	
	/**
	 * Remove identities of the list which not match the user properties rules if any.
	 * 
	 * @param identities
	 * @param ruleList
	 */
	protected void filterIdentitiesByProperty(List<Identity> identities, List<ReminderRule> ruleList) {
		List<ReminderRule> userPropRules = new ArrayList<>(3);
		
		for(Iterator<ReminderRule> ruleIt=ruleList.iterator(); ruleIt.hasNext(); ) {
			ReminderRule rule = ruleIt.next();
			if(USER_PROP_RULE_TYPE.equals(rule.getType())) {
				userPropRules.add(rule);
				ruleIt.remove();
			}
		}
		
		if(userPropRules.size() > 0) {
			int numOfRules = userPropRules.size();
			ReminderRule[] ruleArr = userPropRules.toArray(new ReminderRule[numOfRules]);

			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				
				boolean accept = true;
				for(int i=numOfRules; i-->0; ) {
					accept &= userPropertyRuleSpi.accept(ruleArr[i], identity);
				}
				
				if(!accept) {
					identityIt.remove();
				}
			}
		}
	}
	
	protected void filterByRules(RepositoryEntry entry, List<Identity> identities, List<ReminderRule> ruleList) {
		List<ReminderRule> filterRules = new ArrayList<>(3);
		
		for(Iterator<ReminderRule> ruleIt=ruleList.iterator(); ruleIt.hasNext(); ) {
			ReminderRule rule = ruleIt.next();
			RuleSPI ruleSpi = reminderModule.getRuleSPIByType(rule.getType());
			if(ruleSpi instanceof FilterRuleSPI) {
				filterRules.add(rule);
				ruleIt.remove();
			}
		}
		
		for(ReminderRule rule:filterRules) {
			RuleSPI ruleSpi = reminderModule.getRuleSPIByType(rule.getType());
			if(ruleSpi instanceof FilterRuleSPI) {
				FilterRuleSPI filter = (FilterRuleSPI)ruleSpi;
				filter.filter(entry, identities, rule);
			}	
		}
	}
}
