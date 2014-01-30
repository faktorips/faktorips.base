/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.Token;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionTest {

    @Mock
    private SimpleNode simpleNode;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Expression expression;

    @Test
    public void testGetPosition_simple() throws Exception {
        mockExpression("Test", 0, 2);

        int position = expression.getPosition(simpleNode);

        assertEquals(1, position);
    }

    @Test
    public void testGetPosition_normalLineBreak() throws Exception {
        mockExpression("AnyExpressionblabdaslb\n\nasdas dasd \nabc123", 3, 2);

        int position = expression.getPosition(simpleNode);

        assertEquals(25, position);
    }

    @Test
    public void testGetPosition_macLineBreak() throws Exception {
        mockExpression("AnyExpressionblabdaslb\r\rasdas dasd \rabc123", 3, 2);

        int position = expression.getPosition(simpleNode);

        assertEquals(25, position);
    }

    @Test
    public void testGetPosition_winLineBreak() throws Exception {
        mockExpression("AnyExpressionblabdaslb\r\n\r\nasdas dasd \r\nabc123", 3, 2);

        int position = expression.getPosition(simpleNode);

        assertEquals(27, position);
    }

    private void mockExpression(String value, int line, int column) {
        when(expression.getExpression()).thenReturn(value);
        Token token = new Token();
        token.beginLine = line;
        token.beginColumn = column;
        when(simpleNode.getFirstToken()).thenReturn(token);
    }

}
