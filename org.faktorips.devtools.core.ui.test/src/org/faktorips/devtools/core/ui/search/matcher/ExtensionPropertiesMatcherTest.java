/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
                .thenReturn(new ArrayList<IExtensionPropertyDefinition>());
        when(policyCmptType.getExtensionPropertyDefinitions())
                .thenReturn(new ArrayList<IExtensionPropertyDefinition>());

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
                new ArrayList<IExtensionPropertyDefinition>());

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
                new ArrayList<IExtensionPropertyDefinition>());

        IExtensionPropertyDefinition extensionPropertyDefinition = mock(IExtensionPropertyDefinition.class);
        when(extensionPropertyDefinition.getPropertyId()).thenReturn(PROPERTY_ID);

        when(policyCmptType.getExtensionPropertyDefinitions()).thenReturn(Arrays.asList(extensionPropertyDefinition));

        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher);

        assertTrue(matcher.isMatching(policyCmptType));
        assertFalse(matcher.isMatching(productCmptType));
    }
}
