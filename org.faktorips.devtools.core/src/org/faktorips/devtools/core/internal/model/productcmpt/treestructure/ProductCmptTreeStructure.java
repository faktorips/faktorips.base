/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptVRuleReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of the product component structure
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptTreeStructure implements IProductCmptTreeStructure {

    private final IIpsProject ipsProject;

    private IProductCmptReference root;

    private final GregorianCalendar workingDate;

    private Set<IProductCmptStructureReference> setWithProductCmptsOnly;

    private Set<IProductCmptStructureReference> setWithAll;

    /**
     * Creates a new ProductCmptStructure for the given product component. Instead of a specified
     * working date this constructor uses the latest adjustment (generation).
     * 
     * @deprecated Do not use this constructor anymore! This builds a structure with the latest
     *             generation of every product component. This could lead to invalid structures
     *             containing invalid generation pairs.
     * 
     * @param root The product component to create a structure for.
     * 
     * @throws CycleInProductStructureException if a cycle is detected.
     * @throws NullPointerException if the given product component is <code>null</code>.
     */
    @Deprecated
    public ProductCmptTreeStructure(IProductCmpt root, IIpsProject project) throws CycleInProductStructureException {
        this(root, null, project);
    }

    /**
     * Creates a new ProductCmptStructure for the given product component and the given date.
     * 
     * @param root The product component to create a structure for.
     * @param date The date the structure has to be valid for. That means that the relations between
     *            the product components represented by this structure are valid for the given date.
     * @param project The ips project which ips object path is used as search path.
     * 
     * @throws CycleInProductStructureException if a cycle is detected.
     * @throws NullPointerException if the given product component is <code>null</code> or the given
     *             date is <code>null</code>.
     */
    public ProductCmptTreeStructure(IProductCmpt root, GregorianCalendar date, IIpsProject project)
            throws CycleInProductStructureException {

        ArgumentCheck.notNull(root);
        ArgumentCheck.notNull(project);

        workingDate = date;
        ipsProject = project;
        this.root = buildNode(root, null, null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipsProject == null) ? 0 : ipsProject.hashCode());
        /*
         * We do not use the hash code of root directly but the hash of the wrapped
         * IIpsObjectPartContainer because the hash code of the root uses the hash code of this
         * structure leading to a recursive call.
         */
        if (root == null || root.getWrapped() == null) {
            result = prime * result;
        } else {
            result = prime * result + root.getWrapped().hashCode();
        }
        result = prime * result + ((workingDate == null) ? 0 : workingDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductCmptTreeStructure other = (ProductCmptTreeStructure)obj;
        if (ipsProject == null) {
            if (other.ipsProject != null) {
                return false;
            }
        } else if (!ipsProject.equals(other.ipsProject)) {
            return false;
        }
        /*
         * We do not use the equals method of root directly but the equals method the wrapped
         * IIpsObjectPartContainer because the equals method of the root uses the equals method of
         * this structure leading to a recursive call.
         */
        if (root == null) {
            if (other.root != null) {
                return false;
            }
        } else {
            if (root.getWrapped() == null) {
                if (other.getRoot().getWrapped() != null) {
                    return false;
                }
            } else if (!root.getWrapped().equals(other.root.getWrapped())) {
                return false;
            }
        }
        if (workingDate == null) {
            if (other.workingDate != null) {
                return false;
            }
        } else if (!workingDate.equals(other.workingDate)) {
            return false;
        }
        return true;
    }

    @Override
    public GregorianCalendar getValidAt() {
        return workingDate;
    }

    @Override
    public IProductCmptReference getRoot() {
        return root;
    }

    @Override
    public void refresh() throws CycleInProductStructureException {
        setWithAll = null;
        setWithProductCmptsOnly = null;
        root = buildNode(root.getProductCmpt(), null, null);
    }

    @Override
    public Set<IProductCmptStructureReference> toSet(boolean productCmptOnly) {
        if (productCmptOnly) {
            if (setWithProductCmptsOnly != null) {
                return setWithProductCmptsOnly;
            }
        } else if (setWithAll != null) {
            return setWithAll;
        }

        Set<IProductCmptStructureReference> result = new HashSet<IProductCmptStructureReference>();
        result.add(root);
        addChildrenToList(root, result, productCmptOnly);

        if (productCmptOnly) {
            setWithProductCmptsOnly = result;
        } else {
            setWithAll = result;
        }

        return result;
    }

    /**
     * Requests all children from the given parent and add them to the given list.
     * 
     * @param parent The parent to get the children from.
     * @param set The set to add the children to.
     * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>
     *            s.
     */
    private void addChildrenToList(IProductCmptStructureReference parent,
            Set<IProductCmptStructureReference> set,
            boolean productCmptOnly) {

        if (!productCmptOnly) {
            addChildrenToList(getChildProductCmptTypeAssociationReferences(parent), set, productCmptOnly);
        }
        addChildrenToList(getChildProductCmptReferences(parent), set, productCmptOnly);
        addChildrenToList(getChildProductCmptStructureTblUsageReference(parent), set, productCmptOnly);
    }

    /**
     * Adds all given references to the given list and requests the children for the added ones
     * recursively.
     * 
     * @param children The array of child-references to add to the list.
     * @param set The list to add the children to.
     * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>
     *            s.
     */
    private void addChildrenToList(IProductCmptStructureReference[] children,
            Set<IProductCmptStructureReference> set,
            boolean productCmptOnly) {
        for (IProductCmptStructureReference element : children) {
            set.add(element);
            addChildrenToList(element, set, productCmptOnly);
        }
    }

    /**
     * Creates a new node for the given element and links it to the given parent-node.
     */
    private ProductCmptReference buildNode(IProductCmpt cmpt,
            IProductCmptLink link,
            ProductCmptTypeAssociationReference parent) throws CycleInProductStructureException {

        ProductCmptReference node = new ProductCmptReference(this, parent, cmpt, link);
        node.setChildren(buildChildNodes(cmpt, node));
        return node;
    }

    /**
     * Creates child nodes for the given parent. The children are created out of the given
     * relations.
     * 
     * @param links The relations to create nodes for.
     * @param parent The parent for the new nodes
     */
    private ProductCmptStructureReference[] buildChildNodes(IProductCmptLink[] links,
            ProductCmptTypeAssociationReference parent,
            IProductCmptTypeAssociation association) throws CycleInProductStructureException {

        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();
        for (IProductCmptLink link : links) {
            try {
                IProductCmpt p = link.findTarget(ipsProject);
                if (p != null) {
                    if (association.isAssoziation()) {
                        children.add(new ProductCmptReference(this, parent, p, link));
                    } else {
                        children.add(buildNode(p, link, parent));
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
        return children.toArray(result);
    }

    /**
     * Creates new child nodes for the given element and parent.
     * 
     * @param element The element the new children can be found in as relation-targets.
     * @param parent The parent node for the new children.
     */
    private ProductCmptStructureReference[] buildChildNodes(IIpsElement element, ProductCmptStructureReference parent)
            throws CycleInProductStructureException {

        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();

        if (element instanceof IProductCmpt) {
            IProductCmpt cmpt = ((IProductCmpt)element);
            IProductCmptGeneration activeGeneration = null;
            if (workingDate != null) {
                activeGeneration = cmpt.getGenerationEffectiveOn(workingDate);
            } else {
                IIpsObjectGeneration[] generations = cmpt.getGenerationsOrderedByValidDate();
                if (generations.length > 0) {
                    activeGeneration = (IProductCmptGeneration)generations[generations.length - 1];
                }
            }
            if (activeGeneration == null) {
                return new ProductCmptReference[0];
            }

            // Sort links by association
            Hashtable<String, List<IProductCmptLink>> mapping = new Hashtable<String, List<IProductCmptLink>>();

            List<IProductCmptLink> links = activeGeneration.getLinksIncludingProductCmpt();
            putLinksToMap(links, mapping);

            addAssociationsAsChildren(parent, children, cmpt, mapping);

            List<IValidationRuleConfig> rules = activeGeneration.getValidationRuleConfigs();
            for (IValidationRuleConfig rule : rules) {
                ProductCmptStructureReference node = new ProductCmptVRuleReference(this, parent, rule);
                children.add(node);
            }
            addTableContentUsages(parent, children, cmpt.getTableContentUsages());
            addTableContentUsages(parent, children, activeGeneration.getTableContentUsages());
        }

        ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
        return children.toArray(result);
    }

    private void addTableContentUsages(ProductCmptStructureReference parent,
            List<IProductCmptStructureReference> children,
            ITableContentUsage[] tcus) throws CycleInProductStructureException {
        for (ITableContentUsage tcu : tcus) {
            ProductCmptStructureReference node = new ProductCmptStructureTblUsageReference(this, parent, tcu);
            children.add(node);
        }
    }

    private void addAssociationsAsChildren(ProductCmptStructureReference parent,
            List<IProductCmptStructureReference> children,
            IProductCmpt cmpt,
            Hashtable<String, List<IProductCmptLink>> mapping) throws CycleInProductStructureException {
        List<IProductCmptTypeAssociation> associations = new ArrayList<IProductCmptTypeAssociation>();
        IProductCmptType cmptType = cmpt.findProductCmptType(ipsProject);
        if (cmptType != null) {
            // get again all associations. in the previous constructed list there are only
            // not empty relations
            associations = cmptType.findAllNotDerivedAssociations(ipsProject);
        }
        for (IAssociation iAssociation : associations) {
            IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)iAssociation;
            if (association.isRelevant()) {
                ProductCmptTypeAssociationReference node = new ProductCmptTypeAssociationReference(this, parent,
                        association);
                List<IProductCmptLink> linksList = mapping.get(association.getName());
                if (linksList == null) {
                    linksList = new ArrayList<IProductCmptLink>();
                }
                node.setChildren(
                        buildChildNodes(linksList.toArray(new IProductCmptLink[linksList.size()]), node, association));
                children.add(node);
            }
        }
    }

    private void putLinksToMap(List<IProductCmptLink> links, Hashtable<String, List<IProductCmptLink>> mapping) {
        for (IProductCmptLink link : links) {
            try {
                IProductCmptTypeAssociation association = link.findAssociation(ipsProject);
                if (association == null) {
                    // no relation type found - inconsinstent model or product definition -
                    // ignore it.
                    continue;
                }
                List<IProductCmptLink> linksForAssociation = mapping.get(association.getName());
                if (linksForAssociation == null) {
                    linksForAssociation = new ArrayList<IProductCmptLink>();
                    mapping.put(association.getName(), linksForAssociation);
                    // associations.add(association);
                }
                linksForAssociation.add(link);
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
    }

    @Override
    public IProductCmptReference getParentProductCmptReference(IProductCmptStructureReference child) {
        ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
        IProductCmptStructureReference result = ref.getParent();
        if (result instanceof IProductCmptReference) {
            return (IProductCmptReference)result;
        } else if (result != null) {
            return (IProductCmptReference)result.getParent();
        }
        return null;
    }

    @Override
    public IProductCmptTypeAssociationReference getParentProductCmptTypeRelationReference(
            IProductCmptStructureReference child) {
        ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
        IProductCmptStructureReference result = ref.getParent();
        if (result instanceof IProductCmptTypeAssociationReference) {
            return (IProductCmptTypeAssociationReference)result;
        } else if (result != null) {
            return (IProductCmptTypeAssociationReference)result.getParent();
        }
        return null;
    }

    @Override
    public IProductCmptReference[] getChildProductCmptReferences(IProductCmptStructureReference parent) {
        if (parent instanceof IProductCmptTypeAssociationReference) {
            IProductCmptStructureReference[] children = ((ProductCmptTypeAssociationReference)parent).getChildren();
            IProductCmptReference[] result = new IProductCmptReference[children.length];
            System.arraycopy(children, 0, result, 0, children.length);
            return result;
        } else if (parent instanceof ProductCmptStructureTblUsageReference) {
            return new IProductCmptReference[0];
        } else {
            ProductCmptStructureReference[] children = ((ProductCmptReference)parent).getChildren();
            List<IProductCmptReference> result = new ArrayList<IProductCmptReference>();
            for (ProductCmptStructureReference ref : children) {
                for (ProductCmptStructureReference child : ref.getChildren()) {
                    if (child instanceof IProductCmptReference) {
                        result.add((IProductCmptReference)child);
                    }
                }
            }
            return result.toArray(new IProductCmptReference[result.size()]);
        }
    }

    @Override
    public IProductCmptTypeAssociationReference[] getChildProductCmptTypeAssociationReferences(
            IProductCmptStructureReference parent) {
        return getChildProductCmptTypeAssociationReferences(parent, true);
    }

    @Override
    public IProductCmptTypeAssociationReference[] getChildProductCmptTypeAssociationReferences(
            IProductCmptStructureReference parent, boolean includeEmptyAssociations) {

        if (parent instanceof IProductCmptReference) {
            List<IProductCmptTypeAssociationReference> associationReferences = new ArrayList<IProductCmptTypeAssociationReference>();
            IProductCmptStructureReference[] children = ((ProductCmptReference)parent).getChildren();
            for (IProductCmptStructureReference element : children) {
                if (element instanceof IProductCmptTypeAssociationReference) {
                    IProductCmptTypeAssociationReference relationReference = (IProductCmptTypeAssociationReference)element;
                    if (includeEmptyAssociations || getChildProductCmptReferences(relationReference).length > 0) {
                        associationReferences.add(relationReference);
                    }
                }
            }
            return associationReferences
                    .toArray(new IProductCmptTypeAssociationReference[associationReferences.size()]);
        } else if (parent instanceof ProductCmptStructureReference) {
            ProductCmptStructureReference[] children = ((ProductCmptStructureReference)parent).getChildren();
            List<IProductCmptTypeAssociationReference> result = new ArrayList<IProductCmptTypeAssociationReference>();
            for (ProductCmptStructureReference element : children) {
                if (element instanceof IProductCmptTypeAssociationReference) {
                    result.add((IProductCmptTypeAssociationReference)element);
                }
            }
            return result.toArray(new IProductCmptTypeAssociationReference[result.size()]);
        } else {
            return new IProductCmptTypeAssociationReference[0];
        }
    }

    @Override
    public IProductCmptStructureTblUsageReference[] getChildProductCmptStructureTblUsageReference(
            IProductCmptStructureReference parent) {
        List<ProductCmptStructureTblUsageReference> tblUsageReferences = new ArrayList<ProductCmptStructureTblUsageReference>();
        IProductCmptStructureReference[] children = getChildren(parent);
        for (IProductCmptStructureReference element : children) {
            if (element instanceof ProductCmptStructureTblUsageReference) {
                ProductCmptStructureTblUsageReference tblUsageReference = (ProductCmptStructureTblUsageReference)element;
                if (StringUtils.isNotEmpty(tblUsageReference.getTableContentUsage().getTableContentName())) {
                    tblUsageReferences.add(tblUsageReference);
                }
            }
        }
        return tblUsageReferences.toArray(new IProductCmptStructureTblUsageReference[tblUsageReferences.size()]);
    }

    private IProductCmptStructureReference[] getChildren(IProductCmptStructureReference parent) {
        if (parent instanceof IProductCmptReference) {
            return ((ProductCmptReference)parent).getChildren();
        } else {
            return ((ProductCmptStructureReference)parent).getChildren();
        }
    }

    @Override
    public IProductCmptVRuleReference[] getChildProductCmptVRuleReferences(IProductCmptStructureReference parent) {
        List<ProductCmptVRuleReference> vRuleReferences = new ArrayList<ProductCmptVRuleReference>();
        IProductCmptStructureReference[] children = getChildren(parent);
        for (IProductCmptStructureReference element : children) {
            if (element instanceof ProductCmptVRuleReference) {
                ProductCmptVRuleReference vRuleReference = (ProductCmptVRuleReference)element;
                if (StringUtils.isNotEmpty(vRuleReference.getValidationRuleConfig().getName())) {
                    vRuleReferences.add(vRuleReference);
                }
            }
        }
        return vRuleReferences.toArray(new ProductCmptVRuleReference[vRuleReferences.size()]);
    }

    @Override
    public boolean referencesProductCmptQualifiedName(String prodCmptQualifiedName) {
        return root.findProductCmptReference(prodCmptQualifiedName) != null;
    }

    @Override
    public List<IProductCmptReference> findReferencesFor(List<IProductCmpt> cmpts) {
        ArrayList<IProductCmptReference> result = new ArrayList<IProductCmptReference>();

        for (IIpsElement selectElement : cmpts) {
            result.addAll(getReference(selectElement));
        }

        return result;
    }

    private List<IProductCmptReference> getReference(IIpsElement selectElement) {
        List<IProductCmptReference> result = getReferences(getRoot(), selectElement);

        IProductCmpt rootCmpt = getRoot().getProductCmpt();
        if (selectElement.equals(rootCmpt)) {
            result.add(getRoot());
        }

        return result;
    }

    private List<IProductCmptReference> getReferences(IProductCmptReference reference, IIpsElement selectElement) {
        IProductCmptReference[] children = getChildProductCmptReferences(reference);
        ArrayList<IProductCmptReference> result = new ArrayList<IProductCmptReference>();
        for (IProductCmptReference child : children) {
            if (selectElement.equals(child.getProductCmpt())) {
                result.add(child);
            }
            result.addAll(getReferences(child, selectElement));
        }
        return result;
    }

    @Override
    public GregorianCalendar getValidTo() {
        return getRoot().getValidTo();
    }
}
