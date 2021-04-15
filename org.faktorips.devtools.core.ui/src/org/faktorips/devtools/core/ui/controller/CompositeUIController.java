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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.ui.binding.BindingContext;

/**
 * A controller that contains several other controllers and delegates each call to all of them.
 * 
 * @deprecated deprecated since 3.6, use {@link BindingContext} instead
 */
@Deprecated
public class CompositeUIController implements UIController {

    private List<UIController> controllers = new ArrayList<>();

    public CompositeUIController() {
        super();
    }

    public void add(UIController controller) {
        controllers.add(controller);
    }

    public void remove(UIController controller) {
        controllers.remove(controller);
    }

    @Override
    public void updateModel() {
        for (UIController c : controllers) {
            c.updateModel();
        }
    }

    @Override
    public void updateUI() {
        for (UIController c : controllers) {
            c.updateUI();
        }
    }

}
