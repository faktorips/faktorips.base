/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.editors.EditDialog;

/**
 * TODO AW 02-11-2011: JavaDoc
 * 
 * @author Alexander Weickmann
 */
public class ChangeCategoryDialog extends EditDialog {

    public ChangeCategoryDialog(Shell shell) {
        super(shell, Messages.ChangeCategoryDialog_windowTitle);
    }

    @Override
    protected Composite createWorkArea(Composite parent) {
        Composite editComposite = getToolkit().createLabelEditColumnComposite(parent);

        // TODO AW
        getToolkit().createLabel(editComposite, "blub");

        return editComposite;
    }

}
