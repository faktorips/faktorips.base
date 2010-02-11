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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A controller that contains severall other controllers and delegates each
 * call to all of them.
 */
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

    /** 
     * {@inheritDoc}
     */
    public void updateModel() {
        for (Iterator<UIController> it=controllers.iterator(); it.hasNext(); ) {
            UIController c = it.next();
            c.updateModel();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void updateUI() {
        for (Iterator<UIController> it=controllers.iterator(); it.hasNext(); ) {
            UIController c = it.next();
            c.updateUI();
        }
    }

}
