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

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the log_event_type database table.
 * 
 * @author waiman.mok@csiro.au
 * 
 */
@Entity
@Table(name="log_source_type")
public class LogSourceType extends PersistentEntity implements Serializable {
	private static final long serialVersionUID = -1126253054708678533L;

	@Id
	private int id;

	private String name;

    public LogSourceType() {
    }
    
    public LogSourceType(int id, String name) {
    	this.id = id;
    	this.name = name;
    }    

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
}