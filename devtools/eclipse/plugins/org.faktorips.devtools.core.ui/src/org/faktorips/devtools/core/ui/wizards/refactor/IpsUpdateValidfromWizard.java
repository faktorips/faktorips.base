/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * A wizard that guides the user through the process of updating the "Valid From" date and
 * optionally the generation ID for a product component and its structure.
 */
public class IpsUpdateValidfromWizard extends ResizableWizard {

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 800;

    private static final String SECTION_NAME = "UpdateValidfromWizard"; //$NON-NLS-1$
    private static final String IMAGE_PATH = "icons/wizards/UpdateValidFromWizard.png"; //$NON-NLS-1$
    private static final String PLUGIN_ID = "org.faktorips.devtools.core.ui"; //$NON-NLS-1$

    private final UpdateValidfromPresentationModel presentationModel;

    private String oldValidFromDate;
    private DateFormat dateFormat;

    private AsyncExecutor asyncExecutor = runnable -> Display.getDefault().asyncExec(runnable);

    /**
     * Creates a new wizard instance for updating the Valid-From date and optionally the generation
     * ID.
     *
     * @param product the product component to update
     */
    public IpsUpdateValidfromWizard(IProductCmpt product) {
        super(SECTION_NAME, IpsPlugin.getDefault().getDialogSettings(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        presentationModel = new UpdateValidfromPresentationModel(product);
        setDefaultPageImageDescriptor(IpsPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH));
        initializeDefaults();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        oldValidFromDate = dateFormat.format(product.getValidFrom().getTime());

    }

    public void setAsyncExecutor(AsyncExecutor executor) {
        asyncExecutor = executor;
    }

    @Override
    public void addPages() {
        addPage(new UpdateValidFromSourcePage(UpdateValidFromSourcePage.PAGE_ID));
        addPage(new UpdateValidFromPreviewPage(UpdateValidFromPreviewPage.PAGE_ID));
    }

    private void initializeDefaults() {
        presentationModel.setNewValidFrom(IpsUIPlugin.getDefault().getDefaultValidityDate());
    }

    public UpdateValidfromPresentationModel getPresentationModel() {
        return presentationModel;
    }

    public IProductCmptTreeStructure getStructure() {
        return presentationModel.getStructure();
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public String getOldValidFrom() {
        return oldValidFromDate;
    }

    @Override
    public boolean performFinish() {

        var model = getPresentationModel();

        Set<IProductCmptStructureReference> selectedItems = model.getTreeStatus().getAllEnabledElements(
                CopyOrLink.COPY, getStructure(), false);

        boolean shouldChangeId = model.isChangeGenerationId();

        String newVersionId = model.getNewVersionId();
        GregorianCalendar newValidFrom = model.getNewValidFrom();

        runValidFromUpdateOperation(selectedItems, newValidFrom);
        if (shouldChangeId && StringUtils.isNotBlank(newVersionId)) {
            applyNewGenerationIdsAsync(selectedItems, newVersionId);
        }

        return true;
    }

    private void runValidFromUpdateOperation(Set<IProductCmptStructureReference> selectedItems,
            GregorianCalendar newValidFrom) {

        AWorkspace workspace = getIpsProject().getCorrespondingResource().getWorkspace();
        IWorkspaceRoot schedulingRule = workspace.getRoot().unwrap();

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation(schedulingRule) {
            @Override
            protected void execute(IProgressMonitor monitor) throws IpsException {
                runValidFromUpdateOperation(selectedItems, newValidFrom, monitor);
            }

        };

        try {

            if (getContainer() != null) {
                getContainer().run(true, true, operation);
            } else {
                // only for testing
                operation.run(new NullProgressMonitor());
            }
        } catch (InvocationTargetException | InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private void runValidFromUpdateOperation(Set<IProductCmptStructureReference> selectedItems,
            GregorianCalendar newValidFrom,
            IProgressMonitor monitor) throws IpsException {

        for (IProductCmptStructureReference ref : selectedItems) {
            IIpsObject object = ref.getWrappedIpsObject();
            if (object instanceof IProductCmpt productCmpt) {
                productCmpt.getValidFrom();
                updateValidFromOperation(newValidFrom, monitor, productCmpt);
                if (getPresentationModel().isChangeAttributes()) {
                    updateProductAttributesOperation(newValidFrom, productCmpt);
                    IPolicyCmptType policyCmptType = productCmpt.findPolicyCmptType(getIpsProject());
                    if (policyCmptType != null) {
                        updatePolicyAttributes(newValidFrom, policyCmptType, productCmpt);
                    }
                }
                productCmpt.getIpsSrcFile().save(monitor);
            }
        }
    }

    /**
     * Updates policy component attributes at both product and its adjustments.
     */
    private void updatePolicyAttributes(GregorianCalendar newValidFrom,
            IPolicyCmptType policy,
            IProductCmpt productCmpt) {
        String newValidFromDate = dateFormat.format(newValidFrom.getTime());

        for (IPolicyCmptTypeAttribute attribute : policy.getPolicyCmptTypeAttributes()) {
            updatePolicyAttribute(attribute, productCmpt, newValidFromDate);
        }

        if (productCmpt.allowGenerations()) {
            for (IIpsObjectGeneration generation : productCmpt.getGenerations()) {
                for (IPolicyCmptTypeAttribute attribute : policy.getPolicyCmptTypeAttributes()) {
                    updatePolicyAttribute(attribute, (ProductCmptGeneration)generation, newValidFromDate);
                }
            }
        }
    }

    private void updatePolicyAttribute(IPolicyCmptTypeAttribute attribute,
            IProductCmpt product,
            String newValidFromDate) {
        handlePolicyAttribute(attribute, product, newValidFromDate);
    }

    private void updatePolicyAttribute(IPolicyCmptTypeAttribute attribute,
            ProductCmptGeneration product,
            String newValidFromDate) {
        handlePolicyAttribute(attribute, product, newValidFromDate);
    }

    private void handlePolicyAttribute(IPolicyCmptTypeAttribute attribute,
            Object product,
            String newValidFromDate) {

        String datatype = attribute.getDatatype();
        IConfiguredValueSet valueSet = getConfiguredValueSet(product, attribute);
        IConfiguredDefault defaultValue = getConfiguredDefault(product, attribute);

        if (valueSet == null) {
            return;
        }

        ValueSetType valueSetType = valueSet.getValueSet().getValueSetType();

        if (valueSetType == ValueSetType.ENUM) {
            handleEnumValueSet(attribute, valueSet, defaultValue, datatype, newValidFromDate);
        } else if (valueSetType == ValueSetType.UNRESTRICTED) {
            handleUnrestrictedValueSet(defaultValue, datatype, newValidFromDate);
        }
    }

    private void handleEnumValueSet(IPolicyCmptTypeAttribute attribute,
            IConfiguredValueSet valueSet,
            IConfiguredDefault defaultValue,
            String datatype,
            String newValidFromDate) {

        EnumValueSet configured = (EnumValueSet)valueSet.getValueSet();

        if (attribute.getValueSet() instanceof EnumValueSet model) {
            handleEnumWithModel(model, configured, defaultValue, datatype, newValidFromDate);
        } else {
            handleEnumWithoutModel(configured, defaultValue, datatype, newValidFromDate);
        }
    }

    private void handleEnumWithModel(EnumValueSet model,
            EnumValueSet configured,
            IConfiguredDefault defaultValue,
            String datatype,
            String newValidFromDate) {

        if ("LocalDateTime".equals(datatype)) {
            updateEnumLocalDateTime(model, configured, defaultValue, newValidFromDate);
        } else if ("LocalDate".equals(datatype) || "GregorianCalendar".equals(datatype)) {
            updateEnumLocalDate(model, configured, defaultValue, newValidFromDate);
        }
    }

    private void handleEnumWithoutModel(EnumValueSet configured,
            IConfiguredDefault defaultValue,
            String datatype,
            String newValidFromDate) {

        boolean isLocalDateTime = "LocalDateTime".equals(datatype);
        boolean isDate = "LocalDate".equals(datatype) || "GregorianCalendar".equals(datatype);

        if (!isLocalDateTime && !isDate) {
            return;
        }

        for (int i = 0; i < configured.getValues().length; i++) {
            String current = configured.getValue(i);

            if (matches(current, datatype, oldValidFromDate)) {
                String updated = updateValue(current, newValidFromDate, datatype);
                configured.setValue(i, updated);
                updateDefaultValueIfNeeded(defaultValue, datatype, updated);
            }
        }
    }

    private void updateDefaultValueIfNeeded(IConfiguredDefault defaultValue,
            String datatype,
            String updated) {

        if (defaultValue == null) {
            return;
        }

        if (matches(defaultValue.getValue(), datatype, oldValidFromDate)) {
            defaultValue.setValue(updated);
        }
    }

    private void handleUnrestrictedValueSet(IConfiguredDefault defaultValue,
            String datatype,
            String newValidFromDate) {

        if (defaultValue == null) {
            return;
        }

        String currentValue = defaultValue.getValue();

        switch (datatype) {
            case "LocalDate", "GregorianCalendar" -> {
                if (currentValue.equals(oldValidFromDate)) {
                    defaultValue.setValue(newValidFromDate);
                }
            }
            case "LocalDateTime" -> {
                if (extractDatePart(currentValue).equals(oldValidFromDate)) {
                    String updatedValue = updateLocalDateTimeAttribute(currentValue, newValidFromDate);
                    defaultValue.setValue(updatedValue);
                }
            }
        }
    }

    private void updateEnumLocalDate(EnumValueSet model,
            EnumValueSet configured,
            IConfiguredDefault defaultValue,
            String newDate) {

        if (!model.containsValue(newDate, getIpsProject())) {
            return;
        }

        for (int i = 0; i < configured.getValues().length; i++) {
            if (StringUtils.equals(configured.getValue(i), oldValidFromDate)) {
                configured.setValue(i, newDate);
                if (defaultValue != null) {
                    defaultValue.setValue(newDate);
                }
            }
        }
    }

    private void updateEnumLocalDateTime(EnumValueSet model,
            EnumValueSet configured,
            IConfiguredDefault defaultValue,
            String newDate) {
        model.getValuesAsList().stream()
                .filter(v -> extractDatePart(v).equals(newDate))
                .findFirst()
                .ifPresent(v -> {
                    for (int i = 0; i < configured.getValues().length; i++) {
                        String old = configured.getValue(i);
                        if (extractDatePart(old).equals(oldValidFromDate)) {
                            String updated = updateLocalDateTimeAttribute(old, newDate);
                            configured.setValue(i, updated);
                            if (defaultValue != null) {
                                defaultValue.setValue(updated);
                            }
                        }
                    }
                });
    }

    /**
     * Updates attribute values of type LocalDate or LocalDateTime.
     */
    private void updateProductAttributesOperation(GregorianCalendar newValidFrom, IProductCmpt productCmpt) {
        updateProductAttributes(newValidFrom, productCmpt);
        if (productCmpt.allowGenerations()) {
            productCmpt.getGenerations()
                    .forEach(gen -> updateProductGenerationAttributes(newValidFrom, (ProductCmptGeneration)gen));
        }
    }

    private void updateProductAttributes(GregorianCalendar newValidFrom, IProductCmpt productCmpt) {
        productCmpt.getAttributeValues().forEach(attributeValue -> {
            IProductCmptTypeAttribute attribute = attributeValue.findAttribute(getIpsProject());
            update(newValidFrom, attributeValue, attribute);
        });
    }

    private void updateProductGenerationAttributes(GregorianCalendar newValidFrom, ProductCmptGeneration productGen) {
        Arrays.asList(productGen.getAttributeValues()).forEach(attributeValue -> {
            IProductCmptTypeAttribute attribute = attributeValue.findAttribute(getIpsProject());
            update(newValidFrom, attributeValue, attribute);
        });
    }

    private boolean isDateType(String type) {
        return "LocalDate".equals(type) || "LocalDateTime".equals(type) || "GregorianCalendar".equals(type);
    }

    private boolean matches(String value, String dataType, String expectedDate) {
        if ("LocalDateTime".equals(dataType)) {
            return extractDatePart(value).equals(expectedDate);
        }
        return value.equals(expectedDate);
    }

    private String updateValue(String original, String newDate, String dataType) {
        if ("LocalDateTime".equals(dataType)) {
            return updateLocalDateTimeAttribute(original, newDate);
        }
        return newDate;
    }

    private void handleProductAttributeUnrestricted(IProductCmptTypeAttribute attribute,
            IAttributeValue attributeValue,
            String dataType,
            GregorianCalendar newValidFrom,
            String newValidFromDate) {
        if (isDateType(dataType)) {
            try {
                if (!attribute.isMultiValueAttribute()) {
                    updateSingleValue(attributeValue, newValidFromDate);
                } else {
                    updateMultiValue(newValidFrom, attributeValue, dataType);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleProductAttributeEnum(IProductCmptTypeAttribute attribute,
            IAttributeValue attributeValue,
            EnumValueSet valueSet,
            String dataType,
            GregorianCalendar newValidFrom,
            String newValidFromDate) {

        if (!attribute.isMultiValueAttribute()) {
            updateEnumSingleAttribute(attributeValue, valueSet, dataType, newValidFromDate);
        } else {
            updateEnumMultiAttribute(attributeValue, valueSet, dataType, newValidFrom, newValidFromDate);
        }
    }

    private boolean containsDate(EnumValueSet valueSet, String dataType, String targetDate) {
        return Arrays.stream(valueSet.getValues())
                .anyMatch(v -> matches(v, dataType, targetDate));
    }

    private void updateEnumSingleAttribute(
            IAttributeValue attributeValue,
            EnumValueSet valueSet,
            String dataType,
            String newValidFromDate) {

        if (containsDate(valueSet, dataType, newValidFromDate)) {
            String actualValue = attributeValue.getValueHolder().getStringValue();
            if (matches(actualValue, dataType, oldValidFromDate)) {
                String updated = updateValue(actualValue, newValidFromDate, dataType);
                attributeValue.setValueHolder(new SingleValueHolder(attributeValue, updated));
            }
        }

    }

    private void updateEnumMultiAttribute(
            IAttributeValue attributeValue,
            EnumValueSet valueSet,
            String dataType,
            GregorianCalendar newValidFrom,
            String newValidFromDate) {

        if (containsDate(valueSet, dataType, newValidFromDate)) {
            updateMultiValue(newValidFrom, attributeValue, dataType);
        }

    }

    /**
     * Update an attribute that uses the old valid-from date.
     */
    private void update(GregorianCalendar newValidFrom,
            IAttributeValue attributeValue,
            IProductCmptTypeAttribute attribute) {
        String dataType = attribute.getDatatype();
        String newValidFromDate = getDateFormat().format(newValidFrom.getTime());

        ValueSetType valueSetType = attribute.getValueSet().getValueSetType();

        if (valueSetType == ValueSetType.UNRESTRICTED) {
            handleProductAttributeUnrestricted(attribute, attributeValue, dataType, newValidFrom, newValidFromDate);
        } else if (valueSetType == ValueSetType.ENUM) {
            EnumValueSet valueSet = (EnumValueSet)attribute.getValueSet();
            handleProductAttributeEnum(attribute, attributeValue, valueSet, dataType, newValidFrom, newValidFromDate);
        }
    }

    /**
     * Extracts the date portion from a string value that may include a time component.
     *
     * @param value the string to extract from, e.g. "2025-04-10T12:30:00"
     * @return the date-only part, e.g. "2025-04-10"
     */
    private String extractDatePart(String value) {
        if (value == null) {
            return "";
        }
        int tIndex = value.indexOf('T');
        return (tIndex > 0) ? value.substring(0, tIndex) : value;
    }

    /**
     * Updates the valid-from date of the given product component if it's different from the new
     * one.
     */
    private void updateValidFromOperation(GregorianCalendar newValidFrom,
            IProgressMonitor monitor,
            IProductCmpt productCmpt) {
        if (!Objects.equals(productCmpt.getValidFrom(), newValidFrom)) {
            productCmpt.setValidFrom(newValidFrom);
            productCmpt.getIpsSrcFile().save(monitor);
        }
    }

    /**
     * Updates a single-valued attribute if the current date is before the new valid-from.
     */
    private void updateSingleValue(IAttributeValue attributeValue, String newValidFromValue) throws ParseException {
        String currentValue = attributeValue.getValueHolder().getStringValue();
        String datePart = extractDatePart(currentValue);
        Date currentDate = getDateFormat().parse(datePart);

        if (currentDate.equals(getDateFormat().parse(getOldValidFrom()))) {
            String newValue = updateLocalDateTimeAttribute(currentValue, newValidFromValue);
            attributeValue.setValueHolder(new SingleValueHolder(attributeValue, newValue));
        }
    }

    /**
     * Preserves the time suffix (if any) when updating the date portion of a value.
     *
     * @param original the original date string, possibly with time (e.g. "2024-12-10T13:00:00")
     * @param newDate the new date string to apply (e.g. "2025-01-01")
     * @return the combined result with the original time preserved if present
     */
    private String updateLocalDateTimeAttribute(String original, String newDate) {
        int tIndex = original.indexOf('T');
        return (tIndex > 0) ? newDate + original.substring(tIndex) : newDate;
    }

    /**
     * Filters values that are after the new valid-from and applies them to the multi-value
     * attribute.
     */
    private void updateMultiValue(GregorianCalendar newValidFrom,
            IAttributeValue attributeValue,
            String dataType) {
        List<ISingleValueHolder> currentValues = attributeValue.getValueHolder().getValueList().stream()
                .map(v -> new SingleValueHolder(attributeValue, v.getContentAsString()))
                .collect(Collectors.toCollection(ArrayList::new));

        String newValidFromDate = getDateFormat().format(newValidFrom.getTime());

        ISingleValueHolder match = currentValues.stream()
                .filter(v -> extractDatePart(v.getStringValue()).equals(getOldValidFrom()))
                .findFirst()
                .orElse(null);

        if (match == null) {
            return;
        }

        String replacement;
        if ("LocalDateTime".equals(dataType)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            replacement = LocalDateTime.of(LocalDate.parse(newValidFromDate), LocalTime.now().withNano(0))
                    .format(formatter);
        } else {
            replacement = newValidFromDate;
        }

        currentValues.remove(match);
        currentValues.add(new SingleValueHolder(attributeValue, replacement));

        MultiValueHolder newHolder = new MultiValueHolder(attributeValue);
        newHolder.setValue(currentValues);
        attributeValue.setValueHolder(newHolder);
    }

    /**
     * Initiates asynchronous generation ID updates after the valid-from update.
     */
    private void applyNewGenerationIdsAsync(Set<IProductCmptStructureReference> selectedItems,
            String newVersionId) {

        asyncExecutor.execute(() -> {
            IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
            if (namingStrategy == null || !namingStrategy.supportsVersionId()) {
                return;
            }

            for (IProductCmptStructureReference ref : selectedItems) {
                IIpsObject object = ref.getWrappedIpsObject();
                if (object instanceof IProductCmpt product) {
                    renameIfNecessary(product, newVersionId, namingStrategy);
                } else if (object instanceof TableContents table) {
                    renameIfNecessary(table, newVersionId, namingStrategy);
                }
            }
        });
    }

    /**
     * Renames the given object if its new version ID name is different.
     */
    private void renameIfNecessary(IIpsObject ipsObject,
            String newVersionId,
            IProductCmptNamingStrategy namingStrategy) {

        String currentName = ipsObject.getName();
        String kindId = namingStrategy.getKindId(currentName);
        String newName = namingStrategy.getProductCmptName(kindId, newVersionId);

        if (!newName.equals(currentName)) {
            performRenameRefactoring(ipsObject, newName);
        }
    }

    protected void performRenameRefactoring(IIpsObjectPartContainer target, String newName) {
        IIpsRefactoring refactoring = IpsPlugin.getIpsRefactoringFactory()
                .createRenameRefactoring(target, newName, null, true);
        new IpsRefactoringOperation(refactoring, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell())
                .runDirectExecution();
    }

    private IConfiguredValueSet getConfiguredValueSet(Object container, IPolicyCmptTypeAttribute attribute) {
        if (container instanceof IProductCmpt pc) {
            return pc.getPropertyValue(attribute, IConfiguredValueSet.class);
        } else if (container instanceof ProductCmptGeneration gen) {
            return gen.getPropertyValue(attribute, IConfiguredValueSet.class);
        }
        return null;
    }

    private IConfiguredDefault getConfiguredDefault(Object container, IPolicyCmptTypeAttribute attribute) {
        if (container instanceof IProductCmpt pc) {
            return pc.getPropertyValue(attribute, IConfiguredDefault.class);
        } else if (container instanceof ProductCmptGeneration gen) {
            return gen.getPropertyValue(attribute, IConfiguredDefault.class);
        }
        return null;
    }

    private IIpsProject getIpsProject() {
        return getPresentationModel().getIpsProject();
    }

    private IProductCmptNamingStrategy getNamingStrategy() {
        return getIpsProject().getProductCmptNamingStrategy();
    }

    @FunctionalInterface
    public interface AsyncExecutor {
        void execute(Runnable runnable);
    }

}
