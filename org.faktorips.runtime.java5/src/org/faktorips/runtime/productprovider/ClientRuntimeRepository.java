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
import org.faktorips.runtime.IVersionChecker;
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
public class ClientRuntimeRepository implements IRuntimeRepository, IVersionChecker {

    private final ProductDataProviderRuntimeRepository pdpRepository;

    private String version;

    public ClientRuntimeRepository(ProductDataProviderRuntimeRepository pdpRepository) {
        this.pdpRepository = pdpRepository;
        version = pdpRepository.getLocalVersion();
    }

    public synchronized boolean reloadIfModified() {
        boolean modified = checkBaseVersion(getLocalVersion());
        if (modified) {
            reload();
        }
        return modified;
    }

    /**
     * @return Returns the version.
     */
    public String getLocalVersion() {
        return version;
    }

    public boolean checkLocalVersion(String version) {
        return !getLocalVersion().equals(version);
    }

    public boolean checkBaseVersion(String version) {
        return checkLocalVersion(version) || pdpRepository.checkBaseVersion(version);
    }

    public synchronized void reload() {
        pdpRepository.reloadIfModified();
        version = pdpRepository.getLocalVersion();
    }

    public synchronized void throwExceptionIfModified() {
        if (pdpRepository.checkLocalVersion(getLocalVersion())) {
            throw new DataModifiedRuntimeException("Product data has changed", getLocalVersion(), pdpRepository
                    .getLocalVersion());
        }
    }

    /*
     * ============================================================================================
     * all other methods of the interface IRuntimeRepository delegate to pdpRepository and then call
     * throwExceptionIfVersionExpired()
     * 
     * ============================================================================================
     */

    public synchronized List<IpsTest2> getAllIpsTestCases(IRuntimeRepository runtimeRepository) {
        List<IpsTest2> allIpsTestCases = pdpRepository.getAllIpsTestCases(runtimeRepository);
        throwExceptionIfModified();
        return allIpsTestCases;
    }

    public synchronized Set<String> getAllModelTypeImplementationClasses() {
        Set<String> allModelTypeImplementationClasses = pdpRepository.getAllModelTypeImplementationClasses();
        throwExceptionIfModified();
        return allModelTypeImplementationClasses;
    }

    public synchronized List<String> getAllProductComponentIds() {
        List<String> allProductComponentIds = pdpRepository.getAllProductComponentIds();
        throwExceptionIfModified();
        return allProductComponentIds;
    }

    public synchronized List<IProductComponent> getAllProductComponents(String kindId) {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents(kindId);
        throwExceptionIfModified();
        return allProductComponents;
    }

    public synchronized List<IProductComponent> getAllProductComponents(Class<?> productComponentType) {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents(productComponentType);
        throwExceptionIfModified();
        return allProductComponents;
    }

    public synchronized List<IProductComponent> getAllProductComponents() {
        List<IProductComponent> allProductComponents = pdpRepository.getAllProductComponents();
        throwExceptionIfModified();
        return allProductComponents;
    }

    public synchronized List<IRuntimeRepository> getAllReferencedRepositories() {
        List<IRuntimeRepository> allReferencedRepositories = pdpRepository.getAllReferencedRepositories();
        throwExceptionIfModified();
        return allReferencedRepositories;
    }

    public synchronized List<ITable> getAllTables() {
        List<ITable> allTables = pdpRepository.getAllTables();
        throwExceptionIfModified();
        return allTables;
    }

    public synchronized List<IRuntimeRepository> getDirectlyReferencedRepositories() {
        List<IRuntimeRepository> directlyReferencedRepositories = pdpRepository.getDirectlyReferencedRepositories();
        throwExceptionIfModified();
        return directlyReferencedRepositories;
    }

    public synchronized <T> T getEnumValue(Class<T> clazz, Object id) {
        T enumValue = pdpRepository.getEnumValue(clazz, id);
        throwExceptionIfModified();
        return enumValue;
    }

    public synchronized Object getEnumValue(String uniqueId) {
        Object enumValue = pdpRepository.getEnumValue(uniqueId);
        throwExceptionIfModified();
        return enumValue;
    }

    public synchronized <T> IEnumValueLookupService<T> getEnumValueLookupService(Class<T> enumClazz) {
        IEnumValueLookupService<T> enumValueLookupService = pdpRepository.getEnumValueLookupService(enumClazz);
        throwExceptionIfModified();
        return enumValueLookupService;
    }

    public synchronized <T> List<T> getEnumValues(Class<T> clazz) {
        List<T> enumValues = pdpRepository.getEnumValues(clazz);
        throwExceptionIfModified();
        return enumValues;
    }

    public synchronized IProductComponent getExistingProductComponent(String id) throws ProductCmptNotFoundException {
        IProductComponent existingProductComponent = pdpRepository.getExistingProductComponent(id);
        throwExceptionIfModified();
        return existingProductComponent;
    }

    public synchronized IProductComponentGeneration getExistingProductComponentGeneration(String id,
            Calendar effectiveDate) {
        IProductComponentGeneration existingProductComponentGeneration = pdpRepository
                .getExistingProductComponentGeneration(id, effectiveDate);
        throwExceptionIfModified();
        return existingProductComponentGeneration;
    }

    public synchronized IFormulaEvaluatorFactory getFormulaEvaluatorFactory() {
        IFormulaEvaluatorFactory formulaEvaluatorFactory = pdpRepository.getFormulaEvaluatorFactory();
        throwExceptionIfModified();
        return formulaEvaluatorFactory;
    }

    public synchronized IpsTest2 getIpsTest(String qName) {
        IpsTest2 ipsTest = pdpRepository.getIpsTest(qName);
        throwExceptionIfModified();
        return ipsTest;
    }

    public synchronized IpsTest2 getIpsTest(String qName, IRuntimeRepository runtimeRepository) {
        IpsTest2 ipsTest = pdpRepository.getIpsTest(qName, runtimeRepository);
        throwExceptionIfModified();
        return ipsTest;
    }

    public synchronized IpsTestCaseBase getIpsTestCase(String qName) {
        IpsTestCaseBase ipsTestCase = pdpRepository.getIpsTestCase(qName);
        throwExceptionIfModified();
        return ipsTestCase;
    }

    public synchronized IpsTestCaseBase getIpsTestCase(String qName, IRuntimeRepository runtimeRepository) {
        IpsTestCaseBase ipsTestCase = pdpRepository.getIpsTestCase(qName, runtimeRepository);
        throwExceptionIfModified();
        return ipsTestCase;
    }

    public synchronized List<IpsTest2> getIpsTestCasesStartingWith(String qNamePrefix,
            IRuntimeRepository runtimeRepository) {
        List<IpsTest2> ipsTestCasesStartingWith = pdpRepository.getIpsTestCasesStartingWith(qNamePrefix,
                runtimeRepository);
        throwExceptionIfModified();
        return ipsTestCasesStartingWith;
    }

    public synchronized IpsTestSuite getIpsTestSuite(String qNamePrefix) {
        IpsTestSuite ipsTestSuite = pdpRepository.getIpsTestSuite(qNamePrefix);
        throwExceptionIfModified();
        return ipsTestSuite;
    }

    public synchronized IpsTestSuite getIpsTestSuite(String qNamePrefix, IRuntimeRepository runtimeRepository) {
        IpsTestSuite ipsTestSuite = pdpRepository.getIpsTestSuite(qNamePrefix, runtimeRepository);
        throwExceptionIfModified();
        return ipsTestSuite;
    }

    public synchronized IProductComponentGeneration getLatestProductComponentGeneration(IProductComponent productCmpt) {
        IProductComponentGeneration latestProductComponentGeneration = pdpRepository
                .getLatestProductComponentGeneration(productCmpt);
        throwExceptionIfModified();
        return latestProductComponentGeneration;
    }

    public synchronized IModelType getModelType(Class<?> modelObjectClass) {
        IModelType modelType = pdpRepository.getModelType(modelObjectClass);
        throwExceptionIfModified();
        return modelType;
    }

    public synchronized IModelType getModelType(IModelObject modelObject) {
        IModelType modelType = pdpRepository.getModelType(modelObject);
        throwExceptionIfModified();
        return modelType;
    }

    public synchronized IModelType getModelType(IProductComponent modelObject) {
        IModelType modelType = pdpRepository.getModelType(modelObject);
        throwExceptionIfModified();
        return modelType;
    }

    public synchronized String getName() {
        String name = pdpRepository.getName();
        throwExceptionIfModified();
        return name;
    }

    public synchronized IProductComponentGeneration getNextProductComponentGeneration(IProductComponentGeneration generation) {
        IProductComponentGeneration nextProductComponentGeneration = pdpRepository
                .getNextProductComponentGeneration(generation);
        throwExceptionIfModified();
        return nextProductComponentGeneration;
    }

    public synchronized int getNumberOfProductComponentGenerations(IProductComponent productCmpt) {
        int numberOfProductComponentGenerations = pdpRepository.getNumberOfProductComponentGenerations(productCmpt);
        throwExceptionIfModified();
        return numberOfProductComponentGenerations;
    }

    public synchronized IProductComponentGeneration getPreviousProductComponentGeneration(IProductComponentGeneration generation) {
        IProductComponentGeneration previousProductComponentGeneration = pdpRepository
                .getPreviousProductComponentGeneration(generation);
        throwExceptionIfModified();
        return previousProductComponentGeneration;
    }

    public synchronized IProductComponent getProductComponent(String id) {
        IProductComponent productComponent = pdpRepository.getProductComponent(id);
        throwExceptionIfModified();
        return productComponent;
    }

    public synchronized IProductComponent getProductComponent(String kindId, String versionId) {
        IProductComponent productComponent = pdpRepository.getProductComponent(kindId, versionId);
        throwExceptionIfModified();
        return productComponent;
    }

    public synchronized IProductComponentGeneration getProductComponentGeneration(String id, Calendar effectiveDate) {
        IProductComponentGeneration productComponentGeneration = pdpRepository.getProductComponentGeneration(id,
                effectiveDate);
        throwExceptionIfModified();
        return productComponentGeneration;
    }

    public synchronized List<IProductComponentGeneration> getProductComponentGenerations(IProductComponent productCmpt) {
        List<IProductComponentGeneration> productComponentGenerations = pdpRepository
                .getProductComponentGenerations(productCmpt);
        throwExceptionIfModified();
        return productComponentGenerations;
    }

    public synchronized ITable getTable(Class<?> tableClass) {
        ITable table = pdpRepository.getTable(tableClass);
        throwExceptionIfModified();
        return table;
    }

    public synchronized ITable getTable(String qualifiedTableName) {
        ITable table = pdpRepository.getTable(qualifiedTableName);
        throwExceptionIfModified();
        return table;
    }

    public synchronized JAXBContext newJAXBContext() {
        JAXBContext newJAXBContext = pdpRepository.newJAXBContext();
        throwExceptionIfModified();
        return newJAXBContext;
    }

    public synchronized boolean isModifiable() {
        return pdpRepository.isModifiable();
    }

    public synchronized void addDirectlyReferencedRepository(IRuntimeRepository repository) {
        pdpRepository.addDirectlyReferencedRepository(repository);
    }

    public synchronized void addEnumValueLookupService(IEnumValueLookupService<?> lookupService) {
        pdpRepository.addEnumValueLookupService(lookupService);
    }

    public synchronized void removeEnumValueLookupService(IEnumValueLookupService<?> lookupService) {
        pdpRepository.removeEnumValueLookupService(lookupService);
    }

}
