/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.List;

import org.faktorips.runtime.caching.IComputable;

/**
 * A factory for creating caches used by the repository.
 * 
 * @author Jan Ortmann
 */
public interface ICacheFactory {

    /**
     * Creates a new cache of the given type.
     */
    <K, V> IComputable<K, V> createCache(IComputable<K, V> computable);

    IComputable<String, IProductComponent> createProductCmptCache(
            IComputable<String, IProductComponent> computable);

    IComputable<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(
            IComputable<GenerationId, IProductComponentGeneration> computable);

    IComputable<String, ITable<?>> createTableCache(IComputable<String, ITable<?>> computable);

    IComputable<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable);

}
