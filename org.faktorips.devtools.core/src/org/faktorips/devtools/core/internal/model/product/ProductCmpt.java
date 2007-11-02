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
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.product.CycleInProductStructureException;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptKind;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.product.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
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
    public IPolicyCmptType findPolicyCmptType() throws CoreException {
        IProductCmptType productCmptType = findProductCmptType(getIpsProject());
        if (productCmptType==null) {
            return null;
        }
        return productCmptType.findPolicyCmptType(getIpsProject());
    }

    /**
     * {@inheritDoc}
     */
    public String getProductCmptType() {
        return productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        valueChanged(oldType, newType);
    }
    
    /**
     * {@inheritDoc}
     */
	public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(productCmptType);
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
        IIpsProject ipsProject = getIpsProject();
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            String text = NLS.bind(Messages.ProductCmpt_msgUnknownTemplate, this.productCmptType);
            list.add(new Message(MSGCODE_MISSINGG_PRODUCT_CMPT_TYPE, text, Message.ERROR, this, PROPERTY_PRODUCT_CMPT_TYPE)); //$NON-NLS-1$
        } else {
        	try {
				MessageList list3 = type.validate();
				if (list3.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY) != null || 
                    list3.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND) != null ||
				    list3.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY) != null) {
					String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, this.getProductCmptType());
					list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, msg, Message.ERROR, type, IProductCmptType.PROPERTY_NAME));
				}
			} catch (Exception e) {
				throw new CoreException(new IpsStatus("Error during validate of product component type", e)); //$NON-NLS-1$
			}
        }
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
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

        list2 = getIpsProject().checkForDuplicateRuntimeIds(new IIpsSrcFile[] {this.getIpsSrcFile()});
        list.add(list2);
    }
    
    /** 
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAssociation findPcTypeRelation(String relationName) throws CoreException {
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
            IProductCmptGeneration gen = getProductCmptGeneration(0);
            if (gen.getNumOfFormulas()>0) {
                IFormula[] formulas = gen.getFormulas();
                for (int j = 0; j < formulas.length; j++) {
                    if (formulas[j].getFormulaTestCases().length > 0){
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
    public Dependency[] dependsOn() throws CoreException {

        Set dependencySet = new HashSet();

        if (!StringUtils.isEmpty(productCmptType)) {
            dependencySet.add(Dependency.createInstanceOfDependency(this.getQualifiedNameType(), new QualifiedNameType(
                    productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE_V2)));
        }

        // add dependency to related product cmpt's and add dependency to table contents
        IIpsObjectGeneration[] generations = getGenerations();
        for (int i = 0; i < generations.length; i++) {
            ((ProductCmptGeneration)generations[i]).dependsOn(dependencySet);
        }

        return (Dependency[])dependencySet.toArray(new Dependency[dependencySet.size()]);
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
	public IProductCmptTreeStructure getStructure(IIpsProject ipsProject) throws CycleInProductStructureException {
		return new ProductCmptTreeStructure(this, ipsProject);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject) throws CycleInProductStructureException {
		return new ProductCmptTreeStructure(this, date, ipsProject);
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
                IGenerationToTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToModel();
                if(!delta.isEmpty()){
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
                IGenerationToTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToModel();
                delta.fix();
                
            }
        }
    }
}
