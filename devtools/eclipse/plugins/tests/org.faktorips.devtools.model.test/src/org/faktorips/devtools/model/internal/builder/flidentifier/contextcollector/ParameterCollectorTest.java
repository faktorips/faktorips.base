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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterCollectorTest {
    @Mock
    private ContextProductCmptFinder finder;

    @Mock
    private ParameterNode node;

    @InjectMocks
    private ParameterCollector parameterCollector;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IPolicyCmptType superPolicyCmptType;

    @Mock
    private Datatype datatype;

    @Mock
    private IFormula formula;

    @Mock
    private IProductCmpt productCmpt;

    @Mock
    private IProductCmptGeneration generation;

    @Mock
    private IIpsProject ipsProject;

    @Before
    public void setUpFinderAndFormula() {
        when(finder.getExpression()).thenReturn(formula);
        when(finder.getIpsProject()).thenReturn(ipsProject);
        when(formula.getPropertyValueContainer()).thenReturn(generation);
        when(generation.findPolicyCmptType(ipsProject)).thenReturn(policyCmptType);
        when(generation.getProductCmpt()).thenReturn(productCmpt);
        when(policyCmptType.isSubtypeOrSameType(policyCmptType, ipsProject)).thenReturn(true);
        when(policyCmptType.isSubtypeOrSameType(superPolicyCmptType, ipsProject)).thenReturn(true);
    }

    @Test
    public void testGetContextProductCmpts_correctType() throws Exception {
        when(node.getDatatype()).thenReturn(policyCmptType);

        Set<IProductCmpt> contextProductCmpts = parameterCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

    @Test
    public void testGetContextProductCmpts_superType() throws Exception {
        when(node.getDatatype()).thenReturn(superPolicyCmptType);

        Set<IProductCmpt> contextProductCmpts = parameterCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

    @Test
    public void testGetContextProductCmpts_noPolicyCmptType() throws Exception {
        when(node.getDatatype()).thenReturn(datatype);

        Set<IProductCmpt> contextProductCmpts = parameterCollector.getContextProductCmpts();

        assertNull(contextProductCmpts);
    }

    @Test
    public void testGetContextProductCmpts_wrongPolicyCmptType() throws Exception {
        IPolicyCmptType policyCmptType2 = mock(IPolicyCmptType.class);
        when(node.getDatatype()).thenReturn(policyCmptType2);

        Set<IProductCmpt> contextProductCmpts = parameterCollector.getContextProductCmpts();

        assertNull(contextProductCmpts);
    }

}
