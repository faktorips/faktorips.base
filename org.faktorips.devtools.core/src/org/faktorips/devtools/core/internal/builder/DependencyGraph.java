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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.builder.IDependencyGraph;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * The dependency graph stores IPS object dependencies. It is supposed to be used in a way that it
 * represents the object dependencies after the last build has been completed.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public class DependencyGraph implements Serializable, IDependencyGraph {

    private static final long serialVersionUID = 5692023485881401223L;

    public final static boolean TRACE_DEPENDENCY_GRAPH_MANAGEMENT;

    static {
        TRACE_DEPENDENCY_GRAPH_MANAGEMENT = Boolean.valueOf(
                Platform.getDebugOption("org.faktorips.devtools.core/trace/dependencygraphmanagement")).booleanValue(); //$NON-NLS-1$
    }

    private Map<Object, List<IDependency>> dependantsForMap;
    private Map<QualifiedNameType, List<IDependency>> dependsOnMap;
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
        dependantsForMap = new HashMap<Object, List<IDependency>>();
        dependsOnMap = new HashMap<QualifiedNameType, List<IDependency>>();
        List<IIpsSrcFile> allSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.collectAllIpsSrcFilesOfSrcFolderEntries(allSrcFiles);
        for (IIpsSrcFile file : allSrcFiles) {
            if (!file.exists()) {
                continue;
            }
            IIpsObject ipsObject = file.getIpsObject();
            IDependency[] dependsOn;
            try {
                dependsOn = ipsObject.dependsOn();
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            if (dependsOn == null || dependsOn.length == 0) {
                continue;
            }
            addEntriesToDependsOnMap(dependsOn, ipsObject.getQualifiedNameType());
            addEntryToDependantsForMap(dependsOn);
        }
    }

    private void addEntriesToDependsOnMap(IDependency[] dependsOn, QualifiedNameType requestedNameType) {
        dependsOnMap.put(requestedNameType, CollectionUtil.toArrayList(dependsOn));
    }

    private void addEntryToDependantsForMap(IDependency[] dependsOn) {
        for (IDependency element : dependsOn) {
            List<IDependency> dependants = getDependantsAsList(element.getTarget());
            if (dependants == null) {
                dependants = new ArrayList<IDependency>(0);
                dependantsForMap.put(element.getTarget(), dependants);
            }
            dependants.add(element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDependency[] getDependants(QualifiedNameType id) {
        List<IDependency> qualfiedNameTypes = getDependantsAsList(id);
        if (id.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)
                || id.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT_TYPE)
                || id.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
            List<IDependency> additionalNameTypes = getDependantsAsList(id.getName());
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

    private List<IDependency> getDependantsAsList(Object target) {
        return dependantsForMap.get(target);
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
                addEntriesToDependsOnMap(newDependOnNameTypes, qName);
                addEntryToDependantsForMap(newDependOnNameTypes);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void removeDependency(QualifiedNameType qName) {
        List<IDependency> dependsOnList = dependsOnMap.remove(qName);
        if (dependsOnList != null && !dependsOnList.isEmpty()) {

            for (IDependency dependency : dependsOnList) {
                List<IDependency> dependants = getDependantsAsList(dependency.getTarget());
                if (dependants != null) {
                    for (Iterator<IDependency> itDependants = dependants.iterator(); itDependants.hasNext();) {
                        IDependency dependency2 = itDependants.next();
                        if (dependency2.getSource().equals(qName)) {
                            itDependants.remove();
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

}
