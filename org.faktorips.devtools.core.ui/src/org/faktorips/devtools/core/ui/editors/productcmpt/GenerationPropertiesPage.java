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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IExtensionPropertySectionFactory.Position;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
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

    public GenerationPropertiesPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, ""); // Title will be updated based on selected generation //$NON-NLS-1$
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

        layout();
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
        // Determine categories
        IProductCmptType productCmptType = null;
        List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>(4);
        try {
            productCmptType = getActiveGeneration().findProductCmptType(getActiveGeneration().getIpsProject());
            if (productCmptType != null) {
                categories.addAll(productCmptType.findProductCmptCategories(productCmptType.getIpsProject()));
            }
        } catch (CoreException e) {
            /*
             * If this kind of exception occurs, the categories could not be determined. Recover by
             * not creating any sections for categories and log exception.
             */
            IpsPlugin.log(e);
            return;
        }

        // Create a section for each category
        List<IPropertyValue> allPropertyValues = new ArrayList<IPropertyValue>();
        allPropertyValues.addAll(getActiveGeneration().getAllPropertyValues());
        allPropertyValues.addAll(getActiveGeneration().getProductCmpt().getAllPropertyValues());
        for (IProductCmptCategory category : categories) {
            // Determine the properties assigned to this category
            List<IProductCmptProperty> categoryProperties = new ArrayList<IProductCmptProperty>();
            try {
                categoryProperties = category.findProductCmptProperties(productCmptType, getActiveGeneration()
                        .getIpsProject());
            } catch (CoreException e) {
                /*
                 * If this kind of exception occurs, the properties assigned to the category could
                 * not be determined. Recover by not displaying any properties for this category.
                 * Instead, log the exception and continue to the next category.
                 */
                IpsPlugin.log(e);
                continue;
            }

            // Find the property values corresponding to the category's properties
            // TODO AW 20-10-2011: Can we solve this in a more elegant way?
            List<IPropertyValue> propertyValues = new ArrayList<IPropertyValue>();
            for (IProductCmptProperty property : categoryProperties) {
                for (IPropertyValue propertyValue : allPropertyValues) {
                    if (property.getProductCmptPropertyType().equals(propertyValue.getPropertyType())
                            && property.getPropertyName().equals(propertyValue.getPropertyName())) {
                        propertyValues.add(propertyValue);
                    }
                }
            }

            // Create a new section for this category and attach it to the page
            List<IpsSection> sections = category.isAtLeftPosition() ? leftSections : rightSections;
            Composite parent = category.isAtLeftPosition() ? left : right;
            IpsSection section = new ProductCmptPropertySection(category, getActiveGeneration(), propertyValues,
                    parent, toolkit);
            sections.add(section);
        }
    }

    private void createExtensionFactorySections(Composite left, Composite right) {
        ExtensionPropertyControlFactory extFactory = new ExtensionPropertyControlFactory(getActiveGeneration()
                .getClass());
        leftSections.addAll(extFactory.createSections(left, toolkit, getActiveGeneration(), Position.LEFT));
        rightSections.addAll(extFactory.createSections(right, toolkit, getActiveGeneration(), Position.RIGHT));
    }

    private void createLinksSection(Composite right) {
        IpsSection linksSection = new LinksSection(getActiveGeneration(), right, toolkit, getEditorSite());
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

    private void layout() {
        pageRoot.layout();
    }

    private IProductCmptGeneration getActiveGeneration() {
        return (IProductCmptGeneration)((ProductCmptEditor)getEditor()).getActiveGeneration();
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
                    ((ProductCmptEditor)getEditor()).setActiveGeneration(getGeneration(), true);
                }
            });
        }

    }

}
