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
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Multitenant;

/**
 * Entity mapping the database.
 *
 * @author Jean-Michel Tanguy
 */
@Entity
@Table(name = "record_audit")
@XmlRootElement
@Cacheable(false)
@Multitenant()
public class RecordAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RECORD_AUDIT_GEN")
    @TableGenerator(name = "RECORD_AUDIT_GEN", allocationSize = 100, initialValue = 1, pkColumnValue = "RecordAudit")
    @Basic(optional = false)
    @Column(name = "log_id_")
    private Long log_id_;
    //  
    @Column(name = "record_id_")
    private Long id;
    @Lob
    @Column(name = "data_")
    private byte[] data;
    @Lob
    @Column(name = "document_")
    private byte[] document;
    @Column(name = "record_type_")
    private int recordType;
    @Column(name = "reference_")
    private Long reference_;
    @Column(name = "link_")
    private Long link_;
    @Size(max = 255)
    @Column(name = "value_")
    private String value;
    @Column(name = "numeric_")
    private BigDecimal numeric;
    @Column(name = "date_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    //
    @Column(name = "execution_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date execution;
    @Column(name = "action_")
    private String action;
    @Column(name = "by_")
    private String by;

    // mapping to default tenant column for master management
    @Column(name = "TENANT_ID", insertable = false, updatable = false)
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public RecordAudit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public int getRecordType() {
        return recordType;
    }

    public void setRecordType(int recordType) {
        this.recordType = recordType;
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

    public Date getExecution() {
        return execution;
    }

    public void setExecution(Date execution) {
        this.execution = execution;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
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
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RecordAudit)) {
            return false;
        }
        RecordAudit other = (RecordAudit) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordAudit[ id=" + id + " ]";
    }

    public Long getLog_id_() {
        return log_id_;
    }

    public void setLog_id_(Long log_id_) {
        this.log_id_ = log_id_;
    }

}
