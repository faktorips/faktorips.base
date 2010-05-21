/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
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
public class DependencyGraph implements Serializable {

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
    public DependencyGraph(IIpsProject ipsProject) throws CoreException {
        super();
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
        init();
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) throws CoreException {
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

    public void reInit() throws CoreException {
        init();
    }

    private void init() throws CoreException {
        dependantsForMap = new HashMap<Object, List<IDependency>>();
        dependsOnMap = new HashMap<QualifiedNameType, List<IDependency>>();
        List<IIpsSrcFile> allSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.collectAllIpsSrcFilesOfSrcFolderEntries(allSrcFiles);
        for (IIpsSrcFile file : allSrcFiles) {
            if (!file.exists()) {
                continue;
            }
            IIpsObject ipsObject = file.getIpsObject();
            IDependency[] dependsOn = ipsObject.dependsOn();
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
     * Returns the qualified names of the IPS objects that depend on the object identified by the
     * given qualified name.
     * 
     * @param id the identifier for an IPS object or data type for which the dependent objects
     *            should be returned. Identifier for IpsObjects are QualifiedNameType instances for
     *            data types qualified name strings.
     */
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
     * Updates the graph with the new dependency information for the given object. For an updated
     * IPS object the method works as follows:
     * <ol>
     * <li>Delete all relations for the node identified by the given qName.</li>
     * <li>Get the IPS object identified by the qName.</li>
     * <li>Determine the new dependencies by calling the the dependsOn() method on the IPS object
     * identified by qName.</li>
     * <li>Create new relations between the node identified by the qName and the nodes identified by
     * the dependencies. If one of the nodes doesn't exist it will be created.</li>
     * </ol>
     * For a new IPS object step one is omitted. For deleted IPS object only step one is executed.
     * 
     * @param qName The fully qualified name type of the IPS object.
     */
    public void update(QualifiedNameType qName) throws CoreException {
        if (qName == null) {
            return;
        }
        removeDependency(qName);
        IIpsObject ipsObject = ipsProject.findIpsObject(qName);
        if (ipsObject != null) {
            IDependency[] newDependOnNameTypes = ipsObject.dependsOn();
            addEntriesToDependsOnMap(newDependOnNameTypes, qName);
            addEntryToDependantsForMap(newDependOnNameTypes);
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
