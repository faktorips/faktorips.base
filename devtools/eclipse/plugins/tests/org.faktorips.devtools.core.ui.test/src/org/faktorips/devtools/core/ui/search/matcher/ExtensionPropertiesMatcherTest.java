/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ExtensionPropertiesMatcherTest {

    private static final String PROPERTY_ID = "propertyID";

    private WildcardMatcher wildcardMatcher;

    @Before
    public void setUp() {
        wildcardMatcher = new WildcardMatcher("zahlw*");
    }

    @Test
    public void testKeineExtensionProperties() {
        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher);

        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IProductCmptType productCmptType = mock(IProductCmptType.class);

        when(policyCmptType.getExtensionPropertyDefinitions())
                .thenReturn(new ArrayList<>());
        when(policyCmptType.getExtensionPropertyDefinitions())
                .thenReturn(new ArrayList<>());

        assertFalse(matcher.isMatching(policyCmptType));
        assertFalse(matcher.isMatching(productCmptType));
    }

    @Test
    public void testExtensionPropertiesAberKeinTreffer() {
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IProductCmptType productCmptType = mock(IProductCmptType.class);

        when(policyCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(true);
        when(policyCmptType.getExtPropertyValue(PROPERTY_ID)).thenReturn("Toast");
        when(productCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(false);

        when(productCmptType.getExtensionPropertyDefinitions()).thenReturn(
                new ArrayList<>());

        IExtensionPropertyDefinition extensionPropertyDefinition = mock(IExtensionPropertyDefinition.class);
        when(extensionPropertyDefinition.getPropertyId()).thenReturn(PROPERTY_ID);

        when(policyCmptType.getExtensionPropertyDefinitions()).thenReturn(Arrays.asList(extensionPropertyDefinition));

        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher);

        assertFalse(matcher.isMatching(policyCmptType));
        assertFalse(matcher.isMatching(productCmptType));
    }

    @Test
    public void testExtensionPropertiesTreffer() {
        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IProductCmptType productCmptType = mock(IProductCmptType.class);

        when(policyCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(true);
        when(policyCmptType.getExtPropertyValue(PROPERTY_ID)).thenReturn("Zahlweise");
        when(productCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(false);

        when(productCmptType.getExtensionPropertyDefinitions()).thenReturn(
                new ArrayList<>());

        IExtensionPropertyDefinition extensionPropertyDefinition = mock(IExtensionPropertyDefinition.class);
        when(extensionPropertyDefinition.getPropertyId()).thenReturn(PROPERTY_ID);

        when(policyCmptType.getExtensionPropertyDefinitions()).thenReturn(Arrays.asList(extensionPropertyDefinition));

        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher);

        assertTrue(matcher.isMatching(policyCmptType));
        assertFalse(matcher.isMatching(productCmptType));
    }
}
