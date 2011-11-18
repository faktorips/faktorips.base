/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

public abstract class IpsFileTransferViewerDropAdapter extends IpsViewerDropAdapter {

    private TransferData actualTransferType;
    private List<IProductCmpt> actualTransferElements;

    public IpsFileTransferViewerDropAdapter(Viewer viewer) {
        super(viewer);
    }

    private IFile getFile(String filename) {
        return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename));
    }

    private IProductCmpt getProductCmpt(IIpsElement element) throws CoreException {
        if (element instanceof IIpsSrcFile
                && ((IIpsSrcFile)element).getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            return (IProductCmpt)((IIpsSrcFile)element).getIpsObject();
        } else {
            return null;
        }
    }

    /**
     * Get the transfered (dragged) elements. Returns null if there are no elements, the transfer
     * type is wrong or there is at least one invalid element.
     * <p/>
     * On some systems it is not possible to determine the concrete object while dragging. In this
     * case the method returns an empty array. (e.g. linux)
     */
    protected List<IProductCmpt> getTransferElements(TransferData transferType) {
        if (transferType == null) {
            return null;
        }
        if (transferType.equals(actualTransferType)) {
            return actualTransferElements;
        }
        if (FileTransfer.getInstance().isSupportedType(transferType)) {
            String[] filenames = (String[])FileTransfer.getInstance().nativeToJava(transferType);
            List<IProductCmpt> productCmpts = getProductCmpts(filenames);
            if (productCmpts != null) {
                actualTransferElements = productCmpts;
                actualTransferType = transferType;
                return actualTransferElements;
            }
        }
        actualTransferType = null;
        actualTransferElements = null;
        return null;
    }

    protected List<IProductCmpt> getProductCmpts(String[] filenames) {
        // Under some platforms (linux), the data is not available during dragOver.
        if (filenames == null) {
            return new ArrayList<IProductCmpt>();
        }

        List<IProductCmpt> result = new ArrayList<IProductCmpt>();

        for (String filename : filenames) {
            IFile file = getFile(filename);
            if (file == null) {
                return null;
            }
            try {
                IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
                if (element == null || !element.exists()) {
                    return null;
                }
                IProductCmpt draggedCmpt = getProductCmpt(element);
                if (draggedCmpt == null) {
                    return null;
                }
                result.add(draggedCmpt);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }
        return result;
    }

}
