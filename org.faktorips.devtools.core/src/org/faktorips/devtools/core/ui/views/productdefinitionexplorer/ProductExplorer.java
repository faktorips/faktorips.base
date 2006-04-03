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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;



import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.refactor.MoveOperation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.actions.FindReferenceAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsCutAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.MoveAction;
import org.faktorips.devtools.core.ui.actions.NewFolderAction;
import org.faktorips.devtools.core.ui.actions.NewProductComponentAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.RenameAction;
import org.faktorips.devtools.core.ui.actions.ShowAttributesAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.actions.WrapperAction;
import org.faktorips.devtools.core.ui.views.DefaultDoubleclickListener;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsElementDropListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 *
 */
public class ProductExplorer extends ViewPart implements IShowInTarget, ISelectionProvider{

    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productDefinitionExplorer"; //$NON-NLS-1$
    private TreeViewer tree;
        
    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site) throws PartInitException {
    	super.init(site);
    	
    	site.getActionBars().getToolBarManager().add(new PERefreshAction(site.getShell()));
    }

    /**
     * Overridden.
     */
	public void createPartControl(Composite parent) {
		tree = new TreeViewer(parent);
		tree.setContentProvider(new ProductContentProvider());
        
        ProductLabelProvider labelProvider = new ProductLabelProvider();
       
        DecoratingLabelProvider decoratingLabelProvider = new DecoratingLabelProvider(labelProvider, IpsPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator());
        decoratingLabelProvider = new DecoratingLabelProvider(decoratingLabelProvider, new IpsProblemsLabelDecorator());
		tree.setLabelProvider(decoratingLabelProvider);

        tree.setInput(IpsPlugin.getDefault().getIpsModel());
        tree.addDoubleClickListener(new DefaultDoubleclickListener(tree));
        tree.addDragSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance()}, new IpsElementDragListener(tree));
        tree.addDropSupport(DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance()}, new DropListener());
        tree.setSorter(new Sorter());
        
        IWorkbenchPartSite site = getSite();
        site.setSelectionProvider(this);

        // Create the menu completely manually because we dont wont any other actions 
        // provided by other plugins put in here...
        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), new IpsCutAction(this, this.getSite().getShell()));
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new IpsCopyAction(this, this.getSite().getShell()));
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), new IpsPasteAction(this, this.getSite().getShell()));
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), new IpsDeleteAction(this));
        
        menumanager.add(new OpenEditorAction(tree));
        menumanager.add(new IpsDeepCopyAction(this.getSite().getShell(), tree, DeepCopyWizard.TYPE_COPY_PRODUCT));
        menumanager.add(new IpsDeepCopyAction(this.getSite().getShell(), tree, DeepCopyWizard.TYPE_NEW_VERSION));
        
        MenuManager subMm = new MenuManager(Messages.ProductExplorer_submenuNew);
        subMm.add(new NewFolderAction(this.getSite().getShell(), tree));
        subMm.add(new NewProductComponentAction(this.getSite().getWorkbenchWindow()));
        menumanager.add(subMm);


        menumanager.add(new Separator());
        menumanager.add(ActionFactory.CUT.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(ActionFactory.COPY.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(ActionFactory.PASTE.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(ActionFactory.DELETE.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(new Separator());
        menumanager.add(new ShowStructureAction());
        menumanager.add(new FindReferenceAction(tree));
        menumanager.add(new ShowAttributesAction());
        menumanager.add(new Separator());

        
        subMm = new MenuManager(Messages.ProductExplorer_submenuRefactor);
        subMm.add(new RenameAction(this.getSite().getShell(), tree));
        subMm.add(new MoveAction(this.getSite().getShell(), tree));
        menumanager.add(subMm);

        subMm = new MenuManager(Messages.ProductExplorer_submenuTeam);
        subMm.setRemoveAllWhenShown(false);
        
        subMm.add(new WrapperAction(tree, Messages.ProductExplorer_actionCommit, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.commit")); //$NON-NLS-2$ //$NON-NLS-1$
        subMm.add(new WrapperAction(tree, Messages.ProductExplorer_actionUpdate, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.update")); //$NON-NLS-2$ //$NON-NLS-1$
        subMm.add(new WrapperAction(tree, Messages.ProductExplorer_actionReplace, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.replace")); //$NON-NLS-2$ //$NON-NLS-1$
        subMm.add(new WrapperAction(tree, Messages.ProductExplorer_actionAdd, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.add")); //$NON-NLS-2$ //$NON-NLS-1$
        subMm.add(new WrapperAction(tree, Messages.ProductExplorer_actionShowHistory, "org.eclipse.team.cvs.ui.CVSActionSet", "org.eclipse.team.cvs.ui.showHistory"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        
        menumanager.add(subMm);
        
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
        menumanager.add(new Separator());

        Menu menu = menumanager.createContextMenu(tree.getControl());
        tree.getControl().setMenu(menu);

        // Dont register this context menu to avoid menu-items contributed by other plugins.
        // site.registerContextMenu(menumanager, site.getSelectionProvider());
    
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IpsResourceChangeListener(tree), IResourceChangeEvent.POST_CHANGE);

    }

	public void setFocus() {
        //nothing to do.
	}

    /**
     * Returns the product component for the first item in the selection if possible, 
     * <code>null</code> otherwise.
     */
    public IProductCmpt getSelectedProductCmpt() {
        IStructuredSelection selection = (IStructuredSelection)tree.getSelection();
        if (selection != null) {
            Object selected = selection.getFirstElement();
            
            if (selected != null && selected instanceof IProductCmpt) {
                return (IProductCmpt)selected;
            }
        }
        return null;
    }

    /**
     * Returns the srcfile for the first item in the selection if possible, <code>null</code> otherwise.
     */
    public IIpsSrcFile getIpsSrcFileForSelection() {
        IStructuredSelection selection = (IStructuredSelection)tree.getSelection();
        if (selection != null) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IIpsElement) {
                IIpsElement e = (IIpsElement)selected;
                for(; e != null && !(e instanceof IIpsSrcFile); e = e.getParent());
                return e==null?null:(IIpsSrcFile)e;
            }
        }
        return null;
    }
 
    /**
     * Returns all selected IProductCmptGenerations and, in addition, all generations of selected
     * product components. These generations are found using the date set in preferences.
     */
    public IProductCmptGeneration[] getAllSelectedProductCmptGenerations() {
        IProductCmptGeneration[] result = new IProductCmptGeneration[0];
        ArrayList resultList = new ArrayList();
        
        IStructuredSelection selection = (IStructuredSelection)this.getSelection();
        
        for (Iterator i = selection.iterator(); i.hasNext();) {
            Object selected = i.next();
            if (selected instanceof IProductCmptGeneration) {
                resultList.add(selected);
            }
            else if (selected instanceof IProductCmpt) {
                resultList.add(((IProductCmpt)selected).findGenerationEffectiveOn(IpsPreferences.getWorkingDate()));
            }
        }
        
        if (!resultList.isEmpty()) {
            result = new IProductCmptGeneration[resultList.size()];
            resultList.toArray(result);
        }
        
        return result;
    }
    
    public boolean show(ShowInContext context) {
        ISelection selection = context.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection= ((IStructuredSelection) selection);
            if (structuredSelection.size() >= 1) {
                return reveal(structuredSelection.getFirstElement());
            }
        }
        
        Object input = context.getInput();
        if (input instanceof IProductCmpt) {
            return reveal(context.getInput());
        }
        else if (input instanceof IFileEditorInput) {
           IFile file = ((IFileEditorInput)input).getFile();
           return reveal(file);
        }

        
        
        return false;
    }
    
    private boolean reveal(Object toReveal) {
        Object node;
        if (toReveal instanceof Object[]) {
            node = ((Object[])toReveal)[0];
        }
        else {
            node = toReveal;
        }
        
        if (node instanceof IProductCmptGeneration) {
            tree.setSelection(new StructuredSelection(node), true);
            return true;
        }
        else if (node instanceof IProductCmpt) {
            tree.setSelection(new StructuredSelection(node), true);
            return true;
        }
        else if (node instanceof IFile) {
            try {
                IIpsSrcFile file = (IIpsSrcFile)IpsPlugin.getDefault().getManager().getModel().getIpsElement((IFile)node);
                IIpsObject obj = file.getIpsObject();
                tree.setSelection(new StructuredSelection(obj), true);
                return true;
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return false;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        tree.addSelectionChangedListener(listener);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        tree.removeSelectionChangedListener(listener);
    }

    public void setSelection(ISelection selection) {
        tree.setSelection(selection);
    }

    public ISelection getSelection() {
        return tree.getSelection();
    }
    
    private class DropListener extends IpsElementDropListener {

		/**
		 * {@inheritDoc}
		 */
		public void dragEnter(DropTargetEvent event) {
			if (event.detail == DND.DROP_NONE) {
				event.detail = DND.DROP_MOVE;
			}
			event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_SCROLL;
		}

		/**
		 * {@inheritDoc}
		 */
		public void drop(DropTargetEvent event) {
			if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
				return;
			}
			try {
				IIpsPackageFragment target = getTarget(event);
				if (target == null) {
					return;
				}
				
				IIpsElement[] sources = super.getTransferedElements(event.currentDataType);
				MoveOperation moveOp = new MoveOperation(sources, target);
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(ProductExplorer.this.getSite().getShell());
				dialog.run(false, false, moveOp);
			} catch (CoreException e) {
				IStatus status = e.getStatus();
				if (status instanceof IpsStatus) {
					MessageDialog.openError(ProductExplorer.this.getSite().getShell(), Messages.ProductExplorer_title, ((IpsStatus)status).getMessage());
				}
				else {
					IpsPlugin.log(e);
				}
			} catch (InvocationTargetException e) {
				IpsPlugin.log(e);
			} catch (InterruptedException e) {
				IpsPlugin.log(e);
			}
			
		}

		private IIpsPackageFragment getTarget(DropTargetEvent event) throws CoreException {
			Object dropTarget = event.item.getData();
			IIpsPackageFragment target = null; 
			if (dropTarget instanceof IIpsPackageFragment) {
				target = (IIpsPackageFragment)dropTarget;
			}
			else if (dropTarget instanceof IIpsProject) {
				target = ((IIpsProject)dropTarget).getIpsPackageFragmentRoots()[0].getIpsDefaultPackageFragment();
			}
			return target;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void dropAccept(DropTargetEvent event) {
			// nothing to do
		}
    	
    }
    
    private class PERefreshAction extends RefreshAction {

		/**
		 * @param shell
		 */
		public PERefreshAction(Shell shell) {
			super(shell);
		}
    	
		public void run() {
			super.run();
			tree.refresh();
		}

		public ImageDescriptor getImageDescriptor() {
			return IpsPlugin.getDefault().getImageDescriptor("Refresh.gif"); //$NON-NLS-1$
		}
    }
    
}
