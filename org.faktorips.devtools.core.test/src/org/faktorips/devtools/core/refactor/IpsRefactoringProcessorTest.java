/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsRefactoringProcessorTest {

    @Mock
    private IIpsElement ipsElement;

    @Mock
    private IProgressMonitor progressMonitor;

    @Mock
    private CheckConditionsContext checkConditionsContext;

    private TestProcessor testProcessorSpy;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(ipsElement.exists()).thenReturn(true);

        TestProcessor testProcessor = new TestProcessor(ipsElement);
        testProcessorSpy = spy(testProcessor);
    }

    @Test
    public void shouldReturnOkStatusOnCheckInitialConditionsIfAllConditionsAreFulfilled()
            throws OperationCanceledException, CoreException {

        RefactoringStatus status = testProcessorSpy.checkInitialConditions(progressMonitor);

        assertFalse(status.hasError());
    }

    @Test
    public void shouldReturnFatalErrorStatusOnCheckInitialConditionsIfTheIpsElementToBeRefactoredDoesNotExist()
            throws OperationCanceledException, CoreException {

        when(ipsElement.exists()).thenReturn(false);

        RefactoringStatus status = testProcessorSpy.checkInitialConditions(progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void shouldCallSubclassImplementationOfCheckInitialConditionsIfChecksWereSuccessfulThusFar()
            throws OperationCanceledException, CoreException {

        testProcessorSpy.checkInitialConditions(progressMonitor);

        verify(testProcessorSpy).checkInitialConditionsThis(any(RefactoringStatus.class), eq(progressMonitor));
    }

    @Test
    public void shouldNotCallSubclassImplementationOfCheckInitialConditionsIfChecksWereNotSuccessfulThusFar()
            throws OperationCanceledException, CoreException {

        when(ipsElement.exists()).thenReturn(false);

        testProcessorSpy.checkInitialConditions(progressMonitor);

        verify(testProcessorSpy, never()).checkInitialConditionsThis(any(RefactoringStatus.class), eq(progressMonitor));
    }

    @Test
    public void shouldReturnOkStatusOnCheckFinalConditionsIfAllConditionsAreFulfilled()
            throws OperationCanceledException, CoreException {

        RefactoringStatus status = testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        assertFalse(status.hasError());
    }

    @Test
    public void shouldReturnFatalErrorStatusOnCheckFinalConditionsIfAnAffectedIpsSrcFileIsOutOfSync()
            throws OperationCanceledException, CoreException {

        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, RETURNS_DEEP_STUBS);
        when(ipsSrcFile.getCorrespondingResource().isSynchronized(anyInt())).thenReturn(false);
        testProcessorSpy.addIpsSrcFile(ipsSrcFile);

        RefactoringStatus status = testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void shouldValidateUserInputOnCheckFinalConditions() throws OperationCanceledException, CoreException {
        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);
        assertTrue(testProcessorSpy.validateUserInputThisCalled);
    }

    @Test
    public void shouldCallSubclassImplementationOfCheckFinalConditionsIfChecksWereSuccessfulThusFar()
            throws CoreException {

        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);
        verify(testProcessorSpy).checkFinalConditionsThis(any(RefactoringStatus.class), eq(progressMonitor),
                eq(checkConditionsContext));
    }

    @Test
    public void shouldNotCallSubclassImplementationOfCheckFinalConditionsIfChecksWereNotSuccessfulThusFar()
            throws CoreException {

        testProcessorSpy.invalid = true;

        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        verify(testProcessorSpy, never()).checkFinalConditionsThis(any(RefactoringStatus.class), eq(progressMonitor),
                eq(checkConditionsContext));
    }

    @Test
    public void shouldAlwaysBeApplicable() throws CoreException {
        assertTrue(testProcessorSpy.isApplicable());
    }

    // Public so it can be accessed by Mockito
    public static class TestProcessor extends IpsRefactoringProcessor {

        private boolean invalid;

        private boolean validateUserInputThisCalled;

        protected TestProcessor(IIpsElement ipsElement) {
            super(ipsElement);
        }

        @Override
        protected void validateIpsModel(MessageList validationMessageList) throws CoreException {

        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
            if (invalid) {
                status.addFatalError("foo");
            }
            validateUserInputThisCalled = true;
        }

        @Override
        protected void addIpsSrcFiles() throws CoreException {

        }

        @Override
        protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {

        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public String getProcessorName() {
            return null;
        }

        @Override
        public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
                SharableParticipants sharedParticipants) throws CoreException {

            return null;
        }

        @Override
        public boolean isSourceFilesSavedRequired() {
            return false;
        }

    }

}
