/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IExtensionPropertySectionFactory.Position;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.core.ui.views.modeldescription.ModelDescriptionView;

/**
 * Page to display a generation's properties.
 * 
 * @author Thorsten Guenther
 * @author Alexander Weickmann
 */
public class GenerationPropertiesPage extends IpsObjectEditorPage {

    public final static String PAGE_ID = "Properties"; //$NON-NLS-1$

    private final List<IpsSection> leftSections = new ArrayList<IpsSection>(4);

    private final List<IpsSection> rightSections = new ArrayList<IpsSection>(4);

    /**
     * Layout for this page (see pageRoot) - if the content-structure for this page changes, the
     * current set top level composite is disposed and a completely new one is created. This is to
     * avoid complex code for structural refresh.
     */
    private StackLayout stack;

    /**
     * The composite which serves as root-composite for this page. This composite is controlled by
     * the Forms-framework, so it should not be disposed.
     */
    private Composite pageRoot;

    private UIToolkit toolkit;

    private GotoGenerationAction gotoPreviousGenerationAction;

    private GotoGenerationAction gotoNextGenerationAction;

    public GenerationPropertiesPage(ProductCmptEditor editor) {
        super(editor, PAGE_ID, ""); // Title will be updated based on selected generation //$NON-NLS-1$
    }

    @Override
    public ProductCmptEditor getEditor() {
        return (ProductCmptEditor)super.getEditor();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        pageRoot = formBody;
        this.toolkit = toolkit;

        createStack();
        createPageContent();
        createToolbar();
    }

    /**
     * Create a stack for easy update of the view by disposing the old top of the stack and putting
     * a new one.
     */
    private void createStack() {
        stack = new StackLayout();
        pageRoot.setLayout(stack);
        createAndSetStackTopControl();
    }

    private void createAndSetStackTopControl() {
        stack.topControl = toolkit.createGridComposite(pageRoot, 2, true, true);
    }

    private void createPageContent() {
        Composite left = createColumnComposite();
        Composite right = createColumnComposite();

        createSections(left, right);

        setFocusSuccessors();
        registerSelectionProviderActivation(stack.topControl);

        reduceToOneColumnAsNecessary(left, right);
    }

    private void reduceToOneColumnAsNecessary(Composite left, Composite right) {
        boolean leftEmpty = disposeColumnIfEmpty(left, leftSections);
        boolean rightEmpty = disposeColumnIfEmpty(right, rightSections);
        if (leftEmpty || rightEmpty) {
            GridLayout layout = (GridLayout)((Composite)stack.topControl).getLayout();
            layout.numColumns = 1;
        }
    }

    private boolean disposeColumnIfEmpty(Composite column, List<IpsSection> columnSections) {
        if (columnSections.isEmpty()) {
            column.dispose();
            return true;
        }
        return false;
    }

    private Composite createColumnComposite() {
        return createGridComposite(toolkit, (Composite)stack.topControl, 1, true, GridData.FILL_BOTH
                | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    }

    private void createSections(Composite left, Composite right) {
        createCategorySections(left, right);
        createExtensionFactorySections(left, right);
        createLinksSection(right);
    }

    private void createCategorySections(Composite left, Composite right) {
        // Find product component type
        IProductCmptType productCmptType = null;
        try {
            productCmptType = getActiveGeneration().findProductCmptType(getActiveGeneration().getIpsProject());
        } catch (CoreException e) {
            /*
             * An error occurred while searching for the product component type. Recover by creating
             * a fallback section and log the exception.
             */
            createFallbackSection(left);
            IpsPlugin.log(e);
            return;
        }

        // Create a fallback section if the product component type cannot be found
        if (productCmptType == null) {
            createFallbackSection(left);
            return;
        }

        // Determine categories
        List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>(4);
        try {
            categories.addAll(productCmptType.findCategories(productCmptType.getIpsProject()));
        } catch (CoreException e) {
            /*
             * The categories could not be determined. Recover by creating a fallback section and
             * log the exception.
             */
            createFallbackSection(left);
            IpsPlugin.log(e);
            return;
        }

        // Create a section for each category
        for (IProductCmptCategory category : categories) {
            createSectionForCategory(category, left, right);
        }
    }

    private void createFallbackSection(Composite left) {
        // Obtain all property values
        List<IPropertyValue> propertyValues;
        try {
            propertyValues = getProductCmpt().findPropertyValues(null, getActiveGeneration().getValidFrom(),
                    getIpsObject().getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        leftSections.add(new FallbackSection(propertyValues, left, toolkit));
    }

    private void createSectionForCategory(IProductCmptCategory category, Composite left, Composite right) {
        // Find the property values that match to the category's properties
        List<IPropertyValue> propertyValues;
        try {
            propertyValues = getProductCmpt().findPropertyValues(category, getActiveGeneration().getValidFrom(),
                    getIpsObject().getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        // Create a new section for this category and attach it to the page
        List<IpsSection> sections = category.isAtLeftPosition() ? leftSections : rightSections;
        Composite parent = category.isAtLeftPosition() ? left : right;
        IpsSection section = new PropertySection(category, propertyValues, parent, toolkit);
        sections.add(section);
    }

    private void createExtensionFactorySections(Composite left, Composite right) {
        ExtensionPropertyControlFactory extFactory = new ExtensionPropertyControlFactory(getActiveGeneration()
                .getClass());
        leftSections.addAll(extFactory.createSections(left, toolkit, getActiveGeneration(), Position.LEFT));
        rightSections.addAll(extFactory.createSections(right, toolkit, getActiveGeneration(), Position.RIGHT));
    }

    private void createLinksSection(Composite right) {
        IpsSection linksSection = new LinksSection(getEditor(), getActiveGeneration(), right, toolkit);
        rightSections.add(linksSection);
    }

    private void setFocusSuccessors() {
        for (int i = 0; i < leftSections.size() - 1; i++) {
            leftSections.get(i).setFocusSuccessor(leftSections.get(i + 1));
        }
        for (int i = 0; i < rightSections.size() - 1; i++) {
            rightSections.get(i).setFocusSuccessor(rightSections.get(i + 1));
        }
        if (!leftSections.isEmpty()) {
            leftSections.get(leftSections.size() - 1).setFocusSuccessor(rightSections.get(0));
        }
    }

    private void createToolbar() {
        gotoPreviousGenerationAction = createGotoPreviousGenerationAction();
        gotoNextGenerationAction = createGotoNextGenerationAction();
        Action openModelDescription = createOpenModelDescriptionAction();

        IToolBarManager toolbarManager = getManagedForm().getForm().getToolBarManager();
        toolbarManager.add(gotoPreviousGenerationAction);
        toolbarManager.add(gotoNextGenerationAction);
        toolbarManager.add(openModelDescription);
        getManagedForm().getForm().updateToolBar();
    }

    private GotoGenerationAction createGotoPreviousGenerationAction() {
        return new GotoGenerationAction(this, "ArrowLeft.gif") { //$NON-NLS-1$
            @Override
            protected IIpsObjectGeneration getGeneration() {
                return generationPropertiesPage.getActiveGeneration().getPreviousByValidDate();
            }
        };
    }

    private GotoGenerationAction createGotoNextGenerationAction() {
        return new GotoGenerationAction(this, "ArrowRight.gif") { //$NON-NLS-1$
            @Override
            protected IIpsObjectGeneration getGeneration() {
                return generationPropertiesPage.getActiveGeneration().getNextByValidDate();
            }
        };
    }

    private Action createOpenModelDescriptionAction() {
        return new Action(Messages.GenerationPropertiesPage_openModelDescView, IpsUIPlugin.getImageHandling()
                .createImageDescriptor("ModelDescription.gif")) { //$NON-NLS-1$
            @Override
            public void run() {
                try {
                    IpsUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .showView(ModelDescriptionView.EXTENSION_ID);
                } catch (PartInitException e) {
                    IpsPlugin.log(e);
                }
            }
        };
    }

    @Override
    public void refresh() {
        updateTabname();
        super.refresh();
    }

    /**
     * Refreshes the page when the active generation has changed.
     * <p>
     * A call to this method causes the currently displayed composite to be disposed. A completely
     * new composite is created and stacked on top of the layout. This is done to avoid complex code
     * for structural updates.
     */
    protected void rebuildInclStructuralChanges() {
        // If stack == null, the page contents are not created yet, so do nothing
        if (stack != null) {
            updateStack();
            createPageContent();
            updateTabname();
            resetDataChangeableState();
        }

        gotoPreviousGenerationAction.update();
        gotoNextGenerationAction.update();
    }

    private void updateStack() {
        stack.topControl.dispose();
        createAndSetStackTopControl();
    }

    private String getTabname(IIpsObjectGeneration generation) {
        DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
        String validRange = dateFormat.format(generation.getValidFrom().getTime())
                + " - " + getValidToString(generation); //$NON-NLS-1$
        return generationConceptName + ' ' + validRange;
    }

    private String getValidToString(IIpsObjectGeneration generation) {
        if (generation.getValidTo() == null) {
            return Messages.GenerationPropertiesPage_valueGenerationValidToUnlimited;
        } else {
            return IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(generation.getValidTo().getTime());
        }
    }

    private void updateTabname() {
        setPartName(getTabname(getActiveGeneration()));
        updateTabText(getPartControl());
    }

    private void updateTabText(Control partControl) {
        if (partControl == null) {
            return;
        }
        if (partControl instanceof CTabFolder) {
            ((CTabFolder)partControl).getItem(0).setText(getPartName());
            return;
        }
        updateTabText(partControl.getParent());
    }

    @Override
    protected boolean computeDataChangeableState() {
        return ((ProductCmptEditor)getIpsObjectEditor()).isActiveGenerationEditable();
    }

    private IProductCmptGeneration getActiveGeneration() {
        return (IProductCmptGeneration)(getEditor()).getActiveGeneration();
    }

    private IProductCmpt getProductCmpt() {
        return (IProductCmpt)getIpsObject();
    }

    /**
     * A section that shows all properties of the generation.
     * <p>
     * This kind of section is needed if the {@link IProductCmptType} of the {@link IProductCmpt}
     * cannot be found or if the {@link IProductCmptCategory categories} cannot be determined.
     */
    private static class FallbackSection extends ProductCmptPropertySection {

        private static final String ID = "org.faktorips.devtools.core.ui.editors.productcmpt.FallbackSection"; //$NON-NLS-1$

        private FallbackSection(List<IPropertyValue> propertyValues, Composite parent, UIToolkit toolkit) {
            super(ID, propertyValues, parent, GridData.FILL_BOTH, toolkit);
            initControls();
        }

        @Override
        protected String getSectionTitle() {
            return Messages.GenerationPropertiesPage_fallbackSectionTitle;
        }

    }

    /**
     * Section that displays the property values corresponding to the product component properties
     * assigned to a specific {@link IProductCmptCategory}.
     */
    private static class PropertySection extends ProductCmptPropertySection {

        private final IProductCmptCategory category;

        private PropertySection(IProductCmptCategory category, List<IPropertyValue> propertyValues, Composite parent,
                UIToolkit toolkit) {

            super(category.getId(), propertyValues, parent, category.isAtLeftPosition() ? GridData.FILL_BOTH
                    : GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL, toolkit);

            this.category = category;

            /*
             * The following call is necessary in addition to the above layout data because of the
             * relayoutSection(boolean) method.
             */
            if (category.isAtRightPosition()) {
                setGrabVerticalSpace(false);
            }

            initControls();
        }

        @Override
        protected String getSectionTitle() {
            return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
        }

    }

    private abstract class GotoGenerationAction extends Action {

        /**
         * Explicit reference to outer class (avoids bug
         * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4030374).
         */
        protected GenerationPropertiesPage generationPropertiesPage;

        public GotoGenerationAction(GenerationPropertiesPage generationPropertiesPage, String imageName) {
            this.generationPropertiesPage = generationPropertiesPage;
            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(imageName));
            update();
        }

        abstract protected IIpsObjectGeneration getGeneration();

        public void update() {
            if (getGeneration() == null) {
                setText(null);
                setToolTipText(null);
                setEnabled(false);
            } else {
                String tabName = getTabname(getGeneration());
                setText(tabName);
                setToolTipText(tabName);
                setEnabled(true);
            }
        }

        @Override
        public void run() {
            BusyIndicator.showWhile(pageRoot.getDisplay(), new Runnable() {
                @Override
                public void run() {
                    IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(getGeneration().getValidFrom());
                    (getEditor()).setActiveGeneration(getGeneration(), true);
                }
            });
        }

    }

}
