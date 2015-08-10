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
package com.auxeanne.api.data;

import com.auxeanne.api.data.record.CommentRecord;
import com.auxeanne.api.data.record.PersonRecord;
import java.text.DecimalFormat;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Monitoring on high volume save and query.
 * @author Jean-Michel Tanguy
 */
public class RecordChargeTest {

    private final int LOOPS = 20000;
    DecimalFormat df = new DecimalFormat("0.00 ms");

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nHIGH CHARGE MONITORING\n============================");
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
    public void retrieveInTheMass() {
        for (String pu : PU.getPuList()) {
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            Records records = new Records(emf);
            System.out.println("\n=== Testing " + pu + "  " + LOOPS  + " iterations");
            goInMass(records, LOOPS );
        }
    }

    private void goInMass(Records records, int loops) {
        // init record type
        records.save(new PersonRecord());
        records.save(new CommentRecord());
        // inserting
        long start = System.currentTimeMillis();
        PersonRecord person = null;
        for (int j = 0; j < 100; j++) {
            // batch save
            PersonRecord[] list = new PersonRecord[loops/100];
            for (int i = 0; i < loops / 100; i++) {
                person = new PersonRecord();
                person.setFirstName("John " + i);
                person.setLastName("Do");
                list[i] = person;
            }
            records.save(list);
            // mixing with a different record
            CommentRecord comment = new CommentRecord();
            comment.setMessage(j + "");
            records.save(comment);
        }
        long end = System.currentTimeMillis();
        System.out.println("   Records inserted in : " + (end - start) + " ms ");
        // TEST 1 update in the middle of the list
        start = System.currentTimeMillis();

        person.setFirstName("UPDATED");
        records.save(person);
        end = System.currentTimeMillis();
        System.out.println("   One record updated in : " + (end - start) + " ms for id " + person.getId());

        // TEST 2 retrieve few comments in the mass, using cache
        start = System.currentTimeMillis();
        List<CommentRecord> list1 = records.query(CommentRecord.class).getList();
        end = System.currentTimeMillis();
        System.out.println("   Read Query : " + (end - start) + " ms for " + list1.size() + " entities");
    }
}
