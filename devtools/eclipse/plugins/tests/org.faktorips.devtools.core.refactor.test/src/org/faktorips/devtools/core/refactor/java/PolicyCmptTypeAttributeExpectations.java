/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

import static org.faktorips.devtools.core.refactor.java.RefactoringTestUtil.getGenerationConceptNameAbbreviation;
import static org.faktorips.devtools.core.refactor.java.RefactoringTestUtil.getJavaType;
import static org.faktorips.devtools.core.refactor.java.RefactoringTestUtil.getPublishedInterfaceName;
import static org.faktorips.devtools.model.builder.java.util.ParamUtil.unresolvedParam;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.util.StringUtil;

final class PolicyCmptTypeAttributeExpectations {

    private final IIpsProject ipsProject;

    private final IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private final IPolicyCmptType policyCmptType;

    private final IProductCmptType productCmptType;

    private final IType policyInterface;

    private final IType policyClass;

    private final IType productGenInterface;

    private final IType productGenClass;

    PolicyCmptTypeAttributeExpectations(IPolicyCmptTypeAttribute policyCmptTypeAttribute,
            IPolicyCmptType policyCmptType, IProductCmptType productCmptType) {

        ipsProject = policyCmptType.getIpsProject();
        this.policyCmptTypeAttribute = policyCmptTypeAttribute;
        this.policyCmptType = policyCmptType;
        this.productCmptType = productCmptType;

        policyInterface = getPolicyInterface(policyCmptType);
        policyClass = getPolicyClass(policyCmptType);
        productGenInterface = getProductGenInterface(productCmptType);
        productGenClass = getProductGenClass(productCmptType);
    }

    void check(String oldName, String newName, String datatypeSignature) {
        check(oldName, newName, policyCmptType, productCmptType, datatypeSignature);
    }

    void check(IPolicyCmptType oldPolicyCmptType, IProductCmptType oldProductCmptType, String datatypeSignature) {

        check(policyCmptTypeAttribute.getName(), policyCmptTypeAttribute.getName(), oldPolicyCmptType,
                oldProductCmptType, datatypeSignature);
    }

    private void check(String oldName,
            String newName,
            IPolicyCmptType oldPolicyCmptType,
            IProductCmptType oldProductCmptType,
            String datatypeSignature) {

        ValueSetType valueSetType = policyCmptTypeAttribute.getValueSet().getValueSetType();

        if (ValueSetType.UNRESTRICTED.equals(valueSetType)) {
            checkValueSetUnrestricted(oldName, newName, oldPolicyCmptType, oldProductCmptType, datatypeSignature);

        } else if (ValueSetType.ENUM.equals(valueSetType)) {
            checkValueSetEnum(oldName, newName, oldProductCmptType);

        } else if (ValueSetType.RANGE.equals(valueSetType)) {
            checkValueSetRange(oldName, newName, oldProductCmptType);
        }
    }

    private void checkValueSetUnrestricted(String oldName,
            String newName,
            IPolicyCmptType oldPolicyCmptType,
            IProductCmptType oldProductCmptType,
            String datatypeSignature) {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        IType oldPolicyInterface = getPolicyInterface(oldPolicyCmptType);
        IType oldPolicyClass = getPolicyClass(oldPolicyCmptType);
        IType oldProductGenInterface = getProductGenInterface(oldProductCmptType);
        IType oldProductGenClass = getProductGenClass(oldProductCmptType);

        assertFalse(oldPolicyInterface.getField("PROPERTY_" + oldName.toUpperCase()).exists());
        assertFalse(oldPolicyInterface.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(
                oldPolicyInterface.getMethod("set" + oldNameCamelCase, new String[] { datatypeSignature }).exists());
        assertFalse(oldProductGenInterface.getMethod("getDefaultValue" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldProductGenInterface.getMethod("getSetOfAllowedValuesFor" + oldNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());

        assertFalse(oldPolicyClass.getField(oldName).exists());
        assertFalse(oldPolicyClass.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldPolicyClass.getMethod("set" + oldNameCamelCase, new String[] { datatypeSignature }).exists());
        assertFalse(oldProductGenClass.getField("defaultValue" + oldNameCamelCase).exists());
        assertFalse(oldProductGenClass.getField("setOfAllowedValues" + oldNameCamelCase).exists());
        assertFalse(oldProductGenClass.getMethod("getDefaultValue" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldProductGenClass.getMethod("getSetOfAllowedValuesFor" + oldNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());

        assertTrue(policyInterface.getField("PROPERTY_" + newName.toUpperCase()).exists());
        assertTrue(policyInterface.getMethod("get" + newNameCamelCase, new String[0]).exists());
        assertTrue(policyInterface.getMethod("set" + newNameCamelCase, new String[] { datatypeSignature }).exists());
        assertTrue(productGenInterface.getMethod("getDefaultValue" + newNameCamelCase, new String[0]).exists());
        assertTrue(productGenInterface.getMethod("getSetOfAllowedValuesFor" + newNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());

        assertTrue(policyClass.getField(newName).exists());
        assertTrue(policyClass.getMethod("get" + newNameCamelCase, new String[0]).exists());
        assertTrue(policyClass.getMethod("set" + newNameCamelCase, new String[] { datatypeSignature }).exists());
        assertTrue(productGenClass.getField("defaultValue" + newNameCamelCase).exists());
        assertTrue(productGenClass.getField("setOfAllowedValues" + newNameCamelCase).exists());
        assertTrue(productGenClass.getMethod("getDefaultValue" + newNameCamelCase, new String[0]).exists());
        assertTrue(productGenClass.getMethod("getSetOfAllowedValuesFor" + newNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
    }

    private void checkValueSetEnum(String oldName, String newName, IProductCmptType oldProductCmptType) {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        IType oldProductGenInterface = getProductGenInterface(oldProductCmptType);
        IType oldProductGenClass = getProductGenClass(oldProductCmptType);

        assertFalse(oldProductGenInterface.getMethod("getAllowedValuesFor" + oldNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
        assertFalse(oldProductGenClass.getField("allowedValuesFor" + oldNameCamelCase).exists());
        assertFalse(oldProductGenClass.getMethod("getAllowedValuesFor" + oldNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());

        assertTrue(productGenInterface.getMethod("getAllowedValuesFor" + newNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
        assertTrue(productGenClass.getField("allowedValuesFor" + newNameCamelCase).exists());
        assertTrue(productGenClass.getMethod("getAllowedValuesFor" + newNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
    }

    private void checkValueSetRange(String oldName, String newName, IProductCmptType oldProductCmptType) {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        IType oldProductGenInterface = getProductGenInterface(oldProductCmptType);
        IType oldProductGenClass = getProductGenClass(oldProductCmptType);

        assertFalse(oldProductGenInterface.getMethod("getRangeFor" + oldNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
        assertFalse(oldProductGenClass.getField("rangeFor" + oldNameCamelCase).exists());
        assertFalse(oldProductGenClass.getMethod("getRangeFor" + oldNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());

        assertTrue(productGenInterface.getMethod("getRangeFor" + newNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
        assertTrue(productGenClass.getField("rangeFor" + newNameCamelCase).exists());
        assertTrue(productGenClass.getMethod("getRangeFor" + newNameCamelCase,
                new String[] { unresolvedParam(IValidationContext.class) }).exists());
    }

    private IType getPolicyInterface(IPolicyCmptType policyCmptType) {
        return getJavaType("", getPublishedInterfaceName(policyCmptType.getName(), ipsProject), true, false,
                ipsProject);
    }

    private IType getPolicyClass(IPolicyCmptType policyCmptType) {
        return getJavaType("", policyCmptType.getName(), false, false, ipsProject);
    }

    private IType getProductGenInterface(IProductCmptType productCmptType) {
        return getJavaType(
                "",
                getPublishedInterfaceName(productCmptType.getName() + getGenerationConceptNameAbbreviation(ipsProject),
                        ipsProject),
                true, false, ipsProject);
    }

    private IType getProductGenClass(IProductCmptType productCmptType) {
        return getJavaType("", productCmptType.getName() + getGenerationConceptNameAbbreviation(ipsProject), false,
                false, ipsProject);
    }

}
