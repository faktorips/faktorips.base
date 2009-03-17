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
 * This class contributes a simple viewer for product cmpt type attributes and
 * {@link ITableStructure}:
 * <ul>
 * <li>Show the qualified name as title
 * <li>list specified attributes with their description
 * </ul>
 * <p>
 * The view is supposed to look like the cheatscheet view (ExpandableComposites) and behave like the
 * outline view. For this reason it derives {@link PageBookView}.
 * 
 * @see PageBookView
 * @see ContentOutline
 * 
 * @author Markus Blum
 */
public class ModelDescriptionView extends PageBookView {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ui.views.modelDescription"; //$NON-NLS-1$

    // default message: view not supported.
    final String notSupportedMessage = Messages.ModelDescriptionView_notSupported;

    /**
     * The <code>PageBookView</code> implementation of this <code>IWorkbenchPart</code> method
     * creates a <code>PageBook</code> control with its default page showing.
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
                page = ((IModelDescriptionSupport)part).createModelDescriptionPage();

                if (page == null) {
                    return null;
                }

            } catch (CoreException e) {
                IpsPlugin.log(e);

                page = createExceptionPage(e);
            }

            initPage((IPageBookViewPage)page);
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
        /*
         * care for all editors. If an editor does not support IModelDescriptionSupport we'll show a
         * 'description not supported' message.
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
