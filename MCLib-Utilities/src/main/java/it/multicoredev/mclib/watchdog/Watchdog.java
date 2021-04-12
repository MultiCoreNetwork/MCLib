package it.multicoredev.mclib.watchdog;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright © 2019-2020 by Lorenzo Magni
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
public class Watchdog implements Runnable {
    private WatchdogListener listener;
    private long millisUntilExpiration;
    private Thread wdThread;
    private Lock expirationDateLock;
    private double wdExpiration = 1;
    private boolean isPaused;

    /**
     * Creates an instance of the Watchdog
     * @param listener called when the watchdog dies
     */
    public Watchdog(WatchdogListener listener) {
        this.listener = listener;
        expirationDateLock = new ReentrantLock();
        millisUntilExpiration = (long) wdExpiration * 1000;
        wdThread = new Thread(this);
        wdThread.start();
    }

    /**
     * Feed the watchdog preventing its death and the expiration of the timer.
     * When this method is called the timer is reset to his starting value.
     */
    public void feed() {
        expirationDateLock.lock();
        millisUntilExpiration = (long) wdExpiration * 1000;
        expirationDateLock.unlock();
    }

    /**
     * Kill the watchdog interrupting its thread.
     */
    public void kill() {
        wdThread.interrupt();
    }

    /**
     * Get the current timer value in seconds.
     *
     * @return The current expiration time in seconds
     */
    public double getTimer() {
        return millisUntilExpiration / 1000.0;
    }


    /**
     * Return the expiration time in seconds.
     *
     * @return The expiration time in seconds
     */
    public double getExpiration() {
        return wdExpiration;
    }

    /**
     * Sets the expiration value in seconds.
     *
     * @param expiration The expiration time in seconds
     */
    public void setExpiration(double expiration) {
        wdExpiration = expiration;
    }

    /**
     * Return true if the watchdog is currently enabled.
     *
     * @return True if enabled, False if disabled
     */
    public boolean isEnabled() {
        return !isPaused;
    }

    /**
     * Enable/Disable the watchdog.
     * When set to false the watchdog is immortal and the timer cannot expire.
     *
     * @param enabled True to enable, False to disable
     */
    public void setEnabled(final boolean enabled) {
        expirationDateLock.lock();
        isPaused = !enabled;
        expirationDateLock.unlock();
    }

    /**
     * Return true if the watchdog thread is alive.
     *
     * @return True if the watchdog is alive, False if the watchdog is dead
     */
    public boolean isAlive() {
        return wdThread.isAlive();
    }

    /**
     * The watchdog thread behavior.
     */
    @Override
    public void run() {
        while(millisUntilExpiration > 0) {
            try {
                expirationDateLock.lock();

                if(!isPaused) {
                    millisUntilExpiration -= 5;
                }
                expirationDateLock.unlock();

                Thread.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
        }

        listener.call();
    }
}
