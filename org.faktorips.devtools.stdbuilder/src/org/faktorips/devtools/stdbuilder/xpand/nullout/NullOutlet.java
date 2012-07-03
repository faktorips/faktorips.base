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

package org.faktorips.devtools.stdbuilder.xpand.nullout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.xpand2.output.FileHandle;
import org.eclipse.xpand2.output.FileHandleImpl;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xpand2.output.PostProcessor;
import org.eclipse.xpand2.output.VetoException;
import org.eclipse.xpand2.output.VetoStrategy;
import org.eclipse.xpand2.output.VetoStrategy2;

/**
 * This implementation of {@link Outlet} handles String output instead of file outputs. We use the
 * String output to merge and format the generated code with the existing framework. However it
 * could be a very good option to implement a {@link PostProcessor} instead to handle the merging
 * and formatting.
 * 
 * @author dirmeier
 */
public class NullOutlet extends Outlet {

    private int filesCreated = 0;

    private int filesWrittenAndClosed = 0;

    private final Map<IPath, NullFileHandle> nullHandles = new HashMap<IPath, NullFileHandle>();

    public NullOutlet() {
        super(null);
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(final String path) {
        // do nothing
    }

    @Override
    public FileHandle createFileHandle(final String filePath) throws VetoException {
        NullFileHandle nullHandle = new NullFileHandle(this);
        for (VetoStrategy vetoStrategy : vetoStrategies) {
            if (vetoStrategy instanceof VetoStrategy2) {
                if (((VetoStrategy2)vetoStrategy).hasVetoBeforeOpen(nullHandle)) {
                    throw new VetoException(filePath);
                }
            }
        }
        incFilesCreated();
        nullHandles.put(new Path(filePath), nullHandle);
        return nullHandle;
    }

    @Override
    public void addPostprocessor(final PostProcessor b) {
        postprocessors.add(b);
    }

    @Override
    public void addVetoStrategy(final VetoStrategy b) {
        vetoStrategies.add(b);
    }

    @Override
    public void beforeWriteAndClose(final FileHandle impl) {
        for (final Iterator<PostProcessor> iter = postprocessors.iterator(); iter.hasNext();) {
            final PostProcessor b = iter.next();
            b.beforeWriteAndClose(impl);
        }
    }

    @Override
    public void afterClose(final FileHandle impl) {
        incFilesWrittenAndClosed();
        for (final Iterator<PostProcessor> iter = postprocessors.iterator(); iter.hasNext();) {
            final PostProcessor b = iter.next();
            b.afterClose(impl);
        }
    }

    @Override
    public int getFilesCreated() {
        return filesCreated;
    }

    @Override
    public int getFilesWrittenAndClosed() {
        return filesWrittenAndClosed;
    }

    /**
     * Need this implementation because the original implementation in
     * {@link Outlet#shouldWrite(FileHandleImpl)} uses the implementation instead of the interface.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean shouldWrite(FileHandleImpl fileHandleImpl) {
        return shouldWrite((FileHandle)fileHandleImpl);
    }

    public boolean shouldWrite(FileHandle fileHandleImpl) {
        for (VetoStrategy vs : vetoStrategies) {
            if (vs.hasVeto(fileHandleImpl)) {
                return false;
            }
        }
        return true;
    }

}
