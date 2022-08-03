/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * The dependency graph is designed to access the dependencies of {@link IIpsObject IPS objects} in
 * reverse direction. Every {@link IIpsObject} is able to resolve all direct dependencies by calling
 * the method {@link IIpsObject#dependsOn()}. The dependency graph looks for all dependencies and is
 * able to resolve the reverse question: which dependencies are targeted to a specific object.
 * <p>
 * The dependency graph is always responsible for the dependencies of exactly one project. More
 * precisely it is responsible for all dependencies which source object is in the responsible
 * project. Hence to get all dependencies for a specific object it does not satisfy to check the
 * dependency graph of the object's project. It is necessary to check also every project that
 * depends on this project.
 * 
 */
public interface IDependencyGraph {

    IIpsProject getIpsProject();

    /**
     * Returns the qualified names of the IPS objects that depend on the object identified by the
     * given qualified name.
     * 
     * @param id the identifier for an IPS object or data type for which the dependent objects
     *            should be returned. Identifier for IpsObjects are QualifiedNameType instances for
     *            data types qualified name strings.
     */
    IDependency[] getDependants(QualifiedNameType id);

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
    void update(QualifiedNameType qName);

    void reInit();

}
