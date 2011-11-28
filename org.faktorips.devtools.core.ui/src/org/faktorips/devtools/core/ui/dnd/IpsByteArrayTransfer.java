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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.core.exception.IORuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.util.IoUtil;

/**
 * An abstract base class for easy implementation of transfers enabling drag and drop of objects
 * within Faktor-IPS.
 * <p>
 * <strong>Subclassing:</strong><br>
 * This class provides the infrastructure to convert objects to bytes and vice versa. Subclasses
 * simply need to implement the methods {@link #writeObject(Object, DataOutput)} and
 * {@link #readObject(DataInput)}, using the provided {@code write} and {@code read} methods.
 * <p>
 * If necessary, subclasses may also override the methods {@link #validate(Object)} and / or
 * {@link #validateObject(Object)}. However, it should be the exception that this is necessary.
 * <p>
 * Please read the documentation of the respective methods for further implementation instructions.
 * 
 * @param <T> the type of the objects to be transfered
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see ByteArrayTransfer
 */
public abstract class IpsByteArrayTransfer<T> extends ByteArrayTransfer {

    /**
     * Stores the order in which the different {@code write} methods were invoked.
     */
    private final Queue<Class<?>> writeOrder = new ArrayBlockingQueue<Class<?>>(50);

    private final Class<T> transferClass;

    /**
     * @param transferClass the class of the transfered objects. Used to check the validity of drag
     *            sources
     */
    protected IpsByteArrayTransfer(Class<T> transferClass) {
        super();
        this.transferClass = transferClass;
    }

    /**
     * <strong>Subclassing:</strong><br>
     * This implementation first checks the validity of the operation by
     * <ol>
     * <li>testing whether {@link #isSupportedType(TransferData)} returns true and by
     * <li>testing whether {@link #validate(Object)} returns true.
     * </ol>
     * <p>
     * If any of these conditions is not fulfilled, a drag and drop error of code
     * {@link DND#ERROR_INVALID_DATA} is issued. Otherwise, {@link #writeObject(Object, DataOutput)}
     * is invoked for each dragged object.
     * 
     * @see DND#error(int)
     */
    @Override
    protected final void javaToNative(Object object, TransferData transferData) {
        if (!isSupportedType(transferData) || !validate(object)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(out);
        try {
            @SuppressWarnings("unchecked")
            // This is unsafe cast is necessary because ByteArrayTransfer (Eclipse) is not generic
            T[] typedTransferObjects = (T[])object;
            for (T typedTransferObject : typedTransferObjects) {
                writeObject(typedTransferObject, outputStream);
            }
            byte[] buffer = out.toByteArray();
            super.javaToNative(buffer, transferData);

        } finally {
            IoUtil.close(outputStream);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Subclassing:</strong><br>
     * This implementation first checks that the object is not null and that the object is an array.
     * Then, it is checked that the <em>transfer class</em> - which was provided to this
     * {@link IpsByteArrayTransfer} via the constructor - is assignable from each transfered
     * object's class. Finally, {@link #validateObject(Object)} is invoked for each transfered
     * object. If {@link #validateObject(Object)} returns false for any transfered object, false is
     * returned.
     * <p>
     * Note that this implementation is not final and may be overridden by subclasses if it is
     * inappropriate for the subclass.
     * 
     * @return false if any of the following conditions applies (true otherwise):
     *         <ol>
     *         <li>the object is null <li>the object is not an array <li> the <em>transfer class
     *         </em> is not assignable from an object within the array <li>
     *         {@link #validateObject(Object)} returns false for any object within the array
     *         </ol>
     * 
     * @see Class#isAssignableFrom(Class)
     */
    @Override
    protected boolean validate(Object object) {
        if (object == null || !(object instanceof Object[])) {
            return false;
        }

        for (Object o : (Object[])object) {
            if (!transferClass.isAssignableFrom(o.getClass())) {
                return false;
            }
        }

        for (Object transferObject : (Object[])object) {
            @SuppressWarnings("unchecked")
            // This is unsafe cast is necessary because ByteArrayTransfer (Eclipse) is not generic
            T typedTransferObject = (T)transferObject;
            if (!validateObject(typedTransferObject)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates whether the provided object is of the correct format for this {@link Transfer}.
     * <p>
     * Returns true if the check succeeded, false otherwise.
     * <p>
     * <strong>Subclassing:</strong><br>
     * This implementation checks that the object is not null.
     * <p>
     * Note that this implementation is not final and may be overridden by subclasses if it is
     * inappropriate for the subclass.
     * 
     * @param object the object whose format is to be validated
     * 
     * @return true if the provided object is of the correct format, false if not
     */
    protected boolean validateObject(T object) {
        return object != null;
    }

    /**
     * Writes the provided object to the provided {@link DataOutput} in byte representation.
     * <p>
     * <strong>Subclassing:</strong><br>
     * Subclasses must write a byte representation of the object to be transfered to the provided
     * {@link DataOutput}. For this, the {@code write} methods provided by the base class should be
     * used. What information needs to be written depends on the data that is needed to reconstruct
     * the object by means of the {@link #readObject(DataInput)} method.
     * <p>
     * Example (for {@link IIpsObjectPart}):
     * 
     * <pre>
     * writeString(object.getIpsProject().getName(), output);
     * writeString(object.getType().getQualifiedName(), output);
     * writeString(object.getType().getIpsObjectType().getId(), output);
     * writeString(object.getId(), output);
     * </pre>
     * 
     * @param object the object to be written in byte representation
     * @param output the {@link DataOutput} to write the object to
     */
    protected abstract void writeObject(T object, DataOutput output);

    /**
     * Writes the byte representation of the given string to the {@link DataOutput}.
     */
    protected final void writeString(String string, DataOutput output) {
        try {
            output.writeInt(string.getBytes().length);
            output.write(string.getBytes());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        writeOrder.add(String.class);
    }

    /**
     * Writes the byte representation of the given integer to the {@link DataOutput}.
     */
    protected final void writeInt(int integer, DataOutput output) {
        try {
            output.writeInt(integer);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        writeOrder.add(Integer.class);
    }

    /**
     * <strong>Subclassing:</strong><br>
     * This implementation first checks the validity of the operation by testing whether
     * {@link #isSupportedType(TransferData)} returns true. If this is not the case, null is
     * returned. Otherwise, {@link #readObject(DataInput)} is called as long as more data remains in
     * the byte stream.
     * 
     * @return the array of transfered objects or null if {@link #isSupportedType(TransferData)}
     *         returns false
     */
    @Override
    protected final Object nativeToJava(TransferData transferData) {
        if (!isSupportedType(transferData)) {
            return null;
        }

        byte[] buffer = (byte[])super.nativeToJava(transferData);
        if (buffer == null) {
            return new Object[0];
        }

        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        DataInputStream readInputStream = new DataInputStream(in);
        List<T> objects = new ArrayList<T>();
        try {
            while (readInputStream.available() > 0) {
                T transferObject = readObject(readInputStream);
                if (transferObject == null) {
                    throw new RuntimeException();
                }
                objects.add(transferObject);
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        } finally {
            IoUtil.close(readInputStream);
        }

        @SuppressWarnings({ "unchecked" })
        /*
         * We need this unchecked cast because Array.newInstance(...) has Object as return type, but
         * of course it returns a typed array. Done the assignment to a temporary variable to be
         * able to suppress the warning at assignment-level.
         */
        T[] temp = (T[])Array.newInstance(transferClass, objects.size());
        return objects.toArray(temp);
    }

    /**
     * Reads and returns an object from the bytes obtained from the provided {@link DataInput} .
     * <p>
     * <strong>Subclassing:</strong><br>
     * Subclasses must construct an object of type T from the byte representation provided by the
     * {@link DataInput}. For this, the {@code read} methods provided by the base class should be
     * used. The information that can be obtained depends on what was written to the stream by means
     * of the {@link #writeObject(Object, DataOutput)} method. Note that the stream must be read in
     * the same order as it has been written.
     * 
     * <p>
     * Example (for {@link IIpsObjectPart}):
     * 
     * <pre>
     * String projectName = readString(input);
     * String typeQualifiedName = readString(input);
     * IpsObjectType typeObjectType = IpsObjectType.getTypeForName(readString(input));
     * String partId = readString(input);
     * ... use the above information to obtain and return the part ...
     * </pre>
     * 
     * @param input the {@link DataInput} to retrieve the bytes from
     */
    protected abstract T readObject(DataInput input);

    /**
     * Converts the next chunk of byte data obtained from the {@link DataInput} to a string and
     * returns the string.
     * 
     * @throws IllegalStateException if the next element of the {@link DataInput} is not a string
     */
    protected final String readString(DataInput input) {
        checkValidDataTypeForRead(String.class);
        try {
            byte[] bytes = new byte[input.readInt()];
            input.readFully(bytes);
            return new String(bytes);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * Converts the next chuck of byte data obtained from the {@link DataInput} to an integer and
     * returns the integer.
     * 
     * @throws IllegalStateException if the next element of the {@link DataInput} is not an integer
     */
    protected final int readInt(DataInput input) {
        checkValidDataTypeForRead(Integer.class);
        try {
            return input.readInt();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    private void checkValidDataTypeForRead(Class<?> requestedDataType) {
        Class<?> expectedDataType = writeOrder.poll();
        if (expectedDataType != requestedDataType) {
            throw new IllegalStateException("Tried to read an element of type " + requestedDataType //$NON-NLS-1$
                    + " from the input stream, but the next element is of type " + expectedDataType); //$NON-NLS-1$
        }
    }

}
