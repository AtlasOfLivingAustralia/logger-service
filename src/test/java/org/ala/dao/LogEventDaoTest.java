package org.ala.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.ala.jpa.dao.LogEventDao;
import org.ala.jpa.entity.LogDetail;
import org.ala.jpa.entity.LogEvent;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/LogEventDaoTest-context.xml"})
public class LogEventDaoTest {

    final Logger logger = Logger.getLogger(LogEventDaoTest.class);
    
    @Autowired
    protected LogEventDao logEventDao = null;

    /**
     * Setup method run at the start of each test.
     */
    @Before
    public void before() throws Exception {
    	LogDetail logDetail = new LogDetail();
    	logDetail.setEntityUid("mok");
    	logDetail.setEntityType("a");
    	logDetail.setRecordCount(1);
    	
    	LogEvent entity1 = new LogEvent();
    	entity1.setCreated(new Date());
    	entity1.setComment("World");
    	entity1.setUserEmail("waiman.mok@csiro.au");
    	entity1.setUserIp("123.12.12.123");
                
        HashSet<LogDetail> set = new HashSet<LogDetail>();
        set.add(logDetail);
        entity1.setLogDetails(set);
        logEventDao.save(entity1);        
    }
    
    @Test
    public void testTemplate() throws SQLException {
    	Collection<LogEvent> l = logEventDao.findLogEvents();
    	for(LogEvent e : l){
    		System.out.println("LogEvent: " + e.toString());
    		Set<LogDetail> s = e.getLogDetails();
    		for(LogDetail d : s){
    			System.out.println("LogDetail: " + d.toString());
    		}
    	}
    	
    	LogEvent event = logEventDao.findLogEventById(1);
    	System.out.println(event.toString());
    	
    	l = logEventDao.findLogEventsByEmail("waiman.mok@csiro.au");
    	for(LogEvent e : l){
    		System.out.println("LogEvent: " + e.toString());
    		Set<LogDetail> s = e.getLogDetails();
    		for(LogDetail d : s){
    			System.out.println("LogDetail: " + d.toString());
    		}
    	}
    	
    	LogDetail logDetail = new LogDetail();
    	logDetail.setEntityUid("wai");
    	logDetail.setEntityType("ab");
    	logDetail.setRecordCount(12);
    	
    	event = logEventDao.saveLogDetail(1, logDetail);
    	System.out.println(event.toString());
    	
//    	int id = event.getId();
//    	logEventDao.delete(event);
//    	event = logEventDao.findLogEventById(id);
//    	System.out.println(event.toString());
    	return;
    }    
}

