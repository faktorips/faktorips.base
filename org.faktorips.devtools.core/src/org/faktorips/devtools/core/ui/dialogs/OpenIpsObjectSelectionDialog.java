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

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * 
 * @author Daniel Hohenberger
 */
public class OpenIpsObjectSelectionDialog  extends ElementListSelectionDialog {

    /**
     * @param parent
     * @param renderer
     */
    public OpenIpsObjectSelectionDialog(Shell parent, String dialogTitle, String dialogMessage) {
        super(parent, new DefaultLabelProvider());
        setTitle(dialogTitle);
        setMessage(dialogMessage);
        setIgnoreCase(true);
        setMatchEmptyString(true);
        setMultipleSelection(false);
    }
    
    public IIpsObject getSelectedObject() {
        if (getResult().length>0) {
            return (IIpsObject)getResult()[0];    
        }
        return null;
    }
}