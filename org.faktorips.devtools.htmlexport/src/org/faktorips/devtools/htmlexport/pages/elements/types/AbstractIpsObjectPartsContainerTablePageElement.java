/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public abstract class AbstractIpsObjectPartsContainerTablePageElement<T extends IIpsObjectPartContainer> extends
        AbstractStandardTablePageElement {

    private DocumentationContext context;
    private final List<? extends T> objectParts;

    public AbstractIpsObjectPartsContainerTablePageElement(List<? extends T> objectParts, DocumentationContext context) {
        super();
        this.objectParts = objectParts;
        this.setContext(context);
    }

    @Override
    protected final void addDataRows() {
        for (T rowData : getObjectParts()) {
            addRow(rowData);

        }
    }

    @Override
    protected final List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.addAll(getHeadlineWithIpsObjectPart());
        headline.addAll(getHeadlineWithExtentionPropertiesData());

        return headline;
    }

    protected final void addRow(T rowData) {
        List<IPageElement> values = new ArrayList<IPageElement>();

        values.addAll(createRowWithIpsObjectPart(rowData));
        values.addAll(createRowWithExtentionPropertiesData(rowData));

        addSubElement(new TableRowPageElement(values.toArray(new IPageElement[values.size()])));
    }

    private List<String> getHeadlineWithExtentionPropertiesData() {

        IExtensionPropertyDefinition[] propertyDefinitions = getPropertyDefinitions();

        if (ArrayUtils.isEmpty(propertyDefinitions)) {
            return Collections.emptyList();
        }

        List<String> extensionPropertyNames = new ArrayList<String>();
        for (IExtensionPropertyDefinition property : propertyDefinitions) {
            extensionPropertyNames.add(property.getName());
        }
        return extensionPropertyNames;
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

    private List<IPageElement> createRowWithExtentionPropertiesData(T rowData) {
        List<IPageElement> extensionPropertyValues = new ArrayList<IPageElement>();
        for (IExtensionPropertyDefinition property : getPropertyDefinitions()) {
            Object value = rowData.getExtPropertyValue(property.getPropertyId());
            extensionPropertyValues.add(new TextPageElement(value == null ? null : value.toString()));
        }
        return extensionPropertyValues;
    }

    protected abstract List<IPageElement> createRowWithIpsObjectPart(T rowData);

    protected abstract List<String> getHeadlineWithIpsObjectPart();

    @Override
    public boolean isEmpty() {
        return getObjectParts().isEmpty();
    }

    public void setContext(DocumentationContext context) {
        this.context = context;
    }

    public DocumentationContext getContext() {
        return context;
    }

    protected List<? extends T> getObjectParts() {
        return objectParts;
    }

}