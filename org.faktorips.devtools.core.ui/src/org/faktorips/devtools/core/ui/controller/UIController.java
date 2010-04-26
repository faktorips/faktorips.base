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

package org.faktorips.devtools.core.ui.controller;

/**
 * The UI controler is a mediator between the model and the user interface. It knows how to update
 * the model with the data from the UI and vice versa.
 */
public interface UIController {

    /**
     * Updates the model with the the data in the user interface this controler is responsible for.
     */
    public void updateModel();

    /**
     * Updates the part of the user interface the controler is responsible for with the data from
     * the model.
     */
    public void updateUI();
}
