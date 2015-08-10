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
package com.auxeanne.api.data.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean-Michel
 */
public class ComplexRecord {

    List<PersonRecord> list;
    HashMap<String, PersonRecord> map;

    public ComplexRecord() {
        list = new ArrayList<>();
        map = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            PersonRecord pr = new PersonRecord();
            pr.setFirstName("John");
            pr.setLastName("Do");
            list.add(pr);
            map.put(i + "", pr);
        }
    }

    public List<PersonRecord> getList() {
        return list;
    }

    public void setList(List<PersonRecord> list) {
        this.list = list;
    }

    public HashMap<String, PersonRecord> getMap() {
        return map;
    }

    public void setMap(HashMap<String, PersonRecord> map) {
        this.map = map;
    }

}
