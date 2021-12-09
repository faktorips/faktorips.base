/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.dependency;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * This interface describes details of a dependency (part and property) causing the dependency.
 * Instances are created by the {@link IIpsObject#getDependencyDetails(IDependency)}
 * <p>
 * Dependency details are not part of {@link IDependency} because the {@link IDependency} is used by
 * the {@link IDependencyGraph} and is serialized. The details are not needed by the dependency
 * graph because the builder using the graph only needs to know which files are affected and not,
 * which detail is causing this dependency. To keep the graph as small as possible the details are
 * not part of the dependency itself.
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
     * </li>
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
     * Called by the refactoring framework when the target of this dependency has changed. The
     * implementation has to refactor the referenced property content to match the new name. The
     * rename may only affect the name of the object or the path (packageFragment) of the object,
     * also called move refactoring.
     * 
     * @param targetIpsPackageFragment The new package fragment of the refactored object.
     * @param newName The new name of the refactored object.
     * 
     * @throws CoreRuntimeException In case of any exception while setting the new name, a
     *             {@link CoreException} needs to be thrown. In case of throwing any
     *             {@link RuntimeException} the whole refactoring may break
     */
    public void refactorAfterRename(IIpsPackageFragment targetIpsPackageFragment, String newName) throws CoreRuntimeException;

}
