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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Jean-Michel
 */
@Embeddable
public class RecordIndexPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "key_")
    private String key;
    @Basic(optional = false)
    @NotNull
    @Column(name = "record_")
    private long record;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "field_")
    private String field;

    public RecordIndexPK() {
    }

    public RecordIndexPK(String key, long record, String field) {
        this.key = key;
        this.record = record;
        this.field = field;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (key != null ? key.hashCode() : 0);
        hash += (int) record;
        hash += (field != null ? field.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordIndexPK)) {
            return false;
        }
        RecordIndexPK other = (RecordIndexPK) object;
        if ((this.key == null && other.key != null) || (this.key != null && !this.key.equals(other.key))) {
            return false;
        }
        if (this.record != other.record) {
            return false;
        }
        if ((this.field == null && other.field != null) || (this.field != null && !this.field.equals(other.field))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.auxeanne.api.data.db.RecordIndexPK[ key=" + key + ", record=" + record + ", field=" + field + " ]";
    }
    
}
