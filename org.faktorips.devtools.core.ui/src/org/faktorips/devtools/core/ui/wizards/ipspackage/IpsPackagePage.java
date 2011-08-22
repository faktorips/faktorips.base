/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IDecoratorManager;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragmentRoot;
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
import org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider;
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
    private TextField packageNameField;
    private TextButtonField sourceFolderField;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // page control as defined by the wizard page class
    private Composite pageControl;

    private TreeViewer treeViewer;

    private TextField packagePathTextfield;

    public IpsPackagePage(IStructuredSelection selection) {
        super(Messages.IpsPackagePage_title);
        Object selectedObject = selection.getFirstElement();
        selectedResource = getSelectedResource(selectedObject);
    }

    private IResource getSelectedResource(Object selectedObject) {
        if (selectedObject instanceof IResource) {
            return (IResource)selectedObject;
        } else if (selectedObject instanceof IIpsElement) {
            return ((IIpsElement)selectedObject).getEnclosingResource();
        } else if (selectedObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)selectedObject;
            return (IResource)adaptable.getAdapter(IResource.class);
        } else {
            return null;
        }
    }

    @Override
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

        Composite pathComposite = toolkit.createLabelEditColumnComposite(pageControl);

        toolkit.createFormLabel(pathComposite, Messages.IpsPackagePage_packagePath);
        Text parentPackageText = toolkit.createText(pathComposite);
        packagePathTextfield = new TextField(parentPackageText);
        packagePathTextfield.addChangeListener(this);

        treeViewer = new TreeViewer(pageControl);
        GridData gridData = new GridData(SWT.FILL);
        gridData.grabExcessVerticalSpace = true;
        treeViewer.getControl().setLayoutData(gridData);

        treeViewer.setContentProvider(new IpsPackageContentProvider());
        ModelLabelProvider labelProvider = new ModelLabelProvider();
        IDecoratorManager decoManager = IpsPlugin.getDefault().getWorkbench().getDecoratorManager();
        DecoratingLabelProvider decoProvider = new DecoratingLabelProvider(labelProvider,
                decoManager.getLabelDecorator());
        treeViewer.setLabelProvider(decoProvider);
        treeViewer.addSelectionChangedListener(new IpsPackageSelectionChangedListener());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.horizontalSpan = 2;
        layoutData.heightHint = 250;
        treeViewer.getTree().setLayoutData(layoutData);

        Composite nameComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(nameComposite, Messages.IpsPackagePage_labelName);
        Text nameText = toolkit.createText(nameComposite);
        packageNameField = new TextField(nameText);
        packageNameField.addChangeListener(this);
        nameText.setFocus();

        setDefaults(selectedResource);

        validateInput = true;
    }

    public IIpsPackageFragment getSelectedIIpsPackageFragment() {
        ITreeSelection treeSelection = (ITreeSelection)treeViewer.getSelection();
        return (IIpsPackageFragment)treeSelection.getFirstElement();
    }

    /**
     * Derives the default values for source folder and package from the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource == null) {
            setIpsPackageFragment(null);
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots;
            roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
            if (roots.length > 0) {
                setIpsPackageFragment(roots[0].getDefaultIpsPackageFragment());
            }
        } else if (element instanceof IIpsPackageFragmentRoot) {
            setIpsPackageFragment(((IIpsPackageFragmentRoot)element).getDefaultIpsPackageFragment());
        } else if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element;
            setIpsPackageFragment(pack);
        } else if (element instanceof IIpsSrcFile) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element.getParent();
            setIpsPackageFragment(pack);
        } else {
            if (selectedResource instanceof IProject) {
                // selectedResource is no IpsProject
                setIpsPackageFragment(null);
            } else {
                IProject project = selectedResource.getProject();
                setDefaults(project);
            }
        }
    }

    /**
     * Returns the name of the package to be created, relative to the closest corresponding package
     * as given by getPdPackageFragment().
     */
    public String getIpsPackageName() {
        return packageNameField.getText();
    }

    public String getIpsPackagePath() {
        return packagePathTextfield.getText();
    }

    /**
     * Returns the already existing package fragment corresponding to the longest possible substring
     * of the desired package name. If no such package fragment is found, the default package
     * fragment is returned.
     */
    public IIpsPackageFragment getParentPackageFragment() {
        return getSelectedIIpsPackageFragment();
    }

    public String getSourceFolder() {
        return sourceFolderField.getText();
    }

    /**
     * Returns the package fragment root corresponding to the selected source folder.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return sourceFolderControl.getIpsPackageFragmentRoot();
    }

    protected void packagePathChanged() {
        if (getIpsPackageFragmentRoot() != null) {
            String packageName = packagePathTextfield.getText();
            IIpsPackageFragment pack = getIpsPackageFragmentRoot().getIpsPackageFragment(packageName);
            if (pack != null) {
                treeViewer.setSelection(new StructuredSelection(pack), true);
                treeViewer.expandToLevel(pack, 1);
            }
        }
    }

    private void setIpsPackageFragment(IIpsPackageFragment pack) {
        if (pack != null) {
            sourceFolderControl.setIpsPackageFragmentRoot(pack.getRoot());
            treeViewer.setInput(pack.getRoot());
            treeViewer.setSelection(new StructuredSelection(pack), true);
            treeViewer.expandToLevel(pack, 1);
        } else {
            packagePathTextfield.setText(""); //$NON-NLS-1$
        }
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

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == sourceFolderField) {
            sourceFolderChanged();
        }

        if (e.field == packagePathTextfield) {
            packagePathChanged();
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
        MessageList ml = project.getNamingConventions().validateIpsPackageName(packageNameField.getText());
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
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPackageFragmentRoot();
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
                && !"".equals(packageNameField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    protected void sourceFolderChanged() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPackageFragmentRoot();
        setIpsPackageFragment(root.getDefaultIpsPackageFragment());
    }

    public class IpsPackageContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
            // nothing to dispose
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do

        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IIpsPackageFragment) {
                IIpsPackageFragment ipsPackageFragment = (IIpsPackageFragment)parentElement;
                try {
                    return ipsPackageFragment.getChildIpsPackageFragments();
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof IIpsPackageFragment) {
                IIpsPackageFragment ipsPackageFragment = (IIpsPackageFragment)element;
                return ipsPackageFragment.getParentIpsPackageFragment();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            Object[] ipsPackageFragment = getChildren(element);
            return (ipsPackageFragment.length > 0);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IpsPackageFragmentRoot) {
                IIpsPackageFragment iIpsPackageFragment = ((IpsPackageFragmentRoot)inputElement)
                        .getDefaultIpsPackageFragment();
                return new Object[] { iIpsPackageFragment };
            }

            return new Object[0];
        }
    }

    class IpsPackageSelectionChangedListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            ISelection selection = treeViewer.getSelection();
            if (selection instanceof ITreeSelection) {
                ITreeSelection treeSelection = (ITreeSelection)selection;
                Object selectionElement = treeSelection.getFirstElement();
                if (selectionElement instanceof IpsPackageFragment) {
                    IIpsPackageFragment iIpsPackageFragment = (IIpsPackageFragment)selectionElement;
                    String path = iIpsPackageFragment.toString();
                    int index = path.lastIndexOf("/"); //$NON-NLS-1$
                    String packagePath = path.substring(index + 1);
                    packagePathTextfield.setValue(packagePath, true);

                }
            }
        }
    }
}
