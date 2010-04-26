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

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.util.message.MessageList;

/**
 * The page of the NewIpsPackageWizard.
 */
public class IpsPackagePage extends WizardPage implements ValueChangeListener {

    private static final String BLANK = " "; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    // edit controls
    private IpsPckFragmentRootRefControl sourceFolderControl;

    // edit fields
    private TextField nameField;
    private TextButtonField sourceFolderField;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // page control as defined by the wizard page class
    private Composite pageControl;

    // composite holding the control for the object's name.
    // subclasses can add their own controls.
    private Composite nameComposite;

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
     * {@inheritDoc}
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

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.IpsPackagePage_labelSrcFolder);
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

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
     * Derives the default values for source folder and package from the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) throws CoreException {
        if (selectedResource == null) {
            setIpsPackageFragmentRoot(null);
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots;
            try {
                roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
                if (roots.length > 0) {
                    setIpsPackageFragmentRoot(roots[0]);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e); // user can still work with the system, just so the defaults are
                // missing
                // so just log it.
            }
        } else if (element instanceof IIpsPackageFragmentRoot) {
            setIpsPackageFragmentRoot((IIpsPackageFragmentRoot)element);
        } else if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element;
            setIpsPackageFragment(pack);
        } else if (element instanceof IIpsSrcFile) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element.getParent();
            setIpsPackageFragment(pack);
        } else {
            setIpsPackageFragmentRoot(null);
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

    /**
     * Returns the name of the package to be created, relative to the closest corresponding package
     * as given by getPdPackageFragment().
     */
    public String getIpsPackageName() {
        String parentPackageName = getParentPackageFragment().getName();
        String packageName = nameField.getText();
        if (parentPackageName.length() == packageName.length()) {
            return ""; //$NON-NLS-1$
        }
        if (parentPackageName.length() > 0) {
            return packageName.substring(parentPackageName.length() + 1);
        } else {
            return packageName;
        }
    }

    /**
     * Returns the already existing package fragment corresponding to the longest possible substring
     * of the desired package name. If no such package fragment is found, the default package
     * fragment is returned.
     */
    public IIpsPackageFragment getParentPackageFragment() {
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root == null) {
            return null;
        }
        String packageName = nameField.getText();
        IIpsPackageFragment pack = root.getIpsPackageFragment(packageName);
        if (pack != null && pack.exists()) {
            return pack;
        }
        while (packageName.lastIndexOf('.') > 0) {
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
            pack = root.getIpsPackageFragment(packageName);
            if (pack != null && pack.exists()) {
                return pack;
            }
        }
        return root.getDefaultIpsPackageFragment();
    }

    public String getSourceFolder() {
        return sourceFolderField.getText();
    }

    /**
     * Returns the package fragment root corresponding to the selected source folder.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return sourceFolderControl.getIpsPckFragmentRoot();
    }

    protected void packageChanged() {

    }

    protected void nameChanged() {

    }

    private void setIpsPackageFragment(IIpsPackageFragment pack) {
        if (pack != null) {
            setIpsPackageFragmentRoot(pack.getRoot());
            replacePackageFragment(pack);
        }
    }

    private void replacePackageFragment(IIpsPackageFragment pack) {
        IIpsPackageFragment oldPack = getParentPackageFragment();
        nameField.setText(pack.getName().concat(nameField.getText().substring(oldPack.getName().length())));
        nameField.selectAll();
        nameField.getTextControl().setFocus();
    }

    private void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        sourceFolderControl.setPdPckFragmentRoot(root);
    }

    public IIpsProject getIpsProject() {
        if (getParentPackageFragment() == null) {
            return null;
        }
        return getParentPackageFragment().getIpsProject();
    }

    /**
     * Returns the ips object that is stored in the resource that was selected when the wizard was
     * opened or <code>null</code> if none is selected.
     * 
     * @throws CoreException if the contents of the resource can't be parsed.
     */
    public IIpsObject getSelectedIpsObject() throws CoreException {
        if (selectedResource == null) {
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
        if (e.field == sourceFolderField) {
            sourceFolderChanged();
        }
        if (e.field == nameField) {
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
     * Validates the page and generates error messages if needed. Can be overridden in subclasses to
     * add specific validation logic.s
     */
    protected void validatePage() throws CoreException {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        IIpsProject project = getIpsProject();
        if (project == null) {
            setErrorMessage(Messages.IpsPackagePage_msgSelectSourceFolder);
            return;
        }
        MessageList ml = project.getNamingConventions().validateIpsPackageName(nameField.getText());
        if (!ml.isEmpty()) {
            if (ml.containsErrorMsg()) {
                setErrorMessage(ml.getText());
                return;
            } else {
                setMessage(ml.getText(), IMessageProvider.WARNING);
            }
        }
        validateSourceRoot();
        if (getErrorMessage() != null) {
            return;
        }
        validatePackage();
        if (getErrorMessage() != null) {
            return;
        }
        validateName();
        if (getErrorMessage() != null) {
            return;
        }
        updatePageComplete();
    }

    /**
     * The method validates the package.
     */
    private void validateSourceRoot() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        if (root != null) {
            if (!root.getCorrespondingResource().exists()) {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgRootMissing, root.getName()));
            } else if (!root.exists()) {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgRootNoIPSSrcFolder, root.getName()));
            }
        } else {
            if (sourceFolderControl.getText().length() == 0) {
                setErrorMessage(Messages.IpsPackagePage_msgRootRequired);
            } else {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgRootMissing, sourceFolderControl.getText()));
            }
        }
    }

    /**
     * The method validates the source folder.
     */
    private void validatePackage() {
        IIpsPackageFragment pack = getParentPackageFragment();
        if (pack != null && !pack.exists()) {
            setErrorMessage(NLS.bind(Messages.IpsPackagePage_msgPackageMissing, pack.getName()));
        }
    }

    /**
     * The method validates the name.
     */
    protected void validateName() {
        String name = getIpsPackageName();
        String parentPackageName = getParentPackageFragment().getName();
        // must not be empty
        if (name.length() == 0) {
            if (parentPackageName.length() > 0) {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_PackageAllreadyExists, parentPackageName));
            } else {
                setErrorMessage(Messages.IpsPackagePage_msgEmptyName);
            }
            return;
        }
        if (name.indexOf(BLANK) != -1) {
            setErrorMessage(Messages.IpsPackagePage_PackageNameMustNotContainBlanks);
            return;
        }
        if (name.trim().equals(EMPTY_STRING)) {
            setErrorMessage(Messages.IpsPackagePage_InvalidPackageName);
            return;
        }
        IIpsPackageFragment pack = getParentPackageFragment();
        IIpsPackageFragment ipsPackage = pack.getRoot().getIpsPackageFragment(
                pack.isDefaultPackage() ? name : (pack.getName() + "." + name)); //$NON-NLS-1$
        IFolder folder = (IFolder)ipsPackage.getCorrespondingResource();
        if (folder != null) {
            if (folder.exists()) {
                setErrorMessage(NLS.bind(Messages.IpsPackagePage_PackageAllreadyExists, ipsPackage.getName()));
                return;
            }
        }
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
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
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        setIpsPackageFragmentRoot(root);
    }

}
