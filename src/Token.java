package calculator;

public class Token {
    public enum TokenType {
        CONSTANT, OPERATOR, FUNCTION, PARENTHESIS, VARIABLE
    }

    private final TokenType type;
    private final String value;
    private Token left, right, argument;

    public Token(TokenType type, String value, Token left, Token right) {
        this.type = type;
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public TokenType getType() { return type; }
    public String getValue() { return value; }

    public void setLeft(Token left) { this.left = left; }
    public void setRight(Token right) { this.right = right; }
    public void setArgument(Token argument) { this.argument = argument; }

    public Token getLeft() { return this.left; }
    public Token getRight() { return this.right; }
    public Token getArgument() { return this.argument; }
}
