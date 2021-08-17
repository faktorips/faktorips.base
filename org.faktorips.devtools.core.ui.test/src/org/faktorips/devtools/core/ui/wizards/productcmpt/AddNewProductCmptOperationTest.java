/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddNewProductCmptOperationTest extends AbstractIpsPluginTest {

    private IProgressMonitor monitor = new NullProgressMonitor();

    private IIpsProject ipsProject;

    private SingletonMockHelper singletonMockHelper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        singletonMockHelper = new SingletonMockHelper();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        singletonMockHelper.reset();
    }

    @Test
    public void testRun_AddLinkToProductCmptGenerationAsConfiguredByPMO() throws CoreException,
            InvocationTargetException, InterruptedException {

        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, "TestTargetProductCmptType");
        IProductCmptType sourceProductCmptType = newProductCmptType(ipsProject, "TestSourceProductCmptType");
        IProductCmptTypeAssociation association = sourceProductCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductCmptType.getQualifiedName());

        IProductCmpt sourceProductCmpt = newProductCmpt(sourceProductCmptType, "SourceProductCmpt");
        IProductCmptGeneration sourceProductCmptGeneration = sourceProductCmpt.getFirstGeneration();

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(targetProductCmptType);
        pmo.setEffectiveDate(sourceProductCmptGeneration.getValidFrom());
        pmo.setAddToAssociation(sourceProductCmptGeneration, association);

        AddNewProductCmptOperation operation = new AddNewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        IProductCmptLink newProductCmptLink = sourceProductCmptGeneration.getLinks(association.getName())[0];
        assertEquals(newProductCmpt.getQualifiedName(), newProductCmptLink.getTarget());
    }

    @Test
    public void testRun_DoNotAddLinkToProductCmptGenerationIfGenerationNotEditable() throws CoreException,
            InvocationTargetException, InterruptedException {
        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, "TestTargetProductCmptType");
        IProductCmptType sourceProductCmptType = newProductCmptType(ipsProject, "TestSourceProductCmptType");
        IProductCmptTypeAssociation association = sourceProductCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductCmptType.getQualifiedName());

        IProductCmpt sourceProductCmpt = newProductCmpt(sourceProductCmptType, "SourceProductCmpt");
        IProductCmptGeneration sourceProductCmptGeneration = sourceProductCmpt.getFirstGeneration();

        IpsUIPlugin ipsUiPlugin = mock(IpsUIPlugin.class);
        when(ipsUiPlugin.isGenerationEditable(sourceProductCmptGeneration)).thenReturn(false);
        singletonMockHelper.setSingletonInstance(IpsUIPlugin.class, ipsUiPlugin);

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setAddToAssociation(sourceProductCmptGeneration, association);

        AddNewProductCmptOperation operation = new AddNewProductCmptOperation(pmo);
        operation.run(monitor);

        assertEquals(0, sourceProductCmptGeneration.getNumOfLinks());
        IpsPlugin.getDefault().getIpsPreferences().setWorkingMode(IpsPreferences.WORKING_MODE_EDIT);
    }

    private IIpsPackageFragment getDefaultIpsPackageFragment() {
        return ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment();
    }

}
