/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import static org.faktorips.devtools.model.internal.productcmpttype.ProductCmptCategory.isOverriding;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dnd.IpsByteArrayTransfer;
import org.faktorips.devtools.core.ui.editors.IpsObjectPartChangeRefreshHelper;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.util.ArgumentCheck;

/**
 * A {@link ViewerButtonComposite} that shows the product component properties assigned to the
 * {@link IProductCmptCategory} represented by the section.
 * <p>
 * This composite provides the controls that allow the user to edit the ordering of properties
 * within categories and to edit the assignment of properties to categories.
 */
class CategoryComposite extends ViewerButtonComposite {

    private final ContentsChangeListener policySideChangedListener;

    private final IProductCmptCategory category;

    private final IProductCmptType contextType;

    private final CategorySection categorySection;

    private Button moveUpButton;

    private Button moveDownButton;

    private Button changeCategoryButton;

    private CategoryComposite.PropertyContentProvider contentProvider;

    /**
     * @param category the {@link IProductCmptCategory} that is represented by this composite
     * @param contextType the {@link IProductCmptType} that is being edited
     * @param categorySection the parent {@link CategorySection}, this composite is a part of
     */
    public CategoryComposite(IProductCmptCategory category, IProductCmptType contextType,
            CategorySection categorySection, Composite parent, UIToolkit toolkit) {

        super(parent);

        this.category = category;
        this.contextType = contextType;
        this.categorySection = categorySection;

        initControls(toolkit);

        policySideChangedListener = createPolicySideChangedListener(contextType);
        addContentsChangeListener(policySideChangedListener);
        addDisposeListener();
    }

    private void addContentsChangeListener(ContentsChangeListener listener) {
        contextType.getIpsModel().addChangeListener(listener);
    }

    private void addDisposeListener() {
        addDisposeListener($ -> contextType.getIpsModel().removeChangeListener(policySideChangedListener));
    }

    /**
     * Creates a {@link ContentsChangeListener} that refreshes this section if the
     * {@link IPolicyCmptType} configured by the context {@link IProductCmptType} has changed.
     * <p>
     * The reason for this listener is the <em>whole content changed</em> event that is fired when
     * changes done to an {@link IIpsSrcFile} are discarded. The section needs to react to this
     * event accordingly by refreshing itself.
     */
    private ContentsChangeListener createPolicySideChangedListener(final IProductCmptType contextType) {
        return event -> {
            if (event.isAffected(contextType.findPolicyCmptType(contextType.getIpsProject()))) {
                refresh();
            }
        };
    }

    @Override
    protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
        StructuredViewer viewer = new TableViewer(toolkit.createTable(parent, SWT.NONE));

        setLabelProvider(viewer);
        setContentProvider(viewer);
        addDoubleClickChangeCategoryListener(viewer);
        addDragSupport(viewer);
        addDropSupport(viewer);

        viewer.setInput(category);

        IpsObjectPartChangeRefreshHelper.createAndInit(category.getIpsObject(), viewer);

        return viewer;
    }

    private void setLabelProvider(StructuredViewer viewer) {
        DecoratingStyledCellLabelProvider decoratedLabelProvider = new DecoratingStyledCellLabelProvider(
                new PropertyLabelProvider(), null, null);
        viewer.setLabelProvider(decoratedLabelProvider);
    }

    private void setContentProvider(StructuredViewer viewer) {
        contentProvider = new PropertyContentProvider(contextType, category);
        viewer.setContentProvider(contentProvider);
    }

    private void addDoubleClickChangeCategoryListener(StructuredViewer viewer) {
        viewer.getControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                openChangeCategoryDialog();
            }
        });
    }

    private void addDragSupport(StructuredViewer viewer) {
        viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { ProductCmptPropertyTransfer.getInstance() },
                new PropertyDragListener());
    }

    private void addDropSupport(StructuredViewer viewer) {
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
        moveUpButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
        moveUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveParts(true);
            }
        });
    }

    private void createMoveDownButton(Composite buttonComposite, UIToolkit toolkit) {
        moveDownButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonDown);
        moveDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
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
        } catch (IpsException e) {
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
        List<Object> elements = Arrays.asList(contentProvider.getElements(category));
        IStructuredSelection selection = (IStructuredSelection)getViewer().getSelection();
        int[] selectionIndices = new int[selection.size()];
        for (int i = 0; i < selection.size(); i++) {
            selectionIndices[i] = elements.indexOf(selection.toArray()[i]);
        }
        return selectionIndices;
    }

    private void setSelection(int[] selectionIndices) {
        getViewer().setSelection(createSelection(selectionIndices), true);
        getControl().setFocus();
        refresh();
    }

    private IStructuredSelection createSelection(int[] selectionIndices) {
        List<Object> itemsToSelect = new ArrayList<>(selectionIndices.length);
        for (int index : selectionIndices) {
            itemsToSelect.add(contentProvider.getElements(category)[index]);
        }
        return new StructuredSelection(itemsToSelect);
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
            CategorySection targetCategorySection = categorySection.getCategoryCompositionSection().getCategorySection(
                    selectedCategory);
            targetCategorySection.getViewerButtonComposite().setSelectedObject(selectedProperty);
            refresh();
            targetCategorySection.refresh();

            // Hand the focus to the target category section
            targetCategorySection.getViewerButtonComposite().setFocus();
        }
    }

    @Override
    protected void updateButtonEnabledStates() {
        boolean isMovable = categorySection.isContextTypeEditable()
                && isPropertySelected()
                && isPropertyOfContextTypeSelected()
                && !isOverriding(getSelectedProperty());
        moveUpButton.setEnabled(isMovable && !isFirstLocalPropertyOfContextTypeSelected());
        moveDownButton.setEnabled(isMovable && !isLastPropertyOfContextTypeSelected());
        changeCategoryButton.setEnabled(isSelectedPropertyAllowedToChangeCategory());
    }

    private boolean isFirstLocalPropertyOfContextTypeSelected() {
        return getSelectedObject().equals(getFirstLocalPropertyOfContextType());
    }

    private IProductCmptProperty getFirstLocalPropertyOfContextType() {
        for (Object o : contentProvider.getElements(null)) {
            if (o instanceof IProductCmptProperty p
                    && isPropertyOfContextType(p)
                    && !isOverriding(p)) {
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
                || isPropertyOfPolicyCmptTypeConfiguredByContextType(property);
    }

    private boolean isPropertyOfPolicyCmptTypeConfiguredByContextType(IProductCmptProperty property) {
        return contextType.isConfigurationForPolicyCmptType()
                && Objects.equals(contextType.findPolicyCmptType(contextType.getIpsProject()).getProductCmptType(),
                        contextType.getQualifiedName())
                && property.isOfType(new QualifiedNameType(contextType.getPolicyCmptType(),
                        IpsObjectType.POLICY_CMPT_TYPE));
    }

    private IProductCmptProperty getSelectedProperty() {
        return (IProductCmptProperty)getSelectedObject();
    }

    private boolean isPropertySelected() {
        return !getViewer().getSelection().isEmpty();
    }

    private boolean isTypeOfSelectedPropertyEditable() {
        ArgumentCheck.isTrue(isPropertySelected());
        return IpsUIPlugin.isEditable(getSelectedProperty().getIpsSrcFile());
    }

    private boolean isSelectedPropertyAllowedToChangeCategory() {
        return isPropertySelected() && isPropertyOfContextTypeSelected() && isTypeOfSelectedPropertyEditable();
    }

    private Control getControl() {
        return getViewer().getControl();
    }

    private class PropertyLabelProvider extends LocalizedLabelProvider implements IStyledLabelProvider {
        @Override
        public Image getImage(Object element) {
            if (!(element instanceof IProductCmptProperty property)) {
                return super.getImage(element);
            }

            Image baseImage = getImageForPropertyOfContextType(property);
            return isPropertyOfContextType(property) ? baseImage : getImageForPropertyOfSupertypeHierarchy(property);
        }

        /**
         * Returns the {@link Image} to be used if the provided {@link IProductCmptProperty} belongs
         * to the edited {@link IProductCmptType} itself.
         * <p>
         * This is the default {@link Image} of the corresponding {@link IPropertyValue}.
         *
         * @see ImageHandling#getDefaultImage(Class)
         */
        private Image getImageForPropertyOfContextType(IProductCmptProperty property) {
            return IpsUIPlugin.getImageHandling().getImage(property);
        }

        /**
         * Returns the {@link Image} to be used if the provided {@link IProductCmptProperty}
         * originates from the supertype hierarchy.
         * <p>
         * This is the default {@link Image} of the corresponding {@link IPropertyValue} with an
         * additional 'override' overlay.
         */
        private Image getImageForPropertyOfSupertypeHierarchy(IProductCmptProperty property) {
            Image baseImage = IpsUIPlugin.getImageHandling().getImage(property);
            ImageDescriptor imageDescriptor = new DecorationOverlayIcon(baseImage, OverlayIcons.OVERRIDE_OVR_DESC,
                    IDecoration.BOTTOM_RIGHT);
            return IpsUIPlugin.getImageHandling().getImage(imageDescriptor);
        }

        @Override
        public StyledString getStyledText(Object element) {
            StyledString styledString = new StyledString();
            IProductCmptProperty property = (IProductCmptProperty)element;
            if (!isPropertyOfContextType(property)) {
                styledString.append(getText(element), StyledString.QUALIFIER_STYLER);
            } else {
                styledString.append(getText(element));
            }
            return styledString;
        }
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
            List<IProductCmptProperty> properties = new ArrayList<>();
            try {
                properties.addAll(category.findProductCmptProperties(contextType, true, contextType.getIpsProject()));
            } catch (IpsException e) {
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

    private class PropertyDragListener implements DragSourceListener {

        @Override
        public void dragStart(DragSourceEvent event) {
            event.doit = isSelectedPropertyAllowedToChangeCategory();
        }

        @Override
        public void dragSetData(DragSourceEvent event) {
            event.data = new IProductCmptProperty[] { getSelectedProperty() };
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
            // Cannot drop on properties from the supertype hierarchy
            if ((getCurrentLocation() == LOCATION_ON)
                    || (getTargetProperty() != null && !isPropertyOfContextType(getTargetProperty()))) {
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
            } catch (IpsException e) {
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

        /*
         * Overridden to activate LOCATION_BEFORE and LOCATION_AFTER when the cursor's real location
         * is LOCATION_ON.
         */
        @Override
        protected int determineLocation(DropTargetEvent event) {
            if (!(event.item instanceof Item item)) {
                return LOCATION_NONE;
            }

            Rectangle bounds = getBounds(item);
            if (bounds == null) {
                return LOCATION_NONE;
            }

            /*
             * When the mouse is on an item, return LOCATION_BEFORE or LOCATION_AFTER instead,
             * depending on the distance to the respective location.
             */
            if (isLocationBefore(event)) {
                return LOCATION_BEFORE;
            } else if (isLocationAfter(event)) {
                return LOCATION_AFTER;
            } else {
                return LOCATION_ON;
            }
        }

        private boolean isLocationBefore(DropTargetEvent event) {
            Rectangle bounds = getBounds((Item)event.item);
            if (bounds == null) {
                return false;
            }
            Point coordinates = getControl().toControl(new Point(event.x, event.y));
            return (coordinates.y - bounds.y) < bounds.height / 2;
        }

        private boolean isLocationAfter(DropTargetEvent event) {
            Rectangle bounds = getBounds((Item)event.item);
            if (bounds == null) {
                return false;
            }
            Point coordinates = getControl().toControl(new Point(event.x, event.y));
            return (bounds.y + bounds.height - coordinates.y) < bounds.height / 2;
        }

        @Override
        public void dragOver(DropTargetEvent event) {
            super.dragOver(event);
            if (isLocationBefore(event)) {
                event.feedback = DND.FEEDBACK_INSERT_BEFORE;
            } else if (isLocationAfter(event)) {
                event.feedback = DND.FEEDBACK_INSERT_AFTER;
            }
        }

    }

    private static class ProductCmptPropertyTransfer extends IpsByteArrayTransfer<IProductCmptProperty> {

        private static final String TYPE_NAME = "ProductCmptProperty"; //$NON-NLS-1$

        private static final int TYPE_ID = registerType(TYPE_NAME);

        private static final CategoryComposite.ProductCmptPropertyTransfer INSTANCE = new ProductCmptPropertyTransfer();

        private ProductCmptPropertyTransfer() {
            super(IProductCmptProperty.class);
        }

        private static CategoryComposite.ProductCmptPropertyTransfer getInstance() {
            return INSTANCE;
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
            IIpsProject ipsProject = IIpsModel.get().getIpsProject(projectName);
            IType type = (IType)ipsProject.findIpsObject(typeObjectType, typeQualifiedName);
            IProductCmptProperty property = null;
            for (IIpsElement child : type.getChildren()) {
                if (!(child instanceof IProductCmptProperty potentialProperty)) {
                    continue;
                }
                if (partId.equals(potentialProperty.getId())) {
                    property = potentialProperty;
                    break;
                }
            }
            return property;
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
