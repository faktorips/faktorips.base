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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class Relation extends IpsObjectPart implements IRelation {

    final static String TAG_NAME = "Relation"; //$NON-NLS-1$

    private RelationType type = RelationType.ASSOZIATION;
    private String target = ""; //$NON-NLS-1$
    private String targetRoleSingular = ""; //$NON-NLS-1$
    private String targetRolePlural = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private int maxCardinality = 1; //$NON-NLS-1$
    private boolean productRelevant = true;
    private String containerRelation = ""; //$NON-NLS-1$
    private String reverseRelation = ""; //$NON-NLS-1$
    private boolean readOnlyContainer = false;
    private String targetRoleSingularProductSide = ""; //$NON-NLS-1$
    private String targetRolePluralProductSide = ""; //$NON-NLS-1$
    private int minCardinalityProductSide = 0;
    private int maxCardinalityProductSide = 1;

    public Relation(IPolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public Relation() {
    }

    /**
     * Overridden.
     */
    public IPolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }
    
    /** 
     * Overridden.
     */
    public String getName() {
        return targetRoleSingular;
    }
    
    /** 
     * Overridden.
     */
    public void delete() {
        ((PolicyCmptType)getIpsObject()).removeRelation(this);
        updateSrcFile();
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    /** 
     * Overridden.
     */
    public RelationType getRelationType() {
        return type;
    }

    /** 
     * Overridden.
     */
    public void setRelationType(RelationType newType) {
        RelationType oldType = type;
        type = newType;
        valueChanged(oldType, newType);
    }
    
    /**
     * Overridden.
     */
    public boolean isReadOnlyContainer() {
        return readOnlyContainer;
    }
    
    /**
     * Overridden.
     */
    public void setReadOnlyContainer(boolean flag) {
        boolean oldValue = readOnlyContainer;
        this.readOnlyContainer = flag;
        valueChanged(oldValue, readOnlyContainer);
    }
    
    /** 
     * Overridden.
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * Overridden.
     */
    public IPolicyCmptType findTarget() throws CoreException {
        if (StringUtils.isEmpty(target)) {
            return null;
        }
        return getIpsProject().findPolicyCmptType(target);
    }
    
    /** 
     * Overridden.
     */
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    /** 
     * Overridden.
     */
    public String getTargetRoleSingular() {
        return targetRoleSingular;
    }

    /** 
     * Overridden.
     */
    public void setTargetRoleSingular(String newRole) {
        String oldRole = targetRoleSingular;
        targetRoleSingular = newRole;
        valueChanged(oldRole, newRole);
    }
    

    /**
     * Overridden.
     */
    public String getTargetRolePlural() {
        return targetRolePlural;
    }
    
    /**
     * Overridden.
     */
    public void setTargetRolePlural(String newRole) {
        String oldRole = targetRolePlural;
        targetRolePlural = newRole;
        valueChanged(oldRole, newRole);
    }
    
    /** 
     * Overridden.
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /** 
     * Overridden.
     */
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);
        
    }

    /** 
     * Overridden.
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
     * Overridden.
     */ 
    public void setMaxCardinality(int newValue) {
        int oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * Overridden.
     */
    public boolean isProductRelevant() {
        return productRelevant;
    }

    /** 
     * Overridden.
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        valueChanged(oldValue, newValue);
    }
    
	/**
	 * Overridden.
	 */
	public String getTargetRoleSingularProductSide() {
		return targetRoleSingularProductSide;
	}

	/**
	 * Overridden.
	 */
	public void setTargetRoleSingularProductSide(String newRole) {
        String oldRole = targetRoleSingularProductSide;
        targetRoleSingularProductSide = newRole;
        valueChanged(oldRole, newRole);
	}

	/**
	 * Overridden.
	 */
	public void setTargetRolePluralProductSide(String newRole) {
        String oldRole = targetRolePluralProductSide;
        targetRolePluralProductSide = newRole;
        valueChanged(oldRole, newRole);
	}

    /**
	 * Overridden.
	 */
	public String getTargetRolePluralProductSide() {
		return targetRolePluralProductSide;
	}


	/**
	 * Overridden.
	 */
	public int getMinCardinalityProductSide() {
		return minCardinalityProductSide;
	}

	/**
	 * Overridden.
	 */
	public void setMinCardinalityProductSide(int newMin) {
		int oldMin = minCardinalityProductSide;
		minCardinalityProductSide = newMin;
		valueChanged(oldMin, newMin);
	}

	/**
	 * Overridden.
	 */
	public int getMaxCardinalityProductSide() {
		return maxCardinalityProductSide;
	}

	/**
	 * Overridden.
	 */
	public void setMaxCardinalityProductSide(int newMax) {
		int oldMax = maxCardinalityProductSide;
		maxCardinalityProductSide = newMax;
		valueChanged(oldMax, newMax);
	}

	/** 
     * Overridden.
     */
    public String getContainerRelation() {
        return containerRelation;
    }
    
    /**
     * Overridden.
     */
    public boolean hasContainerRelation() {
        return !StringUtils.isEmpty(containerRelation);
    }

    /**
     * Overridden.
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
	public boolean implementsContainerRelation() throws CoreException {
		return StringUtils.isNotEmpty(containerRelation);
	}

	/** 
     * Overridden.
     */
    public void setContainerRelation(String newRelation) {
        String oldValue = containerRelation;
        containerRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /**
     * Overridden.
     */
    public String getReverseRelation() {
        return reverseRelation;
    }
    
    /**
     * Overridden.
     */
    public void setReverseRelation(String newRelation) {
        String oldValue = this.reverseRelation;
        this.reverseRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /**
     * Overridden.
     */
    public IRelation findReverseRelation() throws CoreException {
        if ((type.isComposition() || type.isReverseComposition()) && implementsContainerRelation()) {
        	return findReverseRelationOfImplementationRelation();
        }
    	if (StringUtils.isEmpty(reverseRelation)) {
            return null;
        }
        IPolicyCmptType target = findTarget();
        if (target==null) {
            return null;
        }
        IRelation[] relations = target.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (relations[i].getName().equals(reverseRelation)) {
                return relations[i];
            }
        }
        return null;
    }
    
    private IRelation findReverseRelationOfImplementationRelation() throws CoreException {
        IRelation containerRel = findContainerRelation();
        if (containerRel==null) {
        	return null;
        }
        IRelation reverseContainerRel = containerRel.findReverseRelation();
        if (reverseContainerRel==null) {
        	return null;
        }
        IPolicyCmptType target = findTarget();
        if (target==null) {
            return null;
        }
        IRelation[] relations = target.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (relations[i].getTarget().equals(getIpsObject().getQualifiedName()) 
            		&& reverseContainerRel==relations[i].findContainerRelation()) {
                return relations[i];
            }
        }
        return null;
    }
    
    /** 
     * Overridden.
     */
    public Image getImage() {
        if (this.type==RelationType.COMPOSITION) {
            return IpsPlugin.getDefault().getImage("Aggregation.gif"); //$NON-NLS-1$
        }
        return IpsPlugin.getDefault().getImage("Relation.gif"); //$NON-NLS-1$
    }

    /** 
     * Overridden.
     */
    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.POLICY_CMPT_TYPE, true, "target", this,  //$NON-NLS-1$
        		PROPERTY_TARGET, MSGCODE_TARGET_DOES_NOT_EXIST, list); //$NON-NLS-1$
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, "target role", this, PROPERTY_TARGET_ROLE_SINGULAR,  //$NON-NLS-1$
        		MSGCODE_TARGET_ROLE_SINGULAR_MUST_BE_SET, list); //$NON-NLS-1$

        if (maxCardinality == 0) {
        	String text = Messages.Relation_msgMaxCardinalityMustBeAtLeast1;
        	list.add(new Message(MSGCODE_MAX_CARDINALITY_MUST_BE_1_FOR_REVERSE_COMPOSITION, text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY)); //$NON-NLS-1$
        } else if (maxCardinality == 1 && isReadOnlyContainer() && getRelationType() != RelationType.REVERSE_COMPOSITION) {
        	String text = Messages.Relation_msgMaxCardinalityForContainerRelationTooLow;
        	list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_READONLY_CONTAINER, PROPERTY_MAX_CARDINALITY})); //$NON-NLS-1$
        } else if (minCardinality > maxCardinality) {
        	String text = Messages.Relation_msgMinCardinalityGreaterThanMaxCardinality;
        	list.add(new Message(MSGCODE_MAX_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[]{PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY})); //$NON-NLS-1$
        }
        
        validateContainerRelation(list);
        validateReverseRelation(list);
    }
    
    private void validateContainerRelation(MessageList list) throws CoreException {
        if (StringUtils.isEmpty(containerRelation)) {
            return;
        }
        IRelation relation = findContainerRelation();
        if (relation==null) {
            String text = NLS.bind(Messages.Relation_msgContainerRelNotInSupertype, containerRelation);
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        if (!relation.isReadOnlyContainer()) {
            String text = Messages.Relation_msgNotMarkedAsContainerRel;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        IPolicyCmptType superRelationTarget = getIpsProject().findPolicyCmptType(relation.getTarget());
        if (superRelationTarget==null) {
            String text = Messages.Relation_msgNoTarget;
            list.add(new Message("", text, Message.WARNING, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        IPolicyCmptType pcType = getIpsProject().findPolicyCmptType(target);
        if (pcType!=null) {
            ITypeHierarchy hierachy = pcType.getSupertypeHierarchy();
            if (!superRelationTarget.equals(pcType) && !hierachy.isSupertypeOf(superRelationTarget, pcType)) {
                String text = Messages.Relation_msgTargetNotSubclass;
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));     //$NON-NLS-1$
            }
        }
        IRelation reverseRel = findContainerRelationOfTypeReverseComposition();
        if(reverseRel != null && reverseRel != relation)  {
            String text = Messages.Relation_msgContainerRelNotReverseRel;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        if (relation.getTargetRolePlural().equals(getTargetRolePlural()))  {
            String text = Messages.Relation_msgSamePluralRolename;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
        if (relation.getTargetRoleSingular().equals(getTargetRoleSingular()))  {
            String text = Messages.Relation_msgSameSingularRoleName;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION)); //$NON-NLS-1$
            return;
        }
    }
    
    private void validateReverseRelation(MessageList list) throws CoreException {
        if (StringUtils.isEmpty(reverseRelation)) {
            return;
        }
        IRelation reverseRelationObj = findReverseRelation();
        if (reverseRelationObj==null) {
            String text = NLS.bind(Messages.Relation_msgRelationNotInTarget, reverseRelation, target);
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_REVERSE_RELATION)); //$NON-NLS-1$
            return;
        }
        if (!reverseRelationObj.getReverseRelation().equals(getName())) {
            String text = Messages.Relation_msgReverseRelationNotSpecified;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_REVERSE_RELATION)); //$NON-NLS-1$
        }
        if (isReadOnlyContainer() && ! reverseRelationObj.isReadOnlyContainer()) {
            String text = Messages.Relation_msgReverseRelOfContainerRelMustBeContainerRelToo;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_REVERSE_RELATION)); //$NON-NLS-1$
        }
        
        if((type.isComposition() && !reverseRelationObj.getRelationType().isReverseComposition())
                || (reverseRelationObj.getRelationType().isComposition() && !type.isReverseComposition())) {
	            String text = Messages.Relation_msgReverseCompositionMissmatch;
	            list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_REVERSE_RELATION, PROPERTY_READONLY_CONTAINER})); //$NON-NLS-1$
	    }
	    if  ((type.isAssoziation() && !reverseRelationObj.getRelationType().isAssoziation())
	            || (reverseRelationObj.getRelationType().isAssoziation() && !type.isAssoziation())) {
	            String text = Messages.Relation_msgReverseAssociationMissmatch;
	            list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_REVERSE_RELATION})); //$NON-NLS-1$
        }
    }
    
    /**
     * Overridden.
     */
    public IRelation findContainerRelationOfTypeReverseComposition() throws CoreException {
        IRelation reverseRel = findReverseRelation();
        if(reverseRel != null) {
            IRelation containerRel = reverseRel.findContainerRelation();
            if(containerRel != null) {
                IRelation reverseContainerRel = containerRel.findReverseRelation();
                if(reverseContainerRel != null && 
                        reverseContainerRel.getRelationType() == RelationType.REVERSE_COMPOSITION) {
                    return reverseContainerRel;
                }
            }
        }
        return null;
    }

    /**
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        type = RelationType.getRelationType(element.getAttribute(PROPERTY_RELATIONTYPE));
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
        reverseRelation = element.getAttribute(PROPERTY_REVERSE_RELATION);
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
     * Overridden.
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
        newElement.setAttribute(PROPERTY_REVERSE_RELATION, reverseRelation);
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
    
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

}
