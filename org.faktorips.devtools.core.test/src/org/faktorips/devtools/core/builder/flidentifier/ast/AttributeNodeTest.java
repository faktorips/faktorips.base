/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.ast;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.AttributeParser;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.util.TextRegion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeNodeTest {

    private static final String LABEL = "myLabel";

    private static final String DESCRIPTION = "myDescription";

    private static final String ATTRIBUTE_NAME = "myAttributeName";

    @Mock
    private IAttribute attribute;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private TextRegion textRegion;

    @Mock
    private MultiLanguageSupport multiLanguageSupport;

    @Test
    public void testGetText_normalAccess() throws Exception {
        AttributeNode attributeNode = new AttributeNode(attribute, false, false, ipsProject, textRegion);
        when(attribute.getName()).thenReturn(ATTRIBUTE_NAME);

        String text = attributeNode.getText();

        assertEquals(ATTRIBUTE_NAME, text);
    }

    @Test
    public void testGetText_defaultAccess() throws Exception {
        AttributeNode attributeNode = new AttributeNode(attribute, true, false, ipsProject, textRegion);
        when(attribute.getName()).thenReturn(ATTRIBUTE_NAME);

        String text = attributeNode.getText();

        assertEquals(ATTRIBUTE_NAME + AttributeParser.DEFAULT_VALUE_SUFFIX, text);
    }

    @Test
    public void testGetDescription() throws Exception {
        AttributeNode attributeNode = new AttributeNode(attribute, false, false, ipsProject, textRegion);
        when(multiLanguageSupport.getLocalizedLabel(attribute)).thenReturn(LABEL);
        when(multiLanguageSupport.getLocalizedDescription(attribute)).thenReturn(DESCRIPTION);

        String description = attributeNode.getDescription(multiLanguageSupport);

        assertEquals(LABEL + " - " + DESCRIPTION, description);
    }

    @Test
    public void testGetDescription_default() throws Exception {
        AttributeNode attributeNode = new AttributeNode(attribute, true, false, ipsProject, textRegion);
        when(multiLanguageSupport.getLocalizedLabel(attribute)).thenReturn(LABEL);
        when(multiLanguageSupport.getLocalizedDescription(attribute)).thenReturn(DESCRIPTION);

        String description = attributeNode.getDescription(multiLanguageSupport);

        assertEquals(NLS.bind(Messages.AttributeNode_defaultOfName, LABEL) + " - " + DESCRIPTION, description);
    }

}
