/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.htmlexport.context.AbstractHtmlExportPluginTest;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.ITestRuleParameter;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;
import org.junit.Test;

public class IpsElementImagePageElementTest extends AbstractHtmlExportPluginTest {

    @Test
    public void testPathPolicyCmptType() {
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(policyCmptType);
    }

    @Test
    public void testPathProductCmptType() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmptType);
    }

    @Test
    public void testPathProductCmptTypeAssociation() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        IProductCmptTypeAssociation association = new ProductCmptTypeAssociation(productCmptType, "xxx.BVBAsso");

        assertEquals("aggregation", new IpsElementImagePageElement(association, getContext()).getFileName());
    }

    @Test
    public void testPathPolicyCmpt() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "yyy.BVB"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmpt);
    }

    @Test
    public void testPathProductCmptMitEigenemBild() {
        String productCmptTypeName = "xxx.BVB";
        IProductCmptType productCmptType = newProductCmptType(ipsProject, productCmptTypeName);
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "yyy.BVB"); //$NON-NLS-1$

        productCmptType.setInstancesIcon("icons/sample.gif");

        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmpt);

        assertEquals(productCmptTypeName, new IpsElementImagePageElement(productCmpt, getContext()).getFileName());
        assertEquals(productCmptTypeName,
                new IpsElementImagePageElement(productCmpt.getIpsSrcFile(), getContext()).getFileName());

        IProductCmptType productCmptTypeOhneEigenesBild = newProductCmptType(ipsProject, "xxx.BVBOhneEigenesBild"); //$NON-NLS-1$
        IProductCmpt productCmptOhneEigenesBild = newProductCmpt(productCmptTypeOhneEigenesBild,
                "yyy.BVBOhneEigenesBild"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmptOhneEigenesBild);

        ImageData imagePageElementOhneEigenesBild = new IpsElementImagePageElement(productCmptOhneEigenesBild,
                getContext())
                        .getImageData();
        ImageData imagePageElement = new IpsElementImagePageElement(productCmpt, getContext()).getImageData();

        assertNotNull(imagePageElement);
        assertNotNull(imagePageElementOhneEigenesBild);

        assertTrue(imagePageElement.data.length > 0);
        assertTrue(imagePageElementOhneEigenesBild.data.length > 0);

        boolean gleich = true;
        for (int x = 0; x < imagePageElement.width; x++) {
            for (int y = 0; y < imagePageElement.height; y++) {
                gleich &= imagePageElement.getPixel(x, y) == imagePageElementOhneEigenesBild.getPixel(x, y);
            }
        }
        assertFalse("Bilder stimmen ueberein!", gleich);
    }

    @Test
    public void testPathProductCmptMitEigenemBildUndCoreException() {
        String productCmptTypeName = "xxx.BVB";
        IProductCmptType productCmptType = newProductCmptType(ipsProject, productCmptTypeName);
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "yyy.BVB"); //$NON-NLS-1$

        productCmptType.setInstancesIcon("icons/sample.gif");

        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmpt);

        assertEquals(productCmptTypeName, new IpsElementImagePageElement(productCmpt, getContext()).getFileName());
        assertEquals(productCmptTypeName,
                new IpsElementImagePageElement(productCmpt.getIpsSrcFile(), getContext()).getFileName());

        IProductCmptType productCmptTypeOhneEigenesBild = newProductCmptType(ipsProject, "xxx.BVBOhneEigenesBild"); //$NON-NLS-1$
        IProductCmpt productCmptOhneEigenesBild = newProductCmpt(productCmptTypeOhneEigenesBild,
                "yyy.BVBOhneEigenesBild"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmptOhneEigenesBild);

        ImageData imagePageElementOhneEigenesBild = new IpsElementImagePageElement(productCmptOhneEigenesBild,
                getContext())
                        .getImageData();
        ImageData imagePageElement = new IpsElementImagePageElement(productCmpt, getContext()).getImageData();

        assertNotNull(imagePageElement);
        assertNotNull(imagePageElementOhneEigenesBild);

        assertTrue(imagePageElement.data.length > 0);
        assertTrue(imagePageElementOhneEigenesBild.data.length > 0);

        boolean gleich = true;
        for (int x = 0; x < imagePageElement.width; x++) {
            for (int y = 0; y < imagePageElement.height; y++) {
                gleich &= imagePageElement.getPixel(x, y) == imagePageElementOhneEigenesBild.getPixel(x, y);
            }
        }
        assertFalse("Bilder stimmen ueberein!", gleich);
    }

    @Test
    public void testPathProductCmptMitEigenemBildAusSuperProductCmpt() {
        String productCmptTypeName = "xxx.BVB";
        String superProductCmptTypeName = "xxx.SuperBVB";

        IProductCmptType productCmptType = newProductCmptType(ipsProject, productCmptTypeName);
        IProductCmptType superProductCmptType = newProductCmptType(ipsProject, superProductCmptTypeName);

        productCmptType.setSupertype(superProductCmptTypeName);

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "yyy.BVB"); //$NON-NLS-1$

        superProductCmptType.setInstancesIcon("icons/sample.gif");

        assertEquals(superProductCmptTypeName, new IpsElementImagePageElement(productCmpt, getContext()).getFileName());
    }

    @Test
    public void testPathTestObject() {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "xxx.TestCaseType");
        ITestCase testCase = newTestCase(testCaseType, "xxxTest"); //$NON-NLS-1$

        assertEquals("testrule", new IpsElementImagePageElement(testCase.newTestRule(), getContext()).getFileName());
        assertEquals("testpolicycmpt",
                new IpsElementImagePageElement(testCase.newTestPolicyCmpt(), getContext()).getFileName());
        ITestValue newTestValue = testCase.newTestValue();

        String testParameterName = "xyz.bbvbv";
        String testDatatype = "xyzDatatype";
        ITestValueParameter parameter = testCaseType.newInputTestValueParameter();

        parameter.setName(testParameterName);
        parameter.setDatatype(testDatatype);

        newTestValue.setTestValueParameter(testParameterName);

        assertEquals("testvalue", new IpsElementImagePageElement(newTestValue, getContext()).getFileName());
    }

    @Test
    public void testPathTestParameter() {
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "xxx.TestCaseType");

        String testParameterName = "xyz.bbvbv";
        String testDatatype = "xyzDatatype";
        ITestValueParameter valueParameter = testCaseType.newInputTestValueParameter();

        valueParameter.setName(testParameterName);
        valueParameter.setDatatype(testDatatype);

        assertEquals("datatype", new IpsElementImagePageElement(valueParameter, getContext()).getFileName());

        ITestPolicyCmptTypeParameter policyCmptTypeParameter = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        policyCmptTypeParameter.setDatatype(testDatatype);
        assertEquals("ipsproductcmpttype",
                new IpsElementImagePageElement(policyCmptTypeParameter, getContext()).getFileName());

        ITestRuleParameter ruleParameter = testCaseType.newExpectedResultRuleParameter();
        assertEquals("testruleparameter", new IpsElementImagePageElement(ruleParameter, getContext()).getFileName());
    }

    private void assertImagePathWorksWithIpsObjectAndIpsSrcFile(IIpsObject ipsObject) {
        String expectedFileName = ipsObject.getIpsObjectType().getFileExtension();

        if (ipsObject instanceof IProductCmpt productCmpt) {
            IProductCmptType productCmptType = productCmpt.findProductCmptType(ipsObject.getIpsProject());
            if (productCmptType.isUseCustomInstanceIcon()) {
                expectedFileName = productCmptType.getQualifiedName();
            }
        }

        IpsElementImagePageElement elementIpsObject = new IpsElementImagePageElement(ipsObject, getContext());
        assertEquals(expectedFileName, elementIpsObject.getFileName());

        IpsElementImagePageElement elementIpsSrcFile = new IpsElementImagePageElement(ipsObject.getIpsSrcFile(),
                getContext());
        assertEquals(expectedFileName, elementIpsSrcFile.getFileName());
    }

}
