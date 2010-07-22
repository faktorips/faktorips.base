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

package org.faktorips.devtools.core.ui.binding;

import java.beans.PropertyChangeListener;

public interface IPropertyChangeListenerSupport {

    /**
     * Adds the property change listener. If the same listener has already been added, the method
     * does nothing.
     * 
     * @throws IllegalArgumentException if listener is <code>null</code>.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes the property change listener. If the listener hasn't been added before, the method
     * does nothing.
     * 
     * @throws IllegalArgumentException if listener is <code>null</code>.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
