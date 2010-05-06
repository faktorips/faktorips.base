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

package org.faktorips.runtime.internal.toc;

import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.internal.DateTime;

public interface IProductCmptTocEntry extends ITocEntryObject {

    public static final String PROPERTY_KIND_ID = "kindId";
    public static final String PROPERTY_VERSION_ID = "versionId";
    public static final String PROPERTY_VALID_TO = "validTo";
    public static final String PROPERTY_GENERATION_IMPL_CLASS_NAME = "generationImplClassName";
    public static final String XML_TAG = "ProductComponent";

    /**
     * Returns the id of the product component kind, if this entry describes a product component,
     * otherwise an empty string.
     */
    public String getKindId();

    /**
     * Returns the version id if this entry describes a product component, otherwise an empty
     * string.
     */
    public String getVersionId();

    /**
     * Returns the validTo date or null if the object doesn't supports a valid to attribute.
     */
    public DateTime getValidTo();

    /**
     * Returns the generation entries or an empty array if this entry does not contain any
     * generation entries.
     */
    public List<TocEntryGeneration> getGenerationEntries();

    /**
     * Returns the number of genertion entries.
     */
    public int getNumberOfGenerationEntries();

    /**
     * Returns the name of the generation implementation class
     */
    public String getGenerationImplClassName();

    /**
     * Sets the generation entries.
     */
    public void setGenerationEntries(List<TocEntryGeneration> entries);

    /**
     * Returns the {@link TocEntryGeneration} successor of the one that is found for the provided
     * validity date. Returns <code>null</code> if either no entry is found for the provided date or
     * if the found one doesn't have a successor.
     */
    public TocEntryGeneration getNextGenerationEntry(Calendar validFrom);

    /**
     * Returns the {@link TocEntryGeneration} that is prior to the one that is found for the
     * provided validity date. Returns <code>null</code> if either no entry is found for the
     * provided date or if the found one doesn't have a predecessor.
     */
    public TocEntryGeneration getPreviousGenerationEntry(Calendar validFrom);

    /**
     * Returns the latest {@link TocEntryGeneration} with repect to the generations validity date.
     */
    public TocEntryGeneration getLatestGenerationEntry();

    /**
     * Returns the toc entry for the generation valid on the given effective date, or
     * <code>null</code> if no generation is effective on the given date or the effective is
     * <code>null</code>.
     */
    public TocEntryGeneration getGenerationEntry(Calendar effectiveDate);

}
