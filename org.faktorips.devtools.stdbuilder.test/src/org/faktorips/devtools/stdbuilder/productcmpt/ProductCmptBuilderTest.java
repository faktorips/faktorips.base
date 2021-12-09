/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptBuilderTest extends AbstractStdBuilderTest {

    private IPolicyCmptType type;
    private IProductCmptType productCmptType;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration productCmptGen;

    private ProductCmptBuilder builder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        type = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");

        productCmptType = type.findProductCmptType(ipsProject);
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setName("age");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("AgeCalculation");

        IProductCmptTypeMethod staticMethod = productCmptType.newProductCmptTypeMethod();
        staticMethod.setDatatype(Datatype.INTEGER.getQualifiedName());
        staticMethod.setName("staticAge");
        staticMethod.setFormulaSignatureDefinition(true);
        staticMethod.setFormulaName("StaticAgeCalculation");
        staticMethod.setChangingOverTime(false);

        assertFalse(type.validate(ipsProject).containsErrorMsg());
        type.getIpsSrcFile().save(true, null);

        productCmpt = newProductCmpt(productCmptType, "Product");
        productCmptGen = productCmpt.getLatestProductCmptGeneration();
        productCmptGen.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IFormula ce = productCmptGen.newFormula();
        ce.setFormulaSignature(method.getFormulaName());
        ce.setExpression("42");
        IFormula staticFormula = productCmpt.newPropertyValue(staticMethod, IFormula.class);
        staticFormula.setFormulaSignature("StaticAgeCalculation");
        staticFormula.setExpression("42");
        productCmpt.getIpsSrcFile().save(true, null);
        assertFalse(productCmpt.validate(ipsProject).containsErrorMsg());

        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder2 : builders) {
            if (builder2 instanceof ProductCmptBuilder) {
                builder = (ProductCmptBuilder)builder2;
            }
        }
        assertNotNull(builder);
    }

    @Test
    public void testBuild_buidJavaFileIntoDerivedFolder() throws CoreRuntimeException {
        // build should not throw an exception even if the reference to the type is missing
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IFile generatedProductCmptJavaFile = createExpectedProductCmptFileFromDerivedFolder();
        IFile generatedProductCmptGenerationJavaFile = createExpectedProductCmptGenerationFileFromDerivedFolder();
        assertTrue(generatedProductCmptJavaFile.exists());
        assertTrue(generatedProductCmptGenerationJavaFile.exists());
    }

    @Test
    public void testBuild_buildJavaFileIntoSrcFolder() throws CoreRuntimeException {
        // build should not throw an exception even if the reference to the type is missing
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IFile generatedProductCmptJavaFile = createExpectedProductCmptFileFromSrcFolder();
        IFile generatedProductCmptGenerationJavaFile = createExpectedProductCmptGenerationFileFromSrcFolder();
        assertTrue(generatedProductCmptJavaFile.exists());
        assertTrue(generatedProductCmptGenerationJavaFile.exists());
    }

    @Test
    public void testBuildMissingType() throws CoreRuntimeException {
        productCmpt.setProductCmptType("");
        productCmpt.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IFile generatedProductCmptGenerationJavaFile = createExpectedProductCmptGenerationFileFromDerivedFolder();
        IFile generatedProductCmptJavaFile = createExpectedProductCmptFileFromDerivedFolder();
        assertFalse(generatedProductCmptGenerationJavaFile.exists());
        assertFalse(generatedProductCmptJavaFile.exists());
    }

    @Test
    public void testDelete_deleteJavaFileFromDerivedFolder() throws CoreRuntimeException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IFile generatedProductCmptGenerationJavaFile = createExpectedProductCmptGenerationFileFromDerivedFolder();
        IFile generatedProductCmptJavaFile = createExpectedProductCmptFileFromDerivedFolder();
        assertTrue(generatedProductCmptGenerationJavaFile.exists());
        assertTrue(generatedProductCmptJavaFile.exists());

        productCmpt.delete();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertFalse(generatedProductCmptGenerationJavaFile.exists());
        assertFalse(generatedProductCmptJavaFile.exists());
    }

    @Test
    public void testDelete_deleteJavaFileFromSrcFolder() throws CoreRuntimeException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IFile generatedProductCmptJavaFile = createExpectedProductCmptFileFromSrcFolder();
        IFile generatedProductCmptGenerationJavaFile = createExpectedProductCmptGenerationFileFromSrcFolder();
        assertTrue(generatedProductCmptJavaFile.exists());
        assertTrue(generatedProductCmptGenerationJavaFile.exists());

        productCmptType.delete();
        ipsProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertFalse(generatedProductCmptJavaFile.exists());
        assertFalse(generatedProductCmptGenerationJavaFile.exists());
    }

    private IFile createExpectedProductCmptGenerationFileFromDerivedFolder() {
        String path = "/" + ipsProject.getName()
                + "/extension/org/faktorips/sample/model/internal/Product___20060101.java";
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

    private IFile createExpectedProductCmptFileFromDerivedFolder() {
        String path = "/" + ipsProject.getName() + "/extension/org/faktorips/sample/model/internal/Product.java";
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

    private IFile createExpectedProductCmptGenerationFileFromSrcFolder() {
        String path = "/" + ipsProject.getName() + "/src/org/faktorips/sample/model/internal/ProductTypeGen.java";
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

    private IFile createExpectedProductCmptFileFromSrcFolder() {
        String path = "/" + ipsProject.getName() + "/src/org/faktorips/sample/model/internal/ProductType.java";
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

    @Test
    public void testIsContainingAvailableFormula_ProductCmpt() {
        assertTrue(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testIsContainingAvailableFormula_ProductCmptGeneration() {
        assertTrue(builder.isContainingAvailableFormula(productCmptGen));
    }

    @Test
    public void testIsContainingAvailableFormula_ProductCmptNoFormula() throws CoreRuntimeException {
        createSetupWithNoFormula();

        assertFalse(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testIsContainingAvailableFormula_ProductCmptGenerationNoFormula() throws CoreRuntimeException {
        createSetupWithNoFormula();

        assertFalse(builder.isContainingAvailableFormula(productCmptGen));
    }

    @Test
    public void testGetImplementationClass_ProductCmpt() {
        String expectedImplClass = "org.faktorips.sample.model.internal.Product";
        assertEquals(expectedImplClass, builder.getImplementationClass(productCmpt));
    }

    @Test
    public void testGetImplementationClass_ProductCmptGeneration() {
        String expectedImplClass = "org.faktorips.sample.model.internal.Product___20060101";
        assertEquals(expectedImplClass, builder.getImplementationClass(productCmptGen));
    }

    @Test
    public void testGetImplementationClass_ProductCmptNoFormula() throws CoreRuntimeException {
        createSetupWithNoFormula();

        String expectedImplClass = "org.faktorips.sample.model.internal.ProductType";
        assertEquals(expectedImplClass, builder.getImplementationClass(productCmpt));
    }

    @Test
    public void testGetImplementationClass_ProductCmptGenerationNoFormula() throws CoreRuntimeException {
        createSetupWithNoFormula();

        String expectedImplClass = "org.faktorips.sample.model.internal.ProductTypeGen";
        assertEquals(expectedImplClass, builder.getImplementationClass(productCmptGen));
    }

    private void createSetupWithNoFormula() throws CoreRuntimeException {
        IIpsProject ipsProject2 = newIpsProject();
        type = newPolicyAndProductCmptType(ipsProject2, "PolicyType", "ProductType");
        productCmptType = type.findProductCmptType(ipsProject2);
        productCmpt = newProductCmpt(productCmptType, "Product");
        productCmptGen = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGen.setValidFrom(new GregorianCalendar(2006, 0, 1));

        IIpsArtefactBuilder[] builders = ipsProject2.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder2 : builders) {
            if (builder2 instanceof ProductCmptBuilder) {
                builder = (ProductCmptBuilder)builder2;
            }
        }
        assertNotNull(builder);
    }

}
