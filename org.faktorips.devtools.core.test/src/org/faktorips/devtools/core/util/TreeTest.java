/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;

import com.google.common.base.Functions;

import org.junit.Test;

public class TreeTest {

    @Test
    public void testIsEmpty() {
        assertThat(Tree.emptyTree().isEmpty(), is(true));
        assertThat(new Tree<String>("").isEmpty(), is(false));
    }

    @Test
    public void testGetAllElements() {

        Tree<String> tree = new Tree<String>("a");
        tree.getRoot().addChild("b").addChild("c");

        assertThat(tree.getAllElements().size(), is(3));
        assertThat(tree.getAllElements(), hasItems("a", "b", "c"));
    }

    @Test
    public void testTransform() {

        Tree<Integer> intTree = new Tree<Integer>(1);
        intTree.getRoot().addChild(2).addChild(3);

        Tree<String> stringTree = intTree.transform(Functions.toStringFunction());
        assertThat(stringTree.getRoot().getElement(), is("1"));
        assertThat(stringTree.getRoot().getChildren().size(), is(1));
        assertThat(stringTree.getRoot().getChildren().get(0).getElement(), is("2"));
        assertThat(stringTree.getRoot().getChildren().get(0).getChildren().size(), is(1));
        assertThat(stringTree.getRoot().getChildren().get(0).getChildren().get(0).getElement(), is("3"));
        assertThat(stringTree.getRoot().getChildren().get(0).getChildren().get(0).getChildren().isEmpty(), is(true));
    }

}
