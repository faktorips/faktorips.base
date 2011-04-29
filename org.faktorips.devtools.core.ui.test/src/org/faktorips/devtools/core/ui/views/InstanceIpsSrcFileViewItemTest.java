/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class InstanceIpsSrcFileViewItemTest extends AbstractIpsPluginTest {

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

        InstanceIpsSrcFileViewItem[] items = InstanceIpsSrcFileViewItem.createItems(Arrays.asList(new IIpsSrcFile[] {
                cmpt0.getIpsSrcFile(), cmpt1.getIpsSrcFile(), cmpt2.getIpsSrcFile() }), type);
        assertEquals(3, items.length);
        assertEquals(cmpt0.getIpsSrcFile(), items[0].getIpsSrcFile());
        assertTrue(items[0].isDuplicateName());
        assertTrue(items[0].isInstanceOfMetaClass());

        assertEquals(cmpt1.getIpsSrcFile(), items[1].getIpsSrcFile());
        assertFalse(items[1].isDuplicateName());
        assertTrue(items[1].isInstanceOfMetaClass());

        assertEquals(cmpt2.getIpsSrcFile(), items[2].getIpsSrcFile());
        assertTrue(items[2].isDuplicateName());
        assertFalse(items[2].isInstanceOfMetaClass());

        items = InstanceIpsSrcFileViewItem.createItems(new ArrayList<IIpsSrcFile>(), type);
        assertEquals(0, items.length);

        // MetaObjectClass = null !
        items = InstanceIpsSrcFileViewItem.createItems(Arrays.asList(new IIpsSrcFile[] { cmpt0.getIpsSrcFile(),
                cmpt1.getIpsSrcFile(), cmpt2.getIpsSrcFile() }), null);
        assertFalse(items[0].isInstanceOfMetaClass());
        assertFalse(items[1].isInstanceOfMetaClass());
        assertFalse(items[2].isInstanceOfMetaClass());

        items = InstanceIpsSrcFileViewItem.createItems(new ArrayList<IIpsSrcFile>(), type);
        assertEquals(0, items.length);

        try {
            InstanceIpsSrcFileViewItem.createItems(null, null);
            fail();
        } catch (NullPointerException e) {
        }

    }
}
