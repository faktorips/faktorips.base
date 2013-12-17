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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewProductCmptOperationTest extends AbstractIpsPluginTest {

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
    public void testRun_CopySrcFileIfCopyMode() throws InvocationTargetException, InterruptedException, CoreException {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        IProductCmptTypeAttribute attribute = createProductCmptTypeAttribute(productCmptType, "testAttribute",
                Datatype.STRING, "");
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "TestProductCmpt");
        IAttributeValue attributeValue = productCmpt.getLatestProductCmptGeneration().newAttributeValue(attribute);
        attributeValue.setValueHolder(AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue,
                ValueFactory.createStringValue("testValue")));
        IIpsPackageFragment targetIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .createPackageFragment("targetPackage", true, null);

        productCmpt.getIpsSrcFile().save(true, null);

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(targetIpsPackageFragment);
        pmo.setCopyProductCmpt(productCmpt);

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile copiedSrcFile = targetIpsPackageFragment.getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        assertTrue(copiedSrcFile.exists());
    }

    @Test
    public void testRun_SetProductCmptProperties() throws CoreException, InvocationTargetException,
            InterruptedException {

        IProductCmptType productCmptType = newProductCmptType(ipsProject, "TestProductCmptType");
        createProductCmptTypeAttribute(productCmptType, "testAttribute", Datatype.STRING, "");

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(productCmptType);

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        assertEquals("testRuntimeId", newProductCmpt.getRuntimeId());
        assertEquals("TestProductCmptType", newProductCmpt.getProductCmptType());
    }

    @Test
    public void testRun_SetGenerationPropertiesIfNotInCopyMode() throws CoreException, InvocationTargetException,
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
    public void testRun_CreatePropertyValuesWithDefaultsFromModelIfNotInCopyMode() throws CoreException,
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
        assertEquals("defaultPolicyValue", newProductCmptGeneration.getConfigElement("testPolicyAttribute").getValue());
        assertEquals("defaultProductValue", newProductCmptGeneration.getAttributeValue("testProductAttribute")
                .getValueHolder().getStringValue());
    }

    @Test
    public void testRun_AddLinkToProductCmptGenerationAsConfiguredByPMO() throws CoreException,
            InvocationTargetException, InterruptedException {

        // TODO PA-2023

        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, "TestTargetProductCmptType");
        IProductCmptType sourceProductCmptType = newProductCmptType(ipsProject, "TestSourceProductCmptType");
        IProductCmptTypeAssociation association = sourceProductCmptType.newProductCmptTypeAssociation();
        association.setTarget(targetProductCmptType.getQualifiedName());

        IProductCmpt productCmpt = newProductCmpt(sourceProductCmptType, "SourceProductCmpt");
        IProductCmptGeneration productCmptGeneration = productCmpt.getFirstGeneration();

        NewProductCmptPMO pmo = new NewProductCmptPMO();
        pmo.setIpsProject(ipsProject);
        pmo.setIpsPackage(getDefaultIpsPackageFragment());
        pmo.setRuntimeId("testRuntimeId");
        pmo.setSelectedType(targetProductCmptType);
        pmo.setEffectiveDate(productCmptGeneration.getValidFrom());

        NewProductCmptOperation operation = new NewProductCmptOperation(pmo);
        operation.run(monitor);

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        IProductCmptGeneration newProductCmptGeneration = newProductCmpt.getFirstGeneration();
        assertEquals("defaultPolicyValue", newProductCmptGeneration.getConfigElement("testPolicyAttribute").getValue());
        assertEquals("defaultProductValue", newProductCmptGeneration.getAttributeValue("testProductAttribute")
                .getValueHolder().getStringValue());
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(IPolicyCmptType policyCmptType,
            String name,
            Datatype datatype,
            String defaultValue) {

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute(name);
        policyCmptTypeAttribute.setDatatype(datatype.getQualifiedName());
        policyCmptTypeAttribute.setDefaultValue(defaultValue);
        policyCmptTypeAttribute.setProductRelevant(true);
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
