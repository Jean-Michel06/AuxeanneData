/*
 * Copyright 2015 Jean-Michel Tanguy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.auxeanne.data; 

import com.auxeanne.data.record.ReportingLine;
import com.auxeanne.data.record.ReportingWeek;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Benchmark performance for a use case.
 * Mixing main features of the API to monitor performances.
 *
 * @author Jean-Michel Tanguy
 */
public class ReportingTest {

    final static int REPORTS = 2;
    final static int LINES = 5000;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nREPORTING SIMULATION\n============================");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("");
    }

    @Before
    public void setUp() {
        PU.getPuList();
    }

    @After
    public void tearDown() {

    }

    //--------------------------------------------------------------------------
    // TESTS
    //--------------------------------------------------------------------------
    @Test
    public void reporting() throws InterruptedException {
        for (String pu : PU.getPuList()) {
            System.out.println("\n== Testing " + pu);
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            Records records = new Records(emf);
            // records.auditAs( "TestUser");
            reportInsert(records);
            reportLineCount(records);
            reportFullGet(records);
            reportPagedGet(records);
            reportRemoveFirstWeek(records);
        }
    }

    private void reportInsert(Records records) {
        for (int i = 0; i < REPORTS; i++) {
            System.out.println("   Inserting report week  " + "W" + i);
            ReportingWeek week = getReportWeek(records, "W" + i);
            ReportingLine[] lines = new ReportingLine[LINES];
            for (int l = 0; l < LINES; l++) {
                ReportingLine line = new ReportingLine();
                line.setLine(l);
                line.getData().put("week", week.getWeek());
                lines[l] = line;
            }
            long start = System.currentTimeMillis();
            records.save(lines);
            long end = System.currentTimeMillis();

            System.out.println("   >>> Save in " + (end - start) + "ms");
            start = System.currentTimeMillis();
            records.link(week).with(lines).save();
            end = System.currentTimeMillis();
            System.out.println("   >>> Linking in " + (end - start) + "ms");
            start = System.currentTimeMillis();
            records.link(week).with(lines).setAttribute("week").save();
            end = System.currentTimeMillis();
            System.out.println("   >>> ReLinking in " + (end - start) + "ms");
            start = System.currentTimeMillis();
            int count = records.query(ReportingLine.class).linking(week).count();
            end = System.currentTimeMillis();
            System.out.println("   >>> Counting " + count + " in " + (end - start) + "ms");
        }
    }

    private void reportLineCount(Records records) {
        long start = System.currentTimeMillis();
        int count = records.query(ReportingLine.class).count();
        long end = System.currentTimeMillis();
        System.out.println("   Counting report lines  " + count + "  in " + (end - start) + "ms");
    }

    private void reportFullGet(Records records) {
        //int count =  records.queryFor(ReportingLine.class).count();
        long start = System.currentTimeMillis();
        records.query(ReportingLine.class).getList();
        long end = System.currentTimeMillis();
        System.out.println("   Full get in " + (end - start) + "ms");
    }

    private void reportPagedGet(Records records) {
        int count = records.query(ReportingLine.class).count();
        long start = System.currentTimeMillis();
        for (int i = 0; i < REPORTS; i++) {
            int page = LINES;
            int first = page * i;
            records.query(ReportingLine.class).setFirstResult(first).setMaxResults(page).getList();
        }
        long end = System.currentTimeMillis();
        System.out.println("   Paged get in  " + count + "  in " + (end - start) + "ms");
    }

    private void reportRemoveFirstWeek(Records records) {
        ReportingWeek reportWeek = getReportWeek(records, "W0");

        long start = System.currentTimeMillis();
        List<ReportingLine> lines = records.query(ReportingLine.class).linking(reportWeek).getList();
        long end = System.currentTimeMillis();
        System.out.println("   Fetching lines for first week : " + lines.size() + "  in " + (end - start) + "ms");
        start = System.currentTimeMillis();
        records.remove(lines);
        end = System.currentTimeMillis();
        System.out.println("   Removing lines for first week in " + (end - start) + "ms");
        Integer count = records.query(ReportingLine.class).linking(reportWeek).count();
        assertEquals(0L, count.longValue());
    }

    private ReportingWeek getReportWeek(Records records, String weekCode) {
        long start = System.currentTimeMillis();
        ReportingWeek week = records.query(ReportingWeek.class).fieldEqualTo("week", weekCode).getFirst();
        long end = System.currentTimeMillis();
        System.out.println("   >>> Getting report week  " + weekCode + "  in " + (end - start) + "ms");
        if (week == null) {
            week = new ReportingWeek();
            week.setWeek(weekCode);
            records.save(week);
        }
        return week;
    }

}
