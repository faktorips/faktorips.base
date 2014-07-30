/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.builder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.IDependencyGraph;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.MultiMap;

/**
 * The dependency graph stores IPS object dependencies. It is supposed to be used in a way that it
 * represents the object dependencies after the last build has been completed.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public class DependencyGraph implements Serializable, IDependencyGraph {

    public static final boolean TRACE_DEPENDENCY_GRAPH_MANAGEMENT;

    private static final long serialVersionUID = 5692023485881401223L;

    static {
        TRACE_DEPENDENCY_GRAPH_MANAGEMENT = Boolean.valueOf(
                Platform.getDebugOption("org.faktorips.devtools.core/trace/dependencygraphmanagement")).booleanValue(); //$NON-NLS-1$
    }

    private final MultiMap<Object, IDependency> dependantsForMap = MultiMap.createWithSetsAsValues();

    private final MultiMap<QualifiedNameType, IDependency> dependsOnMap = MultiMap.createWithSetsAsValues();

    private transient IIpsProject ipsProject;

    /**
     * Creates a new DependencyGraph object.
     * 
     * @param ipsProject the IPS project this dependency graph administers the dependencies of the
     *            IPS objects for.
     */
    public DependencyGraph(IIpsProject ipsProject) {
        super();
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject, this);
        if (this.ipsProject == null) {
            this.ipsProject = ipsProject;
            return;
        }
        if (!this.ipsProject.equals(ipsProject)) {
            this.ipsProject = ipsProject;
            init();
        }
    }

    @Override
    public void reInit() {
        init();
    }

    private void init() {
        dependantsForMap.clear();
        dependsOnMap.clear();
        List<IIpsSrcFile> allSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.collectAllIpsSrcFilesOfSrcFolderEntries(allSrcFiles);
        for (IIpsSrcFile file : allSrcFiles) {
            if (!file.exists()) {
                continue;
            }
            IIpsObject ipsObject = file.getIpsObject();
            IDependency[] dependencies;
            try {
                dependencies = ipsObject.dependsOn();
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            if (dependencies == null || dependencies.length == 0) {
                continue;
            }
            addEntriesToDependsOnMap(ipsObject.getQualifiedNameType(), dependencies);
            addEntryToDependantsForMap(dependencies);
        }
    }

    private void addEntriesToDependsOnMap(QualifiedNameType requestedNameType, IDependency[] dependsOn) {
        dependsOnMap.putReplace(requestedNameType, Arrays.asList(dependsOn));
    }

    private void addEntryToDependantsForMap(IDependency[] dependsOn) {
        for (IDependency element : dependsOn) {
            dependantsForMap.put(element.getTarget(), element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDependency[] getDependants(QualifiedNameType id) {
        Collection<IDependency> qualfiedNameTypes = getDependantsAsList(id);
        if (isUsedAsDatatype(id)) {
            Collection<IDependency> additionalNameTypes = getDependantsAsList(id.getName());
            if (qualfiedNameTypes == null) {
                qualfiedNameTypes = additionalNameTypes;
            } else if (additionalNameTypes != null) {
                qualfiedNameTypes.addAll(additionalNameTypes);
            }
        }

        if (qualfiedNameTypes == null) {
            return new IDependency[0];
        }
        return qualfiedNameTypes.toArray(new IDependency[qualfiedNameTypes.size()]);
    }

    private boolean isUsedAsDatatype(QualifiedNameType id) {
        return id.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)
                || id.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE)
                || id.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE);
    }

    private Collection<IDependency> getDependantsAsList(Object target) {
        return new ArrayList<IDependency>(dependantsForMap.get(target));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(QualifiedNameType qName) {
        if (qName == null) {
            return;
        }
        try {
            removeDependency(qName);
            IIpsObject ipsObject = ipsProject.findIpsObject(qName);
            if (ipsObject != null) {
                IDependency[] newDependOnNameTypes = ipsObject.dependsOn();
                addEntriesToDependsOnMap(qName, newDependOnNameTypes);
                addEntryToDependantsForMap(newDependOnNameTypes);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void removeDependency(QualifiedNameType qName) {
        Collection<IDependency> dependsOnList = dependsOnMap.remove(qName);
        if (dependsOnList != null && !dependsOnList.isEmpty()) {

            for (IDependency dependency : dependsOnList) {
                Collection<IDependency> dependants = getDependantsAsList(dependency.getTarget());
                if (dependants != null) {
                    for (IDependency dependant : dependants) {
                        if (dependant.getSource().equals(qName)) {
                            dependantsForMap.remove(dependency.getTarget(), dependant);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "DependencyGraph for " + ipsProject.getName(); //$NON-NLS-1$
    }

    /**
     * Writes the object status to the {@link ObjectOutputStream}. For the {@link IIpsProject} we
     * simply write the name of the project.
     * 
     * @serialData The maps are serialized by default serialization. The {@link #ipsProject} is
     *             serialized by writing its name.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(ipsProject != null ? ipsProject.getName() : null);
    }

    /**
     * Reading the status of this object from the {@link ObjectInputStream}. The {@link IIpsProject}
     * is set by reading its name and resolving the project from the {@link IIpsModel}.
     * 
     * @serialData The maps are read by default deserialization. The {@link #ipsProject} is read by
     *             reading the name of the project and resolving the project via the
     *             {@link IIpsModel}.
     */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String projectName = (String)s.readObject();
        if (projectName != null) {
            ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
        }
    }

}
