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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsCompositeMoveRefactoringTest {

    @Mock
    private IIpsObject ipsObject1;

    @Mock
    private IIpsObject ipsObject2;

    @Mock
    private IIpsPackageFragment originalIpsPackageFragment;

    @Mock
    private IIpsPackageFragment targetIpsPackageFragment;

    private IpsCompositeMoveRefactoring ipsCompositeMoveRefactoring;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(ipsObject1.getIpsPackageFragment()).thenReturn(originalIpsPackageFragment);
        when(ipsObject2.getIpsPackageFragment()).thenReturn(originalIpsPackageFragment);
        ipsCompositeMoveRefactoring = new IpsCompositeMoveRefactoring(new LinkedHashSet<IIpsObject>(Arrays.asList(
                ipsObject1, ipsObject2)));
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(targetIpsPackageFragment);
    }

    @Test
    public void testValidateUserInputTargetPackageNotSet() throws CoreException {
        ipsCompositeMoveRefactoring = new IpsCompositeMoveRefactoring(new LinkedHashSet<IIpsObject>(
                Arrays.asList(ipsObject1)));

        RefactoringStatus result = ipsCompositeMoveRefactoring.validateUserInput(null);

        assertEquals(1, result.getEntries().length);
        assertEquals(RefactoringStatus.FATAL, result.getSeverity());
    }

    @Test
    public void testValidateUserInputTargetPackageEqualsOriginalPackage() throws CoreException {
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(originalIpsPackageFragment);

        RefactoringStatus result = ipsCompositeMoveRefactoring.validateUserInput(null);

        assertEquals(2, result.getEntries().length);
        assertEquals(RefactoringStatus.WARNING, result.getSeverity());
    }

    @Test
    public void testCreateRefactoring() {
        IIpsProcessorBasedRefactoring ipsMoveRefactoring = (IIpsProcessorBasedRefactoring)ipsCompositeMoveRefactoring
                .createRefactoring(ipsObject1);
        IpsMoveProcessor ipsMoveProcessor = (IpsMoveProcessor)ipsMoveRefactoring.getIpsRefactoringProcessor();

        assertEquals(targetIpsPackageFragment, ipsMoveProcessor.getTargetIpsPackageFragment());
    }

    @Test
    public void testCreateRefactoringTargetIpsPackageFragmentNotYetSet() {
        ipsCompositeMoveRefactoring = new IpsCompositeMoveRefactoring(new LinkedHashSet<IIpsObject>(
                Arrays.asList(ipsObject1)));
        ipsCompositeMoveRefactoring.createRefactoring(mock(IIpsObject.class));
        // Test successful if no NPE has been thrown
    }

    @Test(expected = NullPointerException.class)
    public void testSetTargetIpsPackageFragmentNullFragment() {
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(null);
    }

}
