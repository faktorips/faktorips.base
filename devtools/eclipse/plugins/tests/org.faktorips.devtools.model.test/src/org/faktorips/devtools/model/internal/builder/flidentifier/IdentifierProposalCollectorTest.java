/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IdentifierProposalCollectorTest {

    @Mock
    private AbstractIdentifierNodeParser parser;
    @Mock
    private IdentifierProposal proposal1;
    @Mock
    private IdentifierProposal proposal2;

    private IdentifierProposalCollector collector;

    @BeforeEach
    public void setUp() {
        collector = new IdentifierProposalCollector();
    }

    @Test
    public void testAddMatching_notMatching() {

        collector.addMatchingNode("asd", "", "xyz", IdentifierNodeType.INVALID_IDENTIFIER);

        assertTrue(collector.getProposals().isEmpty());
    }

    @Test
    public void testAddMatching_matching() {

        collector.addMatchingNode("xyz1", "", "xyz", IdentifierNodeType.INVALID_IDENTIFIER);

        assertEquals(1, collector.getProposals().size());
        assertEquals("xyz1", collector.getProposals().get(0).getText());
    }

    @Test
    public void testAddMatching_matchingIgnoreCase() {

        collector.addMatchingNode("xyz1", "", "xYz", IdentifierNodeType.INVALID_IDENTIFIER);

        assertEquals(1, collector.getProposals().size());
        assertEquals("xyz1", collector.getProposals().get(0).getText());
    }

}
