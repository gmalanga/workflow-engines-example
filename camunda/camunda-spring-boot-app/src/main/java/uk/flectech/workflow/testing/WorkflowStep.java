package uk.flectech.workflow.testing;

import java.util.List;
import java.util.ArrayList;

class WorkflowStep { 
   static class FormVariable extends WorkflowStep {
      public String variableName;
      public String variableValue;

      public FormVariable() {}
      public FormVariable(String variableName, String variableValue) {
         this.variableName = variableName;
         this.variableValue = variableValue;
      }
   }
   static class WorkflowStart extends WorkflowStep {
      public List<FormVariable> variables;
      public WorkflowStart() {
         this.variables = new ArrayList<>();
      }
   }
   static class FormSubmit extends WorkflowStep {}
   static class AssertError extends WorkflowStep {
      public String errorType;
      public AssertError(String errorType) {
         this.errorType = errorType;
      }
   }
   static class AssertTask extends WorkflowStep {
      public String task;
      public AssertTask(String task) {
         this.task = task;
      }
   }
   static class AssertVariable extends WorkflowStep {
      public String variableName;
      public String variableValue;
      public AssertVariable() {}
      public AssertVariable(String variableName, String variableValue) {
         this.variableName = variableName;
         this.variableValue = variableValue;
      }
   }
}
