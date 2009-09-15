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

package org.faktorips.devtools.core.model.versionmanager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper for sorting-related stuff belonging the IpsFeatureVersionManager
 * 
 * @author Thorsten Guenther
 */
public class IpsFeatureVersionManagerSorter {

    /**
     * Sorts all given managers for migration. That means, that the manager A, which is a precessor
     * for another manager B is found at an index in the returned array which is less than the index
     * of the manager B.
     * 
     * @param managers The array of managers to sort.
     * @return The sorted array.
     */
    public IIpsFeatureVersionManager[] sortForMigartionOrder(IIpsFeatureVersionManager[] managers) {
        HashMap<String, IIpsFeatureVersionManager> managersById = new HashMap<String, IIpsFeatureVersionManager>();
        for (int i = 0; i < managers.length; i++) {
            managersById.put(managers[i].getId(), managers[i]);
        }

        ArrayList<IIpsFeatureVersionManager> result = new ArrayList<IIpsFeatureVersionManager>();
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        for (int i = 0; i < managers.length; i++) {
            sorter.buildPredecessorList(managersById, result, managers[i].getId());
        }

        return result.toArray(new IIpsFeatureVersionManager[result.size()]);
    }

    /**
     * Walks down the chain of predecessor-managers and fills all visited managers into the
     * result-list in reverse order. If you have a chain of C has predecessor B has predecessor A,
     * after a call to this method with id of C will contain A, B, C in this order. Do duplicate
     * entries will be made.
     * 
     * @param managersById Hashtable for lookup, has to be properly filled, will not be modified.
     * @param result The list of all visited managers (see above).
     * @param id The id fo the manager to start at.
     */
    private void buildPredecessorList(HashMap<String, IIpsFeatureVersionManager> managersById,
            ArrayList<IIpsFeatureVersionManager> result,
            String id) {
        IIpsFeatureVersionManager manager = managersById.get(id);

        if (manager == null) {
            return;
        }

        if (manager.getPredecessorId().length() == 0 && !result.contains(managersById.get(manager.getId()))) {
            result.add(manager);
        } else {
            buildPredecessorList(managersById, result, manager.getPredecessorId());

            if (!result.contains(managersById.get(manager.getId()))) {
                result.add(manager);
            }
        }
    }

}
