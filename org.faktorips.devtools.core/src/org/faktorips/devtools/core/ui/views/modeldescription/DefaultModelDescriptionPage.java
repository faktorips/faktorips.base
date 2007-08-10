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

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.Page;
import org.faktorips.devtools.core.IpsPlugin;

abstract public class DefaultModelDescriptionPage extends Page {

    // SWT basics
    private FormToolkit toolkit;

    // basic view elements
    private ScrolledForm form;
    private Composite expandableContainer;
    
    // Data
    private List defaultList;
    private List activeList;
    private String title; 
    
    public DefaultModelDescriptionPage () {
        defaultList = new ArrayList();
        activeList= new ArrayList();        
    }
    
    /**
     * {@inheritDoc}
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
        
        registerToolbarActions();
	}
    
    private void registerToolbarActions() {
        // register global actions
        IToolBarManager toolBarManager= getSite().getActionBars().getToolBarManager();
        
        toolBarManager.add(new LexicalSortingAction(this));
    }
    
    private void createForm() {
        
        // Set headline title
        form.setText(title);

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
        
        for (int i=0;i < activeList.size();i++) {
            createExpandableControl(expandableContainer, (DescriptionItem) activeList.get(i), index++);
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
        String text;
        
        // TODO check for whitespaces
        if (item.getDescription().length() > 0) {
            text = item.getDescription();
        } else {
            text = new String(Messages.DefaultModelDescriptionPage_NoDescriptionAvailable);
        }
        
        client.setText(text, false, true);        
        client.setBackground(excomposite.getBackground());
        client.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        
        excomposite.setClient(client);
        
        // paint "whitespace"
        Label label = toolkit.createLabel(parent, " "); //$NON-NLS-1$
        label.setSize(1,1);
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		
		if (toolkit != null) {
			toolkit.dispose();
		}
		
		if (form != null) {
			form.dispose();
		}
		
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		if (form == null) {
			return null;
		}
		
		return form;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFocus() {
        if (form == null) {
        	return;
        }
        
        form.setFocus();		
	}

    /**
     * @param productName
     * @param itemList
     */
    public void setInput(String productName, DescriptionItem[] itemList) {
        defaultList.addAll(Arrays.asList(itemList));
        activeList.addAll(Arrays.asList(itemList));
        title = productName;
    }
    
    /**
     * 
     */
    public void refresh() {
        if (expandableContainer != null) { 
           expandableContainer.dispose();
        }
        
        createForm();
        
        form.layout(false, true);
    }

    class LexicalSortingAction extends Action {
        
        private DefaultModelDescriptionSorter defaultSorter;
        private LexicalModelDescriptionSorter lexicalSorter;
        private DefaultModelDescriptionPage page;

        public LexicalSortingAction(DefaultModelDescriptionPage page) {
            super();
            
            this.page = page;
            defaultSorter = new DefaultModelDescriptionSorter();
            lexicalSorter = new LexicalModelDescriptionSorter();
            
            setText(Messages.DefaultModelDescriptionPage_SortText);
            setToolTipText(Messages.DefaultModelDescriptionPage_SortTooltipText);
            setDescription(Messages.DefaultModelDescriptionPage_SortDescription);
            
            // get image: "alphabetical sort enabled"
            ImageDescriptor descriptor = IpsPlugin.getDefault().getImageDescriptor("elcl16/alphab_sort_co.gif"); //$NON-NLS-1$
            this.setHoverImageDescriptor(descriptor);
            this.setImageDescriptor(descriptor); 
            
            boolean checked = IpsPlugin.getDefault().getPreferenceStore().getBoolean("DefaultModelDescriptionPage.LexicalSortingAction.isChecked"); //$NON-NLS-1$
            valueChanged(checked, false);        
        }

        public void run() {
            valueChanged(isChecked(), true);
        }

        private void valueChanged(final boolean on, boolean store) {
            setChecked(on);
            
            BusyIndicator.showWhile(page.getControl().getDisplay(), new Runnable() {
                public void run() {
                    if (on) {
                        lexicalSorter.sort(page);
                    } else {
                        defaultSorter.sort(page);
                    }
                }
            });         
            
            if (store) {
                IpsPlugin.getDefault().getPreferenceStore().setValue("DefaultModelDescriptionPage.LexicalSortingAction.isChecked", store);  //$NON-NLS-1$
            }
        }
    }

    class LexicalModelDescriptionSorter {
        
         /**
         * {@inheritDoc}
         */
        public void sort(DefaultModelDescriptionPage page) {
                    
            Collections.sort(activeList, new DescriptionItemComparator());
            refresh();
        }    
    }

    class DefaultModelDescriptionSorter {

        /**
         * {@inheritDoc}
         */
        public void sort(DefaultModelDescriptionPage page) {
            
            Collections.copy(activeList, defaultList);
            refresh();
            
        }
    }

    class DescriptionItemComparator implements Comparator {

        /**
         * {@inheritDoc}
         */
        public int compare(Object o1, Object o2) {
            
            if (o1 instanceof DescriptionItem) {
                DescriptionItem item1 = (DescriptionItem)o1;
             
                if (o2 instanceof DescriptionItem) {
                    DescriptionItem item2 = (DescriptionItem)o2;
                    
                    return item1.getName().compareTo(item2.getName());
                }
            }
            
            return 0;
        }
    }    
    
}