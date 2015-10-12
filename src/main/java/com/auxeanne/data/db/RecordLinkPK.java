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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Entity mapping the database.
 * 
 * @author Jean-Michel Tanguy
 */
@Embeddable
public class RecordLinkPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "reference_")
    private long reference_;
    @Basic(optional = false)
    @Column(name = "link_")
    private long link_;
    
    public RecordLinkPK() {
    }

    public RecordLinkPK(long reference_, long link_) {
        this.reference_ = reference_;
        this.link_ = link_;
    }

    public long getReference_() {
        return reference_;
    }

    public void setReference_(long reference_) {
        this.reference_ = reference_;
    }

    public long getLink_() {
        return link_;
    }

    public void setLink_(long link_) {
        this.link_ = link_;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) reference_;
        hash += (int) link_;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordLinkPK)) {
            return false;
        }
        RecordLinkPK other = (RecordLinkPK) object;
        if (this.reference_ != other.reference_) {
            return false;
        }
        if (this.link_ != other.link_) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordLinkPK[ referenceId=" + reference_ + ", linkId=" + link_ + " ]";
    }
    
}
