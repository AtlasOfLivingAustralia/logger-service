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

package org.ala.jpa.dao;

import org.ala.jpa.entity.*;

import java.util.Collection;
import java.util.Map;

public interface LogEventDao {

    /**
     * Find LogEvent by id.
     */
    public LogEvent findLogEventById(int id);

    /**
     * Find LogEvent using a start index and max number of results.
     */
    public Collection<LogEvent> findLogEvents(final int startIndex, final int maxResults);

    /**
     * Find LogEvents by userEmail.
     */
    public Collection<LogEvent> findLogEventsByEmail(String userEmail);

    /**
     * Find LogEvents by userIp.
     */
    public Collection<LogEvent> findLogEventsByUserIp(String userIp);
    
    /**
     * Saves LogEvent.
     */
    public LogEvent save(LogEvent logEvent);

    /**
     * Deletes LogEvent.
     */
    public void delete(LogEvent logEvent);

    /**
     * Saves logDetail to LogEvent by adding or updating record.
     */
    public LogEvent saveLogDetail(int logEventId, LogDetail logDetail);

    /**
     * get monthly download count
     */
    public Collection<Object[]> getLogEventsCount(int log_event_type_id, String entity_uid, String year);

    /**
     * execute SQL statement
     */
    public Integer[] getLogEventsByEntity(String entity_uid, int log_event_type_id);

    /**
     * Log events by reason ordered by month
     *
     * @param entity_uid
     * @param log_event_type_id
     * @return
     */
    public Collection<Object[]> getLogEventsByReason(String entity_uid, int log_event_type_id);

    /**
     * Log event counts by email category ordered by month
     * @param entity_uid
     * @param log_event_type_id
     * @return
     */
    public Collection<Object[]> getLogEventsByEmail(String entity_uid, int log_event_type_id);
    
    /**
     * Returns the number of log events and total number of records involved in these log events between the supplied dates
     * for the supplied log event type and entity id
     * @param log_event_type_id the log event type id
     * @param entity_uid the entity uid
     * @param dateFrom the start date - a month in yyyyMM format
     * @param dateTo the end date - a month in yyyyMM format
     * @return the number of log events and total number of the records involved in these events for the supplied event type and uid, between the
     * starting month inclusive and the ending month EXCLUSIVE
     */
    public Collection<Object[]> getEventsDownloadsCount(int log_event_type_id, String entity_uid, String dateFrom, String dateTo);
    
    /**
     * Returns the number of log events and total number of records involved in these log events between the supplied dates
     * for the supplied log event type and entity id. This information is grouped by the log event reason, identified by the log reason type id.
     * @param log_event_type_id the log event type id
     * @param entity_uid the entity uid
     * @param dateFrom the start date - a month in yyyyMM format
     * @param dateTo the end date - a month in yyyyMM format
     * @return the number of log events and total number of the records involved in these events for the supplied event type and uid, between the
     * starting month inclusive and the ending month EXCLUSIVE, grouped by log reason type id. The value -1 is used to indicate the absence of a 
     * reason recorded for the log event.
     */
    public Collection<Object[]> getEventsReasonBreakdown(int log_event_type_id, String entity_uid, String dateFrom, String dateTo);
    
    /**
     * Returns the number of log events and total number of records involved in these log events between the supplied dates
     * for the supplied log event type and entity id. This information is grouped into the following categories based on the email addresses associated
     * with the log events - "edu", "gov", "other" and "unspecified"
     * @param log_event_type_id the log event type id
     * @param entity_uid the entity uid
     * @param dateFrom the start date - a month in yyyyMM format
     * @param dateTo the end date - a month in yyyyMM format
     * @return the number of log events and total number of the records involved in these events for the supplied event type and uid, between the
     * starting month inclusive and the ending month EXCLUSIVE, grouped into the email categories described above.
     */
    public Collection<Object[]> getEventsEmailBreakdown(int log_event_type_id, String entity_uid, String dateFrom, String dateTo);

    /**
     * Returns a breakdown by month for events reason
     *
     * @param log_event_type_id
     * @param entity_uid
     * @param logReasonType if null, will return all events
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public Collection<Object[]> getTemporalEventsReasonBreakdown(int log_event_type_id, String entity_uid, Integer logReasonType, String dateFrom, String dateTo);

    public Collection<Object[]> getTotalsByEventType();
 
    //==== LogReasonType ========
    public Collection<LogReasonType> findLogReasonTypes();
    public LogReasonType findLogReasonById(int id);
    public LogSourceType findLogSourceTypeById(int id);
    public Collection<LogSourceType> findLogSourceTypes();
    public LogEventType findLogEventTypeById(int id);
    public Collection<LogEventType> findLogEventTypes();


    /**
     * Retrieve a list of allowed sources.
     *
     * @return key, value list of remote addresses.
     */
    public Map<String,String> findRemoteAddresses();
}