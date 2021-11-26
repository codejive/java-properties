package org.codejive.properties;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
            if (raw.equals(text)) {
                text = null;
            }
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

        public boolean isEol() {
            int ch = raw.charAt(raw.length() - 1);
            return type == Type.WHITESPACE && (PropertiesParser.isEol(ch) || isEof(ch));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Token)) return false;
            Token token = (Token) o;
            return type == token.type && Objects.equals(getText(), token.getText());
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, getText());
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
    private int pch;
    StringBuilder str;
    boolean hasEscapes;

    public PropertiesParser(Reader rdr) throws IOException {
        this.rdr = rdr;
        state = null;
        str = new StringBuilder();
        readChar();
    }

    public Token nextToken() throws IOException {
        int ch = peekChar();
        if (isEof(ch)) {
            return null;
        }
        int oldch = -1;
        BiFunction<Integer, Integer, Boolean> isValid = (c, oldc) -> false;
        Type nextState = null;
        if (state == null) {
            if (isCommentChar(ch)) {
                state = Type.COMMENT;
                isValid = (c, oldc) -> !isEol(c) && !isEof(c);
            } else if (isWhitespaceChar(ch)) {
                state = Type.WHITESPACE;
                isValid = (c, oldc) -> isWhitespaceChar(c) && !isEol(oldc);
            } else {
                state = Type.KEY;
                isValid = (c, oldc) -> !isSeparatorChar(c);
                nextState = Type.SEPARATOR;
            }
        } else if (state == Type.SEPARATOR) {
            isValid = (c, oldc) -> isSeparatorChar(c);
            nextState = Type.VALUE;
        } else if (state == Type.VALUE) {
            isValid = (c, oldc) -> !isEol(c) && !isEof(c);
        }
        while (true) {
            if (isValid.apply(ch, oldch)) {
                addChar(readChar());
                oldch = ch;
                ch = peekChar();
            } else {
                String text = (state == Type.VALUE || state == Type.COMMENT) ? trimmedString() : string();
                Token token = hasEscapes ? new Token(state, text, unescape(text)) :  new Token(state, text);
                hasEscapes = false;
                state = nextState;
                return token;
            }
        }
    }

    private int peekChar() {
        return pch;
    }

    private int readChar() throws IOException {
        int ch = pch;
        pch = rdr.read();
        return ch;
    }

    private void addChar(int ch) throws IOException {
        str.append((char) ch);
        if (ch == '\\') {
            hasEscapes = true;
            int ch2 = readChar();
            str.append((char) ch2);
            if (ch2 == 'u') {
                for (int i = 0; i < 4; i++) {
                    int chu = readChar();
                    if (!isHexDigitChar(chu)) {
                        throw new IOException("Invalid unicode escape character: " + chu);
                    }
                    str.append((char) chu);
                }
            } else {
                readEol(ch2);
            }
        } else {
            readEol(ch);
        }
    }

    private void readEol(int ch) throws IOException {
        if (ch == '\n') {
            if (peekChar() == '\r') {
                str.append((char) readChar());
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

    static String unescape(String escape) {
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

    private static boolean isSeparatorChar(int ch) {
        return ch == ' ' || ch == '\t' || ch == '=' || ch == ':';
    }

    private static boolean isWhitespaceChar(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\f' || isEol(ch);
    }

    private static boolean isCommentChar(int ch) {
        return ch == '#' || ch == '!';
    }

    private static boolean isHexDigitChar(int ch) {
        int uch = Character.toUpperCase(ch);
        return Character.isDigit(ch) || (uch >= 'A' && uch <= 'F');
    }

    private static boolean isEol(int ch) {
        return ch == '\n' || ch == '\r';
    }

    private static boolean isEof(int ch) {
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
