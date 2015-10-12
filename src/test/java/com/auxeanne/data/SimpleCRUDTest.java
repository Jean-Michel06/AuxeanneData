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
import com.auxeanne.data.record.SimpleRecord;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Basic CRUD test
 * @author Jean-Michel Tanguy
 */
public class SimpleCRUDTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nSIMPLE CRUD\n============================");
        System.out.println("SHOW SQL SHOULD BE ENABLED FOR DEEP TRACE ANALYSIS ON THIS TEST");
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
    public void simple() {

        for (String pu : PU.getPuList()) {
            System.out.println("\n== Testing " + pu);
            EntityManagerFactory emf = PU.getFactoryList().get(pu);
            Records records = new Records(emf);
            records.enableAudit(  "TestUser" );
            // Data Set
            SimpleRecord p1 = new SimpleRecord();
            p1.setData("p2");
            SimpleRecord p2 = new SimpleRecord();
            p2.setData("p2");
            SimpleRecord p3 = new SimpleRecord();
            p3.setData("p3");
            CommentRecord c1 = new CommentRecord();
            CommentRecord c2 = new CommentRecord();
            CommentRecord c3 = new CommentRecord();
            
            CommentRecord c11  = new CommentRecord();
            CommentRecord c12 = new CommentRecord();
            
            System.out.println("---- CREATE ---------------------------------");
            records.save(p1);
            records.save(p2);
            records.save(p3);
            records.save(c1,c2,c3);
            records.save(c11,c12);
            System.out.println("---- UPDATE ----------------------------------");
            p1.setData("UPDATED");
            records.save(p1);
            records.save(p2);
            records.save(p3);
            records.save(c1,c2,c3);
            System.out.println("---- LINK ------------------------------------");
            records.link(p1).with(c1).save();
            records.link(p1).with(c2,c3).save();
            records.link(p1).with(c2,c3).save();// recreate
            records.link(p1).with(c1).setAttribute("TAG").save();
            assertEquals("TAG", records.link(c1).with(p1).getAttribute());
     
            System.out.println("---- COUNT -----------------------------------");
            assertEquals(3L, records.query(SimpleRecord.class).count().longValue());
            System.out.println("---- READ ------------------------------------");
            assertEquals(3L, records.query(SimpleRecord.class).getList().size());
            System.out.println("---- PATH ------------------------------------");
            records.link(c1).asParentOf(c11,c12).save();
            records.link(c1).asChildOf(p1).save();
            records.link(c1).asChildOf(p2).save();
            records.link(c1).asChildOf(p2).save(); // recreate
            System.out.println("---- PATH COUNT------------------------------");
            assertEquals(3L, records.query(CommentRecord.class).below(p1).count().longValue());
            assertEquals(2L, records.query(CommentRecord.class).below(c1).count().longValue());
            assertEquals(2L, records.query(CommentRecord.class).childOf(c1).count().longValue());
            assertEquals(2L, records.query(SimpleRecord.class).above(c11).count().longValue());
            assertEquals(3L, records.query(CommentRecord.class).belowAny(p1,p2).count().longValue());
            assertEquals(3L, records.query(CommentRecord.class).below(p1,p2).count().longValue());
            System.out.println("---- DELETE WITH LINK AND PATH --------------");
            records.remove(p1);
            records.remove(p2,p3);
        }
    }

}
