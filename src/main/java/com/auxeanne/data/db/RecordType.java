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
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.Multitenant;

/**
 * Entity mapping the database.
 * 
 * @author Jean-Michel Tanguy
 */
@Entity
@Table(name = "record_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RecordType.findByCode", query = "SELECT rt FROM RecordType rt WHERE rt.code = :code")})
@Cacheable(true)
@Multitenant() 
public class RecordType implements Serializable { 

    private static final long serialVersionUID = 1L;
    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "RECORD_TYPE_GEN") 
    @TableGenerator(name="RECORD_TYPE_GEN", allocationSize = 2, initialValue = 1, pkColumnValue="RecordType")

    @Basic(optional = false)
    @Column(name = "id_")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "code_")
    private String code;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recordType")
    private List<RecordWrapper> recordList;
    
    // mapping to default tenant column for master management
    @Column(name = "TENANT_ID", insertable = false, updatable = false)
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public RecordType() {
    }

    public RecordType(Integer id) {
        this.id = id;
    }

    public RecordType(Integer id, String code) {
        this.id = id;
        this.code = code;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }



    @XmlTransient
    public List<RecordWrapper> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<RecordWrapper> recordList) {
        this.recordList = recordList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordType)) {
            return false;
        }
        RecordType other = (RecordType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordType[ id=" + id + " ]";
    }




}
