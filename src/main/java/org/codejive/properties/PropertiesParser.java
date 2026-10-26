package org.codejive.properties;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A parser that given a text input that follows the standard Java Properties file format will
 * return a stream of tokens. These tokens will contain _all_ characters that were read from the
 * input which makes it possible to exactly recreate the original, including all whitespace and
 * comments.
 */
public class PropertiesParser {

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

        /**
         * Constructor for tokens where the raw value and the text value are exactly the same.
         *
         * @param type The token's type
         * @param raw The token's value
         */
        public Token(Type type, String raw) {
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
        public Token(Type type, String raw, String text) {
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
         * @return a string containing the token's processed value
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
            if (raw.isEmpty()) return false;
            int ch = raw.charAt(raw.length() - 1);
            return type == Type.WHITESPACE && PropertiesParser.isEol(ch);
        }

        /**
         * Determines if this token is a whitespace NOT ending with an EOL marker.
         *
         * @return true if whitespace NOT ending in EOL, false otherwise
         */
        public boolean isWs() {
            if (raw.isEmpty()) return false;
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

    private enum State {
        START,
        COMMENT,
        WHITESPACE,
        KEY,
        SEPARATOR,
        SEPARATOR_BACKSLASH,
        VALUE_BACKSLASH,
        VALUE
    }

    private State state;
    private int pch;
    private boolean separatorSeen;
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
        state = State.START;
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
        while (true) {
            int ch = peekChar();
            switch (state) {
                case START:
                    if (isEof(ch)) {
                        return null;
                    }
                    separatorSeen = false;
                    state =
                            isCommentChar(ch)
                                    ? State.COMMENT
                                    : isWhitespaceEolChar(ch) ? State.WHITESPACE : State.KEY;
                    break;
                case COMMENT:
                    if (isEol(ch) || isEof(ch)) {
                        return finishToken(Type.COMMENT, State.START);
                    }
                    addChar(readChar());
                    break;
                case WHITESPACE:
                    if (!isWhitespaceEolChar(ch)) {
                        return finishToken(Type.WHITESPACE, State.START);
                    }
                    addChar(readChar());
                    if (isEol(ch)) {
                        return finishToken(Type.WHITESPACE, State.START);
                    }
                    break;
                case KEY:
                    if (isSeparatorChar(ch) || isWhitespaceEolChar(ch) || isEof(ch)) {
                        return finishToken(Type.KEY, State.SEPARATOR);
                    }
                    addChar(readChar());
                    break;
                case SEPARATOR:
                    if (ch == '\\') {
                        // Consume the backslash, but defer which token owns it.
                        readChar();
                        state = State.SEPARATOR_BACKSLASH;
                    } else if (isWhitespaceChar(ch)) {
                        addChar(readChar());
                    } else if (isSeparatorChar(ch) && !separatorSeen) {
                        separatorSeen = true;
                        addChar(readChar());
                    } else {
                        return finishToken(Type.SEPARATOR, State.VALUE);
                    }
                    break;
                case SEPARATOR_BACKSLASH:
                    if (isEol(ch)) {
                        // The pending backslash belongs to a separator continuation.
                        addChar('\\');
                        state = State.SEPARATOR;
                    } else {
                        // Carry the pending backslash into the next token, including at EOF.
                        return finishToken(Type.SEPARATOR, State.VALUE_BACKSLASH);
                    }
                    break;
                case VALUE_BACKSLASH:
                    addChar('\\');
                    state = State.VALUE;
                    break;
                case VALUE:
                    if (isEol(ch) || isEof(ch)) {
                        return finishToken(Type.VALUE, State.START);
                    }
                    addChar(readChar());
                    break;
            }
        }
    }

    private Token finishToken(Type type, State nextState) {
        String raw = string();
        Token token = hasEscapes ? new Token(type, raw, unescape(raw)) : new Token(type, raw);
        hasEscapes = false;
        state = nextState;
        return token;
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
        // Comments have no escapes or line continuations
        if (ch == '\\' && state != State.COMMENT) {
            hasEscapes = true;
            int ch2 = readChar();
            if (isEof(ch2)) {
                // A backslash at the end of the input escapes nothing
                return;
            }
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
                if (isEol(ch2)) {
                    // A line continuation also skips the leading whitespace of the next line
                    while (isWhitespaceChar(peekChar())) {
                        str.append((char) readChar());
                    }
                }
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
    public static String unescape(String escape) {
        StringBuilder txt = new StringBuilder();
        for (int i = 0; i < escape.length(); i++) {
            char ch = escape.charAt(i);
            if (ch == '\\') {
                if (i == escape.length() - 1) {
                    break;
                }
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
                                && isWhitespaceChar(ch = escape.charAt(i + 1))) {
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

    public static boolean isSeparatorChar(int ch) {
        return ch == '=' || ch == ':';
    }

    private static boolean isWhitespaceChar(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\f';
    }

    private static boolean isWhitespaceEolChar(int ch) {
        return isWhitespaceChar(ch) || isEol(ch);
    }

    public static boolean isCommentChar(int ch) {
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
