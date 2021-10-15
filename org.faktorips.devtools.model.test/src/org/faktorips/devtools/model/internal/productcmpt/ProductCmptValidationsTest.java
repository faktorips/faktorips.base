/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptValidationsTest {

    @Mock
    private IProductCmpt template;

    @Mock
    private IProductCmpt productCmpt;

    @Test
    public void testValidateValidFrom_SameValidFrom() throws Exception {
        when(template.getValidFrom()).thenReturn(DateUtil.parseIsoDateStringToGregorianCalendar("2010-1-1"));
        when(productCmpt.getValidFrom()).thenReturn(DateUtil.parseIsoDateStringToGregorianCalendar("2010-1-1"));

        MessageList list = new MessageList();
        ProductCmptValidations.validateValidFrom(list, template, productCmpt);

        assertThat(list, isEmpty());
    }

    @Test
    public void testValidateValidFrom_TemplateValidEarlyer() throws Exception {
        when(template.getValidFrom()).thenReturn(DateUtil.parseIsoDateStringToGregorianCalendar("2009-12-31"));
        when(productCmpt.getValidFrom()).thenReturn(DateUtil.parseIsoDateStringToGregorianCalendar("2010-1-1"));

        MessageList list = new MessageList();
        ProductCmptValidations.validateValidFrom(list, template, productCmpt);

        assertThat(list, isEmpty());
    }

    @Test
    public void testValidateValidFrom_TemplateValidLater() throws Exception {
        when(template.getValidFrom()).thenReturn(DateUtil.parseIsoDateStringToGregorianCalendar("2010-1-2"));
        when(productCmpt.getValidFrom()).thenReturn(DateUtil.parseIsoDateStringToGregorianCalendar("2010-1-1"));

        MessageList list = new MessageList();
        ProductCmptValidations.validateValidFrom(list, template, productCmpt);

        assertThat(list, hasMessageCode(IProductCmpt.MSGCODE_INCONSISTENT_TEMPLATE_VALID_FROM));
    }

}
