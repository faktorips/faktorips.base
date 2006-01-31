
package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.TimedIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;


/**
 * Implementation of product component.
 * 
 * @author Jan Ortmann
 */
public class ProductCmpt extends TimedIpsObject implements IProductCmpt {
    
    private String policyCmptType = "";

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
    }

    public ProductCmpt() {
        super();
    }

    /** 
     * Overridden.
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

    /** 
     * Overridden.
     */
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    /** 
     * Overridden.
     */
    public void setPolicyCmptType(String newPcType) {
        String oldType = policyCmptType;
        policyCmptType = newPcType;
        valueChanged(oldType, policyCmptType);
    }

    /** 
     * Overridden.
     */
    public IPolicyCmptType findPolicyCmptType() throws CoreException {
        return getIpsProject().findPolicyCmptType(policyCmptType);
    }

    /**
	 * Overridden.
	 */
	public IProductCmptType findProductCmptType() throws CoreException {
		IPolicyCmptType policyCmptType = findPolicyCmptType();
		if (policyCmptType==null) {
			return null;
		}
		return policyCmptType.findProductCmptType();
	}

	/** 
     * Overridden.
     */
    protected IpsObjectGeneration createNewGeneration(int id) {
        return new ProductCmptGeneration(this, id);
    }

    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        if (findPolicyCmptType()==null) {
            String text = "The template " + this.policyCmptType + " does not exists";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE));
        }
    }
    
    /** 
     * Overridden.
     */
    public IRelation findPcTypeRelation(String relationName) throws CoreException {
        IPolicyCmptType pcType = findPolicyCmptType();
        if (pcType==null) {
            return null;
        }
        return pcType.getRelation(relationName);
    }
    
    /** 
     * Throws an IllegalArgumentException as no Java type is generated for the product component. Instead
     * Java types are generated for each generation.
     * 
     * Overridden.
     */
    public IType getJavaType(int kind) throws CoreException {
        throw new IllegalArgumentException("Unkown kind");
    }

    /** 
     * Overridden.
     */
    public IType[] getAllJavaTypes() throws CoreException {
        List types = new ArrayList();
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i=0; i<generations.length; i++) {
            IType[] genTypes = ((IProductCmptGeneration)generations[i]).getAllJavaTypes();
            for (int j=0; j<genTypes.length; j++) {
                types.add(genTypes[i]);
            }
        }
        return (IType[])types.toArray(new IType[types.size()]);
    }

    /**
     * Overridden.
     */
    public boolean containsFormula() {
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i=0; i<generations.length; i++) {
            if (((ProductCmptGeneration)generations[i]).containsFormula()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overridden.
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        
        if(StringUtils.isEmpty(policyCmptType)){
            return new QualifiedNameType[0];
        }
        return new QualifiedNameType[]{new QualifiedNameType(policyCmptType, IpsObjectType.POLICY_CMPT_TYPE)};
    }

    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_POLICY_CMPT_TYPE, policyCmptType);
    }

    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        policyCmptType = element.getAttribute(PROPERTY_POLICY_CMPT_TYPE);
    }
}
