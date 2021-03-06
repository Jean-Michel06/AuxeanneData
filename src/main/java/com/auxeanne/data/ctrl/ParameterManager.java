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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Parameter mapping to database.
 *
 * @author Jean-Michel Tanguy
 */
public class ParameterManager {

    public static enum ParameterFilter {

        EQUAL_TO, NOT_EQUAL_TO, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN_OR_EQUAL_TO, ORDER_BY, REVERSE_BY, IN, NOT_IN, LIKE, NOT_LIKE
    };

    public static enum ParameterType {

        STRING, NUMERIC, DATE
    };

    public void filter(CriteriaBuilder cb, List<Predicate> subP, Root subRoot, ParameterFilter filter, ParameterType indexType, Comparable[] values) {
        Path indexPath = getPath(subRoot, indexType);
        switch (filter) {
            case EQUAL_TO:
                subP.add(cb.equal(indexPath, values[0]));
                break;
            case NOT_EQUAL_TO:
                subP.add(cb.notEqual(indexPath, values[0]));
                break;
            case GREATER_THAN:
                subP.add( cb.greaterThan(indexPath, values[0])  );
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                subP.add( cb.greaterThanOrEqualTo(indexPath, values[0]));
                break;
            case LESS_THAN:
                subP.add(cb.lessThan(indexPath, values[0]));
                break;
            case LESS_THAN_OR_EQUAL_TO:
                subP.add(cb.lessThanOrEqualTo(indexPath, values[0]));
                break;
            case IN:
                subP.add(indexPath.in((Object[]) values));
                break;
            case NOT_IN:
                subP.add(cb.not(indexPath.in((Object[]) values)));
                break;
            case LIKE:
                Predicate[] ors = new Predicate[values.length];
                for (int i = 0; i < values.length; i++) {
                    ors[i] = cb.like(indexPath, values[i].toString());
                }
                subP.add(cb.or(ors));
                break;
            case NOT_LIKE:
                Predicate[] nors = new Predicate[values.length];
                for (int i = 0; i < values.length; i++) {
                    nors[i] = cb.notLike(indexPath, values[i].toString());
                }
                subP.add(cb.or(nors));
                break;
        }
    }

    public Comparable getConverted(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> type = value.getClass();
        if (Number.class.isAssignableFrom(type)) {
            return new BigDecimal(value.toString());
        } else if (Date.class.isAssignableFrom(type)) {
            return (Date) value;
        } else if (Calendar.class.isAssignableFrom(type)) {
            return ((Calendar) value).getTime();
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            LocalDateTime ldt = (LocalDateTime) value;
            Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
            Date res = Date.from(instant);
            return res;
        } else if (LocalDate.class.isAssignableFrom(type)) {
            LocalDate ldt = (LocalDate) value;
            Instant instant = ldt.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            Date res = Date.from(instant);
            return res;
        } else if (LocalTime.class.isAssignableFrom(type)) {
            LocalTime lt = (LocalTime) value; // setting same date for all the times for comparison
            Instant instant = lt.atDate(LocalDate.of(2000, Month.JANUARY, 1)).atZone(ZoneId.systemDefault()).toInstant();
            Date res = Date.from(instant);
            return res;
        } else {
            return value.toString();
        }
    }

    public ParameterType getType(Class typeClass) {
        if (Number.class.isAssignableFrom(typeClass)) {
            return ParameterType.NUMERIC;
        } else if (Date.class.isAssignableFrom(typeClass)) {
            return ParameterType.DATE;
        } else if (Calendar.class.isAssignableFrom(typeClass)) {
            return ParameterType.DATE;
        } else if (LocalDateTime.class.isAssignableFrom(typeClass)) {
            return ParameterType.DATE;
        } else if (LocalDate.class.isAssignableFrom(typeClass)) {
            return ParameterType.DATE;
        } else {
            return ParameterType.STRING;
        }
    }

    public Path getPath(Path subRoot, ParameterType type) {
        switch (type) {
            case STRING:
                return subRoot.get("value");
            case NUMERIC:
                return subRoot.get("numeric");
            case DATE:
                return subRoot.<Date>get("date");
        }
        throw new IllegalArgumentException();
    }

    public Path getPath(Root subRoot, Class typeClass) {
        ParameterType type_ = getType(typeClass);
        return getPath(subRoot, type_);
    }

}
