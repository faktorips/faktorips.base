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

import static org.faktorips.runtime.model.type.ModelElement.invokeField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;

import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.type.Type.AnnotatedElementMatcher;
import org.faktorips.valueset.ValueSet;

/**
 * Encapsulates finding a field annotated with a given {@code ANNOTATION} matching certain criteria
 * and getting the field's value.
 *
 * @param <ANNOTATION> an {@link Annotation} that can be applied to a {@link Field}
 * @param <T> the {@link Field}'s datatype
 * @since 24.1
 */
class FieldFinder<ANNOTATION extends Annotation, T> {

    private final Type type;
    private final Class<ANNOTATION> annotationClass;
    private final AnnotatedElementMatcher<ANNOTATION> matcher;
    private Optional<Field> field;
    private boolean changingOverTime;

    private FieldFinder(Type type, Class<ANNOTATION> annotationClass, AnnotatedElementMatcher<ANNOTATION> matcher,
            boolean changingOverTime) {
        this.type = type;
        this.annotationClass = annotationClass;
        this.matcher = matcher;
        this.changingOverTime = changingOverTime;
    }

    /**
     * Returns the field's value, if such a field exists.
     *
     * @throws IllegalArgumentException if accessing the field fails.
     */
    @SuppressWarnings("unchecked")
    public Optional<T> get() {
        return getField().map(field -> (T)invokeField(field, null));
    }

    private Optional<Field> getField() {
        if (field != null) {
            return field;
        } else {
            field = findField();
            return field;
        }
    }

    private Optional<Field> findField() {
        if (type instanceof ProductCmptType && changingOverTime) {
            return ((ProductCmptType)type).findDeclaredFieldFromGeneration(annotationClass, matcher);
        }
        return type.findDeclaredField(annotationClass, matcher);
    }

    /**
     * Finds the field marked with {@link IpsAllowedValues @IpsAllowedValues} matching the given
     * attribute name.
     *
     * @since 24.1
     */
    static class ModelValueSet extends FieldFinder<IpsAllowedValues, ValueSet<?>> {

        ModelValueSet(Type type, String attributeName, boolean changingOverTime) {
            super(type, IpsAllowedValues.class, a -> a.value().equals(attributeName), changingOverTime);
        }

    }

    /**
     * Finds the field marked with {@link IpsAllowedValues IpsDefaultValue} matching the given
     * attribute name.
     *
     * @since 24.1
     */
    static class ModelDefaultValue extends FieldFinder<IpsDefaultValue, Object> {

        ModelDefaultValue(Type type, String attributeName, boolean changingOverTime) {
            super(type, IpsDefaultValue.class, a -> a.value().equals(attributeName), changingOverTime);
        }

    }

}
