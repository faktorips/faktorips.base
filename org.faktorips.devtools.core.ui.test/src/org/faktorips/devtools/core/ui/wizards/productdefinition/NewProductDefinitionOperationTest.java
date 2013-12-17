/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewProductDefinitionOperationTest extends AbstractIpsPluginTest {

    @Mock
    private IProgressMonitor monitor;

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        ipsProject = newIpsProject();
    }

    @Test
    public void testRun_CreateIpsPackageFragmentIfNonExistent() throws InvocationTargetException, InterruptedException {
        NewProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getIpsPackageFragment("notExistent"));

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertTrue(ipsProject.getIpsPackageFragmentRoots()[0].getIpsPackageFragment("notExistent").exists());
    }

    @Test
    public void testRun_CreateIpsSrcFileOfCorrectNameAndType() throws CoreException, InvocationTargetException,
            InterruptedException {

        NewProductDefinitionPMO pmo = new TestProductDefinitionPMO();
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

        NewProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertTrue(operation.finishIpsSrcFileCalled);
    }

    @Test
    public void testRun_IpsSrcFileShouldBeClean() throws CoreException, InvocationTargetException, InterruptedException {
        NewProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertFalse(ipsProject.findIpsSrcFile(pmo.getIpsObjectType(), pmo.getName()).isDirty());
    }

    @Test
    public void testRun_CallPostProcessSubclassImplementation() throws InvocationTargetException, InterruptedException {
        NewProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        assertTrue(operation.postProcessCalled);
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

    private static class TestProductDefinitionOperation extends NewProductDefinitionOperation {

        private boolean finishIpsSrcFileCalled;

        private boolean postProcessCalled;

        public TestProductDefinitionOperation(NewProductDefinitionPMO pmo) {
            super(pmo);
        }

        @Override
        protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
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
