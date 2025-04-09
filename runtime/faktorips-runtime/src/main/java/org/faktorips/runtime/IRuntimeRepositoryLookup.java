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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.runtime.internal.ProductConfiguration;

/**
 * An {@link IRuntimeRepositoryLookup} is an interface for a class that is able to provide a runtime
 * repository. It is used to serialize and deserialize policy components especially the
 * {@link ProductConfiguration} and enumeration contents.
 * <p>
 * An implementation needs to be serializable hence this interface already implements the
 * {@link Serializable} interface. After serializing and deserializing an object of this type the
 * method {@link #getRuntimeRepository()} must return an {@link IRuntimeRepository} with the same
 * content as before - when deserialized on the same machine, it must be the same instance, usually
 * a singleton.
 * <p>
 * For a {@link ClassloaderRuntimeRepository}, its {@link ClassloaderRuntimeRepository#withLookup()
 * withLookup()} method can be used to register it in a static lookup table that returns the same
 * instance, identified by the path to the repository's table-of-contents file.
 * <p>
 * Alternatively, a {@link ClassloaderRuntimeRepository} can be created and registered all in one
 * call to the {@link #byToC(String)} factory method.
 */
public interface IRuntimeRepositoryLookup extends Serializable {

    /**
     * Returns an instance of {@link IRuntimeRepository} that can be used to load the product
     * component and generation of a serialized {@link ProductConfiguration} after deserialization.
     *
     * @return A {@link IRuntimeRepository} used to load product components and product component
     *             generations.
     */
    IRuntimeRepository getRuntimeRepository();

    /**
     * Creates a new {@link ClassloaderRuntimeRepository} for the given table-of-contents file path
     * and returns an {@link IRuntimeRepositoryLookup} that will always return that repository on
     * this machine and recreate it if necessary when deserialized on other machines.
     * <p>
     * Be aware that referenced repositories must be set on the repository returned from
     * {@link IRuntimeRepositoryLookup#getRuntimeRepository()}, and each repository must have an
     * {@link IRuntimeRepositoryLookup}. For deserialization to work with referenced repositories,
     * the lookups need to be initialized prior to deserializing the first instance, as that would
     * create a new repository without referenced repositories.
     *
     * @param tocPath Path to the resource containing the ToC file. E.g.
     *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
     * @return an {@link IRuntimeRepositoryLookup} that will always return the same
     *             {@link ClassloaderRuntimeRepository} created from the given ToC path
     * @since 25.7
     */
    static IRuntimeRepositoryLookup byToC(String tocPath) {
        return RuntimeRepositoryLookupByToC.create(tocPath);
    }

    /**
     * An {@link IRuntimeRepositoryLookup} that serializes only the path to the
     * {@link ClassloaderRuntimeRepository}'s table-of-contents file. Create one via the
     * {@link IRuntimeRepositoryLookup#byToC(String)} or directly create and set int for an existing
     * repository with {@link ClassloaderRuntimeRepository#withLookup()}.
     *
     * @since 25.7
     */
    public static final class RuntimeRepositoryLookupByToC implements IRuntimeRepositoryLookup {

        private static final long serialVersionUID = 1L;

        private static final Map<String, IRuntimeRepository> REPOSITORIES = new ConcurrentHashMap<>();

        private final String tocPath;

        /**
         * Creates a lookup that {@link ClassloaderRuntimeRepository#create(String) creates} a new
         * {@link ClassloaderRuntimeRepository} for the given {@code tocPath} on demand.
         *
         * @param tocPath Path to the resource containing the ToC file, for example
         *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
         */
        RuntimeRepositoryLookupByToC(String tocPath) {
            this.tocPath = tocPath;
        }

        /**
         * Creates a lookup that returns the given {@link ClassloaderRuntimeRepository} for the
         * given {@code tocPath} on demand.
         *
         * @param tocPath Path to the resource containing the ToC file, for example
         *            "org/faktorips/sample/internal/faktorips-repository-toc.xml"
         * @param classloaderRuntimeRepository a {@link ClassloaderRuntimeRepository} created with
         *            the given path.
         */
        RuntimeRepositoryLookupByToC(String tocPath,
                ClassloaderRuntimeRepository classloaderRuntimeRepository) {
            this(tocPath);
            REPOSITORIES.put(tocPath, classloaderRuntimeRepository);
        }

        private static RuntimeRepositoryLookupByToC create(String tocPath) {
            var lookup = new RuntimeRepositoryLookupByToC(tocPath);
            var runtimeRepository = lookup.getRuntimeRepository();
            runtimeRepository.setRuntimeRepositoryLookup(lookup);
            return lookup;
        }

        @Override
        public IRuntimeRepository getRuntimeRepository() {
            return REPOSITORIES.computeIfAbsent(tocPath, ClassloaderRuntimeRepository::create);
        }
    }

}
