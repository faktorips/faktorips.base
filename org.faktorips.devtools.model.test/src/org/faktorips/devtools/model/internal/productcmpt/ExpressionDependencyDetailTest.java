/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.util.TextRegion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionDependencyDetailTest {

    private static final String NEW_NAME = "super";

    private static final String EXPRESSION_TEXT = "My tiny little qualified expression to be replaces";

    private static final String NEW_EXPRESSION_TEXT = "My tiny little super expression to be replaces";

    private static final String NEW_EXPRESSION_TEXT2 = "My tiny little super super to be replaces";

    private static final String NEW_EXPRESSION_TEXT3 = "My tiny little super super super be replaces";

    @Mock
    private IExpression expression;

    @Mock
    private IIpsPackageFragment targetIpsPackageFragment;

    private TextRegion textRegion1 = new TextRegion(EXPRESSION_TEXT, 15, 24);

    private TextRegion textRegion2 = new TextRegion(EXPRESSION_TEXT, 25, 35);

    private TextRegion textRegion3 = new TextRegion(EXPRESSION_TEXT, 36, 38);

    private ExpressionDependencyDetail expressionDependencyDetail;

    @Before
    public void createExpressionDependencyDetail() throws Exception {
        expressionDependencyDetail = new ExpressionDependencyDetail(expression);
    }

    @Test
    public void testRefactorAfterRename() throws Exception {
        expressionDependencyDetail.addTextRegion(textRegion1);
        when(expression.getExpression()).thenReturn(EXPRESSION_TEXT);

        expressionDependencyDetail.refactorAfterRename(targetIpsPackageFragment, NEW_NAME);

        verify(expression).setExpression(NEW_EXPRESSION_TEXT);
    }

    @Test
    public void testRefactorAfterRename_twoRegions() throws Exception {
        expressionDependencyDetail.addTextRegion(textRegion2);
        expressionDependencyDetail.addTextRegion(textRegion1);
        when(expression.getExpression()).thenReturn(EXPRESSION_TEXT);

        expressionDependencyDetail.refactorAfterRename(targetIpsPackageFragment, NEW_NAME);

        verify(expression).setExpression(NEW_EXPRESSION_TEXT2);
    }

    @Test
    public void testRefactorAfterRename_threeRegions() throws Exception {
        expressionDependencyDetail.addTextRegion(textRegion2);
        expressionDependencyDetail.addTextRegion(textRegion1);
        expressionDependencyDetail.addTextRegion(textRegion3);
        when(expression.getExpression()).thenReturn(EXPRESSION_TEXT);

        expressionDependencyDetail.refactorAfterRename(targetIpsPackageFragment, NEW_NAME);

        verify(expression).setExpression(NEW_EXPRESSION_TEXT3);
    }

}
