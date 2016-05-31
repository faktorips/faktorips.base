/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAssociation.AssociationType;

/**
 * Preserves design time information about a model association for runtime reference via
 * {@link IModelTypeAssociation}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IpsAssociation {

    /**
     * The name used for a/the single target of the association.
     */
    String name();

    /**
     * The name used for multiple targets of the association.
     */
    String pluralName();

    AssociationType type();

    /**
     * The association's target.
     */
    Class<?> targetClass();

}
