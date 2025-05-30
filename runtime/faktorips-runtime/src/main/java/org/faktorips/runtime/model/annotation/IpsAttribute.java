/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.faktorips.runtime.model.type.Attribute;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;

/**
 * Preserves design time information about a model attribute for runtime reference via
 * {@link Attribute}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface IpsAttribute {

    String name();

    AttributeKind kind();

    ValueSetKind valueSetKind();

    boolean primitive() default false;

    /**
     * Whether the attribute is hidden from viewing in the product component editor.
     *
     * @since 25.7
     */
    boolean hide() default false;
}
