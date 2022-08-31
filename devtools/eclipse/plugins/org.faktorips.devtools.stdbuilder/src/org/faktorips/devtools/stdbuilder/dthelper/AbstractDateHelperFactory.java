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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractDateHelperFactory<T extends ValueDatatype> implements DatatypeHelperFactory {

    private final Class<T> datatypeClass;

    private final Map<CacheKey<T>, DatatypeHelper> datatypeHelperCache;

    public AbstractDateHelperFactory(Class<T> datatypeClass) {
        super();
        this.datatypeClass = datatypeClass;
        datatypeHelperCache = new ConcurrentHashMap<>();
    }

    @Override
    public DatatypeHelper createDatatypeHelper(Datatype datatype, StandardBuilderSet builderSet) {
        ArgumentCheck.isInstanceOf(datatype, datatypeClass);
        @SuppressWarnings("unchecked")
        T castedDatatype = (T)datatype;
        return datatypeHelperCache.computeIfAbsent(
                new CacheKey<>(castedDatatype, builderSet.getLocalDateHelperVariant()),
                key -> createDatatypeHelper(key.datatype, key.variant));
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
            return Objects.hash(datatype, variant);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            @SuppressWarnings("unchecked")
            CacheKey<T> other = (CacheKey<T>)obj;
            return Objects.equals(datatype, other.datatype)
                    && Objects.equals(variant, other.variant);
        }

    }

}
