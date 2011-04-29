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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;

/**
 * Action toggles the working mode from browse to edit.
 * 
 * @author Joerg Ortmann
 */
public class ToggleWorkingModeAction extends Action implements IPropertyChangeListener {

    public static String ID = "org.faktorips.devtools.actions.ToggleWorkingModeAction"; //$NON-NLS-1$

    private boolean propertyChangedByThisAction = false;

    public ToggleWorkingModeAction() {
        super("", Action.AS_CHECK_BOX); //$NON-NLS-1$
        IpsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
        update();
    }

    private void update() {
        setChecked(getIpsPreferences().isWorkingModeEdit());
    }

    private IpsPreferences getIpsPreferences() {
        return IpsPlugin.getDefault().getIpsPreferences();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!propertyChangedByThisAction && event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            update();
        }
    }

    public void dispose() {
        IpsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
    }

    @Override
    public void run() {
        runInternal();
    }

    private void runInternal() {
        propertyChangedByThisAction = true;
        getIpsPreferences().setWorkingMode(
                isChecked() ? IpsPreferences.WORKING_MODE_EDIT : IpsPreferences.WORKING_MODE_BROWSE);
        propertyChangedByThisAction = false;
    }

    public void setEditor(Object editor) {
        update();
        setEnabled(editor instanceof IpsObjectEditor);
    }

}
