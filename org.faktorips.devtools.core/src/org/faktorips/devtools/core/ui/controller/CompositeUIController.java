/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
    
    private List controllers = new ArrayList();

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
     * Overridden.
     */
    public void updateModel() {
        for (Iterator it=controllers.iterator(); it.hasNext(); ) {
            UIController c = (UIController)it.next();
            c.updateModel();
        }
    }

    /** 
     * Overridden.
     */
    public void updateUI() {
        for (Iterator it=controllers.iterator(); it.hasNext(); ) {
            UIController c = (UIController)it.next();
            c.updateUI();
        }
    }

}
