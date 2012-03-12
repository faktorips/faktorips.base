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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// Cannot test the createChange method as LTK's PerformRefactoringOperation does not allow it
public class IpsCompositeRefactoringTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsElement ipsElement1;

    @Mock
    private IIpsElement ipsElement2;

    @Mock
    private IIpsRefactoring refactoring1;

    @Mock
    private IIpsRefactoring refactoring2;

    @Mock
    private Refactoring ltkRefactoring1;

    @Mock
    private Refactoring ltkRefactoring2;

    @Mock
    private IProgressMonitor progressMonitor;

    private IpsCompositeRefactoring ipsCompositeRefactoring;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(ipsElement1.getIpsProject()).thenReturn(ipsProject);
        when(ipsElement2.getIpsProject()).thenReturn(ipsProject);
        when(refactoring1.toLtkRefactoring()).thenReturn(ltkRefactoring1);
        when(refactoring2.toLtkRefactoring()).thenReturn(ltkRefactoring2);
        ipsCompositeRefactoring = new TestIpsCompositeRefactoring(new LinkedHashSet<IIpsElement>(Arrays.asList(
                ipsElement1, ipsElement2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentExceptionIfEmptyElementSetGiven() {
        new TestIpsCompositeRefactoring(new HashSet<IIpsElement>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentExceptionIfDifferentTypesOfElementsGiven() {
        Set<IIpsElement> elements = new LinkedHashSet<IIpsElement>();
        elements.add(mock(IIpsObject.class));
        elements.add(mock(IIpsObjectPart.class));
        new TestIpsCompositeRefactoring(elements);
    }

    @Test
    public void testNoIllegalArgumentExceptionIfDifferentTypesOfIpsObjectsGiven() {
        Set<IIpsElement> elements = new LinkedHashSet<IIpsElement>();
        elements.add(mock(IPolicyCmptType.class));
        elements.add(mock(IProductCmptType.class));
        new TestIpsCompositeRefactoring(elements);
    }

    @Test
    public void testIsSourceFilesSavedRequired() {
        when(refactoring1.isSourceFilesSavedRequired()).thenReturn(true);
        when(refactoring2.isSourceFilesSavedRequired()).thenReturn(true);
        assertTrue(ipsCompositeRefactoring.isSourceFilesSavedRequired());

        when(refactoring1.isSourceFilesSavedRequired()).thenReturn(false);
        when(refactoring2.isSourceFilesSavedRequired()).thenReturn(true);
        assertTrue(ipsCompositeRefactoring.isSourceFilesSavedRequired());

        when(refactoring1.isSourceFilesSavedRequired()).thenReturn(true);
        when(refactoring2.isSourceFilesSavedRequired()).thenReturn(false);
        assertTrue(ipsCompositeRefactoring.isSourceFilesSavedRequired());

        when(refactoring1.isSourceFilesSavedRequired()).thenReturn(false);
        when(refactoring2.isSourceFilesSavedRequired()).thenReturn(false);
        assertFalse(ipsCompositeRefactoring.isSourceFilesSavedRequired());
    }

    @Test
    public void testCheckInitialConditions() throws OperationCanceledException, CoreException {
        RefactoringStatus status1 = RefactoringStatus.createWarningStatus("");
        when(ltkRefactoring1.checkInitialConditions(any(IProgressMonitor.class))).thenReturn(status1);
        RefactoringStatus status2 = RefactoringStatus.createErrorStatus("");
        when(ltkRefactoring2.checkInitialConditions(any(IProgressMonitor.class))).thenReturn(status2);

        RefactoringStatus refactoringStatus = ipsCompositeRefactoring.checkInitialConditions(progressMonitor);
        assertTrue(refactoringStatus.hasError());
        assertEquals(2, refactoringStatus.getEntries().length);
    }

    @Test
    public void testCheckFinalConditions() throws OperationCanceledException, CoreException {
        RefactoringStatus refactoringStatus = ipsCompositeRefactoring.checkFinalConditions(progressMonitor);
        assertTrue(refactoringStatus.isOK());
    }

    @Test
    public void testGetNumberOfRefactorings() {
        assertEquals(2, ipsCompositeRefactoring.getNumberOfRefactorings());
    }

    @Test
    public void testIsCancelable() {
        assertTrue(ipsCompositeRefactoring.isCancelable());
    }

    @Test
    public void testIsCancelableOnlyOneRefactoring() {
        ipsCompositeRefactoring = new TestIpsCompositeRefactoring(new LinkedHashSet<IIpsElement>(
                Arrays.asList(ipsElement1)));
        assertFalse(ipsCompositeRefactoring.isCancelable());
    }

    @Test
    public void testGetIpsProject() {
        assertEquals(ipsProject, ipsCompositeRefactoring.getIpsProject());
    }

    @Test
    public void testGetIpsElements() {
        assertEquals(new LinkedHashSet<IIpsElement>(Arrays.asList(ipsElement1, ipsElement2)),
                ipsCompositeRefactoring.getIpsElements());
    }

    @Test
    public void testGetIpsElementsOnlyReturnsCopy() {
        Set<IIpsElement> elements = ipsCompositeRefactoring.getIpsElements();
        assertFalse(elements == ipsCompositeRefactoring.getIpsElements());
    }

    @Test
    public void testReturnValueOfSkipElement() {
        assertTrue(ipsCompositeRefactoring.skipElement(ipsElement1));
        assertFalse(ipsCompositeRefactoring.skipElement(ipsElement1));
    }

    @Test
    public void testIgnoreIsSourceFilesSavedRequiredForSkippedElement() {
        ipsCompositeRefactoring.skipElement(ipsElement2);

        IpsCompositeRefactoring spyIpsCompositeRefactoring = spy(ipsCompositeRefactoring);
        spyIpsCompositeRefactoring.isSourceFilesSavedRequired();

        verify(spyIpsCompositeRefactoring).createRefactoring(ipsElement1);
        verify(spyIpsCompositeRefactoring, never()).createRefactoring(ipsElement2);
    }

    @Test
    public void testIgnoreCheckInitialConditionsForSkippedElement() throws OperationCanceledException, CoreException {
        ipsCompositeRefactoring.skipElement(ipsElement2);

        IpsCompositeRefactoring spyIpsCompositeRefactoring = spy(ipsCompositeRefactoring);
        spyIpsCompositeRefactoring.checkInitialConditions(progressMonitor);

        verify(spyIpsCompositeRefactoring).createRefactoring(ipsElement1);
        verify(spyIpsCompositeRefactoring, never()).createRefactoring(ipsElement2);
    }

    @Test
    public void testClearSkippedElements() throws OperationCanceledException, CoreException {
        ipsCompositeRefactoring.skipElement(ipsElement1);
        ipsCompositeRefactoring.skipElement(ipsElement2);

        ipsCompositeRefactoring.clearSkippedElements();

        IpsCompositeRefactoring spyIpsCompositeRefactoring = spy(ipsCompositeRefactoring);
        spyIpsCompositeRefactoring.isSourceFilesSavedRequired();
        spyIpsCompositeRefactoring.checkInitialConditions(progressMonitor);

        verify(spyIpsCompositeRefactoring, times(2)).createRefactoring(ipsElement1);
        verify(spyIpsCompositeRefactoring, times(2)).createRefactoring(ipsElement2);
    }

    // Public so Mockito is able to spy
    public class TestIpsCompositeRefactoring extends IpsCompositeRefactoring {

        protected TestIpsCompositeRefactoring(Set<IIpsElement> ipsElements) {
            super(ipsElements);
        }

        @Override
        protected IIpsRefactoring createRefactoring(IIpsElement ipsElement) {
            if (ipsElement == ipsElement1) {
                return refactoring1;
            } else {
                return refactoring2;
            }
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
            return null;
        }

    }

}
