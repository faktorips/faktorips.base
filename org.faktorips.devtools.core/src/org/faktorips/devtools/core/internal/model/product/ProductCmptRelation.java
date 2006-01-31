package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeRelation;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptRelation extends IpsObjectPart implements IProductCmptRelation{

    final static String TAG_NAME = "Relation";
    
    private String productCmptTypeRelation = "";
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
     * Overridden.
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent().getParent();
    }
    
    /**
     * Overridden.
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }
    
    /** 
     * Overridden.

     */
    public void delete() {
        ((ProductCmptGeneration)getParent()).removeRelation(this);
        parent = null;
    }

    public String getName() {
        return target;
    }

    /** 
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ProductCmptRelation.gif");
    }

    /**
     * {@inheritDoc}
     */
    public String getProductCmptTypeRelation() {
    	return productCmptTypeRelation;
	}
    
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelation findProductCmptTypeRelation() throws CoreException {
		IProductCmptType productCmptType = getProductCmpt().findProductCmptType();
		if (productCmptType==null) {
			return null;
		}
		return productCmptType.getRelation(productCmptTypeRelation);
	}

	void setProductCmptTypeRelation(String newRelation) {
        productCmptTypeRelation = newRelation;
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
    public void setTarget(String newTarget) {
        String oldTarget = target;
        target = newTarget;
        valueChanged(oldTarget, target);
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
     * Overridden.
     */ 
    public void setMaxCardinality(String newValue) {
        String oldValue = maxCardinality;
        maxCardinality = newValue;
        valueChanged(oldValue, newValue);
    }

    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        IProductCmptTypeRelation relation = findProductCmptTypeRelation();
        if (relation==null) {
            String text = "There is no relation " + productCmptTypeRelation + " defined in " + getProductCmpt().getPolicyCmptType() + ".";
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
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    
    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        productCmptTypeRelation = element.getAttribute(PROPERTY_PCTYPE_RELATION);
        target = element.getAttribute(PROPERTY_TARGET);
        minCardinality = Integer.parseInt(element.getAttribute(PROPERTY_MIN_CARDINALITY));
        maxCardinality = element.getAttribute(PROPERTY_MAX_CARDINALITY);
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PCTYPE_RELATION, productCmptTypeRelation);
        element.setAttribute(PROPERTY_TARGET, target);
        element.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality);
        element.setAttribute(PROPERTY_MAX_CARDINALITY, maxCardinality);
    }
    
}
