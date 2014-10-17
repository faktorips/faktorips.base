/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptXMLBuilderTest extends AbstractStdBuilderTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmpt refTarget;

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

        IProductCmptTypeMethod staticMethod = productCmptType.newProductCmptTypeMethod();
        staticMethod.setDatatype(Datatype.INTEGER.getQualifiedName());
        staticMethod.setName("staticAge");
        staticMethod.setFormulaSignatureDefinition(true);
        staticMethod.setFormulaName("StaticAgeCalculation");
        staticMethod.setChangingOverTime(false);

        assertTrue(productCmptType.isValid(ipsProject));

        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setTargetRoleSingular("role");
        association.setTargetRolePlural("roles");
        association.setTarget(productCmptType.getQualifiedName());

        IProductCmptTypeAssociation staticAssociation = productCmptType.newProductCmptTypeAssociation();
        staticAssociation.setTargetRoleSingular("staticRole");
        staticAssociation.setTargetRolePlural("staticRoles");
        staticAssociation.setChangingOverTime(false);
        staticAssociation.setTarget(productCmptType.getQualifiedName());

        productCmptType.getIpsSrcFile().save(true, null);

        productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);
        gen.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IFormula formula = gen.newFormula();
        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("42");

        IFormula staticFormula = (IFormula)productCmpt.newPropertyValue(staticMethod);
        staticFormula.setExpression("42");

        refTarget = newProductCmpt(productCmptType, "RefProduct");
        refTarget.newGeneration(gen.getValidFrom());
        refTarget.setRuntimeId("RefProductRuntimeId");

        IProductCmptLink link = gen.newLink("role");
        link.setTarget(refTarget.getQualifiedName());
        IProductCmptLink staticLink = productCmpt.newLink("staticRole");
        staticLink.setTarget(refTarget.getQualifiedName());

        productCmpt.getIpsSrcFile().save(true, null);
        refTarget.getIpsSrcFile().save(true, null);

        assertFalse(productCmpt.validate(productCmpt.getIpsProject()).containsErrorMsg());
    }

    /**
     * Test if a runtime id change will be correctly updated in the product component which
     * referenced the product cmpt on which the runtime id was changed.
     * 
     */
    @Test
    public void testRuntimeIdDependency() throws CoreException, IOException, SAXException, ParserConfigurationException {
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

        IFile file = getXmlFile(productCmptC);
        assertTargetRuntimeID(file, "newRuntimeId", true);
    }

    private IFile getXmlFile(IProductCmpt productCmpt) {
        String packageName = ((DefaultBuilderSet)ipsProject.getIpsArtefactBuilderSet()).getPackageName(
                productCmpt.getIpsSrcFile(), true, false);
        String productXmlFile = packageName + "." + productCmpt.getName();
        productXmlFile = productXmlFile.replaceAll("\\.", "/");
        productXmlFile += ".xml";
        IFile file = ipsProject.getProject().getFile("bin//" + productXmlFile);
        return file;
    }

    @Test
    public void testSetRuntimeIdForStaticLinks() throws CoreException, IOException, SAXException,
            ParserConfigurationException {
        incrementalBuild();
        IFile xmlFile = getXmlFile(productCmpt);
        assertTargetRuntimeID(xmlFile, refTarget.getRuntimeId(), false);

        refTarget.setRuntimeId("CornelisMussDasCodeReviewMachen_hahahahahaaa!=)");
        refTarget.getIpsSrcFile().save(true, null);
        incrementalBuild();

        assertTargetRuntimeID(xmlFile, refTarget.getRuntimeId(), false);
    }

    private void assertTargetRuntimeID(IFile file, String expectedRuntimeId, boolean changingLinks)
            throws SAXException, IOException, ParserConfigurationException, CoreException {
        Document document = getDocumentBuilder().parse(file.getContents());
        Element prodCmptElement = document.getDocumentElement();
        List<Element> linkElements;
        if (changingLinks) {
            List<Element> generationElements = getChildElementsByTagName(prodCmptElement,
                    IProductCmptGeneration.TAG_NAME);
            linkElements = getChildElementsByTagName(generationElements.get(0), IProductCmptLink.TAG_NAME);
        } else {
            linkElements = getChildElementsByTagName(prodCmptElement, IProductCmptLink.TAG_NAME);
        }
        assertEquals(1, linkElements.size());
        assertTrue(linkElements.get(0).hasAttribute("targetRuntimeId"));
        assertEquals(expectedRuntimeId, linkElements.get(0).getAttribute("targetRuntimeId"));
    }

    private List<Element> getChildElementsByTagName(Element prodCmptElement, String tagName) {
        List<Element> linkElements = new ArrayList<Element>();
        NodeList nodeList = prodCmptElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && ((Element)node).getNodeName().equals(tagName)) {
                linkElements.add((Element)nodeList.item(i));
            }
        }
        return linkElements;
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
