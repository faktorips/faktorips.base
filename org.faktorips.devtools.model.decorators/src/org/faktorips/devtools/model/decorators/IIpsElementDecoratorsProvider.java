/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.decorators;

import java.util.Map;

import org.faktorips.devtools.model.IIpsElement;

/**
 * Provider for additional {@link IIpsElementDecorator IIpsElementDecorators}, to be used by
 * {@link IIpsDecorators}.
 */
public interface IIpsElementDecoratorsProvider {

    Map<Class<? extends IIpsElement>, IIpsElementDecorator> getDecoratorsByElementClass();
}
