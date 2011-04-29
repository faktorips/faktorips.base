/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public abstract class AbstractIpsObjectPartsContainerTablePageElement<T extends IIpsObjectPartContainer> extends
        AbstractStandardTablePageElement {

    private DocumentationContext context;
    protected final List<? extends T> objectParts;
    private IExtensionPropertyDefinition[] propertyDefinitions;

    public AbstractIpsObjectPartsContainerTablePageElement(List<? extends T> objectParts, DocumentationContext context) {
        super();
        this.objectParts = objectParts;
        this.setContext(context);
    }

    @Override
    protected final void addDataRows() {
        for (T rowData : objectParts) {
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
        List<PageElement> values = new ArrayList<PageElement>();

        values.addAll(createRowWithIpsObjectPart(rowData));
        values.addAll(createRowWithExtentionPropertiesData(rowData));

        addSubElement(new TableRowPageElement(values.toArray(new PageElement[values.size()])));
    }

    private List<String> getHeadlineWithExtentionPropertiesData() {
        if (isEmpty()) {
            return Collections.emptyList();
        }

        IIpsElement rowData = objectParts.get(0);
        propertyDefinitions = rowData.getIpsModel().getExtensionPropertyDefinitions(rowData.getClass(), true);

        if (ArrayUtils.isEmpty(propertyDefinitions)) {
            return Collections.emptyList();
        }

        List<String> extensionPropertyNames = new ArrayList<String>();
        for (IExtensionPropertyDefinition property : propertyDefinitions) {
            extensionPropertyNames.add(property.getName());
        }
        return extensionPropertyNames;
    }

    private List<PageElement> createRowWithExtentionPropertiesData(T rowData) {
        List<PageElement> extensionPropertyValues = new ArrayList<PageElement>();
        for (IExtensionPropertyDefinition property : propertyDefinitions) {
            Object value = rowData.getExtPropertyValue(property.getPropertyId());
            extensionPropertyValues.add(new TextPageElement(value == null ? null : value.toString()));
        }
        return extensionPropertyValues;
    }

    protected abstract List<? extends PageElement> createRowWithIpsObjectPart(T rowData);

    protected abstract List<String> getHeadlineWithIpsObjectPart();

    @Override
    public boolean isEmpty() {
        return objectParts.isEmpty();
    }

    public void setContext(DocumentationContext context) {
        this.context = context;
    }

    public DocumentationContext getContext() {
        return context;
    }
}