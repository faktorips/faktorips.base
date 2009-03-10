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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IMessage;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;

/**
 * This wizard is available trough the <code>EnumContentEditor</code> when the
 * <code>IEnumContent</code> to edit does not refer to a valid <code>IEnumType</code> or the number
 * of referenced enum attributes that is stored in the <code>IEnumContent</code> does not correspond
 * to the number of enum attributes defined in the referenced <code>IEnumType</code>.
 * <p>
 * On the first page the wizard lets the user select a valid <code>IEnumType</code> to refer to. The
 * second page provides comfortable assignment of enum attributes to existing enum attribute values.
 * 
 * @see EnumContentEditor
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class FixEnumContentWizard extends Wizard {

    /** The image for this wizard. */
    private final String IMAGE = "wizards/BrokenEnumWizard.png";

    /** The <code>IEnumContent</code> to set a new <code>IEnumType</code> for. */
    private IEnumContent enumContent;

    /** The new enum type that has been chosen. */
    private IEnumType newEnumType;

    /** The ui toolkit to create new ui elements with. */
    private UIToolkit uiToolkit;

    /** The wizard page to choose a new enum type. */
    private ChooseEnumTypePage chooseEnumTypePage;

    /** The wizard page to assign enum attributes. */
    private AssignEnumAttributesPage assignEnumAttributesPage;

    /**
     * Creates a new <code>FixEnumContentWizard</code>.
     * 
     * @param enumContent The <code>IEnumContent</code> to fix.
     */
    public FixEnumContentWizard(IEnumContent enumContent) {
        this.enumContent = enumContent;
        this.uiToolkit = new UIToolkit(null);

        setWindowTitle(Messages.FixEnumContentWizard_title);
        setNeedsProgressMonitor(false);
        setDefaultPageImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor(IMAGE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        chooseEnumTypePage = new ChooseEnumTypePage();
        assignEnumAttributesPage = new AssignEnumAttributesPage();
        addPage(chooseEnumTypePage);
        addPage(assignEnumAttributesPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        // If not all columns have been assigned the user must confirm deletion
        boolean confirmed = true;
        int numberNotAssignedColumns = assignEnumAttributesPage.getCurrentlyNotAssignedColumns().size();
        if (numberNotAssignedColumns > 0) {
            String message = (numberNotAssignedColumns > 1) ? NLS.bind(
                    Messages.FixEnumContentWizard_assignEnumAttributesDeleteColumnsConfirmationMessagePlural,
                    numberNotAssignedColumns) : NLS.bind(
                    Messages.FixEnumContentWizard_assignEnumAttributesDeleteColumnsConfirmationMessageSingular,
                    numberNotAssignedColumns);
            confirmed = MessageDialog.openConfirm(getShell(),
                    Messages.FixEnumContentWizard_assignEnumAttributesDeleteColumnsConfirmationTitle, message);
        }

        if (confirmed) {
            try {
                IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {

                    /**
                     * {@inheritDoc}
                     */
                    public void run(IProgressMonitor monitor) throws CoreException {
                        deleteObsoleteEnumAttributeValues();
                        createNewEnumAttributeValues();
                        moveAttributeValues();
                        enumContent.setEnumType(newEnumType.getQualifiedName());
                    }

                };
                enumContent.getIpsModel().runAndQueueChangeEvents(workspaceRunnable, null);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        return confirmed;
    }

    /**
     * Deletes all existing enum attribute values that are no longer needed according to the not
     * assigned columns of the <code>AssignEnumAttributesPage</code> from every enum value of this
     * enum content .
     */
    private void deleteObsoleteEnumAttributeValues() {
        // Collect all obsolete enum attribute values to delete
        List<Integer> notAssignedColumns = assignEnumAttributesPage.getCurrentlyNotAssignedColumns();
        List<IEnumAttributeValue> enumAttributeValuesToDelete = new ArrayList<IEnumAttributeValue>();
        for (Integer currentNotAssignedColumn : notAssignedColumns) {
            for (IEnumValue currentEnumValue : enumContent.getEnumValues()) {
                enumAttributeValuesToDelete.add(currentEnumValue.getEnumAttributeValues().get(
                        currentNotAssignedColumn.intValue()));
            }
        }

        // Delete all the collected enum attribute values
        for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValuesToDelete) {
            currentEnumAttributeValue.delete();
        }
    }

    /**
     * Creates new enum attribute values on every enum value of the enum content for every new
     * column that has been added according to the column order of the
     * <code>AssignEnumAttributesPage</code>.
     */
    private void createNewEnumAttributeValues() throws CoreException {
        int[] columnOrder = assignEnumAttributesPage.getColumnOrder();
        for (int i = 0; i < columnOrder.length; i++) {
            int currentPosition = columnOrder[i];
            if (currentPosition == 0) {
                for (IEnumValue currentEnumValue : enumContent.getEnumValues()) {
                    currentEnumValue.newEnumAttributeValue();
                }
            }
        }
    }

    /**
     * Moves the enum attribute values in each enum value according to the column order of the
     * <code>AssignEnumAttributesPage</code>.
     */
    private void moveAttributeValues() {
        // The column order as requested by the user
        int[] columnOrder = assignEnumAttributesPage.getColumnOrder();

        // Tracking the enum attribute values order
        int[] enumAttributeValuesOrder = new int[columnOrder.length];
        for (int i = 0; i < enumAttributeValuesOrder.length; i++) {
            enumAttributeValuesOrder[i] = i;
        }

        for (int currentEnumAttributeIndex = 0; currentEnumAttributeIndex < columnOrder.length; currentEnumAttributeIndex++) {
            int currentPosition = columnOrder[currentEnumAttributeIndex];

            // We do not want to create new enum attribute values right now
            if (currentPosition == 0) {
                continue;
            }

            /*
             * Moving neccessary if the position specified in the column order does not correspond
             * to the current enum attribute index.
             */
            if (currentPosition != currentEnumAttributeIndex + 1) {
                /*
                 * Move up if an enum attribute value with a higher number than the current enum
                 * attribute index shall be moved, else move down.
                 */
                boolean up = (currentPosition > currentEnumAttributeIndex + 1) ? false : true;

                // Move the enum attribute values in all enum values of the enum content
                for (IEnumValue currentEnumValue : enumContent.getEnumValues()) {
                    int requestedIndex = currentPosition - 1;
                    // Look up the correct index in the tracking of the enum attribute values order
                    int currentIndex = -1;
                    for (int i = 0; i < enumAttributeValuesOrder.length; i++) {
                        if (enumAttributeValuesOrder[i] == requestedIndex) {
                            currentIndex = i;
                            break;
                        }
                    }

                    // Should theoretically never happen but just to be safe
                    if (currentIndex == -1) {
                        throw new NoSuchElementException();
                    }

                    IEnumAttributeValue enumAttributeValueToMove = currentEnumValue.getEnumAttributeValues().get(
                            currentIndex);
                    /*
                     * Move as long by 1 as the index of the enum attribute value does not
                     * correspond to the index of the enum attribute.
                     */
                    while (currentIndex != currentEnumAttributeIndex) {
                        currentIndex = currentEnumValue.moveEnumAttributeValue(enumAttributeValueToMove, up);
                        // Track enum attribute values order, swap entries
                        int modifier = (up) ? -1 : 1;
                        int temp = enumAttributeValuesOrder[currentIndex + modifier];
                        enumAttributeValuesOrder[currentIndex + modifier] = enumAttributeValuesOrder[currentIndex];
                        enumAttributeValuesOrder[currentIndex] = temp;
                    }
                }
            }
        }
    }

    /** The wizard page that lets the user choose a new enum type. */
    private class ChooseEnumTypePage extends WizardPage {

        /**
         * Creates a new <code>ChooseEnumTypePage</code>.
         */
        private ChooseEnumTypePage() {
            super(Messages.FixEnumContentWizard_chooseEnumTypePageTitle);

            setPageComplete(false);
        }

        /**
         * {@inheritDoc}
         */
        public void createControl(Composite parent) {
            Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
            workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

            // Choose enum type control
            uiToolkit.createFormLabel(workArea, Messages.FixEnumContentWizard_labelNewEnumType);
            final IpsObjectRefControl enumTypeRefControl = uiToolkit.createEnumTypeRefControl(enumContent
                    .getIpsProject(), workArea, false);
            enumTypeRefControl.getTextControl().addModifyListener(new ModifyListener() {

                /**
                 * {@inheritDoc}
                 */
                public void modifyText(ModifyEvent event) {
                    try {
                        enumTypeModified(enumTypeRefControl);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }

            });

            try {
                IEnumType enumType = enumContent.findEnumType();
                if (enumType != null) {
                    if (!(enumType.isAbstract()) && !(enumType.getValuesArePartOfModel())) {
                        enumTypeRefControl.setText(enumType.getQualifiedName());
                    }
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            setMessage(Messages.FixEnumContentWizard_msgChooseEnumType);
            setControl(workArea);
        }

        /**
         * Update the wizards message area and check for page complete when the enum type field has
         * been modified.
         */
        private void enumTypeModified(IpsObjectRefControl enumTypeRefControl) throws CoreException {
            setMessage(Messages.FixEnumContentWizard_msgChooseEnumType);

            String text = enumTypeRefControl.getText();
            newEnumType = enumContent.getIpsProject().findEnumType(text);

            boolean pageComplete = false;
            if (newEnumType != null) {
                if (!(newEnumType.isAbstract()) && !(newEnumType.getValuesArePartOfModel())) {
                    pageComplete = true;
                    assignEnumAttributesPage.refreshControl();
                }
                if (newEnumType.isAbstract()) {
                    setMessage(Messages.FixEnumContentWizard_chosenEnumTypeAbstract, IMessage.ERROR);
                }
                if (newEnumType.getValuesArePartOfModel()) {
                    setMessage(Messages.FixEnumContentWizard_chosenEnumTypeValuesArePartOfModel, IMessage.ERROR);
                }
            } else {
                setMessage(Messages.FixEnumContentWizard_chosenEnumTypeDoesNotExist, IMessage.ERROR);
            }

            setPageComplete(pageComplete);
        }

    }

    /**
     * The wizard page that lets the user comfortably assign enum attributes of the chosen enum
     * type.
     */
    private class AssignEnumAttributesPage extends WizardPage {

        /** All available columns. */
        private List<String> availableColumns;

        /** Array containing a combo box for each enum attribute of the chosen new enum type. */
        private Combo[] combos;

        /** Array containing a label for each enum attribute of the chosen new enum type. */
        private Label[] labels;

        /**
         * Creates a new <code>AssignEnumAttributesPage</code>.
         */
        private AssignEnumAttributesPage() {
            super(Messages.FixEnumContentWizard_assignEnumAttributesPageTitle);

            availableColumns = new ArrayList<String>();
            availableColumns.add(Messages.FixEnumContentWizard_assignEnumAttributesCreateNewColumn);
            for (int i = 0; i < enumContent.getReferencedEnumAttributesCount(); i++) {
                availableColumns.add(NLS.bind(Messages.DefaultColumnName, i + 1));
            }

            setPageComplete(false);
        }

        /**
         * {@inheritDoc}
         */
        public void createControl(Composite parent) {
            Composite control = uiToolkit.createComposite(parent);
            setControl(control);

            refreshControl();

            setMessage(Messages.FixEnumContentWizard_msgAssignEnumAttributes);
        }

        /** Refreshes the controls of this page. */
        public void refreshControl() {
            if (newEnumType == null || getControl() == null) {
                return;
            }

            // Dispose old widgets first if existing
            if (combos != null) {
                for (Combo currentCombo : combos) {
                    currentCombo.dispose();
                }
            }
            if (labels != null) {
                for (Label currentLabel : labels) {
                    currentLabel.dispose();
                }
            }

            // Recreate control
            Composite control = (Composite)getControl();
            Composite parent = control.getParent();
            control.dispose();
            control = uiToolkit.createLabelEditColumnComposite(parent);
            control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            // Create the widgets
            int numberEnumAttributes = newEnumType.getEnumAttributesCount(true);
            combos = new Combo[numberEnumAttributes];
            labels = new Label[numberEnumAttributes];
            List<IEnumAttribute> enumAttributes = newEnumType.findAllEnumAttributes();
            for (int i = 0; i < numberEnumAttributes; i++) {
                IEnumAttribute currentEnumAttribute = enumAttributes.get(i);
                labels[i] = uiToolkit.createFormLabel(control, currentEnumAttribute.getName() + ':');
                combos[i] = uiToolkit.createCombo(control);
                for (int j = 0; j < availableColumns.size(); j++) {
                    String listItem = (j == 0) ? "" : " - ";
                    combos[i].add(listItem + availableColumns.get(j));
                    combos[i].addModifyListener(new ModifyListener() {

                        /**
                         * {@inheritDoc}
                         */
                        public void modifyText(ModifyEvent event) {
                            combosModified();
                        }

                    });
                }
            }

            parent.layout();
            setControl(control);
        }

        /**
         * Update the wizards message area and check for page complete when any combo has been
         * modified.
         */
        private void combosModified() {
            setMessage(Messages.FixEnumContentWizard_msgAssignEnumAttributes);

            boolean pageComplete = true;
            List<String> chosenColumns = new ArrayList<String>();
            for (Combo currentCombo : combos) {

                String currentComboText = currentCombo.getText();
                if (currentComboText.equals("")) {
                    setMessage(Messages.FixEnumContentWizard_assignEnumAttributesAttributeNotAssigned, IMessage.ERROR);
                    pageComplete = false;
                    break;

                } else {
                    // Columns may not be assigned more often than once
                    if (chosenColumns.contains(currentComboText)) {
                        if (!(currentComboText
                                .equals(Messages.FixEnumContentWizard_assignEnumAttributesCreateNewColumn))) {

                            setMessage(Messages.FixEnumContentWizard_assignEnumAttributesDuplicateColumnAssigned,
                                    IMessage.ERROR);
                            pageComplete = false;
                            break;
                        }
                    } else {
                        chosenColumns.add(currentComboText);
                    }
                }
            }

            setPageComplete(pageComplete);
        }

        /**
         * Returns a list containing all column numbers that are currently not assigned to enum
         * attributes (beginning with 1). An empty list will be returned if all columns are
         * assigned, <code>null</code> is never returned.
         */
        public List<Integer> getCurrentlyNotAssignedColumns() {
            List<Integer> currentlyNotAssignedColumns = new ArrayList<Integer>();
            List<String> assignedColumnNames = new ArrayList<String>(availableColumns.size());
            for (Combo currentCombo : combos) {
                String currentColumnName = currentCombo.getText().substring(3);
                if (currentColumnName.equals(Messages.DefaultColumnName)) {
                    continue;
                }
                if (!(assignedColumnNames.contains(currentColumnName))) {
                    assignedColumnNames.add(currentColumnName);
                }
            }

            for (int i = 0; i < availableColumns.size(); i++) {
                String currentColumnName = availableColumns.get(i);
                if (!(assignedColumnNames.contains(currentColumnName))) {
                    currentlyNotAssignedColumns.add(new Integer(i));
                }
            }

            return currentlyNotAssignedColumns;
        }

        /**
         * Returns the current column order.
         * <ul>
         * <li>0 represents a new column that has to be created.
         * </ul>
         */
        public int[] getColumnOrder() {
            int[] columnOrder = new int[combos.length];

            for (int i = 0; i < combos.length; i++) {
                Combo currentCombo = combos[i];
                for (int j = 0; j < availableColumns.size(); j++) {
                    String currentColumn = availableColumns.get(j);
                    if (currentColumn.equals(currentCombo.getText())) {
                        columnOrder[i] = j;
                    }
                }
            }

            return columnOrder;
        }

    }

}
