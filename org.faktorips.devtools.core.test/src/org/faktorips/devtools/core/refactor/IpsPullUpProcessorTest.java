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

package org.faktorips.devtools.core.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IpsPullUpProcessorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsObjectPart ipsObjectPart;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsObject ipsObject;

    @Mock
    private IProgressMonitor progressMonitor;

    private TestIpsPullUpProcessor pullUpProcessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(ipsObjectPart.getIpsObject()).thenReturn(ipsObject);

        pullUpProcessor = new TestIpsPullUpProcessor(ipsObjectPart);
    }

    @Test
    public void testValidateUserInputTargetNotSpecified() throws CoreException {
        RefactoringStatus status = pullUpProcessor.validateUserInput(progressMonitor);
        assertEquals(RefactoringStatus.FATAL, status.getSeverity());
    }

    @Test
    public void testValidateUserInputTargetEqualsCurrentContainer() throws CoreException {
        pullUpProcessor.setTarget(ipsObject);

        RefactoringStatus status = pullUpProcessor.validateUserInput(progressMonitor);

        assertEquals(RefactoringStatus.FATAL, status.getSeverity());
    }

    @Test
    public void testValidateUserInputValid() throws CoreException {
        pullUpProcessor.setTarget(mock(IIpsObjectPartContainer.class));

        RefactoringStatus status = pullUpProcessor.validateUserInput(progressMonitor);

        assertTrue(status.isOK());
    }

    private static class TestIpsPullUpProcessor extends IpsPullUpProcessor<IIpsObjectPartContainer> {

        protected TestIpsPullUpProcessor(IIpsObjectPart ipsObjectPart) {
            super(ipsObjectPart);
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {

        }

        @Override
        protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {

        }

        @Override
        protected void addIpsSrcFiles() throws CoreException {

        }

        @Override
        public boolean isSourceFilesSavedRequired() {
            return false;
        }

        @Override
        public String getIdentifier() {
            return null;
        }

        @Override
        public String getProcessorName() {
            return null;
        }

    }

}
