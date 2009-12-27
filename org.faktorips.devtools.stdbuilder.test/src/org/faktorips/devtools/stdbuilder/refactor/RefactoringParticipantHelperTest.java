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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.text.edits.InsertEdit;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RefactoringParticipantHelperTest extends RefactoringParticipantTest {

    private RefactoringParticipantHelper refactoringHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        refactoringHelper = new RefactoringParticipantHelper();
    }

    public void testInitializeNonIpsElement() {
        assertFalse(refactoringHelper.initialize(new Object()));
    }

    public void testInitializeIpsElement() {
        assertNull(refactoringHelper.getGeneratedJavaElements());
        assertTrue(refactoringHelper.initialize(policyCmptType));
        assertNotNull(refactoringHelper.getGeneratedJavaElements());
    }

    public void testGetSetNewJavaElements() {
        assertNull(refactoringHelper.getNewJavaElements());
        refactoringHelper.initialize(policyCmptType);
        assertNull(refactoringHelper.getNewJavaElements());
        List<IJavaElement> newJavaElements = new ArrayList<IJavaElement>();
        refactoringHelper.setNewJavaElements(newJavaElements);
        assertEquals(newJavaElements, refactoringHelper.getNewJavaElements());
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

}
