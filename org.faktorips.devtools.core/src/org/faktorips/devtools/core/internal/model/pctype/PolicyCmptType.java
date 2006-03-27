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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of IPolicyCmptType.
 * 
 * @author Jan Ortmann
 */
public class PolicyCmptType extends IpsObject implements IPolicyCmptType {

	private boolean configurableByProductCmptType = false;

	private String unqalifiedProductCmptType = ""; //$NON-NLS-1$

	private String supertype = ""; //$NON-NLS-1$

	private boolean abstractFlag = false;

	private boolean forceExtensionCompilationUnitGeneration = false;

	private List attributes = new ArrayList(0);

	private List methods = new ArrayList(0);

	private List relations = new ArrayList(0);

	private List rules = new ArrayList(0);

	public PolicyCmptType(IIpsSrcFile file) {
		super(file);
	}

	/**
	 * Constructor for testing purposes.
	 */
	PolicyCmptType() {
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProductCmptType() {
		return getIpsPackageFragment().getName() + '.'
				+ unqalifiedProductCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isConfigurableByProductCmptType() {
		return configurableByProductCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfigurableByProductCmptType(boolean newValue) {
		boolean oldValue = configurableByProductCmptType;
		configurableByProductCmptType = newValue;
		valueChanged(oldValue, newValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptType findProductCmptType() throws CoreException {
		if (!configurableByProductCmptType) {
			return null;
		}
		if (StringUtils.isEmpty(unqalifiedProductCmptType)) {
			return null;
		}
		return new ProductCmptType(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUnqualifiedProductCmptType() {
		return unqalifiedProductCmptType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUnqualifiedProductCmptType(String newUnqualifiedName) {
		ArgumentCheck.notNull(newUnqualifiedName);
		String oldName = unqalifiedProductCmptType;
		unqalifiedProductCmptType = newUnqualifiedName;
		valueChanged(oldName, newUnqualifiedName);
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		if (partType.equals(IAttribute.class)) {
			return newAttribute();
		} else if (partType.equals(IMethod.class)) {
			return newMethod();
		} else if (partType.equals(IRelation.class)) {
			return newRelation();
		} else if (partType.equals(IValidationRule.class)) {
			return newRule();
		}
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getSupertype() {
		return supertype;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasSupertype() {
		return StringUtils.isNotEmpty(supertype);
	}

	/**
	 * {@inheritDoc}
	 */
	public IPolicyCmptType findSupertype() throws CoreException {
		if (StringUtils.isEmpty(supertype)) {
			return null;
		}
		return getIpsProject().findPolicyCmptType(supertype);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSupertype(String newSupertype) {
		String oldSupertype = supertype;
		supertype = newSupertype;
		valueChanged(oldSupertype, newSupertype);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isForceExtensionCompilationUnitGeneration() {
		return forceExtensionCompilationUnitGeneration;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setForceExtensionCompilationUnitGeneration(boolean flag) {
		boolean oldValue = forceExtensionCompilationUnitGeneration;
		forceExtensionCompilationUnitGeneration = flag;
		valueChanged(oldValue, forceExtensionCompilationUnitGeneration);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExtensionCompilationUnitGenerated() {
		if (forceExtensionCompilationUnitGeneration) {
			return true;
		}
		if (getNumOfRules() > 0) {
			return true;
		}
		for (Iterator it = methods.iterator(); it.hasNext();) {
			IMethod method = (IMethod) it.next();
			if (!method.isAbstract()) {
				return true;
			}
		}
		for (Iterator it = attributes.iterator(); it.hasNext();) {
			IAttribute attribute = (IAttribute) it.next();
			if (attribute.getAttributeType() == AttributeType.COMPUTED) {
				if (!attribute.isProductRelevant()) {
					return true;
				}
			} else if (attribute.getAttributeType() == AttributeType.DERIVED) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
		int numOfChildren = getNumOfAttributes() + getNumOfMethods()
				+ getNumOfRelations() + getNumOfRules();
		IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
		List childrenList = new ArrayList(numOfChildren);
		childrenList.addAll(attributes);
		childrenList.addAll(methods);
		childrenList.addAll(relations);
		childrenList.addAll(rules);
		childrenList.toArray(childrenArray);
		return childrenArray;
	}

	/**
	 * {@inheritDoc}
	 */
	public IAttribute[] getAttributes() {
		IAttribute[] a = new IAttribute[attributes.size()];
		attributes.toArray(a);
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public IAttribute getAttribute(String name) {
		for (Iterator it = attributes.iterator(); it.hasNext();) {
			IAttribute a = (IAttribute) it.next();
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumOfAttributes() {
		return attributes.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public IAttribute newAttribute() {
		Attribute a = newAttributeInternal(getNextPartId());
		updateSrcFile();
		return a;
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] moveAttributes(int[] indexes, boolean up) {
		ListElementMover mover = new ListElementMover(attributes);
		return mover.move(indexes, up);
	}

	/*
	 * Returns the list holding the attributes as a reference. 
	 * Package private for use in TypeHierarchy.  
	 */
	List getAttributeList() {
		return attributes;
	}

	/*
	 * Creates a new attribute without updating the src file.
	 */
	private Attribute newAttributeInternal(int id) {
		Attribute a = new Attribute(this, id);
		attributes.add(a);
		return a;
	}

	/**
	 * Removes the attribute from the type. 
	 */
	void removeAttribute(Attribute attribute) {
		attributes.remove(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	public IMethod[] getMethods() {
		IMethod[] m = new IMethod[methods.size()];
		methods.toArray(m);
		return m;
	}

	/*
	 * Returns the list holding the methods as a reference. 
	 * Package private for use in TypeHierarchy.  
	 */
	List getMethodList() {
		return methods;
	}

	/**
	 * {@inheritDoc}
	 */
	public IMethod newMethod() {
		IMethod m = newMethodInternal(getNextPartId());
		updateSrcFile();
		return m;
	}

	/*
	 * Creates a new attribute without updating the src file.
	 */
	private Method newMethodInternal(int id) {
		Method m = new Method(this, id);
		methods.add(m);
		return m;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumOfMethods() {
		return methods.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] moveMethods(int[] indexes, boolean up) {
		ListElementMover mover = new ListElementMover(methods);
		return mover.move(indexes, up);
	}

	/**
	 * Removes the method from the type. 
	 */
	void removeMethod(Method method) {
		methods.remove(method);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws CoreException 
	 */
	public boolean isAggregateRoot() throws CoreException {
		for (Iterator it = relations.iterator(); it.hasNext();) {
			IRelation each = (IRelation) it.next();
			if (each.getRelationType().isReverseComposition()) {
				return false;
			}
		}
		IPolicyCmptType supertype = findSupertype();
		if (supertype != null) {
			return supertype.isAggregateRoot();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public IRelation[] getRelations() {
		IRelation[] r = new IRelation[relations.size()];
		relations.toArray(r);
		return r;
	}

	/*
	 * Returns the list holding the relations as a reference.
	 * Package private for use in TypeHierarchy.  
	 */
	List getRelationList() {
		return relations;
	}

	/**
	 * {@inheritDoc}
	 */
	public IRelation getRelation(String name) {
		ArgumentCheck.notNull(name);
		for (Iterator it = relations.iterator(); it.hasNext();) {
			IRelation each = (IRelation) it.next();
			if (name.equals(each.getName())) {
				return each;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IRelation[] getProductRelevantRelations() {
		ArrayList productRelevantRelations = new ArrayList();
		for (Iterator it = relations.iterator(); it.hasNext();) {
			IRelation relation = (IRelation) it.next();
			if (relation.isProductRelevant()) {
				productRelevantRelations.add(relation);
			}
		}
		return (IRelation[]) productRelevantRelations
				.toArray(new IRelation[productRelevantRelations.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumOfRelations() {
		return relations.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] moveRelations(int[] indexes, boolean up) {
		ListElementMover mover = new ListElementMover(relations);
		return mover.move(indexes, up);
	}

	/**
	 * {@inheritDoc}
	 */
	public IRelation newRelation() {
		Relation r = newRelationInternal(getNextPartId());
		updateSrcFile();
		return r;
	}

	/*
	 * Creates a new attribute without updating the src file.
	 */
	private Relation newRelationInternal(int id) {
		Relation r = new Relation(this, id);
		relations.add(r);
		return r;
	}

	/**
	 * Removes the attribute from the type. 
	 */
	void removeRelation(Relation relation) {
		relations.remove(relation);
	}

	/**
	 * {@inheritDoc}
	 */
	public IValidationRule[] getRules() {
		IValidationRule[] r = new IValidationRule[rules.size()];
		rules.toArray(r);
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	public IValidationRule newRule() {
		IValidationRule r = newRuleInternal(getNextPartId());
		updateSrcFile();
		return r;
	}

	/*
	 * Creates a new rule without updating the src file.
	 */
	private IValidationRule newRuleInternal(int id) {
		ValidationRule r = new ValidationRule(this, id);
		rules.add(r);
		return r;
	}

	/** 
	 * {@inheritDoc}
	 */
	public int getNumOfRules() {
		return rules.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] moveRules(int[] indexes, boolean up) {
		ListElementMover mover = new ListElementMover(rules);
		return mover.move(indexes, up);
	}

	/**
	 * Removes the method from the type. 
	 */
	void removeRule(ValidationRule rule) {
		rules.remove(rule);
	}

	/**
	 * {@inheritDoc}
	 */
	public IpsObjectType getIpsObjectType() {
		return IpsObjectType.POLICY_CMPT_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		configurableByProductCmptType = Boolean.valueOf(
				element.getAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE))
				.booleanValue();
		unqalifiedProductCmptType = element
				.getAttribute(PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE);
		supertype = element.getAttribute(PROPERTY_SUPERTYPE);
		abstractFlag = Boolean.valueOf(element.getAttribute(PROPERTY_ABSTRACT))
				.booleanValue();
		forceExtensionCompilationUnitGeneration = Boolean
				.valueOf(
						element
								.getAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU))
				.booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reinitPartCollections() {
		attributes.clear();
		methods.clear();
		rules.clear();
		relations.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void reAddPart(IIpsObjectPart part) {
		if (part instanceof IAttribute) {
			attributes.add(part);
			return;
		} else if (part instanceof IMethod) {
			methods.add(part);
			return;
		} else if (part instanceof IRelation) {
			relations.add(part);
			return;
		} else if (part instanceof IValidationRule) {
			rules.add(part);
			return;
		}
		throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected IIpsObjectPart newPart(Element xmlTag, int id) {
		String xmlTagName = xmlTag.getNodeName();
		if (xmlTagName.equals(Attribute.TAG_NAME)) {
			return newAttributeInternal(id);
		} else if (xmlTagName.equals(Relation.TAG_NAME)) {
			return newRelationInternal(id);
		} else if (xmlTagName.equals(Method.TAG_NAME)) {
			return newMethodInternal(id);
		} else if (xmlTagName.equals(ValidationRule.TAG_NAME)) {
			return newRuleInternal(id);
		}
		throw new RuntimeException(
				"Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void propertiesToXml(Element newElement) {
		super.propertiesToXml(newElement);
		newElement.setAttribute(PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE,
				"" + configurableByProductCmptType); //$NON-NLS-1$
		newElement.setAttribute(PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE,
				unqalifiedProductCmptType);
		newElement.setAttribute(PROPERTY_SUPERTYPE, supertype);
		newElement.setAttribute(PROPERTY_ABSTRACT, "" + abstractFlag); //$NON-NLS-1$
		newElement.setAttribute(PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU,
				"" + forceExtensionCompilationUnitGeneration); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list) throws CoreException {
		TypeHierarchy supertypeHierarchy = null;
		try {
			supertypeHierarchy = TypeHierarchy.getSupertypeHierarchy(this);
		} catch (CycleException e) {
			StringBuffer msg = new StringBuffer(
					"Cycle detected in type hierarchy: ");
			IIpsElement[] path = e.getCyclePath();
			for (int i = 0; i < path.length; i++) {
				msg.append(path[i].getName());
				if (i + 1 < path.length) {
					msg.append(" --> ");
				}
			}
			list.add(new Message(MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg
					.toString(), Message.ERROR, this));
			return;
		}

		validateSupertypeHierarchy(supertypeHierarchy, list);

		IPolicyCmptType supertypeObj = null;
		if (!supertype.equals("")) { //$NON-NLS-1$
			supertypeObj = (IPolicyCmptType) getIpsProject().findIpsObject(
					IpsObjectType.POLICY_CMPT_TYPE, supertype);
			if (supertypeObj == null) {
				String text = NLS
						.bind(Messages.PolicyCmptType_msgSupertypeNotFound,
								supertype);
				list
						.add(new Message(MSGCODE_SUPERTYPE_NOT_FOUND, text,
								Message.ERROR, this,
								IPolicyCmptType.PROPERTY_SUPERTYPE)); //$NON-NLS-1$
			}
		}
		if (isConfigurableByProductCmptType()
				&& StringUtils.isEmpty(this.unqalifiedProductCmptType)) {
			String text = Messages.PolicyCmptType_msgNameMissing;
			list.add(new Message(MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING, text,
					Message.ERROR, this,
					IPolicyCmptType.PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE)); //$NON-NLS-1$
		}
		if (!isAbstract()) {
			validateIfAllAbstractMethodsAreImplemented(list);
			IMethod[] methods = getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].isAbstract()) {
					String text = Messages.PolicyCmptType_msgAbstractMissmatch;
					list.add(new Message(MSGCODE_ABSTRACT_MISSING, text,
							Message.ERROR, this,
							IPolicyCmptType.PROPERTY_ABSTRACT)); //$NON-NLS-1$
				}
			}

			if (findUnresolvedAbstractRelations().length > 0) {
				String text = "Policy component has to be marked as abstract because not all abstract relations (relations marked as read only container) are implemented.";
				list.add(new Message(MSGCODE_MUST_IMPLEMENT_ABSTRACT_RELATION,
						text, Message.ERROR, this, PROPERTY_ABSTRACT));
			}
		}
	}

	/**
	 * Returns an array of all relations of this type and all of its supertypes
	 * which are marked as read-only Container (which means they are abstract) 
	 * and not implemented somewhere in the supertype hierarchy.
	 */
	private IRelation[] findUnresolvedAbstractRelations() throws CoreException {
		IPolicyCmptType[] types = this.getSupertypeHierarchy()
				.getAllSupertypesInclSelf(this);
		List result = new ArrayList();
		List abstractRelations = new ArrayList();
		Map nonAbstractRelations = new HashMap();
		for (int i = 0; i < types.length; i++) {
			IRelation[] relations = types[i].getRelations();
			for (int j = 0; j < relations.length; j++) {
				if (relations[j].isReadOnlyContainer()) {
					abstractRelations.add(relations[j]);
				} else {
					nonAbstractRelations.put(relations[j]
							.getTargetRoleSingular(), relations[j]);
				}
			}
		}

		for (Iterator iter = abstractRelations.iterator(); iter.hasNext();) {
			IRelation element = (IRelation) iter.next();
			if (!nonAbstractRelations.containsKey(element.getTarget())) {
				result.add(element);
			}
		}
		return (IRelation[]) result.toArray(new IRelation[result.size()]);
	}

	private void validateIfAllAbstractMethodsAreImplemented(MessageList list)
			throws CoreException {
		ITypeHierarchy hierarchy = getSupertypeHierarchy();
		if (hierarchy.getSupertype(this) == null) {
			return;
		}
		IMethod[] methods = hierarchy.getAllMethods(hierarchy
				.getSupertype(this));
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isAbstract()) {
				if (!isAbstractMethodImplemented(this, methods[i], hierarchy)) {
					String text = NLS
							.bind(
									Messages.PolicyCmptType_msgMustOverrideAbstractMethod,
									methods[i].getName(), methods[i]
											.getPolicyCmptType()
											.getQualifiedName());
					list
							.add(new Message(
									IPolicyCmptType.MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD,
									text, Message.ERROR, this));
				}
			}
		}
	}

	/**
	 * Checks if an MSGCODE_SUPERTYPE_NOT_FOUND error in the supertype hirarchy exists. 
	 * If so, this is reported with a new message with code MSGCODE_INCONSISTENT_TYPE_HIERARCHY 
	 * in the given list. The message(s) returned by the supertype ar not added.
	 */
	private void validateSupertypeHierarchy(TypeHierarchy supertypeHierarchy,
			MessageList ml) {
		if (supertypeHierarchy == null) {
			return;
		}
		IPolicyCmptType supertype = supertypeHierarchy.getSupertype(this);
		if (supertype == null) {
			return;
		}
		try {

			MessageList tmpList = supertype.validate();
			Message msg = tmpList
					.getMessageByCode(MSGCODE_INCONSISTENT_TYPE_HIERARCHY);
			if (msg != null) {
				ml.add(msg);
			} else if (tmpList.getMessageByCode(MSGCODE_SUPERTYPE_NOT_FOUND) != null) {
				ml
						.add(new Message(
								MSGCODE_INCONSISTENT_TYPE_HIERARCHY,
								"An error exists within the type hierarchy of this type.",
								Message.ERROR));
			}

		} catch (Exception e) {
			IpsPlugin.log(e);
		}
	}

	/**
	 * Returns true if the method is implemented in the indicated type or one of the type's supertypes.
	 * 
	 * @param pcType The policy component type where the search for the implementation starts.
	 * @param method An abstract method of one the type's supertypes.
	 * @param hierarchy The supertype hierarchy where the supertype relation is already resolved.
	 */
	private boolean isAbstractMethodImplemented(IPolicyCmptType pcType,
			IMethod method, ITypeHierarchy hierarchy) {
		IMethod match = pcType.getMatchingMethod(method);
		if (match != null && !match.isAbstract()) {
			return true;
		}
		IPolicyCmptType supertype = hierarchy.getSupertype(pcType);
		if (supertype == null) {
			return false;
		}
		return isAbstractMethodImplemented(supertype, method, hierarchy);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAbstract() {
		return abstractFlag;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAbstract(boolean newValue) {
		boolean oldValue = abstractFlag;
		abstractFlag = newValue;
		valueChanged(oldValue, newValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public ITypeHierarchy getSupertypeHierarchy() throws CoreException {
		try {
			return TypeHierarchy.getSupertypeHierarchy(this);
		} catch (CycleException e) {
			IpsStatus status = new IpsStatus("Cycle in Type Hierarchy", e);
			throw new CoreException(status);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ITypeHierarchy getSubtypeHierarchy() throws CoreException {
		try {
			return TypeHierarchy.getSubtypeHierarchy(this);
		} catch (CycleException e) {
			IpsStatus status = new IpsStatus("Cycle in Type Hierarchy", e);
			throw new CoreException(status);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IMethod[] findOverrideCandidates(boolean onlyAbstractMethods)
			throws CoreException {
		List candidates = new ArrayList();
		ITypeHierarchy hierarchy = getSupertypeHierarchy();
		IPolicyCmptType[] supertypes = hierarchy.getAllSupertypes(this);
		for (int i = 0; i < supertypes.length; i++) {
			getOverrideCandidates(supertypes[i], onlyAbstractMethods,
					candidates);
		}
		return (IMethod[]) candidates.toArray(new IMethod[candidates.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public QualifiedNameType[] dependsOn() throws CoreException {
		ArrayList qualifiedNameTypes = new ArrayList();
		qualifiedNameTypes.add(new QualifiedNameType(getSupertype(),
				IpsObjectType.POLICY_CMPT_TYPE));
		addQualifiedNameTypesForRelationTargets(this, qualifiedNameTypes);
		return (QualifiedNameType[]) qualifiedNameTypes
				.toArray(new QualifiedNameType[qualifiedNameTypes.size()]);
	}

	private void addQualifiedNameTypesForRelationTargets(
			IPolicyCmptType policyCmptType, List qualifiedNameTypes) {
		IRelation[] relations = policyCmptType.getRelations();
		for (int i = 0; i < relations.length; i++) {
			String qualifiedName = relations[i].getTarget();
			qualifiedNameTypes.add(new QualifiedNameType(qualifiedName,
					IpsObjectType.POLICY_CMPT_TYPE));
		}
	}

	/*
	 * helper method for getOverrideCandidates
	 */
	private void getOverrideCandidates(IPolicyCmptType type,
			boolean onlyAbstractMethods, List candidates) {
		IMethod[] supertypeMethods = type.getMethods();
		for (int i = 0; i < supertypeMethods.length; i++) {
			if (supertypeMethods[i].getModifier() != Modifier.PRIVATE
					&& !hasSameMethod(supertypeMethods[i])) {
				if (!onlyAbstractMethods || supertypeMethods[i].isAbstract()) {
					// candidate found, but it might be already in the list
					if (!sameMethodAlreadyInCandidateList(supertypeMethods[i],
							candidates)) {
						candidates.add(supertypeMethods[i]);
					}
				}
			}
		}
	}

	/*
	 * helper method for getOverrideCandidates
	 */
	private boolean sameMethodAlreadyInCandidateList(IMethod method,
			List candidates) {
		for (Iterator it = candidates.iterator(); it.hasNext();) {
			IMethod candidate = (IMethod) it.next();
			if (method.isSame(candidate)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasSameMethod(IMethod method) {
		return getMatchingMethod(method) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IMethod getMatchingMethod(IMethod method) {
		for (Iterator it = this.methods.iterator(); it.hasNext();) {
			IMethod thisMethod = (IMethod) it.next();
			if (thisMethod.isSame(method)) {
				return thisMethod;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IMethod[] override(IMethod[] methods) {
		IMethod[] newMethods = new IMethod[methods.length];
		for (int i = 0; i < methods.length; i++) {
			IMethod override = newMethod();
			override.setModifier(methods[i].getModifier());
			override.setAbstract(false);
			override.setDatatype(methods[i].getDatatype());
			override.setName(methods[i].getName());
			override.setParameters(methods[i].getParameters());
			newMethods[i] = override;
		}
		return newMethods;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isVoid() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValueDatatype() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public IType getJavaImplementationType() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJavaClassName() {
		throw new RuntimeException(
				"getJavaClassName is not supported by " + getClass()); //$NON-NLS-1$
	}

}