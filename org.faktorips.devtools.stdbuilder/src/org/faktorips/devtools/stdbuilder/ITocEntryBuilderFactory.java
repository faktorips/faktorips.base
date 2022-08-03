/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

/**
 * Interface for factories that create custom TOC entry builders. A TOC entry builder is responsible
 * for creating table of contents (TOC) entries for a specific IPS object type. Used by the
 * extension point "org.faktorips.devtools.stdbuilder.tocEntryBuilderFactory".
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public interface ITocEntryBuilderFactory {

    /**
     * Creates a {@link ITocEntryBuilder} for the given {@link TocFileBuilder}. This builder will
     * create a new instance of a TOC entry builder every time this method is called.
     * 
     * @param tocFileBuilder the TOC file builder a TOC entry builder is created for.
     * @return a new {@link ITocEntryBuilder} instance
     */
    ITocEntryBuilder createTocEntryBuilder(TocFileBuilder tocFileBuilder);
}
