/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.wizards.enumcontent.FixEnumContentStrategy;
import org.faktorips.devtools.core.ui.wizards.fixcontent.FixContentWizard;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.util.ArgumentCheck;

/**
 * Opens a <code>FixEnumContentWizard</code>.
 * 
 * @see FixContentWizard
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class OpenFixEnumContentWizardAction extends Action {

    /** The name of the image for the action. */
    private static final String IMAGE_NAME = "BrokenEnum.gif"; //$NON-NLS-1$

    /** The <code>IEnumContent</code> to fix. */
    private IEnumContent enumContent;

    /** The parent shell. */
    private Shell parentShell;

    /** The editor page that requested the operation or <code>null</code>. */
    private IpsObjectEditorPage editorPage;

    /**
     * Creates a new <code>OpenFixEnumContentWizardAction</code>.
     * 
     * @param editorPage The <code>IpsObjectEditorPage</code> that requested the operation or
     *            <code>null</code> (the page will be refreshed after the operation was performed if
     *            given).
     * @param enumContent The <code>IEnumContent</code> to fix.
     * @param parentShell The parent shell.
     * 
     * @throws NullPointerException If <code>enumContent</code> or <code>parentShell</code> is
     *             <code>null</code> .
     */
    public OpenFixEnumContentWizardAction(IpsObjectEditorPage editorPage, IEnumContent enumContent, Shell parentShell) {
        super();
        ArgumentCheck.notNull(new Object[] { enumContent, parentShell });

        this.enumContent = enumContent;
        this.parentShell = parentShell;
        this.editorPage = editorPage;

        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumContentPage_labelOpenFixEnumTypeDialog);
        setToolTipText(Messages.EnumContentPage_tooltipOpenFixEnumTypeDialog);
    }

    @Override
    public void run() {
        FixContentWizard<IEnumType, IEnumAttribute> wizard = new FixContentWizard<>(
                enumContent, new FixEnumContentStrategy(enumContent));
        WizardDialog dialog = new WizardDialog(parentShell, wizard);
        dialog.open();
        if (editorPage != null) {
            editorPage.refresh();
        }
    }

}
