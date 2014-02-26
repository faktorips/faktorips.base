/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition2;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExtensionPropertyHandlerTest {

    private static final String ANY_ID = "anyId";

    private static final String ANY_ID2 = "anyId2";

    private static final String MY_DEFAULT_VALUE = "myDefaultValue";

    private static final String MY_DEFAULT_VALUE2 = "myDefaultValue2";

    private static final String MY_VALUE = "myValue";

    @Mock
    private IExtensionPropertyDefinition extPropDef;

    @Mock
    private IExtensionPropertyDefinition2 extPropDef2;

    @Mock
    private IpsObjectPartContainer ipsObjectPartContainer;

    @InjectMocks
    private ExtensionPropertyHandler extensionPropertyHandler;

    @Before
    public void setUpExtPropDefAndPart() {
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(ANY_ID);
        doReturn(extPropDef).when(ipsObjectPartContainer).getExtensionPropertyDefinition(ANY_ID2);
        doReturn(Arrays.asList(extPropDef, extPropDef2)).when(ipsObjectPartContainer).getExtensionPropertyDefinitions();

        when(extPropDef.getPropertyId()).thenReturn(ANY_ID);
        when(extPropDef.getDefaultValue()).thenReturn(MY_DEFAULT_VALUE);
        when(extPropDef.beforeSetValue(any(IIpsObjectPartContainer.class), any())).thenReturn(true);

        when(extPropDef2.getPropertyId()).thenReturn(ANY_ID2);
        when(extPropDef2.getDefaultValue()).thenReturn(MY_DEFAULT_VALUE);
        when(extPropDef2.getDefaultValue(ipsObjectPartContainer)).thenReturn(MY_DEFAULT_VALUE2);
        when(extPropDef2.beforeSetValue(any(IIpsObjectPartContainer.class), any())).thenReturn(true);
    }

    @Test
    public void testGetExtPropertyValue_defaultValue() throws Exception {

        Object propertyValue = extensionPropertyHandler.getExtPropertyValue(ANY_ID);

        assertEquals(MY_DEFAULT_VALUE, propertyValue);
    }

    @Test
    public void testGetExtPropertyValue_defaultValueDependingOnPart() throws Exception {

        Object propertyValue = extensionPropertyHandler.getExtPropertyValue(ANY_ID2);

        assertEquals(MY_DEFAULT_VALUE2, propertyValue);
    }

    @Test
    public void testSetExtPropertyValue() throws Exception {

        extensionPropertyHandler.setExtPropertyValue(ANY_ID, MY_VALUE);

        assertEquals(MY_VALUE, extensionPropertyHandler.getExtPropertyValue(ANY_ID));
    }

    @Test
    public void testCheckExtProperty() throws Exception {
        extensionPropertyHandler.checkExtProperty(ANY_ID);

        verify(ipsObjectPartContainer).getExtensionPropertyDefinition(ANY_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckExtProperty_fail() throws Exception {
        extensionPropertyHandler.checkExtProperty("invalidId");
    }

    @Test
    public void testInitMissingExtProperties() throws Exception {
        extensionPropertyHandler.initMissingExtProperties();

        assertEquals(MY_DEFAULT_VALUE, extensionPropertyHandler.getExtPropertyValues().get(ANY_ID));
        assertEquals(MY_DEFAULT_VALUE2, extensionPropertyHandler.getExtPropertyValues().get(ANY_ID2));
    }

    @Test
    public void testIsExtPropertyDefinitionAvailable() throws Exception {
        assertTrue(extensionPropertyHandler.isExtPropertyDefinitionAvailable(ANY_ID));
        assertFalse(extensionPropertyHandler.isExtPropertyDefinitionAvailable("invalidId"));
    }

}
