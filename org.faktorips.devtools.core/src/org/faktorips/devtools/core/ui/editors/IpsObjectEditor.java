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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.faktorips.devtools.core.IpsPreferences;
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
public abstract class IpsObjectEditor extends FormEditor 
    implements ContentsChangeListener, IModificationStatusChangeListener, IPartListener2,
        IResourceChangeListener, IPropertyChangeListener {

    // the file that's being edited (if any)
    private IIpsSrcFile ipsSrcFile;

    // dirty flag
    private boolean dirty = false;

    // Activates or deactivates the refreshing of this editor
    private boolean active = false;

    // 
    private Boolean contentChangeable;
    
    // the editor's ISelectionProvider 
    private SelectionProviderDispatcher selectionProviderDispatcher;

    /*
     * Storage for the user's decision of not to fix the differences between the
     * product definition structure and the model structure
     */
    private boolean dontFixDifferences = false;
    
    /*
     * Flag indicating an open delta-dialog if <code>true</code>.
     */
    private boolean deltaShowing = false;

    public IpsObjectEditor() {
        super();
    }

    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    /**
     * Returns the ips object of the ips src file currently edited, returns <code>null</code> if
     * the ips object not exists (e.g. if the ips src file is outside an ips package.
     */
    public IIpsObject getIpsObject() {
        try {
            if (getIpsSrcFile().exists()) {
                return getIpsSrcFile().getIpsObject();
            }
            else {
                return null;
            }
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
     * {@inheritDoc}
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

        // check if the ips src file is valid and could be edit in the editor,
        // if the ips src file doesn't exists (e.g. ips src file outside ips package)
        // close the editor and open the current file in the default text editor
        if (!ipsSrcFile.exists()) {
            Runnable closeRunnable = new Runnable() {
                public void run() {
                    IpsObjectEditor.this.close(false);
                    IpsPlugin.getDefault().openEditor(ipsSrcFile.getCorrespondingFile());
                }
            };
            getSite().getShell().getDisplay().syncExec(closeRunnable);
        }
        
        site.getPage().addPartListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);

        selectionProviderDispatcher = new SelectionProviderDispatcher();
        site.setSelectionProvider(selectionProviderDispatcher);
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
    }

    /**
     * {@inheritDoc}
     */
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        refresh();
    }

    /**
     * Refreshes the controls on the active page with the data from the model.<br>
     * Calls to this refresh method are ignored if the activate attribute is set to
     * <code>false</code>.
     */
    protected void refresh() {
        if (!active && !ipsSrcFile.exists()) {
            return;
        }
        try {
            // here we have to request the ips object once, to make sure that 
            // it's state is is synchronized with the enclosing resource.
            // otherwise if some part of the ui keeps a reference to the ips object, it won't contain
            // the correct state.
            if (ipsSrcFile.exists()){
                ipsSrcFile.getIpsObject(); 
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        IEditorPart editor = getActivePageInstance();
        if (editor instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editor;
            page.refresh();
        }
        updateDataChangeableState();
    }
    
    /**
     * Evaluates the new data changeable state and updates it, if it has changed.
     */
    public void updateDataChangeableState() {
        setDataChangeable(computeDataChangeableState());
        IEditorPart editor = getActivePageInstance();
        if (editor instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editor;
            page.updateDataChangeableState();
        }
    }
    
    /**
     * Evaluates if if the data shown in this editor is changeable by the user. 
     * The data is changeable if the the ips source file shown
     * in the editor is mutable and the working mode preference is set to edit mode.
     * 
     * Subclasses may override this method.
     */
    protected boolean computeDataChangeableState() {
        return ipsSrcFile.isMutable() && IpsPlugin.getDefault().getIpsPreferences().isWorkingModeEdit();
    }
    
    /**
     * Returns <code>true</code> if the data shown in this editor is changeable by the user, 
     * otherwise <code>false</code>. 
     */
    public final Boolean isDataChangeable() {
        return contentChangeable;
    }
    
    /**
     * Sets the content changeable state.
     */
    protected void setDataChangeable(boolean changeable) {
        this.contentChangeable = Boolean.valueOf(changeable);
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(ipsSrcFile)) {
            refresh();
        }
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
    public final void partActivated(IWorkbenchPartReference partRef) {
        IWorkbenchPart part = partRef.getPart(false);
        if (part != this) {
            return;
        }
        setActive(true);
        editorActivated();
    }
    
    /**
     * Called when the editor is activated (e.g. by clicking in it).
     */
    protected void editorActivated() {
        checkForInconsistenciesToModel();
    }
    
    /**
     * Does what the methodname says :-)
     */
    public final void checkForInconsistenciesToModel() {
        if (isDataChangeable()==null || !isDataChangeable().booleanValue() || deltaShowing) {
            // no modifications for read-only-editors
            return;
        }
        if (!getIpsSrcFile().exists()){
            // dont't check for inconsistencies if the src file not exists,
            // e.g. if the product cmpt editor is open and the product cmpt was moved
            return;
        }
        if (dontFixDifferences) {
            // user decided not to fix the differences some time ago...
            return;
        }           
        if (getContainer() == null) {
            // do nothing, we will be called again later. This avoids that the user
            // is shown the differences-dialog twice if openening the editor...
            return;
        }
        try {
            deltaShowing = true;
            dontFixDifferences = checkForInconsistenciesToModelInternal();
        } finally {
            deltaShowing = false;
        }
    }    
    
    /**
     * Checks for inconsistencies between the structure shown in this editor and the model.
     * Asks the user if the inconsistencies should be fixed. Specific logic has to be implemented in subclasses.
     * 
     * @return <code>true</code> if the user does not want to fix any existing differences,
     * <code>false</code> otherwise.  Default returns false.
     *
     */
    protected boolean checkForInconsistenciesToModelInternal() {
        return false;
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
            IpsPlugin.getDefault().getIpsPreferences().addChangeListener(this);
            setDirty(ipsSrcFile.isDirty());
            refresh();
        } else {
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
            IpsPlugin.getDefault().getIpsModel().removeModificationStatusChangeListener(this);
            IpsPlugin.getDefault().getIpsPreferences().removeChangeListener(this);
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

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            refresh();        
        }
    }
    
}
