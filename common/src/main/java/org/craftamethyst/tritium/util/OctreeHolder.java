package org.craftamethyst.tritium.util;

import org.craftamethyst.tritium.octree.BoxOctree;

public final class OctreeHolder {
    private static BoxOctree current;

    public static void set(BoxOctree tree) {
        current = tree;
    }

    public static BoxOctree get() {
        return current;
    }

    public static void clear() {
        current = null;
    }
}