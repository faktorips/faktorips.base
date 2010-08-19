/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.pctype;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PolicyCmptTypeAssociation extends Association implements IPolicyCmptTypeAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    private boolean qualified = false;
    private String inverseAssociation = ""; //$NON-NLS-1$
    private IIpsObjectPart persistenceAssociationInfo;

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
    public boolean isComposition() {
        return type.isCompositionDetailToMaster() || type.isCompositionMasterToDetail();
    }

    @Override
    public boolean isCompositionMasterToDetail() {
        return type.isCompositionMasterToDetail();
    }

    @Override
    public boolean isCompositionDetailToMaster() {
        return type.isCompositionDetailToMaster();
    }

    @Override
    public boolean isContainerRelationApplicable() {
        return isAssoziation() || isCompositionMasterToDetail();
    }

    @Override
    public boolean isInverseOfDerivedUnion() throws CoreException {
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
            subsettedDerivedUnion = ""; //$NON-NLS-1$
            derivedUnion = false;
            qualified = false;
            minCardinality = 0;
            maxCardinality = 1;
        }
        super.setAssociationType(newType);
    }

    @Override
    public IPolicyCmptType findTargetPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findPolicyCmptType(target);
    }

    @Override
    public boolean isConstrainedByProductStructure(IIpsProject ipsProject) throws CoreException {
        return findMatchingProductCmptTypeAssociation(ipsProject) != null;
    }

    @Override
    public IProductCmptTypeAssociation findMatchingProductCmptTypeAssociation(IIpsProject ipsProject)
            throws CoreException {
        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType == null) {
            return null;
        }
        IAssociation[] candidates = productCmptType.getAssociationsForTarget(targetType.getProductCmptType());
        int index = getAssociationIndex();
        if (index >= candidates.length) {
            return null;
        }
        return (IProductCmptTypeAssociation)candidates[index];
    }

    private int getAssociationIndex() {
        List<IAssociation> allAssociationsForTheTargetType = new ArrayList<IAssociation>();
        IPolicyCmptTypeAssociation[] ass = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation as : ass) {
            if (target.equals(as.getTarget())) {
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
    public boolean isQualificationPossible(IIpsProject ipsProject) throws CoreException {
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
    public String findQualifierCandidate(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType == null || !targetType.isConfigurableByProductCmptType()) {
            return ""; //$NON-NLS-1$
        }
        return targetType.getProductCmptType();
    }

    @Override
    public IProductCmptType findQualifier(IIpsProject ipsProject) throws CoreException {
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

    @Override
    public IPolicyCmptTypeAssociation findInverseAssociation(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(inverseAssociation)) {
            return null;
        }
        IPolicyCmptType target = findTargetPolicyCmptType(ipsProject);
        if (target == null) {
            return null;
        }
        IPolicyCmptTypeAssociation[] associations = target.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation association : associations) {
            if (association.getName().equals(inverseAssociation)) {
                return association;
            }
        }
        return null;
    }

    /**
     * Returns the target site association with inverse set to this association. The inverse of the
     * target association must be the name of this association and the target of the inverse must be
     * the policy component type this association belongs to.
     * 
     */
    // public to enable testing in the test plugin
    public IPolicyCmptTypeAssociation findTargetAssociationWithCorrespondingInverse(IIpsProject ipsProject)
            throws CoreException {

        IPolicyCmptType target = findTargetPolicyCmptType(ipsProject);
        if (target == null) {
            return null;
        }
        IPolicyCmptTypeAssociation[] associationCandidates = target.getPolicyCmptTypeAssociations();
        for (IPolicyCmptTypeAssociation associationCandidate : associationCandidates) {
            String canditateInverseAssociationName = associationCandidate.getInverseAssociation();
            String canditateTargetQName = getPolicyCmptType().getQualifiedName();
            if (getName().equals(canditateInverseAssociationName)
                    && associationCandidate.getTarget().equals(canditateTargetQName)) {
                return associationCandidate;
            }
        }
        return null;
    }

    @Override
    public IPolicyCmptTypeAssociation newInverseAssociation() throws CoreException {
        IPolicyCmptType targetPolicyCmptType = findTargetPolicyCmptType(getIpsProject());
        if (targetPolicyCmptType == null) {
            throw new CoreException(new IpsStatus("Target policy component type of association " + getName() //$NON-NLS-1$
                    + " not found.")); //$NON-NLS-1$
        }

        IPolicyCmptTypeAssociation inverseAssociation = targetPolicyCmptType.newPolicyCmptTypeAssociation();
        inverseAssociation.setTarget(getPolicyCmptType().getQualifiedName());
        inverseAssociation.setAssociationType(getAssociationType().getCorrespondingAssociationType());

        // we must set the default role name to ensure that both sides are linked together using
        // their names
        inverseAssociation.setTargetRoleSingular(inverseAssociation.getDefaultTargetRoleSingular());
        setInverseAssociation(inverseAssociation.getName());
        inverseAssociation.setInverseAssociation(getName());

        IPolicyCmptTypeAssociation derivedUnionAssociation = (IPolicyCmptTypeAssociation)findSubsettedDerivedUnion(getIpsProject());
        if (isAssoziation() && derivedUnionAssociation != null) {
            inverseAssociation.setSubsettedDerivedUnion(derivedUnionAssociation.getInverseAssociation());
        }

        if (isAssoziation() && isDerivedUnion()) {
            inverseAssociation.setDerivedUnion(true);
        }

        return inverseAssociation;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        if (maxCardinality != 1 && type == AssociationType.COMPOSITION_DETAIL_TO_MASTER) {
            String text = Messages.Association_msg_DetailToMasterAssociationMustHaveMaxCardinality1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION, text, Message.ERROR, this,
                    new String[] { PROPERTY_MAX_CARDINALITY, IAssociation.PROPERTY_ASSOCIATION_TYPE }));
        }
        validateDerivedUnion(list, ipsProject);
        validateInverseRelation(list, ipsProject);
    }

    private void validateDerivedUnion(MessageList list, IIpsProject ipsProject) throws CoreException {
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
            IIpsProject ipsProject) throws CoreException {

        IPolicyCmptTypeAssociation inverseAss = findInverseAssociation(ipsProject);
        if (inverseAss == null) {
            return; // not found => error will be reported in validateInverseRelation
        }
        if (isComposition() || inverseAss.isComposition()) {
            return;
        }
        IPolicyCmptTypeAssociation inverseRelationOfContainerRel = derivedUnion.findInverseAssociation(ipsProject);
        if (inverseRelationOfContainerRel == null) {
            return; // not found => error will be reported in validateReverseRelation
        }
        IAssociation derivedUnionOfInverseRel = inverseAss.findSubsettedDerivedUnion(ipsProject);
        if (derivedUnionOfInverseRel == null || derivedUnionOfInverseRel != inverseRelationOfContainerRel) {
            String text = NLS.bind(Messages.Association_msg_InverseAssociationInconsistentWithDerivedUnion,
                    derivedUnion);
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION, text, Message.ERROR, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
        }
    }

    private void validateInverseRelation(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (type.isCompositionDetailToMaster()) {
            if (StringUtils.isEmpty(inverseAssociation)) {
                // inverse must always be set if type is detail to master
                String text = Messages.PolicyCmptTypeAssociation_Association_msg_InverseAssociationMustNotBeEmpty;
                list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_MUST_BE_SET_IF_TYPE_IS_DETAIL_TO_MASTER, text,
                        Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION));
                return;
            }
        }

        if (StringUtils.isEmpty(inverseAssociation)) {
            // special check in case of subsetted derived union the inverse must be set if the
            // derived union has specified an inverse association
            if (isSubsetOfADerivedUnion()) {
                IPolicyCmptTypeAssociation subsettedDerivedUnion = (IPolicyCmptTypeAssociation)findSubsettedDerivedUnion(ipsProject);
                if (subsettedDerivedUnion == null) {
                    return; // different validation error
                }
                if (StringUtils.isNotEmpty(subsettedDerivedUnion.getInverseAssociation())) {
                    String text = Messages.PolicyCmptTypeAssociation_Association_msg_InverseAssociationMustNotBeEmptyIfDerivedUnionHasInverse;
                    list.add(new Message(
                            MSGCODE_SUBSETTED_DERIVED_UNION_INVERSE_MUST_BE_EXISTS_IF_INVERSE_DERIVED_UNION_EXISTS,
                            text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION));
                }
            }
            return;
        }

        // inverse association must exists
        IPolicyCmptTypeAssociation inverseAss = findInverseAssociation(ipsProject);
        if (inverseAss == null) {
            String text = NLS.bind(Messages.Association_msg_AssociationNotFoundInTarget, inverseAssociation, target);
            list.add(new Message(MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET, text, Message.ERROR, this,
                    PROPERTY_INVERSE_ASSOCIATION));
            return;
        }

        // target of inverse must be this
        if (!inverseAss.getInverseAssociation().equals(targetRoleSingular)) {
            String text = Messages.Association_msg_InverseAssociationMismatch;
            list.add(new Message(MSGCODE_INVERSE_RELATION_MISMATCH, text, Message.ERROR, this,
                    PROPERTY_INVERSE_ASSOCIATION));
        } else {
            // inverse must match both sites
            IPolicyCmptTypeAssociation targetSiteAssociation = findTargetAssociationWithCorrespondingInverse(ipsProject);
            if (targetSiteAssociation == null || !inverseAssociation.equals(targetSiteAssociation.getName())) {
                String text = Messages.Association_msg_InverseAssociationMismatch;
                list.add(new Message(MSGCODE_INVERSE_RELATION_MISMATCH, text, Message.ERROR, this,
                        PROPERTY_INVERSE_ASSOCIATION));
            }
        }

        // check correct type combinations:
        // a) association : association
        if (!((inverseAss.isAssoziation() && type.isAssoziation()) || (!inverseAss.isAssoziation() && !type
                .isAssoziation()))) {
            String text = Messages.Association_msg_InverseAssociationMustBeOfTypeAssociation;
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH, text, Message.ERROR, this,
                    new String[] { PROPERTY_INVERSE_ASSOCIATION }));
            return;
        }

        // b) master to detail : detail to master
        if (!type.isAssoziation()
                && !((type.isCompositionMasterToDetail() && inverseAss.isCompositionDetailToMaster()) || (type
                        .isCompositionDetailToMaster() && inverseAss.isCompositionMasterToDetail()))) {
            String text;
            String code;
            if (type.isCompositionMasterToDetail()) {
                text = Messages.PolicyCmptTypeAssociation_Association_msg_InverseOfMasterToDetailMustBeADetailToMaster;
                code = MSGCODE_INVERSE_MASTER_TO_DETAIL_TYPE_MISSMATCH;
            } else {
                text = Messages.PolicyCmptTypeAssociation_InverseOfDetailToMasterMustBeAMasterToDetail;
                code = MSGCODE_INVERSE_DETAIL_TO_MASTER_TYPE_MISSMATCH;
            }
            list.add(new Message(code, text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION));
        }

        // c) derived union association : not derived union
        // if this is an association marked derived union then the inverse association must also be
        // marked as derived union
        if (type.isAssoziation() && isDerivedUnion() != inverseAss.isDerivedUnion()) {
            String text = Messages.Association_msg_InverseAssociationMustBeMarkedAsDerivedUnionToo;
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER, text, Message.ERROR,
                    this, PROPERTY_INVERSE_ASSOCIATION));
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        derivedUnion = Boolean.valueOf(element.getAttribute(PROPERTY_DERIVED_UNION)).booleanValue();
        qualified = Boolean.valueOf(element.getAttribute(PROPERTY_QUALIFIED)).booleanValue();
        target = element.getAttribute(PROPERTY_TARGET);
        targetRoleSingular = element.getAttribute(PROPERTY_TARGET_ROLE_SINGULAR);
        targetRolePlural = element.getAttribute(PROPERTY_TARGET_ROLE_PLURAL);
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
        }
        String max = element.getAttribute(PROPERTY_MAX_CARDINALITY);
        if (max.equals("*")) { //$NON-NLS-1$
            maxCardinality = CARDINALITY_MANY;
        } else {
            try {
                maxCardinality = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                maxCardinality = 0;
            }
        }
        subsettedDerivedUnion = element.getAttribute(PROPERTY_SUBSETTED_DERIVED_UNION);
        inverseAssociation = element.getAttribute(PROPERTY_INVERSE_ASSOCIATION);
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_QUALIFIED, "" + qualified); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_INVERSE_ASSOCIATION, inverseAssociation);
    }

    @Override
    public IPersistentAssociationInfo getPersistenceAssociatonInfo() {
        return (IPersistentAssociationInfo)persistenceAssociationInfo;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof PersistentAssociationInfo) {
            persistenceAssociationInfo = part;
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        if (xmlTag.getTagName().equals(IPersistentAssociationInfo.XML_TAG)) {
            persistenceAssociationInfo = new PersistentAssociationInfo(this, id);
            return persistenceAssociationInfo;
        }
        return super.newPart(xmlTag, id);
    }

    @Override
    public IIpsObjectPart newPart(Class<? extends IIpsObjectPart> partType) {
        // TODO AW: I don't think this implementation is reasonable as it only should create those
        // instances that can actually be part of the association.
        try {
            Constructor<? extends IIpsObjectPart> constructor = partType.getConstructor(IIpsObjectPart.class,
                    String.class);
            IIpsObjectPart result = constructor.newInstance(this, getNextPartId());
            return result;
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return super.newPart(partType);
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

}
