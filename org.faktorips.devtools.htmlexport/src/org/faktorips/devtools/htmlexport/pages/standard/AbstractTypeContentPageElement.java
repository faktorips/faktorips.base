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
package org.faktorips.devtools.htmlexport.pages.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.TreeNodePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AssociationTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.AttributesTablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MethodsTablePageElement;

/**
 * A complete page representing an {@link IType}
 * 
 * @author dicker
 * 
 * @param <T>
 */
public abstract class AbstractTypeContentPageElement<T extends IType> extends AbstractIpsObjectContentPageElement<T> {

    /**
     * Visitor for superclass hierarchy
     * 
     * @author dicker
     * 
     */
    private class SupertypeHierarchyVisitor extends TypeHierarchyVisitor {
        List<IType> superTypes = new ArrayList<IType>();

        public SupertypeHierarchyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            superTypes.add(currentType);
            return true;
        }

        public List<IType> getSuperTypes() {
            ArrayList<IType> revertedList = new ArrayList<IType>(superTypes);
            Collections.reverse(revertedList);
            return revertedList;
        }
    }

    /**
     * creates a page, which represents the given type according to the given config
     * 
     * @param object
     * @param config
     */
    public AbstractTypeContentPageElement(T object, DocumentorConfiguration config) {
        super(object, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.standard.
     * AbstractObjectContentPageElement#build()
     */
    @Override
    public void build() {
        super.build();

        addAttributesTable();

        addAssociationsTable();

        addMethodsTable();
    }

    /**
     * adds a table which represents the methods of the type
     */
    protected void addMethodsTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper
                .addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_methods,
                        TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(getMethodsTablePageElement(),
                Messages.AbstractTypeContentPageElement_noMethods));

        addPageElements(wrapper);
    }

    /**
     * returns a {@link MethodsTablePageElement} for the type
     * 
     * @return
     */
    MethodsTablePageElement getMethodsTablePageElement() {
        return new MethodsTablePageElement(getDocumentedIpsObject());
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.standard.
     * AbstractObjectContentPageElement#addTypeHierarchy()
     */
    @Override
    protected void addTypeHierarchy() {
        addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_hierarchy, TextType.HEADING_2));
        addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_superclass, TextType.HEADING_3));
        addSuperTypeHierarchy();
        addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_subclass, TextType.HEADING_3));
        addSubTypeHierarchy();
    }

    /**
     * adds a block with subclasses
     */
    protected void addSubTypeHierarchy() {

        List<PageElement> subTypes = new ArrayList<PageElement>();

        for (IIpsSrcFile srcFile : getConfig().getDocumentedSourceFiles(getDocumentedIpsObject().getIpsObjectType())) {
            try {
                IType type = (IType)srcFile.getIpsObject();
                if (type == null) {
                    continue;
                }
                if (type.getSupertype().equals(getDocumentedIpsObject().getQualifiedName())) {
                    subTypes.add(PageElementUtils.createLinkPageElement(getConfig(), type,
                            "content", type.getQualifiedName(), true)); //$NON-NLS-1$
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }

        if (subTypes.size() == 0) {
            return;
        }

        addPageElements(new WrapperPageElement(WrapperType.BLOCK, new ListPageElement(subTypes)));
    }

    /**
     * adds a block with superclasses
     */
    protected void addSuperTypeHierarchy() {
        SupertypeHierarchyVisitor hier = new SupertypeHierarchyVisitor(getDocumentedIpsObject().getIpsProject());
        try {
            hier.start(getDocumentedIpsObject());
        } catch (CoreException e) {
            e.printStackTrace();
        }
        List<IType> superTypes = hier.getSuperTypes();

        if (superTypes.size() == 1) {
            addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_noSuperclasses));
            return;
        }

        TreeNodePageElement baseElement = new TreeNodePageElement(new TreeNodePageElement(PageElementUtils
                .createLinkPageElement(getConfig(), superTypes.get(0),
                        "content", superTypes.get(0).getQualifiedName(), true))); //$NON-NLS-1$
        TreeNodePageElement element = baseElement;

        for (int i = 1; i < superTypes.size(); i++) {
            if (superTypes.get(i) == getDocumentedIpsObject()) {
                element.addPageElements(new TextPageElement(getDocumentedIpsObject().getName()));
                break;
            }
            TreeNodePageElement subElement = new TreeNodePageElement(PageElementUtils.createLinkPageElement(
                    getConfig(), superTypes.get(i), "content", superTypes.get(i).getName(), true));
            element.addPageElements(subElement);
            element = subElement;
        }
        addPageElements(baseElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.standard.
     * AbstractObjectContentPageElement#addStructureData()
     */
    @Override
    protected void addStructureData() {
        super.addStructureData();

        try {
            IType to = getDocumentedIpsObject().findSupertype(getDocumentedIpsObject().getIpsProject());
            if (to == null) {
                return;
            }

            addPageElements(new WrapperPageElement(
                    WrapperType.BLOCK,
                    new PageElement[] {
                            new TextPageElement(Messages.AbstractTypeContentPageElement_extends + " "), PageElementUtils.createLinkPageElement(getConfig(), to, "content", to.getName(), true) })); //$NON-NLS-1$//$NON-NLS-2$ 
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a table with the associations of the type
     */
    protected void addAssociationsTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_associations,
                TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(new AssociationTablePageElement(getDocumentedIpsObject(),
                getConfig()), Messages.AbstractTypeContentPageElement_noAssociations));

        addPageElements(wrapper);
    }

    /**
     * adds a table with the attributes of the type
     */
    protected void addAttributesTable() {
        WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
        wrapper.addPageElements(new TextPageElement(Messages.AbstractTypeContentPageElement_attributes,
                TextType.HEADING_2));

        wrapper.addPageElements(getTableOrAlternativeText(getAttributesTablePageElement(),
                Messages.AbstractTypeContentPageElement_noAttributes));

        addPageElements(wrapper);
    }

    /**
     * returns a table with the attributes of the type
     * 
     * @return
     */
    AttributesTablePageElement getAttributesTablePageElement() {
        return new AttributesTablePageElement(getDocumentedIpsObject());
    }
}
