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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.internal.xpand2.ast.Statement;
import org.eclipse.internal.xpand2.ast.TextStatement;
import org.eclipse.internal.xtend.expression.ast.SyntaxElement;
import org.eclipse.internal.xtend.util.Pair;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xpand2.output.FileHandle;
import org.eclipse.xpand2.output.InsertionPointSupport;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xpand2.output.Output;
import org.eclipse.xpand2.output.OutputImpl;

/**
 * This class is handles the output of for the xpand template generator. It was originally written
 * by Sven Efftinge and is was a copy of {@link OutputImpl} to handle the following code deficits:
 * <li>There was no possibility to change the default {@link Outlet}. We need to change the outlet
 * because of possibly different encodings</li>
 * 
 */
public class StringOutput implements Output, InsertionPointSupport {

    private final static Pattern p = Pattern.compile("(.+)://(.+)");

    private boolean automaticHyphenation = false;

    private final Stack<SyntaxElement> s = new Stack<SyntaxElement>();

    private static ThreadLocal<Stack<FileHandle>> fileHandles = new ThreadLocal<Stack<FileHandle>>();
    private boolean deleteLine = false;
    // private static ThreadLocal<>

    private final Map<String, Outlet> outlets = new HashMap<String, Outlet>();

    private Pair<Outlet, String> resolveOutlet(final Map<String, Outlet> allOutlets, String path, String outletName) {
        if (outletName == null) {
            final Matcher m = p.matcher(path);
            if (m.matches()) {
                outletName = m.group(1);
                path = m.group(2);
            }
        }
        final Outlet o = allOutlets.get(outletName);
        if (o == null) {
            if (outletName == null) {
                throw new IllegalArgumentException("No default outlet was configured!");
            } else {
                throw new IllegalArgumentException("No outlet with the name " + outletName + " could be found!");
            }
        }

        return new Pair<Outlet, String>(o, path);
    }

    public void setAutomaticHyphens(final boolean automaticHyphenation) {
        this.automaticHyphenation = automaticHyphenation;
    }

    @Override
    public void addOutlet(final Outlet outlet) {
        /*
         * This block was originally in the code from Sven Efftinge but we need exactly to overwrite
         * an existing outlet!
         * 
         * if (outlets.containsKey(outlet.getName())) { if (outlet.getName() == null) { throw new
         * IllegalArgumentException("A default outlet is already registered!"); } else { throw new
         * IllegalArgumentException("An outlet with name " + outlet.getName() +
         * " is already registered!"); } }
         */
        outlets.put(outlet.getName(), outlet);
    }

    @Override
    public Outlet getOutlet(final String name) {
        return outlets.get(name);
    }

    protected FileHandle current() {
        return getFileHandles().isEmpty() ? null : getFileHandles().peek();
    }

    @Override
    public void write(final String bytes) {
        if (current() != null) {
            if (deleteLine) {
                final String temp = trimUntilNewline(bytes);
                removeWSAfterLastNewline(current().getBuffer());
                try {
                    ((Appendable)current().getBuffer()).append(temp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    ((Appendable)current().getBuffer()).append(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        deleteLine = false;
    }

    public void removeWSAfterLastNewline(final CharSequence cs) {
        int i = cs.length();
        boolean wsOnly = true;
        for (; i > 0 && wsOnly; i--) {
            final char c = cs.charAt(i - 1);
            wsOnly = Character.isWhitespace(c);
            if (wsOnly && isNewLine(c)) {
                deleteFromCharSequence(cs, i, cs.length());
                return;
            }
        }
        return;
    }

    private void deleteFromCharSequence(CharSequence cs, int start, int end) {
        if (cs instanceof StringBuilder) {
            ((StringBuilder)cs).delete(start, end);
        } else if (cs instanceof StringBuffer) {
            ((StringBuffer)cs).delete(start, end);
        } else {
            throw new IllegalArgumentException("Unsupported CharSequence type " + cs.getClass().getName());
        }
    }

    protected boolean isNewLine(final char c) {
        return c == '\n' || c == '\r';
    }

    public String trimUntilNewline(final String bytes) {
        int i = 0;
        boolean wsOnly = true;
        for (; i < bytes.length() && wsOnly; i++) {
            final char c = bytes.charAt(i);
            wsOnly = Character.isWhitespace(c);
            if (wsOnly && isNewLine(c)) {
                if (c == '\r' && i + 1 < bytes.length() && bytes.charAt(i + 1) == '\n') {
                    i++;
                }
                return bytes.substring(i + 1);
            }
        }
        return bytes;
    }

    @Override
    public void openFile(final String path, final String outletName) {
        final Pair<Outlet, String> raw = resolveOutlet(outlets, path, outletName);

        final Outlet actualOutlet = raw.getFirst();
        final String actualPath = raw.getSecond();

        getFileHandles().push(actualOutlet.createFileHandle(actualPath));
    }

    @Override
    public void closeFile() {
        final FileHandle fi = getFileHandles().pop();
        fi.writeAndClose();
    }

    @Override
    public void pushStatement(final SyntaxElement stmt, final XpandExecutionContext ctx) {
        if (stmt instanceof TextStatement) {
            deleteLine = ((TextStatement)stmt).isDeleteLine();
            if (automaticHyphenation) {
                deleteLine = true;
            }
        }
        s.push(stmt);
    }

    @Override
    public SyntaxElement popStatement() {
        final SyntaxElement se = s.pop();
        return se;
    }

    protected Stack<FileHandle> getFileHandles() {
        Stack<FileHandle> result = fileHandles.get();
        if (result == null) {
            result = new Stack<FileHandle>();
            fileHandles.set(result);
        }
        return result;
    }

    @Override
    public void activateInsertionPoint(Statement stmt) {
        if (current() != null) {
            if (!(current() instanceof InsertionPointSupport)) {
                throw new IllegalStateException("Current handle does not implement InsertionPointSupport.");
            }
            ((InsertionPointSupport)current()).activateInsertionPoint(stmt);
        }
    }

    @Override
    public void deactivateInsertionPoint(Statement stmt) {
        if (current() != null) {
            if (!(current() instanceof InsertionPointSupport)) {
                throw new IllegalStateException("Current handle does not implement InsertionPointSupport.");
            }
            ((InsertionPointSupport)current()).deactivateInsertionPoint(stmt);
        }
    }

    @Override
    public void registerInsertionPoint(Statement stmt) {
        if (!(current() instanceof InsertionPointSupport)) {
            throw new IllegalStateException("Current handle does not implement InsertionPointSupport.");
        }
        ((InsertionPointSupport)current()).registerInsertionPoint(stmt);
    }

}