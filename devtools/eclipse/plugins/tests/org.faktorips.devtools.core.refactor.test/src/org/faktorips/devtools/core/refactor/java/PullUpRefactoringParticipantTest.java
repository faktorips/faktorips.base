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

import static org.faktorips.devtools.model.builder.java.util.ParamUtil.intParam;
import static org.faktorips.devtools.model.builder.java.util.ParamUtil.stringParam;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Test;

public class PullUpRefactoringParticipantTest extends RefactoringParticipantTest {

    @Test
    public void testPullUpPolicyCmptTypeAttributeValueSetUnrestricted() {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.UNRESTRICTED, false);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeValueSetEnum() {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.ENUM, false);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeValueSetRange() {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.RANGE, false);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeFurtherUpInHierarchy() {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.UNRESTRICTED, true);
    }

    private void performTestPullUpPolicyCmptTypeAttribute(ValueSetType valueSetType, boolean useSuperTarget) {

        // Create target product component type configuring a policy component type
        IPolicyCmptType targetPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "TargetPolicy", "TargetProduct",
                true);
        IProductCmptType targetProductCmptType = targetPolicyCmptType.findProductCmptType(ipsProject);

        // If useSuperTarget is true create another hierarchy level
        String sourcePolicySuperType;
        String sourceProductSuperType;
        if (useSuperTarget) {
            IPolicyCmptType inBetweenPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "MidPolicy",
                    "MidProduct", false);
            IProductCmptType inBetweenProductCmptType = inBetweenPolicyCmptType.findProductCmptType(ipsProject);
            inBetweenPolicyCmptType.setSupertype(targetPolicyCmptType.getQualifiedName());
            inBetweenProductCmptType.setSupertype(targetProductCmptType.getQualifiedName());
            sourcePolicySuperType = inBetweenPolicyCmptType.getQualifiedName();
            sourceProductSuperType = inBetweenProductCmptType.getQualifiedName();
        } else {
            sourcePolicySuperType = targetPolicyCmptType.getQualifiedName();
            sourceProductSuperType = targetProductCmptType.getQualifiedName();
        }

        // Create source product component type, also configuring a policy component type
        IPolicyCmptType sourcePolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SourcePolicy", "SourceProduct",
                false);
        IProductCmptType sourceProductCmptType = sourcePolicyCmptType.findProductCmptType(ipsProject);
        sourceProductCmptType.setSupertype(sourceProductSuperType);
        sourcePolicyCmptType.setSupertype(sourcePolicySuperType);

        // Create the policy component type attribute to pull up
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = createPolicyCmptTypeAttribute("foo", sourcePolicyCmptType);
        policyCmptTypeAttribute.setValueSetType(valueSetType);

        // Save source files and build Java source code
        saveIpsSrcFile(targetPolicyCmptType);
        saveIpsSrcFile(targetProductCmptType);
        saveIpsSrcFile(sourcePolicyCmptType);
        saveIpsSrcFile(sourceProductCmptType);
        performFullBuild(ipsProject);

        // Perform the refactoring
        performPullUpRefactoring(policyCmptTypeAttribute, targetPolicyCmptType);
        performFullBuild(ipsProject);

        // Check whether the Java source code was modified correctly
        PolicyCmptTypeAttributeExpectations expectations = new PolicyCmptTypeAttributeExpectations(
                policyCmptTypeAttribute, targetPolicyCmptType, targetProductCmptType);
        expectations.check(sourcePolicyCmptType, sourceProductCmptType, intParam());
    }

    @Test
    public void testPullUpProductCmptTypeAttribute() {
        performTestPullUpProductCmptTypeAttribute(false);
    }

    @Test
    public void testPullUpProductCmptTypeAttributeFurtherUpInHierarchy() {
        performTestPullUpProductCmptTypeAttribute(true);
    }

    private void performTestPullUpProductCmptTypeAttribute(boolean useSuperTarget) {
        // Create target product component type configuring a policy component type
        IPolicyCmptType targetPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "TargetPolicy", "TargetProduct",
                true);
        IProductCmptType targetProductCmptType = targetPolicyCmptType.findProductCmptType(ipsProject);

        // If useSuperTarget is true create another hierarchy level
        String sourcePolicySuperType;
        String sourceProductSuperType;
        if (useSuperTarget) {
            IPolicyCmptType inBetweenPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "MidPolicy",
                    "MidProduct", false);
            IProductCmptType inBetweenProductCmptType = inBetweenPolicyCmptType.findProductCmptType(ipsProject);
            inBetweenPolicyCmptType.setSupertype(targetPolicyCmptType.getQualifiedName());
            inBetweenProductCmptType.setSupertype(targetProductCmptType.getQualifiedName());
            sourcePolicySuperType = inBetweenPolicyCmptType.getQualifiedName();
            sourceProductSuperType = inBetweenProductCmptType.getQualifiedName();
        } else {
            sourcePolicySuperType = targetPolicyCmptType.getQualifiedName();
            sourceProductSuperType = targetProductCmptType.getQualifiedName();
        }

        // Create source product component type, also configuring a policy component type
        IPolicyCmptType sourcePolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SourcePolicy", "SourceProduct",
                false);
        IProductCmptType sourceProductCmptType = sourcePolicyCmptType.findProductCmptType(ipsProject);
        sourceProductCmptType.setSupertype(sourceProductSuperType);
        sourcePolicyCmptType.setSupertype(sourcePolicySuperType);

        // Create the product component type attribute to pull up
        IProductCmptTypeAttribute productCmptTypeAttribute = createProductCmptTypeAttribute("foo",
                sourceProductCmptType);

        // Save source files and build Java source code
        saveIpsSrcFile(targetProductCmptType);
        saveIpsSrcFile(targetPolicyCmptType);
        saveIpsSrcFile(sourceProductCmptType);
        saveIpsSrcFile(sourcePolicyCmptType);
        performFullBuild(ipsProject);

        // Perform the refactoring
        performPullUpRefactoring(productCmptTypeAttribute, targetProductCmptType);
        performFullBuild(ipsProject);

        // Check whether the Java source code was modified correctly
        ProductCmptTypeAttributeExpectations expectations = new ProductCmptTypeAttributeExpectations(
                productCmptTypeAttribute, targetProductCmptType, targetPolicyCmptType);
        expectations.check(sourceProductCmptType, sourcePolicyCmptType, stringParam());
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(String name, IPolicyCmptType policyCmptType) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(name);
        policyCmptTypeAttribute.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        policyCmptTypeAttribute.setDefaultValue("0");
        policyCmptTypeAttribute.getValueSet().setContainsNull(false);
        return policyCmptTypeAttribute;
    }

    private IProductCmptTypeAttribute createProductCmptTypeAttribute(String name, IProductCmptType productCmptType) {
        IProductCmptTypeAttribute productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();
        productCmptTypeAttribute.setName(name);
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        productCmptTypeAttribute.setChangingOverTime(true);
        return productCmptTypeAttribute;
    }

}
