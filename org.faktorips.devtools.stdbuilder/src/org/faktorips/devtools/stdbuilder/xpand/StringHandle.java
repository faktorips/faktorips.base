/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.internal.xpand2.ast.Statement;
import org.eclipse.xpand2.output.FileHandle;
import org.eclipse.xpand2.output.InsertionPointSupport;
import org.eclipse.xpand2.output.Outlet;

public class StringHandle implements FileHandle, InsertionPointSupport {

    private final StringOutlet outlet;

    private List<CharSequence> buffers = new ArrayList<CharSequence>();

    private Map<Statement, CharSequence> namedBuffers = new HashMap<Statement, CharSequence>();

    private CharSequence currentNamedBuffer = null;

    private CharSequence currentUnnamedBuffer;

    private ByteArrayOutputStream out;

    private final String absolutePath;

    private final String encoding;

    public StringHandle(StringOutlet outlet, String absolutePath, String encoding) {
        this.outlet = outlet;
        this.absolutePath = absolutePath;
        this.encoding = encoding;

        buffers.add(new StringBuilder(4096));
        currentUnnamedBuffer = buffers.get(0);

    }

    @Override
    public Outlet getOutlet() {
        return outlet;
    }

    @Override
    public CharSequence getBuffer() {
        if (!namedBuffers.isEmpty()) {
            return currentNamedBuffer != null ? currentNamedBuffer : currentUnnamedBuffer;
        } else {
            if (buffers.size() > 1) {
                // no insertion point used anymore, but multiple buffers available
                // => compact to one buffer again
                StringBuilder compacted = new StringBuilder();
                for (CharSequence cs : buffers) {
                    compacted.append(cs);
                }
                buffers.clear();
                buffers.add(compacted);
                currentUnnamedBuffer = compacted;
            }
            return buffers.get(0);
        }
    }

    @Override
    public void setBuffer(CharSequence newBuffer) {
        if (currentNamedBuffer != null) {
            int idx = buffers.indexOf(currentNamedBuffer);
            while (idx >= 0) {
                buffers.add(idx, newBuffer);
                buffers.remove(idx + 1);
                idx = buffers.indexOf(currentNamedBuffer);
            }
            for (Statement key : namedBuffers.keySet()) {
                if (namedBuffers.get(key) == currentNamedBuffer) {
                    namedBuffers.put(key, newBuffer);
                }
            }
            currentNamedBuffer = newBuffer;
        } else {
            int idx = buffers.indexOf(currentUnnamedBuffer);
            buffers.add(idx, newBuffer);
            buffers.remove(idx + 1);
            currentUnnamedBuffer = newBuffer;
        }
    }

    @Override
    @Deprecated
    public File getTargetFile() {
        throw new RuntimeException("Getting target file is not supported in StringHandler");
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }

    @Override
    public boolean isAppend() {
        return outlet.isAppend();
    }

    @Override
    public boolean isOverwrite() {
        return outlet.isOverwrite();
    }

    @Override
    public String getFileEncoding() {
        return encoding;
    }

    @Override
    public void writeAndClose() {
        try {
            if (!isOverwrite()) {
                return;
            }
            // create all parent directories
            outlet.beforeWriteAndClose(this);
            try {
                if (outlet.shouldWrite(this)) {
                    if (!isAppend() || out == null) {
                        out = new ByteArrayOutputStream();
                    }
                    out.write(getBytes());
                }
            } finally {
                if (out != null) {
                    try {
                        out.close();
                        outlet.afterClose(this);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getOutput() {
        try {
            if (getFileEncoding() != null) {
                return out.toString(getFileEncoding());
            } else {
                return out.toString();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getBytes() {
        CharSequence buffer = null;
        if (buffers.size() == 1) {
            buffer = buffers.get(0);
        } else {
            StringBuilder tmp = new StringBuilder();
            for (CharSequence cs : buffers) {
                tmp.append(cs);
            }
            buffer = tmp;
        }
        if (getFileEncoding() != null) {
            try {
                return buffer.toString().getBytes(getFileEncoding());
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return buffer.toString().getBytes();
    }

    @Override
    public void activateInsertionPoint(Statement stmt) {
        CharSequence buffer = namedBuffers.get(stmt);
        if (buffer == null) {
            throw new IllegalStateException("Unknown insertion point " + stmt + ".");
        }
        currentNamedBuffer = buffer;
    }

    @Override
    public void deactivateInsertionPoint(Statement stmt) {
        if (currentNamedBuffer == null) {
            throw new IllegalStateException("Insertion point for " + stmt + " was not activated.");
        }
        CharSequence buffer = namedBuffers.get(stmt);
        if (buffer == null) {
            throw new IllegalStateException("Unknown insertion point " + stmt + ".");
        }
        if (buffer != currentNamedBuffer) {
            throw new IllegalStateException("Insertion point " + stmt + " is not the active one!");
        }
        namedBuffers.remove(stmt);
        currentNamedBuffer = null;
    }

    @Override
    public void registerInsertionPoint(Statement stmt) {
        CharSequence namedBuffer = namedBuffers.get(stmt);
        if (namedBuffer == null) {
            namedBuffer = new StringBuilder();
            namedBuffers.put(stmt, namedBuffer);
        }

        buffers.add(namedBuffer);
        currentUnnamedBuffer = new StringBuilder();
        buffers.add(currentUnnamedBuffer);
    }

}
