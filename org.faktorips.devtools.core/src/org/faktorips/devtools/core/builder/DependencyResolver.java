/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.internal.builder.DependencyGraph;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.MultiMap;

/**
 * A {@link DependencyResolver} resolves the dependencies for any object using the
 * {@link DependencyGraph}. The dependency resolver searches always in the context of a single
 * project and hence a single dependency graph. However it would create new dependency resolver
 * instances for dependent projects if it is necessary. All dependencies are collected per project.
 * The result is a map with projects as key and a set of the corresponding dependencies as value.
 * The resulting map is returned by the method {@link #getCollectedDependencies()}.
 * <p>
 * Normally only direct dependencies are resolved. Transitive dependencies are only resolved in the
 * following cases:
 * 
 * 1. For Dependency of type {@link DependencyType#SUBTYPE} all transitive dependencies are found
 * 
 * 2. For Dependency of type {@link DependencyType#DATATYPE} or {@link DependencyType#REFERENCE}
 * only transitive dependencies of type {@link DependencyType#INSTANCEOF} are found
 * 
 * 3. For Dependency of type {@link DependencyType#REFERENCE_COMPOSITION_MASTER_DETAIL} all
 * transitive dependencies are found iff the method
 * {@link IIpsArtefactBuilderSet#containsAggregateRootBuilder()} returns true.
 * 
 */
public class DependencyResolver {

    private final IIpsProject ipsProject;

    private final IDependencyGraph graph;

    private final MultiMap<IIpsProject, IDependency> dependenciesForProjectMap = MultiMap.createWithSetsAsValues();

    /**
     * Creates a new {@link DependencyResolver} for the specified project. If there are other
     * projects that depends on this project and hence needs to be searched for dependencies, this
     * dependency resolver creates other instances for the other projects.
     * 
     * @param ipsProject The {@link IIpsProject} for which the dependencies should be collected.
     */
    public DependencyResolver(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        graph = getDependencyGraph();
    }

    private IDependencyGraph getDependencyGraph() {
        return ipsProject.getDependencyGraph();
    }

    /**
     * This method collects all the dependencies for the given list of {@link IIpsSrcFile}. To get
     * the resulting map of dependent objects, call {@link #getCollectedDependencies()}.
     * 
     * @param addedOrChangesIpsSrcFiles Source files that are changed or added
     * @param removedIpsSrcFiles Source files that are removed
     * 
     * @return The number of found dependencies.
     */
    public MultiMap<IIpsProject, IDependency> collectDependenciesForIncrementalBuild(List<IIpsSrcFile> addedOrChangesIpsSrcFiles,
            List<IIpsSrcFile> removedIpsSrcFiles) {
        if (canCollectDependencies()) {
            collectDependenciesFor(addedOrChangesIpsSrcFiles);
            collectDependenciesFor(removedIpsSrcFiles);
        }
        return dependenciesForProjectMap;
    }

    private boolean canCollectDependencies() {
        return graph != null && ipsProject.canBeBuild();
    }

    private void collectDependenciesFor(List<IIpsSrcFile> addedOrChangesIpsSrcFiles) {
        for (IIpsSrcFile ipsSrcFile : addedOrChangesIpsSrcFiles) {
            collectDependencies(ipsSrcFile.getQualifiedNameType(), new HashSet<IIpsProject>(), false);
        }
    }

    /**
     * This method collects all dependencies for a single object identified by its
     * {@link QualifiedNameType}.
     * 
     * @param root The object for which the method should return every dependencies, that means
     *            dependencies that have this root as target.
     * @param visitedProjects Projects that were already visited to avoid cycle lookups
     * @param searchInstanceOfDependencyOnly <code>true</code> to only consider instance of
     *            dependencies. This switch is necessary to no resolve every transitive dependencies
     *            but resolve transitive instance of dependencies.
     */
    void collectDependencies(QualifiedNameType root,
            Set<IIpsProject> visitedProjects,
            boolean searchInstanceOfDependencyOnly) {
        if (canCollectDependencies()) {
            collectDependencies(root, searchInstanceOfDependencyOnly);
            collectDependenciesOfDependantProjects(root, visitedProjects, searchInstanceOfDependencyOnly);
        }
    }

    private void collectDependencies(QualifiedNameType root, boolean searchInstanceOfDependencyOnly) {
        IDependency[] dependencies = graph.getDependants(root);
        for (IDependency dependency : dependencies) {
            if (isProperDependency(dependency, searchInstanceOfDependencyOnly)) {
                dependenciesForProjectMap.put(ipsProject, dependency);
            }
            considerTransitiveDependencies(dependency, searchInstanceOfDependencyOnly);
        }
    }

    private boolean isProperDependency(IDependency dependency, boolean searchInstanceOfOnly) {
        return DependencyType.INSTANCEOF.equals(dependency.getType()) || !searchInstanceOfOnly;
    }

    private void considerTransitiveDependencies(IDependency dependency, boolean searchInstanceOfDependencyOnly) {
        if (!searchInstanceOfDependencyOnly) {
            if (dependency.getType().equals(DependencyType.SUBTYPE)) {
                collectTransitivDependencies(dependency, false);
            } else if (dependency.getType().equals(DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL)
                    && getArtefactBuilderSet().containsAggregateRootBuilder()) {
                collectTransitivDependencies(dependency, false);
            } else if (dependency.getType().equals(DependencyType.REFERENCE)
                    || dependency.getType().equals(DependencyType.DATATYPE)) {
                collectTransitivDependencies(dependency, true);
            }
        }
    }

    private void collectTransitivDependencies(IDependency dependency, boolean onlyInstanceOfDeps) {
        collectDependencies(dependency.getSource(), new HashSet<IIpsProject>(), onlyInstanceOfDeps);
    }

    private void collectDependenciesOfDependantProjects(QualifiedNameType root,
            Set<IIpsProject> visitedProjects,
            boolean searchInstanceOfDependencyOnly) {

        visitedProjects.add(ipsProject);
        IIpsProject[] dependantProjects = ipsProject.findReferencingProjects(false);
        for (IIpsProject dependantProject : dependantProjects) {
            if (!visitedProjects.contains(dependantProject)) {
                DependencyResolver dependencyResolver = new DependencyResolver(dependantProject);
                dependencyResolver.collectDependencies(root, visitedProjects, searchInstanceOfDependencyOnly);
                dependenciesForProjectMap.merge(dependencyResolver.getCollectedDependencies());
            } else {
                break;
            }
        }
    }

    private IIpsArtefactBuilderSet getArtefactBuilderSet() {
        return ipsProject.getIpsArtefactBuilderSet();
    }

    /**
     * Returns the resulting map with the projects as keys and for every project a set with resolved
     * dependencies.
     */
    public MultiMap<IIpsProject, IDependency> getCollectedDependencies() {
        return dependenciesForProjectMap;
    }

}
