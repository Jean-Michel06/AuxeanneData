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

import com.auxeanne.data.db.RecordWrapper;
import com.auxeanne.data.record.PersonRecord;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean-Michel Tanguy
 */
public class RecordTest {

    private final int LOOPS = 1000;
    DecimalFormat df = new DecimalFormat("0.00 ms");

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nRECORD CRUD\n============================");
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
    public void testIsNull() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Testing NULL support for " + pu);
            Records records = new Records(PU.getFactoryList().get(pu));
            records.save((List) null);       // testing null array is handled
            records.save(null, null); // tsting null element in array is handled
        }

    }

    @Test
    public void testCRUD() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n== CRUD testing " + pu );
            crud(PU.getFactoryList().get(pu));
        }

    }

    @Test
    public void testNativetBatch() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n== Batch Performance using native query " + pu + "  " + LOOPS + " iterations (to compare with framework performance)");
            nativeBatch(PU.getFactoryList().get(pu));
        }
    }

    @Test
    public void testFrameworkBatch() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n== Batch Performance using framework query " + pu + "  " + LOOPS + " iterations");
            frameworkBatch(PU.getFactoryList().get(pu));
        }
    }

    private void frameworkBatch(EntityManagerFactory target) {

        // inserting
        long start1 = System.currentTimeMillis();
        Records records = new Records(target);

        List<PersonRecord> list = new ArrayList<>();
        for (int i = 0; i < LOOPS; i++) {
            PersonRecord person = new PersonRecord();
            person.setFirstName("John " + i);
            person.setLastName("Do");
            list.add(person);
        }
        long end2 = System.currentTimeMillis();
        System.out.println("Batch setup : " + (end2 - start1) + " ms ");

        long start = System.currentTimeMillis();

        for (PersonRecord person : list) {
            records.save(person);
        }

        long end = System.currentTimeMillis();
        System.out.println("Batch write : " + (end - start) + " ms ");
        // reading from a different new entity
        //records = new Records(target);
        start = System.currentTimeMillis();
        List<PersonRecord> list1 = records.query(PersonRecord.class).getList();
        end = System.currentTimeMillis();
        System.out.println("Batch read : " + (end - start) + " ms ");

        for (PersonRecord person : list1) {
            person.setLastName("Dos");
        }        
        start = System.currentTimeMillis();
        records.save(list1);
        end = System.currentTimeMillis();
        System.out.println("Batch REwrite : " + (end - start) + " ms ");
    }

    private void crud(EntityManagerFactory target) {
        Records records = new Records(target);

        PersonRecord person = new PersonRecord();
        person.setFirstName("John");
        person.setLastName("Do");

        records.save(person);
        assertNotNull(person.getId());

        person.setFirstName("Johnny");
        records.save(person);

        person = records.query(PersonRecord.class).find(person.getId());

        assertEquals("Johnny", person.getFirstName());

    }

    private void nativeBatch(EntityManagerFactory target) {
        EntityManager em = target.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        long start = System.currentTimeMillis();
        tx.begin();
        for (int i = 0; i < LOOPS; i++) {
            em.persist(new RecordWrapper());
        }
        tx.commit();
        long end = System.currentTimeMillis();
        System.out.println("Native Batch : " + (end - start) + " ms ");
    }

}
