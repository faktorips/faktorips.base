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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
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
    private IProductCmptLink link;
    private IProductCmptLink staticLink;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);

        IProductCmptTypeAttribute internationalStringAttribute = productCmptType.newProductCmptTypeAttribute("i18n");
        internationalStringAttribute.setMultilingual(true);
        internationalStringAttribute.setDatatype(Datatype.STRING.getQualifiedName());

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

        productCmptType.getIpsSrcFile().save(null);

        productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);
        gen.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IFormula formula = gen.newFormula();
        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("42");

        IFormula staticFormula = productCmpt.newPropertyValue(staticMethod, IFormula.class);
        staticFormula.setExpression("42");

        refTarget = newProductCmpt(productCmptType, "RefProduct");
        refTarget.newGeneration(gen.getValidFrom());
        refTarget.setRuntimeId("RefProductRuntimeId");

        link = gen.newLink("role");
        link.setTarget(refTarget.getQualifiedName());
        staticLink = productCmpt.newLink("staticRole");
        staticLink.setTarget(refTarget.getQualifiedName());

        gen.newAttributeValue(internationalStringAttribute);

        productCmpt.getIpsSrcFile().save(null);
        refTarget.getIpsSrcFile().save(null);

        assertFalse(productCmpt.validate(productCmpt.getIpsProject()).containsErrorMsg());
    }

    /**
     * Test if a runtime id change will be correctly updated in the product component which
     * referenced the product cmpt on which the runtime id was changed.
     */
    @Test
    public void testRuntimeIdDependency()
            throws CoreException, IOException, SAXException, ParserConfigurationException {
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IProductCmptType c = newProductCmptType(root, "C");
        IProductCmptType d = newProductCmptType(root, "D");

        IProductCmptTypeAssociation association = c.newProductCmptTypeAssociation();
        association.setTargetRoleSingular("relationD");
        association.setTarget(d.getQualifiedName());
        c.getIpsSrcFile().save(null);
        IProductCmpt productCmptC = newProductCmpt(c, "tests.productC");

        IProductCmpt productCmptD = newProductCmpt(d, "tests.productD");

        IProductCmptGeneration generation1 = productCmptC.getProductCmptGeneration(0);
        IProductCmptLink link = generation1.newLink("relationD");
        link.setTarget(productCmptD.getQualifiedName());
        productCmptC.getIpsSrcFile().save(null);

        incrementalBuild();

        // product cmpt C depends on product D
        // change the runtime id of product D and assert that the target runtime id in product C
        // was updated after rebuild
        productCmptD.setRuntimeId("newRuntimeId");
        productCmptD.getIpsSrcFile().save(null);
        // since 23.6, TargetRuntimeID is persistent and can be fixed automatically
        productCmptC.fixAllDifferencesToModel(ipsProject);
        productCmptC.getIpsSrcFile().save(null);

        incrementalBuild();

        AFile file = getXmlFile(productCmptC);
        assertTargetRuntimeID(file, "newRuntimeId", true);
    }

    private AFile getXmlFile(IProductCmpt productCmpt) {
        String packageName = ((DefaultBuilderSet)ipsProject.getIpsArtefactBuilderSet()).getPackageName(
                productCmpt.getIpsSrcFile(), true, false);
        String productXmlFile = packageName + "." + productCmpt.getName();
        productXmlFile = productXmlFile.replace('.', '/');
        productXmlFile += ".ipsproduct";
        return ipsProject.getProject().getFile("bin/" + productXmlFile);
    }

    @Test
    public void testSetRuntimeIdForStaticLinks() throws CoreException, IOException, SAXException,
            ParserConfigurationException {
        incrementalBuild();
        AFile xmlFile = getXmlFile(productCmpt);
        assertTargetRuntimeID(xmlFile, refTarget.getRuntimeId(), false);

        refTarget.setRuntimeId("CornelisMussDasCodeReviewMachen_hahahahahaaa!=)");
        refTarget.getIpsSrcFile().save(null);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        productCmpt.getIpsSrcFile().save(null);
        incrementalBuild();

        assertTargetRuntimeID(xmlFile, refTarget.getRuntimeId(), false);
    }

    private void assertTargetRuntimeID(AFile file, String expectedRuntimeId, boolean changingLinks)
            throws SAXException, IOException, ParserConfigurationException {
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

    private static List<Element> getChildElementsByTagName(Element prodCmptElement, String tagName) {
        List<Element> linkElements = new ArrayList<>();
        NodeList nodeList = prodCmptElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && node.getNodeName().equals(tagName)) {
                linkElements.add((Element)nodeList.item(i));
            }
        }
        return linkElements;
    }

    @Test
    public void testBuild() {
        ipsProject.getProject().build(ABuildKind.FULL, null);
    }

    @Test
    public void testDelete() {
        ipsProject.getProject().build(ABuildKind.FULL, null);

        productCmpt.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(ABuildKind.FULL, null);
    }

    @Test
    public void testBuild_SetsDefaultLocaleInInternationalString() throws CoreException, IOException, SAXException,
            ParserConfigurationException {
        // Precondition
        Locale defaultLocale = ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
        assertThat(defaultLocale, is(notNullValue()));

        incrementalBuild();

        Element root = parseProductCmptElement();
        NodeList internationalStrings = root.getElementsByTagName(IInternationalString.XML_TAG);
        assertThat(internationalStrings.getLength(), is(1));
        Element internationalString = (Element)internationalStrings.item(0);
        assertThat(internationalString.getAttribute(IInternationalString.XML_ATTR_DEFAULT_LOCALE),
                is(defaultLocale.getLanguage()));
    }

    /**
     * Tests a {@link Locale} that is very likely different from the one derived e.g. from system
     * properties.
     */
    @Test
    public void testBuild_SetsDefaultLocaleInInternationalStringExoticDefaultLocale() throws CoreException,
            IOException, SAXException, ParserConfigurationException {
        // Precondition
        Locale defaultLocale = Locale.KOREAN;
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(defaultLocale);
        properties.setDefaultLanguage(defaultLocale);
        ipsProject.setProperties(properties);
        assertThat(defaultLocale, is(notNullValue()));

        productCmpt.fixAllDifferencesToModel(ipsProject);
        productCmpt.getIpsSrcFile().save(null);
        incrementalBuild();

        Element root = parseProductCmptElement();
        NodeList internationalStrings = root.getElementsByTagName(IInternationalString.XML_TAG);
        assertThat(internationalStrings.getLength(), is(1));
        Element internationalString = (Element)internationalStrings.item(0);
        assertThat(internationalString.getAttribute(IInternationalString.XML_ATTR_DEFAULT_LOCALE),
                is(defaultLocale.getLanguage()));
    }

    private Element parseProductCmptElement() throws SAXException, IOException, ParserConfigurationException {
        AFile xmlFile = getXmlFile(productCmpt);
        Document document = getDocumentBuilder().parse(xmlFile.getContents());
        return document.getDocumentElement();
    }

}
