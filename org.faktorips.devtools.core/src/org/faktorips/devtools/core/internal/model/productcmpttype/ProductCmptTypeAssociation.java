/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptTypeAssociation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAssociation extends Association implements IProductCmptTypeAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    private String matchingAssociationSource = StringUtils.EMPTY;

    private String matchingAssociationName = StringUtils.EMPTY;

    public ProductCmptTypeAssociation(IProductCmptType parent, String id) {
        super(parent, id);
        type = AssociationType.AGGREGATION;
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
    boolean collectPossibleMatchingAssociations(IPolicyCmptType sourcePolicyCmptType,
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
            if (actualAssociationTarget != null && actualAssociationTarget.isConfigurableByProductCmptType()) {
                // the actualAssociationTarget seems to be configured by another ProductCmptType
                continue;
            }
            IPolicyCmptType nextSource = policyCmptTypeAssociation.findTargetPolicyCmptType(ipsProject);
            boolean notVisitedYet = alreadyVisit.add(nextSource);
            if (notVisitedYet
                    && nextSource != null
                    && collectPossibleMatchingAssociations(nextSource, targetQName, foundAssociations, ipsProject,
                            alreadyVisit)) {
                if (!foundAssociations.add(policyCmptTypeAssociation)) {
                    // already visited this component -- return to avoid cycles
                    return true;
                }
                result = true;
            }
        }
        return result;
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

    List<IPolicyCmptTypeAssociation> findMatchingPolicyCmptTypAssociationInternal(IProductCmptType productCmptType,
            IPolicyCmptType policyCmptType,
            IIpsProject ipsProject,
            boolean stopAfterFirst) throws CoreException {
        List<IPolicyCmptTypeAssociation> result = new ArrayList<IPolicyCmptTypeAssociation>();
        for (IAssociation association : policyCmptType.getAssociations()) {
            if (association.getAssociationType().isCompositionDetailToMaster()) {
                continue;
            }
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)association;
            if (productCmptType.getQualifiedName().equals(policyCmptTypeAssociation.getMatchingAssociationSource())
                    && getName().equals(policyCmptTypeAssociation.getMatchingAssociationName())) {
                result.add(policyCmptTypeAssociation);
                if (stopAfterFirst) {
                    return result;
                }
            }
        }

        for (IAssociation association : policyCmptType.getAssociations()) {
            if (!association.getAssociationType().isCompositionMasterToDetail()) {
                continue;
            }
            IPolicyCmptType target = (IPolicyCmptType)association.findTarget(ipsProject);
            if (target == null || target.isConfigurableByProductCmptType()) {
                continue;
            }
            List<IPolicyCmptTypeAssociation> matching = findMatchingPolicyCmptTypAssociationInternal(productCmptType,
                    target, ipsProject, stopAfterFirst);
            result.addAll(matching);
            if (stopAfterFirst && result.size() > 0) {
                return result;
            }
        }
        return result;
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
    }

    private void validateMatchingAsoociation(MessageList list, IIpsProject ipsProject) throws CoreException {
        IPolicyCmptTypeAssociation matchingPolicyCmptTypeAssociation = findMatchingPolicyCmptTypeAssociation(ipsProject);
        if (matchingPolicyCmptTypeAssociation == null) {
            if (StringUtils.isNotEmpty(matchingAssociationSource) && StringUtils.isNotEmpty(matchingAssociationName)) {
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
         * No other association should configure an association with the same name because we would
         * generate duplicated methods
         */
        List<IAssociation> allAssociations = getProductCmptType().findAllAssociations(ipsProject);
        for (IAssociation otherAssociation : allAssociations) {
            if (otherAssociation.equals(this)) {
                continue;
            }
            IPolicyCmptTypeAssociation otherMatchingAssociation = ((IProductCmptTypeAssociation)otherAssociation)
                    .findMatchingPolicyCmptTypeAssociation(ipsProject);
            if (otherMatchingAssociation == null) {
                continue;
            }
            if (otherMatchingAssociation.getName().equals(matchingPolicyCmptTypeAssociation.getName())) {
                list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_DUPLICATE_NAME, NLS.bind(
                        Messages.ProductCmptTypeAssociation_error_MatchingAssociationDuplicateName, otherAssociation,
                        getMatchingAssociationSource()), Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_NAME,
                        PROPERTY_MATCHING_ASSOCIATION_SOURCE));
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
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_MATCHING_ASSOCIATION_SOURCE, matchingAssociationSource);
        newElement.setAttribute(PROPERTY_MATCHING_ASSOCIATION_NAME, matchingAssociationName);
    }
}
