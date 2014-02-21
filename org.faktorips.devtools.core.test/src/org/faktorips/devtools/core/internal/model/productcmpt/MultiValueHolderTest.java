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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder.Factory;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class MultiValueHolderTest {

    @Test
    public void testValidate_nullValue() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, null);
        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_emptyList() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);
        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_oneValues() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);

        ArrayList<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder singleValueHolder = mock(SingleValueHolder.class);
        values.add(singleValueHolder);
        when(singleValueHolder.validate(project)).thenReturn(new MessageList());
        multiValueHolder = new MultiValueHolder(attributeValue, values);

        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());

        when(singleValueHolder.validate(project)).thenReturn(new MessageList(Message.newError("abc", "123")));

        messageList = multiValueHolder.validate(project);
        assertEquals(2, messageList.size());
        assertNotNull(messageList.getMessageByCode("abc"));
        Message messageByCode = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        assertNotNull(messageByCode);
        assertEquals(multiValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(multiValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());
    }

    @Test
    public void testValidate_multiValues() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);

        ArrayList<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder singleValueHolder1 = mock(SingleValueHolder.class);
        when(singleValueHolder1.validate(project)).thenReturn(new MessageList());

        SingleValueHolder singleValueHolder2 = mock(SingleValueHolder.class);
        when(singleValueHolder2.validate(project)).thenReturn(new MessageList());

        values.add(singleValueHolder1);
        values.add(singleValueHolder2);
        multiValueHolder = new MultiValueHolder(attributeValue, values);

        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());

        when(singleValueHolder1.validate(project)).thenReturn(new MessageList(Message.newError("abc", "123")));

        messageList = multiValueHolder.validate(project);
        assertEquals(2, messageList.size());
        assertNotNull(messageList.getMessageByCode("abc"));
        assertNotNull(messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE));

        when(singleValueHolder2.validate(project)).thenReturn(new MessageList(Message.newError("abc2", "1234")));

        messageList = multiValueHolder.validate(project);
        assertEquals(3, messageList.size());
        assertNotNull(messageList.getMessageByCode("abc"));
        assertNotNull(messageList.getMessageByCode("abc2"));
        Message messageByCode = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        assertNotNull(messageByCode);
        assertEquals(multiValueHolder.getParent(), messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IAttributeValue.PROPERTY_VALUE_HOLDER, messageByCode.getInvalidObjectProperties()[0].getProperty());
        assertEquals(multiValueHolder, messageByCode.getInvalidObjectProperties()[1].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[1].getProperty());
    }

    @Test
    public void testValidateDuplicateValues() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        MultiValueHolder multiValueHolder = spy(new MultiValueHolder(attributeValue));
        doNothing().when(multiValueHolder).objectHasChanged(anyObject(), anyObject());

        List<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "A");
        values.add(valueHolder);
        values.add(new SingleValueHolder(attributeValue, "B"));
        values.add(new SingleValueHolder(attributeValue, "A"));
        values.add(new SingleValueHolder(attributeValue, "C"));
        multiValueHolder.setValue(values);

        MessageList messageList = multiValueHolder.validate(ipsProject).getMessages(Message.ERROR);
        assertEquals(2, messageList.getNoOfMessages(Message.ERROR));
        assertEquals(valueHolder, messageList.getMessage(0).getInvalidObjectProperties()[0].getObject());
        assertEquals(multiValueHolder.getParent(),
                messageList.getMessage(1).getInvalidObjectProperties()[0].getObject());
        assertEquals(multiValueHolder, messageList.getMessage(1).getInvalidObjectProperties()[1].getObject());
    }

    @Test
    public void testSplitMultiDefaultValues_null() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue(null);

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(0, splitMultiDefaultValues.size());
    }

    @Test
    public void testSplitMultiDefaultValues_empty() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("");

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(1, splitMultiDefaultValues.size());
        assertEquals("", splitMultiDefaultValues.get(0).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_singleValue() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("xyz");

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(1, splitMultiDefaultValues.size());
        assertEquals("xyz", splitMultiDefaultValues.get(0).getStringValue());
    }

    @Test
    public void testSplitMultiDefaultValues_multipleValues() throws Exception {
        Factory factory = new MultiValueHolder.Factory();
        IAttributeValue parent = mock(IAttributeValue.class);
        StringValue defaultValue = new StringValue("abc|xyz  | 123");

        ArrayList<SingleValueHolder> splitMultiDefaultValues = factory.splitMultiDefaultValues(parent, defaultValue);

        assertEquals(3, splitMultiDefaultValues.size());
        assertEquals("abc", splitMultiDefaultValues.get(0).getStringValue());
        assertEquals("xyz", splitMultiDefaultValues.get(1).getStringValue());
        assertEquals("123", splitMultiDefaultValues.get(2).getStringValue());
    }

    @Test
    public void testGetSplitMultiValue_empty() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("");

        assertEquals(1, multiValue.length);
        assertEquals("", multiValue[0]);
    }

    @Test
    public void testGetSplitMultiValue_singleValue() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("abc123");

        assertEquals(1, multiValue.length);
        assertEquals("abc123", multiValue[0]);
    }

    @Test
    public void testGetSplitMultiValue_multipleValues() throws Exception {
        String[] multiValue = MultiValueHolder.Factory.getSplitMultiValue("abc123|xyz |  123");

        assertEquals(3, multiValue.length);
        assertEquals("abc123", multiValue[0]);
        assertEquals("xyz", multiValue[1]);
        assertEquals("123", multiValue[2]);
    }

}
