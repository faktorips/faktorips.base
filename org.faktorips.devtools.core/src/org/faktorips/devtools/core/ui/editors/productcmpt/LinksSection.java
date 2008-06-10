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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityPaneEditField;
import org.faktorips.devtools.core.ui.editors.ISelectionProviderActivation;
import org.faktorips.devtools.core.ui.editors.TreeMessageHoverService;
import org.faktorips.devtools.core.ui.editors.pctype.ContentsChangeListenerForWidget;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

/**
 * A section to display a product component's relations in a tree.
 * 
 * @author Thorsten Guenther
 */
public class LinksSection extends IpsSection implements ISelectionProviderActivation{

	/**
	 * the generation the displayed informations are based on.
	 */
	private IProductCmptGeneration generation;

	private CardinalityPanel cardinalityPanel;

	private CardinalityPaneEditField cardMinField;

	private CardinalityPaneEditField cardMaxField;

	/**
	 * The tree viewer displaying all the relations.
	 */
	private TreeViewer treeViewer;

	/**
	 * The site this editor is related to (e.g. for menu and toolbar handling)
	 */
	private IEditorSite site;

	/**
	 * Flag to indicate that the generation has changed (<code>true</code>)
	 */
	private boolean generationDirty;

	/**
	 * Field to store the product component relation that should be moved using
	 * drag and drop.
	 */
	private IProductCmptLink toMove;

	/**
	 * <code>true</code> if this section is enabled, <code>false</code>
	 * otherwise. This flag is used to control the enablement-state of the
	 * contained controlls.
	 */
	private boolean enabled;

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

    private OpenReferencedProductCmptInEditorAction openAction;

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
	public LinksSection(IProductCmptGeneration generation,
			Composite parent, UIToolkit toolkit, IEditorSite site) {
		super(parent, Section.TITLE_BAR, GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL, toolkit);
		ArgumentCheck.notNull(generation);
		this.generation = generation;
		this.site = site;
        generationDirty = true;
		enabled = true;
		initControls();
		setText(Messages.PropertiesPage_relations);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initClientComposite(Composite client, UIToolkit toolkit) {
		Composite relationRootPane = toolkit.createComposite(client);
		relationRootPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		LinksContentProvider rcp = new LinksContentProvider();
		
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
	
			LinksLabelProvider labelProvider = new LinksLabelProvider();
	
			selectionChangedListener = new SelectionChangedListener();
			
			treeViewer = new TreeViewer(tree);
			treeViewer.setContentProvider(new LinksContentProvider());
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
	
            final LinkSectionMessageCueLabelProvider msgCueLp = new LinkSectionMessageCueLabelProvider(labelProvider, generation.getIpsProject());
			treeViewer.setLabelProvider(msgCueLp);
            new TreeMessageHoverService(treeViewer){
                protected MessageList getMessagesFor(Object element) throws CoreException {
                    return msgCueLp.getMessages(element);
                }
            };
	
			buildContextMenu();
	
			cardinalityPanel = new CardinalityPanel(relationRootPane, toolkit);
            cardinalityPanel.setDataChangeable(isDataChangeable());
			cardinalityPanel.setEnabled(false);
	
			addFocusControl(treeViewer.getTree());
			registerDoubleClickListener();
			registerOpenLinkListener();
			ModelViewerSynchronizer synchronizer = new ModelViewerSynchronizer(generation, treeViewer);
			synchronizer.setWidget(client);
			IpsPlugin.getDefault().getIpsModel().addChangeListener(synchronizer);
		}
		toolkit.getFormToolkit().paintBordersFor(relationRootPane);
	}
    
    /*
     * If mouse down and the CTRL key is pressed then the selected object will be opened in a new
     * edior
     */
    private void registerOpenLinkListener() {
        MouseAdapter adapter = new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0){
                    openLink();
                }
            }

        };
        treeViewer.getTree().addMouseListener(adapter);
    }

    private void openLink() {
        openAction.run();
    }

    /*
	 * Register a double click listener to open the referenced product component in a new editor 
	 */
	private void registerDoubleClickListener() {
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                openLink();
            }
        });
    }

	/**
	 * Creates the context menu for the treeview.
	 */
	private void buildContextMenu() {

		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(false);

		menuManager.add(new NewProductCmptRelationAction(site.getShell(),
				treeViewer, this));

        openAction = new OpenReferencedProductCmptInEditorAction();
        menuManager.add(openAction);
		menuManager.add(ActionFactory.DELETE.create(site.getWorkbenchWindow()));
		menuManager.add(new OpenProductCmptRelationDialogAction());
        
		menuManager.add(new Separator());
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end")); //$NON-NLS-1$
		menuManager.add(new Separator());

		treePopup = menuManager.createContextMenu(treeViewer.getControl());

		treeViewer.getControl().setMenu(treePopup);

		// Dont register context menu to avoid population with debug etc.
		// site.registerContextMenu("productCmptEditor.relations", menumanager,
		// treeViewer); //$NON-NLS-1$

		// create empty menu for later use
		emptyMenu = new MenuManager().createContextMenu(treeViewer.getControl());
	}

	/**
	 * {@inheritDoc}
	 */
	protected void performRefresh() {
		if (generationDirty && treeViewer != null) {
            treeViewer.refresh();
			treeViewer.expandAll();
            generationDirty = false;
		}
		
		if (selectionChangedListener != null && selectionChangedListener.uiController != null) {
			selectionChangedListener.uiController.updateUI();
		}
	}

	/**
	 * Creates a new link of the given association.
	 */
	public IProductCmptLink newLink(String associationName) {
		return generation.newLink(associationName);
	}

	/**
	 * Returns the currently active generation for this page.
	 */
	public IProductCmptGeneration getActiveGeneration() {
		return generation;
	}
    
    /**
     * Returns the name of the product component type relation identified by the target.
     * The target is either the name itself (the top level nodes of the relation section tree)
     * or instances of IProductCmptRelation. (the nodes below the relation type nodes).
     */
    String getAssociationName(Object target) {
        if (target instanceof String) {
            return (String)target;
        }
        if (target instanceof IProductCmptLink) {
            return ((IProductCmptLink)target).getAssociation();
        }
        return null;
    }
    
	/**
	 * Creates a new link which connects the currently displayed generation
	 * with the given target. The new link is placed before the the given
	 * one.
	 */
	private IProductCmptLink newLink(String target, String association, IProductCmptLink insertBefore) {
        IProductCmptLink newLink = null;
        if (insertBefore != null) {
            newLink = generation.newLink(association, insertBefore);
        }
        else {
            newLink = generation.newLink(association);
        }
		newLink.setTarget(target);
		newLink.setMaxCardinality(1);
        newLink.setMinCardinality(0);
		return newLink;
	}

	/**
	 * Listener for updating the cardinality triggerd by the selection of
	 * another link.
	 */
	private class SelectionChangedListener implements ISelectionChangedListener {
        IpsObjectUIController uiController;

		public void selectionChanged(SelectionChangedEvent event) {
			Object selected = ((IStructuredSelection) event.getSelection())
					.getFirstElement();
			if (selected instanceof IProductCmptLink) {
			    updateCardinalityPanel((IProductCmptLink)selected);
            } else {
                deactivateCardinalityPanel();			}
            if (! isDataChangeable()){
                deactivateCardinalityPanel();
            }
		}
        
        void updateCardinalityPanel(IProductCmptLink link) {
            if (link.isDeleted()) {
                deactivateCardinalityPanel();
                return;
            }
            
            try {
                IIpsProject ipsProject = link.getIpsProject();
                IProductCmptTypeAssociation association = link.findAssociation(ipsProject);
                if (!association.constrainsPolicyCmptTypeAssociation(ipsProject)) {
                    deactivateCardinalityPanel();
                    return;
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                deactivateCardinalityPanel();
                return;
            }

            if (uiController != null) {
                removeMinMaxFields();
            }
            if (uiController == null || !uiController.getIpsObjectPartContainer().equals(link)) {
                uiController = new IpsObjectUIController(link);
            }
            addMinMaxFields(link);
            uiController.updateUI();

            // enable the fields for cardinality only, if this section is enabled.
            cardinalityPanel.setEnabled(enabled);
        }
        
        void deactivateCardinalityPanel() {
            cardinalityPanel.setEnabled(false);
            removeMinMaxFields();
        }
        
        void removeMinMaxFields() {
            if (uiController!=null) {
                uiController.remove(cardMinField);
                uiController.remove(cardMaxField);
            }
        }
        
        void addMinMaxFields(IProductCmptLink link ) {
            cardMinField = new CardinalityPaneEditField(cardinalityPanel, true);
            cardMaxField = new CardinalityPaneEditField(cardinalityPanel, false);
            uiController.add(cardMinField, link, PolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
            uiController.add(cardMaxField, link, PolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
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
			if (!enabled) {
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
				
                boolean accept = false;
                
				for (int i = 0; i < filenames.length; i++) {
					IFile file = getFile(filenames[i]);
					try {
						IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);

						if (element == null || !element.exists()) {
							event.detail = DND.DROP_NONE;
							return;
						}
						
						IProductCmpt target = getProductCmpt(file); 

						String association = null;
						if (insertAt instanceof String) { // product component type association
							association = (String) insertAt;
						} else if (insertAt instanceof IProductCmptLink) {
							association = ((IProductCmptLink)insertAt).getAssociation();
						}

						if (generation.canCreateValidLink(target, association, generation.getIpsProject())) {
						    accept = true;
                        }
					} catch (CoreException e) {
						IpsPlugin.log(e);
					}
				}

                if (accept == true) {
                    // we can create at least on of the requested Relations - so we accept the drop
				    event.detail = oldDetail;
				} 
				else {
				    event.detail = DND.DROP_NONE;
				}

			}
			else if (!(toMove != null && insertAt instanceof IProductCmptLink)) {
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
			if (!isDataChangeable()) {
			    event.detail = DND.DROP_NONE;
            }
		}

		private IFile getFile(String filename) {
			return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
		}
		
		private IProductCmpt getProductCmpt(IFile file) throws CoreException {
			if (file == null) {
				return null;
			}

			IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);

			if (element == null || !element.exists()) {
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
			if (insertBefore instanceof IProductCmptLink) {
				generation.moveLink(toMove,
						(IProductCmptLink) insertBefore);
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
					insert(cmpt, insertAt);
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
		private void insert(IProductCmpt cmpt, Object insertAt) {
            String target = cmpt.getQualifiedName();
            String association = null;
            IProductCmptLink insertBefore = null;
            try {
                if (insertAt instanceof String) { // product component type relation
                    association = (String)insertAt;
                }
                else if (insertAt instanceof IProductCmptLink) {
                    association = ((IProductCmptLink)insertAt).getAssociation();
                    insertBefore = (IProductCmptLink)insertAt;
                }
                if (generation.canCreateValidLink(cmpt, association, generation.getIpsProject())) {
                    newLink(target, association, insertBefore);
                }
            }
            catch (CoreException e) {
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
			event.doit = (selected instanceof IProductCmptLink) && isDataChangeable();
			
			// we provide the event data yet so we can decide if we will
			// accept a drop at drag-over time.
			if (selected instanceof IProductCmptLink) {
				toMove = (IProductCmptLink) selected;
				event.data = "local"; //$NON-NLS-1$
			}
		}

		public void dragSetData(DragSourceEvent event) {
			Object selected = ((IStructuredSelection) selectionProvider
					.getSelection()).getFirstElement();
			if (selected instanceof IProductCmptLink) {
				toMove = (IProductCmptLink) selected;
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
	 * @param relationType
	 *            The type of the relations to find.
	 */
	public IProductCmpt[] getRelationTargetsFor(String relationType) {
		IProductCmptLink[] relations = generation.getLinks(relationType);
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
    
    private void openLinkEditDialog(IProductCmptLink link) {
        try {
            IIpsSrcFile file = link.getIpsObject().getIpsSrcFile();
            IIpsSrcFileMemento memento = file.newMemento();
            LinkEditDialog dialog = new LinkEditDialog(link, getShell());
            if (dialog == null) {
                return;
            }
            dialog.setDataChangeable(isDataChangeable());            
            int rc = dialog.open();
            if (rc == Dialog.CANCEL) {
                file.setMemento(memento);
            } else if (rc == Dialog.OK){
                refresh();
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

	/**
	 * To get access to the informations which depend on the selections that can
	 * be made in this section, only some parts can be disabled, other parts
	 * need special handling.
	 * 
	 * {@inheritDoc}
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

	/**
	 * Special cue label provider to get messages for product component type
	 * relations from the generations instead of the product component type
	 * relation itself.
	 * 
	 * @author Thorsten Guenther
	 */
	private class LinkSectionMessageCueLabelProvider extends MessageCueLabelProvider {

		public LinkSectionMessageCueLabelProvider(ILabelProvider baseProvider, IIpsProject ipsProject) {
			super(baseProvider, ipsProject);
		}

		/**
		 * {@inheritDoc}
		 */
		public MessageList getMessages(Object element) throws CoreException {
			if (element instanceof String) {
				return generation.validate(generation.getIpsProject()).getMessagesFor((String)element);
			}
			return super.getMessages(element);
		}

	}
	
    class OpenProductCmptRelationDialogAction extends IpsAction {

        public OpenProductCmptRelationDialogAction() {
            super(treeViewer);
            setText(Messages.RelationsSection_ContextMenu_Properties);
        }

        /** 
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IProductCmptLink) {
                IProductCmptLink relation = (IProductCmptLink)selected;
                openLinkEditDialog(relation);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IProductCmptLink);
        }

    }

    class OpenReferencedProductCmptInEditorAction extends IpsAction {

        public OpenReferencedProductCmptInEditorAction() {
            super(treeViewer);
            setText(Messages.RelationsSection_ContextMenu_OpenInNewEditor);
        }

        /**
         * {@inheritDoc}
         */
        protected boolean computeEnabledProperty(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            return (selected instanceof IProductCmptLink);
        }

        /** 
         * {@inheritDoc}
         */
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IProductCmptLink) {
                IProductCmptLink relation = (IProductCmptLink)selected;
                try {
                    IProductCmpt target = relation.findTarget(generation.getIpsProject());
                    IpsPlugin.getDefault().openEditor(target);
                } catch (Exception e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
        
    }

    /**
     * Synchronizes the model object thus the generation object with the tree viewer. It implements an algorithm
     * that calculates the next selection of an item after the currently selected item has been selected. 
     *  
     * @author Peter Erzberger
     */
    private class ModelViewerSynchronizer extends ContentsChangeListenerForWidget implements SelectionListener{

        private List lastSelectionPath = null;
        
        private ModelViewerSynchronizer(IProductCmptGeneration generation, TreeViewer treeViewer){
            treeViewer.getTree().addSelectionListener(this);
        }
        
        /**
         * Keeps track of structural changes of relations of the current product component generation.
         * {@inheritDoc}
         */
        public void contentsChangedAndWidgetIsNotDisposed(ContentChangeEvent event) {
            //the generationDirty flag is only necessary because of a buggy behaviour when the
            //the generation of the product component editor has changed. The pages a created newly 
            //in this case and I don't exactly what then happens... (pk). It desperately asks for refactoring
            if (!event.getIpsSrcFile().equals(LinksSection.this.generation.getIpsObject().getIpsSrcFile())) {
                return;
            }
            try {
                IIpsObject obj = event.getIpsSrcFile().getIpsObject();
                if (obj == null || obj.getIpsObjectType() != IpsObjectType.PRODUCT_CMPT) {
                    return;
                }
                
                IProductCmpt cmpt = (IProductCmpt)obj;
                IIpsObjectGeneration gen = cmpt.getGenerationByEffectiveDate(LinksSection.this.generation.getValidFrom());
                if (LinksSection.this.generation.equals(gen)) {
                    generationDirty = true;
                }
            }
            catch (CoreException e) {
                IpsPlugin.log(e);
                generationDirty = true;
            }
            processRelationChanges(event);
        }

        private void processRelationChanges(ContentChangeEvent event){
            if (event.getEventType()==ContentChangeEvent.TYPE_PART_REMOVED
                    && event.containsAffectedObjects(IProductCmptLink.class)) {
                IProductCmptLink relation = (IProductCmptLink)event.getPart();
                treeViewer.refresh(relation.getAssociation());
                TreeItem possibleSelection = getNextPossibleItem(treeViewer.getTree(), lastSelectionPath);
                if(possibleSelection == null){
                    return;
                }
                treeViewer.getTree().setSelection(new TreeItem[]{possibleSelection});
                //this additional sending of an event is necessary due to a bug in swt. The problem is
                //that dispite of the new selection no event is triggered
                treeViewer.getTree().notifyListeners(SWT.Selection, null);
                return;
            }
        }
        
        /**
         * Empty implementation
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            // empty implementation
        }

        /*
         * Calculates the path within the tree that points to the provided TreeItem. The path is a list of indices.
         * The first index in the list is meant to be the index of the TreeItem starting from the root of the tree,
         * the second is the index of the TreeItem starting from the TreeItem calculated before and so on.
         */
        private void createSelectionPath(List path, TreeItem item){
            TreeItem parent = item.getParentItem();
            if(parent == null){
                path.add(new Integer(item.getParent().indexOf(item)));
                return;
            }
            createSelectionPath(path, parent);
            path.add(new Integer(parent.indexOf(item)));
        }
        
        /*
         * Calculates the next possible selection for the provided path. The assumption is that the item the path is targeting at
         * might not exist anymore therefor an item has to be determined that is preferably near to it.
         */
        private TreeItem getNextPossibleItem(Tree tree, List pathList){
            
            if(pathList == null || tree.getItemCount() == 0 || pathList.size() == 0){
                return null;
            }
            TreeItem parent = null;
            Integer index = (Integer)pathList.get(0);
            if(tree.getItemCount() > index.intValue()){
                parent = tree.getItem(((Integer)pathList.get(0)).intValue());
            }
            else{
                parent = tree.getItem(tree.getItemCount() - 1);
            }
            for (int i = 1; i < pathList.size(); i++) {
                if(parent.getItemCount() == 0){
                    return parent;
                }
                index = (Integer)pathList.get(i);
                if(parent.getItemCount() > index.intValue()){
                    TreeItem item = parent.getItem(index.intValue());
                    if(item.getItemCount() == 0){
                        return item;
                    }
                    parent = item;
                    continue;
                }
                return parent.getItem(parent.getItemCount() - 1);
            }
            return parent;
        }
        
        /**
         * Calculates the path starting from the tree root to the currently selected item. The path is kept in the
         * lastSelectionPath member variable.
         */ 
        public void widgetSelected(SelectionEvent e) {
            
            if(e.item != null){
                TreeItem item = (TreeItem)e.item;
                lastSelectionPath = new ArrayList();
                createSelectionPath(lastSelectionPath, item);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public ISelectionProvider getSelectionProvider() {
        return treeViewer;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActivated() {
        if(treeViewer == null){
            return false;
        }
        return treeViewer.getTree().isFocusControl();
    }
}
