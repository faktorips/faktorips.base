package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
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
	public ProductCmptTypeRelation(Relation relation) {
		ArgumentCheck.notNull(relation);
		this.relation = relation;
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
		throw new RuntimeException("Not implemented yet!");
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
		return relation.getTargetRolePlural();
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
	 * Overridden.
	 */
	public boolean hasContainerRelation() {
		return relation.hasContainerRelation();
	}

	/**
	 * Overridden.
	 */
	public String getContainerRelation() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public IProductCmptTypeRelation findContainerRelation() throws CoreException {
		// TODO Auto-generated method stub
		return null;
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
	public String getMaxCardinality() {
		return relation.getMaxCardinalityProductSide();
	}

	/**
	 * Overridden.
	 */
	public boolean is1ToMany() {
		return "*".equals(getMaxCardinality());
	}

	/**
	 * Overridden.
	 */
	public void setMaxCardinality(String newValue) {
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
		throw new RuntimeException("Not implemented yet!");
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
		throw new RuntimeException("Not implemented yet!");
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
		throw new RuntimeException("Not implemented yet!");
	}

	/**
	 * Overridden.
	 */
	public void initFromXml(Element element) {
		throw new RuntimeException("Not implemented yet!");
	}

	/**
	 * Overridden.
	 */
	public Memento newMemento() {
		throw new RuntimeException("Not implemented yet!");
	}

	/**
	 * Overridden.
	 */
	public void setState(Memento memento) {
		throw new RuntimeException("Not implemented yet!");
	}

	/**
	 * Overridden.
	 */
	public Object getExtPropertyValue(String propertyId) {
		throw new RuntimeException("Not implemented yet!");
	}

	/**
	 * Overridden.
	 */
	public void setExtPropertyValue(String propertyId, Object value) {
		throw new RuntimeException("Not implemented yet!");
	}

	public boolean equals(Object o) {
		if (!(o instanceof ProductCmptTypeRelation)) {
			return false;
		}
		ProductCmptTypeRelation other = (ProductCmptTypeRelation)o;
		return relation.equals(other.relation);
	}
}
