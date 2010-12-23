/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.IIpsElement;

public interface PathUtil {

    /**
     * returns relative path to the root from the <code>IIpsElement</code>
     * 
     */
    public abstract String getPathToRoot();

    /**
     * returns relative path from the root to the <code>IIpsElement</code>
     * 
     * @param linkedFileType type of path
     * 
     */
    public abstract String getPathFromRoot(LinkedFileType linkedFileType);

    /**
     * name of an {@link IIpsElement} in a link
     * 
     * @param withImage true: link includes a small image which represents the type of the linked
     *            Object {@link IpsElement}
     */
    public abstract String getLinkText(boolean withImage);

}