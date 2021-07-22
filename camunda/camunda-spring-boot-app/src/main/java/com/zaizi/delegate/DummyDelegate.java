package com.zaizi.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyDelegate implements JavaDelegate {
	
	private final Logger LOGGER = LoggerFactory.getLogger(DummyDelegate.class);
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		LOGGER.info("Dummy delegate executed");
	}

}
