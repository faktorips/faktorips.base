/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder;

import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public interface IDependencyGraph {

    public abstract IIpsProject getIpsProject();

    /**
     * Returns the qualified names of the IPS objects that depend on the object identified by the
     * given qualified name.
     * 
     * @param id the identifier for an IPS object or data type for which the dependent objects
     *            should be returned. Identifier for IpsObjects are QualifiedNameType instances for
     *            data types qualified name strings.
     */
    public abstract IDependency[] getDependants(QualifiedNameType id);

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
    public abstract void update(QualifiedNameType qName);

    public void reInit();

}