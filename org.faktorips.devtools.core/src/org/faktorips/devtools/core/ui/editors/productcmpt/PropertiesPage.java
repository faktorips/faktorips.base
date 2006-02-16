package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 * Page to display the properties owned by one product (attributes, relations, ...)
 * 
 * @author Thorsten Guenther
 */
public class PropertiesPage extends IpsObjectEditorPage {
    
	/**
	 * Id to identify the page.
	 */
    public final static String PAGE_ID = "Properties"; //$NON-NLS-1$

    // Sections for different property-groups
    private ProductAttributesSection productAttributesSection;
	private FormulasSection formulasSection;
	private DefaultsAndRangesSection defaultsAndRangesSection;
	private RelationsSection relationsSection;
	
	private boolean enabled;
	
	/**
	 * Layout for this page (see pageRoot) - if the content-structure for this page changes, the current set top level
	 * composite is disposed and a completely new one is created. This is to avoid complex code for structural
	 * refresh.
	 */
	private StackLayout stack;
	
	/**
	 * The composite which serves as root-composite for this page. This composit is controlled by the Forms-framework,
	 * so it should not be disposed. 
	 */
	private Composite pageRoot;
	
	/**
	 * The toolkit to make ui-construction easier.
	 */
	private UIToolkit toolkit;
	
	/**
	 * Creates a new page for editing properties of a product.
	 * 
	 * @param editor The owner of this page
	 */
    public PropertiesPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, Messages.PropertiesPage_properties);
        
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
    	this.pageRoot = formBody;
    	this.toolkit = toolkit;
    	
    	// create a stack for easy update the view by disposing the old top of stack and put a new one
    	stack = new StackLayout();
    	formBody.setLayout(stack);
    	Composite root = new Composite(formBody, SWT.NONE);
    	stack.topControl = root;
    	
		buildContent(toolkit, root);
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
		productAttributesSection = new ProductAttributesSection(generation, left, toolkit);
		formulasSection = new FormulasSection(generation, left, toolkit);
		
		Composite right = createGridComposite(toolkit, root, 1, true, GridData.FILL_BOTH);
		defaultsAndRangesSection = new DefaultsAndRangesSection(generation, right, toolkit);
		relationsSection = new RelationsSection(generation, right, toolkit, getEditorSite());

		pageRoot.layout();
		setEnabled(enabled);
	}
    
	/**
	 * Enables or disables the page for editing.
	 */
    protected void setEnabled(boolean enabled) {
    	this.enabled = enabled;
    	if (productAttributesSection != null) {
			productAttributesSection.setEnabled(enabled);
			formulasSection.setEnabled(enabled);
			defaultsAndRangesSection.setEnabled(enabled);
			relationsSection.setEnabled(enabled);
    	}
    }

    /**
     * A call to this method causes the currently displayed composite to be disposed. 
     * A completely new composite is created and stacked on top of the layout. This is 
     * done to avoid complex code for structural updates.
     */
	protected void refreshStructure() {
		// if stack == null, the page contents are not created yet, so do nothing.
		if (stack != null) {
			stack.topControl.dispose();
			Composite root = new Composite(pageRoot, SWT.NONE);
			stack.topControl = root;
			buildContent(toolkit, root);
		}		
	}

	/**
	 * Returns the currently active generation set in the owning editor.
	 */
    private IProductCmptGeneration getActiveGeneration() {
    	return (IProductCmptGeneration)((ProductCmptEditor)getEditor()).getActiveGeneration();
    }
    
}
