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


import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.geronimo.gshell.command.IO;

/**
 * An IO implementation that delegates the Input and Output
 * stream to another IO stored in a ThreadLocal.
 * The reason for this class is that Spring AOP can not proxy
 * fields and GShell always access the in, out and err fields
 * directly, hence the need to wrap these using delegates.
 */
public class ProxyIO extends IO {

    private static final ThreadLocal<IO> TLS_IO = new InheritableThreadLocal<IO>();

    public ProxyIO() {
        super(new ProxyInputStream() {
            protected InputStream getIn() {
                return TLS_IO.get().inputStream;
            }
        }, new ProxyOutputStream() {
            protected OutputStream getOut() {
                return TLS_IO.get().outputStream;
            }
        }, new ProxyOutputStream() {
            protected OutputStream getOut() {
                return TLS_IO.get().errorStream;
            }
        });
    }

    public static void setIO(IO io) {
        TLS_IO.set(io);
    }

    public static IO getIO() {
        return TLS_IO.get();
    }

    protected static abstract class ProxyInputStream extends InputStream {
        public int read() throws IOException {
            return getIn().read();
        }
        public int read(byte b[]) throws IOException {
            return read(b, 0, b.length);
        }
        public int read(byte b[], int off, int len) throws IOException {
            return getIn().read(b, off, len);
        }
        public long skip(long n) throws IOException {
            return getIn().skip(n);
        }
        public int available() throws IOException {
            return getIn().available();
        }
        public void close() throws IOException {
            getIn().close();
        }
        public synchronized void mark(int readlimit) {
            getIn().mark(readlimit);
        }
        public synchronized void reset() throws IOException {
            getIn().reset();
        }
        public boolean markSupported() {
            return getIn().markSupported();
        }
        protected abstract InputStream getIn();
    }

    protected static abstract class ProxyOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            getOut().write(b);
        }
        public void write(byte b[]) throws IOException {
            write(b, 0, b.length);
        }
        public void write(byte b[], int off, int len) throws IOException {
            if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
                throw new IndexOutOfBoundsException();
            for (int i = 0 ; i < len ; i++) {
                write(b[off + i]);
            }
        }
        public void flush() throws IOException {
            getOut().flush();
        }
        public void close() throws IOException {
            try {
                flush();
            } catch (IOException ignored) {
            }
            getOut().close();
        }
        protected abstract OutputStream getOut();
    }
}
