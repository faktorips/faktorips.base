/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class PolicyCmptTypeAssociation extends Association implements IPolicyCmptTypeAssociation {

    final static String TAG_NAME = "Association"; //$NON-NLS-1$

    private boolean qualified = false;
    private boolean productRelevant = true;
    private String inverseAssociation = ""; //$NON-NLS-1$
    private String targetRoleSingularProductSide = ""; //$NON-NLS-1$
    private String targetRolePluralProductSide = ""; //$NON-NLS-1$
    private int minCardinalityProductSide = 0;
    private int maxCardinalityProductSide = Integer.MAX_VALUE;

    public PolicyCmptTypeAssociation(IPolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isComposition() {
        return type.isCompositionDetailToMaster() | type.isCompositionMasterToDetail();
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean isCompositionMasterToDetail() {
		return type.isCompositionMasterToDetail();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCompositionDetailToMaster() {
		return type.isCompositionDetailToMaster();
	}
    
    /**
     * {@inheritDoc}
     */
    public boolean isInverseAssociationApplicable() {
        if (isAssoziation()) {
            return true;
        }
        if (isCompositionDetailToMaster()) {
            return false;
        }
        return getIpsProject().getIpsArtefactBuilderSet().isInverseRelationLinkRequiredFor2WayCompositions();
    }
    
    /**
     * {@inheritDoc}
     */
	public boolean isContainerRelationApplicable() {
        return isAssoziation() || isCompositionMasterToDetail();
    }

    /** 
     * {@inheritDoc}
     */
    public void setAssociationType(AssociationType newType) {
        if (newType.isCompositionDetailToMaster()) {
            subsettedDerivedUnion = ""; //$NON-NLS-1$
            inverseAssociation = ""; //$NON-NLS-1$
            derivedUnion = false;
            qualified = false;
            minCardinality = 0;
            maxCardinality = 1;
            productRelevant = false;
            targetRoleSingularProductSide = ""; //$NON-NLS-1$
            targetRolePluralProductSide = ""; //$NON-NLS-1$
        }
        super.setAssociationType(newType);
    }
    
    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findTarget() throws CoreException {
        if (StringUtils.isEmpty(target)) {
            return null;
        }
        return getIpsProject().findPolicyCmptType(target);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType findTargetPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findPolicyCmptType(target);
    }

    /**
     * {@inheritDoc} 
     */
    public boolean isConstrainedByProductStructure(IIpsProject ipsProject) throws CoreException {
        return findMatchingProductCmptTypeAssociation(ipsProject)!=null;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeAssociation findMatchingProductCmptTypeAssociation(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = getPolicyCmptType().findProductCmptType(ipsProject);
        if (productCmptType==null) {
            return null;
        }
        IPolicyCmptType targetType = findTarget();
        if (targetType==null) {
            return null;
        }
        IAssociation[] candidates = productCmptType.getAssociationsForTarget(targetType.getProductCmptType());
        int index = getAssociationIndex();
        if (index>=candidates.length) {
            return null;
        }
        return (IProductCmptTypeAssociation)candidates[index];
    }

    private int getAssociationIndex() {
        List allAssociationsForTheTargetType = new ArrayList();
        IPolicyCmptTypeAssociation[] ass = getPolicyCmptType().getPolicyCmptTypeAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (target.equals(ass[i].getTarget())) {
                allAssociationsForTheTargetType.add(ass[i]);
            }
        }
        int index = 0;
        for (Iterator it=allAssociationsForTheTargetType.iterator(); it.hasNext(); index++) {
            if (it.next()==this) {
                return index;
            }
        }
        throw new RuntimeException("Can't get index of association " + this); //$NON-NLS-1$
    }

    /** 
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        return productRelevant;
    }

    /** 
     * {@inheritDoc}
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
	public boolean isQualified() {
        return qualified;
    }

    /**
     * {@inheritDoc}
     */
    public void setQualified(boolean newValue) {
        boolean oldValue = qualified;
        qualified = newValue;
        valueChanged(oldValue, newValue);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isQualificationPossible(IIpsProject ipsProject) throws CoreException {
        if (!isCompositionMasterToDetail()) {
            return false;
        }
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType==null || !targetType.isConfigurableByProductCmptType()) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String findQualifierCandidate(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType targetType = findTargetPolicyCmptType(ipsProject);
        if (targetType==null || !targetType.isConfigurableByProductCmptType()) {
            return ""; //$NON-NLS-1$
        }
        return targetType.getProductCmptType();
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType findQualifier(IIpsProject ipsProject) throws CoreException {
        if (!qualified) {
            return null;
        }
        return ipsProject.findProductCmptType(findQualifierCandidate(ipsProject));
    }

    /**
     * {@inheritDoc}
     */
    public AssociationType getCorrespondingAssociationType() {
        return type == null ? null : 
            type.isAssoziation() ? AssociationType.ASSOCIATION : 
            type.isCompositionDetailToMaster() ? AssociationType.COMPOSITION_MASTER_TO_DETAIL : 
            type.isCompositionMasterToDetail() ? AssociationType.COMPOSITION_DETAIL_TO_MASTER : 
                null;
    }

    /**
	 * {@inheritDoc}
	 */
	public String getTargetRoleSingularProductSide() {
		return targetRoleSingularProductSide;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetRoleSingularProductSide(String newRole) {
        String oldRole = targetRoleSingularProductSide;
        targetRoleSingularProductSide = newRole;
        valueChanged(oldRole, newRole);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetRolePluralProductSide(String newRole) {
        String oldRole = targetRolePluralProductSide;
        targetRolePluralProductSide = newRole;
        valueChanged(oldRole, newRole);
	}

    /**
	 * {@inheritDoc}
	 */
	public String getTargetRolePluralProductSide() {
		return targetRolePluralProductSide;
	}

    /**
     * {@inheritDoc}
     */
	public String getDefaultTargetRolePluralProductSide() {
        return targetRoleSingularProductSide;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTargetRolePluralRequiredProductSide() {
        return maxCardinalityProductSide>1 || getIpsProject().getIpsArtefactBuilderSet().isRoleNamePluralRequiredForTo1Relations();
    }

    /**
	 * {@inheritDoc}
	 */
	public int getMinCardinalityProductSide() {
		return minCardinalityProductSide;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinCardinalityProductSide(int newMin) {
		int oldMin = minCardinalityProductSide;
		minCardinalityProductSide = newMin;
		valueChanged(oldMin, newMin);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxCardinalityProductSide() {
		return maxCardinalityProductSide;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxCardinalityProductSide(int newMax) {
		int oldMax = maxCardinalityProductSide;
		maxCardinalityProductSide = newMax;
		valueChanged(oldMax, newMax);
	}

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation findSubsettedDerivedUnion() throws CoreException {
    	if (StringUtils.isEmpty(subsettedDerivedUnion)) {
    		return null;
    	}
        IPolicyCmptType type = (IPolicyCmptType)getIpsObject();
        IPolicyCmptType[] supertypes = type.getSupertypeHierarchy().getAllSupertypesInclSelf(type);
        for (int i=0; i<supertypes.length; i++) {
            IPolicyCmptTypeAssociation[] relations = supertypes[i].getPolicyCmptTypeAssociations();
            for (int j=0; j<relations.length; j++) {
                if (subsettedDerivedUnion.equals(relations[j].getTargetRoleSingular())) {
                    return relations[j];
                }
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getInverseAssociation() {
        return inverseAssociation;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasInverseAssociation() {
        return StringUtils.isNotEmpty(inverseAssociation);
    }

    /**
     * {@inheritDoc}
     */
    public void setInverseAssociation(String newRelation) {
        String oldValue = this.inverseAssociation;
        this.inverseAssociation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public IPolicyCmptTypeAssociation findInverseAssociation() throws CoreException {
        if (StringUtils.isEmpty(inverseAssociation)) {
            return null;
        }
        if (type.isCompositionDetailToMaster()) {
            return null;
        }
        IPolicyCmptType target = findTarget();
        if (target==null) {
            return null;
        }
        IPolicyCmptTypeAssociation[] relations = target.getPolicyCmptTypeAssociations();
        for (int i=0; i<relations.length; i++) {
            if (relations[i].getName().equals(inverseAssociation)) {
                return relations[i];
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation newInverseAssociation() throws CoreException {
        IPolicyCmptType targetPolicyCmptType = findTargetPolicyCmptType(getIpsProject());
        if (targetPolicyCmptType == null) {
            throw new CoreException(new IpsStatus("Target policy component type of association " + getName()
                    + " not found."));
        }
        
        IPolicyCmptTypeAssociation inverseAssociation = targetPolicyCmptType.newPolicyCmptTypeAssociation();
        inverseAssociation.setTarget(getPolicyCmptType().getQualifiedName());
        inverseAssociation.setAssociationType(getCorrespondingAssociationType());
        
        if (type == AssociationType.ASSOCIATION){
            // FIXME Joerg: nur bei association inverse setzen?, wenn ja Test erweitern!
            setInverseAssociation(inverseAssociation.getName());
            inverseAssociation.setInverseAssociation(getName());
        }
        
        // FIXME Joerg: richtig so?, wenn ja Test erweitern!
        IPolicyCmptTypeAssociation derivedUnionAssociation = (IPolicyCmptTypeAssociation)findSubsettedDerivedUnion(getIpsProject());
        if (inverseAssociation.isAssoziation() && derivedUnionAssociation != null){
            inverseAssociation.setSubsettedDerivedUnion(derivedUnionAssociation.getInverseAssociation());
        }
        
        if (isAssoziation() && isDerivedUnion()){
            inverseAssociation.setDerivedUnion(true);
        }
        
        return inverseAssociation;
    }

    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
    	String baseImageName = getAssociationType().getImageName();
        try {
            if (isConstrainedByProductStructure(getIpsProject())) {
            	return IpsPlugin.getDefault().getProductRelevantImage(baseImageName);
            }
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return IpsPlugin.getDefault().getImage(baseImageName);
    }

    /** 
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);        
        
        if (maxCardinality != 1 && this.type == AssociationType.COMPOSITION_DETAIL_TO_MASTER) {
        	String text = Messages.Association_msg_DetailToMasterAssociationMustHaveMaxCardinality1;
        	list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION, text, Message.ERROR, this, new String[] {PROPERTY_MAX_CARDINALITY, IAssociation.PROPERTY_ASSOCIATION_TYPE}));
        }
        // TODO v2 - das muss in type implementiert werden!!!
        new CheckForDuplicateRoleNameVisitor(list).start(getPolicyCmptType());
        validateDerivedUnion(list, getIpsProject());
        validateInverseRelation(list);
    }
    
    private void validateDerivedUnion(MessageList list, IIpsProject ipsProject) throws CoreException {
        IAssociation unionAss = findSubsettedDerivedUnion(ipsProject);
        if (unionAss instanceof IPolicyCmptTypeAssociation) {
            checkForDerivedUnionInverseAssociationMismatch((IPolicyCmptTypeAssociation)unionAss, list, ipsProject);
        }
    }
    
    /**
     * Performs the check for the rule with message code
     * @see IPolicyCmptTypeAssociation#MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION
     */
    private void checkForDerivedUnionInverseAssociationMismatch(IPolicyCmptTypeAssociation derivedUnion, MessageList list, IIpsProject ipsProject) throws CoreException {
        IPolicyCmptTypeAssociation inverseAss = findInverseAssociation();
        if (inverseAss==null) {
            return; // not found => error will be reported in validateInverseRelation
        }
        if (isComposition() || inverseAss.isComposition()) {
            return;
        }
        IPolicyCmptTypeAssociation inverseRelationOfContainerRel = derivedUnion.findInverseAssociation();
        if (inverseRelationOfContainerRel==null) {
            return; // not found => error will be reported in validateReverseRelation
        }
        IAssociation derivedUnionOfInverseRel = inverseAss.findSubsettedDerivedUnion(ipsProject);
        if (derivedUnionOfInverseRel==null || derivedUnionOfInverseRel!=inverseRelationOfContainerRel) {
            String text = NLS.bind(Messages.Association_msg_InverseAssociationInconsistentWithDerivedUnion, derivedUnion);
            list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_INCONSTENT_WITH_DERIVED_UNION, text, Message.ERROR, this, PROPERTY_SUBSETTED_DERIVED_UNION)); //$NON-NLS-1$
        }
    }
    
    private void validateInverseRelation(MessageList list) throws CoreException {
        if (StringUtils.isEmpty(inverseAssociation)) {
            return;
        }
        if (isComposition()) {
            String text = Messages.Association_msg_ForDetailToMasterAssociationsNeedNotSpecifyInverse;
            list.add(new Message(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_INFO_NOT_NEEDED, text, Message.WARNING, this, PROPERTY_INVERSE_ASSOCIATION)); //$NON-NLS-1$
            return;
        }
        IPolicyCmptTypeAssociation inverseAss = findInverseAssociation();
        if (inverseAss==null) {
            String text = NLS.bind(Messages.Association_msg_AssociationNotFoundInTarget, inverseAssociation, target);
            list.add(new Message(MSGCODE_INVERSE_RELATION_DOES_NOT_EXIST_IN_TARGET, text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION)); //$NON-NLS-1$
            return;
        }
        if (!inverseAss.getInverseAssociation().equals(targetRoleSingular)) {
            String text = Messages.Association_msg_InverseAssociationMismatch;
            list.add(new Message(MSGCODE_INVERSE_RELATION_MISMATCH, text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION)); //$NON-NLS-1$
        }
        if  ((!inverseAss.getAssociationType().isAssoziation())
                || (inverseAss.getAssociationType().isAssoziation() && !type.isAssoziation())) {
                String text = Messages.Association_msg_InverseAssociationMustBeOfTypeAssociation;
                list.add(new Message(MSGCODE_INVERSE_ASSOCIATION_TYPE_MISSMATCH, text, Message.ERROR, this, new String[]{PROPERTY_INVERSE_ASSOCIATION})); //$NON-NLS-1$
                return;
        } else {
            if (isDerivedUnion()!=inverseAss.isDerivedUnion()) {
                String text = Messages.Association_msg_InverseAssociationMustBeMarkedAsDerivedUnionToo;
                list.add(new Message(MSGCODE_INVERSE_ASSOCIATIONS_MUST_BOTH_BE_MARKED_AS_CONTAINER, text, Message.ERROR, this, PROPERTY_INVERSE_ASSOCIATION)); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
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
        }
        else {
        	try {
        		maxCardinality = Integer.parseInt(max);
        	} catch (NumberFormatException e) {
        		maxCardinality = 0;
        	}
        }
        subsettedDerivedUnion = element.getAttribute(PROPERTY_SUBSETTED_DERIVED_UNION);
        inverseAssociation = element.getAttribute(PROPERTY_INVERSE_ASSOCIATION);
        if (isCompositionDetailToMaster()) {
            inverseAssociation = ""; //$NON-NLS-1$
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_QUALIFIED, "" + qualified); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_INVERSE_ASSOCIATION, inverseAssociation);
    }
    
    private class CheckForDuplicateRoleNameVisitor extends PolicyCmptTypeHierarchyVisitor {

        private MessageList list;

        public CheckForDuplicateRoleNameVisitor(MessageList list) {
            super();
            this.list = list;
        }
        
        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            int numOfMsgs = list.getNoOfMessages();
            IAssociation[] relations = currentType.getAssociations();
            for (int j = 0; j < relations.length; j++) {
                if (relations[j]==PolicyCmptTypeAssociation.this) {
                    continue;
                }
                if (!StringUtils.isEmpty(PolicyCmptTypeAssociation.this.targetRoleSingular) && relations[j].getTargetRoleSingular().equals(targetRoleSingular)) {
                    String text = Messages.Relation_msgSameSingularRoleName;
                    list.add(new Message(MSGCODE_SAME_SINGULAR_ROLENAME, text, Message.ERROR, PolicyCmptTypeAssociation.this, PROPERTY_TARGET_ROLE_SINGULAR));
                }
                if (!StringUtils.isEmpty(PolicyCmptTypeAssociation.this.targetRolePlural) && relations[j].getTargetRolePlural().equals(targetRolePlural))  {
                    String text = Messages.Relation_msgSamePluralRolename;
                    list.add(new Message(MSGCODE_SAME_PLURAL_ROLENAME, text, Message.ERROR, PolicyCmptTypeAssociation.this, PROPERTY_TARGET_ROLE_PLURAL)); //$NON-NLS-1$
                }
            }
            return list.getNoOfMessages()==numOfMsgs; // no new message added, continue visting/validating
        }

    }
    

}
