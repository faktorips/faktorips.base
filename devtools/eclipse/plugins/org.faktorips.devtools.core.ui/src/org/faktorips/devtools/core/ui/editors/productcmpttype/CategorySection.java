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

import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.SectionEditField;
import org.faktorips.devtools.core.ui.dialogs.DialogMementoHelper;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.core.ui.editors.productcmpttype.CategoryPage.CategoryCompositionSection;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;

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
        return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(category);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        setLayout(client);

        viewerButtonComposite = new CategoryComposite(category, contextType, this, client, toolkit);

        getBindingContext().add(new ControlPropertyBinding(getSectionControl(), category,
                IProductCmptCategory.PROPERTY_NAME, String.class) {
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

    ViewerButtonComposite getViewerButtonComposite() {
        return viewerButtonComposite;
    }

    private void updateToolBarEnabledStates() {
        moveUpAction.setEnabled(
                isContextTypeEditable() && !contextType.isFirstCategory(category) && contextType.isDefining(category));
        moveDownAction.setEnabled(
                isContextTypeEditable() && !contextType.isLastCategory(category) && contextType.isDefining(category));
        moveLeftAction.setEnabled(isContextTypeEditable() && contextType.isDefining(category));
        moveRightAction.setEnabled(isContextTypeEditable() && contextType.isDefining(category));

        editAction.setEnabled(contextType.isDefining(category));
        deleteAction.setEnabled(isContextTypeEditable() && contextType.isDefining(category));
    }

    boolean isContextTypeEditable() {
        return IpsUIPlugin.isEditable(contextType.getIpsSrcFile());
    }

    CategoryCompositionSection getCategoryCompositionSection() {
        return categoryCompositionSection;
    }

    /**
     * Abstract base class for all actions within the {@link CategorySection}.
     */
    private abstract static class CategoryAction extends Action {

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

    /**
     * Abstract base class for all move-related actions within the {@link CategorySection}.
     */
    private abstract static class MoveCategoryAction extends CategoryAction {

        private MoveCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        /**
         * <strong>Subclassing:</strong><br>
         * This implementation calls {@link #move()} to perform the move operation. If true is
         * returned by {@link #move()},
         * {@link CategoryCompositionSection#recreateCategorySections(IProductCmptCategory)} is
         * invoked.
         */
        @Override
        public void run() {
            boolean moved = move();
            if (moved) {
                getCategoryCompositionSection().recreateCategorySections(getCategory());
            }
        }

        /**
         * Performs the move operation and returns whether a move has been performed.
         */
        protected abstract boolean move();

    }

    /**
     * {@link Action} to move an {@link IProductCmptCategory} up by one position.
     */
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

    /**
     * {@link Action} to move an {@link IProductCmptCategory} down by one position.
     */
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

    /**
     * {@link Action} to move an {@link IProductCmptCategory} to {@link Position#LEFT}.
     */
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

    /**
     * {@link Action} to move an {@link IProductCmptCategory} to {@link Position#RIGHT}.
     */
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

    /**
     * {@link Action} to delete an {@link IProductCmptCategory}.
     */
    private static class DeleteCategoryAction extends CategoryAction {

        private static final String IMAGE_FILENAME = "elcl16/trash.gif"; //$NON-NLS-1$

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

    /**
     * {@link Action} to open up a {@link CategoryEditDialog}.
     */
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
                    CategoryEditDialog categoryEditDialog = new CategoryEditDialog(getCategory(), getShell());
                    categoryEditDialog.setDataChangeable(IpsUIPlugin.isEditable(getProductCmptType().getIpsSrcFile()));
                    return categoryEditDialog;
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
