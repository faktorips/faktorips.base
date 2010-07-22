/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.internal.model.type.DuplicatePropertyNameValidator;
import org.faktorips.devtools.core.internal.model.type.Type;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.FormulaSignatureFinder;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeValidations;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeValidations;
import org.faktorips.devtools.core.util.TreeSetHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Element;

/**
 * Implementation of IProductCmptType.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptType extends Type implements IProductCmptType {

    private boolean configurationForPolicyCmptType = true;
    private String policyCmptType = ""; //$NON-NLS-1$
    private String instancesIconPath = null;

    private IpsObjectPartCollection<ITableStructureUsage> tableStructureUsages = new IpsObjectPartCollection<ITableStructureUsage>(
            this, TableStructureUsage.class, ITableStructureUsage.class, "TableStructureUsage"); //$NON-NLS-1$

    public ProductCmptType(IIpsSrcFile file) {
        super(file);
    }

    @Override
    protected IpsObjectPartCollection<? extends IAttribute> createCollectionForAttributes() {
        return new IpsObjectPartCollection<IProductCmptTypeAttribute>(this, ProductCmptTypeAttribute.class,
                IProductCmptTypeAttribute.class, ProductCmptTypeAttribute.TAG_NAME);
    }

    @Override
    protected IpsObjectPartCollection<? extends IMethod> createCollectionForMethods() {
        return new IpsObjectPartCollection<IProductCmptTypeMethod>(this, ProductCmptTypeMethod.class,
                IProductCmptTypeMethod.class, "Method"); //$NON-NLS-1$
    }

    @Override
    protected IpsObjectPartCollection<? extends IAssociation> createCollectionForAssociations() {
        return new IpsObjectPartCollection<IProductCmptTypeAssociation>(this, ProductCmptTypeAssociation.class,
                IProductCmptTypeAssociation.class, "Association"); //$NON-NLS-1$
    }

    protected Iterator<ITableStructureUsage> getIteratorForTableStructureUsages() {
        return tableStructureUsages.iterator();
    }

    @Override
    public IType findSupertype(IIpsProject project) throws CoreException {
        if (!hasSupertype()) {
            return null;
        }
        IProductCmptType supertype = findSuperProductCmptType(project);
        if (supertype != null) {
            return supertype;
        }
        return null;
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT_TYPE;
    }

    @Override
    public String getPolicyCmptType() {
        return policyCmptType;
    }

    @Override
    public void setPolicyCmptType(String newType) {
        String oldType = policyCmptType;
        policyCmptType = newType;
        valueChanged(oldType, newType);
    }

    @Override
    public boolean isConfigurationForPolicyCmptType() {
        return configurationForPolicyCmptType;
    }

    @Override
    public void setConfigurationForPolicyCmptType(boolean newValue) {
        boolean oldValue = configurationForPolicyCmptType;
        configurationForPolicyCmptType = newValue;
        if (!newValue && oldValue) {
            policyCmptType = ""; //$NON-NLS-1$
        }
        valueChanged(oldValue, newValue);
    }

    @Override
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException {
        if (!configurationForPolicyCmptType) {
            return null;
        }
        return ipsProject.findPolicyCmptType(policyCmptType);
    }

    @Override
    public IProductCmptType findSuperProductCmptType(IIpsProject project) throws CoreException {
        return (IProductCmptType)project.findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE, getSupertype());
    }

    @Override
    public IProdDefProperty[] findProdDefProperties(IIpsProject ipsProject) throws CoreException {
        ProdDefPropertyCollector collector = new ProdDefPropertyCollector(null, ipsProject);
        collector.start(this);
        return collector.getProperties();
    }

    /**
     * Returns a map containing the property names as keys and the properties as values. This method
     * searches the supertype hierarchy.
     * <p>
     * Note this is a model internal method, it is not part of the published interface.
     * 
     * @param propertyType The type of properties that should be included in the map.
     *            <code>null</code> indicates that all properties should be included in the map.
     */
    public LinkedHashMap<String, IProdDefProperty> getProdDefPropertiesMap(ProdDefPropertyType propertyType,
            IIpsProject ipsProject) throws CoreException {

        ProdDefPropertyCollector collector = new ProdDefPropertyCollector(propertyType, ipsProject);
        collector.start(this);
        return collector.getPropertyMap();
    }

    @Override
    public IProdDefProperty findProdDefProperty(String propName, IIpsProject ipsProject) throws CoreException {
        ProdDefPropertyType[] types = ProdDefPropertyType.values();
        for (ProdDefPropertyType type : types) {
            IProdDefProperty prop = findProdDefProperty(type, propName, ipsProject);
            if (prop != null) {
                return prop;
            }
        }
        return null;
    }

    @Override
    public IProdDefProperty findProdDefProperty(ProdDefPropertyType type, String propName, IIpsProject ipsProject)
            throws CoreException {
        if (ProdDefPropertyType.VALUE == type) {
            return findProductCmptTypeAttribute(propName, ipsProject);
        }
        if (ProdDefPropertyType.FORMULA == type) {
            return findFormulaSignature(propName, ipsProject);
        }
        if (ProdDefPropertyType.TABLE_CONTENT_USAGE == type) {
            return findTableStructureUsage(propName, ipsProject);
        }
        if (ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET != type) {
            throw new RuntimeException("Unknown type " + type); //$NON-NLS-1$
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProject);
        if (policyCmptType == null) {
            return null;
        }
        IPolicyCmptTypeAttribute attr = policyCmptType.findPolicyCmptTypeAttribute(propName, ipsProject);
        if (attr != null && attr.isProductRelevant()) {
            return attr;
        }
        return null;
    }

    @Override
    public IProductCmptTypeAttribute newProductCmptTypeAttribute() {
        return (IProductCmptTypeAttribute)newAttribute();
    }

    @Override
    public IProductCmptTypeAttribute newProductCmptTypeAttribute(String name) {
        IProductCmptTypeAttribute newAttribute = newProductCmptTypeAttribute();
        newAttribute.setName(name);
        return newAttribute;
    }

    @Override
    public IProductCmptTypeAttribute getProductCmptTypeAttribute(String name) {
        return (IProductCmptTypeAttribute)attributes.getPartByName(name);
    }

    @Override
    public IProductCmptTypeAttribute findProductCmptTypeAttribute(String name, IIpsProject ipsProject)
            throws CoreException {

        return (IProductCmptTypeAttribute)findAttribute(name, ipsProject);
    }

    @Override
    public IProductCmptTypeAttribute[] getProductCmptTypeAttributes() {
        return (IProductCmptTypeAttribute[])attributes.toArray(new IProductCmptTypeAttribute[attributes.size()]);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        policyCmptType = element.getAttribute(PROPERTY_POLICY_CMPT_TYPE);
        configurationForPolicyCmptType = Boolean.valueOf(
                element.getAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE)).booleanValue();
        instancesIconPath = element.getAttribute(PROPERTY_ICON_FOR_INSTANCES);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE, String
                .valueOf(configurationForPolicyCmptType));
        element.setAttribute(PROPERTY_POLICY_CMPT_TYPE, policyCmptType);

        element.setAttribute(PROPERTY_ICON_FOR_INSTANCES, instancesIconPath);
    }

    @Override
    public IProductCmptTypeAssociation newProductCmptTypeAssociation() {
        return (IProductCmptTypeAssociation)newAssociation();
    }

    @Override
    public ITableStructureUsage findTableStructureUsage(String roleName, IIpsProject project) throws CoreException {
        TableStructureUsageFinder finder = new TableStructureUsageFinder(project, roleName);
        finder.start(this);
        return finder.tsu;
    }

    @Override
    public int getNumOfTableStructureUsages() {
        return tableStructureUsages.size();
    }

    @Override
    public ITableStructureUsage getTableStructureUsage(String roleName) {
        return tableStructureUsages.getPartByName(roleName);
    }

    @Override
    public ITableStructureUsage[] getTableStructureUsages() {
        return tableStructureUsages.toArray(new ITableStructureUsage[tableStructureUsages.size()]);
    }

    @Override
    public int[] moveTableStructureUsage(int[] indexes, boolean up) {
        return tableStructureUsages.moveParts(indexes, up);
    }

    @Override
    public ITableStructureUsage newTableStructureUsage() {
        return tableStructureUsages.newPart();
    }

    @Override
    public IProductCmptTypeMethod[] getProductCmptTypeMethods() {
        return (IProductCmptTypeMethod[])methods.toArray(new IProductCmptTypeMethod[methods.size()]);
    }

    @Override
    public IProductCmptTypeAssociation[] getProductCmptTypeAssociations() {
        return (IProductCmptTypeAssociation[])associations
                .toArray(new IProductCmptTypeAssociation[associations.size()]);
    }

    @Override
    public IProductCmptTypeMethod[] getNonFormulaProductCmptTypeMethods() {
        ArrayList<IProductCmptTypeMethod> result = new ArrayList<IProductCmptTypeMethod>();
        for (IMethod method : methods) {
            if (!((IProductCmptTypeMethod)method).isFormulaSignatureDefinition()) {
                result.add((IProductCmptTypeMethod)method);
            }
        }
        return result.toArray(new IProductCmptTypeMethod[result.size()]);
    }

    @Override
    public IProductCmptTypeMethod newProductCmptTypeMethod() {
        return (IProductCmptTypeMethod)methods.newPart();
    }

    @Override
    public IProductCmptTypeMethod newFormulaSignature(String formulaName) {
        IProductCmptTypeMethod signature = newProductCmptTypeMethod();
        signature.setFormulaSignatureDefinition(true);
        signature.setFormulaName(formulaName);
        signature.setName(signature.getDefaultMethodName());
        return signature;
    }

    @Override
    public IProductCmptTypeMethod[] findSignaturesOfOverloadedFormulas(IIpsProject ipsProject) throws CoreException {
        ArrayList<IProductCmptTypeMethod> overloadedMethods = new ArrayList<IProductCmptTypeMethod>();
        for (IMethod method2 : methods) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)method2;
            if (method.isFormulaSignatureDefinition() && method.isOverloadsFormula()) {
                IProductCmptTypeMethod overloadedMethod = method.findOverloadedFormulaMethod(ipsProject);
                if (overloadedMethod != null) {
                    overloadedMethods.add(overloadedMethod);
                }
            }
        }
        return overloadedMethods.toArray(new IProductCmptTypeMethod[overloadedMethods.size()]);
    }

    @Override
    public IProductCmptTypeMethod getFormulaSignature(String formulaName) throws CoreException {
        if (StringUtils.isEmpty(formulaName)) {
            return null;
        }
        for (IMethod method2 : methods) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)method2;
            if (method.isFormulaSignatureDefinition() && formulaName.equalsIgnoreCase(method.getFormulaName())) {
                return method;
            }
        }
        return null;
    }

    @Override
    public IProductCmptTypeMethod[] getFormulaSignatures() {

        ArrayList<IProductCmptTypeMethod> result = new ArrayList<IProductCmptTypeMethod>();
        for (IMethod method2 : methods) {
            IProductCmptTypeMethod method = (IProductCmptTypeMethod)method2;
            if (method.isFormulaSignatureDefinition()) {
                result.add(method);
            }
        }
        return result.toArray(new IProductCmptTypeMethod[result.size()]);
    }

    @Override
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) throws CoreException {
        FormulaSignatureFinder finder = new FormulaSignatureFinder(ipsProject, formulaName, true);
        finder.start(this);
        return (IProductCmptTypeMethod)(finder.getMethods().size() != 0 ? finder.getMethods().get(0) : null);
    }

    @Override
    public IMethod[] findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject)
            throws CoreException {

        IMethod[] candidates = super.findOverrideMethodCandidates(onlyNotImplementedAbstractMethods, ipsProject);
        List<IProductCmptTypeMethod> overloadedMethods = Arrays.asList(findSignaturesOfOverloadedFormulas(ipsProject));
        ArrayList<IMethod> result = new ArrayList<IMethod>(candidates.length);
        for (IMethod candidate : candidates) {
            if (overloadedMethods.contains(candidate)) {
                continue;
            }
            result.add(candidate);
        }
        return result.toArray(new IMethod[result.size()]);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType supertype = findSuperProductCmptType(ipsProject);
        if (isConfigurationForPolicyCmptType()) {
            validatePolicyCmptTypeReference(supertype, ipsProject, list);
        } else {
            if (supertype != null && supertype.isConfigurationForPolicyCmptType()) {
                String text = Messages.ProductCmptType_TypeMustConfigureAPolicyCmptTypeIfSupertypeDoes;
                list.add(new Message(IProductCmptType.MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE,
                        text, Message.ERROR, this, IProductCmptType.PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE));
            }
        }
        validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(list, ipsProject);
        validateIfAnOverrideOfOverloadedFormulaExists(list, ipsProject);
        list.add(TypeValidations.validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType.POLICY_CMPT_TYPE,
                getQualifiedName(), ipsProject, this));

        validateIconPath(list, ipsProject);
    }

    // TODO pk: write test case
    private void validateIfAnOverrideOfOverloadedFormulaExists(MessageList msgList, IIpsProject ipsProject)
            throws CoreException {

        ArrayList<IProductCmptTypeMethod> overloadedSupertypeFormulaSignatures = new ArrayList<IProductCmptTypeMethod>();
        IProductCmptTypeMethod[] formulaSignatures = getFormulaSignatures();
        for (IProductCmptTypeMethod formulaSignature : formulaSignatures) {
            if (formulaSignature.isOverloadsFormula()) {
                IProductCmptTypeMethod method = formulaSignature.findOverloadedFormulaMethod(ipsProject);
                if (method != null) {
                    overloadedSupertypeFormulaSignatures.add(method);
                }
            }
        }

        IProductCmptTypeMethod[] nonFormulas = getNonFormulaProductCmptTypeMethods();
        for (IProductCmptTypeMethod overloadedMethod : overloadedSupertypeFormulaSignatures) {
            for (IProductCmptTypeMethod nonFormula : nonFormulas) {
                if (nonFormula.isSameSignature(overloadedMethod)) {
                    String text = NLS.bind(Messages.ProductCmptType_msgOverloadedFormulaMethodCannotBeOverridden,
                            overloadedMethod.getFormulaName());
                    msgList.add(new Message(MSGCODE_OVERLOADED_FORMULA_CANNOT_BE_OVERRIDDEN, text, Message.ERROR,
                            nonFormula, IIpsElement.PROPERTY_NAME));
                }
            }
        }
    }

    // TODO internationalize messages
    private void validateIconPath(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (isUseCustomInstanceIcon()) {
            InputStream stream = ipsProject.getResourceAsStream(getInstancesIcon());
            if (stream == null) {
                String text = "Icon file cannot be resolved. Check path: \"" + getInstancesIcon() + "\".";
                msgList.add(new Message(MSGCODE_ICON_PATH_INVALID, text, Message.ERROR, this,
                        PROPERTY_ICON_FOR_INSTANCES));
            } else {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }
    }

    @Override
    protected DuplicatePropertyNameValidator createDuplicatePropertyNameValidator(IIpsProject ipsProject) {
        return new ProductCmptTypeDuplicatePropertyNameValidator(ipsProject);
    }

    private void validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(MessageList msgList, IIpsProject ipsProject)
            throws CoreException {

        if (StringUtils.isEmpty(getPolicyCmptType())) {
            return;
        }
        IPolicyCmptType policyCmptType = findPolicyCmptType(ipsProject);
        if (policyCmptType != null) {
            msgList.add(ProductCmptTypeValidations.validateProductCmptTypeAbstractWhenPolicyCmptTypeAbstract(
                    policyCmptType.isAbstract(), isAbstract(), this));
        }
    }

    private void validatePolicyCmptTypeReference(IProductCmptType supertype, IIpsProject ipsProject, MessageList list)
            throws CoreException {

        IPolicyCmptType policyCmptTypeObj = findPolicyCmptType(ipsProject);
        if (policyCmptTypeObj == null) {
            String text = NLS.bind(Messages.ProductCmptType_PolicyCmptTypeDoesNotExist, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!policyCmptTypeObj.isConfigurableByProductCmptType()) {
            String text = NLS.bind(Messages.ProductCmptType_notMarkedAsConfigurable, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (!isSubtypeOrSameType(policyCmptTypeObj.findProductCmptType(ipsProject), ipsProject)) {
            String text = NLS.bind(Messages.ProductCmptType_policyCmptTypeDoesNotSpecifyThisType, policyCmptType);
            list.add(new Message(MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE, text, Message.ERROR, this,
                    PROPERTY_POLICY_CMPT_TYPE));
            return;
        }
        if (supertype == null) {
            return;
        }
        IPolicyCmptType policyCmptTypeOfSupertype = supertype.findPolicyCmptType(ipsProject);
        if (policyCmptTypeObj != policyCmptTypeOfSupertype
                && policyCmptTypeObj.findSupertype(ipsProject) != policyCmptTypeOfSupertype) {
            String text = Messages.ProductCmptType_InconsistentTypeHierarchies;
            list.add(new Message(MSGCODE_HIERARCHY_MISMATCH, text, Message.ERROR, this, new String[] {
                    PROPERTY_SUPERTYPE, PROPERTY_POLICY_CMPT_TYPE }));
            return;
        }
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) throws CoreException {
        Set<IDependency> dependencies = new HashSet<IDependency>();
        if (!StringUtils.isEmpty(getPolicyCmptType())) {
            IDependency dependency = IpsObjectDependency.createReferenceDependency(getQualifiedNameType(),
                    new QualifiedNameType(getPolicyCmptType(), IpsObjectType.POLICY_CMPT_TYPE));
            dependencies.add(dependency);
            addDetails(details, dependency, this, PROPERTY_POLICY_CMPT_TYPE);
        }
        // to force a check is a policy component type exists with the same qualified name
        dependencies.add(IpsObjectDependency.createReferenceDependency(getQualifiedNameType(), new QualifiedNameType(
                getQualifiedName(), IpsObjectType.POLICY_CMPT_TYPE)));
        dependsOn(dependencies, details);
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    @Override
    public IIpsSrcFile[] searchProductComponents(boolean includeSubtypes) throws CoreException {
        return searchMetaObjectSrcFiles(includeSubtypes);
    }

    @Override
    public IIpsSrcFile[] searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {
        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllProductCmptSrcFiles(this, includeSubtypes)));
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    @Override
    public String getInstancesIcon() {
        return instancesIconPath;
    }

    @Override
    public boolean isUseCustomInstanceIcon() {
        return StringUtils.isNotEmpty(instancesIconPath);
    }

    @Override
    public void setInstancesIcon(String path) {
        String oldPath = instancesIconPath;
        instancesIconPath = path;
        valueChanged(oldPath, instancesIconPath);
    }

    private static class TableStructureUsageFinder extends ProductCmptTypeHierarchyVisitor {

        private String tsuName;
        private ITableStructureUsage tsu = null;

        public TableStructureUsageFinder(IIpsProject project, String tsuName) {
            super(project);
            this.tsuName = tsuName;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            tsu = currentType.getTableStructureUsage(tsuName);
            return tsu == null;
        }

    }

    private static class ProdDefPropertyCollector extends ProductCmptTypeHierarchyVisitor {

        // if set, indicates the type of properties that are collected
        // if null, all properties are collected
        private ProdDefPropertyType propertyType;

        private List<IProductCmptTypeAttribute> myAttributes = new ArrayList<IProductCmptTypeAttribute>();
        private List<ITableStructureUsage> myTableStructureUsages = new ArrayList<ITableStructureUsage>();
        private List<IProductCmptTypeMethod> myFormulaSignatures = new ArrayList<IProductCmptTypeMethod>();
        private List<IPolicyCmptTypeAttribute> myPolicyCmptTypeAttributes = new ArrayList<IPolicyCmptTypeAttribute>();

        private Set<IPolicyCmptType> visitedPolicyCmptTypes = new HashSet<IPolicyCmptType>();

        public ProdDefPropertyCollector(ProdDefPropertyType propertyType, IIpsProject ipsProject) {
            super(ipsProject);
            this.propertyType = propertyType;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            ProductCmptType currType = (ProductCmptType)currentType;
            if (propertyType == null || ProdDefPropertyType.VALUE.equals(propertyType)) {
                addInReverseOrder(currType.attributes, myAttributes);
            }
            if (propertyType == null || ProdDefPropertyType.TABLE_CONTENT_USAGE.equals(propertyType)) {
                addInReverseOrder(currType.tableStructureUsages, myTableStructureUsages);
            }
            if (propertyType == null || ProdDefPropertyType.FORMULA.equals(propertyType)) {
                for (int i = currType.methods.size() - 1; i >= 0; i--) {
                    IProductCmptTypeMethod method = (IProductCmptTypeMethod)currType.methods.getPart(i);
                    if (method.isFormulaSignatureDefinition()) {
                        myFormulaSignatures.add(method);
                    }
                }
            }
            if (propertyType == null || ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET.equals(propertyType)) {
                IPolicyCmptType policyCmptType = currentType.findPolicyCmptType(ipsProject);
                if (policyCmptType == null || visitedPolicyCmptTypes.contains(policyCmptType)) {
                    return true;
                }
                visitedPolicyCmptTypes.add(policyCmptType);
                IPolicyCmptTypeAttribute[] polAttr = policyCmptType.getPolicyCmptTypeAttributes();
                for (int i = polAttr.length - 1; i >= 0; i--) {
                    if (polAttr[i].isProductRelevant() && polAttr[i].isChangeable()) {
                        myPolicyCmptTypeAttributes.add(polAttr[i]);
                    }
                }
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private <T extends IIpsObjectPart> void addInReverseOrder(IpsObjectPartCollection<T> source, Collection target) {
            int size = source.size();
            for (int i = size - 1; i >= 0; i--) {
                target.add(source.getPart(i));
            }
        }

        public IProdDefProperty[] getProperties() {
            int size = size();
            IProdDefProperty[] props = new IProdDefProperty[size];
            int counter = 0;
            for (int i = myAttributes.size() - 1; i >= 0; i--) {
                props[counter++] = myAttributes.get(i);
            }
            for (int i = myTableStructureUsages.size() - 1; i >= 0; i--) {
                props[counter++] = myTableStructureUsages.get(i);
            }
            for (int i = myFormulaSignatures.size() - 1; i >= 0; i--) {
                props[counter++] = myFormulaSignatures.get(i);
            }
            for (int i = myPolicyCmptTypeAttributes.size() - 1; i >= 0; i--) {
                props[counter++] = myPolicyCmptTypeAttributes.get(i);
            }
            return props;
        }

        public LinkedHashMap<String, IProdDefProperty> getPropertyMap() {
            LinkedHashMap<String, IProdDefProperty> propertyMap = new LinkedHashMap<String, IProdDefProperty>(size());
            add(propertyMap, myAttributes);
            add(propertyMap, myTableStructureUsages);
            add(propertyMap, myFormulaSignatures);
            add(propertyMap, myPolicyCmptTypeAttributes);
            return propertyMap;
        }

        private void add(Map<String, IProdDefProperty> propertyMap, List<? extends IProdDefProperty> propertyList) {
            for (IProdDefProperty property : propertyList) {
                propertyMap.put(property.getPropertyName(), property);
            }
        }

        private int size() {
            return myAttributes.size() + myTableStructureUsages.size() + myFormulaSignatures.size()
                    + myPolicyCmptTypeAttributes.size();
        }
    }

    private static class ProductCmptTypeDuplicatePropertyNameValidator extends DuplicatePropertyNameValidator {

        public ProductCmptTypeDuplicatePropertyNameValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected Message createMessage(String propertyName, ObjectProperty[] invalidObjProperties) {
            // test if only formulas are involved
            boolean onlyFormulas = true;
            boolean onlyFormulasInSameType = true;
            IProductCmptType prodType = null;
            for (ObjectProperty invalidObjPropertie : invalidObjProperties) {
                Object obj = invalidObjPropertie.getObject();
                if (!(obj instanceof IProductCmptTypeMethod)) {
                    onlyFormulas = false;
                    onlyFormulasInSameType = false;
                    break;
                } else {
                    if (prodType == null) {
                        prodType = ((IProductCmptTypeMethod)obj).getProductCmptType();
                        onlyFormulasInSameType = true;
                    }
                    if (onlyFormulasInSameType && !prodType.equals(((IProductCmptTypeMethod)obj).getProductCmptType())) {
                        onlyFormulasInSameType = false;
                    }
                }
            }
            if (onlyFormulasInSameType) {
                String text = Messages.ProductCmptType_msgDuplicateFormulasNotAllowedInSameType;
                return new Message(MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE, text, Message.ERROR,
                        invalidObjProperties);
            } else if (onlyFormulas) {
                String text = NLS.bind(Messages.ProductCmptType_DuplicateFormulaName, propertyName);
                return new Message(MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY, text, Message.ERROR,
                        invalidObjProperties);
            } else {
                String text = NLS.bind(Messages.ProductCmptType_multiplePropertyNames, propertyName);
                return new Message(IType.MSGCODE_DUPLICATE_PROPERTY_NAME, text, Message.ERROR, invalidObjProperties);
            }
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            super.visit(currentType);
            ProductCmptType productCmptType = (ProductCmptType)currentType;
            for (Iterator<ITableStructureUsage> it = productCmptType.getIteratorForTableStructureUsages(); it.hasNext();) {
                ITableStructureUsage tsu = it.next();
                add(tsu.getRoleName(), new ObjectProperty(tsu, ITableStructureUsage.PROPERTY_ROLENAME));
            }
            for (Iterator<? extends IMethod> it = productCmptType.getIteratorForMethods(); it.hasNext();) {
                IProductCmptTypeMethod method = (IProductCmptTypeMethod)it.next();
                if (method.isFormulaSignatureDefinition() && StringUtils.isNotEmpty(method.getFormulaName())
                        && !method.isOverloadsFormula()) {
                    add(method.getFormulaName(), new ObjectProperty(method,
                            IProductCmptTypeMethod.PROPERTY_FORMULA_NAME));
                }
            }
            return true;
        }
    }

}
