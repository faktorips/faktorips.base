/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.editors.enumcontent.EnumContentEditor;
import org.faktorips.devtools.core.ui.editors.enumcontent.Messages;

/**
 * This wizard is available trough the <tt>EnumContentEditor</tt> if the <tt>IEnumContent</tt> to
 * edit does not refer to a valid <tt>IEnumType</tt>, if the number of referenced
 * <tt>IEnumAttribute</tt>s that is stored in the <tt>IEnumContent</tt> does not correspond to the
 * number of <tt>IEnumAttribute</tt>s defined in the referenced <tt>IEnumType</tt> or if the
 * ordering of the <tt>IEnumAttributeValue</tt>s needs to be changed because the ordering of the
 * referenced <tt>IEnumAttribute</tt>s has changed.
 * <p>
 * On the first page the wizard asks the user to select a valid <tt>IEnumType</tt> to refer to. The
 * second page provides comfortable assignment of <tt>IEnumAttribute</tt>s to existing
 * <tt>IEnumAttributeValue</tt>s.
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

    /** The <tt>IEnumContent</tt> to fix. */
    private IEnumContent enumContent;

    /** The new <tt>IEnumType</tt> that has been chosen. */
    private IEnumType newEnumType;

    /** The UI toolkit to create new UI elements with. */
    private UIToolkit uiToolkit;

    /** The wizard page to choose a new <tt>IEnumType</tt>. */
    private ChooseEnumTypePage chooseEnumTypePage;

    /** The wizard page to assign <tt>IEnumAttribute</tt>s to <tt>IEnumAttributeValue</tt>s. */
    private AssignEnumAttributesPage assignEnumAttributesPage;

    /**
     * Creates a new <tt>FixEnumContentWizard</tt>.
     * 
     * @param enumContent The <tt>IEnumContent</tt> to fix.
     */
    public FixEnumContentWizard(IEnumContent enumContent) {
        this.enumContent = enumContent;
        uiToolkit = new UIToolkit(null);

        setWindowTitle(Messages.FixEnumContentWizard_title);
        setNeedsProgressMonitor(false);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    @Override
    public void addPages() {
        chooseEnumTypePage = new ChooseEnumTypePage();
        assignEnumAttributesPage = new AssignEnumAttributesPage();
        addPage(chooseEnumTypePage);
        addPage(assignEnumAttributesPage);
    }

    @Override
    public boolean performFinish() {
        // If not all columns have been assigned the user must confirm deletion.
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
                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        deleteObsoleteEnumAttributeValues();
                        createNewEnumAttributeValues();
                        if (enumContent.getEnumValuesCount() > 0) {
                            moveAttributeValues();
                        }
                        enumContent.setEnumType(newEnumType.getQualifiedName());
                        enumContent.clearUniqueIdentifierValidationCache();
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
     * Deletes all existing <tt>IEnumAttributeValue</tt>s that are no longer needed according to the
     * not assigned columns of the <tt>AssignEnumAttributesPage</tt> from every <tt>IEnumValue</tt>
     * of this <tt>IEnumContent</tt>.
     */
    private void deleteObsoleteEnumAttributeValues() {
        // Collect all obsolete EnumAttributeValues to delete.
        List<Integer> notAssignedColumns = assignEnumAttributesPage.getCurrentlyNotAssignedColumns();
        List<IEnumAttributeValue> enumAttributeValuesToDelete = new ArrayList<IEnumAttributeValue>();
        for (Integer currentNotAssignedColumn : notAssignedColumns) {
            for (IEnumValue currentEnumValue : enumContent.getEnumValues()) {
                enumAttributeValuesToDelete.add(currentEnumValue.getEnumAttributeValues().get(
                        currentNotAssignedColumn.intValue() - 1));
            }
        }

        // Delete all the collected EnumAttributeValues.
        for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValuesToDelete) {
            currentEnumAttributeValue.delete();
        }
    }

    /**
     * Creates new <tt>IEnumAttributeValue</tt>s on every <tt>IEnumValue</tt> of the
     * <tt>IEnumContent</tt> for every new column that has been added according to the column order
     * of the <tt>AssignEnumAttributesPage</tt>.
     */
    private void createNewEnumAttributeValues() throws CoreException {
        int[] columnOrder = assignEnumAttributesPage.getColumnOrder();
        for (int currentPosition : columnOrder) {
            if (currentPosition == 0) {
                for (IEnumValue currentEnumValue : enumContent.getEnumValues()) {
                    currentEnumValue.newEnumAttributeValue();
                }
            }
        }
    }

    /**
     * Each number in the column order array will be decremented by 1 for each number before that is
     * contained in the currently not assigned column numbers.
     * <p>
     * Example column order: 1, 3, 4, 7<br />
     * Not assigned columns: 2, 5, 6<br />
     * Decremented column order: 1, 2, 3, 4
     */
    private int[] computeDecrementedColumnOrder() {
        int[] columnOrder = assignEnumAttributesPage.getColumnOrder();
        List<Integer> notAssignedColumns = assignEnumAttributesPage.getCurrentlyNotAssignedColumns();

        for (int i = 0; i < columnOrder.length; i++) {
            int numberSmallerColumnsNotAssigned = 0;
            for (Integer currentNotAssignedColumn : notAssignedColumns) {
                int currentNotAssignedColumnNumber = currentNotAssignedColumn.intValue();
                if (currentNotAssignedColumnNumber < columnOrder[i]) {
                    numberSmallerColumnsNotAssigned++;
                } else if (currentNotAssignedColumnNumber > columnOrder[i]) {
                    break;
                }
            }
            columnOrder[i] = columnOrder[i] - numberSmallerColumnsNotAssigned;
        }

        return columnOrder;
    }

    /**
     * Returns the order of the <tt>IEnumAttributeValue</tt>s as it is after deleting obsolete
     * <tt>IEnumAttributeValue</tt>s and creating new <tt>IEnumAttributeValue</tt>s. A zero
     * represents an <tt>IEnumAttributeValue</tt> that has been newly created.
     */
    private int[] computeEnumAttributeValuesOrder(int[] decrementedColumnOrder) {
        int[] enumAttributeValuesOrder = new int[decrementedColumnOrder.length];
        Arrays.sort(decrementedColumnOrder);
        for (int i = 0; i < enumAttributeValuesOrder.length; i++) {
            int currentColumnNumber = i + 1;
            /*
             * Set the current position to a 0 if the current column number is higher than the
             * number of EnumAttributes referenced in the EnumContent (this means that for the
             * current column number there must be a new EnumAttributeValue).
             * 
             * Also set the current position to 0 if the current column is not contained in the
             * decremented column order.
             */
            if (currentColumnNumber > enumContent.getEnumAttributeReferencesCount()
                    || Arrays.binarySearch(decrementedColumnOrder, currentColumnNumber) < 0) {
                enumAttributeValuesOrder[i] = 0;
            } else {
                enumAttributeValuesOrder[i] = currentColumnNumber;
            }
        }

        return enumAttributeValuesOrder;
    }

    /**
     * Moves <tt>IEnumAttributeValue</tt>s in each <tt>IEnumValue</tt> according to the column order
     * of the <tt>AssignEnumAttributesPage</tt>.
     * <p>
     * Because this algorithm is rather complicated here is an example how it works:
     * <p>
     * The user requested the following column order: 0 0 0 4 5 6<br />
     * Every zero represents a new column (and therefore <tt>IEnumAttributeValue</tt>) that has been
     * created earlier.
     * <p>
     * Because column 1 2 3 are not requested they have been deleted earlier. Due to this deletion
     * the requested column order must be decremented accordingly, because by deleting the first 3
     * <tt>IEnumAttributeValue</tt>s the last 3 are now the first 3.
     * <p>
     * So the decremented column order is: 0 0 0 1 2 3<br />
     * Remember that the columns 1 2 3 were formally the columns 4 5 6.
     * <p>
     * In every iteration the algorithm goes over every number of the requested decremented column
     * order. If the current number is 0 the algorithm will continue to the next number. Else the
     * number is compared to the current attribute number. If the current number is smaller than the
     * attribute number it means a downwards move has to be performed. If it is higher an upwards
     * move has to be performed and if it is equal nothing has to be performed. The attribute value
     * corresponding to the current number is then moved until it reaches the desired position.
     * <p>
     * The algorithm terminates when the requested decremented column order equals the attribute
     * values order.
     * <p>
     * The following table shows the iterations of the algorithm:
     * </p>
     * 
     * <table>
     * <tr>
     * <th>attribute number
     * <th>requested decremented column order
     * <th>attribute values order init
     * <th>attribute values order it.1
     * <th>attribute values order it.2
     * <th>attribute values order it.3
     * </tr>
     * 
     * <tr>
     * <td>1
     * <td>0
     * <td>1
     * <td>0
     * <td>0
     * <td>0
     * </tr>
     * 
     * <tr>
     * <td>2
     * <td>0
     * <td>2
     * <td>1
     * <td>0
     * <td>0
     * </tr>
     * 
     * <tr>
     * <td>3
     * <td>0
     * <td>3
     * <td>0
     * <td>1
     * <td>0
     * </tr>
     * 
     * <tr>
     * <td>4
     * <td>1
     * <td>0
     * <td>2
     * <td>0
     * <td>1
     * </tr>
     * 
     * <tr>
     * <td>5
     * <td>2
     * <td>0
     * <td>0
     * <td>2
     * <td>2
     * </tr>
     * 
     * <tr>
     * <td>6
     * <td>3
     * <td>0
     * <td>3
     * <td>3
     * <td>3
     * </tr>
     * </table>
     */
    private void moveAttributeValues() {
        /*
         * The column order as requested by the user and decremented where necessary (due to deleted
         * EnumAttributeValues).
         */
        int[] decrementedColumnOrder = computeDecrementedColumnOrder();

        // Tracking of the EnumAttributeValues ordering.
        int[] enumAttributeValuesOrder = computeEnumAttributeValuesOrder(decrementedColumnOrder.clone());

        // The algorithm repeats as long as the orders do not correspond.
        while (!(Arrays.equals(decrementedColumnOrder, enumAttributeValuesOrder))) {

            // For each EnumAttribute we want to get the right EnumAttributeValue into place.
            for (int currentEnumAttributeIndex = 0; currentEnumAttributeIndex < decrementedColumnOrder.length; currentEnumAttributeIndex++) {
                int desiredPosition = decrementedColumnOrder[currentEnumAttributeIndex];
                /*
                 * The newly created EnumAttributeValues will get to their right positions by
                 * themselves over time.
                 */
                if (desiredPosition == 0) {
                    continue;
                }

                /*
                 * Moving is necessary if the position specified in the decremented column order
                 * does not correspond to the index of the current EnumAttribute + 1.
                 */
                if (desiredPosition != currentEnumAttributeIndex + 1) {
                    /*
                     * Look up the correct index of the EnumAttributeValue in the tracking of the
                     * EnumAttributeValues order. This is necessary because when moving
                     * EnumAttributeValues the indexes change.
                     */
                    int currentEnumAttributeValueIndex = getEnumAttributeValueIndex(desiredPosition,
                            enumAttributeValuesOrder);

                    /*
                     * Move up if an EnumAttributeValue with a higher number than the index of the
                     * current EnumAttribute + 1 shall be moved, else move down. This rule works
                     * always with this algorithm.
                     * 
                     * The proof on the example of a higher number is: The corresponding
                     * EnumAttributeValue starts further down in the list. So it must be moved
                     * upwards. It can't obtain a position higher in the list than the index of the
                     * current EnumAttribute + 1 tough because moving will stop when it reaches this
                     * position.
                     */
                    boolean up = (desiredPosition > currentEnumAttributeIndex + 1) ? true : false;

                    // Move the EnumAttributeValues of all EnumValues of the EnumContent.
                    enumAttributeValuesOrder = moveAttributeValues(currentEnumAttributeValueIndex,
                            currentEnumAttributeIndex, enumAttributeValuesOrder, up);
                }
            }
        }
    }

    /**
     * Returns the current index of the <tt>IEnumAttributeValue</tt> to the given requested column
     * index.
     * <p>
     * Example: The user wants the current second column (<tt>IEnumAttributeValue</tt>) to be the
     * 4th one after the fix operation. The requested column index would be 3, the returned index 1
     * (if no moving has already been done during the fix operation).
     */
    private int getEnumAttributeValueIndex(int requestedColumnIndex, int[] enumAttributeValuesOrder) {
        int currentEnumAttributeValueIndex = -1;
        for (int i = 0; i < enumAttributeValuesOrder.length; i++) {
            if (enumAttributeValuesOrder[i] == requestedColumnIndex) {
                currentEnumAttributeValueIndex = i;
                break;
            }
        }
        return currentEnumAttributeValueIndex;
    }

    /**
     * Moves the <tt>IEnumAttributeValue</tt> identified by it's index in each <tt>IEnumValue</tt>
     * of the <tt>IEnumContent</tt> until it reaches the position of the given
     * <tt>enumAttributeIndex</tt>.
     */
    private int[] moveAttributeValues(int enumAttributeValueIndex,
            int enumAttributeIndex,
            int[] enumAttributeValuesOrder,
            boolean up) {

        boolean enumAttributeValuesOrderTracked = false;
        for (IEnumValue currentEnumValue : enumContent.getEnumValues()) {
            IEnumAttributeValue enumAttributeValueToMove = currentEnumValue.getEnumAttributeValues().get(
                    enumAttributeValueIndex);
            /*
             * Move as long by 1 as the index of the EnumAttributeValue does not correspond to the
             * index of the EnumAttribute.
             */
            int moveIndex = enumAttributeValueIndex;
            while (moveIndex != enumAttributeIndex) {
                moveIndex = currentEnumValue.moveEnumAttributeValue(enumAttributeValueToMove, up);
                // Track EnumAttributeValues order, swap entries.
                if (!enumAttributeValuesOrderTracked) {
                    int modifierToOldIndex = (up) ? 1 : -1;
                    int temp = enumAttributeValuesOrder[moveIndex + modifierToOldIndex];
                    enumAttributeValuesOrder[moveIndex + modifierToOldIndex] = enumAttributeValuesOrder[moveIndex];
                    enumAttributeValuesOrder[moveIndex] = temp;
                }
            }

            /*
             * Do not track the EnumAttributeValues order again while moving the EnumAttributes of
             * the other EnumValues. This would break everything.
             */
            enumAttributeValuesOrderTracked = true;
        }

        return enumAttributeValuesOrder;
    }

    /** The wizard page that lets the user choose a new <tt>IEnumType</tt>. */
    private class ChooseEnumTypePage extends WizardPage {

        /** Creates the <tt>ChooseEnumTypePage</tt>. */
        private ChooseEnumTypePage() {
            super(Messages.FixEnumContentWizard_chooseEnumTypePageTitle);
            setTitle(Messages.FixEnumContentWizard_chooseEnumTypePageTitle);
            setPageComplete(false);
        }

        @Override
        public void createControl(Composite parent) {
            Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);
            workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

            // Choose EnumType control.
            uiToolkit.createFormLabel(workArea, Messages.FixEnumContentWizard_labelNewEnumType);
            final IpsObjectRefControl enumTypeRefControl = uiToolkit.createEnumTypeRefControl(enumContent
                    .getIpsProject(), workArea, false);
            enumTypeRefControl.getTextControl().addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    try {
                        enumTypeModified(enumTypeRefControl);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            try {
                IEnumType enumType = enumContent.findEnumType(enumContent.getIpsProject());
                if (enumType != null) {
                    if (!(enumType.isAbstract()) && !(enumType.isContainingValues())) {
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
         * Update the wizards message area and check for page complete when the field to choose an
         * <tt>IEnumType</tt> has been modified.
         */
        private void enumTypeModified(IpsObjectRefControl enumTypeRefControl) throws CoreException {
            setMessage(Messages.FixEnumContentWizard_msgChooseEnumType);

            String text = enumTypeRefControl.getText();
            newEnumType = enumContent.getIpsProject().findEnumType(text);

            setErrorMessage(null);
            boolean pageComplete = true;
            if (text.length() == 0) {
                setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeEmpty);
                pageComplete = false;
            } else {
                if (newEnumType == null) {
                    setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeDoesNotExist);
                    pageComplete = false;
                } else {
                    if (newEnumType.isAbstract()) {
                        setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeAbstract);
                        pageComplete = false;
                    }
                    if (newEnumType.isContainingValues()) {
                        setErrorMessage(Messages.FixEnumContentWizard_chosenEnumTypeValuesArePartOfModel);
                        pageComplete = false;
                    }
                }
            }

            setPageComplete(pageComplete);
            if (pageComplete) {
                assignEnumAttributesPage.refreshControl();
            }
        }

    }

    /**
     * The wizard page that lets the user comfortably assign <tt>IEnumAttribute</tt>s of the chosen
     * <tt>IEnumType</tt> to <tt>IEnumAttributeValue</tt>s of the <tt>IEnumContent</tt> to edit.
     */
    private class AssignEnumAttributesPage extends WizardPage {

        /** All available columns. */
        private List<String> availableColumns;

        /**
         * Array containing a <tt>Combo</tt> for each <tt>IEnumAttribute</tt> of the chosen new
         * <tt>IEnumType</tt>.
         */
        private Combo[] combos;

        /**
         * Array containing a label for each <tt>IEnumAttribute</tt> of the chosen new
         * <tt>IEnumType</tt>.
         */
        private Label[] labels;

        /** Creates the <tt>AssignEnumAttributesPage</tt>. */
        private AssignEnumAttributesPage() {
            super(Messages.FixEnumContentWizard_assignEnumAttributesPageTitle);
            setTitle(Messages.FixEnumContentWizard_assignEnumAttributesPageTitle);

            availableColumns = new ArrayList<String>();
            availableColumns.add(Messages.FixEnumContentWizard_assignEnumAttributesCreateNewColumn);
            List<IEnumAttributeReference> enumAttributeReferences = enumContent.getEnumAttributeReferences();
            for (int i = 0; i < enumContent.getEnumAttributeReferencesCount(); i++) {
                availableColumns.add(enumAttributeReferences.get(i).getName());
            }

            setPageComplete(false);
        }

        @Override
        public void createControl(Composite parent) {
            ScrolledComposite scrolledControl = new ScrolledComposite(parent, SWT.V_SCROLL);
            setControl(scrolledControl);
            refreshControl();
            setMessage(Messages.FixEnumContentWizard_msgAssignEnumAttributes);
        }

        /** Refreshes the controls of this page. */
        public void refreshControl() {
            if (newEnumType == null || getControl() == null) {
                return;
            }

            // Dispose old widgets first if existing.
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

            // Recreate control.
            ScrolledComposite scrolledControl = (ScrolledComposite)getControl();
            Composite parent = scrolledControl.getParent();
            scrolledControl.dispose();
            scrolledControl = new ScrolledComposite(parent, SWT.V_SCROLL);
            Group attributesGroup = uiToolkit.createGroup(scrolledControl,
                    Messages.FixEnumContentWizard_assignEnumAttributesGroup);
            Composite contents = uiToolkit.createLabelEditColumnComposite(attributesGroup);
            contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            // Create the widgets.
            int numberEnumAttributes = newEnumType.getEnumAttributesCountIncludeSupertypeCopies(false);
            combos = new Combo[numberEnumAttributes];
            labels = new Label[numberEnumAttributes];
            List<IEnumAttribute> enumAttributes = newEnumType.getEnumAttributesIncludeSupertypeCopies(false);
            for (int i = 0; i < numberEnumAttributes; i++) {
                IEnumAttribute currentEnumAttribute = enumAttributes.get(i);
                labels[i] = uiToolkit.createFormLabel(contents, currentEnumAttribute.getName() + ':');
                combos[i] = uiToolkit.createCombo(contents);
                for (int j = 0; j < availableColumns.size(); j++) {
                    String listItem = (j == 0) ? "" : " - ";
                    combos[i].add(listItem + availableColumns.get(j));
                    combos[i].addModifyListener(new ModifyListener() {
                        @Override
                        public void modifyText(ModifyEvent event) {
                            combosModified();
                        }
                    });
                }
            }

            scrolledControl.setContent(attributesGroup);
            scrolledControl.setExpandHorizontal(true);
            scrolledControl.setExpandVertical(true);
            scrolledControl.setMinSize(attributesGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            setControl(scrolledControl);

            parent.layout();
        }

        /**
         * Update the wizards message area and check for page complete when any <tt>Combo</tt> has
         * been modified.
         */
        private void combosModified() {
            setMessage(Messages.FixEnumContentWizard_msgAssignEnumAttributes);

            boolean pageComplete = true;
            List<String> chosenColumns = new ArrayList<String>();
            for (Combo currentCombo : combos) {
                String currentComboText = currentCombo.getText();
                if (currentComboText.length() == 0) {
                    setMessage(Messages.FixEnumContentWizard_assignEnumAttributesAttributeNotAssigned,
                            IMessageProvider.ERROR);
                    pageComplete = false;

                } else {
                    // Columns may not be assigned more often than once.
                    if (chosenColumns.contains(currentComboText)) {
                        if (!(currentComboText
                                .equals(Messages.FixEnumContentWizard_assignEnumAttributesCreateNewColumn))) {
                            setMessage(Messages.FixEnumContentWizard_assignEnumAttributesDuplicateColumnAssigned,
                                    IMessageProvider.ERROR);
                            pageComplete = false;
                            break;
                        }
                    } else {
                        if (currentComboText.length() > 0) {
                            chosenColumns.add(currentComboText);
                        }
                    }
                }
            }

            // If all columns have been assigned, fill all remaining automatically for the user.
            if (getCurrentlyNotAssignedColumns().size() == 0) {
                pageComplete = true;
                for (Combo currentCombo : combos) {
                    if (currentCombo.getText().length() == 0) {
                        currentCombo.setText(Messages.FixEnumContentWizard_assignEnumAttributesCreateNewColumn);
                    }
                }
            }

            setPageComplete(pageComplete);
        }

        /**
         * Returns a list containing all column numbers that are currently not assigned to
         * <tt>IEnumAttribute</tt>s (beginning with 1). An empty list will be returned if all
         * columns are assigned, <tt>null</tt> is never returned.
         */
        public List<Integer> getCurrentlyNotAssignedColumns() {
            List<Integer> currentlyNotAssignedColumns = new ArrayList<Integer>();
            List<String> assignedColumnNames = new ArrayList<String>(availableColumns.size());
            for (Combo currentCombo : combos) {
                String comboText = currentCombo.getText();
                if (comboText.length() == 0) {
                    continue;
                }
                String currentColumnName = comboText.substring(3);
                if (!(assignedColumnNames.contains(currentColumnName))) {
                    assignedColumnNames.add(currentColumnName);
                }
            }

            for (int i = 1; i < availableColumns.size(); i++) {
                String currentColumnName = availableColumns.get(i);
                if (!(assignedColumnNames.contains(currentColumnName))) {
                    currentlyNotAssignedColumns.add(new Integer(i));
                }
            }

            return currentlyNotAssignedColumns;
        }

        /**
         * Returns the current column order.
         * <p>
         * A zero represents a new column that has to be created.
         */
        public int[] getColumnOrder() {
            int[] columnOrder = new int[combos.length];
            for (int i = 0; i < combos.length; i++) {
                Combo currentCombo = combos[i];
                for (int j = 0; j < availableColumns.size(); j++) {
                    String currentColumn = availableColumns.get(j);
                    if (currentColumn.equals(currentCombo.getText().substring(3))) {
                        columnOrder[i] = j;
                    }
                }
            }
            return columnOrder;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Assures that the first <tt>Combo</tt> of the <tt>AssignEnumAttributesPage</tt> has focus
         * when making the page visible.
         */
        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                if (combos.length > 0) {
                    combos[0].setFocus();
                }
            }
        }

    }

}
