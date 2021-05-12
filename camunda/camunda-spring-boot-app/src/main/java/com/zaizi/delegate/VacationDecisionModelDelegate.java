package com.zaizi.delegate;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VacationDecisionModelDelegate implements JavaDelegate {
	
	private final Logger LOGGER = LoggerFactory.getLogger(VacationDecisionModelDelegate.class);
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		LOGGER.info("Decision Model");
		
		Date from = addHoursToJavaUtilDate((Date) execution.getVariable("from"), 9);
		LOGGER.info("from: {}", from);
		Date to = addHoursToJavaUtilDate((Date) execution.getVariable("to"), 33);
		LOGGER.info("to: {}", to);
		
		long diffInMillies = Math.abs(to.getTime() - from.getTime());
	    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    LOGGER.info("diff: {}", diff);
	    
	    if(diff <= 3)
	    {
	    	execution.setVariable("moreThanThreeDays", false);
			execution.setVariable("approved", true);
			execution.setVariable("comments", "Automatically approved by the system.");
	    } else
	    {
	    	execution.setVariable("moreThanThreeDays", true);
	    }
		
		
	}
	
	private Date addHoursToJavaUtilDate(Date date, int hours) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.add(Calendar.HOUR_OF_DAY, hours);
	    return calendar.getTime();
	}
}
