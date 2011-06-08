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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PullUpAttributeProcessorTest extends AbstractIpsRefactoringTest {

    @Mock
    private IProgressMonitor progressMonitor;

    private PullUpAttributeProcessor pullUpAttributeProcessor;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        pullUpAttributeProcessor = new PullUpAttributeProcessor(policyCmptTypeAttribute);
    }

    @Test
    public void testValidateUserInputThisTargetTypeNotASupertype() throws CoreException {
        pullUpAttributeProcessor.setTarget(mock(IType.class));

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertEquals(RefactoringStatus.FATAL, status.getSeverity());
    }

    @Test
    public void testValidateUserInputThisAttributeAlreadyExistingInTargetType() throws CoreException {
        IPolicyCmptTypeAttribute alreadyExistingAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        alreadyExistingAttribute.copyFrom(policyCmptTypeAttribute);

        pullUpAttributeProcessor.setTarget(superPolicyCmptType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertEquals(RefactoringStatus.FATAL, status.getSeverity());
    }

    @Test
    public void testValidateUserInputThisValid() throws CoreException {
        pullUpAttributeProcessor.setTarget(superPolicyCmptType);

        RefactoringStatus status = new RefactoringStatus();
        pullUpAttributeProcessor.validateUserInputThis(status, progressMonitor);

        assertTrue(status.isOK());
    }

    @Test
    public void testPullUpPolicyCmptTypeAttribute() throws CoreException {
        performPullUpRefactoring(policyCmptTypeAttribute, superPolicyCmptType);

        // Check that attribute no longer exists in original type
        assertNull(policyCmptType.getAttribute(policyCmptTypeAttribute.getName()));

        // Check that attribute exists in target type
        assertNotNull(superPolicyCmptType.getAttribute(policyCmptTypeAttribute.getName()));
    }

    @Test
    public void testPullUpProductCmptTypeAttribute() throws CoreException {
        performPullUpRefactoring(productCmptTypeAttribute, superProductCmptType);

        // Check that attribute no longer exists in original type
        assertNull(productCmptType.getAttribute(productCmptTypeAttribute.getName()));

        // Check that attribute exists in target type
        assertNotNull(superProductCmptType.getAttribute(productCmptTypeAttribute.getName()));
    }

}
