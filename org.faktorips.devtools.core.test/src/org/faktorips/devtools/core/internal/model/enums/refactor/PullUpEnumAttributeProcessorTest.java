/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
    public void setUp() throws CoreException {
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
    public void testCheckInitialConditionsThisLiteralNameEnumAttribute() throws CoreException {
        enumAttribute = mock(IEnumLiteralNameAttribute.class);
        pullUpEnumAttributeProcessor = new PullUpEnumAttributeProcessor(enumAttribute);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisEnumTypeHasNoSuperEnumType() throws CoreException {
        when(enumType.hasSuperEnumType()).thenReturn(false);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisSuperEnumTypeCannotBeFound() throws CoreException {
        when(enumType.findSuperEnumType(ipsProject)).thenReturn(null);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisValid() throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThisTargetEnumTypeNotASupertypeOfOriginalEnumType() throws CoreException {
        pullUpEnumAttributeProcessor.setTarget(enumType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeAlreadyExistingInTargetEnumType() throws CoreException {
        when(superEnumType.containsEnumAttribute(ENUM_ATTRIBUTE_NAME)).thenReturn(true);

        RefactoringStatus status = new RefactoringStatus();
        pullUpEnumAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisEnumAttributeInheritedButBaseAttributeNotFoundInTargetSupertypeHierarchy()
            throws CoreException {

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
            throws CoreException {

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
    public void testValidateUserInputThisValid() throws CoreException {
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
