/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsCoreExtensions;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.INewProductDefinitionOperationParticipant;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewProductDefinitionOperationTest extends AbstractIpsPluginTest {

    @Mock
    private IProgressMonitor monitor;

    private IIpsProject ipsProject;

    private SingletonMockHelper singletonMockHelper;

    private AutoCloseable openMocks;

    @Override
    @Before
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        ipsProject = newIpsProject();
        singletonMockHelper = new SingletonMockHelper();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        singletonMockHelper.reset();
        openMocks.close();
    }

    @Test
    public void testRun_CreateIpsPackageFragmentIfNonExistent() throws InvocationTargetException, InterruptedException {
        TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getIpsPackageFragment("notExistent"));

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertTrue(ipsProject.getIpsPackageFragmentRoots()[0].getIpsPackageFragment("notExistent").exists());
    }

    @Test
    public void testRun_CreateIpsSrcFileOfCorrectNameAndType() throws InvocationTargetException, InterruptedException {

        TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newIpsSrcFile = ipsProject.findIpsSrcFile(pmo.getIpsObjectType(), pmo.getName());
        assertNotNull(newIpsSrcFile);
        assertTrue(newIpsSrcFile.exists());
    }

    @Test
    public void testRun_CallFinishIpsSrcFileSubclassImplementation() throws InvocationTargetException,
            InterruptedException {

        TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertTrue(operation.finishIpsSrcFileCalled);
    }

    @Test
    public void testRun_IpsSrcFileShouldBeClean() throws InvocationTargetException, InterruptedException {
        TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertFalse(ipsProject.findIpsSrcFile(pmo.getIpsObjectType(), pmo.getName()).isDirty());
    }

    @Test
    public void testRun_CallPostProcessSubclassImplementation() throws InvocationTargetException, InterruptedException {
        TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertTrue(operation.postProcessCalled);
    }

    @Test
    public void testRun_CallParticipants() throws InvocationTargetException, InterruptedException {
        IpsPlugin ipsPlugin = IpsPlugin.getDefault();
        IpsCoreExtensions originalIpsCoreExtensions = ipsPlugin.getIpsCoreExtensions();
        try {
            IpsCoreExtensions ipsCoreExtensions = spy(originalIpsCoreExtensions);
            ipsPlugin.setIpsCoreExtensions(ipsCoreExtensions);

            INewProductDefinitionOperationParticipant testParticipant1 = mock(
                    INewProductDefinitionOperationParticipant.class);
            INewProductDefinitionOperationParticipant testParticipant2 = mock(
                    INewProductDefinitionOperationParticipant.class);
            doReturn(Arrays.asList(testParticipant1, testParticipant2)).when(ipsCoreExtensions)
                    .getNewProductDefinitionOperationParticipants();

            TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
            pmo.setIpsProject(ipsProject);
            pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

            TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
            operation.run(monitor);

            verify(testParticipant1).finishIpsSrcFile(any(IIpsSrcFile.class), any(IProgressMonitor.class));
            verify(testParticipant2).finishIpsSrcFile(any(IIpsSrcFile.class), any(IProgressMonitor.class));
        } finally {
            ipsPlugin.setIpsCoreExtensions(originalIpsCoreExtensions);
        }
    }

    private static class TestProductDefinitionPMO extends NewProductDefinitionPMO {

        @Override
        protected NewProductDefinitionValidator getValidator() {
            return mock(NewProductDefinitionValidator.class);
        }

        @Override
        public String getName() {
            return "TestName";
        }

        @Override
        public IpsObjectType getIpsObjectType() {
            return IpsObjectType.PRODUCT_CMPT;
        }

    }

    private static class TestProductDefinitionOperation
            extends NewProductDefinitionOperation<TestProductDefinitionPMO> {

        private boolean finishIpsSrcFileCalled;

        private boolean postProcessCalled;

        public TestProductDefinitionOperation(TestProductDefinitionPMO pmo) {
            super(pmo);
        }

        @Override
        protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
            finishIpsSrcFileCalled = true;
            // simulate doing some stuff
            ipsSrcFile.markAsDirty();
        }

        @Override
        protected void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
            postProcessCalled = true;
        }

    }

}
