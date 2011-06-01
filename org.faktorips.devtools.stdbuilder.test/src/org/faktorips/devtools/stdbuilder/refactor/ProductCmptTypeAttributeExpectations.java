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

import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getGenerationConceptNameAbbreviation;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getJavaType;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getPublishedInterfaceName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
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
            IProductCmptType productCmptType, IPolicyCmptType policyCmptType) throws CoreException {

        ipsProject = productCmptType.getIpsProject();
        this.productCmptTypeAttribute = productCmptTypeAttribute;
        this.productCmptType = productCmptType;
        this.policyCmptType = policyCmptType;

        productGenInterface = getProductGenInterface(productCmptType);
        productGenClass = getProductGenClass(productCmptType);
        policyClass = getPolicyClass(policyCmptType);
    }

    void check(String oldName, String newName, String datatypeSignature) throws CoreException {
        check(oldName, newName, productCmptType, policyCmptType, datatypeSignature);
    }

    void check(IProductCmptType oldProductCmptType, IPolicyCmptType oldPolicyCmptType, String datatypeSignature)
            throws CoreException {

        check(productCmptTypeAttribute.getName(), productCmptTypeAttribute.getName(), oldProductCmptType,
                oldPolicyCmptType, datatypeSignature);
    }

    void check(String oldName,
            String newName,
            IProductCmptType oldProductCmptType,
            IPolicyCmptType oldPolicyCmptType,
            String datatypeSignature) throws CoreException {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        IType oldProductGenInterface = getProductGenInterface(oldProductCmptType);
        IType oldProductGenClass = getProductGenClass(oldProductCmptType);
        IType oldPolicyClass = getPolicyClass(oldPolicyCmptType);

        assertFalse(oldProductGenInterface.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldProductGenClass.getField(oldName).exists());
        assertFalse(oldProductGenClass.getMethod("get" + oldNameCamelCase, new String[0]).exists());
        assertFalse(oldProductGenClass.getMethod("set" + oldNameCamelCase, new String[] { datatypeSignature }).exists());
        assertFalse(oldPolicyClass.getMethod("get" + oldNameCamelCase, new String[0]).exists());

        assertTrue(productGenInterface.getMethod("get" + newNameCamelCase, new String[0]).exists());
        assertTrue(productGenClass.getField(newName).exists());
        assertTrue(productGenClass.getMethod("get" + newNameCamelCase, new String[0]).exists());
        assertTrue(productGenClass.getMethod("set" + newNameCamelCase, new String[] { datatypeSignature }).exists());
        assertTrue(policyClass.getMethod("get" + newNameCamelCase, new String[0]).exists());
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

    private IType getPolicyClass(IPolicyCmptType policyCmptType) throws CoreException {
        return getJavaType("", policyCmptType.getName(), false, false, ipsProject);
    }

}