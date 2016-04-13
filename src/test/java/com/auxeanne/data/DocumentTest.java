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

import com.auxeanne.data.record.PersonRecord;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing binary document management.
 *
 * @author Jean-Michel Tanguy
 */
public class DocumentTest {


    @BeforeClass
    public static void setUpClass() {
        System.out.println("\n============================\nDOCUMENTS AND BINARY\n============================");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("");
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    ////@Test
    public void image() throws IOException, URISyntaxException {
        URI uri = this.getClass().getResource("/com/auxeanne/api/data/image/image.png").toURI();

        for (String pu : PU.getPuList()) {
            System.out.println("\n=== Testing " + pu);
            Records records = new Records(PU.getFactoryList().get(pu));
            // saving image
            byte[] image1 = Files.readAllBytes(Paths.get(uri));
            PersonRecord p1 = new PersonRecord();
            p1.setLastName("image1");
          ////  p1.setDocument(image1);
            records.save(p1);
            // fetching back
            PersonRecord p2 = records.query(PersonRecord.class).indexEqualTo("lastName", "image1").getFirst();
          ////  byte[] image2 = p2.getDocument();
            // comparing
           //// assertArrayEquals(image1, image2);
        }

    }
}
