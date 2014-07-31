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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.builder.DependencyGraph;
import org.faktorips.devtools.core.model.DependencyType;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.MultiMap;

/**
 * Resolves the dependencies for any object using the {@link DependencyGraph}. Even though the scope
 * of each {@link DependencyGraph} instance is a single project, the dependency resolver creates
 * {@link DependencyResolver} instances for dependent projects where necessary and searches them
 * recursively.
 * <p>
 * The method {@link #getCollectedDependencies()} provides all dependencies in a map. That map
 * contains projects as keys and sets of the corresponding dependencies as values. Thus dependencies
 * a categorized by the project they come from.
 * <p>
 * Normally only direct dependencies are resolved. Transitive dependencies are only resolved in the
 * following cases:
 * <p>
 * 1. For dependencies of type {@link DependencyType#SUBTYPE} all transitive dependencies are taken
 * into account
 * <p>
 * 2. For dependencies of type {@link DependencyType#DATATYPE} and {@link DependencyType#REFERENCE}
 * only transitive dependencies of type {@link DependencyType#INSTANCEOF} are taken into account. To
 * get all {@link DependencyType#INSTANCEOF} dependencies, the {@link DependencyType#SUBTYPE}
 * dependencies are taken into account, too.
 * <p>
 * 3. For dependencies of type {@link DependencyType#REFERENCE_COMPOSITION_MASTER_DETAIL} all
 * transitive dependencies are taken into account if the method
 * {@link IIpsArtefactBuilderSet#containsAggregateRootBuilder()} returns <code>true</code>.
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
     * @param addedOrChangedIpsSrcFiles Source files that are changed or added
     * @param removedIpsSrcFiles Source files that are removed
     * 
     * @return The number of found dependencies.
     */
    public MultiMap<IIpsProject, IDependency> collectDependenciesForIncrementalBuild(List<IIpsSrcFile> addedOrChangedIpsSrcFiles,
            List<IIpsSrcFile> removedIpsSrcFiles) {
        if (canCollectDependencies()) {
            collectDependenciesFor(addedOrChangedIpsSrcFiles);
            collectDependenciesFor(removedIpsSrcFiles);
        }
        return dependenciesForProjectMap;
    }

    private boolean canCollectDependencies() {
        return graph != null && ipsProject.canBeBuild();
    }

    private void collectDependenciesFor(List<IIpsSrcFile> addedOrChangedIpsSrcFiles) {
        for (IIpsSrcFile ipsSrcFile : addedOrChangedIpsSrcFiles) {
            collectDependencies(ipsSrcFile.getQualifiedNameType(), new HashSet<IIpsProject>(), false);
        }
    }

    /**
     * This method collects all dependencies for a single object identified by its
     * {@link QualifiedNameType}.
     * 
     * @param root The object for which the method should return every dependencies, that means
     *            dependencies that have this root as target.
     * @param visitedProjects Projects that were already visited to avoid cycles during lookup
     * @param searchInstanceOfDependencyOnly <code>true</code> to only consider instance-of
     *            dependencies. This switch is necessary to toggle between a mode where all
     *            transitive dependencies are resolved, and a mode where only transitive instance-of
     *            dependencies are resolved.
     */
    void collectDependencies(QualifiedNameType root,
            Set<IIpsProject> visitedProjects,
            boolean searchInstanceOfDependencyOnly) {
        if (canCollectDependencies()) {
            collectEnumContentDependencies(root, visitedProjects, searchInstanceOfDependencyOnly);
            collectDependencies(root, searchInstanceOfDependencyOnly);
            collectDependenciesOfDependantProjects(root, visitedProjects, searchInstanceOfDependencyOnly);
        }
    }

    private void collectEnumContentDependencies(QualifiedNameType root,
            Set<IIpsProject> visitedProjects,
            boolean searchInstanceOfDependencyOnly) {
        if (IpsObjectType.ENUM_CONTENT.equals(root.getIpsObjectType())) {
            try {
                IEnumContent enumContent = (IEnumContent)ipsProject.findIpsObject(root);
                if (enumContent != null) {
                    IIpsProject project = getEnumTypeProject(enumContent);
                    if (project != null) {
                        DependencyResolver dependencyResolver = new DependencyResolver(project);
                        QualifiedNameType enumType = new QualifiedNameType(enumContent.getEnumType(),
                                IpsObjectType.ENUM_TYPE);
                        dependencyResolver.collectDependencies(enumType, visitedProjects,
                                searchInstanceOfDependencyOnly);
                        dependenciesForProjectMap.merge(dependencyResolver.getCollectedDependencies());
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private IIpsProject getEnumTypeProject(IEnumContent enumContent) {
        IEnumType enumType = enumContent.findEnumType(ipsProject);
        if (enumType != null) {
            IIpsProject project = enumType.getIpsProject();
            return project;
        }
        return null;
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
        boolean allDependencies = !searchInstanceOfOnly;
        return allDependencies || DependencyType.INSTANCEOF.equals(dependency.getType())
                || DependencyType.SUBTYPE.equals(dependency.getType());
    }

    private void considerTransitiveDependencies(IDependency dependency, boolean searchInstanceOfDependencyOnly) {
        if (dependency.getType().equals(DependencyType.SUBTYPE)) {
            collectTransitivDependencies(dependency, searchInstanceOfDependencyOnly);
        }
        if (!searchInstanceOfDependencyOnly) {
            if (dependency.getType().equals(DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL)
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
