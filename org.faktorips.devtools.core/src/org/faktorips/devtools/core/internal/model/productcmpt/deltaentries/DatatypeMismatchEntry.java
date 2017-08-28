/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.util.functional.Consumer;

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
        return NLS.bind(Messages.DatatypeMismatchEntry_datatypeMissmatchDescription, Joiner.on(", ").join(oldValues), //$NON-NLS-1$
                Joiner.on(", ").join(convertedValues())); //$NON-NLS-1$
    }

    @Override
    public void fix() {
        List<String> converted = convertedValues();
        valueConsumer.accept(converted);
    }

    private List<String> convertedValues() {
        List<String> converted = Lists.transform(oldValues, new Function<String, String>() {

            @Override
            public String apply(String input) {
                return converter.convert(input, getPropertyValue().getIpsProject());
            }
        });
        return converted;
    }

    /**
     * Creates a {@link DatatypeMismatchEntry} for each {@link IPropertyValue} in the given list
     * that has a datatype not matching the corresponding {@link IProductCmptProperty}'s datatype.
     */
    public static List<DatatypeMismatchEntry> forEachMismatch(List<? extends IPropertyValue> values) {
        List<DatatypeMismatchEntry> result = new ArrayList<DatatypeMismatchEntry>();
        for (IPropertyValue propertyValue : values) {
            if (propertyValue instanceof IAttributeValue) {
                IAttributeValue attributeValue = (IAttributeValue)propertyValue;
                Optional<DatatypeMismatchEntry> entry = createAttributeValue(attributeValue);
                if (entry.isPresent()) {
                    result.add(entry.get());
                }
            } else if (propertyValue instanceof IConfiguredDefault) {
                IConfiguredDefault configuredDefault = (IConfiguredDefault)propertyValue;
                Optional<DatatypeMismatchEntry> entry = createConfiguredDefault(configuredDefault);
                if (entry.isPresent()) {
                    result.add(entry.get());
                }
            }
        }
        return result;
    }

    /* private */ static Optional<DatatypeMismatchEntry> createAttributeValue(final IAttributeValue attributeValue) {
        if (isConversionNeeded(attributeValue)) {
            List<IValue<?>> valueList = attributeValue.getValueHolder().getValueList();
            List<String> values = Lists.transform(valueList, new Function<IValue<?>, String>() {
                @Override
                public String apply(IValue<?> input) {
                    // no usecase for converting international strings
                    return input.getContentAsString();
                }
            });
            ValueDatatype datatype = findDatatype(attributeValue);
            ValueConverter converter = ValueConverter.getByTargetType(datatype);
            if (converter != null) {
                Consumer<List<String>> valueConsumer = new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> t) {
                        List<IValue<?>> newValueList = Lists.transform(t, new Function<String, IValue<?>>() {
                            @Override
                            public IValue<?> apply(String input) {
                                return new StringValue(input);
                            }
                        });
                        attributeValue.getValueHolder().setValueList(newValueList);
                    }
                };
                return Optional.of(new DatatypeMismatchEntry(attributeValue, values, converter, valueConsumer));
            }
        }

        return Optional.absent();
    }

    /* private */ static Optional<DatatypeMismatchEntry> createConfiguredDefault(
            final IConfiguredDefault configuredDefault) {
        if (isConversionNeeded(configuredDefault)) {
            String value = configuredDefault.getValue();
            ValueDatatype datatype = findDatatype(configuredDefault);
            ValueConverter converter = ValueConverter.getByTargetType(datatype);
            if (converter != null) {
                Consumer<List<String>> valueConsumer = new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> t) {
                        configuredDefault.setValue(t.get(0));
                    }
                };
                return Optional.of(new DatatypeMismatchEntry(configuredDefault, Collections.singletonList(value),
                        converter, valueConsumer));
            }
        }

        return Optional.absent();
    }

    private static boolean isConversionNeeded(IPropertyValue attributeValue) {
        try {
            return attributeValue.validate(attributeValue.getIpsProject()).getMessageByCode(
                    IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE) != null;
        } catch (CoreException e) {
            // conversion doesn't make sense in exception case
            return false;
        }
    }

    private static ValueDatatype findDatatype(IPropertyValue attributeValue) {
        try {
            IIpsProject ipsProject = attributeValue.getIpsProject();
            IProductCmptProperty property = attributeValue.findProperty(ipsProject);
            String datatype = property.getPropertyDatatype();
            return ipsProject.findValueDatatype(datatype);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
