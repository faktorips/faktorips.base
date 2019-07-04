/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ValueHolderMismatchEntryTest {

    private static final String TEST_VALUE = "abc123";

    @Captor
    private ArgumentCaptor<IValueHolder<?>> valueHolderCaptor;

    @Mock
    private IAttributeValue value;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFix_singleToMulti() throws Exception {
        SingleValueHolder holder = new SingleValueHolder(value, TEST_VALUE);
        doReturn(holder).when(value).getValueHolder();
        when(attribute.isMultiValueAttribute()).thenReturn(true);

        ValueHolderMismatchEntry valueHolderMismatchEntry = new ValueHolderMismatchEntry(value, attribute);
        valueHolderMismatchEntry.fix();

        verify(value).setValueHolder(valueHolderCaptor.capture());
        assertTrue(valueHolderCaptor.getValue() instanceof MultiValueHolder);

        List<SingleValueHolder> list = ((MultiValueHolder)valueHolderCaptor.getValue()).getValue();
        assertEquals(1, list.size());
        assertEquals(TEST_VALUE, list.get(0).getValue().getContentAsString());
    }

    @Test
    public void testFix_singleToMulti_null() throws Exception {
        SingleValueHolder holder = new SingleValueHolder(value, (String)null);
        doReturn(holder).when(value).getValueHolder();
        when(attribute.isMultiValueAttribute()).thenReturn(true);

        ValueHolderMismatchEntry valueHolderMismatchEntry = new ValueHolderMismatchEntry(value, attribute);
        valueHolderMismatchEntry.fix();

        verify(value).setValueHolder(valueHolderCaptor.capture());
        assertTrue(valueHolderCaptor.getValue() instanceof MultiValueHolder);

        List<SingleValueHolder> list = ((MultiValueHolder)valueHolderCaptor.getValue()).getValue();
        assertEquals(1, list.size());
    }

    @Test
    public void testFix_multiToSingle() throws Exception {
        List<SingleValueHolder> list = new ArrayList<SingleValueHolder>();
        list.add(new SingleValueHolder(value, TEST_VALUE));
        MultiValueHolder holder = new MultiValueHolder(value, list);
        doReturn(holder).when(value).getValueHolder();
        when(attribute.isMultiValueAttribute()).thenReturn(false);

        ValueHolderMismatchEntry valueHolderMismatchEntry = new ValueHolderMismatchEntry(value, attribute);
        valueHolderMismatchEntry.fix();

        verify(value).setValueHolder(valueHolderCaptor.capture());
        assertTrue(valueHolderCaptor.getValue() instanceof SingleValueHolder);

        String result = ((SingleValueHolder)valueHolderCaptor.getValue()).getValue().getContentAsString();
        assertEquals(TEST_VALUE, result);
    }

    @Test
    public void testFix_multiToSingle_emptyMultiValueHolder() throws Exception {
        List<SingleValueHolder> list = new ArrayList<SingleValueHolder>();
        MultiValueHolder holder = new MultiValueHolder(value, list);
        doReturn(holder).when(value).getValueHolder();
        when(attribute.isMultiValueAttribute()).thenReturn(false);

        ValueHolderMismatchEntry valueHolderMismatchEntry = new ValueHolderMismatchEntry(value, attribute);
        valueHolderMismatchEntry.fix();

        verify(value).setValueHolder(valueHolderCaptor.capture());
        assertTrue(valueHolderCaptor.getValue() instanceof SingleValueHolder);

        assertNull(((SingleValueHolder)valueHolderCaptor.getValue()).getValue());
    }

    @Test
    public void testFix_noFixSingle() throws Exception {
        SingleValueHolder holder = new SingleValueHolder(value, TEST_VALUE);
        doReturn(holder).when(value).getValueHolder();
        when(attribute.isMultiValueAttribute()).thenReturn(false);

        ValueHolderMismatchEntry valueHolderMismatchEntry = new ValueHolderMismatchEntry(value, attribute);
        valueHolderMismatchEntry.fix();

        verify(value).getValueHolder();
        verifyNoMoreInteractions(value);
    }

    @Test
    public void testFix_noFixMulti() throws Exception {
        List<SingleValueHolder> list = new ArrayList<SingleValueHolder>();
        list.add(new SingleValueHolder(value, TEST_VALUE));
        MultiValueHolder holder = new MultiValueHolder(value, list);
        doReturn(holder).when(value).getValueHolder();
        when(attribute.isMultiValueAttribute()).thenReturn(true);

        ValueHolderMismatchEntry valueHolderMismatchEntry = new ValueHolderMismatchEntry(value, attribute);
        valueHolderMismatchEntry.fix();

        verify(value).getValueHolder();
        verifyNoMoreInteractions(value);
    }

}
