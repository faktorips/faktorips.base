/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.pctype;

import org.faktorips.devtools.core.model.type.IMethod;

/**
 * Represents the hierarchy formed by the sub type / super type relationship between types.
 */
public interface ITypeHierarchy {

    /**
     * Returns the type this hierarchy was computed for.
     */
    public IPolicyCmptType getType();

    /**
     * Returns <code>true</code> if the type's hierarchy contains a cycle, otherwise
     * <code>false</code>.
     */
    public boolean containsCycle();

    /**
     * Returns the super type for the given type or null if the type either has no super type or the
     * hierarchy does not contain information about that type.
     */
    public IPolicyCmptType getSupertype(IPolicyCmptType type);

    /**
     * Returns the type's super types by traveling up the hierarchy. The first element in the array
     * (if any) is the given type's direkt super type.
     */
    public IPolicyCmptType[] getAllSupertypes(IPolicyCmptType type);

    /**
     * Returns the type's super types and itself. The first element in the array is the type itself.
     */
    public IPolicyCmptType[] getAllSupertypesInclSelf(IPolicyCmptType type);

    /**
     * Returns <code>true</code> if the candidate is a super type of the indicated sub type,
     * otherwise <code>false</code>. Returns <code>false</code> if either candidate or sub type is
     * <code>null</code>.
     */
    public boolean isSupertypeOf(IPolicyCmptType candidate, IPolicyCmptType subtype);

    /**
     * Returns <code>true</code> if the candidate is a sub type of the indicated sub type, otherwise
     * <code>false</code>. Returns <code>false</code> if either candidate or super type is
     * <code>null</code>.
     */
    public boolean isSubtypeOf(IPolicyCmptType candidate, IPolicyCmptType supertype);

    /**
     * Returns all attributes of the given type either defined in the type itself or any of it's
     * super types found in the hierarchy.
     */
    public IPolicyCmptTypeAttribute[] getAllAttributes(IPolicyCmptType type);

    /**
     * Returns all attributes of the given type either defined in the type itself or any of it's
     * super types found in the hierarchy. If an attribute overrides another, only the one
     * overriding is contained in the result, but not the overridden one.
     */
    public IPolicyCmptTypeAttribute[] getAllAttributesRespectingOverride(IPolicyCmptType type);

    /**
     * Returns all methods of the given type either defined in the type itself or any of it's super
     * types found in the hierarchy.
     */
    public IMethod[] getAllMethods(IPolicyCmptType type);

    /**
     * Returns all rules of the given type either defined in the type itself or any of it's super
     * types found in the hierarchy.
     */
    public IValidationRule[] getAllRules(IPolicyCmptType type);

    /**
     * Returns the attribute with the given name if either the type or one of it's super types
     * contains an attribute with that name. Returns <code>null</code> if no attribute with the
     * given name is found.
     */
    public IPolicyCmptTypeAttribute findAttribute(IPolicyCmptType type, String attributeName);

    /**
     * Returns the direct sub types for the given policy component type. Returns an empty array if
     * either type is <code>null</code> or the hierarchy does not contain any sub types for the
     * type.
     */
    public IPolicyCmptType[] getSubtypes(IPolicyCmptType type);

    /**
     * Returns all direct and indirect sub types for the given policy component type.
     * 
     * @return An empty array if either type is <code>null</code> or the hierarchy does not contain
     *         any sub types for the type.
     */
    public IPolicyCmptType[] getAllSubtypes(IPolicyCmptType type);

}
