/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * An {@code IValueSet} is the specification of a set of values. It is assumed that all values in a
 * value set are of the same data type.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the data type changes. E.g. if an attribute's data type is changed by the user from
 * {@code Decimal} to {@code Money}, lower bound and upper bound from a range value set become
 * invalid (if they were valid before) but the string values remain. The user can switch back the
 * data type to {@code Decimal} and the range is valid again. This also works when the attribute's
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
     * support {@code null}-values, but this value set is marked to contain a {@code null} -value.
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
     * Returns {@code true} if the value set contains the indicated value, otherwise {@code false}.
     * 
     * @param value The value to check.
     * @param ipsProject The project to look up the data type.
     * 
     * @throws CoreRuntimeException If an error occurs while checking.
     */
    public boolean containsValue(String value, IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns {@code true} if this value set contains the other value set and both value sets are
     * valid and only contain values that can be parsed to the datatype of their value set,
     * otherwise {@code false}.
     * 
     * @param subset The value set to check.
     * 
     * @throws NullPointerException If {@code subset} is {@code null}.
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
     * Returns a string representation that always represents this value set in the same way
     * independent from the datatype or project. It could be used to compare the content of two
     * value sets for equality.
     * 
     * @return a canonical string representation of this value set
     */
    public String getCanonicalString();

    /**
     * Returns {@code true} if this value set contains the null-value, {@code false} if not.
     */
    public boolean isContainsNull();

    /**
     * Adds or removes the null-value from the indicated {@link IValueSet}.
     * 
     * @param containsNull {@code true} to add the null-value to this {@link IValueSet} or
     *            {@code false} to remove it.
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
     * Returns {@code true} if this value set is abstract, {@code false} otherwise.
     */
    public boolean isAbstract();

    /**
     * Returns {@code true} if this value set is abstract and not unrestricted, {@code false}
     * otherwise.
     */
    public boolean isAbstractAndNotUnrestricted();

    /**
     * Returns {@code true} if this value set is of the same type as the given other value set.
     * Returns {@code false} otherwise. Returns {@code false} if the other value set is
     * {@code null}.
     */
    public boolean isSameTypeOfValueSet(IValueSet other);

    /**
     * Returns {@code true} if the value set is unrestricted. It contains all values defined by an
     * underlying data type.
     * 
     * Prefer this method over instance-of check. If this method returns {@code true} the object
     * could safely be cast to {@link IUnrestrictedValueSet}. But if the object is an instance of
     * {@link IUnrestrictedValueSet} it does not mean that it is really an unrestricted value set
     * e.g. in case of {@link IDelegatingValueSet}
     */
    public boolean isUnrestricted();

    /**
     * Returns {@code true} if the value set is a range, otherwise {@code false}.
     * 
     * Prefer this method over instance-of check. If this method returns {@code true} the object
     * could safely be cast to {@link IRangeValueSet}. But if the object is an instance of
     * {@link IRangeValueSet} it does not mean that it is really a range e.g. in case of
     * {@link IDelegatingValueSet}
     */
    public boolean isRange();

    /**
     * Returns {@code true} if the value set is an enumeration, otherwise {@code false}.
     * 
     * Prefer this method over instance-of check. If this method returns {@code true} the object
     * could safely be cast to {@link IEnumValueSet}. But if the object is an instance of
     * {@link IEnumValueSet} it does not mean that it is really an enum e.g. in case of
     * {@link IDelegatingValueSet}
     */
    public boolean isEnum();

    /**
     * Returns {@code true} if the value set is derived, otherwise {@code false}.
     * 
     * Prefer this method over instance-of check. If this method returns {@code true} the object
     * could safely be cast to {@link IDerivedValueSet}. But if the object is an instance of
     * {@link IDerivedValueSet} it does not mean that it directly implements code to compute a value
     * set, e.g. in case of {@link IDelegatingValueSet}
     * 
     * @since 20.6
     */
    public default boolean isDerived() {
        return getValueSetType().isDerived();
    }

    /**
     * Return {@code true} if the value set is restricting String length, otherwise {@code false}.
     * 
     * @since 20.6
     */
    public default boolean isStringLength() {
        return getValueSetType().isStringLength();
    }

    /**
     * Returns {@code true} if this is a non-abstract enumeration value set. Non-abstract
     * enumeration value sets can be used as supersets for other enumeration value sets. Returns
     * {@code false} otherwise.
     */
    public boolean canBeUsedAsSupersetForAnotherEnumValueSet();

    /**
     * Returns {@code true} if this value set is a more detailed specification of the given value
     * set or is the same specification.
     * <p>
     * If the value set given as parameter is unrestricted, only the parameter containsNull is
     * checked. If the specified value set contains {@code null} this method always returns
     * {@code true}. If the specified value set denies {@code null}, this value set also needs to
     * deny {@code null} values. The type of this value set does not matter at all.
     * <p>
     * If the value set given as parameter is is not unrestricted but has a different type, the
     * method returns {@code false}. Otherwise, if the value sets are of the same type, there are
     * following rules:
     * <ul>
     * <li>If the specified value set excludes {@code null} this value set has to exclude
     * {@code null}, too. (containsNull must match)</li>
     * <li>If the specified value set is abstract and containsNull matches, return {@code true} as
     * an abstract value set contains all values and thus all value sets of the same type.</li>
     * <li>If the specified value set is not abstract, return {@code true} if this value set is a
     * subset of the given value set, that means {@link #containsValueSet(IValueSet)} returns
     * true.</li>
     * </ul>
     */
    public boolean isDetailedSpecificationOf(IValueSet valueSet);

    /**
     * Returns the datatype used by this value set-
     * 
     * @param ipsProject the Faktor-IPS project to search for the datatype
     * @return the datatype that is used by this value set
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject);

    /**
     * Returns {@code true} if this value set contains no values, {@code false} otherwise.
     * 
     * @return whether this value set contains no allowed values
     * 
     * @since 20.6
     */
    boolean isEmpty();

}
