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

package org.faktorips.devtools.htmlexport.helper.path;

/**
 * This interface represents a path location.
 * 
 * A {@link IHtmlPath} is used for setting relative links in the Html-Export
 * 
 * 
 * 
 * @author dicker
 */
public interface IHtmlPath {

    /**
     * returns relative path of this location to the root
     * 
     */
    public abstract String getPathToRoot();

    /**
     * returns relative path of this location from the root
     * 
     * @param linkedFileType type of path
     * 
     */
    public abstract String getPathFromRoot(LinkedFileType linkedFileType);
}