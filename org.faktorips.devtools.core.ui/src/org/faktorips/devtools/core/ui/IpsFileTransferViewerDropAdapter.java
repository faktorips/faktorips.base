/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.NestedProjectFileUtil;

public abstract class IpsFileTransferViewerDropAdapter extends IpsViewerDropAdapter {

    private TransferData actualTransferType;
    private List<IProductCmpt> actualTransferElements;

    public IpsFileTransferViewerDropAdapter(Viewer viewer) {
        super(viewer);
    }

    private IProductCmpt getProductCmpt(IIpsElement element) {
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
     * <p>
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
            return new ArrayList<>();
        }

        List<IProductCmpt> result = new ArrayList<>();

        for (String filename : filenames) {
            IFile file = NestedProjectFileUtil.getFile(filename);
            if (file == null) {
                return null;
            }
            IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(file).as(AFile.class));
            if (element == null || !element.exists()) {
                return null;
            }
            IProductCmpt draggedCmpt = getProductCmpt(element);
            if (draggedCmpt == null) {
                return null;
            }
            result.add(draggedCmpt);
        }
        return result;
    }

}
