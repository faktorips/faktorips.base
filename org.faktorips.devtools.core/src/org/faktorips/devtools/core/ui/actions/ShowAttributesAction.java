package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.ui.views.attrtable.ShowAttributesActionDelegate;

public class ShowAttributesAction extends Action {

    public ShowAttributesAction() {
        super();
        this.setDescription(Messages.ShowAttributesAction_description);
        this.setText(Messages.ShowAttributesAction_name);
        this.setToolTipText(this.getDescription());
    }
    
    public void run() {
        new ShowAttributesActionDelegate().run(this);
    }
    
}
