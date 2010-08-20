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

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.jpa.dao.LogEventDao;
import org.ala.jpa.entity.LogEvent;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.ala.client.model.LogEventVO;

/**
 * Main Controller for ALA-LOGGER
 * 
 * @author waiman.mok@csiro.au
 *
 */
@Controller
public class LoggerController {
	@Autowired
    private LogEventDao logEventDao;
	
	private List<String> remoteAddress;
	
	private static final String JSON_VIEW_NAME = "jsonView";
	
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
	 * URL: http://152.83.198.112:8080/ala-logger/service/logger/get.json?q=dp123&breakdown=0&eventTypeId=12345
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
		//check user
		if(!checkRemoteAddress(request)){
			return this.createErrorResponse(response, HttpStatus.UNAUTHORIZED.value());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				
		//read the existing value
		try {
			LogEventVO logEventVO = mapper.readValue(body, LogEventVO.class);
			if(logEventVO != null){
				LogEvent logEvent = new LogEvent(logEventVO.getEventTypeId(), logEventVO.getUserEmail(), 
						request.getRemoteAddr(), //logEventVO.getUserIP(),
						logEventVO.getComment(), logEventVO.getRecordCounts());
				logEventDao.save(logEvent);
			}
			
		} catch (Exception e) {
			return this.createErrorResponse(response, HttpStatus.NOT_ACCEPTABLE.value());
		}			
		return new ModelAndView(JSON_VIEW_NAME);		
	}
			
	public List<String> getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * inject a list of remote user IP for security.
	 * @param remoteAddress
	 */
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
	
	/**
	 * security check
	 * 
	 * @param request
	 * @return
	 */
	private boolean checkRemoteAddress(HttpServletRequest request){
		if(this.remoteAddress != null){
			logger.debug("***** request.getRemoteAddr(): " + request.getRemoteAddr() + " request.getRemoteHost(): " + request.getRemoteHost());
			if(this.remoteAddress.contains(request.getRemoteAddr()) || this.remoteAddress.contains(request.getRemoteHost())){
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
}
