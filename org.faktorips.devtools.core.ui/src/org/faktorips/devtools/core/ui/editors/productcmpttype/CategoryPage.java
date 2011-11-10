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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.DialogHelper;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.productcmpt.LinksSection;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Page that allows to edit the {@link IProductCmptCategory}s of an {@link IProductCmptType}.
 * <p>
 * The page provides a {@link CategorySection} for each {@link IProductCmptCategory}.
 * 
 * @author Alexander Weickmann
 */
public class CategoryPage extends IpsObjectEditorPage {

    public CategoryPage(ProductCmptTypeEditor editor) {
        super(
                editor,
                "org.faktorips.devtools.core.ui.editors.productcmpttype.CategoryPage", Messages.CategoryPage_tagPageName); //$NON-NLS-1$
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        super.createPageContent(formBody, toolkit);

        formBody.setLayout(createPageLayout(1, false));
        new CategoryCompositionSection(formBody, toolkit);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    class CategoryCompositionSection extends IpsSection {

        private final Map<IProductCmptCategory, CategorySection> categoriesToSections = new LinkedHashMap<IProductCmptCategory, CategorySection>();

        private final IAction newCategoryAction;

        private Composite left;

        private Composite right;

        private IpsSection linksCategorySection;

        public CategoryCompositionSection(Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

            newCategoryAction = new NewCategoryAction(getProductCmptType());

            initControls();
        }

        @Override
        protected String getSectionTitle() {
            return Messages.CategoryCompositionSection_sectionTitle;
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            setLayout(client);
            createLeftComposite(client);
            createRightComposite(client);
            createCategorySections();
            createLinksCategorySection();
            setInitialFocus();
        }

        private void setInitialFocus() {
            if (categoriesToSections.isEmpty()) {
                left.setFocus();
            } else {
                categoriesToSections.values().toArray(new CategorySection[categoriesToSections.size()])[0].setFocus();
            }
        }

        private void setLayout(Composite parent) {
            GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 1;
            layout.marginHeight = 2;
            parent.setLayout(layout);
        }

        private void createLeftComposite(Composite parent) {
            left = createColumnComposite(parent);
        }

        private void createRightComposite(Composite parent) {
            right = createColumnComposite(parent);
        }

        private Composite createColumnComposite(Composite parent) {
            return getToolkit().createGridComposite(parent, 1, true, true);
        }

        private void createCategorySections() {
            // Determine categories
            List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>();
            try {
                categories.addAll(getProductCmptType().findCategories(getProductCmptType().getIpsProject()));
            } catch (CoreException e) {
                // Recover by not displaying any categories
                IpsPlugin.log(e);
            }

            for (IProductCmptCategory category : categories) {
                Composite parent = category.isAtLeftPosition() ? left : right;
                CategorySection categorySection = new CategorySection(category, getProductCmptType(), this, parent,
                        getToolkit());
                categoriesToSections.put(category, categorySection);
            }
        }

        private void createLinksCategorySection() {
            linksCategorySection = new LinksCategorySection(right, getToolkit());
        }

        @Override
        protected void populateToolBar(IToolBarManager toolBarManager) {
            toolBarManager.add(newCategoryAction);
        }

        @Override
        protected void performRefresh() {
            for (CategorySection categorySection : categoriesToSections.values()) {
                categorySection.refresh();
            }
        }

        /**
         * Disposes all {@link CategorySection}s and recreates them in the order they are provided
         * by the underlying model.
         */
        public void recreateCategorySections() {
            disposeCategorySections();
            createCategorySections();
            createLinksCategorySection();
        }

        private void disposeCategorySections() {
            for (CategorySection categorySection : categoriesToSections.values()) {
                categorySection.dispose();
            }
            if (linksCategorySection != null) {
                linksCategorySection.dispose();
            }
        }

        /**
         * Disposes of the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory}.
         * <p>
         * Does nothing if no {@link CategorySection} exists for the indicated
         * {@link IProductCmptCategory}.
         */
        public void deleteCategorySection(IProductCmptCategory category) {
            CategorySection categorySection = getCategorySection(category);
            if (categorySection != null) {
                categorySection.dispose();
                categoriesToSections.remove(category);
            }
        }

        /**
         * Recomputes the layout of the entire page.
         */
        public void relayout() {
            left.layout();
            right.layout();
            getManagedForm().reflow(true);
        }

        /**
         * Returns the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory} or null if no {@link CategorySection} exists for the
         * indicated {@link IProductCmptCategory}.
         */
        public CategorySection getCategorySection(IProductCmptCategory category) {
            return categoriesToSections.get(category);
        }

        /**
         * Sets the focus to the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory}.
         * <p>
         * Does nothing if no {@link CategorySection} exists for the indicated
         * {@link IProductCmptCategory}.
         */
        public void setFocus(IProductCmptCategory category) {
            CategorySection categorySection = categoriesToSections.get(category);
            if (categorySection != null) {
                categorySection.setFocus();
            }
        }

    }

    /**
     * An {@link IpsSection} that represents the <em>Associations</em> category.
     * <p>
     * As of now, {@link IAssociation} cannot be assigned to {@link IProductCmptCategory} and in the
     * {@link ProductCmptEditor}, the {@link LinksSection} is automatically placed at the bottom
     * right.
     * <p>
     * This section represents the {@link LinksSection} in the {@link CategoryCompositionSection}.
     * It cannot be moved or modified by the user in any way. It is merely a reminder to the user
     * that the {@link LinksSection} is placed automatically at the bottom right by the
     * {@link ProductCmptEditor}.
     */
    private static class LinksCategorySection extends IpsSection {

        private Composite rootPane;

        private LinksCategorySection(Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
            initControls();
            setText(Messages.LinksCategorySection_title);
            setEnabled(false);
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            setLayout(client);
            createRootPane(client);
            createExplanationLabel();
        }

        private void setLayout(Composite composite) {
            GridLayout layout = new GridLayout(1, true);
            layout.marginWidth = 1;
            layout.marginHeight = 2;
            composite.setLayout(layout);
        }

        private void createRootPane(Composite client) {
            rootPane = getToolkit().createGridComposite(client, 1, false, true);
            getToolkit().addBorder(rootPane);
        }

        private void createExplanationLabel() {
            getToolkit().createLabel(rootPane, Messages.LinksCategorySection_explanation);
        }

    }

    private static class NewCategoryAction extends Action {

        private final IProductCmptType productCmptType;

        private NewCategoryAction(IProductCmptType productCmptType) {
            this.productCmptType = productCmptType;
        }

        @Override
        public void run() {
            // TODO AW
            DialogHelper dialogHelper = new DialogHelper();
            // Dialog dialog = new CategoryEditDialog(category, parentShell);
            // dialogHelper.openDialogWithMemento(dialog, editedPart);
        }

    }

}
