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

import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

/**
 * This plugin contributes a simple viewer for @see IProductCmptType attributes and {@link ITableStructure}:
 *  - Show the qualified name as title
 *  - and list specified attributes with their description.  
 * 
 * The view is supposed to look like the cheatscheet view (ExpandableComposites) and
 * behave like the outline view. For this reason it derives {@link PageBookView}.
 * 
 * @see PageBookView
 * @see ContentOutline
 * 
 * @author Markus Blum
 */
public class ModelDescriptionView extends PageBookView {
    
    // default message: view not supported.
    final String notSupportedMessage = Messages.ModelDescriptionView_notSupported; 
        
    /**
     * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code>
     * method creates a <code>PageBook</code> control with its default page showing.
     */
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    }
    
	/**
	 * {@inheritDoc}
	 */
	protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        page.setMessage(notSupportedMessage);
        return page;
	}

    /**
     * Create a page for showing the localized message of an excpetion.
     * 
     * @param e Exception
     * @return new page with exception message.
     */
    private IPage createExceptionPage(CoreException e) {
        MessagePage page = new MessagePage();
        page.setMessage(e.getLocalizedMessage());
        return page;
    }
	/**
	 * {@inheritDoc}
	 */
	protected PageRec doCreatePage(IWorkbenchPart part) {
		
		if (part instanceof IModelDescriptionSupport) {
            
			IPage page;
            try {
                page = ((IModelDescriptionSupport) part).createModelDescriptionPage();
            } catch (CoreException e) {
                 IpsPlugin.log(e);
                
                page = createExceptionPage(e);
            }
			
            initPage((IPageBookViewPage) page);
			page.createControl(getPageBook());
			return new PageRec(part, page);				
		}
           
        return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IPage page = rec.page;
        page.dispose();
        rec.dispose();
	}

    /**
     * {@inheritDoc}
     */
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
			return page.getActiveEditor();
		}

        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isImportant(IWorkbenchPart part) {
        /* care for all editors. If an editor does not support IModelDescriptionSupport
         * we'll show a 'description not supported' message.
         */
        
        return (part instanceof IEditorPart);
    }

    /**
     * {@inheritDoc}
     */
    public void partBroughtToTop(IWorkbenchPart part) {
			partActivated(part);
    }

}
