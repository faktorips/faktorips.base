/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

public class IpsSrcFileViewItemTest extends AbstractIpsPluginTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

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

        InstanceIpsSrcFileViewItem[] items = InstanceIpsSrcFileViewItem.createItems(new IIpsSrcFile[] { cmpt0.getIpsSrcFile(),
                cmpt1.getIpsSrcFile(), cmpt2.getIpsSrcFile() }, type);
        assertEquals(3, items.length);
        assertEquals(cmpt0.getIpsSrcFile(), items[0].getIpsSrcFile());
        assertTrue(items[0].isDuplicateName());
        assertFalse(items[0].isInstanceOfSubtype());

        assertEquals(cmpt1.getIpsSrcFile(), items[1].getIpsSrcFile());
        assertFalse(items[1].isDuplicateName());
        assertFalse(items[1].isInstanceOfSubtype());

        assertEquals(cmpt2.getIpsSrcFile(), items[2].getIpsSrcFile());
        assertTrue(items[2].isDuplicateName());
        assertTrue(items[2].isInstanceOfSubtype());

        items = InstanceIpsSrcFileViewItem.createItems(new IIpsSrcFile[] {}, type);
        assertEquals(0, items.length);

        // MetaObjectClass = null !
        items = InstanceIpsSrcFileViewItem.createItems(new IIpsSrcFile[] { cmpt0.getIpsSrcFile(), cmpt1.getIpsSrcFile(),
                cmpt2.getIpsSrcFile() }, null);
        assertFalse(items[0].isInstanceOfSubtype());
        assertFalse(items[1].isInstanceOfSubtype());
        assertFalse(items[2].isInstanceOfSubtype());

        items = InstanceIpsSrcFileViewItem.createItems(new IIpsSrcFile[] {}, type);
        assertEquals(0, items.length);

        try {
            InstanceIpsSrcFileViewItem.createItems(null, null);
            fail();
        } catch (NullPointerException e) {
        }

    }
}
