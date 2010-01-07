/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * Page to display a generation's properties.
 * 
 * @author Thorsten Guenther
 */
public class GenerationPropertiesPage extends IpsObjectEditorPage {

    /**
     * Id to identify the page.
     */
    public final static String PAGE_ID = "Properties"; //$NON-NLS-1$

    // Sections for different property-groups
    private GenerationAttributesSection productAttributesSection;

    private FormulasSection formulasSection;

    private DefaultsAndRangesSection defaultsAndRangesSection;

    private LinksSection linksSection;

    /*
     * Layout for this page (see pageRoot) - if the content-structure for this page changes, the
     * current set top level composite is disposed and a completely new one is created. This is to
     * avoid complex code for structural refresh.
     */
    private StackLayout stack;

    /*
     * The composite which serves as root-composite for this page. This composit is controlled by
     * the Forms-framework, so it should not be disposed.
     */
    private Composite pageRoot;

    /*
     * The toolkit to make ui-construction easier.
     */
    private UIToolkit toolkit;

    private GotoGenerationAction gotoPreviousGenerationAction;
    private GotoGenerationAction gotoNextGenerationAction;

    /**
     * Creates a new page for editing properties of a product.
     * 
     * @param editor The owner of this page
     */
    public GenerationPropertiesPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, ""); // Title will be updated based on selected generation //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        pageRoot = formBody;
        this.toolkit = toolkit;

        // create a stack for easy update the view by disposing the old top of
        // stack and put a new one
        stack = new StackLayout();
        formBody.setLayout(stack);
        Composite root = new Composite(formBody, SWT.NONE);
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

        ScrolledForm form = getManagedForm().getForm();
        form.getToolBarManager().add(gotoPreviousGenerationAction);
        form.getToolBarManager().add(gotoNextGenerationAction);
        form.updateToolBar();
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
                public void run() {
                    IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(getGeneration().getValidFrom());
                    ((ProductCmptEditor)getEditor()).setActiveGeneration(getGeneration(), true);
                }
            });
        }
    }

    /**
     * Create the page-content by building the different sections.
     * 
     * @param toolkit The toolkit to use for control creation.
     * @param root the parent for the new controls.
     */
    private void buildContent(UIToolkit toolkit, Composite root) {
        GridLayout layout = new GridLayout(2, true);
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;

        root.setLayout(layout);
        root.setBackground(pageRoot.getBackground());

        IProductCmptGeneration generation = getActiveGeneration();

        Composite left = createGridComposite(toolkit, root, 1, true, GridData.FILL_BOTH);
        productAttributesSection = new GenerationAttributesSection(generation, left, toolkit,
                (ProductCmptEditor)getEditor());
        formulasSection = new FormulasSection(generation, left, toolkit);

        Composite right = createGridComposite(toolkit, root, 1, true, GridData.FILL_BOTH);
        defaultsAndRangesSection = new DefaultsAndRangesSection(generation, right, toolkit);
        linksSection = new LinksSection(generation, right, toolkit, getEditorSite());

        productAttributesSection.setFocusSuccessor(formulasSection);
        formulasSection.setFocusSuccessor(defaultsAndRangesSection);
        defaultsAndRangesSection.setFocusSuccessor(linksSection);

        // searches for Composites that implement the ISelectionProviderActivation interface and
        // registers
        // them with the selection provider dispatcher of the IpsObjectEditor
        registerSelectionProviderActivation(root);

        pageRoot.layout();
    }

    /**
     * Returns the currently active generation set in the owning editor.
     */
    private IProductCmptGeneration getActiveGeneration() {
        return (IProductCmptGeneration)((ProductCmptEditor)getEditor()).getActiveGeneration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        updateTabname();
        super.refresh();
    }

    /**
     * Refreshes the page when the active generation has changed.
     * 
     * A call to this method causes the currently displayed composite to be disposed. A completely
     * new composite is created and stacked on top of the layout. This is done to avoid complex code
     * for structural updates.
     */
    protected void rebuildInclStructuralChanges() {
        // if stack == null, the page contents are not created yet, so do
        // nothing.
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
        return generationConceptName + " " + validRange; //$NON-NLS-1$
    }

    private void updateTabname() {
        setPartName(getTabname(getActiveGeneration()));
        updateTabText(getPartControl());
    }

    /**
     * @param partControl
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean computeDataChangeableState() {
        return ((ProductCmptEditor)getIpsObjectEditor()).isActiveGenerationEditable();
    }

}
