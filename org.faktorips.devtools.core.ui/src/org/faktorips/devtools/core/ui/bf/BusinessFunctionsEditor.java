package org.faktorips.devtools.core.ui.bf;


import java.util.EventObject;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.faktorips.devtools.bf.ui.edit.BusinessFunctionEditPartFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

public class BusinessFunctionsEditor extends GraphicalEditorWithFlyoutPalette implements ContentsChangeListener,
        ITabbedPropertySheetPageContributor {

    private IIpsSrcFile ipsSrcFile;
    private IBusinessFunction businessFunction;
    private PaletteRoot paletteRoot;

    public BusinessFunctionsEditor() {
    }

    @Override
    protected PaletteRoot getPaletteRoot() {
        return paletteRoot;
    }

    /*
     * Overriden to activate correct dirty behaviour of the editor. (e.g. show the star in the tab,
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
            public void run() throws Exception {
                ipsSrcFile.save(true, monitor);
                getCommandStack().markSaveLocation();
            }
        });
    }

    public IBusinessFunction getBusinessFunction() {
        return businessFunction;
    }

    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        viewer.setRootEditPart(root);
        ConnectionLayer connectionLayer = (ConnectionLayer)root.getLayer(LayerConstants.CONNECTION_LAYER);
        connectionLayer.setConnectionRouter(new BendpointConnectionRouter());
        viewer.setEditPartFactory(new BusinessFunctionEditPartFactory());
    }

    protected void initializeGraphicalViewer() {
        super.initializeGraphicalViewer();
        getGraphicalViewer().setContents(getBusinessFunction());
    }

    // TODO part of this code is duplicate in IpsObjectEditor
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
        super.init(site, input);
    }

    public boolean isDirty() {
        if (super.isDirty()) {
            return true;
        }
        return ipsSrcFile.isDirty();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Unregisters this editor as model change listener.
     */
    public void dispose() {
        super.dispose();
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Registers this editor as model change listener.
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (ipsSrcFile.equals(event.getIpsSrcFile())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public String getContributorId() {
        return getSite().getId();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class) {
            TabbedPropertySheetPage page = new TabbedPropertySheetPage(this);
            return page;
        }
        return super.getAdapter(adapter);
    }
}
