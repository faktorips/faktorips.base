/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.versionmanager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper for sorting-related stuff belonging the {@link IIpsFeatureVersionManager}.
 * 
 * @author Thorsten Guenther
 */
public class IpsFeatureVersionManagerSorter {

    /**
     * Sorts all given managers for migration. That means, that the manager A, which is a
     * predecessor for another manager B is found at an index in the returned array which is less
     * than the index of the manager B.
     * <p>
     * Returns the sorted array.
     * 
     * @param managers The array of managers to sort.
     */
    public IIpsFeatureVersionManager[] sortForMigartionOrder(IIpsFeatureVersionManager[] managers) {
        HashMap<String, IIpsFeatureVersionManager> managersById = new HashMap<>();
        for (IIpsFeatureVersionManager manager : managers) {
            managersById.put(manager.getId(), manager);
        }

        ArrayList<IIpsFeatureVersionManager> result = new ArrayList<>();
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        for (IIpsFeatureVersionManager manager : managers) {
            sorter.buildPredecessorList(managersById, result, manager.getId());
        }

        return result.toArray(new IIpsFeatureVersionManager[result.size()]);
    }

    /**
     * Walks down the chain of predecessor-managers and fills all visited managers into the result
     * list in reverse order. If you have a chain of C has predecessor B has predecessor A, after a
     * call to this method with the id of C the result list will contain A, B, C in this order. No
     * duplicate entries will be made.
     * 
     * @param managersById Hash map for lookup, has to be properly filled, will not be modified.
     * @param result The list of all visited managers (see above).
     * @param id The id for the manager to start with.
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
