package org.codejive.properties;

import java.util.List;
import java.util.function.Predicate;

public class Cursor {
    private final List<PropertiesParser.Token> tokens;
    private int index;

    public static Cursor index(List<PropertiesParser.Token> tokens, int index) {
        return new Cursor(tokens, index);
    }

    public static Cursor first(List<PropertiesParser.Token> tokens) {
        return new Cursor(tokens, tokens.isEmpty() ? -1 : 0);
    }

    public static Cursor last(List<PropertiesParser.Token> tokens) {
        return new Cursor(tokens, tokens.size() - 1);
    }

    private Cursor(List<PropertiesParser.Token> tokens, int index) {
        this.tokens = tokens;
        this.index = index;
    }

    public boolean atStart() {
        return index < 0;
    }

    public int position() {
        return index;
    }

    public boolean hasToken() {
        return hasToken(index);
    }

    private boolean hasToken(int idx) {
        return idx >= 0 && idx < tokens.size();
    }

    public PropertiesParser.Token token() {
        return tokens.get(index);
    }

    public String raw() {
        return token().getRaw();
    }

    public String text() {
        return token().getText();
    }

    public PropertiesParser.Type type() {
        return token().getType();
    }

    public boolean isType(PropertiesParser.Type... types) {
        if (index >= 0 && index < tokens.size()) {
            for (PropertiesParser.Type t : types) {
                if (t == tokens.get(index).getType()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWhitespace() {
        return isType(PropertiesParser.Type.WHITESPACE) && !token().isEol();
    }

    public boolean isEol() {
        return isType(PropertiesParser.Type.WHITESPACE) && token().isEol();
    }

    public Cursor prev() {
        return skip(-1);
    }

    public Cursor next() {
        return skip(1);
    }

    public Cursor skip(int steps) {
        index += steps;
        if (index < -1) {
            index = -1;
        } else if (index > tokens.size()) {
            index = tokens.size();
        }
        return this;
    }

    public boolean nextIf(PropertiesParser.Type type) {
        return nextIf(t -> t.getType() == type);
    }

    public boolean nextIf(Predicate<PropertiesParser.Token> accept) {
        if (hasToken() && accept.test(token())) {
            next();
            return true;
        } else {
            return false;
        }
    }

    public Cursor nextWhile(Predicate<PropertiesParser.Token> accept) {
        while (nextIf(accept)) {}
        return this;
    }

    public int nextCount(Predicate<PropertiesParser.Token> accept) {
        int cnt = 0;
        while (nextIf(accept)) {
            cnt++;
        }
        return cnt;
    }

    public boolean prevIf(PropertiesParser.Type type) {
        return prevIf(t -> t.getType() == type);
    }

    public boolean prevIf(Predicate<PropertiesParser.Token> accept) {
        if (hasToken() && accept.test(token())) {
            prev();
            return true;
        } else {
            return false;
        }
    }

    public Cursor prevWhile(Predicate<PropertiesParser.Token> accept) {
        while (prevIf(accept)) {}
        return this;
    }

    public int prevCount(Predicate<PropertiesParser.Token> accept) {
        int cnt = 0;
        while (prevIf(accept)) {
            cnt++;
        }
        return cnt;
    }

    /**
     * Inserts a token at the current position, shifting the current token (if any) and any subsequent tokens to the right (adds one to their indices).
     * <br/>
     * This method advances the cursor by one, such that the cursor state remains unchanged (pointing to the same token as before).
     * @param token the token to insert.
     * @return {@code this}
     */
    public Cursor add(PropertiesParser.Token token) {
        index = Math.max(index, 0);
        addToken(index++, token);
        return this;
    }

    /**
     * Inserts an EOL Token at the current position.
     * @see #add(PropertiesParser.Token)
     * @return {@code this}
     */
    public Cursor addEol() {
        return add(PropertiesParser.Token.EOL);
    }

    private void addToken(int index, PropertiesParser.Token token) {
        if (hasToken(index)) {
            tokens.add(index, token);
        } else {
            tokens.add(token);
        }
    }

    public Cursor replace(PropertiesParser.Token token) {
        tokens.set(index, token);
        return this;
    }

    public void remove() {
        tokens.remove(index);
    }

    public Cursor copy() {
        return Cursor.index(tokens, index);
    }

    @Override
    public String toString() {
        return (hasToken() ? token() + " " : "") + "@" + position();
    }
}
