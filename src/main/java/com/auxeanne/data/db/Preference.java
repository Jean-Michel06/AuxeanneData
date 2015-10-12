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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Multitenant;

/**
 * Entity mapping the database.
 * 
 * @author Jean-Michel Tanguy
 */
@Entity
@Table(name = "preference")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Preference.findAll", query = "SELECT p FROM Preference p"),
    @NamedQuery(name = "Preference.findById", query = "SELECT p FROM Preference p WHERE p.id = :id"),
    @NamedQuery(name = "Preference.findByKey", query = "SELECT p FROM Preference p WHERE p.key = :key")})
@Multitenant() // using column multitenant (default) as table per tenant is not supported by EclipseLink DDL generation
public class Preference implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue(strategy = GenerationType.TABLE, generator = "PREFERENCE_GEN") 
    @TableGenerator(name="PREFERENCE_GEN", allocationSize = 5, initialValue = 1, pkColumnValue="Preference")
    @Basic(optional = false)
    @Column(name = "id_")
    private Integer id;
     

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "key_")
    private String key;
    @Lob
    @Size(max = 65535)
    @Column(name = "value_")
    private String value;
    
    // mapping to default tenant column for master management
    @Column(name = "TENANT_ID", insertable = false, updatable = false)
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }


    public Preference() {
    }

    public Preference(Integer id) {
        this.id = id;
    }

    public Preference(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        if (!(object instanceof Preference)) {
            return false;
        }
        Preference other = (Preference) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Preference[ id=" + id + " ]";
    }
    
}
