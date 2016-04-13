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
 * Testing link CRUD.
 *
 * @author Jean-Michel Tanguy
 */
public class LinkTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nLINKS\n============================");
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
    public void testRemoveLinks() {
        int loops = 5000;
        for (String pu : PU.getPuList()) {
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            System.out.println("\n=== REMOVE test using " + pu + " ===");
            removeByBatch(emf, loops);
            removeByEntity(emf, loops);
        }
    }

    private void removeByBatch(EntityManagerFactory emf, int loops) {
        PersonRecord root = new PersonRecord();
        PersonRecord[] persons = new PersonRecord[loops];
        for (int i = 0; i < loops; i++) {
            persons[i] = new PersonRecord();
        }
        Records records = new Records(emf);
        records.save(root);
        long start = System.currentTimeMillis();
        records.save(persons);
        long end = System.currentTimeMillis();
        System.out.println("  Inserting " + loops + " in " + (end - start) + "ms");
        start = System.currentTimeMillis();
        records.link(root).with(persons).save();
        end = System.currentTimeMillis();
        System.out.println("  Linking " + loops + " in " + (end - start) + "ms");

        start = System.currentTimeMillis();
        records.remove(persons);
        end = System.currentTimeMillis();
        System.out.println("  Removing By Batch Transaction " + loops + " in " + (end - start) + "ms");
        int count = records.query(PersonRecord.class).linking(root).count();
        assertEquals(0, count);
    }

    private void removeByEntity(EntityManagerFactory emf, int loops) {
        PersonRecord root = new PersonRecord();
        PersonRecord[] persons = new PersonRecord[loops];
        for (int i = 0; i < loops; i++) {
            persons[i] = new PersonRecord();
        }
        Records records = new Records(emf);
        records.save(root);
        long start = System.currentTimeMillis();
        records.save(persons);
        long end = System.currentTimeMillis();
        System.out.println("  Inserting " + loops + " in " + (end - start) + "ms");
        start = System.currentTimeMillis();
        records.link(root).with(persons).save();
        end = System.currentTimeMillis();
        System.out.println("  Linking " + loops + " in " + (end - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < loops; i++) {
            records.remove(persons[i]);
        }
        end = System.currentTimeMillis();
        System.out.println("  Removing By Single Transaction " + loops + " in " + (end - start) + "ms");
        int count = records.query(PersonRecord.class).linking(root).count();
        assertEquals(0, count);
    }

    @Test
    public void testLink() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Link CRUD test with " + pu + " ===");
            crud(PU.getFactoryList().get(pu));
            query(PU.getFactoryList().get(pu));
            attributes(PU.getFactoryList().get(pu));
        }
    }

    private void crud(EntityManagerFactory emf) {
        Records records = new Records(emf);

        PersonRecord p1 = new PersonRecord();
        PersonRecord p2 = new PersonRecord();
        PersonRecord p3 = new PersonRecord();
        records.save(p1, p2, p3);

        CommentRecord c12 = new CommentRecord();
        CommentRecord c13 = new CommentRecord();
        CommentRecord c23 = new CommentRecord();
        records.save(c12, c13, c23);

        records.link(c12).with(p1, p2).save();
        records.link(c13).with(p1, p3).save();
        records.link(c23).with(p2, p3).save();

        assertEquals(c12, records.query(CommentRecord.class).linking(p1, p2).getList().get(0));
        assertEquals(c13, records.query(CommentRecord.class).linking(p1, p3).getList().get(0));
        long start = System.currentTimeMillis();
        assertEquals(2, records.query(CommentRecord.class).linking(p1).count().intValue());
        long end = System.currentTimeMillis();
        System.out.println("  Counting links in " + (end - start) + "ms");
    }

    private void query(EntityManagerFactory emf) {
        Records records = new Records(emf);
        for (int i = 0; i < 200; i++) {
            PersonRecord p = new PersonRecord();
            p.setFirstName("FN" + i);
            p.setLastName("LN" + i);
            records.save(p);
        }
        long start = System.currentTimeMillis();
        assertEquals(2, records.query(PersonRecord.class).indexIn("firstName", "FN10", "FN1").count().intValue());
        long end = System.currentTimeMillis();
        System.out.println("  Counting Matches in " + (end - start) + "ms");
    }

    private void attributes(EntityManagerFactory emf) {
        Records records = new Records(emf);

        PersonRecord p1 = new PersonRecord();
        PersonRecord p2 = new PersonRecord();
        PersonRecord p3 = new PersonRecord();
        PersonRecord p4 = new PersonRecord();
        records.save(p1, p2, p3, p4);

        CommentRecord c12 = new CommentRecord();
        CommentRecord c13 = new CommentRecord();
        CommentRecord c14 = new CommentRecord();
        CommentRecord c23 = new CommentRecord();
        records.save(c12, c13, c23, c14);

        records.link(c12).with(p1, p2).setAttribute("c12").save();
        records.link(c13).with(p1, p3).setAttribute("c13").save();
        records.link(c23).with(p2, p3).setAttribute("c23").save();
        records.link(c14).with(p1, p4).setAttribute("c14").save();

        assertEquals(3, records.query(CommentRecord.class).linking(p1).count().intValue());
        assertEquals(c12, records.query(CommentRecord.class).linking(p1).attributeEqualTo("c12").getList().get(0));
        assertEquals(2, records.query(CommentRecord.class).linking(p1).attributeNotEqualTo("c12").count().intValue());

    }
}
