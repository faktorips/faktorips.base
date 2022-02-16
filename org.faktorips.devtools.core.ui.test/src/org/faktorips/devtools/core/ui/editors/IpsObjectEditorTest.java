/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.forms.IMessage;
import org.faktorips.devtools.core.ui.util.UiMessage;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsObjectEditorTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IpsObjectEditor ipsObjectEditor;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsProject ipsProject;

    @Before
    public void setUp() {
        doReturn(ipsObject).when(ipsObjectEditor).getIpsObject();
        doReturn(ipsProject).when(ipsObjectEditor).getIpsProject();
    }

    @Test
    public void testGetHighestSeverity_empty() throws Exception {
        List<IMessage> messages = new ArrayList<>();

        int highestSeverity = ipsObjectEditor.getHighestSeverity(messages);

        assertThat(highestSeverity, is(IMessageProvider.NONE));
    }

    @Test
    public void testGetHighestSeverity_oneMessage() throws Exception {
        List<IMessage> messages = new ArrayList<>();
        messages.add(new UiMessage(new Message("asd", "test", Message.WARNING)));

        int highestSeverity = ipsObjectEditor.getHighestSeverity(messages);

        assertThat(highestSeverity, is(IMessageProvider.WARNING));
    }

    @Test
    public void testGetHighestSeverity_multipleMessages() throws Exception {
        List<IMessage> messages = new ArrayList<>();
        messages.add(new UiMessage(new Message("asd", "test", Message.WARNING)));
        messages.add(new UiMessage(new Message("asd", "test", Message.INFO)));
        messages.add(new UiMessage(new Message("asd", "test", Message.NONE)));
        messages.add(new UiMessage(new Message("asd", "test", Message.INFO)));

        int highestSeverity = ipsObjectEditor.getHighestSeverity(messages);

        assertThat(highestSeverity, is(IMessageProvider.WARNING));
    }

    @Test
    public void testGetMessages_empty() throws Exception {
        mockValidation();

        List<IMessage> messages = ipsObjectEditor.getMessages();

        assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void testGetMessages_withMessages() throws Exception {
        MessageList messageList = mockValidation();
        messageList.add(new Message("asd", "test", Message.ERROR));
        messageList.add(new Message("asd2", "test2", Message.WARNING));

        List<IMessage> messages = ipsObjectEditor.getMessages();

        assertEquals(2, messages.size());
        assertEquals("test", messages.get(0).getMessage());
        assertEquals(IMessageProvider.ERROR, messages.get(0).getMessageType());
        assertEquals("test2", messages.get(1).getMessage());
        assertEquals(IMessageProvider.WARNING, messages.get(1).getMessageType());
    }

    private MessageList mockValidation() {
        MessageList messageList = new MessageList();
        when(ipsObject.validate(any(IIpsProject.class))).thenReturn(messageList);
        return messageList;
    }

    @Test
    public void testCreateHeaderMessage_noMessage() throws Exception {
        List<IMessage> messages = new ArrayList<>();

        String headerMessage = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.ERROR);

        assertThat(headerMessage.isEmpty(), is(true));
    }

    @Test
    public void testCreateHeaderMessage_oneMessage() throws Exception {
        List<IMessage> messages = new ArrayList<>();
        messages.add(new UiMessage(new Message("asd", "test", Message.NONE)));

        String errorMessage = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.ERROR);
        String warningMessage = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.WARNING);
        String informationMessage = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.INFORMATION);

        assertThat(errorMessage, is(Messages.IpsObjectEditor_singleErrorMessage));
        assertThat(warningMessage, is(Messages.IpsObjectEditor_singleWarningMessage));
        assertThat(informationMessage, is(Messages.IpsObjectEditor_singleInformationMessage));
    }

    @Test
    public void testCreateHeaderMessage_multipleMessage() throws Exception {
        List<IMessage> messages = new ArrayList<>();
        messages.add(new UiMessage(new Message("asd", "test", Message.WARNING)));
        messages.add(new UiMessage(new Message("asd", "test", Message.INFO)));
        messages.add(new UiMessage(new Message("asd", "test", Message.NONE)));
        messages.add(new UiMessage(new Message("asd", "test", Message.INFO)));

        String errorMessages = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.ERROR);
        String warningMessages = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.WARNING);
        String informationMessages = ipsObjectEditor.createHeaderMessage(messages, IMessageProvider.INFORMATION);

        assertThat(errorMessages, is(Messages.IpsObjectEditor_multipleErrorMessages));
        assertThat(warningMessages, is(Messages.IpsObjectEditor_multipleWarningMessages));
        assertThat(informationMessages, is(Messages.IpsObjectEditor_multipleInformationMessages));
    }

    @Test
    public void testgetMessageList_SizeBiggerSix() {
        MessageList messageList = createMessageList();
        messageList.add(new Message("warning2", "warning2", Message.WARNING));
        messageList.add(new Message("err7", "err7", Message.ERROR));

        doReturn(messageList).when(ipsObject).validate(ipsProject);

        List<IMessage> editorMessages = ipsObjectEditor.getMessages();
        String expectedText = NLS.bind(Messages.IpsPartEditDialog_moreMessagesInTooltip, 3);
        assertEquals(expectedText, editorMessages.get(editorMessages.size() - 1).getMessage());
        assertEquals(6, editorMessages.size());
    }

    @Test
    public void testgetMessageList_SizeSixOrSmaller() {
        MessageList messageList = createMessageList();

        doReturn(messageList).when(ipsObject).validate(ipsProject);

        List<IMessage> editorMessages = ipsObjectEditor.getMessages();
        assertEquals(editorMessages.size(), messageList.size());
    }

    private MessageList createMessageList() {
        MessageList messageList = new MessageList();
        messageList.add(new Message("err1", "err1", Message.WARNING));
        messageList.add(new Message("err2", "err2", Message.INFO));
        messageList.add(new Message("err3", "err3", Message.NONE));
        messageList.add(new Message("err4", "err4", Message.INFO));
        messageList.add(new Message("err5", "err5", Message.ERROR));
        messageList.add(new Message("err6", "err6", Message.ERROR));
        return messageList;
    }
}
