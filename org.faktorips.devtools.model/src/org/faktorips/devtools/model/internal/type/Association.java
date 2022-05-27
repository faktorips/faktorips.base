/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.HierarchyVisitor;
import org.faktorips.devtools.model.internal.ValidationUtils;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAssociation.
 * 
 * @author Jan Ortmann
 */
public abstract class Association extends TypePart implements IAssociation {

    public static final String TAG_NAME = "Association"; //$NON-NLS-1$

    private AssociationType type = IAssociation.DEFAULT_RELATION_TYPE;
    private String target = ""; //$NON-NLS-1$
    private String targetRoleSingular = ""; //$NON-NLS-1$
    private String targetRolePlural = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private int maxCardinality = CARDINALITY_MANY;
    private String subsettedDerivedUnion = ""; //$NON-NLS-1$
    private boolean derivedUnion = false;
    private boolean constrain = false;

    protected Association(IType parent, String id) {
        super(parent, id);
    }

    @Override
    public abstract IAssociation findMatchingAssociation();

    @Override
    public AggregationKind getAggregationKind() {
        return getAssociationType().getAggregationKind();
    }

    @Override
    public AssociationType getAssociationType() {
        return type;
    }

    protected void setAssociationTypeInternal(AssociationType newType) {
        type = newType;
    }

    @Override
    public void setAssociationType(AssociationType newType) {
        ArgumentCheck.notNull(newType);
        AssociationType oldType = type;
        setAssociationTypeInternal(newType);
        valueChanged(oldType, newType);
    }

    @Override
    public boolean isAssoziation() {
        return type.isAssoziation();
    }

    @Override
    public String getName() {
        return targetRoleSingular;
    }

    @Override
    public boolean isDerived() {
        return isDerivedUnion();
    }

    @Override
    public boolean isDerivedUnion() {
        return derivedUnion;
    }

    protected void setDerivedUnionInternal(boolean flag) {
        derivedUnion = flag;
    }

    @Override
    public void setDerivedUnion(boolean flag) {
        boolean oldValue = derivedUnion;
        setDerivedUnionInternal(flag);
        valueChanged(oldValue, derivedUnion);
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    @Override
    public IType findTarget(IIpsProject ipsProject) {
        return (IType)ipsProject.findIpsObject(getIpsObject().getIpsObjectType(), target);
    }

    @Override
    public String getTargetRoleSingular() {
        return targetRoleSingular;
    }

    @Override
    public String getDefaultTargetRoleSingular() {
        return StringUtils.capitalize(QNameUtil.getUnqualifiedName(target));
    }

    @Override
    public void setTargetRoleSingular(String newRole) {
        String oldRole = targetRoleSingular;
        targetRoleSingular = newRole;
        valueChanged(oldRole, newRole);
    }

    @Override
    public String getTargetRolePlural() {
        return targetRolePlural;
    }

    @Override
    public String getDefaultTargetRolePlural() {
        return targetRoleSingular;
    }

    @Override
    public void setTargetRolePlural(String newRole) {
        String oldRole = targetRolePlural;
        targetRolePlural = newRole;
        valueChanged(oldRole, newRole);
    }

    @Override
    public boolean isTargetRolePluralRequired() {
        return is1ToMany() || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations();
    }

    @Override
    public int getMinCardinality() {
        return minCardinality;
    }

    protected void setMinCardinalityInternal(int newValue) {
        minCardinality = newValue;
    }

    @Override
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        setMinCardinalityInternal(newValue);
        valueChanged(oldValue, newValue);
    }

    @Override
    public int getMaxCardinality() {
        return maxCardinality;
    }

    @Override
    public boolean is1ToMany() {
        return isQualified() || maxCardinality > 1;
    }

    @Override
    public boolean is1ToManyIgnoringQualifier() {
        return maxCardinality > 1;
    }

    @Override
    public boolean is1To1() {
        return maxCardinality == 1 && !isQualified();
    }

    public void setMaxCardinalityInternal(int newValue) {
        maxCardinality = newValue;
    }

    @Override
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        setMaxCardinalityInternal(newValue);
        valueChanged(oldValue, newValue);
    }

    protected void setSubsettedDerivedUnionInternal(String newRelation) {
        subsettedDerivedUnion = newRelation;
    }

    @Override
    public void setSubsettedDerivedUnion(String newRelation) {
        String oldValue = subsettedDerivedUnion;
        setSubsettedDerivedUnionInternal(newRelation);
        valueChanged(oldValue, newRelation);
    }

    @Override
    public String getSubsettedDerivedUnion() {
        return subsettedDerivedUnion;
    }

    @Override
    public boolean isSubsetOfADerivedUnion() {
        return StringUtils.isNotEmpty(subsettedDerivedUnion);
    }

    @Override
    public boolean isConstrain() {
        return constrain;
    }

    @Override
    public void setConstrain(boolean constrain) {
        boolean oldValue = this.constrain;
        this.constrain = constrain;
        valueChanged(oldValue, constrain);
    }

    @Override
    public IAssociation findSuperAssociationWithSameName(IIpsProject ipsProject) {
        IType supertype = getType().findSupertype(ipsProject);
        if (supertype == null) {
            return null;
        }
        IAssociation superAssociation = supertype.findAssociation(getName(), ipsProject);
        return superAssociation;
    }

    @Override
    public IAssociation findConstrainedAssociation(IIpsProject ipsProject) {
        AssociationHierarchyVisitor visitor = new AssociationHierarchyVisitor(ipsProject) {

            @Override
            protected boolean continueVisiting() {
                return getLastVisited().isConstrain();
            }

        };
        visitor.start(this);
        return visitor.getSuperAssociation();
    }

    @Override
    public IAssociation findSubsettedDerivedUnion(IIpsProject project) {
        return getType().findAssociation(subsettedDerivedUnion, project);
    }

    @Override
    public IAssociation[] findDerivedUnionCandidates(IIpsProject ipsProject) {
        IType targetType = findTarget(ipsProject);
        if (targetType == null) {
            return new IAssociation[0];
        }
        DerivedUnionCandidatesFinder finder = new DerivedUnionCandidatesFinder(targetType, ipsProject);
        finder.start(getType());
        return finder.candidates.toArray(new IAssociation[finder.candidates.size()]);
    }

    @Override
    public boolean isSubsetOfDerivedUnion(IAssociation derivedUnion, IIpsProject project) {
        if (!isSubsetOfADerivedUnion()) {
            return false;
        }
        return derivedUnion.equals(findSubsettedDerivedUnion(project));
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        type = AssociationType.getRelationType(element.getAttribute(PROPERTY_ASSOCIATION_TYPE));
        if (type == null) {
            type = IAssociation.DEFAULT_RELATION_TYPE;
        }
        target = element.getAttribute(PROPERTY_TARGET);
        targetRoleSingular = element.getAttribute(PROPERTY_TARGET_ROLE_SINGULAR);
        targetRolePlural = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_TARGET_ROLE_PLURAL);
        try {
            minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        } catch (NumberFormatException e) {
            minCardinality = 0;
        }
        String max = element.getAttribute(PROPERTY_MAX_CARDINALITY);
        if ("*".equals(max)) { //$NON-NLS-1$
            maxCardinality = CARDINALITY_MANY;
        } else {
            try {
                maxCardinality = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                maxCardinality = 0;
            }
        }
        derivedUnion = ValueToXmlHelper.isAttributeTrue(element, PROPERTY_DERIVED_UNION);
        subsettedDerivedUnion = XmlUtil.getAttributeOrEmptyString(element, PROPERTY_SUBSETTED_DERIVED_UNION);
        constrain = ValueToXmlHelper.isAttributeTrue(element, PROPERTY_CONSTRAIN);
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_ASSOCIATION_TYPE, type.getId());
        newElement.setAttribute(PROPERTY_TARGET, target);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR, targetRoleSingular);
        if (StringUtils.isNotEmpty(targetRolePlural)) {
            newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL, targetRolePlural);
        }
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$

        if (maxCardinality == CARDINALITY_MANY) {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
        }

        newElement.setAttribute(PROPERTY_DERIVED_UNION, "" + derivedUnion); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(subsettedDerivedUnion)) {
            newElement.setAttribute(PROPERTY_SUBSETTED_DERIVED_UNION, subsettedDerivedUnion);
        }
        newElement.setAttribute(PROPERTY_CONSTRAIN, String.valueOf(isConstrain()));
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        validateTarget(list);

        validateTargetRoleSingular(list, ipsProject);
        validateTargetRolePlural(list, ipsProject);

        validateMaxCardinality(list);
        validateMinCardinality(list);

        validateDerivedUnion(list, ipsProject);

        validateConstrain(list, ipsProject);
    }

    private void validateTarget(MessageList list) {
        ValidationUtils.checkIpsObjectReference(target, getIpsObject().getIpsObjectType(), "target", this, //$NON-NLS-1$
                PROPERTY_TARGET, MSGCODE_TARGET_DOES_NOT_EXIST, list);
    }

    private void validateTargetRoleSingular(MessageList list, IIpsProject ipsProject) {
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, Messages.Association_msg_TargetRoleSingular,
                this, PROPERTY_TARGET_ROLE_SINGULAR, MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET, list);

        IStatus javaStatus = ValidationUtils.validateFieldName(targetRoleSingular, ipsProject);
        if (!javaStatus.isOK()) {
            String text = MessageFormat.format(Messages.Association_msg_TargetRoleSingularNotAValidJavaFieldName,
                    targetRoleSingular);
            list.newError(MSGCODE_TARGET_ROLE_SINGULAR_NOT_A_VALID_JAVA_FIELD_NAME, text, this,
                    PROPERTY_TARGET_ROLE_SINGULAR);
        }
    }

    private void validateTargetRolePlural(MessageList list, IIpsProject ipsProject) {
        if (StringUtils.isNotEmpty(targetRolePlural)) {
            IStatus javaStatus = ValidationUtils.validateFieldName(targetRolePlural, ipsProject);
            if (!javaStatus.isOK()) {
                String text = MessageFormat.format(Messages.Association_msg_TargetRolePluralNotAValidJavaFieldName,
                        targetRolePlural);
                list.newError(MSGCODE_TARGET_ROLE_PLURAL_NOT_A_VALID_JAVA_FIELD_NAME, text, this,
                        PROPERTY_TARGET_ROLE_PLURAL);
            }
        }

        if (is1ToMany() || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations()) {
            ValidationUtils.checkStringPropertyNotEmpty(targetRolePlural, Messages.Association_msg_TargetRolePlural,
                    this, PROPERTY_TARGET_ROLE_PLURAL, MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET, list);
        }
    }

    private void validateMaxCardinality(MessageList list) {
        if (maxCardinality == 0) {
            String text = Messages.Association_msg_MaxCardinalityMustBeAtLeast1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1, text, Message.ERROR, this,
                    PROPERTY_MAX_CARDINALITY));
        } else if (is1To1() && isDerivedUnion()
                && !(getAssociationType() == AssociationType.COMPOSITION_DETAIL_TO_MASTER)) {
            String text = Messages.Association_msg_MaxCardinalityForDerivedUnionTooLow;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW, text, Message.ERROR, this,
                    PROPERTY_DERIVED_UNION, PROPERTY_MAX_CARDINALITY));
        }
    }

    private void validateMinCardinality(MessageList list) {
        if (minCardinality > maxCardinality) {
            String text = Messages.Association_msg_MinCardinalityGreaterThanMaxCardinality;
            list.add(new Message(MSGCODE_MAX_IS_LESS_THAN_MIN, text, Message.ERROR, this,
                    PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY));
        }
    }

    private void validateDerivedUnion(MessageList list, IIpsProject ipsProject) {
        if (StringUtils.isEmpty(subsettedDerivedUnion)) {
            return;
        }
        if (subsettedDerivedUnion.equals(getName())) {
            list.add(new Message(MSGCODE_DERIVED_UNION_SUBSET_NOT_SAME_AS_DERIVED_UNION,
                    Messages.Association_msgDerivedUnionNotSubset, Message.ERROR, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
            return;
        }
        IAssociation unionAss = findSubsettedDerivedUnion(ipsProject);
        if (unionAss == null) {
            String text = MessageFormat.format(Messages.Association_msg_DerivedUnionDoesNotExist,
                    subsettedDerivedUnion);
            list.add(new Message(MSGCODE_DERIVED_UNION_NOT_FOUND, text, Message.ERROR, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
            return;
        }
        if (!unionAss.isDerivedUnion()) {
            String text = Messages.Association_msg_NotMarkedAsDerivedUnion;
            list.add(new Message(MSGCODE_NOT_MARKED_AS_DERIVED_UNION, text, Message.ERROR, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
            return;
        }
        if (unionAss.isQualified() && isQualified()) {
            if (unionAss.getMaxCardinality() < getMaxCardinality()) {
                list.add(new Message(MSGCODE_SUBSET_OF_DERIVED_UNION_SAME_MAX_CARDINALITY,
                        Messages.Association_msgSubsetOfDerivedUnionSameMaxCardinality, Message.ERROR, this,
                        PROPERTY_MAX_CARDINALITY));
            }
        }

        validateDerivedUnionsTarget(list, ipsProject, unionAss);

    }

    private void validateDerivedUnionsTarget(MessageList list, IIpsProject ipsProject, IAssociation unionAss) {
        IType unionTarget = unionAss.findTarget(ipsProject);
        if (unionTarget == null) {
            String text = Messages.Association_msg_TargetOfDerivedUnionDoesNotExist;
            list.add(new Message(MSGCODE_TARGET_OF_DERIVED_UNION_DOES_NOT_EXIST, text, Message.WARNING, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
            return;
        }
        IType targetType = findTarget(ipsProject);
        if (targetType != null && !targetType.isSubtypeOrSameType(unionTarget, ipsProject)) {
            String text = Messages.Association_msg_TargetNotSubclass;
            list.add(new Message(IAssociation.MSGCODE_TARGET_TYPE_NOT_A_SUBTYPE, text, Message.ERROR, this,
                    PROPERTY_SUBSETTED_DERIVED_UNION));
        }
    }

    private void validateConstrain(MessageList list, IIpsProject ipsProject) {
        if (isConstrain()) {
            IAssociation constrainedAssociation = findConstrainedAssociation(ipsProject);
            if (constrainedAssociation == null) {
                String text = MessageFormat.format(Messages.Association_msg_ConstrainedAssociationSingularDoesNotExist,
                        getName());
                list.newError(MSGCODE_CONSTRAINED_SINGULAR_NOT_FOUND, text,
                        new ObjectProperty(this, PROPERTY_CONSTRAIN),
                        new ObjectProperty(this, PROPERTY_TARGET_ROLE_SINGULAR));
            } else {
                IAssociation parentAssociation = findSuperAssociationWithSameName(ipsProject);
                validateConstrainedAssociation(list, constrainedAssociation, parentAssociation);
            }
        }
    }

    private void validateConstrainedAssociation(MessageList list,
            IAssociation constrainedAssociation,
            IAssociation parentAssociation) {
        if (!isCovariantTargetType(constrainedAssociation)) {
            String text = MessageFormat.format(Messages.Association_msg_ConstrainedTargetNoSuperclass, getName());
            list.newError(MSGCODE_CONSTRAINED_TARGET_SUPERTYP_NOT_COVARIANT, text,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_TARGET));
        }
        if (!constrainedAssociation.getTargetRolePlural().equals(getTargetRolePlural())) {
            String text = MessageFormat.format(Messages.Association_msg_ConstrainedAssociationPluralDoesNotExist,
                    constrainedAssociation.getTargetRolePlural());
            list.newError(MSGCODE_CONSTRAINED_PLURAL_NOT_FOUND, text, this, PROPERTY_TARGET_ROLE_PLURAL);
        }

        validateConstrainedAssociationType(list, constrainedAssociation);
        validateConstrainedCardinality(list, parentAssociation);
        validateConstrainingNotDerivedUnion(list);
        validateConstrainedAssociationNotDerivedUnion(list, constrainedAssociation);
        validateConstrainedIsMatchingAssociationParallel(list, constrainedAssociation);
    }

    private void validateConstrainedIsMatchingAssociationParallel(MessageList list,
            IAssociation constrainedAssociation) {
        if (!isMatchingAssociationParallel(constrainedAssociation)) {
            String text = Messages.Association_msg_ConstrainedInvalidMatchingAssociation;
            list.newError(MSGCODE_CONSTRAIN_INVALID_MATCHING_ASSOCIATION, text, this, PROPERTY_CONSTRAIN);
        }
    }

    private boolean isMatchingAssociationParallel(IAssociation constrainedAssociation) {
        IAssociation matchingAssociation = findMatchingAssociation();
        IAssociation constrainedMatchingAssociation = constrainedAssociation.findMatchingAssociation();

        if (isNoMatchingAssociationsDefined(matchingAssociation, constrainedMatchingAssociation)) {
            return true;
        } else if (matchingAssociation != null && constrainedMatchingAssociation != null) {
            if (matchingAssociation.equals(constrainedMatchingAssociation)) {
                // for product associations it is valid to have a constrained association only on
                // product side. In this case the matching association and the constrained matching
                // association is the same. For policy association it is simply not possible at all
                return true;
            } else {
                IAssociation matchingConstrainedAssociation = matchingAssociation
                        .findConstrainedAssociation(getIpsProject());
                return constrainedMatchingAssociation.equals(matchingConstrainedAssociation);
            }
        } else {
            return false;
        }
    }

    private boolean isNoMatchingAssociationsDefined(IAssociation matchingAssociation,
            IAssociation constrainedMatchingAssociation) {
        return matchingAssociation == null && constrainedMatchingAssociation == null;
    }

    private void validateConstrainedAssociationType(MessageList list, IAssociation superAssociation) {
        if (!isSameAssociationTypeAs(superAssociation)) {
            String text = MessageFormat.format(Messages.Association_msg_AssociationTypeNotEqualToSuperAssociation,
                    superAssociation.getAssociationType().getName());
            list.newError(MSGCODE_ASSOCIATION_TYPE_NOT_EQUAL_TO_SUPER_ASSOCIATION, text,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_ASSOCIATION_TYPE));
        }
    }

    private boolean isSameAssociationTypeAs(IAssociation otherAssociation) {
        return otherAssociation.getAssociationType().equals(getAssociationType());
    }

    private void validateConstrainedCardinality(MessageList list, IAssociation superAssociation) {
        if (getMaxCardinality() == 1 && superAssociation.getMaxCardinality() > 1) {
            String text = MessageFormat.format(
                    Messages.Association_msg_MaxCardinalityForConstrainMustAllowMultipleItems,
                    getStringCardinality(superAssociation.getMinCardinality()));
            list.newError(MSGCODE_MAX_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION, text,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_MAX_CARDINALITY));
        }
        if (superAssociation.getMinCardinality() > getMinCardinality()) {
            String text = MessageFormat.format(
                    Messages.Association_msg_MinCardinalityForConstrainHigherThanSuperAssociation,
                    getStringCardinality(superAssociation.getMinCardinality()));
            list.newError(MSGCODE_MIN_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION, text,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_MIN_CARDINALITY));
        }
        if (superAssociation.getMaxCardinality() < getMaxCardinality()) {
            String text = MessageFormat.format(
                    Messages.Association_msg_MaxCardinalityForConstrainLowerThanSuperAssociation,
                    getStringCardinality(superAssociation.getMaxCardinality()));
            list.newError(MSGCODE_MAX_CARDINALITY_NOT_VALID_CONSTRAINT_FOR_SUPER_ASSOCIATION, text,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_MAX_CARDINALITY));
        }
    }

    private String getStringCardinality(int cardinality) {
        if (cardinality == Integer.MAX_VALUE) {
            return "*"; //$NON-NLS-1$
        }
        return String.valueOf(cardinality);
    }

    private boolean isCovariantTargetType(IAssociation superAssociation) {
        IType targetType = findTarget(getIpsProject());
        IType superTargetType = superAssociation.findTarget(getIpsProject());
        if (targetType != null && superTargetType != null) {
            return targetType.isSubtypeOrSameType(superTargetType, getIpsProject());
        } else {
            return false;
        }
    }

    private void validateConstrainingNotDerivedUnion(MessageList list) {
        if (isDerivedUnion()) {
            list.newError(MSGCODE_CONSTRAIN_DERIVED_UNION, Messages.Association_msg_ConstraintIsDerivedUnion,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN), new ObjectProperty(this, PROPERTY_DERIVED_UNION));
        }
        if (isSubsetOfADerivedUnion()) {
            list.newError(MSGCODE_CONSTRAIN_SUBSET_DERIVED_UNION,
                    Messages.Association_msg_ConstraintIsSubsetOfDerivedUnion,
                    new ObjectProperty(this, PROPERTY_CONSTRAIN),
                    new ObjectProperty(this, PROPERTY_SUBSETTED_DERIVED_UNION));
        }
    }

    private void validateConstrainedAssociationNotDerivedUnion(MessageList list, IAssociation superAssociation) {
        if (superAssociation.isDerivedUnion()) {
            list.newError(MSGCODE_CONSTRAINED_DERIVED_UNION, Messages.Association_msg_ConstrainedIsDerivedUnion, this,
                    PROPERTY_CONSTRAIN);
        }
        if (superAssociation.isSubsetOfADerivedUnion()) {
            list.newError(MSGCODE_CONSTRAINED_SUBSET_DERIVED_UNION,
                    Messages.Association_msg_ConstrainedIsSubsetOfDerivedUnion, this, PROPERTY_CONSTRAIN);

        }
    }

    @Override
    public boolean isPluralLabelSupported() {
        return true;
    }

    protected abstract static class AssociationHierarchyVisitor extends HierarchyVisitor<IAssociation> {

        protected AssociationHierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected IAssociation findSupertype(IAssociation currentType, IIpsProject ipsProject) {
            return currentType.findSuperAssociationWithSameName(ipsProject);
        }

        @Override
        protected boolean visit(IAssociation currentType) {
            return continueVisiting();
        }

        protected abstract boolean continueVisiting();

        public IAssociation getSuperAssociation() {
            if (getVisited().size() > 1) {
                return getLastVisited();
            } else {
                return null;
            }
        }

        public IAssociation getLastVisited() {
            return getVisited().get(getVisited().size() - 1);
        }

    }

    private class DerivedUnionCandidatesFinder extends TypeHierarchyVisitor<IType> {

        private List<IAssociation> candidates = new ArrayList<>();
        private IType targetType;

        public DerivedUnionCandidatesFinder(IType targetType, IIpsProject ipsProject) {
            super(ipsProject);
            this.targetType = targetType;
        }

        @Override
        protected boolean visit(IType currentType) {
            List<IAssociation> associations = currentType.getAssociations();
            for (IAssociation association : associations) {
                if (!association.isDerivedUnion()) {
                    continue;
                }
                if (association.equals(Association.this)) {
                    continue;
                }
                IType derivedUnionTarget = association.findTarget(getIpsProject());
                if (derivedUnionTarget == null) {
                    continue;
                }

                if (targetType.isSubtypeOrSameType(derivedUnionTarget, getIpsProject())) {
                    candidates.add(association);
                }
            }
            return true;
        }
    }

}
