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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.client.model.LogEventType;
import org.ala.client.model.LogEventVO;
import org.ala.jpa.dao.LogEventDao;
import org.ala.jpa.entity.LogEvent;
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
				
				if(logEventVO.getMonth() == null || (logEventVO.getMonth() != null && logEventVO.getMonth().length() < 4)){
				logEvent = new LogEvent((String)remoteSourceAddress.get(realIp.trim()), type.getId(), logEventVO.getUserEmail(), 
						logEventVO.getUserIP(), logEventVO.getComment(), logEventVO.getRecordCounts());
				}
				else{
					logEvent = new LogEvent((String)remoteSourceAddress.get(realIp.trim()), type.getId(), logEventVO.getUserEmail(), 
							logEventVO.getUserIP(), logEventVO.getComment(), logEventVO.getMonth(), logEventVO.getRecordCounts());
				}
				logEvent = logEventDao.save(logEvent);
			}
			
		} catch (Exception e) {
			logger.error("Invalid LogEvent Type or Id: " + logEventVO.getEventTypeId() + "\n JSON request: \n" +  body, e);
			return this.createErrorResponse(response, HttpStatus.NOT_ACCEPTABLE.value());
		}			
		return new ModelAndView(JSON_VIEW_NAME, "logEvent", logEvent);		
	}
/*
    @RequestMapping(method=RequestMethod.GET, value={"/{entityUid}/{eventType}/counts.json", "/{entityUid}/{eventType}/counts"})
    public ModelAndView getLogEventCounts(
            @PathVariable("entityUid") String entityUid,
            @PathVariable("eventType") Integer eventType){
        
        //return createDownloadSummary(entityUid, eventType);
        return createSummary(entityUid, eventType, SummaryType.DOWNLOAD);
    }

    @RequestMapping(method=RequestMethod.GET, value={"/{entityUid}/downloads/counts.json", "/{entityUid}/downloads/counts"})
    public ModelAndView getLogEventCounts(
            @PathVariable("entityUid") String entityUid){
        //return createDownloadSummary(entityUid, 1002);
        return createSummary(entityUid, 1002, SummaryType.DOWNLOAD);
    }
*/
    /**
     * Create a summary for event downloads
     *
     * @param entityUid
     * @return
     */
    @RequestMapping(method=RequestMethod.GET, value={"/{entityUid}/events/{eventId}/counts.json", "/{entityUid}/events/{eventId}/counts"})
    public ModelAndView getLogEventCounts(
            @PathVariable("entityUid") String entityUid,
            @PathVariable("eventId") int eventId){
        //return createEventSummary(entityUid, eventId);
        return createSummary(entityUid, eventId, SummaryType.EVENT);
    }

    private ModelAndView createSummary(String entityUid, Integer eventType, SummaryType summary) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
    	
        //all
        Integer[] all = logEventDao.getLogEventsByEntity(entityUid, eventType);

        //thisMonth, last3Month, lastYearByMonth
        Calendar curCal = Calendar.getInstance();
        curCal.set(Calendar.DAY_OF_MONTH, 1) ;        
        
        Calendar prevCal = Calendar.getInstance();
        prevCal.set(Calendar.DAY_OF_MONTH, 1) ;
        prevCal.add(Calendar.DAY_OF_MONTH, -1);
        if(prevCal.get(Calendar.MONTH) != Calendar.DECEMBER){
        	 prevCal.add(Calendar.YEAR, -1);
        }
        
        Collection<Object[]> thisYear = null;
        ModelAndView mav = null;
        thisYear = logEventDao.getEventsDownloadsCount(eventType, entityUid, sdf.format(prevCal.getTime()), sdf.format(curCal.getTime()));
        
    	//downloads this month
        mav = new ModelAndView(JSON_VIEW_NAME, "all",createMapForJson(all, summary));
        mav.addObject("last3Months", createMapForJson(thisYear, new int[]{(curCal.get(Calendar.MONTH) + 1), (curCal.get(Calendar.MONTH)), (curCal.get(Calendar.MONTH) - 1)}, summary));
        mav.addObject("thisMonth", createMapForJson(thisYear, new int[]{(curCal.get(Calendar.MONTH) + 1)}, summary));
        mav.addObject("lastYearByMonth", createMonthlyMapForJson(thisYear, curCal.get(Calendar.MONTH) + 1));

        return mav;
    }
        
    private Map<String, Integer> createMapForJson(Integer[] value, SummaryType type) {
        Map<String, Integer> noDownloadsAndCount = new LinkedHashMap<String,Integer>();
        if(value != null && value.length > 1){
	        if(type == SummaryType.EVENT){
	            noDownloadsAndCount.put("numberOfEvents", value[0]);
	            noDownloadsAndCount.put("numberOfEventItems", value[1]);
	        }
	        else{
		        noDownloadsAndCount.put("numberOfDownloads", value[0]);
		        noDownloadsAndCount.put("numberOfDownloadedRecords", value[1]);
	        }
        }
        return noDownloadsAndCount;
    }

    private Map<String, Integer> createMapForJson(Collection<Object[]> l, int[] month, SummaryType type) {
        Map<String, Integer> noDownloadsAndCount = new HashMap<String,Integer>();
        int events = 0;
        int items = 0;
        if(l != null && month != null && type != null){
        	for(int i = 0; i < month.length; i++){
        		Object[] value = getMonthValue(l, month[i]); 
        		if(value != null && value.length > 2){
	        		events += Integer.parseInt(value[SummaryType.EVENT.ordinal() + 1].toString());        		
	        		items += Integer.parseInt(value[SummaryType.DOWNLOAD.ordinal() + 1].toString());
        		}
         	}
        	noDownloadsAndCount = createMapForJson(new Integer[]{events, items}, type);
        }
        return noDownloadsAndCount;
    }
 
    private Map<String, Map<String, Integer>> createMonthlyMapForJson(Collection<Object[]> l, int startMonth) {        
        Map<String, Map<String, Integer>> monthlyCount = new LinkedHashMap<String, Map<String, Integer>>();     
        
        DateFormatSymbols dfs = new DateFormatSymbols();
        int month = startMonth + 1;
        for(int mth = 1; mth <= 12; mth++){
        	Map<String, Integer> noDownloadsAndCount = new LinkedHashMap<String,Integer>();
        	if(month > 12){
        		month = 1;
        	}
        	Object[] value = getMonthValue(l, month);
        	if(value != null){
        		noDownloadsAndCount.put("numberOfEvents", Integer.valueOf(value[SummaryType.EVENT.ordinal() + 1].toString()));
        		noDownloadsAndCount.put("numberOfEventItems", Integer.valueOf(value[SummaryType.DOWNLOAD.ordinal() + 1].toString()));
        	}
        	else{
        		noDownloadsAndCount.put("numberOfEvents", 0);
        		noDownloadsAndCount.put("numberOfEventItems", 0);
        	}
        	monthlyCount.put(dfs.getMonths()[month-1], noDownloadsAndCount);
        	month++;
        }
        return monthlyCount;
    }
    
    private Object[] getMonthValue(Collection<Object[]> l, int mth) {
    	Object[] value = null;
    	if(l != null){
	    	Iterator<Object[]> itr = l.iterator();
	    	while(itr.hasNext()){
	    		Object[] o = itr.next();
	    		if(o[0] != null && o[0].toString().length() > 5){
	    			int month = Integer.valueOf(o[0].toString().substring(4, 6));
	    			if(mth == month){
	    				value = o;
	    				if(value[1] == null || value[1].toString().length() == 0){
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
	 * @param remoteSourceAddress
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
