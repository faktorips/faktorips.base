/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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
        try {
            IIpsObjectPath ipsObjectPath = getIpsProject().getIpsObjectPath();
            if (ipsObjectPath instanceof IpsObjectPath) {
                return (IpsObjectPath)ipsObjectPath;
            } else {
                return null;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
