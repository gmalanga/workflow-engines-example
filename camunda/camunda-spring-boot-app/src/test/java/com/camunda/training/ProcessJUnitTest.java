package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;


@Deployment(resources = "post_tweet_repost.bpmn")
public class ProcessJUnitTest {

//	@Rule
//	public ProcessEngineRule rule = new ProcessEngineRule();
	@Rule @ClassRule
	public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

	@Before
	public void setup() {
		init(rule.getProcessEngine());
	}

	@Test
	public void testHappyPath() {
		
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("tweet_content", "Exercise 4 test1 - Giuseppe");

		// Start process with Java API and variables
		ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("TweetQA-process2", variables);

		assertThat(processInstance).isWaitingAt("UserTask_ReviewTweet");
		assertThat(task()).hasCandidateGroup("management").isNotAssigned();

		// Get the list of available tasks
		List<Task> taskList = taskService().createTaskQuery().taskCandidateGroup("management")
				.processInstanceId(processInstance.getId()).list();

		assertThat(taskList).isNotNull();
		assertThat(taskList).hasSize(1);

		// Complete the task
		Task task = taskList.get(0);
		Map<String, Object> approvedMap = new HashMap<String, Object>();
		approvedMap.put("approved", true);
		taskService().complete(task.getId(), approvedMap);
		
		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}
	
	@Test
	public void testTweetRejected() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("tweet_content", "Exercise 8");
		variables.put("approved", false);
		variables.put("repost", false);

		ProcessInstance processInstance = runtimeService().createProcessInstanceByKey("TweetQA-process2")
				.setVariables(variables).startAfterActivity(findId("Review Tweet")).execute();

		assertThat(processInstance).isWaitingAt(findId("Send Notification")).externalTask()
				.hasTopicName("notification");
		complete(externalTask());

		assertThat(processInstance).isEnded().hasPassed(findId("Modify Tweet"));
	}

}
