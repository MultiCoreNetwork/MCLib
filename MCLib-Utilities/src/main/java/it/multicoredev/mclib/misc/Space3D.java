package it.multicoredev.mclib.misc;

import it.multicoredev.mclib.objects.Pos2D;
import it.multicoredev.mclib.objects.Pos3D;

/**
 * Copyright © 2019-2023 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class Space3D {
    /**
     * Return the distance between two points.
     *
     * @param x1 X cord of the first point
     * @param y1 Y cord of the first point
     * @param z1 Z cord of the first point
     * @param x2 X cord of the second point
     * @param y2 Y cord of the second point
     * @param z2 Z cord of the second point
     * @return Distance between the two points
     */
    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) + Math.pow((z2 - z1), 2));
    }

    /**
     * Return the distance between two points.
     *
     * @param pos1 {@link Pos3D} of the first point
     * @param pos2 {@link Pos3D} of the second point
     * @return Distance between two points
     */
    public static double getDistance(Pos3D pos1, Pos3D pos2) {
        return getDistance(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    /**
     * Return the distance between two points ignoring vertical axe.
     *
     * @param x1 X cord of the first point
     * @param z1 Z cord of the first point
     * @param x2 X cord of the second point
     * @param z2 Z cord of the second point
     * @return Distance between two points
     */
    public static double getDistance(double x1, double z1, double x2, double z2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((z2 - z1), 2));
    }

    /**
     * Return the distance between two points ignoring vertical axe.
     *
     * @param pos1 {@link Pos3D} of the first point
     * @param pos2 {@link Pos3D} of the second point
     * @return Distance between two points
     */
    public static double getDistance(Pos2D pos1, Pos2D pos2) {
        return getDistance(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());
    }

    /**
     * Check if a point is in a region between two vertices.
     *
     * @param p1  {@link Pos3D} of the first vertex of the region
     * @param p2  {@link Pos3D} of the second vertex of the region
     * @param pos {@link Pos3D} of the point to check
     * @return True if the point is inside the region (borders included)
     */
    public static boolean isInRegion(Pos3D p1, Pos3D p2, Pos3D pos) {
        return pos.getX() >= Math.min(p1.getX(), p2.getX()) &&
                pos.getX() <= Math.max(p1.getX(), p2.getX()) &&
                pos.getY() >= Math.min(p1.getY(), p2.getY()) &&
                pos.getY() <= Math.min(p1.getY(), p2.getY()) &&
                pos.getZ() >= Math.min(p1.getZ(), p2.getZ()) &&
                pos.getZ() <= Math.max(p1.getZ(), p2.getZ());
    }

    /**
     * Check if a point is in a region between two vertices.
     *
     * @param p1  {@link Pos2D} of the first vertex of the region
     * @param p2  {@link Pos2D} of the second vertex of the region
     * @param pos {@link Pos3D} of the point to check
     * @return True if the point is inside the region (borders included)
     */
    public static boolean isInRegion(Pos2D p1, Pos2D p2, Pos3D pos) {
        return pos.getX() >= Math.min(p1.getX(), p2.getX()) &&
                pos.getX() <= Math.max(p1.getX(), p2.getX()) &&
                pos.getZ() >= Math.min(p1.getZ(), p2.getZ()) &&
                pos.getZ() <= Math.max(p1.getZ(), p2.getZ());
    }

    /**
     * Check if a point is in a region between two vertices.
     *
     * @param p1  {@link Pos2D} of the first vertex of the region
     * @param p2  {@link Pos2D} of the second vertex of the region
     * @param pos {@link Pos2D} of the point to check
     * @return True if the point is inside the region (borders included)
     */
    public static boolean isInRegion(Pos2D p1, Pos2D p2, Pos2D pos) {
        return pos.getX() >= Math.min(p1.getX(), p2.getX()) &&
                pos.getX() <= Math.max(p1.getX(), p2.getX()) &&
                pos.getZ() >= Math.min(p1.getZ(), p2.getZ()) &&
                pos.getZ() <= Math.max(p1.getZ(), p2.getZ());
    }
}
