/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    void updateModel();

    /**
     * Updates the part of the user interface the controler is responsible for with the data from
     * the model.
     */
    void updateUI();
}
