/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptGenerationClassBuilderTest extends AbstractStdBuilderTest {

    private ProductCmptGenerationClassBuilder productCmptGenerationClassBuilder;

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration productCmptGen;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        policyCmptType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");

        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        policyCmptType.getIpsSrcFile().save(null);

        productCmpt = newProductCmpt(productCmptType, "Product");
        productCmptGen = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGen.setValidFrom(new GregorianCalendar(2006, 0, 1));

        productCmpt.getIpsSrcFile().save(null);
        assertFalse(productCmpt.validate(ipsProject).containsErrorMsg());

        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder : builders) {
            if (builder instanceof ProductCmptGenerationClassBuilder) {
                productCmptGenerationClassBuilder = (ProductCmptGenerationClassBuilder)builder;
            }
        }
        assertNotNull(productCmptGenerationClassBuilder);
    }

    @Test
    public void testBuild_buildJavaFileIntoSrcFolder() {
        // build should not throw an exception even if the reference to the policyCmptType is
        // missing
        ipsProject.getProject().build(ABuildKind.FULL, null);
    }

    @Test
    public void testIsBuilderFor_falseIf_SrcFileIsNotProductCmptType() {
        assertFalse(productCmptGenerationClassBuilder.isBuilderFor(productCmpt.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ProductCmptTypeDoesNotExist() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        IIpsSrcFile ipsSrcFile = productCmptType.getIpsSrcFile();
        productCmptType.delete();

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(ipsSrcFile));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_NotChangingOverTime_ProductCmptGenerationSrcFileExist() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        productCmptType.setChangingOverTime(false);

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_falseIf_ProductCmptType_NotChangingOverTime_ProductCmptGenerationSrcFileDoesNotExist_ProductCmptTypeSrcFileExist()
            throws CoreException {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        createProductCmptGenerationFileFromSrcFolder().delete(true, null);
        productCmptType.setChangingOverTime(false);

        assertFalse(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ChangingOverTime_ProductCmptGenerationSrcFileExist_ProductCmptTypeSrcFileExists() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        productCmptType.setChangingOverTime(true);

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ChangingOverTime_ProductCmptGenerationSrcFileDoesNotExist_ProductCmptTypeSrcFileExists()
            throws CoreException {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        createProductCmptGenerationFileFromSrcFolder().delete(true, null);
        productCmptType.setChangingOverTime(true);

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsGeneratingArtifactsFor_trueIf_PolicyCmptType_ChangingOverTime() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        productCmptType.setChangingOverTime(true);

        assertTrue(productCmptGenerationClassBuilder.isGeneratingArtifactsFor(policyCmptType.getIpsObject()));
    }

    @Test
    public void testIsGeneratingArtifactsFor_falseIf_PolicyCmptType_NotChangingOverTime() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
        productCmptType.setChangingOverTime(false);

        assertFalse(productCmptGenerationClassBuilder.isGeneratingArtifactsFor(policyCmptType.getIpsObject()));
    }

    private IFile createProductCmptGenerationFileFromSrcFolder() {
        String path = "/" + ipsProject.getName() + "/src/org/faktorips/sample/model/internal/ProductTypeGen.java";
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

}
