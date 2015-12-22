/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.template;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.template.TemplateValueSettings;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

/**
 * @see AttributeValue for validation tests
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateValueSettingsTest {
    @Mock
    private Element element;

    @Mock
    private IAttributeValue attrValue;
    @Mock
    private IAttributeValue templateValue;

    private TemplateValueSettings handler;

    @Before
    public void setUp() {
        when(attrValue.isConfiguringTemplateValueStatus()).thenReturn(true);
        when(attrValue.findTemplateProperty(any(IIpsProject.class))).thenReturn(templateValue);
        handler = new TemplateValueSettings(attrValue);
    }

    @Test
    public void testPropertiesToXml_defined() throws Exception {
        when(attrValue.isConfiguringTemplateValueStatus()).thenReturn(true);
        when(attrValue.findTemplateProperty(any(IIpsProject.class))).thenReturn(null);
        handler = new TemplateValueSettings(attrValue);
        handler.propertiesToXml(element);

        verify(element).setAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS, "defined");
    }

    @Test
    public void testPropertiesToXml_excluded() throws Exception {
        handler.setStatus(TemplateValueStatus.UNDEFINED);

        handler.propertiesToXml(element);

        verify(element).setAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS, "undefined");
    }

    @Test
    public void testInitPropertiesFromXml_default() throws Exception {
        when(element.hasAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS)).thenReturn(false);

        handler.initPropertiesFromXml(element);

        assertThat(handler.getStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testInitPropertiesFromXml() throws Exception {
        when(element.hasAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS)).thenReturn(true);
        when(element.getAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS)).thenReturn("inherited");

        handler.initPropertiesFromXml(element);

        assertThat(handler.getStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testInitPropertiesFromXml_illegalString() throws Exception {
        when(element.hasAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS)).thenReturn(true);
        when(element.getAttribute(IPropertyValue.PROPERTY_TEMPLATE_VALUE_STATUS)).thenReturn("illegalValue");

        handler.initPropertiesFromXml(element);

        assertThat(handler.getStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testTemplateValueSettings_noTemplateValue() {
        when(attrValue.findTemplateProperty(any(IIpsProject.class))).thenReturn(null);

        handler = new TemplateValueSettings(attrValue);

        assertThat(handler.getStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testInitialize_definedTemplateValue() {
        when(templateValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        handler = new TemplateValueSettings(attrValue);

        assertThat(handler.getStatus(), is(TemplateValueStatus.INHERITED));
    }

}
