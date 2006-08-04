package org.faktorips.devtools.core.ui.views.modelexplorer;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.actions.RefreshAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.Attribute;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.ui.actions.FindPolicyReferencesAction;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeepCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAndSaveAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.IpsTestAction;
import org.faktorips.devtools.core.ui.actions.MoveAction;
import org.faktorips.devtools.core.ui.actions.NewFolderAction;
import org.faktorips.devtools.core.ui.actions.NewPolicyComponentTypeAction;
import org.faktorips.devtools.core.ui.actions.NewProductComponentAction;
import org.faktorips.devtools.core.ui.actions.NewTableContentAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.RenameAction;
import org.faktorips.devtools.core.ui.actions.ShowAttributesAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;
import org.faktorips.devtools.core.ui.wizards.deepcopy.DeepCopyWizard;

/**
 * The ModelExplorer is a ViewPart for displaying <code>ProductComponent</code>s,
 * <code>TableContents</code>, <code>TableStructure</code>s and <code>PolicyCmptType</code>s
 * along with their Attributes. The view uses a TreeViewer to represent the hierarchical 
 * datastructure. It can be configured to show the tree of PackageFragments in a hierarchical
 * (default) or a flat layout style.
 * 
 * @author Stefan Widmaier
 */

public class ModelExplorer extends ViewPart implements IShowInTarget{
	private static final int HIERARCHICAL_LAYOUT = 0;
	
	private static final int FLAT_LAYOUT = 1;
	
	/**
	 * Used for saving the current layout style in a eclipse memento.
	 */
	private static final String LAYOUT_MEMENTO = "layout"; //$NON-NLS-1$

	/**
	 * Used for saving the current layout style in a eclipse memento.
	 */
	private static final String LAYOUT_STYLE_KEY = "style"; //$NON-NLS-1$
	
	/**
	 * The TreeViewer displaying the object model.
	 */
	private TreeViewer treeViewer;
	
	/**
	 * Decorator for problems in IpsObjects. This decorator is adjusted 
	 * according to the current layout style.
	 */
	private IpsProblemsLabelDecorator ipsDecorator= new IpsProblemsLabelDecorator();
	
	/**
	 * Content provider for the tree viewer. 
	 */
	private ModelContentProvider contentProvider;

	/**
	 * Label provider for the tree viewer. 
	 */
	private ModelLabelProvider labelProvider = new ModelLabelProvider();
	
	/**
	 * Filter used in flat layout, where it filters out empty packageFragments.
	 */
	private ViewerFilter emptyPackageFilter = new EmptyPackageFilter();
	private ViewerFilter typeFilter;
	
	private IpsResourceChangeListener resourceListener;
	protected ModelExplorerConfiguration config;	
	/**
	 * Flag that indicates whether the current layout style is flat (true) or hierarchical (false).
	 */
	private boolean isFlatLayout = false;
	
	public ModelExplorer() {
		super();
		config= createConfig();
	}
	protected ModelExplorerConfiguration createConfig() {
		return new ModelExplorerConfiguration(new Class[] { PolicyCmptType.class
				, TableStructure.class, ProductCmpt.class
				, TableContents.class, Attribute.class
				, Relation.class, TestCase.class, TestCaseType.class}
		, new Class[0], ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS);
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		IWorkbenchAction action = ActionFactory.REFRESH.create(site.getWorkbenchWindow());
		site.getActionBars().setGlobalActionHandler(
				ActionFactory.REFRESH.getId(),
				new ModelExplorerRefreshAction(site));
		action.setImageDescriptor(IpsPlugin.getDefault().getImageDescriptor(
				"Refresh.gif")); //$NON-NLS-1$
		site.getActionBars().getToolBarManager().add(action);
	}

	public void createPartControl(Composite parent) {
		contentProvider = new ModelContentProvider(config, isFlatLayout);
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setSorter(new ModelSorter());
		treeViewer.setInput(IpsPlugin.getDefault().getIpsModel());
		treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));
		treeViewer.addDragSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance()}, new IpsElementDragListener(treeViewer));
		treeViewer.addDropSupport(DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance()}, new ModelExplorerDropListener());
		
		DecoratingLabelProvider decoProvider = new DecoratingLabelProvider(
				labelProvider, IpsPlugin.getDefault().getWorkbench()
						.getDecoratorManager().getLabelDecorator());
		decoProvider = new DecoratingLabelProvider(decoProvider, ipsDecorator); 
		treeViewer.setLabelProvider(decoProvider);
		
		typeFilter= new ModelExplorerFilter(config);
		treeViewer.addFilter(typeFilter);

		getSite().setSelectionProvider(treeViewer);
		resourceListener= new IpsResourceChangeListener(treeViewer);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceListener, IResourceChangeEvent.POST_CHANGE);
		/*
		 * Use the current value of isFlatLayout, which is set while loading the
		 * memento/viewState before this method is called
		 */
		setFlatLayout(isFlatLayout);
		
		createMenu();
		createContextMenu();
	}

	/**
	 * Create menu for layout styles.
	 */
	private void createMenu() {
		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
		IMenuManager layoutMenue = new MenuManager(
				Messages.ModelExplorer_submenuLayout);
		layoutMenue.add(new Action(Messages.ModelExplorer_actionFlatLayout) {
			public void run() {
				setFlatLayout(true);
			}
		});
		layoutMenue.add(new Action(
				Messages.ModelExplorer_actionHierarchicalLayout) {
			public void run() {
				setFlatLayout(false);
			}
		});
		mgr.add(layoutMenue);
	}

	private void createContextMenu() {
//		getViewSite().getActionBars().setGlobalActionHandler(
//				ActionFactory.CUT.getId(),
//				new IpsCutAction(treeViewer, this.getSite().getShell()));
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.COPY.getId()
				,	new IpsCopyAction(treeViewer, this.getSite().getShell()));
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.PASTE.getId(),
				new IpsPasteAction(treeViewer, this.getSite().getShell()));
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.DELETE.getId(), new IpsDeleteAndSaveAction(treeViewer));

        getViewSite().getActionBars().setGlobalActionHandler(
        		ActionFactory.RENAME.getId(), 
        		new RenameAction(this.getSite().getShell(), treeViewer));
        
        
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);	
		manager.addMenuListener(new MenuBuilder());

		Menu contextMenu = manager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(contextMenu);
		getSite().registerContextMenu(manager, treeViewer);
		
	}

	public void setFocus() {
	}

	/**
	 * Answers whether this part shows the packagFragments flat or hierarchical.
	 */
	private boolean isFlatLayout() {
		return isFlatLayout;
	}

	/**
	 * Sets the layout style to flat respectively hierarchical. Informs label
	 * and contentprovider, activates emptyPackageFilter for flat layout to hide
	 * empty PackageFragments.
	 * 
	 * @param b
	 */
	private void setFlatLayout(boolean b) {
		isFlatLayout = b;
		if (isFlatLayout()) {
			// remove Filter in case it was already added by pressing the same layout button twice
			// (treeviewer organizes filters in a list, not a set)
			treeViewer.removeFilter(emptyPackageFilter);
			treeViewer.addFilter(emptyPackageFilter);
		}else{
			treeViewer.removeFilter(emptyPackageFilter);
		}
		
		ipsDecorator.setFlatLayout(isFlatLayout());
		contentProvider.setIsFlatLayout(isFlatLayout());
		labelProvider.setIsFlatLayout(isFlatLayout());

		treeViewer.getControl().setRedraw(false);
		treeViewer.refresh();
		treeViewer.getControl().setRedraw(true);
	}

	/**
	 * Loads the layout style from the given Memento Object. {@inheritDoc}
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if(memento!=null){
			IMemento layout = memento.getChild(LAYOUT_MEMENTO);
			if (layout != null) {
				isFlatLayout = layout.getInteger(LAYOUT_STYLE_KEY).intValue() == FLAT_LAYOUT;
			}
		}
	}

	/**
	 * Saves the current layout style into the given memento object.
	 * {@inheritDoc}
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		IMemento layout = memento.createChild(LAYOUT_MEMENTO);
		layout.putInteger(LAYOUT_STYLE_KEY, isFlatLayout() ? FLAT_LAYOUT
				: HIERARCHICAL_LAYOUT);
	}
	
	/**
	 * Unregisters this part as resource-listener in the workspace, and disposes
	 * of it.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 * Never used...
	 */
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
            treeViewer.setSelection(new StructuredSelection(node), true);
            return true;
        }
        else if (node instanceof IProductCmpt) {
        	treeViewer.setSelection(new StructuredSelection(node), true);
            return true;
        }
        else if (node instanceof IFile) {
            try {
                IIpsSrcFile file = (IIpsSrcFile)IpsPlugin.getDefault().getManager().getModel().getIpsElement((IFile)node);
                IIpsObject obj = file.getIpsObject();
                treeViewer.setSelection(new StructuredSelection(obj), true);
                return true;
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return false;
    }
	
	/**
	 * Action for the ModelExplorer specific refreshButton.
	 */
	private class ModelExplorerRefreshAction extends RefreshAction {
		ModelExplorerRefreshAction(IWorkbenchPartSite site) {
			super(site);
		}

		public void run() {
			super.run();
			treeViewer.refresh();
		}

		public ImageDescriptor getImageDescriptor() {
			return IpsPlugin.getDefault().getImageDescriptor("Refresh.gif"); //$NON-NLS-1$
		}
	}
	
	private class MenuBuilder implements IMenuListener{
		// hold references to enabled RetargetActions
		private IWorkbenchAction copy= ActionFactory.COPY.create(getSite().getWorkbenchWindow());
		private IWorkbenchAction paste= ActionFactory.PASTE.create(getSite().getWorkbenchWindow());
		private IWorkbenchAction delete= ActionFactory.DELETE.create(getSite().getWorkbenchWindow());
		
		/**
		 * Creates this parts' contextmenu in the given MenuManager dynamically. 
		 * The context menu and its elements depend on the current selection and the 
		 * <code>ModelExplorerConfiguration</code>.
		 * {@inheritDoc}
		 */
		public void menuAboutToShow(IMenuManager manager) {
			
			if(!(treeViewer.getSelection() instanceof IStructuredSelection)){
				return;
			}
			Object selected= ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
			if(selected == null){
				// don't show menu if selection is empty
				return;
			}
			
//			createAllContextMenuActions(manager, selected);
			
			createEditActions(manager, selected);
			createNewMenu(manager, selected);
			manager.add(new Separator());
			createReorgActions(manager, selected);
			manager.add(new Separator());
			createObjectInfoActions(manager, selected);
			manager.add(new Separator());
			createTestCaseAction(manager, selected);
			createRefactorMenu(manager, selected);
			manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
			
		}
		
		private void createAllContextMenuActions(IMenuManager manager, Object selected){
			if(selected instanceof IIpsElement){
				IIpsElement element= (IIpsElement)selected;
				
				if(config.isAllowedIpsElementType(element)){
					manager.add(new OpenEditorAction(treeViewer));
				}
				MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew); 
				newMenu.add(new NewFolderAction(getSite().getShell(), treeViewer));
				if(config.isAllowedIpsElementType(ProductCmpt.class)){
					newMenu.add(new NewProductComponentAction(getSite().getWorkbenchWindow()));
					newMenu.add(new NewTableContentAction(getSite().getWorkbenchWindow()));
				}
				if(config.isAllowedIpsElementType(PolicyCmptType.class)){
					newMenu.add(new NewPolicyComponentTypeAction(getSite().getWorkbenchWindow()));
				}
				if(element instanceof IProductCmpt){
					newMenu.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer, DeepCopyWizard.TYPE_NEW_VERSION));
					newMenu.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer, DeepCopyWizard.TYPE_COPY_PRODUCT));
				}
				manager.add(newMenu);
				manager.add(new Separator());
				
				manager.add(copy);
				manager.add(paste);
				manager.add(delete);
				manager.add(new Separator());
	
				if(element instanceof IProductCmpt){
					manager.add(new ShowStructureAction(treeViewer));
					manager.add(new FindProductReferencesAction(treeViewer));
				}
				if(element instanceof IPolicyCmptType){
					manager.add(new FindPolicyReferencesAction(treeViewer));
				}
				if(element instanceof IPolicyCmptType | element instanceof IProductCmpt){
					manager.add(new ShowAttributesAction(treeViewer));
				}
				manager.add(new Separator());
				
		        MenuManager subMm = new MenuManager(Messages.ModelExplorer_submenuRefactor);
		        subMm.add(new RenameAction(getSite().getShell(), treeViewer));
		        subMm.add(new MoveAction(getSite().getShell(), treeViewer));
				manager.add(subMm);
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
						+ "-end"));//$NON-NLS-1$
			}
		}
		
		private void createEditActions(IMenuManager manager, Object selected) {
			if(selected instanceof IIpsElement){
				manager.add(new OpenEditorAction(treeViewer));
			}
		}
		
		private void createNewMenu(IMenuManager manager, Object selected) {
			if(selected instanceof IIpsElement){
				MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew); 
				newMenu.add(new NewFolderAction(getSite().getShell(), treeViewer));
				if(config.isAllowedIpsElementType(ProductCmpt.class)){
					newMenu.add(new NewProductComponentAction(getSite().getWorkbenchWindow()));
				}
				if(config.isAllowedIpsElementType(TableContents.class)){
					newMenu.add(new NewTableContentAction(getSite().getWorkbenchWindow()));
				}
				if(config.isAllowedIpsElementType(PolicyCmptType.class)){
					newMenu.add(new NewPolicyComponentTypeAction(getSite().getWorkbenchWindow()));
				}
				if(selected instanceof IProductCmpt){
					newMenu.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer, DeepCopyWizard.TYPE_NEW_VERSION));
					newMenu.add(new IpsDeepCopyAction(getSite().getShell(), treeViewer, DeepCopyWizard.TYPE_COPY_PRODUCT));
				}
				manager.add(newMenu);
			}
		}
		
		private void createReorgActions(IMenuManager manager, Object selected) {
			if(selected instanceof IIpsElement){
				manager.add(copy);
				manager.add(paste);
				manager.add(delete);
			}
		}
		
		private void createObjectInfoActions(IMenuManager manager, Object selected) {
			if(selected instanceof IIpsElement){
				if(selected instanceof IProductCmpt){
					manager.add(new ShowStructureAction(treeViewer));
					manager.add(new FindProductReferencesAction(treeViewer));
				}
				if(selected instanceof IPolicyCmptType){
					manager.add(new FindPolicyReferencesAction(treeViewer));
				}
				if(selected instanceof IPolicyCmptType | selected instanceof IProductCmpt){
					manager.add(new ShowAttributesAction(treeViewer));
				}
			}
		}

		private void createTestCaseAction(IMenuManager manager, Object selected) {
			if (selected instanceof IIpsPackageFragment
					|| selected instanceof IIpsPackageFragmentRoot
					|| selected instanceof ITestCase) {
				manager.add(new IpsTestAction(treeViewer));
			}
		}
		
		private void createRefactorMenu(IMenuManager manager, Object selected) {
			if(selected instanceof IIpsElement){
		        MenuManager subMm = new MenuManager(Messages.ModelExplorer_submenuRefactor);
		        subMm.add(new RenameAction(getSite().getShell(), treeViewer));
		        subMm.add(new MoveAction(getSite().getShell(), treeViewer));
				manager.add(subMm);
			}
		}
	}
}
