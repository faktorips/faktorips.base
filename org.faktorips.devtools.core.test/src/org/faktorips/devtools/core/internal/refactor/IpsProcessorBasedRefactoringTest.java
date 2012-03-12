/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsProcessorBasedRefactoringTest {

    @Mock
    private IIpsElement ipsElement;

    private TestRefactoringProcessor ipsRefactoringProcessor;

    private IpsProcessorBasedRefactoring ipsProcessorBasedRefactoring;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ipsRefactoringProcessor = new TestRefactoringProcessor(ipsElement);
        ipsProcessorBasedRefactoring = new IpsProcessorBasedRefactoring(ipsRefactoringProcessor);
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
        protected void validateIpsModel(MessageList validationMessageList) throws CoreException {

        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {

        }

        @Override
        protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {

        }

        @Override
        protected void addIpsSrcFiles() throws CoreException {

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

    }

}
