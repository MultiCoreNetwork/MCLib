package it.multicoredev.mclib.network.protocol;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of MCLib-network.
 * MCLib-network is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
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
public class PacketRegistry {
    private HashMap<Integer, Class<Packet<?>>> packets = new HashMap<>();
    private static PacketRegistry instance;

    private PacketRegistry() {
    }

    /**
     * Gets an instance of the packet registry.
     *
     * @return  Return the instance of the PacketRegistry
     */
    public static PacketRegistry getInstance() {
        if (instance == null) {
            instance = new PacketRegistry();
        }
        return instance;
    }

    /**
     * Register a {@link Packet} in the registry.
     *
     * @param packet {@link Packet} class to register
     */
    public void registerPacket(Class<Packet<?>> packet) {
        if (packets.containsValue(packet)) return;
        packets.put(getFirstId(), packet);
    }

    /**
     * Register {@link Packet} in the registry.
     *
     * @param packets {@link Packet} classes to register
     */
    public void registerPackets(Class<Packet<?>>... packets) {
        for (Class<Packet<?>> packet : packets) {
            registerPacket(packet);
        }
    }

    /**
     * Get a packet class from its id.
     *
     * @param id The id of the {@link Packet}
     * @return The class of the {@link Packet} or null if the id does not exists
     */
    @Nullable
    public Class<Packet<?>> getPacketClass(int id) {
        return packets.get(id);
    }

    /**
     * Get the id of a {@link Packet}.
     *
     * @param packet The packet
     * @return  The id of the {@link Packet} or null if the packet is not registered
     */
    @Nullable
    public Integer getPacketId(Packet<?> packet) {
        for (Map.Entry<Integer, Class<Packet<?>>> entry : packets.entrySet()) {
            if (entry.getValue().equals(packet.getClass())) return entry.getKey();
        }

        return null;
    }

    private int getFirstId() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (!packets.containsKey(i)) return i;
        }
        return -1;
    }
}
