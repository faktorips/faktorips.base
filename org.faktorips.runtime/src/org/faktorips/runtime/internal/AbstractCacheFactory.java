/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.List;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.caching.IComputable;

public abstract class AbstractCacheFactory implements ICacheFactory {

    public AbstractCacheFactory() {
        super();
    }

    @Override
    public IComputable<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable) {
        return createCache(computable);
    }

    @Override
    public IComputable<String, IProductComponent> createProductCmptCache(
            IComputable<String, IProductComponent> computable) {
        return createCache(computable);
    }

    @Override
    public IComputable<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(
            IComputable<GenerationId, IProductComponentGeneration> computable) {
        return createCache(computable);
    }

    @Override
    public IComputable<String, ITable<?>> createTableCache(IComputable<String, ITable<?>> computable) {
        return createCache(computable);
    }

}
