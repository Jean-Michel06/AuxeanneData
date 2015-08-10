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

/**
 *
 * @author Jean-Michel Tanguy
 */
@Embeddable
public class RecordPathPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "parent_")
    private long parent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "child_")
    private long child;
    @Basic(optional = false)
    @NotNull
    @Column(name = "path_")
    private long path;

    public RecordPathPK() {
    }

    public RecordPathPK(long parent, long child, long path) {
        this.parent = parent;
        this.child = child;
        this.path = path;
    }

    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public long getChild() {
        return child;
    }

    public void setChild(long child) {
        this.child = child;
    }

    public long getPath() {
        return path;
    }

    public void setPath(long path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) parent;
        hash += (int) child;
        hash += (int) path;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordPathPK)) {
            return false;
        }
        RecordPathPK other = (RecordPathPK) object;
        if (this.parent != other.parent) {
            return false;
        }
        if (this.child != other.child) {
            return false;
        }
        if (this.path != other.path) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordPathPK[ parent=" + parent + ", child=" + child + ", path=" + path + " ]";
    }
    
}
