/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyAccess;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;

public abstract class AbstractIpsObjectPartsContainerTablePageElement<T extends IIpsObjectPartContainer> extends
        AbstractStandardTablePageElement {

    private final List<? extends T> objectParts;

    public AbstractIpsObjectPartsContainerTablePageElement(List<? extends T> objectParts,
            DocumentationContext context) {
        super(context);
        this.objectParts = objectParts;
    }

    @Override
    protected final void addDataRows() {
        for (T rowData : getObjectParts()) {
            addRow(rowData);

        }
    }

    @Override
    protected final List<String> getHeadline() {
        List<String> headline = new ArrayList<>();

        headline.addAll(getHeadlineWithIpsObjectPart());
        headline.addAll(getVersionHeadline());
        headline.addAll(getHeadlineWithExtentionPropertiesData());

        return headline;
    }

    protected abstract List<String> getHeadlineWithIpsObjectPart();

    private List<String> getVersionHeadline() {
        ArrayList<String> result = new ArrayList<>();
        if (getObjectParts().get(0) instanceof IVersionControlledElement) {
            result.add(getContext().getMessage(HtmlExportMessages.TablePageElement_headlineSince));
        }
        return result;
    }

    private List<String> getHeadlineWithExtentionPropertiesData() {

        IExtensionPropertyDefinition[] propertyDefinitions = getPropertyDefinitions();

        if (ArrayUtils.isEmpty(propertyDefinitions)) {
            return Collections.emptyList();
        }

        List<String> extensionPropertyNames = new ArrayList<>();
        for (IExtensionPropertyDefinition property : propertyDefinitions) {
            extensionPropertyNames.add(property.getName());
        }
        return extensionPropertyNames;
    }

    protected final void addRow(T rowData) {
        List<IPageElement> values = getRow(rowData);
        addSubElement(new TableRowPageElement(values.toArray(new IPageElement[values.size()]), getContext()));
    }

    protected List<IPageElement> getRow(T rowData) {
        List<IPageElement> values = new ArrayList<>();

        values.addAll(createRowWithIpsObjectPart(rowData));
        values.addAll(createRowWithVersion(rowData));
        values.addAll(createRowWithExtentionPropertiesData(rowData));
        return values;
    }

    protected abstract List<IPageElement> createRowWithIpsObjectPart(T rowData);

    private List<IPageElement> createRowWithVersion(T rowData) {
        List<IPageElement> values = new ArrayList<>();
        if (rowData instanceof IVersionControlledElement) {
            IVersionControlledElement versionControlledElement = (IVersionControlledElement)rowData;
            IVersion<?> sinceVersion = versionControlledElement.getSinceVersion();
            if (sinceVersion != null) {
                values.add(new TextPageElement(versionControlledElement.getSinceVersion().asString(), getContext()));
            } else {
                values.add(new TextPageElement(StringUtils.EMPTY, getContext()));
            }
        }
        return values;
    }

    private List<IPageElement> createRowWithExtentionPropertiesData(T rowData) {
        List<IPageElement> extensionPropertyValues = new ArrayList<>();
        for (IExtensionPropertyDefinition property : getPropertyDefinitions()) {
            Object value = rowData.getExtPropertyValue(property.getPropertyId());
            extensionPropertyValues.add(new TextPageElement(value == null ? null : value.toString(), getContext()));
        }
        return extensionPropertyValues;
    }

    protected IExtensionPropertyDefinition[] getPropertyDefinitions() {
        if (isEmpty()) {
            return new IExtensionPropertyDefinition[0];
        }

        IIpsElement rowData = getObjectParts().get(0);
        if (rowData instanceof IExtensionPropertyAccess) {
            Collection<IExtensionPropertyDefinition> extensionPropertyDefinitions = ((IExtensionPropertyAccess)rowData)
                    .getExtensionPropertyDefinitions();
            return extensionPropertyDefinitions.toArray(new IExtensionPropertyDefinition[extensionPropertyDefinitions
                    .size()]);
        }
        return new IExtensionPropertyDefinition[0];
    }

    @Override
    public boolean isEmpty() {
        return getObjectParts().isEmpty();
    }

    protected List<? extends T> getObjectParts() {
        return objectParts;
    }

}
