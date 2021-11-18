package org.codejive.properties;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class PropertiesParser {

    public enum Type {
        KEY,
        SEPARATOR,
        VALUE,
        COMMENT,
        WHITESPACE
    }

    public static class Token {
        final Type type;
        final String raw;
        final String text;

        Token(Type type, String raw) {
            this(type, raw, null);
        }

        Token(Type type, String raw, String text) {
            this.type = type;
            this.raw = raw;
            this.text = text;
        }

        public Type getType() {
            return type;
        }

        public String getRaw() {
            return raw;
        }

        public String getText() {
            return text != null ? text : raw;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Token)) return false;
            Token token = (Token) o;
            return type == token.type && raw.equals(token.raw) && Objects.equals(text, token.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, raw, text);
        }

        @Override
        public String toString() {
            if (text == null) {
                return "Token(" + type + ", '" + raw + "')";
            } else {
                return "Token(" + type + ", '" + raw + "'" + ", '" + text + "')";
            }
        }
    }

    private final Reader rdr;

    private Type state;
    private int ch;
    private int pch;
    StringBuilder chr;
    StringBuilder str;
    boolean hasEscapes;

    public PropertiesParser(Reader rdr) throws IOException {
        this.rdr = rdr;
        state = null;
        chr = new StringBuilder();
        str = new StringBuilder();
        pch = -1;
        nextChar();
    }

    public Token nextToken() throws IOException {
        Supplier<Boolean> isValid = () -> false;
        Type nextState = null;
        while (true) {
            if (state == null) {
                if (isCommentChar(ch)) {
                    state = Type.COMMENT;
                    isValid = () -> !isEol(ch);
                    nextState = null;
                } else if (isWhitespaceChar(ch)) {
                    state = Type.WHITESPACE;
                    isValid = () -> isWhitespaceChar(ch);
                    nextState = null;
                } else if (isEof(ch)) {
                    return null;
                } else {
                    state = Type.KEY;
                    isValid = () -> !isSeparatorChar(ch);
                    nextState = Type.SEPARATOR;
                }
            } else if (state == Type.SEPARATOR) {
                isValid = () -> isSeparatorChar(ch);
                nextState = Type.VALUE;
            } else if (state == Type.VALUE) {
                isValid = () -> !isEol(ch);
                nextState = null;
            }
            if (isValid.get()) {
                nextChar();
            } else {
                String text = (state == Type.VALUE || state == Type.COMMENT) ? trimmedString() : string();
                Token token = hasEscapes ? new Token(state, text, unescape(text)) :  new Token(state, text);
                hasEscapes = false;
                state = nextState;
                return token;
            }
        }
    }

    private void nextChar() throws IOException {
        str.append(chr);
        if (chr.length() > 0 && chr.charAt(0) == '\\') {
            hasEscapes = true;
        }
        if (pch == -1) {
            ch = rdr.read();
        } else {
            ch = pch;
            pch = -1;
        }
        chr.setLength(0);
        chr.append((char) ch);
        if (ch == '\\') {
            int ch2 = rdr.read();
            chr.append((char) ch2);
            if (ch2 == 'u') {
                for (int i = 0; i < 4; i++) {
                    int chu = rdr.read();
                    if (!isHexDigitChar(chu)) {
                        throw new IOException("Invalid unicode escape character: " + chu);
                    }
                    chr.append((char) chu);
                }
            } else {
                readEol(ch2);
            }
        } else {
            readEol(ch);
        }
    }

    private static String unescape(String escape) {
        StringBuilder txt = new StringBuilder();
        for (int i = 0; i < escape.length(); i++) {
            char ch = escape.charAt(i);
            if (ch == '\\') {
                ch = escape.charAt(++i);
                switch (ch) {
                    case 't':
                        txt.append('\t');
                        break;
                    case 'f':
                        txt.append('\f');
                        break;
                    case 'n':
                        txt.append('\n');
                        break;
                    case 'r':
                        txt.append('\r');
                        break;
                    case 'u':
                        String num = escape.substring(i + 1, i + 5);
                        txt.append((char) Integer.parseInt(num, 16));
                        i += 4;
                        break;
                    default:
                        txt.append(ch);
                        break;
                }
            } else {
                txt.append(ch);
            }
        }
        return txt.toString();
    }

    private void readEol(int cch) throws IOException {
        if (cch == '\n') {
            // If the next char is a \r we'll add it
            // to the current char buffer, otherwise
            // we'll save the character for next time.
            int nch = rdr.read();
            if (nch == '\r') {
                chr.append((char) nch);
            } else {
                pch = nch;
            }
        }
    }

    private String string() {
        String result = str.toString();
        str.setLength(0);
        return result;
    }

    private String trimmedString() {
        String result = string();
        int last = result.length();
        while (last > 0 && isWhitespaceChar(result.charAt(last - 1))) {
            last--;
        }
        if (last < result.length()) {
            str.append(result.substring(last));
            result = result.substring(0, last);
        }
        return result;
    }

    private boolean isSeparatorChar(int ch) {
        return ch == ' ' || ch == '\t' || ch == '=' || ch == ':';
    }

    private boolean isWhitespaceChar(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\f' || ch == '\n' || ch == '\r';
    }

    private boolean isCommentChar(int ch) {
        return ch == '#' || ch == '!';
    }

    private boolean isHexDigitChar(int ch) {
        int uch = Character.toUpperCase(ch);
        return Character.isDigit(ch) || (uch >= 'A' && uch <= 'F');
    }

    private boolean isEol(int ch) {
        return ch == '\n' || ch == '\r' || isEof(ch);
    }

    private boolean isEof(int ch) {
        return ch == -1;
    }

    public static Stream<Token> tokens(Reader rdr) throws IOException {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<Token>(0, 0) {
            final PropertiesParser p = new PropertiesParser(rdr);
            @Override
            public boolean tryAdvance(Consumer<? super Token> action) {
                try {
                    Token token = p.nextToken();
                    if (token != null) {
                        action.accept(token);
                        return true;
                    } else {
                        return false;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, false);
    }
}
