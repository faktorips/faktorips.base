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

package org.faktorips.devtools.core;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides convenient methods to start Faktor-IPS refactorings and provides a basic model.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractIpsRefactoringTest extends AbstractIpsPluginTest {

    protected static final String POLICY_CMPT_TYPE_ATTRIBUTE_NAME = "policyAttribute";

    protected static final String PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME = "productAttribute";

    protected static final String SUPER_POLICY_NAME = "SuperPolicy";

    protected static final String SUPER_PRODUCT_NAME = "SuperProduct";

    protected static final String PACKAGE = "somepackage";

    protected static final String QUALIFIED_POLICY_NAME = PACKAGE + ".Policy";

    protected static final String QUALIFIED_PRODUCT_NAME = PACKAGE + ".Product";

    protected static final String POLICY_NAME = "Policy";

    protected static final String PRODUCT_NAME = "Product";

    protected IIpsProject ipsProject;

    protected IPolicyCmptType superPolicyCmptType;

    protected IProductCmptType superProductCmptType;

    protected IPolicyCmptType policyCmptType;

    protected IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    protected IProductCmptType productCmptType;

    protected IProductCmptTypeAttribute productCmptTypeAttribute;

    protected ITestCaseType testCaseType;

    protected ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

    protected ITestAttribute testAttribute;

    protected IProductCmpt productCmpt;

    protected IProductCmptGeneration productCmptGeneration;

    protected IAttributeValue attributeValue;

    protected IConfigElement productCmptGenerationConfigElement;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create an IPS project.
        ipsProject = newIpsProject();

        // Create super policy component type.
        superPolicyCmptType = newPolicyCmptType(ipsProject, SUPER_POLICY_NAME);
        superPolicyCmptType.setAbstract(true);
        superPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create super product component type.
        superProductCmptType = newProductCmptType(ipsProject, SUPER_PRODUCT_NAME);
        superProductCmptType.setAbstract(true);
        superProductCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setPolicyCmptType(SUPER_POLICY_NAME);
        superPolicyCmptType.setProductCmptType(SUPER_PRODUCT_NAME);

        // Create a policy component type and a product component type.
        policyCmptType = newPolicyCmptType(ipsProject, QUALIFIED_POLICY_NAME);
        productCmptType = newProductCmptType(ipsProject, QUALIFIED_PRODUCT_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(QUALIFIED_PRODUCT_NAME);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(QUALIFIED_POLICY_NAME);
        policyCmptType.setSupertype(SUPER_POLICY_NAME);
        productCmptType.setSupertype(SUPER_PRODUCT_NAME);

        // Create a policy component type attribute.
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        policyCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setProductRelevant(true);

        // Create a product component type attribute.
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();
        productCmptTypeAttribute.setName(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME);
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute.setModifier(Modifier.PUBLISHED);

        // Create a test case type with a test attribute.
        testCaseType = newTestCaseType(ipsProject, "TestCaseType");
        testPolicyCmptTypeParameter = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyCmptTypeParameter.setPolicyCmptType(QUALIFIED_POLICY_NAME);
        testAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        testAttribute.setAttribute(policyCmptTypeAttribute);
        testAttribute.setName("someTestAttribute");
        testAttribute.setPolicyCmptType(QUALIFIED_POLICY_NAME);
    }

    /**
     * Creates a product component with a generation containing an <tt>IConfigElement</tt> for the
     * <tt>policyCmptTypeAttribute</tt> and an <tt>IAttributeValue</tt> for the
     * <tt>productComponentTypeAttribute</tt>.
     */
    protected final void createProductCmpt() throws CoreException {
        productCmpt = newProductCmpt(productCmptType, "ExampleProduct");
        productCmptGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGenerationConfigElement = productCmptGeneration.newConfigElement(policyCmptTypeAttribute);
        attributeValue = productCmptGeneration.newAttributeValue(productCmptTypeAttribute);
    }

    /**
     * Performs the "Rename" refactoring for the given <tt>IIpsElement</tt> and provided new name.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void performRenameRefactoring(IIpsElement ipsElement, String newName) throws CoreException {
        ArgumentCheck.notNull(new Object[] { ipsElement, newName });

        ProcessorBasedRefactoring renameRefactoring = ipsElement.getRenameRefactoring();
        IIpsRenameProcessor processor = (IIpsRenameProcessor)renameRefactoring.getProcessor();
        processor.setNewName(newName);

        runRefactoring(renameRefactoring);
    }

    /**
     * Performs the "Move" refactoring for the given <tt>IIpsElement</tt> and provided target
     * <tt>IIpsPackageFragment</tt>.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    protected final void performMoveRefactoring(IIpsElement ipsElement, IIpsPackageFragment targetIpsPackageFragment)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { ipsElement, targetIpsPackageFragment });

        ProcessorBasedRefactoring moveRefactoring = ipsElement.getMoveRefactoring();
        IIpsMoveProcessor processor = (IIpsMoveProcessor)moveRefactoring.getProcessor();
        processor.setTargetIpsPackageFragment(targetIpsPackageFragment);

        runRefactoring(moveRefactoring);
    }

    /** Actually runs the given refactoring. */
    private void runRefactoring(Refactoring refactoring) throws CoreException {
        PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
    }

    /** Performs a full build. */
    protected final void performFullBuild() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
