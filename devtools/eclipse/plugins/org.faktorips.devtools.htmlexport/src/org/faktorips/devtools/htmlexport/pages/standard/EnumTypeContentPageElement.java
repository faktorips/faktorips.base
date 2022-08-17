/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AbstractIpsObjectPartsContainerTablePageElement;
import org.faktorips.devtools.model.enums.EnumTypeHierarchyVisitor;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * A complete page representing an {@link IEnumType}
 * 
 * @author dicker
 * 
 */
public class EnumTypeContentPageElement extends AbstractIpsObjectContentPageElement<IEnumType> {

    /**
     * 
     * creates a page, which represents the given enumType according to the given context
     * 
     */
    protected EnumTypeContentPageElement(IEnumType object, DocumentationContext context) {
        super(object, context);
    }

    private List<IEnumAttribute> findAllEnumAttributes() {
        return getDocumentedIpsObject().getEnumAttributesIncludeSupertypeCopies(true);
    }

    @Override
    protected void addTypeHierarchy() {
        addSuperTypeHierarchy();
        addSubTypeHierarchy();
    }

    /**
     * adds the subclasses of the enumType
     */
    protected void addSubTypeHierarchy() {
        List<IIpsSrcFile> allClasses = getContext().getDocumentedSourceFiles(
                getDocumentedIpsObject().getIpsObjectType());

        List<IPageElement> subTypes = new ArrayList<>();
        for (IIpsSrcFile srcFile : allClasses) {
            addSubType(subTypes, srcFile);
        }

        if (subTypes.size() == 0) {
            return;
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(),
                new TextPageElement(getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_subclasses),
                        getContext()),
                new ListPageElement(subTypes, getContext())));
    }

    private void addSubType(List<IPageElement> subTypes, IIpsSrcFile srcFile) {
        IEnumType type;
        type = (IEnumType)srcFile.getIpsObject();

        if (type.getSuperEnumType().equals(getDocumentedIpsObject().getQualifiedName())) {
            subTypes.add(new PageElementUtils(getContext()).createLinkPageElement(getContext(), type,
                    TargetType.CONTENT, type.getQualifiedName(), true));
        }
    }

    /**
     * adds the hierarchy of superclasses
     */
    protected void addSuperTypeHierarchy() {
        SupertypeHierarchieVisitor hier = new SupertypeHierarchieVisitor(getDocumentedIpsObject().getIpsProject());
        try {
            hier.start(getDocumentedIpsObject());
        } catch (IpsException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR,
                            "Error getting Supertype Hierarchy of " + getDocumentedIpsObject().getQualifiedName(), e)); //$NON-NLS-1$
            return;

        }
        List<IEnumType> superTypes = hier.getSuperTypes();

        if (superTypes.size() == 1) {
            addPageElements(new TextPageElement(getDocumentedIpsObject().getName(), getContext()));
            return;
        }

        TreeNodePageElement baseElement = new TreeNodePageElement(new TreeNodePageElement(new PageElementUtils(
                getContext()).createLinkPageElement(getContext(), superTypes.get(0), TargetType.CONTENT,
                        superTypes
                                .get(0).getQualifiedName(),
                        true),
                getContext()), getContext());
        TreeNodePageElement element = baseElement;

        for (int i = 1; i < superTypes.size(); i++) {
            if (superTypes.get(i) == getDocumentedIpsObject()) {
                element.addPageElements(new TextPageElement(getContext().getLabel(getDocumentedIpsObject()),
                        getContext()));
                break;
            }
            TreeNodePageElement subElement = new TreeNodePageElement(
                    new PageElementUtils(getContext()).createLinkPageElement(getContext(), superTypes.get(i),
                            TargetType.CONTENT, getContext().getLabel(superTypes.get(i)), true),
                    getContext());
            element.addPageElements(subElement);
            element = subElement;
        }
        addPageElements(baseElement);
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();
        addAttributesTable();

        addValuesTable();
        if (getDocumentedIpsObject().isExtensible()) {
            addEnumContentsList();
        }
    }

    /**
     * adds table representing the attributes of the enumType
     */
    protected void addAttributesTable() {
        EnumAttributesTablePageElement enumAttributesTable;
        enumAttributesTable = new EnumAttributesTablePageElement(findAllEnumAttributes(), getContext());

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.EnumTypeContentPageElement_attributes), TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(enumAttributesTable,
                getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_noAttributes)));

        addPageElements(wrapper);
    }

    /**
     * adds list representing the enumContents of the enumType
     */
    protected void addEnumContentsList() {
        IEnumContent enumContent;
        try {
            enumContent = getDocumentedIpsObject().findEnumContent(getContext().getIpsProject());
        } catch (IpsException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Could not find EnumContent for EnumType  " //$NON-NLS-1$
                    + getDocumentedIpsObject().getQualifiedName(), e));
            return;
        }
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext())
                .addPageElements(new TextPageElement(IpsObjectType.ENUM_CONTENT.getDisplayName(), TextType.HEADING_2,
                        getContext()));

        if (enumContent == null) {
            addPageElements(wrapper.addPageElements(TextPageElement.createParagraph(
                    getContext().getMessage("EnumTypeContentPageElement_no") //$NON-NLS-1$
                            + IpsObjectType.ENUM_CONTENT.getDisplayName(),
                    getContext())));
            return;
        }
        addPageElements(wrapper.addPageElements(new PageElementUtils(getContext()).createLinkPageElement(getContext(),
                enumContent, TargetType.CONTENT, enumContent.getQualifiedName(), true)));

    }

    /**
     * adds table representing the values of the enumType
     */
    protected void addValuesTable() {
        EnumValuesTablePageElement enumValuesTable;
        try {
            enumValuesTable = new EnumValuesTablePageElement(getDocumentedIpsObject(), getContext());
        } catch (IpsException e) {
            IpsStatus status = new IpsStatus(IStatus.ERROR,
                    "Error creating EnumValuesTable of " + getDocumentedIpsObject().getQualifiedName(), e); //$NON-NLS-1$
            getContext().addStatus(status);
            return;
        }

        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.EnumTypeContentPageElement_values), TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(enumValuesTable,
                getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_noValues)));

        addPageElements(wrapper);
    }

    /**
     * a table representing {@link IEnumAttribute}s of a given {@link IEnumType}
     * 
     * @author dicker
     * 
     */
    private class EnumAttributesTablePageElement extends
            AbstractIpsObjectPartsContainerTablePageElement<IEnumAttribute> {

        public EnumAttributesTablePageElement(List<IEnumAttribute> enumAttributes, DocumentationContext context) {
            super(enumAttributes, context);
        }

        @Override
        protected List<IPageElement> createRowWithIpsObjectPart(IEnumAttribute enumAttribute) {
            List<String> attributeData1 = new ArrayList<>();

            attributeData1.add(getContext().getLabel(enumAttribute));
            attributeData1.add(enumAttribute.getDatatype());
            attributeData1.add(enumAttribute.isIdentifier() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
            attributeData1.add(enumAttribute.isUsedAsNameInFaktorIpsUi() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
            attributeData1.add(enumAttribute.isUnique() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
            attributeData1.add(isInheritedEnumAttribute(enumAttribute) ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
            attributeData1.add(getContext().getDescription(enumAttribute));
            List<String> attributeData = attributeData1;
            return Arrays.asList(new PageElementUtils(getContext()).createTextPageElements(attributeData));
        }

        protected boolean isInheritedEnumAttribute(IEnumAttribute rowData) {
            return !getDocumentedIpsObject().containsEnumAttribute(rowData.getName());
        }

        @Override
        protected List<String> getHeadlineWithIpsObjectPart() {
            List<String> headline = new ArrayList<>();

            headline.add(getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_headlineName));
            headline.add(getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_headlineDatatype));
            addHeadlineAndColumnLayout(headline,
                    getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_headlineIdentifier),
                    Style.CENTER);
            addHeadlineAndColumnLayout(
                    headline,
                    getContext().getMessage(
                            HtmlExportMessages.EnumTypeContentPageElement_headlineUsedAsNameInFaktorIpsUi),
                    Style.CENTER);
            addHeadlineAndColumnLayout(headline,
                    getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_headlineUnique),
                    Style.CENTER);
            addHeadlineAndColumnLayout(headline,
                    getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_headlineInherited),
                    Style.CENTER);
            headline.add(getContext().getMessage(HtmlExportMessages.EnumTypeContentPageElement_headlineDescription));

            return headline;
        }
    }

    /**
     * A visitor to get the supertypes of the given enumType
     * 
     * @author dicker
     * 
     */
    private static class SupertypeHierarchieVisitor extends EnumTypeHierarchyVisitor {
        private List<IEnumType> superTypes = new ArrayList<>();

        public SupertypeHierarchieVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IEnumType currentType) {
            superTypes.add(currentType);
            return true;
        }

        public List<IEnumType> getSuperTypes() {
            ArrayList<IEnumType> revertedList = new ArrayList<>(superTypes);
            Collections.reverse(revertedList);
            return revertedList;
        }
    }

}
