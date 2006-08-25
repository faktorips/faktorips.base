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

package org.faktorips.devtools.stdbuilder.testcase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A builder that transforms the test case into an xml file in runtime format.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseBuilder extends AbstractArtefactBuilder {

    private PolicyCmptImplClassBuilder policyCmptBuilder;
    
    /**
     * @param builderSet
     */
    public TestCaseBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "TestCaseBuilder";
    }
    
    /**
     * Sets the policy component type implementation builder.
     */
    public void setPolicyCmptBuilder(PolicyCmptImplClassBuilder policyCmptBuilder) {
        this.policyCmptBuilder = policyCmptBuilder;
    }

    /**
     * Returns the path to the xml resource as used by the Class.getResourceAsStream() Method.
     * 
     * @see Class#getResourceAsStream(java.lang.String) 
     */
    public String getXmlResourcePath(ITestCase testCase) throws CoreException {
        String packageInternal = getBuilderSet().getPackage(DefaultBuilderSet.KIND_TEST_CASE_XML,
                testCase.getIpsSrcFile());
        return packageInternal.replace('.', '/') + '/' + testCase.getName() + ".xml";
    }
    
    /**
     * {@inheritDoc}
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        ArgumentCheck.isTrue(ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE);
        ITestCase testCase = (ITestCase) ipsSrcFile.getIpsObject();
        InputStream is = null;
        try {
            Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
            Element element = toRuntimeTestCaseXml(doc, testCase);
            String encoding = ipsSrcFile.getIpsProject()==null?"UTF-8":testCase.getIpsProject().getXmlFileCharset(); //$NON-NLS-1$
            String content = XmlUtil.nodeToString(element, encoding);
            is = convertContentAsStream(content, ipsSrcFile.getIpsProject().getProject().getDefaultCharset());
        } catch (TransformerException e) {
            throw new RuntimeException(e); 
            // This is a programing error, rethrow as runtime exception
        }

        IFile file = getXmlContentFile(ipsSrcFile);
        IFolder folder = (IFolder)file.getParent();
        if (!folder.exists()) {
            createFolder(folder);
        }
        if (!file.exists()) {
            file.create(is, true, null);
        }else{
            file.setContents(is, true, true, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.TEST_CASE);
    }

    /**
     * {@inheritDoc}
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = getXmlContentFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(true, null);
        }
    }
    
    /*
     * Converts the given string content as ByteArrayInputStream.
     */
    private ByteArrayInputStream convertContentAsStream(String content, String charSet) throws CoreException{
        try {
            return new ByteArrayInputStream(content.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    /*
     * Creates a folder if not existes.
     */
    private void createFolder(IFolder folder) throws CoreException {
        while (!folder.getParent().exists()) {
            createFolder((IFolder)folder.getParent());
        }
        folder.create(true, true, null);
    }   
    
    /*
     * Returns the package folder for the given ips sourcefile.
     */
    private IFolder getXmlContentFileFolder(IIpsSrcFile ipsSrcFile) throws CoreException {
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_TEST_CASE_XML, ipsSrcFile);
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination().getFolder(
            pathToPack);
    }
    
    /*
     * Returns the file resource of the given ips source file.
     */
    private IFile getXmlContentFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        IFolder folder = getXmlContentFileFolder(ipsSrcFile);
        return folder.getFile(StringUtil.getFilenameWithoutExtension(file.getName()) + ".xml");
    }
    
    /*
     * Transforms the given test case object to an ips test case xml which can executed as ips test case
     * in runtime. 
     * 
     * @param doc the xml document that can be used as a factory to create xml elment.
     * @param testCase the test case which will be transformed to the runtime test case format.
     * 
     * @return the xml representation of the test case
     */
    private Element toRuntimeTestCaseXml(Document doc, ITestCase testCase) throws CoreException {
        Element testCaseElm = doc.createElement("TestCase");
        doc.appendChild(testCaseElm);
        
        Element input = doc.createElement("Input");
        Element expectedResult = doc.createElement("ExpectedResult");
        
        testCaseElm.appendChild(input);
        testCaseElm.appendChild(expectedResult);
        
        addTestValues(doc, input, testCase.getInputTestValues());
        addTestValues(doc, expectedResult, testCase.getExpectedResultTestValues());
        
        addTestPolicyCmpts(doc, input, testCase.getInputTestPolicyCmpts(), null, true);
        addTestPolicyCmpts(doc, expectedResult, testCase.getExpectedResultTestPolicyCmpts(), null, false);
        
        return testCaseElm;
    }

    /*
     * Add the given test values to the given element. 
     */
    private void addTestValues(Document doc, Element input, ITestValue[] testValues) {
        if (testValues == null)
            return; 
        for (int i = 0; i < testValues.length; i++) {
            Element valueElem = XmlUtil.addNewChild(doc, input, testValues[i].getTestValueParameter());
            XmlUtil.addNewCDATAorTextChild(doc, valueElem, testValues[i].getValue());
            valueElem.setAttribute("type", "testvalue");
        }
    }
    
    /*
     * Add test given policy components to the given element.
     */
    private void addTestPolicyCmpts(Document doc,
            Element parent,
            ITestPolicyCmpt[] testPolicyCmpt,
            ITestPolicyCmptRelation relation,
            boolean isInput) throws CoreException {
        if (testPolicyCmpt == null)
            return;
        for (int i = 0; i < testPolicyCmpt.length; i++) {
            Element testPolicyCmptElem = null;
            if (relation != null) {
                ITestPolicyCmptTypeParameter parameter = relation.findTestPolicyCmptType();
                if (parameter == null){
                    throw new CoreException(new IpsStatus(NLS.bind(
                            "The test policy component type parameter {0} was not found.", relation.getTestPolicyCmptType())));
                }
                testPolicyCmptElem = XmlUtil.addNewChild(doc, parent, parameter.getRelation());
                testPolicyCmptElem.setAttribute("type", "composite");
                testPolicyCmptElem.setAttribute("name", testPolicyCmpt[i].getTestPolicyCmptTypeParameter() + "/"
                        + testPolicyCmpt[i].getName());
            } else {
                testPolicyCmptElem = XmlUtil.addNewChild(doc, parent, testPolicyCmpt[i]
                        .getTestPolicyCmptTypeParameter());
                testPolicyCmptElem.setAttribute("name", testPolicyCmpt[i].getName());
            }
            testPolicyCmptElem.setAttribute("productCmpt", testPolicyCmpt[i].getProductCmpt());
            addTestAttrValues(doc, testPolicyCmptElem, testPolicyCmpt[i].getTestAttributeValues(), isInput);
            addRelations(doc, testPolicyCmptElem, testPolicyCmpt[i].getTestPolicyCmptRelations(), isInput);
            
            IProductCmpt productCmpt = testPolicyCmpt[i].findProductCmpt();
            if (productCmpt != null){
                IPolicyCmptType pcType = productCmpt.findPolicyCmptType();
                if (pcType == null){
                    throw new CoreException(new IpsStatus(NLS.bind(
                            "The policy component type {0} was not found.", productCmpt.getPolicyCmptType())));
                }
                testPolicyCmptElem.setAttribute("class", policyCmptBuilder.getQualifiedClassName(pcType));
            }
        }
    }
    
    /*
     * Add the given relations to the given element.
     */
    private void addRelations(Document doc, Element parent, ITestPolicyCmptRelation[] relations, boolean isInput){
        if (relations == null)
            return;
        if (relations.length > 0){
            for(int i = 0; i < relations.length; i++){
                String relationType = "";
                if (relations[i].isComposition()) {
                    try {
                        addTestPolicyCmpts(doc, parent, new ITestPolicyCmpt[]{relations[i].findTarget()}, relations[i], isInput);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }                   
                } else if (relations[i].isAccoziation()){
                    relationType = RelationType.ASSOZIATION.getName().toLowerCase();
                    Element testPolicyCmptElem = XmlUtil.addNewChild(doc, parent, relations[i].getTestPolicyCmptType());
                    testPolicyCmptElem.setAttribute("target", relations[i].getTarget());
                    testPolicyCmptElem.setAttribute("type", relationType);
                }
            }
        }
    }
    
    /*
     * Add the given test attributes to the given element.
     */ 
    private void addTestAttrValues(Document doc, Element testPolicyCmpt, ITestAttributeValue[] testAttrValues, boolean isInput) throws CoreException{
        if (testAttrValues == null)
            return;
        for (int i = 0; i < testAttrValues.length; i++) {
            if (testAttrValues[i].isInputAttribute() && isInput || testAttrValues[i].isExpextedResultAttribute() && ! isInput){
                ITestAttribute testAttribute = testAttrValues[i].findTestAttribute();
                if (testAttribute == null) {
                    throw new CoreException(new IpsStatus(NLS.bind(
                            "The test attribute {0} was not found in the test case type definition.", testAttrValues[i]
                                    .getTestAttribute())));
                }
                
                Element attrValueElem = XmlUtil.addNewChild(doc, testPolicyCmpt, testAttribute.getAttribute());
                XmlUtil.addNewCDATAorTextChild(doc, attrValueElem, testAttrValues[i].getValue());
                attrValueElem.setAttribute("type", "property");
            }
        }
    }
}
