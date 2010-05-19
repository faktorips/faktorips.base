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

package org.faktorips.devtools.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IpsFeatureVersionManagerSorter;

/**
 *
 */
public class IpsFeatureVersionManagerSorterTest extends TestCase {

    public void testGetMigrationOperation() throws CoreException {
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        Manager[] managers = new Manager[] { new Manager("6", "1"), new Manager("2", ""), new Manager("3", ""),
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

    private class Manager implements IIpsFeatureVersionManager {
        private String id;
        private String prevId;

        public Manager(String id, String predecessorId) {
            this.id = id;
            this.prevId = predecessorId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setId(String id) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getId() {
            return id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPredecessorId(String predecessorId) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPredecessorId() {
            return prevId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setFeatureId(String featureId) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getFeatureId() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getCurrentVersion() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCurrentVersionCompatibleWith(String otherVersion) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareToCurrentVersion(String otherVersion) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate)
                throws CoreException {
            return null;
        }
    }
}
