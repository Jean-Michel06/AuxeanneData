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

import com.auxeanne.data.db.RecordLink;
import com.auxeanne.data.db.RecordPath;
import com.auxeanne.data.db.RecordWrapper;
import com.auxeanne.data.ctrl.ParameterManager.ParameterFilter;
import com.auxeanne.data.db.RecordType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.EntityManagerProperties;

/**
 * Fluent API to perform queries in Records.
 *
 * @author Jean-Michel Tanguy
 */
public class FluentQuery {

    /**
     * Implementing all the query steps and operations.
     *
     * @param <T> All POJOs must extend DefaultRecord
     */
    static public class Builder<T extends Record> implements SelectBuilder<T>, QueryBuilder<T>, LinkBuilder<T>, LinkAttributeBuilder<T>, PathBuilder<T>, ExtendedQuery<T>, SortBuilder<T>, SortLinkBuilder<T> {

        private final DatabaseController mc;
        private final Class<T> referenceClass;
        private final CriteriaBuilder cb;
        private final EntityManager em;
        private final CriteriaQuery cq;
        private final List<Predicate> predicateList = new ArrayList<>();
        private final List<Order> orderList = new ArrayList();
        private final HashMap<String, List<String>> equalMap = new HashMap<>();
        private final List<String[]> searchMap = new ArrayList<>();
        private final List<IndexQuery> indexList = new ArrayList<>();

        private Root<RecordWrapper> recordRoot;
        private Root<RecordLink> linkRoot;
        private Root<RecordPath> pathRoot;

        private Integer firstResult = null;
        private Integer maxResults = null;
        private String pathTarget = null;
        private Boolean sortByInsert = null; // true = asc; false = desc; null =none;

        private final ParameterManager pm = new ParameterManager();
        // RecordType is constant for each query
        private final RecordType type;
        // linkingAny, childOfAny.... may bring duplicates when more than 1 target, which requires DISTINCT selector
        private boolean isDistinctRequired = false;

        //----------------------------------------------------------------------
        // QueryBuilder<T> 
        //----------------------------------------------------------------------
        public Builder(DatabaseController mc, Class<T> referenceClass) {
            this.referenceClass = referenceClass;
            this.mc = mc;
            //-- fetching RecordType first to avoid closing the EM prematurely
            type = mc.getType(referenceClass, false);
            //-- opening a new Entity Manager
            em = mc.getTransactionEntityManager();
            cb = em.getCriteriaBuilder();
            cq = cb.createQuery();
        }

        @Override
        public T find(long id) throws IllegalAccessException {
            RecordWrapper record = mc.getTransactionEntityManager().find(RecordWrapper.class, id);
            try {
                if (record != null) {
                    RecordType parent = mc.getTransactionEntityManager().find(RecordType.class, record.getRecordType());
                    Class c = Class.forName(parent.getCode());
                    if (referenceClass.isAssignableFrom(c)) {
                        T t = mc.getRecord(referenceClass, record);
                        return t;
                    } else {
                        throw new IllegalAccessException();
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FluentQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        private void initRecordQuery() {
            recordRoot = cq.from(RecordWrapper.class);
            //-- adding match filter
            applyExtendedQuery(recordRoot, null, predicateList);
        }

        //----------------------------------------------------------------------
        // ExtendedQuery<T>
        //----------------------------------------------------------------------
//        @Override
//        public ExtendedQuery<T> fieldEqualTo(String key, String value) {
//            equalMap.put(key, Arrays.asList(value));
//            return this;
//        }

//        @Override
//        public ExtendedQuery<T> fieldIn(String key, String... values) {
//            equalMap.put(key, Arrays.asList(values));
//            return this;
//        }

        @Override
        public ExtendedQuery<T> indexLike(String field, String... values) {
            indexList.add(new IndexQuery(ParameterFilter.LIKE, field, values));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexNotLike(String field, String... values) {
            indexList.add(new IndexQuery(ParameterFilter.NOT_LIKE, field, values));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexIn(String field, Object... values) {
            indexList.add(new IndexQuery(ParameterFilter.IN, field, values));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexNotIn(String field, Object... values) {
            indexList.add(new IndexQuery(ParameterFilter.NOT_IN, field, values));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexEqualTo(String field, Object value) {
            indexList.add(new IndexQuery(ParameterFilter.EQUAL_TO, field, value));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexNotEqualTo(String field, Object value) {
            indexList.add(new IndexQuery(ParameterFilter.NOT_EQUAL_TO, field, value));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexLessThan(String field, Object value) {
            indexList.add(new IndexQuery(ParameterFilter.LESS_THAN, field, value));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexGreaterThan(String field, Object value) {
            indexList.add(new IndexQuery(ParameterFilter.GREATER_THAN, field, value));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexLessThanOrEqualTo(String field, Object value) {
            indexList.add(new IndexQuery(ParameterFilter.LESS_THAN_OR_EQUAL_TO, field, value));
            return this;
        }

        @Override
        public ExtendedQuery<T> indexGreaterThanOrEqualTo(String field, Object value) {
            indexList.add(new IndexQuery(ParameterFilter.GREATER_THAN_OR_EQUAL_TO, field, value));
            return this;
        }

        @Override
        public SortBuilder<T> orderByIndexedField(String field) {
            indexList.add(new IndexQuery(ParameterFilter.ORDER_BY, field));
            return this;
        }

        @Override
        public SortBuilder<T> reverseByIndexedField(String field) {
            indexList.add(new IndexQuery(ParameterFilter.REVERSE_BY, field));
            return this;
        }

        private void applyExtendedQuery(Root root, String wrapperKey, List<Predicate> predicateList) {
            From wrapperRoot = (wrapperKey == null) ? root : root.join(wrapperKey, JoinType.INNER);
            //-- reducing scope of the query with indexed keys filtering
            //-- 1st limit to correct RecordType
            predicateList.add(0, cb.equal(wrapperRoot.get("recordType"), type.getId()));
            //-- 2nd tenant filtering where it applies. It is directly managed instead of Eclipselink annotations.
            Object tenant = mc.getTransactionEntityManager().getProperties().get(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT);
            if (tenant != null) {
                predicateList.add(cb.equal(wrapperRoot.get("tenant"), tenant.toString()));
            }
            //-- pass on the query parameters
            applyEqualQuery(wrapperRoot, predicateList);
            applyIndexQuery(wrapperRoot, predicateList);
            applySearchQuery(wrapperRoot, predicateList);
        }

        private void applyEqualQuery(Path recordPath, List<Predicate> predicateList) {
            Iterator<String> keyIterator = equalMap.keySet().iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                List<String> values = equalMap.get(key);
                Predicate[] or = new Predicate[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    String pattern = "%\"" + key + "\":\"" + values.get(i) + "\"%";
                    or[i] = cb.like(recordPath.get("data"), pattern);
                }
                predicateList.add(cb.or(or));
            }
        }

        private void applyIndexQuery(From recordPath, List<Predicate> predicateList) {
            IndexQueryManager indexManager = new IndexQueryManager();
            for (IndexQuery indexQuery : indexList) {
                Predicate subQuery = indexManager.getSubQuery(cb, cq, referenceClass, recordPath, indexQuery, orderList);
                if (subQuery != null) {
                    predicateList.add(subQuery);
                }
            }
        }

        private void applySearchQuery(Path recordPath, List<Predicate> predicateList) {
            for (String[] values : searchMap) {
                Predicate[] or = new Predicate[values.length];
                for (int i = 0; i < values.length; i++) {
                    String pattern = "%" + values[i] + "%";
                    or[i] = cb.like(recordPath.get("data"), pattern);
                }
                predicateList.add(cb.or(or));
            }
        }

        //----------------------------------------------------------------------
        // SelectBuilder<T>
        //----------------------------------------------------------------------
        @Override
        public List<T> getList() {
            ArrayList<T> list = new ArrayList<>();
            Query query;
            Path selectPath;
            //-- query setup
            if (linkRoot != null) {
                selectPath = linkRoot.get("referenceR");
            } else if (pathRoot != null) {
                selectPath = pathRoot.get(pathTarget);
            } else {
                initRecordQuery();
                selectPath = recordRoot;
            }
            //-- DISTINCT is expensive, apply only when needed
            if (isDistinctRequired) {
                cq.select(selectPath).distinct(true); // AboveAny / BelowAny multiple path results in multiple paths selection with same target which must be filteres y Distinct
            } else {
                cq.select(selectPath);
            }

            //-- order by , ultimatly ordering by record id
            if (sortByInsert != null) {
                if (sortByInsert) {
                    orderList.add(cb.asc(selectPath.get("id")));
                } else {
                    orderList.add(cb.desc(selectPath.get("id")));
                }
            }
            if (!orderList.isEmpty()) {
                cq.orderBy(orderList);
            }
            //-- where
            cq.where(predicateList.toArray(new Predicate[0]));
            query = em.createQuery(cq);
            //-- query parameters and execution
            if (firstResult != null) {
                query.setFirstResult(firstResult);
            }
            if (maxResults != null) {
                query.setMaxResults(maxResults);
            }
            //long start = System.currentTimeMillis();
            List<RecordWrapper> resultList = query.getResultList();
            //long end = System.currentTimeMillis();
            //System.out.println("[] List query in "+(end-start)+"ms");
            //-- converting to object
            resultList.stream().map((record) -> {
                T model = mc.getRecord(referenceClass, record);
                return model;
            }).forEach((model) -> {
                list.add(model);
            });
            return list;
        }

        @Override
        public T getFirst() {
            setMaxResults(1);
            List<T> list = getList();
            return (list.isEmpty()) ? null : list.get(0);
        }

        @Override
        public Integer count() {
            Query query;
            //-- query setup
            Path selectPath;
            if (linkRoot != null) {
                selectPath = linkRoot.get("referenceR");
            } else if (pathRoot != null) {
                selectPath = pathRoot.get(pathTarget);
            } else {
                initRecordQuery();
                selectPath = recordRoot;
            }
            //-- DISTINCT is expensive, apply only when needed
            if (isDistinctRequired) {
                cq.select(cb.countDistinct(selectPath)); // AboveAny / BelowAny multiple path results in multiple paths selection with same target which must be filteres y Distinct
            } else {
                cq.select(cb.count(selectPath));
            }
            //-- where
            cq.where(predicateList.toArray(new Predicate[0]));
            query = em.createQuery(cq);
            //-- query parameters
            if (firstResult != null) {
                query.setFirstResult(firstResult);
            }
            if (maxResults != null) {
                query.setMaxResults(maxResults);
            }
            return ((Long) query.getSingleResult()).intValue();
        }

        @Override
        public SelectBuilder<T> setMaxResults(int length) {
            maxResults = length;
            return this;
        }

        @Override
        public SelectBuilder<T> setFirstResult(int start) {
            firstResult = start;
            return this;
        }

        @Override
        public SortBuilder<T> orderByInsert() {
            // first occurence matters
            if (sortByInsert == null) {
                sortByInsert = true;
            }
            return this;
        }

        @Override
        public SortBuilder<T> reverseByInsert() {
            // first occurence matters
            if (sortByInsert == null) {
                sortByInsert = false;
            }
            return this;
        }

        //----------------------------------------------------------------------
        // LinkBuilder<T>
        //----------------------------------------------------------------------
        @Override
        public LinkAttributeBuilder<T> linking(Record... records) {
            initLinkQuery();
            connectAll(false, RecordLink.class, linkRoot, "link", "reference", records);
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> linkingAny(Record... records) {
            initLinkQuery();
            connectAny(false, linkRoot, "link", records);
            return this;
        }

        private void initLinkQuery() {
            linkRoot = cq.from(RecordLink.class);
            //Join<RecordLink, RecordWrapper> join = linkRoot.join("link");
            //-- adding match and index filters
            applyExtendedQuery(linkRoot, "referenceR", predicateList);
        }

        //----------------------------------------------------------------------
        // LinkFilterBuilder<T>
        //----------------------------------------------------------------------      
        @Override
        public LinkAttributeBuilder<T> attributeIn(Object... parameters) {
            Path path = pm.getPath(linkRoot, parameters[0].getClass());
            Predicate[] predicates = new Predicate[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                predicates[i] = cb.equal(path, pm.getConverted(parameters[i]));
            }
            predicateList.add(cb.or(predicates));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeNotIn(Object... parameters) {
            Path path = pm.getPath(linkRoot, parameters[0].getClass());
            Predicate[] predicates = new Predicate[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                predicates[i] = cb.notEqual(path, pm.getConverted(parameters[i]));
            }
            predicateList.add(cb.and(predicates));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeEqualTo(Object parameter) {
            Path path = pm.getPath(linkRoot, parameter.getClass());
            predicateList.add(cb.equal(path, pm.getConverted(parameter)));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeNotEqualTo(Object parameter) {
            Path path = pm.getPath(linkRoot, parameter.getClass());
            predicateList.add(cb.notEqual(path, pm.getConverted(parameter)));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeGreaterThan(Object parameter) {
            Path path = pm.getPath(linkRoot, parameter.getClass());
            predicateList.add(cb.greaterThan(path, pm.getConverted(parameter)));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeLessThan(Object parameter) {
            Path path = pm.getPath(linkRoot, parameter.getClass());
            predicateList.add(cb.lessThan(path, pm.getConverted(parameter)));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeGreaterThanOrEqualTo(Object parameter) {
            Path path = pm.getPath(linkRoot, parameter.getClass());
            predicateList.add(cb.greaterThanOrEqualTo(path, pm.getConverted(parameter)));
            return this;
        }

        @Override
        public LinkAttributeBuilder<T> attributeLessThanOrEqualTo(Object parameter) {
            Path path = pm.getPath(linkRoot, parameter.getClass());
            predicateList.add(cb.lessThanOrEqualTo(path, pm.getConverted(parameter)));
            return this;
        }

        @Override
        public SortLinkBuilder<T> orderByAttribute() {
            orderList.add(cb.asc(linkRoot.get("value")));
            orderList.add(cb.asc(linkRoot.get("numeric")));
            orderList.add(cb.asc(linkRoot.get("date")));
            return this;
        }

        @Override
        public SortLinkBuilder<T> reverseByAttribute() {
            orderList.add(cb.desc(linkRoot.get("value")));
            orderList.add(cb.desc(linkRoot.get("numeric")));
            orderList.add(cb.desc(linkRoot.get("date")));
            return this;
        }

        //----------------------------------------------------------------------
        // PathBuilder<T>
        //----------------------------------------------------------------------
        @Override
        public SortBuilder<T> aboveAny(Record... records) {
            connectPath(false, false, "child", "path", records);
            return this;
        }

        @Override
        public SortBuilder<T> belowAny(Record... records) {
            connectPath(false, false, "path", "child", records);
            return this;
        }

        @Override
        public SortBuilder<T> parentOfAny(Record... records) {
            connectPath(true, false, "child", "parent", records);
            return this;
        }

        @Override
        public SortBuilder<T> childOfAny(Record... records) {
            connectPath(true, false, "parent", "child", records);
            return this;
        }

        @Override
        public SortBuilder<T> above(Record... records) {
            connectPath(false, true, "child", "path", records);
            return this;
        }

        @Override
        public SortBuilder<T> below(Record... records) {
            connectPath(false, true, "path", "child", records);
            return this;
        }

        @Override
        public SortBuilder<T> parentOf(Record... records) {
            connectPath(true, true, "child", "parent", records);
            return this;
        }

        @Override
        public SortBuilder<T> childOf(Record... records) {
            connectPath(true, true, "parent", "child", records);
            return this;
        }

        //----------------------------------------------------------------------
        // connection manager
        //----------------------------------------------------------------------
        private <T> void connectPath(boolean limitPath, boolean and, String source, String target, Record... records) {
            pathRoot = cq.from(RecordPath.class);
            pathTarget = target+"R";
            //Join<RecordPath, RecordWrapper> toJoin = pathRoot.join(pathTarget);
            //-- adding predicates
            if (and) {
                connectAll(limitPath, RecordPath.class, pathRoot, source, target, records);
            } else {
                connectAny(limitPath, pathRoot, source, records);
            }
            //-- adding match and index filter
            applyExtendedQuery(pathRoot, pathTarget, predicateList); // trick to pass from embedded key to child
        }

        private <T> void connectAll(boolean limitPath, Class<T> c, Root<T> root, String source, String target, Record... records) {
            Path<Integer> id = root.get("recordPK").get(source);
            predicateList.add(cb.equal(id, records[0].getId()));
            if (limitPath) { // for asChild asParent
                predicateList.add(cb.equal(root.get("recordPK").get("path"), root.get("recordPK").get("parent")));
            }
            //-- join
            for (int i = 1; i < records.length; i++) {
                // from ..., RecordLink join[i]
                Root<T> join = cq.from(c);
                // where ... and join[i].link = :linkList[i]
                predicateList.add(cb.equal(join.get("recordPK").get(source), records[i].getId()));
                // where ... and join[i].reference = root.reference
                predicateList.add(cb.equal(join.get("recordPK").get(target), root.get("recordPK").get(target)));
                // limitPath
                if (limitPath) { // for asChild asParent
                    predicateList.add(cb.equal(join.get("recordPK").get("path"), join.get("recordPK").get("parent")));
                }
            }
        }

        private <T> void connectAny(boolean limitPath, Root<T> root, String source, Record... records) {
            Predicate[] ors = new Predicate[records.length];
            Path<Integer> id = root.get("recordPK").get(source);
            for (int i = 0; i < records.length; i++) {
                if (limitPath) { // for asChild asParent
                    ors[i] = cb.and(cb.equal(id, records[i].getId()), cb.equal(root.get("recordPK").get("path"), root.get("recordPK").get("parent")));
                } else {
                    ors[i] = cb.equal(id, records[i].getId());
                }
            }
            predicateList.add(cb.or(ors));
            //-- duplicates may arise when more than 1 record in the join, DISTINCT optimization is then required
            if (records.length > 1) {
                isDistinctRequired = true;
            }
        }

    }

    //--------------------------------------------------------------------------
    // FLUENT API interfaces
    //--------------------------------------------------------------------------
    static public interface SelectBuilder<T extends Record> {

        /**
         * list all matching records (finalizing the query)
         *
         * @return list of records for the query
         */
        List<T> getList();

        /**
         * get the first record from the query (finalizing the query)
         *
         * @return first matching record for the query or null if query is empty
         */
        T getFirst();

        /**
         * counting matching records (finalizing the query)
         *
         * @return count of matching records for the query
         */
        Integer count();

        /**
         * setting the first position from the matching result (default is 0)
         *
         * @param start position of the first record to retrieve
         * @return Fluent Query
         */
        SelectBuilder<T> setFirstResult(int start);

        /**
         * setting the maximum number of records to retrieve from the query
         * (otherwise all)
         *
         * @param length number of records to get starting from first result
         * @return Fluent Query
         */
        SelectBuilder<T> setMaxResults(int length);

    }

    static public interface SortBuilder<T extends Record> extends SelectBuilder<T> {

        /**
         * sorting the records by their database id (ASC)
         *
         * @return Fluent Query
         */
        SortBuilder<T> orderByInsert();

        /**
         * sorting the records by their database id (DESC)
         *
         * @return Fluent Query
         */
        SortBuilder<T> reverseByInsert();

        /**
         * Sorting the records by the field content if the field is indexed
         * (ASC). Records are sorted as Date, BigDecimal or String depending on
         * what fits the best.
         *
         * @param field field content to sort
         * @return Fluent Query
         */
        SortBuilder<T> orderByIndexedField(String field);

        /**
         * Sorting the records by the field content if the field is indexed
         * (DESC). Records are sorted as Date, BigDecimal or String depending on
         * what fits the best.
         *
         * @param field field content to sort
         * @return Fluent Query
         */
        SortBuilder<T> reverseByIndexedField(String field);
    }

    static public interface QueryBuilder<T extends Record> extends ExtendedQuery<T> {

        /**
         * Retrieve a record based on its id (finalizing the query). The type in
         * database and in the query must match.
         *
         * @param id database id
         * @return record from the database
         */
        T find(long id) throws IllegalAccessException;

    };

    static public interface ExtendedQuery<T extends Record> extends PathBuilder<T>, LinkBuilder<T>, SortBuilder<T> {

        /**
         * Filtering records searching value in any field
         *
         * @param value values to search (ANY)
         * @return Fluent Query
         */
        //ExtendedQuery<T> matchingAny(String... value);
        /**
         * Filtering records based on field content matching value. Field does
         * not need to be indexed.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        //ExtendedQuery<T> fieldEqualTo(String field, String value);

        /**
         * Filtering records based on field content matching any of the values.
         * Field does not need to be indexed.
         *
         * @param field field to compare
         * @param values value to compare to
         * @return Fluent Query
         */
        //ExtendedQuery<T> fieldIn(String field, String... values);

        /**
         *
         * @param field
         * @param values
         * @return
         */
        ExtendedQuery<T> indexLike(String field, String... values);

        /**
         *
         * @param field
         * @param values
         * @return
         */
        ExtendedQuery<T> indexNotLike(String field, String... values);

        /**
         * Filtering records with indexed field content matching value. Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexEqualTo(String field, Object value);

        /**
         * Filtering records with indexed field content not matching value.
         * Date, BigDecimal or String is used depending on what fits the best.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexNotEqualTo(String field, Object value);

        /**
         * Filtering records with indexed field content greater than value.
         * Date, BigDecimal or String is used depending on what fits the best.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexGreaterThan(String field, Object value);

        /**
         * Filtering records with indexed field content less than value. Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexLessThan(String field, Object value);

        /**
         * Filtering records with indexed field content greater than or equal to
         * value. Date, BigDecimal or String is used depending on what fits the
         * best.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexGreaterThanOrEqualTo(String field, Object value);

        /**
         * Filtering records with indexed field content less than or equal to
         * value. Date, BigDecimal or String is used depending on what fits the
         * best.
         *
         * @param field field to compare
         * @param value value to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexLessThanOrEqualTo(String field, Object value);

        /**
         * filtering records with indexed field content in list of values
         *
         * @param field field to compare
         * @param values values to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexIn(String field, Object... values);

        /**
         * filtering records with indexed field content not in list of values
         *
         * @param field field to compare
         * @param values values to compare to
         * @return Fluent Query
         */
        ExtendedQuery<T> indexNotIn(String field, Object... values);

    }

    static public interface PathBuilder<T extends Record> {

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> aboveAny(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> belowAny(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> parentOfAny(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> childOfAny(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> above(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> below(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> parentOf(Record... records);

        /**
         * filtering records matching hierarchical link
         *
         * @param records records matching the hierarchy
         * @return Fluent Link
         */
        SortBuilder<T> childOf(Record... records);

    }

    static public interface LinkBuilder<T extends Record> {

        /**
         * filtering records with bidirectional link on any of the targets
         *
         * @param records linked records
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> linkingAny(Record... records);

        /**
         * filtering records with bidirectional link on all the targets
         *
         * @param records linked records
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> linking(Record... records);

    }

    static public interface LinkAttributeBuilder<T extends Record> extends SortLinkBuilder<T> {

        /**
         * filtering records on bidirectional link attribute value
         *
         * @param attributes values to match
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeIn(Object... attributes);

        /**
         * filtering records on bidirectional link attribute value
         *
         * @param attributes values not to match
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeNotIn(Object... attributes);

        /**
         * filtering records on bidirectional link attribute value
         *
         * @param attribute value to compare to
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeEqualTo(Object attribute);

        /**
         * filtering records on bidirectional link attribute value
         *
         * @param attribute value to compare to
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeNotEqualTo(Object attribute);

        /**
         * Filtering records on bidirectional link attribute value. Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @param attribute value to compare to
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeGreaterThan(Object attribute);

        /**
         * Filtering records on bidirectional link attribute value. Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @param attribute value to compare to
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeLessThan(Object attribute);

        /**
         * Filtering records on bidirectional link attribute value. Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @param attribute value to compare to
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeGreaterThanOrEqualTo(Object attribute);

        /**
         * Filtering records on bidirectional link attribute value. Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @param attribute value to compare to
         * @return Fluent Query
         */
        LinkAttributeBuilder<T> attributeLessThanOrEqualTo(Object attribute);
    }

    static public interface SortLinkBuilder<T extends Record> extends SortBuilder<T> {

        /**
         * Sorting records base on bidirectional link attribute (ASC). Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @return Fluent Query
         */
        SortLinkBuilder<T> orderByAttribute();

        /**
         * Sorting records base on bidirectional link attribute (DESC). Date,
         * BigDecimal or String is used depending on what fits the best.
         *
         * @return Fluent Query
         */
        SortLinkBuilder<T> reverseByAttribute();

    }

}
