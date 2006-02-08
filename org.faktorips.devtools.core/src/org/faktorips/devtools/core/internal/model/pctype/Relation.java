package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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

    final static String TAG_NAME = "Relation";

    private RelationType type = RelationType.ASSOZIATION;
    private String target = "";
    private String targetRoleSingular = "";
    private String targetRolePlural = "";
    private int minCardinality = 0;
    private String maxCardinality = "1";
    private boolean productRelevant = true;
    private String containerRelation = "";
    private String reverseRelation = "";
    private boolean readOnlyContainer = false;
    private String targetRoleSingularProductSide = "";
    private String targetRolePluralProductSide = "";
    private int minCardinalityProductSide = 0;
    private String maxCardinalityProductSide = "1";

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
    public String getMaxCardinality() {
        return maxCardinality;
    }
    
    /**
     * Returns the maximum cardinality as <code>Integer</code>. If the String value of max cardinality can't be parsed
     * to an int, <code>null</code> is returned. Returns <code>Integer.MAXVALUE</code> if max cardinality
     * equals <code>*</code>.
     * @return
     */
    public Integer getMaxCardinalityAsInteger() {
        if ("*".equals(maxCardinality.trim())) {
            return new Integer(Integer.MAX_VALUE);
        }
        try {
            return Integer.valueOf(maxCardinality);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Overridden.
     */
    public boolean is1ToMany() {
        if (maxCardinality.equals("*")) {
            return true;
        }
        try {
            int max = Integer.parseInt(maxCardinality);
            return max>1; 
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Overridden.
     */ 
    public void setMaxCardinality(String newValue) {
        String oldValue = maxCardinality;
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
	public String getMaxCardinalityProductSide() {
		return maxCardinalityProductSide;
	}

	/**
	 * Overridden.
	 */
	public void setMaxCardinalityProductSide(String newMax) {
		String oldMax = maxCardinalityProductSide;
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
    
    /** 
     * Overridden.
     */
    public Image getImage() {
        if (this.type==RelationType.COMPOSITION) {
            return IpsPlugin.getDefault().getImage("Aggregation.gif");
        }
        return IpsPlugin.getDefault().getImage("Relation.gif");
    }

    /** 
     * Overridden.
     */
    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.POLICY_CMPT_TYPE, true, "target", this, PROPERTY_TARGET, list);
        ValidationUtils.checkStringPropertyNotEmpty(targetRoleSingular, "target role", this, PROPERTY_TARGET_ROLE_SINGULAR, list);
        if (ValidationUtils.checkStringPropertyNotEmpty(maxCardinality, "maximum cardinality", this, PROPERTY_MAX_CARDINALITY, list)) {
            int max = -1;
            if (maxCardinality.trim().equals("*")) {
                max = Integer.MAX_VALUE;
            } else {
                try {
                    max = Integer.parseInt(maxCardinality);
                } catch (NumberFormatException e) {
                    String text = "Max cardinality must be either a number or an asterix (*).";
                    list.add(new Message("", text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY));
                }
            }
            if (max==0) {
                String text = "Maximum cardinality must be at least 1.";
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY));
            } else if (max==1 && isReadOnlyContainer() && getRelationType() != RelationType.REVERSE_COMPOSITION) {
                String text = "Maximum cardinality for a container relation must be greater than 1 (otherwise it is not a container).";
                list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_READONLY_CONTAINER, PROPERTY_MAX_CARDINALITY}));
            } else if (max!=-1) {
                if (minCardinality > max) {
                    String text = "Minimum cardinality is greater than maximum cardinality.";
                    list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY}));
                }
            }
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
            String text = "The container relation " + containerRelation + " does not exist in the super type hierarchy";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));
            return;
        }
        if (!relation.isReadOnlyContainer()) {
            String text = "The relation is not marked as a container relation.";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));
            return;
        }
        IPolicyCmptType superRelationTarget = getIpsProject().findPolicyCmptType(relation.getTarget());
        if (superRelationTarget==null) {
            String text = "The target role of the specified container relation is empty or the target does not exists!";
            list.add(new Message("", text, Message.WARNING, this, PROPERTY_CONTAINER_RELATION));
            return;
        }
        IPolicyCmptType pcType = getIpsProject().findPolicyCmptType(target);
        if (pcType!=null) {
            ITypeHierarchy hierachy = pcType.getSupertypeHierarchy();
            if (!superRelationTarget.equals(pcType) && !hierachy.isSupertypeOf(superRelationTarget, pcType)) {
                String text = "The target class of this relation is not a subclass (or the same class) of the container relation's target.";
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));    
            }
        }
        IRelation reverseRel = findContainerRelationOfTypeReverseComposition();
        if(reverseRel != null && reverseRel != relation)  {
            String text = "Container relation is not the reverse relation of the container relation of the reverse relation.";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));
            return;
        }
        if (relation.getTargetRolePlural().equals(getTargetRolePlural()))  {
            String text = "IRelation has same plural rolename as the container relation.";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));
            return;
        }
        if (relation.getTargetRoleSingular().equals(getTargetRoleSingular()))  {
            String text = "IRelation has same singular rolename as the container relation.";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_CONTAINER_RELATION));
            return;
        }
    }
    
    private void validateReverseRelation(MessageList list) throws CoreException {
        if (StringUtils.isEmpty(reverseRelation)) {
            return;
        }
        IRelation reverseRelationObj = findReverseRelation();
        if (reverseRelationObj==null) {
            String text = "The relation " + reverseRelation + " does not exist in the target " + target;
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_REVERSE_RELATION));
            return;
        }
        if (!reverseRelationObj.getReverseRelation().equals(getName())) {
            String text = "The reverse relation does not specify this relation as it's reverse one!";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_REVERSE_RELATION));
        }
        if (isReadOnlyContainer() && ! reverseRelationObj.isReadOnlyContainer()) {
            String text = "The reverse relation of a container relation must be a container relation too!";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_REVERSE_RELATION));
        }
        
        if((type.isComposition() && !reverseRelationObj.getRelationType().isReverseComposition())
                || (reverseRelationObj.getRelationType().isComposition() && !type.isReverseComposition())) {
	            String text = "The reverse relation of a composition must be a reverse composition!";
	            list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_REVERSE_RELATION, PROPERTY_READONLY_CONTAINER}));
	    }
	    if  ((type.isAssoziation() && !reverseRelationObj.getRelationType().isAssoziation())
	            || (reverseRelationObj.getRelationType().isAssoziation() && !type.isAssoziation())) {
	            String text = "The reverse relation of an association must be an assoziation!";
	            list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_REVERSE_RELATION}));
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
    protected void initPropertiesFromXml(Element element, int id) {
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
        maxCardinality = element.getAttribute(PROPERTY_MAX_CARDINALITY);
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
        maxCardinalityProductSide = element.getAttribute(PROPERTY_MAX_CARDINALITY_PRODUCTSIDE);
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_RELATIONTYPE, type.getId());
        newElement.setAttribute(PROPERTY_READONLY_CONTAINER, "" + readOnlyContainer);
        newElement.setAttribute(PROPERTY_TARGET, target);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR, targetRoleSingular);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL, targetRolePlural);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality);
        newElement.setAttribute(PROPERTY_MAX_CARDINALITY, maxCardinality);
        newElement.setAttribute(PROPERTY_CONTAINER_RELATION, containerRelation);
        newElement.setAttribute(PROPERTY_REVERSE_RELATION, reverseRelation);
        newElement.setAttribute(PROPERTY_PRODUCT_RELEVANT, "" + productRelevant);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_SINGULAR_PRODUCTSIDE, targetRoleSingularProductSide);
        newElement.setAttribute(PROPERTY_TARGET_ROLE_PLURAL_PRODUCTSIDE, targetRolePluralProductSide);
        newElement.setAttribute(PROPERTY_MIN_CARDINALITY_PRODUCTSIDE, "" + minCardinalityProductSide);
        newElement.setAttribute(PROPERTY_MAX_CARDINALITY_PRODUCTSIDE, maxCardinalityProductSide);
    }
    
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType);
	}
}
