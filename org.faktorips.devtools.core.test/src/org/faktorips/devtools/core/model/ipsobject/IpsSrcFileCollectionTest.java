/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

import junit.framework.Assert;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class IpsSrcFileCollectionTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateItems() throws CoreException {
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

        IpsSrcFileCollection collection = new IpsSrcFileCollection(new IIpsSrcFile[] { cmpt0.getIpsSrcFile(),
                cmpt1.getIpsSrcFile(), cmpt2.getIpsSrcFile() }, type);
        assertTrue(collection.isDuplicateName(cmpt0.getIpsSrcFile()));
        assertTrue(collection.isInstanceOfMetaClass(cmpt0.getIpsSrcFile()));

        assertFalse(collection.isDuplicateName(cmpt1.getIpsSrcFile()));
        assertTrue(collection.isInstanceOfMetaClass(cmpt1.getIpsSrcFile()));

        assertTrue(collection.isDuplicateName(cmpt2.getIpsSrcFile()));
        assertFalse(collection.isInstanceOfMetaClass(cmpt2.getIpsSrcFile()));

        collection = new IpsSrcFileCollection(new IIpsSrcFile[] {});

        try {
            collection.isDuplicateName(cmpt1.getIpsSrcFile());
            Assert.fail();
        } catch (CoreException ce) {
            // success
        }

        // MetaObjectClass = null !
        collection = new IpsSrcFileCollection(new IIpsSrcFile[] { cmpt0.getIpsSrcFile(), cmpt1.getIpsSrcFile(),
                cmpt2.getIpsSrcFile() }, null);
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
