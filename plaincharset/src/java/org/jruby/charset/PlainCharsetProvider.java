package org.jruby.charset;

import java.util.Iterator;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;

public class PlainCharsetProvider extends CharsetProvider {
    public Charset charsetForName(final String name) {
        if(name.compareToIgnoreCase("plain") == 0) {
            return new PlainCharset();
        }
        return null;
    }
    private static class CharsetIterator implements Iterator {
        private boolean next = true;
        public boolean hasNext() {
            return next;
        }
        
        public Object next() {
            next = false;
            return new PlainCharset();
        }
        public void remove() {
        }
    }
    
    public Iterator charsets() {
        return new CharsetIterator();
    }
}
