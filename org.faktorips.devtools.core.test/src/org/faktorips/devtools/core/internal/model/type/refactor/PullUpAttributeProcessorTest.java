/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PullUpAttributeProcessorTest {

    private static final String ATTRIBUTE_NAME = "foo";

    @Mock
    private IProgressMonitor progressMonitor;

    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IType type;

    @Mock
    private IType superType;

    @Mock
    private IAttribute attribute;

    private PullUpAttributeProcessor pullUpAttributeProcessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        when(attribute.getType()).thenReturn(type);
        when(attribute.getName()).thenReturn(ATTRIBUTE_NAME);
        when(type.isSubtypeOf(superType, ipsProject)).thenReturn(true);

        pullUpAttributeProcessor = new PullUpAttributeProcessor(attribute);
    }

    @Test
    public void testCheckInitialConditionsThisTypeHasNoSupertype() throws CoreRuntimeException {
        when(type.hasSupertype()).thenReturn(false);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisSupertypeCannotBeFound() throws CoreRuntimeException {
        when(type.hasSupertype()).thenReturn(true);
        when(type.findSupertype(ipsProject)).thenReturn(null);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisTargetTypeNotASupertype() throws CoreRuntimeException {
        pullUpAttributeProcessor.setTarget(type);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisAttributeAlreadyExistingInTargetType() throws CoreRuntimeException {
        IAttribute alreadyExistingAttribute = mock(IAttribute.class);
        when(superType.getAttribute(ATTRIBUTE_NAME)).thenReturn(alreadyExistingAttribute);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisOverwrittenAttributeNotFoundInTargetTypeSuperHierarchy() throws CoreRuntimeException {
        when(attribute.isOverwrite()).thenReturn(true);

        // Add another hierarchy level
        IType superSuperType = mock(IType.class);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
        when(type.isSubtypeOf(superSuperType, ipsProject)).thenReturn(true);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisOverwrittenAttributeFoundInTargetTypeSuperHierarchy() throws CoreRuntimeException {
        when(attribute.isOverwrite()).thenReturn(true);

        // Add another hierarchy level
        IType superSuperType = mock(IType.class);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);
        when(type.isSubtypeOf(superSuperType, ipsProject)).thenReturn(true);

        // Create the base attribute
        IAttribute baseAttribute = mock(IAttribute.class);
        when(superSuperType.getAttribute(ATTRIBUTE_NAME)).thenReturn(baseAttribute);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThisValid() throws CoreRuntimeException {
        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testIsTargetTypeAllowed() {
        assertTrue(pullUpAttributeProcessor.isTargetTypeAllowed(mock(IType.class)));
        assertFalse(pullUpAttributeProcessor.isTargetTypeAllowed(mock(IIpsObjectPartContainer.class)));
    }

}
