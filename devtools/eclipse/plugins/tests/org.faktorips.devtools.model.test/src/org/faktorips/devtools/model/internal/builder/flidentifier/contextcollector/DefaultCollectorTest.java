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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCollectorTest {
    @Mock
    private ContextProductCmptFinder finder;

    @Mock
    private IdentifierNode node;

    @InjectMocks
    private DefaultCollector defaultCollector;

    @Mock
    AbstractProductCmptCollector prevCollector;

    @Test
    public void testGetContextProductCmpts() throws Exception {
        Set<IProductCmpt> result = new HashSet<>();
        when(finder.createCollector()).thenReturn(prevCollector);
        when(prevCollector.getContextProductCmpts()).thenReturn(result);

        assertSame(result, defaultCollector.getContextProductCmpts());
    }

    @Test
    public void testGetContextProductCmpts_nullNode() throws Exception {
        defaultCollector = new DefaultCollector(null, finder);
        Set<IProductCmpt> result = new HashSet<>();
        when(finder.createCollector()).thenReturn(prevCollector);
        when(prevCollector.getContextProductCmpts()).thenReturn(result);

        assertSame(null, defaultCollector.getContextProductCmpts());
    }

}
