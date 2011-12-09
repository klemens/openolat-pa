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
*/ 

package org.olat.core.util.mail;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.StringHelper;

/**
 * The ContactList is used to group e-mail addresses and name such a group. It
 * is the frameworks implementation of a MailList. The e-mail adresses are given
 * either by providing them as <code>strings</code> or in the form of
 * identites (<code>org.olat.core.id.Identity</code>). Further it is
 * possible to add all contacts from another ContactList.
 * <P>
 * TODO:pb:a remove dependency to <code>EmailPrioInstitutional</code> if a solution for core.id.user is found
 * Moreover one can specify to use the users institional e-mail first and use
 * the other e-mail as fall-back.
 * 
 * Initial Date: Sep 23, 2004
 * @author patrick
 */

public class ContactList extends LogDelegator {
	private String name;
	private String description;
	//container for addresses contributed as strings
	private Map<String, String> stringEmails = new Hashtable<String, String>();
	//container for addresses contributed as identites
	private Map<String, Identity> identiEmails = new Hashtable<String, Identity>();
	private boolean emailPrioInstitutional = false;

	/**
	 * A ContacList must have at least a name != null, matching ^[^;,:]*$
	 * 
	 * @param name
	 */
	public ContactList(String name) {
		setName(name);
		this.description = null;
	}

	/**
	 * check the priority of the institutional mail is set.
	 * 
	 * @return Returns the emailPrioInstitutional.
	 */
	public boolean isEmailPrioInstitutional() {
		return emailPrioInstitutional;
	}

	/**
	 * set the priority of the institutional mail.
	 * 
	 * @param emailPrioInstitutional The emailPrioInstitutional to set.
	 */
	public void setEmailPrioInstitutional(boolean emailPrioInstitutional) {
		this.emailPrioInstitutional = emailPrioInstitutional;
	}

	/**
	 * A ContactList must have at least a name != null, matching ^[a-zA-Z ]*$, and
	 * can have a description.
	 * 
	 * @param name
	 * @param description
	 */
	public ContactList(String name, String description) {
		setName(name);
		this.description = description;
	}

	/**
	 * contribute a contact as a string email address.
	 * 
	 * @param emailAddress
	 */
	public void add(String emailAddress) {
		stringEmails.put(keyFrom(emailAddress), emailAddress);
	}

	/**
	 * contribute a contact as an identity.
	 * 
	 * @param identity
	 */
	public void add(Identity identity) {
		String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
		//fxdiff VCRP-5
		if (email == null) {
			logError("No email available for identity::" + identity.getName() + " - can not add to contact list", null);
			return;
		}
		identiEmails.put(keyFrom(email), identity);
	}
	
	public void remove(Identity identity) {
		String email = identity.getUser().getProperty(UserConstants.EMAIL, null);
		//fxdiff VCRP-5
		if (email == null) {
			logError("No email available for identity::" + identity.getName() + " - can not remove from contact list", null);
			return;
		}
		identiEmails.remove(keyFrom(email));
	}

	/**
	 * Name getter
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * ContactList-name as String encoded according to
	 * http://www.ietf.org/rfc/rfc2047.txt
	 * 
	 * @return String encoded according to RFC2047
	 */
	public String getRFC2047Name() {
		String rfc2047name;
		// try to MIME-encode the name, if this fails, return the name un-encoded  
		try {
			rfc2047name = javax.mail.internet.MimeUtility.encodeWord(name, "UTF-8", null);
		}
		catch (java.io.UnsupportedEncodingException e) {
			logWarn("Error MIME-encoding name: " + e, e);
			rfc2047name = name;
		}

		return rfc2047name;
	}


	/**
	 * ContactList-name as String formatted according to <a href =
	 * "http://www.ietf.org/rfc/rfc2822.txt"> RFC2822 </a>
	 * 
	 * @return
	 */
	public String getRFC2822Name() {
		return getRFC2047Name() + ":";
	}

	/**
	 * ContactList-name and e-mail adresses as String formatted according to
	 * http://www.ietf.org/rfc/rfc2822.txt
	 * 
	 * @return
	 */
	public String getRFC2822NameWithAddresses() {
		return getRFC2822Name() + toString() + ";";
	}

	/**
	 * Description getter
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * the returned ArrayList contains String Objects representing the e-mail
	 * addresses added.
	 * 
	 * @return
	 */
	//fxdiff VCRP-16: intern mail system
	public List<String> getEmailsAsStrings() {
		List<String> ret = new ArrayList<String>(stringEmails.values());
		/*
		 * if priority is on institutional email get all the institutional emails
		 * first, if they are present, remove the identity from the hashtable. If
		 * they were not present, the user email is used in the next loop.
		 */
		List<Identity> copy = new ArrayList<Identity>(identiEmails.values());
		String addEmail = null;
		if (emailPrioInstitutional) {
			for (Iterator<Identity> it=copy.iterator(); it.hasNext(); ) {
				Identity tmp = it.next();
				addEmail = tmp.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
				if (addEmail != null) {
					ret.add(addEmail);
					it.remove();
				}
			}
		}
		/*
		 * loops over the (remaining) identities, fetches the user email.
		 */
		for (Identity tmp : copy){
			ret.add(tmp.getUser().getProperty(UserConstants.EMAIL, null));
		}
		return ret;
	}

	/**
	 * add members of another ContactList to this ContactList.
	 * 
	 * @param emailList
	 */
	public void add(ContactList emailList) {
		stringEmails.putAll(emailList.getStringEmails());
		identiEmails.putAll(emailList.getIdentiEmails());
	}

	/**
	 * A comma separated list of e-mail addresses. The ContactList name is
	 * ommitted, if the form
	 * ContactList.Name:member1@host.suffix,member2@host.suffix,...,memberN@host.suffix;
	 * is needed, please use the appropriate getRFC2822xxx getter method.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retVal = "";
		String sep = "";
		for (String email:getEmailsAsStrings()) {
			retVal += sep + email;
			sep = ", ";
		}
		return retVal;
	}

	/**
	 * add all identity-Objects in the provided list.
	 * 
	 * @param listOfIdentity List containing Identites
	 */
	public void addAllIdentites(List<Identity> listOfIdentity) {
		for (Identity identity:listOfIdentity) {
			add(identity);
		}
	}

	/**
	 * The e-mail addresses are generated as InternetAddresses, the priority of
	 * the institutional email is taken in account.
	 * 
	 * @return the email addresses as InternetAddresses
	 * @throws AddressException
	 */
	public InternetAddress[] getEmailsAsAddresses() throws AddressException {
		return InternetAddress.parse(toString());
	}

	public Map<String,String> getStringEmails() {
		return stringEmails;
	}

	public Map<String,Identity> getIdentiEmails() {
		return identiEmails;
	}

	private String keyFrom(String unformattedEmailAddr) {
		String key = unformattedEmailAddr.trim();
		return key.toUpperCase();
	}

	private void setName(String nameP) {
		if (!StringHelper.containsNoneOfCoDouSemi(nameP)){
			logWarn("Contact list name \"" + nameP + "\" doesn't match "+ StringHelper.ALL_WITHOUT_COMMA_2POINT_STRPNT, null);
			//replace bad chars with bad char in rfc compliant comments
			nameP = nameP.replaceAll(":","¦");
			nameP = nameP.replaceAll(";","_");
			nameP = nameP.replaceAll(",","-");
			
		}
				
		this.name = nameP;
	}

}