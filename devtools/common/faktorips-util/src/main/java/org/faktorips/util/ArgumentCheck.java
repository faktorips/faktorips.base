/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.util.Arrays;
import java.util.Collection;

import org.faktorips.annotation.UtilityClass;
import org.w3c.dom.Node;

/**
 * A class that provides static methods to check method arguments, e.g. check if an argument is not
 * null or is an instance of a specific class.
 * <p>
 * All methods have an "optional" context parameter. If the check fails, the context's toString
 * method is called and put into the IllegalArgumentException that is thrown. In this way you can
 * provide some information in which context the check has failed and avoid to create a String
 * object in the default case, when the check passes successfully.
 */
@UtilityClass
public class ArgumentCheck {

    private ArgumentCheck() {
        // Prohibit instantiation.
    }

    /**
     * Checks if the indicated argument is not null.
     * 
     * @param arg the argument to check.
     * 
     * @throws NullPointerException if arg is null.
     */
    public static final void notNull(Object arg) {
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
    public static final void notNull(Object[] arg) {
        notNull((Object)arg);
        Arrays.stream(arg).forEach(ArgumentCheck::notNull);

    }

    /**
     * Checks if the provided array or the objects at the array positions are null. If so a
     * NullPointerException is thrown.
     * 
     * @param arg the argument to check.
     * @param context information, in case the test fails, the context's toString()
     */
    public static final void notNull(Object[] arg, Object context) {
        notNull((Object)arg);
        Arrays.stream(arg).forEach(element -> notNull(element, context));
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
    public static final void notNull(Object arg, Object context) {
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
    public static final void isTrue(boolean arg) {
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
    public static final void isTrue(boolean arg, Object context) {
        if (!arg) {
            throw new IllegalArgumentException(context.toString());
        }
    }

    /**
     * Checks if the indicated arguments are equal.
     * 
     * @throws IllegalArgumentException if arg1.equals(arg2) returns falls.
     */
    public static final void equals(Object arg1, Object arg2) {
        if (!arg1.equals(arg2)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the indicated arguments are equal.
     * 
     * @throws IllegalArgumentException if arg1.equals(arg2) returns falls.
     */
    public static final void equals(Object arg1, Object arg2, Object context) {
        if (!arg1.equals(arg2)) {
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
    public static final void isInstanceOf(Object arg, Class<?> clazz) {
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
    public static final void isInstanceOf(Object arg, Class<?> clazz, Object context) {
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
    public static final void length(Object[] arg, int length) {
        if (arg.length != length) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the given collection has at least the given size.
     * 
     * @param c the collection to check.
     * @param size the expected array length
     * 
     * @throws IllegalArgumentException if the array has not the given length.
     */
    public static final void atLeast(Collection<?> c, int size) {
        if (c.size() < size) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the indicated array has the indicated length.
     * 
     * @param a the array to check.
     * @param length the expected array length
     * @param context an Object whose {@code toString} is used as the message for the
     *            IllegalArgumentException if it is thrown
     * 
     * @throws IllegalArgumentException if the array has not the given length.
     */
    public static final void length(Object[] a, int length, Object context) {
        if (a.length != length) {
            throw new IllegalArgumentException(context.toString());
        }
    }

    /**
     * Checks if the given subclass is a subclass of the given superclass.
     * 
     * @throws IllegalArgumentException if subclass is not a subclass of superclass.
     */
    public static final void isSubclassOf(Class<?> subclass, Class<?> superclass) {
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
    public static final void nodeName(Node node, String name) {
        if (!node.getNodeName().equals(name)) {
            throw new IllegalArgumentException("Node has name " + node.getNodeName() + ", expected " + name + ".");
        }
    }
}
