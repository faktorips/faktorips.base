/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.testcase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.internal.ipsobject.DescriptionHelper;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.testcase.ITestValue;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.util.DefaultLineSeparator;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.runtime.internal.IpsStringUtils;
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

    private Map<ITestPolicyCmpt, String> objectIdMap = new HashMap<>();

    private Map<Element, ITestPolicyCmpt> targetObjectIdMap = new HashMap<>();

    public TestCaseBuilder(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public String getName() {
        return "TestCaseBuilder";
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) {
        ArgumentCheck.isTrue(ipsSrcFile.getIpsObjectType() == IpsObjectType.TEST_CASE);
        ITestCase testCase = (ITestCase)ipsSrcFile.getIpsObject();
        if (!testCase.isValid(getIpsProject())) {
            return;
        }
        InputStream is = null;
        String content = null;
        try {
            Document doc = XmlUtil.getDefaultDocumentBuilder().newDocument();
            Element element = toRuntimeTestCaseXml(doc, testCase);
            String encoding = ipsSrcFile.getIpsProject() == null ? "UTF-8" //$NON-NLS-1$
                    : testCase.getIpsProject().getXmlFileCharset();
            content = org.faktorips.runtime.internal.XmlUtil.nodeToString(element, encoding,
                    DefaultLineSeparator.of(ipsSrcFile));
            is = convertContentAsStream(content, encoding);

            AFile file = getXmlContentFile(ipsSrcFile);
            boolean newlyCreated = createFileIfNotThere(file);

            if (newlyCreated) {
                writeToFile(file, is, false);
            } else {
                String currentContent = getContentAsString(file.getContents(), encoding);
                if (!content.equals(currentContent)) {
                    writeToFile(file, is, true);
                }
            }
        } catch (TransformerException e) {
            throw new RuntimeException(e);
            // This is a programing error, rethrow as runtime exception
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // nothing to do
                    return;
                }
            }
        }
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.TEST_CASE);
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) {
        AFile file = getXmlContentFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(null);
        }
    }

    /**
     * Converts the given string content as ByteArrayInputStream.
     */
    private ByteArrayInputStream convertContentAsStream(String content, String charSet) {
        try {
            return new ByteArrayInputStream(content.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    /**
     * Returns the relative path to the generated XML file.
     *
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to generate
     * @return the relative path to the generated XML file
     */
    public Path getXmlContentRelativeFile(IIpsSrcFile ipsSrcFile) {
        String packageString = getBuilderSet().getPackageName(ipsSrcFile, isBuildingInternalArtifacts(),
                !buildsDerivedArtefacts());
        String[] packages = packageString.split("\\.");
        Path pathToPack = Path.of(packages[0], Arrays.copyOfRange(packages, 1, packages.length));
        return pathToPack.resolve(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + ".xml");
    }

    /**
     * Returns the handle to the file where the xml content for the given ips source file is stored.
     */
    public AFile getXmlContentFile(IIpsSrcFile ipsSrcFile) {
        return ((AFolder)ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getResource())
                .getFile(getXmlContentRelativeFile(ipsSrcFile));
    }

    /**
     * Transforms the given test case object to an ips test case xml which can executed as ips test
     * case in runtime.
     *
     * @param doc the xml document that can be used as a factory to create xml elment. @param
     *            testCase the test case which will be transformed to the runtime test case format.
     *
     * @return the xml representation of the test case
     */
    private Element toRuntimeTestCaseXml(Document doc, ITestCase testCase) {
        Element testCaseElm = doc.createElement("TestCase");
        testCaseElm.setAttribute("testCaseType", testCase.getTestCaseType());
        Locale generatorLocale = GeneratorConfig.forIpsObject(testCase).getLanguageUsedInGeneratedSourceCode();
        String description = testCase.getDescriptionText(generatorLocale);
        DescriptionHelper.setDescription(testCaseElm, description);
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
        // because the object id will be used to resolve the references in the input or expected
        // result;
        // references from the input to the exp. result are not supported!
        addTestPolicyCmpts(doc, input, testCase.getInputTestPolicyCmpts(), null, true, new ObjectId());
        resolveAssociations();

        addTestPolicyCmpts(doc, expectedResult, testCase.getExpectedResultTestPolicyCmpts(), null, false,
                new ObjectId());
        resolveAssociations();

        return testCaseElm;
    }

    private void resolveAssociations() {
        for (Element elem : targetObjectIdMap.keySet()) {
            ITestPolicyCmpt target = targetObjectIdMap.get(elem);
            String objectId = objectIdMap.get(target);
            if (objectId != null) {
                elem.setAttribute("targetId", objectId);
            }
        }
        targetObjectIdMap.clear();
        objectIdMap.clear();
    }

    /**
     * Add the given test values to the given element.
     */
    private void addTestValues(Document doc, Element element, ITestValue[] testValues) {
        if (testValues == null) {
            return;
        }
        for (ITestValue testValue : testValues) {
            if (!testValue.isValid(getIpsProject())) {
                continue;
            }
            Element valueElem = XmlUtil.addNewChild(doc, element, testValue.getTestValueParameter());
            addValueElement(doc, valueElem, "testvalue", testValue.getValue());
        }
    }

    /**
     * Add the given test rules to the given element.
     */
    private void addTestRules(Document doc, Element element, ITestRule[] testRules) {
        if (testRules == null) {
            return;
        }
        for (ITestRule testRule : testRules) {
            if (!testRule.isValid(getIpsProject())) {
                continue;
            }
            IValidationRule validationRule = testRule.findValidationRule(getIpsProject());
            if (validationRule == null) {
                // validation rule not found ignore element
                continue;
            }
            Element ruleElem = XmlUtil.addNewChild(doc, element, testRule.getTestParameterName());
            ruleElem.setAttribute("type", "testrule");
            ruleElem.setAttribute("validationRule", testRule.getValidationRule());
            ruleElem.setAttribute("validationRuleMessageCode", validationRule.getMessageCode());
            ruleElem.setAttribute("violationType", testRule.getViolationType().getId());
        }
    }

    /**
     * Add test given policy components to the given element.
     */
    private void addTestPolicyCmpts(Document doc,
            Element parent,
            ITestPolicyCmpt[] testPolicyCmpts,
            ITestPolicyCmptLink link,
            boolean isInput,
            ObjectId objectId) {

        if (testPolicyCmpts == null) {
            return;
        }
        for (ITestPolicyCmpt testPolicyCmpt : testPolicyCmpts) {
            if (!testPolicyCmpt.isValid(getIpsProject())) {
                continue;
            }
            Element testPolicyCmptElem = createTestPolicyCmptElem(doc, parent, testPolicyCmpt, link);

            // set object id
            int currObjectId = objectId.nextValue();
            objectIdMap.put(testPolicyCmpt, "" + currObjectId);
            testPolicyCmptElem.setAttribute("objectId", "" + currObjectId);

            String policyCmptTypeQName = null;
            // get the policyCmptTypeQName
            if (testPolicyCmpt.isProductRelevant()) {
                policyCmptTypeQName = getPolicyCmptTypeNameAndSetProductCmptAttr(testPolicyCmpt, testPolicyCmptElem);
            } else {
                // the test policy cmpt type parameter is not product relevant
                // then the policy cmpt qualified name is stored on the test policy cmpt
                policyCmptTypeQName = testPolicyCmpt.getPolicyCmptType();
                if (IpsStringUtils.isEmpty(policyCmptTypeQName)) {
                    // attribute policyCmptType not set, get the policy cmpt type from test
                    // parameter
                    policyCmptTypeQName = getPolicyCmptTypeNameFromParameter(testPolicyCmpt);
                }
            }

            IIpsSrcFile policyCmptTypeSrcFile = testPolicyCmpt.getIpsProject()
                    .findIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE, policyCmptTypeQName);
            if (policyCmptTypeSrcFile == null) {
                throw new IpsException(
                        new IpsStatus(NLS.bind("The policy component type {0} was not found.", policyCmptTypeQName)));
            }
            testPolicyCmptElem.setAttribute("class", getQualifiedClassName(policyCmptTypeSrcFile.getIpsObject()));
            addTestAttrValues(doc, testPolicyCmptElem, testPolicyCmpt.getTestAttributeValues(), isInput);
            addAssociations(doc, testPolicyCmptElem, testPolicyCmpt.getTestPolicyCmptLinks(), isInput, objectId);
        }
    }

    /* private */ String getQualifiedClassName(IIpsObject policyCmptType) {
        return getBuilderSet().getModelNode(policyCmptType, XPolicyCmptClass.class)
                .getQualifiedName(BuilderAspect.IMPLEMENTATION);
    }

    private String getPolicyCmptTypeNameAndSetProductCmptAttr(ITestPolicyCmpt testPolicyCmpt,
            Element testPolicyCmptElem) {
        IIpsSrcFile productCmptSrcFile = testPolicyCmpt.getIpsProject().findIpsSrcFile(IpsObjectType.PRODUCT_CMPT,
                testPolicyCmpt.getProductCmpt());
        if (productCmptSrcFile == null) {
            throw new IpsException(new IpsStatus(
                    NLS.bind("The product component {0} was not found.", testPolicyCmpt.getProductCmpt())));
        }
        testPolicyCmptElem.setAttribute("productCmpt",
                productCmptSrcFile.getPropertyValue(IProductCmpt.PROPERTY_RUNTIME_ID));
        // because the product can be based on a subtype defined in the test type parameter we must
        // search for the correct policy cmpt
        String productCmptTypeQName = productCmptSrcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
        IIpsSrcFile productCmptTypeSrcFile = testPolicyCmpt.getIpsProject()
                .findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE, productCmptTypeQName);
        return productCmptTypeSrcFile.getPropertyValue(IProductCmptType.PROPERTY_POLICY_CMPT_TYPE);
    }

    private String getPolicyCmptTypeNameFromParameter(ITestPolicyCmpt testPolicyCmpt) {
        ITestPolicyCmptTypeParameter parameter = testPolicyCmpt.findTestPolicyCmptTypeParameter(getIpsProject());
        if (parameter == null) {
            throw new IpsException(
                    new IpsStatus(NLS.bind("The test policy component type parameter {0} was not found.",
                            testPolicyCmpt.getTestPolicyCmptTypeParameter())));
        }
        return parameter.getPolicyCmptType();
    }

    private Element createTestPolicyCmptElem(Document doc,
            Element parent,
            ITestPolicyCmpt testPolicyCmpt,
            ITestPolicyCmptLink link) {
        Element testPolicyCmptElem;
        if (link != null) {
            ITestPolicyCmptTypeParameter parameter = link.findTestPolicyCmptTypeParameter(getIpsProject());
            if (parameter == null) {
                throw new IpsException(
                        new IpsStatus(NLS.bind("The test policy component type parameter {0} was not found.",
                                link.getTestPolicyCmptTypeParameter())));
            }
            testPolicyCmptElem = XmlUtil.addNewChild(doc, parent, parameter.getAssociation());
            testPolicyCmptElem.setAttribute("type", "composite");
            testPolicyCmptElem.setAttribute("name",
                    testPolicyCmpt.getTestPolicyCmptTypeParameter() + "/" + testPolicyCmpt.getName());
        } else {
            testPolicyCmptElem = XmlUtil.addNewChild(doc, parent, testPolicyCmpt.getTestPolicyCmptTypeParameter());
            testPolicyCmptElem.setAttribute("name", testPolicyCmpt.getName());
        }
        return testPolicyCmptElem;
    }

    /**
     * Add the given associations to the given element.
     */
    private void addAssociations(Document doc,
            Element parent,
            ITestPolicyCmptLink[] associations,
            boolean isInput,
            ObjectId objectId) {
        if (associations == null) {
            return;
        }
        if (associations.length > 0) {
            for (ITestPolicyCmptLink association : associations) {
                if (!association.isValid(getIpsProject()) || !associationsParentSameType(association, isInput)) {
                    continue;
                }
                String associationType = "";
                if (association.isComposition()) {
                    addTestPolicyCmpts(doc, parent, new ITestPolicyCmpt[] { association.findTarget() },
                            association, isInput, objectId);
                } else if (association.isAssociation()) {
                    // @see AbstractModelObject
                    associationType = "association";
                    Element testPolicyCmptElem = XmlUtil.addNewChild(doc, parent,
                            association.getTestPolicyCmptTypeParameter());
                    testPolicyCmptElem.setAttribute("target", association.getTarget());
                    testPolicyCmptElem.setAttribute("type", associationType);
                    ITestPolicyCmpt target = association.findTarget();
                    targetObjectIdMap.put(testPolicyCmptElem, target);
                }
            }
        }
    }

    private boolean associationsParentSameType(ITestPolicyCmptLink association, boolean isInput) {
        ITestPolicyCmptTypeParameter param = association.findTestPolicyCmptTypeParameter(getIpsProject());
        if (param == null) {
            return false;
        }
        return param.isInputOrCombinedParameter() && isInput || param.isExpextedResultOrCombinedParameter() && !isInput;
    }

    /**
     * Add the given test attributes to the given element.
     */
    private void addTestAttrValues(Document doc,
            Element testPolicyCmpt,
            ITestAttributeValue[] testAttrValues,
            boolean isInput) {
        if (testAttrValues == null) {
            return;
        }
        IIpsProject ipsProject = getIpsProject();
        for (ITestAttributeValue testAttrValue : testAttrValues) {
            if (!testAttrValue.isValid(getIpsProject())) {
                continue;
            }
            if (testAttrValue.isInputAttribute(ipsProject) && isInput
                    || testAttrValue.isExpectedResultAttribute(ipsProject) && !isInput) {
                ITestAttribute testAttribute = testAttrValue.findTestAttribute(ipsProject);
                if (testAttribute == null) {
                    throw new IpsException(new IpsStatus(
                            NLS.bind("The test attribute {0} was not found in the test case type definition.",
                                    testAttrValue.getTestAttribute())));
                }

                // the child name is either the attribute name or the extension attribute (=test
                // attribute name)
                String childName = IpsStringUtils.isEmpty(testAttribute.getAttribute()) ? testAttribute.getName()
                        : testAttribute.getAttribute();
                Element attrValueElem = XmlUtil.addNewChild(doc, testPolicyCmpt, childName);
                addValueElement(doc, attrValueElem, "property", testAttrValue.getValue());
            }
        }
    }

    private String getContentAsString(InputStream is, String charSet) {
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    private void addValueElement(Document doc, Element element, String type, String value) {
        if (value == null) {
            element.setAttribute("isNull", Boolean.toString(true));
        }
        XmlUtil.addNewCDATAorTextChild(doc, element, value);
        element.setAttribute("type", type);
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

    /**
     * Class to generate an unique object id within the input and the expected result.
     */
    private static class ObjectId {
        private int objectId = 0;

        public int nextValue() {
            return objectId++;
        }

        @Override
        public String toString() {
            return "" + objectId;
        }
    }

}
