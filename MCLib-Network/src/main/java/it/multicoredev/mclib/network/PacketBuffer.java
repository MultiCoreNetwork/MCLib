package it.multicoredev.mclib.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
public class PacketBuffer {
    private ByteBuf buf;

    public PacketBuffer(@NotNull ByteBuf buf) {
        this.buf = buf;
    }

    public ByteBuf buf() {
        return buf;
    }

    public String readString(Charset charset) {
        int len = readInt();
        return readBytes(len).toString();
    }

    public String readString() {
        return readString(StandardCharsets.UTF_8);
    }

    public PacketBuffer writeString(String str) {
        byte[] bytes = str.getBytes();
        writeInt(bytes.length);
        writeBytes(bytes);
        return this;
    }

    public int capacity() {
        return buf.capacity();
    }

    public PacketBuffer capacity(int newCapacity) {
        buf.capacity(newCapacity);
        return this;
    }

    public int maxCapacity() {
        return buf.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return buf.alloc();
    }

    public PacketBuffer unwrap() {
        buf.unwrap();
        return this;
    }

    public boolean isDirect() {
        return buf.isDirect();
    }

    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    public PacketBuffer asReadOnly() {
        buf.asReadOnly();
        return this;
    }

    public int readerIndex() {
        return buf.readerIndex();
    }

    public PacketBuffer readerIndex(int readerIndex) {
        buf.readerIndex(readerIndex);
        return this;
    }

    public int writerIndex() {
        return buf.writerIndex();
    }

    public PacketBuffer writerIndex(int writerIndex) {
        buf.writerIndex(writerIndex);
        return this;
    }

    public PacketBuffer setIndex(int readerIndex, int writerIndex) {
        buf.setInt(readerIndex, writerIndex);
        return this;
    }

    public int readableBytes() {
        return buf.readableBytes();
    }

    public int writableBytes() {
        return buf.writableBytes();
    }

    public int maxWritableBytes() {
        return buf.maxWritableBytes();
    }

    public int maxFastWritableBytes() {
        return buf.writableBytes();
    }

    public boolean isReadable() {
        return buf.isReadable();
    }

    public boolean isReadable(int size) {
        return buf.isReadable(size);
    }

    public boolean isWritable() {
        return buf.isWritable();
    }

    public boolean isWritable(int size) {
        return buf.isWritable(size);
    }

    public PacketBuffer clear() {
        buf.clear();
        return this;
    }

    public PacketBuffer markReaderIndex() {
        buf.markReaderIndex();
        return this;
    }

    public PacketBuffer resetReaderIndex() {
        buf.resetReaderIndex();
        return this;
    }

    public PacketBuffer markWriterIndex() {
        buf.markWriterIndex();
        return this;
    }

    public PacketBuffer resetWriterIndex() {
        buf.resetWriterIndex();
        return this;
    }

    public PacketBuffer discardReadBytes() {
        buf.discardReadBytes();
        return this;
    }

    public PacketBuffer discardSomeReadBytes() {
        buf.discardSomeReadBytes();
        return this;
    }

    public PacketBuffer ensureWritable(int minWriteableBytes) {
        buf.ensureWritable(minWriteableBytes);
        return this;
    }

    public int ensureWritable(int minWriteableBytes, boolean force) {
        return buf.ensureWritable(minWriteableBytes, force);
    }

    public boolean getBoolean(int index) {
        return buf.getBoolean(index);
    }

    public byte getByte(int index) {
        return buf.getByte(index);
    }

    public short getUnsignedByte(int index) {
        return buf.getUnsignedByte(index);
    }

    public short getShort(int index) {
        return buf.getShort(index);
    }

    public short getShortLE(int index) {
        return buf.getShortLE(index);
    }

    public int getUnsignedShort(int index) {
        return buf.getUnsignedShort(index);
    }

    public int getUnsignedShortLE(int index) {
        return buf.getUnsignedShortLE(index);
    }

    public int getMedium(int index) {
        return buf.getMedium(index);
    }

    public int getMediumLE(int index) {
        return buf.getMediumLE(index);
    }

    public int getUnsignedMedium(int index) {
        return buf.getUnsignedMedium(index);
    }

    public int getUnsignedMediumLE(int index) {
        return buf.getUnsignedMediumLE(index);
    }

    public int getInt(int index) {
        return buf.getInt(index);
    }

    public int getIntLE(int index) {
        return buf.getIntLE(index);
    }

    public long getUnsignedInt(int index) {
        return buf.getUnsignedInt(index);
    }

    public long getUnsignedIntLE(int index) {
        return buf.getUnsignedIntLE(index);
    }

    public long getLong(int index) {
        return buf.getLong(index);
    }

    public long getLongLE(int index) {
        return buf.getLongLE(index);
    }

    public char getChar(int index) {
        return buf.getChar(index);
    }

    public float getFloat(int index) {
        return buf.getFloat(index);
    }

    public float getFloatLE(int index) {
        return Float.intBitsToFloat(buf.getIntLE(index));
    }

    public double getDouble(int index) {
        return buf.getDouble(index);
    }

    public double getDoubleLE(int index) {
        return Double.longBitsToDouble(buf.getLongLE(index));
    }

    public PacketBuffer getBytes(int index, ByteBuf dst) {
        buf.getBytes(index, dst);
        return this;
    }

    public PacketBuffer getBytes(int index, ByteBuf dst, int length) {
        buf.getBytes(index, dst, length);
        return this;
    }

    public PacketBuffer getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        buf.getBytes(index, dst, dstIndex, length);
        return this;
    }

    public PacketBuffer getBytes(int index, byte[] dst) {
        buf.getBytes(index, dst);
        return this;
    }

    public PacketBuffer getBytes(int index, byte[] dst, int dstIndex, int legth) {
        buf.getBytes(index, dst, dstIndex, legth);
        return this;
    }

    public PacketBuffer getBytes(int index, ByteBuffer dst) {
        buf.getBytes(index, dst);
        return this;
    }

    public PacketBuffer getBytes(int index, OutputStream out, int length) throws IOException {
        buf.getBytes(index, out, length);
        return this;
    }

    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return buf.getBytes(index, out, position, length);
    }

    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return buf.getCharSequence(index, length, charset);
    }

    public PacketBuffer setBoolean(int index, boolean value) {
        buf.setBoolean(index, value);
        return this;
    }

    public PacketBuffer setByte(int index, int value) {
        buf.setByte(index, value);
        return this;
    }

    public PacketBuffer setShort(int index, int value) {
        buf.setShort(index, value);
        return this;
    }

    public PacketBuffer setShortLE(int index, int value) {
        buf.setShortLE(index, value);
        return this;
    }

    public PacketBuffer setMedium(int index, int value) {
        buf.setMedium(index, value);
        return this;
    }

    public PacketBuffer setMediumLE(int index, int value) {
        buf.setMediumLE(index, value);
        return this;
    }

    public PacketBuffer setInt(int index, int value) {
        buf.setInt(index, value);
        return this;
    }

    public PacketBuffer setIntLE(int index, int value) {
        buf.setIntLE(index, value);
        return this;
    }

    public PacketBuffer setLong(int index, long value) {
        buf.setLong(index, value);
        return this;
    }

    public PacketBuffer setLongLE(int index, long value) {
        buf.setLongLE(index, value);
        return this;
    }

    public PacketBuffer setChar(int index, int value) {
        buf.setChar(index, value);
        return this;
    }

    public PacketBuffer setFloat(int index, float value) {
        buf.setFloat(index, value);
        return this;
    }

    public PacketBuffer setFloatLE(int index, float value) {
        buf.setIntLE(index, Float.floatToRawIntBits(value));
        return this;
    }

    public PacketBuffer setDouble(int index, double value) {
        buf.setDouble(index, value);
        return this;
    }

    public PacketBuffer setDoubleLE(int index, double value) {
        buf.setLongLE(index, Double.doubleToRawLongBits(value));
        return this;
    }

    public PacketBuffer setBytes(int index, ByteBuf src) {
        buf.setBytes(index, src);
        return this;
    }

    public PacketBuffer setBytes(int index, ByteBuf src, int length) {
        buf.setBytes(index, src, length);
        return this;
    }

    public PacketBuffer setBytes(int index, ByteBuf src, int srcIndex, int length) {
        buf.setBytes(index, src, srcIndex, length);
        return this;
    }

    public PacketBuffer setBytes(int index, byte[] src) {
        buf.setBytes(index, src);
        return this;
    }

    public PacketBuffer setBytes(int index, byte[] src, int srcIndex, int length) {
        buf.setBytes(index, src, srcIndex, length);
        return this;
    }

    public PacketBuffer setBytes(int index, ByteBuffer stc) {
        buf.setBytes(index, stc);
        return this;
    }

    public int setBytes(int index, InputStream in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return buf.setBytes(index, in, position, length);
    }

    public PacketBuffer setZero(int index, int length) {
        buf.setZero(index, length);
        return this;
    }

    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return buf.setCharSequence(index, sequence, charset);
    }

    public boolean readBoolean() {
        return buf.readBoolean();
    }

    public byte readByte() {
        return buf.readByte();
    }

    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    public short readShort() {
        return buf.readShort();
    }

    public short readShortLE() {
        return buf.readShortLE();
    }

    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return buf.readUnsignedShortLE();
    }

    public int readMedium() {
        return buf.readMedium();
    }

    public int readMediumLE() {
        return buf.readMediumLE();
    }

    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return buf.readUnsignedMediumLE();
    }

    public int readInt() {
        return buf.readInt();
    }

    public int readIntLE() {
        return buf.readIntLE();
    }

    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return buf.readUnsignedIntLE();
    }

    public long readLong() {
        return buf.readLong();
    }

    public long readLongLE() {
        return buf.readLongLE();
    }

    public char readChar() {
        return buf.readChar();
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    public double readDouble() {
        return buf.readDouble();
    }

    public double readDoubleLE() {
        return Double.longBitsToDouble(buf.readLongLE());
    }

    public PacketBuffer readBytes(int length) {
        buf.readBytes(length);
        return this;
    }

    public PacketBuffer readSlice(int length) {
        buf.readSlice(length);
        return this;
    }

    public PacketBuffer readRetainedSlice(int length) {
        buf.readRetainedSlice(length);
        return this;
    }

    public PacketBuffer readBytes(ByteBuf dst) {
        buf.readBytes(dst);
        return this;
    }

    public PacketBuffer readBytes(ByteBuf dst, int length) {
        buf.readBytes(dst, length);
        return this;
    }

    public PacketBuffer readBytes(ByteBuf dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
        return this;
    }

    public PacketBuffer readBytes(byte[] dst) {
        buf.readBytes(dst);
        return this;
    }

    public PacketBuffer readBytes(byte[] dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
        return this;
    }

    public PacketBuffer readBytes(ByteBuffer dst) {
        buf.readBytes(dst);
        return this;
    }

    public PacketBuffer readBytes(OutputStream out, int length) throws IOException {
        buf.readBytes(out, length);
        return this;
    }

    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    public CharSequence readCharSequence(int length, Charset charset) {
        return buf.readCharSequence(length, charset);
    }

    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return buf.readBytes(out, position, length);
    }

    public PacketBuffer skipBytes(int length) {
        buf.skipBytes(length);
        return this;
    }

    public PacketBuffer writeBoolean(boolean value) {
        buf.writeBoolean(value);
        return this;
    }

    public PacketBuffer writeByte(int value) {
        buf.writeByte(value);
        return this;
    }

    public PacketBuffer writeShort(int value) {
        buf.writeShort(value);
        return this;
    }

    public PacketBuffer writeShortLE(int value) {
        buf.writeShortLE(value);
        return this;
    }

    public PacketBuffer writeMedium(int value) {
        buf.writeMedium(value);
        return this;
    }

    public PacketBuffer writeMediumLE(int value) {
        buf.writeMediumLE(value);
        return this;
    }

    public PacketBuffer writeInt(int value) {
        buf.writeInt(value);
        return this;
    }

    public PacketBuffer writeIntLE(int value) {
        buf.writeIntLE(value);
        return this;
    }

    public PacketBuffer writeLong(long value) {
        buf.writeLong(value);
        return this;
    }

    public PacketBuffer writeLongLE(long value) {
        buf.writeLongLE(value);
        return this;
    }

    public PacketBuffer writeChar(int value) {
        buf.writeChar(value);
        return this;
    }

    public PacketBuffer writeFloat(float value) {
        buf.writeFloat(value);
        return this;
    }

    public PacketBuffer writeFloatLE(float value) {
        buf.writeIntLE(Float.floatToRawIntBits(value));
        return this;
    }

    public PacketBuffer writeDouble(double value) {
        buf.writeDouble(value);
        return this;
    }

    public PacketBuffer writeDoubleLE(double value) {
        buf.writeLongLE(Double.doubleToRawLongBits(value));
        return this;
    }

    public PacketBuffer writeBytes(ByteBuf src) {
        buf.writeBytes(src);
        return this;
    }

    public PacketBuffer writeBytes(ByteBuf src, int length) {
        buf.writeBytes(src, length);
        return this;
    }

    public PacketBuffer writeBytes(ByteBuf src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
        return this;
    }

    public PacketBuffer writeBytes(byte[] src) {
        buf.writeBytes(src);
        return this;
    }

    public PacketBuffer writeBytes(byte[] src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
        return this;
    }

    public PacketBuffer writeBytes(ByteBuffer src) {
        buf.writeBytes(src);
        return this;
    }

    public int writeBytes(InputStream in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return buf.writeBytes(in, position, length);
    }

    public PacketBuffer writeZero(int length) {
        buf.writeZero(length);
        return this;
    }

    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return buf.writeCharSequence(sequence, charset);
    }

    public int indexOf(int fromIndex, int toIndex, byte value) {
        return buf.indexOf(fromIndex, toIndex, value);
    }

    public int bytesBefore(byte value) {
        return buf.bytesBefore(value);
    }

    public int bytesBefore(int length, byte value) {
        return buf.bytesBefore(length, value);
    }

    public int bytesBefore(int index, int length, byte value) {
        return buf.bytesBefore(index, length, value);
    }

    public int forEachByte(ByteProcessor processor) {
        return buf.forEachByte(processor);
    }

    public int forEachByte(int index, int length, ByteProcessor processor) {
        return buf.forEachByte(index, length, processor);
    }

    public int forEachByteDesc(ByteProcessor processor) {
        return buf.forEachByteDesc(processor);
    }

    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return buf.forEachByteDesc(index, length, processor);
    }

    public PacketBuffer copy() {
        return new PacketBuffer(buf.copy());
    }

    public PacketBuffer copy(int index, int length) {
        return new PacketBuffer(buf.copy());
    }

    public PacketBuffer slice() {
        buf.slice();
        return this;
    }

    public PacketBuffer retainedSlice() {
        buf.retainedSlice();
        return this;
    }

    public PacketBuffer slice(int index, int length) {
        buf.slice(index, length);
        return this;
    }

    public PacketBuffer retainedSlice(int index, int length) {
        buf.retainedSlice(index, length);
        return this;
    }

    public PacketBuffer duplicate() {
        buf.duplicate();
        return this;
    }

    public PacketBuffer retainedDuplicate() {
        buf.retainedDuplicate();
        return this;
    }

    public int nioBufferCount() {
        return buf.nioBufferCount();
    }

    public PacketBuffer nioBuffer() {
        buf.nioBuffer();
        return this;
    }

    public PacketBuffer nioBuffer(int index, int length) {
        buf.nioBuffer(index, length);
        return this;
    }

    public PacketBuffer internalNioBuffer(int index, int length) {
        buf.internalNioBuffer(index, length);
        return this;
    }

    public ByteBuffer[] nioBuffers() {
        return buf.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int index, int length) {
        return buf.nioBuffers(index, length);
    }

    public boolean hasArray() {
        return buf.hasArray();
    }

    public byte[] array() {
        return buf.array();
    }

    public int arrayOffset() {
        return buf.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }

    public long memoryAddress() {
        return buf.memoryAddress();
    }

    public boolean isContiguous() {
        return buf.isContiguous();
    }

    public String toString(Charset charset) {
        return buf.toString(charset);
    }

    public String toString(int index, int length, Charset charset) {
        return buf.toString(index, length, charset);
    }

    public int hashCode() {
        return buf.hashCode();
    }

    public boolean equals(Object obj) {
        return buf.equals(obj);
    }

    public int compareTo(ByteBuf buf) {
        return this.buf.compareTo(buf);
    }

    public String toString() {
        return buf.toString();
    }

    public PacketBuffer retain(int increment) {
        buf.retain(increment);
        return this;
    }

    public PacketBuffer retain() {
        buf.retain();
        return this;
    }

    public PacketBuffer touch() {
        buf.touch();
        return this;
    }

    public PacketBuffer touch(Object var1) {
        buf.touch(var1);
        return this;
    }

    boolean isAccessible() {
        return buf.refCnt() != 0;
    }
}
