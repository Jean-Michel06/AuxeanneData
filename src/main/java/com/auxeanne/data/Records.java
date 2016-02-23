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

import com.auxeanne.data.ctrl.Record;
import com.auxeanne.data.db.RecordIndex;
import com.auxeanne.data.db.RecordIndexPK;
import com.auxeanne.data.db.RecordLink;
import com.auxeanne.data.db.RecordLinkPK;
import com.auxeanne.data.db.RecordPath;
import com.auxeanne.data.db.RecordPathPK;
import com.auxeanne.data.ctrl.DatabaseController;
import com.auxeanne.data.db.RecordWrapper;
import com.auxeanne.data.db.RecordType;
import com.auxeanne.data.ctrl.AuditLogger;
import com.auxeanne.data.ctrl.FluentLink;
import com.auxeanne.data.ctrl.FluentQuery;
import com.auxeanne.data.ctrl.FluentQuery.QueryBuilder;
import com.auxeanne.data.ctrl.IndexQueryManager;
import com.auxeanne.data.ctrl.ParameterManager;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import org.eclipse.persistence.queries.CursoredStream;

/**
 * <p>
 * "Records" provides all the CRUD features to manipulate, store, query POJOs
 * from/to the database.</p>
 * <p>
 * POJOs must extend DefaultRecord (or implement the interface Record).<br>
 * Fields are excluded from database using annotation RecordExclusion and
 * indexed using the annotation FieldIndexing.</p>
 * <p>
 * @XmlRootElement is optional in the record extended class.</p>
 * <p>
 * Records relies on a persistence unit to connect to the database, i.e.
 * persistence.xml must be configured accordingly. Please see the TEST resources
 * to find examples for several databases.</p>
 * <p>
 * Multi-tenant is supported and is configured in the entity manager or the
 * entity manager factory.</p>
 * <p>
 * The DefaultRecord class supports also a binary document to save images or
 * other binary files in the database as part of a record. Binary insertion in
 * the database has been preferred to keep coherence when manipulating data
 * (versioning, backup, ...)</p>
 * <p>
 * Links and queries are facilitated by fluent APIs.</p>
 *
 * @author Jean-Michel Tanguy
 */
public class Records {

    /**
     * some databases limit the number of parameter to bind to a statement (ex:
     * PostgreSql)
     */
    protected static final int PARAMETER_PAGING = 32000;

    /**
     * default auditor is silent and must be activated with auditAs
     */
    private AuditLogger auditor = new AuditLogger();

    /**
     * database controller
     */
    private final DatabaseController mc;

    /**
     * mapping attributes or indexes to the database model
     */
    private final ParameterManager pm = new ParameterManager();

    /**
     * Setting the controller with : container managed entity manager and bean
     * managed transaction (BMT). Resource must be JTA. Typically used in JSF
     * managed bean.
     *
     * @param em entity manager
     * @param utx user transaction
     */
    //  public Records(EntityManager em, UserTransaction utx) {
    //      mc = new DatabaseController(em, utx);
    //  }
    /**
     * Setting the controller with : container managed entity manager and
     * transactions (CMT). Resource must be JTA. Typically used within EJB.
     *
     * @param em entity manager
     */
    //   public Records(EntityManager em) {
    //       mc = new DatabaseController(em);
    //   }
    /**
     * Setting the controller with : bean managed entity and entity transaction.
     * Resource can be JTA or Local Resource. Typically used in Java SE or unit
     * tests
     *
     * @param emf entity manager factory
     */
    public Records(EntityManagerFactory emf) {
        mc = new DatabaseController(emf);
    }

    /**
     * Setting the controller with : bean managed entity and entity transaction.
     * Resource can be JTA or Local Resource. Typically used in Java SE or unit
     * tests. With tenant support.
     *
     * @param emf entity manager factory
     * @param tenantId tenant key
     */
    public Records(EntityManagerFactory emf, String tenantId) {
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
    public Records(EntityManagerFactory emf, UserTransaction utx) {
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
    public Records(EntityManagerFactory emf, UserTransaction utx, String tenantId) {
        mc = new DatabaseController(emf, utx, tenantId);
    }

    /**
     * saving one or more records to the database
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param records records to save in the database
     */
    public <T extends Record> void save(T... records) {
        save(Arrays.asList(records));
    }

    /**
     * Saving records to the database. JPA handles bath writing, so parameter
     * paging can be ignored (vs remove).
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param list list of records to save in the database
     */
    public <T extends Record> void save(List<T> list) {
        if (list != null) {
            mc.transaction(() -> {
                list.stream().filter((record) -> (record != null)).forEach((record) -> {
                    Long recordId = record.getId();
                    if (recordId == null) {
                        // persisting new record
                        RecordWrapper wrapper = createRecordWrapper(record);
                        indexRecord(true, record);
                        auditor.logCreateRecord(wrapper);
                    } else {
                        // using reference as only SET is necesssary
                        RecordWrapper wrapper = mc.getTransactionEntityManager().getReference(RecordWrapper.class, recordId); // getReference creating two queries including RecordType
                        wrapper.setData(mc.toWrapper(record));
                        if (record.isDocumentChanged()) {
                            wrapper.setDocument(record.getDocument());
                        }
                        indexRecord(false, record);
                        auditor.logUpdateRecord(wrapper, record.isDocumentChanged());
                    }
                });
            });
        }
    }
    
    /**
     * parsing record to extract indexed fields
     *
     * @param isNew record has never been parsed
     * @param record record to parse
     */
    private void indexRecord(boolean isNew, Record record) {
        for (Field field : mc.getIndexingField(record.getClass())) {
            try {
                // indexes can be shared by providing common key, or default key using field path is used
                String key = field.getAnnotation(FieldIndexing.class).value();
                String name = field.getName();
                if (key.length() == 0) {
                    key = record.getClass().getName() + "." + name;
                }
                // reading field value to add to index list
                boolean status = field.isAccessible();
                field.setAccessible(true);
                Object fieldValue = field.get(record);
                field.setAccessible(status);
                // database access
                EntityManager em = mc.getTransactionEntityManager();
                RecordIndexPK pk = new RecordIndexPK(key, record.getId(), name);
                // forcing indexe deletion to preserve record batch save (npreveting select for each save)
                // Note : make sure the ModelController provides an entity manager with setShouldPerformDeletesFirst(true)
                if (!isNew) {
                    em.remove(em.getReference(RecordIndex.class, pk));
                }
                // mapping the index to the database
                Comparable converted = pm.getConverted(fieldValue);
                if (converted != null) {
                    RecordIndex ri = new RecordIndex(pk);
                    switch (pm.getType(field.getType())) {
                        case DATE:
                            ri.setDate((Date) converted);
                            break;
                        case STRING:
                            ri.setValue((String) converted);
                            break;
                        case NUMERIC:
                            ri.setNumeric((BigDecimal) converted);
                            break;
                    }
                    // saving
                    em.persist(ri);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                // convet to runtime exception
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * deleting one or more records from the database
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param records records to remove from the database
     */
    public <T extends Record> void remove(T... records) {
        Records.this.remove(Arrays.asList(records));
    }

    /**
     * deleting records from the database
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param records list of records to remove from the database
     */
    public <T extends Record> void remove(List<T> records) {
        if (!records.isEmpty()) {
            //-- removing records by max batch of PARAMETER_PAGING which is the max number of paramaters supported by some databases (ex PostgreSQL)
            //-- single transaction for all deletes to preserve integrity and faster processing (batch SQL).
            mc.transaction(() -> {
                int start = 0;
                int end = Math.min(records.size(), PARAMETER_PAGING);
                while (start < records.size()) {
                    removeTransaction(records.subList(start, end));
                    start = end;
                    end = Math.min(records.size(), end + PARAMETER_PAGING);
                }
            });
            //-- if transaction is successfull unvalidating IDs
            records.stream().forEach((record) -> {
                record.setId(-1L);
            });
        }
    }

    /**
     * Logic removing the record as well as the links. Multiple trials are
     * preserved in comments just for reminder on current reference
     * implementation.
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param recordList list of records to remove
     */
    private <T extends Record> void removeTransaction(List<T> recordList) {
        //String recordType = records.get(0).getClass().getName();
        //-- extracting IDs for batch processing
        ArrayList<Long> idList = new ArrayList<>();
        for (Record record : recordList) {
            idList.add(record.getId());
            //-- POSTGRES won't perform correctly with multiple delete query
            // mc.getEntityManager().createNamedQuery("RecordLink.deleteByRecordId").setParameter("record", record.getId()).executeUpdate();
            // mc.getEntityManager().createNamedQuery("RecordWrapper.deleteByRecordId").setParameter("record", record.getId()).executeUpdate();
            // record.setId(null);
            //------------------------------------------------------------------
        }
        //-- DELETE query less efficient than JPA remove (= batch) by x3
        // mc.getEntityManager().createNamedQuery("RecordLink.deleteByRecordIdList").setParameter("list", idList).executeUpdate();
        // mc.getEntityManager().createNamedQuery("RecordWrapper.deleteByRecordIdList").setParameter("list", idList).executeUpdate();
        //----------------------------------------------------------------------
        EntityManager em = mc.getTransactionEntityManager();
        //-- removing links
        //-- two queries to fetch links is more efficient than a single with OR by x10
        //-- CursoredStream is effective in memory management and helps to get better performances than EAGER fetch
        CursoredStream pkList1 = (CursoredStream) em.createNamedQuery("RecordLink.findByReferenceIdList")
                .setParameter("list", idList)
                .setHint("eclipselink.cursor", true)
                .getSingleResult();
        CursoredStream pkList2 = (CursoredStream) em.createNamedQuery("RecordLink.findByLinkIdList")
                .setParameter("list", idList)
                .setHint("eclipselink.cursor", true)
                .getSingleResult();

        //-- using reference to avoid complete loading of obsolete objects
        while (!pkList1.atEnd()) {
            for (Object pkO : pkList1.next(100)) {
                RecordLinkPK pk = (RecordLinkPK) pkO;
                em.remove(em.getReference(RecordLink.class, pk));
                auditor.logRemoveLink(pk.getReference_(), pk.getLink_());
            }
        }
        pkList1.close();
        while (!pkList2.atEnd()) {
            for (Object pkO : pkList2.next(100)) {
                RecordLinkPK pk = (RecordLinkPK) pkO;
                em.remove(em.getReference(RecordLink.class, pk));
                // no log here as duplicated
            }
        }
        pkList2.close();

        //-- removing paths
        List<RecordPathPK> childList = em.createNamedQuery("RecordPath.findChildFromIdList").setParameter("list", idList).getResultList();
        for (RecordPathPK path : childList) {
            removePath(path.getParent(), path.getChild());
            auditor.logRemovePath(path.getParent(), path.getChild());
        }
        List<RecordPathPK> parentList = em.createNamedQuery("RecordPath.findParentFromIdList").setParameter("list", idList).getResultList();
        for (RecordPathPK path : parentList) {
            removePath(path.getParent(), path.getChild());
            auditor.logRemovePath(path.getParent(), path.getChild());
        }

        //-- removing indexes
        List<RecordIndexPK> indexList = em.createNamedQuery("RecordIndex.findIndexFromIdList").setParameter("list", idList).getResultList();
        indexList.stream().forEach((index) -> {
            em.remove(em.getReference(RecordIndex.class, index));
        });

        //-- removing records
        for (Long id : idList) {
            RecordWrapper rw = em.getReference(RecordWrapper.class, id);
            em.remove(rw);
            auditor.logRemoveRecord(rw);
        }

    }

    /*
     //-- KEPT AS KNOWLEDGE REFERENCE
     //-- single  query less efficient than batch remove 
     public void remove(Record record) {
     mc.transaction(() -> {
     //-- JPA parameterized query
     //-- MYSQL will perform with native query and note HQL. JPA overhead seems costly.
     mc.getEntityManager().createNamedQuery("RecordLink.deleteByRecordId").setParameter("record", record.getId()).executeUpdate();
     mc.getEntityManager().createNamedQuery("RecordWrapper.deleteByRecordId").setParameter("record", record.getId()).executeUpdate();

     //-- Parameterized native query
     //mc.getEntityManager().createNativeQuery("DELETE FROM record_link  WHERE  link_ = ?1 OR reference_ = ?1").setParameter(1, record.getId()).executeUpdate();
     //mc.getEntityManager().createNativeQuery("DELETE FROM record_wrapper  WHERE id_ = ?1").setParameter(1, record.getId()).executeUpdate();
     //-- Parameterized native named query - turn on statement caching
     //mc.getEntityManager().createNamedQuery("RecordLink.deleteByRecordId.native").setParameter(1, record.getId()).executeUpdate();
     //mc.getEntityManager().createNamedQuery("RecordWrapper.deleteByRecordId.native").setParameter(1, record.getId()).executeUpdate();
     //-- Native query
     //int id = record.getId();
     //mc.getEntityManager().createNativeQuery("DELETE FROM record_link  WHERE  link_ = "+id+" OR reference_ = "+id).executeUpdate();
     //mc.getEntityManager().createNativeQuery("DELETE FROM record_wrapper  WHERE id_ = "+id).executeUpdate(); 
     });
     record.setId(null);
     }
     */

    /**
     * saving and cloning records
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param recordClass class of the records to clone
     * @param records records to clone
     * @return list of cloned and saved records
     */
    public <T extends Record> List<T> clone(Class<T> recordClass, T... records) {
        RecordWrapper cloneWrapper = new RecordWrapper();
        List<T> list = new ArrayList<>();
        mc.transaction(() -> {
            // saving records
            save(records);
            // cloning
            for (T record : records) {
                RecordWrapper wrapper = mc.getTransactionEntityManager().find(RecordWrapper.class, record.getId());
                cloneWrapper.setRecordType(wrapper.getRecordType());
                if (wrapper.getData() != null) {
                    cloneWrapper.setData(wrapper.getData());
                }
                if (wrapper.getDocument() != null) {
                    cloneWrapper.setDocument(wrapper.getDocument());
                }
                // performing database operations
                mc.getTransactionEntityManager().persist(cloneWrapper);
                T recordClone = mc.getRecord(recordClass, wrapper);
                indexRecord(true, recordClone);
                auditor.logCreateRecord(cloneWrapper);
                list.add(recordClone);
            }
        });
        return list;
    }

    /**
     * fluent link API to manage bidirectional and hierarchical links
     *
     * @param reference record to link
     * @return Fluent Link
     */
    public FluentLink.ConnectionBuilder link(Record reference) {
        return new FluentLink.Builder(mc, reference, auditor);
    }

    /**
     * fluent query API
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param referenceClass record type to retrieve from the database
     * @return Fluent Query
     */
    public <T extends Record> QueryBuilder<T> query(Class<T> referenceClass) {
        return new FluentQuery.Builder<>(mc, referenceClass);
    }
    
    /**
     * Gives the type of a record from its id.
     * Main purpose is to check if the manipulated id has the correct type.
     * @param id
     * @return type name
     */
    public String getRecordType(Long id) {
        return mc.getTransactionEntityManager().find(RecordWrapper.class, id).getRecordType().getCode();
    }
    

    /**
     * enabling auditing
     *
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

    //--------------------------------------------------------------------------
    // Record internal
    //--------------------------------------------------------------------------
    /**
     * listing "distinct" and sorted values from an index key
     *
     * @param key index key to query as defined in the FieldIndexing annotation
     * (if not empty)
     * @return list of string of available values for the index
     */
    public List<String> getIndexList(String key) {
        List selectedList = mc.getTransactionEntityManager().createNamedQuery("RecordIndex.findValueFromKey").setParameter("key", key).getResultList();
        if (selectedList.isEmpty()) {
            selectedList = mc.getTransactionEntityManager().createNamedQuery("RecordIndex.findDateFromKey").setParameter("key", key).getResultList();
        }
        if (selectedList.isEmpty()) {
            selectedList = mc.getTransactionEntityManager().createNamedQuery("RecordIndex.findNumericFromKey").setParameter("key", key).getResultList();
        }

        List<String> list = new ArrayList();
        for (Object o : selectedList) {
            list.add(o.toString());
        }
        return list;
    }

    /**
     * listing "distinct" and sorted values from an indexed field
     *
     * @param targetClass class of the record with the indexed field
     * @param targetField indexed field
     * @return list of string of available values for the index
     */
    public List<String> getIndexList(Class targetClass, String targetField) {
        String key = targetClass.getName() + "." + targetField;
        return getIndexList(key);
    }
    
    
    //--------------------------------------------------------------------------
    // Transactions
    //--------------------------------------------------------------------------
    
    /**
     * spawn transaction for multiple accesses
     * @param runnable 
     */
    public void transaction(Runnable runnable) {
        mc.transaction(runnable);
    }

    //--------------------------------------------------------------------------
    // private helper
    //--------------------------------------------------------------------------
    /**
     * creating a new record
     *
     * @param <T> POJOs must extend DefaultRecord
     * @param record record to save
     * @return the entity encapsulating the record in the database
     */
    private <T extends Record> RecordWrapper createRecordWrapper(T record) {
        RecordWrapper wrapper = new RecordWrapper();
        RecordType recordType = mc.getType(record.getClass(), false);
        wrapper.setRecordType(recordType);
        wrapper.setData(mc.toWrapper(record));
        if (record.isDocumentChanged()) {
            wrapper.setDocument(record.getDocument());
        }
        // getting the id from the JPA
        mc.getTransactionEntityManager().persist(wrapper);
        Long recordId = wrapper.getId();
        record.setId(recordId);
        return wrapper;
    }

    /**
     * removing parents and children
     *
     * @param parentId record id
     * @param childId record id
     */
    private void removePath(Long parentId, Long childId) {
        EntityManager em = mc.getTransactionEntityManager();
        //-- building branch to remove from below hierarchy >> using parent to use only this parent path if more than one exists
        List<Long> pathList = em.createQuery("SELECT rp.recordPathPK.path FROM RecordPath rp WHERE rp.recordPathPK.child = :child").setParameter("child", parentId).getResultList();
        pathList.add(parentId);
        //-- retrieving hierarchies to complete with new branch >> finding all children of reference
        List<RecordPathPK> pkList = em.createQuery("SELECT rp.recordPathPK FROM RecordPath rp WHERE rp.recordPathPK.path = :path").setParameter("path", childId).getResultList();
        pkList.add(new RecordPathPK(parentId, childId, parentId));
        // applying path branch to childrens
        for (RecordPathPK pk : pkList) {
            for (Long path : pathList) {
                pk.setPath(path);
                em.remove(em.getReference(RecordPath.class, pk));
            }
        }
    }

}
