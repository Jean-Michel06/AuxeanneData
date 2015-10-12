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

import com.auxeanne.data.record.CommentRecord;
import com.auxeanne.data.record.PersonRecord;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test multi tenant.
 *
 * @author Jean-Michel Tanguy
 */
public class TenantTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nMULTI TENANT\n============================");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("");
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    //--------------------------------------------------------------------------
    // TESTS
    //--------------------------------------------------------------------------
    @Test
    public void testMultiTenant() {

        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Testing " + pu);
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            Records records1 = new Records(emf, "Tenant 1");
            Records records2 = new Records(emf, "Tenant 2");

            // Setting to T1
            CommentRecord t1 = new CommentRecord();
            records1.save(t1);
            for (int i = 0; i < 5; i++) {
                PersonRecord p = new PersonRecord();
                records1.save(p);
                records1.link(t1).with(p).save();
            }

            // Setting to T2
            CommentRecord t2 = new CommentRecord();
            records2.save(t2);
            for (int i = 0; i < 10; i++) {
                PersonRecord p = new PersonRecord();
                records2.save(p);
                records2.link(t2).with(p).save();
            }
            assertEquals(5L, records1.query(PersonRecord.class).linking(t1).count().longValue());
            assertEquals(5L, records1.query(PersonRecord.class).count().longValue());
            assertEquals(10L, records2.query(PersonRecord.class).count().longValue());
            assertEquals(10L, records2.query(PersonRecord.class).linking(t2).count().longValue());
            assertEquals(0L, records1.query(PersonRecord.class).linking(t2).count().longValue());
        }
    }

}
