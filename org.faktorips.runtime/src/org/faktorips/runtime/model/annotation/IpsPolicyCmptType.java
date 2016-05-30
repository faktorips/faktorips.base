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

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.modeltype.internal.ModelType;

/**
 * Preserves design time information about a {@code IPolicyCmptType} for runtime reference via
 * {@link ModelType}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IpsPolicyCmptType {

    /**
     * The qualified IPS object name.
     */
    String name();

    /**
     * The {@link IProductComponent} configuring this {@link IConfigurableModelObject}
     */
    Class<? extends IProductComponent> configuredBy();

}
