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
