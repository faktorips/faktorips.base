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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.core.ui.controls.RadiobuttonGroup;
import org.faktorips.util.message.MessageList;

/**
 * Page to let the user select products related to each other.
 * 
 * @author Thorsten Guenther
 */
public class SourcePage extends WizardPage implements ValueChangeListener, ICheckStateListener {
    static final String PAGE_ID = "deepCopyWizard.source"; //$NON-NLS-1$

    private IProductCmptTreeStructure structure;
    private CheckboxTreeViewer tree;
    private CheckStateListener checkStateListener;

    // Control for search pattern
    private Text searchInput;
    // Control for replace text
    private Text replaceInput;

    private Map<Text, String> prevValues = new HashMap<Text, String>();

    // The input field for the user to enter a version id to be used for all newly created product
    // components.
    private Text versionId;

    // Controls
    private TextField workingDateField;
    private TextButtonField targetPackRootField;
    private IpsPckFragmentRootRefControl targetPackRootControl;
    private IpsPckFragmentRefControl targetPackageControl;

    // The naming strategy which is to be used to find the correct new names of the product
    // components to create.
    private IProductCmptNamingStrategy namingStrategy;

    // The type of the deep copy wizard (see DeepCopyWizard):
    // DeepCopyWizard.TYPE_COPY_PRODUCT or TYPE_NEW_VERSION
    private int type;

    // The working date format specified in the ips preferences
    private DateFormat dateFormat;

    private Radiobutton copyTableContentsBtn;
    private Radiobutton createEmptyTableContentsBtn;

    private Set<Object> linkedElements = new HashSet<Object>();

    private static String getTitle(int type) {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return Messages.SourcePage_title;
        } else {
            return NLS.bind(Messages.SourcePage_titleNewVersion, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        }
    }

    /**
     * Creates a new page to select the objects to copy.
     */
    protected SourcePage(IProductCmptTreeStructure structure, int type) {
        super(PAGE_ID, getTitle(type), null);
        this.structure = structure;
        this.type = type;

        setDescription(Messages.SourcePage_msgSelect);

        super.setDescription(Messages.SourcePage_description);

        try {
            namingStrategy = structure.getRoot().getProductCmpt().getIpsProject().getProductCmptNamingStrategy();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        setPageComplete(true);
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        if (structure == null) {
            Label errormsg = new Label(parent, SWT.WRAP);
            GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
            errormsg.setLayoutData(layoutData);
            errormsg.setText(Messages.SourcePage_msgCircleRelation);
            setControl(errormsg);
            return;
        }

        UIToolkit toolkit = new UIToolkit(null);
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        final Text workingDate = toolkit.createText(inputRoot);
        String workingDateAsText = dateFormat.format(getDeepCopyWizard().getStructureDate().getTime());
        workingDate.setText(workingDateAsText);
        hasValueChanged(workingDate);
        workingDateField = new TextField(workingDate);
        workingDateField.getControl().addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                // ignored
            }

            public void focusLost(FocusEvent e) {
                if (!hasValueChanged(workingDate)) {
                    // nothing changed
                    return;
                }
                Runnable runnable = new Runnable() {
                    public void run() {
                        getDeepCopyWizard().applyWorkingDate();
                    }
                };
                getShell().getDisplay().asyncExec(runnable);
            }
        });

        toolkit.createFormLabel(inputRoot, Messages.SourcePage_labelSourceFolder);
        targetPackRootControl = toolkit.createPdPackageFragmentRootRefControl(inputRoot, true);
        targetPackRootField = new TextButtonField(targetPackRootControl);
        targetPackRootField.addChangeListener(this);

        // set target default
        IIpsPackageFragment defaultPackage = getDefaultPackage();
        IIpsPackageFragmentRoot defaultPackageRoot = getDefaultPackage().getRoot();
        IIpsPackageFragmentRoot packRoot = defaultPackageRoot;
        if (!packRoot.isBasedOnSourceFolder()) {
            IIpsPackageFragmentRoot srcRoots[];
            try {
                srcRoots = structure.getRoot().getProductCmpt().getIpsProject().getSourceIpsPackageFragmentRoots();
                if (srcRoots.length > 0) {
                    packRoot = srcRoots[0];
                } else {
                    packRoot = null;
                }
            } catch (CoreException e1) {
                packRoot = null;
            }
        }
        targetPackRootControl.setPdPckFragmentRoot(packRoot);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelTargetPackage);
        targetPackageControl = toolkit.createPdPackageFragmentRefControl(packRoot, inputRoot);

        // sets the default package only if the corresponding package root is based on a source
        // folder in other cases reset the default package (because maybe the target package is
        // inside an ips archive)
        targetPackageControl.setIpsPackageFragment(defaultPackageRoot == packRoot ? defaultPackage : null);
        targetPackageControl.getTextControl().addFocusListener(
                new FocusListenerRefreshTreeOnValueChange(new Text[] { targetPackageControl.getTextControl() }));

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelSearchPattern);
            searchInput = toolkit.createText(inputRoot);

            toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelReplacePattern);
            replaceInput = toolkit.createText(inputRoot);

            FocusListenerRefreshTreeOnValueChange focusListener = new FocusListenerRefreshTreeOnValueChange(new Text[] {
                    searchInput, replaceInput });
            searchInput.addFocusListener(focusListener);
            replaceInput.addFocusListener(focusListener);
        }

        String label = NLS.bind(Messages.ReferenceAndPreviewPage_labelVersionId, IpsPlugin.getDefault()
                .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        toolkit.createFormLabel(inputRoot, label);
        versionId = toolkit.createText(inputRoot);
        versionId.addFocusListener(new FocusListenerRefreshTreeOnValueChange(new Text[] { versionId }));
        TextField versionIdField = new TextField(versionId);
        versionIdField.addChangeListener(this);

        // radio button: copy table contents, create empty table contents
        RadiobuttonGroup group = toolkit.createRadiobuttonGroup(root, SWT.SHADOW_IN,
                Messages.SourcePage_labelGroupTableContents);
        group.getGroup().setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        copyTableContentsBtn = group.addRadiobutton(Messages.SourcePage_labelRadioBtnCopyTableContents);
        copyTableContentsBtn.setChecked(true);
        createEmptyTableContentsBtn = group.addRadiobutton(Messages.SourcePage_labelRadioBtnCreateEmptyTableContents);

        tree = new CheckboxTreeViewer(root, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tree.setUseHashlookup(true);
        tree.setContentProvider(new DeepCopyContentProvider(true));
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createColumns();

        checkStateListener = new CheckStateListener(this);
        tree.addCheckStateListener(checkStateListener);
        tree.addCheckStateListener(this);

        refreshStructure(structure);
        refreshVersionId(structure);

        // add Listener to the target text control (must be done here after the default is set)
        targetPackageControl.getTextControl().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getWizard().getContainer().updateButtons();
            }
        });

        refreshTree();
        updateColumnWidth();
    }

    private class FocusListenerRefreshTreeOnValueChange implements FocusListener {
        private Text textControls[];

        public FocusListenerRefreshTreeOnValueChange(Text[] textControls) {
            this.textControls = textControls;
            for (int i = 0; i < textControls.length; i++) {
                hasValueChanged(textControls[i]);
            }
        }

        public void focusGained(FocusEvent e) {
            // ignored;
        }

        public void focusLost(FocusEvent e) {
            for (int i = 0; i < textControls.length; i++) {
                if (hasValueChanged(textControls[i])) {
                    refreshTreeAsync();
                    return;
                }
            }
        }
    }

    private void updateColumnWidth() {
        TableLayout layout = new TableLayout();

        Point wizardSize = getDeepCopyWizard().getSize();
        GC gc = new GC(tree.getTree());
        gc.setFont(tree.getTree().getFont());
        int columnSizeOperation = gc.stringExtent(Messages.SourcePage_columnNameOperation).x + 20;
        int columnSizeNewName = gc.stringExtent(Messages.SourcePage_columnNameNewName).x + 20;
        Map<Object, String> oldObject2newNameMap = getDeepCopyWizard().getDeepCopyPreview().getOldObject2newNameMap();
        for (Iterator<String> iterator = oldObject2newNameMap.values().iterator(); iterator.hasNext();) {
            String newName = iterator.next();
            int columnSizeNewNameCandidate = gc.stringExtent(newName).x + 40;
            columnSizeNewName = Math.max(columnSizeNewName, columnSizeNewNameCandidate);

        }
        gc.dispose();

        int columnSizeStructure = wizardSize.x - columnSizeOperation - columnSizeNewName - 10;

        layout.addColumnData(new ColumnPixelData(columnSizeStructure, true));
        layout.addColumnData(new ColumnPixelData(columnSizeOperation, true));
        layout.addColumnData(new ColumnPixelData(columnSizeNewName, true));
        tree.getTree().setLayout(layout);
        tree.getTree().layout(true);
    }

    /**
     * Create the columns for the receiver.
     * 
     * @param pageWidth
     * 
     * @param currentColumns the columns to refresh
     */
    private void createColumns() {
        SourceTreeCellLabelProvider labelProvider = new SourceTreeCellLabelProvider(new DeepCopyLabelProvider());
        ColumnViewerToolTipSupport.enableFor(tree);

        // source product structure
        TreeViewerColumn columnSource = new TreeViewerColumn(tree, SWT.NONE);
        columnSource.getColumn().setResizable(true);
        columnSource.getColumn().setMoveable(false);
        columnSource.setLabelProvider(labelProvider);
        columnSource.getColumn().setText(Messages.SourcePage_columnNameSourceStructure);

        // column to define copy or link
        TreeViewerColumn copyOrLink = new TreeViewerColumn(tree, SWT.LEAD);
        copyOrLink.getColumn().setResizable(true);
        copyOrLink.getColumn().setMoveable(false);
        copyOrLink.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                if (linkedElements.contains(element)) {
                    cell.setText(Messages.SourcePage_operationLink);
                } else if (getDeepCopyWizard().getDeepCopyPreview().isCopy(element)) {
                    cell.setText(Messages.SourcePage_operationCopy);
                } else {
                    cell.setText(""); //$NON-NLS-1$
                }
            }
        });
        copyOrLink.getColumn().setText(Messages.SourcePage_columnNameOperation);

        // new product name
        TreeViewerColumn columnNewName = new TreeViewerColumn(tree, SWT.NONE);
        columnNewName.getColumn().setResizable(true);
        columnNewName.getColumn().setMoveable(false);
        columnNewName.setLabelProvider(labelProvider);
        columnNewName.getColumn().setText(Messages.SourcePage_columnNameNewName);

        final ComboBoxCellEditor comboBoxCellEditor = new ComboBoxCellEditor(tree.getTree(), new String[] {
                Messages.SourcePage_operationCopy, Messages.SourcePage_operationLink }, SWT.READ_ONLY);
        copyOrLink.setEditingSupport(new EditingSupport(tree) {
            @Override
            protected boolean canEdit(Object element) {
                if (!(element instanceof IProductCmptReference)) {
                    return true;
                }
                return getDeepCopyWizard().getDeepCopyPreview().isCopy(element)
                        || getDeepCopyWizard().getDeepCopyPreview().isLinked(element);
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                return comboBoxCellEditor;
            }

            @Override
            protected Object getValue(Object element) {
                return linkedElements.contains(element) ? new Integer(1) : new Integer(0);
            }

            @Override
            protected void setValue(Object element, Object value) {
                Integer index = (Integer)value;
                if (element instanceof IProductCmptTypeRelationReference) {
                    List<IProductCmptStructureReference> childs = new ArrayList<IProductCmptStructureReference>();
                    getAllChildElements((IProductCmptTypeRelationReference)element, childs);
                    if (childs.size() == 0) {
                        return;
                    }
                    boolean confirmation = MessageDialog.openConfirm(getShell(), "Operation ändern",
                            "wollen sie die Operation für alle Kind Elemente übernehmen?");
                    if (!confirmation) {
                        return;
                    }
                    for (Iterator<IProductCmptStructureReference> iterator = childs.iterator(); iterator.hasNext();) {
                        IProductCmptStructureReference childElem = iterator.next();
                        if (index == 1) {
                            linkedElements.add(childElem);
                        } else {
                            linkedElements.remove(childElem);
                        }
                    }
                } else {
                    if (index == 1) {
                        linkedElements.add(element);
                    } else {
                        linkedElements.remove(element);
                    }
                }
                refreshTree();
            }
        });

        tree.getTree().setLinesVisible(true);
        tree.getTree().setHeaderVisible(true);

        TreeViewerEditor.create(tree, new ColumnViewerEditorActivationStrategy(tree), SWT.NONE);
    }

    private class SourceTreeCellLabelProvider extends CellLabelProvider {

        private ResourceManager resourceManager;

        private DeepCopyLabelProvider labelProvider;

        public SourceTreeCellLabelProvider(DeepCopyLabelProvider labelProvider) {
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
            this.labelProvider = labelProvider;
        }

        @Override
        public void dispose() {
            if (replaceInput != null) {
                replaceInput.dispose();
            }
            super.dispose();
        }

        @Override
        public void update(ViewerCell cell) {
            Object element = cell.getElement();
            if (cell.getVisualIndex() == 0) {
                // source structure column
                cell.setText(labelProvider.getText(element));
                if (element instanceof IProductCmptReference) {
                    if (linkedElements.contains(element)) {
                        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                                "LinkProductCmpt.gif");
                        cell.setImage((Image)resourceManager.get(imageDescriptor));
                    } else {
                        cell.setImage(labelProvider.getImage(element));
                    }
                } else if (element instanceof IProductCmptStructureTblUsageReference) {
                    if (linkedElements.contains(element)) {
                        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                                "LinkTableContents.gif");
                        cell.setImage((Image)resourceManager.get(imageDescriptor));
                    } else {
                        cell.setImage(labelProvider.getImage(element));
                    };
                } else {
                    cell.setImage(labelProvider.getImage(element));
                }
            } else {
                // new name column
                cell.setText(getDeepCopyWizard().getDeepCopyPreview().getOldObject2newNameMap().get(element));
                if (getErrorText(element) != null) {
                    cell.setImage(labelProvider.getErrorImage());
                } else {
                    cell.setImage(null);
                }
            }
        }

        @Override
        public String getToolTipText(Object element) {
            return getErrorText(element);
        }

        public String getErrorText(Object element) {
            DeepCopyPreview deepCopyPreview = getDeepCopyWizard().getDeepCopyPreview();
            return deepCopyPreview.getErrorElements().get(element);
        }
    }

    /**
     * Refresh the structure in the tree and updates the version id
     */
    private void refreshStructure(IProductCmptTreeStructure structure) {
        this.structure = structure;
        tree.setInput(this.structure);
        tree.expandAll();
        setCheckedAll(tree.getTree().getItems(), true);
        tree.refresh();

    }

    /**
     * Refresh the structure in the tree and updates the version id
     */
    void refreshVersionId(IProductCmptTreeStructure structure) {
        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            versionId.setText(namingStrategy.getNextVersionId(structure.getRoot().getProductCmpt()));
        }
        updatePageComplete();
    }

    private void setCheckedAll(TreeItem[] items, boolean checked) {
        for (int i = 0; i < items.length; i++) {
            items[i].setChecked(checked);
            setCheckedAll(items[i].getItems(), checked);
        }
    }

    /**
     * Calculate the number of <code>IPath</code>-segements which are equal for all product
     * component structure refences to copy.
     * 
     * @return 0 if no elements are contained in toCopy, number of all segments, if only one product
     *         component is contained in toCopy and the calculated value as described above for all
     *         other cases.
     */
    int getSegmentsToIgnore(IProductCmptStructureReference[] toCopy) {
        if (toCopy.length == 0) {
            return 0;
        }

        IIpsObject ipsObject = getCorrespondingIpsObject(toCopy[0]);
        IPath refPath = ipsObject.getIpsPackageFragment().getRelativePath();
        if (toCopy.length == 1) {
            return refPath.segmentCount();
        }

        int ignore = Integer.MAX_VALUE;
        for (int i = 1; i < toCopy.length; i++) {
            ipsObject = getCorrespondingIpsObject(toCopy[i]);
            int tmpIgnore;
            IPath nextPath = ipsObject.getIpsPackageFragment().getRelativePath();
            tmpIgnore = nextPath.matchingFirstSegments(refPath);
            if (tmpIgnore < ignore) {
                ignore = tmpIgnore;
            }
        }

        return ignore;
    }

    IIpsObject getCorrespondingIpsObject(IProductCmptStructureReference productCmptStructureReference) {
        if (productCmptStructureReference instanceof IProductCmptReference) {
            return ((IProductCmptReference)productCmptStructureReference).getProductCmpt();
        } else if (productCmptStructureReference instanceof IProductCmptStructureTblUsageReference) {
            ITableContents tableContents;
            try {
                tableContents = ((IProductCmptStructureTblUsageReference)productCmptStructureReference)
                        .getTableContentUsage().findTableContents(getDeepCopyWizard().getIpsProject());
                return tableContents;
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        return null;
    }

    IIpsPackageFragment getDefaultPackage() {
        int ignore = getSegmentsToIgnore(structure.toArray(true));
        IIpsPackageFragment pack = structure.getRoot().getProductCmpt().getIpsPackageFragment();
        int segments = pack.getRelativePath().segmentCount();
        if (segments - ignore > 0) {
            IPath path = pack.getRelativePath().removeLastSegments(segments - ignore);
            pack = pack.getRoot().getIpsPackageFragment(path.toString().replace('/', '.'));
        }
        return pack;
    }

    private void validate() {
        setPageComplete(false);
        setMessage(null);
        setErrorMessage(null);

        if (!(tree != null && tree.getCheckedElements().length > 0)) {
            // no elements checked
            setErrorMessage(Messages.SourcePage_msgNothingSelected);
            return;
        }

        validateWorkingDate();
        if (getErrorMessage() != null) {
            return;
        }

        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList ml = namingStrategy.validateVersionId(versionId.getText());
            if (!ml.isEmpty()) {
                setErrorMessage(ml.getMessage(0).getText());
                return;
            }
        }

        if (structure == null) {
            setErrorMessage(Messages.SourcePage_msgCircleRelationShort);
            return;
        }

        IIpsPackageFragmentRoot ipsPckFragmentRoot = targetPackRootControl.getIpsPckFragmentRoot();
        if (ipsPckFragmentRoot != null && !ipsPckFragmentRoot.exists()) {
            setErrorMessage(NLS.bind(Messages.SourcePage_msgMissingSourceFolder, ipsPckFragmentRoot.getName()));
            return;
        } else if (ipsPckFragmentRoot == null) {
            setErrorMessage(Messages.SourcePage_msgSelectSourceFolder);
            return;
        }

        if (getTargetPackage() != null && !getTargetPackage().exists()) {
            setMessage(NLS.bind(Messages.SourcePage_msgWarningTargetWillBeCreated, getTargetPackage().getName()),
                    WARNING);
        } else if (getTargetPackage() == null) {
            setErrorMessage(Messages.SourcePage_msgBadTargetPackage);
            return;
        }

        if (getDeepCopyWizard().getDeepCopyPreview().getProductCmptStructRefToCopy().length == 0) {
            setMessage(Messages.ReferenceAndPreviewPage_msgSelectAtLeastOneProduct, WARNING);
            return;
        }

        if (getDeepCopyWizard().getDeepCopyPreview().getErrorElements().size() != 0) {
            setErrorMessage(Messages.SourcePage_msgCopyNotPossible + " " //$NON-NLS-1$
                    + getDeepCopyWizard().getDeepCopyPreview().getFirstErrorText());
            return;
        }

        setPageComplete(true);
    }

    private void validateWorkingDate() {
        try {
            Date date = dateFormat.parse(workingDateField.getText());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
        } catch (ParseException e) {
            String pattern;
            if (dateFormat instanceof SimpleDateFormat) {
                pattern = ((SimpleDateFormat)dateFormat).toLocalizedPattern();
            } else {
                pattern = Messages.SourcePage_errorPrefixWorkingDateFormat;
            }
            setErrorMessage(NLS.bind(Messages.SourcePage_errorWorkingDateFormat, pattern));
        }
    }

    public IProductCmptStructureReference[] getCheckedNodes() {
        return Arrays.asList(tree.getCheckedElements()).toArray(
                new IProductCmptStructureReference[tree.getCheckedElements().length]);
    }

    /**
     * Returns the pattern used to find the text to replace. This string is guaranteed to be either
     * empty or a valid pattern for java.util.regex.Pattern.
     */
    public String getSearchPattern() {
        String result = searchInput.getText();
        try {
            Pattern.compile(result);
        } catch (PatternSyntaxException e) {
            result = ""; //$NON-NLS-1$
        }
        return result;
    }

    /**
     * Returns the text to replace the text found by the search pattern.
     */
    public String getReplaceText() {
        return replaceInput.getText();
    }

    /**
     * Returns the text to replace the text found by the search pattern.
     */
    public String getVersion() {
        return versionId.getText();
    }

    /**
     * Returns the text to replace the text found by the search pattern.
     */
    public IProductCmptNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    /**
     * Returns the package fragment which is to be used as target package for the copy.
     */
    public IIpsPackageFragment getTargetPackage() {
        return targetPackageControl.getIpsPackageFragment();
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == targetPackRootField) {
            sourceFolderChanged();
        }
        updatePageComplete();
    }

    private void sourceFolderChanged() {
        if (targetPackageControl != null) {
            targetPackageControl.setIpsPckFragmentRoot(targetPackRootControl.getIpsPckFragmentRoot());
            targetPackageControl.setIpsPackageFragment(null);
        }
    }

    protected void updatePageComplete() {
        validate();
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        setPageComplete(!"".equals(targetPackRootControl.getText())); //$NON-NLS-1$
    }

    /**
     * Returns the working date entered in the text control
     */
    public String getWorkingDate() {
        return workingDateField.getText();
    }

    private DeepCopyWizard getDeepCopyWizard() {
        return (DeepCopyWizard)getWizard();
    }

    /**
     * Returns <code>true</code> if the radio button "create empty table contents" is checked.
     */
    boolean isCreateEmptyTableContents() {
        return createEmptyTableContentsBtn.isChecked();
    }

    /**
     * {@inheritDoc}
     */
    public void checkStateChanged(CheckStateChangedEvent event) {
        Object element = event.getElement();
        if (element instanceof IProductCmptTypeRelationReference) {
            List<IProductCmptStructureReference> childs = new ArrayList<IProductCmptStructureReference>();
            getAllChildElements((IProductCmptTypeRelationReference)element, childs);
            for (Iterator<IProductCmptStructureReference> iterator = childs.iterator(); iterator.hasNext();) {
                linkedElements.remove(iterator.next());
            }
        } else {
            linkedElements.remove(element);
        }

        refreshTree();
        validate();
        getContainer().updateButtons();
    }

    private void getAllChildElements(IProductCmptTypeRelationReference element,
            List<IProductCmptStructureReference> childs) {
        childs.addAll(Arrays.asList(structure.getChildProductCmptReferences(element)));
        childs.addAll(Arrays.asList(structure.getChildProductCmptStructureTblUsageReference(element)));
        IProductCmptTypeRelationReference[] childRefs = structure.getChildProductCmptTypeRelationReferences(element,
                false);
        for (int i = 0; i < childRefs.length; i++) {
            getAllChildElements(childRefs[i], childs);
        }
    }

    public IProductCmptTreeStructure getStructure() {
        return structure;
    }

    public CheckboxTreeViewer getTree() {
        return tree;
    }

    public Set<Object> getLinkedElements() {
        return linkedElements;
    }

    public void refreshTreeAsync() {
        getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                refreshTree();
                validate();
                getContainer().updateButtons();
            }
        });
    }

    public void refreshTree() {
        ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
        pmd.setOpenOnRun(true);
        try {
            getWizard().getContainer().run(false, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }

                    monitor.beginTask(Messages.ReferenceAndPreviewPage_msgValidateCopy, 6);
                    getDeepCopyWizard().getDeepCopyPreview().checkForInvalidTargets();
                    monitor.worked(1);
                    monitor.worked(1);
                    monitor.worked(1);
                    tree.refresh();
                    monitor.worked(1);
                    monitor.worked(1);
                    monitor.worked(1);
                }
            });
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        // otherwise buttons are disabled after run and save ui state?
        copyTableContentsBtn.setEnabled(true);
        createEmptyTableContentsBtn.setEnabled(true);
    }

    private boolean hasValueChanged(Text text) {
        if (text.getText().equals(prevValues.get(text))) {
            return false;
        }
        prevValues.put(text, text.getText());
        return true;
    }
}
