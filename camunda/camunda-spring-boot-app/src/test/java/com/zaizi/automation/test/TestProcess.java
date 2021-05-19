package com.zaizi.automation.test;

import static org.assertj.core.api.Assertions.*;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

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

@Deployment(resources = "my-process-test.bpmn")
public class TestProcess {

	@Rule
	@ClassRule
	public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

	@Before
	public void setup() {
		init(rule.getProcessEngine());
	}

	@Test
	public void testHappyPath() {

		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap<String, Object>();

		// Start process with Java API and variables
		ProcessInstance processInstance = startWorkflow("my-test-process", variables);

		assertThat(processInstance).isWaitingAt("Review1");
		assertThat(task()).hasCandidateGroup("Reviewers1").isNotAssigned();

		// Complete the task
		variables.put("Approved", true);
		claimAndCompleteTask(processInstance.getId(), "Reviewers1", variables);

		assertThat(processInstance).isWaitingAt("Review2");
		assertThat(task()).hasCandidateGroup("Reviewers2").isNotAssigned();

		// Complete the task
		variables.put("Approved2", true);
		claimAndCompleteTask(processInstance.getId(), "Reviewers2", variables);

		// Make assertions on the process instance
		assertThat(processInstance).isEnded();
	}

	@Test
	public void testUnhappyPath() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap<String, Object>();

		// Start process with Java API and variables
		ProcessInstance processInstance = startWorkflow("my-test-process", variables);

		assertThat(processInstance).isWaitingAt("Review1");
		assertThat(task()).hasCandidateGroup("Reviewers1").isNotAssigned();

		// Complete the task
		variables.put("Approved", false);
		claimAndCompleteTask(processInstance.getId(), "Reviewers1", variables);

		assertThat(processInstance).isWaitingAt("Review1");
	}

	@Test
	public void testUnhappyPathTwo() {
		// Create a HashMap to put in variables for the process instance
		Map<String, Object> variables = new HashMap<String, Object>();

		// Start process with Java API and variables
		ProcessInstance processInstance = startWorkflow("my-test-process", variables);

		assertThat(processInstance).isWaitingAt("Review1");
		assertThat(task()).hasCandidateGroup("Reviewers1").isNotAssigned();

		// Complete the task
		variables.put("Approved", true);
		claimAndCompleteTask(processInstance.getId(), "Reviewers1", variables);

		assertThat(processInstance).isWaitingAt("Review2");

		// Complete the task
		variables.put("Approved2", false);
		claimAndCompleteTask(processInstance.getId(), "Reviewers2", variables);

		assertThat(processInstance).isWaitingAt("Review1");
	}

	private ProcessInstance startWorkflow(String ProcessInstanceKey, Map<String, Object> variables) {
		return runtimeService().startProcessInstanceByKey(ProcessInstanceKey, variables);

	}

	private void claimAndCompleteTask(String processInstanceId, String groupId, Map<String, Object> approvedMap) {

		// Get the list of available tasks
		List<Task> taskList = taskService().createTaskQuery().taskCandidateGroup(groupId)
				.processInstanceId(processInstanceId).list();

		assertThat(taskList).isNotNull();
		assertThat(taskList).hasSize(1);

		// Complete the task
		Task task = taskList.get(0);

		taskService().complete(task.getId(), approvedMap);

	}

}
