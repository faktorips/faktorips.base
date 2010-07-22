/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.AggregationKind;
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
 * <p>
 * Note: This implementation is atomic per default. We cannot inherit from
 * {@link AtomicIpsObjectPart} directly since some methods there are "final" and thus cannot be
 * overridden.
 * 
 * @author Jan Ortmann
 */
public abstract class Association extends IpsObjectPart implements IAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    protected AssociationType type = IAssociation.DEFAULT_RELATION_TYPE;
    protected String target = ""; //$NON-NLS-1$
    protected String targetRoleSingular = ""; //$NON-NLS-1$
    protected String targetRolePlural = ""; //$NON-NLS-1$
    protected int minCardinality = 0;
    protected int maxCardinality = Integer.MAX_VALUE;
    protected String subsettedDerivedUnion = ""; //$NON-NLS-1$
    protected boolean derivedUnion = false;

    public Association(IIpsObject parent, String id) {
        super(parent, id);
    }

    @Override
    public IType getType() {
        return (IType)getParent();
    }

    @Override
    public AggregationKind getAggregationKind() {
        return getAssociationType().getAggregationKind();
    }

    @Override
    public AssociationType getAssociationType() {
        return type;
    }

    @Override
    public void setAssociationType(AssociationType newType) {
        ArgumentCheck.notNull(newType);
        AssociationType oldType = type;
        type = newType;
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

    @Override
    public void setDerivedUnion(boolean flag) {
        boolean oldValue = derivedUnion;
        derivedUnion = flag;
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

    @Override
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
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

    @Override
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    @Override
    public void setSubsettedDerivedUnion(String newRelation) {
        String oldValue = subsettedDerivedUnion;
        subsettedDerivedUnion = newRelation;
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
        if (derivedUnion == null) {
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
        if (max.equals("*")) { //$NON-NLS-1$
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
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        ValidationUtils.checkIpsObjectReference(target, getIpsObject().getIpsObjectType(), "target", this, //$NON-NLS-1$
                PROPERTY_TARGET, MSGCODE_TARGET_DOES_NOT_EXIST, list);
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, Messages.Association_msg_TargetRoleSingular,
                this, PROPERTY_TARGET_ROLE_SINGULAR, MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET, list);
        if (maxCardinality == 0) {
            String text = Messages.Association_msg_MaxCardinalityMustBeAtLeast1;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1, text, Message.ERROR, this,
                    PROPERTY_MAX_CARDINALITY));
        } else if (is1To1() && isDerivedUnion()
                && !(getAssociationType() == AssociationType.COMPOSITION_DETAIL_TO_MASTER)) {
            String text = Messages.Association_msg_MaxCardinalityForDerivedUnionTooLow;
            list.add(new Message(MSGCODE_MAX_CARDINALITY_FOR_DERIVED_UNION_TOO_LOW, text, Message.ERROR, this,
                    new String[] { PROPERTY_DERIVED_UNION, PROPERTY_MAX_CARDINALITY }));
        } else if (minCardinality > maxCardinality) {
            String text = Messages.Association_msg_MinCardinalityGreaterThanMaxCardinality;
            list.add(new Message(MSGCODE_MAX_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[] {
                    PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY }));
        }

        if (is1ToMany() || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations()) {
            ValidationUtils.checkStringPropertyNotEmpty(targetRolePlural, Messages.Association_msg_TargetRolePlural,
                    this, PROPERTY_TARGET_ROLE_PLURAL, MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET, list);
        }
        if (StringUtils.isNotEmpty(getTargetRolePlural()) && getTargetRolePlural().equals(getTargetRoleSingular())) {
            String text = Messages.Association_msg_TargetRoleSingularIlleaglySameAsTargetRolePlural;
            list.add(new Message(MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR, text, Message.ERROR, this,
                    new String[] { PROPERTY_TARGET_ROLE_SINGULAR, PROPERTY_TARGET_ROLE_PLURAL }));
        }
        validateDerivedUnion(list, ipsProject);
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

    private class DerivedUnionCandidatesFinder extends TypeHierarchyVisitor {

        private List<IAssociation> candidates = new ArrayList<IAssociation>();
        private IType targetType;

        public DerivedUnionCandidatesFinder(IType targetType, IIpsProject ipsProject) {
            super(ipsProject);
            this.targetType = targetType;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            IAssociation[] associations = currentType.getAssociations();
            for (int j = 0; j < associations.length; j++) {
                if (!associations[j].isDerivedUnion()) {
                    continue;
                }
                if (associations[j].equals(Association.this)) {
                    continue;
                }
                IType derivedUnionTarget = associations[j].findTarget(ipsProject);
                if (derivedUnionTarget == null) {
                    continue;
                }

                if (targetType.isSubtypeOrSameType(derivedUnionTarget, ipsProject)) {
                    candidates.add(associations[j]);
                }
            }
            return true;
        }
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    // TODO Joerg Merge PersistenceBranch: jetzt IpsObjectPart und dann atomic? siehe Note im
    // JavaDoc der Klasse
    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        throw new IllegalArgumentException("This implementation is atomic, cannot add part."); //$NON-NLS-1$
    }

    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("This implementation is atomic, cannot add part: " + partType); //$NON-NLS-1$
    }

    @Override
    protected void reinitPartCollections() {
        // nothing to do
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        throw new UnsupportedOperationException("This implementation is atomic, no part to remove."); //$NON-NLS-1$
    }

}
