/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;

/**
 * Class that creates the GUI elements for a {@link SubsetChooserViewer}.
 * 
 * @author Thorsten Guenther, Stefan Widmaier
 */
public class SubsetChooserBuilder {

    private Label sourceLabel;
    private Label targetLabel;

    private UIToolkit toolkit;
    private Table preDefinedValuesTable;
    private Table resultingValuesTable;
    private Button addSelected;
    private Button addAll;
    private Button removeSelected;
    private Button removeAll;
    private Button up;
    private Button down;
    private Composite mainComposite;

    public void createUI(Composite parent, UIToolkit toolkit) {
        this.toolkit = toolkit;

        mainComposite = toolkit.createComposite(parent);
        mainComposite.setLayout(new GridLayout(4, false));
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        sourceLabel = toolkit.createLabel(mainComposite, Messages.ListChooser_labelAvailableValues);
        GridData srcLabelLayout = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
        srcLabelLayout.horizontalSpan = 2;
        srcLabelLayout.widthHint = 100;
        sourceLabel.setLayoutData(srcLabelLayout);
        targetLabel = toolkit.createLabel(mainComposite, Messages.ListChooser_lableChoosenValues);
        GridData targetLabelLayout = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
        targetLabelLayout.horizontalSpan = 2;
        targetLabelLayout.widthHint = 100;
        targetLabel.setLayoutData(targetLabelLayout);

        TableLayoutComposite srcParent = new TableLayoutComposite(mainComposite, SWT.NONE);
        GridData srcCompositeLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
        srcParent.setLayoutData(srcCompositeLayout);
        preDefinedValuesTable = new Table(srcParent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        GridData srcTableLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
        preDefinedValuesTable.setLayoutData(srcTableLayout);
        // message image
        srcParent.addColumnData(new ColumnPixelData(15, false));
        srcParent.addColumnData(new ColumnWeightData(95, true));

        addChooseButtons(mainComposite);

        TableLayoutComposite targetParent = new TableLayoutComposite(mainComposite, SWT.NONE);
        GridData targetCompositeLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
        targetParent.setLayoutData(targetCompositeLayout);
        resultingValuesTable = new Table(targetParent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        GridData targetTableLayout = new GridData(SWT.FILL, SWT.FILL, true, true);
        targetTableLayout.horizontalIndent = 5;
        resultingValuesTable.setLayoutData(targetTableLayout);
        // message image
        targetParent.addColumnData(new ColumnPixelData(15, false));
        targetParent.addColumnData(new ColumnWeightData(95, true));

        addMoveButtons(mainComposite);
    }

    /**
     * Sets the source section's label text.
     */
    public void setSourceLabel(String label) {
        sourceLabel.setText(label);
    }

    /**
     * Sets the targets section's label text.
     */
    public void setTargetLabel(String label) {
        targetLabel.setText(label);
    }

    /**
     * Add the buttons to take a value from left to right or vice versa.
     */
    private void addChooseButtons(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, false, true);
        layoutData.horizontalIndent = 5;
        root.setLayoutData(layoutData);

        addSelected = toolkit.createButton(root, ">"); //$NON-NLS-1$
        removeSelected = toolkit.createButton(root, "<"); //$NON-NLS-1$
        addAll = toolkit.createButton(root, ">>"); //$NON-NLS-1$
        removeAll = toolkit.createButton(root, "<<"); //$NON-NLS-1$

        addSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        removeSelected.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        addAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        removeAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    }

    /**
     * Add the buttons to move a value in the target list up or down.
     */
    private void addMoveButtons(Composite parent) {
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, true));

        up = toolkit.createButton(root, Messages.ListChooser_buttonUp);
        down = toolkit.createButton(root, Messages.ListChooser_buttonDown);

        up.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        down.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    public Label getSourceLabel() {
        return sourceLabel;
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    public Table getPreDefinedValuesTable() {
        return preDefinedValuesTable;
    }

    public Table getResultingValuesTable() {
        return resultingValuesTable;
    }

    public Button getAddSelectedButton() {
        return addSelected;
    }

    public Button getAddAllButton() {
        return addAll;
    }

    public Button getRemoveSelectedButton() {
        return removeSelected;
    }

    public Button getRemoveAllButton() {
        return removeAll;
    }

    public Button getUpButton() {
        return up;
    }

    public Button getDownButton() {
        return down;
    }

    public Composite getChooserComposite() {
        return mainComposite;
    }

}
