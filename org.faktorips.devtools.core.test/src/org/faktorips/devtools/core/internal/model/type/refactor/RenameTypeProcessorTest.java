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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;

public class RenameTypeProcessorTest extends AbstractIpsRefactoringTest {

    private static final String OTHER_POLICY_NAME = "OtherPolicy";

    private static final String OTHER_PRODUCT_NAME = "OtherProduct";

    private IPolicyCmptType otherPolicyCmptType;

    private IProductCmptType otherProductCmptType;

    private IMethod policyMethod;

    private IMethod productMethod;

    private IAssociation policyToOtherPolicyAssociation;

    private IAssociation otherPolicyToPolicyAssociation;

    private IAssociation productToOtherProductAssociation;

    private IAssociation otherProductToProductAssociation;

    private ITestAttribute superTestAttribute;

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
        policyMethod.newParameter(POLICY_NAME, "toBeChanged");
        policyMethod.newParameter(PRODUCT_NAME, "withProductDatatype");

        // Setup product method.
        productMethod = otherProductCmptType.newMethod();
        productMethod.setName("productMethod");
        productMethod.setDatatype(Datatype.STRING.getQualifiedName());
        productMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "notToBeChanged");
        productMethod.newParameter(PRODUCT_NAME, "toBeChanged");
        productMethod.newParameter(POLICY_NAME, "withPolicyDatatype");

        // Setup policy associations.
        policyToOtherPolicyAssociation = policyCmptType.newAssociation();
        policyToOtherPolicyAssociation.setTarget(OTHER_POLICY_NAME);
        otherPolicyToPolicyAssociation = otherPolicyCmptType.newAssociation();
        otherPolicyToPolicyAssociation.setTarget(POLICY_NAME);

        // Setup product associations.
        productToOtherProductAssociation = productCmptType.newAssociation();
        productToOtherProductAssociation.setTarget(OTHER_PRODUCT_NAME);
        otherProductToProductAssociation = otherProductCmptType.newAssociation();
        otherProductToProductAssociation.setTarget(PRODUCT_NAME);

        // Create a test attribute based on an attribute of the super policy component type.
        IPolicyCmptTypeAttribute superPolicyAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superPolicyAttribute.setName("superPolicyAttribute");
        superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        superTestAttribute.setAttribute(superPolicyAttribute);
        superTestAttribute.setPolicyCmptType(SUPER_POLICY_NAME);

        createProductCmpt();
    }

    public void testRenamePolicyCmptType() throws CoreException {
        String newElementName = "NewPolicy";
        runRenameRefactoring(policyCmptType, newElementName);

        // Old policy component is not modified.
        assertEquals(POLICY_NAME, policyCmptType.getName());

        // Find the new policy component.
        IIpsSrcFile ipsSrcFile = policyCmptType.getIpsPackageFragment().getIpsSrcFile(newElementName,
                policyCmptType.getIpsObjectType());
        assertTrue(ipsSrcFile.exists());
        IPolicyCmptType newPolicyCmptType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
        assertEquals(newElementName, newPolicyCmptType.getName());

        // Check for product component configuration update.
        assertEquals(newElementName, productCmptType.getPolicyCmptType());

        // Check for test parameter and test attribute update.
        assertEquals(newElementName, testPolicyCmptTypeParameter.getPolicyCmptType());
        assertEquals(newElementName, testAttribute.getPolicyCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(newElementName, policyMethod.getParameters()[1].getDatatype());
        assertEquals(newElementName, productMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(newElementName, otherPolicyToPolicyAssociation.getTarget());
    }

    public void testRenameSuperPolicyCmptType() throws CoreException {
        String newElementName = "NewSuperPolicy";
        runRenameRefactoring(superPolicyCmptType, newElementName);

        // Check for test attribute update.
        assertEquals(newElementName, superTestAttribute.getPolicyCmptType());

        // Check for subtype update.
        assertEquals(newElementName, policyCmptType.getSupertype());
    }

    public void testRenameProductCmptType() {
        // TODO AW: Implement test.
    }

    public void testRenameSuperProductCmptType() {
        // TODO AW: Implement test.
    }

}
