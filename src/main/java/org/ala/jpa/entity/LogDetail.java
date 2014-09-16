/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.jpa.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the log_detail database table.
 * 
 * @author waiman.mok@csiro.au
 */
@Entity
@Table(name="log_detail")
public class LogDetail extends PersistentEntity implements Serializable {
	private static final long serialVersionUID = 5987590796892640832L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	@Column(name="entity_type")
	private String entityType;

	@Column(name="entity_uid")
	private String entityUid;

	@Column(name="record_count")
	private int recordCount;
	
    public LogDetail() {}
    
    public LogDetail(String entityType, String entityUid, int recordCount) {
    	this.entityType = entityType;
    	this.entityUid = entityUid;
    	this.recordCount = recordCount;
    }

	public int getId() {
		return this.id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public String getEntityType() {
		return this.entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getEntityUid() {
		return this.entityUid;
	}

	public void setEntityUid(String entityUid) {
		this.entityUid = entityUid;
	}

	public int getRecordCount() {
		return this.recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}	
}