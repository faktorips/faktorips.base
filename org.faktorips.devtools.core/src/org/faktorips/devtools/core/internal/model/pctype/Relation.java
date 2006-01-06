package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
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
    
    private final static String[] JAVA_METHOD_NAMES;
    
    static {
        JAVA_METHOD_NAMES = new String[IRelation.JAVA_NUMOF_METHOD+1];
        // 0 remains empty
        JAVA_METHOD_NAMES[IRelation.JAVA_GETTER_METHOD] = "get";
        JAVA_METHOD_NAMES[IRelation.JAVA_SETTER_METHOD] = "set";
        JAVA_METHOD_NAMES[IRelation.JAVA_ADD_METHOD] = "add";
        JAVA_METHOD_NAMES[IRelation.JAVA_REMOVE_METHOD] = "remove";
        JAVA_METHOD_NAMES[IRelation.JAVA_GETALL_METHOD] = "get";
        JAVA_METHOD_NAMES[IRelation.JAVA_NUMOF_METHOD] = "getAnzahl";
    }

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

    public Relation(IPolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public Relation() {
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getPolicyCmptType()
     */
    public IPolicyCmptType getPolicyCmptType() {
        return (PolicyCmptType)getIpsObject();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName() {
        return targetRoleSingular;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((PolicyCmptType)getIpsObject()).removeRelation(this);
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getRelationType()
     */
    public RelationType getRelationType() {
        return type;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setRelationType(int)
     */
    public void setRelationType(RelationType newType) {
        RelationType oldType = type;
        type = newType;
        valueChanged(oldType, newType);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#isReadOnlyContainer()
     */
    public boolean isReadOnlyContainer() {
        return readOnlyContainer;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setReadOnlyContainer(boolean)
     */
    public void setReadOnlyContainer(boolean flag) {
        boolean oldValue = readOnlyContainer;
        this.readOnlyContainer = flag;
        valueChanged(oldValue, readOnlyContainer);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getTarget()
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#findTarget()
     */
    public IPolicyCmptType findTarget() throws CoreException {
        if (StringUtils.isEmpty(target)) {
            return null;
        }
        return getIpsProject().findPolicyCmptType(target);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setTarget(java.lang.String)
     */
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, newTarget);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getTargetRoleSingular()
     */
    public String getTargetRoleSingular() {
        return targetRoleSingular;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setTargetRoleSingular(java.lang.String)
     */
    public void setTargetRoleSingular(String newRole) {
        String oldRole = targetRoleSingular;
        targetRoleSingular = newRole;
        valueChanged(oldRole, newRole);
    }
    

    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getTargetRolePlural()
     */
    public String getTargetRolePlural() {
        return targetRolePlural;
    }
    
    /**
     * Overridden IMethod.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setTargetRolePlural(java.lang.String)
     */
    public void setTargetRolePlural(String newRole) {
        String oldRole = targetRolePlural;
        targetRolePlural = newRole;
        valueChanged(oldRole, newRole);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getMinCardinality()
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setMinCardinality(int)
     */
    public void setMinCardinality(int newValue) {
        int oldValue = minCardinality;
        minCardinality = newValue;
        valueChanged(oldValue, newValue);
        
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getMaxCardinality()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#is1ToMany()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setMaxCardinality(java.lang.String)
     */ 
    public void setMaxCardinality(String newValue) {
        String oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#isProductRelevant()
     */
    public boolean isProductRelevant() {
        return productRelevant;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setProductRelevant(boolean)
     */
    public void setProductRelevant(boolean newValue) {
        boolean oldValue = productRelevant;
        productRelevant = newValue;
        valueChanged(oldValue, newValue);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getContainerRelation()
     */
    public String getContainerRelation() {
        return containerRelation;
    }
    
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#hasContainerRelation()
     */
    public boolean hasContainerRelation() {
        return !StringUtils.isEmpty(containerRelation);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#findContainerRelation()
     */
    public IRelation findContainerRelation() throws CoreException {
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setContainerRelation(java.lang.String)
     */
    public void setContainerRelation(String newRelation) {
        String oldValue = containerRelation;
        containerRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getReverseRelation()
     */
    public String getReverseRelation() {
        return reverseRelation;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#setReverseRelation(java.lang.String)
     */
    public void setReverseRelation(String newRelation) {
        String oldValue = this.reverseRelation;
        this.reverseRelation = newRelation;
        valueChanged(oldValue, newRelation);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#findReverseRelation()
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        if (this.type==RelationType.COMPOSITION) {
            return IpsPlugin.getDefault().getImage("Aggregation.gif");
        }
        return IpsPlugin.getDefault().getImage("Relation.gif");
    }

    /** 
     * Overridden method.
     * @throws CoreException
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPart#validate(org.faktorips.util.message.MessageList)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getJavaMethod(int)
     */
    public IMethod getJavaMethod(int type) throws CoreException {
        int methodType = type & 255;
        int javaType = type >> 8;
        if (methodType<0 || methodType>=(JAVA_METHOD_NAMES.length)) {
            throw new IllegalArgumentException("Unkown type " + type);
        }
        String methodName;
        if (methodType==IRelation.JAVA_GETALL_METHOD || methodType==IRelation.JAVA_NUMOF_METHOD) {
            methodName = JAVA_METHOD_NAMES[methodType] + StringUtils.capitalise(targetRolePlural);
        } else {
            methodName = JAVA_METHOD_NAMES[methodType] + StringUtils.capitalise(targetRoleSingular);
        }
        if (javaType==IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE 
                || javaType==IPolicyCmptType.JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE) {
            methodName = methodName + "Pk";
        }            
        String[] paramTypeSignature;
        if (methodType==JAVA_GETTER_METHOD 
            || methodType==JAVA_GETALL_METHOD 
            || methodType==JAVA_NUMOF_METHOD) {
            paramTypeSignature = new String[0];
            if (is1ToMany() && (type==JAVA_PRODUCTCMPT_GETTER_METHOD_IMPLEMENTATION || type==JAVA_PRODUCTCMPT_GETTER_METHOD_INTERFACE)) {
                String param = Signature.createTypeSignature(String.class.getName(), false);
                paramTypeSignature = new String[] {param};
            } else {}
        } else {
            IPolicyCmptType targetType = getIpsProject().findPolicyCmptType(target);
            String paramTypeName = targetType.getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE).getElementName();
            String param = Signature.createTypeSignature(paramTypeName, false);
            paramTypeSignature = new String[] {param};
        }
        IType javaTypeObj = getPolicyCmptType().getJavaType(javaType);
        return javaTypeObj.getMethod(methodName, paramTypeSignature);        
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IRelation#getJavaField(int)
     */
    public IField getJavaField(int type) throws CoreException {
        IType javaType;
        if (type==JAVA_PCTYPE_FIELD) {
            javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE);
        } else if (type==JAVA_PRODUCTCMPT_FIELD) {
            javaType = getPolicyCmptType().getJavaType(IPolicyCmptType.JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE);
        } else {
            throw new IllegalArgumentException("Unkown type " + type);
        }
        String name;
        if (is1ToMany()) {
            name = StringUtils.uncapitalise(targetRolePlural);
            if (type==JAVA_PRODUCTCMPT_FIELD) {
                name = name = name + "Pks";
            } else {}
        } else {
            name = StringUtils.uncapitalise(targetRoleSingular);
            if (type==JAVA_PRODUCTCMPT_FIELD) {
                name = name = name + "Pk";
            } else {}            
        }
        return javaType.getField(name);        
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.IRelation#findContainerRelationOfTypeReverseComposition()
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        type = RelationType.getRelationType(element.getAttribute(PROPERTY_RELATIONTYPE));
        readOnlyContainer = Boolean.valueOf(element.getAttribute(PROPERTY_READONLY_CONTAINER)).booleanValue();
        target = element.getAttribute(PROPERTY_TARGET);
        targetRoleSingular = element.getAttribute(PROPERTY_TARGET_ROLE_SINGULAR);
        targetRolePlural = element.getAttribute(PROPERTY_TARGET_ROLE_PLURAL);
        minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        maxCardinality = element.getAttribute(PROPERTY_MAX_CARDINALITY);
        containerRelation = element.getAttribute(PROPERTY_CONTAINER_RELATION);
        reverseRelation = element.getAttribute(PROPERTY_REVERSE_RELATION);
        productRelevant = Boolean.valueOf(element.getAttribute(PROPERTY_PRODUCT_RELEVANT)).booleanValue();
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
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
    }
    
}
