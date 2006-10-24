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


package org.faktorips.devtools.core.internal.model.product;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptKind;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
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
    
    private String policyCmptType = ""; //$NON-NLS-1$
    private String runtimeId = ""; //$NON-NLS-1$

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
    }

    public ProductCmpt() {
        super();
    }

    /** 
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }
    
    /**
	 * {@inheritDoc}
	 */
	public IProductCmptKind findProductCmptKind() throws CoreException {
		IProductCmptNamingStrategy stratgey = getIpsProject().getProductCmptNamingStrategy();
		String kindName = stratgey.getKindId(getName());
		return new ProductCmptKind(kindName, getIpsProject().getRuntimeIdPrefix() + kindName);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionId() throws CoreException {
		try {
			return getIpsProject().getProductCmptNamingStrategy().getVersionId(getName());
		} catch (IllegalArgumentException e) {
			throw new CoreException(new IpsStatus("Can't get version id for " + this, e)); //$NON-NLS-1$
		}
	}

	/** 
	 * {@inheritDoc}
	 */
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    /** 
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IPolicyCmptType type = findPolicyCmptType();
        if (type == null) {
            String text = NLS.bind(Messages.ProductCmpt_msgUnknownTemplate, this.policyCmptType);
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_POLICY_CMPT_TYPE)); //$NON-NLS-1$
        } else {
        	IProductCmptType pType = findProductCmptType();
        	try {
				MessageList list3 = type.validate();
				if (list3.getMessageByCode(IPolicyCmptType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY) != null || 
				    list3.getMessageByCode(IPolicyCmptType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY) != null) {
					String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, this.getPolicyCmptType());
					list.add(new Message(MSGCODE_INCONSISTENCY_IN_POLICY_CMPT_TYPE_HIERARCHY, msg, Message.ERROR, pType, IProductCmptType.PROPERTY_NAME));
				}
			} catch (Exception e) {
				throw new CoreException(new IpsStatus("Error during validate of policy component type", e)); //$NON-NLS-1$
			}
        }
        MessageList list2 = getIpsProject().getProductCmptNamingStrategy().validate(getName());
        list.add(list2);
        
        list2 = getIpsModel().checkForDuplicateRuntimeIds(new IProductCmpt[] {this});
        list.add(list2);
        
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
     * {@inheritDoc}
     */
    public boolean containsFormulaTest() {
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i=0; i<generations.length; i++) {
            if (((ProductCmptGeneration)generations[i]).containsFormula()) {
                IConfigElement[] cfs = ((ProductCmptGeneration)generations[i]).getConfigElements();
                for (int j = 0; j < cfs.length; j++) {
                    if (cfs[j].getFormulaTestCases().length > 0){
                        return true;
                    }
                }
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
        Set qaTypes = new HashSet();
        qaTypes.add(new QualifiedNameType(policyCmptType, IpsObjectType.POLICY_CMPT_TYPE));
        
    	IPolicyCmptType pcType = findPolicyCmptType();
    	if (pcType!=null) {
            qaTypes.addAll(Arrays.asList(((PolicyCmptType)pcType).dependsOn(true)));
    	}        
        return (QualifiedNameType[])qaTypes.toArray(new QualifiedNameType[qaTypes.size()]);
    }

    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_POLICY_CMPT_TYPE, policyCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
    }

    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        policyCmptType = element.getAttribute(PROPERTY_POLICY_CMPT_TYPE);
        runtimeId = element.getAttribute(PROPERTY_RUNTIME_ID);
    }

	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptStructure getStructure() throws CycleException {
		return new ProductCmptStructure(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptStructure getStructure(GregorianCalendar date) throws CycleException {
		return new ProductCmptStructure(this, date);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeId() {
		return runtimeId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRuntimeId(String runtimeId) {
        String oldId = this.runtimeId;
        this.runtimeId = runtimeId;
        valueChanged(oldId, runtimeId);
	}
}
