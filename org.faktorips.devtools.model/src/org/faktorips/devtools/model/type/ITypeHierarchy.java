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

import org.faktorips.devtools.model.pctype.IValidationRule;

/**
 * Represents the hierarchy formed by the sub type / super type relationship between types.
 */
public interface ITypeHierarchy {

    /**
     * Returns the type this hierarchy was computed for.
     */
    IType getType();

    /**
     * Returns <code>true</code> if the type's hierarchy contains a cycle, otherwise
     * <code>false</code>.
     */
    boolean containsCycle();

    /**
     * Returns the super type for the given type or null if the type either has no super type or the
     * hierarchy does not contain information about that type.
     */
    IType getSupertype(IType type);

    /**
     * Returns the type's super types by traveling up the hierarchy. The first element in the array
     * (if any) is the given type's direkt super type.
     */
    List<IType> getAllSupertypes(IType type);

    /**
     * Returns the type's super types and itself. The first element in the array is the type itself.
     */
    List<IType> getAllSupertypesInclSelf(IType type);

    /**
     * Returns <code>true</code> if the candidate is a super type of the indicated sub type,
     * otherwise <code>false</code>. Returns <code>false</code> if either candidate or sub type is
     * <code>null</code>.
     */
    boolean isSupertypeOf(IType candidate, IType subtype);

    /**
     * Returns <code>true</code> if the candidate is a sub type of the indicated sub type, otherwise
     * <code>false</code>. Returns <code>false</code> if either candidate or super type is
     * <code>null</code>.
     */
    boolean isSubtypeOf(IType candidate, IType supertype);

    /**
     * Returns all attributes of the given type either defined in the type itself or any of it's
     * super types found in the hierarchy.
     */
    List<IAttribute> getAllAttributes(IType type);

    /**
     * Returns all attributes of the given type either defined in the type itself or any of it's
     * super types found in the hierarchy. If an attribute overrides another, only the one
     * overriding is contained in the result, but not the overridden one.
     */
    List<IAttribute> getAllAttributesRespectingOverride(IType type);

    /**
     * Returns all methods of the given type either defined in the type itself or any of it's super
     * types found in the hierarchy.
     */
    List<IMethod> getAllMethods(IType type);

    /**
     * Returns all rules of the given type either defined in the type itself or any of it's super
     * types found in the hierarchy. Works only for PolicyCmptType. Returns an empty array for all
     * other types.
     */
    List<IValidationRule> getAllRules(IType type);

    /**
     * Returns the attribute with the given name if either the type or one of it's super types
     * contains an attribute with that name. Returns <code>null</code> if no attribute with the
     * given name is found.
     */
    IAttribute findAttribute(IType type, String attributeName);

    /**
     * Returns the direct sub types for the given policy component type. Returns an empty array if
     * either type is <code>null</code> or the hierarchy does not contain any sub types for the
     * type.
     */
    List<IType> getSubtypes(IType type);

    /**
     * Returns all direct and indirect sub types for the given policy component type.
     * 
     * @return An empty array if either type is <code>null</code> or the hierarchy does not contain
     *             any sub types for the type.
     */
    List<IType> getAllSubtypes(IType type);

    /**
     * Test if the IType with the given name is part of the Hierarchy.
     * 
     * @param name qualified name of the IType
     * @return true if IType is part of the hierarchy, false if IType is not part of the hierarchy
     */
    boolean isPartOfHierarchy(String name);

    /**
     * Test if the IType with the given name is the saved IType of the Hierarchy
     * 
     */
    boolean isSelectedType(String name);
}
