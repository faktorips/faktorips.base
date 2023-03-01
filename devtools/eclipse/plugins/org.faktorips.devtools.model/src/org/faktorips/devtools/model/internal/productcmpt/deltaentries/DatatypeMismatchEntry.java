/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.valueset.IValueSet;

public class DatatypeMismatchEntry extends AbstractDeltaEntryForProperty {

    private final List<String> oldValues;
    private final ValueConverter converter;
    private final Consumer<List<String>> valueConsumer;

    /**
     * Sets the input for conversion, which will be done with the {@code fix()} method. Instead of
     * this constructor the {@link #forEachMismatch(List)} factory method should be used to create
     * all DatatypeMismatchEntries for a {@link IPropertyValueContainer}'s properties.
     * 
     * @param propertyValue The product properties.
     * @param oldValues A list of not converted {@link String} values.
     * @param converter The converter with whom the old Values should be modified.
     * @param valueConsumer Adds a transformed list of {@link IValue} to the propertyValue.
     */
    /* private */ DatatypeMismatchEntry(IPropertyValue propertyValue, List<String> oldValues, ValueConverter converter,
            Consumer<List<String>> valueConsumer) {
        super(propertyValue);
        this.oldValues = oldValues;
        this.converter = converter;
        this.valueConsumer = valueConsumer;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.DATATYPE_MISMATCH;
    }

    @Override
    public String getDescription() {
        return MessageFormat.format(Messages.DatatypeMismatchEntry_datatypeMissmatchDescription,
                String.join(", ", oldValues), //$NON-NLS-1$
                String.join(", ", convertedValues())); //$NON-NLS-1$
    }

    @Override
    public void fix() {
        List<String> converted = convertedValues();
        valueConsumer.accept(converted);
    }

    private List<String> convertedValues() {
        return oldValues.stream().map(
                input -> converter.convert(input, getPropertyValue().getIpsProject()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a {@link DatatypeMismatchEntry} for each {@link IPropertyValue} in the given list
     * that has a datatype not matching the corresponding {@link IProductCmptProperty}'s datatype.
     */
    public static List<DatatypeMismatchEntry> forEachMismatch(List<? extends IPropertyValue> values) {
        return values.stream().map(DatatypeMismatchEntry::createPossibleMismatch).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }

    private static Optional<DatatypeMismatchEntry> createPossibleMismatch(final IPropertyValue propertyValue) {
        if (isConversionNeeded(propertyValue)) {
            ValueDatatype datatype = findDatatype(propertyValue);
            final ValueConverter converter = ValueConverter.getByTargetType(datatype);
            if (converter != null) {
                Optional<DatatypeMismatch<IPropertyValue>> mismatch = createMismatch(propertyValue);
                Function<DatatypeMismatch<?>, DatatypeMismatchEntry> datatypeMismatchEntry = dataTypeMismatch -> new DatatypeMismatchEntry(
                        propertyValue, dataTypeMismatch.getValues(), converter, dataTypeMismatch.getValueConsumer());
                return mismatch.map(datatypeMismatchEntry);
            }
        }
        return Optional.empty();
    }

    private static boolean isConversionNeeded(IPropertyValue attributeValue) {
        try {
            return attributeValue.validate(attributeValue.getIpsProject()).getMessageByCode(
                    IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE) != null;
        } catch (IpsException e) {
            // conversion doesn't make sense in exception case
            return false;
        }
    }

    private static ValueDatatype findDatatype(IPropertyValue attributeValue) {
        IIpsProject ipsProject = attributeValue.getIpsProject();
        IProductCmptProperty property = attributeValue.findProperty(ipsProject);
        String datatype = property.getPropertyDatatype();
        return ipsProject.findValueDatatype(datatype);
    }

    @SuppressWarnings("unchecked")
    private static <P extends IPropertyValue> Optional<DatatypeMismatch<P>> createMismatch(P propertyValue) {
        if (propertyValue instanceof IAttributeValue) {
            return Optional.of((DatatypeMismatch<P>)new AttributeValueDatatypeMismatch((IAttributeValue)propertyValue));
        } else if (propertyValue instanceof IConfiguredDefault) {
            return Optional
                    .of((DatatypeMismatch<P>)new ConfiguredDefaultDatatypeMismatch((IConfiguredDefault)propertyValue));
        } else if (propertyValue instanceof IConfiguredValueSet configuredValueSet) {
            IValueSet valueSet = configuredValueSet.getValueSet();
            if (valueSet.isEnum()) {
                return Optional.of((DatatypeMismatch<P>)new EnumValueSetDatatypeMismatch(configuredValueSet));
            } else if (valueSet.isRange()) {
                return Optional.of((DatatypeMismatch<P>)new RangeValueSetDatatypeMismatch(configuredValueSet));
            }
        }
        return Optional.empty();
    }

    private abstract static class DatatypeMismatch<P extends IPropertyValue> {

        private final P propertyValue;

        public DatatypeMismatch(P propertyValue) {
            this.propertyValue = propertyValue;
        }

        public P getPropertyValue() {
            return propertyValue;
        }

        public abstract List<String> getValues();

        public abstract Consumer<List<String>> getValueConsumer();
    }

    private static class AttributeValueDatatypeMismatch extends DatatypeMismatch<IAttributeValue> {

        public AttributeValueDatatypeMismatch(IAttributeValue attributeValue) {
            super(attributeValue);
        }

        @Override
        public List<String> getValues() {
            List<IValue<?>> valueList = getPropertyValue().getValueHolder().getValueList();
            return valueList.stream().map(IValue::getContentAsString).collect(Collectors.toList());
        }

        @Override
        public Consumer<List<String>> getValueConsumer() {
            return t -> {
                List<IValue<?>> newValueList = t.stream().map(StringValue::new).collect(Collectors.toList());
                getPropertyValue().getValueHolder().setValueList(newValueList);
            };
        }
    }

    private static class ConfiguredDefaultDatatypeMismatch extends DatatypeMismatch<IConfiguredDefault> {

        public ConfiguredDefaultDatatypeMismatch(IConfiguredDefault propertyValue) {
            super(propertyValue);
        }

        @Override
        public List<String> getValues() {
            return Collections.singletonList(getPropertyValue().getValue());
        }

        @Override
        public Consumer<List<String>> getValueConsumer() {
            return t -> getPropertyValue().setValue(t.get(0));
        }

    }

    private abstract static class ConfiguredValueSetDatatypeMismatch<S extends IValueSet>
            extends DatatypeMismatch<IConfiguredValueSet> {

        private final S valueSet;

        @SuppressWarnings("unchecked")
        public ConfiguredValueSetDatatypeMismatch(IConfiguredValueSet propertyValue) {
            super(propertyValue);
            valueSet = (S)propertyValue.getValueSet();
        }

        public S getValueSet() {
            return valueSet;
        }

    }

    private static class EnumValueSetDatatypeMismatch extends ConfiguredValueSetDatatypeMismatch<EnumValueSet> {

        public EnumValueSetDatatypeMismatch(IConfiguredValueSet propertyValue) {
            super(propertyValue);
        }

        @Override
        public List<String> getValues() {
            return Arrays.asList(getValueSet().getValues());
        }

        @Override
        public Consumer<List<String>> getValueConsumer() {
            return values -> {
                int i = 0;
                for (String value : values) {
                    getValueSet().setValue(i++, value);
                }
            };
        }

    }

    private static class RangeValueSetDatatypeMismatch extends ConfiguredValueSetDatatypeMismatch<RangeValueSet> {

        public RangeValueSetDatatypeMismatch(IConfiguredValueSet propertyValue) {
            super(propertyValue);
        }

        @Override
        public List<String> getValues() {
            String lowerBound = getValueSet().getLowerBound();
            String upperBound = getValueSet().getUpperBound();
            String step = getValueSet().getStep();
            return Arrays.asList(lowerBound, upperBound, step);
        }

        @Override
        public Consumer<List<String>> getValueConsumer() {
            return t -> {
                getValueSet().setUpperBound(t.get(1));
                getValueSet().setLowerBound(t.get(0));
                getValueSet().setStep(t.get(2));
            };
        }

    }
}
