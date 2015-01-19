/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptTypeAssociation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociation extends Association implements IProductCmptTypeAssociation {

    private String matchingAssociationSource = StringUtils.EMPTY;

    private String matchingAssociationName = StringUtils.EMPTY;

    private boolean changingOverTime = getProductCmptType().isChangingOverTime();

    private boolean relevant = true;

    public ProductCmptTypeAssociation(IProductCmptType parent, String id) {
        super(parent, id);
        setAssociationTypeInternal(AssociationType.AGGREGATION);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public boolean isQualified() {
        return false;
    }

    @Override
    public IProductCmptType findTargetProductCmptType(IIpsProject ipsProject) throws CoreException {
        return (IProductCmptType)ipsProject.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, getTarget());
    }

    @Override
    public IProductCmptTypeAssociation findConstrainedAssociation(IIpsProject ipsProject) {
        return (IProductCmptTypeAssociation)super.findConstrainedAssociation(ipsProject);
    }

    @Override
    public boolean constrainsPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {
        return findMatchingPolicyCmptTypeAssociation(ipsProject) != null;
    }

    @Override
    public Set<IPolicyCmptTypeAssociation> findPossiblyMatchingPolicyCmptTypeAssociations(IIpsProject ipsProject)
            throws CoreException {
        Set<IPolicyCmptTypeAssociation> result = new LinkedHashSet<IPolicyCmptTypeAssociation>();

        IPolicyCmptType sourcePolicyCmptType = getProductCmptType().findPolicyCmptType(ipsProject);
        if (sourcePolicyCmptType == null) {
            return result;
        }
        IProductCmptType targetProductCmptType = findTargetProductCmptType(ipsProject);
        if (targetProductCmptType == null) {
            return result;
        }
        IPolicyCmptType targetPolicyCmptType = targetProductCmptType.findPolicyCmptType(ipsProject);
        if (targetPolicyCmptType == null) {
            return result;
        }

        String targetQName = targetPolicyCmptType.getQualifiedName();
        collectPossibleMatchingAssociations(sourcePolicyCmptType, targetQName, result, ipsProject,
                new HashSet<IPolicyCmptType>());
        return result;
    }

    /**
     * searching recursively all {@link IPolicyCmptTypeAssociation} that could be configured by this
     * association
     * 
     * @param sourcePolicyCmptType the actual {@link IPolicyCmptType} which associations are
     *            analyzed
     * @param targetQName the name of the {@link IPolicyCmptType} that is configured by the target
     *            of this {@link IProductCmptTypeAssociation}
     * @param foundAssociations The list of already found {@link IPolicyCmptTypeAssociation}
     * @param ipsProject the {@link IIpsProject} used as searching base project
     * @return true if there was at least one match
     * @throws CoreException in case of a CoreException accessing the objects or resources
     */
    private boolean collectPossibleMatchingAssociations(IPolicyCmptType sourcePolicyCmptType,
            String targetQName,
            Set<IPolicyCmptTypeAssociation> foundAssociations,
            IIpsProject ipsProject,
            Set<IPolicyCmptType> alreadyVisit) throws CoreException {
        boolean result = false;
        List<IPolicyCmptTypeAssociation> policyAssociations = sourcePolicyCmptType.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyAssociations) {
            if (AssociationType.COMPOSITION_DETAIL_TO_MASTER.equals(policyCmptTypeAssociation.getAssociationType())) {
                // We ignore Detail-To-Master compositions
                continue;
            }
            if (targetQName.equals(policyCmptTypeAssociation.getTarget())) {
                // the target is the same as the Target-PolicyCmptType
                result = true;
                if (!foundAssociations.add(policyCmptTypeAssociation)) {
                    // already visited this component -- return to avoid cycles
                    return true;
                }
                continue;
            }
            IPolicyCmptType actualAssociationTarget = policyCmptTypeAssociation.findTargetPolicyCmptType(ipsProject);
            if (isTargetConfigurableByProductCmptType(actualAssociationTarget)) {
                // the actualAssociationTarget seems to be configured by another ProductCmptType
                continue;
            }
            IPolicyCmptType nextSource = policyCmptTypeAssociation.findTargetPolicyCmptType(ipsProject);
            if (nextSource != null) {
                boolean notVisitedYet = alreadyVisit.add(nextSource);
                if (notVisitedYet
                        && collectPossibleMatchingAssociations(nextSource, targetQName, foundAssociations, ipsProject,
                                alreadyVisit)) {
                    if (!foundAssociations.add(policyCmptTypeAssociation)) {
                        // already visited this component -- return to avoid cycles
                        return true;
                    }
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean isTargetConfigurableByProductCmptType(IPolicyCmptType actualAssociationTarget) {
        return actualAssociationTarget != null && actualAssociationTarget.isConfigurableByProductCmptType();
    }

    @Override
    public IPolicyCmptTypeAssociation findMatchingPolicyCmptTypeAssociation(IIpsProject ipsProject)
            throws CoreException {
        if (StringUtils.isEmpty(matchingAssociationSource) || StringUtils.isEmpty(matchingAssociationName)) {
            return findDefaultPolicyCmptTypeAssociation(ipsProject);
        }
        IPolicyCmptType policyCmptType = getIpsProject().findPolicyCmptType(matchingAssociationSource);
        if (policyCmptType == null) {
            return null;
        }
        return (IPolicyCmptTypeAssociation)policyCmptType.getAssociation(matchingAssociationName);
    }

    @Override
    public void setMatchingAssociationSource(String matchingAssociationSource) {
        String oldValue = this.matchingAssociationSource;
        this.matchingAssociationSource = matchingAssociationSource;
        valueChanged(oldValue, matchingAssociationSource);
    }

    @Override
    public String getMatchingAssociationSource() {
        return matchingAssociationSource;
    }

    @Override
    public void setMatchingAssociationName(String matchingAssociationName) {
        String oldValue = this.matchingAssociationName;
        this.matchingAssociationName = matchingAssociationName;
        valueChanged(oldValue, matchingAssociationName);
    }

    @Override
    public String getMatchingAssociationName() {
        return matchingAssociationName;
    }

    @Override
    public IPolicyCmptTypeAssociation findDefaultPolicyCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {

        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(ipsProject);
        if (policyCmptType == null) {
            return null;
        }
        // check if the policy component type is configured by this product component type.
        // (FIPS-714)
        if (!policyCmptType.getProductCmptType().equals(getProductCmptType().getQualifiedName())) {
            return null;
        }
        IProductCmptType targetType = findTargetProductCmptType(ipsProject);
        if (targetType == null) {
            return null;
        }
        IPolicyCmptType targetPolicyCmptType = targetType.findPolicyCmptType(ipsProject);
        if (targetPolicyCmptType == null) {
            return null;
        }
        IPolicyCmptTypeAssociation[] policyAssoc = getAssociationsFor(policyCmptType, targetPolicyCmptType);
        if (policyAssoc.length == 0) {
            return null;
        }
        // Assume that both PolicyCmptTypeAssociations and ProductCmptTypeAssociations are listed in
        // the same order.
        int index = getAssociationIndex();
        if (index >= policyAssoc.length) {
            return null;
        }
        return policyAssoc[index];
    }

    /**
     * Returns all {@code IPolicyCmptTypeAssociation}s which have the specified source and target
     * policy component type, but ignoring associations of type COMPOSITION_DETAIL_TO_MASTER.
     */
    private IPolicyCmptTypeAssociation[] getAssociationsFor(IPolicyCmptType from, IPolicyCmptType target) {
        List<IPolicyCmptTypeAssociation> result = new ArrayList<IPolicyCmptTypeAssociation>();
        String targetQName = target.getQualifiedName();
        List<IPolicyCmptTypeAssociation> policyAssociations = from.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation policyCmptTypeAssociation : policyAssociations) {
            if (targetQName.equals(policyCmptTypeAssociation.getTarget())) {
                if (!AssociationType.COMPOSITION_DETAIL_TO_MASTER
                        .equals(policyCmptTypeAssociation.getAssociationType())) {
                    result.add(policyCmptTypeAssociation);
                }
            }
        }
        return result.toArray(new IPolicyCmptTypeAssociation[result.size()]);
    }

    private int getAssociationIndex() {
        List<IAssociation> allAssociationsForTheTargetType = new ArrayList<IAssociation>();
        for (IAssociation element : getType().getAssociations()) {
            if (getTarget().equals(element.getTarget())) {
                allAssociationsForTheTargetType.add(element);
            }
        }
        int index = 0;
        for (Iterator<IAssociation> it = allAssociationsForTheTargetType.iterator(); it.hasNext(); index++) {
            if (it.next() == this) {
                return index;
            }
        }
        throw new RuntimeException("Can't get index of association " + this); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        validateMatchingAsoociation(list, ipsProject);
        validateDerivedUnionChangingOverTimeProperty(list, ipsProject);
        validateConstrainedChangeOverTime(list, ipsProject);
        validateTypeDoesNotAcceptChangingOverTime(list);
    }

    /**
     * Validates that derived unions and their subsets have the same changing over time property.
     * e.g. if a derived union is changing over time its subsets must also be defined as changing
     * over time. If a derived union is static its subsets must also be defined as static.
     * 
     * @param list the message list to add messages to
     * @param ipsProject the IPS project to be used for searching related IPS objects
     */
    protected void validateDerivedUnionChangingOverTimeProperty(MessageList list, IIpsProject ipsProject) {
        if (isSubsetOfADerivedUnion()) {
            try {
                IProductCmptTypeAssociation derivedUnionAssociation = (IProductCmptTypeAssociation)findSubsettedDerivedUnion(ipsProject);
                if (derivedUnionAssociation != null
                        && derivedUnionAssociation.isChangingOverTime() != isChangingOverTime()) {
                    String messageText;
                    if (isChangingOverTime()) {
                        messageText = Messages.ProductCmptTypeAssociation_Msg_DeriveUnionChangingOverTimeMismatch_SubetChanging;
                    } else {
                        messageText = Messages.ProductCmptTypeAssociation_Msg_DeriveUnionChangingOverTimeMismatch_SubetStatic;
                    }
                    String boundMessageText = NLS.bind(messageText, derivedUnionAssociation.getName());
                    Message message = new Message(
                            IProductCmptTypeAssociation.MSGCODE_DERIVED_UNION_CHANGING_OVER_TIME_MISMATCH,
                            boundMessageText, Message.ERROR, new ObjectProperty(this,
                                    IProductCmptTypeAssociation.PROPERTY_CHANGING_OVER_TIME));
                    list.add(message);
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private void validateMatchingAsoociation(MessageList list, IIpsProject ipsProject) throws CoreException {
        IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociation = findMatchingPolicyCmptTypeAssociation(ipsProject);
        if (matchingPolicyCmptTypeAssociation == null) {
            if (isMatchingAssociationSourceAndNameNotEmpty()) {
                list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_NOT_FOUND, NLS.bind(
                        Messages.ProductCmptTypeAssociation_error_matchingAssociationNotFound,
                        getMatchingAssociationName(), getMatchingAssociationSource()), Message.ERROR, this,
                        PROPERTY_MATCHING_ASSOCIATION_NAME, PROPERTY_MATCHING_ASSOCIATION_SOURCE));
            }
            return;
        }
        if (!this.equals(matchingPolicyCmptTypeAssociation.findMatchingProductCmptTypeAssociation(ipsProject))) {
            list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_INVALID, NLS.bind(
                    Messages.ProductCmptTypeAssociation_error_MatchingAssociationInvalid, getMatchingAssociationName(),
                    getMatchingAssociationSource()), Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_NAME,
                    PROPERTY_MATCHING_ASSOCIATION_SOURCE));
            return;
        }
        Set<IPolicyCmptTypeAssociation> possibleMatchingPolicyCmptTypeAssociations = findPossiblyMatchingPolicyCmptTypeAssociations(ipsProject);
        if (!possibleMatchingPolicyCmptTypeAssociations.contains(matchingPolicyCmptTypeAssociation)) {
            list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_INVALID, NLS.bind(
                    Messages.ProductCmptTypeAssociation_error_MatchingAssociationDoesNotReferenceThis,
                    getMatchingAssociationName(), getMatchingAssociationSource()), Message.ERROR, this,
                    PROPERTY_MATCHING_ASSOCIATION_NAME, PROPERTY_MATCHING_ASSOCIATION_SOURCE));
            return;
        }

        /*
         * No other association should configure the same association because we would generate
         * duplicated methods
         */
        List<IAssociation> allAssociations = getProductCmptType().findAllAssociations(ipsProject);
        for (IAssociation otherAssociation : allAssociations) {
            if (this.equals(otherAssociation)) {
                continue;
            }
            IPolicyCmptTypeAssociation otherMatchingAssociation = ((IProductCmptTypeAssociation)otherAssociation)
                    .findMatchingPolicyCmptTypeAssociation(ipsProject);
            if (otherMatchingAssociation == null) {
                continue;
            }
            if (otherMatchingAssociation.equals(matchingPolicyCmptTypeAssociation)) {
                list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_DUPLICATE_NAME, NLS.bind(
                        Messages.ProductCmptTypeAssociation_error_MatchingAssociationDuplicateName, otherAssociation,
                        getMatchingAssociationSource()), Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_NAME,
                        PROPERTY_MATCHING_ASSOCIATION_SOURCE));
            }
        }
    }

    private boolean isMatchingAssociationSourceAndNameNotEmpty() {
        return StringUtils.isNotEmpty(matchingAssociationSource) && StringUtils.isNotEmpty(matchingAssociationName);
    }

    private void validateConstrainedChangeOverTime(MessageList list, IIpsProject ipsProject) {
        if (isConstrain()) {
            IProductCmptTypeAssociation constrainedAssociation = findConstrainedAssociation(ipsProject);
            if (constrainedAssociation != null && isChangingOverTime() != constrainedAssociation.isChangingOverTime()) {
                list.newError(MSGCODE_CONSTRAINED_CHANGEOVERTIME_MISMATCH,
                        Messages.ProductCmptTypeAssociation_errorMsg_constrained_changeOverTime_missmatch,
                        new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this,
                                PROPERTY_CHANGING_OVER_TIME));
            }
        }
    }

    /**
     * Validates if the changing over time flag of the {@link IProductCmptTypeAssociation} can be
     * enabled. This is only possible, if the changing over time flag of its related
     * {@link IProductCmptType} is enabled. If the flag of the type is disabled and the flag of the
     * property is enabled, a new error message will be generated.
     * 
     * @param messageList The {@link MessageList} that holds the validation messages including the
     *            possibly new validation message
     */
    private void validateTypeDoesNotAcceptChangingOverTime(MessageList messageList) {
        if (!StringUtils.isEmpty(getName())) {
            if (!getProductCmptType().isChangingOverTime() && isChangingOverTime()) {
                String changingOverTimePluralName = IpsPlugin.getDefault().getIpsPreferences()
                        .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural();
                String text = NLS.bind(Messages.ProductCmptPropertyValidator_msgTypeDoesNotAcceptChangingOverTime,
                        getName(), changingOverTimePluralName);
                messageList.add(Message.newError(MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME, text, this,
                        PROPERTY_CHANGING_OVER_TIME));
            }
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        matchingAssociationSource = element.getAttribute(PROPERTY_MATCHING_ASSOCIATION_SOURCE);
        matchingAssociationName = element.getAttribute(PROPERTY_MATCHING_ASSOCIATION_NAME);
        // use default value in case null
        initPropertyChangingOverTime(element);
        initPropertyVisible(element);
    }

    private void initPropertyChangingOverTime(Element element) {
        if (element.hasAttribute(PROPERTY_CHANGING_OVER_TIME)) {
            changingOverTime = Boolean.parseBoolean(element.getAttribute(PROPERTY_CHANGING_OVER_TIME));
        }
    }

    private void initPropertyVisible(Element element) {
        if (element.hasAttribute(PROPERTY_RELEVANT)) {
            relevant = Boolean.parseBoolean(element.getAttribute(PROPERTY_RELEVANT));
        }
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_MATCHING_ASSOCIATION_SOURCE, matchingAssociationSource);
        newElement.setAttribute(PROPERTY_MATCHING_ASSOCIATION_NAME, matchingAssociationName);
        newElement.setAttribute(PROPERTY_CHANGING_OVER_TIME, Boolean.toString(changingOverTime));
        newElement.setAttribute(PROPERTY_RELEVANT, Boolean.toString(relevant));
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    @Override
    public void setChangingOverTime(boolean changingOverTime) {
        boolean oldValue = this.changingOverTime;
        this.changingOverTime = changingOverTime;
        valueChanged(oldValue, this.changingOverTime);
    }

    @Override
    public boolean isRelevant() {
        return relevant;
    }

    @Override
    public void setRelevant(boolean relevant) {
        boolean oldValue = this.relevant;
        this.relevant = relevant;
        valueChanged(oldValue, this.relevant);
    }

    @Override
    public IAssociation findMatchingAssociation() throws CoreException {
        return findMatchingPolicyCmptTypeAssociation(getIpsProject());
    }

}
