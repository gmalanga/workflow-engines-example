package uk.flectech.workflow.testing;

import java.util.List;
import java.util.ArrayList;

public class WorkflowTests { 
   public String processID;
   public List<WorkflowStep> steps;

   public WorkflowTests() {
      steps = new ArrayList<>();
   }
   public WorkflowTests(String processID, List<WorkflowStep> steps) {
      this.processID = processID;
      this.steps = steps;
   }
}
