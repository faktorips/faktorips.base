/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
import org.faktorips.devtools.core.model.Dependency;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.util.CollectionUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * The dependency graph stores ips object dependencies. It is supposed to be used in a way that it
 * represents the object dependencies after the last build has been completed.
 * 
 * @author Jan Ortmann, Peter Erzberger
 */
public class DependencyGraph implements Serializable {
    
    private static final long serialVersionUID = 5692023485881401223L;

    public final static boolean TRACE_DEPENDENCY_GRAPH_MANAGEMENT;
    
    static {
        TRACE_DEPENDENCY_GRAPH_MANAGEMENT = Boolean.valueOf(Platform.getDebugOption("org.faktorips.devtools.core/trace/dependencygraphmanagement")).booleanValue(); //$NON-NLS-1$
    }

    private Map dependantsForMap;
    private Map dependsOnMap;
    private transient IIpsProject ipsProject;

    /**
     * Creates a new DependencyGraph object.
     * 
     * @param ipsProject the ips project this dependency graph administers the dependenies of the ips objects for
     */
    public DependencyGraph(IIpsProject ipsProject) throws CoreException {
        super();
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
        init();
    }

    public IIpsProject getIpsProject(){
    	return ipsProject;
    }

    public void setIpsProject(IIpsProject ipsProject) throws CoreException{
        ArgumentCheck.notNull(ipsProject, this);
        if(this.ipsProject == null){
            this.ipsProject = ipsProject;
            return;
        }
        if(!this.ipsProject.equals(ipsProject)){
            this.ipsProject = ipsProject;
            init();
        }
    }
    public void reInit() throws CoreException{
        init();
    }
    
    private void init() throws CoreException {
        dependantsForMap = new HashMap();
        dependsOnMap = new HashMap();
        List allIpsObjects = new ArrayList();
        ipsProject.findAllIpsObjectsOfSrcFolderEntries(allIpsObjects);
        if(TRACE_DEPENDENCY_GRAPH_MANAGEMENT){
            System.out.println("all: " + allIpsObjects); //$NON-NLS-1$
        }
        for (Iterator it = allIpsObjects.iterator(); it.hasNext();) {
            IIpsObject ipsObject = (IIpsObject)it.next();
            Dependency[] dependsOn = ipsObject.dependsOn();
            if(dependsOn == null || dependsOn.length == 0){
                continue;
            }
            addEntriesToDependsOnMap(dependsOn, ipsObject.getQualifiedNameType());
            addEntryToDependantsForMap(dependsOn);
        }
    }

    private void addEntriesToDependsOnMap(Dependency[] dependsOn,
            QualifiedNameType requestedNameType) {
        dependsOnMap.put(requestedNameType, CollectionUtil.toArrayList(dependsOn));
    }

    private void addEntryToDependantsForMap(Dependency[] dependsOn) {
        for (int i = 0; i < dependsOn.length; i++) {
            List dependants = getDependantsAsList(dependsOn[i].getTarget());
            if (dependants == null) {
                dependants = new ArrayList();
                dependantsForMap.put(dependsOn[i].getTarget(), dependants);
            }
            dependants.add(dependsOn[i]);
        }
    }

    /**
     * Returns the qualified names of the ips objects that depend on the object identified by the
     * given qualified name.
     * 
     * @param qName the fully qualified name type of the ips object for which the dependant objects
     *            should be returned.
     */
    public Dependency[] getDependants(QualifiedNameType qName) {
        List qualfiedNameTypes = getDependantsAsList(qName);

        if (qualfiedNameTypes == null) {
            return new Dependency[0];
        }
        return (Dependency[])qualfiedNameTypes
                .toArray(new Dependency[qualfiedNameTypes.size()]);
    }

    private List getDependantsAsList(QualifiedNameType nameType) {
        return (List)dependantsForMap.get(nameType);
    }

    /**
     * Updates the graph with the new dependeny information for the given object. For an updated ips
     * object the method works as follows:
     * <ol>
     * <li>Delete all relations for the node identified by the given qName.</li>
     * <li>Get the ips object identified by the qName.</li>
     * <li>Determine the new dependencies by calling the the dependsOn() method on the ips object
     * identified by qName.</li>
     * <li>Create new relations between the node identified by the qName and the nodes identified
     * by the dependencies. If one of the nodes doesn't exist it will be created.</li>
     * </ol>
     * For a new ips object step one is omitted. For deleted ips object only step one is executed.
     * 
     * 
     * @param qName The fully qualified name type of the ips object.
     * @throws CoreException
     */
    public void update(QualifiedNameType qName) throws CoreException {
        if (qName == null) {
            return;
        }
        removeDependency(qName);
        IIpsObject ipsObject = ipsProject.findIpsObject(qName);
        if (ipsObject != null) {
            Dependency[] newDependOnNameTypes = ipsObject.dependsOn();
            addEntriesToDependsOnMap(newDependOnNameTypes, qName);
            addEntryToDependantsForMap(newDependOnNameTypes);
        }
    }

    private void removeDependency(QualifiedNameType qName) {
        List dependsOnList = (List)dependsOnMap.remove(qName);
        if (dependsOnList != null && !dependsOnList.isEmpty()) {
            
            for (Iterator it = dependsOnList.iterator(); it.hasNext();) {
                Dependency dependency = (Dependency)it.next();
                List dependants = getDependantsAsList(dependency.getTarget());
                if (dependants != null) {
                    for (Iterator itDependants = dependants.iterator(); itDependants.hasNext();) {
                        Dependency dependency2 = (Dependency)itDependants.next();
                        if(dependency2.getSource().equals(qName)){
                            itDependants.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "DependencyGraph for " + ipsProject.getName(); //$NON-NLS-1$
    }
}
