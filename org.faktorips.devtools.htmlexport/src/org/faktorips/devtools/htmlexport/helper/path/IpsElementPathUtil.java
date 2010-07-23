/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Utility, which is used for links und relative links for {@link IIpsElement}s
 * 
 * @author dicker
 * 
 */
public interface IpsElementPathUtil {

    /**
     * returns relative path to the root from the <code>IIpsElement</code>
     * 
     * @return
     */
    public String getPathToRoot();

    /**
     * returns relative path from the root to the <code>IIpsElement</code>
     * 
     * @param linkedFileType type of path
     * 
     * @return
     */
    public String getPathFromRoot(LinkedFileType linkedFileType);

    /**
     * name of an {@link IIpsElement} in a link
     * 
     * @param withImage true: link includes a small image which represents the type of the
     *            {@link IpsElement}
     * @return
     */
    public String getLinkText(boolean withImage);

    /**
     * @return {@link IIpsElement}
     */
    public IIpsElement getIpsElement();

}
