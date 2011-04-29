/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptXMLBuilderTest extends AbstractStdBuilderTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);

        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setName("age");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("AgeCalculation");

        assertTrue(productCmptType.isValid(ipsProject));

        IProductCmptTypeAssociation rel = productCmptType.newProductCmptTypeAssociation();
        rel.setTargetRoleSingular("role");
        rel.setTargetRolePlural("roles");
        rel.setTarget(productCmptType.getQualifiedName());

        productCmptType.getIpsSrcFile().save(true, null);

        productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);
        gen.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IFormula ce = gen.newFormula();
        ce.setFormulaSignature(method.getFormulaName());
        ce.setExpression("42");

        IProductCmpt refTarget = newProductCmpt(productCmptType, "RefProduct");
        refTarget.newGeneration(gen.getValidFrom());
        refTarget.setRuntimeId("RefProductRuntimeId");

        IProductCmptLink link = gen.newLink("role");
        link.setTarget(refTarget.getQualifiedName());

        productCmpt.getIpsSrcFile().save(true, null);
        refTarget.getIpsSrcFile().save(true, null);

        assertFalse(productCmpt.validate(productCmpt.getIpsProject()).containsErrorMsg());
    }

    /**
     * Test if a runtime id change will be correctly updated in the product component which
     * referenced the product cmpt on which the runtime id was changed.
     */
    // FIXME AW: Core test expecting standard builder set
    @Test
    public void testRuntimeIdDependency() throws CoreException, IOException {
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmptType c = newProductCmptType(root, "C");
        IProductCmptType d = newProductCmptType(root, "D");

        org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation association = c
                .newProductCmptTypeAssociation();
        association.setTargetRoleSingular("relationD");
        association.setTarget(d.getQualifiedName());
        IProductCmpt productCmptC = newProductCmpt(c, "tests.productC");

        IProductCmpt productCmptD = newProductCmpt(d, "tests.productD");

        IProductCmptGeneration generation1 = productCmptC.getProductCmptGeneration(0);
        IProductCmptLink link = generation1.newLink("linkCtoD");
        link.setTarget(productCmptD.getQualifiedName());

        incrementalBuild();

        // product cmpt C depends on product D
        // change the runtime id of product D and assert that the target runtime id in product C
        // was updated after rebuild
        productCmptD.setRuntimeId("newRuntimeId");
        productCmptD.getIpsSrcFile().save(true, null);

        incrementalBuild();

        // check if the target runtime id was updated in product cmpt c runtime xml
        String packageOfProductC = ((DefaultBuilderSet)ipsProject.getIpsArtefactBuilderSet()).getPackage(
                DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION, productCmptC.getIpsSrcFile());
        String productCXmlFile = packageOfProductC + "." + "productC";
        productCXmlFile = productCXmlFile.replaceAll("\\.", "/");
        productCXmlFile += ".xml";
        IFile file = ipsProject.getProject().getFile("bin//" + productCXmlFile);
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = file.getContents();
            if (is == null) {
                fail("Can't find resource " + productCXmlFile);
            }
            StringBuffer generatedXml = new StringBuffer();
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                generatedXml.append(line);
            }
            String patternStr = ".*targetRuntimeId=\"([^\"]*)\".*";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(generatedXml);
            assertTrue(matcher.find());
            assertEquals("newRuntimeId", matcher.group(matcher.groupCount()));
        } finally {
            if (is != null) {
                is.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }

    @Test
    public void testBuild() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    @Test
    public void testDelete() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        productCmpt.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
