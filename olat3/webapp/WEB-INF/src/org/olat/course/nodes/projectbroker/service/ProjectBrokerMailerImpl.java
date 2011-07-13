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
* <p>
*/ 

package org.olat.course.nodes.projectbroker.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.properties.Property;


/**
 * 
 * @author guretzki
 */

public class ProjectBrokerMailerImpl implements ProjectBrokerMailer {
	private static final String KEY_ENROLLED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.enrolled.to.participant.subject";
	private static final String KEY_ENROLLED_EMAIL_TO_PARTICIPANT_BODY    = "mail.enrolled.to.participant.body";
	
	private static final String KEY_ENROLLED_EMAIL_TO_MANAGER_SUBJECT     = "mail.enrolled.to.manager.subject";
	private static final String KEY_ENROLLED_EMAIL_TO_MANAGER_BODY        = "mail.enrolled.to.manager.body";

	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.cancel.enrollment.to.participant.subject";
	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_BODY    = "mail.cancel.enrollment.to.participant.body";

	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_SUBJECT = "mail.cancel.enrollment.to.manager.subject";
	private static final String KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_BODY    = "mail.cancel.enrollment.to.manager.body";

	private static final String KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.project.changed.to.participant.subject";
	private static final String KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_BODY    = "mail.project.changed.to.participant.body";
	
	private static final String KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT = "mail.project.deleted.to.participant.subject";
	private static final String KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY    = "mail.project.deleted.to.participant.body";
	
	private static final String KEY_REMOVE_CANDIDATE_EMAIL_SUBJECT = "mail.remove.candidate.subject";
	private static final String KEY_REMOVE_CANDIDATE_EMAIL_BODY    = "mail.remove.candidate.body";
	private static final String KEY_ACCEPT_CANDIDATE_EMAIL_SUBJECT = "mail.accept.candidate.subject";
	private static final String KEY_ACCEPT_CANDIDATE_EMAIL_BODY    = "mail.accept.candidate.body";
	private static final String KEY_ADD_CANDIDATE_EMAIL_SUBJECT = "mail.add.candidate.subject";
	private static final String KEY_ADD_CANDIDATE_EMAIL_BODY    = "mail.add.candidate.body";
	private static final String KEY_ADD_PARTICIPANT_EMAIL_SUBJECT = "mail.add.participant.subject";
	private static final String KEY_ADD_PARTICIPANT_EMAIL_BODY    = "mail.add.participant.body";
	private static final String KEY_REMOVE_PARTICIPANT_EMAIL_SUBJECT = "mail.remove.participant.subject";
	private static final String KEY_REMOVE_PARTICIPANT_EMAIL_BODY    = "mail.remove.participant.body";
	
	private OLog log = Tracing.createLoggerFor(this.getClass()); 
	
	
	// For Enrollment 
	public MailerResult sendEnrolledEmailToParticipant(Identity enrolledIdentity, Project project, Translator pT) {
		return sendEmail(enrolledIdentity, project, 
				             pT.translate(KEY_ENROLLED_EMAIL_TO_PARTICIPANT_SUBJECT), 
				             pT.translate(KEY_ENROLLED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	public MailerResult sendEnrolledEmailToManager(Identity enrolledIdentity, Project project, Translator pT) {
		return sendEmailToGroup(project.getProjectLeaderGroup(), enrolledIdentity, project, 
				                    pT.translate(KEY_ENROLLED_EMAIL_TO_MANAGER_SUBJECT), 
				                    pT.translate(KEY_ENROLLED_EMAIL_TO_MANAGER_BODY), pT.getLocale());
	}

	// For cancel enrollment
	public MailerResult sendCancelEnrollmentEmailToParticipant(Identity enrolledIdentity, Project project, Translator pT) {
		return sendEmail(enrolledIdentity, project, 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	public MailerResult sendCancelEnrollmentEmailToManager(Identity enrolledIdentity, Project project, Translator pT) {
		return sendEmailToGroup(project.getProjectLeaderGroup(), enrolledIdentity, project, 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_SUBJECT), 
        pT.translate(KEY_CANCEL_ENROLLMENT_EMAIL_TO_MANAGER_BODY), pT.getLocale());
	}

	// Project change
	public MailerResult sendProjectChangedEmailToParticipants(Identity changer, Project project, Translator pT) {
		return sendEmailProjectChanged(project.getProjectParticipantGroup(), changer, project, 
        pT.translate(KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_PROJECT_CHANGED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	public MailerResult sendProjectDeletedEmailToParticipants(Identity changer, Project project, Translator pT) {
		return sendEmailProjectChanged(project.getProjectParticipantGroup(), changer, project, 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}
	
	public MailerResult sendProjectDeletedEmailToManager(Identity changer, Project project, Translator pT) {
		return sendEmailProjectChanged(project.getProjectLeaderGroup(), changer, project, 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
	}

	public MailerResult sendProjectDeletedEmailToAccountManagers(Identity changer, Project project, CourseEnvironment courseEnv, CourseNode node, Translator pT){
		Long groupKey = null;
		Property accountManagerGroupProperty = courseEnv.getCoursePropertyManager().findCourseNodeProperty(node, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		// Check if account-manager-group-key-property already exist
		if (accountManagerGroupProperty != null) {
			groupKey = accountManagerGroupProperty.getLongValue();
		} 
		if (groupKey != null) {
			BusinessGroup		accountManagerGroup = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(groupKey, false);
			return sendEmailProjectChanged(accountManagerGroup.getPartipiciantGroup(), changer, project, 
	        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_SUBJECT), 
	        pT.translate(KEY_PROJECT_DELETED_EMAIL_TO_PARTICIPANT_BODY), pT.getLocale());
    }
	  
		return null;
	}
	
	public MailTemplate createRemoveAsCandiadateMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_REMOVE_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_REMOVE_CANDIDATE_EMAIL_BODY), pT.getLocale());
	}

	public MailTemplate createAcceptCandiadateMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_ACCEPT_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_ACCEPT_CANDIDATE_EMAIL_BODY), pT.getLocale());
	}

	public MailTemplate createAddCandidateMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_ADD_CANDIDATE_EMAIL_SUBJECT), pT.translate(KEY_ADD_CANDIDATE_EMAIL_BODY), pT.getLocale());
	}

	public MailTemplate createAddParticipantMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_ADD_PARTICIPANT_EMAIL_SUBJECT), pT.translate(KEY_ADD_PARTICIPANT_EMAIL_BODY), pT.getLocale());
	}

	public MailTemplate createRemoveParticipantMailTemplate(Project project, Identity projectManager, Translator pT) {
		return createProjectChangeMailTemplate( project, projectManager, pT.translate(KEY_REMOVE_PARTICIPANT_EMAIL_SUBJECT), pT.translate(KEY_REMOVE_PARTICIPANT_EMAIL_BODY), pT.getLocale());
	}

	//////////////////
  // Private Methods	
  //////////////////
	private MailerResult sendEmail(Identity enrolledIdentity, Project project, String subject, String body, Locale locale) {
		MailTemplate enrolledMailTemplate = this.createMailTemplate(project, enrolledIdentity, subject, body, locale );
		// TODO: cg/12.01.2010 in der Methode sendMailUsingTemplateContext wurden die Variablen nicht ersetzt (Fehler oder falsch angewendet?) 
		//       als Workaround wurde die Methode sendMailAsSeparateMails verwendet
		List<Identity> enrolledIdentityList = new ArrayList<Identity>();
		enrolledIdentityList.add(enrolledIdentity);
		MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(enrolledIdentityList, null, null, enrolledMailTemplate, null);
		log.audit("ProjectBroker: sendEmail to identity.name=" + enrolledIdentity.getName() + " , mailerResult.returnCode=" + mailerResult.getReturnCode());
		return mailerResult;
	}

	private MailerResult sendEmailToGroup(SecurityGroup group, Identity enrolledIdentity, Project project, String subject, String body, Locale locale) {
		MailTemplate enrolledMailTemplate = this.createMailTemplate(project, enrolledIdentity, subject, body, locale );
		// loop over all project manger
		List<Identity> projectManagerList = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(group);
		StringBuilder identityNames = new StringBuilder();
		for (Identity identity : projectManagerList) {
			if (identityNames.length()>0) identityNames.append(",");
			identityNames.append(identity.getName());
		}
		MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(projectManagerList, null, null, enrolledMailTemplate, null);
		log.audit("ProjectBroker: sendEmailToGroup: identities=" + identityNames.toString() + " , mailerResult.returnCode=" + mailerResult.getReturnCode());
		return mailerResult;
	}

	private MailerResult sendEmailProjectChanged(SecurityGroup group, Identity changer, Project project, String subject, String body, Locale locale) {
		MailTemplate enrolledMailTemplate = this.createProjectChangeMailTemplate(project, changer, subject, body, locale );
		// loop over all project manger
		List<Identity> projectManagerList = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(group);
		StringBuilder identityNames = new StringBuilder();
		for (Identity identity : projectManagerList) {
			if (identityNames.length()>0) identityNames.append(",");
			identityNames.append(identity.getName());
		}
		MailerResult mailerResult = MailerWithTemplate.getInstance().sendMailAsSeparateMails(projectManagerList, null, null, enrolledMailTemplate, null);
		log.audit("ProjectBroker: sendEmailToGroup: identities=" + identityNames.toString() + " , mailerResult.returnCode=" + mailerResult.getReturnCode());
		return mailerResult;
	}

	/**
	 * Create default template which fill in context 'firstname' , 'lastname' and 'username'.
	 * @param subject
	 * @param body
	 * @return
	 */
	private MailTemplate createMailTemplate(Project project, Identity enrolledIdentity, String subject, String body, Locale locale) {	
		final String projectTitle = project.getTitle();
		final String currentDate  = Formatter.getInstance(locale).formatDateAndTime(new Date());
		final String firstNameEnrolledIdentity = enrolledIdentity.getUser().getProperty(UserConstants.FIRSTNAME, null);
		final String lastnameEnrolledIdentity  = enrolledIdentity.getUser().getProperty(UserConstants.LASTNAME, null);
		final String usernameEnrolledIdentity  = enrolledIdentity.getName();
			
		return new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				context.put("enrolled_identity_firstname", firstNameEnrolledIdentity);
				context.put("enrolled_identity_lastname",  lastnameEnrolledIdentity);
				context.put("enrolled_identity_username",  usernameEnrolledIdentity);
				// Put variables from greater context
				context.put("projectTitle", projectTitle);
				context.put("currentDate", currentDate);
			}
		};
	}

	/**
	 * Create default template which fill in context 'firstname' , 'lastname' and 'username'.
	 * @param subject
	 * @param body
	 * @return
	 */
	private MailTemplate createProjectChangeMailTemplate(Project project, Identity changer, String subject, String body, Locale locale) {	
		final String projectTitle = project.getTitle();
		final String currentDate  = Formatter.getInstance(locale).formatDateAndTime(new Date());
		final String firstnameProjectManager = changer.getUser().getProperty(UserConstants.FIRSTNAME, null);
		final String lastnameProjectManager  = changer.getUser().getProperty(UserConstants.LASTNAME, null);
		final String usernameProjectManager  = changer.getName();
			
		return new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// Put variables from greater context
				context.put("projectTitle", projectTitle);
				context.put("currentDate", currentDate);
				context.put("firstnameProjectManager", firstnameProjectManager);
				context.put("lastnameProjectManager", lastnameProjectManager);
				context.put("usernameProjectManager", usernameProjectManager);
			}
		};
	}

	

}
