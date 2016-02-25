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
package com.auxeanne.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Indexing the field.</p>
 * <p>Indexing a field allows finer query filtering and sorting.</p>
 * <p>By default an index is bound to the field only. Instead, a shared index can be used by providing a key name.</p>
 * 
 * @author Jean-Michel Tanguy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) 
public @interface FieldIndexing {
    
    /**
     * optional key allowing merging of indexes of multiple fields 
     * @return key to use as index
     */
    String value() default "";
    
}
