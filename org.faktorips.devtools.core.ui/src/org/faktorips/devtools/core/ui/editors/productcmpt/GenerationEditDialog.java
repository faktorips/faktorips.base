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

import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.inputformat.GregorianCalendarFormat;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * A dialog to edit a product component generation.
 */
public class GenerationEditDialog extends IpsPartEditDialog2 {

    private static final String DIALOG_MSG_CODE = "VALID_FROM_MSG"; //$NON-NLS-1$

    private final GenerationEditDialogPMO pmo;
    private EditField<GregorianCalendar> dateField;

    private IProductCmptGeneration previous;
    private IProductCmptGeneration next;
    private boolean newGenerationDialog = false;

    private ExtensionPropertyControlFactory extFactory;

    /**
     * Creates a new dialog to edit and create a product cmpt generation
     * 
     * @param generation The generation to edit
     * @param parentShell The shell to be used as parent for the dialog
     * @param newGenerationDialog The flag indicates which the button in Editor was selected
     *            (New=true,Edit=false)
     * 
     */
    public GenerationEditDialog(IProductCmptGeneration generation, Shell parentShell, boolean newGenerationDialog) {
        super(generation, parentShell, Messages.GenerationEditDialog_titleChangeValidFromDate);

        // we have to store previous and next here, because the evaulation of
        // previous and next depend on the valid-from date which we will modify...
        previous = (IProductCmptGeneration)generation.getPreviousByValidDate();
        next = (IProductCmptGeneration)generation.getNextByValidDate();
        this.newGenerationDialog = newGenerationDialog;
        pmo = new GenerationEditDialogPMO(generation);
        extFactory = new ExtensionPropertyControlFactory(generation);
    }

    @Override
    public IProductCmptGeneration getIpsPart() {
        return (IProductCmptGeneration)super.getIpsPart();
    }

    @Override
    protected String buildTitle() {
        return IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getIpsPart()) + ' '
                + getIpsPart().getName();
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createLabel(workArea, Messages.GenerationEditDialog_labelValidFrom);
        DateControl dateControl = new DateControl(workArea, getToolkit());
        Text textControl = dateControl.getTextControl();
        dateField = new FormattingTextField<>(textControl, GregorianCalendarFormat.newInstance());

        createExtensionProperties(workArea);

        return workArea;
    }

    private void createExtensionProperties(Composite workArea) {
        if (!newGenerationDialog) {
            createExtensionProperties(workArea, IExtensionPropertyDefinition.POSITION_TOP);
            createExtensionProperties(workArea, IExtensionPropertyDefinition.POSITION_BOTTOM);
        }
    }

    private void createExtensionProperties(Composite workArea, String position) {
        extFactory.createControls(workArea, getToolkit(), getIpsPart(), position);
        extFactory.bind(getBindingContext());
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        bindModel();
    }

    private void bindModel() {
        getBindingContext().bindContent(dateField, pmo, IProductCmptGeneration.PROPERTY_VALID_FROM);
        getBindingContext().bindEnabled(getButton(IDialogConstants.OK_ID), pmo,
                GenerationEditDialogPMO.PROPERTY_OK_BUTTON_ENABLED);
    }

    @Override
    protected void addAdditionalDialogMessages(MessageList messageList) {
        // We need to validate here and "by hand" because this validation is not necessary
        // to be done during normal validation of a generation.
        GregorianCalendar value = getIpsPart().getValidFrom();

        if (value != null) {
            if (previous != null && !value.after(previous.getValidFrom()) && !newGenerationDialog) {
                String msg = NLS.bind(Messages.GenerationEditDialog_msgDateToEarly, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular());
                messageList.add(Message.newWarning(DIALOG_MSG_CODE, msg, getIpsPart(),
                        IProductCmptGeneration.PROPERTY_VALID_FROM));

            } else if (next != null && !next.getValidFrom().after(value) && !newGenerationDialog) {
                String msg = NLS.bind(Messages.GenerationEditDialog_msgDateToLate, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular());
                messageList.add(Message.newWarning(DIALOG_MSG_CODE, msg, getIpsPart(),
                        IProductCmptGeneration.PROPERTY_VALID_FROM));

            }
        }
    }

    /**
     * Pmo for OK-Button
     */
    public static class GenerationEditDialogPMO extends PresentationModelObject {

        public static final String PROPERTY_OK_BUTTON_ENABLED = "okButtonEnabled"; //$NON-NLS-1$

        private final IProductCmptGeneration generation;

        private GregorianCalendar cachedDate = null;

        public GenerationEditDialogPMO(IProductCmptGeneration currentGeneration) {
            generation = currentGeneration;
            cachedDate = generation.getValidFrom();
        }

        public boolean isOkButtonEnabled() {
            return cachedDate != null;
        }

        public void setValidFrom(GregorianCalendar calendar) {
            cachedDate = calendar;
            updateGenerationValidFrom();
            notifyListeners();
        }

        private void updateGenerationValidFrom() {
            if (cachedDate != null) {
                generation.setValidFrom(cachedDate);
            }
        }

        public GregorianCalendar getValidFrom() {
            return cachedDate;
        }
    }

}
