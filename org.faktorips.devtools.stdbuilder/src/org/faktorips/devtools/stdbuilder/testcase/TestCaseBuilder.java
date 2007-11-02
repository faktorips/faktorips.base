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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
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

    private JavaSourceFileBuilder javaSourceFileBuilder;
    
    private Map objectIdMap = new HashMap();

    private Map targetObjectIdMap = new HashMap();
    
    /*
     * Class to generate an unique object id within the input and the expected result.
     */
    private class ObjectId {
        int objectId = 0;
        public int nextValue(){
            return objectId++;
        }
        public String toString(){
            return ""+objectId;
        }
    }
    
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
    public void setJavaSourceFileBuilder(JavaSourceFileBuilder javaSourceFileBuilder) {
        this.javaSourceFileBuilder = javaSourceFileBuilder;
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
     * @throws IOException 
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        ArgumentCheck.isTrue(ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE);
        ITestCase testCase = (ITestCase) ipsSrcFile.getIpsObject();
        if (!testCase.isValid()){
            return;
        }
        InputStream is = null;
        String content = null;
        try {
            Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
            Element element = toRuntimeTestCaseXml(doc, testCase);
            String encoding = ipsSrcFile.getIpsProject()==null?"UTF-8":testCase.getIpsProject().getXmlFileCharset(); //$NON-NLS-1$
            content = XmlUtil.nodeToString(element, encoding);
            is = convertContentAsStream(content, ipsSrcFile.getIpsProject().getProject().getDefaultCharset());

            IFile file = getXmlContentFile(ipsSrcFile);
            boolean newlyCreated = createFileIfNotThere(file);
            
            if(newlyCreated){
                file.setContents(is, true, false, null);
            }else{
                String charSet = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
                String currentContent = getContentAsString(file.getContents(), charSet);
                if(!content.equals(currentContent)){
                    file.setContents(is, true, true, null);
                }
            }
        } catch (TransformerException e) {
            throw new RuntimeException(e); 
            // This is a programing error, rethrow as runtime exception
        }
        finally{
            if(is != null){
                try {
                    is.close();
                } catch (Exception e) {
                    //nothing to do
                }
            }
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
     * Returns the package folder for the given ips sourcefile.
     */
    private IFolder getXmlContentFileFolder(IIpsSrcFile ipsSrcFile) throws CoreException {
        String packageString = getBuilderSet().getPackage(DefaultBuilderSet.KIND_TEST_CASE_XML, ipsSrcFile);
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getFolder(
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
        
        // add test values (input and expected)
        addTestValues(doc, input, testCase.getInputTestValues());
        addTestValues(doc, expectedResult, testCase.getExpectedResultTestValues());
        
        // add test rules (only expected because rules are always expected)
        addTestRules(doc, expectedResult, testCase.getExpectedResultTestRules());
        
        // add test policy cmpt (input and expected)
        // remark: the object id will be only unique in the input and unique in the expected result,
        // the same object id in the input could be differ to the object id in the expected result,
        // because the object id will be used to resolve the references in the input or expected result;
        // references from the input to the exp. result are not supported!
        addTestPolicyCmpts(doc, input, testCase.getInputTestPolicyCmpts(), null, true, new ObjectId());
        resolveAssociations();
        
        addTestPolicyCmpts(doc, expectedResult, testCase.getExpectedResultTestPolicyCmpts(), null, false, new ObjectId());
        resolveAssociations();
        
        return testCaseElm;
    }

    private void resolveAssociations() {
        for (Iterator iter = targetObjectIdMap.keySet().iterator(); iter.hasNext();) {
            ITestPolicyCmpt target = (ITestPolicyCmpt)iter.next();
            String objectId = (String)objectIdMap.get(target);
            if (objectId != null){
                Element elem = (Element) targetObjectIdMap.get(target);
                elem.setAttribute("targetId", objectId);
            }
        }
        targetObjectIdMap.clear();
        objectIdMap.clear();
    }

    /*
     * Add the given test values to the given element. 
     */
    private void addTestValues(Document doc, Element element, ITestValue[] testValues) throws CoreException {
        if (testValues == null){
            return; 
        }
        for (int i = 0; i < testValues.length; i++) {
            if (!testValues[i].isValid()){
                continue;
            }
            Element valueElem = XmlUtil.addNewChild(doc, element, testValues[i].getTestValueParameter());
            XmlUtil.addNewCDATAorTextChild(doc, valueElem, testValues[i].getValue());
            valueElem.setAttribute("type", "testvalue");
        }
    }
    
    /*
     * Add the given test rules to the given element. 
     */
    private void addTestRules(Document doc, Element element, ITestRule[] testRules) throws CoreException {
        if (testRules == null){
            return; 
        }
        for (int i = 0; i < testRules.length; i++) {
            if (!testRules[i].isValid()){
                continue;
            }
            IValidationRule validationRule = testRules[i].findValidationRule();
            if (validationRule == null){
                // validation rule not found ignore element
                continue;
            }
            Element ruleElem = XmlUtil.addNewChild(doc, element, testRules[i].getTestParameterName());
            ruleElem.setAttribute("type", "testrule");
            ruleElem.setAttribute("validationRule", testRules[i].getValidationRule());
            ruleElem.setAttribute("validationRuleMessageCode", validationRule.getMessageCode());
            ruleElem.setAttribute("violationType", testRules[i].getViolationType().getId()); 
        }
    }
    
    /*
     * Add test given policy components to the given element.
     */
    private void addTestPolicyCmpts(Document doc,
            Element parent,
            ITestPolicyCmpt[] testPolicyCmpt,
            ITestPolicyCmptRelation relation,
            boolean isInput,
            ObjectId objectId) throws CoreException {
        if (testPolicyCmpt == null) {
            return;
        }
        for (int i = 0; i < testPolicyCmpt.length; i++) {
            if (!testPolicyCmpt[i].isValid()){
                continue;
            }
            Element testPolicyCmptElem = null;
            if (relation != null) {
                ITestPolicyCmptTypeParameter parameter = relation.findTestPolicyCmptTypeParameter();
                if (parameter == null){
                    throw new CoreException(new IpsStatus(NLS.bind(
                            "The test policy component type parameter {0} was not found.", relation.getTestPolicyCmptTypeParameter())));
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
            int currObjectId = objectId.nextValue();
            objectIdMap.put(testPolicyCmpt[i], "" + currObjectId);
            testPolicyCmptElem.setAttribute("objectId", ""+currObjectId);
            
            IIpsSrcFile policyCmptTypeSrcFile = null;
            String policyCmptTypeQName = null;
            if (testPolicyCmpt[i].isProductRelevant()){
                // the test policy cmpt type parameter is product relevant
                IIpsSrcFile productCmptSrcFile = testPolicyCmpt[i].getIpsProject().findIpsSrcFile(
                        IpsObjectType.PRODUCT_CMPT, testPolicyCmpt[i].getProductCmpt());
                if (productCmptSrcFile == null){
                    throw new CoreException(new IpsStatus(NLS.bind("The product component {0} was not found.",
                            testPolicyCmpt[i].getProductCmpt())));
                }
                testPolicyCmptElem.setAttribute("productCmpt", productCmptSrcFile.getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID));
                // because the product can be based on a subtype defined in the test type parameter we must search for the correct policy cmpt
                String productCmptTypeQName = productCmptSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
                IIpsSrcFile productCmptTypeSrcFile = testPolicyCmpt[i].getIpsProject().findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE_V2, productCmptTypeQName);
                policyCmptTypeQName = productCmptTypeSrcFile.getPropertyValue(IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);
                policyCmptTypeSrcFile = testPolicyCmpt[i].getIpsProject().findIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE, policyCmptTypeQName);
            } else {
                // the test policy cmpt type parameter is not product relevant
                ITestPolicyCmptTypeParameter parameter = testPolicyCmpt[i].findTestPolicyCmptTypeParameter();
                if (parameter == null) {
                    throw new CoreException(new IpsStatus(NLS.bind(
                            "The test policy component type parameter {0} was not found.", relation
                                    .getTestPolicyCmptTypeParameter())));
                }
                policyCmptTypeSrcFile = testPolicyCmpt[i].getIpsProject().findIpsSrcFile(
                        IpsObjectType.POLICY_CMPT_TYPE, parameter.getPolicyCmptType());
                policyCmptTypeQName = parameter.getPolicyCmptType();
            }
            if (policyCmptTypeSrcFile == null) {
                throw new CoreException(new IpsStatus(NLS.bind("The policy component type {0} was not found.",
                        policyCmptTypeQName)));
            }
            testPolicyCmptElem.setAttribute("class", javaSourceFileBuilder.getQualifiedClassName(policyCmptTypeSrcFile));
            addTestAttrValues(doc, testPolicyCmptElem, testPolicyCmpt[i].getTestAttributeValues(), isInput);
            addRelations(doc, testPolicyCmptElem, testPolicyCmpt[i].getTestPolicyCmptRelations(), isInput, objectId);    
        }
    }
    
    /*
     * Add the given relations to the given element.
     */
    private void addRelations(Document doc,
            Element parent,
            ITestPolicyCmptRelation[] relations,
            boolean isInput,
            ObjectId objectId) throws CoreException {
        if (relations == null) {
            return;
        }
        if (relations.length > 0){
            for(int i = 0; i < relations.length; i++){
                if (!relations[i].isValid()){
                    continue;
                }
                if (! relationsParentSameType(relations[i], isInput)){
                    continue;
                }
                String relationType = "";
                if (relations[i].isComposition()) {
                    try {
                        addTestPolicyCmpts(doc, parent, new ITestPolicyCmpt[]{relations[i].findTarget()}, relations[i], isInput, objectId);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                } else if (relations[i].isAccoziation()){
                    relationType = "association"; // @see AbstractModelObject
                    Element testPolicyCmptElem = XmlUtil.addNewChild(doc, parent, relations[i].getTestPolicyCmptTypeParameter());
                    testPolicyCmptElem.setAttribute("target", relations[i].getTarget());
                    testPolicyCmptElem.setAttribute("type", relationType);
                    ITestPolicyCmpt target = relations[i].findTarget();
                    targetObjectIdMap.put(target, testPolicyCmptElem);
                }
            }
        }
    }
    
    private boolean relationsParentSameType(ITestPolicyCmptRelation relation, boolean isInput) throws CoreException {
        ITestPolicyCmptTypeParameter param = relation.findTestPolicyCmptTypeParameter();
        if (param == null){
            return false;
        }
        return param.isInputParameter() && isInput || param.isExpextedResultParameter() && !isInput;
    }

    /*
     * Add the given test attributes to the given element.
     */ 
    private void addTestAttrValues(Document doc, Element testPolicyCmpt, ITestAttributeValue[] testAttrValues, boolean isInput) throws CoreException{
        if (testAttrValues == null){
            return;
        }
        for (int i = 0; i < testAttrValues.length; i++) {
            if (!testAttrValues[i].isValid()){
                continue;
            }
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
    
    private String getContentAsString(InputStream is, String charSet) throws CoreException{
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }    
    
    /**
     * {@inheritDoc}
     * 
     * Returns true.
     */
    public boolean buildsDerivedArtefacts() {
        return true;
    }

}
