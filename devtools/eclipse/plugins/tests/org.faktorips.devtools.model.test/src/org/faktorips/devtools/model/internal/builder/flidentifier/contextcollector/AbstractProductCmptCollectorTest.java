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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class AbstractProductCmptCollectorTest {
    @Mock
    private ContextProductCmptFinder finder;

    @Mock
    private IdentifierNode node;

    private AbstractProductCmptCollector abstractProductCmptCollector;

    @Mock
    private IExpression expression;

    @BeforeEach
    public void setUpCollector() {
        abstractProductCmptCollector = new AbstractProductCmptCollector(node, finder) {

            @Override
            protected Set<IProductCmpt> getContextProductCmpts() {
                return null;
            }
        };
    }

    @BeforeEach
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
