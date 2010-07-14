/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
     * Check whether a given time stamp is expired or not. This method is called very often and have
     * to be as fast as possible!
     * 
     * @param version The version or time stamp to check against the modification checker
     * @return true if the time stamp is expired
     */
    public boolean checkLocalVersion(String version);

    public boolean checkBaseVersion(String version);

    public String getLocalVersion();

}
