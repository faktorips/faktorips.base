/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.pctype.persistence.PersistentAssociationInfo;
import org.faktorips.devtools.model.internal.type.Association;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PolicyCmptTypeAssociation extends Association implements IPolicyCmptTypeAssociation {

    private boolean qualified = false;

    private boolean configurable = true;

    private String inverseAssociation = ""; //$NON-NLS-1$

    /**
     * When the optional constraint
     * {@link IIpsProjectProperties#isSharedDetailToMasterAssociations()} is enabled, a
     * detail-to-master association could be marked as shared association. That means the
     * {@link #getPolicyCmptType()} does not know exactly its parent model object class. Hence the
     * {@link #inverseAssociation} of this association is empty. The shared association must have an
     * association with the same name, target and type in super type which is called the shared
     * association host.
     * <p>
     * Also read the discussion of FIPS-85.
     */
    private boolean sharedAssociation;

    private String matchingAssociationSource = StringUtils.EMPTY;

    private String matchingAssociationName = StringUtils.EMPTY;

    private IPersistentAssociationInfo persistenceAssociationInfo;

    public PolicyCmptTypeAssociation(IPolicyCmptType pcType, String id) {
        super(pcType, id);
        if (pcType.getIpsProject().isPersistenceSupportEnabled()) {
            persistenceAssociationInfo = newPart(PersistentAssociationInfo.class);
        }
    }

    @Override
    public IPolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }

    @Override
    public boolean isConstrain() {
        AssociationType associationType = getAssociationType();
        if (associationType.isCompositionDetailToMaster()) {
            return isConstrainedInverse();
        }
        return super.isConstrain();
    }

    private boolean isConstrainedInverse() {
        IPolicyCmptTypeAssociation foundInverseAssociation = findInverseAssociation(getIpsProject());
        // Need to check master to detail to avoid stack overflow
        if (foundInverseAssociation != null && foundInverseAssociation.isCompositionMasterToDetail()) {
            return foundInverseAssociation.isConstrain();
        } else {
            return false;
        }
    }

    @Override
    public boolean isComposition() {
        return getAssociationType().isCompositionDetailToMaster() || getAssociationType().isCompositionMasterToDetail();
    }

    @Override
    public boolean isCompositionMasterToDetail() {
        return getAssociationType().isCompositionMasterToDetail();
    }

    @Override
    public boolean isCompositionDetailToMaster() {
        return getAssociationType().isCompositionDetailToMaster();
    }

    @Override
    public boolean isDerivedUnionApplicable() {
        return (isAssoziation() || isCompositionMasterToDetail()) && !isConstrain();
    }

    @Override
    public boolean isInverseOfDerivedUnion() {
        if (!isCompositionDetailToMaster()) {
            return false;
        }
        IPolicyCmptTypeAssociation masterToDetail = findInverseAssociation(getIpsProject());
        if (masterToDetail == null) {
            // no master association found, will be a validation error
            return false;
        }
        return masterToDetail.isDerivedUnion();
    }

    @Override
    public void setAssociationType(AssociationType newType) {
        if (newType.isCompositionDetailToMaster()) {
            setSubsettedDerivedUnion(StringUtils.EMPTY);
            setDerivedUnionInternal(false);
            qualified = false;
            setMinCardinalityInternal(0);
            setMaxCardinalityInternal(1);
        }
        super.setAssociationType(newType);
    }

    @Override
    public IPolicyCmptType findTargetPolicyCmptType(IIpsProject ipsProject) {
        return ipsProject.findPolicyCmptType(getTarget());
    }

    @Override
    public boolean isConstrainedByProductStructure(IIpsProject ipsProject) {
        return findMatchingProductCmptTypeAssociation(ipsProject) != null;
    }

    @Override
    public IProductCmptTypeAssociation findMatchingProductCmptTypeAssociation(IIpsProject ipsProject) {
        if (StringUtils.isEmpty(matchingAssociationName) || StringUtils.isEmpty(getMatchingAssociationSource())) {
            return findDefaultMatchingProductCmptTypeAssociation(ipsProject);
        }
        IProductCmptType productCmptType = ipsProject.findProductCmptType(getMatchingAssociationSource());
        if (productCmptType == null) {
            return null;
        }
        IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)productCmptType
                .findAssociation(matchingAssociationName, ipsProject);
        return association;
    }

    @Override
    public IProductCmptTypeAssociation findDefaultMatchingProductCmptTypeAssociation(IIpsProject ipsProject) {
        if (getAssociationType().isCompositionDetailToMaster()) {
            return null;
        }
        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType == null) {
            return null;
        }
        List<IAssociation> candidates = productCmptType.getAssociationsForTarget(targetType.getProductCmptType());
        int index = getAssociationIndex();
        if (index >= candidates.size()) {
            return null;
        }
        return (IProductCmptTypeAssociation)candidates.get(index);
    }

    private int getAssociationIndex() {
        List<IAssociation> allAssociationsForTheTargetType = new ArrayList<>();
        List<IPolicyCmptTypeAssociation> ass = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation as : ass) {
            if (getTarget().equals(as.getTarget())) {
                allAssociationsForTheTargetType.add(as);
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
    public boolean isQualified() {
        return qualified;
    }

    @Override
    public void setQualified(boolean newValue) {
        boolean oldValue = qualified;
        qualified = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public boolean isQualificationPossible(IIpsProject ipsProject) {
        if (!isCompositionMasterToDetail()) {
            return false;
        }
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType == null || !targetType.isConfigurableByProductCmptType()) {
            return false;
        }
        return true;
    }

    @Override
    public String findQualifierCandidate(IIpsProject ipsProject) {
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType == null || !targetType.isConfigurableByProductCmptType()) {
            return ""; //$NON-NLS-1$
        }
        return targetType.getProductCmptType();
    }

    @Override
    public IProductCmptType findQualifier(IIpsProject ipsProject) {
        if (!qualified) {
            return null;
        }
        return ipsProject.findProductCmptType(findQualifierCandidate(ipsProject));
    }

    @Override
    public String getInverseAssociation() {
        return inverseAssociation;
    }

    @Override
    public boolean hasInverseAssociation() {
        return StringUtils.isNotEmpty(inverseAssociation);
    }

    @Override
    public void setInverseAssociation(String newRelation) {
        String oldValue = inverseAssociation;
        inverseAssociation = newRelation;
        valueChanged(oldValue, newRelation);
    }

    /**
     * {@inheritDoc}
     * <p>
     * For shared associations this method always return null because shared associations do not
     * have an inverse association. Instead you have to find the inverse association of the shared
     * association host {@link #findSharedAssociationHost(IIpsProject)}
     */
    @Override
    public IPolicyCmptTypeAssociation findInverseAssociation(IIpsProject ipsProject) {
        if (isSharedAssociation()) {
            return null;
        }
        String searchedAssociation = inverseAssociation;
        IPolicyCmptType target = findTargetPolicyCmptType(ipsProject);
        if (target == null) {
            return null;
        }
        List<IAssociation> associations = target.getAssociations();
        if (StringUtils.isEmpty(searchedAssociation)) {
            return null;
        }
        for (IAssociation association : associations) {
            if (association.getName().equals(searchedAssociation)) {
                return (IPolicyCmptTypeAssociation)association;
            }
        }
        return null;
    }

    @Override
    public IPolicyCmptTypeAssociation newInverseAssociation() {
        IPolicyCmptType targetPolicyCmptType = findTargetPolicyCmptType(getIpsProject());
        if (targetPolicyCmptType == null) {
            throw new IpsException(new IpsStatus("Target policy component type of association " + getName() //$NON-NLS-1$
                    + " not found.")); //$NON-NLS-1$
        }

        IPolicyCmptTypeAssociation newInverseAssociation = targetPolicyCmptType.newPolicyCmptTypeAssociation();
        newInverseAssociation.setTarget(getPolicyCmptType().getQualifiedName());
        newInverseAssociation.setAssociationType(getAssociationType().getCorrespondingAssociationType());

        // we must set the default role name to ensure that both sides are linked together using
        // their names
        newInverseAssociation.setTargetRoleSingular(newInverseAssociation.getDefaultTargetRoleSingular());
        setInverseAssociation(newInverseAssociation.getName());
        newInverseAssociation.setInverseAssociation(getName());

        IPolicyCmptTypeAssociation derivedUnionAssociation = (IPolicyCmptTypeAssociation)findSubsettedDerivedUnion(
                getIpsProject());
        if (isAssoziation() && derivedUnionAssociation != null) {
            newInverseAssociation.setSubsettedDerivedUnion(derivedUnionAssociation.getInverseAssociation());
        }

        if (isAssoziation() && isDerivedUnion()) {
            newInverseAssociation.setDerivedUnion(true);
        }

        return newInverseAssociation;
    }

    @Override
    public void setSharedAssociation(boolean sharedAssociation) {
        boolean oldValue = this.sharedAssociation;
        this.sharedAssociation = sharedAssociation;
        valueChanged(oldValue, sharedAssociation);
    }

    @Override
    public boolean isSharedAssociation() {
        // shared associations are only allowed for detail-to-master associations when the
        // corresponding optional constraint is activated in the project
        return getAssociationType().isCompositionDetailToMaster()
                && getIpsProject().getReadOnlyProperties().isSharedDetailToMasterAssociations() && sharedAssociation;
    }

    @Override
    public IPolicyCmptTypeAssociation findSharedAssociationHost(IIpsProject ipsProject) {
        AssociationHierarchyVisitor visitor = new AssociationHierarchyVisitor(ipsProject) {

            @Override
            protected boolean continueVisiting() {
                return ((IPolicyCmptTypeAssociation)getLastVisited()).isSharedAssociation();
            }

        };
        visitor.start(this);
        IPolicyCmptTypeAssociation associationHostCandidate = (IPolicyCmptTypeAssociation)visitor.getSuperAssociation();
        if (associationHostCandidate != null && associationHostCandidate.getTarget().equals(getTarget())
                && associationHostCandidate.getAssociationType().isCompositionDetailToMaster()) {
            return associationHostCandidate;
        } else {
            return null;
        }
    }

    @Override
    public void setMatchingAssociationSource(String matchingAssociationSource) {
        String oldSource = this.matchingAssociationSource;
        this.matchingAssociationSource = matchingAssociationSource;
        valueChanged(oldSource, matchingAssociationSource);
    }

    @Override
    public String getMatchingAssociationSource() {
        if (getPolicyCmptType().isConfigurableByProductCmptType()) {
            return getPolicyCmptType().getProductCmptType();
        } else {
            return matchingAssociationSource;
        }
    }

    @Override
    public void setMatchingAssociationName(String matchingAssociationName) {
        String oldName = this.matchingAssociationName;
        this.matchingAssociationName = matchingAssociationName;
        valueChanged(oldName, this.matchingAssociationName);
    }

    @Override
    public String getMatchingAssociationName() {
        return matchingAssociationName;
    }

    /**
     * @param configurable The configured to set.
     */
    @Override
    public void setConfigurable(boolean configurable) {
        boolean oldValue = this.configurable;
        this.configurable = configurable;
        valueChanged(oldValue, configurable);
    }

    /**
     * @return Returns the configured.
     */
    @Override
    public boolean isConfigurable() {
        return configurable;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        // detail to master must have maxCardinality = 1
        if (getMaxCardinality() != 1 && getAssociationType() == AssociationType.COMPOSITION_DETAIL_TO_MASTER) {
            String text = Messages.Association_msg_DetailToMasterAssociationMustHaveMaxCardinality1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION, text, Message.ERROR, this,
                    PROPERTY_MAX_CARDINALITY, IAssociation.PROPERTY_ASSOCIATION_TYPE));
        }
        validateDerivedUnion(list, ipsProject);
        validateInverseRelation(list, ipsProject);
        validateMatchingAssociation(list, ipsProject);
        validateConstrainedAssociation(list, ipsProject);
    }

    private void validateDerivedUnion(MessageList list, IIpsProject ipsProject) {
        IAssociation unionAss = findSubsettedDerivedUnion(ipsProject);
        if (unionAss instanceof IPolicyCmptTypeAssociation) {
            checkForDerivedUnionInverseAssociationMismatch((IPolicyCmptTypeAssociation)unionAss, list, ipsProject);
        }
    }

    /**
     * Performs the check for the rule with message code
     * 
     * @see IPolicyCmptTypeAssociation#MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION
     */
    private void checkForDerivedUnionInverseAssociationMismatch(IPolicyCmptTypeAssociation derivedUnion,
            MessageList list,
            IIpsProject ipsProject) {

        IPolicyCmptTypeAssociation inverseAss = findInverseAssociation(ipsProject);
        if (inverseAss == null) {
            // not found => error will be reported in validateInverseRelation
            return;
        }
        if (isComposition() || inverseAss.isComposition()) {
            return;
        }
        IPolicyCmptTypeAssociation inverseRelationOfContainerRel = derivedUnion.findInverseAssociation(ipsProject);
        if (inverseRelationOfContainerRel == null) {
            // not found => error will be reported in validateReverseRelation
            return;
        }
        IAssociation derivedUnionOfInverseRel = inverseAss.findSubsettedDerivedUnion(ipsProject);
        if (derivedUnionOfInverseRel == null || derivedUnionOfInverseRel != inverseRelationOfContainerRel) {
            String text = MessageFormat.format(Messages.Association_msg_InverseAssociationInconsistentWithDerivedUnion,
                    derivedUnion);
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION, text, Message.ERROR, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
        }
    }

    private void validateInverseRelation(MessageList list, IIpsProject ipsProject) {
        if (!validateInverseCompositionDetailToMaster(list)) {
            return;
        }

        if (!validateEmptyInverseAssociation(list, ipsProject)) {
            return;
        }

        // inverse association must exists
        IPolicyCmptTypeAssociation inverseAss = findInverseAssociation(ipsProject);
        if (inverseAss == null) {
            String text = MessageFormat.format(Messages.Association_msg_AssociationNotFoundInTarget, inverseAssociation,
                    getTarget());
            list.add(new Message(MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET, text, Message.ERROR, this,
                    PROPERTY_INVERSE_ASSOCIATION));
            return;
        }

        // target of inverse must be this
        if (!checkInverseAssociation(getIpsProject(), this, inverseAss)) {
            String text = Messages.Association_msg_InverseAssociationMismatch;
            list.add(new Message(MSGCODE_INVERSE_RELATION_MISMATCH, text, Message.ERROR, this,
                    PROPERTY_INVERSE_ASSOCIATION));
        }

        // check correct type combinations:
        // a) association : association
        if (!validateInverseAssociationToAssociation(list, inverseAss)) {
            return;
        }

        // b) master to detail : detail to master
        validateInverseMasterToDetailAndDetailToMaster(list, inverseAss);

        // c) derived union association : not derived union
        // if this is an association marked derived union then the inverse association must also be
        // marked as derived union
        if (getAssociationType().isAssoziation() && isDerivedUnion() != inverseAss.isDerivedUnion()) {
            String text = Messages.Association_msg_InverseAssociationMustBeMarkedAsDerivedUnionToo;
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER, text, Message.ERROR,
                    this, PROPERTY_INVERSE_ASSOCIATION));
        }
    }

    private boolean validateEmptyInverseAssociation(MessageList list, IIpsProject ipsProject) {
        if (StringUtils.isEmpty(inverseAssociation)) {
            // special check in case of subsetted derived union the inverse must be set if the
            // derived union has specified an inverse association
            if (isSubsetOfADerivedUnion()) {
                IPolicyCmptTypeAssociation subsettedDerivedUnion = (IPolicyCmptTypeAssociation)findSubsettedDerivedUnion(
                        ipsProject);
                if (subsettedDerivedUnion == null) {
                    // different validation error
                    return false;
                }
                if (StringUtils.isNotEmpty(subsettedDerivedUnion.getInverseAssociation())) {
                    String text = Messages.PolicyCmptTypeAssociation_Association_msg_InverseAssociationMustNotBeEmptyIfDerivedUnionHasInverse;
                    list.add(new Message(
                            MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS,
                            text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION));
                }
            }
            return false;
        }
        return true;
    }

    private void validateInverseMasterToDetailAndDetailToMaster(MessageList list,
            IPolicyCmptTypeAssociation inverseAss) {
        if (!getAssociationType().isAssoziation() && !(isAssociationTypeMToDAndInverAssociationDToM(inverseAss)
                || isAssociationTypeDToMAndInverAssociationMToD(inverseAss))) {
            String text;
            String code;
            if (getAssociationType().isCompositionMasterToDetail()) {
                text = Messages.PolicyCmptTypeAssociation_Association_msg_InverseOfMasterToDetailMustBeADetailToMaster;
                code = MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH;
            } else {
                text = Messages.PolicyCmptTypeAssociation_InverseOfDetailToMasterMustBeAMasterToDetail;
                code = MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH;
            }
            list.add(new Message(code, text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION));
        }
    }

    private boolean isAssociationTypeMToDAndInverAssociationDToM(IPolicyCmptTypeAssociation inverseAss) {
        return getAssociationType().isCompositionMasterToDetail() && inverseAss.isCompositionDetailToMaster();
    }

    private boolean isAssociationTypeDToMAndInverAssociationMToD(IPolicyCmptTypeAssociation inverseAss) {
        return getAssociationType().isCompositionDetailToMaster() && inverseAss.isCompositionMasterToDetail();
    }

    private boolean validateInverseAssociationToAssociation(MessageList list, IPolicyCmptTypeAssociation inverseAss) {
        if (!((inverseAss.isAssoziation() && getAssociationType().isAssoziation())
                || (!inverseAss.isAssoziation() && !getAssociationType().isAssoziation()))) {
            String text = Messages.Association_msg_InverseAssociationMustBeOfTypeAssociation;
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH, text, Message.ERROR, this,
                    PROPERTY_INVERSE_ASSOCIATION));
            return false;
        }
        return true;
    }

    private boolean validateInverseCompositionDetailToMaster(MessageList list) {
        if (getAssociationType().isCompositionDetailToMaster()) {
            String text = Messages.PolicyCmptTypeAssociation_Association_msg_InverseAssociationMustNotBeEmpty;
            Message errorMessage = Message.newError(MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER,
                    text, this, PROPERTY_INVERSE_ASSOCIATION);
            if (isSharedAssociation()) {
                // for shared associations it is valid to specify no inverse association if there is
                // an inverse association of a derived union, with the same name in superclass
                IPolicyCmptTypeAssociation superAssociation = findSharedAssociationHost(getIpsProject());
                if (superAssociation == null) {
                    list.add(Message.newError(MSGCODE_SHARED_ASSOCIATION_INVALID,
                            Messages.PolicyCmptTypeAssociation_sharedAssociation_noAssociationHost, this,
                            PROPERTY_SHARED_ASSOCIATION));
                    return false;
                }
                if (!superAssociation.isInverseOfDerivedUnion()) {
                    list.add(Message.newError(MSGCODE_SHARED_ASSOCIATION_INVALID,
                            Messages.PolicyCmptTypeAssociation_sharedAssociation_invalidAssociationHost, this,
                            PROPERTY_SHARED_ASSOCIATION));
                    return false;
                }
                // FIPS-85: shared associations does not have a inverse association so we do not
                // have to validate further
                return false;
            } else if (StringUtils.isEmpty(inverseAssociation)) {
                // inverse must always be set if type is detail to master (expect for shared
                // inverse associations)
                list.add(errorMessage);
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the given inverse association is valid for the given association according to
     * the project settings
     * 
     */
    private boolean checkInverseAssociation(IIpsProject ipsProject,
            IPolicyCmptTypeAssociation association,
            IPolicyCmptTypeAssociation inverseAss) {
        if (inverseAss.getInverseAssociation().equals(association.getName())
                && inverseAss.getTarget().equals(association.getType().getQualifiedName())) {
            return true;
        }
        // FIPS-85: For shared associations we have to check the inverse association of the shared
        // association host
        if (inverseAss.isSharedAssociation()) {
            IPolicyCmptTypeAssociation sharedAssociationHost = inverseAss
                    .findSharedAssociationHost(inverseAss.getIpsProject());
            if (sharedAssociationHost == null) {
                return false;
            }
            // when shared associations is activated also a derived union of this one is
            // allowed as inverse association
            IPolicyCmptTypeAssociation subsettedDerivedUnion = (IPolicyCmptTypeAssociation)association
                    .findSubsettedDerivedUnion(ipsProject);
            if (subsettedDerivedUnion == null) {
                return false;
            }
            return checkInverseAssociation(ipsProject, subsettedDerivedUnion, sharedAssociationHost);
        }
        return false;
    }

    private void validateMatchingAssociation(MessageList list, IIpsProject ipsProject) {
        if (!getPolicyCmptType().isConfigurableByProductCmptType()) {
            if (StringUtils.isNotEmpty(getMatchingAssociationSource())) {
                IProductCmptType matchingProdCmptType = ipsProject.findProductCmptType(getMatchingAssociationSource());
                if (matchingProdCmptType == null) {
                    list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_INVALID_SOURCE,
                            Messages.PolicyCmptTypeAssociation_error_MatchingAssociationInvalidSourceForConfiguredType,
                            Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_SOURCE));
                    return;
                } else {
                    IPolicyCmptType matchingPolicyCmptType = matchingProdCmptType.findPolicyCmptType(ipsProject);
                    boolean found = findCorrectMatchingPolicyCmptTypeRecoursive(matchingPolicyCmptType, ipsProject,
                            new HashSet<IPolicyCmptType>());
                    if (!found) {
                        list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_INVALID_SOURCE,
                                Messages.PolicyCmptTypeAssociation_error_MatchingAssociationInvalidSourceForNotConfiguredType,
                                Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_SOURCE));
                        return;
                    }
                }
            }
        }
        IProductCmptTypeAssociation matchingAssociation = findMatchingProductCmptTypeAssociation(ipsProject);
        if (StringUtils.isNotEmpty(matchingAssociationSource) && StringUtils.isNotEmpty(matchingAssociationName)
                && matchingAssociation == null) {
            list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_NOT_FOUND,
                    MessageFormat.format(Messages.PolicyCmptTypeAssociation_error_matchingAssociatonNotFound,
                            getMatchingAssociationName(), getMatchingAssociationSource()),
                    Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_NAME, PROPERTY_MATCHING_ASSOCIATION_SOURCE));
        } else {
            if (matchingAssociation != null
                    && !this.equals(matchingAssociation.findMatchingPolicyCmptTypeAssociation(ipsProject))) {
                list.add(new Message(MSGCODE_MATCHING_ASSOCIATION_INVALID,
                        MessageFormat.format(Messages.PolicyCmptTypeAssociation_error_MatchingAssociationInvalid,
                                getMatchingAssociationName(), getMatchingAssociationSource()),
                        Message.ERROR, this, PROPERTY_MATCHING_ASSOCIATION_NAME, PROPERTY_MATCHING_ASSOCIATION_SOURCE));
            }
        }
    }

    private void validateConstrainedAssociation(MessageList list, IIpsProject ipsProject) {
        if (isConstrain()) {
            IAssociation constrainedAssociation = findConstrainedAssociation(ipsProject);
            if (constrainedAssociation != null && isQualified() != constrainedAssociation.isQualified()) {
                list.newError(MSGCODE_CONSTRAINED_QUALIFIER_MISMATCH,
                        Messages.PolicyCmptTypeAssociation_errorMsg_constrainedPropertyQualifiedMismatch,
                        new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_QUALIFIED));
            }
        }
    }

    boolean findCorrectMatchingPolicyCmptTypeRecoursive(IPolicyCmptType parentPolicyCmptType,
            IIpsProject ipsProject,
            Set<IPolicyCmptType> visited) {
        if (parentPolicyCmptType == null) {
            return false;
        }
        if (!visited.add(parentPolicyCmptType)) {
            return false;
        }
        // breath first search
        for (IPolicyCmptTypeAssociation association : parentPolicyCmptType.getPolicyCmptTypeAssociations()) {
            if (association.isCompositionMasterToDetail()) {
                if (getPolicyCmptType().getQualifiedName().equals(association.getTarget())) {
                    return true;
                }
            }
        }
        for (IPolicyCmptTypeAssociation association : parentPolicyCmptType.getPolicyCmptTypeAssociations()) {
            IPolicyCmptType nextParent = (IPolicyCmptType)association.findTarget(ipsProject);
            if (findCorrectMatchingPolicyCmptTypeRecoursive(nextParent, ipsProject, visited)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        qualified = ValueToXmlHelper.isAttributeTrue(element, PROPERTY_QUALIFIED);
        inverseAssociation = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_INVERSE_ASSOCIATION);
        sharedAssociation = Boolean.parseBoolean(element.getAttribute(PROPERTY_SHARED_ASSOCIATION));
        matchingAssociationName = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_MATCHING_ASSOCIATION_NAME);
        matchingAssociationSource = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_MATCHING_ASSOCIATION_SOURCE);
        initConfigurable(element);
    }

    private void initConfigurable(Element element) {
        if (element.hasAttribute(PROPERTY_CONFIGURABLE)) {
            String attribute = element.getAttribute(PROPERTY_CONFIGURABLE);
            configurable = Boolean.parseBoolean(attribute);
        }
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_QUALIFIED, "" + qualified); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(inverseAssociation)) {
            newElement.setAttribute(PROPERTY_INVERSE_ASSOCIATION, inverseAssociation);
        }
        newElement.setAttribute(PROPERTY_SHARED_ASSOCIATION, Boolean.toString(sharedAssociation));
        if (StringUtils.isNotEmpty(matchingAssociationName)) {
            newElement.setAttribute(PROPERTY_MATCHING_ASSOCIATION_NAME, matchingAssociationName);
        }
        if (StringUtils.isNotEmpty(matchingAssociationSource)) {
            newElement.setAttribute(PROPERTY_MATCHING_ASSOCIATION_SOURCE, getMatchingAssociationSource());
        }
        newElement.setAttribute(PROPERTY_CONFIGURABLE, Boolean.toString(isConfigurable()));
    }

    @Override
    public IPersistentAssociationInfo getPersistenceAssociatonInfo() {
        return persistenceAssociationInfo;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IPersistentAssociationInfo) {
            persistenceAssociationInfo = (IPersistentAssociationInfo)part;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (xmlTag.getTagName().equals(IPersistentAssociationInfo.XML_TAG)) {
            persistenceAssociationInfo = new PersistentAssociationInfo(this, id);
            return persistenceAssociationInfo;
        }
        return null;
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType.isAssignableFrom(PersistentAssociationInfo.class)) {
            return new PersistentAssociationInfo(this, getNextPartId());
        } else {
            return null;
        }
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        if (persistenceAssociationInfo != null) {
            return new IIpsElement[] { persistenceAssociationInfo };
        }
        return new IIpsElement[0];
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    public IAssociation findMatchingAssociation() {
        return findMatchingProductCmptTypeAssociation(getIpsProject());
    }

}
