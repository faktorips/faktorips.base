/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.template;

import static org.faktorips.devtools.model.ipsobject.IpsObjectType.PRODUCT_CMPT;
import static org.faktorips.devtools.model.ipsobject.IpsObjectType.PRODUCT_TEMPLATE;

import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.util.Tree;
import org.faktorips.devtools.model.util.Tree.Node;

public class TemplateHierarchyFinder {

    private TemplateHierarchyFinder() {
        // utility class that should not be instantiated
    }

    /**
     * Returns the hierarchy of the given template as a tree of source files. The root of the tree
     * is the source file of the given template. Source files of product components and templates
     * that (directly or indirectly) reference the given template are nodes in the tree. Returns an
     * empty tree if no template is given or the given product component is not a template.
     * 
     * @param template the template whose hierarchy is returned
     * @param ipsProject the IPS project in which source files are searched
     * @return a tree with the hierarchy of the given template. The tree is empty if no template or
     *             a non-template product component is given.
     */
    public static Tree<IIpsSrcFile> findTemplateHierarchyFor(IProductCmpt template, IIpsProject ipsProject) {
        if (template == null || !template.isProductTemplate()) {
            return Tree.emptyTree();
        }

        Multimap<String, IIpsSrcFile> templateMap = LinkedHashMultimap.create();
        for (IIpsProject p : ipsProject.findReferencingProjectLeavesOrSelf()) {
            templateMap.putAll(createTemplateMap(p));
        }

        Tree<IIpsSrcFile> tree = new Tree<>(template.getIpsSrcFile());
        addTemplateReferences(tree.getRoot(), template.getQualifiedName(), templateMap);
        return tree;
    }

    /**
     * Creates a new template map for the given project. The keys in the template map are the
     * qualified names of templates and the values are the source files found in the given project
     * that reference that template.
     */
    private static Multimap<String, IIpsSrcFile> createTemplateMap(IIpsProject ipsProject) {
        Multimap<String, IIpsSrcFile> templateMap = LinkedHashMultimap.create();
        for (IIpsSrcFile srcFile : ipsProject.findAllIpsSrcFiles(PRODUCT_CMPT, PRODUCT_TEMPLATE)) {
            String template = srcFile.getPropertyValue(IProductCmpt.PROPERTY_TEMPLATE);
            if (IpsStringUtils.isNotBlank(template)) {
                templateMap.put(template, srcFile);
            }
        }
        return templateMap;
    }

    /**
     * Adds child nodes to the given parent node for all source files in the given template map that
     * reference the given template directly or indirectly.
     */
    private static void addTemplateReferences(Node<IIpsSrcFile> parent,
            String templateQName,
            Multimap<String, IIpsSrcFile> templateMap) {

        Set<Node<IIpsSrcFile>> children = addDirectTemplateReferences(parent, templateQName, templateMap);
        for (Node<IIpsSrcFile> child : children) {
            IIpsSrcFile srcFile = child.getElement();
            if (isTemplate(srcFile)) {
                String subTemplateQName = srcFile.getQualifiedNameType().getName();
                addTemplateReferences(child, subTemplateQName, templateMap);
            }
        }
    }

    /**
     * Adds child nodes to the given parent node for all source files in the given template map that
     * directly reference the given template. Returns the added child nodes.
     */
    private static Set<Node<IIpsSrcFile>> addDirectTemplateReferences(Node<IIpsSrcFile> parent,
            String templateQName,
            Multimap<String, IIpsSrcFile> templateMap) {
        Set<Node<IIpsSrcFile>> addedNodes = Sets.newLinkedHashSet();
        for (IIpsSrcFile srcFile : templateMap.get(templateQName)) {
            addedNodes.add(parent.addChild(srcFile));
        }
        return addedNodes;
    }

    private static boolean isTemplate(IIpsSrcFile srcFile) {
        return PRODUCT_TEMPLATE.equals(srcFile.getIpsObjectType());
    }
}
