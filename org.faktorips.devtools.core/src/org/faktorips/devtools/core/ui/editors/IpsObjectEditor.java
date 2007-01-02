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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.IpsSrcFileImmutable;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;

/**
 * Base class for all editors to edit ips objects.
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
    implements ContentsChangeListener, IModificationStatusChangeListener,
        IResourceChangeListener, IPropertyChangeListener{

    // the file that's being edited (if any)
    private IIpsSrcFile ipsSrcFile;

    // dirty flag
    private boolean dirty = false;

    private Boolean contentChangeable;
    
    // the editor's ISelectionProvider 
    private SelectionProviderDispatcher selectionProviderDispatcher;

    /*
     * Storage for the user's decision not to fix the differences between the
     * product definition structure and the model structure
     */
    private boolean dontFixDifferences = false;
    
    /*
     * Storage for the user's decision not to load the changes made directly in the
     * file system.
     */
    private boolean dontLoadChanges = false;
    
    private ActivationListener activationListener;
    
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
        ResourcesPlugin.getWorkspace().addResourceChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsModel().addChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsModel().addModifcationStatusChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsPreferences().addChangeListener(IpsObjectEditor.this);

        if (ipsSrcFile == null) {
            throw new PartInitException("Unsupported editor input type " + input.getClass().getName()); //$NON-NLS-1$
        }

        if (ipsSrcFile.isMutable() && !ipsSrcFile.getEnclosingResource().isSynchronized(0)) {
            try {
                ipsSrcFile.getEnclosingResource().refreshLocal(0, null);
            } catch (CoreException e) {
                throw new PartInitException("Error refreshing resource " + ipsSrcFile.getEnclosingResource()); //$NON-NLS-1$
            }
        }
        
        // check if the ips src file is valid and could be edited in the editor,
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
        } else {
            activationListener = new ActivationListener(site.getPage());
            selectionProviderDispatcher = new SelectionProviderDispatcher();
            site.setSelectionProvider(selectionProviderDispatcher);
        }
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
     * Returns the active IpsObjectEditorPage. If the active page is not an instance of IpsObjectEditorPage 
     * <code>null</code> will be returned.
     */
    public IpsObjectEditorPage getActiveIpsObjectEditorPage(){
        IFormPage page = getActivePageInstance();
        if(page instanceof IpsObjectEditorPage){
            return (IpsObjectEditorPage)getActivePageInstance();
        }
        return null;
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
        if (!ipsSrcFile.exists()) {
            return;
        }
        try {
            // here we have to request the ips object once, to make sure that 
            // it's state is is synchronized with the enclosing resource.
            // otherwise if some part of the ui keeps a reference to the ips object, it won't contain
            // the correct state.
            ipsSrcFile.getIpsObject(); 
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
        if (getIpsSrcFile()!=null) {
            this.setTitleImage(getIpsSrcFile().getIpsObjectType().getImage(changeable));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(final ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(ipsSrcFile)) {
            return;
        }
        Display display = IpsPlugin.getDefault().getWorkbench().getDisplay();
        display.syncExec(new Runnable() {

            public void run() {
                if (event.getEventType()==ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                    refreshInclStructuralChanges();
                } else {
                    refresh();
                }
            }
        });
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
     * We have to close the editor if the underlying resource is removed. 
     * 
     * {@inheritDoc}
     */
    public void resourceChanged(IResourceChangeEvent event) {
        IResource enclResource = ipsSrcFile.getEnclosingResource();
        if (enclResource==null || event.getDelta().findMember(enclResource.getFullPath())==null) {
            return;
        }
        if (!ipsSrcFile.exists()) {
            this.close(false);
            return;
        }
    }

    /**
     * Returns <code>true</code> if the <code>IIpsSrcFile</code> this editor is based on exists
     * and is in sync.
     */
    protected boolean isSrcFileUsable() {
        return ipsSrcFile != null && ipsSrcFile.exists()
                && ipsSrcFile.getEnclosingResource().isSynchronized(IResource.DEPTH_ONE);
    }

    protected void handleEditorActivation() {
        checkForChangesMadeOutsideEclipse();
        editorActivated();
        refresh();
    }

    private void checkForChangesMadeOutsideEclipse() {
        if (dontLoadChanges) {
            return;
        }
        if (getIpsSrcFile().isMutable() && !getIpsSrcFile().getEnclosingResource().isSynchronized(0)) {
            MessageDialog dlg = new MessageDialog(Display.getCurrent().getActiveShell(), Messages.IpsObjectEditor_fileHasChangesOnDiskTitle, (Image)null, 
                    Messages.IpsObjectEditor_fileHasChangesOnDiskMessage, MessageDialog.QUESTION,
                    new String[]{Messages.IpsObjectEditor_fileHasChangesOnDiskYesButton, Messages.IpsObjectEditor_fileHasChangesOnDiskNoButton}, 0);
            dlg.open();
            if (dlg.getReturnCode()==0) {
                try {
                    getIpsSrcFile().getEnclosingResource().refreshLocal(0, null);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            } else {
                dontLoadChanges = true;
            }
            
        }
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
        if (isDataChangeable()==null || !isDataChangeable().booleanValue()) {
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
        if (!(getIpsObject() instanceof IFixDifferencesToModelSupport)) {
            return;
        }
        final IFixDifferencesToModelSupport toFixIpsObject = (IFixDifferencesToModelSupport)getIpsObject();
        try {
            if (!toFixIpsObject.containsDifferenceToModel()){
                return;
            }
            final Dialog dialog = createDialogToFixDifferencesToModel();
            if (getSite() != null) {
                Runnable checkAndFixRunnable = new Runnable() {
                    public void run() {
                        if (dialog.open() == Dialog.OK) {
                            try {
                                IpsPlugin.getDefault().getIpsModel().runAndQueueChangeEvents(
                                        new DifferenceFixer(toFixIpsObject), null);
                                refreshInclStructuralChanges();
                            } catch (CoreException e) {
                                IpsPlugin.logAndShowErrorDialog(e);
                                return;
                            }
                        } else {
                            dontFixDifferences = true;
                        }
                    }
                };
                getSite().getShell().getDisplay().asyncExec(checkAndFixRunnable);
            }
        }
        catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }    
    
    /**
     * Creates a dialog to disblay the differences to the model and ask the user if the
     * inconsistencies should be fixed. Specific logic has to be implemented in subclasses.
     * 
     * @throws CoreException Throws in case of an error
     */
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException{
        throw new UnsupportedOperationException();
    }

    /**
     * Refreshes the UI and can handle structural changes which means not only the content of the
     * controls is updated but also new controls are created or existing ones are disposed if
     * neccessary.
     */
    protected void refreshInclStructuralChanges(){
        refresh();
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
        if (activationListener!=null) {
            activationListener.dispose();
        }
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
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
    
    public String toString() {
        return "Editor for " + getIpsSrcFile(); //$NON-NLS-1$
    }

    /**
     * Class to fix differences to the model as workspace runnable
     * 
     * @author Joerg Ortmann
     */
    private class DifferenceFixer implements IWorkspaceRunnable {
        private IFixDifferencesToModelSupport toFixIpsObject;
        
        public DifferenceFixer(IFixDifferencesToModelSupport toFixIpsObject) {
            this.toFixIpsObject = toFixIpsObject;
        }
        
        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) throws CoreException {
            toFixIpsObject.fixAllDifferencesToModel();
        }
    }
    
    /**
     * Internal part and shell activation listener for triggering state validation.
     * 
     * Copied from AbstractTextEditor
     */
    class ActivationListener implements IPartListener, IWindowListener {

        /** Cache of the active workbench part. */
        private IWorkbenchPart activePart;
        /** Indicates whether activation handling is currently be done. */
        private boolean isHandlingActivation= false;

        private IPartService partService;

        /**
         * Creates this activation listener.
         *
         * @param partService the part service on which to add the part listener
         * @since 3.1
         */
        public ActivationListener(IPartService partService) {
            this.partService = partService;
            partService.addPartListener(this);
            PlatformUI.getWorkbench().addWindowListener(this);
        }

        /**
         * Disposes this activation listener.
         *
         * @since 3.1
         */
        public void dispose() {
            partService.removePartListener(this);
            PlatformUI.getWorkbench().removeWindowListener(this);
            partService= null;
        }

        public void partActivated(IWorkbenchPart part) {
            if (part!=IpsObjectEditor.this) {
                return;
            }
            activePart= part;
            handleActivation();
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
            if (part!=IpsObjectEditor.this) {
                return;
            }
            ipsSrcFile.discardChanges();
            removeListeners();
        }

        public void partDeactivated(IWorkbenchPart part) {
            if (part!=IpsObjectEditor.this) {
                return;
            }
            activePart= null;
        }
        
        private void removeListeners() {
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(IpsObjectEditor.this);
            IpsPlugin.getDefault().getIpsModel().removeModificationStatusChangeListener(IpsObjectEditor.this);
            IpsPlugin.getDefault().getIpsPreferences().removeChangeListener(IpsObjectEditor.this);
        }
        
        public void partOpened(IWorkbenchPart part) {
        }

        /**
         * Handles the activation triggering a element state check in the editor.
         */
        private void handleActivation() {
            if (isHandlingActivation)
                return;

            if (activePart == IpsObjectEditor.this) {
                isHandlingActivation= true;
                try {
                    handleEditorActivation();
                } finally {
                    isHandlingActivation= false;
                }
            }
        }

        public void windowActivated(IWorkbenchWindow window) {
            if (window == getEditorSite().getWorkbenchWindow()) {
                handleActivation();
                /*
                 * Workaround for problem described in
                 * http://dev.eclipse.org/bugs/show_bug.cgi?id=11731
                 * Will be removed when SWT has solved the problem.
                window.getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                    }
                });
                 */
            }
        }

        public void windowDeactivated(IWorkbenchWindow window) {
        }

        public void windowClosed(IWorkbenchWindow window) {
        }

        public void windowOpened(IWorkbenchWindow window) {
        }
    }

    
}
