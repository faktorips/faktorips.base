/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.util.NestedProjectFileUtil;

/**
 * Abstract default implementation of a drop target listener. {@link #dragOver(DropTargetEvent)},
 * {@link #dragLeave(DropTargetEvent)} and {@link #dragOperationChanged(DropTargetEvent)} are
 * ignored by this implementation.
 */
public abstract class IpsElementDropListener implements IIpsElementDropListener {

    /**
     * {@inheritDoc}
     * <p>
     * Empty default implementation.
     */
    @Override
    public void dragLeave(DropTargetEvent event) {
        // Nothing done as default.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Empty default implementation.
     */
    @Override
    public void dragOperationChanged(DropTargetEvent event) {
        // Nothing done as default.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Empty default implementation.
     */
    @Override
    public void dragOver(DropTargetEvent event) {
        // Nothing done as default.
    }

    /**
     * For each transfered item, this method does the following three steps in the given order and
     * adds the last successful result to the returned array:
     * <p>
     * First: get the filename - if this first step fails, the return value is <code>null</code>.
     * <p>
     * Second: get the {@link IResource} for the filename
     * <p>
     * Third: get the {@link IIpsElement} for the resource
     * <p>
     * Example: We transfer the String "EGON" (which is not a valid resource) - so the first step is
     * successful, but the second one will fail. So the String "EGON" is added to the result.
     * 
     * Another Example: We transfer the String "model/Readme" (which is a valid resource, but not a
     * valid {@link IIpsElement}) - Step one and two are successful, but the third one is not, so
     * the {@link IResource} (an {@link IFile} in this example) is added to the result.
     * 
     * <p>
     * Note for Linux: If this method is called during drag action (e.g. called by dropAccept
     * method) the <code>transferData</code> is not set correctly and this method returns
     * <code>null</code>. If you want to check files during drag action you have to use the method
     * {@link ByteArrayTransfer#isSupportedType(TransferData)}
     * 
     */
    protected Object[] getTransferedElements(TransferData transferData) {
        String[] filenames = (String[])getTransfer().nativeToJava(transferData);
        if (filenames == null) {
            return null;
        }
        ArrayList<Object> elements = new ArrayList<>();
        for (String filename : filenames) {
            addElementFromResource(elements, NestedProjectFileUtil.getFile(filename));

            IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(new Path(filename));
            addElementFromResource(elements, container);
        }
        return elements.toArray(new Object[elements.size()]);
    }

    private void addElementFromResource(List<Object> result, IResource resource) {
        if (resource == null) {
            return;
        }

        if (resource.exists()) {
            IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
            if (element != null && element.exists()) {
                result.add(handlePackageFragmentRoot(element));
            } else {
                result.add(resource);
            }
        }
    }

    private IIpsElement handlePackageFragmentRoot(IIpsElement element) {
        /*
         * Moving package fragment roots does not make sense, so in this case the default package is
         * meant to be moved.
         */
        if (element instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot packRoot = (IIpsPackageFragmentRoot)element;
            element = packRoot.getDefaultIpsPackageFragment();
        }
        return element;
    }

    @Override
    public Transfer[] getSupportedTransfers() {
        ArrayList<Transfer> result = new ArrayList<>();
        result.add(FileTransfer.getInstance());
        return result.toArray(new Transfer[result.size()]);
    }

    protected FileTransfer getTransfer() {
        return FileTransfer.getInstance();
    }

    @Override
    public List<IIpsElement> getDraggedElements(TransferData transferData) {
        Object[] found = getTransferedElements(transferData);

        ArrayList<IIpsElement> result = new ArrayList<>();

        for (Object object : found) {
            if (object instanceof IIpsElement) {
                result.add((IIpsElement)object);
            }
        }

        return result;
    }

}
