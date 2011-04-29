/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.util;

import org.w3c.dom.Node;

/**
 * A class that provides static methods to check method arguments, e.g. check if an argument is not
 * null or is an instance of a specific class.
 * <p>
 * All methods have an "optional" context parameter. If the check fails, the context's toString
 * method is called and put into the IllegalArgumentException that is thrown. In this way you can
 * provide some information in which context the check has failed and avoid to create a String
 * object in the default case, when the check passes successfully.
 * 
 * @author Jan Ortmann
 */
public class ArgumentCheck {

    /**
     * Checks if the indicated argument is not null.
     * 
     * @param arg the argument to check.
     * 
     * @throws NullPointerException if arg is null.
     */
    public final static void notNull(Object arg) {
        if (arg == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Checks if the provided array or one of the objects at the array's positions is null. If so a
     * NullPointerException is thrown.
     * 
     * @param arg the argument to check.
     */
    public final static void notNull(Object[] arg) {
        notNull((Object)arg);
        for (Object element : arg) {
            notNull(element);
        }
    }

    /**
     * Checks if the provided array or the objects at the array positions are null. If so a
     * NullPointerException is thrown.
     * 
     * @param arg the argument to check.
     * @param context information, in case the test fails, the context's toString()
     */
    public final static void notNull(Object[] arg, Object context) {
        notNull((Object)arg);
        for (Object element : arg) {
            notNull(element, context);
        }
    }

    /**
     * Checks if the indicated argument is not null.
     * 
     * @param arg the argument to check.
     * @param context context information, in case the test fails, the context's toString() method
     *            is called and the result passed to the NullPointerException.
     * 
     * @throws NullPointerException if arg is null.
     */
    public final static void notNull(Object arg, Object context) {
        if (arg == null) {
            throw new NullPointerException("" + context);
        }
    }

    /**
     * Checks if the indicated argument is true.
     * 
     * @param arg the argument to check.
     * 
     * @throws NullPointerException if arg is null.
     */
    public final static void isTrue(boolean arg) {
        if (!arg) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the indicated argument is true.
     * 
     * @param arg the argument to check.
     * 
     * @throws NullPointerException if arg is null.
     */
    public final static void isTrue(boolean arg, Object context) {
        if (!arg) {
            throw new IllegalArgumentException(context.toString());
        }
    }

    /**
     * Checks if the indicated arguments are equal.
     * 
     * @throws IllegalArgumentException if arg1.equals(arg2) returns falls.
     */
    public final static void equals(Object arg1, Object arg2) {
        if (arg1.equals(arg2)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the indicated arguments are equal.
     * 
     * @throws IllegalArgumentException if arg1.equals(arg2) returns falls.
     */
    public final static void equals(Object arg1, Object arg2, Object context) {
        if (arg1.equals(arg2)) {
            throw new IllegalArgumentException(context.toString());
        }
    }

    /**
     * Checks if the indicated argument is an instance of the indicated class.
     * 
     * @param arg the argument to check.
     * 
     * @throws IllegalArgumentException if arg is null.
     */
    public final static void isInstanceOf(Object arg, Class<?> clazz) {
        if (!clazz.isAssignableFrom(arg.getClass())) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the indicated argument is an instance of the indicated class.
     * 
     * @param arg the argument to check.
     * 
     * @throws IllegalArgumentException if arg is null.
     */
    public final static void isInstanceOf(Object arg, Class<?> clazz, Object context) {
        if (!clazz.isAssignableFrom(arg.getClass())) {
            throw new IllegalArgumentException(context.toString());
        }
    }

    /**
     * Checks if the indicated array has the indicated length.
     * 
     * @param arg the array to check.
     * @param length the expected array length
     * 
     * @throws IllegalArgumentException if the array has not the given length.
     */
    public final static void length(Object[] arg, int length) {
        if (arg.length != length) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the indicated array has the indicated length.
     * 
     * @param arg the array to check.
     * @param length the expected array length
     * 
     * @throws IllegalArgumentException if the array has not the given length.
     */
    public final static void length(Object[] arg, int length, Object context) {
        if (arg.length != length) {
            throw new IllegalArgumentException(context.toString());
        }
    }

    /**
     * Checks if the given subclass is a subclass of the given superclass.
     * 
     * @throws IllegalArgumentException if subclass is not a subclass of superclass.
     */
    public final static void isSubclassOf(Class<?> subclass, Class<?> superclass) {
        if (!superclass.isAssignableFrom(subclass)) {
            throw new IllegalArgumentException(subclass + " is not a subclass of " + superclass);
        }
    }

    /**
     * Checks if the given xml node has the given name.
     * 
     * @throws IllegalArgumentException if the node has not the expected name.
     * @throws NullPointerException if node is <code>null</code>.
     */
    public final static void nodeName(Node node, String name) {
        if (!node.getNodeName().equals(name)) {
            throw new IllegalArgumentException("Node has name " + node.getNodeName() + ", expected " + name + ".");
        }
    }

    private ArgumentCheck() {
        // Prohibit instantiation.
    }

}
