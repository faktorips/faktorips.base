/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestMockingUtils;
import org.faktorips.devtools.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition.RetentionPolicy;
import org.faktorips.devtools.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAttribute;
import org.junit.Test;

public class ExtensionPropertyDefinitionExtensionsTest {

    @Test
    public void testInitExtensionProperty() throws Exception {
        StringExtensionPropertyDefinition definition = new StringExtensionPropertyDefinition();
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("extendedType", ProductCmptTypeAttribute.class.getCanonicalName());
        attributes.put("defaultValue", "test123");
        attributes.put("position", "top");
        attributes.put("order", "1234");
        attributes.put("retention", "DEFINITION");
        TestConfigurationElement element = new TestConfigurationElement("property", attributes, null,
                new IConfigurationElement[] {}, Collections.<String, Object> singletonMap("class", definition));
        element.setExtension(TestMockingUtils.mockExtension("TestExtProperty", element));

        ExtensionPropertyDefinition extensionProperty = new StringExtensionPropertyDefinition();
        ExtensionPropertyDefinitionExtensions.initExtensionProperty(extensionProperty, element);

        assertEquals(extensionProperty.getExtendedType(), ProductCmptTypeAttribute.class);
        assertEquals(extensionProperty.getDefaultValue(null), "test123");
        assertEquals(extensionProperty.getPosition(), "top");
        assertEquals(extensionProperty.getOrder(), 1234);
        assertEquals(extensionProperty.getRetention(), RetentionPolicy.DEFINITION);
    }
}
