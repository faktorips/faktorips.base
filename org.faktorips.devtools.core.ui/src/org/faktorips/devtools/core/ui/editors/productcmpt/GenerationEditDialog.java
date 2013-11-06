/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.inputFormat.GregorianCalendarFormat;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A dialog to edit a product component generation.
 */
public class GenerationEditDialog extends IpsPartEditDialog2 {

    private static final String DIALOG_MSG_CODE = "VALID_FROM_MSG"; //$NON-NLS-1$

    private EditField<GregorianCalendar> dateField;

    private IProductCmptGeneration previous;
    private IProductCmptGeneration next;
    private boolean newGenerationDialog = false;

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
        this.previous = (IProductCmptGeneration)generation.getPreviousByValidDate();
        this.next = (IProductCmptGeneration)generation.getNextByValidDate();
        this.newGenerationDialog = newGenerationDialog;
    }

    @Override
    public IProductCmptGeneration getIpsPart() {
        return (IProductCmptGeneration)super.getIpsPart();
    }

    @Override
    protected String buildTitle() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getIpsPart()) + ' ' + getIpsPart().getName();
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        Composite workArea = getToolkit().createLabelEditColumnComposite(parent);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createLabel(workArea, Messages.GenerationEditDialog_labelValidFrom);
        DateControl dateControl = new DateControl(workArea, getToolkit());
        Text textControl = dateControl.getTextControl();

        dateField = new FormattingTextField<GregorianCalendar>(textControl, GregorianCalendarFormat.newInstance());

        bindModel();
        return workArea;
    }

    private void bindModel() {
        getBindingContext().bindContent(dateField, getIpsPart(), IProductCmptGeneration.PROPERTY_VALID_FROM);
    }

    @Override
    protected void addAdditionalDialogMessages(MessageList messageList) {
        // We need to validate here and "by hand" because this validation is not necessary
        // to be done during normal validation of a generation.
        GregorianCalendar value = getIpsPart().getValidFrom();

        if (value == null) {
            DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
            String formatDescription = format.format(new GregorianCalendar().getTime());
            if (format instanceof SimpleDateFormat) {
                formatDescription = ((SimpleDateFormat)format).toPattern();
            }
            messageList.add(Message.newError(DIALOG_MSG_CODE, Messages.GenerationEditDialog_msgInvalidFormat
                    + formatDescription, getIpsPart(), IProductCmptGeneration.PROPERTY_VALID_FROM));

        } else if (previous != null && !value.after(previous.getValidFrom()) && !newGenerationDialog) {
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
