package org.faktorips.devtools.formulalibrary.internal.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibrary;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibraryIpsObjectType;
import org.junit.Test;

public class FormulaLibraryIpsObjectTypeTest extends AbstractIpsPluginTest {

    @Test
    public void testTypeRegistered() {
        assertNotNull(IpsPlugin.getDefault().getIpsModel().getIpsObjectType(FormulaLibraryIpsObjectType.ID));
    }

    @Test
    public void testNewIpsObject() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IpsObjectType type = IpsPlugin.getDefault().getIpsModel().getIpsObjectType(FormulaLibraryIpsObjectType.ID);
        IIpsPackageFragment pack = ipsProject.getIpsPackageFragmentRoots()[0].createPackageFragment("test", true, null);

        // pack.createIpsFile(...) calls newObject() !!!
        IIpsSrcFile file = pack.createIpsFile(type, "TestObject", true, null);
        IIpsObject ipsObject = file.getIpsObject();

        assertNotNull(ipsObject);
        assertTrue(ipsObject instanceof FormulaLibrary);
    }
}
