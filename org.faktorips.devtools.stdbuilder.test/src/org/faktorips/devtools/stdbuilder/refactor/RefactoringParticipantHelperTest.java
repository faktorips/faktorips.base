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

package org.faktorips.devtools.stdbuilder.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RefactoringParticipantHelperTest extends RefactoringParticipantTest {

    private MockParticipantHelper refactoringHelper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        refactoringHelper = new MockParticipantHelper();
    }

    @Test
    public void testInitializeNonIpsElement() {
        assertFalse(refactoringHelper.initialize(new Object()));
        assertFalse(refactoringHelper.initializeTargetJavaElementsCalled);
    }

    @Test
    public void testInitializeIpsElement() {
        assertTrue(refactoringHelper.initialize(policyCmptType));
        assertTrue(refactoringHelper.initializeTargetJavaElementsCalled);
    }

    @Test
    public void testCheckConditions() throws CoreException {
        refactoringHelper.initialize(policyCmptType);
        performFullBuild();
        RefactoringStatus status = refactoringHelper.checkConditions(new NullProgressMonitor());

        assertFalse(status.hasFatalError());
        assertEquals(0, status.getEntries().length);
    }

    @Test
    public void testCreateChange() throws OperationCanceledException, CoreException {
        refactoringHelper.initialize(policyCmptType);
        performFullBuild();
        refactoringHelper.createChange(new NullProgressMonitor());
    }

    @Test
    public void testCreateChangeJavaElementsNotExisting() throws OperationCanceledException, CoreException {
        refactoringHelper.initialize(policyCmptType);
        refactoringHelper.createChange(new NullProgressMonitor());
    }

    private static class MockParticipantHelper extends RefactoringParticipantHelper {

        private boolean initializeTargetJavaElementsCalled;

        @Override
        protected Refactoring createJdtRefactoring(IJavaElement generatedJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status) {
            return null;
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet) {
            initializeTargetJavaElementsCalled = true;
            setTargetJavaElements(builderSet.getGeneratedJavaElements(ipsElement));
            return true;
        }

    }

}
