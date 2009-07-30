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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsimport.NewImportedIpsObjectPage;
import org.faktorips.util.StringUtil;

/**
 * Wizard page to specify a name and a table structure for table contents to be imported.
 * 
 * @author Thorsten Waertel
 */
public class NewTableContentsPage extends NewImportedIpsObjectPage implements ValueChangeListener {

    public static final String PAGE_NAME = "NewContentsPage"; //$NON-NLS-1$

    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    // edit controls
    private IpsPckFragmentRootRefControl sourceFolderControl;
    private IpsPckFragmentRefControl packageControl;
    private TableStructureRefControl structureControl;

    // edit fields
    private TextButtonField sourceFolderField;
    private TextButtonField packageField;
    private TextButtonField structureField;
    private TextField contentsField;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // page control as defined by the wizard page class
    private Composite pageControl;

    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public NewTableContentsPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.NewContentsPage_title);

        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
        setPageComplete(false);
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.NewContentsPage_title);

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.NewContentsPage_labelSrcFolder);
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

        toolkit.createFormLabel(locationComposite, Messages.NewContentsPage_labelPackage);
        packageControl = toolkit.createPdPackageFragmentRefControl(locationComposite);
        packageField = new TextButtonField(packageControl);
        packageField.addChangeListener(this);

        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite structureComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(structureComposite, Messages.NewContentsPage_labelStructure);
        structureControl = toolkit.createTableStructureRefControl(null, structureComposite);
        structureField = new TextButtonField(structureControl);
        structureField.addChangeListener(this);

        toolkit.createFormLabel(structureComposite, Messages.NewContentsPage_labelContents);
        Text contentsText = toolkit.createText(structureComposite);
        contentsField = new TextField(contentsText);
        contentsField.addChangeListener(this);

        setDefaults(selectedResource);

        validateInput = true;
    }

    /**
     * Derives the default values for source folder and package from the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource == null) {
            setPdPackageFragmentRoot(null);
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots;
            try {
                roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
                if (roots.length > 0) {
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
            // sets the default table structure
            try {
                if (((IIpsSrcFile)element).getIpsObject() instanceof ITableContents) {
                    ITableContents tableContents = (ITableContents)((IIpsSrcFile)element).getIpsObject();
                    structureControl.setText(tableContents.getTableStructure());
                } else if (((IIpsSrcFile)element).getIpsObject() instanceof ITableStructure) {
                    ITableStructure tableStructure = (ITableStructure)((IIpsSrcFile)element).getIpsObject();
                    structureControl.setText(tableStructure.getQualifiedName());
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        } else {
            setPdPackageFragmentRoot(null);
            return;
        }

        validatePage();
    }

    public String getPackage() {
        return packageField.getText();
    }

    public String getSourceFolder() {
        return sourceFolderField.getText();
    }

    public IIpsPackageFragmentRoot getPdPackageFragmentRoot() {
        return sourceFolderControl.getIpsPckFragmentRoot();
    }

    protected void sourceFolderChanged() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        packageControl.setIpsPckFragmentRoot(root);
        if (root != null) {
            structureControl.setIpsProject(root.getIpsProject());
        } else {
            structureControl.setIpsProject(null);
        }
    }

    protected void packageChanged() {

    }

    protected void contentsChanged() {
    }

    private void setPdPackageFragment(IIpsPackageFragment pack) {
        packageControl.setIpsPackageFragment(pack);
        if (pack != null) {
            setPdPackageFragmentRoot(pack.getRoot());
        }
    }

    private void setPdPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        sourceFolderControl.setPdPckFragmentRoot(root);
    }

    public IIpsPackageFragment getIpsPackageFragment() {
        return packageControl.getIpsPackageFragment();
    }

    public IIpsProject getIpsProject() {
        if (getIpsPackageFragment() == null) {
            return null;
        }
        return getIpsPackageFragment().getIpsProject();
    }

    public String getTableStructureName() {
        return structureControl.getText();
    }

    public String getTableContentsName() {
        return contentsField.getText();
    }

    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == sourceFolderField) {
            sourceFolderChanged();
        }
        if (e.field == packageField) {
            packageChanged();
        }
        if (e.field == contentsField) {
            contentsChanged();
        }
        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    /**
     * Validates the page and generates error messages if needed. Can be overridden in subclasses to
     * add specific validation logic.s
     */
    public void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        validateSourceRoot();
        if (getErrorMessage() != null) {
            return;
        }
        validatePackage();
        if (getErrorMessage() != null) {
            return;
        }
        validateStructure();
        if (getErrorMessage() != null) {
            return;
        }
        validateContent();
        if (getErrorMessage() != null) {
            return;
        }
        updatePageComplete();
    }

    private void validateContent() {
        if (contentsField.getText().length() == 0) {
            setErrorMessage(Messages.NewContentsPage_msgEmptyContent);
            return;
        }
        IIpsPackageFragment pack = packageControl.getIpsPackageFragment();
        try {
            if (pack.getIpsProject().findIpsObject(IpsObjectType.TABLE_CONTENTS,
                    StringUtil.qualifiedName(pack.getName(), contentsField.getText())) != null) {
                setErrorMessage(NLS.bind(Messages.NewContentsPage_msgExistingContent, contentsField.getText()));
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            setErrorMessage(e.getMessage());
        }
    }

    private void validateStructure() {
        try {
            if (structureControl.findTableStructure() == null) {
                setErrorMessage(NLS.bind(Messages.NewContentsPage_msgTableStructureNotExists, structureField.getText()));
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            setErrorMessage(e.getMessage());
        }
    }

    /**
     * The method validates the package.
     */
    private void validateSourceRoot() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        if (root != null) {
            if (!root.getCorrespondingResource().exists()) {
                setErrorMessage(NLS.bind(Messages.NewContentsPage_msgRootMissing, root.getName()));
            } else if (!root.exists()) {
                setErrorMessage(NLS.bind(Messages.NewContentsPage_msgRootNoIPSSrcFolder, root.getName()));
            }
        }
    }

    /**
     * The method validates the source folder.
     */
    private void validatePackage() {
        IIpsPackageFragment pack = packageControl.getIpsPackageFragment();
        if (pack != null && !pack.exists()) {
            setErrorMessage(NLS.bind(Messages.NewContentsPage_msgPackageMissing, pack.getName()));
        }
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(sourceFolderControl.getText()) //$NON-NLS-1$
                && !"".equals(structureControl.getText()) //$NON-NLS-1$
                && !"".equals(contentsField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    public ITableStructure getTableStructure() throws CoreException {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        return (ITableStructure)root.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE,
                structureControl.getText());
    }

    /**
     * @return
     * @throws CoreException
     */
    // TODO rg: rename to reflect that new file is created
    public ITableContents getTableContents() throws CoreException {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        ITableStructure structure = getTableStructure();

        IIpsPackageFragment pack = root.createPackageFragment(packageControl.getText(), true, null);
        ITableContents contents = (ITableContents)pack.createIpsFile(IpsObjectType.TABLE_CONTENTS,
                this.contentsField.getText(), true, null).getIpsObject();
        contents.setTableStructure(structureControl.getText());

        while (contents.getNumOfColumns() < structure.getNumOfColumns()) {
            contents.newColumn(null);
        }

        // at the moment, generations are not really supported by tablecontents, so
        // we set a dummy value for the valid-from for the new generation.
        contents.newGeneration(new GregorianCalendar());

        return contents;
    }
}
