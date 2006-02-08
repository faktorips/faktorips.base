package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;



import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.actions.FindReferenceAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsCutAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowAttributesAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.views.DefaultDoubleclickListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;
import org.faktorips.devtools.core.ui.views.ProductCmptDragListener;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.DummyRoot;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 *
 */
public class ProductExplorer extends ViewPart implements IShowInTarget, ISelectionProvider, ContentsChangeListener {

    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productDefinitionExplorer"; //$NON-NLS-1$
    private TreeViewer tree;
    
	public ProductExplorer() {
		super();
        
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
	}

    /**
     * Overridden.
     */
	public void createPartControl(Composite parent) {
		tree = new TreeViewer(parent);
		tree.setContentProvider(new ProductContentProvider());
        
        ProductLabelProvider labelProvider = new ProductLabelProvider();
		tree.setLabelProvider(new DecoratingLabelProvider(labelProvider, new IpsProblemsLabelDecorator()));

        tree.setInput(IpsPlugin.getDefault().getIpsModel());
        tree.addDoubleClickListener(new DefaultDoubleclickListener(tree));
        tree.addDragSupport(DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new ProductCmptDragListener(tree));

        
        IWorkbenchPartSite site = getSite();
        site.setSelectionProvider(this);
        
        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), new IpsCutAction(this, this.getSite().getShell()));
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new IpsCopyAction(this, this.getSite().getShell()));
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), new IpsPasteAction(this, this.getSite().getShell()));
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), new IpsDeleteAction(this));
        
        menumanager.add(new OpenEditorAction(tree));
        menumanager.add(new Separator());
        menumanager.add(ActionFactory.CUT.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(ActionFactory.COPY.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(ActionFactory.PASTE.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(ActionFactory.DELETE.create(this.getSite().getWorkbenchWindow()));
        menumanager.add(new Separator());
        menumanager.add(new ShowStructureAction());
        menumanager.add(new FindReferenceAction());
        menumanager.add(new ShowAttributesAction());
        menumanager.add(new Separator());
        
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
        menumanager.add(new Separator());

        Menu menu = menumanager.createContextMenu(tree.getControl());

        tree.getControl().setMenu(menu);

        site.registerContextMenu(menumanager, site.getSelectionProvider());
    
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
        else if (node instanceof DummyRoot) {
            tree.setSelection(new StructuredSelection(((DummyRoot)node).data), true);
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

    public void contentsChanged(ContentChangeEvent event) {
        tree.refresh();
    }
}
