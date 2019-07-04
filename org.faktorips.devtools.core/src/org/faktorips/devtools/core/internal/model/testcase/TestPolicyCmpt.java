/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.SingleEventModification;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
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

    final static String TAG_NAME = "PolicyCmptTypeObject"; //$NON-NLS-1$

    private String testPolicyCmptType = ""; //$NON-NLS-1$

    private String productCmpt = ""; //$NON-NLS-1$

    /** if productCmpt is empty then the test policy cmpt is stored */
    private String policyCmptType = ""; //$NON-NLS-1$

    private List<ITestAttributeValue> testAttributeValues = new ArrayList<ITestAttributeValue>(0);

    private List<ITestPolicyCmptLink> testPolicyCmptLinks = new ArrayList<ITestPolicyCmptLink>(0);

    public TestPolicyCmpt(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        int size = testAttributeValues.size() + testPolicyCmptLinks.size();
        List<IIpsElement> children = new ArrayList<IIpsElement>(size);
        children.addAll(testAttributeValues);
        children.addAll(testPolicyCmptLinks);
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected void reinitPartCollectionsThis() {
        testAttributeValues = new ArrayList<ITestAttributeValue>();
        testPolicyCmptLinks = new ArrayList<ITestPolicyCmptLink>();
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof TestAttributeValue) {
            testAttributeValues.add((TestAttributeValue)part);
            return true;
        } else if (part instanceof TestPolicyCmptLink) {
            testPolicyCmptLinks.add((TestPolicyCmptLink)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof TestAttributeValue) {
            testAttributeValues.remove(part);
            return true;
        } else if (part instanceof TestPolicyCmptLink) {
            testPolicyCmptLinks.remove(part);
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(TestAttributeValue.TAG_NAME)) {
            return newTestAttributeValueInternal(id);
        } else if (xmlTagName.equals(TestPolicyCmptLink.TAG_NAME)) {
            return newTestPcTypeLinkInternal(id);
        }
        return null;
    }

    @Override
    public String getTestPolicyCmptTypeParameter() {
        return testPolicyCmptType;
    }

    @Override
    public void setTestPolicyCmptTypeParameter(String testPolicyCmptTypeParameter) {
        String oldPolicyCmptType = testPolicyCmptType;
        testPolicyCmptType = testPolicyCmptTypeParameter;
        valueChanged(oldPolicyCmptType, testPolicyCmptTypeParameter);
    }

    @Override
    public ITestParameter findTestParameter(IIpsProject ipsProject) throws CoreException {
        return findTestPolicyCmptTypeParameter(ipsProject);
    }

    @Override
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(testPolicyCmptType)) {
            return null;
        }
        return ((TestCase)getTestCase()).findTestPolicyCmptTypeParameter(this, ipsProject);
    }

    @Override
    public String getTestParameterName() {
        return testPolicyCmptType;
    }

    /**
     * Use {@link #setProductCmptAndNameAfterIfApplicable(String)} if standard naming (or manual
     * naming) should be retained.
     */
    @Override
    public void setProductCmpt(String newProductCmpt) {
        String oldTestProductCmpt = productCmpt;
        productCmpt = newProductCmpt;
        valueChanged(oldTestProductCmpt, newProductCmpt);
    }

    @Override
    public String getProductCmpt() {
        return productCmpt;
    }

    @Override
    public void setProductCmptAndNameAfterIfApplicable(String prodCmptQName) {
        String oldProdCmptName = getNullSafeUnqualifiedName(getProductCmpt());
        String pctParameterName = getNullSafeUnqualifiedName(getTestPolicyCmptTypeParameter());

        setProductCmpt(prodCmptQName);

        /*
         * Regular expression to match product component names with and without a " (<number>)"
         * postfix.
         */
        if (getName() == null || (StringUtils.isEmpty(oldProdCmptName) && getName().equals(pctParameterName))
                || getName().matches(oldProdCmptName + "( \\([1-9][0-9]*\\))?")) { //$NON-NLS-1$
            String uniqueName = null;
            if (StringUtils.isEmpty(prodCmptQName)) {
                uniqueName = getTestCase().generateUniqueNameForTestPolicyCmpt(this, pctParameterName);
            } else {
                uniqueName = getTestCase().generateUniqueNameForTestPolicyCmpt(this,
                        StringUtil.unqualifiedName(prodCmptQName));
            }
            setName(uniqueName);
        }
    }

    private String getNullSafeUnqualifiedName(String qName) {
        return qName == null ? "" : StringUtil.unqualifiedName(qName); //$NON-NLS-1$
    }

    @Override
    public IProductCmpt findProductCmpt(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(productCmpt)) {
            return null;
        }
        IProductCmpt pc = ipsProject.findProductCmpt(productCmpt);
        return pc;
    }

    @Override
    public boolean isProductRelevant() {
        return !StringUtils.isEmpty(productCmpt);
    }

    /**
     * For test purposes and special cases only. Use
     * {@link #setProductCmptAndNameAfterIfApplicable(String)} if standard naming (or manual naming)
     * should be retained.
     */
    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    /**
     * Returns the top level test case.
     */
    @Override
    public ITestCase getTestCase() {
        return ((ITestCase)getRoot().getParent());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        testPolicyCmptType = element.getAttribute(PROPERTY_TESTPOLICYCMPTTYPE);
        policyCmptType = element.getAttribute(PROPERTY_POLICYCMPTTYPE);
        productCmpt = element.getAttribute(PROPERTY_PRODUCTCMPT);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_TESTPOLICYCMPTTYPE, testPolicyCmptType);
        element.setAttribute(PROPERTY_POLICYCMPTTYPE, policyCmptType);
        element.setAttribute(PROPERTY_PRODUCTCMPT, productCmpt);
    }

    @Override
    public ITestAttributeValue newTestAttributeValue() {
        TestAttributeValue a = newTestAttributeValueInternal(getNextPartId());
        objectHasChanged();
        return a;
    }

    /**
     * Creates a new test attribute without updating the src file.
     */
    private TestAttributeValue newTestAttributeValueInternal(String id) {
        TestAttributeValue a = new TestAttributeValue(this, id);
        testAttributeValues.add(a);
        return a;
    }

    @Override
    public ITestAttributeValue getTestAttributeValue(String name) {
        for (ITestAttributeValue a : testAttributeValues) {
            if (a.getTestAttribute().equals(name)) {
                return a;
            }
        }
        return null;
    }

    @Override
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

    @Override
    public ITestPolicyCmptLink getTestPolicyCmptLink(String testPolicyCmptType) {
        ArgumentCheck.notNull(testPolicyCmptType);
        for (ITestPolicyCmptLink r : testPolicyCmptLinks) {
            if (r.getTestPolicyCmptTypeParameter().equals(testPolicyCmptType)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public ITestPolicyCmptLink[] getTestPolicyCmptLinks() {
        ITestPolicyCmptLink[] r = new ITestPolicyCmptLink[testPolicyCmptLinks.size()];
        testPolicyCmptLinks.toArray(r);
        return r;
    }

    @Override
    public ITestPolicyCmptLink[] getTestPolicyCmptLinks(String typeParameterName) {
        List<ITestPolicyCmptLink> links = new ArrayList<ITestPolicyCmptLink>();
        for (ITestPolicyCmptLink element : testPolicyCmptLinks) {
            if (element.getTestPolicyCmptTypeParameter().equals(typeParameterName)) {
                links.add(element);
            }
        }
        return links.toArray(new ITestPolicyCmptLink[0]);
    }

    @Override
    public ITestPolicyCmptLink newTestPolicyCmptLink() {
        ITestPolicyCmptLink r = newTestPcTypeLinkInternal(getNextPartId());
        objectHasChanged();
        return r;
    }

    /**
     * Creates a new test link without updating the src file.
     */
    private TestPolicyCmptLink newTestPcTypeLinkInternal(String id) {
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

    @Override
    public boolean isRoot() {
        return (!(getParent() instanceof TestPolicyCmptLink));
    }

    @Override
    public ITestObject getRoot() {
        ITestPolicyCmpt testPolicyCmpt = this;
        while (!testPolicyCmpt.isRoot()) {
            testPolicyCmpt = testPolicyCmpt.getParentTestPolicyCmpt();
        }
        return testPolicyCmpt;
    }

    @Override
    public ITestPolicyCmpt getParentTestPolicyCmpt() {
        if (isRoot()) {
            return null;
        }
        ITestPolicyCmptLink testPcTypeLink = (ITestPolicyCmptLink)getParent();
        return (ITestPolicyCmpt)testPcTypeLink.getParent();
    }

    @Override
    public void removeLink(ITestPolicyCmptLink link) {
        int idx = 0;
        int foundIdx = -1;
        for (ITestPolicyCmptLink element : testPolicyCmptLinks) {
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

    @Override
    public ITestPolicyCmptLink addTestPcTypeLink(ITestPolicyCmptTypeParameter typeParam,
            String productCmpt,
            String policyCmptType,
            String targetName) throws CoreException {

        return addTestPcTypeLink(typeParam, productCmpt, policyCmptType, targetName, false);
    }

    @Override
    public ITestPolicyCmptLink addTestPcTypeLink(final ITestPolicyCmptTypeParameter typeParam,
            final String productCmpt,
            final String policyCmptType,
            final String targetName,
            final boolean recursivelyAddRequired) throws CoreException {

        ArgumentCheck.notNull(typeParam);
        if (!StringUtils.isEmpty(productCmpt) && !StringUtils.isEmpty(policyCmptType)) {
            throw new CoreException(new IpsStatus(Messages.TestPolicyCmpt_Error_ProductCmpAndPolicyCmptTypeGiven));
        }

        final IPolicyCmptTypeAssociation policyCmptTypeAssociation = typeParam.findAssociation(typeParam
                .getIpsProject());
        if (policyCmptTypeAssociation == null) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TestPolicyCmpt_Error_LinkNotFound,
                    typeParam.getAssociation())));
        }

        return getIpsModel().executeModificationsWithSingleEvent(
                new SingleEventModification<ITestPolicyCmptLink>(getIpsSrcFile()) {
                    private ITestPolicyCmptLink result;

                    @Override
                    protected ITestPolicyCmptLink getResult() {
                        return result;
                    }

                    @Override
                    protected boolean execute() throws CoreException {
                        if (!policyCmptTypeAssociation.isAssoziation()) {
                            // link is composition
                            // add new link including a test policy component child
                            result = new TestPolicyCmptLink(TestPolicyCmpt.this, getNextPartId());
                            result.setTestPolicyCmptTypeParameter(typeParam.getName());

                            ITestPolicyCmpt newTestPolicyCmpt = result.newTargetTestPolicyCmptChild();
                            newTestPolicyCmpt.setTestPolicyCmptTypeParameter(typeParam.getName());
                            newTestPolicyCmpt.setPolicyCmptType(StringUtils.isEmpty(policyCmptType) ? "" : policyCmptType); //$NON-NLS-1$
                            newTestPolicyCmpt.setProductCmptAndNameAfterIfApplicable(productCmpt);

                            // add all test attribute values as specified in the test parameter type
                            ITestAttribute attributes[] = typeParam.getTestAttributes();
                            for (ITestAttribute attribute : attributes) {
                                ITestAttributeValue attrValue = newTestPolicyCmpt.newTestAttributeValue();
                                attrValue.setTestAttribute(attribute.getName());
                            }

                            // set the defaults for all attribute values
                            newTestPolicyCmpt.updateDefaultTestAttributeValues();

                            // if desired, recursively add links as possible
                            if (recursivelyAddRequired && newTestPolicyCmpt.findProductCmpt(getIpsProject()) != null) {
                                newTestPolicyCmpt.addRequiredLinks(typeParam.getIpsProject());
                            }

                        } else {
                            // link is association
                            // add new association link (only the target will be set and no child
                            // will be created)
                            result = new TestPolicyCmptLink(TestPolicyCmpt.this, getNextPartId());
                            result.setTestPolicyCmptTypeParameter(typeParam.getName());
                            result.setTarget(targetName);
                        }

                        // add the new link at the end of the existing links, grouped by the link
                        // name
                        ITestPolicyCmptLink prevLinkWithSameName = null;
                        for (ITestPolicyCmptLink currLink : testPolicyCmptLinks) {
                            if (result.getTestPolicyCmptTypeParameter().equals(
                                    currLink.getTestPolicyCmptTypeParameter())) {
                                prevLinkWithSameName = currLink;
                            }
                        }

                        if (prevLinkWithSameName != null) {
                            int idx = testPolicyCmptLinks.indexOf(prevLinkWithSameName);
                            testPolicyCmptLinks.add(idx + 1, result);
                        } else {
                            testPolicyCmptLinks.add(result);
                        }

                        fixDifferentChildSortOrder();

                        return true;
                    }
                });
    }

    @Override
    public void addRequiredLinks(final IIpsProject ipsProject) throws CoreException {
        final IProductCmpt originalProductCmpt = findProductCmpt(getIpsProject());
        if (originalProductCmpt == null) {
            throw new IllegalStateException();
        }

        getIpsModel().executeModificationsWithSingleEvent(new SingleEventModification<Void>(getIpsSrcFile()) {
            @Override
            protected boolean execute() throws CoreException {
                for (ITestPolicyCmptTypeParameter childParameter : findTestPolicyCmptTypeParameter(ipsProject)
                        .getTestPolicyCmptTypeParamChilds()) {
                    boolean addedViaTestParameter = addRequiredLinksViaTestParameter(childParameter);
                    if (!addedViaTestParameter) {
                        addRequiredLinksViaParentProductCmpt(childParameter, originalProductCmpt);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Adds recursive {@linkplain ITestPolicyCmptLink test policy component links} by deducing the
     * target product component from the test parameter, returns {@code false} if that was not
     * possible.
     */
    private boolean addRequiredLinksViaTestParameter(ITestPolicyCmptTypeParameter testParameter) throws CoreException {
        IIpsSrcFile[] allowedProductCmptSrcFiles = testParameter.getAllowedProductCmpt(getIpsProject(), null);
        if (allowedProductCmptSrcFiles.length != 1 || testParameter.getMinInstances() == 0) {
            return false;
        }

        // add as many links as defined by the minimum instances
        for (int i = 0; i < testParameter.getMinInstances(); i++) {
            addTestPcTypeLink(testParameter, allowedProductCmptSrcFiles[0].getQualifiedNameType().getName(), null,
                    null, true);
        }
        return true;
    }

    /**
     * Adds recursive {@linkplain ITestPolicyCmptLink test policy component links} as is possible by
     * deducing the target product component from the parent product component.
     */
    private void addRequiredLinksViaParentProductCmpt(ITestPolicyCmptTypeParameter testParameter,
            IProductCmpt originalProductCmpt) throws CoreException {

        IIpsSrcFile[] allowedProductCmptSrcFiles = testParameter.getAllowedProductCmpt(getIpsProject(),
                originalProductCmpt);
        List<IProductCmptLink> links = originalProductCmpt.getLinksIncludingGenerations();
        for (IIpsSrcFile allowedProductCmptSrcFile : allowedProductCmptSrcFiles) {
            for (IProductCmptLink link : links) {
                if (link.getTarget().equals(allowedProductCmptSrcFile.getQualifiedNameType().getName())) {
                    // add as many links as defined by the minimum cardinality
                    for (int i = 0; i < link.getMinCardinality(); i++) {
                        addTestPcTypeLink(testParameter, allowedProductCmptSrcFile.getQualifiedNameType().getName(),
                                null, null, true);
                    }
                }
            }
        }
    }

    @Override
    public void updateDefaultTestAttributeValues() throws CoreException {
        // add the attributes which are defined in the test case type parameter
        IProductCmptGeneration generation = findProductCmpsCurrentGeneration(getIpsProject());
        ITestAttributeValue[] testAttrValues = getTestAttributeValues();
        for (ITestAttributeValue testAttrValue : testAttrValues) {
            /*
             * set default value only if the model attribute is relevant by the specified product
             * cmpt otherwise set the value to null, therefore if the value is null and the
             * currently specified product cmpt doesn't configure this attribute the attribute will
             * be hidden in the test case editor (because it is null), otherwise if the value isn't
             * null the test attribute value will be displayed and a warning will be shown
             */
            if (generation != null) {
                ITestAttribute testAttribute = testAttrValue.findTestAttribute(getIpsProject());
                if (testAttribute != null) {
                    if (!testAttribute.isAttributeRelevantByProductCmpt(generation.getProductCmpt(), getIpsProject())) {
                        ((TestAttributeValue)testAttrValue).setValue(null);
                        continue;
                    }
                }
                ((TestAttributeValue)testAttrValue).setDefaultTestAttributeValueInternal(generation);
            }
        }
    }

    @Override
    public int getIndexOfChildTestPolicyCmpt(ITestPolicyCmpt testPolicyCmpt) throws CoreException {
        Assert.isNotNull(testPolicyCmpt);
        int idx = 0;
        for (ITestPolicyCmptLink testPolicyCmptLink : testPolicyCmptLinks) {
            if (testPolicyCmpt.equals(testPolicyCmptLink.findTarget())) {
                return idx;
            }
            idx++;
        }
        throw new CoreException(new IpsStatus(Messages.TestPolicyCmpt_Error_MoveNotPossibleBelongsToNoLink));
    }

    @Override
    public int[] moveTestPolicyCmptLink(int[] indexes, boolean up) {
        ListElementMover<ITestPolicyCmptLink> mover = new ListElementMover<ITestPolicyCmptLink>(testPolicyCmptLinks);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }

    /**
     * Returns the latest product component generation.
     * <p>
     * Returns {@code null} if the test policy component is not product relevant or if the product
     * component cannot be found.
     */
    public IProductCmptGeneration findProductCmpsCurrentGeneration(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(productCmpt)) {
            return null;
        }

        IProductCmpt productCmptObj = ipsProject.findProductCmpt(productCmpt);
        if (productCmptObj == null) {
            return null;
        }

        return productCmptObj.getLatestProductCmptGeneration();
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
        for (ITestPolicyCmptLink testPolicyCmptLink : oldLinks) {
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
        /*
         * iterate over all links in the corresponding parameter and add the link lists to the new
         * whole link list
         */
        for (ITestPolicyCmptTypeParameter element : paramChild) {
            // get the list of links for the parameter
            List<ITestPolicyCmptLink> links = param2Links.get(element);
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
        for (ITestAttribute element : testAttr) {
            ITestAttributeValue testAttrValue = getTestAttributeValue(element.getName());
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
     * Returns all test policy cmpt links.
     */
    // Package private to enable testing only.
    protected ITestPolicyCmptLink[] getPolicyCmptLink() {
        return testPolicyCmptLinks.toArray(new ITestPolicyCmptLink[testPolicyCmptLinks.size()]);
    }

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
            /*
             * check if the param defines the requirement for a product component but no product
             * component is specified
             */
            if (param.isRequiresProductCmpt() && !hasProductCmpt()) {
                String text = Messages.TestPolicyCmpt_ValidationError_ProductCmptRequired;
                Message msg = new Message(MSGCODE_PRODUCT_CMPT_IS_REQUIRED, text, Message.ERROR, this,
                        PROPERTY_PRODUCTCMPT);
                list.add(msg);
            }
            // check if the policy component type exists
            final IPolicyCmptType policyCmptTypeDefinedInTestType = param.findPolicyCmptType(ipsProject);
            if (policyCmptTypeDefinedInTestType == null) {
                String text = NLS.bind(Messages.TestPolicyCmpt_ValidationWarning_PolicyCmptNotExists,
                        param.getPolicyCmptType(), testPolicyCmptType);
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
                                    .bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationErrorPolicyCmptTypeNoSubtypeOrSameTypeParam,
                                            policyCmptTypeUsed.getQualifiedName(),
                                            policyCmptTypeDefinedInTestType.getQualifiedName());
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
        if (hasProductCmpt() && productCmptObj == null) {
            String text = NLS.bind(Messages.TestPolicyCmpt_ValidationWarning_ProductComponentNotExists, productCmpt,
                    testPolicyCmptType);
            Message msg = new Message(MSGCODE_PRODUCT_CMPT_NOT_EXISTS, text, Message.ERROR, this, PROPERTY_PRODUCTCMPT);
            list.add(msg);
        }

        /*
         * validate the min and max occurence defined in the test policy component type parameter,
         * get all possible link defined in the parameter and check the min and may instances
         */
        if (param != null) {
            ITestPolicyCmptTypeParameter[] paramForLinks = param.getTestPolicyCmptTypeParamChilds();
            for (ITestPolicyCmptTypeParameter paramForLink : paramForLinks) {
                int currNumberOfInstances = getTestPolicyCmptLinks(paramForLink.getName()).length;

                // check min and max instances
                int minInstances = paramForLink.getMinInstances();
                int maxInstances = paramForLink.getMaxInstances();
                if (currNumberOfInstances < minInstances) {
                    String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_MinimumNotReached,
                            "" + paramForLink.getMinInstances(), paramForLink.getName()); //$NON-NLS-1$
                    Message msg = new Message(MSGCODE_MIN_INSTANCES_NOT_REACHED, text, Message.ERROR, this,
                            ITestPolicyCmptTypeParameter.PROPERTY_MIN_INSTANCES);
                    list.add(msg);
                }

                if (currNumberOfInstances > maxInstances) {
                    String text = NLS.bind(Messages.TestPolicyCmptLink_ValidationError_MaximumReached,
                            "" + maxInstances, paramForLink.getName()); //$NON-NLS-1$
                    Message msg = new Message(MSGCODE_MAX_INSTANCES_REACHED, text, Message.ERROR, this,
                            ITestPolicyCmptTypeParameter.PROPERTY_MAX_INSTANCES);
                    list.add(msg);
                }
            }
        }

        // check that only one, the product component or the policy cmpt type is given
        if (StringUtils.isNotEmpty(policyCmptType) && hasProductCmpt()) {
            String text = Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_PolicyCmptTypeNotAllowedIfProductCmptIsSet;
            Message msg = new Message(MSGCODE_POLICY_CMPT_TYPE_AND_PRODUCT_CMPT_TYPE_GIVEN, text, Message.ERROR, this,
                    ITestPolicyCmpt.PROPERTY_POLICYCMPTTYPE);
            list.add(msg);
        }

        // check correct product cmpt
        validateAllowedProductCmpt(list, param, productCmptObj, ipsProject);
    }

    /**
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
            }
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParam = findTestPolicyCmptTypeParameter(ipsProject);
            if (testPolicyCmptTypeParam != null) {
                return testPolicyCmptTypeParam.getPolicyCmptType();
            }
        }
        return null;
    }

    private void validateAllowedProductCmpt(MessageList list,
            ITestPolicyCmptTypeParameter param,
            IProductCmpt productCmptCandidateObj,
            IIpsProject ipsProject) throws CoreException {

        /*
         * abort validation if no product cmpt was found/or specified or if the parameter wasn't
         * found
         */
        if (param == null || productCmptCandidateObj == null) {
            return;
        }

        /*
         * if this is the root element, only check the policy component type of the specified
         * product cmpt
         */
        IPolicyCmptType policyCmptType = param.findPolicyCmptType(ipsProject);
        IPolicyCmptType policyCmptTypeOfCandidate = productCmptCandidateObj.findPolicyCmptType(ipsProject);
        if (policyCmptType != null && !policyCmptType.equals(policyCmptTypeOfCandidate)) {
            // maybe the policy cmpt type of the product cmpt candidate is a subtype of the
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
            /*
             * this is a child test policy cmpt, check allowed product depending on parent product
             * cmpt
             */
            ITestPolicyCmpt parentTestPolicyCmpt = getParentTestPolicyCmpt();
            if (parentTestPolicyCmpt == null) {
                // no further validation possible because parent policy cmpt not found
                return;
            } else {
                IPolicyCmptType parentPolicyCmptType = parentTestPolicyCmpt.findPolicyCmptType();
                if (parentPolicyCmptType == null || !parentPolicyCmptType.isConfigurableByProductCmptType()) {
                    /*
                     * No further validation possible as parent policyCmptType is not product
                     * relevant
                     */
                    return;
                }
            }

            ITestPolicyCmptTypeParameter parentParameter = param.getParentTestPolicyCmptTypeParam();
            if (parentParameter == null) {
                /*
                 * No further validation possible because parent policyTypeparameter not found
                 */
                return;
            }

            /*
             * Add warning and abort validation in all cases in which no parentProductCmpt was
             * found, even if parent does not require/define a product component. Add warning to
             * inform user, that validation could not be completed due to errors in parent.
             */
            IProductCmpt productCmptOfParent = parentTestPolicyCmpt.findProductCmpt(ipsProject);
            if (productCmptOfParent == null) {
                String text = NLS
                        .bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpCouldNotValidatedParentNotFound,
                                productCmptCandidateObj.getName());
                Message msg = new Message(MSGCODE_PARENT_PRODUCT_CMPT_OF_LINK_NOT_SPECIFIED, text, Message.WARNING,
                        this, ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                list.add(msg);
                return;
            }

            // Check allowed product cmpt by using parent product cmpt.
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

            // if no association between parentProdCmpt and childProdCmpt exists, add error
            if (!productCmptOfParent.isReferencingProductCmpt(ipsProject, productCmptCandidateObj)) {
                String text = NLS.bind(Messages.TestPolicyCmpt_TestPolicyCmpt_ValidationError_ProductCmpNotAllowed,
                        productCmptCandidateObj.getName());
                Message msg = new Message(MSGCODE_WRONG_PRODUCT_CMPT_OF_LINK, text, Message.ERROR, this,
                        ITestPolicyCmpt.PROPERTY_PRODUCTCMPT);
                list.add(msg);
                return;
            }
        }
    }

    @Override
    public IAttribute findProductCmptTypeAttribute(String attribute, IIpsProject ipsProject) throws CoreException {
        if (!hasProductCmpt()) {
            /*
             * no product cmpt is set, therefore no attribute could be searched, currently an
             * attributes (from sublcasses) could only be searched if an product cmpt was set
             */
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

    @Override
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

    @Override
    public boolean hasProductCmpt() {
        return !StringUtils.isEmpty(getProductCmpt());
    }

    @Override
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    @Override
    public void setPolicyCmptType(String policyCmptType) {
        String oldPolicyCmptType = this.policyCmptType;
        this.policyCmptType = policyCmptType;
        valueChanged(oldPolicyCmptType, policyCmptType);
    }

}
