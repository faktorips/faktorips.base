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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;
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
	 * Overridden.
	 */
	public IProductCmptType getProductCmptType() {
		return new ProductCmptType((PolicyCmptType)relation.getPolicyCmptType());
	}
	
	/**
	 * Overridden.
	 */
	public IRelation findPolicyCmptTypeRelation() throws CoreException {
		return relation;
	}

	/**
	 * Overridden.
	 */
	public RelationType getRelationType() {
		return relation.getRelationType();
	}
	
	/**
	 * Overridden.
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
	 * Overridden.
	 */
	public IProductCmptType findTarget() throws CoreException {
		IPolicyCmptType type = relation.findTarget();
		if (type==null) {
			return null;
		}
		return new ProductCmptType((PolicyCmptType)type);
	}

	/**
	 * Overridden.
	 */
	public void setTarget(String newTarget) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public String getTargetRoleSingular() {
		return relation.getTargetRoleSingularProductSide();
	}

	/**
	 * Overridden.
	 */
	public void setTargetRoleSingular(String newRole) {
		relation.setTargetRoleSingularProductSide(newRole);
	}

	/**
	 * Overridden.
	 */
	public String getTargetRolePlural() {
		return relation.getTargetRolePluralProductSide();
	}

	/**
	 * Overridden.
	 */
	public void setTargetRolePlural(String newRole) {
		relation.setTargetRolePluralProductSide(newRole);
	}

	/**
	 * Overridden.
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
	 * Overridden.
	 */
	public int getMinCardinality() {
		return relation.getMinCardinalityProductSide();
	}

	/**
	 * Overridden.
	 */
	public void setMinCardinality(int newValue) {
		relation.setMinCardinalityProductSide(newValue);
	}

	/**
	 * Overridden.
	 */
	public int getMaxCardinality() {
		return relation.getMaxCardinalityProductSide();
	}

	/**
	 * Overridden.
	 */
	public boolean is1ToMany() {
		return getMaxCardinality() == CARDINALITY_MANY;
	}

	/**
	 * Overridden.
	 */
	public void setMaxCardinality(int newValue) {
		relation.setMaxCardinalityProductSide(newValue);
	}

	/**
	 * Overridden.
	 */
	public IIpsObject getIpsObject() {
		return new ProductCmptType((PolicyCmptType)relation.getPolicyCmptType());
	}

	/**
	 * Overridden.
	 */
	public int getId() {
		return relation.getId();
	}

	/**
	 * Overridden.
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
	 * Overridden.
	 */
	public String getDescription() {
		return relation.getDescription();
	}

	/**
	 * Overridden.
	 */
	public void setDescription(String newDescription) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public String getName() {
		return getTargetRoleSingular();
	}

	/**
	 * Overridden.
	 */
	public IIpsModel getIpsModel() {
		return relation.getIpsModel();
	}

	/**
	 * Overridden.
	 */
	public IIpsProject getIpsProject() {
		return relation.getIpsProject();
	}

	/**
	 * Overridden.
	 */
	public boolean exists() {
		return true;
	}

	/**
	 * Overridden.
	 */
	public IResource getCorrespondingResource() {
		return null;
	}

	/**
	 * Overridden.
	 */
	public IResource getEnclosingResource() {
		return relation.getEnclosingResource();
	}

	/**
	 * Overridden.
	 */
	public IIpsElement getParent() {
		return getIpsObject();
	}

	/**
	 * Overridden.
	 */
	public IIpsElement[] getChildren() throws CoreException {
		return new IIpsElement[0];
	}

	/**
	 * Overridden.
	 */
	public boolean hasChildren() throws CoreException {
		return false;
	}

	/**
	 * Overridden.
	 */
	public Image getImage() {
		return relation.getImage();
	}

	/**
	 * Overridden.
	 */
	public MessageList validate() throws CoreException {
		return relation.validate();
	}

	/**
	 * Overridden.
	 */
	public Element toXml(Document doc) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public void initFromXml(Element element) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public Memento newMemento() {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public void setState(Memento memento) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public Object getExtPropertyValue(String propertyId) {
		throw new RuntimeException("Not implemented yet!"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
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
}
