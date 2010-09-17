/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.SelectionProviderDispatcher;

public class ProjectPropertyEditor extends FormEditor implements ContentsChangeListener {
    private IIpsSrcFile ipsSrcFile;
    private boolean isCheckingForChangesMadeOutsideEclipse;
    private boolean dontLoadChanges;
    private ActivationListener activationListener;
    private SelectionProviderDispatcher selectionProviderDispatcher;
    private boolean updatingPageStructure;
    private boolean pagesForParsableSrcFileShown;
    private IIpsProjectProperties aaa;
    public final static boolean TRACE = IpsPlugin.TRACE_UI;

    public ProjectPropertyEditor() {
        super();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void doSaveAs() {
        // TODO Auto-generated method stub

    }

    private void checkForChangesMadeOutsideEclipse() {
        // if (dontLoadChanges || isCheckingForChangesMadeOutsideEclipse) {
        // return;
        // }
        //
        // try {
        //
        // isCheckingForChangesMadeOutsideEclipse = true;
        // if (TRACE) {
        //                logMethodStarted("checkForChangesMadeOutsideEclipse()"); //$NON-NLS-1$
        // }
        //
        // if (getIpsSrcFile().isMutable() &&
        // !getIpsSrcFile().getEnclosingResource().isSynchronized(0)) {
        // MessageDialog dlg = new MessageDialog(Display.getCurrent().getActiveShell(),
        // Messages.IpsObjectEditor_fileHasChangesOnDiskTitle, (Image)null,
        // Messages.IpsObjectEditor_fileHasChangesOnDiskMessage, MessageDialog.QUESTION, new
        // String[] {
        // Messages.IpsObjectEditor_fileHasChangesOnDiskYesButton,
        // Messages.IpsObjectEditor_fileHasChangesOnDiskNoButton }, 0);
        // dlg.open();
        // if (dlg.getReturnCode() == 0) {
        // try {
        // if (TRACE) {
        //                            log("checkForChangesMadeOutsideEclipse(): Change found, sync file with filesystem (refreshLocal)"); //$NON-NLS-1$
        // }
        // getIpsSrcFile().getEnclosingResource().refreshLocal(0, null);
        // updatePageStructure(true);
        // } catch (CoreException e) {
        // throw new RuntimeException(e);
        // }
        // } else {
        // dontLoadChanges = true;
        // }
        // }
        //
        // if (TRACE) {
        //                logMethodFinished("checkForChangesMadeOutsideEclipse()"); //$NON-NLS-1$
        // }
        //
        // } finally {
        // isCheckingForChangesMadeOutsideEclipse = false;
        // }
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (TRACE) {
            logMethodStarted("init"); //$NON-NLS-1$
        }

        super.init(site, input);

        IIpsModel model = IpsPlugin.getDefault().getIpsModel();

        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            aaa = model.getIpsProject(file.getProject()).getProperties();
            if (aaa != null) {

            }
            // ipsSrcFile = (IIpsSrcFile)model.getIpsElement(file);
        }

        String title = "Projectproperties"; // ipsSrcFile.getIpsObjectName();
        setPartName(title);
        // setContentDescription(ipsSrcFile.getParent().getEnclosingResource().getFullPath().toOSString());

        // if (ipsSrcFile.isMutable() && !ipsSrcFile.getEnclosingResource().isSynchronized(0)) {
        // try {
        // ipsSrcFile.getEnclosingResource().refreshLocal(0, null);
        // } catch (CoreException e) {
        //                throw new PartInitException("Error refreshing resource " + ipsSrcFile.getEnclosingResource()); //$NON-NLS-1$
        // }
        // }

        /**
         * Check if the ips src file is valid and could be edited in the editor, if the ips src file
         * doesn't exists (e.g. ips src file outside ips package) close the editor and open the
         * current file in the default text editor.
         */
        // if (!ipsSrcFile.exists()) {
        // Runnable closeRunnable = new Runnable() {
        // @Override
        // public void run() {
        // ProjectPropertyEditor.this.close(false);
        // IpsUIPlugin.getDefault().openEditor(ipsSrcFile.getCorrespondingFile());
        // }
        // };
        // getSite().getShell().getDisplay().syncExec(closeRunnable);
        // } else {
        activationListener = new ActivationListener(site.getPage());
        selectionProviderDispatcher = new SelectionProviderDispatcher();
        site.setSelectionProvider(selectionProviderDispatcher);
        // IpsUIPlugin.getDefault().addHistoryItem(ipsSrcFile);
        // }

        setDataChangeable(computeDataChangeableState());

        if (TRACE) {
            logMethodFinished("init"); //$NON-NLS-1$
        }

    }

    @Override
    protected void createPages() {
        super.createPages();

        // ResourcesPlugin.getWorkspace().addResourceChangeListener(IpsObjectEditor.this);
        // IpsPlugin.getDefault().getIpsModel().addChangeListener(IpsObjectEditor.this);
        // IpsPlugin.getDefault().getIpsModel().addModifcationStatusChangeListener(IpsObjectEditor.this);
        // IpsPlugin.getDefault().getIpsPreferences().addChangeListener(IpsObjectEditor.this);
        // activateContext();
    }

    @Override
    final protected void addPages() {

        try {
            addPage(new DatatypesPropertiesPage(this));
            addPage(new OverviewPropertiesPage(this));
            addPage(new BuildPathPropertiesPage(this));
            addPage(new CodeGeneratorPropertiesPage(this));
            addPage(new ModellPropertiesPage(this));
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setDataChangeable(boolean computeDataChangeableState) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDirty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

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

    public IIpsProjectProperties getProperty() {
        return aaa;
    }

    private String getLogPrefix() {
        return "ProjectPropertyEditor"; //$NON-NLS-1$
    }

    protected boolean computeDataChangeableState() {
        return IpsUIPlugin.isEditable(ipsSrcFile);
    }

    protected void handleEditorActivation() {
        if (TRACE) {
            logMethodStarted("handleEditorActivation()"); //$NON-NLS-1$
        }

        checkForChangesMadeOutsideEclipse();
        // editorActivated();
        // refresh();

        if (TRACE) {
            logMethodFinished("handleEditorActivation()"); //$NON-NLS-1$
        }
    }

    protected void updatePageStructure(boolean forceRefreshInclStructuralChanges) {

    }

    private void refreshInclStructuralChanges() {
        // TODO Auto-generated method stub

    }

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
            if (part != ProjectPropertyEditor.this) {
                return;
            }

            handleEditorActivation();
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
            // Nothing to do
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            if (part != ProjectPropertyEditor.this) {
                return;
            }
            removeListeners();

            if (!IpsPlugin.getDefault().getWorkbench().isClosing()) {
                // IIpsObjectEditorSettings settings =
                // IpsUIPlugin.getDefault().getIpsEditorSettings();
                // settings.remove(ipsSrcFile);
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            // Nothing to do
        }

        private void removeListeners() {
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(ProjectPropertyEditor.this);
            // IpsPlugin.getDefault().getIpsModel().removeModificationStatusChangeListener(ProjectPropertyEditor.this);
            // IpsPlugin.getDefault().getIpsPreferences().removeChangeListener(ProjectPropertyEditor.this);
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

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        // TODO Auto-generated method stub

    }
}
