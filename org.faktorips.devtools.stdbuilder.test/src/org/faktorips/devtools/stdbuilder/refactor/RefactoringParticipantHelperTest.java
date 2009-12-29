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

package org.faktorips.devtools.stdbuilder.refactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.InsertEdit;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RefactoringParticipantHelperTest extends RefactoringParticipantTest {

    private MockParticipantHelper refactoringHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        refactoringHelper = new MockParticipantHelper();
    }

    public void testInitializeNonIpsElement() {
        assertFalse(refactoringHelper.initialize(new Object()));
    }

    public void testInitializeIpsElement() {
        assertNull(refactoringHelper.getGeneratedJavaElementsTest());
        assertTrue(refactoringHelper.initialize(policyCmptType));
        assertNotNull(refactoringHelper.getGeneratedJavaElementsTest());
    }

    public void testGetSetNewJavaElements() {
        assertNull(refactoringHelper.getNewJavaElementsTest());
        refactoringHelper.initialize(policyCmptType);
        assertNull(refactoringHelper.getNewJavaElementsTest());
        List<IJavaElement> newJavaElements = new ArrayList<IJavaElement>();
        refactoringHelper.setNewJavaElementsTest(newJavaElements);
        assertEquals(newJavaElements, refactoringHelper.getNewJavaElementsTest());
    }

    public void testCheckConditions() throws CoreException, IOException {
        refactoringHelper.initialize(policyCmptType);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        RefactoringStatus status = refactoringHelper.checkConditions(new NullProgressMonitor(),
                new CheckConditionsContext());

        assertFalse(status.hasFatalError());
        assertEquals(0, status.getEntries().length);

        // Cause compile errors.
        policyClass.getCompilationUnit().applyTextEdit(new InsertEdit(0, "Hello"), null);
        policyClass.getCompilationUnit().makeConsistent(null);

        status = refactoringHelper.checkConditions(new NullProgressMonitor(), new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    private static class MockParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected void createChangeThis(IJavaElement originalJavaElement,
                IJavaElement newJavaElement,
                IProgressMonitor pm) throws CoreException, OperationCanceledException {

        }

        @Override
        protected boolean initializeNewJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet) {
            return true;
        }

        private List<IJavaElement> getGeneratedJavaElementsTest() {
            return getGeneratedJavaElements();
        }

        private List<IJavaElement> getNewJavaElementsTest() {
            return getNewJavaElements();
        }

        private void setNewJavaElementsTest(List<IJavaElement> newJavaElements) {
            setNewJavaElements(newJavaElements);
        }

    }

}
