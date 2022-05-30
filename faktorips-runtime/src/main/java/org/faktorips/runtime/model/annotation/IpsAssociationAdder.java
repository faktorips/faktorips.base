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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the method for adding target objects to an association.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface IpsAssociationAdder {

    /**
     * The name of the association.
     */
    String association();

    /**
     * Indicates whether the association has a cardinality
     * 
     * @since 20.6
     */
    boolean withCardinality() default false;
}
