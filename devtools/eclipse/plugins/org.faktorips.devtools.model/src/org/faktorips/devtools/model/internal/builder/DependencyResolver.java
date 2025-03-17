/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.MultiMap;

/**
 * Resolves the dependencies for any object using the {@link IDependencyGraph}. Even though the
 * scope of each {@link IDependencyGraph} instance is a single project, the dependency resolver
 * creates {@link DependencyResolver} instances for dependent projects where necessary and searches
 * them recursively.
 * <p>
 * The method {@link #getCollectedDependencies()} provides all dependencies in a map. That map
 * contains projects as keys and sets of the corresponding dependencies as values. Thus dependencies
 * a categorized by the project they come from.
 * <p>
 * Some dependencies are resolved transitive. For example if the datatype of an product component
 * type attribute was changed, all instances of this product component type needs to be validated.
 * To find all these instances it is necessary to check all subtypes etc. Which transitive
 * dependencies are considered and which are not is configured by the {@link DependencyType}.
 * 
 */
public class DependencyResolver {

    private final IIpsProject referenceProject;

    private final IIpsProject ipsProject;

    private final IDependencyGraph graph;

    private final MultiMap<IIpsProject, IDependency> dependenciesForProjectMap;

    /**
     * Creates a new {@link DependencyResolver} for the specified project. If there are other
     * projects that depends on this project and hence needs to be searched for dependencies, this
     * dependency resolver creates other instances for the other projects.
     * 
     * @param ipsProject The {@link IIpsProject} for which the dependencies should be collected.
     */
    public DependencyResolver(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        referenceProject = ipsProject;
        graph = getDependencyGraph();
        dependenciesForProjectMap = MultiMap.createWithSetsAsValues();
    }

    /**
     * Creates a new dependency resolver and uses the previous resolver to set up the
     * {@link #referenceProject} and the already found {@link #dependenciesForProjectMap}
     * 
     */
    private DependencyResolver(IIpsProject ipsProject, DependencyResolver previousResolver) {
        this.ipsProject = ipsProject;
        graph = getDependencyGraph();
        referenceProject = previousResolver.referenceProject;
        dependenciesForProjectMap = previousResolver.dependenciesForProjectMap;
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
     * @return A {@link MultiMap} containing all found dependencies. The key is the project where
     *             the dependency comes from.
     */
    public MultiMap<IIpsProject, IDependency> collectDependenciesForIncrementalBuild(
            List<IIpsSrcFile> addedOrChangedIpsSrcFiles,
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

    private void collectDependenciesFor(List<IIpsSrcFile> ipsSrcFiles) {
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            collectDependencies(ipsSrcFile.getQualifiedNameType(), new HashSet<>(),
                    EnumSet.allOf(DependencyType.class));
        }
    }

    /**
     * This method collects all dependencies for a single object identified by its
     * {@link QualifiedNameType}.
     * 
     * @param root The object for which the method should return every dependencies, that means
     *            dependencies that have this root as target.
     * @param visitedProjects Projects that were already visited to avoid cycles during lookup
     * @param transitiveTypes <code>true</code> to only consider instance-of dependencies. This
     *            switch is necessary to toggle between a mode where all transitive dependencies are
     *            resolved, and a mode where only transitive instance-of dependencies are resolved.
     */
    void collectDependencies(QualifiedNameType root,
            Set<IIpsProject> visitedProjects,
            EnumSet<DependencyType> transitiveTypes) {
        if (canCollectDependencies()) {
            collectEnumContentDependencies(root, transitiveTypes);
            collectDependencies(root, transitiveTypes);
            collectDependenciesOfDependantProjects(root, visitedProjects, transitiveTypes);
        }
    }

    /**
     * Collects the dependencies for an {@link IEnumContent} by collecting the dependencies for the
     * related {@link IEnumType}. This is necessary because if the {@link IEnumContent} changes, all
     * objects using this {@link IEnumContent} over an extensible {@link IEnumType} have to be
     * rebuild, too. The problem is that the objects have no dependency to the {@link IEnumContent}
     * they are using. They only have a dependency to the {@link IEnumType}. Hence we need to find
     * all objects that has a dependency to the enum type.
     */
    private void collectEnumContentDependencies(QualifiedNameType root, EnumSet<DependencyType> transitiveTypes) {
        if (isEnumContent(root) && transitiveTypes.contains(DependencyType.DATATYPE)) {
            IEnumContent enumContent = findEnumContentIpsObject(root);
            if (enumContent != null) {
                IIpsProject enumTypeProject = getEnumTypeProject(enumContent);
                if (enumTypeProject != null) {
                    collectEnumTypeDependencies(enumContent, enumTypeProject, transitiveTypes);
                }
            }
        }
    }

    private boolean isEnumContent(QualifiedNameType root) {
        return IpsObjectType.ENUM_CONTENT.equals(root.getIpsObjectType());
    }

    private IEnumContent findEnumContentIpsObject(QualifiedNameType root) {
        return (IEnumContent)ipsProject.findIpsObject(root);
    }

    private IIpsProject getEnumTypeProject(IEnumContent enumContent) {
        IEnumType enumType = enumContent.findEnumType(ipsProject);
        if (enumType != null) {
            return enumType.getIpsProject();
        }
        return null;
    }

    private void collectEnumTypeDependencies(IEnumContent enumContent,
            IIpsProject enumTypeProject,
            EnumSet<DependencyType> transitiveTypes) {
        Set<IIpsProject> visitedProjectsForEnumContent = new HashSet<>();
        DependencyResolver enumTypedependencyResolver = new DependencyResolver(enumTypeProject, this);
        QualifiedNameType enumType = new QualifiedNameType(enumContent.getEnumType(), IpsObjectType.ENUM_TYPE);
        enumTypedependencyResolver.collectDependencies(enumType, visitedProjectsForEnumContent, transitiveTypes);
    }

    private void collectDependencies(QualifiedNameType root, EnumSet<DependencyType> transitiveTypes) {
        IDependency[] dependencies = graph.getDependants(root);
        for (IDependency dependency : dependencies) {
            if (!isAlreadyCollected(dependency)) {
                if (isProperDependency(dependency, transitiveTypes)) {
                    if (ipsProject.equals(referenceProject) || ipsProject.isReferencing(referenceProject)) {
                        dependenciesForProjectMap.put(ipsProject, dependency);
                    }
                    collectTransitivDependencies(dependency, transitiveTypes);
                }
            }
        }
    }

    private boolean isAlreadyCollected(IDependency dependency) {
        return dependenciesForProjectMap.get(ipsProject).contains(dependency);
    }

    private boolean isProperDependency(IDependency dependency, EnumSet<DependencyType> transitiveTypes) {
        return transitiveTypes.contains(dependency.getType());
    }

    private void collectTransitivDependencies(IDependency dependency, EnumSet<DependencyType> transitiveTypes) {
        collectDependencies(dependency.getSource(), new HashSet<>(), dependency.getType()
                .getNextTransitiveTypes(transitiveTypes, ipsProject));
    }

    private void collectDependenciesOfDependantProjects(QualifiedNameType root,
            Set<IIpsProject> visitedProjects,
            EnumSet<DependencyType> transitiveTypes) {
        visitedProjects.add(ipsProject);
        IIpsProject[] dependantProjects = ipsProject.findReferencingProjects(false);
        for (IIpsProject dependantProject : dependantProjects) {
            if (!visitedProjects.contains(dependantProject)
                    && (dependantProject.equals(referenceProject) || referenceProject.isReferencing(dependantProject)
                            || dependantProject
                                    .isReferencing(referenceProject))) {
                DependencyResolver dependencyResolver = new DependencyResolver(dependantProject, this);
                dependencyResolver.collectDependencies(root, visitedProjects, transitiveTypes);
            }
        }
    }

    /**
     * Returns the resulting map with the projects as keys and for every project a set with resolved
     * dependencies.
     */
    public MultiMap<IIpsProject, IDependency> getCollectedDependencies() {
        return dependenciesForProjectMap;
    }

}
