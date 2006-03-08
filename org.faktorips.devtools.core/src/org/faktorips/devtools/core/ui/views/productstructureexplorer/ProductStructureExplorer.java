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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
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
    private IIpsSrcFile file;
    private ProductStructureContentProvider contentProvider;
    
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site) throws PartInitException {
    	super.init(site);
    	
    	site.getActionBars().getToolBarManager().add(new Action("", Action.AS_CHECK_BOX) {

    		public ImageDescriptor getImageDescriptor() {
    			return IpsPlugin.getDefault().getImageDescriptor("ShowRelationTypeNodes.gif");
    		}
    		
			public void run() {
				contentProvider.setRelationTypeShowing(!contentProvider.isRelationTypeShowing());
				tree.refresh();
		        tree.expandAll();
			}
		});
    }
    
    /**
     * Overridden
     */
	public void createPartControl(Composite parent) {
		tree = new TreeViewer(parent);
		contentProvider = new ProductStructureContentProvider(false);
		tree.setContentProvider(contentProvider);

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
     * Displays the structure of the product component defined by the given file. 
     * 
     * @param selectedItems The selection to display
     * @throws CoreException 
     */
    public void showStructure(IIpsSrcFile file) throws CoreException {
    	showStructure((IProductCmpt)file.getIpsObject());
    }

    /**
     * Displays the structure of the given product component.
     */
    public void showStructure(IProductCmpt product) {
    	this.file = product.getIpsSrcFile();
        tree.setInput(product.getStructure());
        tree.expandAll();
    }
    
    public void contentsChanged(ContentChangeEvent event) {
    	if (file == null) {
    		// no contents set - nothing to refresh.
    		return;
    	}
    	
    	Object input = tree.getInput();
    	if (input instanceof IProductCmptStructure) {
    		((IProductCmptStructure)input).refresh();
    	}
    	
    	tree.refresh();
    	if (event.getPdSrcFile().equals(file)) {
    		tree.refresh();
    		System.err.println("refreshed tree2"); //$NON-NLS-1$
    	}
    }

    public ShowInContext getShowInContext() {
        ShowInContext context = new ShowInContext(null, tree.getSelection());
        return context;
    }

}
