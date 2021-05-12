package com.zaizi.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDelegate implements JavaDelegate {
	
	private final Logger LOGGER = LoggerFactory.getLogger(TestDelegate.class.getName());

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		LOGGER.info("Hello world");
		
		execution.setVariable("MySubVariable", "TEST MyVariable");
	}

}
