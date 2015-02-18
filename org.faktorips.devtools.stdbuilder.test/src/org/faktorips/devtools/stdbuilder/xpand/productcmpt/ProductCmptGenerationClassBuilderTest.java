/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.URL;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptGenerationClassBuilderTest extends AbstractStdBuilderTest {

    private ProductCmptGenerationClassBuilder productCmptGenerationClassBuilder;

    private IPolicyCmptType type;
    private IProductCmptType productCmptType;
    private ProductCmpt productCmpt;
    private IProductCmptGeneration productCmptGen;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        type = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");

        productCmptType = type.findProductCmptType(ipsProject);
        type.getIpsSrcFile().save(true, null);

        productCmpt = newProductCmpt(productCmptType, "Product");
        productCmptGen = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGen.setValidFrom(new GregorianCalendar(2006, 0, 1));

        productCmpt.getIpsSrcFile().save(true, null);
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
    public void testBuild_buildJavaFileIntoSrcFolder() throws CoreException {
        // build should not throw an exception even if the reference to the type is missing
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    @Test
    public void testGetTemplate_exists() {
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);

        String template = policyCmptClassBuilder.getTemplate();
        template = template.substring(0, template.lastIndexOf(SyntaxConstants.NS_DELIM));
        String templatePath = template.replaceAll(SyntaxConstants.NS_DELIM, "/") + ".xpt";
        URL resource = PolicyCmptClassBuilder.class.getClassLoader().getResource(templatePath);

        assertNotNull(resource);
    }

    @Test
    public void testIsBuilderFor_falseIf_SrcFileIsNotProductCmptType() throws CoreException {
        assertFalse(productCmptGenerationClassBuilder.isBuilderFor(productCmpt.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ProductCmptTypeDoesNotExist() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsSrcFile ipsSrcFile = productCmptType.getIpsSrcFile();
        productCmptType.delete();

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(ipsSrcFile));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_NotChangingOverTime_ProductCmptGenerationSrcFileExist()
            throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        productCmptType.setChangingOverTime(false);

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_falseIf_ProductCmptType_NotChangingOverTime_ProductCmptGenerationSrcFileDoesNotExist_ProductCmptTypeSrcFileExist()
            throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        createProductCmptGenerationFileFromSrcFolder().delete(true, null);
        productCmptType.setChangingOverTime(false);

        assertFalse(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ChangingOverTime_ProductCmptGenerationSrcFileExist_ProductCmptTypeSrcFileExists()
            throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        productCmptType.setChangingOverTime(true);

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ChangingOverTime_ProductCmptGenerationSrcFileDoesNotExist_ProductCmptTypeSrcFileExists()
            throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        createProductCmptGenerationFileFromSrcFolder().delete(true, null);
        productCmptType.setChangingOverTime(true);

        assertTrue(productCmptGenerationClassBuilder.isBuilderFor(productCmptType.getIpsSrcFile()));
    }

    private IFile createProductCmptGenerationFileFromSrcFolder() {
        String path = "/" + ipsProject.getName() + "/src/org/faktorips/sample/model/internal/ProductTypeGen.java";
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
    }

}
