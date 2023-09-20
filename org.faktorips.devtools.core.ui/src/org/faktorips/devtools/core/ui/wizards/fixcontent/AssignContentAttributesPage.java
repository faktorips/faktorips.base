/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.fixcontent;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IPartReference;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * The wizard page that lets the user comfortably assign {@code ContentAttribute}s of the chosen
 * {@code ContentType} to {@code ContentAttributeValue}s of the {@code Content} to edit.
 */
public class AssignContentAttributesPage<T extends IIpsObject, E extends ILabeledElement> extends WizardPage {

    public static final String AVAIABLECOLUMN_PREFIX = " - "; //$NON-NLS-1$
    private DeltaFixWizardStrategy<T, E> contentStrategy;
    private UIToolkit uiToolkit;

    /** All available columns. */
    private List<String> availableColumns;

    /**
     * Array containing a {@code Combo} for each {@code ContentAttribute} of the chosen new
     * {@code ContentType}.
     */
    private Combo[] combos;

    /**
     * Array containing a label for each {@code ContentAttribute} of the chosen new
     * {@code ContentType}.
     */
    private Label[] labels;
    private T contentType;
    private Checkbox fillNewColumnsWithNull;

    /** Creates the {@code AssignContentAttributesPage}. */
    public AssignContentAttributesPage(T contentType, UIToolkit uiToolkit,
            DeltaFixWizardStrategy<T, E> contentStrategy) {
        super(Messages.FixContentWizard_assignColumnsPageTitle);
        setTitle(Messages.FixContentWizard_assignColumnsPageTitle);
        this.contentType = contentType;
        this.uiToolkit = uiToolkit;
        this.contentStrategy = contentStrategy;
        availableColumns = new ArrayList<String>();
        availableColumns.add(Messages.FixContentWizard_assignColumnsCreateNewColumn);
        List<IPartReference> contentAttributeReferences = contentStrategy.getContentAttributeReferences();
        for (int i = 0; i < contentStrategy.getContentAttributeReferencesCount(); i++) {
            availableColumns.add(contentStrategy.getContentAttributeReferenceName(contentAttributeReferences, i));
        }

        setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {
        ScrolledComposite scrolledControl = new ScrolledComposite(parent, SWT.V_SCROLL);
        setControl(scrolledControl);
        refreshControl();
        setMessage(Messages.FixContentWizard_msgAssignColumns);
    }

    /** Refreshes the controls of this page. */
    public void refreshControl() {
        contentType = contentStrategy.findContentType(contentStrategy.getIpsProject());
        if (contentType == null || getControl() == null) {
            return;
        }

        // Dispose old widgets first if existing.
        disposeOldWidgets();

        // Recreate control.
        ScrolledComposite scrolledControl = (ScrolledComposite)getControl();
        Composite parent = scrolledControl.getParent();
        scrolledControl.dispose();
        scrolledControl = new ScrolledComposite(parent, SWT.V_SCROLL);
        Group attributesGroup = uiToolkit.createGroup(scrolledControl, Messages.FixContentWizard_assignColumnsGroup);
        Composite contents = uiToolkit.createLabelEditColumnComposite(attributesGroup);
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create the widgets.
        int numberContentAttributes = contentStrategy.getContentAttributesCountIncludeSupertypeCopies(contentType,
                false);
        combos = new Combo[numberContentAttributes];
        labels = new Label[numberContentAttributes];
        int[] preSelectedComboIndexes = new int[numberContentAttributes];
        List<E> contentAttributes = contentStrategy.getContentAttributesIncludeSupertypeCopies(contentType, false);
        for (int i = 0; i < numberContentAttributes; i++) {
            E currentContentAttribute = contentAttributes.get(i);
            String localizedLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(currentContentAttribute);
            labels[i] = uiToolkit.createFormLabel(contents, localizedLabel + ':');
            combos[i] = uiToolkit.createCombo(contents);
            for (int j = 0; j < availableColumns.size(); j++) {
                if (availableColumns.get(j).equals(currentContentAttribute.getName())) {
                    preSelectedComboIndexes[i] = j;
                }
                String listItem = (j == 0) ? "" : AVAIABLECOLUMN_PREFIX; //$NON-NLS-1$
                combos[i].add(listItem + availableColumns.get(j));
                combos[i].addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent event) {
                        combosModified();
                    }
                });
            }
        }
        // Initialize combo selections (column name to attribute name)
        for (int i = 0; i < numberContentAttributes; i++) {
            combos[i].select(preSelectedComboIndexes[i]);
        }

        if (contentType instanceof ITableStructure) {
            fillNewColumnsWithNull = uiToolkit.createCheckbox(contents,
                    Messages.bind(Messages.FixContentWizard_checkboxFillNewColumnsWithNull,
                            IpsPlugin.getDefault().getIpsPreferences().getNullPresentation()));
        }

        scrolledControl.setContent(attributesGroup);
        scrolledControl.setExpandHorizontal(true);
        scrolledControl.setExpandVertical(true);
        scrolledControl.setMinSize(attributesGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        setControl(scrolledControl);

        parent.layout();
    }

    private void disposeOldWidgets() {
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
    }

    /**
     * Update the wizards message area and check for page complete when any {@link Combo} has been
     * modified.
     */
    private void combosModified() {
        setMessage(Messages.FixContentWizard_msgAssignColumns);

        boolean pageComplete = true;
        List<String> chosenColumns = new ArrayList<String>();
        for (int i = 0; i < combos.length; i++) {
            String currentComboText = combos[i].getText();
            if (currentComboText.length() == 0) {
                setMessage(Messages.FixContentWizard_assignColumnsAttributeNotAssigned, IMessageProvider.ERROR);
                pageComplete = false;
                setPageComplete(pageComplete);
                return;
            } else {
                // Columns may not be assigned more often than once.
                if (chosenColumns.contains(currentComboText) && comboBoxIsColumn(currentComboText)) {
                    setMessage(Messages.FixContentWizard_assignColumnsDuplicateColumnAssigned, IMessageProvider.ERROR);
                    pageComplete = false;
                    setPageComplete(pageComplete);
                    return;
                } else {
                    chosenColumns.add(currentComboText);
                }
            }
            // Checking whether the datatypes of two columns match. Only gives a warning because
            // some datatypes can be converted into another one e.g. Integer to Double.
            // String columns will always be valid.
            if (checkForMismatchedDatatypes(currentComboText, i)) {
                setMessage(Messages.FixContentWizard_assignColumnsDataTypesNotMatching, IMessageProvider.WARNING);
            }

        }

        // If all columns have been assigned, fill all remaining automatically for the user.
        if (getCurrentlyNotAssignedColumns().size() == 0) {
            pageComplete = true;
            for (Combo currentCombo : combos) {
                if (currentCombo.getText().length() == 0) {
                    currentCombo.setText(Messages.FixContentWizard_assignColumnsCreateNewColumn);
                }
            }
        }

        setPageComplete(pageComplete);
    }

    private boolean checkForMismatchedDatatypes(String currentComboText, int currentIndex) {
        return !currentComboText.equals(Messages.FixContentWizard_assignColumnsCreateNewColumn)
                && !contentStrategy.checkForCorrectDataType(currentComboText, currentIndex);
    }

    private boolean comboBoxIsColumn(String columnComboText) {
        return !(columnComboText.equals(Messages.FixContentWizard_assignColumnsCreateNewColumn));
    }

    /**
     * Returns a list containing all column numbers that are currently not assigned to
     * {@code ContentAttribute}s (beginning with 1). An empty list will be returned if all columns
     * are assigned, {@code null} is never returned.
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
                currentlyNotAssignedColumns.add(Integer.valueOf(i));
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

    public boolean isFillNewColumnsWithNull() {
        return fillNewColumnsWithNull != null & fillNewColumnsWithNull.isChecked();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Assures that the first {@link Combo} of the {@link AssignContentAttributesPage} has focus
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
