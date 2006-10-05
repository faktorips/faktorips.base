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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.NewProductCmptRelationAction;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityPaneEditField;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * A section to display a product component's relations in a tree.
 * 
 * @author Thorsten Guenther
 */
public class RelationsSection extends IpsSection{

	/**
	 * the generation the displayed informations are based on.
	 */
	private IProductCmptGeneration generation;

	private CardinalityPanel cardinalityPanel;

	private CardinalityPaneEditField kardMinField;

	private CardinalityPaneEditField kardMaxField;

	/**
	 * The tree viewer displaying all the relations.
	 */
	private TreeViewer treeViewer;

	/**
	 * The site this editor is related to (e.g. for menu and toolbar handling)
	 */
	private IEditorSite site;

	/**
	 * Flag to indicate that the generation this informations are based on has
	 * changed (<code>true</code>)
	 */
	private boolean fGenerationDirty;

	/**
	 * Field to store the product component relation that should be moved using
	 * drag and drop.
	 */
	private IProductCmptRelation toMove;

	/**
	 * <code>true</code> if this section is enabled, <code>false</code>
	 * otherwise. This flag is used to control the enablement-state of the
	 * contained controlls.
	 */
	private boolean fEnabled;

	/**
	 * The popup-Menu for the treeview if enabled.
	 */
	private Menu treePopup;

	/**
	 * Empty popup-Menu.
	 */
	private Menu emptyMenu;
	
	/**
	 * Listener to update the cardinality-pane on selection changes.
	 */
	private SelectionChangedListener selectionChangedListener;

	/**
	 * Creates a new RelationsSection which displays relations for the given
	 * generation.
	 * 
	 * @param generation
	 *            The base to get the relations from.
	 * @param parent
	 *            The composite whicht is the ui-parent for this section.
	 * @param toolkit
	 *            The ui-toolkit to support drawing.
	 */
	public RelationsSection(IProductCmptGeneration generation,
			Composite parent, UIToolkit toolkit, IEditorSite site) {
		super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL, toolkit);
		ArgumentCheck.notNull(generation);
		this.generation = generation;
		this.site = site;
		fGenerationDirty = true;
		fEnabled = true;
		initControls();

		setText(Messages.PropertiesPage_relations);
		
		site.setSelectionProvider(treeViewer);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		Composite relationRootPane = toolkit.createComposite(client);
		relationRootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		RelationsContentProvider rcp = new RelationsContentProvider();
		
		if (rcp.getElements(generation).length == 0) {
			GridLayout layout = (GridLayout) client.getLayout();
			layout.marginHeight = 2;
			layout.marginWidth = 1;

			relationRootPane.setLayout(new GridLayout(1, true));
			relationRootPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					true, true));

			toolkit.createLabel(relationRootPane,
					Messages.PropertiesPage_noRelationsDefined).setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));
		} else {
			relationRootPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
					true, true));
			GridLayout layout = new GridLayout(2, false);
			relationRootPane.setLayout(layout);
	
			Tree tree = toolkit.getFormToolkit().createTree(relationRootPane,
					SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			tree.setLayoutData(layoutData);
	
			RelationsLabelProvider labelProvider = new RelationsLabelProvider();
	
			selectionChangedListener = new SelectionChangedListener();
			
			treeViewer = new TreeViewer(tree);
			treeViewer.setContentProvider(new RelationsContentProvider());
			treeViewer.setLabelProvider(new MyMessageCueLabelProvider(
					labelProvider));
			treeViewer.setInput(generation);
			treeViewer
					.addSelectionChangedListener(selectionChangedListener);
			treeViewer.addDropSupport(DND.DROP_LINK | DND.DROP_MOVE,
					new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() },
					new DropListener());
			treeViewer.addDragSupport(DND.DROP_MOVE,
					new Transfer[] { TextTransfer.getInstance() },
					new DragListener(treeViewer));
			treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
			treeViewer.expandAll();
	
			new MessageService(treeViewer);
	
			buildContextMenu();
	
			cardinalityPanel = new CardinalityPanel(relationRootPane, toolkit);
			cardinalityPanel.setEnabled(false);
	
			addFocusControl(treeViewer.getTree());
			registerDoubleClickListener();
		}
		toolkit.getFormToolkit().paintBordersFor(relationRootPane);
	}

	/**
	 * register a double click listener to open the edit-dialog to edit the relation. 
	 */
	private void registerDoubleClickListener() {
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!(event.getSelection() instanceof IStructuredSelection)) {
					return;
				}
				
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object selected = selection.getFirstElement();
				
				if (!(selected instanceof IProductCmptRelation)) {
					return;
				}
				
				RelationEditDialog dialog = new RelationEditDialog((IProductCmptRelation)selected, site.getShell());
				dialog.open();
				refresh();

			}
		});
	}

	/**
	 * Creates the context menu for the treeview.
	 */
	private void buildContextMenu() {

		MenuManager menumanager = new MenuManager();
		menumanager.setRemoveAllWhenShown(false);

		menumanager.add(new NewProductCmptRelationAction(site.getShell(),
				treeViewer, this));

		menumanager.add(ActionFactory.DELETE.create(site.getWorkbenchWindow()));

		menumanager.add(new Separator());
		menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end")); //$NON-NLS-1$
		menumanager.add(new Separator());

		treePopup = menumanager.createContextMenu(treeViewer.getControl());

		treeViewer.getControl().setMenu(treePopup);

		// Dont register context menu to avoid population with debug etc.
		// site.registerContextMenu("productCmptEditor.relations", menumanager,
		// treeViewer); //$NON-NLS-1$

		// create empty menu for later use
		emptyMenu = new MenuManager()
				.createContextMenu(treeViewer.getControl());
	}

	/**
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		if (fGenerationDirty && treeViewer != null) {
			treeViewer.setInput(generation);
			treeViewer.expandAll();
		}
		
		if (selectionChangedListener != null && selectionChangedListener.uiController != null) {
			selectionChangedListener.uiController.updateUI();
		}
	}

	/**
	 * Creates a new Relation of the given type.
	 */
	public IProductCmptRelation newRelation(
			IProductCmptTypeRelation relationType) {
		return generation.newRelation(relationType.getName());
	}

	/**
	 * Returns the currently active generation for this page.
	 */
	public IProductCmptGeneration getActiveGeneration() {
		return generation;
	}

	/**
	 * Creates a new relation which connects the currently displayed generation
	 * with the given target. The max cardinality for the new relation is set to
	 * the max cardinality of the given type.
	 * 
	 * @param target
	 *            The target for the new relation.
	 * @param relation
	 *            The type of the new relation.
	 */
	private IProductCmptRelation newRelation(String target, IProductCmptTypeRelation relation) {
		IProductCmptRelation prodRelation = generation.newRelation(relation
				.getName());
		prodRelation.setTarget(target);
		prodRelation.setMaxCardinality(1);
		prodRelation.setMinCardinality(relation.getMinCardinality());
		return prodRelation;
	}

	/**
	 * Creates a new relation which connects the currently displayed generation
	 * with the given target. The new relation is placed before the the given
	 * one.
	 */
	private IProductCmptRelation newRelation(String target, IProductCmptTypeRelation relation,
			IProductCmptRelation insertBefore) {
		IProductCmptRelation prodRelation = generation.newRelation(relation
				.getName(), insertBefore);
		prodRelation.setTarget(target);
		prodRelation.setMaxCardinality(1);
		prodRelation.setMinCardinality(relation.getMinCardinality());
		return prodRelation;
	}

	/**
	 * Listener for updating the kardinality triggerd by the selection of
	 * another relation.
	 */
	private class SelectionChangedListener implements ISelectionChangedListener {
		IpsPartUIController uiController;

		public void selectionChanged(SelectionChangedEvent event) {
			Object selected = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (selected instanceof IProductCmptRelation) {
				IProductCmptRelation rel = (IProductCmptRelation) selected;

				if (rel.isDeleted()) {
					uiController.remove(kardMinField);
					uiController.remove(kardMaxField);
					cardinalityPanel.setEnabled(false);
					return;
				}

				if (uiController != null) {
					uiController.remove(kardMinField);
					uiController.remove(kardMaxField);
				}

				if (uiController == null
						|| !uiController.getIpsObjectPart().equals(rel)) {
					uiController = new IpsPartUIController(rel);
				}

				kardMinField = new CardinalityPaneEditField(cardinalityPanel,
						true);
				kardMaxField = new CardinalityPaneEditField(cardinalityPanel,
						false);

				uiController.add(kardMinField, rel,
						Relation.PROPERTY_MIN_CARDINALITY);
				uiController.add(kardMaxField, rel,
						Relation.PROPERTY_MAX_CARDINALITY);
				uiController.updateUI();

				// enable the fields for cardinality only, if this section
				// is enabled.
				cardinalityPanel.setEnabled(fEnabled);
			} else {
				cardinalityPanel.setEnabled(false);
			}
		}
	}

	/**
	 * Listener for Drop-Actions to create new relations.
	 * 
	 * @author Thorsten Guenther
	 */
	private class DropListener implements DropTargetListener {

		private int oldDetail = DND.DROP_NONE;
		
		public void dragEnter(DropTargetEvent event) {
			if (!fEnabled) {
				event.detail = DND.DROP_NONE;
				return;
			}

			if (event.detail == 0) {
				event.detail = DND.DROP_LINK;
			}
			
			oldDetail = event.detail;
			
			event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT
					| DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_SCROLL;
		}

		public void dragLeave(DropTargetEvent event) {
			// nothing to do
		}

		public void dragOperationChanged(DropTargetEvent event) {
			// nothing to do
		}

		public void dragOver(DropTargetEvent event) {
			Object insertAt = getInsertAt(event);
			if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
				// we have a file transfer
				String[] filenames = (String[]) FileTransfer.getInstance().nativeToJava(event.currentDataType);

				// Under some platforms, the data is not available during dragOver.
				if (filenames == null) {
					return;
				}
				
				for (int i = 0; i < filenames.length; i++) {
					IFile file = getFile(filenames[i]);
					try {
						IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);

						if (element == null || !element.exists()) {
							event.detail = DND.DROP_NONE;
							return;
						}
						
						IProductCmpt target = getProductCmpt(file); 

						IProductCmptTypeRelation relation = null;
						if (insertAt instanceof IProductCmptTypeRelation) {
							relation = (IProductCmptTypeRelation) insertAt;
						} else if (insertAt instanceof IProductCmptRelation) {
							relation = ((IProductCmptRelation)insertAt).findProductCmptTypeRelation();
						}

						if (generation.canCreateValidRelation(target, relation)) {
							event.detail = oldDetail;
						} 
						else {
							event.detail = DND.DROP_NONE;
						}
					} catch (CoreException e) {
						IpsPlugin.log(e);
						event.detail = DND.DROP_NONE;
					}
				}
			}
			else if (!(toMove != null && insertAt instanceof IProductCmptRelation)) {
				event.detail = DND.DROP_NONE;
			}
		}

		public void drop(DropTargetEvent event) {
			Object insertAt = getInsertAt(event);

			// found no relation or relationtype which gives us the information
			// about
			// the position of the insert, so dont drop.
			if (insertAt == null) {
				return;
			}

			if (event.operations == DND.DROP_MOVE) {
				move(insertAt);
			} else 	if (FileTransfer.getInstance().isSupportedType(
					event.currentDataType)) {
				// we have a file transfer
				String[] filenames = (String[]) FileTransfer.getInstance()
				.nativeToJava(event.currentDataType);
				for (int i = 0; i < filenames.length; i++) {
					IFile file = getFile(filenames[i]);
					insert(file, insertAt);
				}
			}
			treeViewer.refresh();
			treeViewer.expandAll();
		}

		public void dropAccept(DropTargetEvent event) {
			// nothing to do
		}

		private IFile getFile(String filename) {
			return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
		}
		
		private IProductCmpt getProductCmpt(IFile file) throws CoreException {
			if (file == null) {
				return null;
			}

			IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);

			if (!element.exists()) {
				return null;
			}

			if (element instanceof IIpsSrcFile
					&& ((IIpsSrcFile) element).getIpsObjectType().equals(
							IpsObjectType.PRODUCT_CMPT)) {
				return (IProductCmpt) ((IIpsSrcFile) element).getIpsObject();
			}
			
			return null;
		}
		
		private void move(Object insertBefore) {
			if (insertBefore instanceof IProductCmptRelation) {
				generation.moveRelation(toMove,
						(IProductCmptRelation) insertBefore);
			}
		}

		private Object getInsertAt(DropTargetEvent event) {
			if (event.item != null && event.item.getData() != null) {
				return event.item.getData();
			} else {
				// event happened on the treeview, but not targeted at an entry
				TreeItem[] items = treeViewer.getTree().getItems();
				if (items.length > 0) {
					return items[items.length - 1].getData();
				}
			}
			return null;
		}
		
		/**
		 * Insert a new relation to the product component contained in the given
		 * file. If the file is <code>null</code> or does not contain a
		 * product component, the insert is aborted.
		 * 
		 * @param file
		 *            The file describing a product component (can be null, no
		 *            insert takes place then).
		 * @param insertAt
		 *            The relation or relation type to insert at.
		 */
		private void insert(IFile file, Object insertAt) {
			try {
				IProductCmpt cmpt = getProductCmpt(file);
				if (cmpt != null) {
					insert(cmpt.getQualifiedName(), insertAt);
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}

		/**
		 * Inserts a new relation to the product component identified by the
		 * given target name.
		 * 
		 * @param target
		 *            The qualified name for the target product component
		 * @param insertAt
		 *            The product component relation or product component type
		 *            relation the new relations has to be inserted. The type of
		 *            the new relation is determined from this object (which
		 *            means the new relation has the same product component
		 *            relation type as the given one or is of the given type).
		 */
		private void insert(String target, Object insertAt) {
			try {
				if (insertAt instanceof IProductCmptTypeRelation) {
					newRelation(target, ((IProductCmptTypeRelation) insertAt));
				} else if (insertAt instanceof IProductCmptRelation) {
					newRelation(target, ((IProductCmptRelation) insertAt)
							.findProductCmptTypeRelation(),
							(IProductCmptRelation) insertAt);
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
	}

	/**
	 * Listener to handle the move of relations.
	 * 
	 * @author Thorsten Guenther
	 */
	private class DragListener implements DragSourceListener {
		ISelectionProvider selectionProvider;

		public DragListener(ISelectionProvider selectionProvider) {
			this.selectionProvider = selectionProvider;
		}

		public void dragStart(DragSourceEvent event) {
			Object selected = ((IStructuredSelection) selectionProvider
					.getSelection()).getFirstElement();
			event.doit = selected instanceof IProductCmptRelation;
			
			// we provide the event data yet so we can decide if we will
			// accept a drop at drag-over time.
			if (selected instanceof IProductCmptRelation) {
				toMove = (IProductCmptRelation) selected;
				event.data = "local"; //$NON-NLS-1$
			}
		}

		public void dragSetData(DragSourceEvent event) {
			Object selected = ((IStructuredSelection) selectionProvider
					.getSelection()).getFirstElement();
			if (selected instanceof IProductCmptRelation) {
				toMove = (IProductCmptRelation) selected;
				event.data = "local"; //$NON-NLS-1$
			}
		}

		public void dragFinished(DragSourceEvent event) {
			toMove = null;
		}

	}

	/**
	 * Returns all targets for all relations defined with the given product
	 * component relation type.
	 * 
	 * @param relation
	 *            The type of the relations to find.
	 */
	public IProductCmpt[] getRelationTargetsFor(
			IProductCmptTypeRelation relation) {
		IProductCmptRelation[] relations = generation.getRelations(relation
				.getName());

		IProductCmpt[] targets = new IProductCmpt[relations.length];
		for (int i = 0; i < relations.length; i++) {
			try {
				targets[i] = (IProductCmpt) generation.getIpsProject()
						.findIpsObject(IpsObjectType.PRODUCT_CMPT,
								relations[i].getTarget());
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
		}
		return targets;
	}

	/**
	 * To get access to the informations which depend on the selections that can
	 * be made in this section, only some parts can be disabled, other parts
	 * need special handling.
	 * 
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		this.fEnabled = enabled;
		if (treeViewer == null) {
			// no relations defined, so no tree to disable.
			return;
		}

		if (enabled) {
			treeViewer.getTree().setMenu(this.treePopup);
		} else {
			treeViewer.getTree().setMenu(emptyMenu);
		}
		cardinalityPanel.setEnabled(enabled);

		// disabele IPSDeleteAction
		IAction delAction= site.getActionBars().getGlobalActionHandler(ActionFactory.DELETE.getId());
		if(delAction!=null){
			delAction.setEnabled(enabled);
		}
		
	}

	private MessageList validate(Object element) throws CoreException {
		if (element instanceof IProductCmptRelation) {
			return ((IProductCmptRelation) element).validate();
		} else if (element instanceof IProductCmptTypeRelation) {
			MessageList ml = generation.validate();
			return ml.getMessagesFor(((IProductCmptTypeRelation) element)
					.getTargetRoleSingular());
		}
		return new MessageList();
	}

	private class MessageService extends TreeMessageHoverService {

		public MessageService(TreeViewer viewer) {
			super(viewer);
		}

		protected MessageList getMessagesFor(Object element)
				throws CoreException {
			return validate(element);
		}
	}

	/**
	 * Special cue label provider to get messages for product component type
	 * relations from the generations instead of the product component type
	 * relation itself.
	 * 
	 * @author Thorsten Guenther
	 */
	private class MyMessageCueLabelProvider extends MessageCueLabelProvider {

		public MyMessageCueLabelProvider(ILabelProvider baseProvider) {
			super(baseProvider);
		}

		/**
		 * {@inheritDoc}
		 */
		protected MessageList getMessages(Object element) throws CoreException {
			if (element instanceof IProductCmptTypeRelation) {
				IProductCmptTypeRelation relType = (IProductCmptTypeRelation) element;
				return generation.validate().getMessagesFor(
						relType.getTargetRoleSingular());
			}

			return super.getMessages(element);
		}

	}
}
