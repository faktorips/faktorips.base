/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierNodeCollectorTest {

    @Mock
    private AbstractIdentifierNodeParser parser;
    @Mock
    private IdentifierNode node1;
    @Mock
    private IdentifierNode node2;
    private IdentifierNodeCollector collector;

    @Before
    public void setUp() {
        when(parser.isMatchingNode(node1, "")).thenReturn(false);
        when(parser.isMatchingNode(node2, "")).thenReturn(true);
        collector = new IdentifierNodeCollector(parser);
    }

    @Test
    public void testAddMatching() {
        collector.addMatchingNode(node1, "");
        assertTrue(collector.getNodes().isEmpty());

        collector.addMatchingNode(node2, "");
        assertEquals(1, collector.getNodes().size());
        assertEquals(node2, collector.getNodes().get(0));
    }

}
