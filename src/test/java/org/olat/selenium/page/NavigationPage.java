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
package org.olat.selenium.page;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.course.MyCoursesPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupsPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.CatalogAdminPage;
import org.olat.selenium.page.repository.CatalogPage;
import org.olat.selenium.page.user.PortalPage;
import org.olat.selenium.page.user.UserAdminPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page fragment which control the navigation bar with the static sites.
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NavigationPage {
	
	public static final By toolbarBackBy = By.cssSelector("li.o_breadcrumb_back>a");

	@Drone
	private WebDriver browser;
	
	private By navigationSitesBy = By.cssSelector("ul.o_navbar_sites");
	private By authoringEnvTabBy = By.cssSelector("li.o_site_author_env > a");
	private By portalBy = By.cssSelector("li.o_site_portal > a");
	private By myCoursesBy = By.cssSelector("li.o_site_repository > a");
	private By userManagementBy = By.cssSelector("li.o_site_useradmin > a");
	private By administrationBy = By.cssSelector("li.o_site_admin > a");
	private By catalogBy = By.cssSelector("li.o_site_catalog > a");
	private By catalogAdministrationBy = By.cssSelector("li.o_site_catalog_admin > a");
	private	By groupsBy = By.cssSelector("li.o_site_groups > a");
	
	public static final By myCoursesAssertBy = By.xpath("//div[contains(@class,'o_segments')]//a[contains(@onclick,'search.mycourses.student')]");
	public static final By portalAssertBy = By.className("o_portal");
	
	public NavigationPage() {
		//
	}
	
	public NavigationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public NavigationPage assertOnNavigationPage() {
		WebElement navigationSites = browser.findElement(navigationSitesBy);
		Assert.assertTrue(navigationSites.isDisplayed());
		return this;
	}
	
	public AuthoringEnvPage openAuthoringEnvironment() {
		navigate(authoringEnvTabBy);
		backToTheTop();
		OOGraphene.closeBlueMessageWindow(browser);
		return new AuthoringEnvPage(browser);
	}
	
	public PortalPage openPortal() {
		navigate(portalBy);
		WebElement main = browser.findElement(By.id("o_main"));
		return Graphene.createPageFragment(PortalPage.class, main);
	}
	
	public MyCoursesPage openMyCourses() {
		navigate(myCoursesBy);
		OOGraphene.waitElement(myCoursesAssertBy, browser);
		return new MyCoursesPage(browser);
	}
	
	public UserAdminPage openUserManagement() {
		navigate(userManagementBy);
		return UserAdminPage.getUserAdminPage(browser);
	}
	
	public AdministrationPage openAdministration() {
		navigate(administrationBy);
		return new AdministrationPage(browser);
	}
	
	public CatalogAdminPage openCatalogAdministration() {
		navigate(catalogAdministrationBy);
		return new CatalogAdminPage(browser);
	}
	
	public CatalogPage openCatalog() {
		navigate(catalogBy);
		return new CatalogPage(browser);
	}
	
	public GroupsPage openGroups(WebDriver currentBrowser) {
		navigate(groupsBy);
		return new GroupsPage(currentBrowser);
	}
	
	private void navigate(By linkBy) {
		OOGraphene.closeBlueMessageWindow(browser);
		List<WebElement> links = browser.findElements(linkBy);
		if(links.isEmpty() || !links.get(0).isDisplayed()) {
			//try to open the more menu
			openMoreMenu();
			links = browser.findElements(linkBy);
		}
		Assert.assertFalse(links.isEmpty());

		links = browser.findElements(linkBy);
		Assert.assertFalse(links.isEmpty());
		OOGraphene.waitElement(links.get(0), browser);
		links.get(0).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitingTransition(browser);
	}
	
	public void openCourse(String title) {
		By courseTab = By.xpath("//li/a[@title='" + title + "']");
		List<WebElement> courseLinks = browser.findElements(courseTab);
		if(courseLinks.isEmpty() || !courseLinks.get(0).isDisplayed()) {
			openMoreMenu();
			courseLinks = browser.findElements(courseTab);
		}
		Assert.assertFalse(courseLinks.isEmpty());
		
		courseLinks.get(0).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
	}
	
	private void openMoreMenu() {
		By openMoreBy = By.cssSelector("#o_navbar_more a.dropdown-toggle");
		List<WebElement> openMoreLinks = browser.findElements(openMoreBy);
		Assert.assertFalse(openMoreLinks.isEmpty());
		openMoreLinks.get(0).click();
	}
	
	public NavigationPage backToTheTop() {
		List<WebElement> backList = browser.findElements(toolbarBackBy);
		
		int count = 0;
		while(backList.size() > 0) {
			backList.get(count).click();
			OOGraphene.waitBusy(browser);
			backList = browser.findElements(toolbarBackBy);
			
			Assert.assertTrue(count++ < 3);
		}

		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
}
