/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.wizards.enumcontent.FixEnumContentWizard;
import org.faktorips.util.ArgumentCheck;

/**
 * Opens a <tt>FixEnumContentWizard</tt>.
 * 
 * @see FixEnumContentWizard
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class OpenFixEnumContentWizardAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "BrokenEnum.gif"; //$NON-NLS-1$

    /** The <tt>IEnumContent</tt> to fix. */
    private IEnumContent enumContent;

    /** The parent shell. */
    private Shell parentShell;

    /** The editor page that requested the operation or <tt>null</tt>. */
    private IpsObjectEditorPage editorPage;

    /**
     * Creates a new <tt>OpenFixEnumContentWizardAction</tt>.
     * 
     * @param editorPage The <tt>IpsObjectEditorPage</tt> that requested the operation or
     *            <tt>null</tt> (the page will be refreshed after the operation was performed if
     *            given).
     * @param enumContent The <tt>IEnumContent</tt> to fix.
     * @param parentShell The parent shell.
     * 
     * @throws NullPointerException If <tt>enumContent</tt> or <tt>parentShell</tt> is <tt>null</tt>
     *             .
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
        FixEnumContentWizard wizard = new FixEnumContentWizard(enumContent);
        WizardDialog dialog = new WizardDialog(parentShell, wizard);
        dialog.open();
        if (editorPage != null) {
            editorPage.refresh();
        }
    }

}
