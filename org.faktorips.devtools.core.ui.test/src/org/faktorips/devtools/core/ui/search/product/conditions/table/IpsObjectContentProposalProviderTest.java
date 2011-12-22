/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class IpsObjectContentProposalProviderTest {

    @Test
    public void test() {
        List<String> ipsObjects = new ArrayList<String>();

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
        List<String> ipsObjects = new ArrayList<String>();

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
