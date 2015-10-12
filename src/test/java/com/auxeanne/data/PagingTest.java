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

import com.auxeanne.data.record.PersonRecord;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test database parameter paging to handle size limitation by some databases (ex: PostgreSQL).
 * @author Jean-Michel Tanguy
 */
public class PagingTest {


    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nDATABASE PARAMETER PAGING\n============================");
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

    /**
     * Test will failed is number of parameters per query exceeds the database
     * limit. The API should split to queries to handle this limitation (for example: PostgresSQL)
     */
    @Test
    public void canSaveAboveLimit() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Testing " + pu);
            List<PersonRecord> list = new ArrayList<>();
            for (int i = 0; i < 50000; i++) {
                list.add(new PersonRecord());
            }
            Records records = new Records(PU.getFactoryList().get(pu));
            records.save(list);
            records.remove(list);
        }

    }
}
