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
package com.auxeanne.data.ctrl;

import com.auxeanne.data.db.RecordAudit;
import com.auxeanne.data.db.RecordWrapper;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Dedicated operations to drop transactions in an audit table.
 * 
 *
 * @author Jean-Michel Tanguy
 */
public class AuditLogger {

    private String user;
    private DatabaseController mc;
    private boolean enabled = false;

    public AuditLogger() {

    }

    public AuditLogger(DatabaseController mc, String user) {
        this.mc = mc;
        this.user = user;
        enabled = true;
    }

    private void log(RecordAudit ra) {
        ra.setExecution(new Date());
        ra.setBy(user);
        mc.getTransactionEntityManager().persist(ra);
    }

    public void logCreateRecord(RecordWrapper rw) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("RECORD_CREATE");

            ra.setId(rw.getId());
            ra.setData(rw.getData());
            ra.setDocument(rw.getDocument());
            ra.setRecordType(rw.getRecordType().getCode());

            log(ra);
        }
    }

    public void logUpdateRecord(RecordWrapper rw, boolean documentChanged) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("RECORD_UPDATE");
            //
            ra.setId(rw.getId());
            ra.setData(rw.getData());
            if (documentChanged) {
                ra.setDocument(rw.getDocument());
            }
            ra.setRecordType(rw.getRecordType().getCode());
            //
            log(ra);
        }
    }

    public void logRemoveRecord(RecordWrapper rw) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("RECORD_REMOVE");
            //
            ra.setId(rw.getId());
            ra.setRecordType(rw.getRecordType().getCode());
            //
            log(ra);
        }
    }

    public void logCreateLink(Long reference, Long link, String value, BigDecimal numeric, Date date) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("LINK_CREATE");
            //
            ra.setLink_(link);
            ra.setReference_(reference);
            ra.setValue(value);
            ra.setNumeric(numeric);
            ra.setDate(date);
            //
            log(ra);
        }
    }

    public void logRemoveLink(Long reference, Long link) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("LINK_REMOVE");
            //
            ra.setLink_(link);
            ra.setReference_(reference);
            //
            log(ra);
        }
    }

    public void logPreference(String key, String value) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("PREFERENCE");
            //
            ra.setValue(key);
            ra.setData(value);
            //
            log(ra);
        }
    }

    public void logSavePath(Long parent, Long child) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("PATH_SAVE");
            //
            ra.setLink_(child);
            ra.setReference_(parent);
            //
            log(ra);
        }
    }

    public void logRemovePath(Long parent, Long child) {
        if (enabled) {
            RecordAudit ra = new RecordAudit();
            ra.setAction("PATH_REMOVE");
            //
            ra.setLink_(child);
            ra.setReference_(parent);
            //
            log(ra);
        }
    }

}
