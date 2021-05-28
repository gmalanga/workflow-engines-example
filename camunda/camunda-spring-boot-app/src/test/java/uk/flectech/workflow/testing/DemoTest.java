package uk.flectech.workflow.testing;

import uk.flectech.workflow.testing.*;
import uk.flectech.workflow.testing.WorkflowStep.AssertTask;
import uk.flectech.workflow.testing.WorkflowStep.AssertVariable;
import uk.flectech.workflow.testing.WorkflowStep.FormVariable;
import uk.flectech.workflow.testing.WorkflowStep.WorkflowStart;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import java.io.File;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deployment(resources = "my-process-test.bpmn")
public class DemoTest {

	private final Logger LOGGER = LoggerFactory.getLogger(DemoTest.class);

	@Rule
	@ClassRule
	public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

	@Before
	public void setup() {
		init(rule.getProcessEngine());
	}

	@Test
	public void testFromSpreadsheet() throws IOException {

		ProcessInstance processInstance = null;
		String processInstanceKey = "";
		String initiator = "";
		String taskName = "";
		String candidateGroup = "";
		Map<String, Object> variables = new HashMap<String, Object>();

		SSReader r = new SSReader();
		WorkflowTests wf = r.readSpreadSheet(new File("src/test/resources/Testcase-Example-2.xlsx"));

		LOGGER.info("Workflow ID: {}", wf.processID);
		processInstanceKey = wf.processID;

		for (WorkflowStep wfs : wf.steps) {
			LOGGER.info("TODO Do {}", wfs);

			// ACTION START
			if (wfs.toString().contains("WorkflowStart")) {

				WorkflowStart ws = (WorkflowStart) wfs;
				List<FormVariable> vars = ws.variables;
				
				for (FormVariable fv : vars) {
					variables.put(fv.variableName, fv.variableValue);
				}

				// Start process with Java API and variables
				processInstance = startWorkflow(processInstanceKey, variables);
			}
			// ASSERT TASK
			if (wfs.toString().contains("AssertTask")) {
				AssertTask as = (AssertTask) wfs;
				taskName = as.task;
				if(taskName.equals("End"))
				{
					assertThat(processInstance).isEnded();
				} else {
					assertThat(processInstance).isWaitingAt(taskName);
				}
			}
			// ASSERT VARIABLE
			if (wfs.toString().contains("AssertVariable")) {
				AssertVariable av = (AssertVariable) wfs;
				assertThat(processInstance).isWaitingAt(taskName).variables()
						.contains(entry(av.variableName, av.variableValue));
			}
			// FORM VARIABLE
			if (wfs.toString().contains("FormVariable")) {
				FormVariable fv = (FormVariable) wfs;
				variables.put(fv.variableName, fv.variableValue);
			}
			// FORM SUBMIT
			if (wfs.toString().contains("FormSubmit")) {
				// TODO Include the candidate groups in the spreadsheet, or the owner of the task
				if(taskName.equals("Review1")) {
					candidateGroup = "Reviewers1";
				}
				if(taskName.equals("Review2")) {
					candidateGroup = "Reviewers2";
				}
				claimAndCompleteTask(processInstance.getId(), candidateGroup, variables);
			}
			// ASSERT ERROR
			if (wfs.toString().contains("AssertError")) {

			}
		}
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
