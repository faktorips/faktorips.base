/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcase.transform;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestCaseTestCaseTypeDelta;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
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

/**
 * Class to transform runtime test cases to the model test case format.
 * The imported model test cases could be read and handled by the test case editor.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTransformer {
    
    private TransformerJob job;

    private ITestCaseType type;

    private Exception lastException;
    
    private int importedTestCase;

    /*
     * Job class to run the transformation.
     */
    private class TransformerJob extends WorkspaceJob {
        IStructuredSelection selection;
        IIpsPackageFragment targtePackage; 
        String testCaseTypeName;
        String nameExtension;
        
        public TransformerJob(IStructuredSelection selection, 
                IIpsPackageFragment targtePackage, String testCaseTypeName, String nameExtension){
            super(Messages.TestCaseTransformer_Job_Title);
            this.selection = selection;
            this.targtePackage = targtePackage;
            this.testCaseTypeName = testCaseTypeName;
            this.nameExtension = nameExtension;
        }
        
        public IStatus runInWorkspace(IProgressMonitor monitor) {
            try {
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object selObj = (Object) iter.next();
                    IIpsPackageFragmentRoot root = targtePackage.getRoot();
                    if (selObj instanceof IFile){
                        transformFile((IFile) selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension);
                    } else if (selObj instanceof IFolder){
                        transformFolder((IFolder) selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension, ""); //$NON-NLS-1$
                    } else if (selObj instanceof IPackageFragment){
                        if (targtePackage.equals((IPackageFragment) selObj))
                            throw new CoreException(new IpsStatus(Messages.TestCaseTransformer_Error_ImportPackageEqualsTargetPackage));

                        transformFolder((IFolder) ((IPackageFragment) selObj).getCorrespondingResource(), root, 
                                targtePackage.getName(), testCaseTypeName, nameExtension, ""); //$NON-NLS-1$
                    } else if (selObj instanceof IPackageFragmentRoot){
                        if (root.equals((IPackageFragmentRoot) selObj))
                            throw new CoreException(new IpsStatus(Messages.TestCaseTransformer_Error_ImportPackageEqualsTargetPackage));

                        transformFolder((IFolder) ((IPackageFragmentRoot) selObj).getCorrespondingResource(), root, 
                                targtePackage.getName(), testCaseTypeName, nameExtension, ""); //$NON-NLS-1$
                    } else if (selObj instanceof IJavaProject){
                        IJavaProject project = (IJavaProject) selObj;
                        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
                        for (int i = 0; i < roots.length; i++) {
                            if (root.equals(roots[i])){
                                IpsPlugin.log(new IpsStatus(
                                        NLS.bind(Messages.TestCaseTransformer_Error_Skip_Because_ImportPackageEqualsTargetPackage, roots[i].getElementName())));
                                continue;
                            }
                            transformFolder((IFolder) roots[i].getCorrespondingResource(), root, 
                                    targtePackage.getName(), testCaseTypeName, nameExtension, ""); //$NON-NLS-1$
                        }
                    } else if (selObj instanceof IProject){
                        IResource[] members = ((IProject) selObj).members();
                        for (int i = 0; i < members.length; i++) {
                            if (members[i] instanceof IFile)
                                transformFile((IFile) selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension);
                            else if (members[i] instanceof IFolder)
                                transformFolder((IFolder) selObj, root, targtePackage.getName(), testCaseTypeName, nameExtension, ""); //$NON-NLS-1$
                        }
                    }
                }
            } catch (Exception e) {
                lastException = e;
                return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
        }
    }    

    /**
     * Returns the job which performs the transformation.
     */
    public TransformerJob getJob() {
        return job;
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
     * @param file the test case runtime file (xml formated) which will be transformed to the model format
     * @param testCaseTypeName the test case type which is the template for the given test case
     * @param root the root of the target package
     * @param targetPackageName the target for the new transformed model test case
     * @param nameExtension (optional) the extension which will be added to the transformed test case file name
     * 
     * @throws Exception if an error occurs
     */
    public ITestCase createTestCaseFromRuntimeXml(IFile file, String testCaseTypeName, 
    		IIpsPackageFragmentRoot root, String targetPackageName, String nameExtension) throws Exception{
        if (nameExtension == null){
            nameExtension = ""; //$NON-NLS-1$
        }
        
        Document doc = XmlUtil.getDocument(file.getContents());
        Element elem = XmlUtil.getFirstElement(doc, "TestCase"); //$NON-NLS-1$
        if (elem == null){
            // test case node not found, this file is no test case
            return null;
        }
        
        String testCaseName = file.getName().substring(0, 
                file.getName().indexOf(file.getFileExtension()) - 1) + nameExtension;
        
        type = (ITestCaseType)root.getIpsProject().findIpsObject(IpsObjectType.TEST_CASE_TYPE, testCaseTypeName);
        if (type == null){
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_TestCaseType_Not_Found,testCaseTypeName)));
        }
        
        ITestCase newTestCase = createNewTestCase(root, targetPackageName, testCaseName);
        newTestCase.setTestCaseType(testCaseTypeName);        
        
        initTestPolicyCmpts(XmlUtil.getFirstElement(elem, "Input"), newTestCase, true); //$NON-NLS-1$
        initTestPolicyCmpts(XmlUtil.getFirstElement(elem, "ExpectedResult"), newTestCase, false); //$NON-NLS-1$
        
        // fix differences, e.g. different sort order which couldn't be determined from the runtime xml
        // (because the input and expected result are in different parent nodes, therfore the objects will be imported in this
        // wrong sort order)
        ITestCaseTestCaseTypeDelta delta = newTestCase.computeDeltaToTestCaseType();
        newTestCase.fixDifferences(delta);

        newTestCase.getIpsSrcFile().save(true, null);
        
        return newTestCase;
    }
    
    private void initTestPolicyCmpts(Element parent, ITestCase testCase, boolean isInput) throws CoreException {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element elem = (Element) nl.item(i);
                if ("testvalue".equals(elem.getAttribute("type"))){ //$NON-NLS-1$ //$NON-NLS-2$
                    parseTestValue(elem, testCase.newTestValue());
                } else if ("testrule".equals(elem.getAttribute("type"))){ //$NON-NLS-1$ //$NON-NLS-2$
                    parseTestRule(elem, testCase.newTestRule());
                } else {
                    String testPolicyCmptName = elem.getNodeName();
                    ITestPolicyCmpt testPolicyCmpt = null;
                    ITestPolicyCmpt[] pcs = testCase.getTestPolicyCmpts();
                    for (int j = 0; j < pcs.length; j++) {
                        if (pcs[j].getTestPolicyCmptTypeParameter().equals(testPolicyCmptName)){
                            testPolicyCmpt = pcs[j];
                            break;
                        }
                    }
                    if (testPolicyCmpt == null){
                        // new test policy cmpt
                        parseTestPolicyCmpt(elem, testCase.newTestPolicyCmpt(), isInput);
                    } else {
                        // the element was already parsed, e.g. this could be the expecpected result attribute for an already imported test policy cmpt
                        ITestPolicyCmptTypeParameter param = testPolicyCmpt.findTestPolicyCmptTypeParameter();
                        if (param != null){
                            if (isInput && param.getTestParameterType().equals(TestParameterType.INPUT) && 
                                    (!isInput && param.isInputParameter() || (isInput && param
                                    .isExpextedResultParameter()))) {
                                // the runtime xml contains and input or expected policy cmpt,
                                // but the corresponding test parameter doesn't specifies such type
                                // here!
                                throw new CoreException(new IpsStatus(NLS.bind(
                                        Messages.TestCaseTransformer_Error_WrongTypeOfTestPolicyCmpt,
                                        new String[] {
                                                getTestParameterTypeName(isInput),
                                                testPolicyCmptName, param.getTestParameterType().toString() })));
                            }
                            parseTestPolicyCmptChilds(elem, testPolicyCmpt, isInput);
                        }
                    }
                }
            }
        }
    }
    
    private void parseTestValue(Element element, ITestValue testValue){
        if (XmlUtil.getTextNode(element) == null){
            return;
        }
        testValue.setValue(XmlUtil.getTextNode(element).getData());
        testValue.setTestValueParameter(element.getNodeName());
    }
    
    private void parseTestRule(Element element, ITestRule testRule){
        testRule.setValidationRule(element.getAttribute("validationRule")); //$NON-NLS-1$
        TestRuleViolationType violationType = TestRuleViolationType.getTestRuleViolationType(element.getAttribute("violationType")); //$NON-NLS-1$
        testRule.setViolationType(violationType);
        testRule.setTestRuleParameter(element.getNodeName());
    }
    
    private void parseTestPolicyCmpt(Element element, ITestPolicyCmpt testPolicyCmpt, boolean isInput) throws CoreException{
        // init test policy component
        String policyCmpt = element.getNodeName();
        String runtimeId = element.getAttribute("productCmpt"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(runtimeId)){
            // the product cmpt for the runtime test case is the runtime id,
            // therefore search the product and store the qualified name
            String uniqueLabel = ""; //$NON-NLS-1$
            IProductCmpt productCmpt = testPolicyCmpt.getIpsProject().findProductCmptByRuntimeId(runtimeId);
            if (productCmpt != null){
                uniqueLabel = productCmpt.getName();
                testPolicyCmpt.setProductCmpt(productCmpt.getQualifiedName());
            } else {
                // error product cmpt not found, store at least the runtime id
                uniqueLabel = runtimeId;
                testPolicyCmpt.setProductCmpt(runtimeId);
            }
            
            ITestCase testCase = testPolicyCmpt.getTestCase();
            uniqueLabel = testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, uniqueLabel);
            testPolicyCmpt.setName(uniqueLabel);
        }else{
            testPolicyCmpt.setName(policyCmpt);
        }
        testPolicyCmpt.setTestPolicyCmptTypeParameter(policyCmpt);
        
        parseTestPolicyCmptChilds(element, testPolicyCmpt, isInput);
    }

    private String getTestAttributeName(ITestPolicyCmpt testPolicyCmpt, String attributeName, boolean isInput)
            throws CoreException {
        // try to find the name of attribute in the test case type
        String testAttributeName = ""; //$NON-NLS-1$
        ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = testPolicyCmpt.findTestPolicyCmptTypeParameter();
        ITestAttribute[] testAttributes = testPolicyCmptTypeParam.getTestAttributes();
        for (int j = 0; j < testAttributes.length; j++) {
            if (testAttributes[j].getAttribute().equals(attributeName)) {
                if (testAttributes[j].getTestAttributeType() == TestParameterType.INPUT && isInput
                        || testAttributes[j].getTestAttributeType() == TestParameterType.EXPECTED_RESULT && !isInput) {
                    testAttributeName = testAttributes[j].getName();
                    break;
                }
            }
        }
        return testAttributeName;
    }
    
    private void parseTestPolicyCmptChilds(Element element, ITestPolicyCmpt testPolicyCmpt, boolean isInput) throws CoreException {
        // read childs
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element child = (Element) nl.item(i);
                if (child.getAttribute("type") == null){ //$NON-NLS-1$
                    // no type given do nothing 
                    IpsPlugin.log(new IpsStatus(Messages.TestCaseTransformer_Error_NoTypeAttributeSpecified));
                } else if (child.getAttribute("type").equals("property")){ //$NON-NLS-1$ //$NON-NLS-2$
                    ITestAttributeValue testAttrValue = testPolicyCmpt.newTestAttributeValue();
                    String testAttributeName = getTestAttributeName(testPolicyCmpt, child.getNodeName(), isInput);
                    if (StringUtils.isEmpty(testAttributeName)) {
                        throw new CoreException(new IpsStatus(NLS.bind(
                                Messages.TestCaseTransformer_Error_TestAttributeNotExists, child
                                        .getNodeName(), getTestParameterTypeName(isInput))));
                    }
                    testAttrValue.setTestAttribute(testAttributeName);
                    if (XmlUtil.getTextNode(child) != null) {
                        testAttrValue.setValue(XmlUtil.getTextNode(child).getData());
                    }
                    
                    ITestAttribute attr = null;
                    try {
                        attr = testAttrValue.findTestAttribute();
                    }
                    catch (CoreException e) {
                        // nothing to do
                    }
                    
                    if (attr == null) {
                        testAttrValue.delete();
                    }
                    
                }else if (child.getAttribute("type").equals("composite")){ //$NON-NLS-1$ //$NON-NLS-2$
                    // this is a child policy component
                    if (! isInput){
                        // merge expected results into input elements
                        ITestPolicyCmptRelation[] relations = testPolicyCmpt.getTestPolicyCmptRelations(child.getNodeName());
                        if (relations.length>0){
                            parseTestPolicyCmptChilds(child, relations[0].findTarget(), isInput);
                            continue;
                        }
                    }
                    ITestPolicyCmptRelation relation = testPolicyCmpt.newTestPolicyCmptRelation();
                    relation.setTestPolicyCmptTypeParameter(child.getNodeName());
                    parseTestPolicyCmpt(child, relation.newTargetTestPolicyCmptChild(), isInput);
                
                }
            }
        }
    }
    
    /*
     * Creates and returns a new test case object. The given package and name specified the full qualified name
     * of the new test case.
     */
    private ITestCase createNewTestCase(IIpsPackageFragmentRoot root, String packageName, String testCaseName) throws CoreException{
        IIpsPackageFragment packageFragment = root.getIpsPackageFragment(packageName);
        if (packageFragment == null){
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_PackageFragmentNotFound, packageName))); //$NON-NLS-2$
        }
        IIpsSrcFile file = packageFragment.createIpsFile(IpsObjectType.TEST_CASE, testCaseName, true, null);
        return (ITestCase) file.getIpsObject();
    }

	private void transformFolder(IFolder folder, IIpsPackageFragmentRoot root, String packageName, String testCaseTypeName, String nameExtension, String targetPackage) throws CoreException {
		if (folder == null)
			return;
		
		// create target package if not exists
		if (targetPackage.length() > 0){
			IIpsPackageFragment packageFragment = root.getIpsPackageFragment(packageName + "." + targetPackage); //$NON-NLS-1$
			
			if (! packageFragment.exists()){
				root.createPackageFragment(packageName + "." + targetPackage, false, null); //$NON-NLS-1$
			}
		}
		
		IResource[] members = folder.members();
		for (int i = 0; i < members.length; i++) {
			if (members[i] instanceof IFolder){
				targetPackage = targetPackage.length()>0?targetPackage + ".":""; //$NON-NLS-1$ //$NON-NLS-2$
				transformFolder((IFolder) members[i], root, packageName, testCaseTypeName, nameExtension,
						targetPackage + ((IFolder) members[i]).getName());
			}else if (members[i] instanceof IFile){
				transformFile((IFile) members[i], root, packageName + (targetPackage.length()>0? "." + targetPackage:""), testCaseTypeName, nameExtension); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private void transformFile(IFile file, IIpsPackageFragmentRoot root, String packageName, String testCaseTypeName, String nameExtension) throws CoreException {
		if (! file.getFileExtension().equals("xml")) //$NON-NLS-1$
			return;
		try {
            createTestCaseFromRuntimeXml(file, testCaseTypeName, root, packageName, nameExtension);
            importedTestCase ++;
		} catch (Exception e) {
			throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_Unknown, file.getName()), e)); //$NON-NLS-2$
		}
	}

	/**
	 * Starts the job to transform runtime test cases from the given selection.
	 */
	public void startTestRunnerJob(IStructuredSelection selection, 
			IIpsPackageFragment targetPackage, String testCaseTypeName, String nameExtension){
        lastException = null;
        importedTestCase = 0;
		job = new TransformerJob(selection, targetPackage, testCaseTypeName, nameExtension);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		job.setRule(workspace.getRoot());
		job.schedule();	
	}
    
    private String getTestParameterTypeName(boolean isInput){
        return isInput ? TestParameterType.INPUT.toString()
                : TestParameterType.EXPECTED_RESULT.toString();
    }    
}
