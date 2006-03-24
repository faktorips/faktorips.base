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
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
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
    private Label errormsg;
    
    public static String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.productStructureExplorer"; //$NON-NLS-1$

    public ProductStructureExplorer() {
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void init(IViewSite site) throws PartInitException {
    	super.init(site);
    	
    	site.getActionBars().getToolBarManager().add(new Action() { //$NON-NLS-1$

    		public ImageDescriptor getImageDescriptor() {
    			return IpsPlugin.getDefault().getImageDescriptor("Refresh.gif"); //$NON-NLS-1$
    		}
    		
			public void run() {
				tree.refresh();
		        tree.expandAll();
			}

			public String getToolTipText() {
				return "Refresh contents";
			}
		});

    	site.getActionBars().getToolBarManager().add(new Action("", Action.AS_CHECK_BOX) { //$NON-NLS-1$

    		public ImageDescriptor getImageDescriptor() {
    			return IpsPlugin.getDefault().getImageDescriptor("ShowRelationTypeNodes.gif"); //$NON-NLS-1$
    		}
    		
			public void run() {
				contentProvider.setRelationTypeShowing(!contentProvider.isRelationTypeShowing());
				tree.refresh();
		        tree.expandAll();
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
    }
    
    /**
     * Overridden
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
        
        tree.addDoubleClickListener(new DefaultDoubleclickListener(tree));
        tree.expandAll();
        tree.addDragSupport(DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new ProductCmptDragListener(tree));
        tree.addDropSupport(DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new ProductCmptDropListener(tree));

        MenuManager menumanager = new MenuManager();
        menumanager.setRemoveAllWhenShown(false);
        menumanager.add(new OpenEditorAction(tree));
        menumanager.add(new FindReferenceAction(tree));
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
        try {
        	errormsg.setVisible(false);
    		((GridData)errormsg.getLayoutData()).exclude = true;
        	
    		tree.getTree().setVisible(true);
    		((GridData)tree.getTree().getLayoutData()).exclude = false;
    		tree.getTree().getParent().layout();
			tree.setInput(product.getStructure());
		} catch (CycleException e) {
			handleCircle(e);
		}
        tree.expandAll();
    }
    
    public void contentsChanged(ContentChangeEvent event) {
    	if (file == null || !event.getIpsSrcFile().equals(file)) {
    		// no contents set - nothing to refresh.
    		return;
    	}
    	
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
   		tree.refresh();
    }

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

}
