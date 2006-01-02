package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptRelation extends IpsObjectPart implements IProductCmptRelation{

    final static String TAG_NAME = "Relation";
    
    private String pcTypeRelation = "";
    private String target = "";
    private int minCardinality = 0;
    private String maxCardinality = "1";

    public ProductCmptRelation(IProductCmptGeneration generation, int id) {
        super(generation, id);
    }

    ProductCmptRelation() {
        super();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IConfigElement#getProductCmpt()
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent().getParent();
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IConfigElement#getProductCmptGeneration()
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((ProductCmptGeneration)getParent()).removeRelation(this);
        parent = null;
    }

    public String getName() {
        return target;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ProductCmptRelation.gif");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptRelation#getPcTypeRelation()
     */
    public String getPcTypeRelation() {
        return pcTypeRelation;
    }
    
    void setPcTypeRelation(String newRelation) {
        pcTypeRelation = newRelation;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptRelation#getTarget()
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.product.IProductCmptRelation#setTarget(java.lang.String)
     */
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, target);
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
     * @see org.faktorips.devtools.core.model.product.IProductCmptRelation#findPcTypeRelation()
     */
    public IRelation findPcTypeRelation() throws CoreException {
        IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType();
        if (pcType==null) {
            return null;
        }
        return pcType.getRelation(this.pcTypeRelation);
    }
    
    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        IRelation relation = findPcTypeRelation();
        if (relation==null) {
            String text = "There is no relation " + pcTypeRelation + " defined in " + getProductCmpt().getPolicyCmptType() + ".";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_PCTYPE_RELATION));
        }
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.PRODUCT_CMPT, true, "target", this, PROPERTY_TARGET, list);
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
            } else if (max!=-1) {
                if (minCardinality > max) {
                    String text = "Minimum cardinality is greater than maximum cardinality.";
                    list.add(new Message("", text, Message.ERROR, this, new String[]{PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY}));
                }
            }
        }
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
        pcTypeRelation = element.getAttribute(PROPERTY_PCTYPE_RELATION);
        target = element.getAttribute(PROPERTY_TARGET);
        minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        maxCardinality = element.getAttribute(PROPERTY_MAX_CARDINALITY);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PCTYPE_RELATION, pcTypeRelation);
        element.setAttribute(PROPERTY_TARGET, target);
        element.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality);
        element.setAttribute(PROPERTY_MAX_CARDINALITY, maxCardinality);
    }
    
}
