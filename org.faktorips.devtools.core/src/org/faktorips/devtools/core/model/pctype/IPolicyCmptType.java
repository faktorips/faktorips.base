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

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;



/**
 * The policy component type represents a Java class that is part of a policy class model.
 */
public interface IPolicyCmptType extends IIpsObject, Datatype {
    
	
    /**
     * The name of the product component type property.
     */
    public final static String PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE = "configurableByProductCmptType"; //$NON-NLS-1$

	/**
     * The name of the product component type property.
     */
    public final static String PROPERTY_UNQUALIFIED_PRODUCT_CMPT_TYPE = "unqualifiedProductCmptType"; //$NON-NLS-1$

    /**
     * The name of the supertype property.
     */
    public final static String PROPERTY_SUPERTYPE = "supertype"; //$NON-NLS-1$
    
    /**
     * The name of the abstract property.
     */
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$
    
    /**
     * The name of the abstract property.
     */
    public final static String PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU = "forceExtensionCompilationUnitGeneration"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "POLICYCMPTTYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an abstract method exists in the type's supertype
     * hierarchy that must be overriden in the conrete type.
     */
    public final static String MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD = MSGCODE_PREFIX + "MustOverrideAbstractMethod"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the supertype hierarchy contains a cycle.
     */
    public final static String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "SupertypeHierarchyContainsCycle"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a readonly-container relation exists in the type's supertype
     * hierarchy that must be implemented, or the type must also be abstract.
     */
    public final static String MSGCODE_MUST_IMPLEMENT_CONTAINER_RELATION = MSGCODE_PREFIX + "MustImplementContainerRelation"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that there exists an error within the type hierarchy of this type.
     */
    public final static String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierarchy"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the supertype can not be found.
     */
    public final static String MSGCODE_SUPERTYPE_NOT_FOUND = 
    	MSGCODE_PREFIX + "SupertypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined configurable by 
     * product, but the product cmpt type name is not set.
     */
    public final static String MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING = 
    	MSGCODE_PREFIX + "ProductCmptTypeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined configurable by 
     * product, but the product cmpt type name is not set.
     */
    public final static String MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSMATCH = 
    	MSGCODE_PREFIX + "ProductCmptTypeNameMissmatch"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that at least one abstract method is not implemented, but this
     * policy component type is not marked as abstract.
     */
    public final static String MSGCODE_ABSTRACT_MISSING = 
    	MSGCODE_PREFIX + "AbstractMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the product component type is not a valid
     * java identifier.
     */
    public final static String MSGCODE_INVALID_PRODUCT_CMPT_TYPE_NAME = 
    	MSGCODE_PREFIX + "InvalidProductCmptTypeName"; //$NON-NLS-1$
    
    /**
     * Returns <code>true</code> if this class has a corresponding product component type,
     * otherwise <code>false</code>.
     */
    public boolean isConfigurableByProductCmptType();
    
    /**
     * Sets if this policy component type has a corresponding product component type or not.
     */
    public void setConfigurableByProductCmptType(boolean newValue);
    
    /**
     * Returns the qualified name of the product component type. Returns an empty string if this policy
     * component type has no corresponding product component type.
     */
    public String getProductCmptType();
    
    /**
     * Returns the product component type this type refers to.  
     * Returns <code>null</code> if either this type does not refer to a product component type
     * or the product component type can't be found on the project's ips object path.
     * This policy component type does not refer to a product component type if the  
     * <code>ConfigurableByProductCmptType</code> property is <code>false</code>.
     *
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType() throws CoreException;

    /**
     * Returns the unqualified name of the product component type. Returns an empty
     * string if no product component type is specified.
     */
    public String getUnqualifiedProductCmptType();

    /**
     * Sets the unqualified name of the product component type.
     */
    public void setUnqualifiedProductCmptType(String unqualifiedName);
    
    /**
     * Returns the qualified name of the type's supertype. Returns an empty
     * string if this type has no supertype.
     */
    public String getSupertype();
    
    /**
     * Returns <code>true</code> if this type has a supertype, otherwise <code>false</code>.
     * This method also returns <code>true</code> if the type refers to a supertype but the 
     * supertype does not exist.
     */
    public boolean hasSupertype();
    
    /**
     * Returns the type's supertype if the type is based on a supertype and the supertype can be found
     * on the project's ips object path. Returns <code>null</code> if either this type is not based on
     * a supertype or the supertype can't be found on the project's ips object path. 

     * @throws CoreException if an error occurs while searching for the supertype.
     */
    public IPolicyCmptType findSupertype() throws CoreException;
    
    /**
     * Sets the type's supertype.
     * 
     * @throws IllegalArgumentException if newSupertype is null.
     */
    public void setSupertype(String newSupertype);
    
    /**
     * Returns true if the type is abstract, otherwise false.
     */
    public boolean isAbstract();
    
    /**
     * Sets the type's abstract property.
     */
    public void setAbstract(boolean newValue);
    
    /**
     * Returns <code>true</code> if an exension comilation unit is generated whether it is neccessary 
     * because of the presence of none abstract methods or valition rules or not. 
     */
    public boolean isForceExtensionCompilationUnitGeneration();
    
    /**
     * Sets if an exension comilation unit should be generated in any case.
     * <p>
     * The developer using FaktorIps can set this property, if he wants to override methods for
     * relation and attribute handling that are normally not overridden. 
     */
    public void setForceExtensionCompilationUnitGeneration(boolean flag);
    
    /**
     * Returns <code>true</code> if an extension Java compilation unit should exists for policy component type,
     * where the developer using FaktorIps can add or override code.
     * An extension compilation unit exists in the following cases:
     * <ol>
     * 	<li>The policy component type has a none abstract method.
     * 	<li>The policy component type has a validation rule.
     * 	<li>The policy component type has a computed or derived attribute that is not product relevant.
     * 	<li>The flag forceGenerationOfExtensionCompilationUnit is set.
     * </ol>
     *   
     * Returns <code>false</code> otherwise. 
     */
    public boolean isExtensionCompilationUnitGenerated();
    
    /**
     * Returns the type's attributes.
     */
    public IAttribute[] getAttributes();
    
    /**
     * Returns the attribute with the given name. If more than one attribute
     * with the name exist, the first attribute with the name is returned.
     * Returns <code>null</code> if no attribute with the given name exists. 
     */
    public IAttribute getAttribute(String name);
    
    /**
     * Creates a new attribute and returns it.
     */
    public IAttribute newAttribute();

    /**
     * Returns the number of attributes.
     */
    public int getNumOfAttributes();
    
    /**
     * Moves the attributes identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first attribute), no attribute is moved up. 
     * If one of the indexes is the number of attributes - 1 (the last attribute)
     * no attribute is moved down. 
     * 
     * @param indexes	The indexes identifying the attributes.
     * @param up 		<code>true</code>, to move the attributes up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * an attribute.
     */
    public int[] moveAttributes(int[] indexes, boolean up);
    
    /**
     * Returns the type's methods. 
     */
    public IMethod[] getMethods();
    
    /**
     * Creates a new method and returns it.
     */
    public IMethod newMethod();

    /**
     * Returns the number of methods.
     */
    public int getNumOfMethods();
    
    /**
     * Moves the methods identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first method), no method is moved up. 
     * If one of the indexes is the number of methods - 1 (the last method)
     * no method is moved down. 
     * 
     * @param indexes	The indexes identifying the methods.
     * @param up 		<code>true</code>, to move the methods up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved methods.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a method.
     */
    public int[] moveMethods(int[] indexes, boolean up);
    
    /**
     * Returns true if this type has a same method as the indicated one.
     * Two methods are considered to be same when they have the same name,
     * the same number of parameters and the parameter's datatypes are equal. 
     */
    public boolean hasSameMethod(IMethod method);
    
    /**
     * Returns the method that matches the indicated one regarding it's signature. Two methods match if they have 
     * the same name, the same number of parameters and the parameter's datatypes are equal.
     * Returns <code>null</code> if the type does not contain a matching method or the indicated method is 
     * <code>null</code>. 
     */
    public IMethod getMatchingMethod(IMethod method);

    /**
     * Returns a list of methods defined in any of the type's supertypes
     * that can be overriden (and isn't overriden yet).
     * 
     * @param onlyAbstractMethods if true only abstract methods are returned.
     */
    public IMethod[] findOverrideMethodCandidates(boolean onlyAbstractMethods) throws CoreException;
    
    /**
     * Returns an array of all attributes of all supertypes not yet overwritten by this 
     * policy component type.
     */
    public IAttribute[] findOverrideAttributeCandidates() throws CoreException;
    
    /**
     * Creates new methods in this type that overrides the given methods.
     * Note that it is not checked, if the methods really belong to one of
     * the type's supertypes.
     */
    public IMethod[] overrideMethods(IMethod[] methods);
    
    /**
     * Creates new attributes in this type overriding the given attributes.
     * Note that it is not checked, if the attributes really belong to one of
     * the type's supertypes.
     * 
     * @return The created attributes.
     */
    public IAttribute[] overrideAttributes(IAttribute[] attributes);
    
    /**
     * Returns the type's vallidation rules.
     */
    public IValidationRule[] getRules();

    /**
     * Creates a new validation rule and returns it.
     */
    public IValidationRule newRule();

    /**
     * Returns the number of rules.
     */
    public int getNumOfRules();
    
    /**
     * Moves the rules identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first rule), no rule is moved up. 
     * If one of the indexes is the number of rules - 1 (the last rule)
     * no rule is moved down. 
     * 
     * @param indexes	The indexes identifying the rules.
     * @param up 		<code>true</code>, to move the rules up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved rules.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a rule.
     */
    public int[] moveRules(int[] indexes, boolean up);

    /**
     * Returns <code>true</code> if this class represents the root of a complex aggregate,
     * otherwise <code>false</code>. For example an insurance policy is complex aggregate that
     * consist of a policy class itself but also of coverages, insured persons, etc. In this
     * case the policy class is the root of the complex policy aggregate.
     * <p>
     * A policy component type is considered an aggregate root if it and it's supertypes 
     * havn't got a reverse composite relation. 
     * 
     * @throws CoreException if an error occurs while searching the supertype hierarchy. 
     */
    public boolean isAggregateRoot() throws CoreException;
    
    /**
     * Returns the type's relations.
     */
    public IRelation[] getRelations();
    
    /**
     * Returns the relations of this type that are indicated as product relevant.
     */
    public IRelation[] getProductRelevantRelations();

    /**
     * Returns the first relation with the indicated name or null if
     * no such relation exists.
     * <p>
     * Note that a relation's name is equal to it's target role, so you
     * can also use the target role as parameter.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>.
     */
    public IRelation getRelation(String name);

    /**
     * Creates a new relation and returns it.
     */
    public IRelation newRelation();
    
    /**
     * Returns the number of relations.
     */
    public int getNumOfRelations();
    
    /**
     * Moves the relations identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first relation), no relation is moved up. 
     * If one of the indexes is the number of relations - 1 (the last relation)
     * no relation is moved down. 
     * 
     * @param indexes	The indexes identifying the relations.
     * @param up 		<code>true</code>, to move the relations up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved relations.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a relation.
     */
    public int[] moveRelations(int[] indexes, boolean up);
    
    /**
     * Returns the type's relations implementing the given container relation.
     * Returns an empty array if no such relation exists or containerRelation is <code>null</code>.
     * 
     * @param containerRelation the container relation to search implementing relations for
     * @param seachSupertypeHierarchy if also the type's supertypes should be searched.
     * 
     * @throws CoreException if an exception occurs while searching
     */
    public IRelation[] findRelationsImplementingContainerRelation(IRelation containerRelation, boolean searchSupertypeHierarchy) throws CoreException;
    
    /**
     * Creates a new supertype hierarchy for the type and returns it.
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException;
    
    /**
     * Creates a new subtype hierarchy for the type and returns it.
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException;
    
}
