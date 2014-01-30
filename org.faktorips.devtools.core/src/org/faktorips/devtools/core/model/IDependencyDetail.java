/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.DependencyGraph;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * This interface describes details of a dependency (part and property) causing the dependency.
 * Instances are created by the {@link IIpsObject#getDependencyDetails(IDependency)}
 * <p>
 * Dependency details are not part of {@link IDependency} because the {@link IDependency} is used by
 * the {@link DependencyGraph} and is serialized. The details are not needed by the dependency graph
 * because the builder using the graph only needs to know which files are affected and not, which
 * detail is causing this dependency. To keep the graph as small as possible the details are not
 * part of the dependency itself.
 * 
 * @author Thorsten Günther
 */
public interface IDependencyDetail {

    /**
     * Returns the part container that is responsible for the existence of this dependency.
     * Examples:
     * <ul>
     * <li>If a policy component type has a super type, the IPS object representing the policy
     * component type is responsible for the dependency (the policy component type depends on its
     * super type). So this method returns the IPS object representing the policy component type.
     * <li/>
     * <li>If a policy component type has an association and the target of the association is
     * another policy component type, the association is responsible for the dependency between the
     * policy component type and the other type. OIn this case this method returns the association.
     * </ul>
     * 
     * @return The part of the source causing the dependency
     */
    public IIpsObjectPartContainer getPart();

    /**
     * The property name of the part causing this dependency.
     * <p>
     * <strong>Caution:</strong> This method will return null if this object was de-serialized and
     * not gathered from the {@link IIpsObject#dependsOn()} method.
     * 
     * @return The name of the property causing this dependency
     */
    public String getPropertyName();

    /**
     * This method updates the value of the property this {@link IDependencyDetail} points to. It is
     * used by the refactoring framework to update the value after a renaming or moving an
     * {@link IIpsObject}.
     * 
     * 
     * @param targetIpsPackageFragment The new package framework of the refactored
     *            {@link IIpsObject}
     * @param newName The new name of the refactored {@link IIpsObject}
     */
    public void refactorValue(IIpsPackageFragment targetIpsPackageFragment, String newName) throws CoreException;

}
