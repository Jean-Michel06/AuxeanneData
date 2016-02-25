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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.eclipse.persistence.config.EntityManagerProperties;

/**
 * Called by all the tests to the databases to benchmark.
 * 
 * All database must allow a user (test/test) to access a ModelTest schema.
 * Check persistence.xml in "Other Test Sources" to setup test databases.
 * 
 * Provide one or more database target in "pus" to run all the tests. 
 *
 * @author Jean-Michel
 */
public class PU {

    // private static final String[] pus = {"JAVADB_TEST_PU","POSTGRES_TEST_PU", "MYSQL_TEST_PU","JAVADB_EMBEDDED_TEST_PU", "SQL_SERVER_TEST_PU","ORACLE_XE_TEST_PU"};
    private static final String[] pus = { "MYSQL_TEST_PU", "POSTGRES_TEST_PU", "MYSQL_TEST_PU", "POSTGRES_TEST_PU"};
    
    private static HashMap<String, EntityManagerFactory> map = null;
    private static List<String> puList;

    /**
     * providing the entity managers available for the tests
     * @return 
     */
    public static HashMap<String, EntityManagerFactory> getFactoryList() {
        if (map == null) {
            map = new HashMap<>();
            puList = new ArrayList();
            HashMap properties = new HashMap();
            properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, "TEST");

            for (String pu : pus) {
                try {
                    EntityManagerFactory factory = Persistence.createEntityManagerFactory(pu, properties);
                    map.put(pu, factory);
                    puList.add(pu);
                } catch (Exception e) {
                    // ignore just
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    /**
     * listing targeted entity managers for the tests
     * @return 
     */
    public static List<String> getPuList() {
        if (puList == null) {
            getFactoryList();
        }
        return puList;
    }

}
