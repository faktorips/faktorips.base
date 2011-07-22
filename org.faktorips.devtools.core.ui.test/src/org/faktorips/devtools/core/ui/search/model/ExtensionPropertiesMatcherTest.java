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

package org.faktorips.devtools.core.ui.search.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.IIpsModel;
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
        IIpsModel ipsModel = mock(IIpsModel.class);

        when(ipsModel.getExtensionPropertyDefinitions(any(Class.class), anyBoolean())).thenReturn(
                new IExtensionPropertyDefinition[0]);

        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher, ipsModel);

        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IProductCmptType productCmptType = mock(IProductCmptType.class);

        assertFalse(matcher.isMatchingElement(policyCmptType));
        assertFalse(matcher.isMatchingElement(productCmptType));
    }

    @Test
    public void testExtensionPropertiesAberKeinTreffer() {
        IIpsModel ipsModel = mock(IIpsModel.class);

        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IProductCmptType productCmptType = mock(IProductCmptType.class);

        when(policyCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(true);
        when(policyCmptType.getExtPropertyValue(PROPERTY_ID)).thenReturn("Toast");
        when(productCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(false);

        when(ipsModel.getExtensionPropertyDefinitions(productCmptType.getClass(), true)).thenReturn(
                new IExtensionPropertyDefinition[0]);

        IExtensionPropertyDefinition extensionPropertyDefinition = mock(IExtensionPropertyDefinition.class);
        when(extensionPropertyDefinition.getPropertyId()).thenReturn(PROPERTY_ID);

        when(ipsModel.getExtensionPropertyDefinitions(policyCmptType.getClass(), true)).thenReturn(
                new IExtensionPropertyDefinition[] { extensionPropertyDefinition });

        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher, ipsModel);

        assertFalse(matcher.isMatchingElement(policyCmptType));
        assertFalse(matcher.isMatchingElement(productCmptType));
    }

    @Test
    public void testExtensionPropertiesTreffer() {
        IIpsModel ipsModel = mock(IIpsModel.class);

        IPolicyCmptType policyCmptType = mock(IPolicyCmptType.class);
        IProductCmptType productCmptType = mock(IProductCmptType.class);

        when(policyCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(true);
        when(policyCmptType.getExtPropertyValue(PROPERTY_ID)).thenReturn("Zahlweise");
        when(productCmptType.isExtPropertyDefinitionAvailable(PROPERTY_ID)).thenReturn(false);

        when(ipsModel.getExtensionPropertyDefinitions(productCmptType.getClass(), true)).thenReturn(
                new IExtensionPropertyDefinition[0]);

        IExtensionPropertyDefinition extensionPropertyDefinition = mock(IExtensionPropertyDefinition.class);
        when(extensionPropertyDefinition.getPropertyId()).thenReturn(PROPERTY_ID);

        when(ipsModel.getExtensionPropertyDefinitions(policyCmptType.getClass(), true)).thenReturn(
                new IExtensionPropertyDefinition[] { extensionPropertyDefinition });

        ExtensionPropertyMatcher matcher = new ExtensionPropertyMatcher(wildcardMatcher, ipsModel);

        assertTrue(matcher.isMatchingElement(policyCmptType));
        assertFalse(matcher.isMatchingElement(productCmptType));
    }
}
