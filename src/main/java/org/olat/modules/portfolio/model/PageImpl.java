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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.Section;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfpage")
@Table(name="o_pf_page")
public class PageImpl implements Persistable, ModifiedInfo, CreateInfo, Page {
	
	private static final long serialVersionUID = -3846753221071930063L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	/** Only used for order by */
	@GeneratedValue
	@Column(name="pos")
	private long pos;
	
	@Column(name="p_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="p_summary", nullable=true, insertable=true, updatable=true)
	private String summary;
	@Column(name="p_status", nullable=true, insertable=true, updatable=true)
	private String status;
	
	@Column(name="p_version", nullable=true, insertable=true, updatable=true)
	private int version;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_initial_publish_date", nullable=true, insertable=true, updatable=true)
	private Date initialPublicationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_last_publish_date", nullable=true, insertable=true, updatable=true)
	private Date lastPublicationDate;
	
	@ManyToOne(targetEntity=PageBodyImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_body_id", nullable=false, insertable=true, updatable=true)
	private PageBody body;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;
	
	@ManyToOne(targetEntity=SectionImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_section_id", nullable=true, insertable=true, updatable=true)
	private Section section;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}
		
	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public PageBody getBody() {
		return body;
	}

	public void setBody(PageBody body) {
		this.body = body;
	}

	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	@Override
	public int hashCode() {
		return key == null ? 8965446 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PageImpl) {
			PageImpl page = (PageImpl)obj;
			return key != null && key.equals(page.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}