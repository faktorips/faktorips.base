/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static java.util.Objects.requireNonNull;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.UnrestrictedValueSet;
import org.faktorips.valueset.ValueSet;

/**
 * A {@link Attribute} represents an attribute from a PolicyCmptType or a ProductCmptType.
 */
public abstract class Attribute extends TypePart {

    private final IpsAttribute attributeAnnotation;

    private final Class<?> datatype;

    private final boolean changingOverTime;

    private final FieldFinder.ModelValueSet modelValueSet;

    private final FieldFinder.ModelDefaultValue modelDefaultValue;

    public Attribute(Type type, IpsAttribute attributeAnnotation, IpsExtensionProperties extensionProperties,
            Class<?> datatype, boolean changingOverTime, Optional<Deprecation> deprecation) {
        super(attributeAnnotation.name(), type, extensionProperties, deprecation);
        this.attributeAnnotation = attributeAnnotation;
        this.datatype = datatype;
        this.changingOverTime = changingOverTime;
        modelValueSet = new FieldFinder.ModelValueSet(type, getName(), changingOverTime);
        modelDefaultValue = new FieldFinder.ModelDefaultValue(type, getName(), changingOverTime);
    }

    /**
     * Returns true if this attribute is changing over time. For product attribute that means the
     * attribute resides in the generation. For policy attributes the optional product configuration
     * resides in the generation.
     *
     * @return <code>true</code> if the attribute is changing over time, <code>false</code> if not
     */
    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    /**
     * Returns true if this attribute is configured by the product. Product attributes are always
     * product relevant.
     *
     * @return <code>true</code> if this attribute is configured by the product, <code>false</code>
     *             if not
     */
    public abstract boolean isProductRelevant();

    /**
     * Returns the data type of this attribute.
     *
     * @return the attribute's datatype <code>Class</code>
     */
    public Class<?> getDatatype() {
        return datatype;
    }

    /**
     * Returns the possible kinds of this attribute.
     *
     * @return the kind of attribute
     */
    public AttributeKind getAttributeKind() {
        return attributeAnnotation.kind();
    }

    /**
     * Returns the <code>ValueSetKind</code> of this attribute.
     *
     * @return the kind of value set restricting this attribute
     */
    public ValueSetKind getValueSetKind() {
        return attributeAnnotation.valueSetKind();
    }

    /**
     * Returns <code>true</code> if this attribute overrides another attribute. That means a
     * supertype declares an attribute with the same name.
     *
     * @return <code>true</code> if this attribute overrides another, <code>false</code> if not
     * @see #getSuperAttribute()
     */
    public boolean isOverriding() {
        return getType().findSuperType().map(s -> s.isAttributePresent(getName())).orElse(false);
    }

    /**
     * Returns the attribute that is overridden by this attribute if this attribute overrides
     * another one. Otherwise returns <code>null</code>.
     *
     * @return The attribute that is overridden by this attribute.
     * @see #isOverriding()
     */
    public Attribute getSuperAttribute() {
        return findSuperAttribute().orElse(null);
    }

    /**
     * Returns the attribute that is overridden by this attribute if this attribute overrides
     * another one. Otherwise returns <code>null</code>.
     *
     * @return The attribute that is overridden by this attribute.
     * @see #isOverriding()
     */
    public Optional<Attribute> findSuperAttribute() {
        return isOverriding() ? getType().findSuperType().map(s -> s.getAttribute(getName())) : Optional.empty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(datatype.getSimpleName());
        sb.append('(');
        sb.append(getAttributeKind());
        sb.append(", ");
        sb.append(getValueSetKind());
        if (isProductRelevant()) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Creates an attribute model for a sub type in which this attribute is overwritten. This is
     * necessary to retrieve information contained in the class annotation such as labels if no
     * getter is generated for the overwritten attribute in the sub class.
     *
     * @param subType a model type representing a sub type of this attribute's model type
     * @return a {@link Attribute} for the given sub model type
     */
    public abstract Attribute createOverwritingAttributeFor(Type subType);

    protected Object getRelevantProductObject(IProductComponent productComponent, Calendar effectiveDate) {
        return getRelevantProductObject(productComponent, effectiveDate, isChangingOverTime());
    }

    @Override
    protected String getDocumentation(Locale locale, DocumentationKind type, String fallback) {
        return Documentation.of(this, type, locale, fallback, this::findSuperAttribute);
    }

    /**
     * Validates this attribute's configuration in the given product against the model.
     *
     * @param list a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     * @param product the {@link IProductComponent} to validate
     * @param effectiveDate the date that determines which {@link IProductComponentGeneration} is to
     *            be validated, if the {@link IProductComponent} has any
     */
    @Override
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent product,
            Calendar effectiveDate) {
        requireNonNull(list, "list must not be null");
        requireNonNull(context, "context must not be null");
        requireNonNull(product, "product must not be null");
    }

    /**
     * Returns the value set defined in the model for this attribute.
     *
     * @since 24.1
     */
    public ValueSet<?> getValueSetFromModel() {
        return modelValueSet.get().orElseGet(UnrestrictedValueSet::new);
    }

    /**
     * Returns the default value defined in the model for this attribute.
     *
     * @since 24.1
     */
    @SuppressWarnings("unchecked")
    public <T> T getDefaultValueFromModel() {
        return (T)modelDefaultValue.get().orElseGet(() -> NullObjects.of(getDatatype()));
    }

    // CSOFF: ParameterNumber
    protected <V, R> void validate(MessageList list,
            IValidationContext context,
            Supplier<V> valueGetter,
            Supplier<R> referenceValueGetter,
            BiPredicate<V, R> valueChecker,
            String msgCode,
            String msgKey,
            String property,
            ObjectProperty invalidObjectProperty) {
        V value = valueGetter.get();
        if (!ObjectUtil.isNull(value)) {
            R referenceValue = referenceValueGetter.get();
            if (!ObjectUtil.isNull(referenceValue) && !valueChecker.test(value, referenceValue)) {
                Locale locale = context.getLocale();
                ResourceBundle messages = ResourceBundle.getBundle(getResourceBundleName(), locale);
                String formatString = String.format(messages.getString(msgKey), getLabel(locale), value,
                        referenceValue);
                if (value instanceof GregorianCalendar calendar) {
                    formatString = createFormattedGregorianCalendarString(msgKey,
                            calendar,
                            referenceValue,
                            locale,
                            messages);
                }
                if (value instanceof TemporalAccessor temporalAccessor) {
                    formatString = createFormattedJava8TimeString(msgKey,
                            temporalAccessor,
                            referenceValue,
                            locale,
                            messages);
                }
                list.newError(msgCode, formatString, new ObjectProperty(this, property), invalidObjectProperty);
            }
        }
    }
    // CSON: ParameterNumber

    private <R> String createFormattedJava8TimeString(String msgKey,
            TemporalAccessor value,
            R referenceValue,
            Locale locale,
            ResourceBundle messages) {

        final DateTimeFormatter dateTimeFormatter;
        if (value.isSupported(ChronoField.MINUTE_OF_HOUR) && value.isSupported(ChronoField.YEAR)) {
            dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
        } else if (value.isSupported(ChronoField.YEAR) && value.isSupported(ChronoField.DAY_OF_MONTH)) {
            dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        } else if (value.isSupported(ChronoField.DAY_OF_MONTH) && value.isSupported(ChronoField.MONTH_OF_YEAR)) {
            dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd", locale);
        } else {
            dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale);
        }

        String formattedValue = dateTimeFormatter.format(value);
        String formattedRefValue = referenceValue.toString();

        if (referenceValue instanceof ValueSet) {
            ValueSet<TemporalAccessor> valueSet = castJava8TimeValueSet(referenceValue);
            if (valueSet.isDiscrete()) {
                formattedRefValue = valueSet.getValues(false).stream().map(t -> {
                    if (t == null) {
                        return String.valueOf(t);
                    } else {
                        return dateTimeFormatter.format(t);
                    }
                }).collect(Collectors.joining(", ", "", ""));
            }
        }
        return String.format(messages.getString(msgKey), formattedValue, getLabel(locale),
                formattedRefValue);
    }

    private <R> String createFormattedGregorianCalendarString(String msgKey,
            GregorianCalendar calendarValue,
            R referenceValue,
            Locale locale,
            ResourceBundle messages) {
        SimpleDateFormat sdf = (SimpleDateFormat)SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT,
                locale);
        String formattedValue = sdf.format(calendarValue.getTime());
        String formattedRefValue = referenceValue.toString();

        if (referenceValue instanceof ValueSet) {
            ValueSet<GregorianCalendar> valueSet = castGregorianCalendarValueSet(referenceValue);
            if (valueSet.isDiscrete()) {
                formattedRefValue = valueSet.getValues(false).stream().map(c -> {
                    if (c == null) {
                        return String.valueOf(c);
                    } else {
                        return sdf.format(c.getTime());
                    }
                }).collect(Collectors.joining(", ", "", ""));
            }
        }
        return String.format(messages.getString(msgKey), formattedValue, getLabel(locale),
                formattedRefValue);
    }

    @SuppressWarnings("unchecked")
    private <R> ValueSet<GregorianCalendar> castGregorianCalendarValueSet(R referenceValue) {
        return (ValueSet<GregorianCalendar>)referenceValue;
    }

    @SuppressWarnings("unchecked")
    private <R> ValueSet<TemporalAccessor> castJava8TimeValueSet(R referenceValue) {
        return (ValueSet<TemporalAccessor>)referenceValue;
    }

    protected abstract String getResourceBundleName();

}
