/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class IpsSrcFileCollectionTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateItems() {
        IIpsProject ipsProject = newIpsProject();

        IProductCmptType type = newProductCmptType(ipsProject, "home.Product");
        IProductCmptType subtype = newProductCmptType(ipsProject, "home.HomeProduct");
        subtype.setSupertype(type.getQualifiedName());

        IProductCmpt cmpt0 = newProductCmpt(ipsProject, "home.ProductA");
        cmpt0.setProductCmptType(type.getQualifiedName());
        IProductCmpt cmpt1 = newProductCmpt(ipsProject, "home.ProductB");
        cmpt1.setProductCmptType(type.getQualifiedName());
        IProductCmpt cmpt2 = newProductCmpt(ipsProject, "home2.ProductA");
        cmpt2.setProductCmptType(subtype.getQualifiedName());

        IpsSrcFileCollection collection = new IpsSrcFileCollection(
                Arrays.asList(cmpt0.getIpsSrcFile(), cmpt1.getIpsSrcFile(), cmpt2.getIpsSrcFile()), type);
        assertTrue(collection.isDuplicateName(cmpt0.getIpsSrcFile()));
        assertTrue(collection.isInstanceOfMetaClass(cmpt0.getIpsSrcFile()));

        assertFalse(collection.isDuplicateName(cmpt1.getIpsSrcFile()));
        assertTrue(collection.isInstanceOfMetaClass(cmpt1.getIpsSrcFile()));

        assertTrue(collection.isDuplicateName(cmpt2.getIpsSrcFile()));
        assertFalse(collection.isInstanceOfMetaClass(cmpt2.getIpsSrcFile()));

        collection = new IpsSrcFileCollection(Arrays.asList());

        try {
            collection.isDuplicateName(cmpt1.getIpsSrcFile());
            fail();
        } catch (IpsException ce) {
            // success
        }

        // MetaObjectClass = null !
        collection = new IpsSrcFileCollection(
                Arrays.asList(cmpt0.getIpsSrcFile(), cmpt1.getIpsSrcFile(), cmpt2.getIpsSrcFile()), null);
        assertFalse(collection.isInstanceOfMetaClass(cmpt0.getIpsSrcFile()));
        assertFalse(collection.isInstanceOfMetaClass(cmpt1.getIpsSrcFile()));
        assertFalse(collection.isInstanceOfMetaClass(cmpt2.getIpsSrcFile()));

        try {
            new IpsSrcFileCollection(null, null);
            fail();
        } catch (NullPointerException e) {
            // success
        }

    }
}
