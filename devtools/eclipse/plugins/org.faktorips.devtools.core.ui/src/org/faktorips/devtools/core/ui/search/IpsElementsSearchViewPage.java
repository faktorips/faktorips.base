/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.actions.OpenEditorAction;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;

/**
 * An implementation of the {@link ISearchResultPage} for searches of {@link IIpsElement
 * IIpsElements}.
 * <p>
 * The IpsElementsSearchViewPage does not support the table layout
 * {@link AbstractTextSearchViewPage#FLAG_LAYOUT_FLAT}
 * 
 * 
 * @author dicker
 */
public class IpsElementsSearchViewPage extends AbstractTextSearchViewPage {

    private IpsSearchResultLabelProvider labelProvider;
    private IpsSearchResultTreePathContentProvider contentProvider;

    public IpsElementsSearchViewPage() {
        super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
    }

    @Override
    public StructuredViewer getViewer() {
        // increase viewer visibility
        return super.getViewer();
    }

    @Override
    protected synchronized void elementsChanged(Object[] objects) {
        if (objects.length == 0) {
            contentProvider.elementsChanged(contentProvider.getFoundIpsElements());
        } else {
            contentProvider.elementsChanged(objects);
        }
    }

    @Override
    protected void clear() {
        contentProvider.clear();
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        if (labelProvider == null) {
            labelProvider = new IpsSearchResultLabelProvider();
        }
        if (contentProvider == null) {
            // contentProvider = new SearchResultContentProvider(this);
            contentProvider = new IpsSearchResultTreePathContentProvider(this);
        }
        viewer.setLabelProvider(labelProvider);
        viewer.setContentProvider(contentProvider);
        viewer.setUseHashlookup(true);
    }

    /**
     * This method does nothing, because the table layout is not supported for the search result
     */
    @Override
    protected void configureTableViewer(TableViewer viewer) {
        // nothing to do
    }

    @Override
    protected void fillContextMenu(IMenuManager mgr) {
        mgr.appendToGroup(IContextMenuConstants.GROUP_OPEN, new OpenEditorAction(getViewer()));
        IIpsSrcFile ipsSrcFile = getIpsSrcFileForSelection();
        if (ipsSrcFile != null && IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {
            mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, new Separator(IpsMenuId.GROUP_NAVIGATE.getId()));
        }
        super.fillContextMenu(mgr);
    }

    private IIpsSrcFile getIpsSrcFileForSelection() {
        Object selection = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
        // retrieve first element of the selection
        return getCorrespondingIpsSrcFile(selection);
    }

    /**
     * Returns an corresponding {@link IIpsSrcFile} out of the given parameter. If there is no
     * corresponding IIpsSrcFile, {@code null} is returned.
     * <p>
     * The method is called to sort the result and fill the context menu.
     * <p>
     * If the object is an {@link IIpsObject} or an {@link IIpsObjectPart}, the corresponding
     * IIpsSrcFile is returned. If the parameter is an Object[], the first element is checked,
     * whether it is an IIpsObject or IIpsObjectPart.
     * 
     */
    protected IIpsSrcFile getCorrespondingIpsSrcFile(Object object) {
        Object selection = object;

        if (selection instanceof Object[]) {
            selection = ((Object[])selection)[0];
        }
        if (selection instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)selection).getIpsObject().getIpsSrcFile();
        }
        if (selection instanceof IIpsObject) {
            return ((IIpsObject)selection).getIpsSrcFile();
        }
        return null;
    }

    @Override
    protected void handleOpen(OpenEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection) {
            new OpenEditorAction(getViewer()).run((IStructuredSelection)selection);
            return;
        }
        super.handleOpen(event);
    }

}
