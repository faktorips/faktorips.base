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

package org.faktorips.devtools.core.ui.wizards.move;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Page to let the user enter a new name for the object to rename.
 * 
 * @author Thorsten Guenther
 */
public class RenamePage extends WizardPage implements ModifyListener {

    // The input field holding the complete new name.
    private Text newName;

    // Input holding the version id part of the name.
    private Text versionId;

    // Input for the constant part of the name.
    private Text constNamePart;

    // Input for the runtime id.
    private Text runtimeId;

    // The object to rename
    private IIpsElement renameObject;

    // The page-id to identify this page.
    private static final String PAGE_ID = "MoveWizard.configure"; //$NON-NLS-1$

    // The naming strategy to use for move/rename.
    private IProductCmptNamingStrategy namingStrategy;

    /**
     * Creates a new page to select the objects to copy.
     */
    protected RenamePage(IIpsElement renameObject) {
        super(PAGE_ID, Messages.RenamePage_rename, null);

        this.renameObject = renameObject;

        namingStrategy = renameObject.getIpsProject().getProductCmptNamingStrategy();

        super.setDescription(Messages.RenamePage_msgChooseNewName);
        setPageComplete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);

        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        if (renameObject instanceof IpsPackageFragment || renameObject instanceof ITableContents
                || renameObject instanceof ITestCase) {
            createControlForObject(toolkit, inputRoot, renameObject);
        } else if (renameObject instanceof IProductCmpt) {
            createControlForProduct(toolkit, inputRoot, (IProductCmpt)renameObject);
        } else {
            throw new RuntimeException("Rename not supported for object type " + renameObject.getClass().getName()); //$NON-NLS-1$
        }
        newName.addModifyListener(this);
        setPageComplete();
    }

    /**
     * Creates the input controlls for an IpsObject.
     */
    private void createControlForObject(UIToolkit toolkit, Composite parent, IIpsElement obj) {
        toolkit.createLabel(parent, Messages.RenamePage_newName);
        newName = toolkit.createText(parent);
        if (obj instanceof IpsPackageFragment) {
            newName.setText(((IIpsPackageFragment)obj).getLastSegmentName());
        } else {
            newName.setText(obj.getName());
        }
    }

    /**
     * Creates the input controls for a product component to rename
     */
    private void createControlForProduct(UIToolkit toolkit, Composite parent, IProductCmpt product) {
        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            toolkit.createLabel(parent, Messages.RenamePage_labelConstNamePart);
            constNamePart = toolkit.createText(parent);

            String label = NLS.bind(Messages.RenamePage_labelVersionId, IpsPlugin.getDefault().getIpsPreferences()
                    .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
            toolkit.createLabel(parent, label);
            versionId = toolkit.createText(parent);

            toolkit.createLabel(parent, Messages.RenamePage_labelRuntimeId);
            runtimeId = toolkit.createText(parent);
            runtimeId.setEnabled(false);
            runtimeId.setText(product.getRuntimeId());
            runtimeId.setEnabled(IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId());
            runtimeId.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateFullName();
                }
            });

            toolkit.createLabel(parent, Messages.RenamePage_newName);
            newName = toolkit.createText(parent);
            newName.setEnabled(false);

            versionId.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateFullName();
                }
            });
            constNamePart.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateFullName();
                }
            });
            try {
                versionId.setText(namingStrategy.getVersionId(product.getName()));
                constNamePart.setText(namingStrategy.getKindId(product.getName()));
            } catch (IllegalArgumentException e) {
                constNamePart.setText(product.getName());
                versionId.setText(""); //$NON-NLS-1$
            }

        } else {
            toolkit.createLabel(parent, Messages.RenamePage_newName);
            newName = toolkit.createText(parent);
            newName.setText(renameObject.getName());
        }
    }

    /**
     * Constructs the full name out of the version id and the constant name part using the active
     * product coponent naming strategy.
     */
    private void updateFullName() {
        newName.setText(namingStrategy.getProductCmptName(constNamePart.getText(), versionId.getText()));
    }

    /**
     * If at least one message is contained in the given list, the first message is set as
     * error-message.
     * 
     * @param list The list to look for messages in.
     * @return <code>true</code> if an error message was found and set, <code>false</code>
     *         otherwise.
     */
    private boolean setMessageFromList(MessageList list) {
        if (!list.isEmpty()) {
            String text = list.getFirstMessage(list.getSeverity()).getText();
            if (list.getSeverity() == Message.ERROR) {
                setMessage(text, IMessageProvider.ERROR);
                return true;
            } else if (list.getSeverity() == Message.WARNING) {
                setMessage(text, IMessageProvider.WARNING);
            } else if (list.getSeverity() == Message.INFO) {
                setMessage(text, IMessageProvider.INFORMATION);
            } else {
                setMessage(text, IMessageProvider.NONE);
            }
        }
        return false;
    }

    /**
     * Set the current completion state (and, if neccessary, messages for the user to help him to
     * get the page complete).
     */
    private void setPageComplete() {

        super.setMessage(null);
        super.setPageComplete(false);

        if (newName == null) {
            // page not yet created, do nothing.
            return;
        }

        // validate the name conventions
        if (namingStrategy != null && namingStrategy.supportsVersionId() && versionId != null) {
            // name check for product cmpt
            if (setMessageFromList(namingStrategy.validateVersionId(versionId.getText()))) {
                return;
            }

            if (setMessageFromList(namingStrategy.validateKindId(constNamePart.getText()))) {
                return;
            }
            MessageList ml = new MessageList();

            try {
                validateForRuntimeId(ml);
            } catch (CoreException e) {
                // error during validation show error dialog and exit
                IpsPlugin.logAndShowErrorDialog(e);
                return;
            }
            if (setMessageFromList(ml)) {
                return;
            }
        } else {
            String name = newName.getText();
            IIpsProjectNamingConventions pnc = renameObject.getIpsProject().getNamingConventions();
            try {
                MessageList ml = null;
                if (renameObject instanceof IIpsObject) {
                    // ips object, validate the unqualified ips object name
                    ml = pnc.validateUnqualifiedIpsObjectName(((IIpsObject)renameObject).getIpsObjectType(), name);
                    validateForRuntimeId(ml);
                } else {
                    // no ips object, validate for ips package name
                    ml = pnc.validateIpsPackageName(name);
                }
                if (setMessageFromList(ml)) {
                    return;
                }
            } catch (CoreException e) {
                // error during validation of the name,
                // show error dialog and exit
                IpsPlugin.logAndShowErrorDialog(e);
                return;
            }
        }

        // if the new name is the same as the old name, no error is shown and it
        // also not possible to click finish
        if (newName.getText().equals(renameObject.getName())) {
            return;
        }

        // validate that an object with the name not exists
        IIpsPackageFragment pack = null;
        if (renameObject instanceof IProductCmpt || renameObject instanceof ITableContents
                || renameObject instanceof ITestCase) {
            pack = ((IIpsObject)renameObject).getIpsPackageFragment();
            IIpsSrcFile newFile = pack.getIpsSrcFile(((IIpsObject)renameObject).getIpsObjectType().getFileName(
                    newName.getText()));
            if (newFile.exists()) {
                setMessage(Messages.RenamePage_errorFileExists, ERROR);
                return;
            }
        } else if (renameObject instanceof IIpsPackageFragment) {
            pack = (IIpsPackageFragment)renameObject;
            IFolder folder = (IFolder)pack.getCorrespondingResource();
            IFolder newFolder = folder.getParent().getFolder(new Path(newName.getText()));

            if (newFolder.exists()) {
                setMessage(Messages.RenamePage_errorFolderExists, ERROR);
                return;
            } else {
                // fix for windows: can not rename to an object with a name only
                // different in case.
                if (hasContentWithNameEqualsIgnoreCase(folder.getParent(), newName.getText())) {
                    setMessage(Messages.RenamePage_errorFolderExists, ERROR);
                    return;
                }
            }
        }

        super.setPageComplete(true);
    }

    private void validateForRuntimeId(MessageList ml) throws CoreException {
        if (renameObject instanceof IProductCmpt) {
            // additional validate the runtime id
            IProductCmpt pcmtWithSameRuntimeId = renameObject.getIpsProject().findProductCmptByRuntimeId(
                    runtimeId.getText());
            if (pcmtWithSameRuntimeId != null && pcmtWithSameRuntimeId != renameObject) {
                String text = NLS.bind(Messages.RenamePage_msgRuntimeCollision, newName.getText(),
                        pcmtWithSameRuntimeId.getName());
                Message msg = new Message(IIpsProject.MSGCODE_RUNTIME_ID_COLLISION, text, Message.ERROR, renameObject,
                        IProductCmpt.PROPERTY_RUNTIME_ID);
                ml.add(msg);
            }
        }
    }

    private boolean hasContentWithNameEqualsIgnoreCase(IContainer parentFolder, String name) {
        try {
            IResource[] children = parentFolder.members();
            for (IResource element : children) {
                if (element.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return false;
    }

    /**
     * Returns the name the user has entered. The name is always qualified with the package name of
     * the package containing the object to rename.
     */
    public String getNewName() {
        String name = ""; //$NON-NLS-1$
        if (renameObject instanceof IProductCmpt || renameObject instanceof ITableContents
                || renameObject instanceof ITestCase) {
            name = ((IIpsObject)renameObject).getIpsPackageFragment().getName();
        } else if (renameObject instanceof IIpsPackageFragment) {
            IIpsPackageFragment parent = ((IIpsPackageFragment)renameObject).getParentIpsPackageFragment();
            if (parent != null) {
                name = parent.getName();
            } else {
                name = ""; //$NON-NLS-1$
            }
        }

        if (!name.equals("")) { //$NON-NLS-1$
            name += "."; //$NON-NLS-1$
        }
        return name + newName.getText();
    }

    /**
     * If this page is used to rename a product component, this method returns the new runtime id
     * for the component. Returns <code>null</code> otherwise.
     */
    public String getNewRuntimeId() {
        if (runtimeId != null) {
            return runtimeId.getText();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyText(ModifyEvent e) {
        setPageComplete();
    }
}
