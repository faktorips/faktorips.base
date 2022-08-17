/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.template;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateValueStatusTest {

    @Mock
    private IAttributeValue attributeValue;

    @Mock
    private IProductCmptLink link;

    @Test
    public void testGetNextStatus_INHERITED() throws Exception {
        makeProductCmpt(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.INHERITED), is(TemplateValueStatus.DEFINED));
        assertThat(nextNextStatus(TemplateValueStatus.INHERITED), is(TemplateValueStatus.DEFINED));

        addTemplateValue(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.INHERITED), is(TemplateValueStatus.DEFINED));
        assertThat(nextNextStatus(TemplateValueStatus.INHERITED), is(TemplateValueStatus.INHERITED));

        makeTemplate(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.INHERITED), is(TemplateValueStatus.DEFINED));
        assertThat(nextNextStatus(TemplateValueStatus.INHERITED), is(TemplateValueStatus.UNDEFINED));
    }

    @Test
    public void testGetNextStatus_DEFINED() throws Exception {
        makeProductCmpt(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.DEFINED), is(TemplateValueStatus.DEFINED));
        assertThat(nextNextStatus(TemplateValueStatus.DEFINED), is(TemplateValueStatus.DEFINED));

        addTemplateValue(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.DEFINED), is(TemplateValueStatus.INHERITED));
        assertThat(nextNextStatus(TemplateValueStatus.DEFINED), is(TemplateValueStatus.DEFINED));

        makeTemplate(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.DEFINED), is(TemplateValueStatus.UNDEFINED));
        assertThat(nextNextStatus(TemplateValueStatus.DEFINED), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testGetNextStatus_UNDEFINED() throws Exception {
        makeProductCmpt(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.UNDEFINED), is(TemplateValueStatus.DEFINED));
        assertThat(nextNextStatus(TemplateValueStatus.UNDEFINED), is(TemplateValueStatus.DEFINED));

        addTemplateValue(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.UNDEFINED), is(TemplateValueStatus.INHERITED));
        assertThat(nextNextStatus(TemplateValueStatus.UNDEFINED), is(TemplateValueStatus.DEFINED));

        makeTemplate(attributeValue);
        assertThat(nextStatus(TemplateValueStatus.UNDEFINED), is(TemplateValueStatus.INHERITED));
        assertThat(nextNextStatus(TemplateValueStatus.UNDEFINED), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testIsAllowedStatus_UNDEFINED() throws Exception {
        makeProductCmpt(attributeValue);
        assertThat(TemplateValueStatus.UNDEFINED.isAllowedStatus(attributeValue), is(false));

        makeTemplate(attributeValue);
        assertThat(TemplateValueStatus.UNDEFINED.isAllowedStatus(attributeValue), is(true));
    }

    @Test
    public void testIsAllowedStatus_Link_UNDEFINED() throws Exception {
        makeProductCmpt(link);
        assertThat(TemplateValueStatus.UNDEFINED.isAllowedStatus(link), is(false));

        ITemplatedValue templateLink = mock(IProductCmptLink.class);
        makeTemplate(templateLink);
        doReturn(templateLink).when(link).findTemplateProperty(any(IIpsProject.class));

        assertThat(TemplateValueStatus.UNDEFINED.isAllowedStatus(link), is(true));
        assertThat(TemplateValueStatus.UNDEFINED.isAllowedStatus(templateLink), is(false));
    }

    @Test
    public void testIsAllowedStatus_INHERITED() throws Exception {
        assertThat(TemplateValueStatus.INHERITED.isAllowedStatus(attributeValue), is(false));

        addTemplateValue(attributeValue);
        assertThat(TemplateValueStatus.INHERITED.isAllowedStatus(attributeValue), is(true));
    }

    @Test
    public void testIsAllowedStatus_DEFINED() throws Exception {
        assertThat(TemplateValueStatus.DEFINED.isAllowedStatus(attributeValue), is(true));
    }

    private void addTemplateValue(IAttributeValue value) {
        IAttributeValue templateValue = mock(IAttributeValue.class);
        when(value.findTemplateProperty(any(IIpsProject.class))).thenReturn(templateValue);
    }

    private void makeTemplate(ITemplatedValue value) {
        IPropertyValueContainer container = mock(IPropertyValueContainer.class);
        when(container.isProductTemplate()).thenReturn(true);
        when(value.getTemplatedValueContainer()).thenReturn(container);
    }

    private void makeProductCmpt(ITemplatedValue value) {
        IPropertyValueContainer container = mock(IPropertyValueContainer.class);
        when(container.isProductTemplate()).thenReturn(false);
        when(value.getTemplatedValueContainer()).thenReturn(container);
    }

    private TemplateValueStatus nextStatus(TemplateValueStatus start) {
        return start.getNextStatus(attributeValue);
    }

    private TemplateValueStatus nextNextStatus(TemplateValueStatus start) {
        return start.getNextStatus(attributeValue).getNextStatus(attributeValue);
    }

}
