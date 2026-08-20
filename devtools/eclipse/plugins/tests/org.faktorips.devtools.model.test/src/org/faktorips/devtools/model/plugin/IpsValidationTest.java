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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class IpsValidationTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IpsValidationTask validationTask1;

    @Mock
    private IpsValidationTask validationTask2;

    private MockitoSession mockito;

    private IpsValidation ipsValidation;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        ipsValidation = new IpsValidation();
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mockito.finishMocking();
    }

    @Test
    public void shouldExecuteEveryAddedTask() {
        addTask(validationTask1, true);
        addTask(validationTask2, true);

        ipsValidation.validate(ipsProject);

        InOrder inOrder = inOrder(validationTask1, validationTask2);
        inOrder.verify(validationTask1).execute(ipsProject);
        inOrder.verify(validationTask2).execute(ipsProject);
    }

    @Test
    public void shouldAddResultOfEveryAddedTaskToResultMessageList() {
        addTask(validationTask1, true);
        addTask(validationTask2, true);

        associateErrorMessage(validationTask1, "msg1");
        associateErrorMessage(validationTask2, "msg2");

        MessageList result = ipsValidation.validate(ipsProject);

        assertEquals(2, result.size());
        assertNotNull(result.getMessageByCode("msg1"));
        assertNotNull(result.getMessageByCode("msg2"));
    }

    @Test
    public void shouldStopIfATaskReturnsAnErrorWhileContinueOnErrorIsFalse() {
        addTask(validationTask1, false);
        addTask(validationTask2, true);

        associateErrorMessage(validationTask1, "msg1");
        associateErrorMessage(validationTask2, "msg2");

        ipsValidation.validate(ipsProject);

        verify(validationTask1).execute(ipsProject);
        verify(validationTask2, never()).execute(ipsProject);
    }

    @Test
    public void shouldThrowNullPointerExceptionTryingToValidateGivenNullAsContextProject() {
        assertThrows(NullPointerException.class, () -> {
            ipsValidation.validate(null);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenTryingToAddNullAsTask() {
        assertThrows(NullPointerException.class, () -> {
            ipsValidation.addTask(null);
        });
    }

    private void addTask(IpsValidationTask task, boolean continueOnError) {
        lenient().when(task.isContinueOnError()).thenReturn(continueOnError);
        ipsValidation.addTask(task);
    }

    private void associateErrorMessage(IpsValidationTask task, String code) {
        Message message = new Message(code, "text", Message.ERROR);
        lenient().when(task.execute(ipsProject)).thenReturn(message);
    }

}
