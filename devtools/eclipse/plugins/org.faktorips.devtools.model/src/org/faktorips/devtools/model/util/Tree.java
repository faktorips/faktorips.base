/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

/**
 * A tree whose nodes enclose an element.
 * 
 * @param <T> the type of elements enclosed in the tree
 */
public class Tree<T> {

    private final Node<T> root;

    /** Constructs a new tree whose root node holds the given element. */
    public Tree(T element) {
        this(new Node<>(null, element));
    }

    private Tree(Node<T> root) {
        super();
        this.root = root;
    }

    /** Return the root of the tree. If this tree is empty, {@code null} is returned. */
    public Node<T> getRoot() {
        return root;
    }

    /** Returns {@code true} if this is an empty tree, i.e. it does not have any nodes. */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Returns the elements from all nodes in the tree. Returns an empty list if the tree is empty.
     */
    public List<T> getAllElements() {
        if (isEmpty()) {
            return Collections.emptyList();
        }
        return root.getAllElements();
    }

    /**
     * Transforms the tree to a new tree holding elements of type U by transforming all nodes in the
     * tree using the given function.
     */
    public <U> Tree<U> transform(Function<? super T, U> transformFunction) {
        if (isEmpty()) {
            return emptyTree();
        }
        return new Tree<>(root.transform(transformFunction, null));
    }

    @Override
    public String toString() {
        return String.format("Tree [root=%s]", root); //$NON-NLS-1$
    }

    /** Returns a new empty tree. */
    public static <U> Tree<U> emptyTree() {
        return new Tree<>((Node<U>)null);
    }

    /**
     * A node in the tree enclosing that encloses an element.
     * 
     * @param <T> the type of element enclosed in the node
     */
    public static class Node<T> {

        private final Node<T> parent;
        private final List<Node<T>> children = Lists.newArrayList();
        private final T element;

        private Node(Node<T> parent, T element) {
            super();
            this.parent = parent;
            this.element = element;
        }

        /**
         * Returns {@code true} if the node has a parent node, i.e. if it is not the root of a tree.
         */
        public boolean hasParent() {
            return parent != null;
        }

        /**
         * Returns the parent node. Returns {@code null} if the node has no parent, i.e. if it is
         * the root of a tree.
         */
        public Node<T> getParent() {
            return parent;
        }

        /** Returns {@code true} if this node has child nodes. */
        public boolean hasChildren() {
            return !children.isEmpty();
        }

        /** Returns the children of the node. Returns an empty list if the node has no children. */
        public List<Node<T>> getChildren() {
            return Collections.unmodifiableList(children);
        }

        /** Returns the element the node encloses. */
        public T getElement() {
            return element;
        }

        /**
         * Creates and returns a new node that is a child of this node and encloses the given
         * element.
         */
        public Node<T> addChild(T s) {
            Node<T> child = new Node<>(this, s);
            addChild(child);
            return child;
        }

        @Override
        public String toString() {
            return String.format("Node [element=%s]", element); //$NON-NLS-1$
        }

        private boolean addChild(Node<T> child) {
            return children.add(child);
        }

        private List<T> getAllElements() {
            List<T> allIpsSrcFiles = Lists.newArrayList();
            allIpsSrcFiles.add(element);
            for (Node<T> child : children) {
                allIpsSrcFiles.addAll(child.getAllElements());
            }
            return allIpsSrcFiles;
        }

        private <U> Node<U> transform(Function<? super T, U> transformFunction, Node<U> newParent) {
            Node<U> transformedNode = new Node<>(newParent, transformFunction.apply(element));
            for (Node<T> child : children) {
                transformedNode.addChild(child.transform(transformFunction, transformedNode));
            }
            return transformedNode;
        }

    }

}
