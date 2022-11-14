package org.codejive.properties;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A parser that given a text input that follows the standard Java Properties file format will
 * return a stream of tokens. These tokens will contain _all_ characters that were read from the
 * input which makes it possible to exactly recreate the original, including all whitespace and
 * comments.
 */
class PropertiesParser {

    /** The type of token. */
    public enum Type {
        /** The key part of a key-value pair */
        KEY,
        /**
         * The separator between a key and a value. This will include any whitespace that exists
         * before and after the separator!
         */
        SEPARATOR,
        /** The value part of a key-value pair */
        VALUE,
        /** A comment, both # and ! */
        COMMENT,
        /**
         * Any whitespace. Multiple consecutive lines will be split into separate tokens at the
         * end-of-line character(s).
         */
        WHITESPACE
    }

    /**
     * Tokens are returned by the parser for each part if the input. Their type is defined by <code>
     * Type</code> and they have both a raw value, meaning the exact value as it was encountered in
     * the input, and a text value, which is the raw value after all escape sequences have been
     * processed.
     */
    public static class Token {
        final Type type;
        final String raw;
        final String text;

        public static final Token EOL =
                new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n");

        /**
         * Constructor for tokens where the raw value and the text value are exactly the same.
         *
         * @param type The token's type
         * @param raw The token's value
         */
        Token(Type type, String raw) {
            this(type, raw, null);
        }

        /**
         * The constructor for tokens that have a raw value and a processed value where escape
         * sequences have been turned into their actual values.
         *
         * @param type The token's type
         * @param raw The token's raw value (including escape sequences)
         * @param text The token's processed value (no escape sequences)
         */
        Token(Type type, String raw, String text) {
            this.type = type;
            this.raw = raw;
            if (raw.equals(text)) {
                text = null;
            }
            this.text = text;
        }

        /**
         * Returns the token's type
         *
         * @return The token's type
         */
        public Type getType() {
            return type;
        }

        /**
         * Returns the token's unprocessed/raw value. Meaning this value can contain escape
         * sequences.
         *
         * @return a string containing the token's raw value
         */
        public String getRaw() {
            return raw;
        }

        /**
         * Returns the token's processed value. Meaning this value will not contain any escape
         * sequences but only actual characters.
         *
         * @return
         */
        public String getText() {
            return text != null ? text : raw;
        }

        /**
         * Determines if this token is a whitespace ending with an EOL marker.
         *
         * @return true if whitespace ending in EOL, false otherwise
         */
        public boolean isEol() {
            int ch = raw.charAt(raw.length() - 1);
            return type == Type.WHITESPACE && PropertiesParser.isEol(ch);
        }

        /**
         * Determines if this token is a whitespace NOT ending with an EOL marker.
         *
         * @return true if whitespace NOT ending in EOL, false otherwise
         */
        public boolean isWs() {
            int ch = raw.charAt(raw.length() - 1);
            return type == Type.WHITESPACE && !PropertiesParser.isEol(ch);
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
    private StringBuilder str;
    private boolean hasEscapes;

    /**
     * Constructor that takes a <code>Reader</code> for reading the input to parse.
     *
     * @param rdr a <code>Reader</code> object
     * @throws IOException Thrown when any IO error occurs during parsing
     */
    public PropertiesParser(Reader rdr) throws IOException {
        this.rdr = rdr;
        state = null;
        str = new StringBuilder();
        readChar();
    }

    /**
     * Returns a stream of tokens for the given input.
     *
     * @param rdr a <code>Reader</code> object
     * @return a <code>Stream</code> of <code>Token</code>
     * @throws IOException Thrown when any IO error occurs during parsing
     */
    public static Stream<Token> tokens(Reader rdr) throws IOException {
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Token>(0, 0) {
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
                },
                false);
    }

    /**
     * Returns the next token in the input or <code>null</code> if the end of the input was reached.
     *
     * @return a <code>Token</code> or <code>null</code>
     * @throws IOException Thrown when any IO error occurs during parsing
     */
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
                String text = string();
                Token token =
                        hasEscapes
                                ? new Token(state, text, unescape(text))
                                : new Token(state, text);
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
        if (ch == '\r') {
            if (peekChar() == '\n') {
                str.append((char) readChar());
            }
        }
    }

    private String string() {
        String result = str.toString();
        str.setLength(0);
        return result;
    }

    /**
     * Returns a copy of the given string where all escape sequences have been turned into their
     * representative values.
     *
     * @param escape Input string
     * @return Decoded string
     */
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
                    case '\r':
                        // Skip the next character if it's a '\n'
                        if (i < (escape.length() - 1) && escape.charAt(i + 1) == '\n') {
                            i++;
                        }
                        // fall-through!
                    case '\n':
                        // Skip any leading whitespace
                        while (i < (escape.length() - 1)
                                && isWhitespaceChar(ch = escape.charAt(i + 1))
                                && !isEol(ch)) {
                            i++;
                        }
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
}
