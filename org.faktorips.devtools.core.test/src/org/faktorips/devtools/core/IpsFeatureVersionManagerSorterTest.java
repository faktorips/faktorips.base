/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.devtools.core.model.versionmanager.IpsFeatureVersionManagerSorter;


/**
 *
 */
public class IpsFeatureVersionManagerSorterTest extends TestCase {
    
    public void testGetMigrationOperation() throws CoreException {
        IpsFeatureVersionManagerSorter sorter = new IpsFeatureVersionManagerSorter();
        Manager[] managers = new Manager[] {
                new Manager("6", "1"),
                new Manager("2", ""),
                new Manager("3", ""),
                new Manager("4", ""),
                new Manager("5", ""),
                new Manager("1", "")
        };
        
        IIpsFeatureVersionManager[] result = sorter.sortForMigartionOrder(managers);
        boolean found = false;
        for (int i = 0; i < result.length; i++) {
            if (result[i].getId().equals("1")) {
                found = true;
            }
            if (result[i].getId().equals("6") && !found) {
                fail();
            }
        }

        managers = new Manager[] {
                new Manager("2", ""),
                new Manager("3", ""),
                new Manager("6", "1"),
                new Manager("4", ""),
                new Manager("5", ""),
                new Manager("1", "")
        };
        
        result = sorter.sortForMigartionOrder(managers);
        found = false;
        for (int i = 0; i < result.length; i++) {
            if (result[i].getId().equals("1")) {
                found = true;
            }
            if (result[i].getId().equals("6") && !found) {
                fail();
            }
        }

        managers = new Manager[] {
                new Manager("2", ""),
                new Manager("5", "3"),
                new Manager("6", "1"),
                new Manager("4", ""),
                new Manager("3", ""),
                new Manager("1", "")
        };
        
        result = sorter.sortForMigartionOrder(managers);
        found = false;
        boolean foundThree = false;
        for (int i = 0; i < result.length; i++) {
            if (result[i].getId().equals("1")) {
                found = true;
            }
            if (result[i].getId().equals("3")) {
                foundThree = true;
            }
            if (result[i].getId().equals("6") && !found) {
                fail();
            }
            if (result[i].getId().equals("5") && !foundThree) {
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
        public void setId(String id) {
        }

        /**
         * {@inheritDoc}
         */
        public String getId() {
            return id;
        }

        /**
         * {@inheritDoc}
         */
        public void setPredecessorId(String predecessorId) {
        }

        /**
         * {@inheritDoc}
         */
        public String getPredecessorId() {
            return prevId;
        }

        /**
         * {@inheritDoc}
         */
        public void setFeatureId(String featureId) {
        }

        /**
         * {@inheritDoc}
         */
        public String getFeatureId() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getCurrentVersion() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isCurrentVersionCompatibleWith(String otherVersion) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int compareToCurrentVersion(String otherVersion) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        public AbstractIpsProjectMigrationOperation[] getMigrationOperations(IIpsProject projectToMigrate) throws CoreException {
            return null;
        }        
    }
}