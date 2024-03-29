/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation;

import static org.faktorips.maven.plugin.validation.IpsValidationMessageMapper.MOJO_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IpsValidationMessageMapperTest {

    private Log mockLog;
    private MavenProject mockProject;

    @BeforeEach
    public void setUp() {
        mockLog = mock(Log.class);
        mockProject = mock(MavenProject.class);
    }

    @Test
    public void testLogMessages_Error() {
        Message message = new Message.Builder("foo", Severity.ERROR)
                .code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0")))
                .create();
        MessageList ml = MessageList.of(message);

        new IpsValidationMessageMapper(mockLog, mockProject).logMessages(ml);

        verify(mockLog).error(MOJO_NAME + " foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_Warning() {
        Message message = new Message.Builder("foo", Severity.WARNING)
                .code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0")))
                .create();
        MessageList ml = MessageList.of(message);

        new IpsValidationMessageMapper(mockLog, mockProject).logMessages(ml);

        verify(mockLog).warn(MOJO_NAME + " foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_Info() {
        Message message = new Message.Builder("foo", Severity.INFO)
                .code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0")))
                .create();
        MessageList ml = MessageList.of(message);

        new IpsValidationMessageMapper(mockLog, mockProject).logMessages(ml);

        verify(mockLog).info(MOJO_NAME + " foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_NoObjectProperties() {
        MessageList ml = MessageList.of(Message.newInfo("info", "foo"));

        new IpsValidationMessageMapper(mockLog, mockProject).logMessages(ml);

        verify(mockLog).info(MOJO_NAME + " foo (info)[]");
    }

    @Test
    public void testLogMessages_MultipleObjectProperties() {
        MessageList ml = MessageList.of(Message.newError("error", "baz", "object", "prop1", "prop2"));

        new IpsValidationMessageMapper(mockLog, mockProject).logMessages(ml);

        verify(mockLog).error(MOJO_NAME + " baz (error)[object.prop1, object.prop2]");
    }

    @Test
    public void testLogMessages_MultipleLogs() {
        MessageList ml = new MessageList();

        Message infoMessage = new Message.Builder("foo", Severity.INFO)
                .code("info")
                .create();
        Message warningMessage = new Message.Builder("bar", Severity.WARNING)
                .code("warning")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop")))
                .create();
        Message errorMessage = new Message.Builder("baz", Severity.ERROR)
                .code("error")
                .invalidObjectWithProperties("object", "prop1", "prop2")
                .create();

        ml.add(infoMessage);
        ml.add(infoMessage);
        ml.add(infoMessage);

        ml.add(warningMessage);
        ml.add(warningMessage);

        ml.add(errorMessage);

        new IpsValidationMessageMapper(mockLog, mockProject).logMessages(ml);

        verify(mockLog, times(3)).info(MOJO_NAME + " foo (info)[]");
        verify(mockLog, times(2)).warn(MOJO_NAME + " bar (warning)[object.prop]");
        verify(mockLog, times(1)).error(MOJO_NAME + " baz (error)[object.prop1, object.prop2]");
    }

}
