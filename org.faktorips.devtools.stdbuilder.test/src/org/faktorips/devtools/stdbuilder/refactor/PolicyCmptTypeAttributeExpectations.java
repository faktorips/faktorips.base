/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getGenerationConceptNameAbbreviation;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getJavaType;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getPublishedInterfaceName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
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
            IPolicyCmptType policyCmptType, IProductCmptType productCmptType) throws CoreException {

        ipsProject = policyCmptType.getIpsProject();
        this.policyCmptTypeAttribute = policyCmptTypeAttribute;
        this.policyCmptType = policyCmptType;
        this.productCmptType = productCmptType;

        policyInterface = getPolicyInterface(policyCmptType);
        policyClass = getPolicyClass(policyCmptType);
        productGenInterface = getProductGenInterface(productCmptType);
        productGenClass = getProductGenClass(productCmptType);
    }

    void check(String oldName, String newName, String datatypeSignature) throws CoreException {
        check(oldName, newName, policyCmptType, productCmptType, datatypeSignature);
    }

    void check(IPolicyCmptType oldPolicyCmptType, IProductCmptType oldProductCmptType, String datatypeSignature)
            throws CoreException {

        check(policyCmptTypeAttribute.getName(), policyCmptTypeAttribute.getName(), oldPolicyCmptType,
                oldProductCmptType, datatypeSignature);
    }

    private void check(String oldName,
            String newName,
            IPolicyCmptType oldPolicyCmptType,
            IProductCmptType oldProductCmptType,
            String datatypeSignature) throws CoreException {

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
            String datatypeSignature) throws CoreException {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        IType oldPolicyInterface = getPolicyInterface(oldPolicyCmptType);
        IType oldPolicyClass = getPolicyClass(oldPolicyCmptType);
        IType oldProductGenInterface = getProductGenInterface(oldProductCmptType);
        IType oldProductGenClass = getProductGenClass(oldProductCmptType);

        assertFalse(oldPolicyInterface.getField("PROPERTY_" + oldName.toUpperCase()).exists());
        assertFalse(oldPolicyInterface.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldPolicyInterface.getMethod("set" + oldNameCamelCase, new String[] { datatypeSignature }).exists());
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

    private void checkValueSetEnum(String oldName, String newName, IProductCmptType oldProductCmptType)
            throws CoreException {

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

    private void checkValueSetRange(String oldName, String newName, IProductCmptType oldProductCmptType)
            throws CoreException {

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

    private IType getPolicyInterface(IPolicyCmptType policyCmptType) throws CoreException {
        return getJavaType("", getPublishedInterfaceName(policyCmptType.getName(), ipsProject), true, false, ipsProject);
    }

    private IType getPolicyClass(IPolicyCmptType policyCmptType) throws CoreException {
        return getJavaType("", policyCmptType.getName(), false, false, ipsProject);
    }

    private IType getProductGenInterface(IProductCmptType productCmptType) throws CoreException {
        return getJavaType(
                "",
                getPublishedInterfaceName(productCmptType.getName() + getGenerationConceptNameAbbreviation(ipsProject),
                        ipsProject), true, false, ipsProject);
    }

    private IType getProductGenClass(IProductCmptType productCmptType) throws CoreException {
        return getJavaType("", productCmptType.getName() + getGenerationConceptNameAbbreviation(ipsProject), false,
                false, ipsProject);
    }

}