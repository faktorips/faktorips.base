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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptType implements IProductCmptType {

	private PolicyCmptType policyCmptType;
	
	/**
	 * 
	 */
	public ProductCmptType(IPolicyCmptType policyCmptType) {
		ArgumentCheck.notNull(policyCmptType);
		this.policyCmptType = (PolicyCmptType)policyCmptType;
	}

	/**
     * {@inheritDoc}
	 */
	public boolean isAbstract() {
		return policyCmptType.isAbstract();
	}

	/**
	 * {@inheritDoc}
	 */
	public IPolicyCmptType findPolicyCmptyType() throws CoreException {
		return policyCmptType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptType findSupertype() throws CoreException {
		IPolicyCmptType superPolicyCmptType = policyCmptType.findSupertype();
		if (superPolicyCmptType==null) {
			return null;
		}
        if (!superPolicyCmptType.isConfigurableByProductCmptType()) {
            return null;
        }
        if (StringUtils.isEmpty(superPolicyCmptType.getUnqualifiedProductCmptType())) {
            return null;
        }
		return new ProductCmptType((PolicyCmptType)superPolicyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelation[] getRelations() {
		IRelation[] relations = policyCmptType.getRelations();
		List result = new ArrayList(relations.length);
		for (int i = 0; i < relations.length; i++) {
			if (relations[i].isProductRelevant()) {
				result.add(new ProductCmptTypeRelation((Relation)relations[i]));
			}
		}
		return (IProductCmptTypeRelation[])result.toArray(new IProductCmptTypeRelation[result.size()]);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IAttribute[] getAttributes() {
		IAttribute[] attributes = policyCmptType.getAttributes();
		List result = new ArrayList(attributes.length);
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].isProductRelevant() && !attributes[i].isDerivedOrComputed()) {
				result.add(attributes[i]);
			}
		}
		return (IAttribute[])result.toArray(new IAttribute[result.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPolicyCmptyType() {
		return policyCmptType.getQualifiedName();
	}

	/**
	 * {@inheritDoc}
	 */
	public IpsObjectType getIpsObjectType() {
		return IpsObjectType.PRODUCT_CMPT_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsSrcFile getIpsSrcFile() {
		return policyCmptType.getIpsSrcFile();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getQualifiedName() {
        String pack = StringUtil.getPackageName(policyCmptType.getQualifiedName());
        if (pack.equals("")) { //$NON-NLS-1$
        	return getName();
        }
        return pack + '.' + getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsPackageFragment getIpsPackageFragment() {
		return policyCmptType.getIpsPackageFragment();
	}

	/**
	 * {@inheritDoc}
	 */
	public QualifiedNameType[] dependsOn() throws CoreException {
		return new QualifiedNameType[]{policyCmptType.getQualifiedNameType()};
	}

	/**
	 * {@inheritDoc}
	 */
	public IType getJavaType(int kind) throws CoreException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IType[] getAllJavaTypes() throws CoreException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		if (StringUtils.isEmpty(policyCmptType.getUnqualifiedProductCmptType())) {
			return policyCmptType.getName() + "Pk"; //$NON-NLS-1$
		} else {
			return policyCmptType.getUnqualifiedProductCmptType();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsModel getIpsModel() {
		return policyCmptType.getIpsModel();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsProject getIpsProject() {
		return policyCmptType.getIpsProject();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists() {
		return policyCmptType.exists();
	}

	/**
	 * {@inheritDoc}
	 */
	public IResource getCorrespondingResource() {
		return policyCmptType.getCorrespondingResource();
	}

	/**
	 * {@inheritDoc}
	 */
	public IResource getEnclosingResource() {
		return policyCmptType.getEnclosingResource();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement getParent() {
		return policyCmptType.getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() throws CoreException {
		return getRelations();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren() throws CoreException {
		return getChildren().length>0;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage() {
		return getIpsObjectType().getImage();
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
		return new MessageList();
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
	public void setDescription(String newDescription) {
		policyCmptType.setDescription(newDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return policyCmptType.getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (!(o instanceof ProductCmptType)) {
			return false;
		}
		return ((ProductCmptType)o).policyCmptType.equals(policyCmptType);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return getName().hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
        return getParent().toString() + "/" + getName(); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelation getRelation(String relationName) {
		ArgumentCheck.notNull(relationName);
		IProductCmptTypeRelation[] relations = getRelations();
		for (int i = 0; i < relations.length; i++) {
			if (relations[i].getName().equals(relationName)) {
				return relations[i];
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
	
	
}
