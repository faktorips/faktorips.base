/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.refactor.TextRegion;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
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

    @Mock
    private IExpression expression;

    @Mock
    private IIpsPackageFragment targetIpsPackageFragment;

    private TextRegion textRegion = new TextRegion(15, 24);

    private ExpressionDependencyDetail expressionDependencyDetail;

    @Before
    public void createExpressionDependencyDetail() throws Exception {
        expressionDependencyDetail = new ExpressionDependencyDetail(expression, textRegion);
    }

    @Test
    public void testRefactorAfterRename() throws Exception {
        when(expression.getExpression()).thenReturn(EXPRESSION_TEXT);

        expressionDependencyDetail.refactorAfterRename(targetIpsPackageFragment, NEW_NAME);

        verify(expression).setExpression(NEW_EXPRESSION_TEXT);
    }
}
