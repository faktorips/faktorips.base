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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * 
 */
public class ProductCmptGeneration extends IpsObjectGeneration implements
		IProductCmptGeneration {

	private List configElements = new ArrayList(0);

	private ArrayList relations = new ArrayList(0);

	public ProductCmptGeneration(ITimedIpsObject ipsObject, int id) {
		super(ipsObject, id);
	}

	public ProductCmptGeneration() {
	}

	/**
	 * Overridden.
	 */
	public IProductCmpt getProductCmpt() {
		return (IProductCmpt) getParent();
	}

	/**
	 * Overridden IMethod.
	 *
	 * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
	 */
	public IIpsElement[] getChildren() {
		int numOfChildren = getNumOfConfigElements() + getNumOfRelations();
		IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
		List childrenList = new ArrayList(numOfChildren);
		childrenList.addAll(configElements);
		childrenList.addAll(relations);
		childrenList.toArray(childrenArray);
		return childrenArray;
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmpt#computeDeltaToPolicyCmptType()
	 */
	public IProductCmptGenerationPolicyCmptTypeDelta computeDeltaToPolicyCmptType()
			throws CoreException {
		IPolicyCmptType pcType = getProductCmpt().findPolicyCmptType();
		if (pcType != null) {
			return new ProductCmptGenerationPolicyCmptTypeDelta(this, pcType);
		}
		return null;
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#fixDifferences(org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta)
	 */
	public void fixDifferences(IProductCmptGenerationPolicyCmptTypeDelta delta)
			throws CoreException {
		if (delta == null) {
			return;
		}
		IAttribute[] attributes = delta
				.getAttributesWithMissingConfigElements();
		for (int i = 0; i < attributes.length; i++) {
			IConfigElement element = newConfigElement();
			element.setPcTypeAttribute(attributes[i].getName());
			element.setType(attributes[i].getConfigElementType());
			element.setValue(attributes[i].getDefaultValue());
		}
		IConfigElement[] elements = delta
				.getConfigElementsWithMissingAttributes();
		for (int i = 0; i < elements.length; i++) {
			elements[i].delete();
		}
		elements = delta.getTypeMismatchElements();
		for (int i = 0; i < elements.length; i++) {
			IAttribute a = elements[i].findPcTypeAttribute();
			if (elements[i].getType() == ConfigElementType.FORMULA) {
				elements[i].setValue(""); //$NON-NLS-1$
			}
			elements[i].setType(a.getConfigElementType());
		}
		elements = delta.getElementsWithValueSetMismatch();
		for (int i = 0; i < elements.length; i++) {
			IAttribute a = elements[i].findPcTypeAttribute();
			elements[i].setValueSetCopy(a.getValueSet());
		}
		IProductCmptRelation[] relations = delta
				.getRelationsWithMissingPcTypeRelations();
		for (int i = 0; i < relations.length; i++) {
			relations[i].delete();
		}
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getConfigElements()
	 */
	public IConfigElement[] getConfigElements() {
		return (IConfigElement[]) configElements
				.toArray(new IConfigElement[configElements.size()]);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getConfigElement(java.lang.String)
	 */
	public IConfigElement getConfigElement(String attributeName) {
		for (Iterator it = configElements.iterator(); it.hasNext();) {
			IConfigElement each = (IConfigElement) it.next();
			if (each.getPcTypeAttribute().equals(attributeName)) {
				return each;
			}
		}
		return null;
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getConfigElements(org.faktorips.devtools.core.model.product.ConfigElementType)
	 */
	public IConfigElement[] getConfigElements(ConfigElementType type) {
		List result = new ArrayList(configElements.size());
		for (Iterator it = configElements.iterator(); it.hasNext();) {
			IConfigElement configEl = (IConfigElement) it.next();
			if (configEl.getType().equals(type)) {
				result.add(configEl);
			}
		}
		return (IConfigElement[]) result.toArray(new IConfigElement[result
				.size()]);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getNumOfConfigElements()
	 */
	public int getNumOfConfigElements() {
		return configElements.size();
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#newConfigElement()
	 */
	public IConfigElement newConfigElement() {
		IConfigElement newElement = newConfigElementInternal(getNextPartId());
		updateSrcFile();
		return newElement;
	}

	/*
	 * Creates a new attribute without updating the src file.
	 */
	private ConfigElement newConfigElementInternal(int id) {
		ConfigElement e = new ConfigElement(this, id);
		configElements.add(e);
		return e;
	}

	void removeConfigElement(ConfigElement element) {
		configElements.remove(element);
		updateSrcFile();
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getRelations()
	 */
	public IProductCmptRelation[] getRelations() {
		return (IProductCmptRelation[]) relations
				.toArray(new ProductCmptRelation[relations.size()]);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getRelations(java.lang.String)
	 */
	public IProductCmptRelation[] getRelations(String typeRelation) {
		List result = new ArrayList();
		for (Iterator it = relations.iterator(); it.hasNext();) {
			IProductCmptRelation relation = (IProductCmptRelation) it.next();
			if (relation.getProductCmptTypeRelation().equals(typeRelation)) {
				result.add(relation);
			}
		}
		return (IProductCmptRelation[]) result
				.toArray(new ProductCmptRelation[result.size()]);
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#getNumOfRelations()
	 */
	public int getNumOfRelations() {
		return relations.size();
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.devtools.core.model.product.IProductCmptGeneration#newRelation(java.lang.String)
	 */
	public IProductCmptRelation newRelation(String pcTypeRelation) {
		ProductCmptRelation newRelation = newRelationInternal(getNextPartId());
		newRelation.setProductCmptTypeRelation(pcTypeRelation);
		updateSrcFile();
		return newRelation;
	}

	public IProductCmptRelation newRelation(String pcTypeRelation,
			IProductCmptRelation insertBefore) {
		ProductCmptRelation newRelation = newRelationInternal(getNextPartId(),
				insertBefore);
		newRelation.setProductCmptTypeRelation(pcTypeRelation);
		updateSrcFile();
		return newRelation;
	}

	public IProductCmptRelation newRelation() {
		return newRelationInternal(getNextPartId());
	}

	private ProductCmptRelation newRelationInternal(int id,
			IProductCmptRelation insertBefore) {
		ProductCmptRelation newRelation = new ProductCmptRelation(this, id);
		if (insertBefore == null) {
			relations.add(newRelation);
		} else {
			int index = relations.indexOf(insertBefore);
			if (index == -1) {
				relations.add(newRelation);
			} else {
				relations.add(index, newRelation);
			}
		}
		return newRelation;
	}

	private ProductCmptRelation newRelationInternal(int id) {
		return newRelationInternal(id, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveRelation(IProductCmptRelation toMove,
			IProductCmptRelation moveBefore) {
		relations.remove(toMove);
		int index = relations.indexOf(moveBefore);
		if (index == -1) {
			relations.add(toMove);
		} else {
			relations.add(index, toMove);
		}
		updateSrcFile();
	}

	void removeRelation(ProductCmptRelation relation) {
		relations.remove(relation);
		updateSrcFile();
	}

	/*
	 * Returns true if the generation contains a formula config element, otherwise false.
	 */
	boolean containsFormula() {
		for (Iterator it = configElements.iterator(); it.hasNext();) {
			IConfigElement element = (IConfigElement) it.next();
			if (element.getType().equals(ConfigElementType.FORMULA)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Overridden IMethod.
	 *
	 * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#newPart(java.lang.String, int)
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(ConfigElement.TAG_NAME)) {
			return newConfigElementInternal(id);
		} else if (xmlTagName.equals(ProductCmptRelation.TAG_NAME)) {
			return newRelationInternal(id);
		}
		throw new RuntimeException(
				"Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
	}

	/**
	 * Overridden IMethod.
	 *
	 * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reAddPart(org.faktorips.devtools.core.model.IIpsObjectPart)
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof IConfigElement) {
			configElements.add(part);
			return;
		} else if (part instanceof IProductCmptRelation) {
			relations.add(part);
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * Overridden IMethod.
	 *
	 * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#reinitPartCollections()
	 */
	protected void reinitPartCollections() {
		configElements.clear();
		relations.clear();
	}

	protected void validate(MessageList list) throws CoreException {
		super.validate(list);
		IConfigElement[] configElements = getConfigElements();
		for (int i = 0; i < configElements.length; i++) {
			((ConfigElement) configElements[i]).validate(list);
		}

		for (int i = 0; i < relations.size(); i++) {
			((ProductCmptRelation) relations.get(i)).validate(list);
		}

		IProductCmptType type = getProductCmpt().findProductCmptType();

		// no type information available, so no further validation possible
		if (type == null) {
			list.add(new Message(MSGCODE_NO_TEMPLATE,
					Messages.ProductCmptGeneration_msgTemplateNotFound,
					Message.ERROR, this));
			return;
		}

		IProductCmptTypeRelation[] relationTypes = type.getRelations();
		for (int i = 0; i < relationTypes.length; i++) {
			IProductCmptRelation[] relations = getRelations(relationTypes[i]
					.getTargetRoleSingular());

			if (relationTypes[i].getMinCardinality() > relations.length) {
				Object[] params = { new Integer(relations.length),
						relationTypes[i].getTargetRoleSingular(),
						new Integer(relationTypes[i].getMinCardinality()) };
				String msg = NLS.bind(
						Messages.ProductCmptGeneration_msgNotEnoughRelations,
						params);
				ObjectProperty prop1 = new ObjectProperty(this, null);
				ObjectProperty prop2 = new ObjectProperty(relationTypes[i]
						.getTargetRoleSingular(), null);
				list.add(new Message(MSGCODE_NOT_ENOUGH_RELATIONS, msg,
						Message.ERROR, new ObjectProperty[] { prop1, prop2 }));
			}

			int maxCardinality = relationTypes[i].getMaxCardinality();
			if (maxCardinality < relations.length) {
				Object[] params = {
						new Integer(relations.length),
						"" + maxCardinality, relationTypes[i].getTargetRoleSingular() }; //$NON-NLS-1$
				String msg = NLS.bind(
						Messages.ProductCmptGeneration_msgTooManyRelations,
						params);
				ObjectProperty prop1 = new ObjectProperty(this, null);
				ObjectProperty prop2 = new ObjectProperty(relationTypes[i]
						.getTargetRoleSingular(), null);
				list.add(new Message(MSGCODE_TOO_MANY_RELATIONS, msg,
						Message.ERROR, new ObjectProperty[] { prop1, prop2 }));
			}

			Map targets = new Hashtable();
			String msg = null;
			for (int j = 0; j < relations.length; j++) {
				String target = relations[j].getTarget();
				if (targets.get(target) != null) {
					if (msg == null) {
						msg = NLS
								.bind(
										"The relation of type {0} is defined more than once for the target {1}.",
										relationTypes[i]
												.getTargetRoleSingular(),
										target);
					}
					list.add(new Message(MSGCODE_DUPLICATE_RELATION_TARGET,
							msg, Message.ERROR, relationTypes[i].getTargetRoleSingular()));
				} else {
					targets.put(target, target);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		if (partType.equals(IConfigElement.class)) {
			return newConfigElement();
		} else if (partType.equals(IRelation.class)) {
			return newRelation();
		}

		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

}
