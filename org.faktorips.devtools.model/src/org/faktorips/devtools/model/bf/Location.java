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
 * Represents a location in 2-dimensional space (x, y).
 */
public class Location {
    private final int x;
    private final int y;

    /**
     * Constructs a {@link Location} at (0,0).
     */
    public Location() {
        this(0, 0);
    }

    /**
     * Constructs a {@link Location} with the same coordinates as the given {@link Location}.
     */
    public Location(Location p) {
        this(p.x, p.y);
    }

    /**
     * Constructs a {@link Location} with the specified x and y.
     */
    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Location) {
            Location l = (Location)o;
            return x == l.x && y == l.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (prime + x) + y;
    }

    @Override
    public String toString() {
        return "Location(" + x + ", " + y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
