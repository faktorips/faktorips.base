package org.faktorips.devtools.core.ui.wizards;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
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
 * Firist page of a wizards to create a new ips object. Allows the user to specify the ipssourcefolder, 
 * the ips package and the object's name. 
 */
public abstract class IpsObjectPage extends WizardPage implements ValueChangeListener {

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
    public IpsObjectPage(IStructuredSelection selection, String pageName) throws JavaModelException {
        super(pageName);
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();                
        }
    }
    
    /**
     * Overridden method.
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public final void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(getPdObjectType().getName());
        setMessage("Create a new " + getPdObjectType().getName() + ".");
        
        parent.setLayout(new GridLayout(1, false));
        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);
        
        Composite locationComposite  = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, "Source Folder:");
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

        toolkit.createFormLabel(locationComposite, "Package:");
        packageControl = toolkit.createPdPackageFragmentRefControl(locationComposite);
        packageField = new TextButtonField(packageControl);
        packageField.addChangeListener(this);
        
        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        nameComposite = toolkit.createLabelEditColumnComposite(pageControl);        
        fillNameComposite(nameComposite, toolkit);
        setDefaults();

        validateInput = true;
    }
    
    /**
     * Derives the default values for source folder and package from
     * the selected resource.
     */
    protected void setDefaults() {
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
                throw new RuntimeException(e);
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
        toolkit.createFormLabel(nameComposite, "Name:");
        Text nameText = toolkit.createText(nameComposite);
        nameText.setFocus();
        nameField = new TextField(nameText);
        nameField.addChangeListener(this);
        return nameText;
    }
    
    public String getPdObjectName() {
        return nameField.getText();
    }
    
    public void setPdObjectName(String newName) {
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
    
    protected void sourceFolderChanged() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getPdPckFragmentRoot();
        packageControl.setPdPckFragmentRoot(root);
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
    
    public IIpsPackageFragment getPdPackageFragment() {
        return packageControl.getPdPackageFragment();
    }
    
    protected IpsObjectType getPdObjectType() {
        return ((NewIpsObjectWizard)getWizard()).getIpsObjectType();
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
     * Can be overridden in subclasses to add specific validation logic. 
     */
    protected void validatePage() throws CoreException {
        setMessage("", IMessageProvider.NONE);
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
                setErrorMessage(root.getName() + " does not exist.");
            } else if (!root.exists()) {
                setErrorMessage(root.getName() + " is not an IPS source folder.");
            }
        }
	}
	
	/**
	 * The method validates the source folder.
	 */
	private void validatePackage() {
	    IIpsPackageFragment pack = packageControl.getPdPackageFragment(); 
        if (pack!=null && !pack.exists()) {
            setErrorMessage(pack.getName() + " does not exist.");
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
			setErrorMessage("Name is empty.");
			return;
		}
		if (name.indexOf('.') != -1) {
			setErrorMessage("Name must not be qualified.");
			return;
		}
		IStatus val= JavaConventions.validateJavaTypeName(name);
		if (val.getSeverity() == IStatus.ERROR) {
			setErrorMessage("The name " + name + " is not valid.");
			return;
		} else if (val.getSeverity() == IStatus.WARNING) {
			setMessage("The name is discouraged.", IMessageProvider.WARNING);
			// continue checking
		}		
		IIpsPackageFragment pack = packageControl.getPdPackageFragment();
		String filename = getPdObjectType().getFileName(name);
		if (pack!=null) {
		    IFolder folder = (IFolder)pack.getCorrespondingResource();
		    if (folder.getFile(filename).exists()) {
		        setErrorMessage("Object already exists.");
		        return;
		    }
		}
	}
    
    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(sourceFolderControl.getText())
        	&& !"".equals(nameField.getText());
        setPageComplete(complete);
    }

}
