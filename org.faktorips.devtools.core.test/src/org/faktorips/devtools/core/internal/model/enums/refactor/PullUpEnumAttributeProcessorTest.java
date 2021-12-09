/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PullUpEnumAttributeProcessorTest {

    private static final String ENUM_ATTRIBUTE_NAME = "foo";

    private PullUpEnumAttributeProcessor pullUpEnumAttributeProcessor;

    @Mock
    private IProgressMonitor progressMonitor;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IEnumType enumType;

    @Mock
    private IEnumType superEnumType;

    @Mock
    private IEnumAttribute enumAttribute;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        when(enumAttribute.getEnumType()).thenReturn(enumType);
        when(enumAttribute.getName()).thenReturn(ENUM_ATTRIBUTE_NAME);
        when(enumType.findSuperEnumType(ipsProject)).thenReturn(superEnumType);
        when(enumType.isSubEnumTypeOf(superEnumType, ipsProject)).thenReturn(true);
        when(enumType.hasSuperEnumType()).thenReturn(true);

        pullUpEnumAttributeProcessor = new PullUpEnumAttributeProcessor(enumAttribute);
        pullUpEnumAttributeProcessor.setTarget(superEnumType);
    }

    @Test
    public void testCheckInitialConditionsThisLiteralNameEnumAttribute() {
        enumAttribute = mock(IEnumLiteralNameAttribute.class);
        pullUpEnumAttributeProcessor = new PullUpEnumAttributeProcessor(enumAttribute);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisEnumTypeHasNoSuperEnumType() {
        when(enumType.hasSuperEnumType()).thenReturn(false);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisSuperEnumTypeCannotBeFound() {
        when(enumType.findSuperEnumType(ipsProject)).thenReturn(null);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisValid() {
        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThisTargetEnumTypeNotASupertypeOfOriginalEnumType() throws CoreRuntimeException {
        pullUpEnumAttributeProcessor.setTarget(enumType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeAlreadyExistingInTargetEnumType() throws CoreRuntimeException {
        when(superEnumType.containsEnumAttributeIncludeSupertypeCopies(ENUM_ATTRIBUTE_NAME)).thenReturn(true);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeInheritedButBaseAttributeNotFoundInTargetSupertypeHierarchy()
            throws CoreRuntimeException {

        when(enumAttribute.isInherited()).thenReturn(true);

        // Add another level
        IEnumType superSuperEnumType = mock(IEnumType.class);
        when(superEnumType.findSuperEnumType(ipsProject)).thenReturn(superSuperEnumType);
        when(superEnumType.isSubEnumTypeOf(superSuperEnumType, ipsProject)).thenReturn(true);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeInheritedBaseAttributeFoundInTargetSupertypeHierarchy()
            throws CoreRuntimeException {

        when(enumAttribute.isInherited()).thenReturn(true);

        // Add another level
        IEnumType superSuperEnumType = mock(IEnumType.class);
        when(superEnumType.findSuperEnumType(ipsProject)).thenReturn(superSuperEnumType);
        when(superEnumType.isSubEnumTypeOf(superSuperEnumType, ipsProject)).thenReturn(true);

        // Create the base enum attribute
        when(superSuperEnumType.containsEnumAttribute(ENUM_ATTRIBUTE_NAME)).thenReturn(true);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThisValid() throws CoreRuntimeException {
        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testIsTargetTypeAllowed() {
        assertTrue(pullUpEnumAttributeProcessor.isTargetTypeAllowed(mock(IEnumType.class)));
        assertFalse(pullUpEnumAttributeProcessor.isTargetTypeAllowed(mock(IIpsObjectPartContainer.class)));
    }

}
