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

package org.faktorips.devtools.core.ui.controls.parametertable;

public interface ParameterListChangeListener {

    /**
     * Gets fired when the given parameter has changed
     * 
     * @param parameter the parameter that has changed.
     */
    public void parameterChanged(ParameterInfo parameter);

    /**
     * Gets fired when the given parameter has been added
     * 
     * @param parameter the parameter that has been added.
     */
    public void parameterAdded(ParameterInfo parameter);

    /**
     * Gets fired if the parameter list got modified by reordering or removing parameters (note that
     * adding is handled by <code>parameterAdded</code>))
     */
    public void parameterListChanged();
}
