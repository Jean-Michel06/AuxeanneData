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
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Index;

/**
 * Entity mapping the database.
 *
 * @author Jean-Michel Tanguy
 */
@Entity
@Table(name = "record_link")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RecordLink.findExistingLinks", query = "SELECT rl FROM RecordLink rl WHERE rl.recordLinkPK.reference_ = :id AND rl.recordLinkPK.link_ in :list"),
    @NamedQuery(name = "RecordLink.findExistingReferences", query = "SELECT rl FROM RecordLink rl WHERE rl.recordLinkPK.link_ = :id AND rl.recordLinkPK.reference_ in :list"),
    @NamedQuery(name = "RecordLink.findByReferenceId", query = "SELECT rl.recordLinkPK FROM RecordLink rl  WHERE  rl.reference.id = :id"),
    @NamedQuery(name = "RecordLink.findByLinkId", query = "SELECT rl.recordLinkPK FROM RecordLink rl  WHERE  rl.link.id = :id"),
    @NamedQuery(name = "RecordLink.findByReferenceIdList", query = "SELECT rl.recordLinkPK FROM RecordLink rl  WHERE  rl.reference.id in :list"),
    @NamedQuery(name = "RecordLink.findByLinkIdList", query = "SELECT rl.recordLinkPK FROM RecordLink rl  WHERE  rl.link.id in :list")
   })
@Index(name = "EMP_NAME_INDEX", columnNames = {"reference_", "link_"})
@Cacheable(true)
//-- @Multitenant() >> Multi tenant is derived from RecordType
public class RecordLink implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected RecordLinkPK recordLinkPK;
    @Index
    @Size(max = 255)
    @Column(name = "value_")
    private String value;
    @Index
    @Column(name = "numeric_")
    private BigDecimal numeric;
    @Index
    @Column(name = "date_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    
    
    @JoinColumn(name = "reference_", referencedColumnName = "id_", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private RecordWrapper reference;
    @JoinColumn(name = "link_", referencedColumnName = "id_", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private RecordWrapper link;

    public RecordLink() {
    }

    public RecordLink(RecordLinkPK recordLinkPK) {
        this.recordLinkPK = recordLinkPK;
    }

    public RecordLink(int idRecord, int idLink) {
        this.recordLinkPK = new RecordLinkPK(idRecord, idLink);
    }

    public RecordLinkPK getRecordLinkPK() {
        return recordLinkPK;
    }

    public void setRecordLinkPK(RecordLinkPK recordLinkPK) {
        this.recordLinkPK = recordLinkPK;
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

 

    public RecordWrapper getReference() {
        return reference;
    }

    public void setReference(RecordWrapper reference) {
        this.reference = reference;
    }

    public RecordWrapper getLink() {
        return link;
    }

    public void setLink(RecordWrapper link) {
        this.link = link;
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
        hash += (recordLinkPK != null ? recordLinkPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordLink)) {
            return false;
        }
        RecordLink other = (RecordLink) object;
        if ((this.recordLinkPK == null && other.recordLinkPK != null) || (this.recordLinkPK != null && !this.recordLinkPK.equals(other.recordLinkPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordLink[ recordLinkPK=" + recordLinkPK + " ]";
    }

}
