/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.refactor.IIpsMoveRenameIpsObjectProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RefactoringParticipantHelperTest extends RefactoringParticipantTest {

    @Mock
    private IIpsObjectPartContainer mockIpsObjectPartContainer;

    @Mock
    private IIpsProject mockIpsProject;

    @Mock
    private StandardBuilderSet mockStandardBuilderSet;

    @Mock
    private IpsRefactoringProcessor ipsRefactoringProcessor;

    private TestParticipantHelper spyRefactoringHelper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        when(ipsRefactoringProcessor.refactorIpsModel(any(IProgressMonitor.class))).thenReturn(
                new IpsRefactoringModificationSet(null));
        TestParticipantHelper refactoringHelper = new TestParticipantHelper();
        spyRefactoringHelper = spy(refactoringHelper);
    }

    @Test
    public void testInitializeNonIpsElement() {
        assertFalse(spyRefactoringHelper.initialize(ipsRefactoringProcessor, new Object()));
    }

    @Test
    public void testInitializeIpsObjectPartContainer() {
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockStandardBuilderSet);
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(mockIpsObjectPartContainer);
        IIpsObjectPartContainer targetIpsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        modificationSet.setTargetElement(targetIpsObjectPartContainer);
        when(ipsRefactoringProcessor.refactorIpsModel(any(IProgressMonitor.class))).thenReturn(modificationSet);

        assertTrue(spyRefactoringHelper.initialize(ipsRefactoringProcessor, mockIpsObjectPartContainer));

        verify(spyRefactoringHelper).initializeJavaElements(targetIpsObjectPartContainer, mockStandardBuilderSet);
    }

    @Test
    public void testInitializeIpsObjectPartContainer_targetsRemembered() {
        ipsRefactoringProcessor = mock(IpsRefactoringProcessor.class,
                withSettings().extraInterfaces(IIpsMoveRenameIpsObjectProcessor.class));
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockStandardBuilderSet);
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(mockIpsObjectPartContainer);
        IIpsObjectPartContainer targetIpsObjectPartContainer = mock(IIpsObjectPartContainer.class);
        modificationSet.setTargetElement(targetIpsObjectPartContainer);
        when(ipsRefactoringProcessor.refactorIpsModel(any(IProgressMonitor.class))).thenReturn(modificationSet);

        assertTrue(spyRefactoringHelper.initialize(ipsRefactoringProcessor, mockIpsObjectPartContainer));

        verify(mockStandardBuilderSet).getGeneratedJavaElements(mockIpsObjectPartContainer);
        ((IIpsMoveRenameIpsObjectProcessor)verify(ipsRefactoringProcessor)).getTargetJavaElements();
    }

    @Test
    public void testInitializeNoStandardBuilderSetGiven() {
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        IIpsArtefactBuilderSet mockBuilderSet = mock(IIpsArtefactBuilderSet.class);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockBuilderSet);

        assertFalse(spyRefactoringHelper.initialize(ipsRefactoringProcessor, mockIpsObjectPartContainer));
    }

    public static class TestParticipantHelper extends RefactoringParticipantHelper {

        public TestParticipantHelper() {
            super();
        }

        @Override
        protected JavaRefactoring createJavaRefactoring(IJavaElement generatedJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) {

            return null;
        }

        @Override
        protected IJavaElement getTargetJavaElementForOriginalJavaElement(IJavaElement originalJavaElement) {
            return null;
        }

    }

}
