/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class RenameRefactoringProcessorTest extends AbstractIpsRefactoringTest {

    private static final String TEST_ERROR_MESSAGE = "TestError";

    private static final String IGNORED_MESSAGE_CODE = "ignoredMessageCode";

    private MockProcessor mockProcessor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockProcessor = new MockProcessor(policyCmptType);
    }

    public void testCheckInitialConditions() throws OperationCanceledException, CoreException {
        RefactoringStatus status = mockProcessor.checkInitialConditions(new NullProgressMonitor());
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasError());
        assertEquals("TestError", status.getEntries()[0].getMessage());
    }

    public void testCheckInitialConditionsNonExistingIpsElement() throws OperationCanceledException, CoreException {
        // Overridden exists() method.
        class MockIpsElement extends IpsElement {

            public IResource getCorrespondingResource() {
                return null;
            }

            public Image getImage() {
                return null;
            }

            @Override
            public boolean exists() {
                return false;
            }

        }
        MockProcessor mockProcessor = new MockProcessor(new MockIpsElement());
        RefactoringStatus status = mockProcessor.checkInitialConditions(new NullProgressMonitor());
        assertEquals(2, status.getEntries().length);
        assertTrue(status.hasError());
        assertEquals(TEST_ERROR_MESSAGE, status.getEntries()[1].getMessage());
    }

    public void testCheckFinalConditions() throws OperationCanceledException, CoreException {
        RefactoringStatus status = mockProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertEquals(2, status.getEntries().length);
        assertTrue(status.hasError());
        assertEquals(TEST_ERROR_MESSAGE, status.getEntries()[1].getMessage());
        assertTrue(mockProcessor.validateNewElementNameThisCalled);
    }

    public void testValidateNewElementNameEmpty() throws CoreException {
        RefactoringStatus status = mockProcessor.validateNewElementName(new NullProgressMonitor());
        assertTrue(mockProcessor.validateNewElementNameThisCalled);
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasError());
    }

    public void testValidateNewElementNameEqualsOldElementName() throws CoreException {
        mockProcessor.setNewElementName(POLICY_NAME);
        RefactoringStatus status = mockProcessor.validateNewElementName(new NullProgressMonitor());
        assertTrue(mockProcessor.validateNewElementNameThisCalled);
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasError());
    }

    public void testAddValidationMessagesToStatusEmptyList() throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        MessageList validationMessageList = new MessageList();
        mockProcessor.addValidationMessagesToStatusTest(validationMessageList, status);
        assertFalse(status.hasEntries());
    }

    public void testAddValidationMessagesToStatusIgnoredMessageCode() throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        MessageList validationMessageList = new MessageList();
        validationMessageList.add(new Message(IGNORED_MESSAGE_CODE, "foo", Message.ERROR));
        mockProcessor.addValidationMessagesToStatusTest(validationMessageList, status);
        assertFalse(status.hasEntries());
    }

    public void testAddValidationMessagesToStatusError() throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        MessageList validationMessageList = new MessageList();
        validationMessageList.add(new Message("error", "error", Message.ERROR));
        mockProcessor.addValidationMessagesToStatusTest(validationMessageList, status);
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasFatalError());
    }

    public void testAddValidationMessagesToStatusWarning() throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        MessageList validationMessageList = new MessageList();
        validationMessageList.add(new Message("warning", "warning", Message.WARNING));
        mockProcessor.addValidationMessagesToStatusTest(validationMessageList, status);
        assertEquals(1, status.getEntries().length);
        assertFalse(status.hasFatalError());
        assertTrue(status.hasWarning());
    }

    public void testAddValidationMessagesToStatusInfo() throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        MessageList validationMessageList = new MessageList();
        validationMessageList.add(new Message("info", "info", Message.INFO));
        mockProcessor.addValidationMessagesToStatusTest(validationMessageList, status);
        assertEquals(1, status.getEntries().length);
        assertFalse(status.hasFatalError());
        assertTrue(status.hasInfo());
    }

    public void testCreateChange() throws OperationCanceledException, CoreException {
        assertNull(mockProcessor.createChange(new NullProgressMonitor()));
    }

    /**
     * Tests that <tt>refactorModel</tt> is called and that the returned <tt>Change</tt> is properly
     * executed.
     * <p>
     * Also tests that any modified source files are saved.
     */
    public void testPostCreateChange() throws OperationCanceledException, CoreException {
        policyCmptType.getIpsSrcFile().markAsDirty();
        productCmptType.getIpsSrcFile().markAsDirty();

        Change change = mockProcessor.postCreateChange(new Change[0], new NullProgressMonitor());
        assertNull(change);
        assertTrue(mockProcessor.mockChangeExecuted);

        assertFalse(policyCmptType.getIpsSrcFile().isDirty());
        assertTrue(productCmptType.getIpsSrcFile().isDirty());
    }

    public void testGetElements() {
        Object[] elements = mockProcessor.getElements();
        assertEquals(1, elements.length);
        assertEquals(policyCmptType, elements[0]);
    }

    public void testIsApplicable() throws CoreException {
        assertTrue(mockProcessor.isApplicable());
    }

    public void testGetSetNewElementName() {
        mockProcessor.setNewElementName("fooBar");
        assertEquals("fooBar", mockProcessor.getNewElementName());
    }

    public void testGetOriginalElementName() {
        mockProcessor.setNewElementName("fooBar");
        assertEquals(POLICY_NAME, mockProcessor.getOriginalElementName());
    }

    private class MockProcessor extends RenameRefactoringProcessor {

        private boolean validateNewElementNameThisCalled;

        private boolean mockChangeExecuted;

        protected MockProcessor(IIpsElement ipsElement) {
            super(ipsElement);
            getIgnoredValidationMessageCodes().add(IGNORED_MESSAGE_CODE);
        }

        @Override
        protected void checkFinalConditionsThis(RefactoringStatus status,
                IProgressMonitor pm,
                CheckConditionsContext context) throws CoreException {
            status.addError(TEST_ERROR_MESSAGE);
        }

        @Override
        protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
            status.addFatalError(TEST_ERROR_MESSAGE);
        }

        @Override
        protected Change refactorModel(IProgressMonitor pm) throws CoreException {
            addModifiedSrcFile(policyCmptType.getIpsSrcFile());
            return new MockChange();
        }

        @Override
        protected void validateNewElementNameThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
            validateNewElementNameThisCalled = true;
        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public String getProcessorName() {
            return null;
        }

        private void addValidationMessagesToStatusTest(MessageList validationMessageList, RefactoringStatus status) {
            addValidationMessagesToStatus(validationMessageList, status);
        }

        private class MockChange extends Change {

            @Override
            public Object getModifiedElement() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public void initializeValidationData(IProgressMonitor pm) {

            }

            @Override
            public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
                return new RefactoringStatus();
            }

            @Override
            public Change perform(IProgressMonitor pm) throws CoreException {
                mockChangeExecuted = true;
                return null;
            }

        }

    }

}
