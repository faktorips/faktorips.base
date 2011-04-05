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

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;

/**
 * Provides convenient methods to start Faktor-IPS refactorings and provides a basic model.
 * 
 * @author Alexander Weickmann
 */
public abstract class AbstractIpsRefactoringTest extends AbstractIpsPluginTest {

    protected static final String ENUM_ATTRIBUTE_NAME = "id";

    protected static final String POLICY_CMPT_TYPE_ATTRIBUTE_NAME = "policyAttribute";

    protected static final String PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME = "productAttribute";

    protected static final String SUPER_POLICY_CMPT_TYPE_NAME = "SuperPolicyCmptType";

    protected static final String SUPER_PRODUCT_CMPT_TYPE_NAME = "SuperProductCmptType";

    protected static final String PACKAGE_NAME = "somepackage";

    protected static final String POLICY_CMPT_TYPE_NAME = "PolicyCmptType";

    protected static final String PRODUCT_CMPT_TYPE_NAME = "ProductCmptType";

    protected static final String OTHER_POLICY_CMPT_TYPE_NAME = "OtherPolicy";

    protected static final String OTHER_PRODUCT_CMPT_TYPE_NAME = "OtherProductType";

    protected static final String TEST_CASE_TYPE_NAME = "TestCaseType";

    protected static final String PRODUCT_NAME = "Product";

    protected static final String TEST_CASE_NAME = "TestCase";

    protected static final String QUALIFIED_POLICY_CMPT_TYPE_NAME = PACKAGE_NAME + '.' + POLICY_CMPT_TYPE_NAME;

    protected static final String QUALIFIED_SUPER_POLICY_CMPT_TYPE_NAME = PACKAGE_NAME + '.'
            + SUPER_POLICY_CMPT_TYPE_NAME;

    protected static final String QUALIFIED_PRODUCT_CMPT_TYPE_NAME = PACKAGE_NAME + '.' + PRODUCT_CMPT_TYPE_NAME;

    protected static final String QUALIFIED_SUPER_PRODUCT_CMPT_TYPE_NAME = PACKAGE_NAME + '.'
            + SUPER_PRODUCT_CMPT_TYPE_NAME;

    protected static final String ENUM_TYPE_NAME = "EnumType";

    protected static final String ENUM_CONTENT_NAME = "EnumContent";

    protected static final String TABLE_STRUCTURE_NAME = "TableStructure";

    protected static final String TABLE_CONTENTS_NAME = "TableContents";

    protected static final String BUSINESS_FUNCTION_NAME = "BusinessFunction";

    protected IIpsProject ipsProject;

    protected IPolicyCmptType superPolicyCmptType;

    protected IProductCmptType superProductCmptType;

    protected IPolicyCmptType policyCmptType;

    protected IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    protected IProductCmptType productCmptType;

    protected IProductCmptTypeAttribute productCmptTypeAttribute;

    protected IPolicyCmptType otherPolicyCmptType;

    protected IProductCmptType otherProductCmptType;

    protected IPolicyCmptTypeAssociation policyToSelfAssociation;

    protected IPolicyCmptTypeAssociation otherPolicyToPolicyAssociation;

    protected IProductCmptTypeAssociation otherProductToProductAssociation;

    protected ITestCaseType testCaseType;

    protected ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

    protected ITestPolicyCmptTypeParameter testParameterChild1;

    protected ITestPolicyCmptTypeParameter testParameterChild2;

    protected ITestPolicyCmptTypeParameter testParameterChild3;

    protected ITestAttribute testAttribute;

    protected IProductCmpt productCmpt;

    protected IProductCmptGeneration productCmptGeneration;

    protected IAttributeValue attributeValue;

    protected IConfigElement productCmptGenerationConfigElement;

    protected ITestCase testCase;

    protected IEnumType enumType;

    protected IEnumAttribute enumAttribute;

    protected IEnumContent enumContent;

    protected IEnumType valuedEnumType;

    protected IEnumLiteralNameAttributeValue enumLiteralNameAttributeValue;

    protected ITableStructure tableStructure;

    protected ITableContents tableContents;

    protected IBusinessFunction businessFunction;

    @Override
    @Before
    public void setUp() throws Exception {
        IpsPlugin.getDefault().setSuppressLoggingDuringTest(false);
        ((IpsModel)IpsPlugin.getDefault().getIpsModel()).stopListeningToResourceChanges();
        IpsPlugin.getDefault().setFeatureVersionManagers(
                new IIpsFeatureVersionManager[] { new TestIpsFeatureVersionManager() });
        setAutoBuild(false);
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(new GregorianCalendar());

        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Start deleting projects.");
                }
                waitForIndexer();
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                for (IProject project : projects) {
                    project.close(null);
                    project.delete(true, true, null);
                }
                if (IpsModel.TRACE_MODEL_MANAGEMENT) {
                    System.out.println("AbstractIpsPlugin.setUp(): Projects deleted.");
                }
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

        ipsProject = newIpsProject();

        createPolicyAndProductModel();
        createTestModel();
        createEnumModel();
        createTableModel();
        createBusinessModel();

        createProductCmpt();
        createEnumContent();
        createTableContents();
    }

    private void createPolicyAndProductModel() throws CoreException {
        // Create super policy component type.
        superPolicyCmptType = newPolicyCmptType(ipsProject, QUALIFIED_SUPER_POLICY_CMPT_TYPE_NAME);
        superPolicyCmptType.setAbstract(true);
        superPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create super product component type.
        superProductCmptType = newProductCmptType(ipsProject, QUALIFIED_SUPER_PRODUCT_CMPT_TYPE_NAME);
        superProductCmptType.setAbstract(true);
        superProductCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setPolicyCmptType(QUALIFIED_SUPER_POLICY_CMPT_TYPE_NAME);
        superPolicyCmptType.setProductCmptType(QUALIFIED_SUPER_PRODUCT_CMPT_TYPE_NAME);

        // Create a policy component type and a product component type.
        policyCmptType = newPolicyCmptType(ipsProject, QUALIFIED_POLICY_CMPT_TYPE_NAME);
        productCmptType = newProductCmptType(ipsProject, QUALIFIED_PRODUCT_CMPT_TYPE_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(QUALIFIED_PRODUCT_CMPT_TYPE_NAME);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(QUALIFIED_POLICY_CMPT_TYPE_NAME);
        policyCmptType.setSupertype(QUALIFIED_SUPER_POLICY_CMPT_TYPE_NAME);
        productCmptType.setSupertype(QUALIFIED_SUPER_PRODUCT_CMPT_TYPE_NAME);

        // Create a policy component type attribute.
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        policyCmptTypeAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setProductRelevant(true);

        // Create a product component type attribute.
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();
        productCmptTypeAttribute.setName(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME);
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute.setModifier(Modifier.PUBLISHED);

        // Create another policy component type and another product component type.
        otherPolicyCmptType = newPolicyCmptType(ipsProject, OTHER_POLICY_CMPT_TYPE_NAME);
        otherProductCmptType = newProductCmptType(ipsProject, OTHER_PRODUCT_CMPT_TYPE_NAME);

        // Create policy associations.
        policyToSelfAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        policyToSelfAssociation.setTarget(policyCmptType.getQualifiedName());
        policyToSelfAssociation.setTargetRoleSingular("singular");
        policyToSelfAssociation.setTargetRolePlural("plural");
        otherPolicyToPolicyAssociation = otherPolicyCmptType.newPolicyCmptTypeAssociation();
        otherPolicyToPolicyAssociation.setTarget(QUALIFIED_POLICY_CMPT_TYPE_NAME);
        otherPolicyToPolicyAssociation.setTargetRoleSingular(POLICY_CMPT_TYPE_NAME);

        // Create product associations.
        otherProductToProductAssociation = otherProductCmptType.newProductCmptTypeAssociation();
        otherProductToProductAssociation.setTarget(QUALIFIED_PRODUCT_CMPT_TYPE_NAME);
        otherProductToProductAssociation.setTargetRoleSingular(PRODUCT_CMPT_TYPE_NAME);
    }

    private void createTestModel() throws CoreException {
        // Create a test case type with a test attribute.
        testCaseType = newTestCaseType(ipsProject, TEST_CASE_TYPE_NAME);
        testPolicyCmptTypeParameter = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyCmptTypeParameter.setPolicyCmptType(QUALIFIED_POLICY_CMPT_TYPE_NAME);
        testPolicyCmptTypeParameter.setName("testParameter");
        testAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        testAttribute.setAttribute(policyCmptTypeAttribute);
        testAttribute.setName("someTestAttribute");
        testAttribute.setPolicyCmptType(QUALIFIED_POLICY_CMPT_TYPE_NAME);

        // Create some child test parameters.
        testParameterChild1 = testPolicyCmptTypeParameter.newTestPolicyCmptTypeParamChild();
        testParameterChild1.setPolicyCmptType(QUALIFIED_POLICY_CMPT_TYPE_NAME);
        testParameterChild1.setName("child1");
        testParameterChild1.setAssociation(policyToSelfAssociation.getName());
        testParameterChild2 = testParameterChild1.newTestPolicyCmptTypeParamChild();
        testParameterChild2.setPolicyCmptType(QUALIFIED_POLICY_CMPT_TYPE_NAME);
        testParameterChild2.setName("child2");
        testParameterChild2.setAssociation(policyToSelfAssociation.getName());
        testParameterChild3 = testParameterChild2.newTestPolicyCmptTypeParamChild();
        testParameterChild3.setPolicyCmptType(QUALIFIED_POLICY_CMPT_TYPE_NAME);
        testParameterChild3.setName("child3");
        testParameterChild3.setAssociation(policyToSelfAssociation.getName());

        // Create a test case based on the test case type.
        testCase = newTestCase(ipsProject, TEST_CASE_NAME);
        testCase.setTestCaseType(testCaseType.getQualifiedName());
    }

    private void createEnumModel() throws CoreException {
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setEnumContentName(ENUM_CONTENT_NAME);
        enumType.setContainingValues(false);
        enumType.setAbstract(false);
        enumAttribute = enumType.newEnumAttribute();
        enumAttribute.setName(ENUM_ATTRIBUTE_NAME);
        enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        enumAttribute.setIdentifier(true);
        enumAttribute.setUnique(true);
        enumAttribute.setUsedAsNameInFaktorIpsUi(true);

        valuedEnumType = newEnumType(ipsProject, "ValuedEnumType");
        valuedEnumType.setAbstract(false);
        valuedEnumType.setContainingValues(true);

        IEnumAttribute idAttribute = valuedEnumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);

        IEnumAttribute nameAttribute = valuedEnumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUnique(true);
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);

        IEnumLiteralNameAttribute literalAttribute = valuedEnumType.newEnumLiteralNameAttribute();
        literalAttribute.setDefaultValueProviderAttribute("name");

        IEnumValue enumValue = valuedEnumType.newEnumValue();
        enumValue.setEnumAttributeValue(0, "0");
        enumValue.setEnumAttributeValue(1, "foo");
        enumValue.setEnumAttributeValue(2, "FOO");

        enumLiteralNameAttributeValue = enumValue.getEnumLiteralNameAttributeValue();
    }

    private void createTableModel() throws CoreException {
        tableStructure = newTableStructure(ipsProject, TABLE_STRUCTURE_NAME);
        tableStructure.setTableStructureType(TableStructureType.SINGLE_CONTENT);
    }

    private void createBusinessModel() throws CoreException {
        businessFunction = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                BUSINESS_FUNCTION_NAME);
        businessFunction.newStart(new Point(0, 0));
        businessFunction.newEnd(new Point(10, 10));
        IControlFlow controlFlow = businessFunction.newControlFlow();
        controlFlow.setSource(businessFunction.getStart());
        controlFlow.setTarget(businessFunction.getEnd());
    }

    private void createProductCmpt() throws CoreException {
        productCmpt = newProductCmpt(productCmptType, PRODUCT_NAME);
        productCmptGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGeneration.setValidFrom(new GregorianCalendar(2010, 3, 10));
        productCmptGenerationConfigElement = productCmptGeneration.newConfigElement(policyCmptTypeAttribute);
        attributeValue = productCmptGeneration.newAttributeValue(productCmptTypeAttribute);
    }

    private void createEnumContent() throws CoreException {
        enumContent = newEnumContent(ipsProject, ENUM_CONTENT_NAME);
        enumContent.setEnumType(enumType.getQualifiedName());
    }

    private void createTableContents() throws CoreException {
        tableContents = newTableContents(ipsProject, TABLE_CONTENTS_NAME);
        tableContents.setTableStructure(tableStructure.getQualifiedName());
    }

    /**
     * Performs the "Rename" refactoring for the given {@link IIpsObjectPartContainer} and provided
     * new name.
     * 
     * @return
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName) throws CoreException {

        return performRenameRefactoring(ipsObjectPartContainer, newName, null, false);
    }

    protected final RefactoringStatus performRenameRefactoring(IProductCmpt productCmpt,
            String newName,
            boolean adaptRuntimeId) throws CoreException {

        return performRenameRefactoring(productCmpt, newName, null, adaptRuntimeId);
    }

    /**
     * Performs the "Rename" refactoring for the given {@link IIpsObjectPartContainer}, provided new
     * name and provided new plural name.
     * 
     * @return
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName,
            String newPluralName) throws CoreException {

        return performRenameRefactoring(ipsObjectPartContainer, newName, newPluralName, false);
    }

    private RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName,
            String newPluralName,
            boolean adaptRuntimeId) throws CoreException {

        printValidationResult(ipsObjectPartContainer);

        saveIpsSourceFiles();

        ProcessorBasedRefactoring renameRefactoring = ipsObjectPartContainer.getRenameRefactoring();
        IIpsRenameProcessor processor = (IIpsRenameProcessor)renameRefactoring.getProcessor();
        processor.setNewName(newName);

        if (newPluralName != null) {
            processor.setNewPluralName(newPluralName);
        }

        processor.setAdaptRuntimeId(adaptRuntimeId);

        return runRefactoring(renameRefactoring);
    }

    protected final RefactoringStatus performMoveRefactoring(IProductCmpt productCmpt,
            IIpsPackageFragment targetIpsPackageFragment,
            boolean adaptRuntimeId) throws CoreException {

        return performMoveRefactoring((IIpsObjectPartContainer)productCmpt, targetIpsPackageFragment, adaptRuntimeId);
    }

    /**
     * Performs the "Move" refactoring for the given {@link IIpsObjectPartContainer} and provided
     * target {@link IIpsPackageFragment}.
     * 
     * @return
     */
    protected final RefactoringStatus performMoveRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            IIpsPackageFragment targetIpsPackageFragment) throws CoreException {

        return performMoveRefactoring(ipsObjectPartContainer, targetIpsPackageFragment, false);
    }

    private RefactoringStatus performMoveRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            IIpsPackageFragment targetIpsPackageFragment,
            boolean adaptRuntimeId) throws CoreException {

        printValidationResult(ipsObjectPartContainer);

        saveIpsSourceFiles();

        ProcessorBasedRefactoring moveRefactoring = ipsObjectPartContainer.getMoveRefactoring();
        IIpsMoveProcessor processor = (IIpsMoveProcessor)moveRefactoring.getProcessor();
        processor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        processor.setAdaptRuntimeId(adaptRuntimeId);

        return runRefactoring(moveRefactoring);
    }

    /**
     * The created model must be saved before refactoring is done because source files are copied
     * during rename or move refactorings.
     */
    private void saveIpsSourceFiles() throws CoreException {
        List<IIpsSrcFile> ipsSrcFiles = new ArrayList<IIpsSrcFile>();

        ipsSrcFiles.add(superPolicyCmptType.getIpsSrcFile());
        ipsSrcFiles.add(superProductCmptType.getIpsSrcFile());
        ipsSrcFiles.add(policyCmptType.getIpsSrcFile());
        ipsSrcFiles.add(productCmptType.getIpsSrcFile());
        ipsSrcFiles.add(testCaseType.getIpsSrcFile());
        ipsSrcFiles.add(enumType.getIpsSrcFile());
        ipsSrcFiles.add(tableStructure.getIpsSrcFile());
        ipsSrcFiles.add(businessFunction.getIpsSrcFile());

        ipsSrcFiles.add(productCmpt.getIpsSrcFile());
        ipsSrcFiles.add(enumContent.getIpsSrcFile());
        ipsSrcFiles.add(testCase.getIpsSrcFile());
        ipsSrcFiles.add(tableContents.getIpsSrcFile());

        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            ipsSrcFile.save(true, null);
        }
    }

    private void printValidationResult(IIpsObjectPartContainer ipsObjectPartContainer) throws CoreException {
        MessageList validationResult = ipsObjectPartContainer.validate(ipsProject);
        if (validationResult.containsErrorMsg()) {
            System.out.println(validationResult.getFirstMessage(Message.ERROR));
        }
    }

    private RefactoringStatus runRefactoring(Refactoring refactoring) throws CoreException {
        PerformRefactoringOperation operation = new PerformRefactoringOperation(refactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
        RefactoringStatus validationStatus = operation.getValidationStatus();
        System.out.println(validationStatus);
        return validationStatus;
    }

    protected final void performFullBuild() throws CoreException {
        clearOutputFolders(); // To avoid code merging problems
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    private void clearOutputFolders() throws CoreException {
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        if (ipsObjectPath.isOutputDefinedPerSrcFolder()) {
            for (IIpsSrcFolderEntry srcFolderEntry : ipsObjectPath.getSourceFolderEntries()) {
                IFolder outputFolderDerived = srcFolderEntry.getOutputFolderForDerivedJavaFiles();
                IFolder outputFolderMergable = srcFolderEntry.getOutputFolderForMergableJavaFiles();
                clearFolder(outputFolderDerived);
                clearFolder(outputFolderMergable);
            }
        } else {
            IFolder outputFolderDerived = ipsObjectPath.getOutputFolderForDerivedSources();
            IFolder outputFolderMergable = ipsObjectPath.getOutputFolderForMergableSources();
            clearFolder(outputFolderDerived);
            clearFolder(outputFolderMergable);
        }
    }

    private void clearFolder(IFolder folder) throws CoreException {
        for (IResource resource : folder.members()) {
            resource.delete(true, null);
        }
    }

}
