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
package org.ala.jpa.dao.impl;

import org.ala.jpa.dao.LogEventDao;
import org.ala.jpa.entity.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA DAO implementation
 * 
 * @author waiman.mok@csiro.au
 */
@Repository
@Transactional(readOnly = true)
public class LogEventDaoImpl implements LogEventDao {

    protected static Logger logger = Logger.getLogger(LogEventDaoImpl.class);

    private EntityManager em = null;

    /**
     * Sets the entity manager.
     */
    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void delete(LogEvent logEvent) {
        em.remove(em.merge(logEvent));
    }

    public LogEvent findLogEventById(int id) {
        return em.find(LogEvent.class, id);
    }

    @SuppressWarnings("unchecked")
    public Map<String,String> findRemoteAddresses() {
        List<RemoteAddress> addresses = em.createQuery("from RemoteAddress").getResultList();
        Map<String, String> lookup = new HashMap<String,String>();
        for(RemoteAddress ra : addresses){
            lookup.put(ra.getIp(), ra.getHostName());
        }
        return lookup;
    }


    @SuppressWarnings("unchecked")
    public Collection<Object[]> getLogEventsByReason(String entityUid, int log_event_type_id) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT month, log_reason_type_id, number_of_events, record_count FROM event_summary_breakdown_reason_entity");
        sb.append(" WHERE log_event_type_id = ? AND entity_uid = ?");
        sb.append(" ORDER BY month desc");

        logger.debug(sb.toString());
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter(1, log_event_type_id);
        q.setParameter(2, entityUid);

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getLogEventsByEmail( String entityUid, int log_event_type_id) {

        StringBuilder sb = new StringBuilder();

        sb.append("SELECT month, user_email_category, number_of_events, record_count FROM event_summary_breakdown_email_entity");
        sb.append(" WHERE log_event_type_id = ? AND entity_uid = ?");
        sb.append(" ORDER BY month desc");

        logger.debug(sb.toString());
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter(1, log_event_type_id);
        q.setParameter(2, entityUid);

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<LogEvent> findLogEvents() {
        return em.createQuery("select p from LogEvent p order by p.id").getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<LogEvent> findLogEvents(int startIndex, int maxResults) {
        return em.createQuery("select p from LogEvent p order by p.id").setFirstResult(startIndex).setMaxResults(maxResults).getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<LogEvent> findLogEventsByEmail(String userEmail) {
        return em.createQuery("select p from LogEvent p where p.userEmail = :userEmail order by p.id").setParameter("userEmail", userEmail).getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<LogEvent> findLogEventsByUserIp(String userIp) {
        return em.createQuery("select p from LogEvent p where userIp = :userIp order by p.id").setParameter("userIp", userIp).getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getLogEventsCount(int log_event_type_id, String entity_uid, String year) {
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT month, SUM(record_count) FROM event_summary_breakdown_reason_entity");
        sb.append(" WHERE log_event_type_id = ? AND month like ? AND entity_uid = ?");
        sb.append(" GROUP BY month");

        logger.debug(sb.toString());
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter(1, log_event_type_id);
        q.setParameter(2, year + "%");
        q.setParameter(3, entity_uid);

        return q.getResultList();
    }

    private String increaseOneMonth(String date) {
        String toString = date;
        if (date != null && date.length() > 5) {
            int toYr = Integer.valueOf(date.substring(0, 4));
            int toMonth = Integer.valueOf(date.substring(4, 6)) + 1;
            if (toMonth > 12) {
                toMonth = 1;
                toYr = toYr + 1;
            }
            toString = toYr + "" + (toMonth > 9 ? "" + toMonth : "0" + toMonth);
        }
        return toString;
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getEventsDownloadsCount(int log_event_type_id, String entity_uid, String dateFrom, String dateTo) {
        logger.debug(dateFrom);
        logger.debug(dateTo);
        
        // increase one month to cover whole month range.
        String dateToModified = increaseOneMonth(dateTo);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT month, SUM(number_of_events), SUM(record_count) FROM event_summary_breakdown_reason_entity");
        sb.append(" WHERE log_event_type_id = ? AND entity_uid = ? AND month >= ? AND month < ?");
        sb.append(" GROUP BY month");

        logger.debug(sb.toString());
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter(1, log_event_type_id);
        q.setParameter(2, entity_uid);
        q.setParameter(3, dateFrom);
        q.setParameter(4, dateToModified);

        return q.getResultList();
    }

    private Integer[] toIntegerArray(Object[] numbers) {
        int noOfDownloads = 0;
        int noRecordDownloaded = 0;
        if (numbers != null && numbers.length > 0) {
            if (numbers[0] != null) {
                noOfDownloads = ((Number) numbers[0]).intValue();
            }
            if (numbers[1] != null) {
                noRecordDownloaded = ((Number) numbers[1]).intValue();
            }
        }
        return new Integer[] { noOfDownloads, noRecordDownloaded };
    }

    /**
     * @see org.ala.jpa.dao.LogEventDao#getLogEventsByEntity(java.lang.String)
     */
    public Integer[] getLogEventsByEntity(String entity_uid, int log_event_type_id) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(number_of_events) as noOfEvents, SUM(record_count) as noOfRecords from event_summary_breakdown_reason_entity");
        sb.append(" WHERE log_event_type_id = ? and entity_uid = ?");

        logger.debug(sb.toString());
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter(1, log_event_type_id);
        q.setParameter(2, entity_uid);

        Object[] numbers = (Object[]) q.getResultList().get(0);

        return toIntegerArray(numbers);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public LogEvent save(LogEvent logEvent) {
        return em.merge(logEvent);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public LogEvent saveLogDetail(int logEventId, LogDetail logDetail) {
        LogEvent logEvent = findLogEventById(logEventId);

        if (logEvent.getLogDetails().contains(logDetail)) {
            logEvent.getLogDetails().remove(logDetail);
        }

        logEvent.getLogDetails().add(logDetail);

        return save(logEvent);
    }

    // ==== LogReasonType ========
    public LogReasonType findLogReasonById(int id) {
        return em.find(LogReasonType.class, id);
    }

    @SuppressWarnings("unchecked")
    public Collection<LogReasonType> findLogReasonTypes() {
        return em.createQuery("select p from LogReasonType p order by p.id").getResultList();
    }

    public LogEventType findLogEventTypeById(int id) {
        return em.find(LogEventType.class, id);
    }

    @SuppressWarnings("unchecked")
    public Collection<LogEventType> findLogEventTypes() {
        return em.createQuery("select p from LogEventType p order by p.id").getResultList();
    }

    public LogSourceType findLogSourceTypeById(int id) {
        return em.find(LogSourceType.class, id);
    }

    @SuppressWarnings("unchecked")
    public Collection<LogSourceType> findLogSourceTypes() {
        return em.createQuery("select p from LogSourceType p order by p.id").getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getEventsReasonBreakdown(int log_event_type_id, String entity_uid, String dateFrom, String dateTo) {
        logger.debug(dateFrom);
        logger.debug(dateTo);
        
        if (!((dateFrom != null && dateTo != null) || dateFrom == null && dateTo == null)) {
            throw new IllegalArgumentException("Must supply both a dateFrom and dateTo string or neither");
        }

        StringBuilder sb = new StringBuilder();
        Query q = null;

        // Filter by entityUid if one was supplied
        if (entity_uid != null) {
            sb.append("SELECT log_reason_type_id, SUM(number_of_events), SUM(record_count) FROM event_summary_breakdown_reason_entity");
            sb.append(" WHERE log_event_type_id = ? AND entity_uid = ?");
            if (dateFrom != null && dateTo != null) {
                sb.append(" AND month >= ? AND month < ?");
            }
            sb.append(" GROUP BY log_reason_type_id");

            q = em.createNativeQuery(sb.toString());
            q.setParameter(1, log_event_type_id);
            q.setParameter(2, entity_uid);

            if (dateFrom != null && dateTo != null) {
                q.setParameter(3, dateFrom);
                q.setParameter(4, dateTo);
            }
        } else {
            sb.append("SELECT log_reason_type_id, SUM(number_of_events), SUM(record_count) from event_summary_breakdown_reason");
            sb.append(" WHERE log_event_type_id = ?");
            if (dateFrom != null && dateTo != null) {
                sb.append(" AND month >= ? AND month < ?");
            }
            sb.append(" GROUP BY log_reason_type_id");

            q = em.createNativeQuery(sb.toString());
            q.setParameter(1, log_event_type_id);

            if (dateFrom != null && dateTo != null) {
                q.setParameter(2, dateFrom);
                q.setParameter(3, dateTo);
            }
        }

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getTemporalEventsReasonBreakdown(int log_event_type_id, String entity_uid, Integer logReasonType, String dateFrom, String dateTo) {
        logger.debug(dateFrom);
        logger.debug(dateTo);

        if (!((dateFrom != null && dateTo != null) || dateFrom == null && dateTo == null)) {
            throw new IllegalArgumentException("Must supply both a dateFrom and dateTo string or neither");
        }

        StringBuilder sb = new StringBuilder();

        // Filter by entityUid if one was supplied
        sb.append("SELECT month, SUM(number_of_events), SUM(record_count) FROM event_summary_breakdown_reason_entity");
        sb.append(" WHERE log_event_type_id = :log_event_type_id");

        if(logReasonType != null){
            sb.append(" AND log_reason_type_id = :log_reason_type_id");
        }

        if(entity_uid != null){
            sb.append(" AND entity_uid = :entity_uid");
        } else {
            sb.append(" AND entity_uid like 'dr%'");
        }

        if (dateFrom != null && dateTo != null) {
            sb.append(" AND month >= :dateFrom AND month < :dateTo");
        }

        sb.append(" GROUP BY month");

        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("log_event_type_id", log_event_type_id);

        if (entity_uid != null){
            q.setParameter("entity_uid", entity_uid);
        }

        if(logReasonType != null){
            q.setParameter("log_reason_type_id", logReasonType);
        }

        if (dateFrom != null && dateTo != null) {
            q.setParameter("dateFrom", dateFrom);
            q.setParameter("dateTo", dateTo);
        }

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getEventsEmailBreakdown(int log_event_type_id, String entity_uid, String dateFrom, String dateTo) {
        if (!((dateFrom != null && dateTo != null) || dateFrom == null && dateTo == null)) {
            throw new IllegalArgumentException("Must supply both a dateFrom and dateTo string or neither");
        }

        StringBuilder sb = new StringBuilder();
        Query q;

        // Filter by entityUid if one was supplied
        if (entity_uid != null) {
            sb.append("SELECT user_email_category, SUM(number_of_events), SUM(record_count) from event_summary_breakdown_email_entity");
            sb.append(" WHERE log_event_type_id = ? AND entity_uid = ?");
            if (dateFrom != null && dateTo != null) {
                sb.append(" AND month >= ? AND month < ?");
            }
            sb.append(" GROUP BY user_email_category;");

            q = em.createNativeQuery(sb.toString());
            q.setParameter(1, log_event_type_id);
            q.setParameter(2, entity_uid);

            if (dateFrom != null && dateTo != null) {
                q.setParameter(3, dateFrom);
                q.setParameter(4, dateTo);
            }
        } else {
            sb.append("SELECT user_email_category, SUM(number_of_events), SUM(record_count) from event_summary_breakdown_email");
            sb.append(" WHERE log_event_type_id = ? ");
            if (dateFrom != null && dateTo != null) {
                sb.append(" AND month >= ? AND month < ?");
            }
            sb.append(" GROUP BY user_email_category;");

            q = em.createNativeQuery(sb.toString());
            q.setParameter(1, log_event_type_id);

            if (dateFrom != null && dateTo != null) {
                q.setParameter(2, dateFrom);
                q.setParameter(3, dateTo);
            }
        }

        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    public Collection<Object[]> getTotalsByEventType() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT log_event_type_id, SUM(number_of_events), SUM(record_count) from event_summary_totals");
        sb.append(" GROUP BY log_event_type_id");
        Query q = em.createNativeQuery(sb.toString());
        return q.getResultList();
    }
}