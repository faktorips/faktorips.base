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

package org.faktorips.devtools.core.model.versionmanager;

import org.faktorips.devtools.core.IpsPlugin;

/**
 * This interface extends the {@link IIpsFeatureVersionManager} with the property contributor name.
 * This property is used to identify extended migration strategies registered in the different
 * plug-Ins.
 * <p>
 * If you register a version manager that implements this interface, the contributor name will be
 * set automatically by the {@link IpsPlugin}.
 * 
 * @author dirmeier
 */
public interface IExtendableVersionManager extends IIpsFeatureVersionManager {

    /**
     * Returns the name of the contributing plug-In in which this version manager is registered.
     */
    public String getContributorName();

    /**
     * Setting the name of the contributing plug-In in which this version manager is registered
     * 
     * @param contributorName The name of the contibutin plug-In
     */
    public void setContributorName(String contributorName);

}
