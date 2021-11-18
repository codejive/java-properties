package org.codejive.properties;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class PropertiesParser {

    public static class Token {
        final String text;

        Token(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Token)) return false;
            Token token = (Token) o;
            return text.equals(token.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{'" + text + "'}";
        }
    }

    public static class KeyToken extends Token {
        KeyToken(String text) {
            super(text);
        }
    }

    public static class ValueToken extends Token {
        ValueToken(String text) {
            super(text);
        }
    }

    public static class SeparatorToken extends Token {
        SeparatorToken(String text) {
            super(text);
        }
    }

    public static class CommentToken extends Token {
        CommentToken(String text) {
            super(text);
        }
    }

    public static class WhitespaceToken extends Token {
        WhitespaceToken(String text) {
            super(text);
        }
    }

    private enum State {
        INIT,
        KEY,
        SEPARATOR,
        VALUE,
        COMMENT,
        WHITESPACE
    }

    private final Reader rdr;

    private State state;
    private int ch;
    private int pch;
    StringBuilder chr;
    StringBuilder str;

    public PropertiesParser(Reader rdr) throws IOException {
        this.rdr = rdr;
        state = State.INIT;
        chr = new StringBuilder();
        str = new StringBuilder();
        pch = -1;
        nextChar();
    }

    public Token nextToken() throws IOException {
        while (true) {
            switch (state) {
                case INIT:
                    if (isCommentChar(ch)) {
                        state = State.COMMENT;
                    } else if (isWhitespaceChar(ch)) {
                        state = State.WHITESPACE;
                    } else if (isEof(ch)) {
                        return null;
                    } else {
                        state = State.KEY;
                    }
                    break;
                case KEY:
                    if (!isSeparatorChar(ch)) {
                        str.append(chr);
                        nextChar();
                    } else {
                        state = State.SEPARATOR;
                        return new KeyToken(string());
                    }
                    break;
                case SEPARATOR:
                    if (isSeparatorChar(ch)) {
                        str.append(chr);
                        nextChar();
                    } else {
                        state = State.VALUE;
                        return new SeparatorToken(string());
                    }
                    break;
                case VALUE:
                    if (!isEol(ch)) {
                        str.append(chr);
                        nextChar();
                    } else {
                        state = State.INIT;
                        return new ValueToken(trimmedString());
                    }
                    break;
                case COMMENT:
                    if (!isEol(ch)) {
                        str.append(chr);
                        nextChar();
                    } else {
                        state = State.INIT;
                        return new CommentToken(trimmedString());
                    }
                    break;
                case WHITESPACE:
                    if (isWhitespaceChar(ch)) {
                        str.append(chr);
                        nextChar();
                    } else {
                        state = State.INIT;
                        return new WhitespaceToken(string());
                    }
                    break;
            }
        }
    }

    private void nextChar() throws IOException {
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

    public static List<Token> tokens(Reader rdr) throws IOException {
        List<Token> result = new ArrayList<>();
        PropertiesParser p = new PropertiesParser(rdr);
        Token token;
        while ((token = p.nextToken()) != null) {
            result.add(token);
        }
        return result;
    }
}
