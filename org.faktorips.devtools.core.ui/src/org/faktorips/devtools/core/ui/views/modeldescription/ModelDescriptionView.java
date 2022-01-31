/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * This class contributes a simple viewer for object classes implementing IModelDescriptionSupport
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
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
    }

    @Override
    protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        page.setMessage(notSupportedMessage);
        return page;
    }

    /**
     * Create a page for showing the localized message of an excetpion.
     * 
     * @param e Exception
     * @return new page with exception message.
     */
    private IPage createExceptionPage(CoreRuntimeException e) {
        MessagePage page = new MessagePage();
        page.setMessage(e.getLocalizedMessage());
        return page;
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        if (part instanceof IModelDescriptionSupport) {
            IPage page;
            try {
                page = ((IModelDescriptionSupport)part).createModelDescriptionPage();

                if (page == null) {
                    return null;
                }

            } catch (CoreRuntimeException e) {
                IpsPlugin.log(e);
                page = createExceptionPage(e);
            }
            initPage((IPageBookViewPage)page);
            page.createControl(getPageBook());
            return new PageRec(part, page);
        }

        return null;
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
        IPage page = rec.page;
        page.dispose();
        rec.dispose();
    }

    @Override
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null) {
            return page.getActiveEditor();
        }

        return null;
    }

    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        /*
         * care for all editors. If an editor does not support IModelDescriptionSupport we'll show a
         * 'description not supported' message.
         */

        return (part instanceof IEditorPart);
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
        partActivated(part);
    }

}
