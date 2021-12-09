/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.IpsSrcFileFromEditorInputFactory;
import org.faktorips.devtools.core.ui.util.UiMessage;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.outline.OutlinePage;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IModificationStatusChangeListener;
import org.faktorips.devtools.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

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
 * to be register the <code>SelectionProviderIntermediate</code> of this editor. The currently
 * active selection provider should set
 * {@link SelectionProviderIntermediate#setSelectionProviderDelegate(org.eclipse.jface.viewers.ISelectionProvider)}
 * . If your composite implements the interface {@link ICompositeWithSelectableViewer} the
 * {@link IpsObjectEditorPage} would find the composite and register the necessary listeners
 * automatically. Otherwise you could handle setting and removing your selection provider for your
 * own.
 * 
 * @see org.eclipse.jface.viewers.ISelectionProvider
 * @see SelectionProviderIntermediate
 * @see ICompositeWithSelectableViewer
 */
public abstract class IpsObjectEditor extends FormEditor implements ContentsChangeListener,
        IModificationStatusChangeListener, IResourceChangeListener, IPropertyChangeListener, IIpsSrcFileEditor {

    public static final boolean TRACE = IpsPlugin.TRACE_UI;

    /**
     * Setting key for user's decision not to fix the differences between the product definition
     * structure and the model structure
     */
    private static final String SETTING_DONT_FIX_DIFFERENCES = "dontFixDifferences"; //$NON-NLS-1$

    private static final int MAX_MSG_LIST_SIZE = 5;

    /** The file that's being edited (if any) */
    private IIpsSrcFile ipsSrcFile;

    private boolean dirty = false;

    private boolean contentChangeable = false;

    /** The editor's ISelectionProvider */
    private SelectionProviderIntermediate selectionProviderDispatcher;

    /**
     * Storage for the user's decision not to load the changes made directly in the file system.
     */
    private boolean dontLoadChanges = false;

    private boolean isCheckingForChangesMadeOutsideEclipse = false;

    /**
     * True if the editor contains the pages that are shown for a parsable ips source file, false if
     * an error page is shown.
     */
    private boolean pagesForParsableSrcFileShown;

    private boolean updatingPageStructure = false;

    private ActivationListener activationListener;

    /** Updates the title image if there are ips marker changes on the editor's input */
    private IpsObjectEditorErrorMarkerUpdater errorTickupdater;

    /**
     * Encapsulates the creation of an {@link IIpsSrcFile} dependent on a {@link IEditorInput}.
     */
    private IpsSrcFileFromEditorInputFactory ipsSrcFileFromEditorInputFactory;

    private IContentOutlinePage outlinePage;

    /**
     * Creates a new <code>IpsObjectEditor</code>.
     */
    public IpsObjectEditor() {
        super();
        errorTickupdater = new IpsObjectEditorErrorMarkerUpdater(this);
        ipsSrcFileFromEditorInputFactory = new IpsSrcFileFromEditorInputFactory();
    }

    /**
     * Returns the ips src file being edited.
     * 
     * @return Returns the ips src file to be edited by this editor.
     */
    @Override
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
        if (getIpsSrcFile().exists()) {
            return getIpsSrcFile().getIpsObject();
        } else {
            return null;
        }
    }

    /** Returns the title that is shown on every page. */
    protected abstract String getUniformPageTitle();

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        logMethodStarted("init"); //$NON-NLS-1$
        super.init(site, input);

        ipsSrcFile = ipsSrcFileFromEditorInputFactory.createIpsSrcFile(input);

        if (ipsSrcFile == null) {
            throw new PartInitException("Unsupported editor input type " + input.getClass().getName()); //$NON-NLS-1$
        }

        String title = ipsSrcFile.getIpsObjectName();
        setPartName(title);
        setContentDescription(ipsSrcFile.getParent().getEnclosingResource().getWorkspaceRelativePath().toOSString());

        if (ipsSrcFile.isMutable() && !ipsSrcFile.getEnclosingResource().isSynchronized(0)) {
            try {
                ipsSrcFile.getEnclosingResource().refreshLocal(0, null);
            } catch (CoreException e) {
                throw new PartInitException("Error refreshing resource " + ipsSrcFile.getEnclosingResource()); //$NON-NLS-1$
            }
        }

        /**
         * Check if the ips src file is valid and could be edited in the editor, if the ips src file
         * doesn't exists (e.g. ips src file outside ips package) close the editor and open the
         * current file in the default text editor.
         */
        if (!ipsSrcFile.exists()) {
            Runnable closeRunnable = () -> {
                IpsObjectEditor.this.close(false);
                IpsUIPlugin.getDefault().openEditor(ipsSrcFile.getCorrespondingFile());
            };
            getSite().getShell().getDisplay().syncExec(closeRunnable);
        } else {
            activationListener = new ActivationListener(site.getPage());
            selectionProviderDispatcher = new SelectionProviderIntermediate();
            site.setSelectionProvider(selectionProviderDispatcher);
            IpsUIPlugin.getDefault().addHistoryItem(ipsSrcFile);
        }

        setDataChangeable(computeDataChangeableState());

        logMethodFinished("init"); //$NON-NLS-1$
    }

    @Override
    protected void createPages() {
        super.createPages();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(IpsObjectEditor.this);
        IIpsModel.get().addChangeListener(IpsObjectEditor.this);
        IIpsModel.get().addModifcationStatusChangeListener(IpsObjectEditor.this);
        IpsPlugin.getDefault().getIpsPreferences().addChangeListener(IpsObjectEditor.this);
        activateContext();
    }

    /**
     * 
     * Activate a context that this view uses. It will be tied to this * view activation events and
     * will be removed when the view is disposed.
     */

    private void activateContext() {
        IContextService service = getSite().getService(IContextService.class);
        service.activateContext("org.faktorips.devtools.core.ui.views.modelExplorer.context"); //$NON-NLS-1$
    }

    @Override
    protected final void addPages() {
        logMethodStarted("addPages"); //$NON-NLS-1$

        pagesForParsableSrcFileShown = false;

        try {

            if (getIpsSrcFile() == null) {
                log("addPages(): Page for unreachable file created."); //$NON-NLS-1$
                addPage(new UnreachableFilePage(this));
                return;
            }

            if (!getIpsSrcFile().isContentParsable()) {
                log("addPages(): Page for unparsable files created."); //$NON-NLS-1$
                addPage(new UnparsableFilePage(this));
                return;
            }

            if (!ipsSrcFile.exists()) {
                log("addPages(): Page for missing files created."); //$NON-NLS-1$
                addPage(new MissingResourcePage(this));
                return;
            }

            logMethodStarted("addPagesForParsableSrcFile()"); //$NON-NLS-1$
            addPagesForParsableSrcFile();
            addPage(new DocumentationPage(this));

            logMethodFinished("addPagesForParsableSrcFile()"); //$NON-NLS-1$

            pagesForParsableSrcFileShown = true;
            logMethodFinished("addPages"); //$NON-NLS-1$

        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * This method extends the <code>addPages()</code> operation and must be implemented by
     * subclasses by adding the pages to edit the ips object with.
     * 
     * @throws PartInitException if there is an exception while initializing a part
     * @throws CoreRuntimeException in case of other core exceptions
     */
    protected abstract void addPagesForParsableSrcFile() throws CoreRuntimeException;

    protected void updatePageStructure(boolean forceRefreshInclStructuralChanges) {
        logMethodStarted("updatePageStructure"); //$NON-NLS-1$
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
                    refreshIncludingStructuralChanges();
                }
                return;
            }
            updatingPageStructure = true;
            ipsSrcFile.getIpsObject();
            // remove all pages
            for (int i = getPageCount(); i > 0; i--) {
                removePage(0);
            }
            log("updatePageStructure(): Existing pages removed. Must recreate."); //$NON-NLS-1$
            addPages();
            updatingPageStructure = false;
            // also triggers the refresh
            super.setActivePage(0);
            logMethodFinished("updatePageStructure"); //$NON-NLS-1$
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
        logMethodStarted("pageChange(): newPage=" + newPageIndex); //$NON-NLS-1$

        // must be called even if the file isn't parsable,
        // otherwise the unparsable file page wouldn't be shown
        super.pageChange(newPageIndex);

        refresh();
        logMethodFinished("pageChange(): newPage=" + newPageIndex); //$NON-NLS-1$
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

        logMethodStarted("refresh"); //$NON-NLS-1$

        // check to enable all controls,
        // note that this must be done before refresh the control states because
        // maybe a control are disabled by the controls own enable/disable logic
        if (computeDataChangeableState()) {
            updateDataChangeableState();
        }

        // refresh the pages and control state
        IEditorPart editorPart = getActivePageInstance();
        if (editorPart instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editorPart;
            page.refresh();
        }

        // check to disable all controls,
        // note that this must be done after refresh the control states because
        // maybe a control will be enabled by the controls own enable/disable logic
        if (!computeDataChangeableState()) {
            updateDataChangeableState();
        }

        updateHeaderMessage();

        logMethodFinished("refresh"); //$NON-NLS-1$
    }

    /**
     * Evaluates the new data changeable state and updates it
     * 
     */
    public void updateDataChangeableState() {
        boolean newState = computeDataChangeableState();
        setDataChangeable(newState);
        IEditorPart editor = getActivePageInstance();
        if (editor instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage page = (IpsObjectEditorPage)editor;
            page.updateDataChangeableState();
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
        return IpsUIPlugin.isEditable(ipsSrcFile) && isPageChangeable();
    }

    private boolean isPageChangeable() {
        if (getActivePageInstance() instanceof IpsObjectEditorPage) {
            IpsObjectEditorPage ipsPage = (IpsObjectEditorPage)getActivePageInstance();
            return ipsPage.computeDataChangeableState();
        } else {
            return true;
        }
    }

    /**
     * Returns <code>true</code> if the data shown in this editor is changeable by the user,
     * otherwise <code>false</code>.
     * <p>
     * This method is not intended to be overwritten by client. Instead you should overwrite
     * {@link #computeDataChangeableState()}.
     */
    public boolean isDataChangeable() {
        return contentChangeable;
    }

    /**
     * Sets the content changeable state.
     * <p>
     * This method is final. If you want to change an editor's data changeable behavior override
     * {@link #computeDataChangeableState()}.
     */
    protected final void setDataChangeable(boolean changeable) {
        contentChangeable = changeable;
        if (getIpsSrcFile() != null) {
            setTitleImage(errorTickupdater.getDecoratedImage());
        }
    }

    @Override
    public void contentsChanged(final ContentChangeEvent event) {
        if (!event.getIpsSrcFile().equals(ipsSrcFile)) {
            return;
        }
        if (isVisible()) {
            Display display = IpsPlugin.getDefault().getWorkbench().getDisplay();
            display.asyncExec(() -> {
                logMethodStarted("contentsChanged(): Received content changed event for the file being edited." //$NON-NLS-1$
                        + event.getEventType());

                updateHeaderMessage();
                if (event.isAffected(getIpsObject())) {
                    updatePageStructure(false);
                }

                logMethodFinished("contentChanged()"); //$NON-NLS-1$
            });
        }
    }

    @Override
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
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        setDirty(ipsSrcFile.isDirty());
    }

    @Override
    public void doSaveAs() {
        // empty implementation. save as not supported @see #isSaveAsAllowed
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * We have to close the editor if the underlying resource is removed.
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IResource enclResource = ipsSrcFile.getEnclosingResource();
        if (enclResource == null || event.getDelta() == null
                || event.getDelta().findMember(enclResource.getFullPath()) == null) {
            return;
        }

        logMethodStarted("resourceChanged(): Received resource changed event for the file being edited."); //$NON-NLS-1$

        if (!ipsSrcFile.exists()) {
            close(false);
        }

        logMethodFinished("resourceChanged()"); //$NON-NLS-1$
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
        return Display.getCurrent().getActiveShell() == getSite().getShell()
                && this == getSite().getPage().getActiveEditor();
    }

    protected boolean isVisible() {
        return getSite().getPage().isPartVisible(this);
    }

    protected void handleEditorActivation() {
        logMethodStarted("handleEditorActivation()"); //$NON-NLS-1$

        checkForChangesMadeOutsideEclipse();
        editorActivated();
        refresh();

        logMethodFinished("handleEditorActivation()"); //$NON-NLS-1$
    }

    private void checkForChangesMadeOutsideEclipse() {
        if (dontLoadChanges || isCheckingForChangesMadeOutsideEclipse) {
            return;
        }

        try {

            isCheckingForChangesMadeOutsideEclipse = true;
            logMethodStarted("checkForChangesMadeOutsideEclipse()"); //$NON-NLS-1$

            if (getIpsSrcFile().isMutable() && !getIpsSrcFile().getEnclosingResource().isSynchronized(0)) {
                MessageDialog dlg = new MessageDialog(Display.getCurrent().getActiveShell(),
                        Messages.IpsObjectEditor_fileHasChangesOnDiskTitle, (Image)null,
                        Messages.IpsObjectEditor_fileHasChangesOnDiskMessage, MessageDialog.QUESTION,
                        new String[] { Messages.IpsObjectEditor_fileHasChangesOnDiskYesButton,
                                Messages.IpsObjectEditor_fileHasChangesOnDiskNoButton },
                        0);
                dlg.open();
                if (dlg.getReturnCode() == 0) {
                    try {
                        log("checkForChangesMadeOutsideEclipse(): Change found, sync file with filesystem (refreshLocal)"); //$NON-NLS-1$
                        getIpsSrcFile().getEnclosingResource().refreshLocal(0, null);
                        updatePageStructure(true);
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    dontLoadChanges = true;
                }
            }

            logMethodFinished("checkForChangesMadeOutsideEclipse()"); //$NON-NLS-1$

        } finally {
            isCheckingForChangesMadeOutsideEclipse = false;
        }
    }

    /**
     * Called when the editor is activated (e.g. by clicking in it).
     */
    protected void editorActivated() {
        logMethodStarted("editorActivated()"); //$NON-NLS-1$
        checkForInconsistenciesToModel();
        logMethodFinished("editorActivated()"); //$NON-NLS-1$
    }

    /**
     * Does what the method name says :-)
     */
    protected void checkForInconsistenciesToModel() {
        logMethodStarted("checkForInconsistenciesToModel"); //$NON-NLS-1$

        if (!isDataChangeable()) {
            logMethodFinished("checkForInconsistenciesToModel - no need to check, content is read-only."); //$NON-NLS-1$
            return;
        }

        if (!getIpsSrcFile().exists()) {
            logMethodFinished("checkForInconsistenciesToModel - no need to check, file does not exists."); //$NON-NLS-1$
            return;
        }

        if (getSettings().getBoolean(getIpsSrcFile(), SETTING_DONT_FIX_DIFFERENCES)) {
            logMethodFinished("checkForInconsistenciesToModel - no need to check, user decided no to fix."); //$NON-NLS-1$
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
                logMethodFinished("checkForInconsistenciesToModel - no differences found."); //$NON-NLS-1$
                return;
            }

            Dialog dialog = createDialogToFixDifferencesToModel();
            if (dialog.open() == Window.OK) {
                log("checkForInconsistenciesToModel - differences found, start fixing differenced."); //$NON-NLS-1$
                ICoreRunnable fix = $ -> toFixIpsObject.fixAllDifferencesToModel(getIpsProject());
                IpsUIPlugin.getDefault().runWorkspaceModification(fix);
                refreshIncludingStructuralChanges();
            } else {
                getSettings().put(getIpsSrcFile(), SETTING_DONT_FIX_DIFFERENCES, true);
            }

            logMethodFinished("checkForInconsistenciesToModel"); //$NON-NLS-1$

        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return;
        }
    }

    /**
     * Creates a dialog to disblay the differences to the model and ask the user if the
     * inconsistencies should be fixed. Specific logic has to be implemented in subclasses.
     * 
     * @throws CoreRuntimeException May be thrown if any error occurs.
     */
    protected Dialog createDialogToFixDifferencesToModel() throws CoreRuntimeException {
        throw new UnsupportedOperationException();
    }

    /**
     * Refreshes the UI and can handle structural changes which means not only the content of the
     * controls is updated but also new controls are created or existing ones are disposed if
     * necessary.
     */
    protected void refreshIncludingStructuralChanges() {
        if (updatingPageStructure) {
            return;
        }
        refresh();
    }

    /**
     * Returns the <code>SelectionProviderIntermediate</code> which is the
     * <code>ISelectionProvider</code> for this <code>IEditorPart</code>.
     */
    public SelectionProviderIntermediate getSelectionProviderIntermediate() {
        return selectionProviderDispatcher;
    }

    @Override
    public final void dispose() {
        super.dispose();

        if (selectionProviderDispatcher != null) {
            selectionProviderDispatcher.setSelectionProviderDelegate(null);
        }

        if (activationListener != null) {
            activationListener.dispose();
        }

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        if (errorTickupdater != null) {
            errorTickupdater.dispose();
        }

        disposeInternal();

        log("disposed."); //$NON-NLS-1$
    }

    /**
     * Empty. Can be overridden by subclasses for dispose purposes.
     */
    protected void disposeInternal() {
        // Default implementation does nothing, may be overridden by subclasses
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        logMethodStarted("propertyChange(): Received property changed event " + event); //$NON-NLS-1$

        if (isVisible() && event.getProperty().equals(IpsPreferences.WORKING_MODE)) {
            refresh();
        }

        logMethodFinished("propertyChange()"); //$NON-NLS-1$
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
        if (TRACE) {
            logInternal("." + msg + " - started"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
        }
    }

    private void logMethodFinished(String msg) {
        if (TRACE) {
            logInternal("." + msg + " - finished"); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
        }
    }

    private void log(String msg) {
        if (TRACE) {
            logInternal(": " + msg); //$NON-NLS-1$
        }
    }

    private void logInternal(String msg) {
        String file = ipsSrcFile == null ? "null" : ipsSrcFile.getName(); // $NON-NLS-1$ //$NON-NLS-1$
        System.out.println(
                getLogPrefix() + msg + ", IpsSrcFile=" + file + ", Thread=" + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
                                                                                                                 // $NON-NLS-2$
    }

    private String getLogPrefix() {
        return "IpsObjectEditor"; //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.equals(IContentOutlinePage.class)) {
            if (null == outlinePage) {
                outlinePage = new OutlinePage(getIpsSrcFile());
            }
            return (T)outlinePage;
        }
        return super.getAdapter(adapter);
    }

    private void updateHeaderMessage() {
        // if getActivaPage = -1 then the editor is not yet initialized
        if (getActivePage() > -1 && isVisible()) {
            List<IMessage> messages = getMessages();
            int messageType = getHighestSeverity(messages);
            setHeaderMessage(messages, messageType);
        }
    }

    protected int getHighestSeverity(List<IMessage> messages) {
        int messageType = IMessageProvider.NONE;
        for (IMessage message : messages) {
            if (message.getMessageType() > messageType) {
                messageType = message.getMessageType();
            }
        }
        return messageType;
    }

    private void setHeaderMessage(List<IMessage> messages, int messageType) {
        ScrolledForm form = getActiveIpsObjectEditorPage().getManagedForm().getForm();
        if (messageType == IMessageProvider.NONE) {
            form.setMessage(StringUtils.EMPTY, IMessageProvider.NONE);
            return;
        }
        form.setMessage(createHeaderMessage(messages, messageType), messageType,
                messages.toArray(new IMessage[messages.size()]));
    }

    protected String createHeaderMessage(List<IMessage> messages, int messageType) {
        if (messages.isEmpty() || messageType == IMessageProvider.NONE) {
            return StringUtils.EMPTY;
        }
        if (messages.size() > 1) {
            return getTextForMultipleMessages(messageType);
        } else {
            return getTextForSingleMessage(messageType);
        }
    }

    private String getTextForMultipleMessages(int messageType) {
        switch (messageType) {
            case IMessageProvider.ERROR:
                return Messages.IpsObjectEditor_multipleErrorMessages;
            case IMessageProvider.WARNING:
                return Messages.IpsObjectEditor_multipleWarningMessages;
            case IMessageProvider.INFORMATION:
                return Messages.IpsObjectEditor_multipleInformationMessages;
            default:
                // should not happen
                return StringUtils.EMPTY;
        }
    }

    private String getTextForSingleMessage(int messageType) {
        switch (messageType) {
            case IMessageProvider.ERROR:
                return Messages.IpsObjectEditor_singleErrorMessage;
            case IMessageProvider.WARNING:
                return Messages.IpsObjectEditor_singleWarningMessage;
            case IMessageProvider.INFORMATION:
                return Messages.IpsObjectEditor_singleInformationMessage;
            default:
                // should not happen
                return StringUtils.EMPTY;
        }
    }

    protected List<IMessage> getMessages() {
        try {
            MessageList msgList = getIpsObject().validate(getIpsProject());
            MessageList subList = msgList.getSubList(MAX_MSG_LIST_SIZE);
            List<IMessage> messages = getUiMessages(subList);

            int oneMore = MAX_MSG_LIST_SIZE + 1;
            if (msgList.size() == oneMore) {
                messages.add(new UiMessage(msgList.getMessage(msgList.size() - 1)));
            } else if (msgList.size() > oneMore) {
                messages.add(new UiMessage(NLS.bind(Messages.IpsPartEditDialog_moreMessagesInTooltip,
                        (msgList.size() - MAX_MSG_LIST_SIZE))));
            }
            return messages;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }

    }

    private List<IMessage> getUiMessages(MessageList msgList) {
        List<IMessage> newList = new ArrayList<>();
        for (Message message : msgList) {
            newList.add(new UiMessage(message));
        }
        return newList;
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

        @Override
        public void partActivated(IWorkbenchPart part) {
            if (part != IpsObjectEditor.this) {
                return;
            }

            handleEditorActivation();
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
            // partBroughtToTop is called for example when selecting an object in package explorer
            // which is already opened in the editor. In this case the editor is visible but not
            // activated, hence no partActivated event is triggered.
            partActivated(part);
        }

        @Override
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

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            // Nothing to do
        }

        private void removeListeners() {
            IIpsModel.get().removeChangeListener(IpsObjectEditor.this);
            IIpsModel.get().removeModificationStatusChangeListener(IpsObjectEditor.this);
            IpsPlugin.getDefault().getIpsPreferences().removeChangeListener(IpsObjectEditor.this);
        }

        @Override
        public void partOpened(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void windowActivated(IWorkbenchWindow window) {
            if (window == getEditorSite().getWorkbenchWindow()) {
                checkForChangesMadeOutsideEclipse();
            }
        }

        @Override
        public void windowDeactivated(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowClosed(IWorkbenchWindow window) {
            // Nothing to do
        }

        @Override
        public void windowOpened(IWorkbenchWindow window) {
            // Nothing to do
        }

    }

    /**
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

        @Override
        public void problemsChanged(IResource[] changedResources) {
            if (ipsObjectEditor.getIpsSrcFile() == null) {
                // can happen during editor init
                return;
            }
            IResource correspondingResource = ipsObjectEditor.getIpsSrcFile().getCorrespondingResource();
            if (correspondingResource != null) {
                for (IResource changedResource : changedResources) {
                    if (changedResource.equals(correspondingResource)) {
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
                    ipsObjectEditor.isDataChangeable());
            return decorator.decorateImage(titleImage, ipsObjectEditor.getIpsSrcFile());
        }

        private void updateEditorImage() {
            Image image = getDecoratedImage();
            postImageChange(image);
        }

        private void postImageChange(final Image newImage) {
            if (newImage == null || newImage.isDisposed()) {
                return;
            }
            Shell shell = ipsObjectEditor.getEditorSite().getShell();
            if (shell != null && !shell.isDisposed()) {
                shell.getDisplay().syncExec(() -> {
                    if (isVisible()) {
                        refresh();
                    }
                    setTitleImage(newImage);
                });
            }
        }

        public void dispose() {
            decorator.dispose();
            IpsUIPlugin.getDefault().getIpsProblemMarkerManager().removeListener(this);
        }

    }
}
