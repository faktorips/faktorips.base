/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class IpsRenameMoveProcessorTest extends AbstractIpsRefactoringTest {

    private static final String TEST_ERROR_MESSAGE = "TestError";

    private static final String IGNORED_MESSAGE_CODE = "ignoredMessageCode";

    private MockProcessor mockRenameTypeProcessor;

    private LocationDescriptor originalPolicyCmptTypeLocation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originalPolicyCmptTypeLocation = new LocationDescriptor(policyCmptType.getIpsPackageFragment(), POLICY_NAME);
        mockRenameTypeProcessor = new MockProcessor(policyCmptType, false);
    }

    public void testCheckFinalConditions() throws OperationCanceledException, CoreException {
        mockRenameTypeProcessor
                .setTargetLocation(new LocationDescriptor(policyCmptType.getIpsPackageFragment(), "test"));
        RefactoringStatus status = mockRenameTypeProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasError());
        assertEquals(TEST_ERROR_MESSAGE, status.getEntries()[0].getMessage());
        assertTrue(mockRenameTypeProcessor.validateNewElementNameThisCalled);
    }

    public void testValidateTargetLocationEmptyName() throws CoreException {
        mockRenameTypeProcessor.setTargetLocation(new LocationDescriptor(policyCmptType.getIpsPackageFragment(), ""));
        RefactoringStatus status = mockRenameTypeProcessor.validateTargetLocation(new NullProgressMonitor());
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasError());
    }

    public void testValidateTargetLocationNameEqualsOldElementName() throws CoreException {
        mockRenameTypeProcessor.setTargetLocation(new LocationDescriptor(policyCmptType.getIpsPackageFragment(),
                POLICY_NAME));
        RefactoringStatus status = mockRenameTypeProcessor.validateTargetLocation(new NullProgressMonitor());
        assertEquals(1, status.getEntries().length);
        assertTrue(status.hasError());
    }

    public void testGetSetTargetLocation() {
        LocationDescriptor targetLocation = new LocationDescriptor(policyCmptType.getIpsPackageFragment(), "test");
        mockRenameTypeProcessor.setTargetLocation(targetLocation);
        assertEquals(targetLocation, mockRenameTypeProcessor.getTargetLocation());
    }

    public void testGetOriginalLocation() {
        assertEquals(originalPolicyCmptTypeLocation, mockRenameTypeProcessor.getOriginalLocationTest());
    }

    private class MockProcessor extends IpsRenameMoveProcessor {

        private boolean validateNewElementNameThisCalled;

        protected MockProcessor(IIpsElement ipsElement, boolean move) {
            super(ipsElement, move);
            getIgnoredValidationMessageCodes().add(IGNORED_MESSAGE_CODE);
        }

        @Override
        protected LocationDescriptor initOriginalLocation() {
            return originalPolicyCmptTypeLocation;
        }

        @Override
        protected void checkFinalConditionsThis(RefactoringStatus status,
                IProgressMonitor pm,
                CheckConditionsContext context) throws CoreException {
            status.addError(TEST_ERROR_MESSAGE);
        }

        @Override
        protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {

        }

        @Override
        protected Change refactorIpsModel(IProgressMonitor pm) throws CoreException {
            return null;
        }

        @Override
        protected void validateTargetLocationThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
            validateNewElementNameThisCalled = true;
        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public String getProcessorName() {
            return null;
        }

        public LocationDescriptor getOriginalLocationTest() {
            return getOriginalLocation();
        }

    }

}
