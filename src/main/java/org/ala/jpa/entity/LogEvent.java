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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The persistent class for the log_event database table.
 * 
 * @author waiman.mok@csiro.au
 * 
 */
@Entity
@Table(name="log_event")
public class LogEvent extends PersistentEntity implements Serializable {
	private static final long serialVersionUID = -830768533592036342L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

    @Lob()
    @Column(name="comment")
	private String comment;

    @Column(name="created")
	private Date created = new Date();

	@Column(name="month")
	private String month;

	@Column(name="user_email")
	private String userEmail;

	@Column(name="user_ip")
	private String userIp;

	@Column(name="log_event_type_id")
	private int logEventTypeId;	
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinColumn(name="log_event_id", nullable=false)
	private Set<LogDetail> logDetails;

    public LogEvent(int logEventTypeId, String userEmail, String userIp, String comment, Set<LogDetail> logDetails) {
//    	this.userId = userId;
    	this.userEmail = userEmail;
    	this.userIp = userIp;
    	this.comment = comment;
    	this.logDetails = logDetails;
    	this.logEventTypeId = logEventTypeId;
    	
    	Calendar today = Calendar.getInstance();
    	int mth = today.get(Calendar.MONTH) + 1;
    	this.month = (today.get(Calendar.YEAR) + "" + (mth > 9?""+mth: "0"+mth));    	
    }
    
    public LogEvent(int logEventTypeId, String userEmail, String userIp, String comment, Map<String, Integer> recordCounts) {
    	this(logEventTypeId, userEmail,	userIp,	comment, recordCountsToLogDetails(logEventTypeId,recordCounts));
    }    

    public LogEvent() {
    }
    
    public static Set<LogDetail> recordCountsToLogDetails(int logEventTypeId, Map<String, Integer> recordCounts){
    	Set<LogDetail> logDetails = new HashSet<LogDetail>();
    	if(recordCounts != null){
    		Set<String> keys = recordCounts.keySet();
    		Iterator<String> it = keys.iterator();
    		while(it.hasNext()){
    			String key = it.next();
	    		int val = recordCounts.get(key);
	    		logDetails.add(new LogDetail("" + logEventTypeId, key, val));
	    	}
    	}
    	return logDetails;
    }
    
	public int getId() {
		return this.id;
	}
	
	private void setId(int id) {
		this.id = id;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getMonth() {
		return this.month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserIp() {
		return this.userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public Set<LogDetail> getLogDetails() {
		return this.logDetails;
	}

	public void setLogDetails(Set<LogDetail> logDetails) {
		this.logDetails = logDetails;
	}	
	
	public int getLogEventTypeId() {
		return logEventTypeId;
	}

	public void setLogEventTypeId(int logEventTypeId) {
		this.logEventTypeId = logEventTypeId;
	}
	
/*
	@Column(name="record_count")
	private int recordCount;
	
	@Column(name="user_id")
	private String userId;	

	public int getRecordCount() {
		return this.recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}
	
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}	
*/	
}