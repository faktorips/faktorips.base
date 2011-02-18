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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IModificationStatusChangeListener;
import org.faktorips.devtools.core.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class IpsSrcFileTest extends AbstractIpsPluginTest implements IModificationStatusChangeListener,
        ContentsChangeListener {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile parsableFile; // file with parsable contents
    private IPolicyCmptType policyCmptType;
    private IIpsSrcFile unparsableFile; // file with unparsable contents

    private ModificationStatusChangedEvent lastModStatusEvent;
    private ContentChangeEvent lastContentChangedEvent;

    private PrintStream errorStream;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("folder", true, null);

        parsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "ParsableFile", true, null);
        policyCmptType = (IPolicyCmptType)parsableFile.getIpsObject();
        // must supress exception logging and redirect standard error, as we create an invalid ips
        // src file!
        suppressLoggingDuringExecutionOfThisTestCase();
        errorStream = System.err;
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        unparsableFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("UnparsableFile"),
                "blabla", true, null);
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), true, false,
                null);

        parsableFile.getIpsModel().addModifcationStatusChangeListener(this);
        parsableFile.getIpsModel().addChangeListener(this);
    }

    @Override
    protected void tearDownExtension() throws Exception {
        if (errorStream != null) {
            System.setErr(errorStream);
        }
        parsableFile.getIpsModel().removeModificationStatusChangeListener(this);
        parsableFile.getIpsModel().removeChangeListener(this);
    }

    @Test
    public void testGetIpsObjectName() {
        assertEquals("ParsableFile", parsableFile.getIpsObjectName());
    }

    @Test
    public void testSave() throws CoreException {
        policyCmptType.newPolicyCmptTypeAttribute();
        assertTrue(parsableFile.isDirty());

        lastModStatusEvent = null;
        lastContentChangedEvent = null;
        parsableFile.save(true, null);
        assertFalse(parsableFile.isDirty());
        assertNull(lastContentChangedEvent);
        assertEquals(parsableFile, lastModStatusEvent.getIpsSrcFile());
    }

    @Test
    public void testIsContentParsable() throws CoreException {
        assertFalse(unparsableFile.isContentParsable());
        assertTrue(parsableFile.isContentParsable());
    }

    @Test
    public void testDiscardChanges_ParsableContents() throws Exception {
        IPolicyCmptType type = newPolicyCmptType(ipsProject, "Policy");
        IIpsSrcFile file = type.getIpsSrcFile();
        type.newPolicyCmptTypeAttribute();
        assertEquals(1, type.getNumOfAttributes());
        assertTrue(file.isDirty());
        type.setSupertype("UnknownType");
        MessageList list = type.validate(ipsProject);
        assertNotNull(list.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));

        file.discardChanges();
        list = type.validate(ipsProject);
        assertNull(list.getMessageByCode(IType.MSGCODE_SUPERTYPE_NOT_FOUND));

        type = (IPolicyCmptType)file.getIpsObject();
        assertEquals(0, type.getNumOfAttributes());
        assertFalse(file.isDirty());
    }

    @Test
    public void testGetCorrespondingResource() {
        IResource resource = parsableFile.getCorrespondingResource();
        assertTrue(resource.exists());
        assertEquals(parsableFile.getName(), resource.getName());
    }

    @Test
    public void testGetCorrespondingFile() {
        IFile file = parsableFile.getCorrespondingFile();
        assertTrue(file.exists());
        assertEquals(parsableFile.getName(), file.getName());
    }

    @Test
    public void testGetIpsObject() throws CoreException {
        IIpsObject ipsObject = parsableFile.getIpsObject();
        assertNotNull(ipsObject);
        assertTrue(ipsObject.isFromParsableFile());

        ipsObject.setDescription("blabla");
        assertSame(ipsObject, parsableFile.getIpsObject());

        ipsObject = unparsableFile.getIpsObject();
        assertNotNull(ipsObject);
        assertFalse(ipsObject.isFromParsableFile());

        // change from unparsable to parsable
        InputStream is = parsableFile.getCorrespondingFile().getContents();
        unparsableFile.getCorrespondingFile().setContents(is, true, true, null);
        assertSame(ipsObject, unparsableFile.getIpsObject());
        assertTrue(ipsObject.isFromParsableFile());

        // otherway round
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), true, true,
                null);
        assertSame(ipsObject, unparsableFile.getIpsObject());
        assertFalse(ipsObject.isFromParsableFile());
    }

    @Test
    public void testGetElementName() {
        String expectedName = IpsObjectType.POLICY_CMPT_TYPE.getFileName("ParsableFile");
        assertEquals(expectedName, parsableFile.getName());
    }

    @Test
    public void testGetParent() {
    }

    @Test
    public void testGetChildren() throws CoreException {
        assertEquals(0, unparsableFile.getChildren().length);
        assertEquals(1, parsableFile.getChildren().length);
        assertEquals(parsableFile.getIpsObject(), parsableFile.getChildren()[0]);
    }

    @Test
    public void testHasChildren() throws CoreException {
        assertFalse(unparsableFile.hasChildren());
        assertTrue(parsableFile.hasChildren());
    }

    @Test
    public void testIsHistoric() {
        assertFalse(parsableFile.isHistoric());
    }

    @Test
    public void testNewMemento() throws CoreException {
        policyCmptType.newPolicyCmptTypeAttribute();
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        assertEquals(true, memento.isDirty());
        assertEquals(parsableFile, memento.getIpsSrcFile());
    }

    @Test
    public void testSetMemento() throws CoreException {
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        policyCmptType.newPolicyCmptTypeAttribute();
        parsableFile.setMemento(memento);
        assertEquals(0, policyCmptType.getNumOfAttributes());
        assertFalse(parsableFile.isDirty());
    }

    @Override
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
        lastModStatusEvent = event;
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastContentChangedEvent = event;
    }

}
