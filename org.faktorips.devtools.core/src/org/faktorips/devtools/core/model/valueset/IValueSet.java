/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.valueset;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An <tt>IValueSet</tt> is the specification of a set of values. It is assumed that all values in a
 * value set are of the same data type.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the data type changes. E.g. if an attribute's data type is changed by the user from
 * <tt>Decimal</tt> to <tt>Money</tt>, lower bound and upper bound from a range value set become
 * invalid (if they were valid before) but the string values remain. The user can switch back the
 * data type to <tt>Decimal</tt> and the range is valid again. This also works when the attribute's
 * data type is unknown.
 * 
 * @author Thorsten Guenther
 * @author Jan Ortmann
 */
public interface IValueSet extends IIpsObjectPart {

    public static final String PROPERTY_CONTAINS_NULL = "containsNull"; //$NON-NLS-1$

    public static final String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "VALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value could not be parsed.
     */
    public static final String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueNotParsable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set.
     */
    public static final String MSGCODE_UNKNOWN_DATATYPE = MSGCODE_PREFIX + "UnknownDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type this value set is based on does not
     * support <code>null</code>-values, but this value set is marked to contain a <code>null</code>
     * -value.
     */
    public static final String MSGCODE_NULL_NOT_SUPPORTED = MSGCODE_PREFIX + "NullNotSupported"; //$NON-NLS-1$

    /**
     * Return the value set owner which is also the parent of this object.
     */
    public IValueSetOwner getValueSetOwner();

    /**
     * Returns the type of the value set.
     */
    public ValueSetType getValueSetType();

    /**
     * Returns <code>true</code> if the value set contains the indicated value, otherwise
     * <code>false</code>.
     * 
     * @param value The value to check.
     * @param ipsProject The project to look up the data type.
     * 
     * @throws CoreException If an error occurs while checking.
     */
    public boolean containsValue(String value, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if this value set contains the other value set, otherwise
     * <code>false</code>.
     * 
     * @param subset The value set to check.
     * 
     * @throws NullPointerException If <tt>subset</tt> is <code>null</code>.
     */
    public boolean containsValueSet(IValueSet subset);

    /**
     * Creates a copy of this value set (type and values, parent and id are set to the given
     * values).
     */
    public IValueSet copy(IValueSetOwner newParent, String id);

    /**
     * Copies all values including the abstract-flag - if applicable - (but not the parent or the
     * id) of the given source to this value set. If this given source value set is of a different
     * type, only the abstract flag is copied. If this value set is an unrestricted value set, this
     * method does nothing.
     */
    public void setValuesOf(IValueSet source);

    /**
     * Returns the unqualified, human-readable representation of this value set. If the data type
     * provided by the parent supports named values, the names provided from the data type are used
     * to build the value representations.
     */
    public String toShortString();

    /**
     * Returns <tt>true</tt> if this value set contains the null-value, <tt>false</tt> if not.
     * 
     * @deprecated Use {@link #isContainingNull()} instead
     */
    @Deprecated
    public boolean getContainsNull();

    /**
     * Returns <tt>true</tt> if this value set contains the null-value, <tt>false</tt> if not.
     */
    public boolean isContainingNull();

    /**
     * Marks this value set as abstract. An abstract value set does not define concrete values,
     * instead it is a substitute / constraint for the type of an allowed value set. An unrestricted
     * value set if also not abstract, as it defines all values of the underlying data type as
     * member of the set.
     */
    public void setAbstract(boolean isAbstract);

    /**
     * Returns <code>true</code> if this value set is abstract, <code>false</code> otherwise.
     */
    public boolean isAbstract();

    /**
     * Returns <tt>true</tt> if this value set is abstract and not unrestricted, <tt>false</tt>
     * otherwise.
     */
    public boolean isAbstractAndNotUnrestricted();

    /**
     * Returns <code>true</code> if this value set is of the same type as the given other value set.
     * Returns <code>false</code> otherwise. Returns <code>false</code> if the other value set is
     * <code>null</code>.
     */
    public boolean isSameTypeOfValueSet(IValueSet other);

    /**
     * Returns <code>true</code> if the value set is unrestricted. It contains all values defined by
     * an underlying data type.
     */
    public boolean isUnrestricted();

    /**
     * Returns <code>true</code> if the value set is a range, otherwise <code>false</code>.
     */
    public boolean isRange();

    /**
     * Returns <code>true</code> if the value set is an enumeration, otherwise <code>false</code>.
     */
    public boolean isEnum();

    /**
     * Returns <code>true</code> if this is a non-abstract enumeration value set. Non-abstract
     * enumeration value sets can be used as supersets for other enumeration value sets. Returns
     * <code>false</code> otherwise.
     */
    public boolean canBeUsedAsSupersetForAnotherEnumValueSet();

    /**
     * Returns <code>true</code> if this value set is a more detailed specification of the given
     * value set or is the same specification.
     * <p>
     * If the value set given as parameter is unrestricted, the method returns <code>true</code> as
     * all other value sets are more detailed specifications (or if this value set is also
     * unrestricted it is the same specification).
     * <p>
     * If the value set given as parameter is restricted but has a different type, the method
     * returns <code>false</code>. Otherwise, if the value sets are of the same type, there are two
     * cases:
     * <ul>
     * <li>The given value set is abstract -> <code>true</code> is returned as an abstract value set
     * contains all values and thus all value sets of the same type.</li>
     * <li>The given value set is not abstract -> <code>true</code> is returned if this value set is
     * a subset of the given value set.</li>
     * </ul>
     */
    public boolean isDetailedSpecificationOf(IValueSet valueSet);

}
