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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.abstracttest.TestConfigurationElement;
import org.faktorips.abstracttest.TestExtensionRegistry;
import org.faktorips.abstracttest.TestMockingUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.INewProductDefinitionOperationParticipant;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        ipsProject = newIpsProject();
        singletonMockHelper = new SingletonMockHelper();
    }

    @Override
    @After
    public void tearDown() {
        singletonMockHelper.reset();
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
    public void testRun_CreateIpsSrcFileOfCorrectNameAndType() throws CoreException, InvocationTargetException,
            InterruptedException {

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
    public void testRun_IpsSrcFileShouldBeClean() throws CoreException, InvocationTargetException, InterruptedException {
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
        ipsPlugin = spy(ipsPlugin);
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);

        INewProductDefinitionOperationParticipant testParticipant1 = mock(INewProductDefinitionOperationParticipant.class);
        INewProductDefinitionOperationParticipant testParticipant2 = mock(INewProductDefinitionOperationParticipant.class);
        mockNewProductDefinitionParticipants(ipsPlugin, testParticipant1, testParticipant2);

        TestProductDefinitionPMO pmo = new TestProductDefinitionPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment());

        TestProductDefinitionOperation operation = new TestProductDefinitionOperation(pmo);
        operation.run(monitor);

        verify(testParticipant1).finishIpsSrcFile(any(IIpsSrcFile.class), any(IProgressMonitor.class));
        verify(testParticipant2).finishIpsSrcFile(any(IIpsSrcFile.class), any(IProgressMonitor.class));
    }

    private void mockNewProductDefinitionParticipants(IpsPlugin ipsPlugin,
            INewProductDefinitionOperationParticipant... testParticipants) {

        IExtension[] extensions = new IExtension[testParticipants.length];
        for (int i = 0; i < testParticipants.length; i++) {
            Map<String, Object> executableExtensionMap = new HashMap<String, Object>();
            executableExtensionMap.put("class", testParticipants[i]);
            IExtension extension = TestMockingUtils.mockExtension("TestParticipant", new TestConfigurationElement(
                    INewProductDefinitionOperationParticipant.CONFIG_ELEMENT_ID_PARTICIPANT,
                    new HashMap<String, String>(), null, new IConfigurationElement[0], executableExtensionMap));
            extensions[i] = extension;
        }
        IExtensionPoint extensionPoint = TestMockingUtils.mockExtensionPoint(IpsPlugin.PLUGIN_ID,
                INewProductDefinitionOperationParticipant.EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION,
                extensions);
        TestExtensionRegistry extensionRegistry = new TestExtensionRegistry(new IExtensionPoint[] { extensionPoint });
        doReturn(extensionRegistry).when(ipsPlugin).getExtensionRegistry();
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

    private static class TestProductDefinitionOperation extends NewProductDefinitionOperation<TestProductDefinitionPMO> {

        private boolean finishIpsSrcFileCalled;

        private boolean postProcessCalled;

        public TestProductDefinitionOperation(TestProductDefinitionPMO pmo) {
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
