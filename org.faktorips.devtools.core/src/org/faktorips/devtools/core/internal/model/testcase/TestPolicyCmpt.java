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

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestAttributeValue;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test policy component. Defines a test policy component within a test case definition.
 * 
 * @author Joerg Ortmann
 */
public class TestPolicyCmpt extends TestObject implements ITestPolicyCmpt {
    private final String POLICY_CMPT_INSTANCE_INSTANCE_IMAGE_NAME = "PolicyCmptInstance.gif"; //$NON-NLS-1$

    /* Tags */
    final static String TAG_NAME = "PolicyCmptTypeObject"; //$NON-NLS-1$

    private String testPolicyCmptType = ""; //$NON-NLS-1$

    private String productCmpt = ""; //$NON-NLS-1$

    // if productCmpt is empty then the test policy cmpt is stored
    private String policyCmptType = ""; //$NON-NLS-1$

    private List<ITestAttributeValue> testAttributeValues = new ArrayList<ITestAttributeValue>(0);

    private List<ITestPolicyCmptLink> testPolicyCmptLinks = new ArrayList<ITestPolicyCmptLink>(0);

    public TestPolicyCmpt(IIpsObject parent, int id) {
        super(parent, id);
    }

    public TestPolicyCmpt(IIpsObjectPart parent, int id) {
        super(parent, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsElement[] getChildren() {
        int numOfChildren = testAttributeValues.size() + testPolicyCmptLinks.size();
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List<IIpsElement> childrenList = new ArrayList<IIpsElement>(numOfChildren);
        childrenList.addAll(testAttributeValues);
        childrenList.addAll(testPolicyCmptLinks);
        childrenList.toArray(childrenArray);
        return childrenArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitPartCollections() {
        testAttributeValues = new ArrayList<ITestAttributeValue>();
        testPolicyCmptLinks = new ArrayList<ITestPolicyCmptLink>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof TestAttributeValue) {
            testAttributeValues.add((TestAttributeValue)part);
            return;
        } else if (part instanceof TestPolicyCmptLink) {
            testPolicyCmptLinks.add((TestPolicyCmptLink)part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof TestAttributeValue) {
            testAttributeValues.remove(part);
            return;
        } else if (part instanceof TestPolicyCmptLink) {
            testPolicyCmptLinks.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(TestAttributeValue.TAG_NAME)) {
            return newTestAttributeValueInternal(id);
        } else if (xmlTagName.equals(TestPolicyCmptLink.TAG_NAME)) {
            return newTestPcTypeLinkInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTestPolicyCmptTypeParameter() {
        return testPolicyCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestPolicyCmptTypeParameter(String testPolicyCmptTypeParameter) {
        String oldPolicyCmptType = testPolicyCmptType;
        testPolicyCmptType = testPolicyCmptTypeParameter;
        valueChanged(oldPolicyCmptType, testPolicyCmptTypeParameter);
    }

    /**
     * {@inheritDoc}
     */
    public ITestParameter findTestParameter(IIpsProject ipsProject) throws CoreException {
        return findTestPolicyCmptTypeParameter(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(testPolicyCmptType)) {
            return null;
        }
        return ((TestCase)getTestCase()).findTestPolicyCmptTypeParameter(this, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public String getTestParameterName() {
        return testPolicyCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setProductCmpt(String newProductCmpt) {
        String oldTestProductCmpt = productCmpt;
        productCmpt = newProductCmpt;
        valueChanged(oldTestProductCmpt, newProductCmpt);
    }

    /**
     * {@inheritDoc}
     */
    public String getProductCmpt() {
        return productCmpt;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmpt findProductCmpt(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(productCmpt)) {
            return null;
        }
        IProductCmpt pc = ipsProject.findProductCmpt(productCmpt);
        return pc;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        return !StringUtils.isEmpty(productCmpt);
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    /**
     * Returns the top level test case.
     */
    public ITestCase getTestCase() {
        return ((ITestCase)getRoot().getParent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        testPolicyCmptType = element.getAttribute(PROPERTY_TESTPOLICYCMPTTYPE);
        policyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
        productCmpt = element.getAttribute(PROPERTY_PRODUCTCMPT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TESTPOLICYCMPTTYPE, testPolicyCmptType);
        element.setAttribute(PROPERTY_POLICYCMPTTYPE, policyCmptType);
        element.setAttribute(PROPERTY_PRODUCTCMPT, productCmpt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage() {
        if (isProductRelevant()) {
            return IpsPlugin.getDefault().getProductRelevantImage(POLICY_CMPT_INSTANCE_INSTANCE_IMAGE_NAME);
        }
        return IpsPlugin.getDefault().getImage(POLICY_CMPT_INSTANCE_INSTANCE_IMAGE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public ITestAttributeValue newTestAttributeValue() {
        TestAttributeValue a = newTestAttributeValueInternal(getNextPartId());
        objectHasChanged();
        return a;
    }

    /**
     * Creates a new test attribute without updating the src file.
     */
    private TestAttributeValue newTestAttributeValueInternal(int id) {
        TestAttributeValue a = new TestAttributeValue(this, id);
        testAttributeValues.add(a);
        return a;
    }

    /**
     * {@inheritDoc}
     */
    public ITestAttributeValue getTestAttributeValue(String name) {
        for (Iterator<ITestAttributeValue> it = testAttributeValues.iterator(); it.hasNext();) {
            ITestAttributeValue a = it.next();
            if (a.getTestAttribute().equals(name)) {
                return a;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ITestAttributeValue[] getTestAttributeValues() {
        ITestAttributeValue[] a = new ITestAttributeValue[testAttributeValues.size()];
        testAttributeValues.toArray(a);
        return a;
    }

    /**
     * Removes the attribute from the type.
     */
    void removeTestAttributeValue(TestAttributeValue attribute) {
        testAttributeValues.remove(attribute);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptLink getTestPolicyCmptLink(String testPolicyCmptType) {
        ArgumentCheck.notNull(testPolicyCmptType);
        for (Iterator<ITestPolicyCmptLink> it = testPolicyCmptLinks.iterator(); it.hasNext();) {
            ITestPolicyCmptLink r = it.next();
            if (r.getTestPolicyCmptTypeParameter().equals(testPolicyCmptType)) {
                return r;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptLink[] getTestPolicyCmptLinks() {
        ITestPolicyCmptLink[] r = new ITestPolicyCmptLink[testPolicyCmptLinks.size()];
        testPolicyCmptLinks.toArray(r);
        return r;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptLink[] getTestPolicyCmptLinks(String typeParameterName) {
        List<ITestPolicyCmptLink> links = new ArrayList<ITestPolicyCmptLink>();
        for (Iterator<ITestPolicyCmptLink> iter = testPolicyCmptLinks.iterator(); iter.hasNext();) {
            ITestPolicyCmptLink element = iter.next();
            if (element.getTestPolicyCmptTypeParameter().equals(typeParameterName)) {
                links.add(element);
            }
        }
        return links.toArray(new ITestPolicyCmptLink[0]);
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptLink newTestPolicyCmptLink() {
        ITestPolicyCmptLink r = newTestPcTypeLinkInternal(getNextPartId());
        objectHasChanged();
        return r;
    }

    /**
     * Creates a new test link without updating the src file.
     */
    private TestPolicyCmptLink newTestPcTypeLinkInternal(int id) {
        TestPolicyCmptLink r = new TestPolicyCmptLink(this, id);
        testPolicyCmptLinks.add(r);
        return r;
    }

    /**
     * Removes the link from the type.
     */
    void removeTestPcTypeLink(TestPolicyCmptLink link) {
        testPolicyCmptLinks.remove(link);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRoot() {
        return (!(getParent() instanceof TestPolicyCmptLink));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITestObject getRoot() {
        ITestPolicyCmpt testPolicyCmpt = this;
        while (!testPolicyCmpt.isRoot()) {
            testPolicyCmpt = testPolicyCmpt.getParentTestPolicyCmpt();
        }
        return testPolicyCmpt;
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmpt getParentTestPolicyCmpt() {
        if (isRoot()) {
            return null;
        }
        ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)getParent();
        return (ITestPolicyCmpt)testPcTypeLink.getParent();
    }

    /**
     * {@inheritDoc}
     */
    public void removeLink(ITestPolicyCmptLink link) {
        int idx = 0;
        int foundIdx = -1;
        for (Iterator<ITestPolicyCmptLink> iter = testPolicyCmptLinks.iterator(); iter.hasNext();) {
            ITestPolicyCmptLink element = iter.next();
            if (element == link) {
                foundIdx = idx;
                break;
            }
            idx++;
        }
        if (foundIdx >= 0) {
            testPolicyCmptLinks.remove(foundIdx);
            objectHasChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ITestPolicyCmptLink addTestPcTypeLink(ITestPolicyCmptTypeParameter typeParam,
            String productCmpt,
            String policyCmptType,
            String targetName) throws CoreException {
        ArgumentCheck.notNull(typeParam);
        if (!StringUtils.isEmpty(productCmpt) && !StringUtils.isEmpty(policyCmptType)) {
            throw new CoreException(new IpsStatus(Messages.TestPolicyCmpt_Error_ProductCmpAndPolicyCmptTypeGiven));
        }

        IPolicyCmptTypeAssociation link = typeParam.findAssociation(typeParam.getIpsProject());
        if (link == null) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestPolicyCmpt_Error_LinkNotFound, typeParam
                    .getAssociation())));
        }

        ITestPolicyCmptLink newTestPcTypeLink = null;
        if (!link.isAssoziation()) {
            // link is composition
            // add new link including a test policy component child
            newTestPcTypeLink = new TestPolicyCmptLink(this, getNextPartId());
            newTestPcTypeLink.setTestPolicyCmptTypeParameter(typeParam.getName());

            ITestPolicyCmpt newTestPolicyCmpt = newTestPcTypeLink.newTargetTestPolicyCmptChild();
            newTestPolicyCmpt.setTestPolicyCmptTypeParameter(typeParam.getName());
            newTestPolicyCmpt.setProductCmpt(StringUtils.isEmpty(productCmpt) ? "" : productCmpt); //$NON-NLS-1$
            newTestPolicyCmpt.setPolicyCmptType(StringUtils.isEmpty(policyCmptType) ? "" : policyCmptType); //$NON-NLS-1$

            // sets the label for the new child test policy component
            String name = ""; //$NON-NLS-1$
            if (StringUtils.isEmpty(productCmpt)) {
                name = newTestPolicyCmpt.getTestPolicyCmptTypeParameter();
            } else {
                name = productCmpt;
            }
            newTestPolicyCmpt.setName(getTestCase().generateUniqueNameForTestPolicyCmpt(newTestPolicyCmpt,
                    StringUtil.unqualifiedName(name)));

            // add all test attribute values as spedified in the test parameter type
            ITestAttribute attributes[] = typeParam.getTestAttributes();
            for (int i = 0; i < attributes.length; i++) {
                ITestAttribute attribute = attributes[i];
                ITestAttributeValue attrValue = newTestPolicyCmpt.newTestAttributeValue();
                attrValue.setTestAttribute(attribute.getName());
            }

            // set the defaults for all attribute values
            newTestPolicyCmpt.updateDefaultTestAttributeValues();

        } else {
            // link is association
            // add new association link (only the target will be set and no child will be created)
            newTestPcTypeLink = new TestPolicyCmptLink(this, getNextPartId());
            newTestPcTypeLink.setTestPolicyCmptTypeParameter(typeParam.getName());
            newTestPcTypeLink.setTarget(targetName);
        }

        // add the new link at the end of the existing links, grouped by the link name
        ITestPolicyCmptLink prevLinkWithSameName = null;
        for (Iterator<ITestPolicyCmptLink> iter = testPolicyCmptLinks.iterator(); iter.hasNext();) {
            ITestPolicyCmptLink currLink = iter.next();
            if (newTestPcTypeLink.getTestPolicyCmptTypeParameter().equals(currLink.getTestPolicyCmptTypeParameter())) {
                prevLinkWithSameName = currLink;
            }
        }

        if (prevLinkWithSameName != null) {
            int idx = testPolicyCmptLinks.indexOf(prevLinkWithSameName);
            testPolicyCmptLinks.add(idx + 1, newTestPcTypeLink);
        } else {
            testPolicyCmptLinks.add(newTestPcTypeLink);
        }

        fixDifferentChildSortOrder();

        objectHasChanged();
        return newTestPcTypeLink;
    }

    /**
     * {@inheritDoc}
     */
    public void updateDefaultTestAttributeValues() throws CoreException {
        // add the attributes which are defined in the test case type parameter
        IProductCmptGeneration generation = findProductCmpsCurrentGeneration(getIpsProject());
        ITestAttributeValue[] testAttrValues = getTestAttributeValues();
        for (int i = 0; i < testAttrValues.length; i++) {
            // set default value only if the model attribute is relevant by the specified product
            // cmpt
            // otherwise set the value to null,
            // therefore if the value is null and the currently specified product cmpt doesn't
            // configure this attribute
            // the attribute will be hidden in the test case editor (because it is null), otherwise
            // if the value isn't null
            // the test attribute value will be displayed and a warning will be shown
            if (generation != null) {
                ITestAttribute testAttribute = testAttrValues[i].findTestAttribute(getIpsProject());
                if (testAttribute != null) {
                    if (!testAttribute.isAttributeRelevantByProductCmpt(generation.getProductCmpt(), getIpsProject())) {
                        ((TestAttributeValue)testAttrValues[i]).setValue(null);
                        continue;
                    }
                }
                ((TestAttributeValue)testAttrValues[i]).setDefaultTestAttributeValueInternal(generation);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getIndexOfChildTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException {
        Assert.isNotNull(testPolicyCmpt);
        int idx = 0;
        for (Iterator<ITestPolicyCmptLink> iter = testPolicyCmptLinks.iterator(); iter.hasNext();) {
            ITestPolicyCmptLink testPolicyCmptLink = iter.next();
            if (testPolicyCmpt.equals(testPolicyCmptLink.findTarget())) {
                return idx;
            }
            idx++;
        }
        throw new CoreException(new IpsStatus(Messages.TestPolicyCmpt_Error_MoveNotPossibleBelongsToNoLink));
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTestPolicyCmptLink(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(testPolicyCmptLinks);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }

    /**
     * Returns the product components generation depending on the current working date (current
     * working generation). Returns <code>null</code> if the test policy cmpt is not product
     * relevant, or the product cmpt wasn't found.
     */
    public IProductCmptGeneration findProductCmpsCurrentGeneration(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(productCmpt)) {
            return null;
        }
        GregorianCalendar workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        IProductCmpt productCmptObj = ipsProject.findProductCmpt(productCmpt);
        IProductCmptGeneration generation = null;
        if (productCmptObj != null) {
            generation = (IProductCmptGeneration)productCmptObj.findGenerationEffectiveOn(workingDate);
        }
        return generation;
    }

    /**
     * Fix the sort order of the child test policy cmpt links in order to the corresponding test
     * policy cmpt type parameter.
     * 
     * @throws CoreException in case of an error
     */
    void fixDifferentChildSortOrder() throws CoreException {
        List<ITestPolicyCmptLink> oldLinks = testPolicyCmptLinks;
        IIpsProject ipsProject = getIpsProject();
        // fill temporary storage of the links for a test parameter
        HashMap<ITestPolicyCmptTypeParameter, List<ITestPolicyCmptLink>> param2Links = new HashMap<ITestPolicyCmptTypeParameter, List<ITestPolicyCmptLink>>(
                oldLinks.size());
        for (Iterator<ITestPolicyCmptLink> iter = oldLinks.iterator(); iter.hasNext();) {
            ITestPolicyCmptLink testPolicyCmptLink = iter.next();
            ITestPolicyCmptTypeParameter paramOfLink = testPolicyCmptLink.findTestPolicyCmptTypeParameter(ipsProject);
            List<ITestPolicyCmptLink> linkList = param2Links.get(paramOfLink);
            if (linkList == null) {
                linkList = new ArrayList<ITestPolicyCmptLink>();
            }
            linkList.add(testPolicyCmptLink);
            param2Links.put(paramOfLink, linkList);
        }

        // sort the list of links for each parameter in order of their parameter
        List<ITestPolicyCmptLink> newChildList = new ArrayList<ITestPolicyCmptLink>();
        ITestPolicyCmptTypeParameter param = findTestPolicyCmptTypeParameter(getIpsProject());
        if (param == null) {
            throw new RuntimeException("Test parameter not found: " + testPolicyCmptType + "!"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ITestPolicyCmptTypeParameter[] paramChild = param.getTestPolicyCmptTypeParamChilds();
        // iterate over all links in the corresponding parameter and add the link lists to
        // the new whole link list
        for (int i = 0; i < paramChild.length; i++) {
            // get the list of links for the parameter
            List<ITestPolicyCmptLink> links = param2Links.get(paramChild[i]);
            if (links == null) {
                // ignore if there are no such kind of link
                continue;
            }
            newChildList.addAll(links);
        }
        testPolicyCmptLinks = newChildList;
        valueChanged(false, true);
    }

    /**
     * Fix the sort order of the test attribute values in order to the corresponding test policy
     * cmpt type parameter test attributes.
     * 
     * @throws CoreException in case of an error
     */

    void fixDifferentTestAttrValueSortOrder() throws CoreException {
        List<ITestAttributeValue> newTestAttrValueList = new ArrayList<ITestAttributeValue>();
        ITestPolicyCmptTypeParameter param = findTestPolicyCmptTypeParameter(getIpsProject());
        ITestAttribute[] testAttr = param.getTestAttributes();
        for (int i = 0; i < testAttr.length; i++) {
            ITestAttributeValue testAttrValue = getTestAttributeValue(testAttr[i].getName());
            if (testAttrValue == null) {
                throw new CoreException(
                        new IpsStatus(
                                "Couldn't fix the sort order of the test attribute values, because there is a mismatch between test case and its corresponding type!")); //$NON-NLS-1$
            }
            newTestAttrValueList.add(testAttrValue);
        }
        testAttributeValues = newTestAttrValueList;
        valueChanged(false, true);
    }

    /**
     * Returns all test policy cmpt links.<br>
     * Packageprivate to enable testing only.
     */
    protected ITestPolicyCmptLink[] getPolicyCmptLink() {
        return testPolicyCmptLinks.toArray(new ITestPolicyCmptLink[testPolicyCmptLinks.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        // validate if the test case type param exists
        ITestPolicyCmptTypeParameter param = findTestPolicyCmptTypeParameter(ipsProject);
        IProductCmpt productCmptObj = findProductCmpt(ipsProject);

        if (param == null) {
            String text = NLS.bind(Messages.TestPolicyCmpt_ValidationError_TestCaseTypeParamNotFound,
                    testPolicyCmptType);
            Message msg = new Message(MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND, text, Message.ERROR, this,
                    PROPERTY_TESTPOLICYCMPTTYPE);
            list.add(msg);
        } else {
            // check if the param defines the requirement for a product component but not product
            // component is specified
            if (param.isRequiresProductCmpt() && StringUtils.isEmpty(getProductCmpt())) {
                String text = Messages.TestPolicyCmpt_ValidationError_ProductCmptRequired;
                Message msg = new Message(MSGCODE_PRODUCT_CMPT_IS_REQUIRED, text, Message.ERROR, this,
                        PROPERTY_PRODUCTCMPT);
                list.add(msg);
            }
            // check if the policy component type exists
            final IPolicyCmptType policyCmptTypeDefinedInTestType = param.findPolicyCmptType(ipsProject);
            if (policyCmptTypeDefinedInTestType == null) {
                String text = NLS.bind(Messages.TestPolicyCmpt_ValidationWarning_PolicyCmptNotExists, param
                        .getPolicyCmptType(), testPolicyCmptType);
                Message msg = new Message(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, text,
                        Message.WARNING, this, PROPERTY_POLICYCMPTTYPE);
                list.add(msg);
            }
            // check if the policy component type used in the test case is not abstract
            // a) note that the used policy component type in the test case could differ to the
            // specified in the
            // test case type parameter, because in the type parameter super-(abstract)-types could
            // be added and in the
            // test case sub-types could by used
            // b) note that using the test case editor, abstract types couldn't be added to an ips
            // test cases
            // but the abstract flag could be changed afterwards and then we need to the element as
            // error
            IPolicyCmptType policyCmptTypeUsed = findPolicyCmptType();
            if (policyCmptTypeUsed != null) {
                if (policyCmptTypeUsed.isAbstract()) {
                    String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptIsAbstract,
                            policyCmptTypeUsed.getQualifiedName());
                    Message msg = new Message(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_IS_ABSTRACT, text,
                            Message.ERROR, this, PROPERTY_POLICYCMPTTYPE);
                    list.add(msg);
                } else {
                    // check if the policy cmpt type is assignable to the type defined in the test
                    // parameter
                    if (policyCmptTypeDefinedInTestType != null) {
                        if (!policyCmptTypeUsed.isSubtypeOrSameType(policyCmptTypeDefinedInTestType, ipsProject)) {
                            String text = NLS
                                    .bind(
                                            Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationErrorPolicyCmptTypeNoSubtypeOrSameTypeParam,
                                            policyCmptTypeUsed.getQualifiedName(), policyCmptTypeDefinedInTestType
                                                    .getQualifiedName());
                            Message msg = new Message(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_ASSIGNABLE, text,
                                    Message.ERROR, this, PROPERTY_POLICYCMPTTYPE);
                            list.add(msg);
                        }
                    }
                }
            } else {
                // policy cmpt type not found
                String policyCmptTypeUsedQName = findPolicyCmptTypeQName(ipsProject);
                String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptTypeNotExists,
                        policyCmptTypeUsedQName);
                Message msg = new Message(ITestPolicyCmptTypeParameter.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS, text,
                        Message.ERROR, this, PROPERTY_POLICYCMPTTYPE);
                list.add(msg);
            }
        }

        // check if the product component exists
        if (StringUtils.isNotEmpty(productCmpt) && productCmptObj == null) {
            String text = NLS.bind(Messages.TestPolicyCmpt_ValidationWarning_ProductComponentNotExists, productCmpt,
                    testPolicyCmptType);
            Message msg = new Message(MSGCODE_PRODUCT_CMPT_NOT_EXISTS, text, Message.ERROR, this, PROPERTY_PRODUCTCMPT);
            list.add(msg);
        }

        // check if a product component is not required but the test policy cmpt defines a product
        // cmpt
        if (param != null && StringUtils.isNotEmpty(productCmpt) && !param.isRequiresProductCmpt()) {
            String text = NLS.bind(
                    Messages.TestPolicyCmpt_ValidationError_ProductCmptNotRequiredButIsRelatedToProductCmpt,
                    testPolicyCmptType);
            Message msg = new Message(MSGCODE_PRODUCT_COMPONENT_NOT_REQUIRED, text, Message.ERROR, this,
                    PROPERTY_PRODUCTCMPT);
            list.add(msg);
        }

        // validate the min and max occurence defined in the test policy component type
        // parameter, get all possible link defined in the parameter and check the min and may
        // instances
        if (param != null) {
            ITestPolicyCmptTypeParameter[] paramForLinks = param.getTestPolicyCmptTypeParamChilds();
            for (int i = 0; i < paramForLinks.length; i++) {
                int currNumberOfInstances = getTestPolicyCmptLinks(paramForLinks[i].getName()).length;

                // check min and max instances
                int minInstances = paramForLinks[i].getMinInstances();
                int maxInstances = paramForLinks[i].getMaxInstances();
                if (currNumberOfInstances < minInstances) {
                    String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_MinimumNotReached,
                            "" + paramForLinks[i].getMinInstances(), paramForLinks[i].getName()); //$NON-NLS-1$
                    Message msg = new Message(MSGCODE_MIN_INSTANCES_NOT_REACHED, text, Message.ERROR, this,
                            ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
                    list.add(msg);
                }

                if (currNumberOfInstances > maxInstances) {
                    String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_MaximumReached,
                            "" + maxInstances, paramForLinks[i].getName()); //$NON-NLS-1$
                    Message msg = new Message(MSGCODE_MAX_INSTANCES_REACHED, text, Message.ERROR, this,
                            ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
                    list.add(msg);
                }
            }
        }

        // check that only one, the product component or the policy cmpt type is given
        if (StringUtils.isNotEmpty(policyCmptType) && StringUtils.isNotEmpty(productCmpt)) {
            String text = Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptTypeNotAllowedIfProductCmptIsSet;
            Message msg = new Message(MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN, text, Message.ERROR, this,
                    ITestPolicyCmpt.PROPERTY_POLICYCMPTTYPE);
            list.add(msg);
        }

        // check correct product cmpt
        validateAllowedProductCmpt(list, param, productCmptObj, ipsProject);
    }

    /*
     * Returns the qualified name of the corresponding policy component type, if a product is
     * assigned then the qualified name of the policy cmpt type of the product will be returned
     * otherwise if a policy component type is assign (without a product) then this qualified name
     * will be returned, if both are empty then the policy component type defined in the
     * testPolicyCmptTypeParameter will be returned. Returns null if the no related policy component
     * found.
     */
    private String findPolicyCmptTypeQName(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isNotEmpty(productCmpt)) {
            // find using the product cmpt
            IProductCmpt productCmptFound = findProductCmpt(ipsProject);
            if (productCmptFound != null) {
                IPolicyCmptType policyCmptType = productCmptFound.findPolicyCmptType(ipsProject);
                if (policyCmptType == null) {
                    return null;
                }
                return policyCmptType.getQualifiedName();
            }
        } else {
            // "find" using the given policy cmpt type
            if (!StringUtils.isEmpty(policyCmptType)) {
                return policyCmptType;
            } else {
                ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = findTestPolicyCmptTypeParameter(ipsProject);
                if (testPolicyCmptTypeParam != null) {
                    return testPolicyCmptTypeParam.getPolicyCmptType();
                }
            }
        }
        return null;
    }

    private void validateAllowedProductCmpt(MessageList list,
            ITestPolicyCmptTypeParameter param,
            IProductCmpt productCmptCandidateObj,
            IIpsProject ipsProject) throws CoreException {
        // abort validation if no product cmpt was found/or specified
        // or if the parameter wasn't found or if the param isn't product relevant
        if (param == null || productCmptCandidateObj == null || !param.isRequiresProductCmpt()) {
            return;
        }

        // if this is the root element, check only the correct policy cmpt type of the specified
        // product cmpt
        IPolicyCmptType policyCmptType = param.findPolicyCmptType(ipsProject);
        IPolicyCmptType policyCmptTypeOfCandidate = productCmptCandidateObj.findPolicyCmptType(ipsProject);
        if (policyCmptType != null && !policyCmptType.equals(policyCmptTypeOfCandidate)) {
            // maybe the policy cmpt type of the product cmpt candidate is an subtype of the
            // specified type in the test case type param
            if (policyCmptTypeOfCandidate == null || !policyCmptTypeOfCandidate.isSubtypeOf(policyCmptType, ipsProject)) {
                String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowedRoot,
                        productCmptCandidateObj.getName());
                Message msg = new Message(MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK, text, Message.ERROR, this,
                        ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                list.add(msg);
                return;
            }
        }

        if (!isRoot()) {
            // this is a child test policy cmpt, check allowed product depending on parent product
            // cmpt
            ITestPolicyCmpt parentPolicyCmpt = getParentTestPolicyCmpt();
            if (parentPolicyCmpt == null) {
                // no further validation possible because parent policy cmpt not found
                return;
            }

            // if parent product cmpt not found, add warning
            // check allowed product cmpt by using parent product cmpt
            ITestPolicyCmptTypeParameter parentParameter = param.getParentTestPolicyCmptTypeParam();
            if (parentParameter == null || !parentParameter.isRequiresProductCmpt()) {
                // no further validation possible because parent policy cmpt isn't product relevant
                return;
            }

            if (!parentParameter.isRequiresProductCmpt()) {
                // parent isn't product relevant no further validation of allowed target possible
                return;
            }

            IProductCmpt productCmptOfParent = parentPolicyCmpt.findProductCmpt(ipsProject);
            if (productCmptOfParent == null) {
                String text = NLS
                        .bind(
                                Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpCouldNotValidatedParentNotFound,
                                productCmptCandidateObj.getName());
                Message msg = new Message(MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED, text, Message.WARNING,
                        this, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                list.add(msg);
                return;
            }

            IPolicyCmptTypeAssociation association = param.findAssociation(ipsProject);
            if (association == null) {
                // no further validation because association not found
                return;
            }

            IProductCmptTypeAssociation productCmptTypeAssociation = association
                    .findMatchingProductCmptTypeAssociation(ipsProject);
            if (productCmptTypeAssociation == null) {
                // no further validation because association of product cmpt type not found
                return;
            }

            if (!productCmptOfParent.isUsedAsTargetProductCmpt(ipsProject, productCmptCandidateObj)) {
                String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowed,
                        productCmptCandidateObj.getName());
                Message msg = new Message(MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK, text, Message.ERROR, this,
                        ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                list.add(msg);
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IAttribute findProductCmptTypeAttribute(String attribute, IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(getProductCmpt())) {
            // no product cmpt is set, therefore no attribute could be searched,
            // currently an attributes (from sublcasses) could only be searched if an product cmpt
            // was set
            return null;
        }
        IProductCmpt productCmptObj = findProductCmpt(ipsProject);
        if (productCmptObj == null) {
            return null;
        }
        // TODO v2 - Joerg: search attribute using product cmpt type
        IPolicyCmptType pct = productCmptObj.findPolicyCmptType(ipsProject);
        if (pct == null) {
            return null;
        }
        return pct.findPolicyCmptTypeAttribute(attribute, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findPolicyCmptType() {
        IIpsProject ipsProject = getTestCase().getIpsProject();
        try {
            final String policyCmptTypeQName = findPolicyCmptTypeQName(ipsProject);
            if (policyCmptTypeQName == null) {
                return null;
            }
            return ipsProject.findPolicyCmptType(policyCmptTypeQName);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setPolicyCmptType(String policyCmptType) {
        String oldPolicyCmptType = this.policyCmptType;
        this.policyCmptType = policyCmptType;
        valueChanged(oldPolicyCmptType, policyCmptType);
    }
}
