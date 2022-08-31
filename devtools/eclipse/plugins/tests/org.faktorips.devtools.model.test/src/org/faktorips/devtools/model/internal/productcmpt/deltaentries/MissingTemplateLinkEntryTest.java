/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MissingTemplateLinkEntryTest {

    private static final String TARGET = "Target";

    private static final String ASSOCIATION = "Association";

    @Mock
    private IProductCmptLinkContainer linkContainer;

    @Mock
    private IProductCmptLink missingLink;

    @Mock
    private IProductCmptLink newLink;

    @Test
    public void testFix() throws Exception {
        when(missingLink.getTarget()).thenReturn(TARGET);
        when(missingLink.getAssociation()).thenReturn(ASSOCIATION);
        when(linkContainer.newLink(ASSOCIATION)).thenReturn(newLink);
        MissingTemplateLinkEntry missingTemplateLinkEntry = new MissingTemplateLinkEntry(missingLink, linkContainer);

        missingTemplateLinkEntry.fix();

        verify(linkContainer).newLink(ASSOCIATION);
        verify(newLink).setTarget(TARGET);
        verify(newLink).setTemplateValueStatus(TemplateValueStatus.INHERITED);
    }

}
