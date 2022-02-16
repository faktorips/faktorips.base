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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
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
        LinkedHashSet<IIpsElement> ipsObjects = new LinkedHashSet<>(Arrays.asList(ipsObject1, ipsObject2));
        ipsCompositeMoveRefactoring = new IpsCompositeMoveRefactoring(ipsObjects);
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(targetIpsPackageFragment);
    }

    @Test
    public void testValidateUserInputTargetPackageNotSet() {
        LinkedHashSet<IIpsElement> ipsObjects = new LinkedHashSet<>(Arrays.asList(ipsObject1));
        ipsCompositeMoveRefactoring = new IpsCompositeMoveRefactoring(ipsObjects);

        RefactoringStatus result = ipsCompositeMoveRefactoring.validateUserInput(null);

        assertEquals(1, result.getEntries().length);
        assertEquals(RefactoringStatus.FATAL, result.getSeverity());
    }

    @Test
    public void testValidateUserInputTargetPackageEqualsOriginalPackage() {
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
        LinkedHashSet<IIpsElement> ipsObjects = new LinkedHashSet<>(Arrays.asList(ipsObject1));
        ipsCompositeMoveRefactoring = new IpsCompositeMoveRefactoring(ipsObjects);
        ipsCompositeMoveRefactoring.createRefactoring(mock(IIpsObject.class));
        // Test successful if no NPE has been thrown
    }

    @Test(expected = NullPointerException.class)
    public void testSetTargetIpsPackageFragmentNullFragment() {
        ipsCompositeMoveRefactoring.setTargetIpsPackageFragment(null);
    }

}
