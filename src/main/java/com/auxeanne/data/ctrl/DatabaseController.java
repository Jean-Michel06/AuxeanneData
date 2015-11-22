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
package com.auxeanne.data.ctrl;

import com.auxeanne.data.db.RecordType;
import com.auxeanne.data.db.RecordWrapper;
import com.auxeanne.data.FieldIndexing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.eclipse.persistence.exceptions.TransactionException;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaHelper;

/**
 * Providing common database features for "Records", "Preferences" and the fluent
 * APIs.
 *
 * @author Jean-Michel Tanguy
 */
public class DatabaseController {

    /**
     * setup for JSF managed bean with container transaction delegation
     */
    static private final int SETUP_EM_TX__JSF = 0;
    /**
     * setup for EJB
     */
    static private final int SETUP_EM__EJB = 1;
    /**
     * setup for SE and JUnit
     */
    static private final int SETUP_EMF__SE = 2;
    /**
     * setup for JEE with transaction container delegation
     */
    static private final int SETUP_EMF_TX__JSF_EJB = 3;
    /**
     * setup of the controller
     */
    private final int setup;

    /**
     * properties to manage multi tenant when creating entity manager
     */
    private final HashMap properties = new HashMap();

    /**
     * managed transaction for SETUP_EMF__SE
     */
    private EntityTransaction tx;
    /**
     * delegated transaction for SETUP_EM_TX__JSF and SETUP_EMF_TX__JSF_EJB
     */
    final private UserTransaction utx;

    /**
     * GSON Handler : using ISO 8601 date format
     */
    protected final Gson gson = new GsonBuilder().setExclusionStrategies(new RecordExclusionStrategy()).create();

    /**
     * managed entity manager
     */
    private EntityManager em;
    /**
     * factory for SETUP_EMF__SE and SETUP_EMF_TX__JSF_EJB
     */
    private final EntityManagerFactory emf;

    /**
     * memory cache for RecordType to improve global performance
     */
    private final HashMap<Class<? extends Record>, RecordType> typeCache = new HashMap<>();
    /**
     * memory cache for field indexing
     */
    private final HashMap<Class<? extends Record>, List<Field>> fieldCache = new HashMap<>();

    /**
     * Setting the controller with : container managed entity manager and bean
     * managed transaction (BMT). Resource must be JTA. Typically used in JSF
     * managed bean.<br>
     * <b>Must be instantiated for each transaction</b>
     *
     * @param em Entity manager
     * @param utx User transaction
     */
    public DatabaseController(EntityManager em, UserTransaction utx) {
        setup = SETUP_EM_TX__JSF;
        this.em = em;
        this.utx = utx;
        this.emf = null;
        
        JpaEntityManager jem = JpaHelper.getEntityManager(em);
        jem.getUnitOfWork().setShouldPerformDeletesFirst(true);
        this.em = jem;
    }

    /**
     * Setting the controller with : container managed entity manager and
     * transactions (CMT). Resource must be JTA. Typically used within EJB.<br>
     * <b>Must be instantiated for each transaction</b>
     *
     * @param em Entity manager
     */
    public DatabaseController(EntityManager em) {
        setup = SETUP_EM__EJB;
        this.em = em;
        this.utx = null;
        this.emf = null;
        
        JpaEntityManager jem = JpaHelper.getEntityManager(em);
        jem.getUnitOfWork().setShouldPerformDeletesFirst(true);
        this.em = jem;
    }

    /**
     * Setting the controller with : bean managed entity and entity transaction.
     * Resource can be JTA or Local Resource. Typically used in Java SE or unit
     * tests
     *
     * @param emf Entity manager factory
     */
    public DatabaseController(EntityManagerFactory emf) {
        setup = SETUP_EMF__SE;
        this.em = null;
        this.utx = null;
        this.emf = emf;
    }

    /**
     * Setting the controller with : bean managed entity and entity transaction.
     * Resource can be JTA or Local Resource. Typically used in Java SE or unit
     * tests. With tenant support.
     *
     * @param emf Entity manager factory
     * @param tenantId Tenant key
     */
    public DatabaseController(EntityManagerFactory emf, String tenantId) {
        setup = SETUP_EMF__SE;
        properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
        this.em = null;
        this.utx = null;
        this.emf = emf;
    }

    /**
     * Setting the controller with : bean managed entity and bean managed
     * transaction (BMT). Resource must be JTA. Typically used in EJB and
     * managed beans.
     *
     * @param emf Entity manager factory 
     * @param utx User transaction
     */
    public DatabaseController(EntityManagerFactory emf, UserTransaction utx) {
        setup = SETUP_EMF_TX__JSF_EJB;
        this.em = null;
        this.utx = utx;
        this.emf = emf;
    }

    /**
     * Setting the controller with : bean managed entity and bean managed
     * transaction (BMT). Resource must be JTA. Typically used in EJB and
     * managed beans. With tenant support.
     *
     * @param emf Entity manager factory
     * @param utx User transaction
     * @param tenantId Tenant key
     */
    public DatabaseController(EntityManagerFactory emf, UserTransaction utx, String tenantId) {
        properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
        setup = SETUP_EMF_TX__JSF_EJB;
        this.em = null;
        this.utx = utx;
        this.emf = emf;
    }

    //--------------------------------------------------------------------------
    // ENTITY MANAGER
    //--------------------------------------------------------------------------
    /**
     * serving appropriate entity manager from current transaction
     *
     * @return EntityManager
     */
    public EntityManager getTransactionEntityManager() {
        if (em == null || (!em.isOpen()&&emf!=null)) {
            em =null;
            // EclipseLink magic for optimal behavior on model usage by forcing delete first
            JpaEntityManager jem = JpaHelper.getEntityManager(emf.createEntityManager(properties));
            jem.getUnitOfWork().setShouldPerformDeletesFirst(true);
            em = jem;
        }
        return em;
    }

    /**
     * useful lambda to easily encapsulate a transaction
     *
     * @param runnable Runnable
     */
    public void transaction(Runnable runnable) {

        switch (setup) {
            case SETUP_EM__EJB:
                runnable.run();
                break;
            case SETUP_EMF__SE:
                transactionEMF(runnable);
                break;
            case SETUP_EMF_TX__JSF_EJB:
                transactionEMF_TX(runnable);
                break;
            case SETUP_EM_TX__JSF:
                transactionEM_TX(runnable);
                break;
        }
    }

    /**
     * transaction for SETUP_EMF_TX__JSF_EJB
     *
     * @param runnable Runnable
     */
    private void transactionEMF_TX(Runnable runnable) {
        try {
            if (utx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                //-- using existing transaction
                runnable.run();
            } else {
                EntityManager em_ = getTransactionEntityManager();
                try {
                    utx.begin();
                    em_.joinTransaction(); // check if needed
                    runnable.run();
                    utx.commit();
                } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                    // preventing database locking but still passing the exception
                    try {
                        utx.rollback();
                    } catch (IllegalStateException | SecurityException | SystemException ex1) {
                        Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    throw new TransactionException("Transaction rolled back after database exception.", ex);
                } catch (Exception ex) {
                    // any other exception passed as is after rollback
                    try {
                        utx.rollback();
                    } catch (IllegalStateException | SecurityException | SystemException ex1) {
                        Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    throw ex;
                } finally {
                    //-- keeping context 
                    em_.close();
                }
            }
        } catch (SystemException ex) {
            throw new TransactionException("Transaction System Exception.", ex);
        }
    }

    /**
     * transaction for SETUP_EM_TX__JSF
     *
     * @param runnable Runnable
     */
    private void transactionEM_TX(Runnable runnable) {
        try {
            // same behavior for Container managed and Bean managed Entity Manager
            // EJB, JSF Managed Beans
            if (utx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                //-- using existing transaction
                runnable.run();
            } else {
                try {
                    utx.begin();
                    runnable.run();
                    utx.commit();
                } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
                    // preventing database locking but still passing the exception
                    try {
                        utx.rollback();
                    } catch (IllegalStateException | SecurityException | SystemException ex1) {
                        Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    throw new TransactionException("Transaction rolled back after database exception.", ex);
                } catch (Exception ex) {
                    // any other exception passed as is after rollback
                    try {
                        utx.rollback();
                    } catch (IllegalStateException | SecurityException | SystemException ex1) {
                        Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                    throw ex;
                }
            }
        } catch (SystemException ex) {
            throw new TransactionException("Transaction System Exception.", ex);
        }

    }

    /**
     * SETUP_EMF__SE
     *
     * @param runnable Runnable
     */
    private void transactionEMF(Runnable runnable) {
        // Java SE
        if (tx != null && tx.isActive()) {
            // participating to existing transaction
            runnable.run();
        } else {
            EntityManager em_ = getTransactionEntityManager();
            try {
                tx = em_.getTransaction();
                tx.begin();
                runnable.run();
                tx.commit();
            } catch (SecurityException | IllegalStateException ex) {
                // preventing database locking but still passing the exception
                try {
                    tx.rollback();
                } catch (IllegalStateException | SecurityException ex1) {
                    Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex1);
                }
                throw new TransactionException("Transaction rolled back after database exception.", ex);
            } catch (Exception ex) {
                // any other exception passed as is after rollback
                try {
                    tx.rollback();
                } catch (IllegalStateException | SecurityException ex1) {
                    Logger.getLogger(DatabaseController.class.getName()).log(Level.SEVERE, null, ex1);
                }
                throw ex;
            } finally {
                //-- keeping context 
                //-- Unmcomment to recreate EntityManager on each transaction
                em_.close();
            }
        }
    }

    /**
     * json to instance
     *
     * @param <T> Type to return 
     * @param data JSON string
     * @param modelClass Class to convert to
     * @return POJO
     */
    public <T> T fromWrapper(String data, Class<T> modelClass) {
        return gson.fromJson(data, modelClass);
    }

    /**
     * instance to JSON
     *
     * @param <T> Type to convert
     * @param model POJO to convert
     * @return JSON string
     */
    public <T> String toWrapper(T model) {
        return gson.toJson(model);
    }

    /**
     * encapsulating a record and its properties in a POJO
     *
     * @param <T> Type to return 
     * @param wrapper Entity from database
     * @param recordClass Targeted class
     * @return POJO
     */
    public <T extends Record> T getRecord(Class<T> recordClass, RecordWrapper wrapper) {
        String data = wrapper.getData();
        T model = fromWrapper(data, recordClass);
        model.setId(wrapper.getId());
        model.setDocument(wrapper.getDocument());
        model.setDocumentChanged(false);
        return model;
    }

    /**
     * mapping the record model to its entity type using cache
     *
     * @param <T> All POJOs must extend DefaultRecord
     * @param recordClass class of model to map
     * @param skipCreation saves time
     * @return the mapped recordType to the model class
     */
    public <T extends Record> RecordType getType(Class<T> recordClass, boolean skipCreation) {
        RecordType recordType = typeCache.get(recordClass);
        if (recordType == null) {
            String code = recordClass.getName();
            List<RecordType> list = getTransactionEntityManager().createNamedQuery("RecordType.findByCode", RecordType.class).setParameter("code", code).setMaxResults(1).getResultList();
            if (!list.isEmpty()) {
                recordType = list.get(0);
                typeCache.put(recordClass, recordType);
            } else if (!skipCreation) {
                final RecordType rt = new RecordType();
                rt.setCode(code);
                transaction(() -> getTransactionEntityManager().persist(rt));
                typeCache.put(recordClass, rt);
                recordType = rt;
            }
        }
        return recordType;
    }

    /**
     * list (and cache) all the fields of a class with indexing annotation
     * @param <T> All POJOs must extend DefaultRecord
     * @param recordClass Class to parse for indexed field
     * @return List of field with indexing annotation
     */
    public <T extends Record> List<Field> getIndexingField(Class<T> recordClass) {
        List<Field> list = fieldCache.get(recordClass);
        if (list == null) {
            List<Field> indexingList = new ArrayList<>();
            Field[] fields = recordClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(FieldIndexing.class)) {
                    indexingList.add(field);
                }
            }
            fieldCache.put(recordClass, indexingList);
            list = indexingList;
        }
        return list;
    }

}
