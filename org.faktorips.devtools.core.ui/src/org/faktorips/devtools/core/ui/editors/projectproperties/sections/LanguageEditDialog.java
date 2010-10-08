/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.editors.EditDialog;

public class LanguageEditDialog extends EditDialog {

    public LanguageEditDialog(Shell shell, String windowTitle) {
        super(shell, windowTitle);
    }

    @Override
    protected Composite createWorkArea(Composite parent) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        uiToolkit.createFormLabel(workArea, Messages.LanguageEditDialog_label);
        Text languageText = uiToolkit.createText(workArea);

        return workArea;
    }

}
