/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AssociationType;

/**
 * Common interface for types, policy component type and product component type.
 * 
 * @author Jan Ortmann
 */
public interface IType extends IIpsObject, Datatype {

    public final static String PROPERTY_SUPERTYPE = "supertype"; //$NON-NLS-1$
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "Type-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the supertype hierarchy contains a cycle.
     */
    public final static String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "CycleInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists an error within the type hierarchy of
     * this type.
     */
    public final static String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the supertype can not be found.
     */
    public final static String MSGCODE_SUPERTYPE_NOT_FOUND = MSGCODE_PREFIX + "SupertypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an abstract method exists in the type's supertype
     * hierarchy that must be overridden in the concrete type.
     */
    public final static String MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD = MSGCODE_PREFIX + "MustOverrideAbstractMethod"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one abstract method is defined, but this
     * type is not marked as abstract.
     */
    public final static String MSGCODE_ABSTRACT_MISSING = MSGCODE_PREFIX + "AbstractMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least two properties (attriute, associations)
     * share the same name.
     */
    public final static String MSGCODE_DUPLICATE_PROPERTY_NAME = MSGCODE_PREFIX + "DuplicatePropertyName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a property name is used at least twice.
     */
    public final static String MSGCODE_MUST_SPECIFY_DERIVED_UNION = MSGCODE_PREFIX + "MustSpecifyDerivedUnion"; //$NON-NLS-1$

    public final static String MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS = MSGCODE_PREFIX
            + "OtherTypeWithSameNameExists"; //$NON-NLS-1$

    public final static String MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS = MSGCODE_PREFIX
            + "OtherTypeWithSameNameInDependentProjectExists"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    public boolean isAbstract();

    /**
     * Sets whether this is an abstract type or not.
     */
    public void setAbstract(boolean newValue);

    /**
     * Returns the qualified name of the type's supertype. Returns an empty string if this type has
     * no supertype.
     */
    public String getSupertype();

    /**
     * Returns <code>true</code> if this type has a supertype, otherwise <code>false</code>. This
     * method also returns <code>true</code> if the type refers to a supertype but the supertype
     * does not exist.
     */
    public boolean hasSupertype();

    /**
     * Returns the type's supertype if the type is derived from a supertype and the supertype can be
     * found on the project's ips object path. Returns <code>null</code> if either this type is not
     * derived from a supertype or the supertype can't be found on the project's ips object path.
     * 
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the supertype.
     */
    public IType findSupertype(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the type's supertype.
     * 
     * @throws IllegalArgumentException if newSupertype is null.
     */
    public void setSupertype(String newSupertype);

    /**
     * Returns <code>true</code> if this type is a subtype of the given supertype candidate, returns
     * <code>false</code> otherwise. Returns <code>false</code> if supertype candidate is
     * <code>null</code>.
     * 
     * @param supertypeCandidate The type which is the possibly a supertype of this type
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching the type hierarchy.
     */
    public boolean isSubtypeOf(IType supertypeCandidate, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if this type is a subtype of the given candidate, or if the
     * candidate is this same. Returns <code>false</code> otherwise. Returns <code>false</code> if
     * candidate is <code>null</code>.
     * 
     * @param supertypeCandidate The type which is the possibly a supertype of this type
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching the type hierarchy.
     */
    public boolean isSubtypeOrSameType(IType candidate, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's attributes.
     */
    public IAttribute[] getAttributes();

    /**
     * Returns this type's attributes and the attributes within the supertype hierarchy. It
     * considers overridden attributes. That means if an attribute has been overridden by a subtype
     * this attribute instance will be in the returned array all attributes in the supertype
     * hierarchy with the same name will be neglected.
     * 
     * @param ipsProject TODO
     * 
     * @throws CoreException if an exception occurs while collecting the attributes
     */
    public IAttribute[] findAllAttributes(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type (This method
     * does not search the supertype hierarchy.) If more than one attribute with the name exist, the
     * first attribute with the name is returned. Returns <code>null</code> if no attribute with the
     * given name exists.
     */
    public IAttribute getAttribute(String name);

    /**
     * Searches an attribute with the given name in the type and its supertype hierarchy and returns
     * it. Returns <code>null</code> if no such attribute exists.
     * 
     * @param name The attribute's name.
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException if an error occurs while searching.
     */
    public IAttribute findAttribute(String name, IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a new attribute and returns it.
     */
    public IAttribute newAttribute();

    /**
     * Returns the number of attributes.
     */
    public int getNumOfAttributes();

    /**
     * Moves the attributes identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first attribute), no attribute is moved up. If one of the indexes is the
     * number of attributes - 1 (the last attribute) no attribute is moved down.
     * 
     * @param indexes The indexes identifying the attributes.
     * @param up <code>true</code>, to move the attributes up, <false> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an attribute.
     */
    public int[] moveAttributes(int[] indexes, boolean up);

    /**
     * Returns the association with the given name defined in <strong>this</strong> type. (This
     * method does not search the supertype hierarchy.) If more than one association with the name
     * exist, the first one is returned. Returns <code>null</code> if no association with the given
     * name exists or name is <code>null</code>.
     * 
     * @param name The association's role name singular
     */
    public IAssociation getAssociation(String name);

    /**
     * Returns the association with the given role name in plural form defined in
     * <strong>this</strong> type. (This method does not search the supertype hierarchy.) If more
     * than one association with the name exist, the first one is returned. Returns
     * <code>null</code> if no association with the given name exists or name is <code>null</code>.
     * 
     * @param name The association's role name plural
     */
    public IAssociation getAssociationByRoleNamePlural(String roleNamePlural);

    /**
     * Searches an association with the given name in the type and it's supertype hierarchy and
     * returns it. Returns <code>null</code> if no such association exists.
     * 
     * @param name The association's name.
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IAssociation findAssociation(String name, IIpsProject ipsProject) throws CoreException;

    /**
     * Searches an association with the given role name plural in the type and it's supertype
     * hierarchy and returns it. Returns <code>null</code> if no such association exists.
     * 
     * @param name The association's role name in plural form.
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IAssociation findAssociationByRoleNamePlural(String roleNamePlural, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns all associations that have the indicated target and association type in the current
     * type and it's supertype hierarchy. Returns an empty array if no such association exists or
     * target or association type is <code>null</code>.
     * 
     * @param target The qualified name of the target type.
     * @param associationType The association type
     */
    public IAssociation[] findAssociationsForTargetAndAssociationType(String target,
            AssociationType associationType,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's associations.
     */
    public IAssociation[] getAssociations();

    /**
     * Returns all associations that have the indicated target. Returns an empty array if no such
     * association exists or target is <code>null</code>.
     * <p>
     * Note that this does NOT search the supertype hierarchy.
     * 
     * @param target The qualified name of the target type.
     */
    public IAssociation[] getAssociationsForTarget(String target);

    /**
     * Creates a new association and returns it.
     */
    public IAssociation newAssociation();

    /**
     * Returns the number of associations.
     */
    public int getNumOfAssociations();

    /**
     * Moves the associations identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first association), no association is moved up. If one of the indexes is
     * the number of associations - 1 (the last association) no association is moved down.
     * 
     * @param indexes The indexes identifying the associations.
     * @param up <code>true</code>, to move the associations up, <false> to move them down.
     * 
     * @return The new indexes of the moved associations.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an association.
     */
    public int[] moveAssociations(int[] indexes, boolean up);

    /**
     * Returns all methods of this type including the methods of the types within the supertype
     * hierarchy.
     * 
     * @param ipsProject the ips project that is used to determine the types within the supertype
     *            hierarchy.
     * @throws CoreException if an exception ocurres during the execution of this method
     */
    public List<IMethod> findAllMethods(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's methods.
     */
    public IMethod[] getMethods();

    /**
     * Returns the first method with the given name and the given parameters in this type. This
     * method does not search the supertype hierarchy. Returns <code>null</code> if no such method
     * exists.
     * 
     * @param name The method's name.
     * @param datatypes The datatypes of the method's parameters.
     */
    public IMethod getMethod(String methodName, String[] datatypes);

    /**
     * Returns the first method with the given signature. This method does not search the supertype
     * hierarchy. Returns <code>null</code> if no such method exists.
     * 
     * @param signature The method's signature, e.g. calcPremium(base.Vertrag, Integer)
     */
    public IMethod getMethod(String signature);

    /**
     * Searches a method with the given signature in the type and its supertype hierarchy and
     * returns it. Returns <code>null</code> if no such method exists.
     * 
     * @param name The method's signature as string, e.g. computePremium(base.Contract,
     *            base.Coverage)
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException if an error occurs while searching.
     */
    public IMethod findMethod(String signature, IIpsProject ipsProject) throws CoreException;

    /**
     * Searches a method with the given name and the given parameters in the type and its supertype
     * hierarchy and returns it. Returns <code>null</code> if no such method exists.
     * 
     * @param name The method's name.
     * @param datatypes The datatypes of the method's parameters.
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException if an error occurs while searching.
     */
    public IMethod findMethod(String name, String datatypes[], IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a new method and returns it.
     */
    public IMethod newMethod();

    /**
     * Returns the number of methods.
     */
    public int getNumOfMethods();

    /**
     * Moves the methods identified by the indexes up or down by one position. If one of the indexes
     * is 0 (the first method), no method is moved up. If one of the indexes is the number of
     * methods - 1 (the last method) no method is moved down.
     * 
     * @param indexes The indexes identifying the methods.
     * @param up <code>true</code>, to move the methods up, <false> to move them down.
     * 
     * @return The new indexes of the moved methods.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a method.
     */
    public int[] moveMethods(int[] indexes, boolean up);

    /**
     * Returns a list of methods defined in any of the type's supertypes that can be overridden (and
     * isn't overridden yet).
     * 
     * @param onlyAbstractMethods if true only abstract methods are returned.
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    public IMethod[] findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Creates new methods in this type that overrides the given methods. Note that it is not
     * checked, if the methods really belong to one of the type's supertypes.
     */
    public IMethod[] overrideMethods(IMethod[] methods);

    /**
     * Returns true if this type has a same method as the indicated one. Two methods are considered
     * to be same when they have the same name, the same number of parameters and the parameter's
     * datatypes are equal.
     */
    public boolean hasSameMethod(IMethod method);

    /**
     * Returns the method that matches the indicated one regarding it's signature. Two methods match
     * if they have the same name, the same number of parameters and the parameter's datatypes are
     * equal. Returns <code>null</code> if the type does not contain a matching method or the
     * indicated method is <code>null</code>.
     */
    public IMethod getMatchingMethod(IMethod method);

}
