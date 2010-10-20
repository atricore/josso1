/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.tooling.gshell.core.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Wrap an InputStream in a complex structure so that it can
 * be reused and closed even when a read() method is blocking.
 *
 * This stream uses a PipedInputStream / PipedOutputStream to
 * decouple the read InputStream.  When the close() method is
 * called, the pipe will be broken, and a single character will
 * be eaten from the reader thread.
 */
public class NoCloseInputStream extends PipedInputStream {

    private final InputStream in;
    private final PipedOutputStream pos;
    private final Thread thread;
    private IOException exception;

    public NoCloseInputStream(InputStream in) throws IOException {
        this.in = in;
        pos = new PipedOutputStream(this);
        thread = new Thread() {
            public void run() {
                doRead();
            }
        };
        thread.start();
    }

    public synchronized int read() throws IOException {
        if (exception != null) {
            throw exception;
        }
        return super.read();
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        if (exception != null) {
            throw exception;
        }
        return super.read(b, off, len);
    }

    public void close() throws IOException {
        super.close();
        pos.close();
        thread.interrupt();
    }

    protected void doRead() {
        try {
            int c;
            while ((c = in.read()) != -1) {
                pos.write(c);
                // Need to notify, else there is a 1 sec lag for the
                // echo to be displayed on the terminal.  The notify
                // will unblock the reader thread.
                synchronized (this) {
                    this.notifyAll();
                }
            }
        } catch (IOException e) {
            exception = e;
            try {
                pos.close();
            } catch (Exception e2) {
                // ignore
            }
        }
    }

}
