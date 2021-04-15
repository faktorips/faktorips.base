/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class IpsObjectContentProposalProviderTest {

    @Test
    public void test() {
        List<String> ipsObjects = new ArrayList<>();

        ipsObjects.add("Vollkasko");
        ipsObjects.add("Teilkasko");
        ipsObjects.add("VK");
        ipsObjects.add("TK");
        ipsObjects.add("VollKasko");

        IpsObjectContentProposalProvider provider = new IpsObjectContentProposalProvider(ipsObjects);

        assertEquals(5, provider.getProposals("", 0).length);
        assertEquals(5, provider.getProposals("A", 0).length);

        assertEquals(0, provider.getProposals("A", 1).length);

        assertEquals(2, provider.getProposals("T", 1).length);
        assertEquals(2, provider.getProposals("Te", 1).length);
        assertEquals(1, provider.getProposals("Te", 2).length);

        assertEquals(2, provider.getProposals("VollK", 4).length);

        // case insensivity
        assertEquals(2, provider.getProposals("VollK", 5).length);
    }

    @Test
    public void testQualified() {
        List<String> ipsObjects = new ArrayList<>();

        ipsObjects.add("xyz.Vollkasko");
        ipsObjects.add("zyx.Teilkasko");
        ipsObjects.add("xyz.VK");
        ipsObjects.add("xyz.TK");
        ipsObjects.add("VollKasko");

        IpsObjectContentProposalProvider provider = new IpsObjectContentProposalProvider(ipsObjects);

        assertEquals(5, provider.getProposals("", 0).length);
        assertEquals(5, provider.getProposals("A", 0).length);

        assertEquals(0, provider.getProposals("A", 1).length);

        assertEquals(2, provider.getProposals("T", 1).length);

        assertEquals(2, provider.getProposals("VollK", 4).length);

        assertEquals(3, provider.getProposals("xyz.", 4).length);
        assertEquals(2, provider.getProposals("xyz.V", 5).length);
    }

}
