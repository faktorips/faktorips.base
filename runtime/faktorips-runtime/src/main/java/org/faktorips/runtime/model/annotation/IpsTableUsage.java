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

import org.faktorips.runtime.ITable;

/**
 * Preserves design time information about the used table
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IpsTableUsage {
    /**
     * The name of the used table.
     */
    String name();

    /**
     * Indicates whether the table usage is required.
     *
     * @return true if the table usage is required, false otherwise
     */
    boolean required() default false;

    /**
     * Returns the allowed table types.
     *
     * @return an array of classes representing the allowed table types
     */
    Class<? extends ITable<?>>[] tableClasses();
}
