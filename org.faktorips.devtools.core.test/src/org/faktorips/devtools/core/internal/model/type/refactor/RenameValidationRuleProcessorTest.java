/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.junit.Test;

public class RenameValidationRuleProcessorTest extends AbstractIpsRefactoringTest {

    private RenameValidationRuleProcessor ipsRenameProcessor;
    private IValidationRule validationRule;
    private static final String NEW_NAME = "newName";
    private static final String OLD_NAME = "oldName";
    private ITestRule testRule;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        validationRule = policyCmptType.newRule();
        validationRule.setMessageCode(OLD_NAME);
        ipsRenameProcessor = new RenameValidationRuleProcessor(validationRule);
        validationRule.setName(OLD_NAME);
        validationRule.setConfigurableByProductComponent(true);
        productCmptGeneration.newPropertyValues(validationRule);
        testRule = testCase.newTestRule();
        testRule.setValidationRule(OLD_NAME);
    }

    @Test
    public void testCheckInitialConditionsValid() throws CoreException {
        RefactoringStatus status = ipsRenameProcessor.checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsValid() throws CoreException {
        ipsRenameProcessor.setNewName(NEW_NAME);
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    @Test
    public void testAffectedIpsSrcFiles() {
        Set<IIpsSrcFile> affectedIpsSrcFiles = ipsRenameProcessor.getAffectedIpsSrcFiles();

        assertEquals(3, affectedIpsSrcFiles.size());
        assertTrue(affectedIpsSrcFiles.contains(policyCmptType.getIpsSrcFile()));
        assertTrue(affectedIpsSrcFiles.contains(productCmpt.getIpsSrcFile()));
        assertTrue(affectedIpsSrcFiles.contains(testCase.getIpsSrcFile()));
    }

    @Test
    public void testRefactorIpsModel() throws CoreException {
        performRenameRefactoring(validationRule, NEW_NAME);

        assertEquals(NEW_NAME, validationRule.getName());
        assertNull(policyCmptType.getValidationRule(OLD_NAME));
        assertEquals(validationRule, policyCmptType.getValidationRule(NEW_NAME));
        assertEquals(NEW_NAME, productCmptGeneration.getPropertyValue(validationRule, IValidationRuleConfig.class)
                .getName());
        assertEquals(NEW_NAME, testCaseType.findValidationRule(NEW_NAME, ipsProject).getName());
        assertEquals(NEW_NAME, testRule.getValidationRule());
    }

    @Test
    public void testRefactorIpsModel_MultipleProductCmptGen() throws CoreException {
        IProductCmptGeneration productCmptGeneration2 = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGeneration2.newPropertyValues(validationRule);
        performRenameRefactoring(validationRule, NEW_NAME);

        assertEquals(NEW_NAME, validationRule.getName());
        assertNull(policyCmptType.getValidationRule(OLD_NAME));
        assertEquals(validationRule, policyCmptType.getValidationRule(NEW_NAME));
        assertEquals(NEW_NAME, productCmptGeneration.getPropertyValue(validationRule, IValidationRuleConfig.class)
                .getName());
        assertEquals(NEW_NAME, productCmptGeneration2.getPropertyValue(validationRule, IValidationRuleConfig.class)
                .getName());
    }

    @Test
    public void testValidateUserInputInvalid() throws CoreException {
        validationRule.setMessageCode(StringUtils.EMPTY);
        ipsRenameProcessor.setNewName("noMsgCode");
        RefactoringStatus status = ipsRenameProcessor.validateUserInput(new NullProgressMonitor());
        assertFalse(status.isOK());
    }

    @Test
    public void testValidateUserInputValid() throws CoreException {
        ipsRenameProcessor.setNewName(NEW_NAME);
        RefactoringStatus status = ipsRenameProcessor.validateUserInput(new NullProgressMonitor());
        assertTrue(status.isOK());
    }
}
