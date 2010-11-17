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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
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
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Page to let the user select products related to each other.
 * 
 * @author Thorsten Guenther
 */
public class SourcePage extends WizardPage implements ICheckStateListener {
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

    private DeepCopyContentProvider contentProvider;

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

        String descr = null;
        if (type == DeepCopyWizard.TYPE_NEW_VERSION) {
            String versionConcept = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                    .getVersionConceptNameSingular();
            descr = NLS.bind(Messages.SourcePage_description, versionConcept);
        } else {
            descr = Messages.SourcePage_description_copy;
        }
        super.setDescription(descr);

        try {
            namingStrategy = structure.getRoot().getProductCmpt().getIpsProject().getProductCmptNamingStrategy();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        setPageComplete(true);
    }

    @Override
    public void createControl(Composite parent) {
        getDeepCopyWizard().logTraceStart("SourcePage.createControl"); //$NON-NLS-1$
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
        workingDateField = new TextField(workingDate);

        toolkit.createFormLabel(inputRoot, Messages.SourcePage_labelSourceFolder);
        targetPackRootControl = toolkit.createPdPackageFragmentRootRefControl(inputRoot, true);

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

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelSearchPattern);
            searchInput = toolkit.createText(inputRoot);

            toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelReplacePattern);
            replaceInput = toolkit.createText(inputRoot);
        }

        String label = NLS.bind(Messages.ReferenceAndPreviewPage_labelVersionId, IpsPlugin.getDefault()
                .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        toolkit.createFormLabel(inputRoot, label);
        versionId = toolkit.createText(inputRoot);

        // radio button: copy table contents, create empty table contents
        RadiobuttonGroup group = toolkit.createRadiobuttonGroup(root, SWT.SHADOW_IN,
                Messages.SourcePage_labelGroupTableContents);
        group.getGroup().setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        copyTableContentsBtn = group.addRadiobutton(Messages.SourcePage_labelRadioBtnCopyTableContents);
        copyTableContentsBtn.setChecked(true);
        createEmptyTableContentsBtn = group.addRadiobutton(Messages.SourcePage_labelRadioBtnCreateEmptyTableContents);

        tree = new CheckboxTreeViewer(root, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tree.setUseHashlookup(true);
        contentProvider = new DeepCopyContentProvider(true, false);
        tree.setContentProvider(contentProvider);
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createColumns();

        checkStateListener = new CheckStateListener(this);
        tree.addCheckStateListener(checkStateListener);
        tree.addCheckStateListener(this);

        refreshStructure(structure);
        refreshVersionId(structure);

        initLinkElements();

        getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                workingDate.setFocus();

                // run async to ensure that the buttons state (enabled/disabled)
                // can be updated
                if (isRootExists()) {
                    setPageComplete(false);

                    setMessagePleaseEnterWorkingDate();

                    // clear version id because is user must first change the working date
                    versionId.setText(""); //$NON-NLS-1$
                    prevValues.put(versionId, null);
                    // trigger a new validate after focus lost on workingDate text field
                    prevValues.put(workingDate, null);
                } else {
                    refreshPageAferValueChange();
                }

                // update column width only the first time
                // otherwise the resize effect is to strange for the user
                updateColumnWidth();

            }
        });

        addListenerToAllControls();

        getDeepCopyWizard().logTraceEnd("SourcePage.createControl"); //$NON-NLS-1$
    }

    private void setMessagePleaseEnterWorkingDate() {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            String productCmptTypeName = StringUtil.unqualifiedName(structure.getRoot().getProductCmpt()
                    .getProductCmptType());
            setDescription(NLS.bind(Messages.SourcePage_msgPleaseEnterNewWorkingDateNewCopy, productCmptTypeName));
        } else if (type == DeepCopyWizard.TYPE_NEW_VERSION) {
            String versionConceptNameSingular = IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular();
            setDescription(NLS.bind(Messages.SourcePage_msgPleaseEnterNewWorkingDateNewGeneration,
                    versionConceptNameSingular));
        }
    }

    private boolean isRootExists() {
        // just check if the root contains errors or not
        return getDeepCopyWizard().getDeepCopyPreview().newTargetHasError(structure.getRoot());
    }

    private void addListenerToAllControls() {
        // add listener perform validate etc. if focus lost and value change
        // don't use value change event because of validation on every key pressed
        Text[] textCtrls;
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            textCtrls = new Text[] { workingDateField.getTextControl(), versionId, searchInput, replaceInput };
        } else {
            textCtrls = new Text[] { workingDateField.getTextControl(), versionId };
        }
        FocusListenerRefreshTreeOnValueChange focusListener = new FocusListenerRefreshTreeOnValueChange(textCtrls);
        for (Text textCtrl : textCtrls) {
            // control may be null in case of copy type = TYPE_NEW_VERSION
            textCtrl.addFocusListener(focusListener);
        }

        // special listener for text button controls perform validate etc. if value changed
        TextButtonField[] textButtonFields = new TextButtonField[] { new TextButtonField(targetPackRootControl),
                new TextButtonField(targetPackageControl) };
        for (TextButtonField textButtonField : textButtonFields) {
            textButtonField.addChangeListener(new ValueChangeListener() {
                @Override
                public void valueChanged(FieldValueChangedEvent e) {
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            // run asyn otherwise there may be problem with the focus change
                            // running in the same thread
                            // e.g. if using key tab then focus jumps two instead of one control
                            // forward or if using the mouse, to go into another control, then the
                            // focus doesn't change
                            refreshPageAferValueChange();
                        }
                    });
                }
            });
        }
    }

    protected void refreshPageAferValueChange() {
        getDeepCopyWizard().logTraceStart("refreshPageAferValueChange"); //$NON-NLS-1$

        getDeepCopyWizard().getDeepCopyPreview().resetCacheAfterValueChange();
        refreshTree();
        validate();
        updatePageComplete();

        getDeepCopyWizard().logTraceEnd("refreshPageAferValueChange"); //$NON-NLS-1$
    }

    /**
     * Initialize the default linked elements, all associations targets will be linked
     */
    private void initLinkElements() {
        IProductCmptReference[] productCmptReferences = structure.getChildProductCmptReferences(structure.getRoot());
        for (IProductCmptReference productCmptReference : productCmptReferences) {
            IProductCmptTypeAssociation association;
            try {
                IProductCmptLink link = productCmptReference.getLink();
                if (link == null) {
                    continue;
                }
                association = link.findAssociation(getDeepCopyWizard().getIpsProject());
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return;
            }
            if (association != null && association.isAssoziation()) {
                linkedElements.add(productCmptReference);
            }
        }
    }

    private class FocusListenerRefreshTreeOnValueChange implements FocusListener {
        private Text textControls[];

        public FocusListenerRefreshTreeOnValueChange(Text[] textControls) {
            this.textControls = textControls;
            // store first values, to decide the value change later one
            for (Text textControl : textControls) {
                hasValueChanged(textControl);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            // ignored;
        }

        @Override
        public void focusLost(FocusEvent e) {
            boolean refreshDone = false;
            for (int i = 0; i < textControls.length; i++) {
                if (!refreshDone && hasValueChanged(textControls[i])) {
                    // refresh has been performed
                    // so we don't need to check the other controls
                    refreshDone = true;
                    final int idx = i;
                    getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            // run async otherwise there may be problem with the focus change
                            // running in the same thread
                            // e.g. if using key tab then focus jumps two instead of one control
                            // forward or if using the mouse, to go into another control, then the
                            // focus doesn't change
                            if (textControls[idx] == workingDateField.getTextControl()) {
                                getDeepCopyWizard().applyWorkingDate();
                                // set the version id text as previous value
                                // to avoid second refresh
                                hasValueChanged(versionId);
                                refreshPageAferValueChange();
                                updateColumnWidth();
                            } else {
                                refreshPageAferValueChange();
                            }
                        }
                    });
                }
            }
        }
    }

    /*
     * Re-set the column width, the new name will be displayed completely the other columns width
     * will be resized depending on the windows size
     */
    private void updateColumnWidth() {
        TableLayout layout = new TableLayout();

        Point wizardSize = getDeepCopyWizard().getSize();
        GC gc = new GC(tree.getTree());
        gc.setFont(tree.getTree().getFont());
        // Note: we must extend the operation column size to display the full text of all elements
        // in the drop down, otherwise the drop down will not be activated after clicking the
        // column!
        int columnSizeOperation = gc.stringExtent(Messages.SourcePage_columnNameOperation).x + 30;
        int columnSizeNewName = gc.stringExtent(Messages.SourcePage_columnNameNewName).x + 20;
        Map<Object, String> oldObject2newNameMap = getDeepCopyWizard().getDeepCopyPreview().getOldObject2newNameMap();
        for (String newName : oldObject2newNameMap.values()) {
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
                boolean linked = getDeepCopyWizard().getDeepCopyPreview().isLinked(element);
                if (element instanceof ProductCmptTypeAssociationReference) {
                    cell.setText(""); //$NON-NLS-1$
                } else if (linked) {
                    cell.setText(Messages.SourcePage_operationLink);
                } else {
                    // if this is no link then it must be a copy
                    cell.setText(Messages.SourcePage_operationCopy);
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
                if (!(element instanceof IProductCmptStructureReference)) {
                    return false;
                }
                if (element instanceof ProductCmptTypeAssociationReference) {
                    // no operation for association nodes
                    return false;
                }
                if (structure.getRoot() == element) {
                    // operation change on root not allowed,
                    // because reference make no sense
                    return false;
                }

                // operation change is not allowed if parent and child is linked
                // note: if parent is linked and child not linked the operation change is
                // allowed, but this state is normally not possible see
                // setSameOperationForAllChilds(...)
                if (isParentAndCurrentReference(element)) {
                    return false;
                }

                return getDeepCopyWizard().getDeepCopyPreview().isCopy(element)
                        || getDeepCopyWizard().getDeepCopyPreview().isLinked(element);
            }

            private boolean isParentAndCurrentReference(Object element) {
                if (element instanceof IProductCmptStructureReference) {
                    IProductCmptStructureReference parent = ((IProductCmptStructureReference)element).getParent();
                    if (getDeepCopyWizard().getDeepCopyPreview().isLinked(parent)
                            && getDeepCopyWizard().getDeepCopyPreview().isLinked(element)) {
                        return true;
                    }
                } else {
                    throw new RuntimeException("Unsupported class found: " + element.getClass().getName()); //$NON-NLS-1$
                }
                return false;
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
                boolean doRefresh = false;
                if (!(element instanceof IProductCmptTypeAssociationReference)) {
                    boolean linkElement = index == 1;
                    if (linkElement) {
                        if (!linkedElements.contains(element)) {
                            linkedElements.add(element);
                            doRefresh = true;
                        }
                    } else {
                        if (linkedElements.contains(element)) {
                            linkedElements.remove(element);
                            doRefresh = true;
                        }
                    }
                    if (doRefresh && element instanceof IProductCmptReference) {
                        setSameOperationForAllChilds((IProductCmptReference)element, linkElement);
                    }
                }
                if (doRefresh) {
                    refreshPageAferValueChange();
                }
            }

            private void setSameOperationForAllChilds(IProductCmptReference element, boolean asLink) {
                getDeepCopyWizard().logTraceStart("setSameOperationForAllChilds"); //$NON-NLS-1$

                List<IProductCmptStructureReference> childs = new ArrayList<IProductCmptStructureReference>();
                childs.addAll(Arrays.asList(structure.getChildProductCmptReferences(element)));
                childs.addAll(Arrays.asList(structure.getChildProductCmptStructureTblUsageReference(element)));
                if (childs.size() == 0) {
                    return;
                }
                for (IProductCmptStructureReference childElem : childs) {
                    if (!isChecked(childElem)) {
                        continue;
                    }
                    if (asLink) {
                        linkedElements.add(childElem);
                    } else {
                        linkedElements.remove(childElem);
                    }
                }

                getDeepCopyWizard().logTraceEnd("setSameOperationForAllChilds"); //$NON-NLS-1$
            }
        });

        tree.getTree().setLinesVisible(true);
        tree.getTree().setHeaderVisible(true);

        TreeViewerEditor.create(tree, new ColumnViewerEditorActivationStrategy(tree), SWT.NONE);
    }

    private class SourceTreeCellLabelProvider extends CellLabelProvider {
        private ResourceManager resourceManager;
        private ImageDescriptor overlay = IpsUIPlugin.getImageHandling().getSharedImageDescriptor(
                "LinkOverlay.gif", true); //$NON-NLS-1$
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
            resourceManager.dispose();
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
                        Image image = labelProvider.getImage(element);
                        cell.setImage((Image)resourceManager.get(new DecorationOverlayIcon(image, overlay,
                                IDecoration.BOTTOM_RIGHT)));
                    } else {
                        cell.setImage(labelProvider.getImage(element));
                    }
                } else if (element instanceof IProductCmptStructureTblUsageReference) {
                    if (linkedElements.contains(element)) {
                        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                                "LinkTableContents.gif"); //$NON-NLS-1$
                        cell.setImage((Image)resourceManager.get(imageDescriptor));
                    } else {
                        cell.setImage(labelProvider.getImage(element));
                    }
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
    }

    /**
     * Refresh the structure in the tree and updates the version id
     */
    void refreshVersionId(IProductCmptTreeStructure structure) {
        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            String newVersionId = namingStrategy.getNextVersionId(structure.getRoot().getProductCmpt());
            if (!newVersionId.equals(versionId.getText())) {
                versionId.setText(newVersionId);
            }
        }
    }

    private void setCheckedAll(TreeItem[] items, boolean checked) {
        for (TreeItem item : items) {
            item.setChecked(checked);
            setCheckedAll(item.getItems(), checked);
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
            if (ipsObject == null) {
                continue;
            }
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
        getDeepCopyWizard().logTraceStart("validate"); //$NON-NLS-1$

        setMessage(null);
        setErrorMessage(null);

        if (!(tree != null && tree.getCheckedElements().length > 0)) {
            // no elements checked
            setErrorMessage(Messages.SourcePage_msgNothingSelected);
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        validateWorkingDate();
        if (getErrorMessage() != null) {
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList ml = namingStrategy.validateVersionId(versionId.getText());
            if (!ml.isEmpty()) {
                setErrorMessage(ml.getMessage(0).getText());
                getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
                return;
            }
        }

        if (structure == null) {
            setErrorMessage(Messages.SourcePage_msgCircleRelationShort);
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        IIpsPackageFragmentRoot ipsPckFragmentRoot = targetPackRootControl.getIpsPckFragmentRoot();
        if (ipsPckFragmentRoot != null && !ipsPckFragmentRoot.exists()) {
            setErrorMessage(NLS.bind(Messages.SourcePage_msgMissingSourceFolder, ipsPckFragmentRoot.getName()));
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        } else if (ipsPckFragmentRoot == null) {
            setErrorMessage(Messages.SourcePage_msgSelectSourceFolder);
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        if (getTargetPackage() != null && !getTargetPackage().exists()) {
            setMessage(NLS.bind(Messages.SourcePage_msgWarningTargetWillBeCreated, getTargetPackage().getName()),
                    WARNING);
        } else if (getTargetPackage() == null) {
            setErrorMessage(Messages.SourcePage_msgBadTargetPackage);
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        if (getDeepCopyWizard().getDeepCopyPreview().getProductCmptStructRefToCopy().length == 0) {
            setMessage(Messages.ReferenceAndPreviewPage_msgSelectAtLeastOneProduct, WARNING);
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        if (getDeepCopyWizard().getDeepCopyPreview().getErrorElements().size() != 0) {
            setErrorMessage(Messages.SourcePage_msgCopyNotPossible + " " //$NON-NLS-1$
                    + getDeepCopyWizard().getDeepCopyPreview().getFirstErrorText());
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        validateSearchPattern();
        if (getErrorMessage() != null) {
            getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
            return;
        }

        setPageComplete(true);
        getDeepCopyWizard().logTraceEnd("validate"); //$NON-NLS-1$
    }

    private void validateSearchPattern() {
        if (type == DeepCopyWizard.TYPE_NEW_VERSION) {
            return;
        }
        String searchPattern = getSearchPattern();
        String replacePattern = getReplaceText();
        if (searchPattern.length() == 0 && replacePattern.length() == 0) {
            return;
        }

        if (searchPattern.length() == 0) {
            setErrorMessage(Messages.SourcePage_msgSearchPatternNotFound);
            return;
        }

        if (replacePattern.length() == 0) {
            setErrorMessage(Messages.SourcePage_msgReplaceTextNotFound);
            return;
        }

        if (!isSearchPatternFound(searchPattern)) {
            setErrorMessage(NLS.bind(Messages.SourcePage_msgPatternNotFound, searchPattern));
            return;
        }
    }

    private boolean isSearchPatternFound(String searchPattern) {
        List<IProductCmptStructureReference> childs = new ArrayList<IProductCmptStructureReference>();
        IProductCmptTypeAssociationReference[] child = structure.getChildProductCmptTypeAssociationReferences(structure
                .getRoot());

        if (isSearchPatternMatch(structure.getRoot().getWrappedIpsObject().getName(), searchPattern)) {
            return true;
        }

        for (IProductCmptTypeAssociationReference element : child) {
            getAllChildElements(element, childs);
        }
        for (IProductCmptStructureReference reference : childs) {
            if (!getDeepCopyWizard().getDeepCopyPreview().isCopy(reference)) {
                continue;
            }
            if (isSearchPatternMatch(reference.getWrappedIpsObject().getName(), searchPattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSearchPatternMatch(String name, String searchPattern) {
        String newName = name.replaceAll(searchPattern, getReplaceText());
        return !name.equals(newName);
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

    public boolean isChecked(Object element) {
        List<Object> checkedElements = Arrays.asList(tree.getCheckedElements());
        return checkedElements.contains(element);
    }

    public IIpsPackageFragmentRoot getIIpsPackageFragmentRoot() {
        return targetPackRootControl.getIpsPckFragmentRoot();
    }

    public IProductCmptStructureReference[] getCheckedNodes() {
        List<Object> result = new ArrayList<Object>();
        List<Object> checkedElements = Arrays.asList(tree.getCheckedElements());
        result.addAll(checkedElements);
        if (!contentProvider.isShowAssociationTargets()) {
            // associations are hidden, thus these elements are checked per default
            // association targets to the result list
            result.addAll(linkedElements);
        }
        return result.toArray(new IProductCmptStructureReference[result.size()]);
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

    protected void updatePageComplete() {
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
    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        Object element = event.getElement();
        if (element instanceof IProductCmptTypeAssociationReference) {
            List<IProductCmptStructureReference> childs = new ArrayList<IProductCmptStructureReference>();
            getAllChildElements((IProductCmptTypeAssociationReference)element, childs);
            for (IProductCmptStructureReference iProductCmptStructureReference : childs) {
                linkedElements.remove(iProductCmptStructureReference);
            }
        } else {
            linkedElements.remove(element);
        }

        refreshPageAferValueChange();
    }

    private void getAllChildElements(IProductCmptTypeAssociationReference element,
            List<IProductCmptStructureReference> childs) {
        childs.addAll(Arrays.asList(structure.getChildProductCmptReferences(element)));
        childs.addAll(Arrays.asList(structure.getChildProductCmptStructureTblUsageReference(element)));
        IProductCmptReference[] childProductCmptRefs = structure.getChildProductCmptReferences(element);
        for (IProductCmptReference childProductCmptRef : childProductCmptRefs) {
            // get all children from all association references of all child ProductCmptReferences
            IProductCmptTypeAssociationReference[] childRefs2 = structure
                    .getChildProductCmptTypeAssociationReferences(childProductCmptRef);
            for (IProductCmptTypeAssociationReference element2 : childRefs2) {
                getAllChildElements(element2, childs);
            }
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

    private boolean isRefreshing = false;

    public void refreshTree() {
        if (isRefreshing) {
            return;
        }
        ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
        pmd.setOpenOnRun(true);
        try {
            isRefreshing = true;
            getWizard().getContainer().run(false, false, new IRunnableWithProgress() {
                @Override
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
        } finally {
            isRefreshing = false;
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
