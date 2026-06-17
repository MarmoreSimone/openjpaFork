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
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class TestParsedJPQL {

    // Query errate
    private static final String NULL_STRING = null;
    private static final String EMPTY_STRING = "";
    private static final String WRONG_STRUCTURE = "FROM User u SELECT u";
    private static final String MISSPELLED_CLAUSES = "SELETC u FORM User u";
    private static final String TRUNCATED_QUERY = "SELECT u FROM";
    private static final String UNBALANCED_PARENTHESES = "SELECT u FROM User u WHERE (u.id = 1 AND u.age > 18";
    private static final String MALFORMED_OPERATORS = "SELECT u FROM User u WHERE u.age <<= 22";
    private static final String ILLEGAL_CHARACTERS = "SELECT u FROM User u WHERE u.age = 18 @";
    private static final String BAD_AGGREGATION = "SELECT COUNT u FROM User u";
    private static final String TRUNCATED_PATH = "SELECT u. FROM User u";
    private static final String BAD_UPDATE = "UPDATE FROM User u SET u.age = 20";

    // Query corrette
    private static final String CORRECT_SELECT = "SELECT u FROM User u";
    private static final String CASE_INSENSITIVE = "sElEcT u fRoM User u WhErE u.id = 1";
    private static final String ESCAPED_QUOTES = "SELECT u FROM User u WHERE u.surname = 'D''Angelo'";
    private static final String DYNAMIC_PARAMS = "SELECT u FROM User u WHERE u.id = ?1 AND u.role = :roleName";
    private static final String SCALAR_FUNCTIONS = "SELECT UPPER(u.name), SQRT(u.age) FROM User u";

    //TC1
    @Test(expected = Exception.class)
    public void testNullString() {
        new JPQLExpressionBuilder.ParsedJPQL(NULL_STRING);
    }

    //TC2
    @Test(expected = Exception.class)
    public void testEmptyString() {
        new JPQLExpressionBuilder.ParsedJPQL(EMPTY_STRING);
    }

    //TC3
    @Test(expected = Exception.class)
    public void testWrongStructure() {
        new JPQLExpressionBuilder.ParsedJPQL(WRONG_STRUCTURE);
    }

    //TC4
    @Test(expected = Exception.class)
    public void testMisspelledClauses() {
        new JPQLExpressionBuilder.ParsedJPQL(MISSPELLED_CLAUSES);
    }

    //TC5
    @Test(expected = Exception.class)
    public void testTruncatedQuery() {
        new JPQLExpressionBuilder.ParsedJPQL(TRUNCATED_QUERY);
    }

    //TC6
    @Test(expected = Exception.class)
    public void testUnbalancedParentheses() {
        new JPQLExpressionBuilder.ParsedJPQL(UNBALANCED_PARENTHESES);
    }

    //TC7
    @Test(expected = Exception.class)
    public void testMalformedOperators() {
        new JPQLExpressionBuilder.ParsedJPQL(MALFORMED_OPERATORS);
    }

    //TC8
    @Test(expected = Exception.class)
    public void testIllegalCharacters() {
        new JPQLExpressionBuilder.ParsedJPQL(ILLEGAL_CHARACTERS);
    }

    //TC10
    @Test(expected = Exception.class)
    public void testBadAggregationSyntax() {
        new JPQLExpressionBuilder.ParsedJPQL(BAD_AGGREGATION);
    }

    //TC11
    @Test(expected = Exception.class)
    public void testTruncatedPathExpression() {
        new JPQLExpressionBuilder.ParsedJPQL(TRUNCATED_PATH);
    }

    //TC12
    @Test(expected = Exception.class)
    public void testMalformedUpdate() {
        new JPQLExpressionBuilder.ParsedJPQL(BAD_UPDATE);
    }

    //TC9
    @Test
    public void testValidSelectQuery() throws Exception {
        assertValidParsedJPQL(CORRECT_SELECT);
    }

    //TC13
    @Test
    public void testCaseInsensitiveKeywords() throws Exception {
        assertValidParsedJPQL(CASE_INSENSITIVE);
    }

    //TC14
    @Test
    public void testEscapedQuotes() throws Exception {
        assertValidParsedJPQL(ESCAPED_QUOTES);
    }

    //TC15
    @Test
    public void testDynamicParameters() throws Exception {
        assertValidParsedJPQL(DYNAMIC_PARAMS);
    }

    //TC16
    @Test
    public void testScalarFunctions() throws Exception {
        assertValidParsedJPQL(SCALAR_FUNCTIONS);
    }

    private void assertValidParsedJPQL(String inputQuery) throws Exception {
        JPQLExpressionBuilder.ParsedJPQL result = new JPQLExpressionBuilder.ParsedJPQL(inputQuery);
        assertEquals(inputQuery, result.toString());

        // in questo caso si e' usato reflection, senza usarlo si poteva direttamente vedere se l'oggetto Parsed.jpql fosse non nullo
        Field rootField = JPQLExpressionBuilder.ParsedJPQL.class.getDeclaredField("root");
        rootField.setAccessible(true);
        assertNotNull(rootField.get(result));
    }
}