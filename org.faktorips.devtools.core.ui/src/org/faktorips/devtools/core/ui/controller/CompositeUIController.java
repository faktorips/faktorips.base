/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

    private List<UIController> controllers = new ArrayList<UIController>();

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
