/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartState;

public class IpsObjectPartStateListTransfer extends ByteArrayTransfer {

    private static IpsObjectPartStateListTransfer instance = new IpsObjectPartStateListTransfer(null);
    private static final String TYPE_NAME = "IpsObjectPartStateListTransfer"; //$NON-NLS-1$
    private static final int TYPE_ID = registerType(TYPE_NAME);

    private final ClassLoader classLoader;

    public IpsObjectPartStateListTransfer(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static IpsObjectPartStateListTransfer getWriteInstance() {
        return instance;
    }

    @Override
    public void javaToNative(Object object, TransferData transferData) {
        if (object == null || !(object instanceof IpsObjectPartState[])) {
            return;
        }

        if (isSupportedType(transferData)) {
            IpsObjectPartState[] states = (IpsObjectPartState[])object;
            try {
                // write data to a byte array and then ask super to convert to pMedium
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream(out);
                for (IpsObjectPartState state : states) {
                    writeOut.writeChars(state.toString());
                }
                byte[] buffer = out.toByteArray();
                writeOut.close();

                super.javaToNative(buffer, transferData);

            } catch (IOException e) {
                // TODO
            }
        }
    }

    @Override
    public Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[])super.nativeToJava(transferData);
            if (buffer == null) {
                return null;
            }

            List<IpsObjectPartState> states = new ArrayList<>();
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                DataInputStream readIn = new DataInputStream(in);
                StringBuilder sb = new StringBuilder();
                while (readIn.available() > 0) {
                    sb.append(readIn.readChar());
                }
                readIn.close();
                String[] parts = sb.toString().split("<\\?xml"); //$NON-NLS-1$
                for (String part : parts) {
                    if (part != null && !part.isEmpty()) {
                        states.add(new IpsObjectPartState("<?xml" + part, classLoader)); //$NON-NLS-1$
                    }
                }
            } catch (IOException ex) {
                return null;
            }
            return states.toArray(new IpsObjectPartState[states.size()]);
        }

        return null;
    }

    @Override
    protected boolean validate(Object object) {
        return object instanceof IpsObjectPartState[];
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] { TYPE_ID };
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

}
