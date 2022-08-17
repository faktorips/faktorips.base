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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContextProductCmptFinderTest {
    @Mock
    private IExpression expression;

    @Mock
    private IIpsProject ipsProject;

    private LinkedList<IdentifierNode> nodes = new LinkedList<>();

    @InjectMocks
    private ContextProductCmptFinder contextProductCmptFinder;

    @Mock
    private AbstractProductCmptCollector mockCollector;

    private Set<IProductCmpt> myContextCmpts;

    @Before
    public void setUpFinder() {
        contextProductCmptFinder = new ContextProductCmptFinder(nodes, expression, ipsProject);
    }

    @Before
    public void setUpContextCmpts() {
        myContextCmpts = new HashSet<>();
        for (int i = 5; i > 0; i--) {
            IProductCmpt productCmpt = mock(IProductCmpt.class);
            when(productCmpt.getName()).thenReturn("MyCmpt" + i);
            myContextCmpts.add(productCmpt);
        }
    }

    @Test
    public void testGetContextProductCmpts_noContext() throws Exception {
        ContextProductCmptFinder spy = spy(contextProductCmptFinder);
        when(spy.createCollector()).thenReturn(mockCollector);
        when(mockCollector.getContextProductCmpts()).thenReturn(null);

        List<IProductCmpt> contextProductCmps = spy.getContextProductCmpts();

        assertTrue(contextProductCmps.isEmpty());
    }

    @Test
    public void testGetContextProductCmpts() throws Exception {
        ContextProductCmptFinder spy = spy(contextProductCmptFinder);
        when(spy.createCollector()).thenReturn(mockCollector);
        when(mockCollector.getContextProductCmpts()).thenReturn(myContextCmpts);

        List<IProductCmpt> contextProductCmps = spy.getContextProductCmpts();

        assertEquals("MyCmpt1", contextProductCmps.get(0).getName());
        assertEquals("MyCmpt2", contextProductCmps.get(1).getName());
        assertEquals("MyCmpt3", contextProductCmps.get(2).getName());
        assertEquals("MyCmpt4", contextProductCmps.get(3).getName());
        assertEquals("MyCmpt5", contextProductCmps.get(4).getName());
    }

}
