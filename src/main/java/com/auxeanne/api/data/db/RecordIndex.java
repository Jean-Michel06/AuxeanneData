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
package com.auxeanne.api.data.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Multitenant;

/**
 *
 * @author Jean-Michel
 */
@Entity
@Table(name = "record_index")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RecordIndex.findIndexFromIdList", query = "SELECT r.recordIndexPK FROM RecordIndex r WHERE r.recordIndexPK.record in :list "),
    @NamedQuery(name = "RecordIndex.findValueFromKey", query = "SELECT DISTINCT r.value FROM RecordIndex r WHERE r.recordIndexPK.key =:key  AND r.value IS NOT NULL ORDER BY r.value ASC"),
    @NamedQuery(name = "RecordIndex.findDateFromKey", query = "SELECT DISTINCT r.date FROM RecordIndex r WHERE r.recordIndexPK.key =:key  AND r.date IS NOT NULL ORDER BY r.date ASC"),
    @NamedQuery(name = "RecordIndex.findNumericFromKey", query = "SELECT DISTINCT r.numeric FROM RecordIndex r WHERE r.recordIndexPK.key =:key  AND r.numeric IS NOT NULL  ORDER BY r.numeric ASC")})
@Cacheable(true)
@Multitenant()
public class RecordIndex implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected RecordIndexPK recordIndexPK;

    @Size(max = 255)
    @Column(name = "value_")
    private String value;
    @Column(name = "numeric_")
    private BigDecimal numeric;
    @Column(name = "date_")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date date;

//    @JoinColumn(name = "key_", referencedColumnName = "key_", insertable = false, updatable = false)
//    @ManyToOne(optional = false)
//    private RecordIndexKey recordIndexKey;
    @JoinColumn(name = "record_", referencedColumnName = "id_", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RecordWrapper recordWrapper;

    public RecordIndex() {
    }

    public RecordIndex(RecordIndexPK recordIndexPK) {
        this.recordIndexPK = recordIndexPK;
    }

    public RecordIndex(String key, int record, String field) {
        this.recordIndexPK = new RecordIndexPK(key, record, field);
    }

    public RecordIndexPK getRecordIndexPK() {
        return recordIndexPK;
    }

    public void setRecordIndexPK(RecordIndexPK recordIndexPK) {
        this.recordIndexPK = recordIndexPK;
    }

//    public RecordIndexKey getRecordIndexKey() {
//        return recordIndexKey;
//    }
//
//    public void setRecordIndexKey(RecordIndexKey recordIndexKey) {
//        this.recordIndexKey = recordIndexKey;
//    }
    public RecordWrapper getRecordWrapper() {
        return recordWrapper;
    }

    public void setRecordWrapper(RecordWrapper recordWrapper) {
        this.recordWrapper = recordWrapper;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BigDecimal getNumeric() {
        return numeric;
    }

    public void setNumeric(BigDecimal numeric) {
        this.numeric = numeric;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (recordIndexPK != null ? recordIndexPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordIndex)) {
            return false;
        }
        RecordIndex other = (RecordIndex) object;
        if ((this.recordIndexPK == null && other.recordIndexPK != null) || (this.recordIndexPK != null && !this.recordIndexPK.equals(other.recordIndexPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.auxeanne.api.data.db.RecordIndex[ recordIndexPK=" + recordIndexPK + " ]";
    }

}
