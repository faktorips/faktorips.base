/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.intParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.stringParam;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.junit.Test;

public class PullUpRefactoringParticipantTest extends RefactoringParticipantTest {

    @Test
    public void testPullUpPolicyCmptTypeAttributeValueSetUnrestricted() throws CoreException {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.UNRESTRICTED, false);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeValueSetEnum() throws CoreException {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.ENUM, false);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeValueSetRange() throws CoreException {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.RANGE, false);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeFurtherUpInHierarchy() throws CoreException {
        performTestPullUpPolicyCmptTypeAttribute(ValueSetType.UNRESTRICTED, true);
    }

    private void performTestPullUpPolicyCmptTypeAttribute(ValueSetType valueSetType, boolean useSuperTarget)
            throws CoreException {

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

        // Check whether the Java source code was modified correctly
        PolicyCmptTypeAttributeExpectations expectations = new PolicyCmptTypeAttributeExpectations(
                policyCmptTypeAttribute, targetPolicyCmptType, targetProductCmptType);
        expectations.check(sourcePolicyCmptType, sourceProductCmptType, intParam());
    }

    @Test
    public void testPullUpProductCmptTypeAttribute() throws CoreException {
        performTestPullUpProductCmptTypeAttribute(false);
    }

    @Test
    public void testPullUpProductCmptTypeAttributeFurtherUpInHierarchy() throws CoreException {
        performTestPullUpProductCmptTypeAttribute(true);
    }

    private void performTestPullUpProductCmptTypeAttribute(boolean useSuperTarget) throws CoreException {
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
