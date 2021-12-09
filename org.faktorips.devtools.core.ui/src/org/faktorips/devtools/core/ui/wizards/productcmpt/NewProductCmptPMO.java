/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.ui.IIpsSrcFileViewItem;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IMultiLanguageSupport;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * The presentation model object for the {@link NewProductWizard}.
 * 
 * @author dirmeier
 */
public class NewProductCmptPMO extends NewProductDefinitionPMO {

    public static final String PROPERTY_SELECTED_BASE_TYPE = "selectedBaseType"; //$NON-NLS-1$

    public static final String PROPERTY_SELECTED_TYPE = "selectedType"; //$NON-NLS-1$

    public static final String PROPERTY_SELECTED_TEMPLATE = "selectedTemplate"; //$NON-NLS-1$

    public static final String PROPERTY_KIND_ID = "kindId"; //$NON-NLS-1$

    public static final String PROPERTY_VERSION_ID = "versionId"; //$NON-NLS-1$

    public static final String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$

    public static final String PROPERTY_TEMPLATE = "template"; //$NON-NLS-1$

    public static final String PROPERTY_NEED_VERSION_ID = "needVersionId"; //$NON-NLS-1$

    public static final String PROPERTY_SHOW_TEMPLATES = "showTemplates"; //$NON-NLS-1$

    public static final String PROPERTY_SHOW_DESCRIPTION = "showDescription"; //$NON-NLS-1$

    public static final ProductCmptViewItem NULL_TEMPLATE = new ProductCmptViewItem(null);

    private final NewProductCmptValidator validator;

    private IProductCmptType selectedBaseType;

    private IProductCmptType selectedType;

    private ProductCmptViewItem selectedTemplate;

    private final Set<IProductCmptType> baseTypes = new TreeSet<>(new BaseTypeComparator());

    private String kindId = StringUtils.EMPTY;

    private String versionId = StringUtils.EMPTY;

    private IProductCmpt contextProductCmpt = null;

    private IProductCmpt copyProductCmpt;

    private boolean singleTypeSelection = false;

    private final List<IProductCmptType> subtypes = new ArrayList<>();

    private final List<ProductCmptViewItem> templates = new ArrayList<>();

    private IProductCmptGeneration addToProductCmptGeneration;

    private IProductCmptTypeAssociation addToAssociation;

    private String runtimeId = StringUtils.EMPTY;

    private boolean template = false;

    public NewProductCmptPMO() {
        this(false);
    }

    /**
     * Creates a new {@link NewProductCmptPMO}.
     * 
     * @param template whether the new product component is a template
     */
    public NewProductCmptPMO(boolean template) {
        super();
        this.template = template;
        validator = new NewProductCmptValidator(this);
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        updateBaseTypeList(ipsProject);
        if (selectedBaseType != null
                && !selectedBaseType.equals(ipsProject.findProductCmptType(selectedBaseType.getQualifiedName()))) {
            selectedBaseType = null;
        }
        updateVersionId(ipsProject);
        updateRuntimeId(ipsProject);
        super.setIpsProject(ipsProject);
    }

    @Override
    public void setEffectiveDate(GregorianCalendar effectiveDate) {
        super.setEffectiveDate(effectiveDate);
        updateVersionId(getIpsProject());
    }

    private void updateVersionId(IIpsProject ipsProject) {
        if (ipsProject != null && getEffectiveDate() != null) {
            IProductCmptNamingStrategy namingStrategy = ipsProject.getProductCmptNamingStrategy();
            setVersionId(namingStrategy.getNextVersionId(contextProductCmpt, getEffectiveDate()));
        }
    }

    public boolean isNeedVersionId() {
        if (getIpsProject() == null) {
            return true;
        }
        IProductCmptNamingStrategy namingStrategy = getIpsProject().getProductCmptNamingStrategy();
        return namingStrategy.supportsVersionId();
    }

    /**
     * Searches all {@link IProductCmptType} in selected project and adding fills the list of base
     * types.
     * <p>
     * Every type that has either no super type or which super type is an layer supertype is added
     * as an base type.
     */
    void updateBaseTypeList(IIpsProject ipsProject) {
        if (ipsProject == null) {
            return;
        }
        baseTypes.clear();
        try {
            IIpsSrcFile[] findIpsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
            Set<IIpsSrcFile> concreteTypes = new HashSet<>();
            // 1. making a list containing all NOT ABSTRACT types if the new product component is
            // not a template
            for (IIpsSrcFile ipsSrcFile : findIpsSrcFiles) {
                if (isTemplate() || !Boolean.valueOf(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_ABSTRACT))) {
                    concreteTypes.add(ipsSrcFile);
                }
            }
            addBaseTypesRecursive(concreteTypes, ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void addBaseTypesRecursive(Set<IIpsSrcFile> types, IIpsProject ipsProject) throws CoreRuntimeException {
        Set<IIpsSrcFile> superTypes = new HashSet<>();
        for (IIpsSrcFile ipsSrcFile : types) {
            if (Boolean.parseBoolean(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE))) {
                continue;
            }
            String superType = ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_SUPERTYPE);
            IIpsSrcFile superTypeIpsSrcFile = null;
            if (StringUtils.isNotEmpty(superType)) {
                superTypeIpsSrcFile = ipsProject
                        .findIpsSrcFile(new QualifiedNameType(superType, IpsObjectType.PRODUCT_CMPT_TYPE));
            }
            if (superTypeIpsSrcFile != null && !Boolean
                    .parseBoolean(superTypeIpsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE))) {
                superTypes.add(superTypeIpsSrcFile);
            } else {
                baseTypes.add((IProductCmptType)ipsSrcFile.getIpsObject());
            }
        }
        if (!superTypes.isEmpty()) {
            addBaseTypesRecursive(superTypes, ipsProject);
        }
    }

    /**
     * @return Returns the baseTypes.
     */
    public Set<IProductCmptType> getBaseTypes() {
        return baseTypes;
    }

    /**
     * @return Returns the selectedBaseType.
     */
    public IProductCmptType getSelectedBaseType() {
        return selectedBaseType;
    }

    /**
     * @param selectedBaseType The selectedBaseType to set.
     */
    public void setSelectedBaseType(IProductCmptType selectedBaseType) {
        IProductCmptType oldSelection = this.selectedBaseType;
        this.selectedBaseType = selectedBaseType;
        updateTemplatesList();
        updateSubtypeList();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_BASE_TYPE, oldSelection, selectedBaseType));

    }

    /**
     * @param selectedType The selectedType to set.
     */
    public void setSelectedType(IProductCmptType selectedType) {
        IProductCmptType oldSelection = this.selectedType;
        this.selectedType = selectedType;
        updateTemplatesList();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_TYPE, oldSelection, selectedType));
    }

    /**
     * @return Returns the selectedType.
     */
    public IProductCmptType getSelectedType() {
        return selectedType;
    }

    public ProductCmptViewItem getSelectedTemplate() {
        return selectedTemplate;
    }

    public IProductCmpt getSelectedTemplateAsProductCmpt() {
        if (selectedTemplate == null) {
            return null;
        } else {
            return selectedTemplate.getProductCmpt();
        }
    }

    public void setSelectedTemplate(ProductCmptViewItem selectedTemplate) {
        IIpsSrcFileViewItem oldTemplate = this.selectedTemplate;
        this.selectedTemplate = selectedTemplate;
        updateSubtypeList();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_TEMPLATE, oldTemplate, selectedTemplate));
    }

    public List<IProductCmptType> getSubtypes() {
        return subtypes;
    }

    public void updateSubtypeList() {
        subtypes.clear();
        if (isSingleTypeSelection()) {
            subtypes.add(selectedType);
            return;
        }

        IProductCmptType referenceType;
        if (selectedTemplate != null && selectedTemplate.getProductCmpt() != null) {
            referenceType = selectedTemplate.getProductCmpt().findProductCmptType(getIpsProject());
        } else {
            referenceType = selectedBaseType;
        }
        if (referenceType != null) {
            List<IType> subtypesList = referenceType.findSubtypes(true, true, getIpsProject());
            for (IType type : subtypesList) {
                if (isTemplate() || !type.isAbstract()) {
                    subtypes.add((IProductCmptType)type);
                }
            }
            setDefaultSelection();
        }
    }

    /**
     * This method may set a type as selected if there is no valid selection. We only want to have a
     * default selection if there is either only one selectable type or if we do not have templates.
     * If there are template we do not want to select any type because this may reduce the list of
     * available templates.
     */
    private void setDefaultSelection() {
        if (selectedType == null || !subtypes.contains(selectedType)) {
            if (subtypes.size() == 1 || (!subtypes.isEmpty() && isShowDescription())) {
                setSelectedType(subtypes.get(0));
            } else {
                setSelectedType(null);
            }
        }
    }

    public void updateTemplatesList() {
        templates.clear();
        if (isSingleTypeSelection()) {
            return;
        }
        List<IIpsSrcFile> templateSrcFiles;
        Map<String, ProductCmptViewItem> viewItemNames = new LinkedHashMap<>();
        if (selectedBaseType != null && selectedType == null) {
            templateSrcFiles = getIpsProject().findAllProductTemplates(selectedBaseType, true);
        } else if (selectedType != null) {
            templateSrcFiles = getIpsProject().findCompatibleProductTemplates(selectedType);
        } else {
            templateSrcFiles = new ArrayList<>();
        }
        templates.add(NULL_TEMPLATE);
        for (IIpsSrcFile ipsSrcFile : templateSrcFiles) {
            ProductCmptViewItem viewItem = new ProductCmptViewItem(ipsSrcFile);
            viewItemNames.put(viewItem.getName(), viewItem);
        }
        for (ProductCmptViewItem cmptViewItem : viewItemNames.values()) {
            ProductCmptViewItem refTemplate = viewItemNames.get(cmptViewItem.getTemplateName());
            if (refTemplate != null) {
                refTemplate.addChild(cmptViewItem);
            } else {
                templates.add(cmptViewItem);
            }
        }
        if (selectedTemplate != NULL_TEMPLATE && !containsSelectedTemplate()) {
            setSelectedTemplate(NULL_TEMPLATE);
        }
    }

    private boolean containsSelectedTemplate() {
        for (ProductCmptViewItem viewItem : templates) {
            if (viewItem.contains(selectedTemplate)) {
                return true;
            }
        }
        return false;
    }

    public List<ProductCmptViewItem> getTemplates() {
        return templates;
    }

    public boolean isShowTemplates() {
        return !isShowDescription();
    }

    public boolean isShowDescription() {
        return templates.size() <= 1;
    }

    /**
     * @param name The kindId to set.
     */
    public void setKindId(String name) {
        String oldName = this.kindId;
        this.kindId = name;
        updateRuntimeId(getIpsProject());
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_KIND_ID, oldName, name));
    }

    /**
     * @return Returns the kindId.
     */
    public String getKindId() {
        return kindId;
    }

    /**
     * @param versionId The versionId to set.
     */
    public void setVersionId(String versionId) {
        String oldId = this.versionId;
        this.versionId = versionId;
        updateRuntimeId(getIpsProject());
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_VERSION_ID, oldId, versionId));
    }

    /**
     * @return Returns the versionId.
     */
    public String getVersionId() {
        return versionId;
    }

    private void updateRuntimeId(IIpsProject ipsProject) {
        try {
            if (ipsProject != null) {
                setRuntimeId(ipsProject.getProductCmptNamingStrategy().getUniqueRuntimeId(ipsProject, getName()));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        } catch (IllegalArgumentException e) {
            setRuntimeId(StringUtils.EMPTY);
        }
    }

    @Override
    public String getName() {
        if (StringUtils.isEmpty(kindId)) {
            return StringUtils.EMPTY;
        } else {
            return getIpsProject().getProductCmptNamingStrategy().getProductCmptName(kindId, versionId);
        }
    }

    /**
     * Setting the defaults for the new product component wizard.
     * <p>
     * The default package set the project, the package root and the package fragment. The default
     * package should not be null, if it is null, the method does nothing. The default type may be
     * null, if not null it is used to specify the base type as well as the current selected type,
     * if it is not abstract. The default product component may also be null. It does not change the
     * default type but is used to fill default kind id and version id.
     * 
     * @param defaultPackage Used for default project, package root and package fragment, should not
     *            be null.
     * @param defaultType The type to initialize the selected base type and selected concrete type
     * @param defaultProductCmpt a product component to initialize the version id and kind id - does
     *            not set the default type!
     */
    public void initDefaults(IIpsPackageFragment defaultPackage,
            IProductCmptType defaultType,
            IProductCmpt defaultProductCmpt) {
        if (defaultPackage == null) {
            return;
        }
        if (contextProductCmpt != null) {
            contextProductCmpt = defaultProductCmpt;
            setKindId(contextProductCmpt.getKindId().getName());
        }
        setIpsProject(defaultPackage.getIpsProject());
        setPackageRoot(defaultPackage.getRoot());
        setIpsPackage(defaultPackage);
        if (defaultType != null) {
            initDefaultType(defaultType, defaultPackage.getIpsProject());
        }
    }

    private void initDefaultType(IProductCmptType cmptType, IIpsProject ipsProject) {
        SelectedBaseTypeVisitor selectedBaseTypeVisitor = new SelectedBaseTypeVisitor(getBaseTypes(), ipsProject);
        selectedBaseTypeVisitor.start(cmptType);
        setSelectedBaseType(selectedBaseTypeVisitor.selectedBaseType);
        setSelectedType(cmptType);
    }

    /**
     * @return Returns the validator.
     */
    @Override
    protected NewProductCmptValidator getValidator() {
        return validator;
    }

    /**
     * Sets a {@link IProductCmptGeneration} and a {@link IProductCmptTypeAssociation} to which the
     * newly created product component will be added.
     * <p>
     * The type of the new product component have to be compatible to the target type of the
     * {@link #getAddToAssociation()}
     * <p>
     * This method overwrites the selected base type with the target type of the given association.
     * It uses exactly the target type of the association also it may not be in the list of
     * available base type. With this behavior the list of selectable concrete types contains
     * exactly the types that could be selected for this association. If the target type of the
     * association is not abstract it is also used as default selected type.
     * 
     * @param addToProductCmptGeneration The product component you want to add the newly created
     *            product component to
     * @param addToAssociation The association in which context the newly created product component
     *            is added to the {@link #addToProductCmptGeneration}
     */
    public void setAddToAssociation(IProductCmptGeneration addToProductCmptGeneration,
            IProductCmptTypeAssociation addToAssociation) {
        this.addToProductCmptGeneration = null;
        this.addToAssociation = null;
        if (addToProductCmptGeneration == null || addToAssociation == null) {
            return;
        }
        IProductCmptType targetProductCmptType = addToAssociation
                .findTargetProductCmptType(addToProductCmptGeneration.getIpsProject());
        this.addToProductCmptGeneration = addToProductCmptGeneration;
        this.addToAssociation = addToAssociation;
        setEffectiveDate(addToProductCmptGeneration.getValidFrom());
        setSelectedBaseType(targetProductCmptType);
        if (targetProductCmptType != null && !targetProductCmptType.isAbstract()) {
            setSelectedType(targetProductCmptType);
        }
    }

    /**
     * Configures this PMO so that the product component to create is a copy of the given product
     * component.
     * 
     * @throws IllegalStateException if no IPS project has been set using
     *             {@link #setIpsProject(IIpsProject)}
     */
    public void setCopyProductCmpt(IProductCmpt productCmptToCopy) {
        checkIpsProject();
        copyProductCmpt = productCmptToCopy;

        initializeNameForProductCmptCopy();
        initializeTypeAndBaseTypeForProductCmptCopy(productCmptToCopy);
        initializeTemplateForProductCmptCopy(productCmptToCopy);
    }

    private void checkIpsProject() {
        if (getIpsProject() == null) {
            throw new IllegalStateException("IPS Project must be set before setting the product to be copied."); //$NON-NLS-1$
        }
    }

    private void initializeNameForProductCmptCopy() {
        setKindId(copyProductCmpt.getKindId().getName());
    }

    private void initializeTypeAndBaseTypeForProductCmptCopy(IProductCmpt productCmptToCopy) {
        IProductCmptType productCmptType = productCmptToCopy.findProductCmptType(productCmptToCopy.getIpsProject());
        setSingleProductCmptType(productCmptType);
    }

    private void initializeTemplateForProductCmptCopy(IProductCmpt productCmptToCopy) {
        IProductCmpt templateOfCopy = productCmptToCopy.findTemplate(productCmptToCopy.getIpsProject());
        if (templateOfCopy != null) {
            ProductCmptViewItem templateViewItem = new ProductCmptViewItem(templateOfCopy.getIpsSrcFile());
            setSelectedTemplate(templateViewItem);
        }
    }

    public void setSingleProductCmptType(IProductCmptType productCmptType) {
        checkIpsProject();
        singleTypeSelection = productCmptType != null;
        setSelectedType(productCmptType);
        SelectedBaseTypeVisitor baseTypeVisitor = new SelectedBaseTypeVisitor(getBaseTypes(), getIpsProject());
        baseTypeVisitor.start(productCmptType);
        setSelectedBaseType(baseTypeVisitor.selectedBaseType);
    }

    /**
     * Returns the product component generation to which the newly created product component will be
     * added
     * 
     * @see #setAddToAssociation(IProductCmptGeneration, IProductCmptTypeAssociation)
     */
    public IProductCmptGeneration getAddToProductCmptGeneration() {
        return addToProductCmptGeneration;
    }

    /**
     * Returns the association to which the newly created product component will be added
     * 
     * @see #setAddToAssociation(IProductCmptGeneration, IProductCmptTypeAssociation)
     */
    public IProductCmptTypeAssociation getAddToAssociation() {
        return addToAssociation;
    }

    public boolean isAddToMode() {
        return getAddToAssociation() != null && getAddToProductCmptGeneration() != null;
    }

    /**
     * Returns whether the wizard is in copy mode or not.
     * <p>
     * When in copy mode, the product component provided via
     * {@link #setCopyProductCmpt(IProductCmpt)} is being copied.
     */
    public boolean isCopyMode() {
        return copyProductCmpt != null;
    }

    /**
     * Returns true if the wizard is
     * <ul>
     * <li>in copy mode and</li>
     * <li>the product component type of the product component to be copied can be found</li>
     * </ul>
     */
    public boolean isSingleTypeSelection() {
        return singleTypeSelection;
    }

    /**
     * Returns whether the first wizard page (base type selection) is needed or not.
     * <p>
     * The page is not needed if
     * <ul>
     * <li>{@link #isAddToMode()} returns {@code true} or</li>
     * <li>{@link #isCopyMode()} returns {@code true} and the product component type of the
     * product</li>
     * </ul>
     * component being copied can be found
     */
    public boolean isFirstPageNeeded() {
        return !isAddToMode() && !isSingleTypeSelection();
    }

    /**
     * @param runtimeId The runtimeId to set.
     */
    public void setRuntimeId(String runtimeId) {
        String oldRuntimeId = this.runtimeId;
        this.runtimeId = runtimeId;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_RUNTIME_ID, oldRuntimeId, runtimeId));
    }

    /**
     * @return Returns the runtimeId.
     */
    public String getRuntimeId() {
        return runtimeId;
    }

    public boolean hasRuntimeId() {
        return !isTemplate();
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        boolean old = this.template;
        this.template = template;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEMPLATE, old, this.template));
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        if (template) {
            return IpsObjectType.PRODUCT_TEMPLATE;
        } else {
            return IpsObjectType.PRODUCT_CMPT;
        }
    }

    /**
     * Returns the product component being copied by this wizard or {@code null} if this wizard is
     * used to create a new product component.
     */
    public IProductCmpt getCopyProductCmpt() {
        return copyProductCmpt;
    }

    private static final class BaseTypeComparator implements Comparator<IProductCmptType>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(IProductCmptType o1, IProductCmptType o2) {
            IMultiLanguageSupport multiLanguageSupport = IIpsModel.get().getMultiLanguageSupport();
            if (multiLanguageSupport != null) {
                String label1 = multiLanguageSupport.getLocalizedLabel(o1);
                String label2 = multiLanguageSupport.getLocalizedLabel(o2);
                return label1.compareTo(label2);
            } else {
                int namesCompared = o1.getName().compareTo(o2.getName());
                if (namesCompared == 0 && !o1.equals(o2)) {
                    // different objects with the same name, order doesn't matter but we need both
                    return 1;
                } else {
                    return namesCompared;
                }
            }
        }
    }

    /**
     * Searches the type hierarchy and looks for a supertype that is part of the base type list.
     * 
     * @author dirmeier
     */
    private static class SelectedBaseTypeVisitor extends TypeHierarchyVisitor<IProductCmptType> {

        private final Set<IProductCmptType> baseTypes;

        private IProductCmptType selectedBaseType = null;

        public SelectedBaseTypeVisitor(Set<IProductCmptType> baseTypes, IIpsProject ipsProject) {
            super(ipsProject);
            this.baseTypes = baseTypes;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            if (baseTypes.contains(currentType)) {
                selectedBaseType = currentType;
                return false;
            } else {
                return true;
            }
        }

    }

}
