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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enumcontent.IEnumContent;
import org.faktorips.devtools.core.ui.editors.EditDialog;

/**
 * This dialog is available trough the <code>EnumContentEditor</code> when the
 * <code>IEnumContent</code> to edit does not refer to a valid <code>IEnumType</code>. It lets the
 * user select another <code>IEnumType</code> to refer to.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class FixEnumTypeDialog extends EditDialog {

    /** The <code>IEnumContent</code> to set a new <code>IEnumType</code> for. */
    private IEnumContent enumContent;

    /** The message to show to the user. */
    private String message;

    /**
     * Creates a new <code>FixEnumTypeDialog</code> that enables the user to select a new enum type
     * for the given enum content.
     * 
     * @param enumContent The <code>IEnumContent</code> to set a new <code>IEnumType</code> for.
     * @param parentShell The parent ui shell.
     */
    public FixEnumTypeDialog(IEnumContent enumContent, Shell parentShell) {
        super(parentShell, Messages.FixEnumTypeDialog_title);

        this.enumContent = enumContent;

        message = "";
        String enumTypeQualifiedName = enumContent.getEnumType();
        if (enumTypeQualifiedName.equals("")) {
            message = Messages.FixEnumTypeDialog_msgEnumTypeMissing;
        } else {
            try {
                if (enumContent.findEnumType() == null) {
                    message = NLS.bind(Messages.FixEnumTypeDialog_msgEnumTypeDoesNotExist, enumTypeQualifiedName);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }
        
        // TODO set title image
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(workArea, Messages.FixEnumTypeDialog_labelNewEnumType);
        uiToolkit.createEnumTypeRefControl(enumContent.getIpsProject(), workArea, false);

        setMessage(message);

        return workArea;
    }

}
