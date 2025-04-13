/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.inputformat.GregorianCalendarFormat;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyContentProvider;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeStatus;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.runtime.MessageList;

/**
 * Wizard page allowing users to update the "Valid-From" date and optionally change generation IDs
 * for selected product components.
 */
public class UpdateValidFromSourcePage extends WizardPage {

    static final String PAGE_ID = "updateValidFrom.sourcePage"; //$NON-NLS-1$
    private static final String PAGE_TITLE = Messages.UpdateValidFromSourcePage_pageTitle;
    private static final int COLUMN_WIDTH_NAME = 300;
    private static final int COLUMN_WIDTH_VALID_FROM = 200;
    private static final int COLUMN_WIDTH_GENERATION_ID = 200;

    private DateControl newValidFromControl;
    private Text newVersionIdField;

    private Checkbox changeGenerationIDCheckbox;
    private Checkbox changeAttributesCheckbox;

    private CheckboxTreeViewer treeViewer;
    private DeepCopyContentProvider contentProvider;

    private BindingContext bindingContext;

    public UpdateValidFromSourcePage(String pageName) {
        super(pageName);
        setTitle(PAGE_TITLE);
        setDescription(Messages.UpdateValidFromSourcePage_description);
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite root = createRootComposite(parent, toolkit);

        addSourceAndTargetInputGroups(root, toolkit);
        addTreeViewer(root, toolkit);
        setControl(root);
        refreshVersionId();
        initBindingsAndRefresh();
        setPageComplete(false);
    }

    /** Creates the root container composite. */
    private Composite createRootComposite(Composite parent, UIToolkit toolkit) {
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return root;
    }

    /** Adds both source and target (new) product data entry sections. */
    private void addSourceAndTargetInputGroups(Composite root, UIToolkit toolkit) {
        Composite container = toolkit.createComposite(root);
        container.setLayout(new GridLayout(2, true));
        container.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        createSourceGroup(container, toolkit);
        createTargetGroup(container, toolkit);
    }

    /** Displays current product component's generation ID and valid-from. */
    private void createSourceGroup(Composite parent, UIToolkit toolkit) {
        Group group = toolkit.createGroup(parent, Messages.UpdateValidFromSourcePage_sourceGroup);
        Composite composite = toolkit.createLabelEditColumnComposite(group);
        ((GridLayout)composite.getLayout()).numColumns = 3;

        IProductCmpt rootCmpt = getPresentationModel().getStructure().getRoot().getProductCmpt();
        addReadOnlyLabel(toolkit, composite, Messages.UpdateValidFromSourcePage_validFrom,
                IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(rootCmpt.getValidFrom().getTime()));
        addReadOnlyLabel(toolkit, composite, Messages.UpdateValidFromSourcePage_generationID,
                rootCmpt.getVersionId());
    }

    /** Displays input fields for new valid-from and generation ID. */
    private void createTargetGroup(Composite parent, UIToolkit toolkit) {
        Group group = toolkit.createGroup(parent, Messages.UpdateValidFromSourcePage_targetGroup);
        Composite composite = toolkit.createLabelEditColumnComposite(group);

        toolkit.createLabel(composite, Messages.UpdateValidFromSourcePage_validFrom);
        newValidFromControl = new DateControl(composite, toolkit);

        toolkit.createLabel(composite, Messages.UpdateValidFromSourcePage_generationID);
        newVersionIdField = toolkit.createText(composite);

        changeGenerationIDCheckbox = toolkit.createCheckbox(composite,
                Messages.UpdateValidFromSourcePage_changeGenerationIDCheckbox);
        changeGenerationIDCheckbox.setChecked(true);
        GridData genIdCheckboxLayout = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        genIdCheckboxLayout.horizontalSpan = 2;
        changeGenerationIDCheckbox.setLayoutData(genIdCheckboxLayout);

        changeAttributesCheckbox = toolkit.createCheckbox(composite,
                Messages.UpdateValidFromSourcePage_ChangeAttributesCheckbox);
        changeAttributesCheckbox.setChecked(false);
        GridData attrCheckboxLayout = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        attrCheckboxLayout.horizontalSpan = 2;
        changeAttributesCheckbox.setLayoutData(attrCheckboxLayout);
    }

    /** Helper method to add labels for display-only fields. */
    private void addReadOnlyLabel(UIToolkit toolkit, Composite parent, String label, String value) {
        toolkit.createLabel(parent, label);
        Label valueLabel = toolkit.createLabel(parent, value);
        toolkit.createVerticalSpacer(parent, valueLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }

    /** Builds the tree view and its structure. */
    private void addTreeViewer(Composite root, UIToolkit toolkit) {
        toolkit.createHorizonzalLine(root);

        treeViewer = new CheckboxTreeViewer(root, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        treeViewer.setUseHashlookup(true);
        contentProvider = new DeepCopyContentProvider(true, false);
        treeViewer.setContentProvider(contentProvider);

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.horizontalSpan = 2;
        treeViewer.getControl().setLayoutData(layoutData);

        createColumns();
        treeViewer.setInput(getStructure());

        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);

        TreeViewerEditor.create(treeViewer, new ColumnViewerEditorActivationStrategy(treeViewer), SWT.NONE);
    }

    /** Initializes columns for tree viewer. */
    private void createColumns() {
        ColumnViewerToolTipSupport.enableFor(treeViewer);

        addColumn(Messages.UpdateValidFromSourcePage_productComponent, COLUMN_WIDTH_NAME, cell -> {
            var labelProvider = new UpdateValidFromLabelProvider(getPresentationModel());
            cell.setText(labelProvider.getOldName(cell.getElement()));
            cell.setImage(labelProvider.getObjectImage(cell.getElement(), false));
        });

        addColumn(Messages.UpdateValidFromSourcePage_validFrom, COLUMN_WIDTH_VALID_FROM, cell -> {
            var labelProvider = new UpdateValidFromLabelProvider(getPresentationModel());
            cell.setText(labelProvider.getValidFrom(cell.getElement()));
        });

        addColumn(Messages.UpdateValidFromSourcePage_generationID, COLUMN_WIDTH_GENERATION_ID, cell -> {
            var labelProvider = new UpdateValidFromLabelProvider(getPresentationModel());
            cell.setText(labelProvider.getGenerationID(cell.getElement()));
        });
    }

    /** Helper to abstract TreeViewer column creation. */
    private void addColumn(String title, int width, Consumer<ViewerCell> updater) {
        TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE);
        column.getColumn().setText(title);
        column.getColumn().setWidth(width);
        column.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                updater.accept(cell);
            }
        });
    }

    /** Triggers after property updates. Controls whether page is complete. */
    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        setPageComplete(true);
    }

    /** Sets up all bindings and refreshes the UI. */
    private void initBindingsAndRefresh() {
        treeViewer.expandAll();
        bindingContext = new BindingContext();

        ChangeListener listener = new ChangeListener();
        getPresentationModel().addPropertyChangeListener(listener);
        getTreeStatus().addPropertyChangeListener(listener);

        bindingContext.bindContent(
                new DateControlField<>(newValidFromControl, GregorianCalendarFormat.newInstance(), false),
                getPresentationModel(), UpdateValidfromPresentationModel.NEW_VALID_FROM);

        bindingContext.bindContent(new TextField(newVersionIdField), getPresentationModel(),
                UpdateValidfromPresentationModel.NEW_VERSION_ID);

        bindingContext.bindContent(changeGenerationIDCheckbox, getPresentationModel(),
                UpdateValidfromPresentationModel.CHANGE_GENERATION_ID);
        bindingContext.bindContent(changeAttributesCheckbox, getPresentationModel(),
                UpdateValidfromPresentationModel.CHANGE_ATTRIBUTES);

        treeViewer.addCheckStateListener(event -> {
            if (event.getElement() instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference reference = (IProductCmptStructureReference)event.getElement();
                boolean isChecked = event.getChecked();
                getTreeStatus().setChecked(reference, isChecked);

                if (!isChecked) {
                    uncheckAllChildren(reference);
                }
            }
        });

        treeViewer.addTreeListener(new ITreeViewerListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent e) {
                updateAfterExpansion((IProductCmptStructureReference)e.getElement(), false);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent e) {
                /* nothing */ }
        });

        bindingContext.updateUI();

        getShell().getDisplay().asyncExec(() -> {
            if (!newValidFromControl.isDisposed()) {
                newValidFromControl.setFocus();
                refreshPageAfterValueChange();
                treeViewer.refresh(true);
            }
        });
    }

    private void uncheckAllChildren(IProductCmptStructureReference parent) {
        for (IProductCmptStructureReference child : parent.getChildren()) {
            getTreeStatus().setChecked(child, false);
            uncheckAllChildren(child);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bindingContext != null) {
            bindingContext.dispose();
        }
    }

    /** Refreshes tree and performs validation. */
    private void refreshPageAfterValueChange() {
        treeViewer.refresh(true);
        updateCheckedAndGrayedStatus(getStructure());
        validate();
        updatePageComplete();
    }

    /** Validates user inputs and sets error message if necessary. */
    private void validate() {
        setErrorMessage(null);
        setMessage(null);

        UpdateValidfromPresentationModel model = getPresentationModel();

        if (model.getNewValidFrom() == null) {
            setErrorMessage(Messages.UpdateValidFromSourcePage_emptyValidFomDateError);
            return;
        }

        if (model.isChangeGenerationId() && StringUtils.isBlank(model.getNewVersionId())) {
            setErrorMessage(Messages.UpdateValidFromSourcePage_emptyVersionIdError);
            return;
        }

        if (model.getStructure() == null) {
            setErrorMessage(Messages.UpdateValidFromSourcePage_missingStructureError);
            return;
        }
        validateWorkingDate();
        if (getErrorMessage() != null) {
            return;
        }

        IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
        if (namingStrategy != null && namingStrategy.supportsVersionId() && model.isChangeGenerationId()) {
            MessageList validation = namingStrategy.validateVersionId(model.getNewVersionId());
            if (validation.containsErrorMsg()) {
                setErrorMessage(validation.getMessage(0).getText());
                return;
            }
        }

        setPageComplete(true);
    }

    /** Validates format of the entered date. */
    private void validateWorkingDate() {
        Calendar calendar = getPresentationModel().getNewValidFrom();
        if (calendar == null) {
            String pattern;
            DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
            if (dateFormat instanceof SimpleDateFormat) {
                pattern = ((SimpleDateFormat)dateFormat).toLocalizedPattern();
            } else {
                pattern = "\"" + dateFormat.format(new GregorianCalendar().getTime()) + "\""; // NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
            }
            setErrorMessage(NLS.bind(Messages.UpdateValidFromSourcePage_ValidFromDateFormatError, pattern));
        }
    }

    private IProductCmptNamingStrategy getNamingStrategy() {
        if (getPresentationModel() == null) {
            return null;
        }
        return getPresentationModel().getIpsProject().getProductCmptNamingStrategy();
    }

    /** Suggests a new version ID based on date. */
    void refreshVersionId() {
        if (getNamingStrategy() != null && getNamingStrategy().supportsVersionId()) {
            GregorianCalendar validFrom = getPresentationModel().getNewValidFrom();
            if (validFrom == null) {
                return;
            }
            String newVersionId = getNamingStrategy().getNextVersionId(getStructure().getRoot().getProductCmpt(),
                    validFrom);
            if (!newVersionId.equals(getPresentationModel().getNewVersionId())) {
                getPresentationModel().setNewVersionId(newVersionId);
            }
        }
    }

    /**
     * Updates check/grayed state when a node is expanded.
     *
     * @param ref The expanded reference.
     * @param recursive If true, updates all children recursively.
     */
    private void updateAfterExpansion(IProductCmptStructureReference ref, boolean recursive) {
        for (IProductCmptStructureReference child : ref.getChildren()) {
            treeViewer.setChecked(child, getTreeStatus().isChecked(child));
            treeViewer.setGrayed(child, !getTreeStatus().isEnabled(child));
            if (recursive) {
                updateAfterExpansion(child, true);
            }
        }
    }

    /**
     * Updates the checked and grayed state of the entire tree structure.
     *
     * @param structure The product component tree structure to update.
     */
    public void updateCheckedAndGrayedStatus(IProductCmptTreeStructure structure) {
        updateCheckedAndGrayStatus(structure.getRoot());
    }

    /**
     * Recursively updates the checked and grayed state of a tree node.
     *
     * @param ref The node to evaluate.
     * @return true if the node or any of its children are grayed.
     */
    private boolean updateCheckedAndGrayStatus(IProductCmptStructureReference ref) {
        if (ref instanceof IProductCmptTypeAssociationReference assocRef &&
                (assocRef.getChildren().length == 0 || assocRef.getAssociation().isAssoziation())) {
            return false;
        }

        boolean hasGrayChild = false;
        for (IProductCmptStructureReference child : ref.getChildren()) {
            hasGrayChild |= updateCheckedAndGrayStatus(child);
        }

        boolean checked = getTreeStatus().isChecked(ref);
        boolean grayed = hasGrayChild || !checked;

        treeViewer.setChecked(ref, checked);
        treeViewer.setGrayed(ref, grayed || !getTreeStatus().isEnabled(ref));
        return grayed;
    }

    /**
     * Returns the current tree status (selection and state of all nodes).
     */
    public DeepCopyTreeStatus getTreeStatus() {
        return getPresentationModel().getTreeStatus();
    }

    private IProductCmptTreeStructure getStructure() {
        return getPresentationModel().getStructure();
    }

    private UpdateValidfromPresentationModel getPresentationModel() {
        return getWizard().getPresentationModel();
    }

    private void updateVersionIdFieldEnabledState() {
        boolean isEnabled = changeGenerationIDCheckbox.isChecked();
        newVersionIdField.setEnabled(isEnabled);
    }

    private void refreshTreeColumnGenerationId() {
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            // Only refresh Generation ID column, which is column index 2
            TreeItem[] items = treeViewer.getTree().getItems();
            for (TreeItem item : items) {
                refreshColumnRecursive(item, 2);
            }
        }
    }

    /**
     * Recursively updates a specific column in the tree.
     *
     * @param item The root tree item to start from.
     * @param columnIndex The column index to refresh.
     */
    private void refreshColumnRecursive(TreeItem item, int columnIndex) {
        treeViewer.update(item.getData(), null);
        for (TreeItem child : item.getItems()) {
            refreshColumnRecursive(child, columnIndex);
        }
    }

    @Override
    public IpsUpdateValidfromWizard getWizard() {
        return (IpsUpdateValidfromWizard)super.getWizard();
    }

    /**
     * Listener that reacts to changes in the presentation model and triggers UI updates
     * accordingly.
     */
    private class ChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case UpdateValidfromPresentationModel.NEW_VALID_FROM -> {
                    refreshVersionId();
                    refreshPageAfterValueChange();
                }
                case UpdateValidfromPresentationModel.NEW_VERSION_ID -> {
                    refreshTreeColumnGenerationId();
                    refreshPageAfterValueChange();
                }
                case UpdateValidfromPresentationModel.CHANGE_GENERATION_ID -> {
                    updateVersionIdFieldEnabledState();
                    refreshTreeColumnGenerationId();
                    refreshPageAfterValueChange();
                }
                case LinkStatus.CHECKED -> refreshPageAfterValueChange();
                default -> {
                    /* other properties not handled */ }
            }
        }
    }
}
