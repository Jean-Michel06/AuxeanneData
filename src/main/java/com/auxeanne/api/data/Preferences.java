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

import com.auxeanne.api.data.ctrl.DatabaseController;
import com.auxeanne.api.data.db.Preference;
import com.auxeanne.api.data.ctrl.AuditLogger;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 * <p>Providing "Preferences" like feature but storing the values in the database.
 * POJO can be stored which is handy for global configuration.</p>
 * <p>Multi tenant is supported.</p>
 *
 * @author Jean-Michel Tanguy
 */
public class Preferences {

    /**
     * database controller
     */
    private DatabaseController mc;
    
    /**
     * default auditor is silent and must be activated with auditAs
     */
    private AuditLogger auditor = new AuditLogger();

    /**
     * Setting the controller with : container managed entity manager and bean
     * managed transaction (BMT). Resource must be JTA. Typically used in JSF
     * managed bean.
     *
     * @param em entity manager
     * @param utx user transaction
     */
    public Preferences(EntityManager em, UserTransaction utx) {
        mc = new DatabaseController(em, utx);
    }

    /**
     * Setting the controller with : container managed entity manager and
     * transactions (CMT). Resource must be JTA. Typically used within EJB.
     *
     * @param em entity manager
     */
    public Preferences(EntityManager em) {
        mc = new DatabaseController(em);
    }

    /**
     * Setting the controller with : bean managed entity and entity transaction.
     * Resource can be JTA or Local Resource. Typically used in Java SE or unit
     * tests
     *
     * @param emf entity manager factory
     */
    public Preferences(EntityManagerFactory emf) {
        mc = new DatabaseController(emf);
    }
    
    /**
     * Setting the controller with : bean managed entity and entity transaction.
     * Resource can be JTA or Local Resource. Typically used in Java SE or unit
     * tests
     *
     * @param emf entity manager factory
     * @param tenantId tenant key
     */
    public Preferences(EntityManagerFactory emf, String tenantId) {
        mc = new DatabaseController(emf, tenantId);
    }

    /**
     * Setting the controller with : bean managed entity and bean managed
     * transaction (BMT). Resource must be JTA. Typically used in EJB and
     * managed beans.
     *
     * @param emf entity manager factory
     * @param utx user transaction
     */
    public Preferences(EntityManagerFactory emf, UserTransaction utx) {
        mc = new DatabaseController(emf, utx);
    }
    
    /**
     * Setting the controller with : bean managed entity and bean managed
     * transaction (BMT). Resource must be JTA. Typically used in EJB and
     * managed beans. With tenant support.
     *
     * @param emf entity manager factory
     * @param utx user transaction
     * @param tenantId tenant key
     */
    public Preferences(EntityManagerFactory emf, UserTransaction utx, String tenantId) {
        mc = new DatabaseController(emf, utx, tenantId);
    }

    /**
     * retrieving stored preference for provided key
     *
     * @param key preference key
     * @return preference value or null if key does not exist
     */
    public String get(String key) {
        Preference preference = getPreference(key);
        if (preference == null) {
            return null;
        }
        return preference.getValue();
    }

    /**
     * storing preference
     *
     * @param key preference key
     * @param value preference value
     */
    public void put(String key, String value) {
        mc.transaction(() -> {
            Preference preference = getPreference(key);
            if (preference==null) {
                preference = new Preference(key, value);
                mc.getTransactionEntityManager().persist(preference);
            } else {
                preference.setValue(value);
            }
            auditor.logPreference(key, value);
        });
    }

    /**
     * mapping the preference to an object with fields annotated @RecordContent
     *
     * @param <T> Returned value matches the target class
     * @param key preference key
     * @param target class of the object to map
     * @return mapped object or null if key does not exist
     */
    public <T> T get(String key, Class<T> target) {
        String content = get(key);
        if (content == null) {
            return null;
        }
        T value = mc.fromWrapper(content, target); //gson.fromJson(content, c);
        return value;
    }

    /**
     * storing object fields annotated @RecordContent as preference
     *
     * @param key preference key
     * @param value mapped object
     */
    public void put(String key, Object value) {
        String content = mc.toWrapper(value); //gson.toJson(value);
        put(key, content);
    }

    /**
     * reading the entity from the database
     * @param key preference key
     * @return entity
     */
    private Preference getPreference(String key) {
       List<Preference> list = mc.getTransactionEntityManager().createQuery("SELECT p FROM Preference p WHERE p.key = :key ").setParameter("key", key).setMaxResults(1).getResultList();
       return (list.isEmpty())?null:list.get(0);
    }
    

    /**
     * enabling auditing
     * @param user name used for auditing logs
     */
    public void enableAudit(String user) {
        auditor = new AuditLogger(mc, user);
    }
    
    /**
     * disabling audit for better performances
     */
    public void disableAudit() {
        auditor = new AuditLogger();
    }
}
