/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.util.StringUtil;

public abstract class IpsAbstractHandler extends AbstractHandler {

    protected static final String ARCHIVE_LINK = "ARCHIVE_LINK"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IIpsSrcFile ipsSrcFile = getCurrentlySelectedIpsSrcFile();
        if (ipsSrcFile == null) {
            return null;
        }

        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWindow.getActivePage();
        execute(event, activePage, ipsSrcFile);

        // return must be null - see jdoc
        return null;
    }

    protected TypedSelection<IAdaptable> getSelectionFromSelectionProvider() {
        TypedSelection<IAdaptable> typedSelection;
        ISelectionService selectionService = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        typedSelection = new TypedSelection<IAdaptable>(IAdaptable.class, selectionService.getSelection());
        return typedSelection;
    }

    protected TypedSelection<IAdaptable> getSelectionFromEditor(IWorkbenchPart part) {
        IEditorInput input = ((IEditorPart)part).getEditorInput();
        if (input instanceof IFileEditorInput) {
            return new TypedSelection<IAdaptable>(IAdaptable.class, new StructuredSelection(
                    ((IFileEditorInput)input).getFile()));
        } else {
            return null;
        }
    }

    public abstract void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException;

    protected IIpsSrcFile getCurrentlySelectedIpsSrcFile() {
        IWorkbenchWindow activeWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

        IWorkbenchPart part = activeWindow.getPartService().getActivePart();
        TypedSelection<IAdaptable> typedSelection;
        if (part instanceof IEditorPart) {
            typedSelection = getSelectionFromEditor(part);
        } else {
            typedSelection = getSelectionFromSelectionProvider();
        }
        if (typedSelection == null || !typedSelection.isValid()) {
            return null;
        }

        return (IIpsSrcFile)typedSelection.getFirstElement().getAdapter(IIpsSrcFile.class);
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
        List<Object> result = new ArrayList<Object>();
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

        List<Transfer> resultList = new ArrayList<Transfer>();
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
