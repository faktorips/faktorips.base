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

import static org.faktorips.maven.plugin.validation.IpsValidationMojo.logMessages;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.Arrays;

import org.apache.maven.plugin.logging.Log;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class IpsValidationMojoTest {

    private static Log mockLog;

    @BeforeAll
    public static void setUp() {
        mockLog = mock(Log.class);
    }

    @Test
    public void testLogMessages_Error() {
        Message message = new Message.Builder("foo", Severity.ERROR).code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0"))).create();
        MessageList ml = MessageList.of(message);

        logMessages(ml, mockLog);

        Mockito.verify(mockLog).error("foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_Warning() {
        Message message = new Message.Builder("foo", Severity.WARNING).code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0"))).create();
        MessageList ml = MessageList.of(message);

        logMessages(ml, mockLog);

        Mockito.verify(mockLog).warn("foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_Info() {
        Message message = new Message.Builder("foo", Severity.INFO).code("testcode")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop0"))).create();
        MessageList ml = MessageList.of(message);

        logMessages(ml, mockLog);

        Mockito.verify(mockLog).info("foo (testcode)[object.prop0]");
    }

    @Test
    public void testLogMessages_MultipleLogs() {
        MessageList ml = new MessageList();

        Message infoMessage = new Message.Builder("foo", Severity.INFO).code("info").create();
        Message warningMessage = new Message.Builder("bar", Severity.WARNING).code("warning")
                .invalidObjects(Arrays.asList(new ObjectProperty("object", "prop"))).create();
        Message errorMessage = new Message.Builder("baz", Severity.ERROR).code("error")
                .invalidObjectWithProperties("object", "prop1", "prop2").create();

        ml.add(infoMessage);
        ml.add(infoMessage);
        ml.add(infoMessage);

        ml.add(warningMessage);
        ml.add(warningMessage);

        ml.add(errorMessage);

        logMessages(ml, mockLog);

        Mockito.verify(mockLog, times(3)).info("foo (info)[]");
        Mockito.verify(mockLog, times(2)).warn("bar (warning)[object.prop]");
        Mockito.verify(mockLog, times(1)).error("baz (error)[object.prop1, object.prop2]");
    }

}
