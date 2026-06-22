/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.kernel.jpql;

import org.junit.Test;
import org.apache.openjpa.util.UserException;
import org.junit.Assert;

import java.lang.reflect.Field;
public class TestParserFacade {

    //TC1
    @Test
    public void stringNull() {
        Assert.assertThrows(
                ParseException.class,
                () -> new JPQLExpressionBuilder.ParsedJPQL(null)
        );
    }

    //TC2
    @Test
    public void stringEmpty() {
        Assert.assertThrows(
                ParseException.class,
                () -> new JPQLExpressionBuilder.ParsedJPQL("")
        );
    }

    //TC3
    // struttura della query invertita
    @Test
    public void syntacticErrorInQuery() {
        Assert.assertThrows(
                ParseException.class,
                () -> new JPQLExpressionBuilder.ParsedJPQL("FROM User u SELECT u")
        );
    }

    //TC4
    // il @ é un carattere non riconosciuto
    @Test
    public void lexicalErrorInQuery() {
        Assert.assertThrows(
                UserException.class,
                () -> new JPQLExpressionBuilder.ParsedJPQL("@ User u SELECT u")
        );
    }

    //TC5
    // generiamo una query con 10.000 parentesi annidate per forzare la ricorsione infinita del parser e
    // mandare in overflow lo stack
    @Test
    public void queryStackOverflow() {

        int k = 10000;
        StringBuilder query = new StringBuilder();

        query.append("SELECT e FROM Entity e WHERE ");
        for (int i = 0; i < k; i++) {
            query.append("(");
        }
        query.append("e.id = 1");
        for (int i = 0; i < k; i++) {
            query.append(")");
        }

        Assert.assertThrows(
                UserException.class,
                () -> new JPQLExpressionBuilder.ParsedJPQL(query.toString())
        );

    }

   //TC6
    @Test
    public void queryValida() throws Exception {
        String queryValida = "SELECT u FROM User u";
        JPQLExpressionBuilder.ParsedJPQL parsedJPQL = new JPQLExpressionBuilder.ParsedJPQL(queryValida);

        // verifica che la stringa sia stata salvata correttamente
        Assert.assertEquals("la stringa della query salvata nello stato non corrisponde all'input", queryValida, parsedJPQL.toString());

        // verifica che l'AST non sia nullo tramite reflection
        Field rootField = JPQLExpressionBuilder.ParsedJPQL.class.getDeclaredField("root");
        rootField.setAccessible(true);
        Object rootValue = rootField.get(parsedJPQL);
        Assert.assertNotNull("L'AST (il nodo root) è nullo, il parsing è fallito", rootValue);
    }
}


