/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;



import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.ui.UpdateUiJob;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowAttributesAction;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 *
 */
public class ProductStructureExplorer extends ViewPart implements ContentsChangeListener, IShowInSource, IResourceChangeListener {
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    private TreeViewer tree; 
    private IIpsSrcFile file;
    private ProductStructureContentProvider contentProvider;
    private Label errormsg;
    
    // Job to refresh the ui in asynchronous manner
    private UpdateUiJob updateUiJob;
    
    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
        
        // add as resource listener because refactoring-actions like move or rename
        // does not cause a model-changed-event.
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }
    
    private Display getDisplay(){
        return getViewSite().getShell().getDisplay();
    }
    
    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site) throws PartInitException {
    	super.init(site);

        Action refreshAction= new Action() { //$NON-NLS-1$

    		public ImageDescriptor getImageDescriptor() {
    			return IpsPlugin.getDefault().getImageDescriptor("Refresh.gif"); //$NON-NLS-1$
    		}
    		
			public void run() {
                updateUiJob.update(this);
		        tree.expandAll();
			}

			public String getToolTipText() {
				return Messages.ProductStructureExplorer_tooltipRefreshContents;
			}
		};
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        IWorkbenchAction retargetAction = ActionFactory.REFRESH.create(getViewSite().getWorkbenchWindow());
        retargetAction.setImageDescriptor(refreshAction.getImageDescriptor());
        retargetAction.setToolTipText(refreshAction.getToolTipText());
        getViewSite().getActionBars().getToolBarManager().add(retargetAction);

    	site.getActionBars().getToolBarManager().add(new Action("", Action.AS_CHECK_BOX) { //$NON-NLS-1$

    		public ImageDescriptor getImageDescriptor() {
    			return IpsPlugin.getDefault().getImageDescriptor("ShowRelationTypeNodes.gif"); //$NON-NLS-1$
    		}
    		
			public void run() {
				contentProvider.setRelationTypeShowing(!contentProvider.isRelationTypeShowing());
                updateUiJob.update(this);
			}

			public String getToolTipText() {
				return Messages.ProductStructureExplorer_tooltipToggleRelationTypeNodes;
			}
		});
    	
    	site.getActionBars().getToolBarManager().add(new Action() {
		
			public void run() {
				tree.setInput(null);
				tree.refresh();
			}
		
			public ImageDescriptor getImageDescriptor() {
    			return IpsPlugin.getDefault().getImageDescriptor("Clear.gif"); //$NON-NLS-1$
			}

			public String getToolTipText() {
				return Messages.ProductStructureExplorer_tooltipClear;
			}
		
		});
        
        // add asynchronous refreh ui job
        Runnable refreshCommand = new Runnable() {
            public void run() {
                if (getDisplay().isDisposed())
                    return;
              // refresh the whole content of the tree
              refresh();
            }
        };
        updateUiJob = new UpdateUiJob(getDisplay(), refreshCommand);        
    }
    
    /**
     * {@inheritDoc}
     */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		errormsg = new Label(parent, SWT.WRAP);
		GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
		layoutData.exclude = true;
		errormsg.setLayoutData(layoutData);
		errormsg.setVisible(false);

		tree = new TreeViewer(parent);
		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		contentProvider = new ProductStructureContentProvider(false);
		tree.setContentProvider(contentProvider);

        ProductStructureLabelProvider labelProvider = new ProductStructureLabelProvider();
        tree.setLabelProvider(new DecoratingLabelProvider(labelProvider, new IpsProblemsLabelDecorator()));
        
        tree.addDoubleClickListener(new TreeViewerDoubleclickListener(tree));
        tree.expandAll();
        tree.addDragSupport(DND.DROP_LINK, new Transfer[] {FileTransfer.getInstance()}, new IpsElementDragListener(tree));
        tree.addDropSupport(DND.DROP_LINK, new Transfer[] {FileTransfer.getInstance()}, new ProductCmptDropListener());

        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);
        menumanager.add(new OpenEditorAction(tree));
        menumanager.add(new FindProductReferencesAction(tree));
        menumanager.add(new ShowAttributesAction(tree)); 
        
        Menu menu = menumanager.createContextMenu(tree.getControl());
        tree.getControl().setMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
	public void setFocus() {
        //nothing to do.
	}

    /**
     * Displays the structure of the product component defined by the given file. 
     * 
     * @param selectedItems The selection to display
     * @throws CoreException 
     */
    public void showStructure(IIpsSrcFile file) throws CoreException {
    	if(file!=null && file.getIpsObjectType()==IpsObjectType.PRODUCT_CMPT){
    		showStructure((IProductCmpt) file.getIpsObject());
    	}
    }

    /**
     * Displays the structure of the given product component.
     */
    public void showStructure(IProductCmpt product) {
    	if (product == null) {
    		return;
    	}
    	this.file = product.getIpsSrcFile();
        try {
        	errormsg.setVisible(false);
    		((GridData)errormsg.getLayoutData()).exclude = true;
        	
    		tree.getTree().setVisible(true);
    		((GridData)tree.getTree().getLayoutData()).exclude = false;
    		tree.getTree().getParent().layout();
			tree.setInput(product.getStructure());
            tree.expandAll();
		} catch (CycleException e) {
			handleCircle(e);
		}
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
    	if (file == null || !event.getIpsSrcFile().equals(file)) {
    		// no contents set or event concerncs another source file - nothing to refresh.
    		return;
    	}
        updateUiJob.update(this);
    }

    private void refresh() {
        Control ctrl = tree.getControl();
        
        if (ctrl == null || ctrl.isDisposed()) {
        	return;
        }
        
        try {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (!tree.getControl().isDisposed()) {
                        Object input = tree.getInput();
                        if (input instanceof IProductCmptStructure) {
                            try {
                                ((IProductCmptStructure)input).refresh();
                            } catch (CycleException e) {
                                handleCircle(e);
                                return;
                            }
                        }

                        errormsg.setVisible(false);
                        ((GridData)errormsg.getLayoutData()).exclude = true;

                        tree.getTree().setVisible(true);
                        ((GridData)tree.getTree().getLayoutData()).exclude = false;
                        tree.getTree().getParent().layout();
                        tree.setInput(input);
                        tree.expandAll();
                    }
                }
            };
            ctrl.setRedraw(false);
            ctrl.getDisplay().syncExec(runnable);
        } finally {
            ctrl.setRedraw(true);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(null, tree.getSelection());
        return context;
    }
    
    private void handleCircle(CycleException e) {
		IpsPlugin.log(e);
		tree.getTree().setVisible(false);
		((GridData)tree.getTree().getLayoutData()).exclude = true;
		String msg = Messages.ProductStructureExplorer_labelCircleRelation;
		IIpsElement[] cyclePath = e.getCyclePath();
		StringBuffer path = new StringBuffer();
		for (int i = cyclePath.length-1; i >= 0; i--) {
			path.append(cyclePath[i].getName());
			if (i%2 != 0) {
				path.append(" -> "); //$NON-NLS-1$
			}
			else if (i%2 == 0 && i > 0) {
				path.append(":"); //$NON-NLS-1$
			}
		}

		
		errormsg.setText(msg + path);
		
		errormsg.setVisible(true);
		((GridData)errormsg.getLayoutData()).exclude = false;
		errormsg.getParent().layout();
    }

    /**
     * {@inheritDoc}
     */
	public void resourceChanged(IResourceChangeEvent event) {
        if (file == null) {
            return;
        }
        updateUiJob.update(this);
    }
	
	private class ProductCmptDropListener extends IpsElementDropListener {

	    public void dragEnter(DropTargetEvent event) {
	        event.detail = DND.DROP_LINK;
	    }

	    public void drop(DropTargetEvent event) {
	    	IIpsElement[] transferred = super.getTransferedElements(event.currentDataType);
	    	if (transferred.length > 0 && transferred[0] instanceof IIpsSrcFile) {
	    		try {
	    			showStructure((IIpsSrcFile)transferred[0]);
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
	    	}
	    }

	    public void dropAccept(DropTargetEvent event) {
	        event.detail = DND.DROP_LINK;
	    }
	}

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }
}
