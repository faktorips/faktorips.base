/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.contextcollector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void setUpFinderAndFormula() throws CoreException {
        when(finder.getExpression()).thenReturn(formula);
        when(finder.getIpsProject()).thenReturn(ipsProject);
        when(formula.getProductCmptGeneration()).thenReturn(generation);
        when(generation.findPolicyCmptType(ipsProject)).thenReturn(policyCmptType);
        when(generation.getProductCmpt()).thenReturn(productCmpt);
    }

    @Test
    public void testGetContextProductCmpts_correctType() throws Exception {
        when(node.getDatatype()).thenReturn(policyCmptType);

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
