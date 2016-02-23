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
import com.auxeanne.data.db.RecordLinkPK;
import com.auxeanne.data.db.RecordPath;
import com.auxeanne.data.db.RecordPathPK;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Fluent API to perform link operations on records.
 *
 * @author Jean-Michel
 */
public class FluentLink {

    /**
     * Implementing all the link steps and operations. Note : using criteria
     * properties instead of metamodel as Eclipselink does not properly
     * initialize in junit.
     */
    static public class Builder implements CreateBuilder, RemoveBuilder, PathBuilder, ParameterBuilder, LinkBuilder, LinkWatchBuilder, ConnectionBuilder {

        private enum Action {

            LINKING, AS_PARENT, AS_CHILD
        };

        private final DatabaseController mc;
        private final Record reference;
        private final AuditLogger auditor;
        private String value;
        private BigDecimal numeric;
        private Date date;
        private Record[] records;
        private Action action;
        private ParameterManager pm = new ParameterManager();

        /**
         *
         * @param mc Database controller
         * @param reference Reference POJO to link
         * @param auditor Auditor logger
         */
        public Builder(DatabaseController mc, Record reference, AuditLogger auditor) {
            this.mc = mc;
            this.reference = reference;
            this.auditor = auditor;
        }

        //----------------------------------------------------------------------
        // ConnectionBuilder
        //----------------------------------------------------------------------
        @Override
        public LinkBuilder with(Record... records) {
            this.records = records;
            action = Action.LINKING;
            return this;
        }

        @Override
        public LinkWatchBuilder with(Record record) {
            this.records = new Record[]{record};
            action = Action.LINKING;
            return this;
        }

        @Override
        public PathBuilder asParentOf(Record... records) {
            this.records = records;
            action = Action.AS_PARENT;
            return this;
        }

        @Override
        public PathBuilder asChildOf(Record... records) {
            this.records = records;
            action = Action.AS_CHILD;
            return this;
        }

        //----------------------------------------------------------------------
        //  CreateBuilder, RemoveBuilder, UpdateBuilder
        //----------------------------------------------------------------------
        @Override
        public void save() {
            switch (action) {
                case LINKING:
                    link(records);
                    break;
                case AS_PARENT:
                    addChild(records);
                    break;
                case AS_CHILD:
                    addParent(records);
                    break;
            }
        }

        @Override
        public void remove() {
            switch (action) {
                case LINKING:
                    removeLink(records);
                    break;
                case AS_PARENT:
                    removeChild(records);
                    break;
                case AS_CHILD:
                    removeParent(records);
                    break;
            }
        }

        //  @Override
//        public void update() {
//            link(false, records);
//        }
        //----------------------------------------------------------------------
        // ParameterBuilder
        //----------------------------------------------------------------------
//        @Override
//        public ParameterBuilder setWeight(Long weight) {
//            this.weight = weight;
//            return this;
//        }
//
//        @Override
//        public ParameterBuilder setAttribute(String tag) {
//            this.tag = tag;
//            return this;
//        }
//
//        @Override
//        public ParameterBuilder setDate(Date date) {
//            this.date = date;
//            return this;
//        }
        @Override
        public PathBuilder setAttribute(Object parameter) {
            Comparable converted = pm.getConverted(parameter);
            numeric = null;
            date = null;
            value = null;
            if (converted != null) {
                if (converted instanceof BigDecimal) {
                    numeric = (BigDecimal) converted;
                }
                if (converted instanceof Date) {
                    date = (Date) converted;
                }
                if (converted instanceof String) {
                    value = (String) converted;
                }
            }
            return this;
        }

        //----------------------------------------------------------------------
        // LinkWatchBuilder
        //----------------------------------------------------------------------
        @Override
        public Comparable getAttribute() {
            RecordLinkPK pk = new RecordLinkPK(reference.getId(), records[0].getId());
            RecordLink recordLink = mc.getTransactionEntityManager().find(RecordLink.class, pk);
            if (recordLink != null) {
                if (recordLink.getNumeric() != null) {
                    return recordLink.getNumeric();
                }
                if (recordLink.getValue() != null) {
                    return recordLink.getValue();
                }
                if (recordLink.getDate() != null) {
                    return recordLink.getDate();
                }
            }
            return null;
        }

        @Override
        public boolean isAvailable() {
            RecordLinkPK pk = new RecordLinkPK(reference.getId(), records[0].getId());
            RecordLink recordLink = mc.getTransactionEntityManager().find(RecordLink.class, pk);
            return (recordLink != null);
        }

//        @Override
//        public String getValue() {
//            RecordLinkPK pk = new RecordLinkPK(reference.getId(), records[0].getId());
//            RecordLink recordLink = mc.getTransactionEntityManager().find(RecordLink.class, pk);
//            if (recordLink != null) {
//                return recordLink.getValue();
//            }
//            return null;
//        }
//
//        @Override
//        public Date getDate() {
//            RecordLinkPK pk = new RecordLinkPK(reference.getId(), records[0].getId());
//            RecordLink recordLink = mc.getTransactionEntityManager().find(RecordLink.class, pk);
//            if (recordLink != null) {
//                return recordLink.getDate();
//            }
//            return null;
//        }
        //----------------------------------------------------------------------
        // internal link management
        //----------------------------------------------------------------------
        /**
         * optimized for batch processing
         *
         * @param links POJOs to link to reference
         */
        private void link(Record... links) {
            mc.transaction(() -> {
                EntityManager em = mc.getTransactionEntityManager();
                //-- optimized for batch processing        
                List<Long> batchList = new ArrayList<>();
                for (Record link : links) {
                    batchList.add(link.getId());
                    auditor.logCreateLink(reference.getId(), link.getId(), value, numeric, date);
                }
                //-- first processing updates
                List<Long> processedList = new ArrayList<>();
                //-- batch fetch for existing links to limit the number of queries
                List<RecordLink> existingList = em.createNamedQuery("RecordLink.findExistingLinks")
                        .setParameter("id", reference.getId())
                        .setParameter("list", batchList)
                        .getResultList();
                for (RecordLink link : existingList) {
                    long linkId = link.getRecordLinkPK().getLink_();
                    if (batchList.contains(linkId)) {
                        link.setNumeric(numeric);
                        link.setValue(value);
                        link.setDate(date);
                        processedList.add(link.getRecordLinkPK().getLink_());
                    }
                }
                //-- processing revert of existing links
                if (!processedList.isEmpty()) {
                    List<RecordLink> alterList = em.createNamedQuery("RecordLink.findExistingReferences")
                            .setParameter("id", reference.getId())
                            .setParameter("list", processedList)
                            .getResultList();
                    for (RecordLink link : alterList) {
                        link.setNumeric(numeric);
                        link.setValue(value);
                        link.setDate(date);
                    }
                }
                //-- second inserting remaining links
                batchList.removeAll(processedList);
                for (Long link : batchList) {
                    persistLink(reference.getId(), link, value, numeric, date);
                    persistLink(link, reference.getId(), value, numeric, date);
                }
            });
        }

        private void persistLink(Long reference, Long link, String value_, BigDecimal numeric_, Date date_) {
            RecordLinkPK pk = new RecordLinkPK(reference, link);
            RecordLink recordLink = new RecordLink(pk);
            //-- not filling objects as never used, only queries, then saving fetch time
            //recordLink.setReference(reference);
            //recordLink.setLink(link);
            //------------------------------------------------------------------
            recordLink.setNumeric(numeric_);
            recordLink.setValue(value_);
            recordLink.setDate(date_);
            mc.getTransactionEntityManager().persist(recordLink);
        }

        private void removeLink(Record... records) {
            mc.transaction(() -> {
                for (Record record : records) {
                    EntityManager em = mc.getTransactionEntityManager();
                    em.remove(em.getReference(RecordLink.class, new RecordLinkPK(reference.getId(), record.getId())));
                    em.remove(em.getReference(RecordLink.class, new RecordLinkPK(record.getId(), reference.getId())));
                    auditor.logRemoveLink(reference.getId(), record.getId());
                }
            });
        }

        //----------------------------------------------------------------------
        // internal path management
        //----------------------------------------------------------------------
        private void addParent(Record... records) {
            mc.transaction(() -> {
                EntityManager em = mc.getTransactionEntityManager();
                List<Long> batch = new ArrayList<>();
                for (Record child : records) {
                    batch.add(child.getId());
                }
                List<Long> selectList = em.createNamedQuery("RecordPath.findExistingParents")
                        .setParameter("id", reference.getId())
                        .setParameter("list", batch)
                        .getResultList();
                batch.removeAll(selectList);
                for (Long id : batch) {
                    addHierarchy(id, reference.getId());
                }
            });
        }

        private void removeParent(Record... records) {
            mc.transaction(() -> {
                for (Record parent : records) {
                    removeHierarchy(parent.getId(), reference.getId());
                }
            });
        }

        private void addChild(Record... records) {
            mc.transaction(() -> {
                EntityManager em = mc.getTransactionEntityManager();
                List<Long> batchList = new ArrayList<>();
                for (Record child : records) {
                    batchList.add(child.getId());
                }
                List<Long> selectList = em.createNamedQuery("RecordPath.findExistingChildren")
                        .setParameter("id", reference.getId())
                        .setParameter("list", batchList)
                        .getResultList();
                batchList.removeAll(selectList);
                for (Long id : batchList) {
                    addHierarchy(reference.getId(), id);
                }

            });
        }

        private void removeChild(Record... records) {
            mc.transaction(() -> {
                for (Record child : records) {
                    removeHierarchy(reference.getId(), child.getId());
                }
            });
        }

        private void addHierarchy(Long parentId, Long childId) {
            EntityManager em = mc.getTransactionEntityManager();
            RecordPathPK newPk = new RecordPathPK(parentId, childId, parentId);
            //if (em.find(RecordPath.class, newPk) == null) {
            //-- building branch to propagate to new hierarchy >> getting the full path from the parent and including parent as new start point
            List<Long> pathList = em.createQuery("SELECT rp.recordPathPK.path FROM RecordPath rp WHERE rp.recordPathPK.child = :child").setParameter("child", parentId).getResultList();
            pathList.add(parentId);
            //-- retrieving hierarchies to complete with new branch >> finding all children of reference
            List<RecordPathPK> pkList = em.createQuery("SELECT rp.recordPathPK FROM RecordPath rp WHERE rp.recordPathPK.path = :path").setParameter("path", childId).getResultList();
            pkList.add(newPk);
            // applying path branch to childrens
            for (RecordPathPK pk : pkList) {
                for (Long path : pathList) {
                    pk.setPath(path);
                    em.persist(new RecordPath(pk));
                }
            }
            auditor.logSavePath(parentId, childId);
        }

        private void removeHierarchy(Long parentId, Long childId) {
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
            auditor.logRemovePath(parentId, childId);
        }

    }

    static public interface CreateBuilder {

        /**
         * save the link (finalizing)
         */
        void save();
    }

    static public interface RemoveBuilder {

        /**
         * remove the link (finalizing)
         */
        void remove();
    }

    static public interface PathBuilder extends CreateBuilder, RemoveBuilder {

    }

    static public interface ParameterBuilder extends CreateBuilder {

        /**
         * adding an attribute to a bidirectional link
         *
         * @param parameter any object converted as Date, BigDecimal or String,
         * depending on what fits best. Null clears the attribute.
         * @return Fluent Link
         */
        CreateBuilder setAttribute(Object parameter);

    }

    static public interface LinkBuilder extends CreateBuilder, RemoveBuilder, ParameterBuilder {

    }

    static public interface LinkWatchBuilder extends CreateBuilder, RemoveBuilder, ParameterBuilder {

        /**
         * retrieving the attribute of a bidirectional link (finalizing)
         *
         * @return Date, BigDecimal or String depending of the attribute or null
         * is not attribute is set
         */
        Comparable getAttribute();

        /**
         * checking if a link is set between records
         *
         * @return true is the link exists
         */
        boolean isAvailable();

    }

    static public interface ConnectionBuilder {

        /**
         * setting bidirectional link
         *
         * @param record POJO to link to reference
         * @return Fluent Link
         */
        LinkWatchBuilder with(Record record);

        /**
         * setting bidirectional links
         *
         * @param records POJOs to link to the reference
         * @return Fluent Link
         */
        LinkBuilder with(Record... records);

        /**
         * setting hierarchical link
         *
         * @param records children of the reference
         * @return Fluent Link
         */
        PathBuilder asParentOf(Record... records);

        /**
         * setting hierarchical link
         *
         * @param records parents of the reference
         * @return Fluent Link
         */
        PathBuilder asChildOf(Record... records);

    }

}
