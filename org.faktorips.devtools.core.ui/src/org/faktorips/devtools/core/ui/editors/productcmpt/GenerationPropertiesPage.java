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
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ScrolledForm;
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

    private LinksSection linksSection;

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

        /*
         * Create a stack for easy update of the view by disposing the old top of the stack and
         * putting a new one.
         */
        stack = new StackLayout();
        formBody.setLayout(stack);
        Composite root = toolkit.createGridComposite(formBody, 2, true, true);
        stack.topControl = root;

        buildContent(toolkit, root);

        createNavigationButtons();
    }

    private void createNavigationButtons() {
        gotoPreviousGenerationAction = new GotoGenerationAction(this, "ArrowLeft.gif") { //$NON-NLS-1$
            @Override
            protected IIpsObjectGeneration getGeneration() {
                return generationPropertiesPage.getActiveGeneration().getPreviousByValidDate();
            }
        };

        gotoNextGenerationAction = new GotoGenerationAction(this, "ArrowRight.gif") { //$NON-NLS-1$
            @Override
            protected IIpsObjectGeneration getGeneration() {
                return generationPropertiesPage.getActiveGeneration().getNextByValidDate();
            }
        };

        Action openModelDescription = new Action(Messages.GenerationPropertiesPage_openModelDescView, IpsUIPlugin
                .getImageHandling().createImageDescriptor("ModelDescription.gif")) { //$NON-NLS-1$
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

        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(gotoPreviousGenerationAction);
        form.getToolBarManager().add(gotoNextGenerationAction);
        form.getToolBarManager().add(openModelDescription);
        form.updateToolBar();
    }

    /**
     * Creates the page content by building the different sections.
     * 
     * @param toolkit The toolkit to use for control creation
     * @param root The parent for the new controls
     */
    private void buildContent(UIToolkit toolkit, Composite root) {
        IProductCmptGeneration generation = getActiveGeneration();

        ExtensionPropertyControlFactory extFactory = new ExtensionPropertyControlFactory(generation.getClass());

        // Create left and right composite
        Composite left = createGridComposite(toolkit, root, 1, true, GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        Composite right = createGridComposite(toolkit, root, 1, true, GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);

        // Determine categories
        IProductCmptType productCmptType = null;
        List<IProductCmptCategory> categories = new ArrayList<IProductCmptCategory>(4);
        try {
            productCmptType = generation.findProductCmptType(generation.getIpsProject());
            categories.addAll(productCmptType.findProductCmptCategories(productCmptType.getIpsProject()));
        } catch (CoreException e) {
            // TODO AW
        }

        // Create a section for each category
        List<IPropertyValue> allPropertyValues = new ArrayList<IPropertyValue>(generation.getAllPropertyValues().size()
                + generation.getProductCmpt().getAllPropertyValues().size());
        allPropertyValues.addAll(generation.getAllPropertyValues());
        allPropertyValues.addAll(generation.getProductCmpt().getAllPropertyValues());
        for (IProductCmptCategory category : categories) {
            List<IProductCmptProperty> categoryProperties = new ArrayList<IProductCmptProperty>();
            try {
                categoryProperties = category.findProductCmptProperties(productCmptType, generation.getIpsProject());
            } catch (CoreException e) {
                // TODO AW
            }
            List<IPropertyValue> propertyValues = new ArrayList<IPropertyValue>();
            for (IProductCmptProperty property : categoryProperties) {
                for (IPropertyValue propertyValue : allPropertyValues) {
                    if (property.getProductCmptPropertyType().equals(propertyValue.getPropertyType())
                            && property.getPropertyName().equals(propertyValue.getPropertyName())) {
                        propertyValues.add(propertyValue);
                    }
                }
            }
            Composite parent = category.isAtLeftPosition() ? left : right;
            IpsSection section = new ProductCmptPropertySection(category, generation, parent, toolkit, propertyValues);
            if (category.isAtLeftPosition()) {
                leftSections.add(section);
            } else {
                rightSections.add(section);
            }
        }

        // Create sections of extension factory
        leftSections.addAll(extFactory.createSections(left, toolkit, generation, Position.LEFT));
        rightSections.addAll(extFactory.createSections(left, toolkit, generation, Position.RIGHT));

        // Create links section
        linksSection = new LinksSection(generation, right, toolkit, getEditorSite());
        rightSections.add(linksSection);

        // Set focus successors
        for (int i = 0; i < leftSections.size() - 1; i++) {
            leftSections.get(i).setFocusSuccessor(leftSections.get(i + 1));
        }
        for (int i = 0; i < rightSections.size() - 1; i++) {
            rightSections.get(i).setFocusSuccessor(rightSections.get(i + 1));
        }
        if (!leftSections.isEmpty()) {
            leftSections.get(leftSections.size() - 1).setFocusSuccessor(rightSections.get(0));
        }

        /*
         * Searches for composites that implement the ISelectionProviderActivation interface and
         * registers them with the selection provider dispatcher of the IpsObjectEditor.
         */
        registerSelectionProviderActivation(root);

        pageRoot.layout();
    }

    /**
     * Returns the currently active generation set in the owning editor.
     */
    private IProductCmptGeneration getActiveGeneration() {
        return (IProductCmptGeneration)((ProductCmptEditor)getEditor()).getActiveGeneration();
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
            stack.topControl.dispose();
            Composite root = new Composite(pageRoot, SWT.NONE);
            stack.topControl = root;
            buildContent(toolkit, root);
            updateTabname();
            resetDataChangeableState();
        }

        gotoPreviousGenerationAction.update();
        gotoNextGenerationAction.update();
    }

    private String getTabname(IIpsObjectGeneration generation) {
        DateFormat format = IpsPlugin.getDefault().getIpsPreferences().getDateFormat();
        String validRange = format.format(generation.getValidFrom().getTime());

        GregorianCalendar date = generation.getValidTo();
        String validToString;
        if (date == null) {
            validToString = Messages.ProductAttributesSection_valueGenerationValidToUnlimited;
        } else {
            validToString = IpsPlugin.getDefault().getIpsPreferences().getDateFormat().format(date.getTime());
        }

        validRange += " - " + validToString; //$NON-NLS-1$
        String generationConceptName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular();
        return generationConceptName + ' ' + validRange;
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
