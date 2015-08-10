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
package com.auxeanne.api.data.ctrl;

import com.auxeanne.api.data.ctrl.ParameterManager.ParameterFilter;

/**
 * Option holder for queries on indexes.
 * @author Jean-Michel Tanguy
 */
public class IndexQuery {

    ParameterFilter query;
    String field;
    Object[] values;

    public IndexQuery(ParameterFilter query, String field, Object... values) {
        this.query = query;
        this.field = field;
        this.values = values;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public ParameterFilter getQuery() {
        return query;
    }

    public void setQuery(ParameterFilter query) {
        this.query = query;
    }

}
