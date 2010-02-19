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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFileImmutable;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;

/**
 * Base class for all editors that want to edit IPS objects.
 * <p>
 * This editor uses an implementation of <code>ISelectionProvider</code> where
 * <code>ISelectionProvider</code> objects used on the different pages of this editor can be
 * registered.
 * <p>
 * The <code>ISelectionProvider</code> of this editor is registered at the selection service of the
 * workbench so that only this selection provider is the active one within the workbench when this
 * editor is active.
 * <p>
 * Implementations of <code>ISelectionProvider</code> that are used on the pages of this editor have
 * to be registered at the <code>SelectionProviderDispatcher</code> of the
 * <code>ISelectionProvider</code> of this editor. The dispatcher finds the currently active of all
 * registered selection providers and forwards requests to it.
 * <p>
 * There are two ways of registering with the <code>SelectionProviderDispatcher</code>:
 * <ol>
 * <li>The <code>Composite</code> where the control of the <code>ISelectionProvider</code>
 * implementation e.g. a <code>TreeViewer</code> is added to has to implement the
 * {@link ISelectionProviderActivation} interface. The editor will track all the implementations of
 * this interface at initialization time and register them with the dispatcher.</li>
 * <li>The dispatcher can be retrieved by the <code>getSelectionProviderDispatcher()</code> method
 * of this editor and an {@link ISelectionProviderActivation} can be registered manually.</li>
 * </ol>
 * 
 * @see org.eclipse.jface.viewers.ISelectionProvider
 */
public abstract class IpsObjectEditor extends FormEditor implements ContentsChangeListener,
        IModificationStatusChangeListener, IResourceChangeListener, IPropertyChangeListener {

    public final static boolean TRACE = IpsPlugin.TRACE_UI;

    /*
     * Setting key for user's decision not to fix the differences between the product definition
     * structure and the model structure
     */
    private final static String SETTING_DONT_FIX_DIFFERENCES = "dontFixDifferences"; //$NON-NLS-1$

    // The file that's being edited (if any)
    private IIpsSrcFile ipsSrcFile;

    // Dirty flag
    private boolean dirty = false;

    private Boolean contentChangeable = null;

    // The editor's ISelectionProvider
    private SelectionProviderDispatcher selectionProviderDispatcher;

    /*
     * Storage for the user's decision not to load the changes made directly in the file system.
     */
    private boolean dontLoadChanges = false;

    private boolean isCheckingForChangesMadeOutsideEclipse = false;

    /*
     * True if the editor contains the pages that are shown for a parsable ips source file, false if
     * an error page is shown.
     */
    private boolean pagesForParsableSrcFileShown;

    private boolean updatingPageStructure = false;

    private ActivationListener activationListener;

    /* Updates the title image if there are ips marker changes on the editor's input */
    private IpsObjectEditorErrorMarkerUpdater errorTickupdater;

    private IContentOutlinePage outlinePage;

    /**
     * Creates a new <code>IpsObjectEditor</code>.
     */
    public IpsObjectEditor() {
        super();
        errorTickupdater = new IpsObjectEditorErrorMarkerUpdater(this);
    }

    /**
     * Returns the ips src file being edited.
     * 
     * @return Returns the ips src file to be edited by this editor.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    /**
     * Returns the ips project of the ips src file to be edited.
     * <p>
     * This is a shortcut for <code>getIpsSrcFile().getIpsProject()</code>.
     */
    public IIpsProject getIpsProject() {
        return ipsSrcFile.getIpsProject();
    }

    /**
     * Returns the ips object that is contained in the ips src file currently edited, returns
     * <code>null</code> if the ips object does not exist (e.g. if the ips src file is outside an
     * ips package).
     */
    public IIpsObject getIpsObject() {
        try {
            if (getIpsSrcFile().exists()) {
                return getIpsSrcFile().getIpsObject();
            } else {
                return null;
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /** Returns the title that is shown on every page. */
    protected abstract String getUniformPageTitle();

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (TRACE) {
            logMethodStarted("init"); //$NON-NLS-1$
        }

        super.init(site, input);

        IIpsModel model = IpsPlugin.getDefault().getIpsModel();

        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            ipsSrcFile = (IIpsSrcFile)model.getIpsElement(file);
        } else if (input instanceof IpsArchiveEditorInput) {
            ipsSrcFile = ((IpsArchiveEditorInput)input).getIpsSrcFile();
        } else if (input instanceof IStorageEditorInput) {
            initFromStorageEditorInput((IStorageEditorInput)input);
            setPartName(((IStorageEditorInput)input).getName());
        }
        if (ipsSrcFile == null) {
            throw new PartInitException("Unsupported editor input type " + input.getClass().getName()); //$NON-NLS-1$
        }

        String title = ipsSrcFile.getIpsObjectName();
        setPartName(title);
        setContentDescription(ipsSrcFile.getParent().getEnclosingResource().getFullPath().toOSString());

        if (ipsSrcFile.isMutable() && !ipsSrcFile.getEnclosingResource().isSynchronized(0)) {
            try {
                ipsSrcFile.getEnclosingResource().refreshLocal(0, null);
            } catch (CoreException e) {
                throw new PartInitException("Error refreshing resource " + ipsSrcFile.getEnclosingResource()); //$NON-NLS-1$
            }
        }

        /*
         * Check if the ips src file is valid and could be edited in the editor, if the ips src file
         * doesn't exists (e.g. ips src file outside ips package) close the editor and open the
         * current file in the default text editor.
         */
        if (!ipsSrcFile.exists()) {
            Runnable closeRunnable = new Runnable() {
                public void run() {
                    IpsObjectEditor.this.close(false);
                    IpsUIPlugin.getDefault().openEditor(ipsSrcFile.getCorrespondingFile());
                }
            };
            getSite().getShell().getDisplay().syncExec(closeRunnable);
        } else {
            activationListener = new ActivationListener(site.getPage());
            selectionProviderDispatcher = new SelectionProviderDispatcher();
            site.setSelectionProvider(selectionProviderDispatcher);
            IpsUIPlugin.getDefault().addHistoryItem(ipsSrcFile);
        }

        setDataChangeable(computeDataChangeableState());

        if (TRACE) {
            logMethodFinished("init"); //$NON-NLS-1$
        }
    }

    private void initFromStorageEditorInput(IStorageEditorInput input) throws PartInitException {
        if (TRACE) {
            logMethodStarted("initFromStorageEditorInput"); //$NON-NLS-1$
        }
        try {
            IStorage storage = input.getStorage();
            ipsSrcFile = new IpsSrcFileImmutable(storage.getName(), storage.getContents());
            if (TRACE) {
                logMethodFinished("initFromStorageEditorInput"); //$NON-NLS-1$
            }
        } catch (CoreException e) {
            throw new PartInitException(e.getStatus());
        } catch (Exception e) {
            IpsPlugin.log(e);
            throw new PartInitException(e.getMessage());
        }
    }

    @Override
    protected void createPages() {
        super.createPages();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsModel().addChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsModel().addModifcationStatusChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsPreferences().addChangeListener(IpsObjectEditor.this);
    }

    @Override
    final protected void addPages() {
        if (TRACE) {
            logMethodStarted("addPages"); //$NON-NLS-1$
        }

        pagesForParsableSrcFileShown = false;

        try {

            if (getIpsSrcFile() == null) {
                if (TRACE) {
                    log("addPages(): Page for unreachable file created."); //$NON-NLS-1$
                }
                addPage(new UnreachableFilePage(this));
                return;
            }

            if (!getIpsSrcFile().isContentParsable()) {
                if (TRACE) {
                    log("addPages(): Page for unparsable files created."); //$NON-NLS-1$
                }
                addPage(new UnparsableFilePage(this));
                return;
            }

            if (!ipsSrcFile.exists()) {
                if (TRACE) {
                    log("addPages(): Page for missing files created."); //$NON-NLS-1$
                }
                addPage(new MissingResourcePage(this));
                return;
            }

            if (TRACE) {
                logMethodStarted("addPagesForParsableSrcFile()"); //$NON-NLS-1$
            }
            addPagesForParsableSrcFile();

            if (TRACE) {
                logMethodFinished("addPagesForParsableSrcFile()"); //$NON-NLS-1$
            }

            pagesForParsableSrcFileShown = true;
            if (TRACE) {
                logMethodFinished("addPages"); //$NON-NLS-1$
            }

        } catch (Exception e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * This method extends the <code>addPages()</code> operation and must be implemented by
     * subclasses by adding the pages to edit the ips object with.
     * 
     * @throws PartInitException
     * @throws CoreException
     */
    protected abstract void addPagesForParsableSrcFile() throws PartInitException, CoreException;

    protected void updatePageStructure(boolean forceRefreshInclStructuralChanges) {
        if (TRACE) {
            logMethodStarted("updatePageStructure"); //$NON-NLS-1$
        }

        try {
            if (getIpsSrcFile().isContentParsable() == pagesForParsableSrcFileShown) {
                // no recreating (remove and add) of pages necessary because:
                // a) the source file isn't parsable and wasn't parsable before
                // b) the source file is parsable and was parsable before

                // if the source file is parsable then a refresh with structural changes is
                // necessary for all editors which shows a dynamic structures inside
                // (editors which overwrites the corresponding method e.g. ProductcmptEditor or
                // TestCaseEditor)
                if (forceRefreshInclStructuralChanges && getIpsSrcFile().isContentParsable()) {
                    refreshInclStructuralChanges();
                }

                return;
            }

            updatingPageStructure = true;
            ipsSrcFile.getIpsObject();
            // remove all pages
            for (int i = getPageCount(); i > 0; i--) {
                removePage(0);
            }

            if (TRACE) {
                System.out.println("updatePageStructure(): Existing pages removed. Must recreate."); //$NON-NLS-1$
            }
            addPages();
            updatingPageStructure = false;

            super.setActivePage(0); // also triggers the refresh

            if (TRACE) {
                logMethodFinished("updatePageStructure"); //$NON-NLS-1$
            }

        } catch (CoreException e) {
            updatingPageStructure = false;
            IpsPlugin.log(e);
            return;
        }
    }

    @Override
    protected void setActivePage(int pageIndex) {
        super.setActivePage(pageIndex);
        refresh();
    }

    /**
     * Returns the active <code>IpsObjectEditorPage</code>. If the active page is not an instance of
     * <code>IpsObjectEditorPage</code> <code>null</code> will be returned.
     */
    public IpsObjectEditorPage getActiveIpsObjectEditorPage() {
        IFormPage page = getActivePageInstance();
        if (page instanceof IpsObjectEditorPage) {
            return (IpsObjectEditorPage)getActivePageInstance();
        }

        return null;
    }

    @Override
    protected void pageChange(int newPageIndex) {
        if (TRACE) {
            logMethodStarted("pageChange(): newPage=" + newPageIndex); //$NON-NLS-1$
        }

        // must be called even if the file isn't parsable,
        // otherwise the unparsable file page wouldn't be shown
        super.pageChange(newPageIndex);

        refresh();
        if (TRACE) {
            logMethodFinished("pageChange(): newPage=" + newPageIndex); //$NON-NLS-1$
        }
    }

    /**
     * Refreshes the controls on the active page with the data from the model. Calls to this refresh
     * method are ignored if the activate attribute is set to <code>false</code>.
     */
    protected void refresh() {
        if (updatingPageStructure) {
            return;
        }

        /*
         * ipsSrcFile can be null if the editor is opened on an ips source file that is not in an
         * ips package.
         */

        if (ipsSrcFile == null || !ipsSrcFile.exists()) {
            return;
        }

        try {
            if (!ipsSrcFile.isContentParsable()) {
                return;
            }
            /*
             * here we have to request the ips object once, to make sure that it's state is
             * synchronized with the enclosing resource.
             * 
             * otherwise if some part of the ui keeps a reference to the ips object, it won't
             * contain the correct state.
             */
            ipsSrcFile.getIpsObject();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        if (TRACE) {
            logMethodStarted("refresh"); //$NON-NLS-1$
        }

        IEditorPart editorPart = getActivePageInstance();
        if (editorPart instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editorPart;
            page.refresh();
        }

        updateDataChangeableState();

        if (TRACE) {
            logMethodFinished("refresh"); //$NON-NLS-1$
        }
    }

    /**
     * Evaluates the new data changeable state and updates it, if it has changed.
     */
    public void updateDataChangeableState() {
        if (TRACE) {
            logMethodStarted("updateDataChangeable"); //$NON-NLS-1$
        }

        boolean newState = computeDataChangeableState();

        if (TRACE) {
            log("Next data changeable state=" + newState + ", oldState=" + isDataChangeable()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        setDataChangeable(newState);
        IEditorPart editor = getActivePageInstance();
        if (editor instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editor;
            page.updateDataChangeableState();
        }

        if (TRACE) {
            logMethodFinished("updateDataChangeable"); //$NON-NLS-1$
        }
    }

    /**
     * Evaluates whether the data shown in this editor is changeable by the user.
     * <p>
     * The data is changeable if the ips source file shown in the editor is mutable and the working
     * mode preference is set to edit mode.
     * <p>
     * Subclasses may override this method.
     */
    protected boolean computeDataChangeableState() {
        return IpsUIPlugin.isEditable(ipsSrcFile);
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
     * <p>
     * This method is final. If you want to change an editor's data changeable behaviour override
     * {@link #computeDataChangeableState()}.
     */
    final protected void setDataChangeable(boolean changeable) {
        contentChangeable = Boolean.valueOf(changeable);
        if (getIpsSrcFile() != null) {
            setTitleImage(errorTickupdater.getDecoratedImage());
        }
    }

    public void contentsChanged(final ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(ipsSrcFile)) {
            return;
        }

        Display display = IpsPlugin.getDefault().getWorkbench().getDisplay();
        display.syncExec(new Runnable() {

            public void run() {
                if (TRACE) {
                    logMethodStarted("contentsChanged(): Received content changed event for the file being edited." + event.getEventType()); //$NON-NLS-1$
                }

                if (event.getEventType() == ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED) {
                    updatePageStructure(false);
                }

                refresh();

                if (TRACE) {
                    logMethodFinished("contentChanged()"); //$NON-NLS-1$
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

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            ipsSrcFile.save(true, monitor);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        setDirty(ipsSrcFile.isDirty());
    }

    @Override
    public void doSaveAs() {

    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * We have to close the editor if the underlying resource is removed.
     */
    public void resourceChanged(IResourceChangeEvent event) {
        IResource enclResource = ipsSrcFile.getEnclosingResource();
        if (enclResource == null || event.getDelta() == null
                || event.getDelta().findMember(enclResource.getFullPath()) == null) {
            return;
        }

        if (TRACE) {
            logMethodStarted("resourceChanged(): Received resource changed event for the file being edited."); //$NON-NLS-1$
        }

        if (!ipsSrcFile.exists()) {
            close(false);
        }

        if (TRACE) {
            logMethodFinished("resourceChanged()"); //$NON-NLS-1$
        }
    }

    /**
     * Returns <code>true</code> if the <code>IIpsSrcFile</code> this editor is based upon exists
     * and is in sync.
     */
    protected boolean isSrcFileUsable() {
        return ipsSrcFile != null && ipsSrcFile.exists()
                && ipsSrcFile.getEnclosingResource().isSynchronized(IResource.DEPTH_ONE);
    }

    /**
     * Returns <code>true</code> if this is the active editor, otherwise <code>false</code>.
     */
    protected boolean isActive() {
        return this == getSite().getPage().getActiveEditor();
    }

    protected void handleEditorActivation() {
        if (TRACE) {
            logMethodStarted("handleEditorActivation()"); //$NON-NLS-1$
        }

        checkForChangesMadeOutsideEclipse();
        editorActivated();
        refresh();

        if (TRACE) {
            logMethodFinished("handleEditorActivation()"); //$NON-NLS-1$
        }
    }

    private void checkForChangesMadeOutsideEclipse() {
        if (dontLoadChanges || isCheckingForChangesMadeOutsideEclipse) {
            return;
        }

        try {

            isCheckingForChangesMadeOutsideEclipse = true;
            if (TRACE) {
                logMethodStarted("checkForChangesMadeOutsideEclipse()"); //$NON-NLS-1$
            }

            if (getIpsSrcFile().isMutable() && !getIpsSrcFile().getEnclosingResource().isSynchronized(0)) {
                MessageDialog dlg = new MessageDialog(Display.getCurrent().getActiveShell(),
                        Messages.IpsObjectEditor_fileHasChangesOnDiskTitle, (Image)null,
                        Messages.IpsObjectEditor_fileHasChangesOnDiskMessage, MessageDialog.QUESTION, new String[] {
                                Messages.IpsObjectEditor_fileHasChangesOnDiskYesButton,
                                Messages.IpsObjectEditor_fileHasChangesOnDiskNoButton }, 0);
                dlg.open();
                if (dlg.getReturnCode() == 0) {
                    try {
                        if (TRACE) {
                            log("checkForChangesMadeOutsideEclipse(): Change found, sync file with filesystem (refreshLocal)"); //$NON-NLS-1$
                        }
                        getIpsSrcFile().getEnclosingResource().refreshLocal(0, null);
                        updatePageStructure(true);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    dontLoadChanges = true;
                }
            }

            if (TRACE) {
                logMethodFinished("checkForChangesMadeOutsideEclipse()"); //$NON-NLS-1$
            }

        } finally {
            isCheckingForChangesMadeOutsideEclipse = false;
        }
    }

    /**
     * Called when the editor is activated (e.g. by clicking in it).
     */
    protected void editorActivated() {
        if (TRACE) {
            logMethodStarted("editorActivated()"); //$NON-NLS-1$
        }

        checkForInconsistenciesToModel();

        if (TRACE) {
            logMethodFinished("editorActivated()"); //$NON-NLS-1$
        }
    }

    /**
     * Does what the method name says :-)
     */
    protected void checkForInconsistenciesToModel() {
        if (TRACE) {
            logMethodStarted("checkForInconsistenciesToModel"); //$NON-NLS-1$
        }

        if (isDataChangeable() == null || !isDataChangeable().booleanValue()) {
            if (TRACE) {
                logMethodFinished("checkForInconsistenciesToModel - no need to check, content is read-only."); //$NON-NLS-1$
            }
            return;
        }

        if (!getIpsSrcFile().exists()) {
            if (TRACE) {
                logMethodFinished("checkForInconsistenciesToModel - no need to check, file does not exists."); //$NON-NLS-1$
            }
            return;
        }

        if (getSettings().getBoolean(getIpsSrcFile(), SETTING_DONT_FIX_DIFFERENCES)) {
            if (TRACE) {
                logMethodFinished("checkForInconsistenciesToModel - no need to check, user decided no to fix."); //$NON-NLS-1$
            }
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

            if (!toFixIpsObject.containsDifferenceToModel(getIpsProject())) {
                if (TRACE) {
                    logMethodFinished("checkForInconsistenciesToModel - no differences found."); //$NON-NLS-1$
                }
                return;
            }

            Dialog dialog = createDialogToFixDifferencesToModel();
            if (dialog.open() == Window.OK) {
                if (TRACE) {
                    log("checkForInconsistenciesToModel - differences found, start fixing differenced."); //$NON-NLS-1$
                }
                IWorkspaceRunnable fix = new IWorkspaceRunnable() {
                    public void run(IProgressMonitor monitor) throws CoreException {
                        toFixIpsObject.fixAllDifferencesToModel(getIpsProject());
                    }
                };
                IpsPlugin.getDefault().getIpsModel().runAndQueueChangeEvents(fix, null);
                refreshInclStructuralChanges();
            } else {
                getSettings().put(getIpsSrcFile(), SETTING_DONT_FIX_DIFFERENCES, true);
            }

            if (TRACE) {
                logMethodFinished("checkForInconsistenciesToModel"); //$NON-NLS-1$
            }

        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }

    /**
     * Creates a dialog to disblay the differences to the model and ask the user if the
     * inconsistencies should be fixed. Specific logic has to be implemented in subclasses.
     * 
     * @throws CoreException May be thrown if any error occurs.
     */
    protected Dialog createDialogToFixDifferencesToModel() throws CoreException {
        throw new UnsupportedOperationException();
    }

    /**
     * Refreshes the UI and can handle structural changes which means not only the content of the
     * controls is updated but also new controls are created or existing ones are disposed if
     * neccessary.
     */
    protected void refreshInclStructuralChanges() {
        if (updatingPageStructure) {
            return;
        }

        refresh();
    }

    /**
     * Returns the <code>SelectionProviderDispatcher</code> which is the
     * <code>ISelectionProvider</code> for this <code>IEditorPart</code>.
     */
    public SelectionProviderDispatcher getSelectionProviderDispatcher() {
        return selectionProviderDispatcher;
    }

    @Override
    public final void dispose() {
        super.dispose();

        if (selectionProviderDispatcher != null) {
            selectionProviderDispatcher.dispose();
        }

        if (activationListener != null) {
            activationListener.dispose();
        }

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        if (errorTickupdater != null) {
            errorTickupdater.dispose();
        }

        disposeInternal();

        if (TRACE) {
            log("disposed."); //$NON-NLS-1$
        }
    }

    /**
     * Empty. Can be overridden by subclasses for dispose purposes.
     */
    protected void disposeInternal() {

    }

    public void propertyChange(PropertyChangeEvent event) {
        if (TRACE) {
            logMethodStarted("propertyChange(): Received property changed event " + event); //$NON-NLS-1$
        }

        if (!isActive()) {
            return;
        }

        if (event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            refresh();
        }

        if (TRACE) {
            logMethodFinished("propertyChange()"); //$NON-NLS-1$
        }
    }

    /**
     * Returns the settings for ips object editors. This method never returns <code>null</code>.
     */
    protected IIpsObjectEditorSettings getSettings() {
        return IpsUIPlugin.getDefault().getIpsEditorSettings();
    }

    @Override
    public String toString() {
        return "Editor for " + getIpsSrcFile(); //$NON-NLS-1$
    }

    private void logMethodStarted(String msg) {
        logInternal("." + msg + " - started"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    private void logMethodFinished(String msg) {
        logInternal("." + msg + " - finished"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    private void log(String msg) {
        logInternal(": " + msg); //$NON-NLS-1$
    }

    private void logInternal(String msg) {
        String file = ipsSrcFile == null ? "null" : ipsSrcFile.getName(); // $NON-NLS-1$ //$NON-NLS-1$
        System.out.println(getLogPrefix() + msg
                + ", IpsSrcFile=" + file + ", Thread=" + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    private String getLogPrefix() {
        return "IpsObjectEditor"; //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    // eclipse api is not generified
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.equals(IContentOutlinePage.class)) {
            if (null == outlinePage) {
                outlinePage = new OutlinePage();
            }
            return outlinePage;
        }
        return super.getAdapter(adapter);
    }

    private class OutlinePage extends ContentOutlinePage {
        @Override
        public void createControl(Composite gParent) {
            super.createControl(gParent);
            TreeViewer treeView = super.getTreeViewer();
            treeView.setContentProvider(new WorkbenchContentProvider());
            treeView.setLabelProvider(new WorkbenchLabelProvider());
            treeView.setInput(getIpsSrcFile());
        }
    }

    /**
     * Internal part and shell activation listener.
     * <p>
     * Copied from <code>AbstractTextEditor</code>.
     */
    private class ActivationListener implements IPartListener, IWindowListener {

        private IPartService partService;

        /**
         * Creates this activation listener.
         * 
         * @param partService the part service on which to add the part listener
         * 
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
            partService = null;
        }

        public void partActivated(IWorkbenchPart part) {
            if (part != IpsObjectEditor.this) {
                return;
            }

            handleEditorActivation();
        }

        public void partBroughtToTop(IWorkbenchPart part) {

        }

        public void partClosed(IWorkbenchPart part) {
            if (part != IpsObjectEditor.this) {
                return;
            }

            ipsSrcFile.discardChanges();
            removeListeners();

            if (!IpsPlugin.getDefault().getWorkbench().isClosing()) {
                IIpsObjectEditorSettings settings = IpsUIPlugin.getDefault().getIpsEditorSettings();
                settings.remove(ipsSrcFile);
            }
        }

        public void partDeactivated(IWorkbenchPart part) {

        }

        private void removeListeners() {
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(IpsObjectEditor.this);
            IpsPlugin.getDefault().getIpsModel().removeModificationStatusChangeListener(IpsObjectEditor.this);
            IpsPlugin.getDefault().getIpsPreferences().removeChangeListener(IpsObjectEditor.this);
        }

        public void partOpened(IWorkbenchPart part) {

        }

        public void windowActivated(IWorkbenchWindow window) {
            if (window == getEditorSite().getWorkbenchWindow()) {
                checkForChangesMadeOutsideEclipse();
            }
        }

        public void windowDeactivated(IWorkbenchWindow window) {

        }

        public void windowClosed(IWorkbenchWindow window) {

        }

        public void windowOpened(IWorkbenchWindow window) {

        }

    }

    /*
     * The <code>IpsObjectEditorErrorMarkerUpdater</code> will register as a
     * IIpsProblemChangedListener to listen on ips problem changes that correspond to the editor's
     * input. It updates the title images and refreshes the editor if it is active.
     * 
     * @author Joerg Ortmann, Peter Erzberger
     */
    private class IpsObjectEditorErrorMarkerUpdater implements IIpsProblemChangedListener {

        private IpsObjectEditor ipsObjectEditor;
        private IpsProblemsLabelDecorator decorator;

        public IpsObjectEditorErrorMarkerUpdater(IpsObjectEditor ipsObjectEditor) {
            this.ipsObjectEditor = ipsObjectEditor;
            decorator = new IpsProblemsLabelDecorator();
            IpsUIPlugin.getDefault().getIpsProblemMarkerManager().addListener(this);
        }

        /**
         * {@inheritDoc}
         */
        public void problemsChanged(IResource[] changedResources) {
            if (ipsObjectEditor.getIpsSrcFile() == null) {
                return; // can happen during editor init
            }
            IResource correspondingResource = ipsObjectEditor.getIpsSrcFile().getCorrespondingResource();
            if (correspondingResource != null) {
                for (int i = 0; i < changedResources.length; i++) {
                    if (changedResources[i].equals(correspondingResource)) {
                        updateEditorImage();
                    }
                }
            }
        }

        /**
         * Returns the image of the ips object inside the ips object editor which is optional
         * decorated with an ips marker image if a marker exists.
         */
        Image getDecoratedImage() {
            Image titleImage = IpsUIPlugin.getImageHandling().getImage(ipsObjectEditor.getIpsSrcFile(),
                    ipsObjectEditor.isDataChangeable().booleanValue());
            return decorator.decorateImage(titleImage, ipsObjectEditor.getIpsSrcFile());
        }

        private void updateEditorImage() {
            Image image = getDecoratedImage();
            postImageChange(image);
        }

        private void postImageChange(final Image newImage) {
            Shell shell = ipsObjectEditor.getEditorSite().getShell();
            if (shell != null && !shell.isDisposed()) {
                shell.getDisplay().syncExec(new Runnable() {
                    public void run() {
                        if (isActive()) {
                            refresh();
                        }
                        setTitleImage(newImage);
                    }
                });
            }
        }

        public void dispose() {
            decorator.dispose();
            IpsUIPlugin.getDefault().getIpsProblemMarkerManager().removeListener(this);
        }

    }

}
