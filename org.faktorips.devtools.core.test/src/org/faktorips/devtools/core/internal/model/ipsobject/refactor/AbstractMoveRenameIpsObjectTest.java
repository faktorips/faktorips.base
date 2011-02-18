/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.junit.Before;

/**
 * Provides test model for <tt>RenameTypeProcessorTest</tt> and <tt>MoveTypeProcessorTest</tt>.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractMoveRenameIpsObjectTest extends AbstractIpsRefactoringTest {

    protected static final String OTHER_PRODUCT_NAME = "OtherProduct";

    protected IProductCmpt otherProductCmpt;

    protected IMethod policyMethod;

    protected IMethod productMethod;

    protected IProductCmptLink otherProductToProductLink;

    protected ITestAttribute superTestAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Setup policy method.
        policyMethod = otherPolicyCmptType.newMethod();
        policyMethod.setName("policyMethod");
        policyMethod.setDatatype(Datatype.STRING.getQualifiedName());
        policyMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "notToBeChanged");
        policyMethod.newParameter(QUALIFIED_POLICY_CMPT_TYPE_NAME, "toBeChanged");
        policyMethod.newParameter(QUALIFIED_PRODUCT_CMPT_TYPE_NAME, "withProductDatatype");

        // Setup product method.
        productMethod = otherProductCmptType.newMethod();
        productMethod.setName("productMethod");
        productMethod.setDatatype(Datatype.STRING.getQualifiedName());
        productMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "notToBeChanged");
        productMethod.newParameter(QUALIFIED_PRODUCT_CMPT_TYPE_NAME, "toBeChanged");
        productMethod.newParameter(QUALIFIED_POLICY_CMPT_TYPE_NAME, "withPolicyDatatype");

        // Create a test attribute based on an attribute of the super policy component type.
        IPolicyCmptTypeAttribute superPolicyAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superPolicyAttribute.setName("superPolicyAttribute");
        superPolicyAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superPolicyAttribute.setModifier(Modifier.PUBLISHED);
        superPolicyAttribute.setAttributeType(AttributeType.CHANGEABLE);
        superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        superTestAttribute.setAttribute(superPolicyAttribute);
        superTestAttribute.setPolicyCmptType(QUALIFIED_SUPER_POLICY_CMPT_TYPE_NAME);
        superTestAttribute.setName("superPolicyAttribute");

        otherProductCmpt = newProductCmpt(otherProductCmptType, OTHER_PRODUCT_NAME);
        IProductCmptGeneration productCmptGeneration = (IProductCmptGeneration)otherProductCmpt.getFirstGeneration();
        otherProductToProductLink = productCmptGeneration.newLink(otherProductToProductAssociation);
        otherProductToProductLink.setTarget(productCmpt.getQualifiedName());
    }

    protected void checkIpsSourceFiles(String oldName,
            String newName,
            IIpsPackageFragment originalIpsPackageFragment,
            IIpsPackageFragment targetIpsPackageFragment,
            IpsObjectType ipsObjectType) throws CoreException {

        // The old file must no longer exist.
        IIpsSrcFile oldIpsSrcFile = originalIpsPackageFragment.getIpsSrcFile(oldName, ipsObjectType);
        assertFalse(oldIpsSrcFile.exists());

        // Find the new file and validate the IPS object.
        IIpsSrcFile newIpsSrcFile = targetIpsPackageFragment.getIpsSrcFile(newName, ipsObjectType);
        assertTrue(newIpsSrcFile.exists());
        IIpsObject newIpsObject = newIpsSrcFile.getIpsObject();
        assertEquals(newName, newIpsObject.getName());
        assertEquals(targetIpsPackageFragment, newIpsObject.getIpsPackageFragment());
    }

    protected void checkPolicyCmptTypeReferences(String newQualifiedName) {
        // Check for product component type configuration update.
        assertEquals(newQualifiedName, productCmptType.getPolicyCmptType());

        // Check for test parameter and test attribute update.
        assertEquals(newQualifiedName, testPolicyCmptTypeParameter.getPolicyCmptType());
        assertEquals(newQualifiedName, testAttribute.getPolicyCmptType());
        assertEquals(newQualifiedName, testParameterChild1.getPolicyCmptType());
        assertEquals(newQualifiedName, testParameterChild2.getPolicyCmptType());
        assertEquals(newQualifiedName, testParameterChild3.getPolicyCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(newQualifiedName, policyMethod.getParameters()[1].getDatatype());
        assertEquals(newQualifiedName, productMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(newQualifiedName, otherPolicyToPolicyAssociation.getTarget());
    }

    protected void checkSuperPolicyCmptTypeReferences(String newQualifiedName) {
        // Check for test attribute update.
        assertEquals(newQualifiedName, superTestAttribute.getPolicyCmptType());

        // Check for subtype update.
        assertEquals(newQualifiedName, policyCmptType.getSupertype());
    }

    protected void checkProductCmptTypeReferences(String newQualifiedName) {
        // Check for policy component type configuration update.
        assertEquals(newQualifiedName, policyCmptType.getProductCmptType());

        // Check for product component reference update.
        assertEquals(newQualifiedName, productCmpt.getProductCmptType());

        // Check for method parameter update.
        assertEquals(Datatype.INTEGER.getQualifiedName(), policyMethod.getParameters()[0].getDatatype());
        assertEquals(newQualifiedName, productMethod.getParameters()[1].getDatatype());
        assertEquals(newQualifiedName, policyMethod.getParameters()[2].getDatatype());

        // Check for association update.
        assertEquals(newQualifiedName, otherProductToProductAssociation.getTarget());
    }

    protected void checkSuperProductCmptTypeReferences(String newQualifiedName) {
        // Check for subtype update.
        assertEquals(newQualifiedName, productCmptType.getSupertype());
    }

    protected void checkTestCaseTypeReferences(String newQualifiedName) {
        // Check for test case reference update.
        assertEquals(newQualifiedName, testCase.getTestCaseType());
    }

    protected void checkEnumTypeReferences(String newQualifiedName) {
        // Check for enumeration content reference update.
        assertEquals(newQualifiedName, enumContent.getEnumType());
    }

    protected void checkTableStructureReferences(String newQualifiedName) {
        // Check for table contents reference update.
        assertEquals(newQualifiedName, tableContents.getTableStructure());
    }

    /**
     * @param newQualifiedName qualified name
     */
    protected void checkBusinessFunctionReferences(String newQualifiedName) {
        // Currently there are no known business function references.
    }

    protected void checkProductCmptReferences(String newQualifiedName) {
        // Check for update of referring product component generation.
        IProductCmptGeneration generation = (IProductCmptGeneration)otherProductCmpt.getFirstGeneration();
        IProductCmptLink[] links = generation.getLinks();
        assertEquals(1, links.length);
        assertEquals(newQualifiedName, links[0].getTarget());
    }

    /**
     * @param newQualifiedName qualified name
     */
    protected void checkTestCaseReferences(String newQualifiedName) {
        // Currently there are no known test case references.
    }

    /**
     * @param newQualifiedName qualified name
     */
    protected void checkEnumContentReferences(String newQualifiedName) {
        // Currently there are no known enumeration content references.
    }

    /**
     * @param newQualifiedName qualified name
     */
    protected void checkTableContentsReferences(String newQualifiedName) {
        // Currently there are no known table contents references.
    }

    protected abstract ProcessorBasedRefactoring getRefactoring(IIpsElement ipsElement);

}
