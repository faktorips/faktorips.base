package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsObject;



/**
 * The policy component type represents a Java class that is part of a policy class model.
 */
public interface IPolicyCmptType extends IIpsObject, Datatype {
    
    /**
     * The name of the supertype property.
     */
    public final static String PROPERTY_SUPERTYPE = "supertype";
    
    /**
     * The name of the abstract property.
     */
    public final static String PROPERTY_ABSTRACT = "abstract";
    
    /**
     * The name of the abstract property.
     */
    public final static String PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU = "forceExtensionCompilationUnitGeneration";
    
    /**
     * The Java type that contains the policy component implementation.
     * 
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getJavaCompilationUnit(int)
     */
    public final static int JAVA_POLICY_CMPT_IMPLEMENTATION_TYPE = 0;
    
    /**
     * The Java type that contains the policy component extension implementation.
     * The extension implementation is the implementation where the developer using
     * FaktorIps places his own code.
     * @deprecated
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getJavaCompilationUnit(int)
     */
    public final static int JAVA_POLICY_CMPT_EXTENSTION_IMPLEMENTATION_TYPE = 1;
    
    /**
     * The Java type that contains the policy component's published interface.
     * @deprecated
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getJavaCompilationUnit(int)
     */
    public final static int JAVA_POLICY_CMPT_PUBLISHED_INTERFACE_TYPE = 2;
    
    /**
     * The Java type that contains the product component implementation.
     * @deprecated
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getJavaCompilationUnit(int)
     */
    public final static int JAVA_PRODUCT_CMPT_IMPLEMENTATION_TYPE = 3;
    
    /**
     * The Java type that contains the product component's published interface.
     * @deprecated
     * @see org.faktorips.devtools.core.model.IIpsSrcFile#getJavaCompilationUnit(int)
     */
    public final static int JAVA_PRODUCT_CMPT_PUBLISHED_INTERFACE_TYPE = 4;
    
    /**
     * Validation message code to indicate that an abstract method exists in the type's supertype
     * hierarchy that must be overriden in the conrete type.
     */
    public final static String VLDCODE_MUST_OVERRIDE_ABSTRACT_METHOD = "PCTYPE-MustOverrideAbstractMethod";
    
    /**
     * Returns the qualified name of the type's supertype. Returns an empty
     * string if this type has no supertype.
     */
    public String getSupertype();
    
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
     * If an extension compilation unit is generated for this policy component type, the extension type (defined
     * in the compilation unit) is returned. Otherwise the base implementation type is returned.
     * @deprecated
     * @throws CoreException if an error occurs while accesing the Java type.
     */
    public IType getJavaImplementationType() throws CoreException;

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
    public IMethod[] findOverrideCandidates(boolean onlyAbstractMethods) throws CoreException;
    
    /**
     * Creates new methods in this type that overrides the given methods.
     * Note that it is not checked, if the methods really belong to one of
     * the type's supertypes.
     */
    public IMethod[] override(IMethod[] methods);
    
    /**
     * Returns the type's vallidation rules.
     */
    public IValidationRuleDef[] getRules();

    /**
     * Creates a new validation rule and returns it.
     */
    public IValidationRuleDef newRule();

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
     * @throws IllegalArgumentException if name is null.
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
     * Creates a new supertype hierarchy for the type and returns it.
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException;
    
    /**
     * Creates a new subtype hierarchy for the type and returns it.
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException;
    
}
