package uk.flectech.workflow.testing;

import uk.flectech.workflow.testing.*;
import java.io.IOException;
import java.io.File;
import org.junit.Test;

public class DemoTest {
   @Test
   public void testFromSpreadsheet() throws IOException {
      SSReader r = new SSReader();
      WorkflowTests wf = r.readSpreadSheet(new File("src/test/resources/Testcase-Example.xls"));

      System.err.println("TODO Start workflow "+wf.processID);

      for (WorkflowStep wfs : wf.steps) {
        System.err.println("TODO Do "+wfs);
      }
   }
}
