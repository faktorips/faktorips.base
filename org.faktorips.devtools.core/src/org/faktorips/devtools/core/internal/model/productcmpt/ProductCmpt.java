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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.internal.model.ipsobject.TimedIpsObject;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ProductCmptValidations;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * Implementation of product component.
 * 
 * @author Jan Ortmann
 */
public class ProductCmpt extends TimedIpsObject implements IProductCmpt {

    private String productCmptType = ""; //$NON-NLS-1$
    private String runtimeId = ""; //$NON-NLS-1$

    public ProductCmpt(IIpsSrcFile file) {
        super(file);
    }

    public ProductCmpt() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptGeneration getProductCmptGeneration(int index) {
        return (IProductCmptGeneration)getGeneration(index);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersionId() throws CoreException {
        try {
            return getIpsProject().getProductCmptNamingStrategy().getVersionId(getName());
        } catch (IllegalArgumentException e) {
            throw new CoreException(new IpsStatus("Can't get version id for " + this, e)); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        IProductCmptType productCmptType = findProductCmptType(ipsProject);
        if (productCmptType == null) {
            return null;
        }
        return productCmptType.findPolicyCmptType(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProductCmptType() {
        return productCmptType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProductCmptType(String newType) {
        String oldType = productCmptType;
        productCmptType = newType;
        valueChanged(oldType, newType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findProductCmptType(productCmptType);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void sortPropertiesAccordingToModel(IIpsProject ipsProject) throws CoreException {
        int max = getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration gen = getProductCmptGeneration(i);
            gen.sortPropertiesAccordingToModel(ipsProject);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsObjectGeneration createNewGeneration(String id) {
        return new ProductCmptGeneration(this, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = ProductCmptValidations.validateProductCmptType(this, productCmptType, list, ipsProject);
        if (type != null) {
            try {
                MessageList list3 = type.validate(ipsProject);
                if (list3.getMessageByCode(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY) != null
                        || list3.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND) != null
                        || list3.getMessageByCode(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY) != null) {
                    String msg = NLS.bind(Messages.ProductCmpt_msgInvalidTypeHierarchy, getProductCmptType());
                    list.add(new Message(MSGCODE_INCONSISTENT_TYPE_HIERARCHY, msg, Message.ERROR, type,
                            IIpsElement.PROPERTY_NAME));
                }
            } catch (Exception e) {
                throw new CoreException(new IpsStatus("Error during validate of product component type", e)); //$NON-NLS-1$
            }
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsFormulaTest() {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            IProductCmptGeneration gen = getProductCmptGeneration(0);
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_PRODUCT_CMPT_TYPE, productCmptType);
        element.setAttribute(PROPERTY_RUNTIME_ID, runtimeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        productCmptType = element.getAttribute(PROPERTY_PRODUCT_CMPT_TYPE);
        runtimeId = element.getAttribute(PROPERTY_RUNTIME_ID);
    }

    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptTreeStructure getStructure(IIpsProject ipsProject) throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptTreeStructure getStructure(GregorianCalendar date, IIpsProject ipsProject)
            throws CycleInProductStructureException {
        return new ProductCmptTreeStructure(this, date, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRuntimeId() {
        return runtimeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRuntimeId(String runtimeId) {
        String oldId = this.runtimeId;
        this.runtimeId = runtimeId;
        valueChanged(oldId, runtimeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) throws CoreException {
        IIpsObjectGeneration[] generations = getGenerationsOrderedByValidDate();
        for (IIpsObjectGeneration generation : generations) {
            if (generation instanceof IProductCmptGeneration) {
                IGenerationToTypeDelta delta = ((IProductCmptGeneration)generation).computeDeltaToModel(ipsProject);
                if (!delta.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fixAllDifferencesToModel(IIpsProject ipsProject) throws CoreException {
        int max = getNumOfGenerations();
        for (int i = 0; i < max; i++) {
            IProductCmptGeneration generation = getProductCmptGeneration(i);
            IGenerationToTypeDelta delta = (generation).computeDeltaToModel(ipsProject);
            delta.fix();
        }
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUsedAsTargetProductCmpt(IIpsProject ipsProjectToSearch, IProductCmpt productCmptCandidate) {
        return isReferencingProductCmpt(ipsProjectToSearch, productCmptCandidate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.core.model.IIpsMetaObject#findMetaClass(org.faktorips.devtools.core
     * .model.ipsproject.IIpsProject)
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE, getProductCmptType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.core.model.IIpsMetaObject#getMetaClass()
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public String getMetaClass() {
        return getProductCmptType();
    }

}
