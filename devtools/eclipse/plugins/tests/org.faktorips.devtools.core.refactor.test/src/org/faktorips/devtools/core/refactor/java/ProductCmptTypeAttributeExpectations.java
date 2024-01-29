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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.StringUtil;

final class ProductCmptTypeAttributeExpectations {

    private final IIpsProject ipsProject;

    private final IProductCmptTypeAttribute productCmptTypeAttribute;

    private final IProductCmptType productCmptType;

    private final IPolicyCmptType policyCmptType;

    private final IType policyClass;

    private final IType productGenInterface;

    private final IType productGenClass;

    ProductCmptTypeAttributeExpectations(IProductCmptTypeAttribute productCmptTypeAttribute,
            IProductCmptType productCmptType, IPolicyCmptType policyCmptType) {

        ipsProject = productCmptType.getIpsProject();
        this.productCmptTypeAttribute = productCmptTypeAttribute;
        this.productCmptType = productCmptType;
        this.policyCmptType = policyCmptType;

        productGenInterface = getProductGenInterface(productCmptType);
        productGenClass = getProductGenClass(productCmptType);
        policyClass = getPolicyClass(policyCmptType);
    }

    void check(String oldName, String newName, String datatypeSignature) {
        check(oldName, newName, productCmptType, policyCmptType, datatypeSignature);
    }

    void check(IProductCmptType oldProductCmptType, IPolicyCmptType oldPolicyCmptType, String datatypeSignature) {

        check(productCmptTypeAttribute.getName(), productCmptTypeAttribute.getName(), oldProductCmptType,
                oldPolicyCmptType, datatypeSignature);
    }

    void check(String oldName,
            String newName,
            IProductCmptType oldProductCmptType,
            IPolicyCmptType oldPolicyCmptType,
            String datatypeSignature) {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        IType oldProductGenInterface = getProductGenInterface(oldProductCmptType);
        IType oldProductGenClass = getProductGenClass(oldProductCmptType);
        IType oldPolicyClass = getPolicyClass(oldPolicyCmptType);

        assertFalse(oldProductGenInterface.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldProductGenClass.getField(oldName).exists());
        assertFalse(oldProductGenClass.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(
                oldProductGenClass.getMethod("set" + oldNameCamelCase, new String[] { datatypeSignature }).exists());
        assertFalse(oldPolicyClass.getMethod("get" + oldNameCamelCase, new String[0]).exists());

        assertTrue(productGenInterface.getMethod("get" + newNameCamelCase, new String[0]).exists());
        assertTrue(productGenClass.getField(newName).exists());
        assertTrue(productGenClass.getMethod("get" + newNameCamelCase, new String[0]).exists());
        try {
            IMethod[] methods = productGenClass.getMethods();
            Arrays.stream(methods).forEach(System.out::println);
        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(productGenClass.getMethod("set" + newNameCamelCase, new String[] { datatypeSignature }).exists());
        assertTrue(policyClass.getMethod("get" + newNameCamelCase, new String[0]).exists());
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

    private IType getPolicyClass(IPolicyCmptType policyCmptType) {
        return getJavaType("", policyCmptType.getName(), false, false, ipsProject);
    }

}
