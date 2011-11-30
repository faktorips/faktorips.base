/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.SectionEditField;
import org.faktorips.devtools.core.ui.dialogs.DialogMementoHelper;
import org.faktorips.devtools.core.ui.dnd.IpsByteArrayTransfer;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.core.ui.editors.productcmpttype.CategoryPage.CategoryCompositionSection;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * A section allowing the user to edit an {@link IProductCmptCategory}.
 * <p>
 * By means of this section, the user is able to change the order of the product component
 * properties assigned to the {@link IProductCmptCategory} that is being edited. This can be done
 * via 'Up' and 'Down' buttons, or via drag and drop. Furthermore, the {@link IProductCmptCategory}
 * itself can be moved up, down, left or right.
 * <p>
 * A separate dialog enables the user to change the properties of an {@link IProductCmptCategory},
 * for example it's name and whether the {@link IProductCmptCategory} is marked as default for a
 * specific {@link ProductCmptPropertyType}.
 * <p>
 * Yet another dialog allows the user to change the {@link IProductCmptCategory} of an
 * {@link IProductCmptProperty}. This can also be achieved via drag and drop.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IProductCmptCategory
 * @see IProductCmptProperty
 * @see ProductCmptPropertyType
 */
public class CategorySection extends IpsSection {

    private final IProductCmptCategory category;

    private final IProductCmptType contextType;

    private final CategoryCompositionSection categoryCompositionSection;

    private final IAction moveUpAction;

    private final IAction moveDownAction;

    private final IAction moveLeftAction;

    private final IAction moveRightAction;

    private final IAction deleteAction;

    private final IAction editAction;

    private ViewerButtonComposite viewerButtonComposite;

    /**
     * @param category the {@link IProductCmptCategory} that is represented by this section
     * @param contextType the {@link IProductCmptType} that is being edited
     * @param categoryCompositionSection the parent {@link CategoryCompositionSection}, this section
     *            is a part of
     */
    public CategorySection(IProductCmptCategory category, IProductCmptType contextType,
            CategoryCompositionSection categoryCompositionSection, Composite parent, UIToolkit toolkit) {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        this.category = category;
        this.contextType = contextType;
        this.categoryCompositionSection = categoryCompositionSection;

        moveUpAction = new MoveCategoryUpAction(contextType, category, categoryCompositionSection);
        moveDownAction = new MoveCategoryDownAction(contextType, category, categoryCompositionSection);
        moveLeftAction = new MoveCategoryLeftAction(contextType, category, categoryCompositionSection);
        moveRightAction = new MoveCategoryRightAction(contextType, category, categoryCompositionSection);
        deleteAction = new DeleteCategoryAction(contextType, category, categoryCompositionSection);
        editAction = new EditCategoryAction(contextType, category, categoryCompositionSection);

        initControls();
    }

    @Override
    protected String getSectionTitle() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        setLayout(client);

        viewerButtonComposite = new CategoryComposite(category, contextType, categoryCompositionSection, client,
                toolkit);

        getBindingContext().add(
                new ControlPropertyBinding(getSectionControl(), category, IProductCmptCategory.PROPERTY_NAME,
                        String.class) {
                    @Override
                    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                        updateSectionTitle();
                    }
                });
        SectionEditField sectionEditField = new SectionEditField(getSectionControl());
        getBindingContext().bindProblemMarker(sectionEditField, category, IProductCmptCategory.PROPERTY_NAME);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    private void setLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        parent.setLayout(layout);
    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        addMoveActions(toolBarManager);
        toolBarManager.add(new Separator());
        toolBarManager.add(editAction);
        toolBarManager.add(deleteAction);
    }

    private void addMoveActions(IToolBarManager toolBarManager) {
        if (category.isAtRightPosition()) {
            toolBarManager.add(moveLeftAction);
        }

        toolBarManager.add(moveUpAction);
        toolBarManager.add(moveDownAction);

        if (category.isAtLeftPosition()) {
            toolBarManager.add(moveRightAction);
        }
    }

    @Override
    protected void performRefresh() {
        viewerButtonComposite.refresh();
        updateToolBarEnabledStates();
    }

    private ViewerButtonComposite getViewerButtonComposite() {
        return viewerButtonComposite;
    }

    private void updateToolBarEnabledStates() {
        moveUpAction.setEnabled(!contextType.isFirstCategory(category) && contextType.isDefining(category));
        moveDownAction.setEnabled(!contextType.isLastCategory(category) && contextType.isDefining(category));
        moveLeftAction.setEnabled(contextType.isDefining(category));
        moveRightAction.setEnabled(contextType.isDefining(category));

        editAction.setEnabled(contextType.isDefining(category));
        deleteAction.setEnabled(contextType.isDefining(category));
    }

    /**
     * A {@link ViewerButtonComposite} that shows the product component properties assigned to the
     * {@link IProductCmptCategory} represented by the section.
     * <p>
     * This composite provides the controls that allow the user to edit the ordering of properties
     * within categories and to edit the assignment of properties to categories.
     */
    private static class CategoryComposite extends ViewerButtonComposite {

        private final ContentsChangeListener policySideChangedListener;

        private final IProductCmptCategory category;

        private final IProductCmptType contextType;

        private final CategoryCompositionSection categoryCompositionSection;

        private Button moveUpButton;

        private Button moveDownButton;

        private Button changeCategoryButton;

        private PropertyContentProvider contentProvider;

        /**
         * @param category the {@link IProductCmptCategory} that is represented by this composite
         * @param contextType the {@link IProductCmptType} that is being edited
         * @param categoryCompositionSection the parent {@link CategoryCompositionSection}, this
         *            composite is a part of
         */
        public CategoryComposite(IProductCmptCategory category, IProductCmptType contextType,
                CategoryCompositionSection categoryCompositionSection, Composite parent, UIToolkit toolkit) {

            super(parent);

            this.category = category;
            this.contextType = contextType;
            this.categoryCompositionSection = categoryCompositionSection;

            initControls(toolkit);

            policySideChangedListener = createPolicySideChangedListener(contextType);
            addContentsChangeListener(policySideChangedListener);
            addDisposeListener();
        }

        private void addContentsChangeListener(ContentsChangeListener listener) {
            contextType.getIpsModel().addChangeListener(listener);
        }

        private void addDisposeListener() {
            addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    contextType.getIpsModel().removeChangeListener(policySideChangedListener);
                }
            });
        }

        /**
         * Creates a {@link ContentsChangeListener} that refreshes this section if the
         * {@link IPolicyCmptType} configured by the context {@link IProductCmptType} has changed.
         * <p>
         * The reason for this listener is the <em>whole content changed</em> event that is fired
         * when changes done to an {@link IIpsSrcFile} are discarded. The section needs to react to
         * this event accordingly by refreshing itself.
         */
        private ContentsChangeListener createPolicySideChangedListener(final IProductCmptType contextType) {
            return new ContentsChangeListener() {
                @Override
                public void contentsChanged(ContentChangeEvent event) {
                    try {
                        if (event.isAffected(contextType.findPolicyCmptType(contextType.getIpsProject()))) {
                            refresh();
                        }
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            };
        }

        @Override
        protected void refreshThis() {
            updateTreeItemColors();
        }

        /**
         * Sets the foreground {@link Color} of all tree items representing properties from the
         * supertype hierarchy to {@link JFacePreferences#QUALIFIER_COLOR}.
         */
        private void updateTreeItemColors() {
            for (TreeItem item : getTree().getItems()) {
                IProductCmptProperty property = (IProductCmptProperty)item.getData();
                if (!isPropertyOfContextType(property)) {
                    item.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR));
                }
            }
        }

        @Override
        protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
            TreeViewer viewer = new TreeViewer(toolkit.getFormToolkit().createTree(parent, SWT.NONE));

            setLabelProvider(viewer);
            setContentProvider(viewer);
            addDoubleClickChangeCategoryListener(viewer);
            addDragSupport(viewer);
            addDropSupport(viewer);

            viewer.setInput(category);

            return viewer;
        }

        private void setLabelProvider(TreeViewer viewer) {
            viewer.setLabelProvider(new LocalizedLabelProvider() {
                @Override
                public Image getImage(Object element) {
                    if (!(element instanceof IProductCmptProperty)) {
                        return super.getImage(element);
                    }

                    IProductCmptProperty property = (IProductCmptProperty)element;
                    Image baseImage = getImageForPropertyOfContextType(property);
                    return isPropertyOfContextType(property) ? baseImage
                            : getImageForPropertyOfSupertypeHierarchy(property);
                }

                /**
                 * Returns the {@link Image} to be used if the provided {@link IProductCmptProperty}
                 * belongs to the edited {@link IProductCmptType} itself.
                 * <p>
                 * This is the default {@link Image} of the corresponding {@link IPropertyValue}.
                 * 
                 * @see ImageHandling#getDefaultImage(Class)
                 */
                private Image getImageForPropertyOfContextType(IProductCmptProperty property) {
                    return IpsUIPlugin.getImageHandling().getDefaultImage(
                            property.getProductCmptPropertyType().getValueImplementationClass());
                }

                /**
                 * Returns the {@link Image} to be used if the provided {@link IProductCmptProperty}
                 * originates from the supertype hierarchy.
                 * <p>
                 * This is the disabled version of the default {@link Image} of the corresponding
                 * {@link IPropertyValue} with an additional 'override' overlay.
                 */
                private Image getImageForPropertyOfSupertypeHierarchy(IProductCmptProperty property) {
                    Image baseImage = IpsUIPlugin.getImageHandling().getDefaultImage(
                            property.getProductCmptPropertyType().getValueImplementationClass());
                    ImageDescriptor imageDescriptor = new DecorationOverlayIcon(baseImage,
                            OverlayIcons.OVERRIDE_OVR_DESC, IDecoration.BOTTOM_RIGHT);
                    return IpsUIPlugin.getImageHandling().getDisabledSharedImage(imageDescriptor);
                }
            });
        }

        private void setContentProvider(TreeViewer viewer) {
            contentProvider = new PropertyContentProvider(contextType, category);
            viewer.setContentProvider(contentProvider);
        }

        private void addDoubleClickChangeCategoryListener(TreeViewer viewer) {
            viewer.getTree().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    openChangeCategoryDialog();
                }
            });
        }

        private void addDragSupport(final TreeViewer viewer) {
            viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { ProductCmptPropertyTransfer.getInstance() },
                    new PropertyDragListener(this));
        }

        private void addDropSupport(TreeViewer viewer) {
            viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { ProductCmptPropertyTransfer.getInstance() },
                    new PropertyDropAdapter(viewer));
        }

        @Override
        protected boolean createButtons(Composite buttonComposite, UIToolkit toolkit) {
            createMoveUpButton(buttonComposite, toolkit);
            createMoveDownButton(buttonComposite, toolkit);
            createChangeCategoryButton(buttonComposite, toolkit);
            return true;
        }

        private void createMoveUpButton(Composite buttonComposite, UIToolkit toolkit) {
            moveUpButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonUp);
            moveUpButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            moveUpButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveParts(true);
                }
            });
        }

        private void createMoveDownButton(Composite buttonComposite, UIToolkit toolkit) {
            moveDownButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonDown);
            moveDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            moveDownButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveParts(false);
                }
            });
        }

        private void moveParts(boolean up) {
            int[] selectionIndices = getSelectionIndices();
            int[] newSelectionIndices = Arrays.copyOf(selectionIndices, selectionIndices.length);
            int[] moveIndices = getSelectionIndicesMinusSupertypePropertyOffset(selectionIndices);
            try {
                int[] newIndices = category.moveProductCmptProperties(moveIndices, up, contextType);
                newSelectionIndices = getSelectionindicesPlusSupertypePropertyOffset(newIndices);
            } catch (CoreException e) {
                // The elements could not be moved so the new selection equals the old selection
                IpsPlugin.log(e);
                newSelectionIndices = Arrays.copyOf(selectionIndices, selectionIndices.length);
            }
            setSelection(newSelectionIndices);
        }

        private int[] getSelectionindicesPlusSupertypePropertyOffset(int[] selectionIndices) {
            int[] result = Arrays.copyOf(selectionIndices, selectionIndices.length);
            for (int i = 0; i < selectionIndices.length; i++) {
                result[i] += getNumberOfPropertiesFromSupertypeHierarchy();
            }
            return result;
        }

        private int[] getSelectionIndicesMinusSupertypePropertyOffset(int[] selectionIndices) {
            int[] result = Arrays.copyOf(selectionIndices, selectionIndices.length);
            for (int i = 0; i < selectionIndices.length; i++) {
                result[i] -= getNumberOfPropertiesFromSupertypeHierarchy();
            }
            return result;
        }

        private int[] getSelectionIndices() {
            TreeItem[] selection = getTree().getSelection();
            int[] selectionIndices = new int[selection.length];
            for (int i = 0; i < selection.length; i++) {
                selectionIndices[i] = getTree().indexOf(selection[i]);
            }
            return selectionIndices;
        }

        private void setSelection(int[] selectionIndices) {
            for (int index : selectionIndices) {
                getTree().select(getTree().getItem(index));
            }
            getTree().setFocus();
            refresh();
        }

        private void createChangeCategoryButton(Composite buttonComposite, UIToolkit toolkit) {
            changeCategoryButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonChangeCategory);
            changeCategoryButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            changeCategoryButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openChangeCategoryDialog();
                }
            });
        }

        private void openChangeCategoryDialog() {
            if (!isPropertySelected() || !isPropertyOfContextTypeSelected()) {
                return;
            }

            IProductCmptProperty selectedProperty = getSelectedProperty();
            ChangeCategoryDialog dialog = new ChangeCategoryDialog(contextType, selectedProperty, category, getShell());
            int returnCode = dialog.open();
            if (returnCode == Window.OK) {
                // Set the selection to the selected property in the target category
                IProductCmptCategory selectedCategory = dialog.getSelectedCategory();
                CategorySection targetCategorySection = categoryCompositionSection.getCategorySection(selectedCategory);
                targetCategorySection.getViewerButtonComposite().setSelectedObject(selectedProperty);

                // Hand the focus to the target category section
                targetCategorySection.getViewerButtonComposite().setFocus();
            }
        }

        @Override
        protected void updateButtonEnabledStates() {
            moveUpButton.setEnabled(isPropertySelected() && !isFirstPropertyOfContextTypeSelected()
                    && isPropertyOfContextTypeSelected());
            moveDownButton.setEnabled(isPropertySelected() && !isLastPropertyOfContextTypeSelected()
                    && isPropertyOfContextTypeSelected());
            changeCategoryButton.setEnabled(isPropertySelected() && isPropertyOfContextTypeSelected());
        }

        private boolean isFirstPropertyOfContextTypeSelected() {
            return getSelectedObject().equals(getFirstPropertyOfContextType());
        }

        private IProductCmptProperty getFirstPropertyOfContextType() {
            for (Object o : contentProvider.getElements(null)) {
                if (isPropertyOfContextType((IProductCmptProperty)o)) {
                    return (IProductCmptProperty)o;
                }
            }
            return null;
        }

        private int getNumberOfPropertiesFromSupertypeHierarchy() {
            int numberPropertiesFromSupertypeHierarchy = 0;
            for (Object o : contentProvider.getElements(null)) {
                if (!isPropertyOfContextType((IProductCmptProperty)o)) {
                    numberPropertiesFromSupertypeHierarchy++;
                }
            }
            return numberPropertiesFromSupertypeHierarchy;

        }

        private boolean isLastPropertyOfContextTypeSelected() {
            return isLastElementSelected();
        }

        private boolean isPropertyOfContextTypeSelected() {
            return isPropertyOfContextType(getSelectedProperty());
        }

        private boolean isPropertyOfContextType(IProductCmptProperty property) {
            return property.isOfType(contextType.getQualifiedNameType())
                    || property.isOfType(new QualifiedNameType(contextType.getPolicyCmptType(),
                            IpsObjectType.POLICY_CMPT_TYPE));
        }

        private IProductCmptProperty getSelectedProperty() {
            return (IProductCmptProperty)getSelectedObject();
        }

        private boolean isPropertySelected() {
            return getTree().getSelectionCount() > 0;
        }

        private Tree getTree() {
            return getTreeViewer().getTree();
        }

        private TreeViewer getTreeViewer() {
            return (TreeViewer)getViewer();
        }

        private static class PropertyContentProvider implements ITreeContentProvider {

            private final IProductCmptType contextType;

            private final IProductCmptCategory category;

            private PropertyContentProvider(IProductCmptType contextType, IProductCmptCategory category) {
                this.contextType = contextType;
                this.category = category;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();
                try {
                    properties
                            .addAll(category.findProductCmptProperties(contextType, true, contextType.getIpsProject()));
                } catch (CoreException e) {
                    // Recover by not displaying any properties
                    IpsPlugin.log(e);
                }
                return properties.toArray();
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // Nothing to do
            }

            @Override
            public void dispose() {
                // Nothing to do
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                // Not really a tree, there are never any children
                return new Object[0];
            }

            @Override
            public Object getParent(Object element) {
                // As there are no children, there are no parents as well
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                // Not really a tree, there are never any children
                return false;
            }

        }

        private static class PropertyDragListener implements DragSourceListener {

            private final CategoryComposite categoryComposite;

            private PropertyDragListener(CategoryComposite categoryComposite) {
                this.categoryComposite = categoryComposite;
            }

            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = categoryComposite.isPropertySelected()
                        && categoryComposite.isPropertyOfContextTypeSelected();
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new IProductCmptProperty[] { categoryComposite.getSelectedProperty() };
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                // Nothing to do
            }

        }

        private class PropertyDropAdapter extends ViewerDropAdapter {

            private PropertyDropAdapter(Viewer viewer) {
                super(viewer);
            }

            @Override
            public boolean validateDrop(Object target, int operation, TransferData transferType) {
                // Cannot drop directly onto items
                if (getCurrentLocation() == LOCATION_ON) {
                    return false;
                }

                // Cannot drop on properties from the supertype hierarchy
                if (getTargetProperty() != null && !isPropertyOfContextType(getTargetProperty())) {
                    return false;
                }

                return ProductCmptPropertyTransfer.getInstance().isSupportedType(transferType);
            }

            @Override
            public boolean performDrop(Object data) {
                IProductCmptProperty droppedProperty = getDroppedProperty(data);
                try {
                    if (getCurrentLocation() == LOCATION_BEFORE) {
                        getTargetCategory().insertProductCmptProperty(droppedProperty, getTargetProperty(), true);
                    } else if (getCurrentLocation() == LOCATION_AFTER) {
                        getTargetCategory().insertProductCmptProperty(droppedProperty, getTargetProperty(), false);
                    } else if (getCurrentLocation() == LOCATION_NONE) {
                        getTargetCategory().insertProductCmptProperty(droppedProperty, null, false);
                    }
                } catch (CoreException e) {
                    return false;
                }

                setSelectedObject(droppedProperty);
                setFocus();

                return true;
            }

            private IProductCmptProperty getTargetProperty() {
                return (IProductCmptProperty)getCurrentTarget();
            }

            private IProductCmptCategory getTargetCategory() {
                return (IProductCmptCategory)getViewer().getInput();
            }

            private IProductCmptProperty getDroppedProperty(Object data) {
                return (IProductCmptProperty)((Object[])data)[0];
            }

            @Override
            protected int determineLocation(DropTargetEvent event) {
                if (!(event.item instanceof Item)) {
                    return LOCATION_NONE;
                }

                Item item = (Item)event.item;
                Rectangle bounds = getBounds(item);
                if (bounds == null) {
                    return LOCATION_NONE;
                }

                /*
                 * When the mouse is on an item, return LOCATION_BEFORE or LOCATION_AFTER instead,
                 * depending on the distance to the respective location.
                 */
                Point coordinates = getTree().toControl(new Point(event.x, event.y));
                if ((coordinates.y - bounds.y) < bounds.height / 2) {
                    return LOCATION_BEFORE;
                } else if ((bounds.y + bounds.height - coordinates.y) < bounds.height / 2) {
                    return LOCATION_AFTER;
                } else {
                    return LOCATION_ON;
                }
            }

        }

        private static class ProductCmptPropertyTransfer extends IpsByteArrayTransfer<IProductCmptProperty> {

            private static final String TYPE_NAME = "ProductCmptProperty"; //$NON-NLS-1$

            private static final int TYPE_ID = registerType(TYPE_NAME);

            private static final ProductCmptPropertyTransfer instance = new ProductCmptPropertyTransfer();

            private ProductCmptPropertyTransfer() {
                super(IProductCmptProperty.class);
            }

            private static ProductCmptPropertyTransfer getInstance() {
                return instance;
            }

            @Override
            protected void writeObject(IProductCmptProperty part, DataOutput output) {
                writeString(part.getIpsProject().getName(), output);
                writeString(part.getType().getQualifiedName(), output);
                writeString(part.getType().getIpsObjectType().getId(), output);
                writeString(part.getId(), output);
            }

            @Override
            protected IProductCmptProperty readObject(DataInput input) {
                String projectName = readString(input);
                String typeQualifiedName = readString(input);
                IpsObjectType typeObjectType = IpsObjectType.getTypeForName(readString(input));
                String partId = readString(input);

                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
                try {
                    IType type = (IType)ipsProject.findIpsObject(typeObjectType, typeQualifiedName);
                    IProductCmptProperty property = null;
                    for (IIpsElement child : type.getChildren()) {
                        if (!(child instanceof IProductCmptProperty)) {
                            continue;
                        }
                        IProductCmptProperty potentialProperty = (IProductCmptProperty)child;
                        if (partId.equals(potentialProperty.getId())) {
                            property = potentialProperty;
                            break;
                        }
                    }
                    return property;
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }

            @Override
            protected String[] getTypeNames() {
                return new String[] { TYPE_NAME };
            }

            @Override
            protected int[] getTypeIds() {
                return new int[] { TYPE_ID };
            }

        }

    }

    private static abstract class CategoryAction extends Action {

        private final IProductCmptType productCmptType;

        private final IProductCmptCategory category;

        private final CategoryCompositionSection categoryCompositionSection;

        private CategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            this.productCmptType = productCmptType;
            this.category = category;
            this.categoryCompositionSection = categoryCompositionSection;
        }

        protected IProductCmptType getProductCmptType() {
            return productCmptType;
        }

        protected IProductCmptCategory getCategory() {
            return category;
        }

        protected CategoryCompositionSection getCategoryCompositionSection() {
            return categoryCompositionSection;
        }

        protected Shell getShell() {
            return categoryCompositionSection.getShell();
        }

    }

    private static abstract class MoveCategoryAction extends CategoryAction {

        private MoveCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        @Override
        public void run() {
            boolean moved = move();
            if (moved) {
                getCategoryCompositionSection().recreateCategorySections(getCategory());
            }
        }

        protected abstract boolean move();

    }

    private static class MoveCategoryUpAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowUp.gif"; //$NON-NLS-1$

        private MoveCategoryUpAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryUpAction_label);
            setToolTipText(Messages.MoveCategoryUpAction_tooltip);
        }

        @Override
        protected boolean move() {
            return getProductCmptType().moveCategories(Arrays.asList(getCategory()), true);
        }

    }

    private static class MoveCategoryDownAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowDown.gif"; //$NON-NLS-1$

        private MoveCategoryDownAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryDownAction_label);
            setToolTipText(Messages.MoveCategoryDownAction_tooltip);
        }

        @Override
        protected boolean move() {
            return getProductCmptType().moveCategories(Arrays.asList(getCategory()), false);
        }

    }

    private static class MoveCategoryLeftAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowLeft.gif"; //$NON-NLS-1$

        private MoveCategoryLeftAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryLeftAction_label);
            setToolTipText(Messages.MoveCategoryLeftAction_tooltip);
        }

        @Override
        protected boolean move() {
            if (!getCategory().isAtLeftPosition()) {
                getCategory().setPosition(Position.LEFT);
                return true;
            }
            return false;
        }

    }

    private static class MoveCategoryRightAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowRight.gif"; //$NON-NLS-1$

        private MoveCategoryRightAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryRightAction_label);
            setToolTipText(Messages.MoveCategoryRightAction_tooltip);
        }

        @Override
        protected boolean move() {
            if (!getCategory().isAtRightPosition()) {
                getCategory().setPosition(Position.RIGHT);
                return true;
            }
            return false;
        }

    }

    private static class DeleteCategoryAction extends CategoryAction {

        private static final String IMAGE_FILENAME = "elcl16/Trash.gif"; //$NON-NLS-1$

        private DeleteCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.DeleteCategoryAction_label);
            setToolTipText(Messages.DeleteCategoryAction_tooltip);
        }

        @Override
        public void run() {
            boolean oldDeleted = getCategory().isDeleted();
            getCategory().delete();
            if (oldDeleted != getCategory().isDeleted()) {
                getCategoryCompositionSection().deleteCategorySection(getCategory());
            }
        }

    }

    private static class EditCategoryAction extends CategoryAction {

        private static final String IMAGE_FILENAME = "Edit.gif"; //$NON-NLS-1$

        private EditCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.EditCategoryAction_label);
            setToolTipText(Messages.EditCategoryAction_tooltip);
        }

        @Override
        public void run() {
            DialogMementoHelper dialogHelper = new DialogMementoHelper() {
                @Override
                protected Dialog createDialog() {
                    return new CategoryEditDialog(getCategory(), getShell());
                }
            };

            Position oldPosition = getCategory().getPosition();
            int returnCode = dialogHelper.openDialogWithMemento(getCategory());
            // Recreate the category sections if the category's position has changed (= move)
            if (returnCode == Window.OK && !oldPosition.equals(getCategory().getPosition())) {
                getCategoryCompositionSection().recreateCategorySections(getCategory());
            }
        }

    }

}
