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
import com.auxeanne.data.record.PersonRecordWithIndex;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing link CRUD.
 *
 * @author Jean-Michel Tanguy
 */
public class SortTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nSORTING\n============================");
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
    
    /**
     * checking sorting is working when above/below liking keywords.
     */
    @Test
    public void testSort() {
        int loops = 5000;
        for (String pu : PU.getPuList()) {
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            System.out.println("\n=== Sort test using " + pu + " ===");
            testSort(emf, loops);
        }
    }

    private void testSort(EntityManagerFactory emf, int loops) {
        Records records = new Records(emf);
        //cleaning
        List<PersonRecordWithIndex> toDelete = records.query(PersonRecordWithIndex.class).getList();
        records.remove(toDelete);
        
        
        PersonRecord root = new PersonRecord();
        PersonRecordWithIndex[] persons = new PersonRecordWithIndex[3];

        persons[0] = new PersonRecordWithIndex();
        persons[1] = new PersonRecordWithIndex();
        persons[2] = new PersonRecordWithIndex();

        persons[0].setAge(20);
        persons[1].setAge(30);
        persons[2].setAge(10);

        
        records.save(root);
        records.save(persons[0]);
        records.save(persons[1]);
        records.save(persons[2]);

        records.link(root).asParentOf(persons).save();

        List<PersonRecordWithIndex> list = records.query(PersonRecordWithIndex.class)
                .childOfAny(root)
                .reverseByIndexedField("age")
                .setFirstResult(0).setMaxResults(10).getList();

        assertEquals((long) 30, (long) list.get(0).getAge());
        assertEquals((long) 20, (long) list.get(1).getAge());
        assertEquals((long) 10, (long) list.get(2).getAge());

        List<PersonRecordWithIndex> list2 = records.query(PersonRecordWithIndex.class).indexGreaterThan("age", 10)
                .childOfAny(root)
                .reverseByIndexedField("age")
                .setFirstResult(0).setMaxResults(10).getList();
        assertEquals(2, list2.size());
        
        // to prevent side effect on other sorting test
        records.remove(persons);
        records.remove(root);

    }

}
