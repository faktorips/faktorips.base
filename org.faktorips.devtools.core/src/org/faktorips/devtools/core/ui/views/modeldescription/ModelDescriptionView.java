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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.views.contentoutline.ContentOutline;

/**
 * This plugin contributes a simple viewer for @see IProductCmptType attributes:
 *  - Show the qualified name as title
 *  - and list specified attributes with their description.  
 * 
 * The view is supposed to look like the cheatscheet view (ExpandableItems) and
 * behave like the outline view. For this reason it derives PageBookView.
 * 
 * @see PageBookView
 * @see ContentOutline
 * 
 * @author Markus Blum
 */
public class ModelDescriptionView extends PageBookView {
    
    // default message
    final String notSupportedMessage = Messages.ModelDescriptionView_notSupported; 
        
    /**
     * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>PageBook</code> control with its default page showing.
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    }
    
    /* (non-Javadoc)
     * Method declared on PageBookView.
     */   
	protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        page.setMessage(notSupportedMessage);
        return page;
	}

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */	
	protected PageRec doCreatePage(IWorkbenchPart part) {
		
		if (part instanceof IModelDescriptionSupport) {				
			IPage page = ((IModelDescriptionSupport) part).createModelDescriptionPage();
			initPage((IPageBookViewPage) page);
			page.createControl(getPageBook());
			return new PageRec(part, page);				
		}
           
        return null;
	}

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */	
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IPage page = rec.page;
        page.dispose();
        rec.dispose();
	}

    /* (non-Javadoc)
     * Method declared on PageBookView.
     */
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
			return page.getActiveEditor();
		}

        return null;
    }

    /* (non-Javadoc)
     * Method declared on PageBookView.
     * We only want to track editors.
     */
    protected boolean isImportant(IWorkbenchPart part) {
        //We only care about IpsObjectEditors
        return (part instanceof IEditorPart);
    }

    /* (non-Javadoc)
     * Method declared on IViewPart.
     * Treat this the same as part activation.
     */
    public void partBroughtToTop(IWorkbenchPart part) {
			partActivated(part);
    }

}
