/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class CommonTypeFinderTest extends AbstractIpsPluginTest {

    @Test
    public void testFindCommonType_NoTypes() throws CoreException {
        IIpsProject project = newIpsProject();

        CommonTypeFinder<IType> finder = CommonTypeFinder.in(project);
        assertThat(finder.findCommonType(null), is(nullValue()));
        assertThat(finder.findCommonType(new ArrayList<IType>()), is(nullValue()));
    }

    /**
     * Scenario type hierarchy:
     * 
     * <pre>
     *   a (abstract)
     *   |
     *   b (abstract)
     *  / \
     * c   d
     *     |
     *     e
     * </pre>
     */
    @Test
    public void testFindCommonType_CommonSuperType() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType a = newProductCmptType(project, "a");
        IProductCmptType b = newProductCmptType(a, "b");
        IProductCmptType c = newProductCmptType(b, "c");
        IProductCmptType d = newProductCmptType(b, "d");
        IProductCmptType e = newProductCmptType(d, "e");
        a.setAbstract(true);
        b.setAbstract(true);

        CommonTypeFinder<IProductCmptType> finder = CommonTypeFinder.in(project);
        assertThat(finder.findCommonType(Lists.newArrayList(a)), is(a));
        assertThat(finder.findCommonType(Lists.newArrayList(e)), is(e));
        assertThat(finder.findCommonType(Lists.newArrayList(e, e)), is(e));
        assertThat(finder.findCommonType(Lists.newArrayList(d, e)), is(d));
        assertThat(finder.findCommonType(Lists.newArrayList(c, d, e)), is(b));
        assertThat(finder.findCommonType(Lists.newArrayList(e, d, c)), is(b));
        assertThat(finder.findCommonType(Lists.newArrayList(b, d, e)), is(b));
        assertThat(finder.findCommonType(Lists.newArrayList(a, b, c)), is(a));
    }

    /**
     * Scenario type hierarchy:
     * 
     * <pre>
     *   a    x
     *   |    |
     *   b    y
     * </pre>
     */
    @Test
    public void testFindCommonType_NoCommonSuperType() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType a = newProductCmptType(project, "a");
        IProductCmptType b = newProductCmptType(a, "b");
        IProductCmptType x = newProductCmptType(project, "x");
        IProductCmptType y = newProductCmptType(x, "y");

        CommonTypeFinder<IProductCmptType> finder = CommonTypeFinder.in(project);
        assertThat(finder.findCommonType(Lists.newArrayList(b, y)), is(nullValue()));
        assertThat(finder.findCommonType(Lists.newArrayList(a, b, x)), is(nullValue()));
        assertThat(finder.findCommonType(Lists.newArrayList(y, x, a)), is(nullValue()));
    }

}
