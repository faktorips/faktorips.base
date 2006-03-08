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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
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
public class DependencyGraph {

    private Map dependantsForMap;
    private Map dependsOnMap;
    private IIpsProject ipsProject;

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
    
    public void reInit() throws CoreException{
    	init();
    }
    
    private void init() throws CoreException {
        dependantsForMap = new HashMap();
        dependsOnMap = new HashMap();
        List allIpsObjects = new ArrayList();
        ipsProject.findAllIpsObjects(allIpsObjects);
        for (Iterator it = allIpsObjects.iterator(); it.hasNext();) {
            IIpsObject ipsObject = (IIpsObject)it.next();
            QualifiedNameType[] dependsOn = ipsObject.dependsOn();
            addEntriesToDependsOnMap(dependsOn, ipsObject.getQualifiedNameType());
            addEntryToDependantsForMap(dependsOn, ipsObject.getQualifiedNameType());
        }
    }

    private void addEntriesToDependsOnMap(QualifiedNameType[] dependsOn,
            QualifiedNameType requestedNameType) {
        dependsOnMap.put(requestedNameType, CollectionUtil.toArrayList(dependsOn));
    }

    private void addEntryToDependantsForMap(QualifiedNameType[] dependsOn, QualifiedNameType entry) {
        for (int i = 0; i < dependsOn.length; i++) {
            List dependants = getDependantsAsList(dependsOn[i]);
            if (dependants == null) {
                dependants = new ArrayList();
                dependantsForMap.put(dependsOn[i], dependants);
            }
            dependants.add(entry);
        }
    }

    /**
     * Returns the qualified names of the ips objects that depend on the object identified by the
     * given qualified name.
     * 
     * @param qName the fully qualified name type of the ips object for which the dependant objects
     *            should be returned.
     */
    public QualifiedNameType[] getDependants(QualifiedNameType qName) {
        List qualfiedNameTypes = getDependantsAsList(qName);

        if (qualfiedNameTypes == null) {
            return new QualifiedNameType[0];
        }
        return (QualifiedNameType[])qualfiedNameTypes
                .toArray(new QualifiedNameType[qualfiedNameTypes.size()]);
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
            QualifiedNameType[] newDependOnNameTypes = ipsObject.dependsOn();
            addEntriesToDependsOnMap(newDependOnNameTypes, qName);
            addEntryToDependantsForMap(newDependOnNameTypes, qName);
        }
    }

    private void removeDependency(QualifiedNameType qName) {
        List dependsOnList = (List)dependsOnMap.remove(qName);
        if (dependsOnList != null && !dependsOnList.isEmpty()) {
            for (Iterator it = dependsOnList.iterator(); it.hasNext();) {
                QualifiedNameType nameType = (QualifiedNameType)it.next();
                List dependants = getDependantsAsList(nameType);
                if (dependants != null) {
                    dependants.remove(qName);
                }
            }
        }
    }
    
}
