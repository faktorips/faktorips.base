/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IpsStringUtils;

public class TocHierarchyCreator {

    private static final Pattern PACKAGE_WITH_TREE = Pattern.compile("(?:<|>|\\[|\\])");

    /**
     * This utility class creates a {@link IRuntimeRepository} with a hierarchy from a list of
     * runtime packages. For more details see {@link #createRuntimeRepository(List, ClassLoader)}.
     */
    private TocHierarchyCreator() {
        // utility class
    }

    /**
     * Creates a {@link IRuntimeRepository} with a hierarchy from a list of runtime packages.
     * Supports flat and nested packages.
     * <p>
     * 
     * <pre>
     *       Basis
     *      /     \
     *   Sparte   VO
     *      \     /
     *     Produkte
     * </pre>
     * 
     * flat packages:
     * <p>
     * Produkte-toc.xml, Sparte-toc.xml, VO-toc.xml, Basis-toc.xml
     * <p>
     * nested packages:
     * <p>
     * Produkte-toc.xml<br>
     * Sparte-toc.xml[&lt;Base-toc.xml&gt;]<br>
     * VO-toc.xml[&lt;Base-toc.xml&gt;]<br>
     * 
     * @param runtimePackages the runtime packages
     * @param clazzLoader the {@link ClassLoader} to use
     * @return a {@link IRuntimeRepository} with additional directly referenced repositories or
     *             {@code null}
     */
    public static IRuntimeRepository createRuntimeRepository(List<String> runtimePackages,
            ClassLoader clazzLoader) {

        if (containsTocHierarchyItems(runtimePackages)) {
            Set<TocHierarchy> createTocHierarchy = createTocHierarchy(runtimePackages, clazzLoader);
            return appendToRootPackage(createTocHierarchy);
        }
        return daisyChainRuntimeRepositories(runtimePackages, clazzLoader);
    }

    /**
     * Checks if a list of packages contains hierarchy information.
     * 
     * @param runtimePackages the runtime packages
     * @return {@code true} if a package contains &gt;, &lt;, [ or ]
     */
    public static boolean containsTocHierarchyItems(List<String> runtimePackages) {
        for (String string : runtimePackages) {
            if (PACKAGE_WITH_TREE.matcher(string).find()) {
                return true;
            }
        }
        return false;
    }

    private static IRuntimeRepository daisyChainRuntimeRepositories(List<String> runtimePackages,
            ClassLoader clazzLoader) {
        IRuntimeRepository additionalRepositories = null;
        IRuntimeRepository currRepository = null;
        for (String pckg : runtimePackages) {
            IRuntimeRepository addRep = ClassloaderRuntimeRepository.create(pckg, clazzLoader);
            if (currRepository != null) {
                currRepository.addDirectlyReferencedRepository(addRep);
            } else {
                additionalRepositories = addRep;
            }
            currRepository = addRep;
        }
        return additionalRepositories;
    }

    private static IRuntimeRepository appendToRootPackage(Set<TocHierarchy> hierarchy) {
        if (!hierarchy.isEmpty()) {
            Iterator<TocHierarchy> iter = hierarchy.iterator();
            IRuntimeRepository root = iter.next().getRepository();

            while (iter.hasNext()) {
                root.addDirectlyReferencedRepository(iter.next().getRepository());
            }
            return root;
        }
        return null;
    }

    private static Set<TocHierarchy> createTocHierarchy(List<String> runtimePackages,
            ClassLoader clazzLoader) {
        Map<String, TocHierarchy> cachedTocs = new HashMap<>();

        return createTocHierarchy(runtimePackages, cachedTocs, clazzLoader);
    }

    private static Set<TocHierarchy> createTocHierarchy(List<String> tocs,
            Map<String, TocHierarchy> cachedTocs,
            ClassLoader clazzLoader) {
        Set<TocHierarchy> pckgs = new LinkedHashSet<>();

        for (String string : tocs) {
            if (string.contains("[")) {
                String thePckg = string.substring(0, string.indexOf('['));
                TocHierarchy dep = cachedTocs.computeIfAbsent(thePckg,
                        k -> new TocHierarchy(k, clazzLoader));

                List<String> internTocs = Arrays
                        .asList(string.substring(string.indexOf('[') + 2, string.lastIndexOf(']') - 1).split(">,<"));

                dep.addAll(createTocHierarchy(internTocs, cachedTocs, clazzLoader));
                pckgs.add(dep);
            } else if (IpsStringUtils.isNotBlank(string)) {
                TocHierarchy dep = cachedTocs.computeIfAbsent(string, k -> new TocHierarchy(string, clazzLoader));
                pckgs.add(dep);
            }
        }
        return pckgs;
    }

    private static class TocHierarchy {

        private final String pckg;
        private final IRuntimeRepository classLoaderRepository;
        private final Set<TocHierarchy> dependencies;

        public TocHierarchy(String pckg, ClassLoader clazzLoader) {
            this.pckg = pckg;
            classLoaderRepository = ClassloaderRuntimeRepository.create(pckg, clazzLoader);
            dependencies = new LinkedHashSet<>();
        }

        public void addAll(Collection<TocHierarchy> deps) {
            for (TocHierarchy th : deps) {
                classLoaderRepository.addDirectlyReferencedRepository(th.classLoaderRepository);
                dependencies.add(th);
            }
        }

        public IRuntimeRepository getRepository() {
            return classLoaderRepository;
        }

        @Override
        public String toString() {
            return pckg + ":" + dependencies.stream()
                    .map(TocHierarchy::toString)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
    }
}
