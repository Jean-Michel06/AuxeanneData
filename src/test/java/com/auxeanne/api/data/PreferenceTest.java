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

import java.text.DecimalFormat;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the preference utility.
 * 
 * @author Jean-Michel Tanguy
 */
public class PreferenceTest {

    private final int LOOPS = 3000;
    DecimalFormat df = new DecimalFormat("0.00 ms");

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nPREFERENCES\n============================");
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

    @Test
    public void writeRead() {
        for (String pu : PU.getPuList()) {
            readWrite(PU.getFactoryList().get(pu));
        }
    }

    @Test
    public void performance() {
        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Preferences performance for " + pu + "  " + LOOPS + " iterations");
            loop(PU.getFactoryList().get(pu));
        }
    }

    private void loop(EntityManagerFactory target) {
        EntityManager em = target.createEntityManager();
        Preferences preferences = new Preferences(em);
        // SETS
        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOPS; i++) {
            preferences.put("key" + i, "value" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println(" SETs : " + (end - start) + " ms ");
        // GETS
        start = System.currentTimeMillis();
        for (int i = 0; i < LOOPS; i++) {
            preferences.get("key" + i);
        }
        end = System.currentTimeMillis();
        System.out.println(" GETs : " + (end - start) + " ms ");
        // BATCH transaction
        start = System.currentTimeMillis();
        em.getTransaction().begin();
        for (int i = 0; i < LOOPS; i++) {
            preferences.put("batch" + i, "value" + i);
        }
        em.getTransaction().commit();
        end = System.currentTimeMillis();
        System.out.println(" Batch : " + (end - start) + " ms ");
    }

    private void readWrite(EntityManagerFactory target) {
        Preferences preferences = new Preferences(target);
        preferences.enableAudit("TestUser");
        // writing values
        preferences.put("key1", "value1");
        preferences.put("key2", "value2");
        // reading back values
        assertEquals("value1", preferences.get("key1"));
        assertEquals("value2", preferences.get("key2"));
        // reading back with new instance
        preferences = new Preferences(target);
        assertEquals("value1", preferences.get("key1"));
        assertEquals("value2", preferences.get("key2"));
    }

}
