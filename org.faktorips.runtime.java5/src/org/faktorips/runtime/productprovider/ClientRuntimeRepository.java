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
import org.faktorips.runtime.test.IpsTest2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.runtime.test.IpsTestSuite;

/**
 * <p>
 * This runtime repository delegates every method of {@link IRuntimeRepository} to a shared
 * {@link ProductDataProviderRuntimeRepository}. After the delegation, the locally stored version is
 * compared with the actual Version of the {@link ProductDataProviderRuntimeRepository}. If the
 * version is expired, a {@link DataModifiedRuntimeException} is thrown.
 * <p>
 * It is important to check the version after the delegation. In this case it is possible to get a
 * false-negative result i.e. an exception is thrown although the product data was correct. But
 * checking the version before delegating the getter method it would be possible to get product data
 * with wrong version without exception. Here are the two szenario:
 * <p>
 * Szenario 'false positiv': This is possible if check version would be before delegation. In this
 * implementation this case should not be possible. Client1 and Client 2 are two instances of
 * {@link ClientRuntimeRepository}:
 * <ol>
 * <li>Client1 check version (ok)</li>
 * <li>Client2 check version (ok)</li>
 * <li>>> Product Data changes!</li>
 * <li>Client1 get product data --> exception</li>
 * <li>Client1 reload table of contents</li>
 * <li>Client2 get product data --> should throw an exception but repository was already reloaded</li>
 * </ol>
 * <p>
 * Szenario 'false negativ': Could happen in this implementation. Client1 and Client 2 are two
 * instances of {@link ClientRuntimeRepository}:
 * <ol>
 * <li>Client1 get product data (ok)</li>
 * <li>>> Product Data changes!</li>
 * <li>Client2 get product data --> exception</li>
 * <li>Client2 reload table of contents</li>
 * <li>Client1 check version --> exception although the received data was of the correct (old)
 * version</li>
 * </ol>
 * 
 * @author dirmeier
 */
public class ClientRuntimeRepository implements IRuntimeRepository {

    private final ProductDataProviderRuntimeRepository pdpRepository;

    private String version;

    public ClientRuntimeRepository(ProductDataProviderRuntimeRepository pdpRepository) {
        this.pdpRepository = pdpRepository;
        version = pdpRepository.getProductDataVersion();
        reload();
    }

    public boolean checkForModifications() {
        boolean modified = pdpRepository.isExpired(version) || pdpRepository.checkForModifications();
        if (modified) {
            version = pdpRepository.getProductDataVersion();
        }
        return modified;
    }

    public void reload() {
        pdpRepository.reload();
        version = pdpRepository.getProductDataVersion();
    }

    public void throwExceptionIfVersionExpired() {
        if (pdpRepository.isExpired(version)) {
            throw new DataModifiedRuntimeException("Product data has changed", version, pdpRepository
                    .getProductDataVersion());
        }
    }

    /*
     * ==========================================================================================
     * all other methods of the interface IRuntimeRepository call checkForModifications() and then
     * delegate to pdpRepository
     * ==========================================================================================
     */

    public List<IpsTest2> getAllIpsTestCases(IRuntimeRepository runtimeRepository) {
        List<IpsTest2> allIpsTestCases = pdpRepository.getAllIpsTestCases(runtimeRepository);
        throwExceptionIfVersionExpired();
        return allIpsTestCases;
    }

    public Set<String> getAllModelTypeImplementationClasses() {
        Set<String> allModelTypeImplementationClasses = pdpRepository.getAllModelTypeImplementationClasses();
        throwExceptionIfVersionExpired();
        return allModelTypeImplementationClasses;
    }

    public List<String> getAllProductComponentIds() {
        List<String> allProductComponentIds = pdpRepository.getAllProductComponentIds();
        throwExceptionIfVersionExpired();
        return allProductComponentIds;
    }

    public List<IProductComponent> getAllProductComponents(String kindId) {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents(kindId);
        throwExceptionIfVersionExpired();
        return allProductComponents;
    }

    public List<IProductComponent> getAllProductComponents(Class<?> productComponentType) {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents(productComponentType);
        throwExceptionIfVersionExpired();
        return allProductComponents;
    }

    public List<IProductComponent> getAllProductComponents() {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents();
        throwExceptionIfVersionExpired();
        return allProductComponents;
    }

    public List<IRuntimeRepository> getAllReferencedRepositories() {
        List<IRuntimeRepository> allReferencedRepositories = pdpRepository.getAllReferencedRepositories();
        throwExceptionIfVersionExpired();
        return allReferencedRepositories;
    }

    public List<ITable> getAllTables() {
        List<ITable> allTables = pdpRepository.getAllTables();
        throwExceptionIfVersionExpired();
        return allTables;
    }

    public List<IRuntimeRepository> getDirectlyReferencedRepositories() {
        List<IRuntimeRepository> directlyReferencedRepositories = pdpRepository.getDirectlyReferencedRepositories();
        throwExceptionIfVersionExpired();
        return directlyReferencedRepositories;
    }

    public <T> T getEnumValue(Class<T> clazz, Object id) {
        T enumValue = pdpRepository.getEnumValue(clazz, id);
        throwExceptionIfVersionExpired();
        return enumValue;
    }

    public Object getEnumValue(String uniqueId) {
        Object enumValue = pdpRepository.getEnumValue(uniqueId);
        throwExceptionIfVersionExpired();
        return enumValue;
    }

    public <T> IEnumValueLookupService<T> getEnumValueLookupService(Class<T> enumClazz) {
        IEnumValueLookupService<T> enumValueLookupService = pdpRepository.getEnumValueLookupService(enumClazz);
        throwExceptionIfVersionExpired();
        return enumValueLookupService;
    }

    public <T> List<T> getEnumValues(Class<T> clazz) {
        List<T> enumValues = pdpRepository.getEnumValues(clazz);
        throwExceptionIfVersionExpired();
        return enumValues;
    }

    public IProductComponent getExistingProductComponent(String id) throws ProductCmptNotFoundException {
        IProductComponent existingProductComponent = pdpRepository.getExistingProductComponent(id);
        throwExceptionIfVersionExpired();
        return existingProductComponent;
    }

    public IProductComponentGeneration getExistingProductComponentGeneration(String id, Calendar effectiveDate) {
        IProductComponentGeneration existingProductComponentGeneration = pdpRepository
                .getExistingProductComponentGeneration(id, effectiveDate);
        throwExceptionIfVersionExpired();
        return existingProductComponentGeneration;
    }

    public IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
        IFormulaEvaluatorFactory formulaEvaluatorFactory = pdpRepository.getFormulaEvaluatorFactory();
        throwExceptionIfVersionExpired();
        return formulaEvaluatorFactory;
    }

    public IpsTest2 getIpsTest(String qName) {
        IpsTest2 ipsTest = pdpRepository.getIpsTest(qName);
        throwExceptionIfVersionExpired();
        return ipsTest;
    }

    public IpsTest2 getIpsTest(String qName, IRuntimeRepository runtimeRepository) {
        IpsTest2 ipsTest = pdpRepository.getIpsTest(qName, runtimeRepository);
        throwExceptionIfVersionExpired();
        return ipsTest;
    }

    public IpsTestCaseBase getIpsTestCase(String qName) {
        IpsTestCaseBase ipsTestCase = pdpRepository.getIpsTestCase(qName);
        throwExceptionIfVersionExpired();
        return ipsTestCase;
    }

    public IpsTestCaseBase getIpsTestCase(String qName, IRuntimeRepository runtimeRepository) {
        IpsTestCaseBase ipsTestCase = pdpRepository.getIpsTestCase(qName, runtimeRepository);
        throwExceptionIfVersionExpired();
        return ipsTestCase;
    }

    public List<IpsTest2> getIpsTestCasesStartingWith(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        List<IpsTest2> ipsTestCasesStartingWith = pdpRepository.getIpsTestCasesStartingWith(qNamePrefix,
                runtimeRepository);
        throwExceptionIfVersionExpired();
        return ipsTestCasesStartingWith;
    }

    public IpsTestSuite getIpsTestSuite(String qNamePrefix) {
        IpsTestSuite ipsTestSuite = pdpRepository.getIpsTestSuite(qNamePrefix);
        throwExceptionIfVersionExpired();
        return ipsTestSuite;
    }

    public IpsTestSuite getIpsTestSuite(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        IpsTestSuite ipsTestSuite = pdpRepository.getIpsTestSuite(qNamePrefix, runtimeRepository);
        throwExceptionIfVersionExpired();
        return ipsTestSuite;
    }

    public IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        IProductComponentGeneration latestProductComponentGeneration = pdpRepository
                .getLatestProductComponentGeneration(productCmpt);
        throwExceptionIfVersionExpired();
        return latestProductComponentGeneration;
    }

    public IModelType getModelType(Class<?> modelObjectClass) {
        IModelType modelType = pdpRepository.getModelType(modelObjectClass);
        throwExceptionIfVersionExpired();
        return modelType;
    }

    public IModelType getModelType(IModelObject modelObject) {
        IModelType modelType = pdpRepository.getModelType(modelObject);
        throwExceptionIfVersionExpired();
        return modelType;
    }

    public IModelType getModelType(IProductComponent modelObject) {
        IModelType modelType = pdpRepository.getModelType(modelObject);
        throwExceptionIfVersionExpired();
        return modelType;
    }

    public String getName() {
        String name = pdpRepository.getName();
        throwExceptionIfVersionExpired();
        return name;
    }

    public IProductComponentGeneration getNextProductComponentGeneration(IProductComponentGeneration generation) {
        IProductComponentGeneration nextProductComponentGeneration = pdpRepository
                .getNextProductComponentGeneration(generation);
        throwExceptionIfVersionExpired();
        return nextProductComponentGeneration;
    }

    public int getNumberOfProductComponentGenerations(IProductComponent productCmpt) {
        int numberOfProductComponentGenerations = pdpRepository.getNumberOfProductComponentGenerations(productCmpt);
        throwExceptionIfVersionExpired();
        return numberOfProductComponentGenerations;
    }

    public IProductComponentGeneration getPreviousProductComponentGeneration(IProductComponentGeneration generation) {
        IProductComponentGeneration previousProductComponentGeneration = pdpRepository
                .getPreviousProductComponentGeneration(generation);
        throwExceptionIfVersionExpired();
        return previousProductComponentGeneration;
    }

    public IProductComponent getProductComponent(String id) {
        IProductComponent productComponent = pdpRepository.getProductComponent(id);
        throwExceptionIfVersionExpired();
        return productComponent;
    }

    public IProductComponent getProductComponent(String kindId, String versionId) {
        IProductComponent productComponent = pdpRepository.getProductComponent(kindId, versionId);
        throwExceptionIfVersionExpired();
        return productComponent;
    }

    public IProductComponentGeneration getProductComponentGeneration(String id, Calendar effectiveDate) {
        IProductComponentGeneration productComponentGeneration = pdpRepository.getProductComponentGeneration(id,
                effectiveDate);
        throwExceptionIfVersionExpired();
        return productComponentGeneration;
    }

    public List<IProductComponentGeneration> getProductComponentGenerations(IProductComponent productCmpt) {
        List<IProductComponentGeneration> productComponentGenerations = pdpRepository
                .getProductComponentGenerations(productCmpt);
        throwExceptionIfVersionExpired();
        return productComponentGenerations;
    }

    public ITable getTable(Class<?> tableClass) {
        ITable table = pdpRepository.getTable(tableClass);
        throwExceptionIfVersionExpired();
        return table;
    }

    public ITable getTable(String qualifiedTableName) {
        ITable table = pdpRepository.getTable(qualifiedTableName);
        throwExceptionIfVersionExpired();
        return table;
    }

    public JAXBContext newJAXBContext() {
        JAXBContext newJAXBContext = pdpRepository.newJAXBContext();
        throwExceptionIfVersionExpired();
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
