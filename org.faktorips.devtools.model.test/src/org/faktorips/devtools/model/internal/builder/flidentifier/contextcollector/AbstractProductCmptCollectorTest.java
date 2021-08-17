/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.contextcollector;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractProductCmptCollectorTest {
    @Mock
    private ContextProductCmptFinder finder;

    @Mock
    private IdentifierNode node;

    private AbstractProductCmptCollector abstractProductCmptCollector;

    @Mock
    private IExpression expression;

    @Before
    public void setUpCollector() {
        abstractProductCmptCollector = new AbstractProductCmptCollector(node, finder) {

            @Override
            protected Set<IProductCmpt> getContextProductCmpts() {
                return null;
            }
        };
    }

    @Before
    public void setUpFinder() {
        when(finder.getExpression()).thenReturn(expression);
    }

    @Test
    public void testGetOriginGeneration_noFormula() throws Exception {
        assertNull(abstractProductCmptCollector.getOriginGeneration());
    }

    @Test
    public void testGetOriginGeneration_formula() throws Exception {
        IProductCmptGeneration generation = mockGenerationForFormula();

        assertSame(generation, abstractProductCmptCollector.getOriginGeneration());
    }

    private IProductCmptGeneration mockGenerationForFormula() {
        IFormula formula = mock(IFormula.class);
        IProductCmptGeneration generation = mock(IProductCmptGeneration.class);
        when(formula.getPropertyValueContainer()).thenReturn(generation);
        when(finder.getExpression()).thenReturn(formula);
        return generation;
    }

    @Test
    public void testGetValidFrom() throws Exception {
        GregorianCalendar validFrom = mock(GregorianCalendar.class);
        IProductCmptGeneration cmptGeneration = mockGenerationForFormula();
        when(cmptGeneration.getValidFrom()).thenReturn(validFrom);

        assertSame(validFrom, abstractProductCmptCollector.getValidFrom());
    }

}
