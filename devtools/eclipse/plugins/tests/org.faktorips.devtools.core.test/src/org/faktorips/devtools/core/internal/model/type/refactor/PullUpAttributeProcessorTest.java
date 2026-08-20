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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

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

    private MockitoSession mockito;

    private PullUpAttributeProcessor pullUpAttributeProcessor;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        lenient().when(attribute.getIpsProject()).thenReturn(ipsProject);
        lenient().when(attribute.getType()).thenReturn(type);
        lenient().when(attribute.getName()).thenReturn(ATTRIBUTE_NAME);
        lenient().when(type.isSubtypeOf(superType, ipsProject)).thenReturn(true);

        pullUpAttributeProcessor = new PullUpAttributeProcessor(attribute);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mockito.finishMocking();
    }

    @Test
    public void testCheckInitialConditionsThisTypeHasNoSupertype() {
        when(type.hasSupertype()).thenReturn(false);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisSupertypeCannotBeFound() {
        when(type.hasSupertype()).thenReturn(true);
        when(type.findSupertype(ipsProject)).thenReturn(null);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisTargetTypeNotASupertype() {
        pullUpAttributeProcessor.setTarget(type);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisAttributeAlreadyExistingInTargetType() {
        IAttribute alreadyExistingAttribute = mock(IAttribute.class);
        when(superType.getAttribute(ATTRIBUTE_NAME)).thenReturn(alreadyExistingAttribute);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisOverwrittenAttributeNotFoundInTargetTypeSuperHierarchy() {
        when(attribute.isOverwrite()).thenReturn(true);

        // Add another hierarchy level
        IType superSuperType = mock(IType.class);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisOverwrittenAttributeFoundInTargetTypeSuperHierarchy() {
        when(attribute.isOverwrite()).thenReturn(true);

        // Add another hierarchy level
        IType superSuperType = mock(IType.class);
        when(superType.findSupertype(ipsProject)).thenReturn(superSuperType);

        // Create the base attribute
        IAttribute baseAttribute = mock(IAttribute.class);
        when(superSuperType.getAttribute(ATTRIBUTE_NAME)).thenReturn(baseAttribute);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testValidateUserInputThisValid() {
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
