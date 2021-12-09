/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.runtime.MessageList;
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

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsSrcFile ipsSrcFile;

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
        Set<IIpsSrcFile> affectedFiles = new HashSet<>();
        affectedFiles.add(ipsSrcFile);
        when(testProcessorSpy.getAffectedIpsSrcFiles()).thenReturn(affectedFiles);

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
            throws CoreRuntimeException {

        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);
        verify(testProcessorSpy).checkFinalConditionsThis(any(RefactoringStatus.class), eq(progressMonitor),
                eq(checkConditionsContext));
    }

    @Test
    public void shouldNotCallSubclassImplementationOfCheckFinalConditionsIfChecksWereNotSuccessfulThusFar()
            throws CoreRuntimeException {

        testProcessorSpy.invalid = true;

        testProcessorSpy.checkFinalConditions(progressMonitor, checkConditionsContext);

        verify(testProcessorSpy, never()).checkFinalConditionsThis(any(RefactoringStatus.class), eq(progressMonitor),
                eq(checkConditionsContext));
    }

    @Test
    public void shouldAlwaysBeApplicable() throws CoreRuntimeException {
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
        protected void validateIpsModel(MessageList validationMessageList) throws CoreRuntimeException {

        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreRuntimeException {
            if (invalid) {
                status.addFatalError("foo");
            }
            validateUserInputThisCalled = true;
        }

        @Override
        protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
            return new HashSet<>();

        }

        @Override
        public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreRuntimeException {
            IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(null);
            addAffectedSrcFiles(modificationSet);
            return modificationSet;
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
                SharableParticipants sharedParticipants) throws CoreRuntimeException {

            return null;
        }

        @Override
        public boolean isSourceFilesSavedRequired() {
            return false;
        }

    }

    @Test
    public void testGetIpsElement_ipsObjectPart() throws Exception {
        IIpsObjectPart ipsObjectPart = mock(IIpsObjectPart.class);
        IpsRefactoringProcessor refactoringProcessor = new TestProcessor(ipsObjectPart);

        when(ipsObjectPart.isDeleted()).thenReturn(false);
        assertSame(ipsObjectPart, refactoringProcessor.getIpsElement());

        IIpsObjectPart ipsObjectPart2 = mock(IIpsObjectPart.class);

        when(ipsObjectPart.getId()).thenReturn("1");
        when(ipsObjectPart2.getId()).thenReturn("1");

        when(ipsObjectPart.isDeleted()).thenReturn(true);
        when(ipsObjectPart.getParent()).thenReturn(ipsObject);
        when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getChildren()).thenReturn(new IIpsElement[] { ipsObjectPart2 });

        assertSame(ipsObjectPart2, refactoringProcessor.getIpsElement());
    }

    @Test
    public void testGetIpsElement_ipsObject() throws Exception {
        IpsRefactoringProcessor refactoringProcessor = new TestProcessor(ipsObject);
        IIpsObject ipsObject2 = mock(IIpsObject.class);

        when(ipsObject.exists()).thenReturn(true);
        when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.getIpsObject()).thenReturn(ipsObject2);

        assertSame(ipsObject2, refactoringProcessor.getIpsElement());
    }

    @Test
    public void testGetIpsElement_anyOther() throws Exception {
        IIpsElement ipsElement = mock(IIpsElement.class);

        IpsRefactoringProcessor refactoringProcessor = new TestProcessor(ipsElement);

        when(ipsElement.exists()).thenReturn(true);
        assertSame(ipsElement, refactoringProcessor.getIpsElement());

        when(ipsElement.exists()).thenReturn(false);

        assertSame(ipsElement, refactoringProcessor.getIpsElement());
    }

}
