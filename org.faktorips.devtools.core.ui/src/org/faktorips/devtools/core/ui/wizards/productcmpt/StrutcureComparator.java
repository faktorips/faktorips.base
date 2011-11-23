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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * This comparator compares product component types to sort them in structural order.
 * 
 * @author dirmeier
 * @since 3.6
 */
public class StrutcureComparator implements Comparator<IProductCmptType> {

    private Map<String, Node> graph;
    private final IIpsProject ipsProject;

    public StrutcureComparator(List<IProductCmptType> typesToCompare, IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        initGraph(typesToCompare);
    }

    private void initGraph(List<IProductCmptType> typesToCompare) {
        graph = new HashMap<String, StrutcureComparator.Node>();
        for (IProductCmptType productCmptType : typesToCompare) {
            graph.put(productCmptType.getQualifiedName(), new Node(productCmptType));
        }
        for (Node node : graph.values()) {
            IProductCmptType productCmptType = node.getProductCmptType();
            try {
                List<IAssociation> allAssociations = productCmptType.findAllAssociations(ipsProject);
                for (IAssociation association : allAssociations) {
                    if (!association.isAssoziation()) {
                        Node associatedNode = graph.get(association.getTarget());
                        if (associatedNode != null) {
                            node.addChild(associatedNode);
                        }
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    @Override
    public int compare(IProductCmptType type1, IProductCmptType type2) {
        Node node1 = graph.get(type1.getQualifiedName());
        Node node2 = graph.get(type2.getQualifiedName());
        if (node1.isChildNode(node2)) {
            return -1;
        }
        if (node2.isChildNode(node1)) {
            return 1;
        }
        // if (node1.getChildren().size() != node2.getChildren().size()) {
        // return node2.getChildren().size() - node1.getChildren().size();
        // }
        return type1.getName().compareTo(type2.getName());
    }

    private static class Node {

        private final IProductCmptType productCmptType;

        private final Set<Node> children = new HashSet<StrutcureComparator.Node>();

        public Node(IProductCmptType productCmptType) {
            this.productCmptType = productCmptType;
        }

        /**
         * @return Returns the productCmptType.
         */
        public IProductCmptType getProductCmptType() {
            return productCmptType;
        }

        /**
         * @return Returns the children.
         */
        public Set<Node> getChildren() {
            return children;
        }

        public boolean addChild(Node node) {
            return children.add(node);
        }

        public boolean isChildNode(Node node) {
            return isChildNode(node, new HashSet<StrutcureComparator.Node>());
        }

        private boolean isChildNode(Node node, Set<Node> visited) {
            if (children.contains(node)) {
                return true;
            } else {
                if (!visited.add(this)) {
                    return false;
                }
                for (Node childNode : getChildren()) {
                    if (childNode.isChildNode(node, visited)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Node [productCmptType=" + productCmptType.getQualifiedName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

}
