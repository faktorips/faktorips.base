/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;

/**
 * This class caches for all {@link ITableStructure} the corresponding {@link ITableContents}.
 * <p>
 * This class registers a {@link IIpsSrcFilesChangeListener} in the provided {@link IIpsModel}. It
 * is designed to be instantiated only once for every {@link IIpsModel} and is bound to the
 * lifecycle of the model. Hence there is no method for removing the registered listener.
 */
public class TableContentsStructureCache {

    private enum State {
        NEW,
        INITIALIZED
    }

    /**
     * This field is marked as volatile because it is used to check the initialized state of the map
     * using the double null check, @see {@link #checkInit()} .
     */
    private volatile State state = State.NEW;

    private final IIpsModel ipsModel;

    /**
     * The cached table structures to available table contents.
     */
    private final TableStructureMap tableStructureMap;

    private final TableContentUpdater updater;

    /**
     * The constructor getting the {@link IIpsModel} which is used to get the table contents and
     * structures. An {@link IIpsSrcFilesChangeListener} is also registered in the given
     * {@link IIpsModel} to recognize any changes.
     * 
     * @param ipsModel The {@link IIpsModel} which holds all the {@link IIpsSrcFile source files
     *            that should be cached}
     */
    public TableContentsStructureCache(IIpsModel ipsModel) {
        ArgumentCheck.notNull(ipsModel);
        this.ipsModel = ipsModel;
        tableStructureMap = new TableStructureMap();
        updater = new TableContentUpdater(tableStructureMap);
        ipsModel.addIpsSrcFilesChangedListener(updater);
    }

    /**
     * Returns the list of table contents that are instances of the given table structure. If there
     * is no table content for the given table structure, an empty list is returned.
     * <p>
     * When first calling this method all table contents will be checked and initialized. This may
     * take some time. When the cache is initialized it is updated using
     * {@link IpsSrcFilesChangedEvent}.
     * 
     * @param tableStructure The {@link IIpsSrcFile} of the table structure you want get the
     *            contents for.
     * @return The list of found table contents for the given table structure.
     */
    public List<IIpsSrcFile> getTableContents(IIpsSrcFile tableStructure) {
        checkInit();
        return new ArrayList<IIpsSrcFile>(tableStructureMap.get(tableStructure));
    }

    private void checkInit() {
        if (state != State.INITIALIZED) {
            synchronized (this) {
                if (state != State.INITIALIZED) {
                    init();
                    state = State.INITIALIZED;
                }
            }
        }
    }

    private void init() {
        IIpsProject[] ipsProjects;
        ipsProjects = getIpsProjects();
        updater.updateTableContents(ipsProjects);
    }

    private IIpsProject[] getIpsProjects() {
        try {
            return ipsModel.getIpsProjects();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * This class is responsible for updating the table structure map. It is an implementation of
     * {@link IIpsSrcFilesChangeListener} and is registered in the {@link IIpsModel} to get notified
     * for every changed {@link IIpsSrcFile}. It could also be used for the initialization of the
     * table structure map.
     * 
     */
    private static class TableContentUpdater implements IIpsSrcFilesChangeListener {

        /**
         * The kind of update operation. We only distinguish added and removed. While kind
         * {@link #REMOVED} removes an existing mapping, all other operations are treated as
         * {@link #CHANGED}.
         */
        public enum OperationKind {
            CHANGED,
            REMOVED
        }

        private final TableStructureMap tableStructureMap;

        public TableContentUpdater(TableStructureMap tableStructureMap) {
            this.tableStructureMap = tableStructureMap;
        }

        public void updateTableContents(IIpsProject[] ipsProjects) {
            for (IIpsProject ipsProject : ipsProjects) {
                List<IIpsSrcFile> ipsSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
                updateTableContents(ipsSrcFiles);
            }
        }

        private void updateTableContents(List<IIpsSrcFile> ipsSrcFiles) {
            for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
                updateTableContent(ipsSrcFile, TableContentUpdater.OperationKind.CHANGED);
            }
        }

        @Override
        public void ipsSrcFilesChanged(IpsSrcFilesChangedEvent event) {
            Set<IIpsSrcFile> changedIpsSrcFiles = event.getChangedIpsSrcFiles();
            for (IIpsSrcFile ipsSrcFile : changedIpsSrcFiles) {
                OperationKind operationKind = getOperationKind(event, ipsSrcFile);
                handleTableStructureChange(ipsSrcFile, operationKind);
                updateTableContent(ipsSrcFile, operationKind);
            }
        }

        private OperationKind getOperationKind(IpsSrcFilesChangedEvent event, IIpsSrcFile ipsSrcFile) {
            IResourceDelta resourceDelta = event.getResourceDelta(ipsSrcFile);
            if ((resourceDelta.getKind() & IResourceDelta.REMOVED) != 0) {
                return OperationKind.REMOVED;
            } else {
                return OperationKind.CHANGED;
            }
        }

        private void handleTableStructureChange(IIpsSrcFile ipsSrcFile, OperationKind operation) {
            if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())) {
                if (operation == OperationKind.REMOVED) {
                    tableStructureMap.removeTableStructure(ipsSrcFile);
                } else {
                    updateTableContentsForReferencingProjects(ipsSrcFile);
                }
            }
        }

        private void updateTableContentsForReferencingProjects(IIpsSrcFile ipsSrcFile) {
            try {
                IIpsProject[] ipsProjects = ipsSrcFile.getIpsProject().findReferencingProjectLeavesOrSelf();
                updateTableContents(ipsProjects);
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        public void updateTableContent(IIpsSrcFile ipsSrcFile, OperationKind operation) {
            if (IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType())) {
                IIpsSrcFile tableStructure = getTableStructure(ipsSrcFile, operation);
                tableStructureMap.updateContent(tableStructure, ipsSrcFile);
            }
        }

        private IIpsSrcFile getTableStructure(IIpsSrcFile ipsSrcFile, OperationKind operation) {
            if (operation == OperationKind.REMOVED) {
                return null;
            } else {
                return getReferencedTableStructure(ipsSrcFile);
            }
        }

        private IIpsSrcFile getReferencedTableStructure(IIpsSrcFile ipsSrcFile) {
            try {
                String structureName = ipsSrcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
                return ipsSrcFile.getIpsProject().findIpsSrcFile(
                        new QualifiedNameType(structureName, IpsObjectType.TABLE_STRUCTURE));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

    }

    /**
     * This data structure hold the necessary mappings for {@link ITableContents} and their
     * corresponding {@link ITableStructure}. It has the ability to update existing
     * content-structure relations. The data structure is designed to be thread safe. We need to
     * synchronize every writing operation because we work with two maps which content depends on
     * each other. Doing a more fine granularity synchronization is not worth the effort.
     */
    private static class TableStructureMap {

        private final ConcurrentHashMap<IIpsSrcFile, Set<IIpsSrcFile>> tableStructureToContents = new ConcurrentHashMap<IIpsSrcFile, Set<IIpsSrcFile>>();

        private final ConcurrentHashMap<IIpsSrcFile, IIpsSrcFile> tableContentsToStructure = new ConcurrentHashMap<IIpsSrcFile, IIpsSrcFile>();

        /**
         * Returns the set of table contents for the given table structure.
         * <p>
         * This method does not need to be synchronized because we use concurrent hash maps. We
         * cannot guarantee that the result may be currently changed by other threads. However also
         * using synchronized there is no guarantee that we get the result before or after the other
         * thread is updating the result.
         */
        public Set<IIpsSrcFile> get(IIpsSrcFile tableStructure) {
            Set<IIpsSrcFile> list = tableStructureToContents.get(tableStructure);
            if (list == null) {
                return Collections.emptySet();
            } else {
                return list;
            }
        }

        /**
         * Updates the mapping of table structure and table content. If there is no new table
         * structure (tableStructure==null) the old table content entry is removed and no new one is
         * created.
         * 
         * @param tableStructure The table structure that should be mapped to the table content, may
         *            be null to insert no new mapping but removing an maybe existing old one.
         * @param tableContent The table content for the mapping, should never be null.
         */
        public synchronized void updateContent(IIpsSrcFile tableStructure, IIpsSrcFile tableContent) {
            removeTableContent(tableContent);
            if (tableStructure != null) {
                put(tableStructure, tableContent);
            }
        }

        public synchronized void put(IIpsSrcFile tableStructure, IIpsSrcFile tableContent) {
            Set<IIpsSrcFile> list = tableStructureToContents.get(tableStructure);
            if (list == null) {
                list = new HashSet<IIpsSrcFile>();
                Set<IIpsSrcFile> alreadyAddedList = tableStructureToContents.putIfAbsent(tableStructure, list);
                if (alreadyAddedList != null) {
                    list = alreadyAddedList;
                }
            }
            list.add(tableContent);
            tableContentsToStructure.put(tableContent, tableStructure);
        }

        public synchronized void removeTableContent(IIpsSrcFile tableContent) {
            IIpsSrcFile oldTableStructure = tableContentsToStructure.get(tableContent);
            if (oldTableStructure != null) {
                Set<IIpsSrcFile> list = get(oldTableStructure);
                list.remove(tableContent);
            }
            tableContentsToStructure.remove(tableContent);
        }

        public synchronized void removeTableStructure(IIpsSrcFile tableStructure) {
            Set<IIpsSrcFile> list = get(tableStructure);
            for (IIpsSrcFile tableContent : list) {
                tableContentsToStructure.remove(tableContent);
            }
            tableStructureToContents.remove(tableStructure);
        }

    }

}
