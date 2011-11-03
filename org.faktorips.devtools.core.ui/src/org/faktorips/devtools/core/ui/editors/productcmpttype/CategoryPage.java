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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.util.ListElementMover;

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
        new CategoryCompositionSection((IProductCmptType)getIpsObject(), formBody, toolkit);
    }

    static class CategoryCompositionSection extends IpsSection {

        private final List<CategorySection> leftSections = new ArrayList<CategorySection>();

        private final List<CategorySection> rightSections = new ArrayList<CategorySection>();

        private final Map<IProductCmptCategory, CategorySection> categoriesToSections = new LinkedHashMap<IProductCmptCategory, CategorySection>();

        private final Map<CategorySection, IProductCmptCategory> sectionsToCategories = new LinkedHashMap<CategorySection, IProductCmptCategory>();

        private final IProductCmptType productCmptType;

        private Composite left;

        private Composite right;

        public CategoryCompositionSection(IProductCmptType productCmptType, Composite parent, UIToolkit toolkit) {
            super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

            this.productCmptType = productCmptType;

            initControls();
        }

        @Override
        protected String getSectionTitle() {
            return Messages.CategoryCompositionSection_sectionTitle;
        }

        @Override
        protected void initClientComposite(Composite client, UIToolkit toolkit) {
            setLayout(client);
            left = createColumnComposite(client);
            right = createColumnComposite(client);
            createCategorySections();
        }

        private void setLayout(Composite parent) {
            GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 1;
            layout.marginHeight = 2;
            parent.setLayout(layout);
        }

        private Composite createColumnComposite(Composite parent) {
            return getToolkit().createGridComposite(parent, 1, true, true);
        }

        private void createCategorySections() {
            // Determine categories
            List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>();
            try {
                categories.addAll(productCmptType.findProductCmptCategories(productCmptType.getIpsProject()));
            } catch (CoreException e) {
                // Recover by not displaying any categories
                IpsPlugin.log(e);
            }

            // Create a section for each category
            for (IProductCmptCategory category : categories) {
                Composite parent = category.isAtLeftPosition() ? left : right;
                CategorySection categorySection = new CategorySection(category, productCmptType, this, parent,
                        getToolkit());

                List<CategorySection> sections = category.isAtLeftPosition() ? leftSections : rightSections;
                sections.add(categorySection);

                categoriesToSections.put(category, categorySection);
                sectionsToCategories.put(categorySection, category);
            }
        }

        // TODO AW 02-11-2011: Create toolbar

        @Override
        protected void performRefresh() {
            for (IpsSection categorySection : leftSections) {
                categorySection.refresh();
            }
            for (IpsSection categorySection : rightSections) {
                categorySection.refresh();
            }
        }

        /**
         * Returns the {@link CategorySection} corresponding to the indicated
         * {@link IProductCmptCategory}.
         */
        public CategorySection getCategorySection(IProductCmptCategory category) {
            return categoriesToSections.get(category);
        }

        /**
         * Moves the {@link CategorySection} for the given {@link IProductCmptCategory} up or down
         * by one position.
         * 
         * @param category the {@link IProductCmptCategory} whose {@link CategorySection} shall be
         *            move
         * @param up flag indicating whether to move up or down
         */
        public void moveCategorySection(IProductCmptCategory category, boolean up) {
            List<CategorySection> sections = category.isAtLeftPosition() ? leftSections : rightSections;
            int sourceIndex = sections.indexOf(categoriesToSections.get(category));
            // TODO AW

            CategorySection sourceSection = sections.get(sourceIndex);
            IProductCmptCategory sourceCategory = sectionsToCategories.get(sourceSection);

            ListElementMover<CategorySection> mover = new ListElementMover<CategorySection>(sections);
            int newIndex = mover.move(new int[] { sourceIndex }, up)[0];
            if (newIndex == sourceIndex) {
                return;
            }

            CategorySection targetSection = sections.get(newIndex);
            IProductCmptCategory targetCategory = sectionsToCategories.get(targetSection);
            if (!sourceCategory.getPosition().equals(targetCategory.getPosition())) {
                return;
            }

            Composite parent = sourceCategory.isAtLeftPosition() ? left : right;
            swapCategorySections(sourceIndex, newIndex, sections, parent);
            parent.layout();
        }

        private void swapCategorySections(int sourceIndex,
                int targetIndex,
                List<CategorySection> sections,
                Composite parent) {

            CategorySection sourceSection = sections.get(sourceIndex);
            CategorySection targetSection = sections.get(targetIndex);
            IProductCmptCategory sourceCategory = sectionsToCategories.get(sourceSection);
            IProductCmptCategory targetCategory = sectionsToCategories.get(targetSection);

            sourceSection.dispose();
            targetSection.dispose();

            CategorySection newSourceSection = new CategorySection(targetCategory, productCmptType, this, parent,
                    getToolkit());
            CategorySection newTargetSection = new CategorySection(sourceCategory, productCmptType, this, parent,
                    getToolkit());

            sectionsToCategories.remove(sourceSection);
            sectionsToCategories.remove(targetSection);
            sectionsToCategories.put(newSourceSection, targetCategory);
            sectionsToCategories.put(newTargetSection, sourceCategory);
            categoriesToSections.put(sourceCategory, newTargetSection);
            categoriesToSections.put(targetCategory, newSourceSection);

            sections.set(sourceIndex, newSourceSection);
            sections.set(targetIndex, newTargetSection);
        }

    }

}
