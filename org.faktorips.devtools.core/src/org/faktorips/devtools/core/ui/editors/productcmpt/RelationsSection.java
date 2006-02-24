package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsCutAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.views.DefaultDoubleclickListener;
import org.faktorips.util.ArgumentCheck;


/**
 * A section to display a product component's relations in a tree.
 * 
 * @author Thorsten Guenther
 */
public class RelationsSection extends IpsSection {
	
	private IProductCmptGeneration generation;
	private Text kardMin;
	private Text kardMax;
	private IntegerField kardMinField;
	private TextField kardMaxField;
	private TreeViewer treeViewer;
	private IEditorSite site;
	private boolean fGenerationDirty;
    private IProductCmptRelation toMove;

	/**
	 * Creates a new RelationsSection which displays relations for the given generation.
	 * 
	 * @param generation The base to get the relations from.
	 * @param parent The composite whicht is the ui-parent for this section.
	 * @param toolkit The ui-toolkit to support drawing.
	 */
	public RelationsSection(IProductCmptGeneration generation, Composite parent, UIToolkit toolkit, IEditorSite site) {
		super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL, toolkit);
		ArgumentCheck.notNull(generation);
		this.generation = generation;
		this.site = site;
		fGenerationDirty = true;
		
		initControls();
		
		setText(Messages.PropertiesPage_relations);
	}
	
	/**
	 * Overridden.
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		String[] pcTypeRelations = getTypeRelations(generation);
		if (pcTypeRelations.length==0) {
		    toolkit.createLabel(client, Messages.PropertiesPage_noRelationsDefined);		    
		} else {
			Composite relationRootPane = toolkit.createComposite(client);
			relationRootPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(2, false);
			relationRootPane.setLayout(layout);

			Tree tree = toolkit.getFormToolkit().createTree(relationRootPane, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			tree.setLayoutData(layoutData);

			RelationsLabelProvider labelProvider = new RelationsLabelProvider();

			treeViewer = new TreeViewer(tree);
			treeViewer.setContentProvider(new RelationsContentProvider());
			treeViewer.setLabelProvider(new MessageCueLabelProvider(labelProvider));
			treeViewer.setInput(generation);
			treeViewer.addSelectionChangedListener(new SelectionChangedListener());
			treeViewer.addDropSupport(DND.DROP_LINK | DND.DROP_MOVE, new Transfer[] {TextTransfer.getInstance(), FileTransfer.getInstance()}, new DropListener());
			treeViewer.addDragSupport(DND.DROP_MOVE, new Transfer[] {TextTransfer.getInstance()}, new DragListener(treeViewer));
			treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
			treeViewer.expandAll();
			treeViewer.addDoubleClickListener(new DefaultDoubleclickListener(treeViewer));
	        buildContextMenu();
	        
			Composite kardinalityRootPane = toolkit.createComposite(relationRootPane);
			layout = new GridLayout(1, false);
			layout.marginHeight = 1;
			kardinalityRootPane.setLayout(layout);
			layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
			kardinalityRootPane.setLayoutData(layoutData);
			
			Composite kardinalityPane = toolkit.createLabelEditColumnComposite(kardinalityRootPane);
			layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
			layoutData.verticalAlignment = SWT.TOP;
			kardinalityPane.setLayoutData(layoutData);
			layoutData = ((GridData)toolkit.createLabel(kardinalityPane, Messages.RelationsSection_cardinality).getLayoutData());
			layoutData.horizontalSpan = 2;
			layoutData.horizontalAlignment = SWT.CENTER;
			toolkit.createFormLabel(kardinalityPane, Messages.PolicyAttributesSection_minimum);
			kardMin = toolkit.createText(kardinalityPane);
			toolkit.createFormLabel(kardinalityPane, Messages.PolicyAttributesSection_maximum);
			kardMax = toolkit.createText(kardinalityPane);
			toolkit.createVerticalSpacer(kardinalityPane, 3).setBackground(kardinalityPane.getBackground());
			
			toolkit.getFormToolkit().paintBordersFor(relationRootPane);

			kardinalityPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
			toolkit.getFormToolkit().paintBordersFor(kardinalityRootPane);

		}
	}

	private void buildContextMenu() {
		
		MenuManager menumanager = new MenuManager();
		menumanager.setRemoveAllWhenShown(false);

		site.getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), new IpsCutAction(treeViewer, site.getShell()));
		site.getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new IpsCopyAction(treeViewer, site.getShell()));
		site.getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), new IpsPasteAction(treeViewer, site.getShell()));
		site.getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), new IpsDeleteAction(treeViewer));

        menumanager.add(ActionFactory.CUT.create(site.getWorkbenchWindow()));
        menumanager.add(ActionFactory.COPY.create(site.getWorkbenchWindow()));
        menumanager.add(ActionFactory.PASTE.create(site.getWorkbenchWindow()));
        menumanager.add(ActionFactory.DELETE.create(site.getWorkbenchWindow()));

        menumanager.add(new Separator());
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
        menumanager.add(new Separator());
		
		Menu menu = menumanager.createContextMenu(treeViewer.getControl());

		treeViewer.getControl().setMenu(menu);

		// Dont register context menu to avoid population with debug etc.
        // site.registerContextMenu("productCmptEditor.relations", menumanager, treeViewer); //$NON-NLS-1$
	}
	
	/**
	 * Overridden.
	 */
	protected void performRefresh() {	
		if (fGenerationDirty && treeViewer != null) {
			treeViewer.setInput(generation);
//			treeViewer.refresh();
			treeViewer.expandAll();

		}
	}
    
    /*
     * Returns all product component type relations that are defined either in the generation
     * or in the type the generation is based on.
     */
    private String[] getTypeRelations(IProductCmptGeneration generation) {
        List result = new ArrayList();
        try {
            IProductCmptType type = generation.getProductCmpt().findProductCmptType();
            if (type!=null) {
                IProductCmptTypeRelation[] typeRelations = type.getRelations();
                for (int i=0; i<typeRelations.length; i++) {
                    result.add(typeRelations[i].getName());
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
		IProductCmptRelation[] relations = generation.getRelations();
        for (int i=0; i<relations.length; i++) {
            if (!result.contains(relations[i].getProductCmptTypeRelation())) {
                result.add(relations[i].getProductCmptTypeRelation());
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }

    /**
     * Creates a new relation which connects the currently displayed generation with the given target.
     * The max cardinality for the new relation is set to the max cardinality of the given type.
     * 
     * @param target The target for the new relation.
     * @param relation The type of the new relation.
     */
    private void newRelation(String target, IProductCmptTypeRelation relation) {
    	IProductCmptRelation prodRelation = generation.newRelation(relation.getName());
    	prodRelation.setTarget(target);
    	prodRelation.setMaxCardinality(relation.getMaxCardinality());
    }

    /**
     * Creates a new relation which connects the currently displayed generation with the given target. The
     * new relation is placed before the the given one.
     */
    private void newRelation(String target, IProductCmptTypeRelation relation, IProductCmptRelation insertBefore) {
    	generation.newRelation(relation.getName(), insertBefore).setTarget(target);
    }
    
    /**
     * Listener for updating the kardinality triggerd by the selection of another relation.
     */
    private class SelectionChangedListener implements ISelectionChangedListener {
    	IpsPartUIController uiController;
    	
		public void selectionChanged(SelectionChangedEvent event) {
			Object selected = ((IStructuredSelection)event.getSelection()).getFirstElement();
			if (selected instanceof IProductCmptRelation) {
				IProductCmptRelation rel = (IProductCmptRelation) selected;

				if (rel.isDeleted()) {
		    		uiController.remove(kardMinField);
		    		uiController.remove(kardMaxField);
					kardMin.setEnabled(false);
					kardMax.setEnabled(false);
					return;
				}

				if (uiController != null) {
					uiController.remove(kardMinField);
					uiController.remove(kardMaxField);
				}

	    		if (uiController == null || !uiController.getIpsObjectPart().equals(rel)) {
		    		uiController = new IpsPartUIController(rel);
				}
				
				kardMin.setEnabled(true);
				kardMax.setEnabled(true);	    		

	    		kardMinField = new IntegerField(kardMin);
	    		kardMaxField = new TextField(kardMax);
	    		
				uiController.add(kardMinField, rel, Relation.PROPERTY_MIN_CARDINALITY);
				uiController.add(kardMaxField, rel, Relation.PROPERTY_MAX_CARDINALITY);
				uiController.updateUI();
			}
			else {
				kardMin.setEnabled(false);
				kardMax.setEnabled(false);
			}
			
		}
    }
    
    /**
     * Listener for Drop-Actions to create new relations.
     * 
     * @author Thorsten Guenther
     */
    private class DropListener implements DropTargetListener {

		public void dragEnter(DropTargetEvent event) {
			if (event.detail == 0) {
				event.detail = DND.DROP_LINK;
			}
			event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SELECT | DND.FEEDBACK_INSERT_AFTER | DND.FEEDBACK_SCROLL;
		}

		public void dragLeave(DropTargetEvent event) {
			// nothing to do
		}

		public void dragOperationChanged(DropTargetEvent event) {
			// nothing to do
		}

		public void dragOver(DropTargetEvent event) {
			// nothing to do
		}

		public void drop(DropTargetEvent event) {
			Object insertAt = null;
			// find the position to insert/move to
			if (event.item != null && event.item.getData() != null) {
				insertAt = event.item.getData();
			}
			else {
				// event happened on the treeview, but not targeted at an entry
				Object[] items = treeViewer.getVisibleExpandedElements();
				if (items.length > 0) {
					insertAt = items[items.length-1];
				}
			}

			// found no relation or relationtype which gives us the information about
			// the position of the insert, so dont drop.
			if (insertAt == null) {
				return;
			}
			
			if (event.operations == DND.DROP_MOVE) {
				move(insertAt);
			}
			else {
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// we have a file transfer
					String[] filenames = (String[])FileTransfer.getInstance().nativeToJava(event.currentDataType);
					for (int i = 0; i < filenames.length; i++) {
						IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filenames[i]));
						insert(file, insertAt);
					}
				}
				else if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// we have a text transfer
					String data = (String)TextTransfer.getInstance().nativeToJava(event.currentDataType);
					insert(data, insertAt);
				}
			}
		}

		public void dropAccept(DropTargetEvent event) {
			//nothing to do
		}    	
		
		private void move(Object insertBefore) {
			if (insertBefore instanceof IProductCmptRelation) {
				generation.moveRelation(toMove, (IProductCmptRelation)insertBefore);
			}
		}
		
		/**
		 * Insert a new relation to the product component contained in the given file.
		 * If the file is <code>null</code> or does not contain a product component, 
		 * the insert is aborted.
		 * 
		 * @param file The file describing a product component (can be null, no insert
		 * takes place then).
		 * @param insertAt The relation or relation type to insert at.
		 */
		private void insert(IFile file, Object insertAt) {
			if (file == null) {
				return;
			}
			
			IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
			if (element instanceof IIpsSrcFile && ((IIpsSrcFile)element).getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
				try {
					insert(((IProductCmpt)((IIpsSrcFile)element).getIpsObject()).getQualifiedName(), insertAt);
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
		}

		/**
		 * Inserts a new relation to the product component identified by the given target name.
		 * @param target The qualified name for the target product component
		 * @param insertAt The product component relation or product component type relation 
		 * the new relations has to be inserted. The type of the new relation is determined from
		 * this object (which means the new relation has the same product component relation type
		 * as the given one or is of the given type).
		 */
		private void insert(String target, Object insertAt) {
			try {
				if (insertAt instanceof IProductCmptTypeRelation) {
					newRelation(target, ((IProductCmptTypeRelation)insertAt));
				}
				else if (insertAt instanceof IProductCmptRelation) {
					newRelation(target, ((IProductCmptRelation)insertAt).findProductCmptTypeRelation(), (IProductCmptRelation)insertAt);
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			
		}
    }
    
    private class DragListener implements DragSourceListener {
    	ISelectionProvider selectionProvider;
    	
    	public DragListener(ISelectionProvider selectionProvider) {
    		this.selectionProvider = selectionProvider;
    	}
    	
		public void dragStart(DragSourceEvent event) {
			Object selected = ((IStructuredSelection)selectionProvider.getSelection()).getFirstElement();
			event.doit = selected instanceof IProductCmptRelation;
		}

		public void dragSetData(DragSourceEvent event) {
	        Object selected = ((IStructuredSelection)selectionProvider.getSelection()).getFirstElement();
	        if (selected instanceof IProductCmptRelation) {
	        	toMove = (IProductCmptRelation)selected;
	        	event.data = "local"; //$NON-NLS-1$
	        }
		}

		public void dragFinished(DragSourceEvent event) {
			toMove = null;
		}
    	
    }
}

