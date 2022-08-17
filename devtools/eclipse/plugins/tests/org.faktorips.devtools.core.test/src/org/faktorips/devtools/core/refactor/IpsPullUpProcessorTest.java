/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
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
    public void testValidateUserInputTargetNotSpecified() {
        RefactoringStatus status = pullUpProcessor.validateUserInput(progressMonitor);
        assertEquals(RefactoringStatus.FATAL, status.getSeverity());
    }

    @Test
    public void testValidateUserInputTargetEqualsCurrentContainer() {
        pullUpProcessor.setTarget(ipsObject);

        RefactoringStatus status = pullUpProcessor.validateUserInput(progressMonitor);

        assertEquals(RefactoringStatus.FATAL, status.getSeverity());
    }

    @Test
    public void testValidateUserInputValid() {
        pullUpProcessor.setTarget(mock(IIpsObjectPartContainer.class));

        RefactoringStatus status = pullUpProcessor.validateUserInput(progressMonitor);

        assertTrue(status.isOK());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTargetNotAllowedTargetType() {
        TestIpsPullUpProcessor spyProcessor = spy(pullUpProcessor);
        when(spyProcessor.isTargetTypeAllowed(any(IIpsObjectPartContainer.class))).thenReturn(false);
        spyProcessor.setTarget(ipsObject);
    }

    // Public so Mockito can spy the class
    public static class TestIpsPullUpProcessor extends IpsPullUpProcessor {

        protected TestIpsPullUpProcessor(IIpsObjectPart ipsObjectPart) {
            super(ipsObjectPart);
        }

        @Override
        public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
            IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
            addAffectedSrcFiles(modificationSet);
            return modificationSet;
        }

        @Override
        protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
            return new HashSet<>();
        }

        @Override
        protected boolean isTargetTypeAllowed(IIpsObjectPartContainer target) {
            return true;
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
