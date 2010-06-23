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
 * The modification checker holds a time stamp for the time it is up to date. You could ask the
 * modification checker whether a given time stamp is expired and you could get the actual time
 * stamp.
 * 
 * @author dirmeier
 */
public interface IModificationChecker {

    /**
     * Check whether a given time stamp is expired or not. This method is called very often and have
     * to be as fast as possible!
     * 
     * @param timestamp The time stamp to check against the modification checker
     * @return true if the time stamp is expired
     */
    public boolean isExpired(long timestamp);

    /**
     * Get the actual time stamp of the modification checker
     * 
     * @return the actual time stamp maybe 0 if the timestamp is not set
     */
    public long getModificationStamp();

}
