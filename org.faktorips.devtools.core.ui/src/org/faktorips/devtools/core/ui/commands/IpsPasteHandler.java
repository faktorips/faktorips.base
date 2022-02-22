/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.devtools.core.ui.wizards.productcmpt.NewProductCmptWizard;
import org.faktorips.devtools.core.ui.wizards.productcmpt.NewProductTemplateWizard;
import org.faktorips.devtools.core.ui.wizards.productcmpt.NewProductWizard;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.util.StringUtil;

/**
 * A handler to paste IpsObjectPartContainer-objects from the clipboard into the model.
 */
public class IpsPasteHandler extends AbstractCopyPasteHandler {

    private static final IIpsObject[] EMPTY_IPS_OBJECT_ARRAY = new IIpsObject[0];

    private Clipboard clipboard;
    private Shell shell;
    private boolean forceUseNameSuggestionIfFileExists;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelectionChecked(event);
        Clipboard clipboard = new Clipboard(HandlerUtil.getActiveShellChecked(event).getDisplay());
        pasteFromClipboard(selection, clipboard, HandlerUtil.getActiveShellChecked(event), true);
        return null;
    }

    public void pasteFromClipboard(IStructuredSelection selection,
            Clipboard clipboard,
            Shell shell,
            boolean forceUseNameSuggestionIfFileExists) {
        this.clipboard = clipboard;
        this.shell = shell;
        this.forceUseNameSuggestionIfFileExists = forceUseNameSuggestionIfFileExists;
        Object object = selection.getFirstElement();
        if (!(object instanceof IAdaptable)) {
            return;
        }
        IAdaptable adaptable = (IAdaptable)object;
        if (adaptable.getAdapter(IIpsElement.class) != null) {
            IIpsElement selected = adaptable.getAdapter(IIpsElement.class);
            if (selected instanceof IIpsSrcFile) {
                IIpsObject ipsObject = ((IIpsSrcFile)selected).getIpsObject();
                if (ipsObject instanceof IpsObjectPartContainer) {
                    IpsObjectPartContainer ipsObjectPartContainer = (IpsObjectPartContainer)ipsObject;
                    paste(ipsObjectPartContainer);
                }
            } else if (selected instanceof IpsObjectPartContainer) {
                paste((IpsObjectPartContainer)selected);
            } else if (selected instanceof IIpsProject) {
                paste((IProject)((IIpsProject)selected).getProject().unwrap());
            } else if (selected instanceof IIpsPackageFragmentRoot) {
                paste(((IIpsPackageFragmentRoot)selected).getDefaultIpsPackageFragment());
            } else if (selected instanceof IIpsPackageFragment) {
                paste((IIpsPackageFragment)selected);
            }
        } else if (adaptable.getAdapter(IResource.class) != null) {
            IResource selected = adaptable.getAdapter(IResource.class);
            if (selected instanceof IContainer) {
                paste((IContainer)selected);
            }
        }
    }

    /**
     * Try to paste an <code>IIpsObject</code> to an <code>IIpsObjectPartContainer</code>. If it is
     * not possible because the stored data does not support this (e.g. is a resource and not a
     * string) paste(IIpsPackageFragement) is called.
     * 
     * @param parent The parent to paste to.
     */
    protected List<IIpsObjectPart> paste(IpsObjectPartContainer parent) {
        String stored = (String)clipboard.getContents(TextTransfer.getInstance());
        IpsObjectPartState[] states = (IpsObjectPartState[])clipboard.getContents(new IpsObjectPartStateListTransfer(
                parent.getClass().getClassLoader()));

        // obtain the package fragment of the given part container
        IIpsPackageFragment parentPackageFrgmt = findParentPackageFragment(parent);

        if (stored == null && states == null && parentPackageFrgmt != null) {
            // the clipboard contains no string, try to paste resources
            paste(parentPackageFrgmt);
        } else {
            // try to paste resource links
            if (parentPackageFrgmt != null && pasteResourceLinks(parentPackageFrgmt, stored)) {
                // the copied text contains links, paste is finished
                return null;
            }
            // no links in string try to paste ips object parts
            try {
                List<IIpsObjectPart> newParts = new ArrayList<>();
                if (states != null) {
                    for (IpsObjectPartState state : states) {
                        newParts.add(state.newPart(parent));
                    }
                }
                return newParts;
            } catch (RuntimeException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
        return null;
    }

    /**
     * obtain the package fragment of the given part container
     * 
     * @param parent a part container
     * @return the package fragment of the given part container
     */
    private IIpsPackageFragment findParentPackageFragment(IIpsObjectPartContainer parent) {
        IIpsPackageFragment parentPackageFrgmt = null;
        IIpsElement pack = parent.getParent();
        while (pack != null && !(pack instanceof IIpsPackageFragment)) {
            pack = pack.getParent();
        }
        if (pack != null) {
            parentPackageFrgmt = (IIpsPackageFragment)pack;
        }
        return parentPackageFrgmt;
    }

    /**
     * Try to paste an <code>IFolder</code> or <code>IFile</code> stored in the clipboard into the
     * given <code>IContainer</code>.
     */
    private void paste(IContainer parent) {
        Object stored = getTransferedObject();
        if (stored instanceof IResource[]) {
            IResource[] res = (IResource[])stored;
            for (IResource re : res) {
                try {
                    copy(parent, re);
                } catch (IpsException e) {
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
        Object stored = getTransferedObject();
        if (stored instanceof IResource[]) {
            copyResources(parent, (IResource[])stored);
        } else if (stored instanceof String[]) {
            copyFiles(parent, (String[])stored);
        }

        // Paste objects by resource links (e.g. files inside an ips archive)
        String storedText = (String)clipboard.getContents(TextTransfer.getInstance());
        pasteResourceLinks(parent, storedText);
    }

    /**
     * Returns the transfered objects (either a {@link IResource IResource[]} or a {@link String
     * String[]}
     */
    private Object getTransferedObject() {
        Object stored = clipboard.getContents(ResourceTransfer.getInstance());
        if (stored == null) {
            stored = clipboard.getContents(FileTransfer.getInstance());
        }
        return stored;
    }

    private void copyResources(IIpsPackageFragment parent, IResource[] resources) {
        for (IResource resource2 : resources) {
            try {
                IResource resource = ((IIpsElement)parent).getCorrespondingResource().unwrap();
                if (resource != null) {
                    copy(resource, resource2);
                } else {
                    showPasteNotSupportedError();
                }
            } catch (IpsException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    private void copyFiles(IIpsPackageFragment parent, String[] fileNames) {
        IResource destinationResource = parent.getEnclosingResource().unwrap();
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

    /**
     * Returns all objects which are represented by links inside the clipboard. If there are no
     * resource links inside the clipboard an empty array will ne returned. If the linked object
     * wasn't found then the object will be ignored (not returned).
     */
    public Object[] getObjectsFromResourceLinks(String resourceLinks) {
        if (resourceLinks == null || !resourceLinks.startsWith(ARCHIVE_LINK)) {
            // no resource links
            return EMPTY_IPS_OBJECT_ARRAY;
        }
        resourceLinks = resourceLinks.substring(ARCHIVE_LINK.length(), resourceLinks.length());

        StringTokenizer tokenizer = new StringTokenizer(resourceLinks, ","); //$NON-NLS-1$
        int count = tokenizer.countTokens();
        List<Object> result = new ArrayList<>(1);
        List<String> links = new ArrayList<>(count);

        while (tokenizer.hasMoreTokens()) {
            links.add(tokenizer.nextToken());
        }

        for (String resourceLink : links) {
            String[] copiedResource = StringUtils.split(resourceLink, "#"); //$NON-NLS-1$
            // 1. find the project
            IIpsProject project = IIpsModel.get().getIpsProject(copiedResource[0]);
            try {
                // 2. find the root
                IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
                IIpsPackageFragmentRoot archive = null;
                for (IIpsPackageFragmentRoot root : roots) {
                    if (root.getName().equals(copiedResource[1])) {
                        archive = root;
                        break;
                    }
                }
                if (archive == null) {
                    continue;
                }
                // 3. find the object or package
                if (copiedResource.length >= 4) {
                    // the link represents an object (object [3] contains the type of the object)
                    // try to find the object
                    IIpsObject ipsObject = archive.findIpsObject(IpsObjectType.getTypeForExtension(copiedResource[3]),
                            copiedResource[2]);
                    if (ipsObject != null) {
                        result.add(ipsObject);
                    }
                } else {
                    // the link represents a package fragment
                    // try to obtain the package fragment
                    IIpsPackageFragment packageFrgmt = archive.getIpsPackageFragment(copiedResource[2]);
                    if (packageFrgmt != null) {
                        result.add(packageFrgmt);
                    }
                }
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
        return result.toArray();
    }

    protected String getContentsOfIpsObject(IIpsObject ipsObject) {
        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        String contents;
        try {
            contents = XmlUtil.nodeToString(
                    ipsObject.toXml(XmlUtil.getDefaultDocumentBuilder().newDocument()),
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
    private void createFile(IIpsPackageFragment parent, IIpsObject ipsObject) {
        String contents = getContentsOfIpsObject(ipsObject);

        String ipsSrcFileName = getNewIpsSrcFileName(parent.getCorrespondingResource().unwrap(), ipsObject);
        if (ipsSrcFileName == null) {
            return;
        }
        parent.createIpsFile(ipsSrcFileName, contents, true, null);
    }

    /**
     * Creates a new file in the parent folder based on the given ips source object.
     */
    private void createFile(IFolder parent, IIpsObject ipsObject) {
        String contents = getContentsOfIpsObject(ipsObject);
        InputStream is;
        try {
            is = new ByteArrayInputStream(contents.getBytes(ipsObject.getIpsProject().getXmlFileCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new IpsException(new IpsStatus(e));
        }

        String newIpsSrcFileName = getNewIpsSrcFileName(parent, ipsObject);
        if (newIpsSrcFileName == null) {
            return;
        }

        IFile file = parent.getFile(newIpsSrcFileName);
        try {
            file.create(is, true, null);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    /**
     * Creates a new package fragment and childs in the parent package fragment based on the given
     * source package fragment.
     */
    private void createPackageFragmentAndChilds(IIpsPackageFragment parent, IIpsPackageFragment sourcePackageFragment)
            {

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
        return getNewNameByDialogIfNecessary(IResource.FILE, targetPath, nameWithoutExtension, extension, false,
                ipsObject.getIpsSrcFile());
    }

    /**
     * Returns a new name for folder or files, returns null if the user aborts the get new name for
     * duplicate source file dialog.
     */
    private String getNewNameByDialogIfNecessary(int resourceType,
            IPath targetPath,
            String nameWithOrWithoutExtension,
            String extension,
            boolean showExtension,
            IIpsSrcFile sourceIpsSrcFile) {

        boolean dialogWasDisplayed = false;
        NewResourceNameValidator validator = new NewResourceNameValidator(targetPath, resourceType, extension,
                sourceIpsSrcFile);
        int doCopy = Window.OK;
        boolean nameChangeRequired = validator.isValid(nameWithOrWithoutExtension) != null;
        if (nameChangeRequired) {
            String suggestedName = validator.getValidResourceName(nameWithOrWithoutExtension);
            nameWithOrWithoutExtension = suggestedName;

            // if force is true don't show dialog (could be true for automated testing purposes)
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
     * all children of the given source package fragment .
     */
    private IFolder createFolderAndFiles(IFolder targetParentFolder, IIpsPackageFragment sourcePackageFragment)
            {

        String packageName = sourcePackageFragment.getLastSegmentName();
        IPath targetPath = targetParentFolder.getFullPath();
        packageName = getNewNameByDialogIfNecessary(IResource.FOLDER, targetPath, packageName, "", false, null); //$NON-NLS-1$
        if (packageName == null) {
            return null;
        }

        IFolder subFolder = targetParentFolder.getFolder(packageName);
        try {
            subFolder.create(true, true, null);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
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
        return subFolder;
    }

    private void showPasteNotSupportedError() {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.IpsPasteAction_errorTitle,
                Messages.IpsPasteAction_Error_CannotPasteIntoSelectedElement);
    }

    /**
     * Copy the given resource to the given target path.
     * 
     * @throws IpsException If copy failed.
     */
    private void copy(IResource targetParent, IResource resource) {
        if (targetParent == null) {
            return;
        }
        IPath targetPath = targetParent.getFullPath();

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
            IIpsElement source = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
            copyProductCmptByWizard((IProductCmpt)((IIpsSrcFile)source).getIpsObject(), targetParent);
        } else {
            // non product cmpt
            IIpsElement source = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
            String newName = getNewNameByDialogIfNecessary(resource.getType(), targetPath, suggestedName, extension,
                    showExtension, source instanceof IIpsSrcFile ? (IIpsSrcFile)source : null);
            copyResource(resource, targetPath, newName);
        }
    }

    /**
     * Copies the {@link IResource resource} to a new file with the given name under the
     * {@link IPath target path}.
     * 
     * @param resource the {@link IResource} to copy
     * @param targetPath the {@link IPath} for the target directory
     * @param newName the name for the new file
     */
    protected void copyResource(IResource resource, IPath targetPath, String newName) {
        if (newName != null) {
            try {
                IPath destination = targetPath.append(newName);
                resource.copy(destination, true, null);
            } catch (Exception e) {
                IpsPlugin.showErrorDialog(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                        Messages.IpsPasteAction_cannot_copy, e));
            }
        }
    }

    private void copyProductCmptByWizard(IProductCmpt productCmpt, IResource target) {
        NewProductWizard wizard;
        if (productCmpt.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            wizard = new NewProductCmptWizard();
        } else {
            wizard = new NewProductTemplateWizard();
        }
        wizard.init(IpsPlugin.getDefault().getWorkbench(), new StructuredSelection(target));
        wizard.setCopyProductCmpt(productCmpt);

        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();
    }

    private boolean isResourceProductCmpt(IResource resource) {
        if (resource instanceof IFile) {
            IIpsElement ipsElement = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
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
        IIpsElement ipsElement = IIpsModel.get().getIpsElement(Wrappers.wrap(file).as(AFile.class));
        if (ipsElement instanceof IIpsSrcFile && ipsElement.exists()) {
            return true;
        }
        return false;
    }

}
