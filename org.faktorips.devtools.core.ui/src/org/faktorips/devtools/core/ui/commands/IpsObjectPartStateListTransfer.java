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
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartState;

public class IpsObjectPartStateListTransfer extends ByteArrayTransfer {

    private static IpsObjectPartStateListTransfer INSTANCE = new IpsObjectPartStateListTransfer(null);
    private static final String TYPE_NAME = "IpsObjectPartStateListTransfer"; //$NON-NLS-1$
    private static final int TYPE_ID = registerType(TYPE_NAME);

    private final ClassLoader classLoader;

    public IpsObjectPartStateListTransfer(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static IpsObjectPartStateListTransfer getWriteInstance() {
        return INSTANCE;
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

            List<IpsObjectPartState> states = new ArrayList<IpsObjectPartState>();
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
