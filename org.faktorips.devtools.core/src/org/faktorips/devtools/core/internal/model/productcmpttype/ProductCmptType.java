package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.memento.Memento;
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
	public ProductCmptType(PolicyCmptType policyCmptType) {
		ArgumentCheck.notNull(policyCmptType);
		this.policyCmptType = policyCmptType;
	}

	/**
	 * Overridden.
	 */
	public IPolicyCmptType findPolicyCmptyType() throws CoreException {
		return policyCmptType;
	}

	/**
	 * Overridden.
	 */
	public String getPolicyCmptyType() {
		return policyCmptType.getQualifiedName();
	}

	/**
	 * Overridden.
	 */
	public IpsObjectType getIpsObjectType() {
		return IpsObjectType.PRODUCT_CMPT_TYPE;
	}

	/**
	 * Overridden.
	 */
	public IIpsSrcFile getIpsSrcFile() {
		return policyCmptType.getIpsSrcFile();
	}

	/**
	 * Overridden.
	 */
	public String getQualifiedName() {
        String pack = StringUtil.getPackageName(policyCmptType.getQualifiedName());
        return pack + '.' + getName();
	}

	/**
	 * Overridden.
	 */
	public QualifiedNameType getQualifiedNameType() {
        return new QualifiedNameType(getQualifiedName(), getIpsObjectType());
	}

	/**
	 * Overridden.
	 */
	public IIpsPackageFragment getIpsPackageFragment() {
		return policyCmptType.getIpsPackageFragment();
	}

	/**
	 * Overridden.
	 */
	public QualifiedNameType[] dependsOn() throws CoreException {
		return new QualifiedNameType[]{policyCmptType.getQualifiedNameType()};
	}

	/**
	 * Overridden.
	 */
	public IType getJavaType(int kind) throws CoreException {
		return null;
	}

	/**
	 * Overridden.
	 */
	public IType[] getAllJavaTypes() throws CoreException {
		return null;
	}

	/**
	 * Overridden.
	 */
	public String getName() {
		if (StringUtils.isEmpty(policyCmptType.getUnqualifiedProductCmptType())) {
			return policyCmptType.getName() + "Pk";
		} else {
			return policyCmptType.getUnqualifiedProductCmptType();
		}
	}

	/**
	 * Overridden.
	 */
	public IIpsModel getIpsModel() {
		return policyCmptType.getIpsModel();
	}

	/**
	 * Overridden.
	 */
	public IIpsProject getIpsProject() {
		return policyCmptType.getIpsProject();
	}

	/**
	 * Overridden.
	 */
	public boolean exists() {
		return policyCmptType.exists();
	}

	/**
	 * Overridden.
	 */
	public IResource getCorrespondingResource() {
		return policyCmptType.getCorrespondingResource();
	}

	/**
	 * Overridden.
	 */
	public IResource getEnclosingResource() {
		return policyCmptType.getEnclosingResource();
	}

	/**
	 * Overridden.
	 */
	public IIpsElement getParent() {
		return policyCmptType.getParent();
	}

	/**
	 * Overridden.
	 */
	public IIpsElement[] getChildren() throws CoreException {
		// TODO implement
		throw new RuntimeException("Not implemented yet!");
	}

	/**
	 * Overridden.
	 */
	public boolean hasChildren() throws CoreException {
		return getChildren().length>0;
	}

	/**
	 * Overridden.
	 */
	public Image getImage() {
		return getIpsObjectType().getImage();
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

	/**
	 * Overridden.
	 */
	public MessageList validate() throws CoreException {
		return new MessageList();
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
	public void setDescription(String newDescription) {
		policyCmptType.setDescription(newDescription);
	}

	/**
	 * Overridden.
	 */
	public String getDescription() {
		return policyCmptType.getDescription();
	}

	public boolean equals(Object o) {
		if (!(o instanceof ProductCmptType)) {
			return false;
		}
		return ((ProductCmptType)o).policyCmptType.equals(policyCmptType);
	}
}
