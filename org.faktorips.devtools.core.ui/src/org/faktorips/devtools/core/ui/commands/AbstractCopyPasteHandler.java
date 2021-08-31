/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.StringUtil;

public abstract class AbstractCopyPasteHandler extends AbstractHandler {

    protected static final String ARCHIVE_LINK = "ARCHIVE_LINK"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     * <p>
     * The Faktor-Ips handlers should only handle the {@link ExecutionEvent ExecutionEvents} if they
     * are called from a {@link Tree} e.g. package- , model- or project-explorer.
     */
    @Override
    public boolean isHandled() {
        Display display = Display.getCurrent();
        if (display == null) {
            throw new CoreRuntimeException(
                    MessageFormat.format("This {0} can only be called in a user-interface thread.", this.getClass())); //$NON-NLS-1$
        }
        return display.getFocusControl() instanceof Tree;
    }

    /**
     * Extracted as protected method to get along with one suppress warnings annotation.
     */
    @SuppressWarnings("unchecked")
    protected static Iterator<Object> getSelectionIterator(IStructuredSelection selection) {
        return selection.iterator();
    }

    /**
     * Builds the data-array for clipboard operations (copy, drag,...).
     * 
     * @param copiedObjects The list of {@link IpsObjectPartState IpsObjectPartStates} to put to the
     *            clipboard
     * @param resourceItems The list of resources to put to the clipboard
     * @param copiedResourceLinks The list of resource links to put in the clipboard
     */
    protected static Object[] getDataArray(List<IpsObjectPartState> copiedObjects,
            List<IResource> resourceItems,
            List<String> copiedResourceLinks) {
        List<Object> result = new ArrayList<>();
        // add copied resources
        if (resourceItems.size() > 0) {
            IResource[] res = new IResource[resourceItems.size()];
            result.add(resourceItems.toArray(res));
        }
        // add copied resource links (e.g. links to an object inside an archiv)
        // all links will be merged to one text inside the clipboard
        if (copiedResourceLinks != null) {
            String strReferences = ""; //$NON-NLS-1$
            for (String element : copiedResourceLinks) {
                strReferences += strReferences.length() > 0 ? "," + element : element; //$NON-NLS-1$
            }
            if (StringUtils.isNotEmpty(strReferences)) {
                result.add(ARCHIVE_LINK + strReferences);
            }
        }
        // add copied states
        if (copiedObjects != null && !copiedObjects.isEmpty()) {
            result.add(copiedObjects.toArray(new IpsObjectPartState[copiedObjects.size()]));
        }
        return result.toArray();
    }

    /**
     * Returns the apropriate Transfer for every item in the given lists in the same order as the
     * data is returend in getDataArray.
     */
    protected static Transfer[] getTypeArray(List<IpsObjectPartState> copiedObjects,
            List<IResource> resourceItems,
            List<String> copiedResourceLinks) {

        List<Transfer> resultList = new ArrayList<>();
        if (resourceItems.size() > 0) {
            resultList.add(ResourceTransfer.getInstance());
        }
        if (copiedResourceLinks != null && copiedResourceLinks.size() > 0) {
            // the links will be merged to one text inside the clipboard
            resultList.add(TextTransfer.getInstance());
        }
        if (copiedObjects != null && !copiedObjects.isEmpty()) {
            resultList.add(IpsObjectPartStateListTransfer.getWriteInstance());
        }
        Transfer[] result = new Transfer[resultList.size()];
        return resultList.toArray(result);
    }

    public static String getResourceLinkInArchive(IIpsObject ipsObject) {
        IIpsPackageFragmentRoot root = ipsObject.getIpsPackageFragment().getRoot();
        String srcFileName = ipsObject.getIpsSrcFile().getName();
        String content = root.getIpsProject().getName() + "#" + root.getName() + "#" + ipsObject.getQualifiedName() //$NON-NLS-1$ //$NON-NLS-2$
                + "#" + StringUtil.getFileExtension(srcFileName); //$NON-NLS-1$
        return content;
    }

    public static String getResourceLinkInArchive(IIpsPackageFragment fragment) {
        IIpsPackageFragmentRoot root = fragment.getRoot();
        String content = root.getIpsProject().getName() + "#" + root.getName() + "#" + fragment.getName(); //$NON-NLS-1$ //$NON-NLS-2$
        return content;
    }

}
