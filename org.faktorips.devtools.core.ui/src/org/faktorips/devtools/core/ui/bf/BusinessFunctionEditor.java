/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.bf;

import java.util.EventObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.bf.edit.BusinessFunctionEditPartFactory;
import org.faktorips.devtools.core.ui.editors.IIpsProblemChangedListener;
import org.faktorips.devtools.core.ui.editors.IIpsSrcFileEditor;
import org.faktorips.devtools.core.ui.editors.Messages;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;

/**
 * The editor for business functions.
 * 
 * @author Peter Erzberger
 */
public class BusinessFunctionEditor extends GraphicalEditorWithFlyoutPalette implements ContentsChangeListener,
        ITabbedPropertySheetPageContributor, IIpsProblemChangedListener, IIpsSrcFileEditor {

    private IIpsSrcFile ipsSrcFile;
    private IBusinessFunction businessFunction;
    private PaletteRoot paletteRoot;
    private IpsProblemsLabelDecorator decorator;
    private ScrollingGraphicalViewer viewer;

    /**
     * Storage for the user's decision not to load the changes made directly in the file system.
     */
    private boolean dontLoadChanges = false;

    private boolean isCheckingForChangesMadeOutsideEclipse = false;

    private ActivationListener activationListener;

    public BusinessFunctionEditor() {
        decorator = new IpsProblemsLabelDecorator();
    }

    @Override
    protected PaletteRoot getPaletteRoot() {
        return paletteRoot;
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    /**
     * Returns the image of the ips object inside the ips object editor which is optional decorated
     * with an ips marker image if a marker exists.
     */
    // TODO duplicate code in IpsObjectEditor
    private Image getDecoratedImage() {
        Image titleImage = IpsUIPlugin.getImageHandling().getImage(ipsSrcFile);
        return decorator.decorateImage(titleImage, ipsSrcFile);
    }

    // TODO duplicate code in IpsObjectEditor
    @Override
    public void problemsChanged(IResource[] changedResources) {
        IResource correspondingResource = ipsSrcFile.getCorrespondingResource();
        if (correspondingResource != null) {
            for (IResource changedResource : changedResources) {
                if (changedResource.equals(correspondingResource)) {
                    postImageChange();
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if this is the active editor, otherwise <code>false</code>.
     */
    private boolean isActive() {
        return this == getSite().getPage().getActiveEditor();
    }

    // TODO duplicate code in IpsObjectEditor
    private void postImageChange() {
        Shell shell = getEditorSite().getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
                    if (isActive()) {
                        refresh();
                    }
                    setTitleImage(getDecoratedImage());
                }
            });
        }
    }

    /**
     * Overridden to activate correct dirty behavior of the editor. (e.g. show the star in the tab,
     * when the editor input has changed)
     */
    @Override
    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        SafeRunner.run(new SafeRunnable() {
            @Override
            public void run() throws Exception {
                ipsSrcFile.save(true, monitor);
                getCommandStack().markSaveLocation();
            }
        });
    }

    /**
     * Returns the business function of this editor.
     */
    public IBusinessFunction getBusinessFunction() {
        return businessFunction;
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        viewer.setRootEditPart(root);
        ConnectionLayer connectionLayer = (ConnectionLayer)root.getLayer(LayerConstants.CONNECTION_LAYER);
        connectionLayer.setConnectionRouter(new BendpointConnectionRouter());
        viewer.setEditPartFactory(new BusinessFunctionEditPartFactory());
    }

    @Override
    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getGraphicalViewer().setContents(getBusinessFunction());
    }

    // TODO part of this code is duplicate in IpsObjectEditor
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        IIpsModel model = IpsPlugin.getDefault().getIpsModel();

        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)input).getFile();
            ipsSrcFile = (IIpsSrcFile)model.getIpsElement(file);
            if (ipsSrcFile == null) {
                return;
            }
            // for what ever reason they made setTitle deprecated. This method does something
            // different than the offered alternatives
            setTitle(ipsSrcFile.getName());
        }

        if (ipsSrcFile == null) {
            throw new PartInitException("Unsupported editor input type " + input.getClass().getName()); //$NON-NLS-1$
        }

        try {
            businessFunction = (IBusinessFunction)ipsSrcFile.getIpsObject();
            ipsSrcFile.getIpsModel().addChangeListener(this);
        } catch (CoreException e) {
            throw new PartInitException("Unable to create a business function object from the provided ips source file"); //$NON-NLS-1$
        }
        paletteRoot = new PaletteBuilder().buildPalette();
        setEditDomain(new DefaultEditDomain(this));
        IpsUIPlugin.getDefault().getIpsProblemMarkerManager().addListener(this);
        activationListener = new ActivationListener(site.getPage());
        super.init(site, input);
    }

    @Override
    public boolean isDirty() {
        if (super.isDirty()) {
            return true;
        }
        return ipsSrcFile.isDirty();
    }

    /**
     * Unregisters this editor as model change listener.
     */
    @Override
    public void dispose() {
        super.dispose();
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        decorator.dispose();
        IpsUIPlugin.getDefault().getIpsProblemMarkerManager().removeListener(this);
        if (activationListener != null) {
            activationListener.dispose();
        }
    }

    /**
     * Fires a dirty event if the model has changed.
     */
    @Override
    public void contentsChanged(ContentChangeEvent event) {
        if (ipsSrcFile.equals(event.getIpsSrcFile())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public String getContributorId() {
        return getSite().getId();
    }

    // Eclipse API uses unchecked type
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class) {
            TabbedPropertySheetPage page = new TabbedPropertySheetPage(this);
            return page;
        }
        return super.getAdapter(adapter);
    }

    /**
     * Refreshes the controls on the active page with the data from the model.<br>
     * Calls to this refresh method are ignored if the activate attribute is set to
     * <code>false</code>.
     */
    private void refresh() {
        // ipsSrcFile can be null if the editor is opend on a ips source file that is not in a ips
        // package
        if (ipsSrcFile == null || !ipsSrcFile.exists()) {
            return;
        }
        try {
            if (!ipsSrcFile.isContentParsable()) {
                return;
            }
            /*
             * here we have to request the ips object once, to make sure that it's state is is
             * synchronized with the enclosing resource. otherwise if some part of the ui keeps a
             * reference to the ips object, it won't contain the correct state.
             */
            ipsSrcFile.getIpsObject();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        if (viewer != null) {
            viewer.getContents().refresh();
        }
    }

    private void handleEditorActivation() {
        checkForChangesMadeOutsideEclipse();
        refresh();
    }

    private void checkForChangesMadeOutsideEclipse() {
        if (dontLoadChanges || isCheckingForChangesMadeOutsideEclipse) {
            return;
        }
        try {
            isCheckingForChangesMadeOutsideEclipse = true;
            if (ipsSrcFile.isMutable() && !ipsSrcFile.getEnclosingResource().isSynchronized(0)) {
                MessageDialog dlg = new MessageDialog(Display.getCurrent().getActiveShell(),
                        Messages.IpsObjectEditor_fileHasChangesOnDiskTitle, (Image)null,
                        Messages.IpsObjectEditor_fileHasChangesOnDiskMessage, MessageDialog.QUESTION, new String[] {
                                Messages.IpsObjectEditor_fileHasChangesOnDiskYesButton,
                                Messages.IpsObjectEditor_fileHasChangesOnDiskNoButton }, 0);
                dlg.open();
                if (dlg.getReturnCode() == 0) {
                    try {
                        ipsSrcFile.getEnclosingResource().refreshLocal(0, null);
                        refresh();
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    dontLoadChanges = true;
                }

            }
        } finally {
            isCheckingForChangesMadeOutsideEclipse = false;
        }
    }

    /**
     * Internal part and shell activation listener.
     * <p>
     * Copied from AbstractTextEditor.
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
            if (part != BusinessFunctionEditor.this) {
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
            if (part != BusinessFunctionEditor.this) {
                return;
            }
            ipsSrcFile.discardChanges();
            removeListeners();
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
            // Nothing to do
        }

        private void removeListeners() {
            IpsPlugin.getDefault().getIpsModel().removeChangeListener(BusinessFunctionEditor.this);
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

}
