/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MultiValueSubsetChooserModelTest {

    private MultiValueHolder multiValueHolder;
    private MultiValueSubsetChooserModel model;
    private SingleValueHolder holderA;
    private SingleValueHolder holderC;
    private SingleValueHolder holderB;
    private SingleValueHolder holder1;
    private SingleValueHolder holder2;

    @Mock
    private IAttributeValue attributeValue;

    @Before
    public void setUp() {
        setUpMultiValueHolder();

        model = new MultiValueSubsetChooserModel(new ArrayList<String>(), multiValueHolder, null, attributeValue);
        List<ListChooserValue> values = model.getResultingValues();
        assertEquals(5, values.size());
        assertEquals("A", values.get(0).getValue());
        assertEquals("B", values.get(1).getValue());
        assertEquals("C", values.get(2).getValue());
        assertEquals("1", values.get(3).getValue());
        assertEquals("2", values.get(4).getValue());
    }

    protected void setUpMultiValueHolder() {
        List<ISingleValueHolder> holderList = new ArrayList<>();
        multiValueHolder = mock(MultiValueHolder.class);
        when(multiValueHolder.getValue()).thenReturn(holderList);
        holderA = mock(SingleValueHolder.class);
        when(holderA.getStringValue()).thenReturn("A");
        doReturn(ValueFactory.createStringValue("A")).when(holderA).getValue();
        holderList.add(holderA);
        holderB = mock(SingleValueHolder.class);
        when(holderB.getStringValue()).thenReturn("B");
        doReturn(ValueFactory.createStringValue("B")).when(holderB).getValue();
        holderList.add(holderB);
        holderC = mock(SingleValueHolder.class);
        when(holderC.getStringValue()).thenReturn("C");
        doReturn(ValueFactory.createStringValue("C")).when(holderC).getValue();
        holderList.add(holderC);
        holder1 = mock(SingleValueHolder.class);
        when(holder1.getStringValue()).thenReturn("1");
        doReturn(ValueFactory.createStringValue("1")).when(holder1).getValue();
        holderList.add(holder1);
        holder2 = mock(SingleValueHolder.class);
        when(holder2.getStringValue()).thenReturn("2");
        doReturn(ValueFactory.createStringValue("2")).when(holder2).getValue();
        holderList.add(holder2);
    }

    @Test
    public void testGetValueIndices() {
        List<ListChooserValue> stringValues = new ArrayList<>();
        stringValues.add(new ListChooserValue("A"));
        stringValues.add(new ListChooserValue("C"));
        stringValues.add(new ListChooserValue("2"));

        int[] indices = model.getValueIndices(stringValues);
        assertEquals(3, indices.length);
        assertEquals(0, indices[0]);
        assertEquals(2, indices[1]);
        assertEquals(4, indices[2]);
    }

    @Test
    public void moveValuesUp() {
        model = spy(model);

        List<ListChooserValue> valuesToMove = new ArrayList<>();
        valuesToMove.add(new ListChooserValue("B"));
        valuesToMove.add(new ListChooserValue("1"));
        model.move(valuesToMove, true);
        verify(model).updateMultiValueHolder();

        List<ListChooserValue> newValues = model.getResultingValues();
        assertEquals(5, newValues.size());
        assertEquals("B", newValues.get(0).getValue());
        assertEquals("A", newValues.get(1).getValue());
        assertEquals("1", newValues.get(2).getValue());
        assertEquals("C", newValues.get(3).getValue());
        assertEquals("2", newValues.get(4).getValue());
    }

    @Test
    public void moveValuesDown() {
        model = spy(model);

        List<ListChooserValue> valuesToMove = new ArrayList<>();
        valuesToMove.add(new ListChooserValue("B"));
        valuesToMove.add(new ListChooserValue("1"));
        model.move(valuesToMove, false);
        verify(model).updateMultiValueHolder();

        List<ListChooserValue> newValues = model.getResultingValues();
        assertEquals(5, newValues.size());
        assertEquals("A", newValues.get(0).getValue());
        assertEquals("C", newValues.get(1).getValue());
        assertEquals("B", newValues.get(2).getValue());
        assertEquals("2", newValues.get(3).getValue());
        assertEquals("1", newValues.get(4).getValue());
    }

    @Test
    public void removeFromResultingValues() {
        model = spy(model);

        List<ListChooserValue> valueList = new ArrayList<>();
        valueList.add(new ListChooserValue("B"));
        model.removeFromResultingValues(valueList);
        verify(model).updateMultiValueHolder();
        List<ListChooserValue> newValues = model.getResultingValues();
        assertEquals(4, newValues.size());
        assertEquals("A", newValues.get(0).getValue());
        assertEquals("C", newValues.get(1).getValue());
        assertEquals("1", newValues.get(2).getValue());
        assertEquals("2", newValues.get(3).getValue());
    }

    @Test
    public void addToResultingValues() {
        model = spy(model);

        List<ListChooserValue> valueList = new ArrayList<>();
        valueList.add(new ListChooserValue("X"));
        model.addToResultingValues(valueList);
        verify(model).updateMultiValueHolder();
        List<ListChooserValue> newValues = model.getResultingValues();
        assertEquals(6, newValues.size());
        assertEquals("A", newValues.get(0).getValue());
        assertEquals("B", newValues.get(1).getValue());
        assertEquals("C", newValues.get(2).getValue());
        assertEquals("1", newValues.get(3).getValue());
        assertEquals("2", newValues.get(4).getValue());
        assertEquals("X", newValues.get(5).getValue());
    }

    // Mockito does not support type-safety when capturing lists
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void updateMultiValueHolder() {
        model = spy(model);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        List<ListChooserValue> valueList = new ArrayList<>();
        valueList.add(new ListChooserValue("X"));
        model.addToResultingValues(valueList);

        verify(multiValueHolder).setValue(captor.capture());

        List<ISingleValueHolder> holderList = captor.getValue();
        assertEquals(6, holderList.size());
        assertEquals("A", holderList.get(0).getStringValue());
        assertEquals("B", holderList.get(1).getStringValue());
        assertEquals("C", holderList.get(2).getStringValue());
        assertEquals("1", holderList.get(3).getStringValue());
        assertEquals("2", holderList.get(4).getStringValue());
        assertEquals("X", holderList.get(5).getStringValue());
    }

    @Test
    public void findSingleValueHolderFor() {
        setUpMultiValueHolder();

        model = new MultiValueSubsetChooserModel(new ArrayList<String>(), multiValueHolder, null, attributeValue);
        ISingleValueHolder holder = model.findSingleValueHolderFor(new ListChooserValue("C"));
        assertSame(multiValueHolder.getValue().get(2), holder);
    }

    @Test
    public void findSingleValueHolderForNull() {
        setUpMultiValueHolder();
        multiValueHolder.getValue().add(new SingleValueHolder(null, (String)null));

        model = new MultiValueSubsetChooserModel(new ArrayList<String>(), multiValueHolder, null, attributeValue);
        ISingleValueHolder holder = model.findSingleValueHolderFor(new ListChooserValue(null));
        assertSame(multiValueHolder.getValue().get(5), holder);
    }

    @Test
    public void findSingleValueHolderForNull2() {
        setUpMultiValueHolder();
        multiValueHolder.getValue().add(new SingleValueHolder(null, "X"));

        model = new MultiValueSubsetChooserModel(new ArrayList<String>(), multiValueHolder, null, attributeValue);
        ISingleValueHolder holder = model.findSingleValueHolderFor(new ListChooserValue(null));
        assertNull(holder);
    }

    @Test
    public void findSingleValueHolderForNull3() {
        setUpMultiValueHolder();
        multiValueHolder.getValue().add(new SingleValueHolder(null, (String)null));

        model = new MultiValueSubsetChooserModel(new ArrayList<String>(), multiValueHolder, null, attributeValue);
        ISingleValueHolder holder = model.findSingleValueHolderFor(new ListChooserValue("X"));
        assertNull(holder);
    }

    @Test
    public void testValidateValue_NoError() throws CoreRuntimeException {
        setUpMultiValueHolder();
        MessageList messageList = mock(MessageList.class);
        doReturn(messageList).when(multiValueHolder).validate(any(IIpsProject.class));

        setUpMultiValueHolder();

        model = spy(new MultiValueSubsetChooserModel(new ArrayList<String>(), multiValueHolder, null, attributeValue));
        doReturn(holderC).when(model).findSingleValueHolderFor(any(ListChooserValue.class));
        doReturn(messageList).when(multiValueHolder).validate(any(IIpsProject.class));

        model.validateValue(null);
        verify(messageList, never()).getMessagesFor(holderA);
        verify(messageList, never()).getMessagesFor(holderB);
        verify(messageList).getMessagesFor(holderC);
        verify(messageList, never()).getMessagesFor(holder1);
        verify(messageList, never()).getMessagesFor(holder2);
    }
}
