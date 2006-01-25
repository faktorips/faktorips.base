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
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
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
		super();
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
	public RelationType getRelationType() {
		return relation.getRelationType();
	}

	/**
	 * Overridden.
	 */
	public String getTarget() {
		return null;
	}

	/**
	 * Overridden.
	 */
	public IProductCmptType findTarget() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void setTarget(String newTarget) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public String getTargetRoleSingular() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void setTargetRoleSingular(String newRole) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public String getTargetRolePlural() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void setTargetRolePlural(String newRole) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public boolean isAbstractContainer() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Overridden.
	 */
	public int getMinCardinality() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Overridden.
	 */
	public void setMinCardinality(int newValue) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public String getMaxCardinality() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public boolean is1ToMany() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Overridden.
	 */
	public void setMaxCardinality(String newValue) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public IIpsObject getIpsObject() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Overridden.
	 */
	public void delete() {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void setDescription(String newDescription) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public IIpsModel getIpsModel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public IIpsProject getIpsProject() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Overridden.
	 */
	public IResource getCorrespondingResource() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public IResource getEnclosingResource() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public IIpsElement getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public IIpsElement[] getChildren() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public boolean hasChildren() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Overridden.
	 */
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public MessageList validate() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public Element toXml(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void initFromXml(Element element) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public Memento newMemento() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void setState(Memento memento) {
		// TODO Auto-generated method stub

	}

	/**
	 * Overridden.
	 */
	public Object getExtPropertyValue(String propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Overridden.
	 */
	public void setExtPropertyValue(String propertyId, Object value) {
		// TODO Auto-generated method stub

	}

}
