/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsProcessorBasedRefactoringTest {

    @Mock
    private IIpsElement ipsElement;

    private TestRefactoringProcessor ipsRefactoringProcessor;

    private IpsProcessorBasedRefactoring ipsProcessorBasedRefactoring;

    private AutoCloseable openMocks;

    @Before
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        ipsRefactoringProcessor = new TestRefactoringProcessor(ipsElement);
        ipsProcessorBasedRefactoring = new IpsProcessorBasedRefactoring(ipsRefactoringProcessor);
    }

    @After
    public void releaseMocks() throws Exception {
        openMocks.close();
    }

    @Test
    public void testIsSourceFilesSavedRequired() {
        ipsRefactoringProcessor.setSourceFilesSavedRequired(false);
        assertFalse(ipsProcessorBasedRefactoring.isSourceFilesSavedRequired());
        ipsRefactoringProcessor.setSourceFilesSavedRequired(true);
        assertTrue(ipsProcessorBasedRefactoring.isSourceFilesSavedRequired());
    }

    @Test
    public void testIsCancelable() {
        assertFalse(ipsProcessorBasedRefactoring.isCancelable());
    }

    @Test
    public void testGetIpsProject() {
        IIpsProject ipsProject = mock(IIpsProject.class);
        when(ipsElement.getIpsProject()).thenReturn(ipsProject);

        assertEquals(ipsProject, ipsProcessorBasedRefactoring.getIpsProject());
    }

    @Test
    public void testGetIpsElements() {
        assertTrue(ipsProcessorBasedRefactoring.getIpsElements().contains(ipsElement));
        assertEquals(1, ipsProcessorBasedRefactoring.getIpsElements().size());
    }

    @Test
    public void testGetIpsElementsOnlyReturnsCopy() {
        Set<IIpsElement> elements = ipsProcessorBasedRefactoring.getIpsElements();
        assertFalse(elements == ipsProcessorBasedRefactoring.getIpsElements());
    }

    private static class TestRefactoringProcessor extends IpsRefactoringProcessor {

        private boolean sourceFilesSavedRequired;

        protected TestRefactoringProcessor(IIpsElement ipsElement) {
            super(ipsElement);
        }

        @Override
        public boolean isSourceFilesSavedRequired() {
            return sourceFilesSavedRequired;
        }

        private void setSourceFilesSavedRequired(boolean sourceFilesSavedRequired) {
            this.sourceFilesSavedRequired = sourceFilesSavedRequired;
        }

        @Override
        protected void validateIpsModel(MessageList validationMessageList) {
            // nothing to do
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) {
            // nothing to do
        }

        @Override
        public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
            IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(null);
            addAffectedSrcFiles(modificationSet);
            return modificationSet;
        }

        @Override
        protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
            return new HashSet<>();

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
                SharableParticipants sharedParticipants) {
            return null;
        }

    }

}
