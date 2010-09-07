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

package org.faktorips.devtools.core.model.ipsproject;

/**
 * A version format is able to validate a version string for specified syntax.
 * 
 * @author dirmeier
 */
public interface IVersionFormat {

    /**
     * Validates the version string for the syntax of this version format object
     * 
     * @param version the version string to validate, could be null
     * @return true if the version format is correct, false otherwise
     */
    public boolean isCorrectVersionFormat(String version);

    /**
     * Returns a user readable string representation of the version format.
     * 
     * @return a readable version of the format
     */
    public String getVersionFormat();

}
