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

import com.google.common.base.Function;

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
        Function<IAttributeValue, Object> function = new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue input) {
                return "";
            }
        };
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.INHERITED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property, function);

        assertThat(status, is(TemplateValueUiStatus.INHERITED));
    }

    @Test
    public void testMapStatus_UNDEFINED() throws Exception {
        Function<IAttributeValue, Object> function = new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue input) {
                return "";
            }
        };
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.UNDEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property, function);

        assertThat(status, is(TemplateValueUiStatus.UNDEFINED));
    }

    @Test
    public void testMapStatus_DEFINED__NEWLY_DEFINED() throws Exception {
        Function<IAttributeValue, Object> function = new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue input) {
                return "";
            }
        };
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property, function);

        assertThat(status, is(TemplateValueUiStatus.NEWLY_DEFINED));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__null() throws Exception {
        when(property.findTemplateProperty(null)).thenReturn(templateProperty);
        Function<IAttributeValue, Object> function = new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue input) {
                return null;
            }
        };
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property, function);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE_EQUAL__nonNull() throws Exception {
        when(property.findTemplateProperty(null)).thenReturn(templateProperty);
        Function<IAttributeValue, Object> function = new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue input) {
                return "asdf";
            }
        };
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property, function);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE_EQUAL));
    }

    @Test
    public void testMapStatus_DEFINED__OVERWRITE() throws Exception {
        when(property.findTemplateProperty(null)).thenReturn(templateProperty);
        Function<IAttributeValue, Object> function = new Function<IAttributeValue, Object>() {

            @Override
            public Object apply(IAttributeValue input) {
                return input.toString();
            }
        };
        when(property.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);

        TemplateValueUiStatus status = TemplateValueUiStatus.mapStatus(property, function);

        assertThat(status, is(TemplateValueUiStatus.OVERWRITE));
    }

}
