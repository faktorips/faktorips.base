/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ContainsSearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.EqualitySearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.PolicyAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductComponentAssociationConditionType;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ConditionPMOPersistenceTest extends AbstractIpsPluginTest {

    private DialogSettings dialogSettings;

    private ProductSearchPresentationModel searchPMO;

    private ProductSearchConditionPresentationModel condition1;
    private ProductSearchConditionPresentationModel condition2;
    private ProductSearchConditionPresentationModel condition3;

    private IIpsProject ipsProject;

    private PolicyCmptType policyCmptType;
    private IPolicyCmptTypeAttribute policyAttribute;

    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute productAttribute;

    private IProductCmptTypeAssociation productAssocition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        policyAttribute = (IPolicyCmptTypeAttribute)policyCmptType.newAttribute();
        policyAttribute.setName("policyAttr");
        policyAttribute.setDatatype("String");
        policyAttribute.setValueSetConfiguredByProduct(true);

        productCmptType = ipsProject.findProductCmptType("Product");
        productAttribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        productAttribute.setName("productAttr");
        productAttribute.setDatatype("String");

        productAssocition = (IProductCmptTypeAssociation)productCmptType.newAssociation();
        productAssocition.setTargetRoleSingular("productAssoc");
        productAssocition.setTargetRolePlural("productAssocs");
        productAssocition.setTarget("idk");

        searchPMO = new ProductSearchPresentationModel();
        searchPMO.setProductCmptType(productCmptType);
    }

    private void setUpConditionPMOs() {
        condition1 = searchPMO.createProductSearchConditionPresentationModel();
        condition2 = searchPMO.createProductSearchConditionPresentationModel();
        condition3 = searchPMO.createProductSearchConditionPresentationModel();

        condition1.setCondition(new PolicyAttributeConditionType());
        condition1.setOperatorType(EqualitySearchOperatorType.EQUALITY);
        condition1.setSearchedElement(policyAttribute);
        condition1.setArgument("arg1");
        condition2.setCondition(new ProductAttributeConditionType());
        condition2.setOperatorType(EqualitySearchOperatorType.INEQUALITY);
        condition2.setSearchedElement(productAttribute);
        condition2.setArgument("arg2");
        condition3.setCondition(new ProductComponentAssociationConditionType());
        condition3.setOperatorType(ContainsSearchOperatorType.CONTAINS);
        condition3.setSearchedElement(productAssocition);
        condition3.setArgument("arg3");
    }

    private void setUpDialogSettings() {
        dialogSettings = new DialogSettings("rootSection");
        DialogSettings pmoSection = new DialogSettings("ProductSearchConditionPMOs-Section");
        pmoSection.put(ConditionPMOPersistence.CONDITION_TYPE_KEY, new String[] {
                ConditionPMOPersistence.POLICY_ATTRIBUTE_CONDITION_TYPE,
                ConditionPMOPersistence.PRODUCT_ATTRIBUTE_CONDITION_TYPE,
                ConditionPMOPersistence.PRODUCT_COMPONENT_ASSOCIATION_CONDITION_TYPE });
        pmoSection.put(ConditionPMOPersistence.ARGUMENTS_KEY, new String[] { "arg1", "arg2", "arg3" });
        pmoSection
                .put(ConditionPMOPersistence.OPERATOR_TYPE_KEY, new String[] { "EQUALITY", "INEQUALITY", "CONTAINS" });
        pmoSection.put(ConditionPMOPersistence.SEARCHED_ELEMENT_KEY, new String[] { "policyAttr", "productAttr",
                "productAssoc" });

        dialogSettings.addSection(pmoSection);
    }

    @Test
    public void testLoadConditions_noDialogSettings() {
        dialogSettings = new DialogSettings("rootSection");
        List<ProductSearchConditionPresentationModel> conditions = new ConditionPMOPersistence(searchPMO,
                dialogSettings).loadConditions();

        assertEquals(Collections.emptyList(), conditions);
    }

    @Test
    public void testLoadConditions() {
        setUpDialogSettings();

        List<ProductSearchConditionPresentationModel> conditionPMOs = new ConditionPMOPersistence(searchPMO,
                dialogSettings).loadConditions();

        assertEquals(3, conditionPMOs.size());
        assertEquals("arg1", conditionPMOs.get(0).getArgument());
        assertEquals("arg2", conditionPMOs.get(1).getArgument());
        assertEquals("arg3", conditionPMOs.get(2).getArgument());

        assertEquals(EqualitySearchOperatorType.EQUALITY, conditionPMOs.get(0).getOperatorType());
        assertEquals(EqualitySearchOperatorType.INEQUALITY, conditionPMOs.get(1).getOperatorType());
        assertEquals(ContainsSearchOperatorType.CONTAINS, conditionPMOs.get(2).getOperatorType());

        assertTrue(conditionPMOs.get(0).getConditionType() instanceof PolicyAttributeConditionType);
        assertTrue(conditionPMOs.get(1).getConditionType() instanceof ProductAttributeConditionType);
        assertTrue(conditionPMOs.get(2).getConditionType() instanceof ProductComponentAssociationConditionType);

        assertEquals(policyAttribute, conditionPMOs.get(0).getSearchedElement());
        assertEquals(productAttribute, conditionPMOs.get(1).getSearchedElement());
        assertEquals(productAssocition, conditionPMOs.get(2).getSearchedElement());
    }

    @Test
    public void testSaveConditions() {
        setUpConditionPMOs();

        DialogSettings settings = new DialogSettings("root-settings");
        new ConditionPMOPersistence(searchPMO, settings).saveConditions();

        IDialogSettings section = settings.getSection(ConditionPMOPersistence.SECTION_NAME);
        assertNotNull(section);
        String[] arguments = section.getArray(ConditionPMOPersistence.ARGUMENTS_KEY);
        String[] operandTypes = section.getArray(ConditionPMOPersistence.OPERATOR_TYPE_KEY);
        String[] serachedElements = section.getArray(ConditionPMOPersistence.SEARCHED_ELEMENT_KEY);
        String[] conditionTypes = section.getArray(ConditionPMOPersistence.CONDITION_TYPE_KEY);

        assertNotNull(conditionTypes);
        assertEquals(ConditionPMOPersistence.POLICY_ATTRIBUTE_CONDITION_TYPE, conditionTypes[0]);
        assertEquals(ConditionPMOPersistence.PRODUCT_ATTRIBUTE_CONDITION_TYPE, conditionTypes[1]);
        assertEquals(ConditionPMOPersistence.PRODUCT_COMPONENT_ASSOCIATION_CONDITION_TYPE, conditionTypes[2]);

        assertNotNull(arguments);
        assertEquals("arg1", arguments[0]);
        assertEquals("arg2", arguments[1]);
        assertEquals("arg3", arguments[2]);

        assertNotNull(operandTypes);
        assertEquals("EQUALITY", operandTypes[0]);
        assertEquals("INEQUALITY", operandTypes[1]);
        assertEquals("CONTAINS", operandTypes[2]);

        assertNotNull(serachedElements);
        assertEquals("policyAttr", serachedElements[0]);
        assertEquals("productAttr", serachedElements[1]);
        assertEquals("productAssoc", serachedElements[2]);
    }

}
