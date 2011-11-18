/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;

/**
 * Helper for drag and drop operations. It is possible to register a so called drop handler as an
 * extension to Faktor IPS. These handlers are asked for the required drop-informations, so we can
 * have more than one drop-listener.
 * 
 * @author guenther
 */
public final class IpsDropUtil {

    private static final IpsDropUtil instance = new IpsDropUtil();

    private Map<Viewer, Set<IIpsDropListener>> adapterMap;
    private Map<Viewer, IIpsDropListener> currentAdapter;

    private IpsDropUtil() {
        adapterMap = new HashMap<Viewer, Set<IIpsDropListener>>();
        currentAdapter = new HashMap<Viewer, IIpsDropListener>();
    }

    public static IpsDropUtil getInstance() {
        return instance;
    }

    public boolean validateDrop(Viewer viewer, Object target, int operation, TransferData data) {
        Set<IIpsDropListener> adapters = adapterMap.get(viewer);

        if (adapters == null) {
            return false;
        }

        for (IIpsDropListener adapter : adapters) {
            if (adapter.validateDropSingle(target, operation, data)) {
                currentAdapter.put(viewer, adapter);
                return true;
            }
        }
        return false;
    }

    public boolean performDrop(Viewer viewer, Object data) {
        IIpsDropListener current = currentAdapter.get(viewer);
        if (current == null) {
            return false;
        }
        currentAdapter.remove(viewer);
        return current.performDropSingle(data);
    }

    public static void addDropSupport(StructuredViewer viewer) {
        Control control = viewer.getControl();
        DropTarget dropTarget = new DropTarget(control, getSupportedOperations());
        dropTarget.setTransfer(getSupportedTransferTypes());
        addDropListener(dropTarget, viewer);
    }

    private static Transfer[] getSupportedTransferTypes() {
        Set<Transfer> result = new HashSet<Transfer>();
        List<IIpsDropListenerProvider> handlers = IpsUIPlugin.getDefault().getProductCmptDnDHandler();

        for (IIpsDropListenerProvider handler : handlers) {
            result.addAll(handler.getSupportedTransferTypes());
        }
        return result.toArray(new Transfer[result.size()]);
    }

    private static void addDropListener(DropTarget dropTarget, StructuredViewer viewer) {
        List<IIpsDropListenerProvider> handlers = IpsUIPlugin.getDefault().getProductCmptDnDHandler();
        Set<IIpsDropListener> adapters = new HashSet<IIpsDropListener>();
        for (IIpsDropListenerProvider handler : handlers) {
            IIpsDropListener adapter = handler.getDropListener(viewer);
            dropTarget.addDropListener(adapter);
            adapters.add(adapter);
        }
        instance.adapterMap.put(viewer, adapters);
    }

    private static int getSupportedOperations() {
        int result = 0;
        List<IIpsDropListenerProvider> handlers = IpsUIPlugin.getDefault().getProductCmptDnDHandler();
        for (IIpsDropListenerProvider handler : handlers) {
            result = result | handler.getSupportedOperations();
        }
        return result;
    }

}
