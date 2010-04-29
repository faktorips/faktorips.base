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

package org.faktorips.runtime.pds;

import java.util.List;

import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.AbstractTocBasedRuntimeRepository;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.TocEntryObject;
import org.faktorips.runtime.test.IpsTestCaseBase;

public class PdsRuntimeRepository extends AbstractTocBasedRuntimeRepository {

    public PdsRuntimeRepository(String name, ICacheFactory cacheFactory) {
        super(name, cacheFactory);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected <T> List<T> createEnumValues(TocEntryObject tocEntry, Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IProductComponent createProductCmpt(TocEntryObject tocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(TocEntryGeneration tocEntryGeneration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ITable createTable(TocEntryObject tocEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected IpsTestCaseBase createTestCase(TocEntryObject tocEntry, IRuntimeRepository runtimeRepository) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected AbstractReadonlyTableOfContents loadTableOfContents() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isModifiable() {
        // TODO Auto-generated method stub
        return false;
    }

}
