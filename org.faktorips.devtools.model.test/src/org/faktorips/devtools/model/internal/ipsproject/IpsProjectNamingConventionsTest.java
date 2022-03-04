/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Hohenberger
 */
public class IpsProjectNamingConventionsTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IIpsProjectNamingConventions namingConventions;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject("TestProject");
        namingConventions = ipsProject.getNamingConventions();
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.model.internal.ipsproject.DefaultIpsProjectNamingConventions#validateIpsPackageName(java.lang.String)}
     * .
     */
    @Test
    public void testValidateIpsPackageName() throws CoreException {
        MessageList ml = namingConventions.validateIpsPackageName("validName");
        assertFalse(ml.containsErrorMsg());
        assertEquals(0, ml.size());

        ml = namingConventions.validateIpsPackageName("1_invalid");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.size());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());

        ml = namingConventions.validateIpsPackageName("no blanks allowed");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.size());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());

        ml = namingConventions.validateIpsPackageName("new");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.size());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());

        ml = namingConventions.validateIpsPackageName("BIG");
        assertFalse(ml.containsErrorMsg());
        assertEquals(1, ml.size());
        assertEquals(Message.WARNING, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.DISCOURAGED_NAME, ml.getMessage(0).getCode());
    }

    @Test
    public void testValidateNameForPolicyCmptType() throws CoreException {
        IpsObjectType type = IpsObjectType.POLICY_CMPT_TYPE;
        testCommonJavaTypeNameValidation(type);
    }

    @Test
    public void testValidateNameForProductCmptType() throws CoreException {
        IpsObjectType type = IpsObjectType.PRODUCT_CMPT_TYPE;
        testCommonJavaTypeNameValidation(type);
    }

    @Test
    public void testValidateNameForTableStructure() throws CoreException {
        IpsObjectType type = IpsObjectType.TABLE_STRUCTURE;
        testCommonJavaTypeNameValidation(type);
    }

    @Test
    public void testValidateNameForProductCmpt() throws CoreException {
        IpsObjectType type = IpsObjectType.PRODUCT_CMPT;

        // check only that the name must not be empty and is not qualified,
        // the other naming conventions are specified in the implementation of
        // IProductCmptNamingStrategy
        List<String> validNames = new ArrayList<>();
        List<String> invalidNames = new ArrayList<>();
        List<String> invalidNamesMsgCodes = new ArrayList<>();

        // check unqualified names
        invalidNames.add("");
        invalidNames.add("test.test");
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_MISSING);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_QUALIFIED);
        validateAndAssertNames(type, false, validNames, invalidNames, invalidNamesMsgCodes);

        validNames.clear();
        invalidNames.clear();
        invalidNamesMsgCodes.clear();

        // check qualified names
        validNames.add("test.test");
        invalidNames.add("");
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_MISSING);
        validateAndAssertNames(type, true, validNames, invalidNames, invalidNamesMsgCodes);
    }

    @Test
    public void testValidateNameForTableContents() throws CoreException {
        IpsObjectType type = IpsObjectType.TABLE_CONTENTS;
        testCommonOSNameValidation(type);
    }

    @Test
    public void testValidateNameForTestCaseType() throws CoreException {
        IpsObjectType type = IpsObjectType.TEST_CASE_TYPE;
        testCommonJavaTypeNameValidation(type);
    }

    @Test
    public void testValidateNameForTestCase() throws CoreException {
        IpsObjectType type = IpsObjectType.TEST_CASE;
        testCommonOSNameValidation(type);
    }

    private void testCommonJavaTypeNameValidation(IpsObjectType type) throws CoreException {
        List<String> validNames = new ArrayList<>();
        List<String> invalidNames = new ArrayList<>();
        List<String> invalidNamesMsgCodes = new ArrayList<>();

        // check unqualified names
        getCommonJavaTypeQualifiedNamesTestData(validNames, invalidNames, invalidNamesMsgCodes);
        validateAndAssertNames(type, false, validNames, invalidNames, invalidNamesMsgCodes);

        validNames.clear();
        invalidNames.clear();
        invalidNamesMsgCodes.clear();

        // check qualified names
        validNames.add("test.test");
        invalidNames.add("");
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_MISSING);
        validateAndAssertNames(type, true, validNames, invalidNames, invalidNamesMsgCodes);
    }

    private void testCommonOSNameValidation(IpsObjectType type) throws CoreException {
        List<String> validNames = new ArrayList<>();
        List<String> invalidNames = new ArrayList<>();
        List<String> invalidNamesMsgCodes = new ArrayList<>();

        // check unqualified names
        validNames.add("test test");
        invalidNames.add("");
        invalidNames.add("test\\");
        invalidNames.add("test/");
        invalidNames.add("test:");
        invalidNames.add("test*");
        invalidNames.add("test?");
        invalidNames.add("test\"");
        invalidNames.add("test<");
        invalidNames.add("test>");
        invalidNames.add("test|");
        invalidNames.add("test/");
        invalidNames.add("test\0");
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_MISSING);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        validateAndAssertNames(type, false, validNames, invalidNames, invalidNamesMsgCodes);

        validNames.clear();
        invalidNames.clear();
        invalidNamesMsgCodes.clear();

        // check qualified names
        validNames.add("test.test");
        invalidNames.add("");
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_MISSING);
        validateAndAssertNames(type, true, validNames, invalidNames, invalidNamesMsgCodes);
    }

    private void getCommonJavaTypeQualifiedNamesTestData(List<String> validNames,
            List<String> invalidNames,
            List<String> invalidNamesMsgCodes) {
        validNames.add("Test");
        validNames.add("TEST");

        invalidNames.add("");
        invalidNames.add("test.test");
        invalidNames.add("test test");
        invalidNames.add("1test");
        invalidNames.add("tEST");
        invalidNames.add("test");

        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_MISSING);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.NAME_IS_QUALIFIED);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.INVALID_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.DISCOURAGED_NAME);
        invalidNamesMsgCodes.add(IIpsProjectNamingConventions.DISCOURAGED_NAME);
    }

    /**
     * Validates the given valid- and invalid names. For each invalid name the relevant message code
     * must be given, to perform the assert against this message code.
     */
    private void validateAndAssertNames(IpsObjectType type,
            boolean qualifiedNames,
            List<String> validNames,
            List<String> invalidNames,
            List<String> invalidNamesMsgCodes) throws CoreException {
        assertEquals("Wrong usage of assert method, each invalid name should have a message code", invalidNames.size(),
                invalidNamesMsgCodes.size());

        for (String validName : validNames) {
            MessageList ml = null;
            if (qualifiedNames) {
                ml = namingConventions.validateQualifiedIpsObjectName(type, validName);
            } else {
                ml = namingConventions.validateUnqualifiedIpsObjectName(type, validName);
            }
            assertEquals("\"" + validName + "\"" + " is invalid, expected was valid!", 0, ml.size());
        }

        for (int i = 0; i < invalidNames.size(); i++) {
            String invalidName = invalidNames.get(i);
            String msgCode = invalidNamesMsgCodes.get(i);
            MessageList ml = null;
            if (qualifiedNames) {
                ml = namingConventions.validateQualifiedIpsObjectName(type, invalidName);
            } else {
                ml = namingConventions.validateUnqualifiedIpsObjectName(type, invalidName);
            }
            assertNotNull("\"" + invalidName + "\"" + " is valid against " + msgCode + ", expected was invalid!",
                    ml.getMessageByCode(msgCode));
        }
    }

    /**
     * Test if the validate method of the ips objects includes the naming validation.
     */
    @Test
    public void testValidateNameWithIpsObjects() throws Exception {
        // Policy cmpt type
        IPolicyCmptType pct = newPolicyCmptType(ipsProject, "1test");
        pct.setConfigurableByProductCmptType(false);
        MessageList ml = pct.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));
        pct = newPolicyCmptType(ipsProject, "test.test2");
        pct.setConfigurableByProductCmptType(false);
        ml = pct.validate(ipsProject);
        assertNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));

        // Product cmpt type
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "test.1Product");
        ml = productCmptType.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));
        assertEquals(IIpsElement.PROPERTY_NAME, ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME)
                .getInvalidObjectProperties().get(0).getProperty());
        // testTypesCount ++; is currently no ips object type

        // Product cmpt
        IProductCmpt pc = newProductCmpt(ipsProject, "/test");
        ml = pc.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptNamingStrategy.MSGCODE_ILLEGAL_CHARACTERS));

        // Test case type
        ITestCaseType tct = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "1test");
        ml = tct.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));

        // Test case
        ITestCase tc = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "/test");
        ml = tc.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));

        // Table structure
        ITableStructure ts = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE, "1test");
        ml = ts.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));

        // Table contents
        ITableContents tco = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS, "/test");
        ml = tco.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsProjectNamingConventions.INVALID_NAME));
    }
}
