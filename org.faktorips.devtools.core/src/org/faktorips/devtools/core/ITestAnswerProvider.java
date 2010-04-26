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

package org.faktorips.devtools.core;

/**
 * Interface for classes which provide values to be returned on simulated user interactions.
 * 
 * @author Thorsten Guenther
 */
public interface ITestAnswerProvider {

    /**
     * Returns the answer as boolean.
     */
    public boolean getBooleanAnswer();

    /**
     * Returns the answer as int.
     */
    public int getIntAnswer();

    /**
     * Returns the answer as String.
     */
    public String getStringAnswer();

    /**
     * Returns the answer as unspecified object.
     */
    public Object getAnswer();

}
