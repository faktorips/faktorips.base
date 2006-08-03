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

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class ProductCmptRelation extends IpsObjectPart implements
		IProductCmptRelation {

	final static String TAG_NAME = "Relation"; //$NON-NLS-1$

	private String productCmptTypeRelation = ""; //$NON-NLS-1$

	private String target = ""; //$NON-NLS-1$

	private int minCardinality = 0;

	private int maxCardinality = 1;
	
	private String targetRuntimeId = ""; //$NON-NLS-1$

	private boolean deleted = false;


	public ProductCmptRelation(IProductCmptGeneration generation, int id) {
		super(generation, id);
	}

	public ProductCmptRelation() {
		super();
	}

	/**
	 * Overridden.
	 */
	public IProductCmpt getProductCmpt() {
		return (IProductCmpt) getParent().getParent();
	}

	/**
	 * Overridden.
	 */
	public IProductCmptGeneration getProductCmptGeneration() {
		return (IProductCmptGeneration) getParent();
	}

	/**
	 * Overridden.
	 * 
	 */
	public void delete() {
		if (deleted) {
			return;
		}
		deleted = true;
		((ProductCmptGeneration) getParent()).removeRelation(this);
		parent = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeleted() {
		return deleted;
	}

	public String getName() {
		return target;
	}

	/**
	 * Overridden.
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("ProductCmptRelation.gif"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProductCmptTypeRelation() {
		return productCmptTypeRelation;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelation findProductCmptTypeRelation()
			throws CoreException {
		IProductCmptType productCmptType = getProductCmpt()
				.findProductCmptType();
		IProductCmptTypeRelation relation = null;

		while (productCmptType != null && relation == null) {
			relation = productCmptType.getRelation(productCmptTypeRelation);
			productCmptType = productCmptType.findSupertype();
		}

		return relation;
	}

	void setProductCmptTypeRelation(String newRelation) {
		productCmptTypeRelation = newRelation;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTargetRuntimeId() {
		return targetRuntimeId;
	}

	private void setRuntimeIdForTarget(String newTarget, String oldTarget){
	
		if(!ObjectUtils.equals(newTarget, oldTarget)){
			IProductCmpt productCmpt;
			try {
				productCmpt = getIpsProject().findProductCmptByQualifiedName(newTarget);
				
				if(productCmpt != null){
					targetRuntimeId = productCmpt.getRuntimeId();
					return;
				}
				targetRuntimeId = ""; //$NON-NLS-1$

			} catch (CoreException e) {
				IpsPlugin.logAndShowErrorDialog(e);
			}
		}
	}
	
	/**
	 * Overridden.
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmpt findTarget() {
		try {
			return (IProductCmpt)getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, getTarget());
		} catch (CoreException e) {
			// no valid target defined...
			return null;
		}
	}

	/**
	 * Overridden.
	 */
	public void setTarget(String newTarget) {
		String oldTarget = target;
		target = newTarget;
		setRuntimeIdForTarget(newTarget, oldTarget);
		valueChanged(oldTarget, target);
	}

	/**
	 * Overridden.
	 */
	public int getMinCardinality() {
		return minCardinality;
	}

	/**
	 * Overridden.
	 */
	public void setMinCardinality(int newValue) {
		int oldValue = minCardinality;
		minCardinality = newValue;
		valueChanged(oldValue, newValue);

	}

	/**
	 * Overridden.
	 */
	public int getMaxCardinality() {
		return maxCardinality;
	}

	/**
	 * Overridden.
	 */
	public void setMaxCardinality(int newValue) {
		int oldValue = maxCardinality;
		maxCardinality = newValue;
		valueChanged(oldValue, newValue);
	}

	protected void validateThis(MessageList list) throws CoreException {
		if (isDeleted()) {
			return;
		}
		super.validateThis(list);
		IProductCmptTypeRelation relation = findProductCmptTypeRelation();
		IRelation relType = null;
		if (relation == null) {
			String text = NLS.bind(
					Messages.ProductCmptRelation_msgNoRelationDefined,
					productCmptTypeRelation, getProductCmpt()
							.getPolicyCmptType());
			list.add(new Message(MSGCODE_UNKNWON_RELATIONTYPE, text,
					Message.ERROR, this, PROPERTY_PCTYPE_RELATION));
		} else {
			relType = relation.findPolicyCmptTypeRelation();
		}
		ValidationUtils.checkIpsObjectReference(target,
				IpsObjectType.PRODUCT_CMPT, "target", this, PROPERTY_TARGET, MSGCODE_UNKNWON_TARGET, list); //$NON-NLS-1$
		if (maxCardinality == 0) {
			String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThan1;
			list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1, text,
					Message.ERROR, this, PROPERTY_MAX_CARDINALITY));
		} else if (maxCardinality != -1) {
			if (minCardinality > maxCardinality) {
				String text = Messages.ProductCmptRelation_msgMaxCardinalityIsLessThanMin;
				list.add(new Message(MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN,
						text, Message.ERROR, this, new String[] {
								PROPERTY_MIN_CARDINALITY,
								PROPERTY_MAX_CARDINALITY }));
			}
			if (relType != null
					&& !(relType.getMaxCardinality() == IRelation.CARDINALITY_MANY)) { //$NON-NLS-1$
				try {
					int maxType = relType.getMaxCardinality();
					if (maxCardinality > maxType) {
						String text = NLS
								.bind(
										Messages.ProductCmptRelation_msgMaxCardinalityExceedsModelMax,
										"" + maxCardinality, "" + relType.getMaxCardinality()); //$NON-NLS-1$ //$NON-NLS-2$
						list.add(new Message(
								MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX,
								text, Message.ERROR, this,
								PROPERTY_MAX_CARDINALITY));
					}
				} catch (NumberFormatException e) {
					// ignore this problem in the model
				}
			}
		}

		IProductCmpt target = findTarget();
		if (!willBeValid(target, relation) && target != null && relation != null) {
			String msg = NLS.bind(Messages.ProductCmptRelation_msgInvalidTarget, target.getQualifiedName(), relation.getTargetRoleSingular());
			list.add(new Message(MSGCODE_INVALID_TARGET, msg, Message.ERROR, PROPERTY_TARGET));
		}
		
	}

	/**
	 * @param target
	 *            The product component that will be used as target for the new
	 *            relation.
	 * @param relationType
	 *            The type of the new relation.
	 * @return <code>true</code> if it is possible to create a valid relation
	 *         with the given parameters at this time, <code>false</code>
	 *         otherwise.
	 * @throws CoreException
	 *             if an error occurs during supertype-evaluation
	 */
	public static boolean willBeValid(IProductCmpt target, IProductCmptTypeRelation relationType) throws CoreException {
		if (target == null || relationType == null) {
			return false;
		}
		
		boolean valid = false;

		IRelation policyRelation = null;
		policyRelation = relationType.findPolicyCmptTypeRelation();
		
		IPolicyCmptType targetType = target.findPolicyCmptType();
		if (targetType == null) {
			return false;
		}
		
		IPolicyCmptType[] targetTypes = targetType.getSupertypeHierarchy().getAllSupertypesInclSelf(targetType);
		
		
		IPolicyCmptType type = null;
		if (policyRelation != null) {
			type = policyRelation.findTarget();
		}
		if (type != null) {
			ITypeHierarchy hierarchy = type.getSupertypeHierarchy();
			
			IPolicyCmptType[] types = hierarchy.getAllSupertypesInclSelf(type);
			
			for (int i = 0; i < types.length && !valid; i++) {
				for (int j = 0; j < targetTypes.length && !valid; j++) {
					valid = targetTypes[j].getQualifiedName().equals(types[i].getQualifiedName());
				}
			} 
			
			hierarchy = type.getSubtypeHierarchy();
			types = hierarchy.getAllSubtypes(type);
			for (int i = 0; i < types.length && !valid; i++) {
				valid = target.getPolicyCmptType().equals(types[i].getQualifiedName()); 
			} 
		}
		
		return valid;
	}
	
	/**
	 * Overridden.
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}

	/**
	 * Overridden.
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		productCmptTypeRelation = element
				.getAttribute(PROPERTY_PCTYPE_RELATION);
		target = element.getAttribute(PROPERTY_TARGET);
		targetRuntimeId = element.getAttribute(PROPERTY_TARGET_RUNTIME_ID);
		try {
			minCardinality = Integer.parseInt(element
					.getAttribute(PROPERTY_MIN_CARDINALITY));
		} catch (NumberFormatException e) {
			minCardinality = 0;
		}
		String max = element.getAttribute(PROPERTY_MAX_CARDINALITY);
		if (max.equals("*")) { //$NON-NLS-1$
			maxCardinality = CARDINALITY_MANY;
		} else {
			try {
				maxCardinality = Integer.parseInt(max);
			} catch (NumberFormatException e) {
				maxCardinality = 0;
			}
		}
	}

	/**
	 * Overridden.
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_PCTYPE_RELATION, productCmptTypeRelation);
		element.setAttribute(PROPERTY_TARGET, target);
		element.setAttribute(PROPERTY_TARGET_RUNTIME_ID, targetRuntimeId);
		element.setAttribute(PROPERTY_MIN_CARDINALITY, "" + minCardinality); //$NON-NLS-1$

		if (maxCardinality == CARDINALITY_MANY) {
			element.setAttribute(PROPERTY_MAX_CARDINALITY, "*"); //$NON-NLS-1$
		} else {
			element.setAttribute(PROPERTY_MAX_CARDINALITY, "" + maxCardinality); //$NON-NLS-1$
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
}
