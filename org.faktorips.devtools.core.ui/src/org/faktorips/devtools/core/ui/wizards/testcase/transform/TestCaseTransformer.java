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

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcase.TestRuleViolationType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Class to transform runtime test cases to the model test case format. The imported model test
 * cases could be read and handled by the test case editor.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTransformer implements IWorkspaceRunnable {

    private static final Object RUNTIME_TEST_CASES_EXTENSION = "xml"; //$NON-NLS-1$

    private ITestCaseType type;

    private Exception lastException;

    private int importedTestCase;

    IStructuredSelection selection;
    IIpsPackageFragment targtePackage;
    String testCaseTypeName;
    String nameExtension;

    private IIpsProject ipsProject;

    // store test attributes for each test policy cmpt to check if the attribute is unique
    // thus if there are more then one elements for the same test parameter then the
    // attributes will be added to the correct test policy cmpt
    // because the input and expected are stored in different paremt elements
    // but must be put together in one
    private List<String> uniqueTestAttributeValues = new ArrayList<String>();

    public TestCaseTransformer(IStructuredSelection selection, IIpsPackageFragment targtePackage,
            String testCaseTypeName, String nameExtension) {
        super();
        this.selection = selection;
        this.targtePackage = targtePackage;
        this.testCaseTypeName = testCaseTypeName;
        this.nameExtension = nameExtension;
        lastException = null;
        importedTestCase = 0;
        ipsProject = targtePackage.getIpsProject();
    }

    /**
     * Gets the count of imported test cases.
     */
    public int getImportedTestCase() {
        return importedTestCase;
    }

    /**
     * Returns the last exception if there was one.
     */
    public Exception getLastException() {
        return lastException;
    }

    /**
     * Creates and returns a test case object from the given runetime test case xml file.
     * 
     * @param file the test case runtime file (xml formated) which will be transformed to the model
     *            format
     * @param testCaseTypeName the test case type which is the template for the given test case
     * @param root the root of the target package
     * @param targetPackageName the target for the new transformed model test case
     * @param nameExtension (optional) the extension which will be added to the transformed test
     *            case file name
     * 
     * @throws Exception if an error occurs
     */
    public ITestCase createTestCaseFromRuntimeXml(IFile file,
            String testCaseTypeName,
            IIpsPackageFragmentRoot root,
            String targetPackageName,
            String nameExtension) throws CoreException {
        if (nameExtension == null) {
            nameExtension = ""; //$NON-NLS-1$
        }

        Document doc;
        try {
            doc = XmlUtil.getDocument(file.getContents());
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
        Element testCaseElem = XmlUtil.getFirstElement(doc, "TestCase"); //$NON-NLS-1$
        if (testCaseElem == null) {
            // test case node not found, this file is no test case
            return null;
        }

        String testCaseName = file.getName().substring(0, file.getName().indexOf(file.getFileExtension()) - 1)
                + nameExtension;

        ITestCase newTestCase = createNewTestCase(root, targetPackageName, testCaseName);

        // if no test case name is specified in the dialog then use the test case type stored
        // in the file
        if (testCaseTypeName.length() == 0) {
            testCaseTypeName = testCaseElem.getAttribute("testCaseType"); //$NON-NLS-1$
            newTestCase.setTestCaseType(testCaseTypeName);
        } else {
            newTestCase.setTestCaseType(testCaseTypeName);
        }

        // assert test case type exists
        type = (ITestCaseType)root.getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseTypeName);
        if (type == null) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_TestCaseType_Not_Found,
                    testCaseTypeName)));
        }

        initTestPolicyCmpts(XmlUtil.getFirstElement(testCaseElem, "Input"), newTestCase, true); //$NON-NLS-1$
        initTestPolicyCmpts(XmlUtil.getFirstElement(testCaseElem, "ExpectedResult"), newTestCase, false); //$NON-NLS-1$

        newTestCase.setDescription(DescriptionHelper.getDescription(testCaseElem));

        newTestCase.getIpsSrcFile().save(true, null);

        return newTestCase;
    }

    private void initTestPolicyCmpts(Element parent, ITestCase testCase, boolean isInput) throws CoreException {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element elem = (Element)nl.item(i);
                if ("testvalue".equals(elem.getAttribute("type"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    parseTestValue(elem, testCase.newTestValue());
                } else if ("testrule".equals(elem.getAttribute("type"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    parseTestRule(elem, testCase.newTestRule());
                } else {
                    parseDefault(elem, testCase, isInput);
                }
            }
        }
    }

    private void parseDefault(Element elem, ITestCase testCase, boolean isInput) throws CoreException {
        String testPolicyCmptName = elem.getNodeName();
        ITestPolicyCmpt testPolicyCmptFound = null;
        ITestPolicyCmpt[] pcs = testCase.getTestPolicyCmpts();
        for (ITestPolicyCmpt pc : pcs) {
            if (pc.getTestPolicyCmptTypeParameter().equals(testPolicyCmptName)) {
                testPolicyCmptFound = pc;
                break;
            }
        }
        if (testPolicyCmptFound == null) {
            // new test policy cmpt
            parseTestPolicyCmpt(elem, testCase.newTestPolicyCmpt(), isInput);
        } else {
            // the element was already parsed, e.g. this could be the expecpected result attribute
            // for an already imported test policy cmpt
            ITestPolicyCmptTypeParameter param = testPolicyCmptFound.findTestPolicyCmptTypeParameter(ipsProject);
            if (param != null) {
                if (isInput
                        && param.getTestParameterType().equals(TestParameterType.INPUT)
                        && (!isInput && param.isInputOrCombinedParameter() || (isInput && param
                                .isExpextedResultOrCombinedParameter()))) {
                    // the runtime xml contains and input or expected policy cmpt,
                    // but the corresponding test parameter doesn't specifies such type
                    // here!
                    throw new CoreException(new IpsStatus(NLS.bind(
                            Messages.TestCaseTransformer_Error_WrongTypeOfTestPolicyCmpt, new String[] {
                                    getTestParameterTypeName(isInput), testPolicyCmptName,
                                    param.getTestParameterType().toString() })));
                }
                parseTestPolicyCmptChilds(elem, testPolicyCmptFound, isInput);
            }
        }
    }

    private void parseTestValue(Element element, ITestValue testValue) {
        testValue.setTestValueParameter(element.getNodeName());
        if (XmlUtil.getTextNode(element) == null) {
            testValue.setValue(null);
        } else {
            testValue.setValue(XmlUtil.getTextNode(element).getData());
        }
    }

    private void parseTestRule(Element element, ITestRule testRule) {
        testRule.setValidationRule(element.getAttribute("validationRule")); //$NON-NLS-1$
        TestRuleViolationType violationType = TestRuleViolationType.getTestRuleViolationType(element
                .getAttribute("violationType")); //$NON-NLS-1$
        testRule.setViolationType(violationType);
        testRule.setTestRuleParameter(element.getNodeName());
    }

    private void parseTestPolicyCmpt(Element element, ITestPolicyCmpt testPolicyCmpt, boolean isInput)
            throws CoreException {
        // init test policy component
        String policyCmpt = element.getNodeName();
        String runtimeId = element.getAttribute("productCmpt"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(runtimeId)) {
            // the product cmpt for the runtime test case is the runtime id,
            // therefore search the product and store the qualified name
            String uniqueLabel = ""; //$NON-NLS-1$
            IProductCmpt productCmpt = testPolicyCmpt.getIpsProject().findProductCmptByRuntimeId(runtimeId);
            if (productCmpt != null) {
                uniqueLabel = productCmpt.getName();
                testPolicyCmpt.setProductCmpt(productCmpt.getQualifiedName());
            } else {
                // error product cmpt not found, store at least the runtime id
                uniqueLabel = runtimeId;
                testPolicyCmpt.setProductCmpt(runtimeId);
            }

            /*
             * setProductCmptAndNameAfterIfApplicable() is not called because of the potential
             * naming after the runtime-id.
             */
            ITestCase testCase = testPolicyCmpt.getTestCase();
            uniqueLabel = testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, uniqueLabel);
            testPolicyCmpt.setName(uniqueLabel);
        } else {
            testPolicyCmpt.setName(policyCmpt);
        }
        testPolicyCmpt.setTestPolicyCmptTypeParameter(policyCmpt);

        parseTestPolicyCmptChilds(element, testPolicyCmpt, isInput);
    }

    private String getTestAttributeName(ITestPolicyCmpt testPolicyCmpt, String attributeName, boolean isInput)
            throws CoreException {
        // try to find the name of attribute in the test case type
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = testPolicyCmpt
                .findTestPolicyCmptTypeParameter(ipsProject);
        if (testPolicyCmptTypeParam != null) {
            ITestAttribute[] testAttributes = testPolicyCmptTypeParam.getTestAttributes();
            for (ITestAttribute testAttribute : testAttributes) {
                if (testAttribute.getAttribute().equals(attributeName)) {
                    if (testAttribute.getTestAttributeType() == TestParameterType.INPUT && isInput
                            || testAttribute.getTestAttributeType() == TestParameterType.EXPECTED_RESULT && !isInput) {
                        return testAttribute.getName();
                    }
                }
            }
            logError(testPolicyCmpt.getIpsObject().getQualifiedName(), NLS.bind(
                    Messages.TestCaseTransformer_Error_TestAttributeWithTypeNotFound, attributeName,
                    getShortTestParameterTypeName(isInput)));
            return null;
        } else {
            logError(testPolicyCmpt.getIpsObject().getQualifiedName(), NLS.bind(
                    Messages.TestCaseTransformer_Error_TestPolicyCmptTypeNotFound, testPolicyCmpt
                            .getTestPolicyCmptTypeParameter()));
        }
        return null;
    }

    private void parseTestPolicyCmptChilds(Element element, ITestPolicyCmpt testPolicyCmpt, boolean isInput)
            throws CoreException {
        // read childs
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element child = (Element)nl.item(i);
                if (child.getAttribute("type") == null) { //$NON-NLS-1$
                    // no type given do nothing
                    logError(testPolicyCmpt.getIpsObject().getQualifiedName(),
                            Messages.TestCaseTransformer_Error_NoTypeAttributeSpecified);
                } else if (child.getAttribute("type").equals("property")) { //$NON-NLS-1$ //$NON-NLS-2$
                    parsePropertyType(testPolicyCmpt, child, isInput);
                } else if (child.getAttribute("type").equals("composite")) { //$NON-NLS-1$ //$NON-NLS-2$
                    // this is a child policy component
                    parseCompositeType(testPolicyCmpt, child, isInput);
                }
            }
        }
    }

    private void parseCompositeType(ITestPolicyCmpt testPolicyCmpt, Element child, boolean isInput)
            throws CoreException {
        if (!isInput) {
            // merge expected results into input elements
            ITestPolicyCmptLink[] links = testPolicyCmpt.getTestPolicyCmptLinks(child.getNodeName());
            if (links.length > 0) {
                String productCmpt = child.getAttribute("productCmpt"); //$NON-NLS-1$
                ITestPolicyCmptLink currlink = links[0];

                for (ITestPolicyCmptLink link : links) {
                    // the name is equal compare the product cmpt
                    IProductCmpt pc = link.findTarget().findProductCmpt(ipsProject);
                    if (pc != null && StringUtils.isNotEmpty(productCmpt)) {
                        if (productCmpt.equals(pc.getRuntimeId())) {
                            currlink = link;
                            break;
                        }
                    }
                }

                parseTestPolicyCmptChilds(child, currlink.findTarget(), isInput);
                return;
            }
        }
        ITestPolicyCmptLink link = testPolicyCmpt.newTestPolicyCmptLink();
        link.setTestPolicyCmptTypeParameter(child.getNodeName());
        parseTestPolicyCmpt(child, link.newTargetTestPolicyCmptChild(), isInput);
    }

    private void parsePropertyType(ITestPolicyCmpt testPolicyCmpt, Element child, boolean isInput) throws CoreException {
        boolean isExtensionAttr = false;
        Text textNode = XmlUtil.getTextNode(child);
        String value = textNode == null ? "" : textNode.getData(); //$NON-NLS-1$
        String testAttributeName = getTestAttributeName(testPolicyCmpt, child.getNodeName(), isInput);
        if (StringUtils.isEmpty(testAttributeName)) {
            // test attribute not found
            // maybe this is an extension attribute
            testAttributeName = child.getNodeName();
            isExtensionAttr = true;
        }

        ITestPolicyCmpt testPolicyCmptToEdit = null;
        if (isAlreadyFound(testPolicyCmpt, testAttributeName)) {
            testPolicyCmptToEdit = getNextTestPolicyCmpt(testPolicyCmpt, testAttributeName);
            if (testPolicyCmptToEdit == null) {
                throw new CoreException(new IpsStatus(NLS.bind(
                        Messages.TestCaseTransformer_Error_DuplicateTestAttributeValue, testAttributeName)));
            }
        } else {
            testPolicyCmptToEdit = testPolicyCmpt;
        }
        ITestAttributeValue testAttrValue = testPolicyCmptToEdit.newTestAttributeValue();

        testAttrValue.setTestAttribute(testAttributeName);
        if (XmlUtil.getTextNode(child) != null) {
            testAttrValue.setValue(value);
        }

        if (isExtensionAttr) {
            return;
        }

        ITestAttribute attr = null;
        try {
            attr = testAttrValue.findTestAttribute(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        if (attr == null) {
            testAttrValue.delete();
        }
    }

    // Returns the next test policy cmpt on which the given attribute wasn't added yet
    private ITestPolicyCmpt getNextTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt, String testAttributeName)
            throws CoreException {
        ITestPolicyCmptLink[] pcs = testPolicyCmpt.getParentTestPolicyCmpt().getTestPolicyCmptLinks(
                testPolicyCmpt.getTestParameterName());

        for (ITestPolicyCmptLink pc : pcs) {
            ITestPolicyCmpt cmpt = pc.findTarget();
            if (!isAlreadyFound(cmpt, testAttributeName)) {
                return cmpt;
            }
        }
        return null;
    }

    /*
     * Creates and returns a new test case object. The given package and name specified the full
     * qualified name of the new test case.
     */
    private ITestCase createNewTestCase(IIpsPackageFragmentRoot root, String packageName, String testCaseName)
            throws CoreException {
        IIpsPackageFragment packageFragment = root.getIpsPackageFragment(packageName);
        if (packageFragment == null) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_PackageFragmentNotFound,
                    packageName)));
        }
        String fileName = IpsObjectType.TEST_CASE.getFileName(testCaseName);
        IIpsSrcFile srcFile = packageFragment.getIpsSrcFile(fileName);
        if (srcFile.exists()) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_FileAlreadyExists,
                    fileName)));
        }
        IIpsSrcFile file = packageFragment.createIpsFile(IpsObjectType.TEST_CASE, testCaseName, true, null);
        return (ITestCase)file.getIpsObject();
    }

    private void transformFolder(IFolder folder,
            IIpsPackageFragmentRoot root,
            String packageName,
            String testCaseTypeName,
            String nameExtension,
            String targetPackage,
            IProgressMonitor monitor) throws CoreException {
        monitor.internalWorked(1);
        if (folder == null) {
            return;
        }

        // create target package if not exists
        if (targetPackage.length() > 0) {
            IIpsPackageFragment packageFragment = root.getIpsPackageFragment(packageName + "." + targetPackage); //$NON-NLS-1$

            if (!packageFragment.exists()) {
                root.createPackageFragment(packageName + "." + targetPackage, false, null); //$NON-NLS-1$
            }
        }

        IResource[] members = folder.members();
        for (IResource member : members) {
            if (member instanceof IFolder) {
                targetPackage = targetPackage.length() > 0 ? targetPackage + "." : ""; //$NON-NLS-1$ //$NON-NLS-2$
                transformFolder((IFolder)member, root, packageName, testCaseTypeName, nameExtension, targetPackage
                        + ((IFolder)member).getName(), monitor);
            } else if (member instanceof IFile) {
                transformFile(
                        (IFile)member,
                        root,
                        packageName + (targetPackage.length() > 0 ? "." + targetPackage : ""), testCaseTypeName, nameExtension, monitor); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    private void transformFile(IFile file,
            IIpsPackageFragmentRoot root,
            String packageName,
            String testCaseTypeName,
            String nameExtension,
            IProgressMonitor monitor) throws CoreException {
        monitor.internalWorked(1);
        if (!file.getFileExtension().equals(RUNTIME_TEST_CASES_EXTENSION)) {
            return;
        }
        createTestCaseFromRuntimeXml(file, testCaseTypeName, root, packageName, nameExtension);
        importedTestCase++;
    }

    private String getTestParameterTypeName(boolean isInput) {
        return isInput ? TestParameterType.INPUT.toString() : TestParameterType.EXPECTED_RESULT.toString();
    }

    private String getShortTestParameterTypeName(boolean isInput) {
        return isInput ? Messages.TestCaseTransformer_MessageTextInput
                : Messages.TestCaseTransformer_MessageTextInputExpectedResult;
    }

    private void logError(String objectName, String message) {
        IpsPlugin.getDefault().getLog().log(
                new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, 0,
                        "TestCaseTransformer: " + objectName + " - " + message, null)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void startTransforming(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.TestCaseTransformer_Job_Title, IProgressMonitor.UNKNOWN);
        for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
            Object selObj = iter.next();
            IIpsPackageFragmentRoot root = targtePackage.getRoot();
            if (selObj instanceof IFile) {
                transformFile((IFile)selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension, monitor);
            } else if (selObj instanceof IFolder) {
                transformFolder((IFolder)selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension,
                        "", monitor); //$NON-NLS-1$
            } else if (selObj instanceof IPackageFragment) {
                if (targtePackage.equals(selObj)) {
                    throw new CoreException(new IpsStatus(
                            Messages.TestCaseTransformer_Error_ImportPackageEqualsTargetPackage));
                }

                transformFolder((IFolder)((IPackageFragment)selObj).getCorrespondingResource(), root, targtePackage
                        .getName(), testCaseTypeName, nameExtension, "", monitor); //$NON-NLS-1$
            } else if (selObj instanceof IPackageFragmentRoot) {
                if (root.equals(selObj)) {
                    throw new CoreException(new IpsStatus(
                            Messages.TestCaseTransformer_Error_ImportPackageEqualsTargetPackage));
                }

                transformFolder((IFolder)((IPackageFragmentRoot)selObj).getCorrespondingResource(), root, targtePackage
                        .getName(), testCaseTypeName, nameExtension, "", monitor); //$NON-NLS-1$
            } else if (selObj instanceof IJavaProject) {
                IJavaProject project = (IJavaProject)selObj;
                IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
                for (IPackageFragmentRoot root2 : roots) {
                    if (root.equals(root2)) {
                        logError(
                                "" + project, NLS.bind(Messages.TestCaseTransformer_Error_Skip_Because_ImportPackageEqualsTargetPackage, root2.getElementName())); //$NON-NLS-1$
                        continue;
                    }
                    transformFolder((IFolder)root2.getCorrespondingResource(), root, targtePackage.getName(),
                            testCaseTypeName, nameExtension, "", monitor); //$NON-NLS-1$
                }
            } else if (selObj instanceof IProject) {
                IResource[] members = ((IProject)selObj).members();
                for (IResource member : members) {
                    if (member instanceof IFile) {
                        transformFile((IFile)selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension,
                                monitor);
                    } else if (member instanceof IFolder) {
                        transformFolder((IFolder)selObj, root, targtePackage.getName(), testCaseTypeName,
                                nameExtension, "", monitor); //$NON-NLS-1$
                    }
                }
            }
        }
        monitor.done();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        startTransforming(monitor);
        if (importedTestCase == 0) {
            IpsPlugin.log(new IpsStatus(IStatus.WARNING, NLS.bind(Messages.TestCaseTransformer_WarningNoTestCasesFound,
                    RUNTIME_TEST_CASES_EXTENSION), null));
        }
    }

    private boolean isAlreadyFound(ITestPolicyCmpt cmpt, String attributeName) {
        String key = cmpt + "#" + attributeName; //$NON-NLS-1$
        if (uniqueTestAttributeValues.contains(key)) {
            return true;
        }
        uniqueTestAttributeValues.add(key);
        return false;
    }
}
