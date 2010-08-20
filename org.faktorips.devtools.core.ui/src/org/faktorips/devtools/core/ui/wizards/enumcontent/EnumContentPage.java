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

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumContentValidations;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.wizards.AbstractIpsObjectNewWizardPage;
import org.faktorips.util.message.MessageList;

/**
 * The wizard page for the <code>NewEnumContentWizard</code>.
 * 
 * @see NewEnumContentWizard
 * 
 * @author Alexander Weickmann, Peter Kuntz
 * 
 * @since 2.3
 */
public class EnumContentPage extends AbstractIpsObjectNewWizardPage implements ValueChangeListener {

    /** The image for the wizard page. */
    private final String PAGE_IMAGE = "wizards/NewEnumContentWizard.png"; //$NON-NLS-1$

    /**
     * The text field to choose the <tt>IEnumType</tt> on which the new <tt>IEnumContent</tt> shall
     * be based upon.
     */
    private TextButtonField enumTypeField;

    private TextButtonField sourceFolderField;

    private IpsPckFragmentRootRefControl sourceFolderControl;

    /**
     * Flag that is <tt>true</tt> if the input is validated and errors are displayed in the messages
     * area.
     */
    protected boolean validateInput = true;

    /** Page control as defined by the wizard page class. */
    private Composite pageControl;

    private IEnumContent createdEnumContent;

    /**
     * Creates the <tt>EnumContentEditorPage</tt>.
     * 
     * @param selection If a selection is provided default values for some fields can be derived
     *            from that.
     */
    public EnumContentPage(IStructuredSelection selection) {
        super(selection, Messages.Page_Title);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(PAGE_IMAGE));

    }

    @Override
    protected Control createControlInternal(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(NLS.bind(org.faktorips.devtools.core.ui.wizards.Messages.NewIpsObjectWizard_title, WordUtils
                .capitalize(IpsObjectType.ENUM_CONTENT.getDisplayName())));
        setMessage(NLS.bind(org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgNew,
                IpsObjectType.ENUM_CONTENT.getDisplayName()));

        pageControl = new Composite(parent, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        pageControl.setLayoutData(data);
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite,
                org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_labelSrcFolder);
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

        // EnumType.
        toolkit.createFormLabel(locationComposite, Messages.Fields_EnumType + ':');
        IpsObjectRefControl enumTypeControl = toolkit.createEnumTypeRefControl(null, locationComposite, false);
        enumTypeField = new TextButtonField(enumTypeControl);
        enumTypeField.addChangeListener(this);
        validateInput = true;
        return pageControl;
    }

    @Override
    protected void setDefaultsExtension(IResource selectedResource) throws CoreException {
        if (selectedResource == null) {
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsSrcFile) {
            IIpsObject ipsObject = ((IIpsSrcFile)element).getIpsObject();
            if (ipsObject instanceof IEnumType) {
                IEnumType enumType = (IEnumType)ipsObject;
                if (!(enumType.isAbstract()) && !(enumType.isContainingValues())) {
                    enumTypeField.setText(enumType.getQualifiedName());
                }
            }
        }
    }

    @Override
    protected void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        sourceFolderControl.setPdPckFragmentRoot(root);
    }

    private String getSourceFolder() {
        return sourceFolderField.getText();
    }

    @Override
    protected void setDefaultFocus() {
        if (StringUtils.isEmpty(getSourceFolder())) {
            sourceFolderControl.setFocus();
        }
    }

    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return sourceFolderControl.getIpsPckFragmentRoot();
    }

    @Override
    public IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IEnumType enumType = getEnumType();
        if (enumType != null) {
            IIpsPackageFragment packageFrag = getIpsPackageFragment();
            if (!packageFrag.exists()) {
                packageFrag = getIpsPackageFragmentRoot().createPackageFragment(packageFrag.getName(), false, monitor);
            }
            IIpsSrcFile createdIpsSrcFile = packageFrag.createIpsFile(IpsObjectType.ENUM_CONTENT, getIpsObjectName(),
                    true, monitor);
            createdEnumContent = (IEnumContent)createdIpsSrcFile.getIpsObject();
            return createdIpsSrcFile;
        }
        return null;
    }

    protected void sourceFolderChanged() {
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)enumTypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)enumTypeField.getControl()).setIpsProject(null);
        }
    }

    @Override
    protected void finishIpsObjectsExtension(IIpsObject newIpsObject, Set<IIpsObject> modifiedIpsObjects)
            throws CoreException {

        IEnumContent newEnumContent = (IEnumContent)newIpsObject;
        newEnumContent.setEnumType(enumTypeField.getText());

        modifiedIpsObjects.add(newEnumContent);
        newEnumContent.getIpsSrcFile().markAsDirty();
    }

    /**
     * Validates the page and generates error messages that are displayed in the message area of the
     * wizard container.
     */
    public final void validatePage() throws CoreException {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage((String)null);
        validateSourceRoot();

        if (getErrorMessage() != null) {
            updatePageComplete();
            return;
        }
        validatePageInternal();
        updatePageComplete();
    }

    private void validatePageInternal() throws CoreException {
        String enumTypeFieldText = enumTypeField.getText();
        if (StringUtils.isEmpty(enumTypeFieldText)) {
            setErrorMessage(Messages.EnumContentPage_msgEnumTypeMissing);
            return;
        }
        if (getEnumType() == null) {
            setErrorMessage(Messages.EnumContentPage_msgEnumTypeNotExisting);
            return;
        }
        IEnumType enumType = getEnumType();
        if (StringUtils.isEmpty(enumType.getEnumContentName())) {
            setErrorMessage(Messages.EnumContentPage_msgEnumContentNameOfEnumTypeMissing);
            return;
        }
        IEnumContent enumContent = getIpsPackageFragmentRoot().getIpsProject().findEnumContent(enumType);
        if (enumContent != null) {
            setErrorMessage(Messages.EnumContentPage_msgEnumContentAlreadyExists);
            return;
        }
        IIpsSrcFile enumContentSrcFile = getIpsPackageFragmentRoot().getIpsProject().findIpsSrcFile(
                IpsObjectType.ENUM_CONTENT, enumType.getEnumContentName());
        if (enumContentSrcFile != null) {
            setErrorMessage(NLS.bind(Messages.EnumContentPage_msgEnumContentExistsForNameExistsAlready, enumType
                    .getEnumContentName(), enumType.getQualifiedName()));
            return;
        }

        /*
         * Some of the validations in the following method are already made above. This is because
         * the text messages a designed to meet the needs of the wizard user interface.
         */
        MessageList msgList = new MessageList();
        EnumContentValidations.validateEnumType(msgList, null, enumTypeFieldText, getIpsPackageFragmentRoot()
                .getIpsProject());
        if (!msgList.isEmpty()) {
            setErrorMessage(msgList.getMessage(0).getText());
        }
    }

    /**
     * Returns the selected <tt>IEnumType</tt> which defines the structure for the
     * <tt>IEnumContent</tt> to be created.
     */
    public IEnumType getEnumType() {
        try {
            IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
            return (IEnumType)root.getIpsProject().findIpsObject(IpsObjectType.ENUM_TYPE, enumTypeField.getText());
        } catch (CoreException e) {
            // Page controls are currently invalid, return null.
            return null;
        }
    }

    /**
     * Returns the <tt>IEnumContent</tt> that has been created by this page or <tt>null</tt> if it
     * has not been created yet.
     */
    public IEnumContent getCreatedEnumContent() {
        return createdEnumContent;
    }

    @Override
    public final void valueChanged(FieldValueChangedEvent e) {
        if (e.field == sourceFolderField) {
            sourceFolderChanged();
        }
        if (validateInput) { // Don't validate during control creating!
            try {
                validatePage();
            } catch (CoreException coreEx) {
                IpsPlugin.logAndShowErrorDialog(coreEx);
            }

        }
        updatePageComplete();
    }

    /** Validates the package. */
    private void validateSourceRoot() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getIpsPckFragmentRoot();
        if (root != null) {
            if (!root.getCorrespondingResource().exists()) {
                setErrorMessage(NLS.bind(org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgRootMissing,
                        root.getName()));
            } else if (!root.exists()) {
                setErrorMessage(NLS.bind(
                        org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgRootNoIPSSrcFolder, root
                                .getName()));
            }
        } else {
            if (sourceFolderControl.getText().length() == 0) {
                setErrorMessage(org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgRootRequired);
            } else {
                setErrorMessage(NLS.bind(org.faktorips.devtools.core.ui.wizards.Messages.IpsObjectPage_msgRootMissing,
                        sourceFolderControl.getText()));
            }
        }
    }

    @Override
    protected boolean canCreateIpsSrcFile() {
        return true;
    }

    @Override
    protected String getIpsObjectName() {
        IEnumType enumType = getEnumType();
        if (enumType != null) {
            String enumContentName = enumType.getEnumContentName();
            if (enumContentName == null) {
                return null;
            }
            int index = enumContentName.lastIndexOf('.');
            if (index != -1) {
                return enumContentName.substring(index + 1, enumContentName.length());
            }
            return enumContentName;
        }
        return null;
    }

    @Override
    protected IpsObjectType getIpsObjectType() {
        return IpsObjectType.ENUM_CONTENT;
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        IEnumType enumType = getEnumType();
        if (enumType != null) {
            String enumContentName = enumType.getEnumContentName();
            if (enumContentName == null) {
                return null;
            }
            int index = enumContentName.lastIndexOf('.');
            if (index != -1) {
                String enumContentPackage = enumContentName.substring(0, index);
                return getIpsPackageFragmentRoot().getIpsPackageFragment(enumContentPackage);
            }
            return getIpsPackageFragmentRoot().getDefaultIpsPackageFragment();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Not used.
     */
    @Override
    protected void setIpsPackageFragment(IIpsPackageFragment pack) {
        // nothing to do
    }

}
