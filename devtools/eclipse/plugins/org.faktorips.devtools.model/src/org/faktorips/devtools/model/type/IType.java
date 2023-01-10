/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Common interface for types, policy component type and product component type. Common for all
 * types is, that a type could have a supertype, could be abstract and could have attributes,
 * associations and methods ({@link ITypePart ITypeParts}).
 * 
 * @author Jan Ortmann
 */
public interface IType extends IOverridableElement, IIpsObject, Datatype, ILabeledElement, IVersionControlledElement {

    String PROPERTY_SUPERTYPE = "supertype"; //$NON-NLS-1$

    String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "Type-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the supertype hierarchy contains a cycle.
     */
    String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "CycleInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists an error within the type hierarchy of
     * this type.
     */
    String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the supertype can not be found.
     */
    String MSGCODE_SUPERTYPE_NOT_FOUND = MSGCODE_PREFIX + "SupertypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an abstract method exists in the type's supertype
     * hierarchy that must be overridden in the concrete type.
     */
    String MSGCODE_MUST_OVERRIDE_ABSTRACT_METHOD = MSGCODE_PREFIX + "MustOverrideAbstractMethod"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least one abstract method is defined, but this
     * type is not marked as abstract.
     */
    String MSGCODE_ABSTRACT_MISSING = MSGCODE_PREFIX + "AbstractMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least two properties (attribute, associations)
     * share the same name.
     */
    String MSGCODE_DUPLICATE_PROPERTY_NAME = MSGCODE_PREFIX + "DuplicatePropertyName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that in case of a derived union the type must be abstract
     * or at lease one implementation of the derived union must exists in the type.
     */
    String MSGCODE_MUST_SPECIFY_DERIVED_UNION = MSGCODE_PREFIX + "MustSpecifyDerivedUnion"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that in case of an inverse derived union the type must be
     * abstract or at lease one inverse of an implementation of the derived union must exists in the
     * type.
     */
    String MSGCODE_MUST_SPECIFY_INVERSE_OF_DERIVED_UNION = MSGCODE_PREFIX
            + "MustSpecifyInverseDerivedUnion"; //$NON-NLS-1$

    String MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS = MSGCODE_PREFIX
            + "OtherTypeWithSameNameExists"; //$NON-NLS-1$

    String MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS = MSGCODE_PREFIX
            + "OtherTypeWithSameNameInDependentProjectExists"; //$NON-NLS-1$

    @Override
    boolean isAbstract();

    /**
     * Sets whether this is an abstract type or not.
     */
    void setAbstract(boolean newValue);

    /**
     * Returns the qualified name of the type's supertype. Returns an empty string if this type has
     * no supertype.
     */
    String getSupertype();

    /**
     * Returns <code>true</code> if this type has a supertype, otherwise <code>false</code>. This
     * method also returns <code>true</code> if the type refers to a supertype but the supertype
     * does not exist.
     */
    boolean hasSupertype();

    /**
     * Returns whether this <code>IType</code> has a supertype that really exists.
     * 
     * @param ipsProject The <code>IIpsProject</code> providing the object path that is used to
     *            search for the supertype.
     * 
     */
    boolean hasExistingSupertype(IIpsProject ipsProject);

    /**
     * Returns the type's supertype if the type is derived from a supertype and the supertype can be
     * found on the project's IPS object path. Returns <code>null</code> if either this type is not
     * derived from a supertype or the supertype can't be found on the project's IPS object path.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     */
    IType findSupertype(IIpsProject ipsProject);

    /**
     * Sets the type's supertype.
     * 
     * @throws IllegalArgumentException If <code>newSupertype</code> is <code>null</code>.
     */
    void setSupertype(String newSupertype);

    /**
     * Returns <code>true</code> if this type is a subtype of the given supertype candidate, returns
     * <code>false</code> otherwise. Returns <code>false</code> if supertype candidate is
     * <code>null</code>.
     * 
     * @param supertypeCandidate The type which is possibly a supertype of this type.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     */
    boolean isSubtypeOf(IType supertypeCandidate, IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if this type is a subtype of the given candidate, or if the
     * candidate is this same. Returns <code>false</code> otherwise. Returns <code>false</code> if
     * candidate is <code>null</code>.
     * 
     * @param candidate The type which is possibly a supertype of this type or possible the same
     *            type as this type.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     */
    boolean isSubtypeOrSameType(IType candidate, IIpsProject ipsProject);

    /**
     * Returns the type's attributes.
     */
    List<IAttribute> getAttributes();

    /**
     * Returns this type's attributes and the attributes within the supertype hierarchy. It
     * considers overridden attributes. That means if an attribute has been overridden by a subtype
     * this attribute instance will be in the returned array all attributes in the supertype
     * hierarchy with the same name will be neglected.
     * 
     * @throws IpsException If an exception occurs while collecting the attributes.
     */
    List<IAttribute> findAllAttributes(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns this type's associations within the supertype hierarchy.
     * <p>
     * Constrained associations are not added to the result if a constraining association is already
     * added.
     * 
     * @throws IpsException If an exception occurs while collecting the associations.
     */
    List<IAssociation> findAllAssociations(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type (This method
     * does not search the supertype hierarchy.) If more than one attribute with the name exist, the
     * first attribute with the name is returned. Returns <code>null</code> if no attribute with the
     * given name exists.
     */
    IAttribute getAttribute(String name);

    /**
     * Searches an attribute with the given name in the type and its supertype hierarchy and returns
     * it. Returns <code>null</code> if no such attribute exists.
     * 
     * @param name The attribute's name.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException If <code>project</code> is <code>null</code>.
     */
    IAttribute findAttribute(String name, IIpsProject ipsProject);

    /**
     * Creates a new attribute and returns it.
     */
    IAttribute newAttribute();

    /**
     * Returns the number of attributes.
     */
    int getNumOfAttributes();

    /**
     * Moves the attributes identified by the indices up or down by one position. If one of the
     * indices is 0 (the first attribute), no attribute is moved up. If one of the indices is the
     * number of attributes - 1 (the last attribute), no attribute is moved down.
     * <p>
     * Returns the new indices of the moved attributes.
     * 
     * @param indices The indices identifying the attributes.
     * @param up <code>true</code>, to move the attributes up, <code>false</code> to move them down.
     * 
     * @throws NullPointerException If <code>indices</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If one of the <code>indices</code> does not identify an
     *             attribute.
     */
    int[] moveAttributes(int[] indices, boolean up);

    /**
     * Returns the association with the given name defined in <strong>this</strong> type. This
     * method does not search the supertype hierarchy.
     * <p>
     * If more than one association with the given role name singular exists, the first one is
     * returned. Returns <code>null</code> if no association with the given role name singular
     * exists or <code>roleNameSingular</code> is <code>null</code>.
     * 
     * @param roleNameSingular The association's role name singular.
     */
    IAssociation getAssociation(String roleNameSingular);

    /**
     * Returns the association with the given role name in plural form defined in
     * <strong>this</strong> type. This method does not search the supertype hierarchy.
     * <p>
     * If more than one association with the role name plural exist, the first one is returned.
     * Returns <code>null</code> if no association with the given role name plural exists or
     * <code>roleNamePlural</code> is <code>null</code>.
     * 
     * @param roleNamePlural The association's role name plural.
     */
    IAssociation getAssociationByRoleNamePlural(String roleNamePlural);

    /**
     * Searches an association with the given name in the type and it's supertype hierarchy and
     * returns it. Returns <code>null</code> if no such association exists.
     * 
     * @param name The association's name.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    IAssociation findAssociation(String name, IIpsProject ipsProject);

    /**
     * Searches an association with the given role name plural in the type and it's supertype
     * hierarchy and returns it. Returns <code>null</code> if no such association exists.
     * 
     * @param roleNamePlural The association's role name in plural form.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws IpsException If an error occurs while searching.
     */
    IAssociation findAssociationByRoleNamePlural(String roleNamePlural, IIpsProject ipsProject)
            throws IpsException;

    /**
     * Returns all associations that have the indicated target and association type in the current
     * type and optional it's supertype hierarchy. Returns an empty array if no such association
     * exists or target or <code>associationType</code> is <code>null</code>.
     * <p>
     * Constrained associations are not added to the result if a constraining association is already
     * added.
     * 
     * @param target The qualified name of the target type.
     * @param associationType The association type.
     * @param includeSupertypes <code>true</code> if the supertype hierarchy should be included in
     *            the search, <code>false</code> otherwise.
     */
    List<IAssociation> findAssociationsForTargetAndAssociationType(String target,
            AssociationType associationType,
            IIpsProject ipsProject,
            boolean includeSupertypes) throws IpsException;

    /**
     * Returns the type's associations.
     */
    List<IAssociation> getAssociations();

    /**
     * Returns all {@link IAssociation associations} that have one of the indicated
     * {@link AssociationType AssociationTypes}.
     * 
     * @param types an array of desired {@link AssociationType AssociationTypes}
     */
    List<IAssociation> getAssociations(AssociationType... types);

    /**
     * Returns all associations that have the indicated target. Returns an empty array if no such
     * association exists or target is <code>null</code>.
     * <p>
     * Note that this does NOT search the supertype hierarchy.
     * 
     * @param target The qualified name of the target type.
     */
    List<IAssociation> getAssociationsForTarget(String target);

    /**
     * Creates a new association and returns it.
     */
    IAssociation newAssociation();

    /**
     * Returns the number of associations.
     */
    int getNumOfAssociations();

    /**
     * Moves the associations identified by the given indices up or down by one position. If one of
     * the indices is 0 (the first association), no association is moved up. If one of the indices
     * is the number of associations - 1 (the last association) no association is moved down.
     * <p>
     * Returns an array containing the new indices of the moved associations.
     * 
     * @param indices The indices identifying the associations.
     * @param up <code>true</code>, to move the associations up, <code>false</code> to move them
     *            down.
     * 
     * @throws NullPointerException If <code>indices</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If one of the <code>indices</code> does not identify an
     *             association.
     */
    int[] moveAssociations(int[] indices, boolean up);

    /**
     * Returns all methods of this type including the methods of the types within the supertype
     * hierarchy.
     * 
     * @param ipsProject The IPS project that is used to determine the types within the supertype
     *            hierarchy.
     * @throws IpsException If an exception occurs during the execution of this method.
     */
    List<IMethod> findAllMethods(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the type's methods.
     */
    List<IMethod> getMethods();

    /**
     * Returns the first method with the given name and the given parameters in this type. This
     * method does not search the supertype hierarchy. Returns <code>null</code> if no such method
     * exists.
     * 
     * @param methodName The method's name.
     * @param datatypes The data types of the method's parameters.
     */
    IMethod getMethod(String methodName, String[] datatypes);

    /**
     * Returns the first method with the given signature. This method does not search the supertype
     * hierarchy. Returns <code>null</code> if no such method exists.
     * 
     * @param signature The method's signature, e.g. <code>calcPremium(base.Vertrag, Integer)</code>
     */
    IMethod getMethod(String signature);

    /**
     * Searches a method with the given signature in the type and its supertype hierarchy and
     * returns it. Returns <code>null</code> if no such method exists.
     * 
     * @param signature The method's signature as string, e.g. <code>computePremium(base.Contract,
     *            base.Coverage)</code>
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException If project is <code>null</code>.
     * @throws IpsException If an error occurs while searching.
     */
    IMethod findMethod(String signature, IIpsProject ipsProject) throws IpsException;

    /**
     * Searches a method with the given name and the given parameters in the type and its supertype
     * hierarchy and returns it. Returns <code>null</code> if no such method exists.
     * 
     * @param name The method's name.
     * @param datatypes The data types of the method's parameters.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException If project is <code>null</code>.
     * @throws IpsException If an error occurs while searching.
     */
    IMethod findMethod(String name, String[] datatypes, IIpsProject ipsProject) throws IpsException;

    /**
     * Creates a new method and returns it.
     */
    IMethod newMethod();

    /**
     * Returns the number of methods.
     */
    int getNumOfMethods();

    /**
     * Moves the methods identified by the indices up or down by one position. If one of the indices
     * is 0 (the first method), no method is moved up. If one of the indices is the number of
     * methods - 1 (the last method) no method is moved down.
     * <p>
     * Returns an array containing the new indices of the moved methods.
     * 
     * @param indices The indices identifying the methods.
     * @param up <code>true</code>, to move the methods up, <code>false</code> to move them down.
     * 
     * @throws NullPointerException If <code>indices</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If one of the indices does not identify a method.
     */
    int[] moveMethods(int[] indices, boolean up);

    /**
     * Returns a list of methods defined in any of the type's supertypes that can be overridden (and
     * isn't overridden yet).
     * 
     * @param onlyNotImplementedAbstractMethods If true only not implemented, abstract methods are
     *            returned.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     */
    List<IMethod> findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject)
            throws IpsException;

    /**
     * Creates new methods in this type that override the given methods. Note that it is not checked
     * whether the methods really belong to one of the type's supertypes.
     */
    List<IMethod> overrideMethods(List<IMethod> methods);

    /**
     * Returns true if this type has a method equal to the indicated one. Two methods are considered
     * to be equal when they have the same name, the same number of parameters and the parameter's
     * data types are equal.
     */
    boolean hasSameMethod(IMethod method);

    /**
     * Returns the method that matches the indicated one regarding it's signature. Two methods match
     * if they have the same name, the same number of parameters and the parameter's data types are
     * equal. Returns <code>null</code> if the type does not contain a matching method or the
     * indicated method is <code>null</code>.
     */
    IMethod getMatchingMethod(IMethod method);

    /**
     * Finds sub types extending this type by searching in the given project root.
     * <p>
     * By setting the property transitive you could include or exclude indirect extending types.
     * <p>
     * If you want to find all sub types in the current workspace you have to use
     * {@link #searchSubtypes(boolean, boolean)} to include all projects depending on this project.
     * 
     * @param transitive {@code true} to include indirect extending types, {@code false} for only
     *            return direct sub types
     * @param includingSelf {@code true} to include this type in result list
     * @param project the root for finding types
     * 
     * @return a list of types extending the current type, directly or indirectly depends on
     *             parameter transitive
     * @see #searchSubtypes(boolean, boolean)
     */
    List<IType> findSubtypes(boolean transitive, boolean includingSelf, IIpsProject project);

    /**
     * Searches for all types extending the current type in the whole workspace.
     * <p>
     * By setting the property transitive you could include or exclude indirect extending types.
     * <p>
     * To find all sub types the implementation would search in the current project and in every
     * project that depends on the current project. If you want to find only types in one project
     * including depending projects, use {@link #findSubtypes(boolean, boolean, IIpsProject)}.
     * 
     * @param transitive {@code true} to include indirect extending types, {@code false} for only
     *            return direct sub types
     * @param includingSelf {@code true} to include this type in the result list
     * 
     * @return a list containing every type in the workspace that extends this type
     * 
     * @see #findSubtypes(boolean, boolean, IIpsProject)
     */
    List<IType> searchSubtypes(boolean transitive, boolean includingSelf);

    /**
     * Creates new attributes in this type overriding the given attributes. Note that it is not
     * checked, if the attributes really belong to one of the type's super types.
     * 
     * @return The created attributes.
     */
    List<IAttribute> overrideAttributes(List<? extends IAttribute> attributes);

    /**
     * Creates a new super type hierarchy for the type and returns it.
     */
    ITypeHierarchy getSupertypeHierarchy();

    /**
     * Returns an array of all attributes of all super types not yet overwritten by this component
     * type.
     */
    List<IAttribute> findOverrideAttributeCandidates(IIpsProject ipsProject);

    /**
     * Creates new association in this type that override the given association with type.
     * 
     * @param association that will be constrain
     * @return new created associations
     */
    IAssociation constrainAssociation(IAssociation association, IType targetType);

    /**
     * Returns an array of all associations of all super types not yet overwritten by this component
     * type.
     */
    List<IAssociation> findConstrainableAssociationCandidates(IIpsProject ipsProject) throws IpsException;

    /**
     * Creates a new sub type hierarchy for the type and returns it.
     */
    ITypeHierarchy getSubtypeHierarchy() throws IpsException;

    @Override
    default IOverridableElement findOverriddenElement(IIpsProject ipsProject) {
        return findSupertype(ipsProject);
    }

}
