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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.exception.IORuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.IoUtil;

/**
 * An abstract base class for easy implementation transfers enabling drag and drop of IPS object
 * part containers.
 * <p>
 * <strong>Subclassing:</strong><br>
 * This class provides the infrastructure to convert IPS object part containers to bytes and vice
 * versa. Subclasses simply need to implement the methods
 * {@link #writePartContainer(IIpsObjectPartContainer, DataOutputStream)} and
 * {@link #readPartContainer(DataInputStream)}, using the provided {@code write} and {@code read}
 * methods. Please read the documentation of the respective methods for further implementation
 * instructions.
 * 
 * @param <T> the type of the IPS object part containers to be transfered
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IIpsObjectPartContainer
 * @see ByteArrayTransfer
 */
public abstract class IpsObjectPartContainerByteArrayTransfer<T extends IIpsObjectPartContainer> extends
        ByteArrayTransfer {

    private final Class<T> transferClass;

    /**
     * @param transferClass the class of the transfered IPS object part containers. Used to check
     *            the validity of drag sources
     */
    protected IpsObjectPartContainerByteArrayTransfer(Class<T> transferClass) {
        super();
        this.transferClass = transferClass;
    }

    /**
     * <strong>Subclassing:</strong><br>
     * This implementation checks the validity of the operation and calls
     * {@link #writePartContainer(IIpsObjectPartContainer, DataOutputStream)} for each dragged
     * {@link IIpsObjectPartContainer}.
     */
    @Override
    protected void javaToNative(Object object, TransferData transferData) {
        if (object == null || !(object instanceof Object[])) {
            return;
        }
        for (Object o : (Object[])object) {
            if (!transferClass.isAssignableFrom(o.getClass())) {
                return;
            }
        }

        if (!isSupportedType(transferData)) {
            return;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        try {
            @SuppressWarnings("unchecked")
            // This is unsafe cast is necessary because ByteArrayTransfer (Eclipse) is not generic
            T[] partContainers = (T[])object;
            for (T partContainer : partContainers) {
                writePartContainer(partContainer, outputStream);
            }
            byte[] buffer = out.toByteArray();
            super.javaToNative(buffer, transferData);

        } finally {
            IoUtil.close(outputStream);
        }
    }

    /**
     * Writes the provided {@link IIpsObjectPartContainer} to the provided {@link DataOutputStream}
     * in byte representation.
     * <p>
     * <strong>Subclassing:</strong><br>
     * Subclasses must write a byte representation of the {@link IIpsObjectPartContainer} to be
     * transfered to the provided {@link DataOutputStream}. For this, the {@code write} methods
     * provided by the base class should be used. What information needs to be written depends on
     * the data that is needed to reconstruct the {@link IpsObjectPartContainer} by means of the
     * {@link #readPartContainer(DataInputStream)} method.
     * <p>
     * Example:
     * 
     * <pre>
     * writeString(part.getIpsProject().getName(), outputStream);
     * writeString(part.getType().getQualifiedName(), outputStream);
     * writeString(part.getType().getIpsObjectType().getId(), outputStream);
     * writeString(part.getId(), outputStream);
     * </pre>
     * 
     * @param partContainer the {@link IIpsObjectPartContainer} to be written in byte representation
     * @param outputStream the {@link DataOutputStream} to write the {@link IIpsObjectPartContainer}
     *            to
     */
    protected abstract void writePartContainer(T partContainer, DataOutputStream outputStream);

    /**
     * Writes the byte representation of the given string to the {@link DataOutputStream}.
     */
    protected final void writeString(String string, DataOutputStream writeOut) {
        try {
            writeOut.writeInt(string.getBytes().length);
            writeOut.write(string.getBytes());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * <strong>Subclassing:</strong><br>
     * This implementation checks the validity of the operation and calls
     * {@link #readPartContainer(DataInputStream)} as long as more data remains in the byte stream.
     */
    @Override
    public Object nativeToJava(TransferData transferData) {
        if (!isSupportedType(transferData)) {
            return null;
        }

        byte[] buffer = (byte[])super.nativeToJava(transferData);
        if (buffer == null) {
            return null;
        }

        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        DataInputStream readInputStream = new DataInputStream(in);
        List<T> partContainers = new ArrayList<T>();
        try {
            while (readInputStream.available() > 20) {
                T partContainer = readPartContainer(readInputStream);
                if (partContainer == null) {
                    throw new RuntimeException();
                }
                partContainers.add(partContainer);
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            IoUtil.close(readInputStream);
        }

        return partContainers.toArray();
    }

    /**
     * Reads and returns an {@link IIpsObjectPartContainer} from the bytes obtained from the
     * provided {@link DataInputStream}.
     * <p>
     * <strong>Subclassing:</strong><br>
     * Subclasses must reconstruct an {@link IIpsObjectPartContainer} from the byte representation
     * provided by the {@link DataOutputStream}. For this, the {@code read} methods provided by the
     * base class should be used. The information that can be obtained depends on what was written
     * to the stream by means of the
     * {@link #writePartContainer(IIpsObjectPartContainer, DataOutputStream)} method. Note that the
     * stream must be read in the same order as it has been written.
     * 
     * <p>
     * Example:
     * 
     * <pre>
     * String projectName = readString(readIn);
     * String typeQualifiedName = readString(readIn);
     * IpsObjectType typeObjectType = IpsObjectType.getTypeForName(readString(readIn));
     * String partId = readString(readIn);
     * ... use the above information to obtain and return the part ...
     * </pre>
     * 
     * @param readInputStream the {@link DataInputStream} to retrieve the bytes from
     */
    protected abstract T readPartContainer(DataInputStream readInputStream);

    /**
     * Converts the next chunk of byte data obtained from the {@link DataInputStream} to a string
     * and returns the string.
     */
    protected final String readString(DataInputStream readInputStream) {
        try {
            byte[] bytes = new byte[readInputStream.readInt()];
            readInputStream.read(bytes);
            return new String(bytes);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

}
