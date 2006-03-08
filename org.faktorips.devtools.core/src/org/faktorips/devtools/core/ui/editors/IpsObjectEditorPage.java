/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;


/**
 *
 */
public abstract class IpsObjectEditorPage extends FormPage {
    
    // the space between two sections 
    public final static int HORIZONTAL_SECTION_SPACE = 15;
    public final static int VERTICAL_SECTION_SPACE = 10;

    /**
     * @param editor	The editor the page belongs to.
     * @param id		Page id used to identify the page.
     * @param title		The title shown at the top of the page when the page is selected.
     * @param title	The page name shown at the bottom of the editor as tab page. 
     */
    public IpsObjectEditorPage(
            IpsObjectEditor editor, 
            String id, 
            String tabPageName) {
        super(editor, id, tabPageName);
    }
    
    protected IpsObjectEditor getPdObjectEditor() {
        return (IpsObjectEditor)getEditor();
    }
    
    protected IIpsObject getPdObject() {
        return getPdObjectEditor().getIpsObject();
    }
    
	protected final void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		form.setText(getPdObjectEditor().getUniformPageTitle());
		FormToolkit toolkit = managedForm.getToolkit();
		createPageContent(form.getBody(), new UIToolkit(toolkit));
	}
	
	protected abstract void createPageContent(Composite formBody, UIToolkit toolkit);
	
	/**
	 * Creates a grid layout for the page with the indicated number of columns
	 * and the default margins.
	 * 
	 * @param numOfColumns	Number of columns in the grid.
	 * @param equalSize		True if the columns should have the same size
	 */
	protected GridLayout createPageLayout(int numOfColumns, boolean equalSize) {
	    GridLayout layout = new GridLayout(numOfColumns, equalSize);
	    layout.marginHeight = 10;
	    layout.marginWidth = 10;
	    layout.verticalSpacing = VERTICAL_SECTION_SPACE;
	    layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;
	    return layout;
	}
	
	/**
	 * Creates a grid composite for the inner page structure. The composite
	 * has no margins but the default spacing settings.
	 * 
	 * @param numOfColumns	Number of columns in the grid.
	 * @param equalSize		True if the columns should have the same size
	 */
	protected Composite createGridComposite(
	        UIToolkit toolkit, 
	        Composite parent, 
	        int numOfColumns, 
	        boolean equalSize,
	        int gridData) {
	    Composite composite = toolkit.getFormToolkit().createComposite(parent);
	    GridLayout layout = new GridLayout(numOfColumns, equalSize);
	    layout.marginHeight = 0;
	    layout.marginWidth = 0;
	    layout.verticalSpacing = VERTICAL_SECTION_SPACE;
	    layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;
	    composite.setLayout(layout);
	    composite.setLayoutData(new GridData(gridData));
	    return composite;
	}
	
    /**
     * Default implementation refreshs all ancestors that are instances
     * of <code>IpsSection</code>. By ancestors we mean the children of the
     * composite that represents this page and their children.
     * <p>
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage#refresh()
     */
    protected void refresh() {
        if (!(getPartControl() instanceof Composite)) {
            return;
        }
        refresh((Composite)getPartControl());
    }
    
    private void refresh(Composite composite) {
        Control[] children =composite.getChildren();
        for (int i=0; i<children.length; i++) {
            if (children[i] instanceof IpsSection) {
                ((IpsSection)children[i]).refresh();
            }
            else if (children[i] instanceof Composite) {
                refresh((Composite)children[i]);
            }
        }
        
    }
    
    

}
