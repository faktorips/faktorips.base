/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.SupertypeHierarchyPartsContentProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.ITypePart;

/**
 * A checkbox filter dialog, to filter Model Descriptions by selected description types.
 */
public class FilterDescriptionsByTypeDialog extends CheckedTreeSelectionDialog {

    private static final String DEFAULT_MODEL_DESCRIPTION_PAGE_FILTER_DESCRIPTIONS_BY_TYPE_ACTION_IS_CHECKED = "DefaultModelDescriptionPage.FilterDescriptionsByTypeAction.isChecked"; //$NON-NLS-1$

    private static final List<Class<? extends ITypePart>> POSSIBLE_SELECTIONS = List.of(IAssociation.class,
            IAttribute.class,
            IMethod.class, ITableStructureUsage.class, IValidationRule.class);

    private int width;

    /**
     * The height of the UI tree widget showing the available {@link IIpsObjectPartContainer}.
     */
    private int height;

    /**
     * @param parent The parent {@link Shell} to show this dialog in.
     * @param contentProvider A {@link SupertypeHierarchyPartsContentProvider} providing this dialog
     *            with available {@link IIpsObjectPart}s.
     */
    public FilterDescriptionsByTypeDialog(Shell parent, FilterDescriptionByTypeDialogContentProvider contentProvider) {
        super(parent, new TypeLabelProvider(), contentProvider);
        setSize(5, 9);
        setContainerMode(false);
        setMessage(null);
        setInput(new Object());
    }

    /**
     * Sets the size of the tree in unit of characters.
     * 
     * @param width the width of the tree.
     * @param height the height of the tree.
     */
    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        GridData gd = null;

        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);

        Label messageLabel = createMessageArea(composite);
        if (messageLabel != null) {
            gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            gd.horizontalSpan = 0;
            messageLabel.setLayoutData(gd);
        }

        Composite inner = new Composite(composite, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 1;
        innerLayout.marginHeight = 0;
        innerLayout.marginWidth = 10;
        inner.setLayout(innerLayout);
        inner.setFont(parent.getFont());

        CheckboxTreeViewer treeViewer = createTreeViewer(inner);

        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(width);
        gd.heightHint = convertHeightInCharsToPixels(height);
        treeViewer.getControl().setLayoutData(gd);

        Composite buttonComposite = createSelectionButtons(inner);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        buttonComposite.setLayoutData(gd);

        gd = new GridData(GridData.FILL_BOTH);
        inner.setLayoutData(gd);

        gd = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(gd);

        applyDialogFont(composite);

        return composite;
    }

    @Override
    protected CheckboxTreeViewer createTreeViewer(Composite composite) {
        initializeDialogUnits(composite);
        ViewForm pane = new ViewForm(composite, SWT.NONE);

        CheckboxTreeViewer treeViewer = super.createTreeViewer(pane);
        pane.setContent(treeViewer.getControl());
        GridLayout paneLayout = new GridLayout();
        paneLayout.marginHeight = 0;
        paneLayout.marginWidth = 0;
        paneLayout.numColumns = 0;
        pane.setLayout(paneLayout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(10);
        gd.heightHint = convertHeightInCharsToPixels(6);
        pane.setLayoutData(gd);

        treeViewer.expandAll();
        treeViewer.getTree().setFocus();

        return treeViewer;
    }

    @Override
    protected Composite createSelectionButtons(Composite composite) {
        Composite buttonComposite = super.createSelectionButtons(composite);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 2;
        layout.numColumns = 2;
        buttonComposite.setLayout(layout);

        return buttonComposite;
    }

    @Override
    public int open() {
        this.setInitialElementSelections(getSavedParts());
        return super.open();
    }

    @Override
    protected void okPressed() {
        setSelections(POSSIBLE_SELECTIONS, false);
        super.okPressed();
        setSelections(this.getSelectedParts(), true);
    }

    private void setSelections(List<Class<? extends ITypePart>> selections, boolean status) {
        for (Class<? extends ITypePart> selection : selections) {
            getPreferenceStore()
                    .setValue(DEFAULT_MODEL_DESCRIPTION_PAGE_FILTER_DESCRIPTIONS_BY_TYPE_ACTION_IS_CHECKED
                            + selection.getName(), status);
        }
    }

    /**
     * @return the parts the user has selected via checkbox.
     */
    @SuppressWarnings("unchecked")
    public List<Class<? extends ITypePart>> getSelectedParts() {
        List<Class<? extends ITypePart>> partsSelected = new ArrayList<>();
        Object[] selectedParts = getResult();
        if (selectedParts == null) {
            return List.of();
        }
        for (Object part : selectedParts) {
            partsSelected.add((Class<? extends ITypePart>)part);
        }
        return partsSelected;
    }

    /**
     * @return The parts which were selected and were saved in store
     */
    public List<Class<? extends ITypePart>> getSavedParts() {
        return POSSIBLE_SELECTIONS.stream().filter(this::isPartInStore).collect(Collectors.toList());
    }

    private boolean isPartInStore(Class<? extends ITypePart> part) {
        return getPreferenceStore()
                .getBoolean(DEFAULT_MODEL_DESCRIPTION_PAGE_FILTER_DESCRIPTIONS_BY_TYPE_ACTION_IS_CHECKED
                        + part.getName());
    }

    private IPreferenceStore getPreferenceStore() {
        return IpsPlugin.getDefault().getPreferenceStore();
    }

    private static class TypeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            if (IAssociation.class.equals(element)) {
                return Messages.FilterDescriptionsByTypeDialog_getTextForAssociation;
            }
            if (IValidationRule.class.equals(element)) {
                return Messages.FilterDescriptionsByTypeDialog_getTextForRule;
            }
            if (IAttribute.class.equals(element)) {
                return Messages.FilterDescriptionsByTypeDialog_getTextForAttribute;
            }
            if (ITableStructureUsage.class.equals(element)) {
                return Messages.FilterDescriptionsByTypeDialog_getTextForTable;
            }
            if (IMethod.class.equals(element)) {
                return Messages.FilterDescriptionsByTypeDialog_getTextForMethod;
            }
            return null;

        }
    }

}
