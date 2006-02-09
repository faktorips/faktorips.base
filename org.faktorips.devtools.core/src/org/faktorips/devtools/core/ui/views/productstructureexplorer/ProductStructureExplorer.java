package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.actions.FindReferenceAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowAttributesAction;
import org.faktorips.devtools.core.ui.views.DefaultDoubleclickListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;
import org.faktorips.devtools.core.ui.views.ProductCmptDragListener;
import org.faktorips.devtools.core.ui.views.ProductCmptDropListener;

/**
 * Navigate all Products defined in the active Project.
 * 
 * @author guenther
 *
 */
public class ProductStructureExplorer extends ViewPart implements ContentsChangeListener, IShowInSource {

    private TreeViewer tree; 
    
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }
    
    /**
     * Overridden
     */
	public void createPartControl(Composite parent) {
		tree = new TreeViewer(parent);
		tree.setContentProvider(new ProductStructureContentProvider(false));

        ProductStructureLabelProvider labelProvider = new ProductStructureLabelProvider();
        tree.setLabelProvider(new DecoratingLabelProvider(labelProvider, new IpsProblemsLabelDecorator()));
        
        tree.addDoubleClickListener(new DefaultDoubleclickListener(tree));
        tree.expandAll();
        tree.addDragSupport(DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new ProductCmptDragListener(tree));
        tree.addDropSupport(DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new ProductCmptDropListener(tree));

        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);
        menumanager.add(new OpenEditorAction(tree));
        menumanager.add(new FindReferenceAction());
        menumanager.add(new ShowAttributesAction());
        
        Menu menu = menumanager.createContextMenu(tree.getControl());
        tree.getControl().setMenu(menu);
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IpsResourceChangeListener(tree), IResourceChangeEvent.POST_CHANGE);
    }

    /**
     * Overridden
     */
	public void setFocus() {
        //nothing to do.
	}

    /**
     * Displays the structure of the given file. 
     * 
     * @param selectedItems The selection to display
     * @throws CoreException 
     */
    public void showStructure(IIpsSrcFile file) throws CoreException {
        tree.setInput(file.getIpsObject());
        tree.expandAll();
    }

    public void contentsChanged(ContentChangeEvent event) {
        if (tree.getContentProvider() != null) {
            Object currentData = tree.getInput();
            tree.setInput(null);
            tree.setInput(currentData);
            tree.expandAll();
        }
    }

    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(null, tree.getSelection());
        return context;
    }

}
