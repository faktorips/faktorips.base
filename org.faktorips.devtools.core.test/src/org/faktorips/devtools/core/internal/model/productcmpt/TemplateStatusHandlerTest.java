/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue.TemplateStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

/**
 * @see AttributeValue for validation tests
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateStatusHandlerTest {
    @Mock
    private Element element;

    private TemplateStatusHandler handler = new TemplateStatusHandler();

    @Test
    public void testPropertiesToXml_default() throws Exception {
        handler.propertiesToXml(element);

        verify(element).setAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS, "defined");
    }

    @Test
    public void testPropertiesToXml_excluded() throws Exception {
        handler.setTemplateStatus(TemplateStatus.UNDEFINED);

        handler.propertiesToXml(element);

        verify(element).setAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS, "excluded");
    }

    @Test
    public void testInitPropertiesFromXml_default() throws Exception {
        when(element.hasAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS)).thenReturn(false);

        handler.initPropertiesFromXml(element);

        assertThat(handler.getTemplateStatus(), is(TemplateStatus.DEFINED));
    }

    @Test
    public void testInitPropertiesFromXml() throws Exception {
        when(element.hasAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS)).thenReturn(true);
        when(element.getAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS)).thenReturn("inherited");

        handler.initPropertiesFromXml(element);

        assertThat(handler.getTemplateStatus(), is(TemplateStatus.INHERITED));
    }

    @Test
    public void testInitPropertiesFromXml_illegalString() throws Exception {
        when(element.hasAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS)).thenReturn(true);
        when(element.getAttribute(IAttributeValue.PROPERTY_TEMPLATE_STATUS)).thenReturn("illegalValue");

        handler.initPropertiesFromXml(element);

        assertThat(handler.getTemplateStatus(), is(TemplateStatus.DEFINED));
    }
}
