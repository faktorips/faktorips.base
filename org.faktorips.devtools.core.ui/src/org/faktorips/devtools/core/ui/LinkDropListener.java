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

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;

public class LinkDropListener extends ViewerDropAdapter {

    private TransferData actualTransferType;

    private List<IProductCmpt> actualTransferElements;

    private LinkCreatorUtil linkCreator;

    private IProductCmptLink movedCmptLink;

    public LinkDropListener(Viewer viewer) {
        super(viewer);
        setFeedbackEnabled(false);
        setLinkCreator(new LinkCreatorUtil(true));
    }

    /**
     * @param autoSave The autoSave to set.
     */
    public void setAutoSave(boolean autoSave) {
        linkCreator.setAutoSave(autoSave);
    }

    /**
     * @return Returns the autoSave.
     */
    public boolean isAutoSave() {
        return linkCreator.isAutoSave();
    }

    @Override
    public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_NONE) {
            if (movedCmptLink == null) {
                event.detail = DND.DROP_LINK;
            } else {
                event.detail = DND.DROP_MOVE;
            }
        }
        System.out.println("Enter " + event.detail);
        super.dragEnter(event);
        System.out.println("After Enter " + event.detail);
        // if (movedCmptLink != null) {
        // return;
        // }
        // if ((event.detail & DND.DROP_DEFAULT) == DND.DROP_DEFAULT || event.detail ==
        // DND.DROP_NONE) {
        // event.detail = DND.DROP_LINK;
        // }
        // super.dragEnter(event);
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        System.out.println(target + "   -- " + operation);
        if (target == null) {
            return false;
        }
        if (movedCmptLink != null) {
            if (target instanceof IProductCmptLink) {
                IProductCmptLink targetCmptLink = (IProductCmptLink)target;
                boolean result;
                if (targetCmptLink.getAssociation().equals(movedCmptLink.getAssociation())) {
                    result = true;
                } else {
                    List<IProductCmpt> draggedCmpts = new ArrayList<IProductCmpt>();
                    draggedCmpts.add(movedCmptLink.getProductCmpt());
                    try {
                        result = getLinkCreator().canCreateLinks(target, draggedCmpts);
                    } catch (CoreException e) {
                        IpsPlugin.log(e);
                        result = false;
                    }
                }
                setFeedbackEnabled(result);
                System.out.println("move: " + result);
                return result;
            } else {
                return false;
            }
        } else {
            setFeedbackEnabled(false);
            List<IProductCmpt> draggedCmpts = getTransferElements(transferType);
            if (draggedCmpts == null) {
                System.out.println("draggedCmpt == null");
                return false;
            }
            // Linux bug - @see comment of getTransferElements(..)
            if (draggedCmpts.isEmpty()) {
                System.out.println("draggedCmpt.isEmpty (linux true)");
                return true;
            }
            try {
                boolean result = getLinkCreator().canCreateLinks(target, draggedCmpts);
                System.out.println("canCreateLink: " + result);
                return result;
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return false;
            }
        }
    }

    @Override
    public boolean performDrop(Object data) {
        if ((getCurrentOperation() & DND.DROP_LINK) != DND.DROP_LINK) {
            return false;
        }
        if (!(data instanceof String[])) {
            return false;
        }
        List<IProductCmpt> droppedCmpts = getProductCmpts((String[])data);
        Object target = getCurrentTarget();
        return getLinkCreator().createLinks(droppedCmpts, target);
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
     * 
     * @param transferType
     * @return
     */
    private List<IProductCmpt> getTransferElements(TransferData transferType) {
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

    private List<IProductCmpt> getProductCmpts(String[] filenames) {
        // TODO debug for linux!
        // Under some platforms, the data is not available during dragOver.
        if (filenames == null) {
            return new ArrayList<IProductCmpt>();
        }

        List<IProductCmpt> result = new ArrayList<IProductCmpt>();

        for (int i = 0; i < filenames.length; i++) {
            IFile file = getFile(filenames[i]);
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

    /**
     * @param linkCreator The linkCreator to set.
     */
    public void setLinkCreator(LinkCreatorUtil linkCreator) {
        this.linkCreator = linkCreator;
    }

    /**
     * @return Returns the linkCreator.
     */
    public LinkCreatorUtil getLinkCreator() {
        return linkCreator;
    }

    public void setToMove(IProductCmptLink selected) {
        movedCmptLink = selected;
    }

}
