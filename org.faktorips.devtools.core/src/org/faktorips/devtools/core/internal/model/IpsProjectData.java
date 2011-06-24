/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.HashMap;
import java.util.Map;

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

    private IIpsProject ipsProject;
    private IpsObjectPathContainerFactory containerFactory;
    private Map<ContainerTypeAndPath, IIpsObjectPathContainer> containers = new HashMap<ContainerTypeAndPath, IIpsObjectPathContainer>();

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
