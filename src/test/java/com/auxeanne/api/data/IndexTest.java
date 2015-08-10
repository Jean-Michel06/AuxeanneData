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

import com.auxeanne.api.data.record.PersonRecord;
import com.auxeanne.api.data.record.PersonRecordWithIndex;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing indexing features.
 *
 * @author Jean-Michel Tanguy
 */
public class IndexTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nINDEXING\n============================");
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
    public void testIndex() {
        int loops = 5000;
        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Testing " + pu);
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            sortWithIndex(emf);
            noIndex(emf, loops);
            withIndex(emf, loops);
        }
    }

    private void noIndex(EntityManagerFactory emf, int loops) {
        System.out.println("-- Performances for query with no index");
        long start, end;
        Records records = new Records(emf);
        List<PersonRecord> list = new ArrayList<>();

        for (int i = 0; i < loops; i++) {
            PersonRecord r = new PersonRecord();
            r.setLastName("NAME" + (i % 50));
            list.add(r);
        }
        start = System.currentTimeMillis();
        records.save(list);
        end = System.currentTimeMillis();
        System.out.println("    Inserting : " + loops + " in " + (end - start) + "ms");

        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).fieldEqualTo("lastName", "NAME0").getList();
        end = System.currentTimeMillis();
        System.out.println("    Query with match 1/50 : " + list.size() + " in " + (end - start) + "ms");

        list = records.query(PersonRecord.class).getList();
        start = System.currentTimeMillis();
        records.remove(list);
        end = System.currentTimeMillis();
        System.out.println("    Deleting : " + list.size() + " in " + (end - start) + "ms");
    }

    private void withIndex(EntityManagerFactory emf, int loops) {
        Records records = new Records(emf);
        System.out.println("-- Checking Index update ");
        PersonRecordWithIndex r2 = new PersonRecordWithIndex();
        r2.setLastName("NAME");
        records.save(r2);
        r2.setLastName("UPDATED");
        records.save(r2);
        records.remove(r2);

        System.out.println("-- Performances for query with index ");
        long start, end;

        List<PersonRecordWithIndex> list = new ArrayList<>();

        for (int i = 0; i < loops; i++) {
            PersonRecordWithIndex r = new PersonRecordWithIndex();
            r.setLastName("NAME" + (i % 50));
            list.add(r);
        }
        start = System.currentTimeMillis();
        records.save(list);
        end = System.currentTimeMillis();
        System.out.println("    Inserting : " + loops + " in " + (end - start) + "ms");

        start = System.currentTimeMillis();
        list = records.query(PersonRecordWithIndex.class).indexEqualTo("lastName", "NAME0").getList();
        end = System.currentTimeMillis();
        System.out.println("    Query with match 1/50 : " + list.size() + " in " + (end - start) + "ms");

        list = records.query(PersonRecordWithIndex.class).getList();
        start = System.currentTimeMillis();
        records.remove(list);
        end = System.currentTimeMillis();
        System.out.println("    Deleting : " + list.size() + " in " + (end - start) + "ms");
    }

    private void sortWithIndex(EntityManagerFactory emf) {
        Records records = new Records(emf);

        System.out.println("-- Checking query index with sort");
        long start, end;
        // setting
        PersonRecordWithIndex daddy = new PersonRecordWithIndex();
        daddy.setLastName("Daddy");
        daddy.setAge(40);

        PersonRecordWithIndex mummy = new PersonRecordWithIndex();
        mummy.setLastName("Mummy");
        mummy.setAge(30);

        PersonRecordWithIndex baby = new PersonRecordWithIndex();
        baby.setLastName("Baby");
        baby.setAge(5);

        records.save(daddy, baby, mummy);

        // queries
        assertEquals(mummy, records.query(PersonRecordWithIndex.class).indexEqualTo("age", 30).getFirst());
        assertEquals(daddy, records.query(PersonRecordWithIndex.class).indexIn("age", 40, 50).getFirst());
        assertEquals(baby, records.query(PersonRecordWithIndex.class).indexEqualTo("lastName", "Baby").getFirst());

        assertEquals(baby, records.query(PersonRecordWithIndex.class).orderByIndexedField("age").getFirst());
        assertEquals(daddy, records.query(PersonRecordWithIndex.class).reverseByIndexedField("age").getFirst());

        assertEquals(baby, records.query(PersonRecordWithIndex.class).orderByIndexedField("lastName").getFirst());
        assertEquals(mummy, records.query(PersonRecordWithIndex.class).reverseByIndexedField("lastName").getFirst());

        assertEquals(mummy, records.query(PersonRecordWithIndex.class).reverseByInsert().getFirst());
        assertEquals(daddy, records.query(PersonRecordWithIndex.class).orderByInsert().getFirst());
        //
        records.remove(daddy, baby, mummy);
    }
}
