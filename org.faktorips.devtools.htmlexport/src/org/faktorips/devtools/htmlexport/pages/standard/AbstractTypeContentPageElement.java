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
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.types.AssociationTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.InheritedTypeAssociationsPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.InheritedTypeAttributesPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.InheritedTypeMethodsPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * A complete page representing an {@link IType}
 * 
 * @author dicker
 * 
 */
public abstract class AbstractTypeContentPageElement<T extends IType> extends AbstractIpsObjectContentPageElement<T> {

    private List<IType> superTypes;

    /**
     * creates a page, which represents the given type according to the given context
     * 
     */
    public AbstractTypeContentPageElement(T object, DocumentationContext context) {
        super(object, context);
        initSuperTypes();
    }

    @Override
    protected void buildInternal() {
        super.buildInternal();

        addAttributesTable();

        addAssociationsTable();

        addMethodsTable();
    }

    /**
     * adds a table which represents the methods of the type
     */
    protected void addMethodsTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractTypeContentPageElement_methods), TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(getMethodsTablePageElement(),
                getContext().getMessage(HtmlExportMessages.AbstractTypeContentPageElement_noMethods)));

        addInheritedMethods(wrapper);

        addPageElements(wrapper);
    }

    protected void addInheritedMethods(ICompositePageElement wrapper) {
        List<IType> revertedSuperTypes = new ArrayList<>(superTypes);
        Collections.reverse(revertedSuperTypes);
        wrapper.addPageElements(new InheritedTypeMethodsPageElement(getContext(), getDocumentedIpsObject(),
                revertedSuperTypes));
    }

    /**
     * returns a {@link MethodsTablePageElement} for the type
     * 
     */
    MethodsTablePageElement getMethodsTablePageElement() {
        return new MethodsTablePageElement(getDocumentedIpsObject(), getContext());
    }

    @Override
    protected void addTypeHierarchy() {
        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractTypeContentPageElement_hierarchy), TextType.HEADING_2, getContext()));
        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractTypeContentPageElement_superclass), TextType.HEADING_3, getContext()));
        addSuperTypeHierarchy();
        addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractTypeContentPageElement_subclass), TextType.HEADING_3, getContext()));
        addSubTypeHierarchy();
    }

    /**
     * adds a block with subclasses
     */
    protected void addSubTypeHierarchy() {

        List<IPageElement> subTypes = new ArrayList<>();

        for (IIpsSrcFile srcFile : getContext().getDocumentedSourceFiles(getDocumentedIpsObject().getIpsObjectType())) {
            addSubType(subTypes, srcFile);
        }

        if (subTypes.size() == 0) {
            return;
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, getContext(), new ListPageElement(subTypes,
                getContext())));
    }

    private void addSubType(List<IPageElement> subTypes, IIpsSrcFile srcFile) {
        IType type;
        type = (IType)srcFile.getIpsObject();

        if (type == null) {
            return;
        }

        if (type.getSupertype().equals(getDocumentedIpsObject().getQualifiedName())) {
            subTypes.add(new PageElementUtils(getContext()).createLinkPageElement(getContext(), type,
                    TargetType.CONTENT, type.getQualifiedName(), true));
        }
    }

    /**
     * adds a block with superclasses
     */
    protected void addSuperTypeHierarchy() {
        if (superTypes.size() == 1) {
            addPageElements(new TextPageElement(getContext().getMessage(
                    HtmlExportMessages.AbstractTypeContentPageElement_noSuperclasses), getContext()));
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

    protected void initSuperTypes() {
        SupertypeHierarchyVisitor hier = new SupertypeHierarchyVisitor(getDocumentedIpsObject().getIpsProject());
        try {
            hier.start(getDocumentedIpsObject());
        } catch (IpsException e) {
            getContext().addStatus(
                    new IpsStatus(IStatus.ERROR,
                            "Error getting Supertype Hierarchy of " + getDocumentedIpsObject().getQualifiedName(), e)); //$NON-NLS-1$
            superTypes = Collections.emptyList();
            return;
        }
        superTypes = hier.getSuperTypes();
    }

    @Override
    protected void addStructureData() {
        super.addStructureData();

        IType to;
        to = getDocumentedIpsObject().findSupertype(getDocumentedIpsObject().getIpsProject());
        if (to == null) {
            return;
        }

        addPageElements(new WrapperPageElement(
                WrapperType.BLOCK,
                getContext(),
                new TextPageElement(getContext().getMessage(
                        HtmlExportMessages.AbstractTypeContentPageElement_extends)
                        + " ", getContext()), //$NON-NLS-1$
                new PageElementUtils(getContext()).createLinkPageElement(getContext(), to, TargetType.CONTENT,
                        getContext().getLabel(to), true)));
    }

    /**
     * adds a table with the associations of the type
     */
    protected void addAssociationsTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());
        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                "AbstractTypeContentPageElement_associations"), //$NON-NLS-1$
                TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(new AssociationTablePageElement(getDocumentedIpsObject(),
                getContext()),
                getContext()
                        .getMessage(HtmlExportMessages.AbstractTypeContentPageElement_noAssociations)));

        addInheritedAssociations(wrapper);

        addPageElements(wrapper);
    }

    protected void addInheritedAssociations(ICompositePageElement wrapper) {
        List<IType> revertedSuperTypes = new ArrayList<>(superTypes);
        Collections.reverse(revertedSuperTypes);
        wrapper.addPageElements(new InheritedTypeAssociationsPageElement(getContext(), getDocumentedIpsObject(),
                revertedSuperTypes));
    }

    /**
     * adds a table with the attributes of the type
     */
    protected void addAttributesTable() {
        ICompositePageElement wrapper = new WrapperPageElement(WrapperType.BLOCK, getContext());

        wrapper.addPageElements(new TextPageElement(getContext().getMessage(
                HtmlExportMessages.AbstractTypeContentPageElement_attributes), TextType.HEADING_2, getContext()));

        wrapper.addPageElements(getTableOrAlternativeText(getAttributesTablePageElement(),
                getContext().getMessage(HtmlExportMessages.AbstractTypeContentPageElement_noAttributes)));

        addInheritedAttributes(wrapper);

        addPageElements(wrapper);
    }

    protected void addInheritedAttributes(ICompositePageElement wrapper) {
        if (getContext().showInheritedObjectPartsInTable()) {
            return;
        }

        List<IType> revertedSuperTypes = new ArrayList<>(superTypes);
        Collections.reverse(revertedSuperTypes);
        wrapper.addPageElements(new InheritedTypeAttributesPageElement(getContext(), getDocumentedIpsObject(),
                revertedSuperTypes));
    }

    /**
     * returns a table with the attributes of the type
     * 
     */
    abstract AttributesTablePageElement getAttributesTablePageElement();

    private static class SupertypeHierarchyVisitor extends TypeHierarchyVisitor<IType> {

        private List<IType> hierSuperTypes = new ArrayList<>();

        public SupertypeHierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            hierSuperTypes.add(currentType);
            return true;
        }

        public List<IType> getSuperTypes() {
            ArrayList<IType> revertedList = new ArrayList<>(hierSuperTypes);
            Collections.reverse(revertedList);
            return revertedList;
        }

    }

}
