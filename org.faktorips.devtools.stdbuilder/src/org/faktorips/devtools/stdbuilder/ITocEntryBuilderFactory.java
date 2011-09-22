/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
    public ITocEntryBuilder createTocEntryBuilder(TocFileBuilder tocFileBuilder);
}
