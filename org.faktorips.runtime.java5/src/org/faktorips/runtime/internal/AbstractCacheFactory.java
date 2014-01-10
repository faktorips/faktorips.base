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

    public IComputable<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable) {
        return createCache(computable);
    }

    public IComputable<String, IProductComponent> createProductCmptCache(IComputable<String, IProductComponent> computable) {
        return createCache(computable);
    }

    public IComputable<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(IComputable<GenerationId, IProductComponentGeneration> computable) {
        return createCache(computable);
    }

    public IComputable<String, ITable> createTableCache(IComputable<String, ITable> computable) {
        return createCache(computable);
    }

}
