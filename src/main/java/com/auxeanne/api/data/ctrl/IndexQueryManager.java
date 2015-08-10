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
package com.auxeanne.api.data.ctrl;

import com.auxeanne.api.data.db.RecordIndex;
import com.auxeanne.api.data.ctrl.ParameterManager.ParameterType;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * Subset of the Query API for indexes
 * 
 * @author Jean-Michel Tanguy
 */
public class IndexQueryManager {
    
    /**
     * mapping parameter to database model
     */
    ParameterManager pm = new ParameterManager();

    
    public Predicate getSubQuery(CriteriaBuilder cb, CriteriaQuery cq, Class referenceClass, Path recordWrapperPath, IndexQuery indexQuery, List<Order> orderList) {
        try {
            switch (indexQuery.getQuery()) {
                case ORDER_BY:
                case REVERSE_BY:
                    return sorting(cb, cq, referenceClass, recordWrapperPath, indexQuery, orderList);
                default:
                    return filtering(cb, cq, referenceClass, recordWrapperPath, indexQuery);
            }
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Predicate sorting(CriteriaBuilder cb, CriteriaQuery cq, Class referenceClass, Path recordWrapperPath, IndexQuery indexQuery, List<Order> orderList) throws NoSuchFieldException {
        
         Root<RecordIndex> join = cq.from(RecordIndex.class);
         
         Class<?> type = referenceClass.getDeclaredField(indexQuery.getField()).getType();
         ParameterType indexType = pm.getType(type);
         Path indexPath = pm.getPath(join, indexType);
         
          switch (indexQuery.getQuery()) {
              case ORDER_BY : orderList.add( cb.asc(indexPath) );  break;
              case REVERSE_BY : orderList.add( cb.desc(indexPath) );break;
          }
         //-- where ... and join[i].link = :linkList[i]
          return cb.and(   cb.equal(join.get("recordWrapper"), recordWrapperPath) ,  cb.equal(join.get("recordIndexPK").get("field"), indexQuery.getField() ) );
    }

    public Predicate filtering(CriteriaBuilder cb, CriteriaQuery cq, Class referenceClass, Path recordWrapperPath, IndexQuery indexQuery) throws NoSuchFieldException {
        //-- sub query setup
        Subquery subquery = cq.subquery(RecordIndex.class);
        Root subRoot = subquery.from(RecordIndex.class);
        subquery.select(subRoot);
        //-- predicate paths
        Path fieldSubPath = subRoot.get("recordIndexPK").get("field");
        Path recordSubPath = subRoot.get("recordWrapper");
        //-- inner join
        List<Predicate> subP = new ArrayList<>();
        subP.add(cb.equal(fieldSubPath, indexQuery.getField()));
        subP.add(cb.equal(recordSubPath, recordWrapperPath));
        //-- converting types
        Class<?> type = referenceClass.getDeclaredField(indexQuery.getField()).getType();
        ParameterType indexType = pm.getType(type);
        Object[] converted = new Object[indexQuery.values.length];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = pm.getConverted(indexQuery.values[i]);
        }
        //-- 
        pm.filter(cb, subP,  subRoot,indexQuery.getQuery(), indexType, converted);
        //-- finalizing
        subquery.where(subP.toArray(new Predicate[0]));
        Predicate exists = cb.exists(subquery);
        return exists;
    }

   

}
