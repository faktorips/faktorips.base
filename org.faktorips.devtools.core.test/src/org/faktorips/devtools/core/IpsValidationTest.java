/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsValidationTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IpsValidationTask validationTask1;

    @Mock
    private IpsValidationTask validationTask2;

    private IpsValidation ipsValidation;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ipsValidation = new IpsValidation();
    }

    @Test
    public void shouldExecuteEveryAddedTask() throws CoreException {
        addTask(validationTask1, true);
        addTask(validationTask2, true);

        ipsValidation.validate(ipsProject);

        InOrder inOrder = inOrder(validationTask1, validationTask2);
        inOrder.verify(validationTask1).execute(ipsProject);
        inOrder.verify(validationTask2).execute(ipsProject);
    }

    @Test
    public void shouldAddResultOfEveryAddedTaskToResultMessageList() throws CoreException {
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
    public void shouldStopIfATaskReturnsAnErrorWhileContinueOnErrorIsFalse() throws CoreException {
        addTask(validationTask1, false);
        addTask(validationTask2, true);

        associateErrorMessage(validationTask1, "msg1");
        associateErrorMessage(validationTask2, "msg2");

        ipsValidation.validate(ipsProject);

        verify(validationTask1).execute(ipsProject);
        verify(validationTask2, never()).execute(ipsProject);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionTryingToValidateGivenNullAsContextProject() throws CoreException {
        ipsValidation.validate(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenTryingToAddNullAsTask() {
        ipsValidation.addTask(null);
    }

    private void addTask(IpsValidationTask task, boolean continueOnError) {
        when(task.isContinueOnError()).thenReturn(continueOnError);
        ipsValidation.addTask(task);
    }

    private void associateErrorMessage(IpsValidationTask task, String code) throws CoreException {
        Message message = new Message(code, "text", Message.ERROR);
        when(task.execute(ipsProject)).thenReturn(message);
    }

}
