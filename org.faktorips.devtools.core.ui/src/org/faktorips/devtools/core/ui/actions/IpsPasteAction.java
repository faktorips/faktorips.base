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

package org.faktorips.devtools.core.ui.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.wizards.productcmpt.CopyProductCmptWizard;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.StringUtil;

/**
 * Action to paste IpsElements or resources.
 * 
 * @author Thorsten Guenther
 */
public class IpsPasteAction extends IpsAction {

    /**
     * The clipboard used to transfer the data
     */
    private Clipboard clipboard;

    /**
     * The shell for this session
     */
    private Shell shell;

    /**
     * Indicates that the new name will be used without a dialog question, if the file already
     * exists
     */
    private boolean forceUseNameSuggestionIfFileExists = false;

    /**
     * Creates a new action to paste <code>IIpsElement</code>s or resources.
     * 
     * @param selectionProvider The provider for the selection to get the target from.
     * @param shell The shell for this session.
     */
    public IpsPasteAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
        this.shell = shell;
    }

    /**
     * Sets that new name suggestions will be used without interaction with the user (no ui dialog)
     * if there are existing files.
     */
    public void setForceUseNameSuggestionIfFileExists(boolean forceUseNameSuggestionIfFileExists) {
        this.forceUseNameSuggestionIfFileExists = forceUseNameSuggestionIfFileExists;
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IIpsObjectPartContainer) {
            paste((IIpsObjectPartContainer)selected);
        } else if (selected instanceof IIpsProject) {
            paste(((IIpsProject)selected).getProject());
        } else if (selected instanceof IIpsPackageFragmentRoot) {
            paste(((IIpsPackageFragmentRoot)selected).getDefaultIpsPackageFragment());
        } else if (selected instanceof IIpsPackageFragment) {
            paste((IIpsPackageFragment)selected);
        } else if (selected instanceof IContainer) {
            paste((IContainer)selected);
        }
    }

    /**
     * Try to paste an <code>IIpsObject</code> to an <code>IIpsObjectPartContainer</code>. If it is
     * not possible because the stored data does not support this (e.g. is a resource and not a
     * string) paste(IIpsPackageFragement) is called.
     * 
     * @param parent The parent to paste to.
     */
    private void paste(IIpsObjectPartContainer parent) {
        String stored = (String)clipboard.getContents(TextTransfer.getInstance());

        // obtain the package fragment of the given part container
        IIpsPackageFragment parentPackageFrgmt = null;
        IIpsElement pack = parent.getParent();
        while (pack != null && !(pack instanceof IIpsPackageFragment)) {
            pack = pack.getParent();
        }
        if (pack != null) {
            parentPackageFrgmt = (IIpsPackageFragment)pack;
        }

        if (stored == null && parentPackageFrgmt != null) {
            // the clipboard contains no string, try to paste resources
            paste(parentPackageFrgmt);
        } else {
            // try to paste resource links
            if (parentPackageFrgmt != null && pasteResourceLinks(parentPackageFrgmt, stored)) {
                // the copied text contains links, paste is finished
                return;
            }
            // no links in string try to paste ips object parts
            try {
                IpsObjectPartState state = new IpsObjectPartState(stored);
                state.newPart(parent);
            } catch (RuntimeException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /**
     * Try to paste an <code>IFolder</code> or <code>IFile</code> stored in the clipboard into the
     * given <code>IContainer</code>.
     */
    private void paste(IContainer parent) {
        Object stored = getTransgeredObject();
        if (stored instanceof IResource[]) {
            IResource[] res = (IResource[])stored;
            for (IResource re : res) {
                try {
                    copy(parent, re);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
        // Paste objects by resource links (e.g. files inside an ips archive)
        String storedText = (String)clipboard.getContents(TextTransfer.getInstance());
        if (parent instanceof IFolder) {
            pasteResourceLinks((IFolder)parent, storedText);
        }
    }

    /**
     * Try to paste the <code>IResource</code> stored on the clipboard to the given parent.
     */
    private void paste(IIpsPackageFragment parent) {
        Object stored = getTransgeredObject();
        if (stored instanceof IResource[]) {
            copyResources(parent, (IResource[])stored);
        } else if (stored instanceof String[]) {
            copyFiles(parent, (String[])stored);
        }

        // Paste objects by resource links (e.g. files inside an ips archive)
        String storedText = (String)clipboard.getContents(TextTransfer.getInstance());
        pasteResourceLinks(parent, storedText);
    }

    private Object getTransgeredObject() {
        Object stored = clipboard.getContents(ResourceTransfer.getInstance());
        if (stored == null) {
            stored = clipboard.getContents(FileTransfer.getInstance());
        }
        return stored;
    }

    private void copyResources(IIpsPackageFragment parent, IResource[] resources) {
        for (IResource resource2 : resources) {
            try {
                IResource resource = ((IIpsElement)parent).getCorrespondingResource();
                if (resource != null) {
                    copy(resource, resource2);
                } else {
                    showPasteNotSupportedError();
                }
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    private void copyFiles(IIpsPackageFragment parent, String[] fileNames) {
        IResource destinationResource = parent.getEnclosingResource();
        if (destinationResource instanceof IContainer) {
            copyFiles(shell, (IFolder)destinationResource, fileNames);
        }
    }

    public static void copyFiles(Shell shell, IContainer parentFolder, String[] fileNames) {
        CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(shell);
        operation.copyFiles(fileNames, parentFolder);
    }

    /**
     * Try to paste resource links, if the given text contains no such links do nothing. Rerurns
     * true if the text contains resource links otherwise return false.
     */
    private boolean pasteResourceLinks(IIpsPackageFragment parent, String storedText) {
        boolean result = false;
        Object[] resourceLinks = getObjectsFromResourceLinks(storedText);
        try {
            if (resourceLinks.length > 0) {
                result = true;
            }
            for (Object resourceLink : resourceLinks) {
                if (resourceLink instanceof IIpsObject) {
                    createFile(parent, (IIpsObject)resourceLink);
                } else if (resourceLink instanceof IIpsPackageFragment) {
                    IIpsPackageFragment packageFragment = (IIpsPackageFragment)resourceLink;
                    createPackageFragmentAndChilds(parent, packageFragment);
                } else {
                    showPasteNotSupportedError();
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return result;
    }

    /**
     * Try to paste resource links, if the given text contains no such links do nothing. Rerurns
     * true if the text contains resource links otherwise return false.
     */
    private boolean pasteResourceLinks(IFolder folder, String storedText) {
        boolean result = false;
        Object[] resourceLinks = getObjectsFromResourceLinks(storedText);
        try {
            if (resourceLinks.length > 0) {
                result = true;
            }
            for (Object resourceLink : resourceLinks) {
                if (resourceLink instanceof IIpsObject) {
                    createFile(folder, (IIpsObject)resourceLink);
                } else if (resourceLink instanceof IIpsPackageFragment) {
                    IIpsPackageFragment packageFragment = (IIpsPackageFragment)resourceLink;
                    createFolderAndFiles(folder, packageFragment);
                } else {
                    showPasteNotSupportedError();
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return result;
    }

    private String getContentsOfIpsObject(IIpsObject ipsObject) {
        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        String contents;
        try {
            contents = XmlUtil.nodeToString(ipsObject.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument()),
                    encoding);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
            // This is a programing error, rethrow as runtime exception
        }
        return contents;
    }

    /**
     * Creates a new file in the parent package fragment based on the given ips source object.
     */
    private void createFile(IIpsPackageFragment parent, IIpsObject ipsObject) throws CoreException {
        String contents = getContentsOfIpsObject(ipsObject);

        String ipsSrcFileName = getNewIpsSrcFileName((IFolder)parent.getCorrespondingResource(), ipsObject);
        if (ipsSrcFileName == null) {
            return;
        }
        parent.createIpsFile(ipsSrcFileName, contents, true, null);
    }

    /**
     * Creates a new file in the parent folder based on the given ips source object.
     */
    private void createFile(IFolder parent, IIpsObject ipsObject) throws CoreException {
        String contents = getContentsOfIpsObject(ipsObject);
        InputStream is;
        try {
            is = new ByteArrayInputStream(contents.getBytes(ipsObject.getIpsProject().getXmlFileCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }

        String newIpsSrcFileName = getNewIpsSrcFileName(parent, ipsObject);
        if (newIpsSrcFileName == null) {
            return;
        }

        IFile file = parent.getFile(newIpsSrcFileName);
        file.create(is, true, null);
    }

    /**
     * Creates a new package fragment and childs in the parent package fragment based on the given
     * source package fragment.
     */
    private void createPackageFragmentAndChilds(IIpsPackageFragment parent, IIpsPackageFragment sourcePackageFragment)
            throws CoreException {

        String packageName = sourcePackageFragment.getLastSegmentName();
        IIpsPackageFragment destination = parent.createSubPackage(packageName, true, null);
        IIpsElement[] children = sourcePackageFragment.getChildren();
        for (IIpsElement element : children) {
            if (element instanceof IIpsSrcFile) {
                IIpsObject ipsObject = ((IIpsSrcFile)element).getIpsObject();
                createFile(destination, ipsObject);
            }
        }
        IIpsPackageFragment[] childPackages = sourcePackageFragment.getChildIpsPackageFragments();
        for (IIpsPackageFragment childPackage : childPackages) {
            createPackageFragmentAndChilds(destination, childPackage);
        }
    }

    /**
     * Returns a new unique ips source file name, returns null if the user aborts the get new name
     * for duplicate souce file dialog.
     */
    private String getNewIpsSrcFileName(IFolder parent, IIpsObject ipsObject) {
        String nameWithoutExtension = ipsObject.getName();
        String extension = "." + StringUtil.getFileExtension(ipsObject.getIpsSrcFile().getName()); //$NON-NLS-1$
        IPath targetPath = parent.getFullPath();
        return getNewNameByDialogIfNecessary(IResource.FILE, targetPath, nameWithoutExtension, extension, false);
    }

    /**
     * Returns a new name for folder or files, returns null if the user aborts the get new name for
     * duplicate souce file dialog.
     */
    private String getNewNameByDialogIfNecessary(int resourceType,
            IPath targetPath,
            String nameWithOrWithoutExtension,
            String extension,
            boolean showExtension) {

        boolean dialogWasDisplayed = false;
        Validator validator = new Validator(targetPath, resourceType, extension);
        String suggestedName = nameWithOrWithoutExtension;
        int doCopy = Window.OK;
        boolean nameChangeRequired = validator.isValid(nameWithOrWithoutExtension) != null;
        if (nameChangeRequired) {
            for (int count = 0; validator.isValid(suggestedName) != null; count++) {
                if (count == 0) {
                    suggestedName = Messages.IpsPasteAction_suggestedNamePrefixSimple + nameWithOrWithoutExtension;
                } else {
                    suggestedName = NLS.bind(Messages.IpsPasteAction_suggestedNamePrefixComplex, new Integer(count),
                            nameWithOrWithoutExtension);
                }
            }
            nameWithOrWithoutExtension = suggestedName;

            // if force is true don't show dialog (for automated testing purposite force could be
            // set to true)
            if (!forceUseNameSuggestionIfFileExists) {
                dialogWasDisplayed = true;
                suggestedName += showExtension ? extension : ""; //$NON-NLS-1$
                InputDialog dialog = new InputDialog(shell, Messages.IpsPasteAction_titleNamingConflict, NLS.bind(
                        Messages.IpsPasteAction_msgNamingConflict, nameWithOrWithoutExtension), suggestedName,
                        validator);
                dialog.setBlockOnOpen(true);
                doCopy = dialog.open();
                nameWithOrWithoutExtension = dialog.getValue();
                if (doCopy != Window.OK) {
                    return null;
                }
            }
        }
        if (showExtension && dialogWasDisplayed) {
            // the extension was already shown in the dialog
            return nameWithOrWithoutExtension;
        } else {
            return nameWithOrWithoutExtension + extension;
        }
    }

    /**
     * Create a new folder in the parent folder, based on the given source package fragment. Creates
     * all childs of the given source package fragment .
     */
    private void createFolderAndFiles(IFolder targetParentFolder, IIpsPackageFragment sourcePackageFragment)
            throws CoreException {

        String packageName = sourcePackageFragment.getLastSegmentName();
        IPath targetPath = targetParentFolder.getFullPath();
        packageName = getNewNameByDialogIfNecessary(IResource.FOLDER, targetPath, packageName, "", false); //$NON-NLS-1$
        if (packageName == null) {
            return;
        }

        IFolder subFolder = targetParentFolder.getFolder(packageName);
        subFolder.create(true, true, null);
        IIpsElement[] children = sourcePackageFragment.getChildren();
        for (IIpsElement element : children) {
            if (element instanceof IIpsSrcFile) {
                IIpsObject ipsObject = ((IIpsSrcFile)element).getIpsObject();
                createFile(subFolder, ipsObject);
            }
        }
        IIpsPackageFragment[] childPackages = sourcePackageFragment.getChildIpsPackageFragments();
        for (IIpsPackageFragment childPackage : childPackages) {
            createFolderAndFiles(subFolder, childPackage);
        }
    }

    private void showPasteNotSupportedError() {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.IpsPasteAction_errorTitle,
                Messages.IpsPasteAction_Error_CannotPasteIntoSelectedElement);
    }

    /**
     * Copy the given resource to the given target path.
     * 
     * @throws CoreException If copy failed.
     */
    private void copy(IResource target, IResource resource) throws CoreException {
        if (target == null) {
            return;
        }
        IPath targetPath = target.getFullPath();

        String name = resource.getName();
        String extension = StringUtil.getFileExtension(name);
        if (extension != null) {
            extension = "." + extension; //$NON-NLS-1$
        } else {
            extension = ""; //$NON-NLS-1$
        }
        String suggestedName = StringUtil.getFilenameWithoutExtension(name);

        boolean showExtension = !isResourceIpsObject(resource);

        if (isResourceProductCmpt(resource)) {
            IIpsElement source = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
            copyProductCmptByWizard((IProductCmpt)((IIpsSrcFile)source).getIpsObject(), target);
        } else {
            // non product cmpt
            String newName = getNewNameByDialogIfNecessary(resource.getType(), targetPath, suggestedName, extension,
                    showExtension);
            if (newName != null) {
                try {
                    resource.copy(targetPath.append(newName), true, null);
                } catch (Exception e) {
                    IpsPlugin.showErrorDialog(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                            Messages.IpsPasteAction_cannot_copy, e));
                }
            }
        }
    }

    private void copyProductCmptByWizard(IProductCmpt productCmpt, IResource target) {
        CopyProductCmptWizard wizard = new CopyProductCmptWizard(productCmpt);
        wizard.init(IpsPlugin.getDefault().getWorkbench(), new StructuredSelection(target));
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

    private boolean isResourceProductCmpt(IResource resource) throws CoreException {
        if (resource instanceof IFile) {
            IIpsElement ipsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
            if (ipsElement instanceof IIpsSrcFile && ipsElement.exists()) {
                if (((IIpsSrcFile)ipsElement).getIpsObject() instanceof IProductCmpt) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isResourceIpsObject(IResource resource) {
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(resource.getFullPath());
        IIpsElement ipsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
        if (ipsElement instanceof IIpsSrcFile && ipsElement.exists()) {
            return true;
        }
        return false;
    }

    /**
     * Validator for new resource name.
     * 
     * @author Thorsten Guenther
     */
    private class Validator implements IInputValidator {

        IPath root;
        String extension;
        int resourceType;

        public Validator(IPath root, int resourceType, String extension) {
            this.root = root;
            this.extension = extension;
            this.resourceType = resourceType;
        }

        @Override
        public String isValid(String newText) {
            IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
            IResource test = null;
            if (resourceType == IResource.FILE) {
                test = wsRoot.getFile(root.append(newText + extension));
            } else if (resourceType == IResource.FOLDER) {
                test = wsRoot.getFolder(root.append(newText));
            }
            if (test != null && test.exists()) {
                return newText + extension + Messages.IpsPasteAction_msgFileAllreadyExists;
            }

            return null;
        }
    }

    @Override
    protected boolean computeEnabledProperty(IStructuredSelection selection) {
        // disable action if the selection contains at least one ips object part
        Object[] objects = selection.toArray();
        for (Object object : objects) {
            if (object instanceof IIpsObjectPart) {
                return false;
            }
        }
        return true;
    }

}
