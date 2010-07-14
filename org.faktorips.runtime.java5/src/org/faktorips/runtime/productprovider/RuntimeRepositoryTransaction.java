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

package org.faktorips.runtime.productprovider;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import org.faktorips.runtime.IEnumValueLookupService;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.ProductCmptNotFoundException;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.productprovider.DetachedContentRuntimeRepositoryManager.DetachedContentRuntimeRepository;
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.test.IpsTestSuite;

/**
 * This is an implementation of {@link IRuntimeRepository} to use the
 * {@link DetachedContentRuntimeRepository}.
 * <p>
 * This runtime repository delegates every method of {@link IRuntimeRepository} to a shared
 * {@link DetachedContentRuntimeRepository}. The {@link DetachedContentRuntimeRepository} is nested
 * in a {@link DetachedContentRuntimeRepositoryManager}. After the delegation, this transaction
 * checks whether it still valid. If it is invalid because the product data has changed, a
 * {@link DataModifiedRuntimeException} is thrown.
 * <p>
 * This implements a optimistic locking mechanism. That means that nothing happens until the
 * requested data is invalid.
 * 
 * @author dirmeier
 */
public class RuntimeRepositoryTransaction implements IRuntimeRepository {

    private final DetachedContentRuntimeRepositoryManager.DetachedContentRuntimeRepository pdpRepository;

    private final String version;

    /**
     * Constructor should only called by the {@link DetachedContentRuntimeRepositoryManager}
     * 
     * @param repository The {@link DetachedContentRuntimeRepository} to delegate all methods to
     * @param version The actual version this transaction is valid for
     */
    RuntimeRepositoryTransaction(DetachedContentRuntimeRepositoryManager.DetachedContentRuntimeRepository repository,
            String version) {
        this.pdpRepository = repository;
        this.version = version;
    }

    /**
     * Getting the version this transaction was built for.
     * 
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * This method is not allowed for transactions and will always throw an
     * {@link IllegalStateException}
     */
    public void reload() {
        pdpRepository.reload();
        // throw new IllegalStateException("This method is not allowed for transactions");
    }

    private void assertValidTransaction() {
        pdpRepository.assertValidTransaction(this);
    }

    /*
     * ============================================================================================
     * all other methods of the interface IRuntimeRepository delegate to pdpRepository and then call
     * throwExceptionIfVersionExpired()
     * 
     * ============================================================================================
     */

    public List<IpsTest2> getAllIpsTestCases(IRuntimeRepository runtimeRepository) {
        List<IpsTest2> allIpsTestCases = pdpRepository.getAllIpsTestCases(runtimeRepository);
        assertValidTransaction();
        return allIpsTestCases;
    }

    public Set<String> getAllModelTypeImplementationClasses() {
        Set<String> allModelTypeImplementationClasses = pdpRepository.getAllModelTypeImplementationClasses();
        assertValidTransaction();
        return allModelTypeImplementationClasses;
    }

    public List<String> getAllProductComponentIds() {
        List<String> allProductComponentIds = pdpRepository.getAllProductComponentIds();
        assertValidTransaction();
        return allProductComponentIds;
    }

    public List<IProductComponent> getAllProductComponents(String kindId) {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents(kindId);
        assertValidTransaction();
        return allProductComponents;
    }

    public List<IProductComponent> getAllProductComponents(Class<?> productComponentType) {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents(productComponentType);
        assertValidTransaction();
        return allProductComponents;
    }

    public List<IProductComponent> getAllProductComponents() {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents();
        assertValidTransaction();
        return allProductComponents;
    }

    public List<IRuntimeRepository> getAllReferencedRepositories() {
        List<IRuntimeRepository> allReferencedRepositories = pdpRepository.getAllReferencedRepositories();
        assertValidTransaction();
        return allReferencedRepositories;
    }

    public List<ITable> getAllTables() {
        List<ITable> allTables = pdpRepository.getAllTables();
        assertValidTransaction();
        return allTables;
    }

    public List<IRuntimeRepository> getDirectlyReferencedRepositories() {
        List<IRuntimeRepository> directlyReferencedRepositories = pdpRepository.getDirectlyReferencedRepositories();
        assertValidTransaction();
        return directlyReferencedRepositories;
    }

    public <T> T getEnumValue(Class<T> clazz, Object id) {
        T enumValue = pdpRepository.getEnumValue(clazz, id);
        assertValidTransaction();
        return enumValue;
    }

    public Object getEnumValue(String uniqueId) {
        Object enumValue = pdpRepository.getEnumValue(uniqueId);
        assertValidTransaction();
        return enumValue;
    }

    public <T> IEnumValueLookupService<T> getEnumValueLookupService(Class<T> enumClazz) {
        IEnumValueLookupService<T> enumValueLookupService = pdpRepository.getEnumValueLookupService(enumClazz);
        assertValidTransaction();
        return enumValueLookupService;
    }

    public <T> List<T> getEnumValues(Class<T> clazz) {
        List<T> enumValues = pdpRepository.getEnumValues(clazz);
        assertValidTransaction();
        return enumValues;
    }

    public IProductComponent getExistingProductComponent(String id) throws ProductCmptNotFoundException {
        IProductComponent existingProductComponent = pdpRepository.getExistingProductComponent(id);
        assertValidTransaction();
        return existingProductComponent;
    }

    public IProductComponentGeneration getExistingProductComponentGeneration(String id, Calendar effectiveDate) {
        IProductComponentGeneration existingProductComponentGeneration = pdpRepository
                .getExistingProductComponentGeneration(id, effectiveDate);
        assertValidTransaction();
        return existingProductComponentGeneration;
    }

    public IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
        IFormulaEvaluatorFactory formulaEvaluatorFactory = pdpRepository.getFormulaEvaluatorFactory();
        assertValidTransaction();
        return formulaEvaluatorFactory;
    }

    public IpsTest2 getIpsTest(String qName) {
        IpsTest2 ipsTest = pdpRepository.getIpsTest(qName);
        assertValidTransaction();
        return ipsTest;
    }

    public IpsTest2 getIpsTest(String qName, IRuntimeRepository runtimeRepository) {
        IpsTest2 ipsTest = pdpRepository.getIpsTest(qName, runtimeRepository);
        assertValidTransaction();
        return ipsTest;
    }

    public IpsTestCaseBase getIpsTestCase(String qName) {
        IpsTestCaseBase ipsTestCase = pdpRepository.getIpsTestCase(qName);
        assertValidTransaction();
        return ipsTestCase;
    }

    public IpsTestCaseBase getIpsTestCase(String qName, IRuntimeRepository runtimeRepository) {
        IpsTestCaseBase ipsTestCase = pdpRepository.getIpsTestCase(qName, runtimeRepository);
        assertValidTransaction();
        return ipsTestCase;
    }

    public List<IpsTest2> getIpsTestCasesStartingWith(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        List<IpsTest2> ipsTestCasesStartingWith = pdpRepository.getIpsTestCasesStartingWith(qNamePrefix,
                runtimeRepository);
        assertValidTransaction();
        return ipsTestCasesStartingWith;
    }

    public IpsTestSuite getIpsTestSuite(String qNamePrefix) {
        IpsTestSuite ipsTestSuite = pdpRepository.getIpsTestSuite(qNamePrefix);
        assertValidTransaction();
        return ipsTestSuite;
    }

    public IpsTestSuite getIpsTestSuite(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        IpsTestSuite ipsTestSuite = pdpRepository.getIpsTestSuite(qNamePrefix, runtimeRepository);
        assertValidTransaction();
        return ipsTestSuite;
    }

    public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        IProductComponentGeneration latestProductComponentGeneration = pdpRepository
                .getLatestProductComponentGeneration(productCmpt);
        assertValidTransaction();
        return latestProductComponentGeneration;
    }

    public IModelType getModelType(Class<?> modelObjectClass) {
        IModelType modelType = pdpRepository.getModelType(modelObjectClass);
        assertValidTransaction();
        return modelType;
    }

    public IModelType getModelType(IModelObject modelObject) {
        IModelType modelType = pdpRepository.getModelType(modelObject);
        assertValidTransaction();
        return modelType;
    }

    public IModelType getModelType(IProductComponent modelObject) {
        IModelType modelType = pdpRepository.getModelType(modelObject);
        assertValidTransaction();
        return modelType;
    }

    public String getName() {
        String name = pdpRepository.getName();
        assertValidTransaction();
        return name;
    }

    public IProductComponentGeneration getNextProductComponentGeneration(IProductComponentGeneration generation) {
        IProductComponentGeneration nextProductComponentGeneration = pdpRepository
                .getNextProductComponentGeneration(generation);
        assertValidTransaction();
        return nextProductComponentGeneration;
    }

    public int getNumberOfProductComponentGenerations(IProductComponent productCmpt) {
        int numberOfProductComponentGenerations = pdpRepository.getNumberOfProductComponentGenerations(productCmpt);
        assertValidTransaction();
        return numberOfProductComponentGenerations;
    }

    public IProductComponentGeneration getPreviousProductComponentGeneration(IProductComponentGeneration generation) {
        IProductComponentGeneration previousProductComponentGeneration = pdpRepository
                .getPreviousProductComponentGeneration(generation);
        assertValidTransaction();
        return previousProductComponentGeneration;
    }

    public IProductComponent getProductComponent(String id) {
        IProductComponent productComponent = pdpRepository.getProductComponent(id);
        assertValidTransaction();
        return productComponent;
    }

    public IProductComponent getProductComponent(String kindId, String versionId) {
        IProductComponent productComponent = pdpRepository.getProductComponent(kindId, versionId);
        assertValidTransaction();
        return productComponent;
    }

    public IProductComponentGeneration getProductComponentGeneration(String id, Calendar effectiveDate) {
        IProductComponentGeneration productComponentGeneration = pdpRepository.getProductComponentGeneration(id,
                effectiveDate);
        assertValidTransaction();
        return productComponentGeneration;
    }

    public List<IProductComponentGeneration> getProductComponentGenerations(IProductComponent productCmpt) {
        List<IProductComponentGeneration> productComponentGenerations = pdpRepository
                .getProductComponentGenerations(productCmpt);
        assertValidTransaction();
        return productComponentGenerations;
    }

    public ITable getTable(Class<?> tableClass) {
        ITable table = pdpRepository.getTable(tableClass);
        assertValidTransaction();
        return table;
    }

    public ITable getTable(String qualifiedTableName) {
        ITable table = pdpRepository.getTable(qualifiedTableName);
        assertValidTransaction();
        return table;
    }

    public JAXBContext newJAXBContext() {
        JAXBContext newJAXBContext = pdpRepository.newJAXBContext();
        assertValidTransaction();
        return newJAXBContext;
    }

    public boolean isModifiable() {
        return pdpRepository.isModifiable();
    }

    public void addDirectlyReferencedRepository(IRuntimeRepository repository) {
        pdpRepository.addDirectlyReferencedRepository(repository);
    }

    public void addEnumValueLookupService(IEnumValueLookupService<?> lookupService) {
        pdpRepository.addEnumValueLookupService(lookupService);
    }

    public void removeEnumValueLookupService(IEnumValueLookupService<?> lookupService) {
        pdpRepository.removeEnumValueLookupService(lookupService);
    }

}
