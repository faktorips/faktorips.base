/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.junit.Test;

public class IpsStatusTest {

    @Test
    public void testOfMessageList_Empty() throws Exception {
        assertThat(IpsStatus.of(MessageList.of()), is(Status.OK_STATUS));
    }

    @Test
    public void testOfMessageList_Null() throws Exception {
        assertThat(IpsStatus.of((MessageList)null), is(Status.OK_STATUS));
    }

    @Test
    public void testOfMessageList_Single() throws Exception {
        Message message = Message.newWarning("CODE", "Text");

        IStatus status = IpsStatus.of(MessageList.of(message));

        assertThat(status, is(instanceOf(IpsStatus.class)));
        assertThat(status.getSeverity(), is(IStatus.WARNING));
        assertThat(status.getMessage(), is("Text"));
    }

    @Test
    public void testOfMessageList() throws Exception {
        Message infoMessage = Message.newInfo("CODE1", "Info");
        Message warnMessage = Message.newWarning("CODE2", "Warn");
        Message errorMessage = Message.newError("CODE3", "Error");
        Message otherMessage = new Message("Other", Severity.NONE);

        IStatus status = IpsStatus.of(MessageList.of(infoMessage, warnMessage, errorMessage, otherMessage));

        assertThat(status.isMultiStatus(), is(true));
        assertThat(status.getPlugin(), is(IpsModelActivator.PLUGIN_ID));
        assertThat(status.getSeverity(), is(IStatus.ERROR));
        assertThat(status.getMessage(), is("Info\nWarn\nError\nOther"));
        IStatus[] children = status.getChildren();
        assertThat(children.length, is(4));
        assertThat(children[0], is(instanceOf(IpsStatus.class)));
        assertThat(children[0].getSeverity(), is(IStatus.INFO));
        assertThat(children[0].getMessage(), is("Info"));
        assertThat(children[1], is(instanceOf(IpsStatus.class)));
        assertThat(children[1].getSeverity(), is(IStatus.WARNING));
        assertThat(children[1].getMessage(), is("Warn"));
        assertThat(children[2], is(instanceOf(IpsStatus.class)));
        assertThat(children[2].getSeverity(), is(IStatus.ERROR));
        assertThat(children[2].getMessage(), is("Error"));
        assertThat(children[3], is(instanceOf(IpsStatus.class)));
        assertThat(children[3].getSeverity(), is(IStatus.OK));
        assertThat(children[3].getMessage(), is("Other"));

    }

    @Test
    public void testOfMessage_Null() throws Exception {
        assertThat(IpsStatus.of((Message)null), is(Status.OK_STATUS));
    }

    @Test
    public void testOfMessage() throws Exception {
        Message message = Message.newInfo("CODE", "text");

        IStatus status = IpsStatus.of(message);

        assertThat(status, is(instanceOf(IpsStatus.class)));
        assertThat(status.getSeverity(), is(IStatus.INFO));
        assertThat(status.getMessage(), is("text"));
    }

}
