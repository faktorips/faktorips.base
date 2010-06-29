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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarField;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;

/**
 * A dialog to edit a product component generation.
 */
public class GenerationEditDialog extends IpsPartEditDialog implements ModifyListener {

    private GregorianCalendarField dateField;

    private IProductCmptGeneration previous;
    private IProductCmptGeneration next;

    /**
     * Creates a new dialog to edit a product cmpt generation
     * 
     * @param generation The generation to edit
     * @param parentShell The shell to be used as parent for the dialog
     */
    public GenerationEditDialog(IProductCmptGeneration generation, Shell parentShell) {
        super(generation, parentShell, Messages.GenerationEditDialog_titleChangeValidFromDate, true);

        // we have to store previous and next here, because the evaulation of
        // previous and next depend on the valid-from date which we will modify...
        this.previous = (IProductCmptGeneration)generation.getPreviousByValidDate();
        this.next = (IProductCmptGeneration)generation.getNextByValidDate();
    }

    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.GenerationEditDialog_pagetitleValidFromDate);
        firstPage.setControl(createFirstPage(folder));
        createDescriptionTabItem(folder);
        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(workArea, Messages.GenerationEditDialog_labelValidFrom);
        Text date = uiToolkit.createText(workArea);

        date.addModifyListener(this);

        dateField = new GregorianCalendarField(date);
        return c;
    }

    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(dateField, IProductCmptGeneration.PROPERTY_VALID_FROM);
    }

    @Override
    public void modifyText(ModifyEvent e) {
        // We need to validate here and "by hand" because this validation is not neccessary
        // to be done during normal validation of a generation.
        GregorianCalendar value = (GregorianCalendar)dateField.getValue();
        if (value == null) {
            SimpleDateFormat format = (SimpleDateFormat)SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
            super.setErrorMessage(Messages.GenerationEditDialog_msgInvalidFormat + format.toPattern());
            getButton(OK).setEnabled(false);
        } else if (previous != null && !value.after(previous.getValidFrom())) {
            String msg = NLS.bind(Messages.GenerationEditDialog_msgDateToEarly, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular());
            super.setErrorMessage(msg);
            getButton(OK).setEnabled(false);
        } else if (next != null && !next.getValidFrom().after(value)) {
            String msg = NLS.bind(Messages.GenerationEditDialog_msgDateToLate, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNameSingular());
            super.setErrorMessage(msg);
            getButton(OK).setEnabled(false);
        } else {
            super.setErrorMessage(null);
            getButton(OK).setEnabled(true);
        }
    }

}
