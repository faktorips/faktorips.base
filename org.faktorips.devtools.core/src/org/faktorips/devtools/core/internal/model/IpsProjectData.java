/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.builder.DependencyGraph;
import org.faktorips.devtools.core.internal.model.ipsproject.ClassLoaderProvider;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.IVersionProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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

    private final Map<ContainerTypeAndPath, IIpsObjectPathContainer> containers = new HashMap<ContainerTypeAndPath, IIpsObjectPathContainer>();

    private IIpsArtefactBuilderSet ipsArtefactBuilderSet;

    private DependencyGraph dependencyGraph;

    private ClassLoaderProvider classLoaderProvider;

    private ExtensionFunctionResolversCache functionResolver;

    private IpsProjectProperties projectProperties;

    private Set<IIpsSrcFile> markerEnums;

    /**
     * a map containing a set of datatypes.
     */
    private final LinkedHashMap<String, Datatype> projectDatatypesMap = new LinkedHashMap<String, Datatype>();

    /**
     * A map contain the datatypes as keys and the datatype helper as values.
     */
    private final Map<ValueDatatype, DatatypeHelper> projectDatatypeHelpersMap = new ConcurrentHashMap<ValueDatatype, DatatypeHelper>();

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
        IIpsObjectPathContainer container = containers.get(typeAndPath);
        if (container == null) {
            container = containerFactory.newContainer(ipsProject, containerTypeId, optionalPath);
            containers.put(typeAndPath, container);
        }
        return container;
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

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    public void setDependencyGraph(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    public ClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public void setClassLoaderProvider(ClassLoaderProvider classLoaderProvider) {
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

    public Map<ValueDatatype, DatatypeHelper> getProjectDatatypeHelpersMap() {
        return projectDatatypeHelpersMap;
    }

    public IVersionProvider<?> getVersionProvider() {
        return versionFormat;
    }

    public void setVersionProvider(IVersionProvider<?> versionProvider) {
        this.versionFormat = versionProvider;
    }

    public Set<IIpsSrcFile> getMarkerEnums() {
        return markerEnums;
    }

    public void setMarkerEnums() {
        markerEnums = ipsProject.getMarkerEnums();
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
            final int prime = 31;
            int result = 1;
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
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
            ContainerTypeAndPath other = (ContainerTypeAndPath)obj;
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "ContainerTypeAndPath [type=" + type + ", path=" + path + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

    }
}
