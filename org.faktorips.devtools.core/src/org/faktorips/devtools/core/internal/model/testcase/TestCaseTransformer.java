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

package org.faktorips.devtools.core.internal.model.testcase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class to transform runtime test cases to the model test case format.
 * The model test cases could be read and handled by the test case editor.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseTransformer {

    private IIpsPackageFragmentRoot root;

    /**
     * Creates and returns a test case object from the given runetime test case xml file.
     * 
     * @param file the test case runtime file (xml formated) which will be transformed to the model format
     * @param testCaseTypeName the test case type which is the template for the given test case
     * @param targetPackageName the target for the new transformed model test case
     * @param nameExtension (optional) the extension which will be added to the transformed test case file name
     * 
     * @throws Exception if an error occurs
     */
    public ITestCase createTestCaseFromRuntimeXml(IFile file, String testCaseTypeName, String targetPackageName, String nameExtension) throws Exception{
        String testCaseName = file.getName().substring(0, 
                file.getName().indexOf(file.getFileExtension()) - 1) + nameExtension;
        
        // get the first ips package fragment root inside the selected files project
        IProject project = file.getProject();
        if (! project.hasNature(IIpsProject.NATURE_ID)){
            throw new CoreException(new IpsStatus("Project has no IPS-Nature!"));
        }
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
        if (! (ipsProject.getIpsPackageFragmentRoots().length > 0)){
            throw new CoreException(new IpsStatus("No package fragment roots found!"));
        }
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        
        ITestCase newTestCase = createNewTestCase(targetPackageName, testCaseName);
        newTestCase.setTestCaseType(testCaseTypeName);
        Document doc = XmlUtil.getDocument(file.getContents());
        
        Element elem = XmlUtil.getFirstElement(doc, "TestCase");
        
        initTestPolicyCmpts(XmlUtil.getFirstElement(elem, "Input"), newTestCase, true);
        initTestPolicyCmpts(XmlUtil.getFirstElement(elem, "ExpectedResult"), newTestCase, false);
        
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
        String productCmpt = parent.getAttribute("productCmpt");
        if (StringUtils.isNotEmpty(productCmpt)){
            testPolicyCmpt.setProductCmpt(productCmpt);
            testPolicyCmpt.setLabel(productCmpt);
        }else{
            testPolicyCmpt.setLabel(policyCmpt);
        }
        testPolicyCmpt.setTestPolicyCmptType(policyCmpt);
        
        // read childs
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element element = (Element) nl.item(i);
                if (element.getAttribute("type") == null){
                    // do nothing no type given
                    System.err.println("No type attribute specified!");
                } else if (element.getAttribute("type").equals("property")){
                    ITestAttributeValue testAttrValue = testPolicyCmpt.newTestAttributeValue();
                    testAttrValue.setTestAttribute(element.getNodeName());
                    testAttrValue.setValue(XmlUtil.getTextNode(element).getData());
                }else if (element.getAttribute("type").equals("composite")){
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
    private ITestCase createNewTestCase(String packageName, String testCaseName) throws CoreException{
        IIpsPackageFragment packageFragment = root.getIpsPackageFragment(packageName);
        if (packageFragment == null){
            throw new CoreException(new IpsStatus("Package fragment: " + packageName + " not found."));
        }
        IIpsSrcFile file = packageFragment.createIpsFile(IpsObjectType.TEST_CASE, testCaseName, true, null);
        return (ITestCase) file.getIpsObject();
    }
}
