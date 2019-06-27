/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for IPS object path container implementations.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsObjectPathContainer implements IIpsObjectPathContainer {

    private final String containerId;
    private final IIpsProject ipsProject;
    private final IPath optionalPath;

    public AbstractIpsObjectPathContainer(String containerId, String optionalPath, IIpsProject ipsProject) {
        ArgumentCheck.notNull(containerId);
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(optionalPath);
        this.containerId = containerId;
        this.ipsProject = ipsProject;
        this.optionalPath = new Path(optionalPath);
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public IPath getOptionalPath() {
        return optionalPath;
    }

    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    public String getName() {
        return getContainerId();
    }

    /**
     * Returns the ips project's object path.
     */
    public IpsObjectPath getIpsObjectPath() {
        IIpsObjectPath ipsObjectPath = getIpsProject().getIpsObjectPath();
        if (ipsObjectPath instanceof IpsObjectPath) {
            return (IpsObjectPath)ipsObjectPath;
        } else {
            return null;
        }
    }
}
