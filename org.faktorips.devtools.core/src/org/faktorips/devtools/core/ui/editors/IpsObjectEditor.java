/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;

/**
 * TODO comment 
 * 
 * <p>This editor uses an implementation of ISelectionProvider where ISelectionProviders
 * used on the different pages of this editor can be registered. The ISelectionProvider of this
 * editor is registered at the selection service of the workbench so that only this selection
 * provider is the active one within the workbench when this editor is active. Implementations of
 * ISelectionProvider that are used on the pages of this editor have to be registered at the
 * SelectionProviderDispatcher the ISelectionProvider of this editor. The dispatcher finds the
 * currently active of all registered selection providers and forwards request to it. There are to
 * ways of registering with the SelectionProviderDispatcher.
 * <ol>
 * <li>The <code>Composite</code> where the control of the ISelectionProvider implementation e.g. a
 * TreeViewer is added to has to implement the {@link ISelectionProviderActivation} interface. The
 * editor will track all the implementations of this interface at initialization time an register
 * them with the dispatcher. 
 * </li> 
 * <li>The dispatcher can be retrieved by the
 * getSelectionProviderDispatcher() method of this editor and an
 * {@link ISelectionProviderActivation} can be registered manually 
 * </li>
 * </ol>
 */
public abstract class IpsObjectEditor extends FormEditor implements ContentsChangeListener, IModificationStatusChangeListener, IPartListener2,
        IResourceChangeListener {

    // the file that's being edited (if any)
    private IIpsSrcFile ipsSrcFile;

    // dirty flag
    private boolean dirty = false;

    /** Activates or deactivates the refreshing of this editor */
    private boolean active = false;

    // the ISelectionProvider of this IEditorPart
    private SelectionProviderDispatcher selectionProviderDispatcher;

    /**
     * 
     */
    public IpsObjectEditor() {
        super();
    }

    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    public IIpsObject getIpsObject() {
        try {
            return getIpsSrcFile().getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the title that is shown on every page.
     */
    protected abstract String getUniformPageTitle();

    /**
     * Overridden method.
     * 
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        IIpsModel model = IpsPlugin.getDefault().getIpsModel();

        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            ipsSrcFile = (IIpsSrcFile)model.getIpsElement(file);
            setPartName(ipsSrcFile.getName());
        } else if (input instanceof IpsArchiveEditorInput) {
            ipsSrcFile = ((IpsArchiveEditorInput)input).getIpsSrcFile();
            setPartName(ipsSrcFile.getName());
        } else if (input instanceof IStorageEditorInput) {
            initFromStorageEditorInput((IStorageEditorInput)input);
            setPartName(((IStorageEditorInput)input).getName());
        }

        if (ipsSrcFile == null) {
            throw new PartInitException("Unsupported editor input type " + input.getClass().getName()); //$NON-NLS-1$
        }

        site.getPage().addPartListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        // TODO remark: disabled because of performance problems
        // IpsPlugin.getDefault().getIpsModel().addChangeListener(this);

        selectionProviderDispatcher = new SelectionProviderDispatcher();
        site.setSelectionProvider(selectionProviderDispatcher);
        setActive(true);
    }

    private void initFromStorageEditorInput(IStorageEditorInput input) throws PartInitException {
        try {
            IStorage storage = input.getStorage();
            IPath path = storage.getFullPath();
            if (path == null) {
                return;
            }

            String extension = IpsObjectType.PRODUCT_CMPT.getFileExtension();
            int nameIndex = path.lastSegment().indexOf(extension);

            IpsObjectType[] types = IpsObjectType.ALL_TYPES;
            for (int i = 0; i < types.length; i++) {
                extension = types[i].getFileExtension();
                nameIndex = path.lastSegment().indexOf(extension);
                if (nameIndex != -1) {
                    break;
                }
            }

            if (nameIndex == -1) {
                return;
            }
            String name = path.lastSegment().substring(0, nameIndex) + extension;
            ipsSrcFile = new IpsSrcFileImmutable(name, storage.getContents());

        } catch (CoreException e) {
            throw new PartInitException(e.getStatus());
        } catch (Exception e) {
            IpsPlugin.log(e);
            throw new PartInitException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void setActivePage(int pageIndex) {
        super.setActivePage(pageIndex);
        refresh();
        if (!ipsSrcFile.isMutable()) {
            super.getActivePageInstance().getPartControl().setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        refresh();
    }

    /**
     * Refresh the controls on the active page with the data from the model.<br>
     * Calls to this refresh method are ignored if the activate attribute is set to
     * <code>false</code>.
     */
    protected void refresh() {
        if (!active) {
            return;
        }
        IEditorPart editor = getActivePageInstance();
        if (editor instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editor;
            page.refresh();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        // no refresh neccessary - this method is only called if this editor is the active one.
        // we only need a refresh here if the content of one field of this editorwill have an
        // effect on another field in this editor, but this is not the case yet.

        // TODO remark: disabled because of performance problems
        // refresh();
    }
    
    /**
     * {@inheritDoc}
     */
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
        if (!ipsSrcFile.equals(event.getIpsSrcFile())) {
            return;
        }
        setDirty(ipsSrcFile.isDirty());
    }

    protected void setDirty(boolean newValue) {
        if (dirty == newValue) {
            return;
        }
        dirty = newValue;
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * {@inheritDoc}
     */
    public void doSave(IProgressMonitor monitor) {
        try {
            ipsSrcFile.save(true, monitor);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        setDirty(ipsSrcFile.isDirty());
    }

    /**
     * {@inheritDoc}
     */
    public void doSaveAs() {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * We have to close the editor if the underlying resource is removed. {@inheritDoc}
     */
    public void resourceChanged(IResourceChangeEvent event) {
        if (!ipsSrcFile.exists()) {
            this.close(false);
        }
    }

    /**
     * Returns <code>true</code> if the <code>IIpsSrcFile</code> this editor is based on exists
     * and is in sync.
     */
    protected boolean isSrcFileUsable() {
        if (ipsSrcFile instanceof IpsSrcFileImmutable) {
            return true;
        }
        if (ipsSrcFile.getCorrespondingFile() == null) {
            return false;
        }
        return ipsSrcFile != null && ipsSrcFile.exists()
                && ipsSrcFile.getCorrespondingFile().isSynchronized(IResource.DEPTH_ONE);
    }

    /**
     * {@inheritDoc}
     */
    public void partActivated(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part != this) {
            return;
        }
        setActive(true);
        refresh();
    }

    /**
     * {@inheritDoc}
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false) != this) {
            return;
        }

        if (ipsSrcFile instanceof IpsSrcFileImmutable) {
            return;
        }
        if (!ipsSrcFile.getEnclosingResource().isSynchronized(IResource.DEPTH_ONE)) {
            String msg = NLS.bind(Messages.IpsObjectEditor_msgResourceOutOfSync, ipsSrcFile.getName());
            if (isDirty()) {
                msg += Messages.IpsObjectEditor_msgOutOfSyncOptions;
                boolean ok = MessageDialog.openQuestion(super.getSite().getShell(),
                        Messages.IpsObjectEditor_msgOutOfSyncTitle, msg);
                if (ok) {
                    this.close(false);
                } else {
                    doSave(null);
                }
            } else {
                msg += Messages.IpsObjectEditor_msgEditorWillBeClosed;
                MessageDialog.openError(super.getSite().getShell(), Messages.IpsObjectEditor_msgOutOfSyncTitle, msg);
                this.close(false);
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public void partClosed(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part == this) {
            ipsSrcFile.discardChanges();
            part.getSite().getPage().removePartListener(this);
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void partHidden(IWorkbenchPartReference partRef) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void partOpened(IWorkbenchPartReference partRef) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void partVisible(IWorkbenchPartReference partRef) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void partDeactivated(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part != this || partRef.getPage().isPartVisible(part)) {
            return;
        }
        setActive(false);
    }

    public String toString() {
        return "Editor for " + getIpsSrcFile(); //$NON-NLS-1$
    }

    /**
     * @see IpsObjectEditor#active
     */
    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        if (active) {
            IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
            IpsPlugin.getDefault().getIpsModel().addModifcationStatusChangeListener(this);
            setDirty(ipsSrcFile.isDirty());
            refresh();
        } else {
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
            IpsPlugin.getDefault().getIpsModel().removeModificationStatusChangeListener(this);
        }
    }

    /**
     * Returns the SelectionProviderDispatcher which is the ISelectionProvider for this IEditorPart.
     */
    public SelectionProviderDispatcher getSelectionProviderDispatcher() {
        return selectionProviderDispatcher;
    }

    /**
     * {@inheritDoc}
     */
    public final void dispose() {
        super.dispose();
        selectionProviderDispatcher.dispose();
        disposeInternal();
    }

    /**
     * Empty. Can be overridden by subclasses for dispose purposes.
     */
    protected void disposeInternal() {
    }
}
