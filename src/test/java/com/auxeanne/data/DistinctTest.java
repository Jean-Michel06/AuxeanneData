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
 * Targeting links and paths correct behavior and performance.
 *
 * @author Jean-Michel Tanguy
 */
public class DistinctTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nDISTINCT\n============================");
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
    public void testLinkPerformance() {
        int loops = 5000;
        for (String pu : PU.getPuList()) {
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            System.out.println("\n=== Link Performance test using " + pu + " ===");
            testLinkPerformance(emf, loops);
        }
    }

    @Test
    public void testPathPerformance() {
        int loops = 5000;
        for (String pu : PU.getPuList()) {
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            System.out.println("\n=== Path Performance test using " + pu + " ===");
            testPathPerformance(emf, loops);
        }
    }

    private void testLinkPerformance(EntityManagerFactory emf, int loops) {
        Records records = new Records(emf);
        long start, end;

        PersonRecord parent = new PersonRecord();
        records.save(parent);
        // create childs
        start = System.currentTimeMillis();
        List<PersonRecord> childList = new ArrayList<>();
        for (int i = 0; i < loops; i++) {
            PersonRecord p = new PersonRecord();
            childList.add(p);
        }
        records.save(childList);
        end = System.currentTimeMillis();
        System.out.println("    Batch save of " + loops + " in " + (end - start) + "ms");

        // linking child
        start = System.currentTimeMillis();
        for (PersonRecord p : childList) {
            records.link(p).with(parent).save();
        }
        end = System.currentTimeMillis();
        System.out.println("    Link 1 by 1 " + loops + " in " + (end - start) + "ms");
        
        // query childs
        start = System.currentTimeMillis();
        List list = records.query(PersonRecord.class).linking(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Warm up " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());

        // query childs
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).linking(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Query " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());

        // query childs with Any query
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).linkingAny(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Query ANY " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());
    }

    private void testPathPerformance(EntityManagerFactory emf, int loops) {
        Records records = new Records(emf);
        long start, end;

        PersonRecord parent = new PersonRecord();
        records.save(parent);
        // create childs
        start = System.currentTimeMillis();
        List<PersonRecord> childList = new ArrayList<>();
        for (int i = 0; i < loops; i++) {
            PersonRecord p = new PersonRecord();
            childList.add(p);
        }
        records.save(childList);
        end = System.currentTimeMillis();
        System.out.println("    Batch save of " + loops + " in " + (end - start) + "ms");

        // child
        start = System.currentTimeMillis();
        for (PersonRecord p : childList) {
            records.link(p).asChildOf(parent).save();
        }
        end = System.currentTimeMillis();
        System.out.println("    Child 1 by 1 " + loops + " in " + (end - start) + "ms");
        
        // query childs
        start = System.currentTimeMillis();
        List list = records.query(PersonRecord.class).below(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Warm up Query " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());

        // query childs
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).below(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Below Query " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());

        // query childs with Any query
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).belowAny(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Below Query ANY " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());
        
        // query childs
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).childOf(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    Warmu up Query " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());

        // query childs
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).childOf(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    ChildOf Query " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());

        // query childs with Any query
        start = System.currentTimeMillis();
        list = records.query(PersonRecord.class).childOfAny(parent).getList();
        end = System.currentTimeMillis();
        System.out.println("    ChildOf Query ANY " + loops + " in " + (end - start) + "ms");
        assertEquals(loops, list.size());
    }

    @Test
    public void testDistinct() {
        for (String pu : PU.getPuList()) {
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            System.out.println("\n=== Distinct test using " + pu + " ===");
            testLink(emf);
            testPath(emf);
        }
    }

    private void testLink(EntityManagerFactory emf) {
        Records records = new Records(emf);

        PersonRecord parent1 = new PersonRecord();
        PersonRecord parent2 = new PersonRecord();
        PersonRecord child0 = new PersonRecord();
        PersonRecord child1 = new PersonRecord();
        PersonRecord child2 = new PersonRecord();

        records.save(parent1, parent2, child0, child1, child2);

        records.link(child0).with(parent1, parent2).save();
        records.link(child0).with(parent1).save(); // check for side effect of relinking
        records.link(child1).with(parent1).save();
        records.link(child2).with(parent2).save();

        //-- matching 2 parents
        assertEquals(1, records.query(PersonRecord.class).linking(parent1, parent2).getList().size());
        assertEquals(3, records.query(PersonRecord.class).linkingAny(parent1, parent2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).linking(parent1, parent2).count().intValue());
        assertEquals(3, records.query(PersonRecord.class).linkingAny(parent1, parent2).count().intValue());

        assertEquals(child0.getId(), records.query(PersonRecord.class).linking(parent1, parent2).getFirst().getId());
        assertNotNull(records.query(PersonRecord.class).linkingAny(parent1, parent2).getFirst());

        //-- matching parent 1
        assertEquals(2, records.query(PersonRecord.class).linking(parent1).getList().size());
        assertEquals(2, records.query(PersonRecord.class).linkingAny(parent1).getList().size());

        assertEquals(2, records.query(PersonRecord.class).linking(parent1).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).linkingAny(parent1).count().intValue());

        assertNotNull(records.query(PersonRecord.class).linking(parent1).getFirst());
        assertNotNull(records.query(PersonRecord.class).linkingAny(parent1).getFirst());

    }

    private void testPath(EntityManagerFactory emf) {
        Records records = new Records(emf);

        PersonRecord parent1 = new PersonRecord();
        PersonRecord parent2 = new PersonRecord();
        PersonRecord child1 = new PersonRecord();
        PersonRecord child2 = new PersonRecord();
        records.save(parent1, parent2, child1, child2);
        records.link(child1).asChildOf(parent1, parent2).save();
        records.link(child2).asChildOf(parent2).save();

        //-- childs matching 2 parents
        assertEquals(1, records.query(PersonRecord.class).below(parent1, parent2).getList().size());
        assertEquals(2, records.query(PersonRecord.class).belowAny(parent1, parent2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).below(parent1, parent2).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).belowAny(parent1, parent2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).below(parent1, parent2).getFirst());
        assertNotNull(records.query(PersonRecord.class).belowAny(parent1, parent2).getFirst());

        assertEquals(1, records.query(PersonRecord.class).childOf(parent1, parent2).getList().size());
        assertEquals(2, records.query(PersonRecord.class).childOfAny(parent1, parent2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).childOf(parent1, parent2).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).childOfAny(parent1, parent2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).childOf(parent1, parent2).getFirst());
        assertNotNull(records.query(PersonRecord.class).childOfAny(parent1, parent2).getFirst());

        //-- childs matching parent 1
        assertEquals(1, records.query(PersonRecord.class).below(parent1).getList().size());
        assertEquals(1, records.query(PersonRecord.class).belowAny(parent1).getList().size());

        assertEquals(1, records.query(PersonRecord.class).below(parent1).count().intValue());
        assertEquals(1, records.query(PersonRecord.class).belowAny(parent1).count().intValue());

        assertNotNull(records.query(PersonRecord.class).below(parent1).getFirst());
        assertNotNull(records.query(PersonRecord.class).belowAny(parent1).getFirst());

        assertEquals(1, records.query(PersonRecord.class).childOf(parent1).getList().size());
        assertEquals(1, records.query(PersonRecord.class).childOfAny(parent1).getList().size());

        assertEquals(1, records.query(PersonRecord.class).childOf(parent1).count().intValue());
        assertEquals(1, records.query(PersonRecord.class).childOfAny(parent1).count().intValue());

        assertNotNull(records.query(PersonRecord.class).childOf(parent1).getFirst());
        assertNotNull(records.query(PersonRecord.class).childOfAny(parent1).getFirst());

        //-- childs matching parent 2
        assertEquals(2, records.query(PersonRecord.class).below(parent2).getList().size());
        assertEquals(2, records.query(PersonRecord.class).belowAny(parent2).getList().size());

        assertEquals(2, records.query(PersonRecord.class).below(parent2).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).belowAny(parent2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).below(parent2).getFirst());
        assertNotNull(records.query(PersonRecord.class).belowAny(parent2).getFirst());

        assertEquals(2, records.query(PersonRecord.class).childOf(parent2).getList().size());
        assertEquals(2, records.query(PersonRecord.class).childOfAny(parent2).getList().size());

        assertEquals(2, records.query(PersonRecord.class).childOf(parent2).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).childOfAny(parent2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).childOf(parent2).getFirst());
        assertNotNull(records.query(PersonRecord.class).childOfAny(parent2).getFirst());

        //-- parents matching 2 childs
        assertEquals(1, records.query(PersonRecord.class).above(child1, child2).getList().size());
        assertEquals(2, records.query(PersonRecord.class).aboveAny(child1, child2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).above(child1, child2).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).aboveAny(child1, child2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).above(child1, child2).getFirst());
        assertNotNull(records.query(PersonRecord.class).aboveAny(child1, child2).getFirst());

        assertEquals(1, records.query(PersonRecord.class).parentOf(child1, child2).getList().size());
        assertEquals(2, records.query(PersonRecord.class).parentOfAny(child1, child2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).parentOf(child1, child2).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).parentOfAny(child1, child2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).parentOf(child1, child2).getFirst());
        assertNotNull(records.query(PersonRecord.class).parentOfAny(child1, child2).getFirst());

        //-- parent matching child 1
        assertEquals(2, records.query(PersonRecord.class).above(child1).getList().size());
        assertEquals(2, records.query(PersonRecord.class).aboveAny(child1).getList().size());

        assertEquals(2, records.query(PersonRecord.class).above(child1).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).aboveAny(child1).count().intValue());

        assertNotNull(records.query(PersonRecord.class).above(child1).getFirst());
        assertNotNull(records.query(PersonRecord.class).aboveAny(child1).getFirst());

        assertEquals(2, records.query(PersonRecord.class).parentOf(child1).getList().size());
        assertEquals(2, records.query(PersonRecord.class).parentOfAny(child1).getList().size());

        assertEquals(2, records.query(PersonRecord.class).parentOf(child1).count().intValue());
        assertEquals(2, records.query(PersonRecord.class).parentOfAny(child1).count().intValue());

        assertNotNull(records.query(PersonRecord.class).parentOf(child1).getFirst());
        assertNotNull(records.query(PersonRecord.class).parentOfAny(child1).getFirst());

        //-- parent matching child 2
        assertEquals(1, records.query(PersonRecord.class).above(child2).getList().size());
        assertEquals(1, records.query(PersonRecord.class).aboveAny(child2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).above(child2).count().intValue());
        assertEquals(1, records.query(PersonRecord.class).aboveAny(child2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).above(child2).getFirst());
        assertNotNull(records.query(PersonRecord.class).aboveAny(child2).getFirst());

        assertEquals(1, records.query(PersonRecord.class).parentOf(child2).getList().size());
        assertEquals(1, records.query(PersonRecord.class).parentOfAny(child2).getList().size());

        assertEquals(1, records.query(PersonRecord.class).parentOf(child2).count().intValue());
        assertEquals(1, records.query(PersonRecord.class).parentOfAny(child2).count().intValue());

        assertNotNull(records.query(PersonRecord.class).parentOf(child2).getFirst());
        assertNotNull(records.query(PersonRecord.class).parentOfAny(child2).getFirst());

    }

}
