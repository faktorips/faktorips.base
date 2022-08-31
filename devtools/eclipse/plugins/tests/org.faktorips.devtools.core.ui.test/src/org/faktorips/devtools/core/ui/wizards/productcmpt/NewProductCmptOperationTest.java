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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class NewProductCmptOperationTest extends AbstractIpsPluginTest {

    @Mock
    private IProgressMonitor monitor;

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @Test
    public void testRun_SetProductCmptProperties() throws IpsException, InvocationTargetException,
            InterruptedException {

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmpt template = newProductTemplate(ipsProject, "template");
        createProductCmptTypeAttribute(productCmptType, "testAttribute", Datatype.STRING, "");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(productCmptType);
        pmo.setSelectedTemplate(new ProductCmptViewItem(template.getIpsSrcFile()));

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        assertEquals("testRuntimeId", newProductCmpt.getRuntimeId());
        assertEquals("TestProductCmptType", newProductCmpt.getProductCmptType());
        assertEquals("template", newProductCmpt.getTemplate());
    }

    @Test
    public void testRun_SetGenerationPropertiesIfNotInCopyMode() throws IpsException, InvocationTargetException,
            InterruptedException {

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        createProductCmptTypeAttribute(productCmptType, "testAttribute", Datatype.STRING, "");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(productCmptType);
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        IProductCmptGeneration newProductCmptGeneration = newProductCmpt.getFirstGeneration();
        assertEquals(new GregorianCalendar(2013, 0, 1), newProductCmptGeneration.getValidFrom());
    }

    @Test
    public void testRun_CreatePropertyValuesWithDefaultsFromModelIfNotInCopyMode() throws IpsException,
            InvocationTargetException, InterruptedException {

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicyCmptType",
                "TestProductCmptType");
        createPolicyCmptTypeAttribute(policyCmptType, "testPolicyAttribute", Datatype.STRING, "defaultPolicyValue");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        createProductCmptTypeAttribute(productCmptType, "testProductAttribute", Datatype.STRING, "defaultProductValue");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(productCmptType);
        pmo.setEffectiveDate(new GregorianCalendar(2013, 0, 1));

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        IProductCmptGeneration newProductCmptGeneration = newProductCmpt.getFirstGeneration();
        assertEquals("defaultPolicyValue", newProductCmptGeneration.getConfiguredDefault("testPolicyAttribute")
                .getValue());
        assertEquals("defaultProductValue", newProductCmptGeneration.getAttributeValue("testProductAttribute")
                .getValueHolder().getStringValue());
    }

    @Test
    public void testRun_AddLinkToProductCmptGenerationAsConfiguredByPMO_SaveIfNotDirtyBefore() throws IpsException,
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

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        assertFalse(sourceProductCmpt.getIpsSrcFile().isDirty());
    }

    @Test
    public void testRun_AddLinkToProductCmptGenerationAsConfiguredByPMO_DoNotSaveIfDirtyBefore() throws IpsException,
            InvocationTargetException, InterruptedException {

        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, "TestTargetProductCmptType");
        IProductCmptType sourceProductCmptType = newProductCmptType(ipsProject, "TestSourceProductCmptType");
        IProductCmptTypeAssociation association = sourceProductCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductCmptType.getQualifiedName());

        IProductCmpt sourceProductCmpt = newProductCmpt(sourceProductCmptType, "SourceProductCmpt");
        sourceProductCmpt.getIpsSrcFile().markAsDirty();
        IProductCmptGeneration sourceProductCmptGeneration = sourceProductCmpt.getFirstGeneration();

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(targetProductCmptType);
        pmo.setEffectiveDate(sourceProductCmptGeneration.getValidFrom());
        pmo.setAddToAssociation(sourceProductCmptGeneration, association);

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        assertTrue(sourceProductCmpt.getIpsSrcFile().isDirty());
    }

    @Test
    public void testRun_DoNotAddLinkToProductCmptGenerationIfValidatorFails() throws IpsException,
            InvocationTargetException, InterruptedException {

        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, "TestTargetProductCmptType");
        IProductCmptType sourceProductCmptType = newProductCmptType(ipsProject, "TestSourceProductCmptType");
        IProductCmptTypeAssociation association = sourceProductCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductCmptType.getQualifiedName());

        IProductCmpt sourceProductCmpt = newProductCmpt(sourceProductCmptType, "SourceProductCmpt");
        IProductCmptGeneration sourceProductCmptGeneration = sourceProductCmpt.getFirstGeneration();

        NewProductCmptPMO pmo = new NewProductCmptPMO() {
            @Override
            protected NewProductCmptValidator getValidator() {
                MessageList validationMessages = new MessageList(new Message("CODE", "text", Message.ERROR));
                NewProductCmptValidator validator = mock(NewProductCmptValidator.class);
                when(validator.validateAddToGeneration()).thenReturn(validationMessages);
                return validator;
            }
        };
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setAddToAssociation(sourceProductCmptGeneration, association);

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        assertEquals(0, sourceProductCmptGeneration.getNumOfLinks());
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(IPolicyCmptType policyCmptType,
            String name,
            Datatype datatype,
            String defaultValue) {

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute(name);
        policyCmptTypeAttribute.setDatatype(datatype.getQualifiedName());
        policyCmptTypeAttribute.setDefaultValue(defaultValue);
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        return policyCmptTypeAttribute;
    }

    private IProductCmptTypeAttribute createProductCmptTypeAttribute(IProductCmptType productCmptType,
            String name,
            Datatype datatype,
            String defaultValue) {

        IProductCmptTypeAttribute productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute(name);
        productCmptTypeAttribute.setDatatype(datatype.getQualifiedName());
        productCmptTypeAttribute.setDefaultValue(defaultValue);
        return productCmptTypeAttribute;
    }

    private IIpsPackageFragment getDefaultIpsPackageFragment() {
        return ipsProject.getIpsPackageFragmentRoots()[0].getDefaultIpsPackageFragment();
    }

}
