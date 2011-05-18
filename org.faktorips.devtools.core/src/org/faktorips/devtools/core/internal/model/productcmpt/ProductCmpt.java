/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.ProductCmptValidations;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of product component.
 * 
 * @author Jan Ortmann
 */
public class ProductCmpt extends TimedIpsObject implements IProductCmpt {

    private final AttributeValueContainer attributeValueContainer;
    private String productCmptType = ""; //$NON-NLS-1$
    private String runtimeId = ""; //$NON-NLS-1$

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
        attributeValueContainer = new AttributeValueContainer(this);
    }

    public ProductCmpt() {
        super();
        attributeValueContainer = new AttributeValueContainer(this);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration(int index) {
        return (IProductCmptGeneration)getGeneration(index);
    }

    @Override
    public IProductCmptKind findProductCmptKind() throws CoreException {
        IProductCmptNamingStrategy stratgey = getIpsProject().getProductCmptNamingStrategy();
        try {
            String kindName = stratgey.getKindId(getName());
            return new ProductCmptKind(kindName, getIpsProject().getRuntimeIdPrefix() + kindName);
        } catch (Exception e) {
            return null; // error in parsing the name results in a "not found" for the client
        }
    }

    @Override
    public String getVersionId() throws CoreException {
        try {
            return getIpsProject().getProductCmptNamingStrategy().getVersionId(getName());
        } catch (IllegalArgumentException e) {
            throw new CoreException(new IpsStatus("Can't get version id for " + this, e)); //$NON-NLS-1$
        }
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return productCmptType.findPolicyCmptType(ipsProject);
    }

    @Override
    public String getProductCmptType() {
        return productCmptType;
    }

    @Override
    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(productCmptType);
    }

    @Override
    public void sortPropertiesAccordingToModel(IIpsProject ipsProject) throws CoreException {
        int max = getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration gen = getProductCmptGeneration(i);
            gen.sortPropertiesAccordingToModel(ipsProject);
        }
    }

    @Override
    protected IpsObjectGeneration createNewGeneration(String id) {
        return new ProductCmptGeneration(this, id);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = ProductCmptValidations.validateProductCmptType(this, productCmptType, list, ipsProject);
        if (type == null) {
            return;
        }
        Message message = TypeValidations.validateTypeHierachy(type, ipsProject);
        if (message != null) {
            String typeLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(type);
            String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, typeLabel);
            list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, msg, Message.ERROR, type,
                    PROPERTY_PRODUCT_CMPT_TYPE));
            // do not continue validation if hierarchy is invalid
            return;
        }
        IProductCmptNamingStrategy strategy = ipsProject.getProductCmptNamingStrategy();
        MessageList list2 = strategy.validate(getName());
        for (Message msg : list2) {
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_NAME);
            list.add(msgNew);
        }
        list2 = strategy.validateRuntimeId(getRuntimeId());
        for (Message msg : list2) {
            Message msgNew = new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_RUNTIME_ID);
            list.add(msgNew);
        }

        list2 = getIpsProject().checkForDuplicateRuntimeIds(new IIpsSrcFile[] { getIpsSrcFile() });
        list.add(list2);
    }

    @Override
    public boolean containsFormula() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            if (((ProductCmptGeneration)generation).containsFormula()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsFormulaTest() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            IProductCmptGeneration gen = (IProductCmptGeneration)generation;
            if (gen.getNumOfFormulas() > 0) {
                IFormula[] formulas = gen.getFormulas();
                for (IFormula formula : formulas) {
                    if (formula.getFormulaTestCases().length > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) throws CoreException {
        Set<IDependency> dependencySet = new HashSet<IDependency>();

        if (!StringUtils.isEmpty(productCmptType)) {
            IDependency dependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                    new QualifiedNameType(productCmptType, IpsObjectType.PRODUCT_CMPT_TYPE));
            dependencySet.add(dependency);
            addDetails(details, dependency, this, PROPERTY_PRODUCT_CMPT_TYPE);
        }

        // add dependency to related product cmpt's and add dependency to table contents
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            ((ProductCmptGeneration)generation).dependsOn(dependencySet, details);
        }

        return dependencySet.toArray(new IDependency[dependencySet.size()]);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        runtimeId = element.getAttribute(PROPERTY_RUNTIME_ID);
    }

    @Override
    @Deprecated
    public IProductCmptTreeStructure getStructure(IIpsProject ipsProject) throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, ipsProject);
    }

    @Override
    public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject)
            throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, date, ipsProject);
    }

    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

    @Override
    public void setRuntimeId(String runtimeId) {
        String oldId = this.runtimeId;
        this.runtimeId = runtimeId;
        valueChanged(oldId, runtimeId);
    }

    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            if (generation instanceof IProductCmptGeneration) {
                IPropertyValueContainerToTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToModel(ipsProject);
                if (!delta.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        int max = getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = getProductCmptGeneration(i);
            IPropertyValueContainerToTypeDelta delta = (generation).computeDeltaToModel(ipsProject);
            delta.fix();
        }
    }

    @Override
    public boolean isReferencingProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate) {
        int numOfGenerations = getNumOfGenerations();
        for (int i = 0; i < numOfGenerations; i++) {
            IProductCmptGeneration generation = (IProductCmptGeneration)getGeneration(i);
            IProductCmptLink[] links = generation.getLinks();
            for (IProductCmptLink link : links) {
                if (productCmptCandidate.getQualifiedName().equals(link.getTarget())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isUsedAsTargetProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate) {
        return isReferencingProductCmpt(ipsProjectToSearch, productCmptCandidate);
    }

    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE, getProductCmptType());
    }

    @Override
    public String getMetaClass() {
        return getProductCmptType();
    }

    @Override
    public IPropertyValue getPropertyValue(IProductCmptProperty property) {
        return attributeValueContainer.getPropertyValue(property);
    }

    @Override
    public IPropertyValue getPropertyValue(String propertyName) {
        return attributeValueContainer.getPropertyValue(propertyName);
    }

    @Override
    public List<IPropertyValue> getPropertyValues(ProductCmptPropertyType type) {
        return attributeValueContainer.getPropertyValues(type);
    }

    @Override
    public IPropertyValue newPropertyValue(IProductCmptProperty property) {
        if (property.getProductCmptPropertyType() == ProductCmptPropertyType.VALUE) {
            return attributeValueContainer.newPropertyValue(property, getNextPartId());
        }
        return null;
    }

    @Override
    public IPropertyValueContainerToTypeDelta computeDeltaToModel(IIpsProject ipsProject) throws CoreException {
        return null;
    }

}
