/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

    private TestParticipantHelper spyRefactoringHelper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        TestParticipantHelper refactoringHelper = new TestParticipantHelper();
        spyRefactoringHelper = spy(refactoringHelper);
    }

    @Test
    public void testInitializeNonIpsElement() {
        assertFalse(spyRefactoringHelper.initialize(new Object()));
    }

    @Test
    public void testInitializeIpsObjectPartContainer() {
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockStandardBuilderSet);

        assertTrue(spyRefactoringHelper.initialize(mockIpsObjectPartContainer));

        verify(spyRefactoringHelper).initializeTargetJavaElements(mockIpsObjectPartContainer, mockStandardBuilderSet);
    }

    @Test
    public void testInitializeNoStandardBuilderSetGiven() {
        when(mockIpsObjectPartContainer.getIpsProject()).thenReturn(mockIpsProject);
        IIpsArtefactBuilderSet mockBuilderSet = mock(IIpsArtefactBuilderSet.class);
        when(mockIpsProject.getIpsArtefactBuilderSet()).thenReturn(mockBuilderSet);

        assertFalse(spyRefactoringHelper.initialize(mockIpsObjectPartContainer));
    }

    public static class TestParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected Refactoring createJdtRefactoring(IJavaElement generatedJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) {

            return null;
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            return true;
        }

        @Override
        protected IJavaElement getTargetJavaElementForOriginalJavaElement(IJavaElement originalJavaElement) {
            return null;
        }

    }

}
