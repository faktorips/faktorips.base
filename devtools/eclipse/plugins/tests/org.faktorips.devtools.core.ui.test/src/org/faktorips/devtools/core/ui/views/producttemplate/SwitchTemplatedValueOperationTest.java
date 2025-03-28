/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.junit.Before;
import org.junit.Test;

public class SwitchTemplatedValueOperationTest extends AbstractIpsPluginTest {

    private static final String FORMULA_NAME = "myFormula";

    private static final String VALUE1 = "value1";

    private static final String VALUE2 = "value1";

    private IFormula defValue1;

    private IFormula defValue2;

    private IFormula inhValue1;

    private IFormula inhValue2;

    private IFormula templatePropertyValue;

    private IIpsProject ipsProject;

    private IProductCmpt template;

    private IProductCmpt defProd1;

    private IProductCmpt defProd2;

    private IProductCmpt inhProd1;

    private IProductCmpt inhProd2;

    private IProductCmptType prodType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        prodType = newProductCmptType(ipsProject, "Type123");
        IProductCmptTypeMethod signature = prodType.newFormulaSignature(FORMULA_NAME);
        signature.setChangingOverTime(false);
        defProd1 = newProductCmpt(prodType, "Prod1");
        defProd1.fixAllDifferencesToModel(ipsProject);
        defProd2 = newProductCmpt(prodType, "Prod2");
        defProd2.fixAllDifferencesToModel(ipsProject);
        inhProd1 = newProductCmpt(prodType, "Prod3");
        inhProd1.fixAllDifferencesToModel(ipsProject);
        inhProd2 = newProductCmpt(prodType, "Prod4");
        inhProd2.fixAllDifferencesToModel(ipsProject);
        template = newProductTemplate(prodType, "tempalte");
        template.fixAllDifferencesToModel(ipsProject);
        defProd1.setTemplate(template.getQualifiedName());
        defProd2.setTemplate(template.getQualifiedName());
        defValue1 = defProd1.getFormula(FORMULA_NAME);
        defValue1.setExpression(VALUE1);
        defValue2 = defProd2.getFormula(FORMULA_NAME);
        defValue2.setExpression(VALUE1);
        inhProd1.setTemplate(template.getQualifiedName());
        inhProd2.setTemplate(template.getQualifiedName());
        inhValue1 = inhProd1.getFormula(FORMULA_NAME);
        inhValue1.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        inhValue2 = inhProd2.getFormula(FORMULA_NAME);
        inhValue2.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        templatePropertyValue = template.getFormula(FORMULA_NAME);
        templatePropertyValue.setExpression(VALUE2);
    }

    @Test
    public void testCreateSwitchTemplatePropertyValueOperation() throws Exception {
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);

        SwitchTemplatedValueOperation operation = SwitchTemplatedValueOperation.create(selected);

        assertEquals(templatePropertyValue, operation.getTemplateValue());
        assertEquals(selected, operation.getDefiningPropertyValues());
        assertEquals(Arrays.asList(inhValue1, inhValue2),
                new ArrayList<>(operation.getInheritingPropertyValues()));
        assertEquals(VALUE1, operation.getNewValue());
    }

    @Test
    public void testCreateSwitchTemplatePropertyValueOperation_MissingTemplate() throws Exception {
        defProd1.setTemplate("Can't touch this");
        defProd2.setTemplate("Oh-oh Oh Oh Oh-oh-oh");
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);

        assertThat(SwitchTemplatedValueOperation.isValidSelection(selected), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSwitchTemplatePropertyValueOperation_MissingTemplate_ExceptionOnCreate() throws Exception {
        defProd1.setTemplate("Can't touch this");
        defProd2.setTemplate("Oh-oh Oh Oh Oh-oh-oh");
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);

        SwitchTemplatedValueOperation.create(selected);
    }

    @Test
    public void testRun() throws Exception {
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);
        SwitchTemplatedValueOperation operation = SwitchTemplatedValueOperation.create(selected);

        operation.run(new NullProgressMonitor());

        assertEquals(VALUE1, templatePropertyValue.getExpression());
        assertEquals(VALUE1, defValue1.getExpression());
        assertEquals(VALUE1, defValue2.getExpression());
        assertEquals(TemplateValueStatus.INHERITED, defValue1.getTemplateValueStatus());
        assertEquals(TemplateValueStatus.INHERITED, defValue2.getTemplateValueStatus());
        assertEquals(VALUE2, inhValue1.getExpression());
        assertEquals(VALUE2, inhValue2.getExpression());
        assertEquals(TemplateValueStatus.DEFINED, inhValue1.getTemplateValueStatus());
        assertEquals(TemplateValueStatus.DEFINED, inhValue2.getTemplateValueStatus());
    }

    @Test
    public void testRun_NotSaveWhenDirty() throws Exception {
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);
        SwitchTemplatedValueOperation operation = SwitchTemplatedValueOperation.create(selected);

        operation.run(new NullProgressMonitor());

        assertEquals(true, defProd1.getIpsSrcFile().isDirty());
        assertEquals(true, defProd2.getIpsSrcFile().isDirty());
        assertEquals(true, inhProd1.getIpsSrcFile().isDirty());
        assertEquals(true, inhProd2.getIpsSrcFile().isDirty());
        assertEquals(true, template.getIpsSrcFile().isDirty());
    }

    @Test
    public void testRun_SaveWhenNotDirty() throws Exception {
        defProd1.getIpsSrcFile().save(null);
        defProd2.getIpsSrcFile().save(null);
        inhProd1.getIpsSrcFile().save(null);
        inhProd2.getIpsSrcFile().save(null);
        template.getIpsSrcFile().save(null);
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);
        SwitchTemplatedValueOperation operation = SwitchTemplatedValueOperation.create(selected);

        operation.run(new NullProgressMonitor());

        assertEquals(false, defProd1.getIpsSrcFile().isDirty());
        assertEquals(false, defProd2.getIpsSrcFile().isDirty());
        assertEquals(false, inhProd1.getIpsSrcFile().isDirty());
        assertEquals(false, inhProd2.getIpsSrcFile().isDirty());
        assertEquals(false, template.getIpsSrcFile().isDirty());
    }

    @Test
    public void testRun_TemplateUnrestricted() throws Exception {
        templatePropertyValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        inhValue1.setFormulaSignature("not needed in this test");
        inhValue2.setFormulaSignature("not needed in this test");
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);
        SwitchTemplatedValueOperation operation = SwitchTemplatedValueOperation.create(selected);

        operation.run(new NullProgressMonitor());

        assertEquals(TemplateValueStatus.DEFINED, templatePropertyValue.getTemplateValueStatus());
        assertEquals(VALUE1, templatePropertyValue.getExpression());
        assertEquals(VALUE1, defValue1.getExpression());
        assertEquals(VALUE1, defValue2.getExpression());
        assertEquals(TemplateValueStatus.INHERITED, defValue1.getTemplateValueStatus());
        assertEquals(TemplateValueStatus.INHERITED, defValue2.getTemplateValueStatus());
    }

    @Test
    public void testIsValidSelection_TemplateUnrestricted() throws Exception {
        templatePropertyValue.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);

        assertTrue(SwitchTemplatedValueOperation.isValidSelection(selected));
    }

    @Test
    public void testIsValidSelection_TemplateNotFound() throws Exception {
        templatePropertyValue.setFormulaSignature("invalid");
        List<IPropertyValue> selected = Arrays.<IPropertyValue> asList(defValue1, defValue2);

        assertFalse(SwitchTemplatedValueOperation.isValidSelection(selected));
    }

}
