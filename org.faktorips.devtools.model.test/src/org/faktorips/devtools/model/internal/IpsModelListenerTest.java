/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IModificationStatusChangeListener;
import org.faktorips.devtools.model.ModificationStatusChangedEvent;
import org.faktorips.devtools.model.abstraction.AFile;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the listener support.
 * 
 * @author Jan Ortmann
 */
public class IpsModelListenerTest extends AbstractIpsPluginTest {

    private TestContentChangeListener contentChangeListener;
    private TestModificationStatusChangeListener statusChangeListener;

    private IIpsProject project;
    private IPolicyCmptType type;
    private IIpsSrcFile file;

    public IpsModelListenerTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject();
        type = newPolicyCmptType(project, "Policy");
        file = type.getIpsSrcFile();
        file.save(true, null);

        contentChangeListener = new TestContentChangeListener();
        statusChangeListener = new TestModificationStatusChangeListener();
        IIpsModel.get().addChangeListener(contentChangeListener);
        IIpsModel.get().addModifcationStatusChangeListener(statusChangeListener);

    }

    @Override
    protected void tearDownExtension() {
        IIpsModel.get().removeChangeListener(contentChangeListener);
        IIpsModel.get().removeModificationStatusChangeListener(statusChangeListener);
    }

    @Test
    public void testChangeIpsObjectProperty() {
        type.setSupertype("Super");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertNull(contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);

        type.setSupertype("NewSuper");
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    @Test
    public void testChangeIpsPartProperty() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = type.newPolicyCmptTypeAttribute();
        file.save(true, null);

        contentChangeListener.count = 0;
        statusChangeListener.count = 0;

        attribute.setName("newName");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_PROPERTY_CHANGED, contentChangeListener.lastEvent.getEventType());
        assertEquals(attribute, contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);

        attribute.setName("NewerName");
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    @Test
    public void testAddPart() {
        IPolicyCmptTypeAttribute attribute = type.newPolicyCmptTypeAttribute();

        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_PART_ADDED, contentChangeListener.lastEvent.getEventType());
        assertEquals(attribute, contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);

        type.newPolicyCmptTypeAttribute();
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    @Test
    public void testDeletePart() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute1 = type.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAttribute attribute2 = type.newPolicyCmptTypeAttribute();
        file.save(true, null);
        contentChangeListener.count = 0;
        statusChangeListener.count = 0;

        attribute1.delete();
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(ContentChangeEvent.TYPE_PART_REMOVED, contentChangeListener.lastEvent.getEventType());
        assertEquals(attribute1, contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);

        attribute2.delete();
        assertEquals(2, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    // NOTE: The asserts in the following test case do not work in all cases, as the resource change
    // notification is run asynchronously and so the event might occur later!
    // So basically, we don't now how to test this!
    // Therefore we comment the whole test case until we have an idea, how we can test this.
    // TODO
    @Ignore
    @Test
    public void testChangeCorrespondigResource() throws Exception {
        AFile ioFile = file.getCorrespondingFile();
        InputStream is = file.getContentFromEnclosingResource();

        TestContentChangeListener listener = new TestContentChangeListener();

        try {
            IIpsModel.get().addChangeListener(listener);
            ioFile.setContents(is, false, null);
            assertEquals(file, listener.lastEvent.getIpsSrcFile());
            assertEquals(ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED, listener.lastEvent.getEventType());
            assertNull(listener.lastEvent.getPart());
            assertEquals(0, listener.lastEvent.getMovedParts().length);

        } finally {
            IIpsModel.get().removeChangeListener(listener);
        }
    }

    @Test
    public void testStopRestartBroadcasting() throws Exception {
        type.setSupertype("Super");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
        assertEquals(file, statusChangeListener.lastEvent.getIpsSrcFile());
        assertEquals(file, contentChangeListener.lastEvent.getIpsSrcFile());
        assertNull(contentChangeListener.lastEvent.getPart());
        assertEquals(0, contentChangeListener.lastEvent.getMovedParts().length);
        file.save(true, null);

        IpsModel model = (IpsModel)file.getIpsModel();
        model.stopBroadcastingChangesMadeByCurrentThread();
        model.stopBroadcastingChangesMadeByCurrentThread();
        model.resumeBroadcastingChangesMadeByCurrentThread();
        contentChangeListener.count = 0;
        statusChangeListener.count = 0;
        type.setSupertype("NewSuper");
        assertEquals(0, contentChangeListener.count);
        assertEquals(0, statusChangeListener.count);
        file.save(true, null);

        model.resumeBroadcastingChangesMadeByCurrentThread();
        contentChangeListener.count = 0;
        statusChangeListener.count = 0;
        type.setSupertype("NewerSuper");
        assertEquals(1, contentChangeListener.count);
        assertEquals(1, statusChangeListener.count);
    }

    class TestContentChangeListener implements ContentsChangeListener {

        int count = 0;
        ContentChangeEvent lastEvent;

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            lastEvent = event;
            count++;
        }

    }

    class TestModificationStatusChangeListener implements IModificationStatusChangeListener {

        int count = 0;
        ModificationStatusChangedEvent lastEvent;

        @Override
        public void modificationStatusHasChanged(ModificationStatusChangedEvent event) {
            lastEvent = event;
            count++;
        }

    }
}
