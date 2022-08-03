/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

import org.faktorips.runtime.IRuntimeRepository;

public interface IDetachedContentRuntimeRepository extends IRuntimeRepository {

    /**
     * Returning the actual version set in the product data provider.
     * 
     * @see IProductDataProvider#getVersion()
     * 
     * @return version of the product data provider
     */
    String getProductDataVersion();

}
