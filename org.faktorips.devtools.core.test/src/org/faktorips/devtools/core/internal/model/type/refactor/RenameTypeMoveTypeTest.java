/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;

/**
 * Provides test model for <tt>RenameTypeProcessorTest</tt> and <tt>MoveTypeProcessorTest</tt>.
 * 
 * @author Alexander Weickmann
 */
public abstract class RenameTypeMoveTypeTest extends AbstractIpsRefactoringTest {

    protected static final String OTHER_POLICY_NAME = "OtherPolicy";

    protected static final String OTHER_PRODUCT_NAME = "OtherProduct";

    protected IPolicyCmptType otherPolicyCmptType;

    protected IProductCmptType otherProductCmptType;

    protected IMethod policyMethod;

    protected IMethod productMethod;

    protected IAssociation otherPolicyToPolicyAssociation;

    protected IAssociation otherProductToProductAssociation;

    protected ITestAttribute superTestAttribute;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create another policy component type and another product component type.
        otherPolicyCmptType = newPolicyCmptType(ipsProject, OTHER_POLICY_NAME);
        otherProductCmptType = newProductCmptType(ipsProject, OTHER_PRODUCT_NAME);

        // Setup policy method.
        policyMethod = otherPolicyCmptType.newMethod();
        policyMethod.setName("policyMethod");
        policyMethod.setDatatype(Datatype.STRING.getQualifiedName());
        policyMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "notToBeChanged");
        policyMethod.newParameter(QUALIFIED_POLICY_NAME, "toBeChanged");
        policyMethod.newParameter(QUALIFIED_PRODUCT_NAME, "withProductDatatype");

        // Setup product method.
        productMethod = otherProductCmptType.newMethod();
        productMethod.setName("productMethod");
        productMethod.setDatatype(Datatype.STRING.getQualifiedName());
        productMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "notToBeChanged");
        productMethod.newParameter(QUALIFIED_PRODUCT_NAME, "toBeChanged");
        productMethod.newParameter(QUALIFIED_POLICY_NAME, "withPolicyDatatype");

        // Setup policy associations.
        otherPolicyToPolicyAssociation = otherPolicyCmptType.newAssociation();
        otherPolicyToPolicyAssociation.setTarget(QUALIFIED_POLICY_NAME);

        // Setup product associations.
        otherProductToProductAssociation = otherProductCmptType.newAssociation();
        otherProductToProductAssociation.setTarget(QUALIFIED_PRODUCT_NAME);

        // Create a test attribute based on an attribute of the super policy component type.
        IPolicyCmptTypeAttribute superPolicyAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superPolicyAttribute.setName("superPolicyAttribute");
        superPolicyAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superPolicyAttribute.setModifier(Modifier.PUBLISHED);
        superPolicyAttribute.setAttributeType(AttributeType.CHANGEABLE);
        superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        superTestAttribute.setAttribute(superPolicyAttribute);
        superTestAttribute.setPolicyCmptType(SUPER_POLICY_NAME);

        createProductCmpt();
    }

}
