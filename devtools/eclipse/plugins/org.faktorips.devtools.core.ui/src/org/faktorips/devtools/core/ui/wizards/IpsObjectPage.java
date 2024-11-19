/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.apache.commons.text.WordUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * A page that provides basic functionality for creating ips objects. Allows the user to specify the
 * ipssourcefolder, the ips package and the object's name.
 * <p>
 * Subclasses can specify an image for the page by setting it with the
 * <code>setImageDescriptor()</code> method within the subclass constructor. Alternatively the image
 * can also be set in the constructor of the wizard if the wizard contains only one page or if the
 * image doesn't change when the page within the wizard changes.
 */
public abstract class IpsObjectPage extends AbstractIpsObjectNewWizardPage implements ValueChangeListener {

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // edit controls
    private IpsPckFragmentRootRefControl sourceFolderControl;
    private IpsPckFragmentRefControl packageControl;

    // edit fields
    private TextField nameField;
    private TextButtonField sourceFolderField;
    private TextButtonField packageField;

    // page control as defined by the wizard page class
    private Composite pageControl;

    // composite holding the control for the object's name.
    // subclasses can add their own controls.
    private Composite nameComposite;

    // further composite for subclasses to fill controls into
    private Composite additionalComposite;

    private IpsObjectType ipsObjectType;

    /**
     * @param ipsObjectType the object type to show must not be null
     * @param selection a structure selection
     * @param pageName the name of the page
     *
     */
    public IpsObjectPage(IpsObjectType ipsObjectType, IStructuredSelection selection, String pageName) {
        super(selection, pageName);
        ArgumentCheck.notNull(ipsObjectType, this);
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    protected Control createControlInternal(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(NLS.bind(Messages.NewIpsObjectWizard_title, WordUtils.capitalize(ipsObjectType.getDisplayName())));
        setMessage(NLS.bind(Messages.IpsObjectPage_msgNew, getIpsObjectType().getDisplayName()));

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

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.IpsObjectPage_labelSrcFolder);
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

        toolkit.createFormLabel(locationComposite, Messages.IpsObjectPage_labelPackage);
        packageControl = toolkit.createPdPackageFragmentRefControl(locationComposite);
        packageField = new TextButtonField(packageControl);
        packageField.addChangeListener(this);

        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameComposite = toolkit.createLabelEditColumnComposite(pageControl);
        fillNameComposite(nameComposite, toolkit);
        if (useAdditionalComposite()) {
            line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
            line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            additionalComposite = toolkit.createComposite(pageControl);
            fillAdditionalComposite(additionalComposite, toolkit);
        }
        validateInput = true;
        return pageControl;
    }

    /**
     * Returns qualified name of the IpsObject that is about to be created by means of this page.
     */
    public String getQualifiedIpsObjectName() {
        StringBuilder sb = new StringBuilder();
        if (!IpsStringUtils.isEmpty(getPackage())) {
            sb.append(getPackage());
            sb.append('.');
        }
        sb.append(getIpsObjectName());

        return sb.toString();
    }

    /**
     * If the provided message parameter is not <code>null</code> this method sets the text of the
     * message object as error message text of this page. <code>null</code> as a parameter value
     * will be ignored.
     */
    public void setErrorMessage(Message msg) {
        if (msg != null) {
            setErrorMessage(msg.getText());
        }
    }

    public void setMessage(Message msg) {
        setMessage(msg.getText(), UIToolkit.convertToJFaceSeverity(msg.getSeverity()));
    }

    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        Text nameText = addNameLabelField(toolkit, nameComposite);
        nameText.setFocus();
    }

    /**
     * Empty implementation, subclasses may overwrite.
     * <p>
     * There is no layout set for the additional composite, so subclasses must first set a layout to
     * the <code>additionalComposite</code>.
     *
     * @param additionalComposite the additional composite to fill
     * @param toolkit the toolkit to get widgets
     */
    protected void fillAdditionalComposite(Composite additionalComposite, UIToolkit toolkit) {
        // may be override by subclass
    }

    /**
     * Use the additional composite to show controls or not? Default is <code>false</code>,
     * overwrite to change.
     */
    protected boolean useAdditionalComposite() {
        return false;
    }

    protected Text addNameLabelField(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, Messages.IpsObjectPage_labelName);
        return addNameField(toolkit, parent);
    }

    protected Text addNameField(UIToolkit toolkit, Composite parent) {
        Text nameText = toolkit.createText(parent);
        nameField = new TextField(nameText);
        nameField.addChangeListener(this);

        return nameText;
    }

    @Override
    public String getIpsObjectName() {
        return nameField == null ? null : nameField.getText();
    }

    public void setIpsObjectName(String newName) {
        nameField.setText(newName);
    }

    public String getPackage() {
        return packageField.getText();
    }

    public void setPackage(String packageName) {
        packageField.setText(packageName);
    }

    public String getSourceFolder() {
        return sourceFolderField.getText();
    }

    public void setSourceFolder(String sourceFolder) {
        sourceFolderField.setText(sourceFolder);
    }

    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return sourceFolderControl.getIpsPackageFragmentRoot();
    }

    protected void sourceFolderChanged() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPackageFragmentRoot();
        packageControl.setIpsPckFragmentRoot(root);
    }

    protected void packageChanged() {
        // may be override by subclasses
    }

    protected void nameChanged() {
        // may be override by subclasses
    }

    @Override
    protected void setIpsPackageFragment(IIpsPackageFragment pack) {
        packageControl.setIpsPackageFragment(pack);
    }

    @Override
    public IIpsPackageFragment getIpsPackageFragment() {
        return packageControl.getIpsPackageFragment();
    }

    protected IIpsProject getIpsProject() {
        if (getIpsPackageFragmentRoot() == null) {
            return null;
        }

        return getIpsPackageFragmentRoot().getIpsProject();
    }

    @Override
    protected IpsObjectType getIpsObjectType() {
        if (ipsObjectType == null) {
            return ((INewIpsObjectWizard)getWizard()).getIpsObjectType();
        }
        return ipsObjectType;
    }

    public void setIpsObjectType(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    protected Composite getNameComposite() {
        return nameComposite;
    }

    @Override
    public final void valueChanged(FieldValueChangedEvent e) {
        if (e.field == sourceFolderField) {
            sourceFolderChanged();
        }
        if (e.field == packageField) {
            packageChanged();
        }
        if (e.field == nameField) {
            nameChanged();
        }
        try {
            valueChangedExtension(e);
        } catch (IpsException exception) {
            IpsPlugin.log(exception);
        }

        if (validateInput) {
            // don't validate during control creating!
            try {
                validatePage();
            } catch (IpsException coreEx) {
                IpsPlugin.logAndShowErrorDialog(coreEx);
            }

        }
        updatePageComplete();
    }

    /**
     * This method is empty by default. Subclasses can override it when they register a subclass
     * specific <code>org.faktorips.devtools.core.ui.controller.EditField</code>s with the this
     * page's value change lister to take appropriate action if the control's value has changed.
     *
     * @param e the event containing the changed field
     *
     * @throws IpsException in case of exception
     */
    protected void valueChangedExtension(FieldValueChangedEvent e) {
        // may be override by subclasses
    }

    /**
     * Validates the page and generates error messages that are displayed in the message area of the
     * wizard container. If subclasses what to add further validations they can override the
     * validatePageExtension() Method. The validationPageExtension() method is called by this method
     * before the page get updated. This method is protected because subclasses might need to call
     * it within event scenarios implemented within the subclass.
     */
    public final void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage((String)null);
        validateSourceRoot();

        if (getErrorMessage() != null) {
            updatePageComplete();
            return;
        }

        validatePackage();
        if (getErrorMessage() != null) {
            updatePageComplete();
            return;
        }

        validateName();
        validatePageExtension();
        updatePageComplete();
    }

    /**
     * This method is empty by default. Subclasses might override it to add specific validations.
     * This method is called by the validatePage() method before the page will be updated.
     *
     * @throws IpsException if these exceptions are thrown by subclasses they will be logged and
     *             displayed in an error dialog
     */
    protected void validatePageExtension() {
        // may be override by subclasses
    }

    /**
     * The method validates the package.
     */
    private void validateSourceRoot() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPackageFragmentRoot();
        if (root != null) {
            if (!root.getCorrespondingResource().exists()) {
                setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgRootMissing, root.getName()));
            } else if (!root.exists()) {
                setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgRootNoIPSSrcFolder, root.getName()));
            }
        } else {
            if (sourceFolderControl.getText().length() == 0) {
                setErrorMessage(Messages.IpsObjectPage_msgRootRequired);
            } else {
                setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgRootMissing, sourceFolderControl.getText()));
            }
        }
    }

    /**
     * The method validates the source folder.
     */
    private void validatePackage() {
        IIpsPackageFragment pack = packageControl.getIpsPackageFragment();
        if (pack == null) {
            setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgInvalidPackage, packageField.getText()));
        } else if (!pack.exists()) {
            setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgPackageMissing, pack.getName()));
        }
    }

    /**
     * The method validates the name.
     * <p>
     * Subclasses may extend this method to perform their own validation.
     * </p>
     *
     * @throws IpsException in case of exception
     */
    protected void validateName() {
        if (getIpsProject() == null) {
            return;
        }

        // validate naming conventions
        String name = nameField.getText();
        IIpsProjectNamingConventions namingConventions = getIpsProject().getNamingConventions();
        MessageList ml;
        try {
            ml = namingConventions.validateUnqualifiedIpsObjectName(getIpsObjectType(), name);
            if (ml.size() > 0) {
                String msgText = ml.getFirstMessage(ml.getSeverity()).getText();
                if (ml.getSeverity() == Message.ERROR) {
                    setErrorMessage(msgText);
                    return;
                } else if (ml.getSeverity() == Message.WARNING) {
                    setMessage(msgText, IMessageProvider.WARNING);
                } else if (ml.getSeverity() == Message.INFO) {
                    setMessage(msgText, IMessageProvider.INFORMATION);
                } else {
                    setMessage(msgText, IMessageProvider.NONE);
                }
            }
        } catch (IpsException e) {
            // an error occurred while validating the name
            IpsPlugin.logAndShowErrorDialog(e);
        }

        // check if an ipsobject already exists that has the same name and generates a java class
        // to avoid conflicts with java classes that have the same name
        IIpsSrcFile file = findExistingIpsSrcFile();
        if (file != null) {
            StringBuilder msg = new StringBuilder();
            msg.append(Messages.IpsObjectPage_msgIpsObjectAlreadyExists1);
            msg.append(' ');
            if (file.getIpsObjectType().equals(getIpsObjectType())) {
                msg.append(Messages.IpsObjectPage_msgIpsObjectAlreadyExists2);
                msg.append(' ');
                msg.append(getIpsObjectType().getDisplayName());
            } else {
                msg.append(Messages.IpsObjectPage_msgIpsObjectAlreadyExists3);
            }
            msg.append(' ');
            msg.append(Messages.IpsObjectPage_msgIpsObjectAlreadyExists4);
            if (getIpsProject() != null && !getIpsProject().equals(file.getIpsProject())) {
                msg.append(' ');
                msg.append(Messages.IpsObjectPage_msgIpsObjectAlreadyExists5);
                msg.append(' ');
                msg.append(file.getIpsProject().getName());
            }
            msg.append('.');
            setErrorMessage(msg.toString());
        }
    }

    private IIpsSrcFile findExistingIpsSrcFile() {
        IIpsSrcFile file = null;
        IIpsPackageFragment ipsPackageFragment = getIpsPackageFragment();
        if (ipsPackageFragment != null) {
            IIpsSrcFile[] ipsSrcFiles = ipsPackageFragment.getIpsSrcFiles();
            for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                if (ipsSrcFile.getIpsObjectName().equalsIgnoreCase(getIpsObjectName())) {
                    file = ipsSrcFile;
                }
            }
        }
        return file;
    }

    @Override
    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(sourceFolderControl.getText()) //$NON-NLS-1$
                && !"".equals(nameField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }

    @Override
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) {
        IProgressMonitor splitMonitor = monitor instanceof SubMonitor submonitor ? submonitor.split(1) : monitor;
        return getIpsPackageFragment().createIpsFile(getIpsObjectType(), getIpsObjectName(), true, splitMonitor);
    }

    /**
     * Sets the focus to the source folder control if empty if not to the name control.
     */
    @Override
    protected void setDefaultFocus() {
        if (IpsStringUtils.isEmpty(sourceFolderField.getText())) {
            sourceFolderField.getControl().setFocus();
            return;
        }
        // the package field is not considered at this point since it can be empty which means it is
        // the default package
        nameField.getControl().setFocus();
    }

    protected IpsPckFragmentRootRefControl getSourceFolderControl() {
        return sourceFolderControl;
    }

    protected IpsPckFragmentRefControl getPackageControl() {
        return packageControl;
    }

    @Override
    protected void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        sourceFolderControl.setIpsPackageFragmentRoot(root);
    }

}
