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

package org.faktorips.values;

/**
 * This interface marks a class as supporting the null object pattern. Instances of this class are
 * either a "normal" object or the null object.
 * 
 * @see org.faktorips.values.NullObject
 * 
 * @author Jan Ortmann
 */
public interface NullObjectSupport {

    /**
     * Returns <code>true</code> if this is the object representing <code>null</code>, otherwise
     * <code>false</code>.
     */
    public boolean isNull();

    /**
     * Returns <code>false</code> if this is the object representing <code>null</code>, otherwise
     * <code>true</code>.
     */
    public boolean isNotNull();

}
