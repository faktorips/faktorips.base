/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.dnd;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.junit.Before;
import org.junit.Test;

public class IpsByteArrayTransferTest {

    private TestTransfer transfer;

    @Before
    public void setUp() {
        transfer = new TestTransfer();
    }

    @Test(expected = SWTException.class)
    public void testJavaToNative_ThrowExceptionIfTransferTypeIsNotSupported() {
        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.isSupportedType(any(TransferData.class))).thenReturn(false);

        transfer.javaToNative(new Object(), new TransferData());
    }

    @Test(expected = SWTException.class)
    public void testJavaToNative_ThrowExceptionIfValidateFails() {
        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.validate(any())).thenReturn(false);

        transfer.javaToNative(new Object(), new TransferData());
    }

    @Test
    public void testJavaToNative_CallWriteObjectForAllTransferedObjects() {
        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.isSupportedType(any(TransferData.class))).thenReturn(true);
        when(transferSpy.validate(any())).thenReturn(true);

        IIpsObjectPart object1 = mock(IIpsObjectPart.class);
        IIpsObjectPart object2 = mock(IIpsObjectPart.class);
        transferSpy.javaToNative(new Object[] { object1, object2 }, new TransferData());

        verify(transferSpy).writeObject(eq(object1), any(DataOutput.class));
        verify(transferSpy).writeObject(eq(object2), any(DataOutput.class));
    }

    @Test
    public void testValidate_ReturnFalseIfObjectIsNull() {
        assertFalse(transfer.validate(null));
    }

    @Test
    public void testValidate_ReturnFalseIfObjectIsNotAnArray() {
        assertFalse(transfer.validate(new Object()));
    }

    @Test
    public void testValidateReturnFalseIfTheTransferClassIsNotAssignableFromATransferedObject() {
        assertFalse(transfer.validate(new Object[] { mock(IIpsObjectPart.class), new Object(),
                mock(IIpsObjectPart.class) }));
    }

    @Test
    public void testValidate_ReturnTrueIfAllObjectsAreValid() {
        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.validateObject(any(IIpsObjectPart.class))).thenReturn(true);

        assertTrue(transferSpy.validate(new Object[] { mock(IIpsObjectPart.class), mock(IIpsObjectPart.class) }));
    }

    @Test
    public void testValidate_ReturnFalseIfAnyObjectIsNotValid() {
        IIpsObjectPart object1 = mock(IIpsObjectPart.class);
        IIpsObjectPart object2 = mock(IIpsObjectPart.class);
        IIpsObjectPart object3 = mock(IIpsObjectPart.class);

        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.validateObject(object1)).thenReturn(true);
        when(transferSpy.validateObject(object2)).thenReturn(false);
        when(transferSpy.validateObject(object3)).thenReturn(true);

        assertFalse(transferSpy.validate(new Object[] { object1, object2, object3 }));
    }

    @Test
    public void testValidateObject_ReturnFalseIfObjectIsNull() {
        assertFalse(transfer.validateObject(null));
    }

    @Test
    public void testValidateObject_ReturnTrueIfObjectIsNotNull() {
        assertTrue(transfer.validateObject(mock(IIpsObjectPart.class)));
    }

    @Test
    public void testNativeToJava_ReturnNullIfTransferTypeIsNotSupported() {
        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.isSupportedType(any(TransferData.class))).thenReturn(false);

        assertNull(transferSpy.nativeToJava(new TransferData()));
    }

    @Test
    public void testNativeToJava_ReturnEmptyArrayIfNoBytesAreTransfered() {
        TestTransfer transferSpy = spy(transfer);
        when(transferSpy.isSupportedType(any(TransferData.class))).thenReturn(true);
        when(transferSpy.validate(any())).thenReturn(true);

        assertArrayEquals(new Object[0], (Object[])transferSpy.nativeToJava(new TransferData()));
    }

    @Test
    public void testWriteAndReadString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        String writeString = "foo";
        transfer.writeString(writeString, outputStream);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        DataInputStream readInputStream = new DataInputStream(in);
        String readString = transfer.readString(readInputStream);

        assertEquals(writeString, readString);
    }

    @Test
    public void testWriteAndReadInt() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        int writeInt = 5;
        transfer.writeInt(writeInt, outputStream);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        DataInputStream readInputStream = new DataInputStream(in);
        int readInt = transfer.readInt(readInputStream);

        assertEquals(writeInt, readInt);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadString_ThrowExceptionIfNextElementIsNotAString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        transfer.writeInt(1, outputStream);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        DataInputStream readInputStream = new DataInputStream(in);
        transfer.readString(readInputStream);
    }

    @Test(expected = IllegalStateException.class)
    public void testReadInt_ThrowExceptionIfNextElementIsNotAnInt() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        transfer.writeString("foo", outputStream);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        DataInputStream readInputStream = new DataInputStream(in);
        transfer.readInt(readInputStream);
    }

    private static class TestTransfer extends IpsByteArrayTransfer<IIpsObjectPart> {

        private final IIpsObjectPart transferedObject = mock(IIpsObjectPart.class);

        protected TestTransfer() {
            super(IIpsObjectPart.class);
        }

        @Override
        protected void writeObject(IIpsObjectPart object, DataOutput output) {
            writeInt(1, output);
        }

        @Override
        protected IIpsObjectPart readObject(DataInput input) {
            return transferedObject;
        }

        @Override
        protected int[] getTypeIds() {
            return null;
        }

        @Override
        protected String[] getTypeNames() {
            return null;
        }

        @Override
        public boolean isSupportedType(TransferData transferData) {
            return false;
        }

    }

}
