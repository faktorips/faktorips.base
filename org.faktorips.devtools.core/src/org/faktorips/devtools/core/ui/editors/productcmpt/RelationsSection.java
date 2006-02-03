package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
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
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsCopyAction;
import org.faktorips.devtools.core.ui.actions.IpsCutAction;
import org.faktorips.devtools.core.ui.actions.IpsDeleteAction;
import org.faktorips.devtools.core.ui.actions.IpsPasteAction;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.forms.IpsSection;
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

			treeViewer = new TreeViewer(tree);
			treeViewer.setContentProvider(new RelationsContentProvider());
			treeViewer.setLabelProvider(new RelationsLabelProvider());
			treeViewer.setInput(generation);
			treeViewer.addSelectionChangedListener(new SelectionChangedListener());
			treeViewer.addDropSupport(DND.DROP_LINK, new Transfer[] {TextTransfer.getInstance()}, new DropListener());
			treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
			treeViewer.expandAll();

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
        menumanager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));
        menumanager.add(new Separator());
		
		Menu menu = menumanager.createContextMenu(treeViewer.getControl());

		treeViewer.getControl().setMenu(menu);

		site.registerContextMenu("productCmptEditor.relations", menumanager, treeViewer);
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
     * 
     * @param target The target for the new relation.
     * @param relation The type of the new relation.
     */
    private void newRelation(String target, IProductCmptTypeRelation relation) {
    	generation.newRelation(relation.getName()).setTarget(target);
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
					return;
				}
				
				if (uiController == null || !uiController.getIpsObjectPart().equals(rel)) {
		    		uiController = new IpsPartUIController(rel);
				}
				
				kardMin.setEnabled(true);
				kardMax.setEnabled(true);

	    		uiController.remove(kardMinField);
	    		uiController.remove(kardMaxField);
	    		

	    		kardMinField = new IntegerField(kardMin);
	    		kardMaxField = new TextField(kardMax);
	    		
				uiController.add(kardMinField, rel, Relation.PROPERTY_MIN_CARDINALITY);
				uiController.add(kardMaxField, rel, Relation.PROPERTY_MAX_CARDINALITY);
				System.out.println("update: " + rel);
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
			event.detail = DND.DROP_LINK;
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
			if (event.item != null && event.item.getData() != null) {
				Object dropAt = event.item.getData();
				
				try {
					if (dropAt instanceof IProductCmptTypeRelation) {
						newRelation((String)event.data, ((IProductCmptTypeRelation)dropAt));
					}
					else if (dropAt instanceof IProductCmptRelation) {
						newRelation((String)event.data, ((IProductCmptRelation)dropAt).findProductCmptTypeRelation());
					}
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}

			}
		}

		public void dropAccept(DropTargetEvent event) {
			event.detail = DND.DROP_LINK;
			
		}    	
    }

	public void setActiveGeneration(IProductCmptGeneration generation) {
		if (this.generation.equals(generation)) {
			return;
		}
		
		if (generation instanceof IProductCmptGeneration) {
			this.generation = (IProductCmptGeneration)generation;
			fGenerationDirty = true;
			performRefresh();
		}
	}

}

