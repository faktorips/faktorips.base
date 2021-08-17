/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.bf;

/**
 * Stores an integer width and height.
 */
public class Size {
    private final int width;
    private final int height;

    /**
     * Constructs a {@link Size} object with the supplied width and height values.
     */
    public Size(int w, int h) {
        width = w;
        height = h;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Size) {
            Size s = (Size)o;
            return height == s.height && width == s.width;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (prime + width) + height;
    }

    @Override
    public String toString() {
        return "Size(" + width + ", " + height + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
