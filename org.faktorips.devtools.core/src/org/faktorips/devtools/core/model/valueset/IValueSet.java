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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.DelegatingValueSet;
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
public interface IValueSet extends IIpsObjectPart, Comparable<IValueSet> {

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
     * Returns the same value as {@link #isContainsNull()}. This method was introduced in mistake.
     * 
     * @deprecated use {@link #isContainsNull()} instead.
     */
    @Deprecated
    public boolean isContainingNull();

    /**
     * Returns <tt>true</tt> if this value set contains the null-value, <tt>false</tt> if not.
     */
    public boolean isContainsNull();

    /**
     * Adds or removes the null-value from the indicated {@link IValueSet}.
     * 
     * @param containsNull <code>true</code> to add the null-value to this {@link IValueSet} or
     *            <code>false</code> to remove it.
     */
    public void setContainsNull(boolean containsNull);

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
     * 
     * Prefer this method over instance-of check. If this method returns <code>true</code> the
     * object could safely be casted to {@link IUnrestrictedValueSet}. But if the object is an
     * instance of {@link IUnrestrictedValueSet} it does not mean that it is really an unrestricted
     * value set e.g. in case of {@link DelegatingValueSet}
     */
    public boolean isUnrestricted();

    /**
     * Returns <code>true</code> if the value set is a range, otherwise <code>false</code>.
     * 
     * Prefer this method over instance-of check. If this method returns <code>true</code> the
     * object could safely be casted to {@link IRangeValueSet}. But if the object is an instance of
     * {@link IRangeValueSet} it does not mean that it is really a range e.g. in case of
     * {@link DelegatingValueSet}
     */
    public boolean isRange();

    /**
     * Returns <code>true</code> if the value set is an enumeration, otherwise <code>false</code>.
     * 
     * Prefer this method over instance-of check. If this method returns <code>true</code> the
     * object could safely be casted to {@link IEnumValueSet}. But if the object is an instance of
     * {@link IEnumValueSet} it does not mean that it is really an enum e.g. in case of
     * {@link DelegatingValueSet}
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
     * If the value set given as parameter is unrestricted, only the parameter containsNull is
     * checked. If the specified value set contains <code>null</code> this method always returns
     * <code>true</code>. If the specified value set denies <code>null</code>, this value set also
     * needs to deny <code>null</code> values. The type of this value set does not matter at all.
     * <p>
     * If the value set given as parameter is is not unrestricted but has a different type, the
     * method returns <code>false</code>. Otherwise, if the value sets are of the same type, there
     * are following rules:
     * <ul>
     * <li>If the specified value set excludes <code>null</code> this value set has to exclude
     * <code>null</code>, too. (containsNull must match)</li>
     * <li>If the specified value set is abstract and containsNull matches, return <code>true</code>
     * as an abstract value set contains all values and thus all value sets of the same type.</li>
     * <li>If the specified value set is not abstract, return <code>true</code> if this value set is
     * a subset of the given value set, that means {@link #containsValueSet(IValueSet)} returns
     * true.</li>
     * </ul>
     */
    public boolean isDetailedSpecificationOf(IValueSet valueSet);

    /**
     * Returns the datatype used by this value set
     * 
     * @param ipsProject The ips project to search for the datatype
     * @return The datatype that is used by this value set
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject);

}
