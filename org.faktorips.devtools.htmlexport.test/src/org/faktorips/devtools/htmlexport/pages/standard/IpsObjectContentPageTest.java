/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.standard;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractPageElement;

public class IpsObjectContentPageTest extends AbstractXmlUnitHtmlExportTest {

    public void testPolicyCmptType() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        String expectedHeadline = policy.getIpsObjectType().getDisplayName() + " " + policy.getName();

        String xPath = "//h1[. = '" + expectedHeadline + "']";

        AbstractPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(),
                config);
        assertXPathExists(objectContentPage, xPath);
    }
}
