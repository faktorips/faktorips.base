/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * The source of an {@link IProductComponentLink}. This may be a {@link IProductComponent} or a
 * {@link IProductComponentGeneration}.
 * 
 */
public interface IProductComponentLinkSource {

    /**
     * Getting the runtime repository of this link source.
     * 
     * @return The {@link IRuntimeRepository} that was used to create this
     *         {@link IProductComponentLinkSource}
     */
    public IRuntimeRepository getRepository();

}
