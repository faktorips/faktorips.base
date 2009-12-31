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

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * Abstract default implementation of a drop target listener. Drag over and drag leave are ignored
 * by this implementation, dargOperationChanged too.
 * 
 * @author Thorsten Guenther
 */
public abstract class IpsElementDropListener implements DropTargetListener {

    /**
     * {@inheritDoc}
     * <p>
     * Empty default implementation.
     */
    public void dragLeave(DropTargetEvent event) {
        // Nothing done as default.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Empty default implementation.
     */
    public void dragOperationChanged(DropTargetEvent event) {
        // Nothing done as default.
    }

    /**
     * {@inheritDoc}
     * <p>
     * Empty default implementation.
     */
    public void dragOver(DropTargetEvent event) {
        // Nothing done as default.
    }

    /**
     * Returns all <tt>IIpsElement</tt>s transferred as files by the given <tt>TransferData</tt> as
     * array.
     * <p>
     * Note for Linux: If this method is called during drag action (e.g. called by dropAccept
     * method) the <tt>transferData</tt> is not set correctly and this method returns <tt>null</tt>.
     * If you want to check files during drag action you have to use the method
     * {@link ByteArrayTransfer#isSupportedType(TransferData)}
     * 
     */
    protected Object[] getTransferedElements(TransferData transferData) {
        String[] filenames = (String[])getTransfer().nativeToJava(transferData);
        if (filenames == null) {
            return null;
        }
        ArrayList<Object> elements = new ArrayList<Object>();
        for (int i = 0; i < filenames.length; i++) {
            Path path = new Path(filenames[i]);

            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
            IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
            if (file != null) {
                // getFileForLocation returns a file even if the path points to a folder.
                // In this case file.exists() returns false.
                if (file.exists()) {
                    IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(file);
                    if (element != null && element.exists()) {
                        elements.add(element);
                    } else {
                        elements.add(file);
                    }
                }
            } else {
                elements.add(filenames[i]);
            }
            if (container != null) {
                if (container.exists()) {
                    IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(container);
                    if (element != null && element.exists()) {
                        elements.add(element);
                    } else {
                        elements.add(container);
                    }
                }
            }
        }
        return elements.toArray(new Object[elements.size()]);
    }

    protected FileTransfer getTransfer() {
        return FileTransfer.getInstance();
    }

}
