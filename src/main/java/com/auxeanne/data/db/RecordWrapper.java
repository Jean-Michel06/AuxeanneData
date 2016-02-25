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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.Index;

/**
 * Entity mapping the database.
 *
 * @author Jean-Michel Tanguy
 */
@Entity
@Table(name = "record_wrapper")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RecordWrapper.deleteByRecordId", query = "DELETE FROM RecordWrapper r WHERE r.id = :record"),
    @NamedQuery(name = "RecordWrapper.deleteByRecordIdList", query = "DELETE FROM RecordWrapper r WHERE r.id in :list"),
    @NamedQuery(name = "RecordWrapper.findByRecordType", query = "SELECT r FROM RecordWrapper r WHERE r.recordType = :recordType"),
    @NamedQuery(name = "RecordWrapper.searchByDataAndRecordType", query = "SELECT r FROM RecordWrapper r WHERE r.data like :search AND r.recordType = :recordType")
})
@Cacheable(true)
@Index(name = "EMP_NAME_INDEX", columnNames = {"tenant_", "record_type_"})
//-- @Customizer(com.auxeanne.api.data.util.HistoryCustomizer.class) >> Hit on performance, so setting up custom Auditing
public class RecordWrapper implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recordWrapper")
    private List<RecordIndex> recordIndexList;

    private static final long serialVersionUID = 1L;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parentR")
    private List<RecordPath> parentList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "childR")
    private List<RecordPath> childList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pathR")
    private List<RecordPath> pathList;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RECORD_GEN")
    @TableGenerator(name = "RECORD_GEN", allocationSize = 100, initialValue = 1, pkColumnValue = "Record")
    @Basic(optional = false)
    @Column(name = "id_")
    private Long id;
    @Lob
    @Column(name = "data_")
    private String data;
    @Index
    @Column(name = "record_type_")
    private int recordType;
    @OneToMany(mappedBy = "reference", fetch = FetchType.LAZY)
    private List<RecordLink> referenceList;
    @OneToMany(mappedBy = "link", fetch = FetchType.LAZY)
    private List<RecordLink> linkList;
    @Lob
    @Column(name = "document_")
    private byte[] document;
    @Index
    @Column(name = "tenant_")
    private String tenant;

    public RecordWrapper() {
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public RecordWrapper(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
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

    @XmlTransient
    public List<RecordLink> getReferenceList() {
        return referenceList;
    }

    public void setReferenceList(List<RecordLink> referenceList) {
        this.referenceList = referenceList;
    }

    @XmlTransient
    public List<RecordLink> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<RecordLink> linkList) {
        this.linkList = linkList;
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
        if (!(object instanceof RecordWrapper)) {
            return false;
        }
        RecordWrapper other = (RecordWrapper) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RecordWrapper[ id=" + id + " ]";
    }

    @XmlTransient
    public List<RecordPath> getParentList() {
        return parentList;
    }

    public void setParentList(List<RecordPath> parentList) {
        this.parentList = parentList;
    }

    @XmlTransient
    public List<RecordPath> getChildList() {
        return childList;
    }

    public void setChildList(List<RecordPath> childList) {
        this.childList = childList;
    }

    @XmlTransient
    public List<RecordPath> getPathList() {
        return pathList;
    }

    public void setPathList(List<RecordPath> pathList) {
        this.pathList = pathList;
    }

    @XmlTransient
    public List<RecordIndex> getRecordIndexList() {
        return recordIndexList;
    }

    public void setRecordIndexList(List<RecordIndex> recordIndexList) {
        this.recordIndexList = recordIndexList;
    }

}
