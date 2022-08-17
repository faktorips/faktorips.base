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

import org.faktorips.runtime.model.type.Association;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.PolicyAssociation;
import org.faktorips.runtime.model.type.ProductAssociation;

/**
 * Preserves design time information about a model association for runtime reference via
 * {@link Association}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IpsAssociation {

    /**
     * The name used for a/the single target of the association.
     */
    String name();

    /**
     * The name used for multiple targets of the association.
     */
    String pluralName() default "";

    AssociationKind kind();

    /**
     * The association's target.
     */
    Class<?> targetClass();

    /**
     * Minimal number of targets for this association
     */
    int min();

    /**
     * Maximal number of targets for this association
     */
    int max();

    /**
     * Whether the association is "qualified". A {@link PolicyAssociation} can be "qualified" by its
     * matching {@link ProductAssociation}, meaning that the {@link PolicyAssociation}'s
     * cardinalities are not defined for all targets but per target of the
     * {@link ProductAssociation}.
     * 
     * @since 22.6
     */
    boolean qualified() default false;
}
