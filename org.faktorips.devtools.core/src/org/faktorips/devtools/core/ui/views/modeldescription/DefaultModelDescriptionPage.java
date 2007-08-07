package org.faktorips.devtools.core.ui.views.modeldescription;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.Page;
import org.faktorips.devtools.core.ui.editors.productcmpt.ProductCmptEditor;

public class DefaultModelDescriptionPage extends Page {

    // SWT basics
    private FormToolkit toolkit;

    // basic view elements
    private ScrolledForm form;
    
    private DescriptionItem[] list;
    private String title;

    public void ModelDescriptionView() {
    }
    
	public void createControl(Composite parent) {
        // Use form-styled widgets in this view
        toolkit = new FormToolkit(parent.getDisplay());

        form = toolkit.createScrolledForm(parent);
        
        // Using TableWrapLayout
        TableWrapLayout layoutForm = new TableWrapLayout();
        layoutForm.verticalSpacing = 1;
        layoutForm.horizontalSpacing = 1;
        layoutForm.numColumns = 1;

        form.getBody().setLayout(layoutForm);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	public Control getControl() {
		if (form == null) {
			return null;
		}
		
		return form;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	public void setFocus() {
        if (form == null) {
        	return;
        }
        
        form.setFocus();		
	}

    /**
     * Set new model data. 
     * @param title TODO
     * @param input IProductCmptType of the {@link ProductCmptEditor}
     */
    public void setInput(String title, DescriptionItem[] list) {
    	this.title = title;
    	this.list = list;

    	// Set headline title
    	form.setText(title);
    				        	
	    // collect all attributes in one container
		Composite expandableContainer = toolkit.createComposite(form.getBody());
	    
	    // use TableWrapLayout for automated line wrap
	    TableWrapLayout layout = new TableWrapLayout();
	    layout.verticalSpacing = 0;
	    layout.horizontalSpacing = 0;
	    layout.numColumns = 1;
	    
	    expandableContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
	    expandableContainer.setLayout(layout);
	    
	    int index = 2; // simple mechanism for color coding for lines 
	                   // in alternating colors: odd/even
	    
	    for (int i=0;i < this.list.length;i++) {
	        createExpandableControl(expandableContainer, this.list[i], index++);
	    }    	
    }

    /**
     * Create a single ExpandableComposite object with name=faktorips.attributename
     * and child(text)=faktorips.description.
     * 
     * @param parent rootContainer object.
     * @param column faktorips data
     * @param index
     */
    private void createExpandableControl(Composite parent, DescriptionItem item, int index) {
        
        ExpandableComposite excomposite = toolkit.createExpandableComposite(parent, 
                ExpandableComposite.TWISTIE|
                ExpandableComposite.COMPACT|
                ExpandableComposite.EXPANDED );
        
        // Set faktorips.attribute name
        excomposite.setText(StringUtils.capitalise(item.getName()));
        
        if ((index % 2) == 0) {
            Color grey = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            excomposite.setBackground(grey);
        }
        
        excomposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        excomposite.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        
        // Set faktorips.attribute description
        FormText client = toolkit.createFormText(excomposite, true);
        client.setText(item.getDescription(), false, true);        
        client.setBackground(excomposite.getBackground());
        client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        
        excomposite.setClient(client);
    }

	public DescriptionItem[] getList() {
		return list;
	}

	public void setList(DescriptionItem[] list) {
		this.list = list;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void dispose() {
		
		if (toolkit != null) {
			toolkit.dispose();
		}
		
		if (form != null) {
			form.dispose();
		}
		
		super.dispose();
	}
	
}