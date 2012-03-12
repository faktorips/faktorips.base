/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

/* Generated By:JJTree: Do not edit this line. Node.java */

package org.faktorips.fl.parser;

/*
 * All AST nodes must implement this interface. It provides basic machinery for constructing the
 * parent and child relationships between nodes.
 */

public interface Node {

    /**
     * This method is called after the node has been made the current node. It indicates that child
     * nodes can now be added to it.
     */
    public void jjtOpen();

    /**
     * This method is called after all the child nodes have been added.
     */
    public void jjtClose();

    /**
     * This pair of methods are used to inform the node of its parent.
     */
    public void jjtSetParent(Node n);

    public Node jjtGetParent();

    /**
     * This method tells the node to add its argument to the node's list of children.
     */
    public void jjtAddChild(Node n, int i);

    /**
     * This method returns a child node. The children are numbered from zero, left to right.
     */
    public Node jjtGetChild(int i);

    /** Return the number of children the node has. */
    public int jjtGetNumChildren();

    /** Accept the visitor. **/
    public Object jjtAccept(FlParserVisitor visitor, Object data);
}
