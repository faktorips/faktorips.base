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

import static org.junit.Assert.fail;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Test;

public class IpsFeatureVersionManagerSorterTest {

    @Test
    public void testGetMigrationOperation() {
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        Manager[] managers = { new Manager("6", "1"), new Manager("2", ""), new Manager("3", ""),
                new Manager("4", ""), new Manager("5", ""), new Manager("1", "") };

        IIpsFeatureVersionManager[] result = sorter.sortForMigartionOrder(managers);
        boolean found = false;
        for (IIpsFeatureVersionManager element : result) {
            if (element.getId().equals("1")) {
                found = true;
            }
            if (element.getId().equals("6") && !found) {
                fail();
            }
        }

        managers = new Manager[] { new Manager("2", ""), new Manager("3", ""), new Manager("6", "1"),
                new Manager("4", ""), new Manager("5", ""), new Manager("1", "") };

        result = sorter.sortForMigartionOrder(managers);
        found = false;
        for (IIpsFeatureVersionManager element : result) {
            if (element.getId().equals("1")) {
                found = true;
            }
            if (element.getId().equals("6") && !found) {
                fail();
            }
        }

        managers = new Manager[] { new Manager("2", ""), new Manager("5", "3"), new Manager("6", "1"),
                new Manager("4", ""), new Manager("3", ""), new Manager("1", "") };

        result = sorter.sortForMigartionOrder(managers);
        found = false;
        boolean foundThree = false;
        for (IIpsFeatureVersionManager element : result) {
            if (element.getId().equals("1")) {
                found = true;
            }
            if (element.getId().equals("3")) {
                foundThree = true;
            }
            if (element.getId().equals("6") && !found) {
                fail();
            }
            if (element.getId().equals("5") && !foundThree) {
                fail();
            }
        }
    }

    private static class Manager implements IIpsFeatureVersionManager {

        private String id;

        private String prevId;

        public Manager(String id, String predecessorId) {
            this.id = id;
            prevId = predecessorId;
        }

        @Override
        public void setId(String id) {

        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setPredecessorId(String predecessorId) {

        }

        @Override
        public String getPredecessorId() {
            return prevId;
        }

        @Override
        public void setFeatureId(String featureId) {

        }

        @Override
        public String getFeatureId() {
            return null;
        }

        @Override
        public String getCurrentVersion() {
            return null;
        }

        @Override
        public boolean isCurrentVersionCompatibleWith(String otherVersion) {
            return false;
        }

        @Override
        public int compareToCurrentVersion(String otherVersion) {
            return 0;
        }

        @Override
        public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) {
            return null;
        }

        @Override
        public boolean isRequiredForAllProjects() {
            return false;
        }

        @Override
        public void setRequiredForAllProjects(boolean required) {
        }

    }

}
