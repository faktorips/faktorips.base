/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A dialog to edit the product component type (template) of a product compnent.
 */
public class SetProductCmptTypeDialog extends EditDialog {

    private IProductCmpt productCmpt;
    private ProductCmptType2RefControl template;
    private String message;

    /**
     * Creates a new dialog to edit a product cmpt generation
     * 
     * @param parentShell The shell to be used as parent for the dialog
     * @param message The message to be displayed to the user if no error message is set.
     */
    public SetProductCmptTypeDialog(IProductCmpt cmpt, Shell parentShell, String message) {
        super(parentShell, Messages.SetTemplateDialog_titleNewTemplate, false);
        this.message = message;
        productCmpt = cmpt;
    }

    @Override
    protected Composite createWorkArea(Composite parent) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(workArea, Messages.SetTemplateDialog_labelNewTemplate);
        template = new ProductCmptType2RefControl(productCmpt.getIpsProject(), workArea, getToolkit(), true);
        template.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        template.getTextControl().addModifyListener($ -> {
            if (IpsStringUtils.isEmpty(template.getText())) {
                getButton(OK).setEnabled(false);
                String msg = NLS.bind(Messages.SetTemplateDialog_msgTemplateDoesNotExist, template.getText());
                setMessage(msg, IMessageProvider.ERROR);
            } else {
                getButton(OK).setEnabled(true);
                setMessage(message);
            }
        });
        super.setMessage(message);

        return workArea;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        super.getButton(OK).setEnabled(false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == OK) {
            productCmpt.setProductCmptType(template.getText());
        }
        super.buttonPressed(buttonId);
    }

}
