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

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.ValueSetKind;
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
        public ValueSet<?> asValueSetFor(IModelObject modelObject, PolicyAttribute policyAttribute) {
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();
            if (valueSetKind == ValueSetKind.Range) {
                return new DefaultRange<>();
            }
            return new OrderedValueSet<>(false, null);
        }
    },
    /**
     * An attribute with value set {@link ValueSet#containsNull() containing} {@code null} - a value
     * can be set but is not required.
     */
    OPTIONAL {
        @Override
        public ValueSet<?> asValueSetFor(IModelObject modelObject, PolicyAttribute policyAttribute) {
            ValueSet<?> valueSet = policyAttribute.getValueSet(modelObject);
            Class<?> datatype = policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();

            if (Boolean.class.equals(datatype) || boolean.class.equals(datatype)) {
                return new OrderedValueSet<>(true, null, Boolean.TRUE, Boolean.FALSE);
            }
            if (valueSetKind == ValueSetKind.Range) {
                if (valueSet.containsNull()) {
                    return valueSet;
                }
                return changeRangeRelevance(valueSet, true);
            }
            if (valueSetKind == ValueSetKind.Enum) {
                return new OrderedValueSet<>(true, null, datatype.getEnumConstants());
            }
            return new UnrestrictedValueSet<>(true);
        }
    },
    /**
     * An attribute with a non-empty value set not {@link ValueSet#containsNull() containing}
     * {@code null} - a value must be set.
     */
    MANDATORY {
        @Override
        public ValueSet<?> asValueSetFor(IModelObject modelObject, PolicyAttribute policyAttribute) {
            ValueSet<?> valueSet = policyAttribute.getValueSet(modelObject);
            Class<?> datatype = policyAttribute.getDatatype();
            ValueSetKind valueSetKind = policyAttribute.getValueSetKind();

            if (Boolean.class.equals(datatype) || boolean.class.equals(datatype)) {
                return new OrderedValueSet<>(false, null, Boolean.TRUE, Boolean.FALSE);
            }
            if (valueSetKind == ValueSetKind.Range) {
                if (!valueSet.containsNull()) {
                    return valueSet;
                }
                return changeRangeRelevance(valueSet, false);
            }
            if (valueSetKind == ValueSetKind.Enum) {
                return new OrderedValueSet<>(false, null, datatype.getEnumConstants());
            }
            return new UnrestrictedValueSet<>(false);
        }
    };

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
        ValueSet<?> valueSet = policyAttribute.getValueSet(modelObject);
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
    public abstract ValueSet<?> asValueSetFor(IModelObject modelObject, PolicyAttribute policyAttribute);

    /**
     * Returns a {@link ValueSet} for the given model object's attribute identified by the given
     * property name that matches this {@link Relevance}.
     */
    public ValueSet<?> asValueSetFor(IModelObject modelObject, String property) {
        return this.asValueSetFor(modelObject, IpsModel.getPolicyCmptType(modelObject).getAttribute(property));
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

}
