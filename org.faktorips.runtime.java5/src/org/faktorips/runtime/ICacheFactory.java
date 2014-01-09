/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public <K, V> IComputable<K, V> createCache(IComputable<K, V> computable);

    public IComputable<String, IProductComponent> createProductCmptCache(IComputable<String, IProductComponent> computable);

    public IComputable<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(IComputable<GenerationId, IProductComponentGeneration> computable);

    public IComputable<String, ITable> createTableCache(IComputable<String, ITable> computable);

    public IComputable<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable);

}
