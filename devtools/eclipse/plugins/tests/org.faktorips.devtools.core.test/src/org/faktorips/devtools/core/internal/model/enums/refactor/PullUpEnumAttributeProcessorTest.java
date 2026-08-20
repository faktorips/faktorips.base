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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

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

    private MockitoSession mockito;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        lenient().when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        lenient().when(enumAttribute.getEnumType()).thenReturn(enumType);
        lenient().when(enumAttribute.getName()).thenReturn(ENUM_ATTRIBUTE_NAME);
        lenient().when(enumType.findSuperEnumType(ipsProject)).thenReturn(superEnumType);
        lenient().when(enumType.isSubEnumTypeOf(superEnumType, ipsProject)).thenReturn(true);
        lenient().when(enumType.hasSuperEnumType()).thenReturn(true);

        pullUpEnumAttributeProcessor = new PullUpEnumAttributeProcessor(enumAttribute);
        pullUpEnumAttributeProcessor.setTarget(superEnumType);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mockito.finishMocking();
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
    public void testValidateUserInputThisTargetEnumTypeNotASupertypeOfOriginalEnumType() {
        pullUpEnumAttributeProcessor.setTarget(enumType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeAlreadyExistingInTargetEnumType() {
        when(superEnumType.containsEnumAttributeIncludeSupertypeCopies(ENUM_ATTRIBUTE_NAME)).thenReturn(true);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeInheritedButBaseAttributeNotFoundInTargetSupertypeHierarchy() {

        when(enumAttribute.isInherited()).thenReturn(true);

        // Add another level
        IEnumType superSuperEnumType = mock(IEnumType.class);
        when(superEnumType.findSuperEnumType(ipsProject)).thenReturn(superSuperEnumType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeInheritedBaseAttributeFoundInTargetSupertypeHierarchy() {

        when(enumAttribute.isInherited()).thenReturn(true);

        // Add another level
        IEnumType superSuperEnumType = mock(IEnumType.class);
        when(superEnumType.findSuperEnumType(ipsProject)).thenReturn(superSuperEnumType);

        // Create the base enum attribute
        when(superSuperEnumType.containsEnumAttribute(ENUM_ATTRIBUTE_NAME)).thenReturn(true);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThisValid() {
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
