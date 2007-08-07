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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class Relation extends AtomicIpsObjectPart implements IRelation {

    final static String TAG_NAME = "Relation"; //$NON-NLS-1$

    private RelationType type = IRelation.DEFAULT_RELATION_TYPE;
    private String target = ""; //$NON-NLS-1$
    private String targetRoleSingular = ""; //$NON-NLS-1$
    private String targetRolePlural = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE; 
    private boolean productRelevant = true;
    private String containerRelation = ""; //$NON-NLS-1$
    private String inverseRelation = ""; //$NON-NLS-1$
    private boolean readOnlyContainer = false;
    private String targetRoleSingularProductSide = ""; //$NON-NLS-1$
    private String targetRolePluralProductSide = ""; //$NON-NLS-1$
    private int minCardinalityProductSide = 0;
    private int maxCardinalityProductSide = Integer.MAX_VALUE;

    public Relation(IPolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public Relation() {
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
    public String getName() {
        return targetRoleSingular;
    }
    
    /** 
     * {@inheritDoc}
     */
    public RelationType getRelationType() {
        return type;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public boolean isAssoziation() {
		return type.isAssoziation();
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
    public boolean isInverseRelationApplicable() {
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
    public void setRelationType(RelationType newType) {
        RelationType oldType = type;
        type = newType;
        if (newType!= null && newType.isCompositionDetailToMaster()) {
            containerRelation = "";
            inverseRelation = "";
            readOnlyContainer = false;
            minCardinality = 0;
            maxCardinality = 1;
            productRelevant = false;
            targetRoleSingularProductSide = "";
            targetRolePluralProductSide = "";
        }
        valueChanged(oldType, newType);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isReadOnlyContainer() {
        return readOnlyContainer;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setReadOnlyContainer(boolean flag) {
        boolean oldValue = readOnlyContainer;
        this.readOnlyContainer = flag;
        valueChanged(oldValue, readOnlyContainer);
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getTarget() {
        return target;
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
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    /** 
     * {@inheritDoc}
     */
    public String getTargetRoleSingular() {
        return targetRoleSingular;
    }

    /** 
     * {@inheritDoc}
     */
    public void setTargetRoleSingular(String newRole) {
        String oldRole = targetRoleSingular;
        targetRoleSingular = newRole;
        valueChanged(oldRole, newRole);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDefaultTargetRoleSingular() {
        String defaultRole = target;
        if (defaultRole==null) {
            defaultRole = "";
        }
        int pos = target.lastIndexOf('.');
        if (pos!=-1) {
            defaultRole = target.substring(pos+1);
        } 
        setTargetRoleSingular(defaultRole);
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetRolePlural() {
        return targetRolePlural;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTargetRolePlural(String newRole) {
        String oldRole = targetRolePlural;
        targetRolePlural = newRole;
        valueChanged(oldRole, newRole);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDefaultTargetRolePlural() {
        setTargetRolePlural(targetRoleSingular);
    }

    /** 
     * {@inheritDoc}
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /** 
     * {@inheritDoc}
     */
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);
        
    }

    /** 
     * {@inheritDoc}
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean is1ToMany() {
    	return maxCardinality > 1;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public boolean is1To1() {
		return maxCardinality == 1;
	}

	/**
     * {@inheritDoc}
     */ 
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
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
    public String getContainerRelation() {
        return containerRelation;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasContainerRelation() {
        return !StringUtils.isEmpty(containerRelation);
    }

    /**
     * {@inheritDoc}
     */
    public IRelation findContainerRelation() throws CoreException {
    	if (StringUtils.isEmpty(containerRelation)) {
    		return null;
    	}
        IPolicyCmptType type = (IPolicyCmptType)getIpsObject();
        IPolicyCmptType[] supertypes = type.getSupertypeHierarchy().getAllSupertypesInclSelf(type);
        for (int i=0; i<supertypes.length; i++) {
            IRelation[] relations = supertypes[i].getRelations();
            for (int j=0; j<relations.length; j++) {
                if (containerRelation.equals(relations[j].getTargetRoleSingular())) {
                    return relations[j];
                }
            }
        }
        return null;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public boolean isContainerRelationImplementation() throws CoreException {
		return StringUtils.isNotEmpty(containerRelation);
	}
    
	/**
     * {@inheritDoc}
     */
    public boolean isContainerRelationImplementation(IRelation containerRelation) throws CoreException {
        if (containerRelation==null) {
            return false;
        }
        if (!containerRelation.isReadOnlyContainer()) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.Relation_Error_RelationIsNoContainerRelation, "" + containerRelation))); //$NON-NLS-1$
        }
        if (!isContainerRelationImplementation()) {
            return false;
        }
        return containerRelation.equals(findContainerRelation());
    }

    /** 
     * {@inheritDoc}
     */
    public void setContainerRelation(String newRelation) {
        String oldValue = containerRelation;
        containerRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getInverseRelation() {
        return inverseRelation;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasInverseRelation() {
        return StringUtils.isNotEmpty(inverseRelation);
    }

    /**
     * {@inheritDoc}
     */
    public void setInverseRelation(String newRelation) {
        String oldValue = this.inverseRelation;
        this.inverseRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public IRelation findInverseRelation() throws CoreException {
        if (type.isCompositionDetailToMaster()) {
            return null;
        }
    	if (StringUtils.isEmpty(inverseRelation)) {
            return null;
        }
        IPolicyCmptType target = findTarget();
        if (target==null) {
            return null;
        }
        IRelation[] relations = target.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (relations[i].getName().equals(inverseRelation)) {
                return relations[i];
            }
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
    	String baseImageName = ""; //$NON-NLS-1$
        if (this.type==RelationType.COMPOSITION_MASTER_TO_DETAIL) {
        	baseImageName = "Composition.gif"; //$NON-NLS-1$
        } else if (this.type==RelationType.COMPOSITION_DETAIL_TO_MASTER) {
        	baseImageName = "ReverseComposition.gif"; //$NON-NLS-1$
        } else {
        	baseImageName = "Relation.gif"; //$NON-NLS-1$ 
        }
        if (isProductRelevant()) {
        	return IpsPlugin.getDefault().getProductRelevantImage(baseImageName);
        } else {
        	return IpsPlugin.getDefault().getImage(baseImageName);
        }
    }

    /** 
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.POLICY_CMPT_TYPE, "target", this,  //$NON-NLS-1$
        		PROPERTY_TARGET, MSGCODE_TARGET_DOES_NOT_EXIST, list); //$NON-NLS-1$
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, Messages.Relation_msgTargetRoleSingular, this, PROPERTY_TARGET_ROLE_SINGULAR,
        		MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET, list);
        
        if (maxCardinality == 0) {
        	String text = Messages.Relation_msgMaxCardinalityMustBeAtLeast1;
        	list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_AT_LEAST_1, text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY)); //$NON-NLS-1$
        } else if (maxCardinality == 1 && isReadOnlyContainer() && getRelationType() != RelationType.COMPOSITION_DETAIL_TO_MASTER) {
        	String text = Messages.Relation_msgMaxCardinalityForContainerRelationTooLow;
        	list.add(new Message(MSGCODE_MAX_CARDINALITY_FOR_CONTAINERRELATION_TOO_LOW, text, Message.ERROR, this, new String[]{PROPERTY_READONLY_CONTAINER, PROPERTY_MAX_CARDINALITY})); //$NON-NLS-1$
        } else if (minCardinality > maxCardinality) {
        	String text = Messages.Relation_msgMinCardinalityGreaterThanMaxCardinality;
        	list.add(new Message(MSGCODE_MAX_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[]{PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY})); //$NON-NLS-1$
        }
        
        if (maxCardinality > 1) {
			ValidationUtils.checkStringPropertyNotEmpty(targetRolePlural,
					Messages.Relation_msgTargetRolePlural, this, PROPERTY_TARGET_ROLE_PLURAL,
					MSGCODE_TARGET_ROLE_PLURAL_MUST_BE_SET, list);
		}
        
		if (StringUtils.isNotEmpty(this.getTargetRolePlural())
				&& this.getTargetRolePlural().equals(
						this.getTargetRoleSingular())) {
			String text = Messages.Relation_msgTargetRoleSingularIlleaglySameAsTargetRolePlural;
			list.add(new Message(
					MSGCODE_TARGET_ROLE_PLURAL_EQUALS_TARGET_ROLE_SINGULAR,
					text, Message.ERROR, this, new String[] {
							PROPERTY_TARGET_ROLE_SINGULAR,
							PROPERTY_TARGET_ROLE_PLURAL }));
		}
		
        if (maxCardinality != 1 && this.type == RelationType.COMPOSITION_DETAIL_TO_MASTER) {
        	String text = Messages.Relation_msgRevereseCompositionMustHaveMaxCardinality1;
        	list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION, text, Message.ERROR, this, new String[] {PROPERTY_MAX_CARDINALITY, PROPERTY_RELATIONTYPE}));
        }
        
        if (this.type == RelationType.COMPOSITION_DETAIL_TO_MASTER && isProductRelevant()) {
        	String text = Messages.Relation_msgReverseCompositionCantBeMarkedAsProductRelevant;
        	list.add(new Message(MSGCODE_REVERSE_COMPOSITION_CANT_BE_MARKED_AS_PRODUCT_RELEVANT, text, Message.ERROR, this, new String[] {PROPERTY_PRODUCT_RELEVANT, PROPERTY_RELATIONTYPE}));
        }
       
        IPolicyCmptType targetPolicyCmptType = findTarget();
		if (targetPolicyCmptType != null && 
				this.type != RelationType.COMPOSITION_DETAIL_TO_MASTER &&
				this.isProductRelevant() &&
				! targetPolicyCmptType.isConfigurableByProductCmptType()) {
			String text = Messages.Relation_msgRelationCanOnlyProdRelIfTargetTypeIsConfByProduct;
			list.add(new Message(
							MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TARGET_TYPE_IS,
							text, Message.ERROR, this,
							PROPERTY_PRODUCT_RELEVANT));
		}
        
        new CheckForDuplicateRoleNameVisitor(list).start(getPolicyCmptType());
        validateProductSide(list);
        validateContainerRelation(list);
        validateInverseRelation(list);
    }
    
    private void validateProductSide(MessageList list) {
		if (!isProductRelevant()) {
			return;
		}

		if (!this.getPolicyCmptType().isConfigurableByProductCmptType()) {
			String text = Messages.Relation_msgRelationCanBeProductRelevantOnlyIfTypeIs;
			list
					.add(new Message(
							MSGCODE_RELATION_CAN_ONLY_BE_PRODUCT_RELEVANT_IF_THE_TYPE_IS,
							text, Message.ERROR, this,
							PROPERTY_PRODUCT_RELEVANT));
		}

		if (StringUtils.isEmpty(this.getTargetRoleSingularProductSide())) {
			String text = Messages.Relation_msgNoTargetRoleSingular;
			list.add(new Message(MSGCODE_NO_TARGET_ROLE_SINGULAR_PRODUCTSIDE,
					text, Message.ERROR, this,
					PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
		}

		if (StringUtils.isEmpty(this.getTargetRolePluralProductSide())) {
			String text = Messages.Relation_msgNoTargetRolePlural;
			list.add(new Message(MSGCODE_NO_TARGET_ROLE_PLURAL_PRODUCTSIDE,
					text, Message.ERROR, this,
					PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE));
		} else {
			if (this.getTargetRolePluralProductSide().equals(
					this.getTargetRoleSingularProductSide())) {
				String text = Messages.Relation_msgTargetRoleSingularIlleaglySameAsTargetRolePluralProdSide;
				list
						.add(new Message(
								MSGCODE_TARGET_ROLE_PLURAL_PRODUCTSIDE_EQUALS_TARGET_ROLE_SINGULAR_PRODUCTSIDE,
								text,
								Message.ERROR,
								this,
								new String[] {
										PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE,
										PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE }));
			}
		}
	}
    
    private void validateContainerRelation(MessageList list) throws CoreException {
        if (StringUtils.isEmpty(containerRelation)) {
            return;
        }
        IRelation containerRel = findContainerRelation();
        if (containerRel==null) {
            String text = NLS.bind(Messages.Relation_msgContainerRelNotInSupertype, containerRelation);
            list.add(new Message(MSGCODE_CONTAINERRELATION_NOT_IN_SUPERTYPE, text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        if (!containerRel.isReadOnlyContainer()) {
            String text = Messages.Relation_msgNotMarkedAsContainerRel;
            list.add(new Message(MSGCODE_NOT_MARKED_AS_CONTAINERRELATION, text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        if (containerRel.isProductRelevant() != isProductRelevant()) {
			String text = Messages.Relation_msgImplementationMustHaveSameProductRelevantValue;
			list
					.add(new Message(
							MSGCODE_IMPLEMENTATION_MUST_HAVE_SAME_PRODUCT_RELEVANT_VALUE,
							text, Message.ERROR, new String[] {
									PROPERTY_CONTAINER_RELATION,
									PROPERTY_PRODUCT_RELEVANT }));
		}
        IPolicyCmptType superRelationTarget = getIpsProject().findPolicyCmptType(containerRel.getTarget());
        if (superRelationTarget==null) {
            String text = Messages.Relation_msgNoTarget;
            list.add(new Message(MSGCODE_CONTAINERRELATION_TARGET_DOES_NOT_EXIST, text, Message.WARNING, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        IPolicyCmptType pcType = getIpsProject().findPolicyCmptType(target);
        if (pcType!=null) {
            ITypeHierarchy hierachy = pcType.getSupertypeHierarchy();
            if (!superRelationTarget.equals(pcType) && !hierachy.isSupertypeOf(superRelationTarget, pcType)) {
                String text = Messages.Relation_msgTargetNotSubclass;
                list.add(new Message(MSGCODE_TARGET_CLASS_NOT_A_SUBCLASS, text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));     //$NON-NLS-1$
            }
        }
        checkForContainerRelationReverseRelationMismatch(containerRel, list);
    }
    
    /**
     * Performs the check for the rule with message code
     * @see IRelation#MSGCODE_CONTAINERRELATION_REVERSERELATION_MISMATCH
     */
    private void checkForContainerRelationReverseRelationMismatch(IRelation containerRel, MessageList list) throws CoreException {
        IRelation inverseRel = findInverseRelation();
        if (inverseRel==null) {
            return; // not found => error will be reported in validateInverseRelation
        }
        IRelation reverseRelationOfContainerRel = containerRel.findInverseRelation();
        if (reverseRelationOfContainerRel==null) {
            return; // not found => error will be reported in validateReverseRelation
        }
        IRelation containerRelationofReverseRel = inverseRel.findContainerRelation();
        if (containerRelationofReverseRel==null || containerRelationofReverseRel!=reverseRelationOfContainerRel) {
            String text = Messages.Relation_msgContainerRelNotReverseRel;
            list.add(new Message(MSGCODE_CONTAINERRELATION_REVERSERELATION_MISMATCH, text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
        }
    }
    
    private void validateInverseRelation(MessageList list) throws CoreException {
        if (StringUtils.isEmpty(inverseRelation)) {
            return;
        }
        if (isCompositionDetailToMaster()) {
            String text = Messages.Relation_noReverseRelationNeededForDetailToMasterRelations;
            list.add(new Message("SomeNewCode", text, Message.WARNING, this, PROPERTY_INVERSE_RELATION)); //$NON-NLS-1$
            return;
        }
        IRelation reverseRelationObj = findInverseRelation();
        if (reverseRelationObj==null) {
            String text = NLS.bind(Messages.Relation_msgRelationNotInTarget, inverseRelation, target);
            list.add(new Message(MSGCODE_REVERSERELATION_NOT_IN_TARGET, text, Message.ERROR, this, PROPERTY_INVERSE_RELATION)); //$NON-NLS-1$
            return;
        }
        if (isAssoziation() && (!reverseRelationObj.getInverseRelation().equals(getName()))) {
            String text = Messages.Relation_msgReverseRelationNotSpecified;
            list.add(new Message(MSGCODE_REVERSE_RELATION_MISMATCH, text, Message.ERROR, this, PROPERTY_INVERSE_RELATION)); //$NON-NLS-1$
        }

        if (isReadOnlyContainer()!=reverseRelationObj.isReadOnlyContainer()) {
            String text = Messages.Relation_msgReverseRelOfContainerRelMustBeContainerRelToo;
            list.add(new Message(MSGCODE_FORWARD_AND_REVERSE_RELATION_MUST_BOTH_BE_MARKED_AS_CONTAINER, text, Message.ERROR, this, PROPERTY_INVERSE_RELATION)); //$NON-NLS-1$
        }
        
        if((type.isCompositionMasterToDetail() && !reverseRelationObj.getRelationType().isCompositionDetailToMaster())
                || (reverseRelationObj.getRelationType().isCompositionMasterToDetail() && !type.isCompositionDetailToMaster())) {
	            String text = Messages.Relation_msgReverseCompositionMissmatch;
	            list.add(new Message(MSGCODE_REVERSE_COMPOSITION_MISSMATCH, text, Message.ERROR, this, new String[]{PROPERTY_INVERSE_RELATION, PROPERTY_READONLY_CONTAINER})); //$NON-NLS-1$
	    }
	    if  ((type.isAssoziation() && !reverseRelationObj.getRelationType().isAssoziation())
	            || (reverseRelationObj.getRelationType().isAssoziation() && !type.isAssoziation())) {
	            String text = Messages.Relation_msgReverseAssociationMissmatch;
	            list.add(new Message(MSGCODE_REVERSE_ASSOCIATION_MISSMATCH, text, Message.ERROR, this, new String[]{PROPERTY_INVERSE_RELATION})); //$NON-NLS-1$
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public IRelation[] findContainerRelationCandidates() throws CoreException {
        List containerRelationCandidates = new ArrayList();
        IPolicyCmptType targetPolicyCmptType = findTarget();
        if (targetPolicyCmptType != null){
            IPolicyCmptType type = getPolicyCmptType();
            IPolicyCmptType[] supertypes = type.getSupertypeHierarchy().getAllSupertypesInclSelf(type);
            // search for relations inside each policy cmpt inside the supertype hierarchy
            for (int i = 0; i < supertypes.length; i++) {
                // check all relations of the policy cmpt type
                IRelation[] relations = supertypes[i].getRelations();
                for (int j = 0; j < relations.length; j++) {
                    if (!relations[j].isReadOnlyContainer())
                        continue;
                    
                    IPolicyCmptType targetOfContainerRelation = relations[j].findTarget();
                    if (targetOfContainerRelation == null)
                        continue;
                    
                    // check if the target of the container relation is the same policy cmpt type
                    // the relation uses as target
                    if (targetOfContainerRelation.equals(targetPolicyCmptType)) {
                        // candidate found: the target of the container relation is equal the
                        // target of this relation
                        containerRelationCandidates.add(relations[j]);
                        continue;
                    }
                    
                    // check if the target of the container relation is a subertype of the
                    // target of this relation
                    ITypeHierarchy hierarchyOfTarget = targetPolicyCmptType.getSupertypeHierarchy();
                    if (hierarchyOfTarget.isSupertypeOf(targetOfContainerRelation, targetPolicyCmptType)){
                        // candidate found: the target of the container relation is a supertype of the
                        // target of this relation
                        containerRelationCandidates.add(relations[j]);
                        continue;
                    }
                }
            }
        }
        return (IRelation[]) containerRelationCandidates.toArray(new IRelation[0]);
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
        type = RelationType.getRelationType(element.getAttribute(PROPERTY_RELATIONTYPE));
        if (type==null) {
            type = IRelation.DEFAULT_RELATION_TYPE;
        }
        readOnlyContainer = Boolean.valueOf(element.getAttribute(PROPERTY_READONLY_CONTAINER)).booleanValue();
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
        containerRelation = element.getAttribute(PROPERTY_CONTAINER_RELATION);
        if (element.hasAttribute(PROPERTY_INVERSE_RELATION)) {
            inverseRelation = element.getAttribute(PROPERTY_INVERSE_RELATION);
        } else {
            // prior to version 1.0.4 the inverse relation was named reverse relation
            // this code does a one-the-fly migration for old xml.
            inverseRelation = element.getAttribute("reverseRelation");
        }
        if (isCompositionDetailToMaster()) {
            inverseRelation = ""; //$NON-NLS-1$
        }
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
        targetRoleSingularProductSide = element.getAttribute(PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE);
        targetRolePluralProductSide = element.getAttribute(PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE);
        try {
            minCardinalityProductSide = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY_PRODUCTSIDE));
        } catch (NumberFormatException e) {
        	minCardinalityProductSide = 0;
        }
        String maxPS = element.getAttribute(PROPERTY_MAX_CARDINALITY_PRODUCTSIDE);
        if (maxPS.equals("*")) { //$NON-NLS-1$
        	maxCardinalityProductSide = CARDINALITY_MANY;
        }
        else {
        	try {
        		maxCardinalityProductSide = Integer.parseInt(maxPS);
        	} catch (NumberFormatException e) {
        		maxCardinalityProductSide = 0;
        	}
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_RELATIONTYPE, type.getId());
        newElement.setAttribute(PROPERTY_READONLY_CONTAINER, "" + readOnlyContainer); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_TARGET, target);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR, targetRoleSingular);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL, targetRolePlural);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$
        
        if (maxCardinality == CARDINALITY_MANY) {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
        }
        else {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
        }
        
        newElement.setAttribute(PROPERTY_CONTAINER_RELATION, containerRelation);
        newElement.setAttribute(PROPERTY_INVERSE_RELATION, inverseRelation);
        newElement.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant); //$NON-NLS-1$
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE, targetRoleSingularProductSide);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE, targetRolePluralProductSide);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY_PRODUCTSIDE, "" + minCardinalityProductSide); //$NON-NLS-1$
        
        if (maxCardinalityProductSide == CARDINALITY_MANY) {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY_PRODUCTSIDE, "*"); //$NON-NLS-1$
        }
        else {
            newElement.setAttribute(PROPERTY_MAX_CARDINALITY_PRODUCTSIDE, "" + maxCardinalityProductSide); //$NON-NLS-1$
        }
    }
    
    private class CheckForDuplicateRoleNameVisitor extends PolicyCmptTypeHierarchyVisitor {

        private MessageList list;

        public CheckForDuplicateRoleNameVisitor(MessageList list) {
            super();
            this.list = list;
        }
        
        /**
         * {@inheritDoc}
         * @throws CoreException 
         */
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            int numOfMsgs = list.getNoOfMessages();
            IRelation[] relations = currentType.getRelations();
            for (int j = 0; j < relations.length; j++) {
                if (relations[j]==Relation.this) {
                    continue;
                }
                if (!StringUtils.isEmpty(Relation.this.targetRoleSingular) && relations[j].getTargetRoleSingular().equals(targetRoleSingular)) {
                    String text = Messages.Relation_msgSameSingularRoleName;
                    list.add(new Message(MSGCODE_SAME_SINGULAR_ROLENAME, text, Message.ERROR, Relation.this, PROPERTY_TARGET_ROLE_SINGULAR));
                }
                if (!StringUtils.isEmpty(Relation.this.targetRolePlural) && relations[j].getTargetRolePlural().equals(targetRolePlural))  {
                    String text = Messages.Relation_msgSamePluralRolename;
                    list.add(new Message(MSGCODE_SAME_PLURAL_ROLENAME, text, Message.ERROR, Relation.this, PROPERTY_TARGET_ROLE_PLURAL)); //$NON-NLS-1$
                }
                
                if (!Relation.this.isProductRelevant()) {
                    continue;
                }
                
                if (!StringUtils.isEmpty(Relation.this.targetRoleSingularProductSide) && relations[j].getTargetRoleSingularProductSide().equals(targetRoleSingularProductSide)) {
                    String text = Messages.Relation_msgSameSingularRoleName;
                    list.add(new Message(MSGCODE_SAME_SINGULAR_ROLENAME_PRODUCTSIDE, text, Message.ERROR, Relation.this, PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE));
                }
                if (!StringUtils.isEmpty(Relation.this.targetRolePluralProductSide) && relations[j].getTargetRolePluralProductSide().equals(targetRolePluralProductSide)) {
                    String text = Messages.Relation_msgSameSingularRoleName;
                    list.add(new Message(MSGCODE_SAME_PLURAL_ROLENAME_PRODUCTSIDE, text, Message.ERROR, Relation.this, PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE));
                }
            }
            return list.getNoOfMessages()==numOfMsgs; // no new message added, continue visting/validating
        }

    }

}
