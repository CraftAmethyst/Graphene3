package org.craftamethyst.tritium.octree;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoxOctree {
    private static final int MAX_DEPTH = 6;
    private static final int MIN_SIZE = 16;
    private static final int MAX_OBJECTS_PER_NODE = 8;

    private final OctreeNode root;
    private final AABB bounds;

    public BoxOctree(AABB bounds) {
        this.bounds = bounds;
        this.root = new OctreeNode(bounds, 0);
    }

    public void addShape(VoxelShape shape, Object owner) {
        AABB shapeBounds = shape.bounds();
        root.addObject(new OctreeObject(shapeBounds, shape, owner));
    }

    public boolean intersects(AABB box) {
        return root.intersects(box);
    }

    public boolean intersects(VoxelShape shape) {
        return root.intersects(shape.bounds());
    }

    public void getIntersectingShapes(AABB box, Consumer<VoxelShape> consumer) {
        root.getIntersectingObjects(box, obj -> consumer.accept(obj.shape));
    }

    public void getNearbyShapes(AABB box, Consumer<VoxelShape> consumer, double maxDistance) {
        AABB expandedBox = box.inflate(maxDistance);
        root.getIntersectingObjects(expandedBox, obj -> consumer.accept(obj.shape));
    }

    public void clear() {
        root.clear();
    }

    private static class OctreeNode {
        private final AABB bounds;
        private final int depth;
        private final List<OctreeObject> objects = new ArrayList<>();
        private OctreeNode[] children;
        private boolean isLeaf = true;

        public OctreeNode(AABB bounds, int depth) {
            this.bounds = bounds;
            this.depth = depth;
        }

        public void addObject(OctreeObject obj) {
            if (isLeaf) {
                objects.add(obj);
                if (objects.size() > MAX_OBJECTS_PER_NODE && depth < MAX_DEPTH &&
                        bounds.getXsize() > MIN_SIZE && bounds.getYsize() > MIN_SIZE && bounds.getZsize() > MIN_SIZE) {
                    split();
                }
            } else {
                int index = getContainingChildIndex(obj.bounds);
                if (index != -1) {
                    children[index].addObject(obj);
                } else {
                    objects.add(obj);
                }
            }
        }

        public boolean intersects(AABB box) {
            if (!bounds.intersects(box)) {
                return false;
            }

            for (OctreeObject obj : objects) {
                if (obj.bounds.intersects(box)) {
                    return true;
                }
            }

            if (!isLeaf) {
                for (OctreeNode child : children) {
                    if (child.intersects(box)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public void getIntersectingObjects(AABB box, Consumer<OctreeObject> consumer) {
            if (!bounds.intersects(box)) {
                return;
            }

            for (OctreeObject obj : objects) {
                if (obj.bounds.intersects(box)) {
                    consumer.accept(obj);
                }
            }

            if (!isLeaf) {
                for (OctreeNode child : children) {
                    child.getIntersectingObjects(box, consumer);
                }
            }
        }

        private void split() {
            children = new OctreeNode[8];
            double halfX = bounds.getXsize() / 2.0;
            double halfY = bounds.getYsize() / 2.0;
            double halfZ = bounds.getZsize() / 2.0;

            double minX = bounds.minX;
            double minY = bounds.minY;
            double minZ = bounds.minZ;

            for (int i = 0; i < 8; i++) {
                double childMinX = minX + ((i & 1) * halfX);
                double childMinY = minY + ((i & 2) / 2 * halfY);
                double childMinZ = minZ + ((i & 4) / 4 * halfZ);

                AABB childBounds = new AABB(
                        childMinX, childMinY, childMinZ,
                        childMinX + halfX, childMinY + halfY, childMinZ + halfZ
                );

                children[i] = new OctreeNode(childBounds, depth + 1);
            }

            // Move objects to children
            List<OctreeObject> objectsToKeep = new ArrayList<>();
            for (OctreeObject obj : objects) {
                int index = getContainingChildIndex(obj.bounds);
                if (index != -1) {
                    children[index].addObject(obj);
                } else {
                    objectsToKeep.add(obj);
                }
            }

            objects.clear();
            objects.addAll(objectsToKeep);
            isLeaf = false;
        }

        private int getContainingChildIndex(AABB objBounds) {
            if (children == null) return -1;

            for (int i = 0; i < 8; i++) {
                if (children[i].bounds.contains(objBounds.minX, objBounds.minY, objBounds.minZ) &&
                        children[i].bounds.contains(objBounds.maxX, objBounds.maxY, objBounds.maxZ)) {
                    return i;
                }
            }
            return -1;
        }

        public void clear() {
            objects.clear();
            if (!isLeaf) {
                for (OctreeNode child : children) {
                    child.clear();
                }
                children = null;
                isLeaf = true;
            }
        }
    }

    private record OctreeObject(AABB bounds, VoxelShape shape, Object owner) {
    }
}