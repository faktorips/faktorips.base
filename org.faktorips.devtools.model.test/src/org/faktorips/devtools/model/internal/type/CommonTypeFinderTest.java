/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.CommonTypeFinder;
import org.junit.Before;
import org.junit.Test;

public class CommonTypeFinderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private CommonTypeFinder finder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        finder = new CommonTypeFinder();
    }

    @Test
    public void testFindCommonType_NoTypes() {
        assertThat(finder.findCommonType(null), is(nullValue()));
        assertThat(finder.findCommonType(new ArrayList<IProductCmpt>()), is(nullValue()));
    }

    /**
     * Scenario type hierarchy:
     * 
     * <pre>
     * a | b
     * </pre>
     */
    @Test
    public void testCommonTypeOf() throws CoreException {
        IProductCmptType a = newProductCmptType(ipsProject, "a");
        IProductCmptType b = newProductCmptType(a, "b");
        IProductCmpt cmptA = newProductCmpt(a, "cmptA");
        IProductCmpt cmptB = newProductCmpt(b, "cmptB");

        assertThat(CommonTypeFinder.commonTypeOf(Lists.newArrayList(cmptB, cmptB)), is(b));
        assertThat(CommonTypeFinder.commonTypeOf(Lists.newArrayList(cmptA, cmptB)), is(a));
        assertThat(CommonTypeFinder.commonTypeOf(Lists.newArrayList(cmptB, cmptA)), is(a));
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
        IProductCmptType aType = newProductCmptType(ipsProject, "aType");
        IProductCmptType bType = newProductCmptType(aType, "bType");
        IProductCmptType cType = newProductCmptType(bType, "cType");
        IProductCmptType dType = newProductCmptType(bType, "dType");
        IProductCmptType eType = newProductCmptType(dType, "eType");
        aType.setAbstract(true);
        bType.setAbstract(true);
        IProductCmpt a = newProductCmpt(aType, "a");
        IProductCmpt b = newProductCmpt(bType, "b");
        IProductCmpt c = newProductCmpt(cType, "c");
        IProductCmpt d = newProductCmpt(dType, "d");
        IProductCmpt e = newProductCmpt(eType, "e");

        assertThat(finder.findCommonType(Lists.newArrayList(a)), is(aType));
        assertThat(finder.findCommonType(Lists.newArrayList(e)), is(eType));
        assertThat(finder.findCommonType(Lists.newArrayList(e, e)), is(eType));
        assertThat(finder.findCommonType(Lists.newArrayList(d, e)), is(dType));
        assertThat(finder.findCommonType(Lists.newArrayList(c, d, e)), is(bType));
        assertThat(finder.findCommonType(Lists.newArrayList(e, d, c)), is(bType));
        assertThat(finder.findCommonType(Lists.newArrayList(b, d, e)), is(bType));
        assertThat(finder.findCommonType(Lists.newArrayList(a, b, c)), is(aType));
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
        IProductCmptType aType = newProductCmptType(ipsProject, "aType");
        IProductCmptType bType = newProductCmptType(aType, "bType");
        IProductCmptType xType = newProductCmptType(ipsProject, "xType");
        IProductCmptType yType = newProductCmptType(xType, "yType");
        IProductCmpt a = newProductCmpt(aType, "a");
        IProductCmpt b = newProductCmpt(bType, "b");
        IProductCmpt x = newProductCmpt(xType, "x");
        IProductCmpt y = newProductCmpt(yType, "y");

        assertThat(finder.findCommonType(Lists.newArrayList(b, y)), is(nullValue()));
        assertThat(finder.findCommonType(Lists.newArrayList(a, b, x)), is(nullValue()));
        assertThat(finder.findCommonType(Lists.newArrayList(y, x, a)), is(nullValue()));
    }

}
