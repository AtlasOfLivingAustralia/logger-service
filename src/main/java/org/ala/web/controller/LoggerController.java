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
package org.ala.web.controller;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.client.model.LogEventVO;
import org.ala.jpa.dao.LogEventDao;
import org.ala.jpa.entity.LogEvent;
import org.ala.jpa.entity.LogEventType;
import org.ala.jpa.entity.LogReasonType;
import org.ala.jpa.entity.LogSourceType;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

enum SummaryType {
    EVENT, DOWNLOAD;
}

/**
 * Main Controller for ALA-LOGGER
 * 
 * @author waiman.mok@csiro.au
 */
@Controller
public class LoggerController {
    @Autowired
    private LogEventDao logEventDao;

    private Map remoteSourceAddress;

    private static final String JSON_VIEW_NAME = "jsonView";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    protected static Logger logger = Logger.getLogger(LoggerController.class);

    protected static String[] EMAIL_CATEGORIES = { "edu", "gov", "other", "unspecified" };

    /**
     * 
     * URL: http://152.83.198.112:8080/ala-logger/service/logger/1 METHOD: "GET"
     * 
     * @param id
     *            log_event_id
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/logger/{id}")
    public ModelAndView getLogEvent(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
        LogEvent logEvent = null;

        // check user
        if (!checkRemoteAddress(request)) {
            return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());
        }

        try {
            logEvent = logEventDao.findLogEventById(id);
            if (logEvent == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            return this.createErrorResponse(response, HttpStatus.NOT_FOUND.value());
        }
        return new ModelAndView(JSON_VIEW_NAME, "logEvent", logEvent);
    }

    /**
     * URL:
     * http://152.83.198.112:8080/ala-logger/service/logger/get.json?q=dp123
     * &eventTypeId=12345&year=2010 METHOD: "GET"
     * 
     * Output json format: {"months":[["201007",64],["201008",64]]}
     * 
     * @param get
     * @param q
     * @param eventTypeId
     * @param monthly
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/logger/{get}.json", method = RequestMethod.GET)
    public ModelAndView listStatusJson(@PathVariable("get") String get, @RequestParam(value = "q", required = true) String q, @RequestParam(value = "eventTypeId", required = true) int eventTypeId,
            @RequestParam(value = "year", required = false, defaultValue = "") String year, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Collection<Object[]> l = null;

        // check user
        if (!checkRemoteAddress(request)) {
            return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());
        }

        if ("get".compareToIgnoreCase(get) != 0) {
            return this.createErrorResponse(response, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
        }

        logger.debug("**** get: " + get + " q: " + q + " eventTypeId: " + eventTypeId + " year: " + year);
        if ("".equals(year) || year == null) {
            l = logEventDao.getLogEventsCount(eventTypeId, q, "" + (Calendar.getInstance().get(Calendar.YEAR)));
        } else {
            l = logEventDao.getLogEventsCount(eventTypeId, q, year);
        }
        return new ModelAndView(JSON_VIEW_NAME, "months", l);
    }

    /**
     * URL: http://152.83.198.112:8080/ala-logger/service/logger/ METHOD: "POST"
     * 
     * Expected json input format: { "eventTypeId": 12345, "comment":
     * "For doing some research with..", "userEmail" : "David.Martin@csiro.au",
     * "userIP": "123.123.123.123", "recordCounts" : { "dp123": 32, "dr143": 22,
     * "ins322": 55 } }
     * 
     * @param body
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/logger")
    public ModelAndView addLogEvent(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
        LogEvent logEvent = null;
        LogEventVO logEventVO = null;

        // check user
        if (!checkRemoteAddress(request)) {
            return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // read the existing value
        try {
            logEventVO = mapper.readValue(body, LogEventVO.class);
            if (logEventVO != null) {
                // validate logEventType & LogReasonType & LogSourceType
                // mysql doesn't support field check constraint
                // eg:
                // "alter table log_event add CHECK ( log_reason_type_id IN (select id from log_reason_type UNION select NULL));"
                LogEventType type = logEventDao.findLogEventTypeById(logEventVO.getEventTypeId());
                if (type == null) {
                    throw new NoSuchFieldException();
                }
                LogReasonType rtype = null;
                if (logEventVO.getReasonTypeId() != null) {
                    rtype = logEventDao.findLogReasonById(logEventVO.getReasonTypeId());
                    if (rtype == null) {
                        throw new NoSuchFieldException();
                    }
                }
                LogSourceType stype = null;
                if (logEventVO.getSourceTypeId() != null) {
                    stype = logEventDao.findLogSourceTypeById(logEventVO.getSourceTypeId());
                    if (stype == null) {
                        throw new NoSuchFieldException();
                    }
                }

                String realIp = request.getHeader(X_FORWARDED_FOR);
                if (realIp == null || "".equals(realIp)) {
                    realIp = request.getRemoteAddr();
                }

                // populate vo
                if (logEventVO.getMonth() == null || (logEventVO.getMonth() != null && logEventVO.getMonth().length() < 4)) {
                    logEvent = new LogEvent((String) remoteSourceAddress.get(realIp.trim()), type.getId(), rtype == null ? null : rtype.getId(), stype == null ? null : stype.getId(),
                            logEventVO.getUserEmail(), logEventVO.getUserIP(), logEventVO.getComment(), logEventVO.getRecordCounts());
                } else {
                    logEvent = new LogEvent((String) remoteSourceAddress.get(realIp.trim()), type.getId(), rtype == null ? null : rtype.getId(), stype == null ? null : stype.getId(),
                            logEventVO.getUserEmail(), logEventVO.getUserIP(), logEventVO.getComment(), logEventVO.getMonth(), logEventVO.getRecordCounts());
                }
                logEvent = logEventDao.save(logEvent);
            }

        } catch (Exception e) {
            logger.error("Invalid LogEventType or LogReasonType or Id, \n JSON request: \n" + body, e);
            return this.createErrorResponse(response, HttpStatus.NOT_ACCEPTABLE.value());
        }
        return new ModelAndView(JSON_VIEW_NAME, "logEvent", logEvent);
    }

    /*
     * @RequestMapping(method=RequestMethod.GET,
     * value={"/{entityUid}/{eventType}/counts.json",
     * "/{entityUid}/{eventType}/counts"}) public ModelAndView
     * getLogEventCounts(
     * 
     * @PathVariable("entityUid") String entityUid,
     * 
     * @PathVariable("eventType") Integer eventType){
     * 
     * //return createDownloadSummary(entityUid, eventType); return
     * createSummary(entityUid, eventType, SummaryType.DOWNLOAD); }
     * 
     * @RequestMapping(method=RequestMethod.GET,
     * value={"/{entityUid}/downloads/counts.json",
     * "/{entityUid}/downloads/counts"}) public ModelAndView getLogEventCounts(
     * 
     * @PathVariable("entityUid") String entityUid){ //return
     * createDownloadSummary(entityUid, 1002); return createSummary(entityUid,
     * 1002, SummaryType.DOWNLOAD); }
     */
    /**
     * Create a summary for event downloads
     * 
     * @param entityUid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = { "/{entityUid}/events/{eventId}/counts.json", "/{entityUid}/events/{eventId}/counts" })
    public ModelAndView getLogEventCounts(@PathVariable("entityUid") String entityUid, @PathVariable("eventId") int eventId) {
        // return createEventSummary(entityUid, eventId);
        return createSummary(entityUid, eventId, SummaryType.EVENT);
    }

    private ModelAndView createSummary(String entityUid, Integer eventType, SummaryType summary) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

        // all
        Integer[] all = logEventDao.getLogEventsByEntity(entityUid, eventType);

        // search for events up to the beginning of the next month
        Calendar toCal = Calendar.getInstance();
        toCal.set(Calendar.DAY_OF_MONTH, 1);
        toCal.add(Calendar.MONTH, 1);
        Calendar fromCal = Calendar.getInstance();
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        fromCal.add(Calendar.MONTH, 1);
        fromCal.add(Calendar.YEAR, -1);
        
        Collection<Object[]> thisYear = null;
        ModelAndView mav = null;
        thisYear = logEventDao.getEventsDownloadsCount(eventType, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));

        mav = new ModelAndView(JSON_VIEW_NAME, "all", createMapForJson(all, summary));
        mav.addObject("lastYear", createTotalsMapForJson(thisYear));
        mav.addObject("last3Months", createMapForJson(thisYear, new int[] { (toCal.get(Calendar.MONTH)), (toCal.get(Calendar.MONTH) - 1), (toCal.get(Calendar.MONTH) - 2) }, summary));
        mav.addObject("thisMonth", createMapForJson(thisYear, new int[] { (toCal.get(Calendar.MONTH)) }, summary));

        return mav;
    }
    
    private Map<String, Integer> createTotalsMapForJson(Collection<Object[]> l) {
        Map<String, Integer> noDownloadsAndCount = new HashMap<String, Integer>();
        
        int events = 0;
        int items = 0;
        
        if (l != null) {
            for(Object[] row: l) {
                events += ((BigDecimal) row[1]).toBigIntegerExact().intValue();
                items += ((BigDecimal) row[2]).toBigIntegerExact().intValue();
            }
        }
        
        noDownloadsAndCount.put("numberOfEvents", events);
        noDownloadsAndCount.put("numberOfEventItems", items);
        
        return noDownloadsAndCount;
    }

    private Map<String, Integer> createMapForJson(Integer[] value, SummaryType type) {
        Map<String, Integer> noDownloadsAndCount = new LinkedHashMap<String, Integer>();
        if (value != null && value.length > 1) {
            if (type == SummaryType.EVENT) {
                noDownloadsAndCount.put("numberOfEvents", value[0]);
                noDownloadsAndCount.put("numberOfEventItems", value[1]);
            } else {
                noDownloadsAndCount.put("numberOfDownloads", value[0]);
                noDownloadsAndCount.put("numberOfDownloadedRecords", value[1]);
            }
        }
        return noDownloadsAndCount;
    }

    private Map<String, Integer> createMapForJson(Collection<Object[]> l, int[] month, SummaryType type) {
        Map<String, Integer> noDownloadsAndCount = new HashMap<String, Integer>();
        int events = 0;
        int items = 0;
        if (l != null && month != null && type != null) {
            for (int i = 0; i < month.length; i++) {
                Object[] value = getMonthValue(l, month[i]);
                if (value != null && value.length > 2) {
                    events += Integer.parseInt(value[SummaryType.EVENT.ordinal() + 1].toString());
                    items += Integer.parseInt(value[SummaryType.DOWNLOAD.ordinal() + 1].toString());
                }
            }
            noDownloadsAndCount = createMapForJson(new Integer[] { events, items }, type);
        }
        return noDownloadsAndCount;
    }

    private Object[] getMonthValue(Collection<Object[]> l, int mth) {
        Object[] value = null;
        if (l != null) {
            Iterator<Object[]> itr = l.iterator();
            while (itr.hasNext()) {
                Object[] o = itr.next();
                if (o[0] != null && o[0].toString().length() > 5) {
                    int month = Integer.valueOf(o[0].toString().substring(4, 6));
                    if (mth == month) {
                        value = o;
                        if (value[1] == null || value[1].toString().length() == 0) {
                            value[1] = 0;
                        }
                        break;
                    }
                }
            }
        }
        return value;
    }

    /**
     * inject a map of remote user IP for security.
     * 
     * @param remoteSourceAddress
     */
    @Autowired
    public void setRemoteSourceAddress(MapFactoryBean remoteSourceAddress) {
        if (this.remoteSourceAddress == null) {
            try {
                this.remoteSourceAddress = (Map) remoteSourceAddress.getObject();
            } catch (Exception e) {
                logger.error(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * security check
     * 
     * @param request
     * @return
     */
    private boolean checkRemoteAddress(HttpServletRequest request) {
        String realIp = request.getHeader(X_FORWARDED_FOR);
        if (realIp == null || "".equals(realIp)) {
            realIp = request.getRemoteAddr();
        }

        if (remoteSourceAddress != null && realIp != null) {
            logger.debug("***** request.getRemoteAddr(): " + request.getRemoteAddr() + " request.getRemoteHost(): " + request.getRemoteHost() + " , realIp: " + request.getHeader(X_FORWARDED_FOR));
            if (remoteSourceAddress.containsKey(realIp.trim())) {
                return true;
            }
        }
        return false;
    }

    private ModelAndView createErrorResponse(HttpServletResponse response, int statusCode) {
        response.setStatus(statusCode);
        ModelMap model = new ModelMap();
        return new ModelAndView(JSON_VIEW_NAME, model);
    }

    /**
     * inject a list of remote user IP for security.
     * 
     * <bean id="remoteAddress"
     * class="org.springframework.beans.factory.config.ListFactoryBean">
     * <property name="sourceList"> <list> <value>bie.ala.org.au</value>
     * <value>152.83.198.112</value> <value>150.229.66.87</value>
     * <value>127.0.0.1</value> <value>152.83.198.139</value> </list>
     * </property> </bean>
     */
    /*
     * private List<String> remoteAddress;
     * 
     * @Autowired public void setRemoteAddress(ListFactoryBean remoteAddress) {
     * if(this.remoteAddress == null) { try{ this.remoteAddress =
     * (List<String>)remoteAddress.getObject(); }catch(Exception e) {
     * e.printStackTrace(); } } }
     * 
     * public List<String> getRemoteAddress() { return remoteAddress; }
     */

    @RequestMapping(method = RequestMethod.GET, value = "/logger/reasons", headers = "Accept=application/json")
    public @ResponseBody
    Collection<LogReasonType> getLogReasons(HttpServletRequest request, HttpServletResponse response) {
        Collection<LogReasonType> types = null;

        try {
            types = logEventDao.findLogReasonTypes();
        } catch (Exception e) {
            return new ArrayList<LogReasonType>();
        }
        return types;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/logger/sources", headers = "Accept=application/json")
    public @ResponseBody
    Collection<LogSourceType> loadLogReasonType(HttpServletRequest request, HttpServletResponse response) {
        Collection<LogSourceType> types = null;

        try {
            types = logEventDao.findLogSourceTypes();
        } catch (Exception e) {
            return new ArrayList<LogSourceType>();
        }
        return types;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/logger/events", headers = "Accept=application/json")
    public @ResponseBody
    Collection<LogEventType> loadLogEventType(HttpServletRequest request, HttpServletResponse response) {
        Collection<LogEventType> types = null;

        try {
            types = logEventDao.findLogEventTypes();
        } catch (Exception e) {
            return new ArrayList<LogEventType>();
        }
        return types;
    }

    /**
     * Create a summary for event downloads with breakdown by reason for
     * download
     * 
     * @param entityUid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = { "/reasonBreakdown", "/reasonBreakdown.json" })
    public ModelAndView getReasonBreakdown(@RequestParam(value = "entityUid", required = false) String entityUid, @RequestParam(value = "eventId", required = true) int eventId) {

        Collection<LogReasonType> logReasonTypes = logEventDao.findLogReasonTypes();
        Map<Integer, String> logReasonTypesMap = new HashMap<Integer, String>();
        for (LogReasonType reasonType : logReasonTypes) {
            logReasonTypesMap.put(reasonType.getId(), reasonType.getName());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();

        // search for events up to the beginning of the next month
        toCal.set(Calendar.DAY_OF_MONTH, 1);
        toCal.add(Calendar.MONTH, 1);
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        fromCal.add(Calendar.MONTH, 1);

        // this month
        fromCal.add(Calendar.MONTH, -1);
        Collection<Object[]> thisMonthRawData = logEventDao.getEventsReasonBreakdown(eventId, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));
        Map<String, Object> thisMonthReasonBreakdown = buildReasonBreakdownMap(thisMonthRawData, logReasonTypesMap);

        // last 3 months
        fromCal.add(Calendar.MONTH, -2);
        Collection<Object[]> last3MonthsRawData = logEventDao.getEventsReasonBreakdown(eventId, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));
        Map<String, Object> last3MonthsReasonBreakdown = buildReasonBreakdownMap(last3MonthsRawData, logReasonTypesMap);

        // last year
        fromCal = Calendar.getInstance();
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        fromCal.add(Calendar.MONTH, 1);
        fromCal.add(Calendar.YEAR, -1);
        Collection<Object[]> lastYearRawData = logEventDao.getEventsReasonBreakdown(eventId, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));
        Map<String, Object> lastYearReasonBreakdown = buildReasonBreakdownMap(lastYearRawData, logReasonTypesMap);

        // all
        Collection<Object[]> allTimeRawData = logEventDao.getEventsReasonBreakdown(eventId, entityUid, null, null);
        Map<String, Object> allTimeReasonBreakdown = buildReasonBreakdownMap(allTimeRawData, logReasonTypesMap);

        ModelAndView mav = new ModelAndView(JSON_VIEW_NAME, "thisMonth", thisMonthReasonBreakdown);
        mav.addObject("last3Months", last3MonthsReasonBreakdown);
        mav.addObject("lastYear", lastYearReasonBreakdown);
        mav.addObject("all", allTimeReasonBreakdown);

        return mav;
    }

    private Map<String, Object> buildReasonBreakdownMap(Collection<Object[]> rawData, Map<Integer, String> logReasonTypesMap) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        Map<String, Map<String, Object>> reasonBreakdownDetailMap = new HashMap<String, Map<String, Object>>();

        BigInteger totalNumberOfEvents = BigInteger.ZERO;
        BigInteger totalNumberOfEventItems = BigInteger.ZERO;

        for (Object[] dataElement : rawData) {
            Integer reasonTypeId = (Integer) dataElement[0];
            BigInteger numberOfEvents = ((BigDecimal) dataElement[1]).toBigIntegerExact();
            BigInteger numberOfEventItems = ((BigDecimal) dataElement[2]).toBigIntegerExact();

            String reasonName;
            if (reasonTypeId == -1) {
                reasonName = "unclassified";
            } else {
                reasonName = logReasonTypesMap.get(reasonTypeId);
            }

            Map<String, Object> reasonNumbersMap = new HashMap<String, Object>();
            reasonNumbersMap.put("events", numberOfEvents);
            reasonNumbersMap.put("records", numberOfEventItems);

            reasonBreakdownDetailMap.put(reasonName, reasonNumbersMap);

            totalNumberOfEvents = totalNumberOfEvents.add(numberOfEvents);
            totalNumberOfEventItems = totalNumberOfEventItems.add(numberOfEventItems);
        }

        // need to add in zero values for any reasons for which there are no
        // recorded events in the time period
        for (String reasonName : logReasonTypesMap.values()) {
            if (!reasonBreakdownDetailMap.containsKey(reasonName)) {
                Map<String, Object> reasonNumbersMap = new HashMap<String, Object>();
                reasonNumbersMap.put("events", 0);
                reasonNumbersMap.put("records", 0);
                reasonBreakdownDetailMap.put(reasonName, reasonNumbersMap);
            }
        }

        retMap.put("events", totalNumberOfEvents);
        retMap.put("records", totalNumberOfEventItems);
        retMap.put("reasonBreakdown", reasonBreakdownDetailMap);

        return retMap;
    }

    /**
     * Create a summary for event downloads with breakdown by reason for
     * download
     * 
     * @param entityUid
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = { "/emailBreakdown", "/emailBreakdown.json" })
    public ModelAndView getEmailBreakdown(@RequestParam(value = "entityUid", required = false) String entityUid, @RequestParam(value = "eventId", required = true) int eventId) {

        Collection<LogReasonType> logReasonTypes = logEventDao.findLogReasonTypes();
        Map<Integer, String> logReasonTypesMap = new HashMap<Integer, String>();
        for (LogReasonType reasonType : logReasonTypes) {
            logReasonTypesMap.put(reasonType.getId(), reasonType.getName());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();

        // search for events up to the beginning of the next month
        toCal.set(Calendar.DAY_OF_MONTH, 1);
        toCal.add(Calendar.MONTH, 1);
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        fromCal.add(Calendar.MONTH, 1);

        // this month
        fromCal.add(Calendar.MONTH, -1);
        Collection<Object[]> thisMonthRawData = logEventDao.getEventsEmailBreakdown(eventId, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));
        Map<String, Object> thisMonthEmailBreakdown = buildEmailBreakdownMap(thisMonthRawData);

        // last 3 months
        fromCal.add(Calendar.MONTH, -2);
        Collection<Object[]> last3MonthsRawData = logEventDao.getEventsEmailBreakdown(eventId, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));
        Map<String, Object> last3MonthsEmailBreakdown = buildEmailBreakdownMap(last3MonthsRawData);

        // last year
        fromCal = Calendar.getInstance();
        fromCal.set(Calendar.DAY_OF_MONTH, 1);
        fromCal.add(Calendar.MONTH, 1);
        fromCal.add(Calendar.YEAR, -1);
        Collection<Object[]> lastYearRawData = logEventDao.getEventsEmailBreakdown(eventId, entityUid, sdf.format(fromCal.getTime()), sdf.format(toCal.getTime()));
        Map<String, Object> lastYearEmailBreakdown = buildEmailBreakdownMap(lastYearRawData);

        // all
        Collection<Object[]> allTimeRawData = logEventDao.getEventsEmailBreakdown(eventId, entityUid, null, null);
        Map<String, Object> allTimeEmailBreakdown = buildEmailBreakdownMap(allTimeRawData);

        ModelAndView mav = new ModelAndView(JSON_VIEW_NAME, "thisMonth", thisMonthEmailBreakdown);
        mav.addObject("last3Months", last3MonthsEmailBreakdown);
        mav.addObject("lastYear", lastYearEmailBreakdown);
        mav.addObject("all", allTimeEmailBreakdown);

        return mav;
    }

    private Map<String, Object> buildEmailBreakdownMap(Collection<Object[]> rawData) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        Map<String, Map<String, Object>> emailBreakdownDetailMap = new HashMap<String, Map<String, Object>>();

        BigInteger totalNumberOfEvents = BigInteger.ZERO;
        BigInteger totalNumberOfEventItems = BigInteger.ZERO;

        for (Object[] dataElement : rawData) {
            String email = (String) dataElement[0];
            BigInteger numberOfEvents = ((BigDecimal) dataElement[1]).toBigIntegerExact();
            BigInteger numberOfEventItems = ((BigDecimal) dataElement[2]).toBigIntegerExact();

            Map<String, Object> categoryNumbersMap = new HashMap<String, Object>();
            categoryNumbersMap.put("events", numberOfEvents);
            categoryNumbersMap.put("records", numberOfEventItems);

            emailBreakdownDetailMap.put(email, categoryNumbersMap);

            totalNumberOfEvents = totalNumberOfEvents.add(numberOfEvents);
            totalNumberOfEventItems = totalNumberOfEventItems.add(numberOfEventItems);
        }

        // need to add in zero values for any email categories for which there
        // are no recorded events in the time period
        for (String reasonName : EMAIL_CATEGORIES) {
            if (!emailBreakdownDetailMap.containsKey(reasonName)) {
                Map<String, Object> categoryNumbersMap = new HashMap<String, Object>();
                categoryNumbersMap.put("events", 0);
                categoryNumbersMap.put("records", 0);
                emailBreakdownDetailMap.put(reasonName, categoryNumbersMap);
            }
        }

        retMap.put("events", totalNumberOfEvents);
        retMap.put("records", totalNumberOfEventItems);
        retMap.put("emailBreakdown", emailBreakdownDetailMap);

        return retMap;
    }

    /**
     * Create a summary for log events by broken down by download
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/totalsByType")
    public ModelAndView getTotalsByEventType() {
        Collection<Object[]> totalsByType = logEventDao.getTotalsByEventType();

        Map<String, Map<String, BigDecimal>> totalsByTypeMap = new HashMap<String, Map<String, BigDecimal>>();

        for (Object[] resultRow : totalsByType) {
            int logEventTypeId = (Integer) resultRow[0];
            BigDecimal numberOfEvents = (BigDecimal) resultRow[1];
            BigDecimal numberOfRecords = (BigDecimal) resultRow[2];

            Map<String, BigDecimal> mapForRow = new HashMap<String, BigDecimal>();
            mapForRow.put("events", numberOfEvents);
            mapForRow.put("records", numberOfRecords);

            totalsByTypeMap.put(Integer.toString(logEventTypeId), mapForRow);
        }

        ModelAndView mav = new ModelAndView(JSON_VIEW_NAME, "totals", totalsByTypeMap);

        return mav;
    }
}
