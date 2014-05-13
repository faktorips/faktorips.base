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
 * This class caches for all {@link ITableStructure} the corresponding {@link ITableContents}. It
 * could be used validate how much table contents are instantiated for a table structure. This
 * information is necessary for example to validate that there exists only one content for
 * single-table structures.
 * <p>
 * This class registers a {@link IIpsSrcFilesChangeListener} in the provided {@link IIpsModel}. It
 * is designed to be instantiated only once for every {@link IIpsModel} and is bounded to the
 * lifecycle of the model. Hence there is no removing of the registered listener.
 */
public class TableContentsValidationCache {

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
     * The cached tabled structures to available table contents.
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
    public TableContentsValidationCache(IIpsModel ipsModel) {
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
     * @param tableStructures The {@link IIpsSrcFile} of the table structure you want get the
     *            contents for.
     * @return The list of found table contents for the given table structure.
     */
    public List<IIpsSrcFile> getTableContents(IIpsSrcFile tableStructures) {
        checkInit();
        return Collections.unmodifiableList(tableStructureMap.get(tableStructures));
    }

    private void checkInit() {
        if (state != State.INITIALIZED) {
            synchronized (state) {
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

        private TableStructureMap tableStructureMap;

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
                IResourceDelta resourceDelta = event.getResourceDelta(ipsSrcFile);
                OperationKind operation;
                if ((resourceDelta.getKind() & IResourceDelta.REMOVED) != 0) {
                    operation = OperationKind.REMOVED;
                } else {
                    operation = OperationKind.CHANGED;
                }
                handleTableStructureChange(ipsSrcFile, operation);
                updateTableContent(ipsSrcFile, operation);
            }
        }

        private void handleTableStructureChange(IIpsSrcFile ipsSrcFile, OperationKind operation) {
            if (IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType())) {
                if (operation == OperationKind.REMOVED) {
                    tableStructureMap.removeTableStructure(ipsSrcFile);
                } else {
                    try {
                        IIpsProject[] ipsProjects = ipsSrcFile.getIpsProject().findReferencingProjectLeavesOrSelf();
                        updateTableContents(ipsProjects);
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
        }

        public void updateTableContent(IIpsSrcFile ipsSrcFile, OperationKind operation) {
            if (IpsObjectType.TABLE_CONTENTS.equals(ipsSrcFile.getIpsObjectType())) {
                IIpsSrcFile tableStructure;
                if (operation == OperationKind.REMOVED) {
                    tableStructure = null;
                } else {
                    tableStructure = getReferencedTableStructure(ipsSrcFile);
                }
                tableStructureMap.updateContent(tableStructure, ipsSrcFile);
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

    private static class TableStructureMap {

        private ConcurrentHashMap<IIpsSrcFile, List<IIpsSrcFile>> tableStructureToContents = new ConcurrentHashMap<IIpsSrcFile, List<IIpsSrcFile>>();

        private ConcurrentHashMap<IIpsSrcFile, IIpsSrcFile> tableContentsToStructure = new ConcurrentHashMap<IIpsSrcFile, IIpsSrcFile>();

        public List<IIpsSrcFile> get(IIpsSrcFile tableStructure) {
            List<IIpsSrcFile> list = tableStructureToContents.get(tableStructure);
            if (list == null) {
                return Collections.emptyList();
            } else {
                return list;
            }
        }

        /**
         * Updates the mapping of table structure and table mapping. If there is no new table
         * structure (tableStructure==null) the old table content entry is removed and no new one is
         * created.
         * 
         * @param tableStructure The table structure that should be mapped to the table content, may
         *            be null to insert no new mapping but removing an maybe existing old one.
         * @param tableContent The table content for the mapping, should never be null.
         */
        public void updateContent(IIpsSrcFile tableStructure, IIpsSrcFile tableContent) {
            removeTableContent(tableContent);
            if (tableStructure != null) {
                put(tableStructure, tableContent);
            }
        }

        public void put(IIpsSrcFile tableStructure, IIpsSrcFile tableContent) {
            List<IIpsSrcFile> list = tableStructureToContents.get(tableStructure);
            if (list == null) {
                list = new ArrayList<IIpsSrcFile>();
                tableStructureToContents.put(tableStructure, list);
            }
            list.add(tableContent);
            tableContentsToStructure.put(tableContent, tableStructure);
        }

        public void removeTableContent(IIpsSrcFile tableContent) {
            IIpsSrcFile oldTableStructure = tableContentsToStructure.get(tableContent);
            if (oldTableStructure != null) {
                List<IIpsSrcFile> list = get(oldTableStructure);
                list.remove(tableContent);
            }
            tableContentsToStructure.remove(tableContent);
        }

        public void removeTableStructure(IIpsSrcFile tableStructure) {
            List<IIpsSrcFile> list = get(tableStructure);
            for (IIpsSrcFile tableContent : list) {
                tableContentsToStructure.remove(tableContent);
            }
            tableStructureToContents.remove(tableStructure);
        }

    }

}
