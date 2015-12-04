/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateValueStatusTest {

    @Mock
    private IAttributeValue attributeValue;

    @Test
    public void testGetNextStatus__INHERITED_DEFINED() throws Exception {
        assertNextStatus(TemplateValueStatus.INHERITED, TemplateValueStatus.DEFINED);
        assertNextStatusAllowAll(TemplateValueStatus.INHERITED, TemplateValueStatus.DEFINED);
    }

    @Test
    public void testGetNextStatus__INHERITED_UNDEFINED() throws Exception {
        assertNextStatus(TemplateValueStatus.INHERITED, TemplateValueStatus.UNDEFINED);
    }

    @Test
    public void testGetNextStatus__DEFINED_UNDEFINED() throws Exception {
        assertNextStatus(TemplateValueStatus.DEFINED, TemplateValueStatus.UNDEFINED);
        assertNextStatusAllowAll(TemplateValueStatus.DEFINED, TemplateValueStatus.UNDEFINED);
    }

    @Test
    public void testGetNextStatus__DEFINED_INHERITED() throws Exception {
        assertNextStatus(TemplateValueStatus.DEFINED, TemplateValueStatus.INHERITED);
    }

    @Test
    public void testGetNextStatus__UNDEFINED_INHERITED() throws Exception {
        assertNextStatus(TemplateValueStatus.UNDEFINED, TemplateValueStatus.INHERITED);
        assertNextStatusAllowAll(TemplateValueStatus.UNDEFINED, TemplateValueStatus.INHERITED);
    }

    @Test
    public void testGetNextStatus__UNDEFINED_DEFINED() throws Exception {
        assertNextStatus(TemplateValueStatus.UNDEFINED, TemplateValueStatus.DEFINED);
    }

    private void assertNextStatus(TemplateValueStatus initialStatus, TemplateValueStatus nextStatus) {
        testStartExpectedAllowed(initialStatus, nextStatus, false);
    }

    private void assertNextStatusAllowAll(TemplateValueStatus initialStatus, TemplateValueStatus nextStatus) {
        testStartExpectedAllowed(initialStatus, nextStatus, true);
    }

    private void testStartExpectedAllowed(TemplateValueStatus start, TemplateValueStatus expected, boolean allAllowed) {
        EnumSet<TemplateValueStatus> allowed;
        if (allAllowed) {
            allowed = EnumSet.allOf(TemplateValueStatus.class);
        } else {
            allowed = EnumSet.of(start, expected);
        }
        for (TemplateValueStatus templateValueStatus : allowed) {
            when(attributeValue.isAllowedTemplateValueStatus(templateValueStatus)).thenReturn(true);
        }
        TemplateValueStatus nextStatus = start.getNextStatus(attributeValue);
        assertThat(nextStatus, is(expected));
    }

}
