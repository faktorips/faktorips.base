/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.junit.Test;

public class IpsObjectContentPageTest extends AbstractXmlUnitHtmlExportTest {

    @Test
    public void testPolicyCmptType() throws Exception {
        PolicyCmptType policy = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragProdukt");

        String expectedHeadline = context.getMessage(policy.getIpsObjectType()) + " " + policy.getName();

        String xPath = "//h1[. = '" + expectedHeadline + "']";

        IPageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), context);
        assertXPathExists(objectContentPage, xPath);
    }
}
