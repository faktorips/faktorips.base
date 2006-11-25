/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Daniel Hohenberger
 */
public class IpsProjectNamingConventionsTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IIpsProjectNamingConventions namingConventions;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject("TestProject");
        namingConventions = ipsProject.getNamingConventions();
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.DefaultIpsProjectNamingConventions#validateIpsPackageName(java.lang.String)}.
     * @throws CoreException 
     */
    public void testValidateIpsPackageName() throws CoreException {
        MessageList ml = namingConventions.validateIpsPackageName("validName");
        assertFalse(ml.containsErrorMsg());
        assertEquals(0, ml.getNoOfMessages());
        
        ml = namingConventions.validateIpsPackageName("1_invalid");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());
        
        ml = namingConventions.validateIpsPackageName("no blanks allowed");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());
        
        ml = namingConventions.validateIpsPackageName("new");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());
        
        ml = namingConventions.validateIpsPackageName("BIG");
        assertFalse(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.WARNING, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.DISCOURAGED_NAME, ml.getMessage(0).getCode());
    }

    public void testValidateNameForBusinessFunction() throws CoreException {
        IpsObjectType type = IpsObjectType.BUSINESS_FUNCTION;
        testCommonNameValidation(type); 
    }
    
    public void testValidateNameForPolicyCmptType() throws CoreException {
        IpsObjectType type = IpsObjectType.POLICY_CMPT_TYPE;
        testCommonNameValidation(type);        
    }

    public void testValidateNameForProductCmptType() throws CoreException {
        IpsObjectType type = IpsObjectType.PRODUCT_CMPT_TYPE;
        testCommonNameValidation(type); 
    }
    
    public void testValidateNameForTableStructure() throws CoreException {
        IpsObjectType type = IpsObjectType.TABLE_STRUCTURE;
        testCommonNameValidation(type); 
    }

    public void testValidateNameForProductCmpt() throws CoreException {
        IpsObjectType type = IpsObjectType.PRODUCT_CMPT;
        
        // check only that the name must not be empty and is not qualified,
        // the other naming conventions are specified in the implementation of IProductCmptNamingStrategy
        List validNames = new ArrayList();
        List invalidNames = new ArrayList();
        List invalidNamesMsgCodes = new ArrayList();
        
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

    public void testValidateNameForTableContents() throws CoreException {
        IpsObjectType type = IpsObjectType.TABLE_CONTENTS;
        testCommonNameValidation(type); 
    }

    public void testValidateNameForTestCaseType() throws CoreException {
        IpsObjectType type = IpsObjectType.TEST_CASE_TYPE;
        testCommonNameValidation(type); 
    }

    public void testValidateNameForTestCase() throws CoreException {
        IpsObjectType type = IpsObjectType.TEST_CASE;
        testCommonNameValidation(type); 
    }
    
    private void testCommonNameValidation(IpsObjectType type) throws CoreException {
        List validNames = new ArrayList();
        List invalidNames = new ArrayList();
        List invalidNamesMsgCodes = new ArrayList();
        
        // check unqualified names
        getCommonQualifiedNamesTestData(validNames, invalidNames, invalidNamesMsgCodes);
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
    
    private void getCommonQualifiedNamesTestData(List validNames, List invalidNames, List invalidNamesMsgCodes){
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
    
    /*
     * Validates the given valid- and invalid names. 
     * For each invalid name the relevant message code must be given, to perform the assert against this message code.
     */
    private void validateAndAssertNames(IpsObjectType type, boolean qualifiedNames,
            List validNames,
            List invalidNames,
            List invalidNamesMsgCodes) throws CoreException {
        assertEquals("Wrong usage of assert method, each invalid name should have a message code", invalidNames.size(),
                invalidNamesMsgCodes.size());
        
        for (Iterator iter = validNames.iterator(); iter.hasNext();) {
            String validName= (String )iter.next();
            MessageList ml = null;
            if (qualifiedNames){
                 ml = namingConventions.validateQualifiedIpsObjectName(type, validName);
            } else {
                ml = namingConventions.validateUnqualifiedIpsObjectName(type, validName);
            }
            assertEquals("\"" + validName + "\"" + " is invalid, expected was valid!", 0, ml.getNoOfMessages()); 
        }
 
        for (int i = 0; i < invalidNames.size(); i++) {
            String invalidName = (String)invalidNames.get(i);
            String msgCode = (String)invalidNamesMsgCodes.get(i);
            MessageList ml = null;
            if (qualifiedNames) {
                ml = namingConventions.validateQualifiedIpsObjectName(type, invalidName);
            }
            else {
                ml = namingConventions.validateUnqualifiedIpsObjectName(type, invalidName);
            }
            assertNotNull("\"" + invalidName + "\"" + " is valid against " + msgCode
                    + ", expected was invalid!", ml.getMessageByCode(msgCode));
        }
    }
}
