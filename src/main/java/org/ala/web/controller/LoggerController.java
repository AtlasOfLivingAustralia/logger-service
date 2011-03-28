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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.client.model.LogEventType;
import org.ala.client.model.LogEventVO;
import org.ala.jpa.dao.LogEventDao;
import org.ala.jpa.entity.LogEvent;
import org.apache.commons.lang.time.DateUtils;
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
import org.springframework.web.servlet.ModelAndView;

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
	
	/**
	 * 
	 * URL: http://152.83.198.112:8080/ala-logger/service/logger/1
	 * METHOD: "GET"
	 * 
	 * @param id log_event_id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET, value="/logger/{id}")
	public ModelAndView getLogEvent(@PathVariable int id, HttpServletRequest request, HttpServletResponse response) {
		LogEvent logEvent = null;
		
		//check user
		if(!checkRemoteAddress(request)){
			return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());			
		}
				
		try {			
			logEvent = logEventDao.findLogEventById(id);
			if(logEvent == null){
				throw new Exception();
			}			
		} 
		catch (Exception e) {
			return this.createErrorResponse(response, HttpStatus.NOT_FOUND.value());
		} 
		return new ModelAndView(JSON_VIEW_NAME, "logEvent", logEvent);
	}
	
	/**
	 * URL: http://152.83.198.112:8080/ala-logger/service/logger/get.json?q=dp123&eventTypeId=12345&year=2010
	 * METHOD: "GET"
	 * 
	 * Output json format:
	 * {"months":[["201007",64],["201008",64]]}
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
	public ModelAndView listStatusJson(@PathVariable("get") String get,
			@RequestParam(value="q", required=true) String q,
			@RequestParam(value="eventTypeId", required=true) int eventTypeId,
			@RequestParam(value="year", required=false, defaultValue ="") String year,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Collection<Object[]> l = null;
		
		//check user
		if(!checkRemoteAddress(request)){
			return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());			
		}
		
		if("get".compareToIgnoreCase(get) != 0){
			return this.createErrorResponse(response, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
		}

		logger.debug("**** get: " + get + " q: " + q + " eventTypeId: " + eventTypeId + " year: " + year);
		if("".equals(year) || year == null){
			l = logEventDao.getLogEventsCount(eventTypeId, q, "" + (Calendar.getInstance().get(Calendar.YEAR)));			
		}
		else{
			l = logEventDao.getLogEventsCount(eventTypeId, q, year);
		}
		return new ModelAndView(JSON_VIEW_NAME, "months", l);
	}	
	
	/**
	 * URL: http://152.83.198.112:8080/ala-logger/service/logger/
	 * METHOD: "POST"
	 * 
	 * Expected json input format:
	 * {
   	 * "eventTypeId": 12345,
     * "comment": "For doing some research with..",
     * "userEmail" : "David.Martin@csiro.au",
     * "userIP": "123.123.123.123",
     * "recordCounts" : {
     * "dp123": 32,
     * "dr143": 22,
     *  "ins322": 55
   	 * }
	 * }
	 * 
	 * @param body
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/logger")
	public ModelAndView addLogEvent(@RequestBody String body, HttpServletRequest request, HttpServletResponse response){
		LogEvent logEvent = null;
		LogEventVO logEventVO = null;
		
		//check user
		if(!checkRemoteAddress(request)){
			return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
		//read the existing value
		try {
			logEventVO = mapper.readValue(body, LogEventVO.class);		
			if(logEventVO != null){
				// validate logEventType
				LogEventType type = LogEventType.getLogEventType(logEventVO.getEventTypeId());
				if(type == null){
					throw new NoSuchFieldException();
				}
				String realIp = request.getHeader(X_FORWARDED_FOR);
				if(realIp == null || "".equals(realIp)){
					realIp = request.getRemoteAddr();
				}
								
				logEvent = new LogEvent((String)remoteSourceAddress.get(realIp.trim()), type.getId(), logEventVO.getUserEmail(), 
						logEventVO.getUserIP(), logEventVO.getComment(), logEventVO.getRecordCounts());
				logEvent = logEventDao.save(logEvent);
			}
			
		} catch (Exception e) {
			logger.error("Invalid LogEvent Type or Id: " + logEventVO.getEventTypeId() + "\n JSON request: \n" +  body, e);
			return this.createErrorResponse(response, HttpStatus.NOT_ACCEPTABLE.value());
		}			
		return new ModelAndView(JSON_VIEW_NAME, "logEvent", logEvent);		
	}

    @RequestMapping(method=RequestMethod.GET, value={"/{entityUid}/{eventType}/counts.json", "/{entityUid}/{eventType}/counts"})
    public ModelAndView getLogEventCounts(
            @PathVariable("entityUid") String entityUid,
            @PathVariable("eventType") Integer eventType){
        
        return createEventSummary(entityUid, eventType);
    }

    /**
     * Create a summary for downloads
     * 
     * @param entityUid
     * @return
     */
    @RequestMapping(method=RequestMethod.GET, value={"/{entityUid}/downloads/counts.json", "/{entityUid}/downloads/counts"})
    public ModelAndView getLogEventCounts(
            @PathVariable("entityUid") String entityUid){
        return createEventSummary(entityUid, 1002);
    }
    
    /**
     * Create a summary for the supplied event type and entity UID.
     * 
     * @param entityUid
     * @param eventType
     * @return
     */
    private ModelAndView createEventSummary(String entityUid, Integer eventType) {
        //all
        Integer all = logEventDao.getLogEventsByEntity(entityUid, eventType);
        
        Date now = new Date();
        
        Date lastMonth = DateUtils.add(now, Calendar.MONTH, -1);
        Date threeMonthsAgo = DateUtils.add(now, Calendar.MONTH, -4);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        
        //last 3 months
        Integer last3Months = logEventDao.getLogEventsByEntityAndMonthRange(entityUid, eventType, sdf.format(threeMonthsAgo), sdf.format(lastMonth));
        Date firstOfMonth = DateUtils.setDays(now, 1);
        
        //within the last month
        Integer thisMonth = logEventDao.getLogEventsByEntityAndDateRange(entityUid, eventType, firstOfMonth, now);
        
        //downloads this month
        ModelAndView mav = new ModelAndView(JSON_VIEW_NAME, "all", all);
        mav.addObject("last3Months", last3Months);
        mav.addObject("thisMonth", thisMonth);
        
        return mav;
    }
    
	/**
	 * inject a map of remote user IP for security.
	 * @param remoteAddress
	 */
	@Autowired
	public void setRemoteSourceAddress(MapFactoryBean remoteSourceAddress) {
		if(this.remoteSourceAddress == null) {
			try{
				this.remoteSourceAddress = (Map)remoteSourceAddress.getObject();
			}
			catch(Exception e) {
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
	private boolean checkRemoteAddress(HttpServletRequest request){
		String realIp = request.getHeader(X_FORWARDED_FOR);
		if(realIp == null || "".equals(realIp)){
			realIp = request.getRemoteAddr();
		}
		
		if(remoteSourceAddress != null && realIp != null){
			logger.debug("***** request.getRemoteAddr(): " + request.getRemoteAddr() + " request.getRemoteHost(): " + request.getRemoteHost() + " , realIp: " +  request.getHeader(X_FORWARDED_FOR));
			if(remoteSourceAddress.containsKey(realIp.trim())){
				return true;
			}
		}
		return false;
	}
	
	private ModelAndView createErrorResponse(HttpServletResponse response, int statusCode){
		response.setStatus(statusCode);
        ModelMap model = new ModelMap();
        return new ModelAndView(JSON_VIEW_NAME, model);		
	}
	
	/**
	 * inject a list of remote user IP for security.
	 * 
	 * 	<bean id="remoteAddress" class="org.springframework.beans.factory.config.ListFactoryBean">
	 *  <property name="sourceList">
	 *		<list>
	 *		<value>bie.ala.org.au</value>
	 *			<value>152.83.198.112</value>
	 *		<value>150.229.66.87</value>
	 *			<value>127.0.0.1</value>
	 *			<value>152.83.198.139</value>
	 *		</list>
	 *	</property>
	 *	</bean>
	 */
	/*
	private List<String> remoteAddress;
	@Autowired
	public void setRemoteAddress(ListFactoryBean remoteAddress) {
		if(this.remoteAddress == null) {
			try{
				this.remoteAddress = (List<String>)remoteAddress.getObject();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}

	public List<String> getRemoteAddress() {
		return remoteAddress;
	}
	*/	
}
