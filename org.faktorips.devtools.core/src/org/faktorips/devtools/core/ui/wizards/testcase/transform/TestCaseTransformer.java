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
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.StringUtil;
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
        String testCaseName = file.getName().substring(0, 
                file.getName().indexOf(file.getFileExtension()) - 1) + nameExtension;
        
        ITestCase newTestCase = createNewTestCase(root, targetPackageName, testCaseName);
        newTestCase.setTestCaseType(testCaseTypeName);
        Document doc = XmlUtil.getDocument(file.getContents());
        
        Element elem = XmlUtil.getFirstElement(doc, "TestCase"); //$NON-NLS-1$
        
        initTestPolicyCmpts(XmlUtil.getFirstElement(elem, "Input"), newTestCase, true); //$NON-NLS-1$
        initTestPolicyCmpts(XmlUtil.getFirstElement(elem, "ExpectedResult"), newTestCase, false); //$NON-NLS-1$
        
        newTestCase.getIpsSrcFile().save(true, null);
        return newTestCase;
    }
    
    private void initTestPolicyCmpts(Element parent, ITestCase testCase, boolean isInput){
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                if (isInput){
                    parsePolicyCmpt((Element) nl.item(i), testCase.newInputPolicyCmpt());
                }else{
                    parsePolicyCmpt((Element) nl.item(i), testCase.newExpectedResultPolicyCmpt());
                }
            }
        }
    }
    
    private void parsePolicyCmpt(Element parent, ITestPolicyCmpt testPolicyCmpt){
        // init test policy component
        String policyCmpt = parent.getNodeName();
        String productCmpt = parent.getAttribute("productCmpt"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(productCmpt)){
            testPolicyCmpt.setProductCmpt(productCmpt);
            testPolicyCmpt.setLabel(StringUtil.unqualifiedName(productCmpt));
        }else{
            testPolicyCmpt.setLabel(policyCmpt);
        }
        testPolicyCmpt.setTestPolicyCmptType(policyCmpt);
        
        // read childs
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element) nl.item(i);
                if (element.getAttribute("type") == null){ //$NON-NLS-1$
                    // do nothing no type given
                    IpsPlugin.log(new IpsStatus(Messages.TestCaseTransformer_Error_NoTypeAttributeSpecified));
                } else if (element.getAttribute("type").equals("property")){ //$NON-NLS-1$ //$NON-NLS-2$
                    ITestAttributeValue testAttrValue = testPolicyCmpt.newTestAttributeValue();
                    testAttrValue.setTestAttribute(element.getNodeName());
                    testAttrValue.setValue(XmlUtil.getTextNode(element).getData());
                }else if (element.getAttribute("type").equals("composite")){ //$NON-NLS-1$ //$NON-NLS-2$
                    // this is a child policy component
                    ITestPolicyCmptRelation relation = testPolicyCmpt.newTestPolicyCmptRelation();
                    relation.setTestPolicyCmptType(element.getNodeName());
                    parsePolicyCmpt(element, relation.newTargetTestPolicyCmptChild());
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
		if (! file.getFileExtension().equals("ipstestcase")) //$NON-NLS-1$
			return;
		
		TestCaseTransformer testCaseTransformer = new TestCaseTransformer();
		try {
			testCaseTransformer.createTestCaseFromRuntimeXml(file, testCaseTypeName, root, packageName, nameExtension);
		} catch (Exception e) {
			throw new CoreException(new IpsStatus(NLS.bind(Messages.TestCaseTransformer_Error_Unknown, file.getName()), e)); //$NON-NLS-2$
		}
	}


	
	/**
	 * Starts the job to transform runtime test cases from the given selection.
	 */
	public void startTestRunnerJob(IStructuredSelection selection, 
			IIpsPackageFragment targetPackage, String testCaseTypeName, String nameExtension){
		TransformerJob job = new TransformerJob(selection, targetPackage, testCaseTypeName, nameExtension);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		job.setRule(workspace.getRoot());
		job.schedule();	
	}
	
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
				IpsPlugin.log(e);
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}    
}
