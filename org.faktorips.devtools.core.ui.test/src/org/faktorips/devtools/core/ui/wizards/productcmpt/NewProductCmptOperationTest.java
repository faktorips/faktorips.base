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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
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
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NewProductCmptOperationTest extends AbstractIpsPluginTest {

    @Mock
    private IProgressMonitor monitor;

    private IIpsProject ipsProject;

    private SingletonMockHelper singletonMockHelper;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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

        IIpsSrcFile newSrcFile = getDefaultIpsPackageFragment().getIpsSrcFile(pmo.getName(), pmo.getIpsObjectType());
        IProductCmpt newProductCmpt = (IProductCmpt)newSrcFile.getIpsObject();
        IProductCmptLink newProductCmptLink = sourceProductCmptGeneration.getLinks(association.getName())[0];
        assertEquals(newProductCmpt.getQualifiedName(), newProductCmptLink.getTarget());
    }

    @Test
    public void testRun_AddLinkToProductCmptGenerationAsConfiguredByPMO_SaveIfNotDirtyBefore() throws CoreException,
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
    public void testRun_AddLinkToProductCmptGenerationAsConfiguredByPMO_DoNotSaveIfDirtyBefore() throws CoreException,
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
    public void testRun_DoNotAddLinkToProductCmptGenerationIfValidatorFails() throws CoreException,
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
