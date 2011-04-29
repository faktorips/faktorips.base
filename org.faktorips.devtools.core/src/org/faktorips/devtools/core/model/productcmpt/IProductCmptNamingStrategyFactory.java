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

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A factory to create naming strategies for product components.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptNamingStrategyFactory {

    /**
     * Returns the ID of the type of naming strategy. This method never returns <code>null</code>.
     */
    public String getExtensionId();

    /**
     * Creates a new naming strategy. This method never returns <code>null</code>.
     * 
     * @param ipsProject The project this strategy is for.
     * 
     * @throws NullPointerException if the ips project is <code>null</code>.
     */
    IProductCmptNamingStrategy newProductCmptNamingStrategy(IIpsProject ipsProject);

}
