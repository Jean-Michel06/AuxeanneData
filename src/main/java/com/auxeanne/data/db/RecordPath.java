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
package com.auxeanne.data.db;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jean-Michel Tanguy
 */
@Entity
@Table(name = "record_path")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RecordPath.findExistingParents", query = "SELECT p.recordPK.parent FROM RecordPath p WHERE p.parentR = p.pathR AND p.recordPK.parent in :list AND p.recordPK.child =:id "),
    @NamedQuery(name = "RecordPath.findExistingChildren", query = "SELECT p.recordPK.child FROM RecordPath p WHERE p.parentR = p.pathR AND p.recordPK.child in :list AND p.recordPK.parent = :id "),
    @NamedQuery(name = "RecordPath.findChildFromIdList", query = "SELECT rp.recordPK FROM RecordPath rp WHERE rp.recordPK.child in :list"),
    @NamedQuery(name = "RecordPath.findParentFromIdList", query = "SELECT rp.recordPK FROM RecordPath rp WHERE rp.recordPK.parent in :list")
})

public class RecordPath implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected RecordPathPK recordPK; // name optimized for fluent query

    @JoinColumn(name = "parent_", referencedColumnName = "id_", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RecordWrapper parentR;

    @JoinColumn(name = "child_", referencedColumnName = "id_", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RecordWrapper childR;

    @JoinColumn(name = "path_", referencedColumnName = "id_", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private RecordWrapper pathR;

    public RecordPath() {
    }

    public RecordPath(RecordPathPK recordPathPK) {
        this.recordPK = recordPathPK;
    }

    public RecordPath(Long parent, Long child, Long path) {
        this.recordPK = new RecordPathPK(parent, child, path);
    }

    public RecordPathPK getRecordPK() {
        return recordPK;
    }

    public void setRecordPK(RecordPathPK recordPathPK) {
        this.recordPK = recordPathPK;
    }

    public RecordWrapper getParentR() {
        return parentR;
    }

    public void setParentR(RecordWrapper parentR) {
        this.parentR = parentR;
    }

    public RecordWrapper getChildR() {
        return childR;
    }

    public void setChildR(RecordWrapper childR) {
        this.childR = childR;
    }

    public RecordWrapper getPathR() {
        return pathR;
    }

    public void setPathR(RecordWrapper pathR) {
        this.pathR = pathR;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (recordPK != null ? recordPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordPath)) {
            return false;
        }
        RecordPath other = (RecordPath) object;
        if ((this.recordPK == null && other.recordPK != null) || (this.recordPK != null && !this.recordPK.equals(other.recordPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordPath[ recordPathPK=" + recordPK + " ]";
    }

}
