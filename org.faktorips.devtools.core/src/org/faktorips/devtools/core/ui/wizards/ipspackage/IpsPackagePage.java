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

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;


/**
 *
 */
public class IpsPackagePage extends WizardPage implements ValueChangeListener {

    private static final String BLANK = " ";

    private static final String EMPTY_STRING = "";

    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;
    
    // edit controls
    private IpsPckFragmentRootRefControl sourceFolderControl;
    private IpsPckFragmentRefControl packageControl;
    
    // edit fields
    private TextField nameField;
    private TextButtonField sourceFolderField;
    private TextButtonField packageField;
    
    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;
    
    // page control as defined by the wizard page class
    private Composite pageControl;
    
    // composite holding the control for the object's name.
    // subclasses can add their own controls.
    private Composite nameComposite;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public IpsPackagePage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.IpsPackagePage_title);
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();                
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
    }
    
    /**
     * Overridden method.
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public final void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.IpsPackagePage_title);
        setMessage(Messages.IpsPackagePage_msgNew); 

        // dont set the layout of the parent composite - this will lead to 
        // layout-problems when this wizard-page is opened within allready open dialogs
        // (for example when the user wants a new policy class and starts the wizard using
        // the file-menu File->New->Other).
        
        // parent.setLayout(new GridLayout(1, false));
        
        pageControl = new Composite(parent, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        pageControl.setLayoutData(data);
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);
        
        Composite locationComposite  = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.IpsPackagePage_labelSrcFolder);
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

        toolkit.createFormLabel(locationComposite, Messages.IpsPackagePage_labelPackage);
        packageControl = toolkit.createPdPackageFragmentRefControl(locationComposite);
        packageField = new TextButtonField(packageControl);
        packageField.addChangeListener(this);
        
        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        nameComposite = toolkit.createLabelEditColumnComposite(pageControl);        
        fillNameComposite(nameComposite, toolkit);
        try {
            setDefaults(selectedResource);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        validateInput = true;
    }
    
    /**
     * Derives the default values for source folder and package from
     * the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when
     * the wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) throws CoreException {
        if (selectedResource==null) {
            setPdPackageFragmentRoot(null);
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots;
            try {
                roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
                if (roots.length>0) {
                    setPdPackageFragmentRoot(roots[0]);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e); // user can still work with the system, just so the defaults are missing
                // so just log it. 
            }
        } else if (element instanceof IIpsPackageFragmentRoot) {
            setPdPackageFragmentRoot((IIpsPackageFragmentRoot)element);
        } else if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element;
            setPdPackageFragment(pack);
        } else if (element instanceof IIpsSrcFile) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element.getParent();
            setPdPackageFragment(pack);
        } else {
            setPdPackageFragmentRoot(null);    
        }
    }

    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        Text nameText = addNameLabelField(toolkit);
        nameText.setFocus();
    }
    
    protected Text addNameLabelField(UIToolkit toolkit) {
        toolkit.createFormLabel(nameComposite, Messages.IpsPackagePage_labelName); 
        return addNameField(toolkit);
    }
    
    protected Text addNameField(UIToolkit toolkit) {
        Text nameText = toolkit.createText(nameComposite);
        nameText.setFocus();
        nameField = new TextField(nameText);
        nameField.addChangeListener(this);
        return nameText;
    }
    
    public String getIpsPackageName() {
        return nameField.getText();
    }
    
    public void setIpsObjectName(String newName) {
        nameField.setText(newName);
    }
    
    public String getPackage() {
        return packageField.getText();
    }
    
    public String getSourceFolder() {
        return sourceFolderField.getText();
    }
    
    public IIpsPackageFragmentRoot getPdPackageFragmentRoot() {
        return sourceFolderControl.getPdPckFragmentRoot();
    }
        
    protected void packageChanged() {
        
    }
    
    protected void nameChanged() {
        
    }
    
    private void setPdPackageFragment(IIpsPackageFragment pack) {
        packageControl.setPdPackageFragment(pack);
        if (pack!=null) {
            setPdPackageFragmentRoot(pack.getRoot());    
        }
    }
    
    private void setPdPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        sourceFolderControl.setPdPckFragmentRoot(root);
    }
    
    public IIpsPackageFragment getIpsPackageFragment() {
        return packageControl.getPdPackageFragment();
    }
    
    public IIpsProject getIpsProject() {
        if (getIpsPackageFragment()==null) {
            return null;
        }
        return getIpsPackageFragment().getIpsProject();
    }
    
    /**
     * Returns the ips object that is stored in the resource that was selected when
     * the wizard was opened or <code>null</code> if none is selected.
     * 
     * @throws CoreException if the contents of the resource can't be parsed. 
     */
    public IIpsObject getSelectedIpsObject() throws CoreException {
        if (selectedResource==null) {
            return null;
        }
        IIpsElement el = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (el instanceof IIpsSrcFile) {
            return ((IIpsSrcFile)el).getIpsObject();
        }
        return null;
    }
        
    protected Composite getNameComposite() {
        return nameComposite;
    }

    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field==sourceFolderField) {
            sourceFolderChanged();
        }
        if (e.field==packageField) {
            packageChanged();
        }
        if (e.field==nameField) {
            nameChanged();
        }
        if (validateInput) { // don't validate during control creating!
            try {
                validatePage();    
            } catch (CoreException coreEx) {
                IpsPlugin.logAndShowErrorDialog(coreEx);
            }
            
        }
        updatePageComplete();
    }
    
    /**
     * Validates the page and generates error messages if needed. 
     * Can be overridden in subclasses to add specific validation logic.s 
     */
    protected void validatePage() throws CoreException {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        validateSourceRoot();
        if (getErrorMessage()!=null) {
            return;
        }
        validatePackage();
        if (getErrorMessage()!=null) {
            return;
        }
        validateName();
        updatePageComplete();
    }
    
    /**
     * The method validates the package.
     */
    private void validateSourceRoot() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getPdPckFragmentRoot(); 
        if (root!=null) {
            if (!root.getCorrespondingResource().exists()) {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgRootMissing, root.getName())); 
            } else if (!root.exists()) {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgRootNoIPSSrcFolder, root.getName())); 
            }
        }
        else {
            if (sourceFolderControl.getText().length() == 0) {
            setErrorMessage(Messages.IpsPackagePage_msgRootRequired);
            }
            else {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgRootMissing, sourceFolderControl.getText())); 
            }
        }
    }
    
    /**
     * The method validates the source folder.
     */
    private void validatePackage() {
        IIpsPackageFragment pack = packageControl.getPdPackageFragment(); 
        if (pack!=null && !pack.exists()) {
            setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgPackageMissing, pack.getName())); 
        }
    }
    
    /**
     * The method validates the name.
     * <p>
     * Subclasses may extend this method to perform their own validation.
     * </p>
     */
    protected void validateName() {
        String name=nameField.getText(); 
        // must not be empty
        if (name.length() == 0) {
            setErrorMessage(Messages.IpsPackagePage_msgEmptyName);
            return;
        }
        if (name.indexOf(BLANK) != -1) {
            setErrorMessage(Messages.IpsPackagePage_PackageNameMustNotContainBlanks);
            return;
        }
        if (name.trim().equals(EMPTY_STRING)) {
            setErrorMessage( Messages.IpsPackagePage_InvalidPackageName);
            return;
        }
        IStatus status = JavaConventions.validatePackageName(name);
        if (status.getSeverity() == IStatus.ERROR) {
            setErrorMessage(NLS.bind(Messages.IpsPackagePage_InvalidPackageName, status.getMessage()));
            return;
        }
        if (status.getSeverity() == IStatus.WARNING) {
            this.setMessage(NLS.bind(Messages.IpsPackagePage_msgNameDiscouraged, status.getMessage()), IMessageProvider.WARNING);
            return;
        }
        IIpsPackageFragment pack = packageControl.getPdPackageFragment();
        IIpsPackageFragment ipsPackage = pack.getRoot().getIpsPackageFragment(pack.isDefaultPackage()?name:(pack.getName() + "." + name));
        IFolder folder = (IFolder)ipsPackage.getCorrespondingResource();
        if (folder != null) {
            if (folder.exists()) {
                setErrorMessage( NLS.bind(Messages.IpsPackagePage_PackageAllreadyExists, ipsPackage.getName()));
                return;
            }
        }
    }
    
    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(sourceFolderControl.getText()) //$NON-NLS-1$
            && !"".equals(nameField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    
    /** 
     * {@inheritDoc}
     */
    protected void sourceFolderChanged() {
            IIpsPackageFragmentRoot root = sourceFolderControl.getPdPckFragmentRoot();
            packageControl.setPdPckFragmentRoot(root);
    }

    
    
}
