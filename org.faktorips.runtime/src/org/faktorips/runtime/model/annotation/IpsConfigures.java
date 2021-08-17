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

import org.faktorips.runtime.IConfigurableModelObject;

/**
 * Links a {@code IProductCmptType} with the policy component type that it configures.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IpsConfigures {

    /**
     * The policy component type class that is configured by this product component type. If a
     * published interface is generated, the published interface class is returned. Else the
     * implementation class is returned.
     */
    Class<? extends IConfigurableModelObject> value();
}
