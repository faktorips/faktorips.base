/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractDateHelperFactory<T extends ValueDatatype> implements DatatypeHelperFactory {

    private final Class<T> datatypeClass;

    private final LoadingCache<CacheKey<T>, DatatypeHelper> datatypeHelperCache;

    public AbstractDateHelperFactory(Class<T> datatypeClass) {
        super();
        this.datatypeClass = datatypeClass;
        datatypeHelperCache = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey<T>, DatatypeHelper>() {

            @Override
            public DatatypeHelper load(CacheKey<T> key) throws Exception {
                return createDatatypeHelper(key.datatype, key.variant);
            }

        });
    }

    @Override
    public DatatypeHelper createDatatypeHelper(Datatype datatype, StandardBuilderSet builderSet) {
        ArgumentCheck.isInstanceOf(datatype, datatypeClass);
        try {
            @SuppressWarnings("unchecked")
            T castedDatatype = (T)datatype;
            return datatypeHelperCache.get(new CacheKey<>(castedDatatype, builderSet.getLocalDateHelperVariant()));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    abstract DatatypeHelper createDatatypeHelper(T datatype, LocalDateHelperVariant variant);

    private static class CacheKey<T extends Datatype> {

        private final LocalDateHelperVariant variant;

        private final T datatype;

        public CacheKey(T datatype, LocalDateHelperVariant variant) {
            this.datatype = datatype;
            this.variant = variant;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
            result = prime * result + ((variant == null) ? 0 : variant.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            @SuppressWarnings("unchecked")
            CacheKey<T> other = (CacheKey<T>)obj;
            if (datatype == null) {
                if (other.datatype != null) {
                    return false;
                }
            } else if (!datatype.equals(other.datatype)) {
                return false;
            }
            if (variant == null) {
                if (other.variant != null) {
                    return false;
                }
            } else if (!variant.equals(other.variant)) {
                return false;
            }
            return true;
        }

    }

}
