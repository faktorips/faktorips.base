/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.ArgumentCheck;

/**
 * A container/cache for IPS project related data. An instance of this cache is kept per IPS project
 * by the IPS model.
 *
 * @author Jan Ortmann
 */
public class IpsProjectData {

    private final IIpsProject ipsProject;

    private final IpsObjectPathContainerFactory containerFactory;

    private final Map<ContainerTypeAndPath, IIpsObjectPathContainer> containers = new ConcurrentHashMap<>();

    private IIpsArtefactBuilderSet ipsArtefactBuilderSet;

    private IDependencyGraph dependencyGraph;

    private IClassLoaderProvider classLoaderProvider;

    private ExtensionFunctionResolversCache functionResolver;

    private volatile LinkedHashSet<IIpsSrcFile> markerEnums;

    private IpsProjectProperties projectProperties;

    /**
     * a map containing a set of datatypes.
     */
    private final LinkedHashMap<String, Datatype> projectDatatypesMap = new LinkedHashMap<>();

    private IVersionProvider<?> versionFormat;

    public IpsProjectData(IIpsProject ipsProject, IpsObjectPathContainerFactory containerFactory) {
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(containerFactory);
        this.ipsProject = ipsProject;
        this.containerFactory = containerFactory;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    /**
     * Returns the container identified by the given container kind or <code>null</code> if no such
     * container is found.
     *
     * @throws NullPointerException if containerTypeId or optionalPath is <code>null</code>.
     */
    public IIpsObjectPathContainer getIpsObjectPathContainer(String containerTypeId, String optionalPath) {
        ArgumentCheck.notNull(containerTypeId);
        ArgumentCheck.notNull(optionalPath);
        ContainerTypeAndPath typeAndPath = new ContainerTypeAndPath(containerTypeId, optionalPath);
        return containers.computeIfAbsent(typeAndPath,
                $ -> containerFactory.newContainer(ipsProject, containerTypeId, optionalPath));
    }

    @Override
    public String toString() {
        return "IpsProjectData [project=" + ipsProject.getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public IIpsArtefactBuilderSet getIpsArtefactBuilderSet() {
        return ipsArtefactBuilderSet;
    }

    public void setIpsArtefactBuilderSet(IIpsArtefactBuilderSet ipsArtefactBuilderSet) {
        this.ipsArtefactBuilderSet = ipsArtefactBuilderSet;
    }

    public IDependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    public void setDependencyGraph(IDependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    public IClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public void setClassLoaderProvider(IClassLoaderProvider classLoaderProvider) {
        this.classLoaderProvider = classLoaderProvider;
    }

    public ExtensionFunctionResolversCache getFunctionResolver() {
        return functionResolver;
    }

    public void setFunctionResolver(ExtensionFunctionResolversCache functionResolver) {
        this.functionResolver = functionResolver;
    }

    public IpsProjectProperties getProjectProperties() {
        return projectProperties;
    }

    public void setProjectProperties(IpsProjectProperties projectProperties) {
        this.projectProperties = projectProperties;
    }

    public LinkedHashMap<String, Datatype> getProjectDatatypesMap() {
        return projectDatatypesMap;
    }

    public IVersionProvider<?> getVersionProvider() {
        return versionFormat;
    }

    public void setVersionProvider(IVersionProvider<?> versionProvider) {
        versionFormat = versionProvider;
    }

    public LinkedHashSet<IIpsSrcFile> getMarkerEnums() {
        LinkedHashSet<IIpsSrcFile> result = markerEnums;
        if (result != null) {
            return result;
        }
        synchronized (this) {
            if (markerEnums == null) {
                markerEnums = initMarkerEnums();
            }
            return markerEnums;
        }
    }

    private LinkedHashSet<IIpsSrcFile> initMarkerEnums() {
        LinkedHashSet<IIpsSrcFile> result = new LinkedHashSet<>();
        IIpsProjectProperties properties = ipsProject.getReadOnlyProperties();
        if (properties.isMarkerEnumsEnabled()) {
            LinkedHashSet<String> markerEnumsQNames = properties.getMarkerEnums();
            for (String qualifiedName : markerEnumsQNames) {
                IIpsSrcFile ipsSrcFile = ipsProject.findIpsSrcFile(IpsObjectType.ENUM_TYPE, qualifiedName);
                if (ipsSrcFile != null && ipsSrcFile.exists()) {
                    result.add(ipsSrcFile);
                }
            }
        }
        return result;
    }

    static class ContainerTypeAndPath {
        private String type;
        private String path;

        ContainerTypeAndPath(String type, String path) {
            super();
            this.type = type;
            this.path = path;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, type);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            ContainerTypeAndPath other = (ContainerTypeAndPath)obj;
            return Objects.equals(path, other.path)
                    && Objects.equals(type, other.type);
        }

        @Override
        public String toString() {
            return "ContainerTypeAndPath [type=" + type + ", path=" + path + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

    }
}
