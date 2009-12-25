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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;

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
        policyToOtherPolicyAssociation = policyCmptType.newAssociation();
        policyToOtherPolicyAssociation.setTarget(OTHER_POLICY_NAME);
        policyToOtherPolicyAssociation.setTargetRoleSingular(OTHER_POLICY_NAME);
        policyToOtherPolicyAssociation.setTargetRolePlural(OTHER_POLICY_NAME + "s");
        otherPolicyToPolicyAssociation = otherPolicyCmptType.newAssociation();
        otherPolicyToPolicyAssociation.setTarget(QUALIFIED_POLICY_NAME);

        // Setup product associations.
        productToOtherProductAssociation = productCmptType.newAssociation();
        productToOtherProductAssociation.setTarget(OTHER_PRODUCT_NAME);
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

    public void testCheckInitialConditionsValid() throws CoreException {
        RenameRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RefactoringStatus status = refactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    public void testCheckInitialConditionsInvalid() throws CoreException {
        policyCmptType.setProductCmptType("abc");

        RenameRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RefactoringStatus status = refactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsValid() throws CoreException {
        RenameRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
        processor.setNewElementName("test");
        RefactoringStatus status = processor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    public void testCheckFinalConditionsFileAlreadyExists() throws CoreException {
        RenameRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
        processor.setNewElementName(PRODUCT_NAME);
        RefactoringStatus status = processor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    public void testCheckFinalConditionsInvalidTypeName() throws CoreException {
        RenameRefactoring refactoring = policyCmptType.getRenameRefactoring();
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)refactoring.getProcessor();
        processor.setNewElementName("$§§  $");
        RefactoringStatus status = processor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
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
        assertEquals(PACKAGE + "." + newElementName, productCmptType.getPolicyCmptType());

        // Check for test parameter and test attribute update.
        assertEquals(PACKAGE + "." + newElementName, testPolicyCmptTypeParameter.getPolicyCmptType());
        assertEquals(PACKAGE + "." + newElementName, testAttribute.getPolicyCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(PACKAGE + "." + newElementName, policyMethod.getParameters()[1].getDatatype());
        assertEquals(PACKAGE + "." + newElementName, productMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(PACKAGE + "." + newElementName, otherPolicyToPolicyAssociation.getTarget());
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
