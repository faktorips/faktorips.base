/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import static org.faktorips.devtools.model.productcmpt.template.ITemplatedValue.MSGCODE_INVALID_TEMPLATE_VALUE_STATUS;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
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

    @Mock
    private IIpsProject ipsProject;

    private TemplateValueSettings handler;

    @Before
    public void setUp() {
        when(attrValue.getIpsProject()).thenReturn(ipsProject);
        when(attrValue.isPartOfTemplateHierarchy()).thenReturn(true);
        when(attrValue.findTemplateProperty(ipsProject)).thenReturn(templateValue);
        handler = new TemplateValueSettings(attrValue);
    }

    @Test
    public void testPropertiesToXml_defined() throws Exception {
        when(attrValue.isPartOfTemplateHierarchy()).thenReturn(true);
        when(attrValue.findTemplateProperty(ipsProject)).thenReturn(null);
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
        when(attrValue.findTemplateProperty(ipsProject)).thenReturn(null);

        handler = new TemplateValueSettings(attrValue);

        assertThat(handler.getStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testTemplateValueSettings_notDefinedInTemplate() {
        IAttributeValue templateValue = mock(IAttributeValue.class);
        when(attrValue.findTemplateProperty(ipsProject)).thenReturn(templateValue);

        handler = new TemplateValueSettings(attrValue);

        assertThat(handler.getStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testInitialize_definedTemplateValue() {
        when(templateValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        handler = new TemplateValueSettings(attrValue);

        assertThat(handler.getStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testValidate_InheritedLinkWithMissingTemplate() {
        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(link.findTemplateProperty(ipsProject)).thenReturn(null);

        TemplateValueSettings settings = new TemplateValueSettings(link);
        assertThat(settings.validate(link, ipsProject), hasMessageCode(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testValidate_InheritedLinkWithTemplate() {
        IProductCmptLink link = mock(IProductCmptLink.class);
        IProductCmptLink templateLink = mock(IProductCmptLink.class);
        when(link.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);
        when(link.findTemplateProperty(ipsProject)).thenReturn(templateLink);

        TemplateValueSettings settings = new TemplateValueSettings(link);
        assertThat(settings.validate(link, ipsProject), lacksMessageCode(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testValidate_UndefinedLinkWithMissingTemplate() {
        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(link.findTemplateProperty(ipsProject)).thenReturn(null);

        TemplateValueSettings settings = new TemplateValueSettings(link);
        assertThat(settings.validate(link, ipsProject), hasMessageCode(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testValidate_UndefinedLinkWithTemplate() {
        IProductCmptLink link = mock(IProductCmptLink.class);
        IProductCmptLink templateLink = mock(IProductCmptLink.class);
        when(link.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);
        when(link.findTemplateProperty(ipsProject)).thenReturn(templateLink);

        TemplateValueSettings settings = new TemplateValueSettings(link);
        assertThat(settings.validate(link, ipsProject), lacksMessageCode(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testValidate_DefinedLinkWithoutTemplate() {
        IProductCmptLink link = mock(IProductCmptLink.class);
        when(link.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(link.findTemplateProperty(ipsProject)).thenReturn(null);

        TemplateValueSettings settings = new TemplateValueSettings(link);
        assertThat(settings.validate(link, ipsProject), lacksMessageCode(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }

    @Test
    public void testValidate_DefinedLinkWithTemplate() {
        IProductCmptLink link = mock(IProductCmptLink.class);
        IProductCmptLink templateLink = mock(IProductCmptLink.class);
        when(link.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(link.findTemplateProperty(ipsProject)).thenReturn(templateLink);

        TemplateValueSettings settings = new TemplateValueSettings(link);
        assertThat(settings.validate(link, ipsProject), lacksMessageCode(MSGCODE_INVALID_TEMPLATE_VALUE_STATUS));
    }
}
