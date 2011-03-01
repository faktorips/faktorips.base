/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.productdataprovider;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.AbstractRuntimeRepositoryManager;

/**
 * The {@link DetachedContentRuntimeRepositoryManager} manages the access to the
 * {@link DetachedContentRuntimeRepository}. To get a runtime repository that provides the actual
 * product data you have to call {@link #getActualRuntimeRepository()}.
 * <p>
 * To create a new {@link DetachedContentRuntimeRepositoryManager} use the {@link DetachedContentRuntimeBuilder}
 * 
 * 
 * @see DetachedContentRuntimeRepository
 * 
 * 
 * @author dirmeier
 */
public class DetachedContentRuntimeRepositoryManager extends AbstractRuntimeRepositoryManager {

    final DetachedContentRuntimeBuilder builder;

    /**
     * This is the constructor for the {@link DetachedContentRuntimeRepositoryManager}. The
     * constructor is only called from the internal {@link DetachedContentRuntimeBuilder}.
     * 
     */
    DetachedContentRuntimeRepositoryManager(DetachedContentRuntimeBuilder builder) {
        this.builder = builder;
    }

    @Override
    protected boolean isRepositoryUpToDate(IRuntimeRepository actualRuntimeRepository) {
        if (actualRuntimeRepository instanceof DetachedContentRuntimeRepository) {
            DetachedContentRuntimeRepository detachedContentRR = (DetachedContentRuntimeRepository)actualRuntimeRepository;
            return detachedContentRR.isUpToDate();
        } else {
            return false;
        }
    }

    @Override
    protected IRuntimeRepository createNewRuntimeRepository() {
        return new DetachedContentRuntimeRepository(builder);
    }

}
