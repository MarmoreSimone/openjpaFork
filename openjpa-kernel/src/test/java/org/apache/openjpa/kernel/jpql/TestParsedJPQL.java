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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestParsedJPQL {

    private static final String NULL_STRING = null;
    private static final String EMPTY_STRING = "";
    private static final String WRONG_STRUCTURE = "FROM User u SELECT u";
    private static final String MISSPELLED_CLAUSES = "SELETC u FORM User u";
    private static final String TRUNCATED_QUERY = "SELECT u FROM";
    private static final String UNBALANCED_PARENTHESES = "SELECT u FROM User u WHERE (u.id = 1 AND u.age > 18";
    private static final String MALFORMED_OPERATORS = "SELECT u FROM User u WHERE u.age <<= 22";
    private static final String ILLEGAL_CHARACTERS = "SELECT u FROM User u WHERE u.age = 18 @";
    private static final String CORRECT_QUERY = "SELECT u FROM User u";

    @Test(expected = Exception.class)
    public void testWrongStructure() {
        new JPQLExpressionBuilder.ParsedJPQL(WRONG_STRUCTURE);
    }

    @Test(expected = Exception.class)
    public void testNullString() {
        new JPQLExpressionBuilder.ParsedJPQL(NULL_STRING);
    }

    @Test(expected = Exception.class)
    public void testEmptyString() {
        new JPQLExpressionBuilder.ParsedJPQL(EMPTY_STRING);
    }

    @Test(expected = Exception.class)
    public void testMisspelledClauses() {
        new JPQLExpressionBuilder.ParsedJPQL(MISSPELLED_CLAUSES);
    }

    @Test(expected = Exception.class)
    public void testTruncatedQuery() {
        new JPQLExpressionBuilder.ParsedJPQL(TRUNCATED_QUERY);
    }

    @Test(expected = Exception.class)
    public void testUnbalancedParentheses() {
        new JPQLExpressionBuilder.ParsedJPQL(UNBALANCED_PARENTHESES);
    }

    @Test(expected = Exception.class)
    public void testMalformedOperators() {
        new JPQLExpressionBuilder.ParsedJPQL(MALFORMED_OPERATORS);
    }

    @Test(expected = Exception.class)
    public void testIllegalCharacters() {
        new JPQLExpressionBuilder.ParsedJPQL(ILLEGAL_CHARACTERS);
    }

    @Test(expected = Exception.class)
    public void testLongQuery() {
        StringBuilder infiniteQuery = new StringBuilder("SELECT u FROM User u WHERE ");
        for (int i = 0; i < 10000; i++) infiniteQuery.append("(");
        infiniteQuery.append(" u.id = 1 ");
        for (int i = 0; i < 10000; i++) infiniteQuery.append(")");
        new JPQLExpressionBuilder.ParsedJPQL(infiniteQuery.toString());
    }

    @Test
    public void correctQuery(){
        try {
            JPQLExpressionBuilder.JPQLNode root = (JPQLExpressionBuilder.JPQLNode) new JPQL(CORRECT_QUERY).parseQuery();
            assertNotNull(root);
        } catch (ParseException ex) {
            fail();
        }
    }
}