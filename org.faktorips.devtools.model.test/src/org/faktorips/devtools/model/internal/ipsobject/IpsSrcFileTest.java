/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.devtools.model.IModificationStatusChangeListener;
import org.faktorips.devtools.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;

public class IpsSrcFileTest extends AbstractIpsPluginTest implements IModificationStatusChangeListener {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile parsableFile; // file with parsable contents
    private IPolicyCmptType policyCmptType;
    private IIpsSrcFile unparsableFile; // file with unparsable contents

    private ModificationStatusChangedEvent lastModStatusEvent;

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
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), false, null);

        parsableFile.getIpsModel().addModifcationStatusChangeListener(this);
    }

    @Override
    protected void tearDownExtension() throws Exception {
        if (errorStream != null) {
            System.setErr(errorStream);
        }
        parsableFile.getIpsModel().removeModificationStatusChangeListener(this);
    }

    @Test
    public void testGetIpsObjectName() {
        assertEquals("ParsableFile", parsableFile.getIpsObjectName());
    }

    @Test
    public void testSave() throws CoreRuntimeException {
        policyCmptType.newPolicyCmptTypeAttribute();
        assertTrue(parsableFile.isDirty());

        lastModStatusEvent = null;
        resetLastContentChangeEvent();
        parsableFile.save(true, null);
        assertFalse(parsableFile.isDirty());
        assertNull(getLastContentChangeEvent());
        assertEquals(parsableFile, lastModStatusEvent.getIpsSrcFile());
    }

    @Test
    public void testSaveWithPreProcessor() throws CoreRuntimeException {
        try (var testIpsModelExtensions = new TestIpsModelExtensions()) {
            testIpsModelExtensions
                    .setPreSaveProcessor(IpsObjectType.POLICY_CMPT_TYPE, (ipsObject) -> {
                        IDescription description = ((IPolicyCmptType)ipsObject).getDescription(Locale.GERMAN);
                        description.setText(description.getText().toUpperCase());
                    });
            policyCmptType.getDescription(Locale.GERMAN).setText("foo");
            assertTrue(parsableFile.isDirty());

            parsableFile.save(true, null);
            assertFalse(parsableFile.isDirty());
            assertThat(parsableFile.getIpsObject().getDescription(Locale.GERMAN).getText(), is("FOO"));
        }
    }

    @Test
    public void testSaveWithPreProcessorForDifferentIpsObjectType() throws CoreRuntimeException {
        try (var testIpsModelExtensions = new TestIpsModelExtensions()) {
            testIpsModelExtensions
                    .setPreSaveProcessor(IpsObjectType.PRODUCT_CMPT, (ipsObject) -> {
                        fail("This PreSaveProcessor should never be called while saving a PolicyCmptType as it is registered for ProductCmpts");
                    });
            policyCmptType.getDescription(Locale.GERMAN).setText("foo");
            assertTrue(parsableFile.isDirty());

            parsableFile.save(true, null);
            assertFalse(parsableFile.isDirty());
            assertThat(parsableFile.getIpsObject().getDescription(Locale.GERMAN).getText(), is("foo"));
        }
    }

    @Test
    public void testIsContentParsable() throws CoreRuntimeException {
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
        AResource resource = parsableFile.getCorrespondingResource();
        assertTrue(resource.exists());
        assertEquals(parsableFile.getName(), resource.getName());
    }

    @Test
    public void testGetCorrespondingFile() {
        AFile file = parsableFile.getCorrespondingFile();
        assertTrue(file.exists());
        assertEquals(parsableFile.getName(), file.getName());
    }

    @Test
    public void testGetIpsObject() throws CoreRuntimeException {
        IIpsObject ipsObject = parsableFile.getIpsObject();
        assertNotNull(ipsObject);
        assertTrue(ipsObject.isFromParsableFile());

        assertSame(ipsObject, parsableFile.getIpsObject());

        ipsObject = unparsableFile.getIpsObject();
        assertNotNull(ipsObject);
        assertFalse(ipsObject.isFromParsableFile());

        // change from unparsable to parsable
        InputStream is = parsableFile.getCorrespondingFile().getContents();
        unparsableFile.getCorrespondingFile().setContents(is, true, null);
        assertSame(ipsObject, unparsableFile.getIpsObject());
        assertTrue(ipsObject.isFromParsableFile());

        // otherway round
        unparsableFile.getCorrespondingFile().setContents(new ByteArrayInputStream("Blabla".getBytes()), true, null);
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
    public void testGetChildren() throws CoreRuntimeException {
        assertEquals(0, unparsableFile.getChildren().length);
        assertEquals(1, parsableFile.getChildren().length);
        assertEquals(parsableFile.getIpsObject(), parsableFile.getChildren()[0]);
    }

    @Test
    public void testHasChildren() throws CoreRuntimeException {
        assertFalse(unparsableFile.hasChildren());
        assertTrue(parsableFile.hasChildren());
    }

    @Test
    public void testIsHistoric() {
        assertFalse(parsableFile.isHistoric());
    }

    @Test
    public void testNewMemento() throws CoreRuntimeException {
        policyCmptType.newPolicyCmptTypeAttribute();
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        assertEquals(true, memento.isDirty());
        assertEquals(parsableFile, memento.getIpsSrcFile());
    }

    @Test
    public void testSetMemento() throws CoreRuntimeException {
        IIpsSrcFileMemento memento = parsableFile.newMemento();
        policyCmptType.newPolicyCmptTypeAttribute();
        parsableFile.setMemento(memento);
        assertEquals(0, policyCmptType.getNumOfAttributes());
        assertFalse(parsableFile.isDirty());
    }

    @Test
    public void testDelete() throws CoreRuntimeException {
        parsableFile.delete();
        assertFalse(parsableFile.exists());
        assertFalse(((IpsModel)parsableFile.getIpsModel()).isCached(parsableFile));
    }

    @Test
    public void testDeleteSrcFileNotExisting() throws CoreRuntimeException {
        parsableFile.delete();
        parsableFile.delete();
        // Test successful if no exception occurs
    }

    @Override
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
        lastModStatusEvent = event;
    }

}
