/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainerType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for IPS object path container implementations.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsObjectPathContainer implements IIpsObjectPathContainer {

    private IIpsObjectPathContainerType containerType;
    private IIpsProject ipsProject;
    private String optionalPath;

    public AbstractIpsObjectPathContainer(IIpsObjectPathContainerType containerType, IIpsProject ipsProject,
            String optionalPath) {

        ArgumentCheck.notNull(containerType);
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(optionalPath);
        this.containerType = containerType;
        this.ipsProject = ipsProject;
        this.optionalPath = optionalPath;
    }

    @Override
    public IIpsObjectPathContainerType getContainerType() {
        return containerType;
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public String getOptionalPath() {
        return optionalPath;
    }

    @Override
    public String getName() {
        return containerType.getId();
    }

    /**
     * Returns the ips project's object path.
     */
    public IpsObjectPath getIpsObjectPath() {
        IpsModel model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
        return (IpsObjectPath)model.getIpsProjectProperties((IpsProject)getIpsProject()).getIpsObjectPath();
    }
}
