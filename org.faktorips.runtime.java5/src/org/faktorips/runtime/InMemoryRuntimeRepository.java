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

package org.faktorips.runtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;

/**
 * A ProductComponentRegistry that keeps it's data in memory.
 */
public class InMemoryRuntimeRepository extends AbstractRuntimeRepository {

    // contains product component ids as keys and instances of product components as values.
    private HashMap<String, IProductComponent> productCmpts = new HashMap<String, IProductComponent>();

    // a map that contains a list (value) by product component id (key).
    // each list contains the generations for the product component.
    private HashMap<String, SortedSet<IProductComponentGeneration>> productCmptGenLists = new HashMap<String, SortedSet<IProductComponentGeneration>>();

    // contains all table contents
    private List<ITable> tables = new ArrayList<ITable>();

    // contains the table contents for structures that allow multiple contents
    // key is the qName, value the table
    private Map<String, ITable> multipleContentTables = new HashMap<String, ITable>();

    // contains all test cases with their qualified name as key.
    private HashMap<String, IpsTestCaseBase> testCasesByQName = new HashMap<String, IpsTestCaseBase>();

    // contains all enumeration values for the faktor ips enumerations which content is deferred 
    private Map<Class<IEnumValue>, List<IEnumValue>> enumValuesMap = new HashMap<Class<IEnumValue>, List<IEnumValue>>(); 
    
    public InMemoryRuntimeRepository() {
        super("InMemoryRuntimeRepository");
    }

    public InMemoryRuntimeRepository(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponent getProductComponentInternal(String id) {
        return productCmpts.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponent getProductComponentInternal(String kindId, String versionId) {
        if (kindId == null) {
            return null;
        }
        if (versionId == null) {
            throw new RuntimeException("Not implemented yet.");
        }
        for (Iterator<IProductComponent> it = productCmpts.values().iterator(); it.hasNext();) {
            IProductComponent cmpt = it.next();
            if (kindId.equals(cmpt.getKindId()) && versionId.equals(cmpt.getVersionId())) {
                return cmpt;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void getAllProductComponents(String kindId, List<IProductComponent> result) {
        for (Iterator<IProductComponent> it = productCmpts.values().iterator(); it.hasNext();) {
            IProductComponent cmpt = it.next();
            if (kindId.equals(cmpt.getKindId())) {
                result.add(cmpt);
            }
        }
    }

    private SortedSet<IProductComponentGeneration> getLoadedProductCmptGenerations(String productCmptId) {
        return productCmptGenLists.get(productCmptId);
    }

    /**
     * {@inheritDoc}
     */
    public void getProductComponentGenerations(IProductComponent productCmpt, List<IProductComponentGeneration> result) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(productCmpt.getId());
        if (genSet != null) {
            result.addAll(genSet);
        }
    }

    /**
     * Nothing to do for the inmemory repository.
     */
    public void reload() {
    }

    /**
     * InMemoryRepository also searches for tables that are instances of subclasses of the given
     * tableClass. This allows to mock a table class for testing purposes.
     * 
     * {@inheritDoc}
     */
    public ITable getTableInternal(Class<?> tableClass) {
        for (Iterator<ITable> it = tables.iterator(); it.hasNext();) {
            ITable table = it.next();
            if (tableClass.isAssignableFrom(table.getClass())) {
                return table;
            }
        }
        return null;
    }

    /**
     * Puts the table intto the repository. Replaces any table instance of the same class or any of
     * it's superclasses. The latter check is needed to replace tables with mock implementations.
     * 
     * @throws NullPointerException if table is <code>null</code>.
     */
    public void putTable(ITable table) {
        Class<? extends ITable> tableClass = table.getClass();
        for (Iterator<ITable> it = tables.iterator(); it.hasNext();) {
            ITable each = it.next();
            if (each.getClass().isAssignableFrom(tableClass)) {
                it.remove();
            }
        }
        tables.add(table);
    }

    /**
     * Puts the table with the indicated name into the repository with . Replaces any table instance
     * with the same qualified name.
     * 
     * @throws NullPointerException if table or qName is <code>null</code>.
     */
    public void putTable(ITable table, String qName) {
        multipleContentTables.put(qName, table);
        putTable(table);
    }

    public void putEnumValue(Class<IEnumValue> clazz, List<IEnumValue> enumValues){
        enumValuesMap.put(clazz, enumValues);
    }
    
    /**
     * {@inheritDoc}
     */
    public ITable getTableInternal(String qualifiedTableName) {
        return multipleContentTables.get(qualifiedTableName);
    }

    /**
     * {@inheritDoc}
     */
    public void getAllProductComponents(List<IProductComponent> result) {
        result.addAll(productCmpts.values());
    }

    /**
     * {@inheritDoc}
     */
    public void getAllProductComponentIds(List<String> result) {
        result.addAll(productCmpts.keySet());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModifiable() {
        return true;
    }

    /**
     * Puts the product component into the repository. If the repository already contains a
     * component with the same id, the new component replaces the old one.
     * 
     * @throws IllegalRepositoryModificationException if this repository does not allows to modify
     * @throws NullPointerException if cmpt is <code>null</code> it's contents.
     * 
     * @see IRuntimeRepository#isModifiable()
     */
    public void putProductComponent(IProductComponent productCmpt) {
        if (productCmpt == null) {
            throw new NullPointerException();
        }
        productCmpts.put(productCmpt.getId(), productCmpt);
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getProductComponentGenerationInternal(String productCmptId,
            Calendar effectiveDate) {
        if (productCmptId == null) {
            return null;
        }
        if (effectiveDate == null) {
            return null;
        }
        SortedSet<IProductComponentGeneration> genSortedSet = getGenerationSortedSet(productCmptId);
        IProductComponentGeneration foundGen = null;
        long effectiveTime = effectiveDate.getTimeInMillis();
        long foundGenValidFrom = Long.MIN_VALUE;
        long genValidFrom;
        for (Iterator<IProductComponentGeneration> it = genSortedSet.iterator(); it.hasNext();) {
            IProductComponentGeneration gen = it.next();
            genValidFrom = gen.getValidFrom(effectiveDate.getTimeZone()).getTime();
            if (effectiveTime >= genValidFrom && genValidFrom > foundGenValidFrom) {
                foundGen = gen;
                foundGenValidFrom = genValidFrom;
            }
        }
        return foundGen;
    }

    /**
     * Puts the product component generation and it's product componet into the repository. If the
     * repository already contains a generation with the same id, the new component replaces the old
     * one. The same applies for the product component.
     * 
     * @throws IllegalRepositoryModificationException if this repository does not allows to modify
     *             it's contents.
     * @throws NullPointerException if generation is <code>null</code>
     * 
     * @see IRuntimeRepository#isModifiable()
     */
    public void putProductCmptGeneration(IProductComponentGeneration generation) {
        if (generation == null) {
            throw new NullPointerException();
        }
        getGenerationSortedSet(generation.getProductComponent().getId()).add(generation);
        putProductComponent(generation.getProductComponent());
    }

    private SortedSet<IProductComponentGeneration> getGenerationSortedSet(String productCmptId) {
        SortedSet<IProductComponentGeneration> genSortedSet = productCmptGenLists.get(productCmptId);
        if (genSortedSet == null) {
            genSortedSet = new TreeSet<IProductComponentGeneration>(new ProductCmptGenerationComparator(TimeZone
                    .getDefault()));
            productCmptGenLists.put(productCmptId, genSortedSet);
        }
        return genSortedSet;
    }

    /**
     * {@inheritDoc}
     */
    protected void getAllIpsTestCases(List<IpsTest2> result, IRuntimeRepository runtimeRepository) {
        // ignore the runtimeRepository to instantiate the test case because we using in memory
        result.addAll(testCasesByQName.values());
    }

    /**
     * {@inheritDoc}
     */
    protected void getIpsTestCasesStartingWith(String qNamePrefix,
            List<IpsTest2> result,
            IRuntimeRepository runtimeRepository) {
        for (Iterator<String> iter = testCasesByQName.keySet().iterator(); iter.hasNext();) {
            String qName = iter.next();
            if (qName.startsWith(qNamePrefix)) {
                result.add(testCasesByQName.get(qName));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected IpsTestCaseBase getIpsTestCaseInternal(String qName, IRuntimeRepository runtimeRepository) {
        // ignore the runtimeRepository to instantiate the test case because we using in memory
        return testCasesByQName.get(qName);
    }

    /**
     * Puts the test case into the repository.
     */
    public void putIpsTestCase(IpsTestCaseBase test) {
        testCasesByQName.put(test.getQualifiedName(), test);
    }

    @Override
    protected void getAllModelTypeImplementationClasses(Set<String> result) {
        throw new RuntimeException("Currently not supported by InMemoryRuntimeRepository.");
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getNextProductComponentGenerationInternal(IProductComponentGeneration generation) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(generation
                .getProductComponent().getId());
        SortedSet<IProductComponentGeneration> successor = genSet.tailSet(generation);
        if (successor == null) {
            return null;
        }
        for (Iterator<IProductComponentGeneration> it = successor.iterator(); it.hasNext();) {
            IProductComponentGeneration next = it.next();
            if (next.equals(generation)) {
                continue;
            }
            return next;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected int getNumberOfProductComponentGenerationsInternal(IProductComponent productCmpt) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(productCmpt.getId());
        return genSet.size();
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getPreviousProductComponentGenerationInternal(IProductComponentGeneration generation) {
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(generation
                .getProductComponent().getId());
        SortedSet<IProductComponentGeneration> predecessors = genSet.headSet(generation);
        if (predecessors.isEmpty()) {
            return null;
        }
        return predecessors.last();
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        if (productCmpt == null) {
            throw new NullPointerException("The parameter productCmpt cannot be null.");
        }
        SortedSet<IProductComponentGeneration> genSet = getLoadedProductCmptGenerations(productCmpt.getId());
        return genSet.last();
    }

    private static class ProductCmptGenerationComparator implements Comparator<IProductComponentGeneration> {

        private TimeZone timeZone;

        private ProductCmptGenerationComparator(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        public int compare(IProductComponentGeneration gen1, IProductComponentGeneration gen2) {

            if (gen1.getValidFrom(timeZone).before(gen2.getValidFrom(timeZone))) {
                return -1;
            }
            if (gen1.getValidFrom(timeZone).after(gen2.getValidFrom(timeZone))) {
                return 1;
            }
            return 0;
        }

    }

    @Override
    protected <T extends IEnumValue> List<T> getEnumValuesInternal(Class<T> clazz) {
        return (List<T>)enumValuesMap.get(clazz);
    }

}
