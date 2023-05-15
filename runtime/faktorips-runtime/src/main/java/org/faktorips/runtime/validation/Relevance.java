/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.validation;

import static org.faktorips.runtime.model.IpsModel.isEnumType;

import java.math.BigDecimal;
import java.util.Collection;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ValueSetKind;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.valueset.BigDecimalRange;
import org.faktorips.valueset.DecimalRange;
import org.faktorips.valueset.DefaultRange;
import org.faktorips.valueset.DoubleRange;
import org.faktorips.valueset.IntegerRange;
import org.faktorips.valueset.LongRange;
import org.faktorips.valueset.MoneyRange;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Defines the relevance of a {@link PolicyAttribute}, derived from its
 * {@link PolicyAttribute#getValueSet(IModelObject) value set}:
 * <ul>
 * <li>An attribute with an empty (or {@code null}) value set is considered {@link #IRRELEVANT} - no
 * value should be set</li>
 * <li>An attribute with value set {@link ValueSet#containsNull() containing} {@code null} is
 * considered {@link #OPTIONAL} - a value can be set but is not required</li>
 * <li>An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
 * {@code null} is considered {@link #MANDATORY} - a value must be set</li>
 * </ul>
 * An attribute that is not {@link #IRRELEVANT} ({@link #OPTIONAL} or {@link #MANDATORY}) is
 * considered relevant - its value should for example be checked against the value set.
 */
public enum Relevance {
    /**
     * An attribute with an empty (or {@code null}) value set - no value should be set.
     */
    IRRELEVANT {

        @Override
        public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
                PolicyAttribute policyAttribute,
                ValueSet<T> values) {
            @SuppressWarnings("unchecked")
            Class<T> datatype = (Class<T>)policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            if (valueSetKind == ValueSetKind.Range) {
                return castValueSet(datatype);
            }
            return new OrderedValueSet<>(false, null);
        }

        @SuppressWarnings("unchecked")
        private <T> ValueSet<T> castValueSet(Class<T> datatype) {
            return (ValueSet<T>)createEmptyRange(datatype);
        }
    },
    /**
     * An attribute with value set {@link ValueSet#containsNull() containing} {@code null} - a value
     * can be set but is not required.
     */
    OPTIONAL {

        @Override
        public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
                PolicyAttribute policyAttribute,
                ValueSet<T> values) {
            @SuppressWarnings("unchecked")
            Class<T> datatype = (Class<T>)policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            return asValueSet(datatype, valueSetKind, true, values);
        }
    },
    /**
     * An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
     * {@code null} - a value must be set.
     */
    MANDATORY {

        @Override
        public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
                PolicyAttribute policyAttribute,
                ValueSet<T> values) {
            @SuppressWarnings("unchecked")
            Class<T> datatype = (Class<T>)policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            return asValueSet(datatype, valueSetKind, false, values);
        }
    };

    private static <T> ValueSet<T> asValueSet(Class<T> datatype,
            ValueSetKind valueSetKind,
            boolean containsNull,
            ValueSet<T> values) {
        if (isCompatible(values, valueSetKind) && values.containsNull() == containsNull) {
            return values;
        }
        if (isBoolean(datatype)) {
            return asBooleanValueSet(containsNull, values);
        }
        if (isRange(valueSetKind, values)) {
            return asRange(datatype, containsNull, values);
        }
        if (isEnum(valueSetKind, values) || datatype.isEnum() || isEnumType(datatype)) {
            return asEnum(datatype, containsNull, values);
        }
        return new UnrestrictedValueSet<>(containsNull);
    }

    private static <T> boolean isEnum(ValueSetKind valueSetKind, ValueSet<T> values) {
        return valueSetKind == ValueSetKind.Enum
                || (values != null && values instanceof OrderedValueSet);
    }

    private static <T> boolean isRange(ValueSetKind valueSetKind, ValueSet<T> values) {
        return valueSetKind == ValueSetKind.Range || (values != null && values.isRange());
    }

    private static <T> boolean isBoolean(Class<T> datatype) {
        return Boolean.class.equals(datatype) || boolean.class.equals(datatype);
    }

    private static <T> ValueSet<T> asEnum(Class<T> datatype, boolean containsNull, ValueSet<T> values) {
        if (values != null) {
            return new OrderedValueSet<>(values.getValues(true), containsNull, nullValue(datatype));
        } else if (!IpsModel.isExtensibleEnumType(datatype)) {
            return new OrderedValueSet<>(containsNull, nullValue(datatype), datatype.getEnumConstants());
        } else {
            return new UnrestrictedValueSet<>(false);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T nullValue(Class<T> datatype) {
        if (Money.class.isAssignableFrom(datatype)) {
            return (T)Money.NULL;
        }
        if (Decimal.class.isAssignableFrom(datatype)) {
            return (T)Decimal.NULL;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> ValueSet<T> asRange(Class<T> datatype, boolean containsNull, ValueSet<T> values) {
        if (values != null) {
            return (ValueSet<T>)changeRangeRelevance(values, containsNull);
        } else {
            return (ValueSet<T>)createRangeRelevance(datatype, containsNull);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ValueSet<T> asBooleanValueSet(boolean containsNull, ValueSet<T> values) {
        if (values != null) {
            return new OrderedValueSet<>(values.getValues(true), containsNull, null);
        } else {
            return (ValueSet<T>)new OrderedValueSet<>(containsNull, null, Boolean.TRUE, Boolean.FALSE);
        }
    }

    private static boolean isCompatible(ValueSet<?> values, ValueSetKind valueSetKind) {
        if (values == null) {
            return false;
        }
        switch (valueSetKind) {
            case AllValues:
                return true;
            case Enum:
                return values instanceof OrderedValueSet;
            case Range:
                return values.isRange();
            default:
                return false;
        }
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #IRRELEVANT}
     * for the given model object.
     */
    public static boolean isIrrelevant(IModelObject modelObject, String property) {
        return isIrrelevant(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #IRRELEVANT} for the given model
     * object.
     */
    public static boolean isIrrelevant(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return IRRELEVANT == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the given {@link ValueSet} is considered {@link #IRRELEVANT}.
     *
     * @since 23.6
     */
    public static boolean isIrrelevant(ValueSet<?> valueSet) {
        return IRRELEVANT == Relevance.of(valueSet);
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #MANDATORY}
     * for the given model object.
     */
    public static boolean isMandatory(IModelObject modelObject, String property) {
        return isMandatory(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #MANDATORY} for the given model
     * object.
     */
    public static boolean isMandatory(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return MANDATORY == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the given {@link ValueSet} is considered {@link #MANDATORY}.
     *
     * @since 23.6
     */
    public static boolean isMandatory(ValueSet<?> valueSet) {
        return MANDATORY == Relevance.of(valueSet);
    }

    /**
     * Returns whether the attribute with the given property name is considered {@link #OPTIONAL}
     * for the given model object.
     */
    public static boolean isOptional(IModelObject modelObject, String property) {
        return isOptional(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered {@link #OPTIONAL} for the given model
     * object.
     */
    public static boolean isOptional(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return OPTIONAL == Relevance.of(modelObject, policyAttribute);
    }

    /**
     * Returns whether the given {@link ValueSet} is considered {@link #OPTIONAL}.
     *
     * @since 23.6
     */
    public static boolean isOptional(ValueSet<?> valueSet) {
        return OPTIONAL == Relevance.of(valueSet);
    }

    /**
     * Returns whether the attribute with the given property name is considered relevant for the
     * given model object.
     */
    public static boolean isRelevant(IModelObject modelObject, String property) {
        return isRelevant(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns whether the given attribute is considered relevant for the given model object.
     */
    public static boolean isRelevant(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return !isIrrelevant(modelObject, policyAttribute);
    }

    /**
     * Returns whether the given {@link ValueSet} is considered relevant.
     *
     * @since 23.6
     */
    public static boolean isRelevant(ValueSet<?> valueSet) {
        return !isIrrelevant(valueSet);
    }

    /**
     * Returns the {@link Relevance} of the {@link PolicyAttribute} identified by the given property
     * name for the given model object.
     */
    public static Relevance of(IModelObject modelObject, String property) {
        return Relevance.of(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns the {@link Relevance} of the given {@link PolicyAttribute} for the given model
     * object.
     */
    public static Relevance of(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return of(policyAttribute.getValueSet(modelObject));
    }

    /**
     * Returns the {@link Relevance} as interpretation of the given {@link ValueSet}.
     *
     * @since 23.6
     */
    public static Relevance of(ValueSet<?> valueSet) {
        if (valueSet == null || valueSet.isEmpty()) {
            return Relevance.IRRELEVANT;
        } else if (valueSet.containsNull()) {
            return Relevance.OPTIONAL;
        } else {
            return Relevance.MANDATORY;
        }
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute that matches this
     * {@link Relevance}.
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject, PolicyAttribute policyAttribute) {
        return asValueSetFor(modelObject, policyAttribute, (ValueSet<T>)null);
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute identified by the given
     * property name that matches this {@link Relevance}.
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject, String property) {
        return this.asValueSetFor(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute that matches this
     * {@link Relevance}. If a parent value set is given, the returned value set will be of the same
     * type (if allowed by the attribute's {@link ValueSetKind}) and contain no values not allowed
     * by that parent value set (with the exception of a null value if the value set is converted to
     * {@link #OPTIONAL}).
     *
     * @param modelObject a model object
     * @param policyAttribute an attribute present on the model object's {@link PolicyCmptType}
     * @param values an optional {@link ValueSet}, which can limit the allowed values for the
     *            returned value set
     */
    public abstract <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
            PolicyAttribute policyAttribute,
            @CheckForNull ValueSet<T> values);

    /**
     * Returns a {@link ValueSet} for the given model object's attribute that matches this
     * {@link Relevance}. If a parent value set is given, the returned value set will be of the same
     * type (if allowed by the attribute's {@link ValueSetKind}) and contain no values not allowed
     * by that parent value set (with the exception of a null value if the value set is converted to
     * {@link #OPTIONAL}).
     *
     * @param modelObject a model object
     * @param policyAttribute an attribute present on the model object's {@link PolicyCmptType}
     * @param values the allowed values for the returned value set
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
            PolicyAttribute policyAttribute,
            @CheckForNull Collection<T> values) {
        return asValueSetFor(modelObject, policyAttribute, OrderedValueSet.of(values));
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute identified by the given
     * property name that matches this {@link Relevance}. If a parent value set is given, the
     * returned value set will be of the same type (if allowed by the attribute's
     * {@link ValueSetKind}) and contain no values not allowed by that parent value set (with the
     * exception of a null value if the value set is converted to {@link #OPTIONAL}).
     *
     * @param modelObject a model object
     * @param property the name of an attribute present on the model object's {@link PolicyCmptType}
     * @param values an optional {@link ValueSet}, which can limit the allowed values for the
     *            returned value set
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
            String property,
            @CheckForNull ValueSet<T> values) {
        return this.asValueSetFor(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property),
                values);
    }

    /**
     * Returns a {@link ValueSet} for the given model object's attribute that matches this
     * {@link Relevance}. If a parent value set is given, the returned value set will be of the same
     * type (if allowed by the attribute's {@link ValueSetKind}) and contain no values not allowed
     * by that parent value set (with the exception of a null value if the value set is converted to
     * {@link #OPTIONAL}).
     *
     * @param modelObject a model object
     * @param property the name of an attribute present on the model object's {@link PolicyCmptType}
     * @param values the allowed values for the returned value set
     */
    public <T> ValueSet<T> asValueSetFor(IModelObject modelObject,
            String property,
            @CheckForNull Collection<T> values) {
        return asValueSetFor(modelObject, property, OrderedValueSet.of(values));
    }

    private static ValueSet<?> changeRangeRelevance(ValueSet<?> valueSet, boolean containsNull) {
        if (valueSet instanceof BigDecimalRange) {
            BigDecimalRange bigDecimalRange = (BigDecimalRange)valueSet;
            return BigDecimalRange.valueOf(bigDecimalRange.getLowerBound(), bigDecimalRange.getUpperBound(),
                    bigDecimalRange.getStep(), containsNull);
        }
        if (valueSet instanceof DecimalRange) {
            DecimalRange decimalRange = (DecimalRange)valueSet;
            return DecimalRange.valueOf(decimalRange.getLowerBound(), decimalRange.getUpperBound(),
                    decimalRange.getStep(), containsNull);
        }
        if (valueSet instanceof DoubleRange) {
            DoubleRange doubleRange = (DoubleRange)valueSet;
            return DoubleRange.valueOf(doubleRange.getLowerBound(), doubleRange.getUpperBound(), doubleRange.getStep(),
                    containsNull);
        }
        if (valueSet instanceof IntegerRange) {
            IntegerRange integerRange = (IntegerRange)valueSet;
            return IntegerRange.valueOf(integerRange.getLowerBound(), integerRange.getUpperBound(),
                    integerRange.getStep(), containsNull);
        }
        if (valueSet instanceof LongRange) {
            LongRange longRange = (LongRange)valueSet;
            return LongRange.valueOf(longRange.getLowerBound(), longRange.getUpperBound(),
                    longRange.getStep(), containsNull);
        }
        if (valueSet instanceof MoneyRange) {
            MoneyRange moneyRange = (MoneyRange)valueSet;
            return MoneyRange.valueOf(moneyRange.getLowerBound(), moneyRange.getUpperBound(),
                    moneyRange.getStep(), containsNull);
        }
        return null;
    }

    private static ValueSet<?> createRangeRelevance(Class<?> datatype, boolean containsNull) {
        if (BigDecimal.class.isAssignableFrom(datatype)) {
            return BigDecimalRange.valueOf((BigDecimal)null, null, null, containsNull);
        }
        if (Decimal.class.isAssignableFrom(datatype)) {
            return DecimalRange.valueOf((Decimal)null, null, null, containsNull);
        }
        if (Double.class.isAssignableFrom(datatype) || Double.TYPE.isAssignableFrom(datatype)) {
            return DoubleRange.valueOf((Double)null, null, null, containsNull);
        }
        if (Integer.class.isAssignableFrom(datatype) || Integer.TYPE.isAssignableFrom(datatype)) {
            return IntegerRange.valueOf((Integer)null, null, null, containsNull);
        }
        if (Long.class.isAssignableFrom(datatype) || Long.TYPE.isAssignableFrom(datatype)) {
            return LongRange.valueOf((Long)null, null, null, containsNull);
        }
        if (Money.class.isAssignableFrom(datatype)) {
            return MoneyRange.valueOf((Money)null, null, null, containsNull);
        }
        return null;
    }

    private static ValueSet<?> createEmptyRange(Class<?> datatype) {
        if (BigDecimal.class.isAssignableFrom(datatype)) {
            return BigDecimalRange.empty();
        }
        if (Decimal.class.isAssignableFrom(datatype)) {
            return DecimalRange.empty();
        }
        if (Double.class.isAssignableFrom(datatype) || Double.TYPE.isAssignableFrom(datatype)) {
            return DoubleRange.empty();
        }
        if (Integer.class.isAssignableFrom(datatype) || Integer.TYPE.isAssignableFrom(datatype)) {
            return IntegerRange.empty();
        }
        if (Long.class.isAssignableFrom(datatype) || Long.TYPE.isAssignableFrom(datatype)) {
            return LongRange.empty();
        }
        if (Money.class.isAssignableFrom(datatype)) {
            return MoneyRange.empty();
        }
        return newDefaultRange();
    }

    @SuppressWarnings("rawtypes")
    private static DefaultRange newDefaultRange() {
        return new DefaultRange();
    }

}
