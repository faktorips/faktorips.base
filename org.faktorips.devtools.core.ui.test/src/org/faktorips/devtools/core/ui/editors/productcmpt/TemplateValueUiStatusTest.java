/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateValueUiStatusTest {

    @Mock
    private IAttributeValue property;

    @Mock
    private IAttributeValue templateProperty;

    @Test
    public void testMapStatus_INHERITED() throws Exception {
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.INHERITED));
    }

    @Test
    public void testMapStatus_UNDEFINED() throws Exception {
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.UNDEFINED));
    }

    @Test
    public void testMapStatus_DEFINED__NEWLY_DEFINED() throws Exception {
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.NEWLY_DEFINED));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__null() throws Exception {
        when(property.findTemplateProperty(null)).thenReturn(templateProperty);
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__nonNull() throws Exception {
        when(property.findTemplateProperty(null)).thenReturn(templateProperty);
        when(property.getPropertyValue()).thenReturn("asdf");
        when(templateProperty.getPropertyValue()).thenReturn("asdf");
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE() throws Exception {
        when(property.findTemplateProperty(null)).thenReturn(templateProperty);
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(property.getPropertyValue()).thenReturn("abc");
        when(property.getPropertyValue()).thenReturn("abcxyz");

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE));
    }
}
