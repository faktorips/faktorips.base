/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;

/**
 * SearchStructure creator for creating a tree that represents the structure of a
 * <code>ITableContents</code> object.
 * 
 * @author Stefan Widmaier
 */
public class TableContentsCompareItemCreator extends AbstractCompareItemCreator {

    public TableContentsCompareItemCreator() {
        super();
    }

    /**
     * Returns the title for the structure-differences viewer. {@inheritDoc}
     */
    @Override
    public String getName() {
        return Messages.TableContentsCompareItemCreator_TableContentsStructureCompare;
    }

    /**
     * Creates a structure/tree of <code>TableContentsCompareItem</code>s from the given
     * <code>IIpsSrcFile</code> to represent an <code>ITableContents</code> object. The
     * <code>IIpsSrcFile</code>, the <code>ITableContents</code>, its generations and all contained
     * rows are each represented by a <code>TableContentsCompareItem</code>.
     * <p>
     * The returned <code>TableContentsCompareItem</code> is the root of the created structure and
     * contains the given <code>IIpsSrcFile</code>. It has exactly one child representing (and
     * referencing) the <code>ITableContents</code> contained in the srcFile. This
     * <code>TableContentsCompareItem</code> has a child for each generation the table posesses.
     * Each generation-compareitem contains multiple <code>TableContentsCompareItem</code>s
     * representing the rows (<code>IRow</code>) of the table (in the current generation).
     * 
     * {@inheritDoc}
     */
    @Override
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        if (file.getIpsObject() instanceof ITableContents) {
            TableContentsCompareItem root = new TableContentsCompareItem(null, file);
            ITableContents table = (ITableContents)file.getIpsObject();
            TableContentsCompareItem ipsObject = new TableContentsCompareItem(root, table);
            // Generations for table
            ITableRows gen = table.getTableRows();
            TableContentsCompareItem generation = new TableContentsCompareItem(ipsObject, gen);
            // rows for each generation
            IRow[] rows = gen.getRows();
            for (IRow row : rows) {
                new TableContentsCompareItem(generation, row);
            }
            // initialize name, root-document and ranges for all nodes
            root.init();
            return root;
        }
        return null;
    }

}
