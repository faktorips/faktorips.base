/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsModelImplTest extends IpsPluginTest {

    private IIpsProject ipsProject;
    private IpsModel model;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        model = (IpsModel)ipsProject.getIpsModel();
    }

    public void testGetIpsObjectPath() throws CoreException{
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForGeneratedJavaClasses("newpackage");
        ipsProject.setIpsObjectPath(path);
        
        //path is created in the first call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());

        //path is read from the cache in the second call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());

        IIpsProject secondProject = newIpsProject("TestProject2");
        IIpsObjectPath secondPath = secondProject.getIpsObjectPath();
        secondPath.getSourceFolderEntries()[0].setSpecificBasePackageNameForGeneratedJavaClasses("secondpackage");
        secondProject.setIpsObjectPath(secondPath);
        
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());
        assertEquals("secondpackage", secondPath.getSourceFolderEntries()[0].getSpecificBasePackageNameForGeneratedJavaClasses());
    }
    
    
    public void testGetValueDatatypes() throws CoreException, IOException {
        ipsProject.setValueDatatypes(new String[]{Datatype.DECIMAL.getQualifiedName()});
        SortedSet datatypes = new TreeSet();
        model.getValueDatatypes(ipsProject, datatypes);
        assertEquals(1, datatypes.size());
        Iterator it = datatypes.iterator();
        assertEquals(Datatype.DECIMAL, it.next());
    }

    public void testGetDatatypeHelpers() throws IOException, CoreException {
        ipsProject.setValueDatatypes(new String[]{Datatype.DECIMAL.getQualifiedName()});
        DatatypeHelper helper = model.getDatatypeHelper(ipsProject, Datatype.DECIMAL);
        assertEquals(DecimalHelper.class, helper.getClass());
        assertNull(model.getDatatypeHelper(ipsProject, Datatype.MONEY));
    }
    
}
