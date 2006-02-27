package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
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

    final static String TAG_NAME = "Relation"; //$NON-NLS-1$
    
    private String productCmptTypeRelation = ""; //$NON-NLS-1$
    private String target = ""; //$NON-NLS-1$
    private int minCardinality = 0;
    private String maxCardinality = "1"; //$NON-NLS-1$

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
    	if (deleted) {
    		return;
    	}
        ((ProductCmptGeneration)getParent()).removeRelation(this);
        parent = null;
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    public String getName() {
        return target;
    }

    /** 
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ProductCmptRelation.gif"); //$NON-NLS-1$
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
    	IRelation relType = null;
        if (relation==null) {
            String text = NLS.bind(Messages.ProductCmptRelation_msgNoRelationDefined, productCmptTypeRelation, getProductCmpt().getPolicyCmptType());
            list.add(new Message(MSGCODE_UNKNWON_RELATIONTYPE, text, Message.ERROR, this, PROPERTY_PCTYPE_RELATION));
        }
        else {
        	relType = relation.findPolicyCmptTypeRelation();
        }
        ValidationUtils.checkIpsObjectReference(target, IpsObjectType.PRODUCT_CMPT, true, "target", this, PROPERTY_TARGET, MSGCODE_UNKNWON_TARGET, list); //$NON-NLS-1$
        if (ValidationUtils.checkStringPropertyNotEmpty(maxCardinality, "maximum cardinality", this, PROPERTY_MAX_CARDINALITY, MSGCODE_MISSING_MAX_CARDINALITY,  list)) { //$NON-NLS-1$
            int max = -1;
            if (maxCardinality.trim().equals("*")) { //$NON-NLS-1$
                max = Integer.MAX_VALUE;
            } else {
                try {
                    max = Integer.parseInt(maxCardinality);
                } catch (NumberFormatException e) {
                    String text = Messages.ProductCmptRelation_msgMalformedMaxCardinality;
                    list.add(new Message("", text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY)); //$NON-NLS-1$
                }
            }
            if (max==0) {
                String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThan1;
                list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1, text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY));
            } else if (max!=-1) {
                if (minCardinality > max) {
                    String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThanMin;
                    list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN, text, Message.ERROR, this, new String[]{PROPERTY_MIN_CARDINALITY, PROPERTY_MAX_CARDINALITY}));
                }
                if (relType != null && !relType.getMaxCardinality().equals("*")) { //$NON-NLS-1$
                    try {
						int maxType = Integer.parseInt(relType.getMaxCardinality());
						if (max > maxType) {
							String text = NLS.bind(Messages.ProductCmptRelation_msgMaxCardinalityExceedsModelMax, maxCardinality, relType.getMaxCardinality());
							list.add(new Message(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, text, Message.ERROR, this, PROPERTY_MAX_CARDINALITY));
						}
					} catch (NumberFormatException e) {
						// ignore this problem in the model
					}
                }
            }
        }
        if (relType != null) {
            if (minCardinality < relType.getMinCardinality()) {
            	Integer min = new Integer(minCardinality);
            	Integer modelMin = new Integer(relType.getMinCardinality());
            	String text = NLS.bind(Messages.ProductCmptRelation_msgMinCardinalityIsLessThanModelMin, min, modelMin);
            	list.add(new Message(MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_MODEL_MIN, text, Message.ERROR, this, PROPERTY_MIN_CARDINALITY));
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
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
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
        element.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$
        element.setAttribute(PROPERTY_MAX_CARDINALITY, maxCardinality);
    }

    /**
     * {@inheritDoc}
     */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}
