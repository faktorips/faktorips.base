/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
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
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.core.ui.controls.RadiobuttonGroup;
import org.faktorips.util.message.Message;
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
    //   DeepCopyWizard.TYPE_COPY_PRODUCT or TYPE_NEW_VERSION
    private int type;

	// The working date format specified in the ips preferences
    private DateFormat dateFormat;

    private Radiobutton createEmptyTableContentsBtn;

    private Map errorElements = new HashMap();

    private static String getTitle(int type) {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return Messages.SourcePage_title;
        }
        else {
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
            this.namingStrategy = structure.getRoot().getProductCmpt().getIpsProject().getProductCmptNamingStrategy();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        
        dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
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
            this.setControl(errormsg);
            return;
        }

        UIToolkit toolkit = new UIToolkit(null);
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        Text workingDate = toolkit.createText(inputRoot);
        workingDate.setText(dateFormat.format(getDeepCopyWizard().getStructureDate().getTime()));
        workingDateField = new TextField(workingDate);
        workingDateField.getControl().addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent e) {
                // ignored
            }
            public void focusLost(FocusEvent e) {
                Runnable runnable = new Runnable(){
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
                if (srcRoots.length>0) {
                    packRoot = srcRoots[0];
                } else {
                    packRoot = null;
                }
            }
            catch (CoreException e1) {
                packRoot = null;
            }
        }
        targetPackRootControl.setPdPckFragmentRoot(packRoot);
        
        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelTargetPackage);
        targetPackageControl = toolkit.createPdPackageFragmentRefControl(packRoot, inputRoot);
        
        // sets the default package only if the corresponding package root is based on a source folder
        // in other cases reset the default package (because maybe the target package is inside an ips archive)
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
        TextField versionIdField = new TextField(versionId);
        versionIdField.addChangeListener(this);

        // radio button: copy table contents, create emtpy table contents
        RadiobuttonGroup group = toolkit.createRadiobuttonGroup(root, SWT.SHADOW_IN, Messages.SourcePage_labelGroupTableContents);
        group.getGroup().setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
        group.addRadiobutton(Messages.SourcePage_labelRadioBtnCopyTableContents).setChecked(true);
        createEmptyTableContentsBtn = group.addRadiobutton(Messages.SourcePage_labelRadioBtnCreateEmptyTableContents);
        
        tree = new CheckboxTreeViewer(root);
        tree.setUseHashlookup(true);
        tree.setLabelProvider(new DeepCopyLabelProviderWithError());
        tree.setContentProvider(new DeepCopyContentProvider(true));
        refreshStructureAndVersionId(structure);
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        checkStateListener = new CheckStateListener(null);
        tree.addCheckStateListener(checkStateListener);
        tree.addCheckStateListener(this);
        
        // add Listener to the target text control (must be done here after the default is set)
        targetPackageControl.getTextControl().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getWizard().getContainer().updateButtons();
            }
        });      
    }

    /**
     * Refresh the strucure in the tree and updates the version id
     */
    void refreshStructureAndVersionId(IProductCmptTreeStructure structure){
        this.structure = structure;
        tree.setInput(this.structure);
        tree.expandAll();
        setCheckedAll(tree.getTree().getItems(), true);        
        tree.refresh();

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
    
    IIpsObject getCorrespondingIpsObject(IProductCmptStructureReference productCmptStructureReference){
        if (productCmptStructureReference instanceof IProductCmptReference){
            return ((IProductCmptReference) productCmptStructureReference).getProductCmpt();
        } else if (productCmptStructureReference instanceof IProductCmptStructureTblUsageReference){
            ITableContents tableContents;
            try {
                tableContents = ((IProductCmptStructureTblUsageReference) productCmptStructureReference).getTableContentUsage().findTableContents(getDeepCopyWizard().getIpsProject());
                return tableContents;
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        return null;
    }
    
    IIpsPackageFragment getDefaultPackage() {
        int ignore = getSegmentsToIgnore((IProductCmptStructureReference[])structure.toArray(true));
        IIpsPackageFragment pack = structure.getRoot().getProductCmpt().getIpsPackageFragment();
        int segments = pack.getRelativePath().segmentCount();
        if (segments - ignore > 0) {
            IPath path = pack.getRelativePath().removeLastSegments(segments - ignore);
            pack = pack.getRoot().getIpsPackageFragment(path.toString().replace('/', '.'));
        }
        return pack;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        validate();
        return (getErrorMessage() == null);
    }

    private void validate() {
        setMessage(null);
        setErrorMessage(null);

        if (!(tree != null && tree.getCheckedElements().length > 0)){
            // no elements checked
            setErrorMessage(Messages.SourcePage_msgNothingSelected);
            return;
        }
        
        validateSameOperation();
        
        validateWorkingDate();
        if (getErrorMessage() != null){
            return;
        }
        
        if (namingStrategy != null && namingStrategy.supportsVersionId() ) {
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
        } else if (getTargetPackage() == null){
            setErrorMessage(Messages.SourcePage_msgBadTargetPackage);
            return;
        }
    }

    private void validateSameOperation() {
        errorElements.clear();
        
        SameOperationValidator operationValidator = new SameOperationValidator(tree, getDeepCopyWizard().getStructure());
        MessageList messages = new MessageList();
        operationValidator.validateSameOperation(messages);
        Message errorMsg = messages.getFirstMessage(Message.ERROR);
        if (errorMsg != null){
            setErrorMessage(Messages.SourcePage_msgCopyNotPossible);
        }
        
        int noOfMessages = messages.getNoOfMessages();
        for (int i = 0; i < noOfMessages; i++) {
            Message currMessage = messages.getMessage(i);
            IProductCmptStructureReference object = (IProductCmptStructureReference)currMessage.getInvalidObjectProperties()[0].getObject();
            errorElements.put(object, currMessage.getText());
        }
        tree.refresh();
    }
    
    private void validateWorkingDate() {
        try {
            Date date = dateFormat.parse(workingDateField.getText());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            if (calendar.before(structure.getRoot().getProductCmpt().getFirstGeneration().getValidFrom())) {
                String msg = NLS
                        .bind(
                                Messages.DeepCopyWizard_msgWorkingDateNotUsedSimple,
                                IpsPlugin.getDefault().getIpsPreferences()
                                        .getChangesOverTimeNamingConvention()
                                        .getGenerationConceptNameSingular());
                setErrorMessage(msg);
            }
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
        return (IProductCmptStructureReference[])Arrays.asList(tree.getCheckedElements()).toArray(
                new IProductCmptStructureReference[0]);
    }

    /**
     * Returns the pattern used to find the text to replace. This string is guaranteed to be either
     * empty or a valid pattern for java.util.regex.Pattern.
     */
    public String getSearchPattern() {
        String result = searchInput.getText();
        try {
            Pattern.compile(result);
        }
        catch (PatternSyntaxException e) {
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
        if (e.field==targetPackRootField) {
            sourceFolderChanged();
        }
        updatePageComplete();
    }

    private void sourceFolderChanged() {
        if (targetPackageControl!=null) {
            targetPackageControl.setIpsPckFragmentRoot(targetPackRootControl.getIpsPckFragmentRoot());
            targetPackageControl.setIpsPackageFragment(null);
        }
    }

    protected void updatePageComplete() {
        validate();
        if (getErrorMessage()!=null) {
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

    private DeepCopyWizard getDeepCopyWizard(){
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
        validate();
        getContainer().updateButtons();
    }
    
    private boolean isInError(Object object){
        return errorElements.containsKey(object);
    }
    
    private String getErrorMessage(Object object) {
        return (String)errorElements.get(object);
    }
    
    private class DeepCopyLabelProviderWithError extends DeepCopyLabelProvider {

        public Image getImage(Object element) {
            if (isInError(element)){
                return super.getErrorImage();
            }
            return super.getImage(element);
        }

        public String getText(Object element) {
            if (isInError(element)){
                return super.getText(element) + Messages.SourcePage_errorLabelInsert + getErrorMessage(element);
            } else {
                return super.getText(element);
            }
        }
    }
}
