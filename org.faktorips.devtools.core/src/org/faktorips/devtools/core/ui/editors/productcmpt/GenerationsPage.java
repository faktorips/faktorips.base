package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Locale;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.DescriptionSection;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


/**
 * A page to display the generations.
 */
public class GenerationsPage extends IpsObjectEditorPage {
    
    final static String PAGE_ID = "Generations";

    public GenerationsPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, IpsPreferences.getChangesInTimeNamingConvention().getGenerationConceptNamePlural(Locale.getDefault()));
    }

    ProductCmptEditor getProductCmptEditor() {
        return (ProductCmptEditor)getEditor();
    }
    
    IProductCmpt getProductCmpt() {
        return getProductCmptEditor().getProductCmpt(); 
    }
    
    /**
     * Overridden.
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
		GridLayout layout = new GridLayout(2, true);
		formBody.setLayout(layout);
		
		final GenerationsSection generationsSection 
			= new GenerationsSection(getProductCmpt(), formBody, toolkit);
		final DescriptionSection descSection = new DescriptionSection(getProductCmpt(), formBody, toolkit);
		generationsSection.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
               descSection.setDescribedObject(generationsSection.getSelectedPart());
            }
		    
		});
    }
    
    /** 
     * Overridden.
     */
    protected void refresh() {
        super.refresh();
    }
    
    

}
