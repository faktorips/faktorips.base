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
import static org.faktorips.maven.plugin.validation.IpsValidationMessageMapper.logMessages;
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

        logMessages(ml, mockLog, mockProject);

        verify(mockLog).error(MOJO_NAME + " foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_Warning() {
        Message message = new Message.Builder("foo", Severity.WARNING)
                .code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0")))
                .create();
        MessageList ml = MessageList.of(message);

        logMessages(ml, mockLog, mockProject);

        verify(mockLog).warn(MOJO_NAME + " foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_Info() {
        Message message = new Message.Builder("foo", Severity.INFO)
                .code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0")))
                .create();
        MessageList ml = MessageList.of(message);

        logMessages(ml, mockLog, mockProject);

        verify(mockLog).info(MOJO_NAME + " foo (testcode)[object.prop0]");
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

        logMessages(ml, mockLog, mockProject);

        verify(mockLog, times(3)).info(MOJO_NAME + " foo (info)[]");
        verify(mockLog, times(2)).warn(MOJO_NAME + " bar (warning)[object.prop]");
        verify(mockLog, times(1)).error(MOJO_NAME + " baz (error)[object.prop1, object.prop2]");
    }

}
