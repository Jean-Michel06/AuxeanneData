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

/**
 * Interface of objects managed by the "Records" controller.
 * 
 * @author Jean-Michel Tanguy
 */
public interface Record {

    Long getId();

    void setId(Long recordId);

    byte[] getDocument();

    void setDocument(byte[] document);

    boolean isDocumentChanged();

    void setDocumentChanged(boolean documentChanged);

}
