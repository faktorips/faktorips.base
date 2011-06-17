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

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
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
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        when(attribute.getType()).thenReturn(type);
        when(attribute.getName()).thenReturn(ATTRIBUTE_NAME);
        when(type.isSubtypeOf(superType, ipsProject)).thenReturn(true);

        pullUpAttributeProcessor = new PullUpAttributeProcessor(attribute);
    }

    @Test
    public void testCheckInitialConditionsThisTypeHasNoSupertype() throws CoreException {
        when(type.hasSupertype()).thenReturn(false);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testCheckInitialConditionsThisSupertypeCannotBeFound() throws CoreException {
        when(type.hasSupertype()).thenReturn(true);
        when(type.findSupertype(ipsProject)).thenReturn(null);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.checkInitialConditionsThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisTargetTypeNotASupertype() throws CoreException {
        pullUpAttributeProcessor.setTarget(type);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisAttributeAlreadyExistingInTargetType() throws CoreException {
        IAttribute alreadyExistingAttribute = mock(IAttribute.class);
        when(superType.getAttribute(ATTRIBUTE_NAME)).thenReturn(alreadyExistingAttribute);

        pullUpAttributeProcessor.setTarget(superType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputThisOverwrittenAttributeNotFoundInTargetTypeSuperHierarchy() throws CoreException {
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
    public void testValidateUserInputThisOverwrittenAttributeFoundInTargetTypeSuperHierarchy() throws CoreException {
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
    public void testValidateUserInputThisValid() throws CoreException {
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
