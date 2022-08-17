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

import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;

/**
 * This interface describes that a source depends on a target. The dependency type describes the
 * kind of the dependency. Dependency instances are created by the dependsOn() methods of
 * {@link IIpsObject IIpsObjects} to indicate the dependency to other IpsObjects. The
 * DependencyGraph which is used by the IpsBuilder to determine the dependent IpsObjects during an
 * incremental build cycle utilizes the dependsOn() method to determine its state. It is up to the
 * IpsBuilder how to interpret this type.
 */
public interface IDependency {

    /**
     * Returns the type of this dependency.
     */
    DependencyType getType();

    /**
     * The source which depends on the target. Sources are described by their qualified name types
     * since a source must always be an IpsObject.
     */
    QualifiedNameType getSource();

    /**
     * The target from which the source depends on.
     */
    Object getTarget();

}
