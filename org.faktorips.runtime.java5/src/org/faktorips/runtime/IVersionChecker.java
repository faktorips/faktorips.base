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

package org.faktorips.runtime;

/**
 * The modification checker holds a version or time stamp for the version or time it is up to date.
 * You could ask the modification checker whether a given version or time stamp is expired and you
 * could get the actual version.
 * 
 * @author dirmeier
 */
public interface IVersionChecker {

    /**
     * Returns true if the new version is compatible to the old version or both versions are equal
     * 
     * @param oldVersion the old version
     * @param newVersion the new version
     * @return true if versions are compatible
     */
    public boolean isCompatibleVersion(String oldVersion, String newVersion);

}
