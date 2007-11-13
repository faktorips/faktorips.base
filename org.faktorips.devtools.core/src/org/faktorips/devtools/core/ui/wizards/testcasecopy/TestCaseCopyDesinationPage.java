/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasecopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.core.ui.controls.RadiobuttonGroup;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.DelegateCellEditor;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Page to specify the target and the product cmpt replacement type.
 * 
 * @author Joerg Ortmann
 */
public class TestCaseCopyDesinationPage extends WizardPage {
    private static final String COLUMN_ROOT_PARAMETER_PRODUCTCMPT = "rootParameter"; //$NON-NLS-1$

    private static final String COLUMN_NEW_PRODUCTCMPT = "productcmpt"; //$NON-NLS-1$

    private UIToolkit toolkit;

    final ILabelProvider defaultLabelProvider = DefaultLabelProvider.createWithIpsSourceFileMapping();
    
    // indicates the initial state, no changes
    private boolean initialState = true;

    // indicates that the target must be recreated (delete and newly create)
    private boolean needRecreateTarget;

    // controls
    private Text targetName;
    private IpsPckFragmentRefControl targetInput;
    private TableViewer tableViewer;
    private CheckboxField checkboxFieldReplaceProductCmptAutomatically;
    private CheckboxField checkboxFieldReplaceProductCmptManual;

    // Cache of available product cmpt to replace with
    private Map rootParameterProductCmpt = new HashMap(10);
    private Map rootParameterProductCmptCandidates = new HashMap(10);
    
    public TestCaseCopyDesinationPage(UIToolkit toolkit) {
        super("TestCaseCopyDesionationPage"); //$NON-NLS-1$
        super.setTitle(Messages.TestCaseCopyDesinationPage_Title);
        
        this.toolkit = toolkit;
        
        setPageComplete(true);
        setMessage(Messages.TestCaseCopyDesinationPage_InfoMessage);
    }

    public void createControl(Composite parent) {
        Composite main = toolkit.createComposite(parent);
        main.setLayout(new GridLayout(1, false));
        main.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        createTargetControls(main);
        createTestCaseCopyTypeControls(main);

        setControl(main);
    }
    
    private void createTargetControls(Composite parent) {
        Composite root = toolkit.createLabelEditColumnComposite(parent);
        
        Group group = toolkit.createGridGroup(root, Messages.TestCaseCopyDesinationPage_TitleTargetGroup, 2, false);
        
        toolkit.createFormLabel(group, Messages.TestCaseCopyDesinationPage_LabelDestinationPackage);
        IIpsPackageFragment targetIpsPackageFragment = getTestCaseCopyWizard().getSourceTestCase().getIpsPackageFragment();
        targetInput = toolkit.createPdPackageFragmentRefControl(targetIpsPackageFragment.getRoot(), group);
        // set target default
        targetInput.setIpsPackageFragment(targetIpsPackageFragment);
        targetInput.getTextControl().addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e) {
                pageChanged();
            }
        });

        toolkit.createFormLabel(group, Messages.TestCaseCopyDesinationPage_LabelTargetName);
        targetName = toolkit.createText(group);
        targetName.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e) {
                pageChanged();
            }
        });
        
        targetName.setFocus();
    }
    
    private void createTestCaseCopyTypeControls(Composite parent) {
        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, true));
        root.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        RadiobuttonGroup group = toolkit.createRadiobuttonGroup(parent, SWT.SHADOW_IN , Messages.TestCaseCopyDesinationPage_TitleProductCmptReplaceGroup);
        group.getGroup().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Radiobutton radiobuttonReplaceAutomatically = group.addRadiobutton(Messages.TestCaseCopyDesinationPage_LabelRadioBtnReplaceProdCmptVersion);
        checkboxFieldReplaceProductCmptAutomatically = new CheckboxField(radiobuttonReplaceAutomatically);
        checkboxFieldReplaceProductCmptAutomatically.addChangeListener(new ValueChangeListener(){
            public void valueChanged(FieldValueChangedEvent e) {
                tableViewer.getTable().setEnabled(true);
                pageChanged();
            }
        });
        
        createRootParameterTable(group.getGroup());

        Radiobutton radiobuttonReplaceProductCmptManual = group.addRadiobutton(Messages.TestCaseCopyDesinationPage_LabelRadioBtnManualReplace);
        checkboxFieldReplaceProductCmptManual = new CheckboxField(radiobuttonReplaceProductCmptManual);
        checkboxFieldReplaceProductCmptManual.addChangeListener(new ValueChangeListener(){
            public void valueChanged(FieldValueChangedEvent e) {
                tableViewer.getTable().setEnabled(false);
                pageChanged();
                needRecreateTarget = true;
            }
        });
        
        radiobuttonReplaceAutomatically.setChecked(true);
    }

    private void createRootParameterTable(Composite parent) {
        Composite tableComposite = toolkit.createComposite(parent);
        tableComposite.setLayout(new GridLayout(1, true));
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalIndent = 11;
        tableComposite.setLayoutData(gridData);
        
        tableViewer = new TableViewer(tableComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setUseHashlookup(true);
        tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableViewer.getTable().setLinesVisible(true);
        
        TableLayout layout = new TableLayout();
        layout.addColumnData(new ColumnWeightData(ColumnWeightData.MINIMUM_WIDTH));
        layout.addColumnData(new ColumnWeightData(ColumnWeightData.MINIMUM_WIDTH));
        tableViewer.getTable().setLayout(layout);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = 100;
        tableViewer.getTable().setLayoutData(gd);
        tableViewer.getTable().setHeaderVisible(true);
        
        TableColumn column1 = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        TableColumn column2 = new TableColumn(tableViewer.getTable(), SWT.LEFT);
        column1.setText(Messages.TestCaseCopyDesinationPage_ColumnTitleToReplace);
        column2.setText(Messages.TestCaseCopyDesinationPage_ColumnReplaceWith);

        tableViewer.setColumnProperties(new String[]{COLUMN_ROOT_PARAMETER_PRODUCTCMPT, COLUMN_NEW_PRODUCTCMPT});
        
        // create content provider
        tableViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof ITestObject[]) {
                    return (ITestObject[])inputElement;
                } else {
                    return new ITestObject[0];
                }
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });
        
        // create label provider
        tableViewer.setLabelProvider(new ITableLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex) {
                if (columnIndex == 0){
                    return defaultLabelProvider.getImage(element);
                } else if (columnIndex == 1){
                    return defaultLabelProvider.getImage(rootParameterProductCmpt.get(element));
                }
                return null;
            }
            public String getColumnText(Object element, int columnIndex) {
                if (columnIndex == 0){
                    return defaultLabelProvider.getText(element);
                } else if (columnIndex == 1){
                    return defaultLabelProvider.getText(rootParameterProductCmpt.get(element));
                }
                return null;
            }
            public void addListener(ILabelProviderListener listener) {
            }
            public void dispose() {
            }
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }
            public void removeListener(ILabelProviderListener listener) {
            }
        });        
        
        // create cell editor/modifier
        CellEditor delegateCellEditor;
        try {
            List relevantTestPolicyCmpts = getRelevantRootTestPolicyCmpts();
            tableViewer.setInput((ITestPolicyCmpt[])relevantTestPolicyCmpts.toArray(new ITestPolicyCmpt[relevantTestPolicyCmpts.size()]));
            delegateCellEditor = createCellEditor(tableViewer, relevantTestPolicyCmpts);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
        tableViewer.setCellEditors(new CellEditor[]{ null, delegateCellEditor});
        tableViewer.setCellModifier(new ICellModifier(){
            public boolean canModify(Object element, String property) {
                if (COLUMN_NEW_PRODUCTCMPT.equals(property)){
                    return true;
                }
                return false;
            }

            public Object getValue(Object element, String property) {
                if (COLUMN_NEW_PRODUCTCMPT.equals(property)){
                    return defaultLabelProvider.getText(rootParameterProductCmpt.get(element));
                }
                return null;
            }

            public void modify(Object element, String property, Object value) {
                ITestPolicyCmpt rootTestPolicyCmpt = null;
                if (COLUMN_NEW_PRODUCTCMPT.equals(property)){
                    rootTestPolicyCmpt = (ITestPolicyCmpt)((TableItem)element).getData();
                    IIpsSrcFile[] canditates = (IIpsSrcFile[])rootParameterProductCmptCandidates.get(rootTestPolicyCmpt);
                    IIpsSrcFile changedValue = null;
                    for (int i = 0; i < canditates.length; i++) {
                        if (defaultLabelProvider.getText(canditates[i]).equals(value)){
                            changedValue = canditates[i];
                            break;
                        }
                    }
                    if (changedValue == null){
                        throw new RuntimeException("Wrong table content!"); //$NON-NLS-1$
                    }
                    Object oldValue = rootParameterProductCmpt.get(rootTestPolicyCmpt);
                    if (oldValue != changedValue){
                        rootParameterProductCmpt.put(rootTestPolicyCmpt, changedValue);
                        needRecreateTarget = true;
                    }
                }
                tableViewer.refresh();
                pageChanged();
                setInfoMessageVersionIdChange(rootTestPolicyCmpt);
            }
        });
        
        column1.pack();
        column2.pack();
        tableViewer.refresh();
        
        // add selection listener to inform about replaced version
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event) {
                if (event.getSelection() instanceof IStructuredSelection){
                    ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)((IStructuredSelection)event.getSelection()).getFirstElement();
                    setInfoMessageVersionIdChange(testPolicyCmpt);
                }
            }
        });
    }

    private List getRelevantRootTestPolicyCmpts() throws CoreException {
        ITestPolicyCmpt[] testPolicyCmpts = getTestCaseCopyWizard().getSourceTestCase().getTestPolicyCmpts();
        List result = new ArrayList(testPolicyCmpts.length);
        for (int i = 0; i < testPolicyCmpts.length; i++) {
            IProductCmpt productCmpt = testPolicyCmpts[i].findProductCmpt();
            if (productCmpt != null && StringUtils.isNotEmpty(testPolicyCmpts[i].getProductCmpt())) {
                result.add(testPolicyCmpts[i]);
            }
        }
        return result;
    }

    private void setInfoMessageVersionIdChange(ITestPolicyCmpt testPolicyCmpt) {
        try {
            IProductCmptNamingStrategy productCmptNamingStrategy = testPolicyCmpt.getIpsProject().getProductCmptNamingStrategy();
            IProductCmpt productCmpt = testPolicyCmpt.findProductCmpt();
            IIpsSrcFile productCmptToReplaceSrcFile = getProductCmptToReplace(testPolicyCmpt);
            IProductCmpt productCmptToReplace = (IProductCmpt)productCmptToReplaceSrcFile.getIpsObject();
            
            String versionId = productCmptNamingStrategy.getVersionId(productCmptToReplace.getName());
            if (!productCmpt.equals(productCmptToReplace)){
                setMessage(NLS.bind(Messages.TestCaseCopyDesinationPage_InfoMessageReplacedVersion, versionId));
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    private CellEditor createCellEditor(TableViewer tableViewer, List testObjects) throws CoreException {
        ILabelProvider provider = DefaultLabelProvider.createWithIpsSourceFileMapping();
        List cellEditors = new ArrayList(10);;
        DelegateCellEditor delegateCellEditor = new DelegateCellEditor(tableViewer, 1);
        ITestCase sourceTestCase = getTestCaseCopyWizard().getSourceTestCase();
        IProductCmptNamingStrategy productCmptNamingStrategy = sourceTestCase.getIpsProject().getProductCmptNamingStrategy();

        for (Iterator iter = testObjects.iterator(); iter.hasNext();) {
            ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)iter.next();
            ITestPolicyCmptTypeParameter parameter = testPolicyCmpt.findTestPolicyCmptTypeParameter(testPolicyCmpt.getIpsProject());
            if (parameter == null || !parameter.isRequiresProductCmpt()){
                cellEditors.add(null);
                continue;
            }
            IProductCmpt productCmpt = testPolicyCmpt.findProductCmpt();
            if (productCmpt != null){
                // add only candidates with same kind id
                String kindId = productCmptNamingStrategy.getKindId(productCmpt.getName());
                IIpsSrcFile[] allowedProductCmpt = parameter.getAllowedProductCmpt(sourceTestCase.getIpsProject(), 
                        null);
                
                List content = new ArrayList(allowedProductCmpt.length);
                List allowedProductCmptList = new ArrayList(allowedProductCmpt.length);
                for (int j = 0; j < allowedProductCmpt.length; j++) {
                    IProductCmpt productCmptCandidate = (IProductCmpt)allowedProductCmpt[j].getIpsObject();
                    String kindIdCandidate = productCmptNamingStrategy.getKindId(productCmptCandidate.getName());
                    if (kindId.equals(kindIdCandidate)){
                        content.add(provider.getText(allowedProductCmpt[j]));
                        allowedProductCmptList.add(allowedProductCmpt[j]);
                    }
                }
                
                Combo combo = toolkit.createCombo(tableViewer.getTable());
                combo.setItems((String[])content.toArray(new String[content.size()]));
                cellEditors.add(new ComboCellEditor(tableViewer, 1, combo));
                
                // store current product cmpt and all found candidates 
                rootParameterProductCmpt.put(testPolicyCmpt, productCmpt.getIpsSrcFile());
                rootParameterProductCmptCandidates.put(testPolicyCmpt, (IIpsSrcFile[])allowedProductCmptList.toArray(new IIpsSrcFile[allowedProductCmptList.size()]));
            }
        }
        delegateCellEditor.setCellEditors((CellEditor[])cellEditors.toArray(new TableCellEditor[cellEditors.size()]));
        return delegateCellEditor;
    }

    private TestCaseCopyWizard getTestCaseCopyWizard() {
        return (TestCaseCopyWizard) super.getWizard();
    }

    private void pageChanged() {
        initialState = false;
        boolean pageComplete = validatePage();
        setPageComplete(pageComplete);
        getContainer().updateButtons();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        return validatePage();
    }
    
    private boolean validatePage(){
        if (initialState){
            return false;
        }
        setMessage(null);
        setErrorMessage(null);
        
        // don't validate if the target hasn't changed - after creating the new target test case
        ITestCase targetTestCase = getTestCaseCopyWizard().getTargetTestCase();
        if (targetTestCase != null
                && targetTestCase.getQualifiedName().equals(getTestCaseCopyWizard().getTargetTestCaseQualifiedName())) {
            return true;
        }
        
        if (!validateTargetName()){
            return false;
        }
        
        if (!validateTargetPackageFragment()){
            return false;
        }
        
        if (!validateTargetExists()){
            return false;
        }
        
        return true;
    }

    private boolean validateTargetName() {
        String targetTestCaseName = targetName.getText();
        IIpsProjectNamingConventions namingConventions = getTestCaseCopyWizard().getSourceTestCase().getIpsProject().getNamingConventions();
        MessageList messageList = null;
        try {
            messageList = namingConventions.validateUnqualifiedIpsObjectName(IpsObjectType.TEST_CASE, targetTestCaseName);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        Message message = messageList.getFirstMessage(Message.ERROR);
        if (message != null){
            setErrorMessage(message.getText());
            return false;
        }
        message = messageList.getFirstMessage(Message.WARNING);
        if (message != null){
            setMessage(message.getText(), WARNING);
        }
        message = messageList.getFirstMessage(Message.INFO);
        if (message != null) {
            setMessage(message.getText(), INFORMATION);
        }
        return true;
    }
    
    private boolean validateTargetPackageFragment(){
        if (getTargetIpsPackageFragment() != null && !getTargetIpsPackageFragment().exists()) {
            setMessage(NLS.bind(Messages.TestCaseCopyDesinationPage_ValidationWarningTargetPackageWillBeCreated,
                    getTargetIpsPackageFragment().getName()),
                    WARNING);
        } else if (getTargetIpsPackageFragment() == null){
            setErrorMessage(Messages.TestCaseCopyDesinationPage_ValidationErrorBadTarget);
            return false;
        }
        return true;
    }
    
    private boolean validateTargetExists() {
        IIpsSrcFile ipsSrcFile = getTargetIpsPackageFragment().getIpsSrcFile(getTargetTestCaseName(), IpsObjectType.TEST_CASE);
        if (ipsSrcFile != null && 
                ipsSrcFile.exists()){
            setErrorMessage(Messages.TestCaseCopyDesinationPage_ValidationTargetAlreadyExists);
            return false;
        }
        return true;
    }
    
    public String getTargetTestCaseName() {
        return targetName.getText();
    }
    
    public IIpsPackageFragment getTargetIpsPackageFragment(){
        return targetInput.getIpsPackageFragment();
    }
    
    public IIpsSrcFile getProductCmptToReplace(ITestPolicyCmpt testPolicyCmpt){
        if (!checkboxFieldReplaceProductCmptAutomatically.getCheckbox().isChecked()){
            return null;
        }
        return (IIpsSrcFile)rootParameterProductCmpt.get(testPolicyCmpt);
    }

    /**
     * @return Returns the needRecreateTarget.
     */
    public boolean isNeedRecreateTarget() {
        return needRecreateTarget;
    }

    /**
     * @param needRecreateTarget The needRecreateTarget to set.
     */
    public void setNeedRecreateTarget(boolean needRecreateTarget) {
        this.needRecreateTarget = needRecreateTarget;
    }
}
