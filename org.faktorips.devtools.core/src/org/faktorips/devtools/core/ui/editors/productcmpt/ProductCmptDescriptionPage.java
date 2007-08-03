/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * A page for presenting the attributes of a {@link IProductCmptType}. This page is
 * connected to a {@link ProductCmptEditor} similiar to the outline view.
 * 
 * The attributes and their description are presented within a ExpandableComposite.
 * 
 * @author blum
 *
 */

public class ProductCmptDescriptionPage extends Page implements IPage {

    // SWT basics
    private FormToolkit toolkit;

    // basic view elements
    private ScrolledForm form;    
    private Composite expandableContainer; // Container for ExpandableComposite widgets
    private IProductCmptGeneration productCmptGen;
    
    public ProductCmptDescriptionPage(IProductCmptGeneration productCmptGen) {
    	super();
    	this.productCmptGen = productCmptGen;
    }


	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
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
        
        if (productCmptGen != null) {
        	setInput(productCmptGen);
        }

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
     * Create a single ExpandableComposite object with name=faktorips.attributename
     * and child(text)=faktorips.description.
     * 
     * @param parent rootContainer object.
     * @param attribute faktorips data
     * @param index
     */
    private void createExpandableControl(Composite parent, IAttribute attribute, int index) {
        
        ExpandableComposite excomposite = toolkit.createExpandableComposite(parent, 
                ExpandableComposite.TWISTIE|
                ExpandableComposite.COMPACT|
                ExpandableComposite.EXPANDED );
        
        // Set faktorips.attribute name
        excomposite.setText(StringUtils.capitalise(attribute.getName()));
        
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
        client.setText(attribute.getDescription(), false, true);        
        client.setBackground(excomposite.getBackground());
        client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        
        excomposite.setClient(client);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.Page#dispose()
     */
    public void dispose() {
        if (toolkit != null)
            toolkit.dispose();
        
        if (form != null)
            form.dispose();
        
        super.dispose();
    }
    
    /**
     * Set new model data. 
     * 
     * @param input IProductCmptType of the {@link ProductCmptEditor}
     */
    public void setInput(IProductCmptGeneration input) {
    	this.productCmptGen = input; 
    	
        // Set headline title    	
    	form.setText(productCmptGen.getProductCmpt().getName());
    	
        // Get names and descriptions
    	IConfigElement[] elements = productCmptGen.getConfigElements();
    	
        if (elements.length > 0)
        {
    		Arrays.sort(elements, new ConfigElementComparator());

            // collect all attributes in one container
            expandableContainer = toolkit.createComposite(form.getBody());
            
            // use TableWrapLayout for automated line wrap
            TableWrapLayout layout = new TableWrapLayout();
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 0;
            layout.numColumns = 1;
            
            expandableContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
            expandableContainer.setLayout(layout);
            
            int index = 2; // simple mechanism for color coding for lines 
                           // in alternating colors: odd/even
            
            for (int i=0;i < elements.length;i++) {
            	try {
            		IAttribute attribute = elements[i].findPcTypeAttribute();
            		
            		if (attribute != null)
            		{
                        createExpandableControl(expandableContainer, attribute, index++);
            		}
            	} catch (CoreException e) {
            		e.printStackTrace();
            	}
            }
        }
    }
    
    
}

