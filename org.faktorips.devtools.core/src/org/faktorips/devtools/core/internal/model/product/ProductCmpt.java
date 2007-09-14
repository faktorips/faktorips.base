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
import java.util.Iterator;
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
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptKind;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
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
    
    private String productCmptType = ""; //$NON-NLS-1$
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
    public IProductCmptGeneration getProductCmptGeneration(int index) {
        return (IProductCmptGeneration)getGeneration(index);
    }

    /**
	 * {@inheritDoc}
	 */
	public IProductCmptKind findProductCmptKind() throws CoreException {
        IProductCmptNamingStrategy stratgey = getIpsProject().getProductCmptNamingStrategy();
        try {
            String kindName = stratgey.getKindId(getName());
            return new ProductCmptKind(kindName, getIpsProject().getRuntimeIdPrefix() + kindName);
        } catch (Exception e) {
            return null; // error in parsing the name results in a "not found" for the client
        }
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
    public IPolicyCmptType findPolicyCmptType() throws CoreException {
        org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType productCmptType = findProductCmptType(getIpsProject());
        if (productCmptType==null) {
            return null;
        }
        return productCmptType.findPolicyCmptType(true, getIpsProject());
    }

    /**
     * {@inheritDoc}
     */
    public String getProductCmptType() {
        return productCmptType;
    }

    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        // TODO: V2-temp code entfernen
        try {
            org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType typeObj = findProductCmptType(getIpsProject());
            if (typeObj!=null) {
                policyCmptType = typeObj.getPolicyCmptType();
            } else {
                policyCmptType = "";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        valueChanged(oldType, newType);
    }
    
    /**
     * {@inheritDoc}
     */
	public IProductCmptType findOldProductCmptType() throws CoreException {
		IPolicyCmptType policyCmptType = findPolicyCmptType();
		if (policyCmptType==null) {
			return null;
		}
		return policyCmptType.findOldProductCmptType();
	}
    
    /**
     * {@inheritDoc}
     */
	public org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return (org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType)ipsProject.findProductCmptType(productCmptType);
    }

    /** 
     * {@inheritDoc}
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
            String text = NLS.bind(Messages.ProductCmpt_msgUnknownTemplate, this.productCmptType);
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_PRODUCT_CMPT_TYPE)); //$NON-NLS-1$
        } else {
        	IProductCmptType pType = findOldProductCmptType();
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
        IProductCmptNamingStrategy strategy = getIpsProject().getProductCmptNamingStrategy();
        MessageList list2 = strategy.validate(getName());
        for (Iterator iter = list2.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_NAME);
            list.add(msgNew);
        }
        list2 = strategy.validateRuntimeId(getRuntimeId());
        for (Iterator iter = list2.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_RUNTIME_ID);
            list.add(msgNew);
        }

        list2 = getIpsProject().checkForDuplicateRuntimeIds(new IProductCmpt[] {this});
        list.add(list2);
    }
    
    /** 
     * {@inheritDoc}
     */
    public IRelation findPcTypeRelation(String relationName) throws CoreException {
        IPolicyCmptType pcType = findPolicyCmptType();
        if (pcType==null) {
            return null;
        }
        return pcType.getRelation(relationName);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public QualifiedNameType[] dependsOn() throws CoreException {
        
        // TODO is this correct? we still have dependencies to other product components!
        if(StringUtils.isEmpty(productCmptType)){
            return new QualifiedNameType[0];
        }
        Set qaTypes = new HashSet();
        qaTypes.add(new QualifiedNameType(policyCmptType, IpsObjectType.POLICY_CMPT_TYPE));
        qaTypes.add(new QualifiedNameType(productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE_V2));
        
    	IPolicyCmptType pcType = findPolicyCmptType();
    	if (pcType!=null) {
            qaTypes.addAll(Arrays.asList(((PolicyCmptType)pcType).dependsOn(true)));
    	}
        // TODO v2 - ist das wirklich richtig?
        org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType type = findProductCmptType(getIpsProject());
        if (type!=null) {
            qaTypes.addAll(Arrays.asList(type.dependsOn()));
        }
        
        
    	// add dependency to related product cmpt's and
    	// add dependency to table contents
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i = 0; i < generations.length; i++) {
            addRelatedProductCmptQualifiedNameTypes(qaTypes, (IProductCmptGeneration) generations[i]);
            addRelatedTableContentsQualifiedNameTypes(qaTypes, (IProductCmptGeneration) generations[i]);
        }
        
        return (QualifiedNameType[])qaTypes.toArray(new QualifiedNameType[qaTypes.size()]);
    }
    
    /*
     * Add the qualified name types of all related table contents inside the given generation to the given set
     */
    private void addRelatedTableContentsQualifiedNameTypes(Set qaTypes, IProductCmptGeneration generation) {
        ITableContentUsage[] tableContentUsages = generation.getTableContentUsages();
        for (int i = 0; i < tableContentUsages.length; i++) {
            qaTypes.add(new QualifiedNameType(tableContentUsages[i].getTableContentName(),
                    IpsObjectType.TABLE_CONTENTS));
        }
    }

    /*
     * Add the qualified name types of all related product cmpt's inside the given generation to the given set
     */
    private void addRelatedProductCmptQualifiedNameTypes(Set qaTypes, IProductCmptGeneration generation) {
        IProductCmptRelation[] relations = generation.getRelations();
        for (int j = 0; j < relations.length; j++) {
            qaTypes.add(new QualifiedNameType(relations[j].getTarget(), IpsObjectType.PRODUCT_CMPT));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
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

    /**
     * {@inheritDoc}
     */
    public boolean containsDifferenceToModel() throws CoreException {
        IIpsObjectGeneration[] generations = this.getGenerations();
        for (int i = 0; i < generations.length; i++) {
            IIpsObjectGeneration generation = generations[i];
            if(generation instanceof IProductCmptGeneration){
                IProductCmptGenerationPolicyCmptTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToPolicyCmptType();
                if(delta!=null && !delta.isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void fixAllDifferencesToModel() throws CoreException {
        IIpsObjectGeneration[] generations = this.getGenerations();
        for (int i = 0; i < generations.length; i++) {
            IIpsObjectGeneration generation = generations[i];
            if(generation instanceof IProductCmptGeneration){
                ((IProductCmptGeneration)generation).fixDifferences(((IProductCmptGeneration)generation).computeDeltaToPolicyCmptType());
                
            }
        }
    }
}
