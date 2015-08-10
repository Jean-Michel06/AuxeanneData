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
package com.auxeanne.api.data;

import com.auxeanne.api.data.ctrl.Record;
import java.io.Serializable;
import java.util.Objects;

/**
 * Default implementation to be extended by all POJOs managed by the framework.
 * Any field can then be added with the getters and setters.
 *
 * @author Jean-Michel Tanguy
 */
public abstract class DefaultRecord  implements Record, Serializable {

    @FieldExclusion
    private transient Long recordId;
    @FieldExclusion
    private transient byte[] document;
    @FieldExclusion
    private transient boolean documentChanged = false;

    /**
     * get the id of the record if persisted or null
     * @return unique database ID of the record
     */
    @Override
    public Long getId() {
        return recordId;
    }

    /**
     * Internal use only as the ID needs to me managed by a "Records" instance only. 
     * Set the database Id of the record.
     * @param recordId unique database ID of the record
     */
    @Override
    public void setId(Long recordId) {
        this.recordId = recordId;
    }

    /**
     * binary document stored in the database
     * @return the binary array
     */
    @Override
    public byte[] getDocument() {
        return document;
    }

    /**
     * Binary document stored in the database.
     * Flag isDocumentChanged is set to true.
     * @param document the binary array to store
     */
    @Override
    public void setDocument(byte[] document) {
        this.document = document;
        this.documentChanged = true;
    }

    /**
     * flag to prevent unnecessary binary write in the database  
     * @return document change status
     */
    @Override
    public boolean isDocumentChanged() {
        return documentChanged;
    }

    /**
     * Enable (true) or disable (false) the document write in the database.
     * This flag is set to true when using setter setDocument.
     * @param documentChanged new document status
     */
    @Override
    public void setDocumentChanged(boolean documentChanged) {
        this.documentChanged = documentChanged;
    }

    @Override
    public int hashCode() {
        int hash = recordId.hashCode();
        return hash;
    }

    /**
     * 
     * @param obj POJO to compare to
     * @return equality status
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        //if (getClass() != obj.getClass()) {
        //    return false;
        //}
        final DefaultRecord other = (DefaultRecord) obj;
        if (!Objects.equals(this.recordId, other.recordId)) {
            return false;
        }
        return true;
    }

    /**
     * using recordId only
     *
     * @return string value
     */
    @Override
    public String toString() {
        return "Record[ id=" + recordId + " ]";
    }

}
