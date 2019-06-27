/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.contextcollector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.faktorips.devtools.core.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QualifierCollectorTest {
    @Mock
    private ContextProductCmptFinder finder;

    @Mock
    private QualifierNode node;

    @InjectMocks
    private QualifierCollector qualifierCollector;

    @Mock
    private IProductCmpt productCmpt;

    @Test
    public void testGetContextProductCmpts() throws Exception {
        when(node.getProductCmpt()).thenReturn(productCmpt);

        Set<IProductCmpt> contextProductCmpts = qualifierCollector.getContextProductCmpts();

        assertEquals(1, contextProductCmpts.size());
        assertThat(contextProductCmpts, hasItem(productCmpt));
    }

}
