/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRefField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRootRefField;
import org.faktorips.devtools.core.ui.controller.fields.StructuredViewerField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.inputformat.GregorianCalendarFormat;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDateViewer;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeSettingsOperation.DeepCopyTreeLoadSettingsOperation;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyTreeSettingsOperation.DeepCopyTreeSaveSettingsOperation;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * Page to let the user select products related to each other.
 *
 * @author Thorsten Guenther
 */
public class SourcePage extends WizardPage {

    static final String PAGE_ID = "deepCopyWizard.source"; //$NON-NLS-1$

    private CheckboxTreeViewer tree;

    private CLabel deactivationHint;
    private GridData deactivationHintData;

    // Control for search pattern
    private Text searchInput;
    // Control for replace text
    private Text replaceInput;

    // The input field for the user to enter a version id to be used for all newly created product
    // components.
    private Text versionId;

    // Controls
    private DateControl newWorkingDateControl;
    private IpsPckFragmentRootRefControl targetPackRootControl;
    private IpsPckFragmentRefControl targetPackageControl;

    // The type of the deep copy wizard (see DeepCopyWizard):
    // DeepCopyWizard.TYPE_COPY_PRODUCT or TYPE_NEW_VERSION
    private final int type;

    private Checkbox copyTableContentsBtn;

    private Checkbox copyExistingGenerationsBtn;

    private DeepCopyContentProvider contentProvider;

    private boolean isRefreshing = false;

    private CopyLinkEditingSupport copyLinkColumneditingSupport;

    private GenerationDateViewer generationDateViewer;

    private BindingContext bindingContext;

    /**
     * Creates a new page to select the objects to copy.
     */
    protected SourcePage(int type) {
        super(PAGE_ID);
        this.type = type;

        String descr = null;
        if (type == DeepCopyWizard.TYPE_NEW_VERSION) {
            String versionConcept = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                    .getVersionConceptNameSingular();
            descr = NLS.bind(Messages.SourcePage_description, versionConcept);
        } else {
            descr = Messages.SourcePage_description_copy;
        }
        super.setDescription(descr);

        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        if (getStructure() == null) {
            Label errormsg = new Label(parent, SWT.WRAP);
            GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
            errormsg.setLayoutData(layoutData);
            errormsg.setText(Messages.SourcePage_msgCircleRelation);
            setControl(errormsg);
            return;
        }

        setTitle(getTitle());

        UIToolkit toolkit = new UIToolkit(null);
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite masterCopyComposite = toolkit.createComposite(root);
        masterCopyComposite.setLayout(new GridLayout(2, true));
        masterCopyComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Group originGroup = toolkit.createGroup(masterCopyComposite, Messages.SourcePage_copyFrom);
        Composite masterComposite = toolkit.createLabelEditColumnComposite(originGroup);
        ((GridLayout)masterComposite.getLayout()).numColumns = 3;
        Group copyGroup = toolkit.createGroup(masterCopyComposite, Messages.SourcePage_copyTo);
        Composite copyComposite = toolkit.createLabelEditColumnComposite(copyGroup);

        String changeOverTimeLabel = NLS.bind(Messages.ReferenceAndPreviewPage_labelVersionId, IpsPlugin.getDefault()
                .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());

        int[] height = new int[5];
        // create content in copy group
        // first create right group to get correct heights
        toolkit.createLabel(copyComposite, Messages.ReferenceAndPreviewPage_labelValidFrom);
        newWorkingDateControl = new DateControl(copyComposite, toolkit);
        height[0] = newWorkingDateControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        toolkit.createVerticalSpacer(copyComposite, 25);
        toolkit.createVerticalSpacer(copyComposite, 25);
        height[1] = 25;

        toolkit.createLabel(copyComposite, changeOverTimeLabel);
        versionId = toolkit.createText(copyComposite);
        height[2] = versionId.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        toolkit.createLabel(copyComposite, Messages.SourcePage_labelTargetRoot);
        targetPackRootControl = toolkit.createPdPackageFragmentRootRefControl(copyComposite, true);
        height[3] = targetPackRootControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        toolkit.createLabel(copyComposite, Messages.ReferenceAndPreviewPage_labelTargetPackage);
        targetPackageControl = toolkit.createPdPackageFragmentRefControl(getPresentationModel().getTargetPackageRoot(),
                copyComposite);
        height[4] = targetPackageControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        toolkit.createLabel(copyComposite, Messages.SourcePage_tables);
        copyTableContentsBtn = toolkit.createCheckbox(copyComposite,
                Messages.SourcePage_labelRadioBtnCopyTableContents);
        copyTableContentsBtn.setChecked(true);

        // create content in origin group
        createOldValidFromControl(toolkit, masterComposite);
        toolkit.createVerticalSpacer(masterComposite, height[0]);

        IProductCmpt rootProductCmpt = getPresentationModel().getStructure().getRoot().getProductCmpt();
        String masterVersionId = ""; //$NON-NLS-1$
        try {
            masterVersionId = rootProductCmpt.getVersionId();
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }

        String generationConceptNamePlural = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(true);
        String generationConceptNamePluralStartOfSentence = IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(false);
        toolkit.createLabel(masterComposite, ""); //$NON-NLS-1$
        String labelCopyExistingGenerations = NLS.bind(Messages.SourcePage_labelCheckboxCopyExistingGenerations,
                generationConceptNamePlural);
        copyExistingGenerationsBtn = toolkit.createCheckbox(masterComposite, labelCopyExistingGenerations);
        copyExistingGenerationsBtn.setChecked(false);
        copyExistingGenerationsBtn.setToolTipText(NLS.bind(Messages.SourcePage_tooltipCheckboxCopyExistingGenerations,
                generationConceptNamePlural, generationConceptNamePluralStartOfSentence));
        toolkit.createVerticalSpacer(masterComposite, height[1]);

        createLabel(toolkit, masterComposite, changeOverTimeLabel, masterVersionId, height[2]);

        createLabel(toolkit, masterComposite, Messages.SourcePage_labelTargetRoot, rootProductCmpt
                .getIpsPackageFragment().getRoot().getCorrespondingResource().getWorkspaceRelativePath().toString()
                .substring(1),
                height[3]);

        createLabel(toolkit, masterComposite, Messages.ReferenceAndPreviewPage_labelTargetPackage,
                rootProductCmpt.getIpsPackageFragment().getName(), height[4]);

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            Group searchReplaceGroup = toolkit.createGroup(root, Messages.SourcePage_searchAndReplace);
            ((GridData)searchReplaceGroup.getLayoutData()).horizontalSpan = 2;
            ((GridData)searchReplaceGroup.getLayoutData()).verticalAlignment = SWT.TOP;
            ((GridData)searchReplaceGroup.getLayoutData()).grabExcessVerticalSpace = false;
            Composite searchReplaceComposite = toolkit.createLabelEditColumnComposite(searchReplaceGroup);

            toolkit.createLabel(searchReplaceComposite, Messages.ReferenceAndPreviewPage_labelSearchPattern);
            searchInput = toolkit.createText(searchReplaceComposite);

            toolkit.createLabel(searchReplaceComposite, Messages.ReferenceAndPreviewPage_labelReplacePattern);
            replaceInput = toolkit.createText(searchReplaceComposite);
        }

        Label horizonzalLine = toolkit.createHorizonzalLine(root);
        ((GridData)horizonzalLine.getLayoutData()).horizontalSpan = 2;

        deactivationHint = new CLabel(root, SWT.NONE);
        String deactivationHintText = NLS.bind(Messages.SourcePage_deactivationHintText, generationConceptNamePlural);
        deactivationHint.setText(deactivationHintText);
        deactivationHint.setImage(
                IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(Severity.INFO), false));
        deactivationHintData = new GridData();
        deactivationHintData.exclude = true;
        deactivationHint.setLayoutData(deactivationHintData);
        deactivationHint.setVisible(false);

        tree = new CheckboxTreeViewer(root, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tree.setUseHashlookup(true);
        contentProvider = new DeepCopyContentProvider(true, false);
        tree.setContentProvider(contentProvider);
        GridData treeLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeLayoutData.horizontalSpan = 2;
        tree.getControl().setLayoutData(treeLayoutData);
        createColumns();

        tree.setInput(getStructure());

        createLoadAndSaveButtons(toolkit, root);

        refreshVersionId();

        initBindingsAndRefresh();

    }

    private void createLoadAndSaveButtons(UIToolkit toolkit, Composite root) {
        Composite loadSaveComposite = toolkit.createComposite(root);
        loadSaveComposite.setLayout(new GridLayout(2, true));
        loadSaveComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
        Button saveButton = toolkit.createButton(loadSaveComposite, Messages.SourcePage_save_button);
        saveButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Export.gif", true)); //$NON-NLS-1$
        saveButton.addSelectionListener(new DeepCopyTreeSaveSettingsOperation(getPresentationModel()));
        Button loadButton = toolkit.createButton(loadSaveComposite, Messages.SourcePage_load_button);
        loadButton.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Import.gif", true)); //$NON-NLS-1$
        loadButton.addSelectionListener(new DeepCopyTreeLoadSettingsOperation(getPresentationModel()));
    }

    @Override
    public String getTitle() {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return NLS.bind(Messages.SourcePage_title,
                    getPresentationModel().getStructure().getRoot().getProductCmpt().getName());
        } else {
            return NLS.bind(Messages.SourcePage_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        }
    }

    private Label createLabel(UIToolkit toolkit,
            Composite parent,
            String descriptionLabel,
            String text,
            int maxHeight) {
        toolkit.createLabel(parent, descriptionLabel);
        Label label = toolkit.createLabel(parent, text);
        toolkit.createVerticalSpacer(parent, maxHeight);
        return label;
    }

    private void createOldValidFromControl(UIToolkit toolkit, Composite inputRoot) {
        toolkit.createLabel(inputRoot, Messages.SourcePage_labelSourceValidFrom);
        generationDateViewer = new GenerationDateViewer(inputRoot);
        IStructuredContentProvider generationContentProvider = new IStructuredContentProvider() {

            private Object[] collectElements;

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                collectElements = getPresentationModel().getGenerationDates().toArray();
            }

            @Override
            public void dispose() {
                // nothing to do
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return collectElements;
            }
        };
        generationDateViewer.setContentProvider(generationContentProvider);
        generationDateViewer.setInput(getPresentationModel().getStructure().getRoot().getProductCmpt());
        generationDateViewer.updateButtons();
    }

    protected void initBindingsAndRefresh() {
        tree.expandAll();
        createBindings();

        getShell().getDisplay().asyncExec(() -> {
            if (!newWorkingDateControl.isDisposed()) {
                newWorkingDateControl.setFocus();
                // run async to ensure that the buttons state (enabled/disabled)
                // can be updated
                refreshPageAfterValueChange();
                updateCheckedAndGrayedStatus(getStructure());
                updateColumnWidth();

                getWizard().getDeepCopyPreview().getErrorElements().clear();
                setErrorMessage(null);
                tree.refresh(true);
                setMessagePleaseEnterWorkingDate();
            }
        });
    }

    private void createBindings() {
        bindingContext = new BindingContext();
        ChangeListener propertyChangeListener = new ChangeListener();
        getPresentationModel().addPropertyChangeListener(propertyChangeListener);

        StructuredViewerField<GenerationDate> generationDateField = StructuredViewerField
                .newInstance(generationDateViewer, GenerationDate.class);
        bindingContext.bindContent(generationDateField, getPresentationModel(),
                DeepCopyPresentationModel.OLD_VALID_FROM);

        DateControlField<GregorianCalendar> newWorkingDateField = new DateControlField<>(
                newWorkingDateControl, GregorianCalendarFormat.newInstance(), false);
        bindingContext.bindContent(newWorkingDateField, getPresentationModel(),
                DeepCopyPresentationModel.NEW_VALID_FROM);

        IpsPckFragmentRootRefField packageRootField = new IpsPckFragmentRootRefField(targetPackRootControl);
        IpsPckFragmentRefField packageField = new IpsPckFragmentRefField(targetPackageControl);
        bindingContext.bindContent(packageRootField, getPresentationModel(),
                DeepCopyPresentationModel.TARGET_PACKAGE_ROOT);
        bindingContext.bindContent(packageField, getPresentationModel(), DeepCopyPresentationModel.TARGET_PACKAGE);

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            TextField searchInputField = new TextField(searchInput);
            TextField replaceInputField = new TextField(replaceInput);
            bindingContext.bindContent(searchInputField, getPresentationModel(),
                    DeepCopyPresentationModel.SEARCH_INPUT);
            bindingContext.bindContent(replaceInputField, getPresentationModel(),
                    DeepCopyPresentationModel.REPLACE_INPUT);

            searchInputField.setText(""); //$NON-NLS-1$
            replaceInputField.setText(""); //$NON-NLS-1$
        }
        TextField versionField = new TextField(versionId);
        bindingContext.bindContent(versionField, getPresentationModel(), DeepCopyPresentationModel.VERSION_ID);

        bindingContext.bindContent(copyTableContentsBtn, getPresentationModel(), DeepCopyPresentationModel.COPY_TABLE);
        bindingContext.bindContent(copyExistingGenerationsBtn, getPresentationModel(),
                DeepCopyPresentationModel.COPY_EXISTING_GENERATIONS);

        tree.addCheckStateListener(event -> {
            if (event.getElement() instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference reference = (IProductCmptStructureReference)event.getElement();
                getTreeStatus().setChecked(reference, event.getChecked());
            }
        });

        tree.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                IProductCmptStructureReference reference = (IProductCmptStructureReference)event.getElement();
                updateAfterExpansion(reference, false);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                // do nothing
            }
        });

        getTreeStatus().addPropertyChangeListener(propertyChangeListener);

        bindingContext.updateUI();
    }

    /**
     * An Element is grayed if not all children or itself is checked. That means, an element is also
     * gray if none of its children is checked or itself is unchecked.
     *
     */
    public void updateCheckedAndGrayedStatus(IProductCmptTreeStructure structure) {
        updateCheckedAndGrayStatus(structure.getRoot());
    }

    /**
     * updating the checked and grayed state of the specified element and returns the grayed state
     * for recursive use.
     *
     */
    private boolean updateCheckedAndGrayStatus(IProductCmptStructureReference reference) {
        if (reference instanceof IProductCmptTypeAssociationReference associationReference) {
            if (associationReference.getChildren().length == 0
                    || associationReference.getAssociation().isAssoziation()) {
                // these elements are not visible so should never change gray state
                return false;
            }
        }
        boolean atLeastOneChildIsGrayed = false;
        for (IProductCmptStructureReference child : reference.getChildren()) {
            boolean grayedChild = updateCheckedAndGrayStatus(child);
            atLeastOneChildIsGrayed = atLeastOneChildIsGrayed || grayedChild;
        }
        boolean thisIsChecked = getTreeStatus().isChecked(reference);
        boolean grayed = atLeastOneChildIsGrayed || !thisIsChecked;
        tree.setGrayed(reference, grayed || !getTreeStatus().isEnabled(reference));
        tree.setChecked(reference, getTreeStatus().isChecked(reference));
        return grayed;
    }

    /**
     * For any reason, the check state of collapsed elements is not updated. So we have to update
     * the correct state when expanding the tree
     */
    private void updateAfterExpansion(IProductCmptStructureReference reference, boolean recursive) {
        for (IProductCmptStructureReference child : reference.getChildren()) {
            boolean checked = getTreeStatus().isChecked(child);
            tree.setChecked(child, checked);
            tree.setGrayed(child, !getTreeStatus().isEnabled(child));
            if (recursive) {
                updateAfterExpansion(child, recursive);
            }
        }
    }

    public DeepCopyTreeStatus getTreeStatus() {
        return getPresentationModel().getTreeStatus();
    }

    private void setMessagePleaseEnterWorkingDate() {
        switch (type) {
            case DeepCopyWizard.TYPE_COPY_PRODUCT -> {
                String productCmptTypeName = IIpsModel.get().getMultiLanguageSupport()
                        .getLocalizedLabel(getStructure().getRoot()
                                .getProductCmpt()
                                .findProductCmptType(getStructure().getRoot().getProductCmpt().getIpsProject()));
                setDescription(NLS.bind(Messages.SourcePage_msgPleaseEnterNewWorkingDateNewCopy, productCmptTypeName));
            }
            case DeepCopyWizard.TYPE_NEW_VERSION -> {
                String versionConceptNameSingular = IpsPlugin.getDefault().getIpsPreferences()
                        .getChangesOverTimeNamingConvention().getVersionConceptNameSingular();
                setDescription(NLS.bind(Messages.SourcePage_msgPleaseEnterNewWorkingDateNewGeneration,
                        versionConceptNameSingular));
            }
        }
    }

    /*
     * Re-set the column width, the new name will be displayed completely the other columns width
     * will be resized depending on the windows size
     */
    private void updateColumnWidth() {
        TableLayout layout = new TableLayout();

        Point wizardSize = getWizard().getSize();
        GC gc = new GC(tree.getTree());
        gc.setFont(tree.getTree().getFont());
        // Note: we must extend the operation column size to display the full text of all elements
        // in the drop down, otherwise the drop down will not be activated after clicking the
        // column!
        int columnSizeOperation = gc.stringExtent(Messages.SourcePage_columnNameOperation).x + 30;
        int columnSizeNewName = gc.stringExtent(Messages.SourcePage_columnNameNewName).x + 20;
        for (String newName : getWizard().getDeepCopyPreview().getNewNames()) {
            int columnSizeNewNameCandidate = gc.stringExtent(newName).x + 40;
            columnSizeNewName = Math.max(columnSizeNewName, columnSizeNewNameCandidate);
        }
        gc.dispose();

        int columnSizeStructure = Math.max(wizardSize.x - columnSizeOperation - columnSizeNewName - 10,
                columnSizeNewName);

        layout.addColumnData(new ColumnPixelData(columnSizeStructure, true));
        layout.addColumnData(new ColumnPixelData(columnSizeOperation, true));
        layout.addColumnData(new ColumnPixelData(columnSizeNewName, true));
        tree.getTree().setLayout(layout);
        tree.getTree().layout(true);
    }

    /**
     * Create the columns for the receiver.
     */
    private void createColumns() {
        ColumnViewerToolTipSupport.enableFor(tree);

        // source product structure
        TreeViewerColumn columnSource = new TreeViewerColumn(tree, SWT.NONE);
        columnSource.getColumn().setResizable(true);
        columnSource.getColumn().setMoveable(false);
        columnSource.setLabelProvider(new DeepCopyLabelProvider(getWizard().getDeepCopyPreview()) {

            @Override
            public void update(ViewerCell cell) {
                cell.setText(getOldName(cell.getElement()));
                cell.setImage(getObjectImage(cell.getElement()));
            }

        });
        columnSource.getColumn().setText(Messages.SourcePage_columnNameSourceStructure);

        // column to define copy or link
        TreeViewerColumn copyOrLinkColumn = new TreeViewerColumn(tree, SWT.LEAD);
        copyOrLinkColumn.getColumn().setResizable(true);
        copyOrLinkColumn.getColumn().setMoveable(false);
        copyOrLinkColumn.setLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                IProductCmptStructureReference reference = (IProductCmptStructureReference)cell.getElement();
                if (getPresentationModel().getTreeStatus().isEnabled(reference)) {
                    cell.setText(getPresentationModel().getTreeStatus().getCopyOrLink(reference).toString());
                } else {
                    cell.setText(""); //$NON-NLS-1$
                }
            }

        });
        copyOrLinkColumn.getColumn().setText(Messages.SourcePage_columnNameOperation);
        copyLinkColumneditingSupport = new CopyLinkEditingSupport(tree, getPresentationModel().getTreeStatus());
        copyOrLinkColumn.setEditingSupport(copyLinkColumneditingSupport);

        // new product name
        TreeViewerColumn columnNewName = new TreeViewerColumn(tree, SWT.NONE);
        columnNewName.getColumn().setResizable(true);
        columnNewName.getColumn().setMoveable(false);
        columnNewName.setLabelProvider(new DeepCopyLabelProvider(getWizard().getDeepCopyPreview()) {

            @Override
            public void update(ViewerCell cell) {
                cell.setText(getNewName(cell.getElement()));
                cell.setImage(getErrorImage(cell.getElement()));
            }

        });
        columnNewName.getColumn().setText(Messages.SourcePage_columnNameNewName);

        tree.getTree().setLinesVisible(true);
        tree.getTree().setHeaderVisible(true);

        TreeViewerEditor.create(tree, new ColumnViewerEditorActivationStrategy(tree), SWT.NONE);
    }

    /**
     * Returns the package fragment which is to be used as target package for the copy.
     */
    public IIpsPackageFragment getTargetPackage() {
        return getPresentationModel().getTargetPackage();
    }

    public IIpsPackageFragmentRoot getIIpsPackageFragmentRoot() {
        return getPresentationModel().getTargetPackageRoot();
    }

    public String getVersionId() {
        return getPresentationModel().getVersionId();
    }

    /**
     * Returns the working date as a {@link GregorianCalendar}, <code>null</code> if no valid date
     * was entered by the user.
     */
    public GregorianCalendar getNewValidFrom() {
        return getPresentationModel().getNewValidFrom();
    }

    @Override
    public DeepCopyWizard getWizard() {
        return (DeepCopyWizard)super.getWizard();
    }

    private DeepCopyPresentationModel getPresentationModel() {
        if (getWizard() == null) {
            return null;
        }
        return getWizard().getPresentationModel();
    }

    private IProductCmptTreeStructure getStructure() {
        if (getPresentationModel() == null) {
            return null;
        }
        return getPresentationModel().getStructure();
    }

    private IProductCmptNamingStrategy getNamingStrategy() {
        if (getPresentationModel() == null) {
            return null;
        }
        return getPresentationModel().getIpsProject().getProductCmptNamingStrategy();
    }

    protected void refreshPageAfterValueChange() {
        refreshTree();
        validate();
        updatePageComplete();
    }

    /**
     * Refresh the structure in the tree and updates the version id
     */
    void refreshVersionId() {
        if (getNamingStrategy() != null && getNamingStrategy().supportsVersionId()) {
            GregorianCalendar validFrom = getPresentationModel().getNewValidFrom();
            if (validFrom == null) {
                return;
            }
            String newVersionId = getNamingStrategy().getNextVersionId(getStructure().getRoot().getProductCmpt(),
                    validFrom);
            if (!newVersionId.equals(getPresentationModel().getVersionId())) {
                getPresentationModel().setVersionId(newVersionId);
            }
        }
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        setPageComplete(getPresentationModel().getTargetPackageRoot() != null);
    }

    // CSOFF: CyclomaticComplexity
    private void validate() {
        setMessage(null);
        setErrorMessage(null);

        if (getPresentationModel().getAllCopyElements(false).size() == 0) {
            // no elements checked
            setErrorMessage(Messages.SourcePage_msgNothingSelected);
            return;
        }

        validateWorkingDate();
        if (getErrorMessage() != null) {
            return;
        }

        if (getNamingStrategy() != null && getNamingStrategy().supportsVersionId()) {
            MessageList ml = getNamingStrategy().validateVersionId(versionId.getText());
            if (!ml.isEmpty()) {
                setErrorMessage(ml.getMessage(0).getText());
                return;
            }
        }

        if (getStructure() == null) {
            setErrorMessage(Messages.SourcePage_msgCircleRelationShort);
            return;
        }

        IIpsPackageFragmentRoot ipsPckFragmentRoot = getPresentationModel().getTargetPackageRoot();
        if (ipsPckFragmentRoot != null && !ipsPckFragmentRoot.exists()) {
            setErrorMessage(NLS.bind(Messages.SourcePage_msgMissingSourceFolder, ipsPckFragmentRoot.getName()));
            return;
        } else if (ipsPckFragmentRoot == null) {
            setErrorMessage(Messages.SourcePage_msgSelectSourceFolder);
            return;
        }

        if (SourcePage.this.getTargetPackage() != null && !SourcePage.this.getTargetPackage().exists()) {
            // type 20 = Severity.WARNING
            setMessage(NLS.bind(Messages.SourcePage_msgWarningTargetWillBeCreated,
                    SourcePage.this.getTargetPackage().getName()), 20);
        } else if (SourcePage.this.getTargetPackage() == null) {
            setErrorMessage(Messages.SourcePage_msgBadTargetPackage);
            return;
        }

        if (getPresentationModel().getTreeStatus().getCopyOrLink(getStructure().getRoot()) != CopyOrLink.COPY) {
            // type 20 = Severity.WARNING
            setMessage(Messages.SourcePage_msgNothingSelected, 20);
            return;
        }

        if (getWizard().getDeepCopyPreview().getErrorElements().size() != 0) {
            setErrorMessage(
                    Messages.SourcePage_msgCopyNotPossible + getWizard().getDeepCopyPreview().getFirstErrorText());
            return;
        }

        validateSearchPattern();
        if (getErrorMessage() != null) {
            return;
        }

        setPageComplete(true);
    }
    // CSON: CyclomaticComplexity

    private void validateSearchPattern() {
        if (type == DeepCopyWizard.TYPE_NEW_VERSION) {
            return;
        }
        String searchPattern = getPresentationModel().getSearchInput();
        String replacePattern = getPresentationModel().getReplaceInput();
        if (searchPattern.length() == 0 && replacePattern.length() == 0) {
            return;
        }

        if (searchPattern.length() == 0) {
            setErrorMessage(Messages.SourcePage_msgSearchPatternNotFound);
            return;
        }

        if (replacePattern.length() == 0) {
            setErrorMessage(Messages.SourcePage_msgReplaceTextNotFound);
        }

    }

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
            setErrorMessage(NLS.bind(Messages.SourcePage_errorWorkingDateFormat, pattern));
        }
    }

    public void refreshTree() {
        if (isRefreshing) {
            return;
        }
        try {
            isRefreshing = true;
            getWizard().getContainer().run(false, false, monitor -> {
                IProgressMonitor progressMonitor = (monitor == null) ? new NullProgressMonitor() : monitor;
                SubMonitor subMonitor = SubMonitor.convert(progressMonitor,
                        Messages.ReferenceAndPreviewPage_msgValidateCopy, 8);
                SubMonitor subProgressMonitor = subMonitor.split(6);
                getWizard().getDeepCopyPreview().createTargetNodes(subProgressMonitor);
                subMonitor.worked(1);
                tree.refresh();
                subMonitor.worked(1);
            });
        } catch (InvocationTargetException | InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } finally {
            isRefreshing = false;
        }
        // otherwise buttons are disabled after run and save ui state?
        copyTableContentsBtn.setEnabled(true);
        copyExistingGenerationsBtn.setEnabled(true);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (bindingContext != null) {
            bindingContext.dispose();
        }
    }

    private class ChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(DeepCopyPresentationModel.TARGET_PACKAGE_ROOT)) {
                targetPackageControl.setIpsPckFragmentRoot((IIpsPackageFragmentRoot)evt.getNewValue());
            }
            if (evt.getPropertyName().equals(DeepCopyPresentationModel.NEW_VALID_FROM)) {
                refreshVersionId();
            }
            if (evt.getPropertyName().equals(LinkStatus.CHECKED)
                    || evt.getPropertyName().equals(LinkStatus.COPY_OR_LINK)) {
                if (evt.getSource() instanceof IProductCmptStructureReference) {
                    expandOrCollapse(evt, (IProductCmptStructureReference)evt.getSource());
                }
                updateCheckedAndGrayedStatus(getStructure());
            }
            if (evt.getPropertyName().equals(DeepCopyPresentationModel.OLD_VALID_FROM)) {
                tree.setInput(getStructure());
                tree.expandAll();
                updateCheckedAndGrayedStatus(getStructure());
            }
            if (evt.getPropertyName().equals(DeepCopyPresentationModel.COPY_EXISTING_GENERATIONS)) {
                deactivationHint.setVisible(copyExistingGenerationsBtn.isChecked());
                deactivationHintData.exclude = !copyExistingGenerationsBtn.isChecked();
                if (copyExistingGenerationsBtn.isChecked()) {
                    getPresentationModel().setOldValidFrom(getPresentationModel().getOldValidFrom());
                }
                tree.getTree().setEnabled(!copyExistingGenerationsBtn.isChecked());
                tree.getTree().getParent().layout();
            }
            refreshPageAfterValueChange();
        }

        private void expandOrCollapse(PropertyChangeEvent evt, IProductCmptStructureReference reference) {
            if (!reference.isRoot()) {
                if (Boolean.TRUE.equals(evt.getNewValue()) || evt.getNewValue() == CopyOrLink.COPY) {
                    tree.expandToLevel(reference, CheckboxTreeViewer.ALL_LEVELS);
                } else {
                    tree.collapseToLevel(reference, CheckboxTreeViewer.ALL_LEVELS);
                }
            }
        }

    }
}
