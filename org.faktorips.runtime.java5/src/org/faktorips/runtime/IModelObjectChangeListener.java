/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.EventListener;

/**
 * A listener for model object changes.
 * <p><strong>
 * The listener support is experimental in this version.
 * The API might change without notice until it is finalized in a future version.
 * </strong>
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectChangeListener extends EventListener {

    /**
     * The method is called when a model object has changed.
     *  
     * <strong>Note that it is the listeners responsibility to implement a proper exception
     * handling. The model object will squeeze any exceptions and go on to notify other
     * listeners.
     * </strong>
     * 
     * @param event
     */
    public void modelObjectChanged(IModelObjectChangedEvent event);
    
}
