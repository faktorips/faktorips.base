/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.binding;

import static org.junit.Assert.assertEquals;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.junit.Before;
import org.junit.Test;

public class ValidatablePMOTest {

    Object object1;
    Object object2;
    Object object3;
    ObjectProperty objectProperty1;
    ObjectProperty objectProperty2;
    ObjectProperty objectProperty3;

    MessageList messageList;

    ValidatablePMO validatablePMO = new ValidatablePMO() {

        @Override
        public MessageList validate(IIpsProject ipsProject) throws CoreRuntimeException {
            return null;
        }

        @Override
        public IIpsProject getIpsProject() {
            return null;
        }
    };

    @Before
    public void testSetUp() {
        creatingObjectProperties();
        createMessageList();
        validatablePMO.mapValidationMessagesFor(objectProperty1).to(objectProperty2);
    }

    @Test
    public void testMapValidationMessages_matchingObjectProperties() {
        testSetUp();

        MessageList copy = validatablePMO.createCopyAndMapObjectProperties(messageList);
        MessageList messageListForObjProp2 = copy.getMessagesFor(objectProperty2.getObject());

        assertEquals(3, messageListForObjProp2.getNoOfMessages(Severity.ERROR));
        assertEquals("This is message 1", messageListForObjProp2.getMessage(0).getText());
        assertEquals(Message.ERROR, messageListForObjProp2.getSeverity());
    }

    @Test
    public void testMapValidationMessages_noMatchingObjectProperties() {
        testSetUp();
        validatablePMO.mapValidationMessagesFor(objectProperty3).to(objectProperty1);
        MessageList copy = validatablePMO.createCopyAndMapObjectProperties(messageList);
        MessageList messageListForObjProp3 = copy.getMessagesFor(objectProperty3.getObject());

        assertEquals(0, messageListForObjProp3.getNoOfMessages(Message.ERROR));
    }

    @Test
    public void testClearObjectPropertyMappings() {
        assertEquals(false, validatablePMO.getObjectPropertyMapping().isEmpty());
        validatablePMO.clearObjectPropertyMappings();
        assertEquals(true, validatablePMO.getObjectPropertyMapping().isEmpty());
    }

    private void createMessageList() {
        Message message1 = new Message("message 1", "This is message 1", Message.ERROR, objectProperty1);
        Message message2 = new Message("message 2", "This is message 2", Message.ERROR, objectProperty1);
        Message message3 = new Message("message 3", "This is message 3", Message.ERROR, objectProperty1);
        messageList = new MessageList(message1);
        messageList.add(message2);
        messageList.add(message3);
    }

    private void creatingObjectProperties() {
        object1 = new Object();
        object2 = new Object();
        object3 = new Object();
        objectProperty1 = new ObjectProperty(object1, "Property of Object 1");
        objectProperty2 = new ObjectProperty(object2, "Property of Object 2");
        objectProperty3 = new ObjectProperty(object3, null);
    }
}
