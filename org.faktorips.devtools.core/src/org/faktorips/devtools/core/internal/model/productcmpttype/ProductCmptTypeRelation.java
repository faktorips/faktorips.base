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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeRelation implements IProductCmptTypeRelation {

	private Relation relation;
	
	/**
	 * 
	 */
	public ProductCmptTypeRelation(IRelation relation) {
		ArgumentCheck.notNull(relation);
		this.relation = (Relation)relation;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptType getProductCmptType() {
		return new ProductCmptType((PolicyCmptType)relation.getPolicyCmptType());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IRelation findPolicyCmptTypeRelation() throws CoreException {
		return relation;
	}

	/**
	 * {@inheritDoc}
	 */
	public RelationType getRelationType() {
		return relation.getRelationType();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isAbstract() {
		return relation.isReadOnlyContainer();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean implementsContainerRelation() throws CoreException {
		return findContainerRelation()!=null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptType findTarget() throws CoreException {
		IPolicyCmptType type = relation.findTarget();
		if (type==null) {
			return null;
		}
		return new ProductCmptType((PolicyCmptType)type);
	}
    
    /**
     * {@inheritDoc}
     */
	public org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType findTarget(IIpsProject ipsProject) throws CoreException {
        IPolicyCmptType type = relation.findTarget();
        if (type==null) {
            return null;
        }
        return type.findProductCmptType(ipsProject);
    }

    /**
	 * {@inheritDoc}
	 */
	public void setTarget(String newTarget) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTargetRoleSingular() {
		return relation.getTargetRoleSingularProductSide();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetRoleSingular(String newRole) {
		relation.setTargetRoleSingularProductSide(newRole);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTargetRolePlural() {
		return relation.getTargetRolePluralProductSide();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetRolePlural(String newRole) {
		relation.setTargetRolePluralProductSide(newRole);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAbstractContainer() {
		return relation.isReadOnlyContainer();
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelation findContainerRelation() throws CoreException {
		IRelation containerRelation = relation.findContainerRelation();
		if (containerRelation==null) {
			return null;
		}
		return new ProductCmptTypeRelation(containerRelation);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMinCardinality() {
		return relation.getMinCardinalityProductSide();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMinCardinality(int newValue) {
		relation.setMinCardinalityProductSide(newValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxCardinality() {
		return relation.getMaxCardinalityProductSide();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean is1ToMany() {
		return getMaxCardinality() == CARDINALITY_MANY;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxCardinality(int newValue) {
		relation.setMaxCardinalityProductSide(newValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObject getIpsObject() {
		return new ProductCmptType((PolicyCmptType)relation.getPolicyCmptType());
	}

	/**
	 * {@inheritDoc}
	 */
	public int getId() {
		return relation.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete() {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return relation.getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(String newDescription) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return getTargetRoleSingular();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsModel getIpsModel() {
		return relation.getIpsModel();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsProject getIpsProject() {
		return relation.getIpsProject();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public IResource getCorrespondingResource() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IResource getEnclosingResource() {
		return relation.getEnclosingResource();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement getParent() {
		return getIpsObject();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() throws CoreException {
		return new IIpsElement[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren() throws CoreException {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return relation.getImage();
	}

    /**
	 * {@inheritDoc}
	 */
	public boolean isValid() throws CoreException {
		return getValidationResultSeverity()!=Message.ERROR;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getValidationResultSeverity() throws CoreException {
		return validate().getSeverity();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MessageList validate() throws CoreException {
		return relation.validate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Element toXml(Document doc) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public void initFromXml(Element element) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public Memento newMemento() {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public void setState(Memento memento) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExtPropertyValue(String propertyId) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExtPropertyValue(String propertyId, Object value) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	public boolean equals(Object o) {
		if (!(o instanceof ProductCmptTypeRelation)) {
			return false;
		}
		ProductCmptTypeRelation other = (ProductCmptTypeRelation)o;
		return relation.equals(other.relation);
	}
	
	public int hashCode() {
		return getName().hashCode();
	}
	
	public String toString() {
        return getParent().toString() + "/" + getName(); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile getIpsSrcFile() {
        return getProductCmptType().getIpsSrcFile();
    }
}
