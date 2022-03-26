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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.faktorips.runtime.model.type.ModelElement;

/**
 * Preserves design time information about an extension property for runtime reference via
 * {@link ModelElement#getExtensionPropertyValue(String)}.
 */
// TODO switch to @Repeating once we have Java 8:
// https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
@Retention(RetentionPolicy.RUNTIME)
public @interface IpsExtensionProperty {

    String id();

    boolean isNull() default false;

    String value() default "";

}
