/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.HierarchyVisitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
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
    private int maxCardinality = Integer.MAX_VALUE;
    private String subsettedDerivedUnion = ""; //$NON-NLS-1$
    private boolean derivedUnion = false;
    private boolean constrain = false;

    protected Association(IType parent, String id) {
        super(parent, id);
    }

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
    public IType findTarget(IIpsProject ipsProject) throws CoreException {
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
        try {
            IType supertype = getType().findSupertype(ipsProject);
            if (supertype == null) {
                return null;
            }
            IAssociation superAssociation = supertype.findAssociation(getName(), ipsProject);
            return superAssociation;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
    public IAssociation findSubsettedDerivedUnion(IIpsProject project) throws CoreException {
        return getType().findAssociation(subsettedDerivedUnion, project);
    }

    @Override
    public IAssociation[] findDerivedUnionCandidates(IIpsProject ipsProject) throws CoreException {
        IType targetType = findTarget(ipsProject);
        if (targetType == null) {
            return new IAssociation[0];
        }
        DerivedUnionCandidatesFinder finder = new DerivedUnionCandidatesFinder(targetType, ipsProject);
        finder.start(getType());
        return finder.candidates.toArray(new IAssociation[finder.candidates.size()]);
    }

    @Override
    public boolean isSubsetOfDerivedUnion(IAssociation derivedUnion, IIpsProject project) throws CoreException {
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
        targetRolePlural = element.getAttribute(PROPERTY_TARGET_ROLE_PLURAL);
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
        derivedUnion = Boolean.valueOf(element.getAttribute(PROPERTY_DERIVED_UNION)).booleanValue();
        subsettedDerivedUnion = element.getAttribute(PROPERTY_SUBSETTED_DERIVED_UNION);
        constrain = Boolean.valueOf(element.getAttribute(PROPERTY_CONSTRAINS)).booleanValue();
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_ASSOCIATION_TYPE, type.getId());
        newElement.setAttribute(PROPERTY_TARGET, target);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR, targetRoleSingular);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL, targetRolePlural);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$

        if (maxCardinality == CARDINALITY_MANY) {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        } else {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
        }

        newElement.setAttribute(PROPERTY_DERIVED_UNION, "" + derivedUnion); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_SUBSETTED_DERIVED_UNION, subsettedDerivedUnion);
        newElement.setAttribute(PROPERTY_CONSTRAINS, String.valueOf(isConstrain()));
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        validateTarget(list);

        validateTargetRoleSingular(list, ipsProject);
        validateTargetRolePlural(list, ipsProject);

        validateMaxCardinality(list);
        validateMinCardinality(list);

        validateDerivedUnion(list, ipsProject);

        validateConstrain(list, ipsProject);
    }

    private void validateTarget(MessageList list) throws CoreException {
        ValidationUtils.checkIpsObjectReference(target, getIpsObject().getIpsObjectType(), "target", this, //$NON-NLS-1$
                PROPERTY_TARGET, MSGCODE_TARGET_DOES_NOT_EXIST, list);
    }

    private void validateTargetRoleSingular(MessageList list, IIpsProject ipsProject) {
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, Messages.Association_msg_TargetRoleSingular,
                this, PROPERTY_TARGET_ROLE_SINGULAR, MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET, list);

        IStatus javaStatus = ValidationUtils.validateFieldName(targetRoleSingular, ipsProject);
        if (!javaStatus.isOK()) {
            String text = NLS.bind(Messages.Association_msg_TargetRoleSingularNotAValidJavaFieldName,
                    targetRoleSingular);
            list.newError(MSGCODE_TARGET_ROLE_SINGULAR_NOT_A_VALID_JAVA_FIELD_NAME, text, this,
                    PROPERTY_TARGET_ROLE_SINGULAR);
        }
    }

    private void validateTargetRolePlural(MessageList list, IIpsProject ipsProject) {
        if (StringUtils.isNotEmpty(targetRolePlural)) {
            IStatus javaStatus = ValidationUtils.validateFieldName(targetRolePlural, ipsProject);
            if (!javaStatus.isOK()) {
                String text = NLS.bind(Messages.Association_msg_TargetRolePluralNotAValidJavaFieldName,
                        targetRolePlural);
                list.newError(MSGCODE_TARGET_ROLE_PLURAL_NOT_A_VALID_JAVA_FIELD_NAME, text, this,
                        PROPERTY_TARGET_ROLE_PLURAL);
            }

            if (targetRolePlural.equals(targetRoleSingular)) {
                String text = Messages.Association_msg_TargetRoleSingularIlleaglySameAsTargetRolePlural;
                list.add(new Message(MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR, text, Message.ERROR, this,
                        new String[] { PROPERTY_TARGET_ROLE_SINGULAR, PROPERTY_TARGET_ROLE_PLURAL }));
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
                    new String[] { PROPERTY_DERIVED_UNION, PROPERTY_MAX_CARDINALITY }));
        }
    }

    private void validateMinCardinality(MessageList list) {
        if (minCardinality > maxCardinality) {
            String text = Messages.Association_msg_MinCardinalityGreaterThanMaxCardinality;
            list.add(new Message(MSGCODE_MAX_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[] {
                    PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY }));
        }
    }

    private void validateDerivedUnion(MessageList list, IIpsProject ipsProject) throws CoreException {
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
            String text = NLS.bind(Messages.Association_msg_DerivedUnionDoesNotExist, subsettedDerivedUnion);
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

    private void validateDerivedUnionsTarget(MessageList list, IIpsProject ipsProject, IAssociation unionAss)
            throws CoreException {
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
            return;
        }
    }

    private void validateConstrain(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (isConstrain()) {
            IType supertype = getType().findSupertype(ipsProject);
            if (supertype == null) {
                return;
            }

            IAssociation superAssociation = supertype.findAssociation(getName(), ipsProject);
            checkConstrainsName(list, superAssociation, getName(), PROPERTY_TARGET_ROLE_SINGULAR);

            superAssociation = supertype.findAssociationByRoleNamePlural(getTargetRolePlural(), ipsProject);
            checkConstrainsName(list, superAssociation, getTargetRolePlural(), PROPERTY_TARGET_ROLE_PLURAL);

            if (isDerivedUnion()) {
                list.newError(MSGCODE_CONSTRAIN_DERIVED_UNION, Messages.Association_msg_ConstraintIsDerivedUnion, this,
                        PROPERTY_CONSTRAINS);
            }
            if (isSubsetOfADerivedUnion()) {
                list.newError(MSGCODE_CONSTRAIN_SUBSET_DERIVED_UNION,
                        Messages.Association_msg_ConstraintIsSubsetOfDerivedUnion, this, PROPERTY_CONSTRAINS);
            }
        }
    }

    private void checkConstrainsName(MessageList list, IAssociation superTypeAssociation, String name, String property) {
        if (superTypeAssociation == null) {
            String text = NLS.bind(Messages.Association_msg_ConstrainedAssociationDoesNotExist, name);
            list.newError(MSGCODE_CONSTRAINED_NOT_FOUND, text, this, property);
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

        private List<IAssociation> candidates = new ArrayList<IAssociation>();
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
                try {
                    IType derivedUnionTarget = association.findTarget(ipsProject);
                    if (derivedUnionTarget == null) {
                        continue;
                    }

                    if (targetType.isSubtypeOrSameType(derivedUnionTarget, ipsProject)) {
                        candidates.add(association);
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
            return true;
        }
    }

}
