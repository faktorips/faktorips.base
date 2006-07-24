package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.ui.actions.RefreshAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.part.ViewPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.actions.FindPolicyReferencesAction;
import org.faktorips.devtools.core.ui.actions.FindProductReferencesAction;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAndSaveAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.actions.NewFolderAction;
import org.faktorips.devtools.core.ui.actions.NewPolicyComponentTypeAction;
import org.faktorips.devtools.core.ui.actions.NewProductComponentAction;
import org.faktorips.devtools.core.ui.actions.NewTableContentAction;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.core.ui.actions.ShowAttributesAction;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.core.ui.views.IpsResourceChangeListener;
import org.faktorips.devtools.core.ui.views.TreeViewerDoubleclickListener;

/**
 * The ModelExplorer is a ViewPart for displaying ProductComponents, TableContents
 * TableStructures and PolicyComponentTypes along with their Attributes. The
 * view uses a TreeViewer to represent the hierarchical datastructure. It
 * can be configured to show the tree of PackageFragments in a hierarchical
 * (default) or a flat layout style.
 * 
 * @author Stefan Widmaier
 */

public class ModelExplorer extends ViewPart{
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
	 * Content provider for the tree viewer. 
	 */
	private ModelContentProvider contentProvider = new ModelContentProvider();

	/**
	 * Label provider for the tree viewer. 
	 */
	private ModelLabelProvider labelProvider = new ModelLabelProvider();
	
	/**
	 * Filter used in flat layout, where it filters out empty packageFragments.
	 */
	private ViewerFilter emptyPackageFilter = new EmptyPackageFilter();
	
	private IpsResourceChangeListener resourceListener;
	
	/**
	 * Flag that indicates whether the current layout style is flat (true) or hierarchical (false).
	 */
	private boolean isFlatLayout = false;
	
	public ModelExplorer() {
		super();
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
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setSorter(new ModelSorter());
		treeViewer.setInput(IpsPlugin.getDefault().getIpsModel());
		treeViewer.addDoubleClickListener(new TreeViewerDoubleclickListener(treeViewer));
		
		DecoratingLabelProvider decoProvider = new DecoratingLabelProvider(
				labelProvider, IpsPlugin.getDefault().getWorkbench()
						.getDecoratorManager().getLabelDecorator());
		decoProvider = new DecoratingLabelProvider(decoProvider,
				new IpsProblemsLabelDecorator());
		treeViewer.setLabelProvider(decoProvider);

		this.getSite().setSelectionProvider(treeViewer);
		resourceListener= new IpsResourceChangeListener(treeViewer);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceListener, IResourceChangeEvent.POST_CHANGE);
		/*
		 * Use the current value of isFlatLayout, which is set while loading the
		 * memento/viewState before this method is called
		 */
		setFlatLayout(isFlatLayout);
		
		
//		getSite().getPage().addPartListener(fPartListener);
//		IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
		

		createMenu();
		createContextMenu();
	}

//	public void contentsChanged(ContentChangeEvent event) {
//		try {
//			treeViewer.refresh(event.getIpsSrcFile().getIpsObject());
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
////		treeViewer.refresh();
//	}
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
				ActionFactory.COPY.getId(),
				new IpsCopyAction(treeViewer, this.getSite().getShell()));
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.PASTE.getId(),
				new IpsPasteAction(treeViewer, this.getSite().getShell()));
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.DELETE.getId(), new IpsDeleteAndSaveAction(treeViewer));

		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(false);

		manager.add(new OpenEditorAction(treeViewer));
		MenuManager newMenu = new MenuManager(Messages.ModelExplorer_submenuNew);
		newMenu.add(new NewFolderAction(getSite().getShell(), treeViewer));
		newMenu.add(new NewProductComponentAction(getSite()
				.getWorkbenchWindow()));
		newMenu.add(new NewTableContentAction(getSite().getWorkbenchWindow()));
		newMenu.add(new NewPolicyComponentTypeAction(getSite()
				.getWorkbenchWindow()));
		manager.add(newMenu);
		manager.add(new Separator());
//		manager.add(ActionFactory.CUT.create(this.getSite()
//				.getWorkbenchWindow()));
		manager.add(ActionFactory.COPY.create(this.getSite()
				.getWorkbenchWindow()));
		manager.add(ActionFactory.PASTE.create(this.getSite()
				.getWorkbenchWindow()));
		manager.add(ActionFactory.DELETE.create(this.getSite()
				.getWorkbenchWindow()));
		manager.add(new Separator());
		manager.add(new ShowStructureAction(treeViewer));
		MenuManager refMenu= new MenuManager(Messages.ModelExplorer_submenueReferences);
		refMenu.add(new FindProductReferencesAction(treeViewer));
		refMenu.add(new FindPolicyReferencesAction(treeViewer));
		manager.add(refMenu);
		manager.add(new ShowAttributesAction());
		manager.add(new Separator());
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end"));//$NON-NLS-1$		

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
			treeViewer.addFilter(emptyPackageFilter);
		} else {
			treeViewer.removeFilter(emptyPackageFilter);
		}
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
	
	public void dispose() {
		super.dispose();
		// unregister listeners
//		IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}


}
