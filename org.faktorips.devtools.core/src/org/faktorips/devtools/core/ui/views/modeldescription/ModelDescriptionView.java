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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IPartListener;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * This plugin contributes a simple viewer for @see IPolicyCmptType attributes:
 *  - Show the qualified name as title
 *  - and list specified attributes with their description.  
 * 
 *  AttributesTypes are shown at the moment by:
 *  - productrelevant property
 *  - contant property
 *  - derived property
 * 
 * The view is supposed to function like the cheatscheet view (ExpandableItems) and
 * the outline view (focus on editor selection).
 * 
 * @author Markus Blum
 */
public class ModelDescriptionView extends ViewPart implements IPartListener {
    
    // SWT basics
    private FormToolkit toolkit;
    private Composite   root; // parent composite of the ModelDescriptionView
    
    // default view
    final String notSupportedMessage = "Es ist kein Produkt im Modell-Explorer ausgewählt.";
    
    // active view
    private ScrolledForm activeForm;    
    private Composite expandableContainer; // Container for ExpandableComposite widgets
    
    private ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
            if (sourcepart != ModelDescriptionView.this &&
                selection instanceof IStructuredSelection) {
                
                IStructuredSelection sel = (IStructuredSelection) selection;
                Object obj = sel.getFirstElement();
                
                if (obj instanceof IPolicyCmptType) {
                    if (root != null) {    
                        createDescriptionView(root, (IPolicyCmptType) obj);
                    }
                }                    
            }
        }
    };
          
    public ModelDescriptionView() {
    }
    
    /**
     * Create within the forms element a rootContainer for all ExpandableComposite
     * objects. Get the input data for each single ExpandableComposite from 
     * the selected faktorIPS model.
     * 
     * @param pcType Selected IPolicyCmptType.
     */
    private void createDescriptionView(Composite parent, IPolicyCmptType pcType) {
        
        // Set page title
        activeForm.setText(pcType.getQualifiedName());
        Color enabledColor = activeForm.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        activeForm.setBackground(enabledColor);            
        
        // Dispose entries of a previous selection.
        if (expandableContainer != null) {
            flushViewer(expandableContainer);
        }

        // Get attributenames and descriptions
        IAttribute[] attributes = pcType.getAttributes();
                    
        if (attributes.length > 0)
        {
            // collect all attributes in one container
            expandableContainer = toolkit.createComposite(activeForm.getBody());
            
            // use TableWrapLayout for automated line wrap
            TableWrapLayout layout = new TableWrapLayout();
            layout.verticalSpacing = 0;
            layout.horizontalSpacing = 0;
            layout.numColumns = 1;
            
            expandableContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
            expandableContainer.setLayout(layout);
            
            int index = 2; // simple mechanism for color coding following lines 
                          // in alternating colors: odd/even
            
            for (int i=0;i < attributes.length;i++) {
                // Show attributes only that:
                // - are productrelevant and
                // - constant or
                // - derived
                
                if (attributes[i].isProductRelevant() && (attributes[i].isDerived() || attributes[i].getAttributeType()==AttributeType.CONSTANT)) {
                    DescriptionViewItem viewItem = new DescriptionViewItem(attributes[i]);
                    createExpandableControl(expandableContainer, viewItem, index++);
                }
            }
        }
        
        // TODO Analyse repaint strategy
        activeForm.layout(true,true);
    }

    /**
     * 
     */
    private void flushViewer(Composite parent) {
        parent.dispose();
    }

    /**
     * Create a single ExpandableComposite object with name=faktorips.attributename
     * and child(text)=faktorips.description.
     * 
     * @param parent rootContainer object.
     * @param viewItem faktorips data
     * @param index
     */
    private void createExpandableControl(Composite parent, DescriptionViewItem viewItem, int index) {
        
        ExpandableComposite excomposite = toolkit.createExpandableComposite(parent, 
                ExpandableComposite.TWISTIE|
                ExpandableComposite.COMPACT|
                ExpandableComposite.EXPANDED );
        
        // Set faktorips.attribute name
        excomposite.setText(viewItem.getAttributeName());
        
        if ((index % 2) == 0) {
            // TODO Configure color from faktorIPS style
            Color grey = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            excomposite.setBackground(grey);
        }
        
        excomposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
        excomposite.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                activeForm.reflow(true);
            }
        });
        
        // Set faktorips.attribute description
        FormText client = toolkit.createFormText(excomposite, true);
        client.setText(viewItem.getDescription(), false, true);        
        client.setBackground(excomposite.getBackground());
        
        excomposite.setClient(client);        
    }
        
    private void createDefaultView(Composite parent) {
        
        Color disabledColor = activeForm.getDisplay().getSystemColor(SWT.COLOR_GRAY);
        activeForm.setBackground(disabledColor);
        
        // TODO Use a different UI component for hiding the default message!!!!
        // woraround!
        activeForm.setText(notSupportedMessage);
    }
    
    public void createPartControl(Composite parent) {
        
        // Save parent composite to instance.
        root = parent;

        // Listen to events from faktorips
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);

        // Use form-styled widgets in this view
        toolkit = new FormToolkit(parent.getDisplay());

        activeForm = toolkit.createScrolledForm(parent);
        
        // Using TableWrapLayout
        TableWrapLayout layoutForm = new TableWrapLayout();
        layoutForm.verticalSpacing = 1;
        layoutForm.horizontalSpacing = 1;
        layoutForm.numColumns = 1;

        activeForm.getBody().setLayout(layoutForm);
        
        createDefaultView(parent);
    }
    
    public void dispose() {
        if (toolkit != null)
            toolkit.dispose();
        
        if (activeForm != null)
            activeForm.dispose();

        if (activeForm != null)
            activeForm.dispose();
        
        ISelectionService s = getSite().getWorkbenchWindow().getSelectionService();
        s.removeSelectionListener(selectionListener);
        
        super.dispose();
    }

    public void setFocus() {
        
        if (activeForm != null) {
            activeForm.setFocus();   
        }
    }

    protected boolean isImportant(IWorkbenchPart part) {
        //We only care about editors
        return (part instanceof IEditorPart);
    }
    
    /**
     * {@inheritDoc}
     */
    public void partActivated(IWorkbenchPart part) {
        
        // Is this an important part? If not just return.
        if (!isImportant(part)) {
            return;
        }
        
        // Dummy
        System.out.println("Editor geöffnet: " + part.getTitle() );
        
    }

    /**
     * {@inheritDoc}
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        // Nothing to do
        
    }

    /**
     * {@inheritDoc}
     */
    public void partClosed(IWorkbenchPart part) {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     */
    public void partDeactivated(IWorkbenchPart part) {
        // TODO Auto-generated method stub
        
    }

    /**
     * {@inheritDoc}
     */
    public void partOpened(IWorkbenchPart part) {
        // TODO Auto-generated method stub
        
    }    
}
