package it.multicoredev.mclib.encryption;

import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

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
public class FileEncryption {
    private static final byte[] salt = {(byte) 0x44, (byte) 0x65, (byte) 0x32, (byte) 0xc4, (byte) 0x7a, (byte) 0x3f, (byte) 0x9c, (byte) 0x12};

    /**
     * Encrypts a string and save it encrypted with a password to a file.
     *
     * @param content     The content of the file to be encrypted
     * @param destination The destination file
     * @param password    The password to encrypt the file
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws IOException IOException
     */
    public static void encrypt(@NotNull String content, @NotNull File destination, @NotNull String password) throws GeneralSecurityException, IOException {
        byte[] decData;
        byte[] encData;
        Cipher cipher = makeCipher(true, password);
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        int blockSize = 8;
        int paddedCount = blockSize - (content.getBytes().length % blockSize);
        int padded = content.getBytes().length + paddedCount;

        decData = new byte[padded];
        is.read(decData);
        is.close();

        for (int i = content.getBytes().length; i < padded; ++i) {
            decData[i] = (byte) paddedCount;
        }

        encData = cipher.doFinal(decData);

        FileOutputStream fos = new FileOutputStream(destination);
        fos.write(encData);
        fos.close();
    }


    /**
     * Decrypts a file and return its content.
     *
     * @param source   The source file to decrypt
     * @param password The password to decrypt the file
     * @return The string of the content of the file
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws IOException IOException
     */
    public static String decrypt(@NotNull File source, @NotNull String password) throws GeneralSecurityException, IOException {
        byte[] encData;
        byte[] decData;
        Cipher cipher = makeCipher(false, password);
        FileInputStream fis = new FileInputStream(source);

        encData = new byte[(int) source.length()];
        fis.read(encData);
        fis.close();

        decData = cipher.doFinal(encData);

        int padCount = (int) decData[decData.length -1];
        if(padCount >= 1 && padCount <= 8) {
            decData = Arrays.copyOfRange(decData, 0, decData.length - padCount);
        }

        return new String(decData, StandardCharsets.UTF_8);
    }

    private static Cipher makeCipher(Boolean encryptMode, String password) throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);

        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        if(encryptMode) {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        }

        return cipher;
    }
}
