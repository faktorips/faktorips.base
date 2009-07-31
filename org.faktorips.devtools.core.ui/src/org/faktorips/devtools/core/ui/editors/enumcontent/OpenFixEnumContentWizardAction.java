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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.util.ArgumentCheck;

/**
 * Opens a <code>FixEnumContentWizard</code>.
 * 
 * @see FixEnumContentWizard
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class OpenFixEnumContentWizardAction extends Action {

    /** The name of the image for the action. */
    private final String IMAGE_NAME = "BrokenEnum.gif";

    /** The enum content to fix. */
    private IEnumContent enumContent;

    /** The parent shell. */
    private Shell parentShell;

    /** The editor page that requested the operation or <tt>null</tt>. */
    private IpsObjectEditorPage editorPage;

    /**
     * Creates a new <code>OpenFixEnumContentWizardAction</code>.
     * 
     * @param editorPage The IpsObjectEditorPage that requested the operation or <tt>null</tt> (the
     *            page will be refreshed after the operation was performed if given).
     * @param enumContent The enum content to fix.
     * @param parentShell The parent shell.
     * 
     * @throws NullPointerException If <code>enumContent</code> or <code>parentShell</code> is
     *             <code>null</code>.
     */
    public OpenFixEnumContentWizardAction(IpsObjectEditorPage editorPage, IEnumContent enumContent, Shell parentShell) {
        super();

        ArgumentCheck.notNull(new Object[] { enumContent, parentShell });

        this.enumContent = enumContent;
        this.parentShell = parentShell;
        this.editorPage = editorPage;

        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE_NAME));
        setText(Messages.EnumContentPage_labelOpenFixEnumTypeDialog);
        setToolTipText(Messages.EnumContentPage_tooltipOpenFixEnumTypeDialog);
    }

    /**
     * {@inheritDoc}
     */
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
