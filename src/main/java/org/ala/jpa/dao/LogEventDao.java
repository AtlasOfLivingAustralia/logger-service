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

import java.util.Collection;
import org.ala.jpa.entity.LogDetail;
import org.ala.jpa.entity.LogEvent;

public interface LogEventDao {

    /**
     * Find LogEvent by id.
     */
    public LogEvent findLogEventById(int id);

    /**
     * Find LogEvents.
     */
    public Collection<LogEvent> findLogEvents();

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
        
    public Collection<Object[]> executeNativeQuery(String sql);
        
    public Collection<Object[]> getEventsDownloadsCount(int log_event_type_id, String entity_uid, String dateFrom, String dateTo);

}