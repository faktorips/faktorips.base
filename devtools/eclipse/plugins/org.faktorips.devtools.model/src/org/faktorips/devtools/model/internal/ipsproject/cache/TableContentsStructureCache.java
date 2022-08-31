/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.MultiMap;

/**
 * This class caches for all {@link ITableStructure} the corresponding {@link ITableContents}.
 * <p>
 * It is designed to be instantiated once for every {@link IIpsProject}.
 */
public class TableContentsStructureCache {

    private enum State {
        NEW,
        INITIALIZED
    }

    /**
     * This field is marked as volatile because it is used to check the initialized state of the map
     * using the double null check, @see {@link #checkedInit()} .
     */
    private volatile State state = State.NEW;

    /**
     * The cached table structures to available table contents.
     */
    private final TableStructureMap tableStructureMap = new TableStructureMap();

    private final IIpsProject ipsProject;

    private final TableContentsStructureCacheUpdater updater;

    /**
     * The constructor getting the {@link IIpsModel} which is used to get the table contents and
     * structures. An {@link IIpsSrcFilesChangeListener} is also registered in the given
     * {@link IIpsModel} to recognize any changes.
     * 
     * @param ipsProject The {@link IIpsProject} which finds all the {@link IIpsSrcFile source files
     *            that should be cached}
     */
    public TableContentsStructureCache(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        this.ipsProject = ipsProject;
        updater = new TableContentsStructureCacheUpdater(this, ipsProject);
        ipsProject.getIpsModel().addIpsSrcFilesChangedListener(updater);
    }

    public void dispose() {
        ipsProject.getIpsModel().removeIpsSrcFilesChangedListener(updater);
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
        checkedInit();
        return new ArrayList<>(tableStructureMap.get(tableStructure));
    }

    private void checkedInit() {
        if (state != State.INITIALIZED) {
            synchronized (this) {
                if (state != State.INITIALIZED) {
                    init();
                    state = State.INITIALIZED;
                }
            }
        }
    }

    public void init() {
        List<IIpsSrcFile> tableContents = ipsProject.findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
        putAllTableContents(tableContents);
    }

    public void clear() {
        checkedClear();
    }

    private void checkedClear() {
        if (state != State.NEW) {
            synchronized (this) {
                if (state != State.NEW) {
                    tableStructureMap.clear();
                    state = State.NEW;
                }
            }
        }
    }

    protected boolean isNew() {
        return state == State.NEW;
    }

    protected boolean isInitialized() {
        return state == State.INITIALIZED;
    }

    private void putAllTableContents(Collection<IIpsSrcFile> tableContents) {
        for (IIpsSrcFile tableContent : tableContents) {
            putTableContent(tableContent);
        }
    }

    /**
     * This method is called when a new table structure was created. Only existing table contents
     * with an invalid table structure are relevant for this update.
     * 
     * @param tableStructure The new table structure
     */
    public void newTableStructure(IIpsSrcFile tableStructure) {
        for (IIpsSrcFile tableContent : tableStructureMap.contentWithInvlidStructure) {
            String tableStructureName = getTableStructureName(tableContent);
            if (Objects.equals(tableStructure.getIpsObjectName(), tableStructureName)) {
                // call putTableContent without the structure to search the structure again. The
                // current structure may not be correct in case of not referencing projects.
                putTableContent(tableContent);
            }
        }
    }

    public void putTableContent(IIpsSrcFile tableContent) {
        IIpsSrcFile tableStructure = getReferencedTableStructure(tableContent);
        tableStructureMap.put(tableStructure, tableContent);
    }

    private IIpsSrcFile getReferencedTableStructure(IIpsSrcFile tableContent) {
        String structureName = getTableStructureName(tableContent);
        return tableContent.getIpsProject().findIpsSrcFile(
                new QualifiedNameType(structureName, IpsObjectType.TABLE_STRUCTURE));
    }

    public void tableContentChanged(IIpsSrcFile tableContent) {
        if (isOutdated(tableContent)) {
            removeTableContent(tableContent);
            putTableContent(tableContent);
        }
    }

    private boolean isOutdated(IIpsSrcFile tableContent) {
        IIpsSrcFile tableStructure = tableStructureMap.getTableStructure(tableContent);
        return tableStructure == null
                || !Objects.equals(tableStructure.getName(), getTableStructureName(tableContent));
    }

    private String getTableStructureName(IIpsSrcFile tableContent) {
        return tableContent.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
    }

    public void removeTableStructure(IIpsSrcFile tableStructure) {
        tableStructureMap.removeTableStructure(tableStructure);
    }

    public void removeTableContent(IIpsSrcFile tableContent) {
        tableStructureMap.removeTableContent(tableContent);
    }

    /**
     * This data structure hold the necessary mappings for {@link ITableContents} and their
     * corresponding {@link ITableStructure}. It has the ability to update existing
     * content-structure relations. The data structure is designed to be thread safe. We need to
     * synchronize every writing operation because we work with two maps which content depends on
     * each other. Doing a more fine granularity synchronization is not worth the effort.
     */
    private static class TableStructureMap {

        private final MultiMap<IIpsSrcFile, IIpsSrcFile> structureToContentMap = MultiMap.createWithSetsAsValues();

        private final ConcurrentHashMap<IIpsSrcFile, IIpsSrcFile> tableContentsToStructure = new ConcurrentHashMap<>();

        private final Set<IIpsSrcFile> contentWithInvlidStructure = new HashSet<>();

        /**
         * Returns the set of table contents for the given table structure.
         * <p>
         * This method does not need to be synchronized because we use concurrent hash maps. We
         * cannot guarantee that the result may be currently changed by other threads. However also
         * using synchronized there is no guarantee that we get the result before or after the other
         * thread is updating the result.
         * 
         * @param tableStructure The table structure for which the content should be returned
         */
        public Collection<IIpsSrcFile> get(IIpsSrcFile tableStructure) {
            return structureToContentMap.get(tableStructure);
        }

        public void clear() {
            structureToContentMap.clear();
            tableContentsToStructure.clear();
            contentWithInvlidStructure.clear();
        }

        public IIpsSrcFile getTableStructure(IIpsSrcFile tableContent) {
            return tableContentsToStructure.get(tableContent);
        }

        public synchronized void put(IIpsSrcFile tableStructure, IIpsSrcFile tableContent) {
            if (tableStructure == null) {
                contentWithInvlidStructure.add(tableContent);
            } else {
                structureToContentMap.put(tableStructure, tableContent);
                tableContentsToStructure.put(tableContent, tableStructure);
            }
        }

        public synchronized void removeTableContent(IIpsSrcFile tableContent) {
            IIpsSrcFile tableStructure = tableContentsToStructure.get(tableContent);
            if (tableStructure != null) {
                structureToContentMap.remove(tableStructure, tableContent);
            }
            tableContentsToStructure.remove(tableContent);
            contentWithInvlidStructure.remove(tableContent);
        }

        public synchronized void removeTableStructure(IIpsSrcFile tableStructure) {
            Collection<IIpsSrcFile> list = get(tableStructure);
            for (IIpsSrcFile tableContent : list) {
                tableContentsToStructure.remove(tableContent);
            }
            structureToContentMap.remove(tableStructure);
        }

    }

}
