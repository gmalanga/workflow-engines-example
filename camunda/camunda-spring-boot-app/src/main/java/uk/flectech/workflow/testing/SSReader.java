package uk.flectech.workflow.testing;

import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.File;

import static uk.flectech.workflow.testing.WorkflowStep.*;

public class SSReader {
   public static final String SHEETNAME = "Workflow";
   public static final String WORKFLOW_ID = "Workflow ID";

   private DataFormatter fmt = new DataFormatter();

   public WorkflowTests readSpreadSheet(File input) throws IOException {
      Workbook wb = WorkbookFactory.create(input, null, true);
      Sheet s = null;

      if (wb.getNumberOfSheets() == 1) {
         s = wb.getSheetAt(0);
      } else {
         s = wb.getSheet(SHEETNAME);
         if (s == null) throw new IllegalArgumentException("Test spreadsheet contained multiple sheets, required sheet ${SHEETNAME} not found");
      }

      WorkflowTests tests = null;
      DataReader dr = null;

      for (int rn=0; rn<s.getLastRowNum(); rn++) {
         List<Object> r = readRow(s, rn);
         if (tests == null) {
            String id = getWorkflowId(r);
            if (id != null) {
               tests = new WorkflowTests(id, new ArrayList<>());
            }
         } else {
            if (dr == null) {
               dr = DataReader.createIfValid(r, tests);
            } else {
               dr.process(r);
            }
         }
      }

      wb.close();

      if (tests == null) throw new IllegalArgumentException("Invalid spreadsheet - Entry for '${WORKFLOW_ID}' not found in sheet ${s.getSheetName()}");

      return tests;
   }

   protected List<Object> readRow(Sheet s, int rn) {
      Row r = s.getRow(rn);
      if (r == null) return Collections.emptyList();

      List<Object> vals = new ArrayList<>();
      for (int cn=0; cn<r.getLastCellNum(); cn++) {
         Cell c = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
         if (c == null) {
            vals.add(null);
            continue;
         } else {
            // TODO Proper value type handling
            vals.add( c.toString() );
         }
      }
      return vals;
   }

   protected String getWorkflowId(List<Object> row) {
      List<String> rv = new ArrayList<>();
      for (Object v : row) {
         if (v == null) continue;
         String vs = v.toString();
         if (! vs.isEmpty()) { rv.add(vs); }
      }

      if (rv.size() < 2) return null;
      if (WORKFLOW_ID.equalsIgnoreCase(rv.get(0))) return rv.get(1);
      return null;
   }

   public static class DataReader {
      // TODO Are there any more things we might want to do or check?
      // TODO What about gateways?
      // TODO What about multiple tokens?
      static final String VARNAME = "Variable Name";
      static final String VARVAL  = "Variable Value";
      static final String ACTION  = "Action";
      static final String EXPERR  = "Expected Error";
      static final String EXPTSK  = "Expected Task";
      static final String EXPVRN  = "Expected Variable Name";
      static final String EXPVRV  = "Expected Variable Value";
      static final List<String> all = Arrays.asList(
                                       VARNAME, VARVAL, ACTION,
                                       EXPERR, EXPTSK, EXPVRN, EXPVRV);

      protected Map<Integer,String> columns;
      protected WorkflowTests wt;
      protected boolean started;

      private DataReader(Map<Integer,String> columns, WorkflowTests wt) {
         this.columns = columns;
         this.wt = wt;

         wt.steps.add(new WorkflowStart());
      }

      static DataReader createIfValid(List<Object> vals, WorkflowTests t) {
         Map<Integer,String> ids = new HashMap<>();
         for (int idx=0; idx<vals.size(); idx++) {
            String v = (String)vals.get(idx);
            if (v == null || v.isEmpty()) continue;
            for (String h : all) { 
               if (h.equalsIgnoreCase(v)) { ids.put(idx,h); }
            }
         }
         if (ids.size() == all.size()) {
            return new DataReader(ids, t);
         }
         return null;
      }

      public void process(List<Object> rowVals) {
         // Map to our columns by name
         Map<String,Object> vals = new HashMap<>();
         for (int idx=0; idx<rowVals.size(); idx++) {
            String col = columns.get(idx);
            if (col != null) {
               Object v = rowVals.get(idx);
               if (v != null && ! v.toString().isEmpty()) {
                  vals.put(col, v);
               }
            }
         }

         // What do they want to do?
         if (vals.get(VARNAME) != null && vals.get(VARVAL) != null) {
            FormVariable fv = new FormVariable((String)vals.get(VARNAME),
                                               (String)vals.get(VARVAL));
            if (started) {
               wt.steps.add(fv);
            } else {
               WorkflowStart ws = (WorkflowStart)wt.steps.get(0);
               ws.variables.add(fv);
            }
         }
         if (vals.get(ACTION) != null && 
               "Start".equalsIgnoreCase( (String)vals.get(ACTION) )) {
            started = true;
         }
         if (vals.get(ACTION) != null && 
               "Submit".equalsIgnoreCase( (String)vals.get(ACTION) )) {
            wt.steps.add(new FormSubmit());
         }
         if (vals.get(EXPERR) != null) {
            wt.steps.add(new AssertError( (String)vals.get(EXPERR) ));
         }
         if (vals.get(EXPTSK) != null) {
            wt.steps.add(new AssertTask( (String)vals.get(EXPTSK) ));
         }
         if (vals.get(EXPVRN) != null && vals.get(EXPVRV) != null) {
            wt.steps.add(new AssertVariable( (String)vals.get(EXPVRN),
                                             (String)vals.get(EXPVRV) ));
         }
      }
   }
}
