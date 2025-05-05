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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsExtensionProperty;
import org.faktorips.runtime.util.MessagesHelper;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * An element from the design time model representation as returned from {@link IpsModel}. Defines
 * basic properties shared by all such elements such as {@linkplain #getName() name},
 * {@linkplain #getLabel(Locale) label}, {@linkplain #getDescription(Locale) description} and
 * {@linkplain #getExtensionPropertyValue(String) extension properties}.
 */
public abstract class ModelElement {

    private final String name;

    private final Map<String, Object> extPropertyValues;

    private final Optional<Deprecation> deprecation;

    public ModelElement(String name, IpsExtensionProperties extensionProperties, Optional<Deprecation> deprecation) {
        this.name = name;
        this.deprecation = deprecation;
        extPropertyValues = initExtensionPropertyMap(extensionProperties);
    }

    private Map<String, Object> initExtensionPropertyMap(IpsExtensionProperties extensionPropertiesAnnotation) {
        Map<String, Object> result = Collections.emptyMap();
        if (extensionPropertiesAnnotation != null) {
            IpsExtensionProperty[] extensionProperties = extensionPropertiesAnnotation.value();
            result = new LinkedHashMap<>(extensionProperties.length, 1f);
            for (IpsExtensionProperty ipsExtensionProperty : extensionProperties) {
                result.put(ipsExtensionProperty.id(), initValue(ipsExtensionProperty));
            }
        }
        return result;
    }

    private Object initValue(IpsExtensionProperty ipsExtensionProperty) {
        if (ipsExtensionProperty.isNull()) {
            return null;
        } else {
            return ipsExtensionProperty.value();
        }
    }

    /**
     * @return the qualified IPS object name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the label for this model element in the specified locale. If there is no label in the
     * specified locale, it tries to find the label in the default locale. If there is also no label
     * in the default locale the element's name is returned.
     *
     * @return the label for the given locale or the element's name if no label exists for the given
     *             locale nor in the default locale
     */
    public String getLabel(Locale locale) {
        return getDocumentation(locale, DocumentationKind.LABEL, getName());
    }

    /**
     * Returns the description for this model element in the specified locale. If there is no
     * description in the specified locale, it tries to find the description in the default locale.
     * If there is also no description in the default locale it returns the empty string.
     *
     * @return the description for the given locale or an empty string if no description exists for
     *             the given locale
     */
    public String getDescription(Locale locale) {
        return getDocumentation(locale, DocumentationKind.DESCRIPTION, IpsStringUtils.EMPTY);
    }

    protected abstract String getMessageKey(DocumentationKind messageType);

    protected String getDocumentation(Locale locale, DocumentationKind type, String fallback) {
        MessagesHelper messageHelper = getMessageHelper();
        if (messageHelper != null) {
            return messageHelper.getMessageOr(getMessageKey(type), locale, fallback);
        } else {
            return fallback;
        }
    }

    protected abstract MessagesHelper getMessageHelper();

    protected MessagesHelper createMessageHelper(IpsDocumented documentedAnnotation, ClassLoader classLoader) {
        if (documentedAnnotation != null) {
            String documentationResourceBundle = documentedAnnotation.bundleName();
            Locale defaultLocale = Locale.of(documentedAnnotation.defaultLocale());
            return new MessagesHelper(documentationResourceBundle, classLoader, defaultLocale);
        } else {
            return null;
        }
    }

    /**
     * Returns the value for the given extension property identified by the specified id.
     * <p>
     * Note: At the moment only {@link String} is supported as extension property value. This method
     * returns {@link Object} for future changes.
     *
     * @return the value of the extension property defined by the given <code>propertyId</code> or
     *             <code>null</code> if the extension property's <code>isNull</code> attribute is
     *             <code>true</code>
     * @throws IllegalArgumentException if no such property exists
     */
    public Object getExtensionPropertyValue(String propertyId) {
        return Optional.ofNullable(extPropertyValues).map(v -> v.get(propertyId)).orElse(null);
    }

    /**
     * @return a set of the extension property ids defined for this element
     */
    public Set<String> getExtensionPropertyIds() {
        return extPropertyValues == null ? new HashSet<>(0) : extPropertyValues.keySet();
    }

    protected static Object invokeMethod(Method method, Object source, Object... arguments) {
        try {
            return method.invoke(source, arguments);
        } catch (InvocationTargetException e) {
            throw createGetterError(source, method, arguments, e.getCause());
        } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
            throw createGetterError(source, method, arguments, e);
        }
    }

    private static IllegalArgumentException createGetterError(Object source,
            Method method,
            Object[] args,
            Throwable e) {
        return new IllegalArgumentException(String.format("Could not call %s(%s) on source object %s.",
                method.getName(), IpsStringUtils.join(args), source), e);
    }

    protected static Object invokeField(Field field, Object source) {
        try {
            return field.get(source);
        } catch (NullPointerException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
            throw createFieldError(source, field, e);
        }
    }

    private static IllegalArgumentException createFieldError(Object source, Field field, Exception e) {
        return new IllegalArgumentException(
                String.format("Could not get value of %s on source object %s.", field.getName(), source), e);
    }

    /**
     * If the <code>changingOverTime</code> is <code>false</code>, the given product component is
     * returned. If changing over time is <code>true</code>, the effective date is used to determine
     * the generation to use. If the effective date is <code>null</code>, the latest product
     * component generation is returned.
     *
     * @param productComponent the product component to potentially retrieve a generation from
     * @param effectiveDate the date to select the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the model element's
     *            configuration is not changing over time.
     * @param changingOverTime whether the model element is changing over time.
     * @return The given product component or the effective generation, depending on
     *             changingOverTime and effectiveDate.
     */
    protected static IProductObject getRelevantProductObject(IProductComponent productComponent,
            @CheckForNull Calendar effectiveDate,
            boolean changingOverTime) {
        IProductObject source = productComponent;
        if (changingOverTime) {
            if (effectiveDate == null) {
                source = productComponent.getLatestProductComponentGeneration();
            } else {
                source = productComponent.getGenerationBase(effectiveDate);
            }
        }
        return source;
    }

    /**
     * If the <code>changingOverTime</code> is <code>true</code>, the given product component
     * generation is returned. If changing over time is <code>false</code>, the generation's product
     * component is returned.
     *
     * @param generation the product component generation to use when changing over time
     * @param changingOverTime whether the model element is changing over time
     * @return The given product component or the effective generation, depending on
     *             changingOverTime and effectiveDate.
     */
    protected static IProductObject getRelevantProductObject(IProductComponentGeneration generation,
            boolean changingOverTime) {
        return changingOverTime ? generation : generation.getProductComponent();
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isDeprecated() {
        return deprecation.isPresent();
    }

    public Optional<Deprecation> getDeprecation() {
        return deprecation;
    }
}
