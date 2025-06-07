package calculator;

import java.util.List;

public class Parser {

    public static Token parseExpression(List<Token> tokens, IndexHolder currentIndexHolder) {
        Token left = parseTerm(tokens, currentIndexHolder);

        while (currentIndexHolder.index < tokens.size()) {
            Token currentToken = tokens.get(currentIndexHolder.index);

            if (currentToken.getValue().equals("+") || currentToken.getValue().equals("-")) {
                currentIndexHolder.index++; // Consume the operator
                Token right = parseTerm(tokens, currentIndexHolder);
                left = new Token(currentToken.getType(), currentToken.getValue(), left, right);
            } else {
                break;
            }
        }

        return left;
    }

    private static Token parseTerm(List<Token> tokens, IndexHolder currentIndexHolder) {
        Token left = parseFactor(tokens, currentIndexHolder);

        while (currentIndexHolder.index < tokens.size()) {
            Token currentToken = tokens.get(currentIndexHolder.index);

            if (currentToken.getValue().equals("×") || currentToken.getValue().equals("÷")) {
                currentIndexHolder.index++; // Consume the operator
                Token right = parseFactor(tokens, currentIndexHolder);
                left = new Token(currentToken.getType(), currentToken.getValue(), left, right);
            } else {
                break;
            }
        }

        return left;
    }

    private static Token parseFactor(List<Token> tokens, IndexHolder currentIndexHolder) {
        Token left = parsePrimary(tokens, currentIndexHolder);

        while (currentIndexHolder.index < tokens.size()) {
            Token currentToken = tokens.get(currentIndexHolder.index);

            if (currentToken.getValue().equals("^")) {
                currentIndexHolder.index++; // Consume the operator
                Token right = parseFactor(tokens, currentIndexHolder);
                left = new Token(currentToken.getType(), currentToken.getValue(), left, right);
            } else {
                break;
            }
        }

        return left;
    }

    private static Token parsePrimary(List<Token> tokens, IndexHolder currentIndexHolder) {
        if (currentIndexHolder.index >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of input");
        }

        Token currentToken = tokens.get(currentIndexHolder.index);

        switch (currentToken.getType()) {
            case CONSTANT, VARIABLE -> {
                currentIndexHolder.index++; // Consume the token
                return currentToken;
            }
            case FUNCTION -> {
                currentIndexHolder.index++; // Consume the function name
                Token functionToken = new Token(currentToken.getType(), currentToken.getValue(), null, null);

                if (currentIndexHolder.index < tokens.size() && tokens.get(currentIndexHolder.index).getValue().equals("(")) {
                    currentIndexHolder.index++; // Consume '('
                    Token argumentToken = parseExpression(tokens, currentIndexHolder);

                    if (currentIndexHolder.index >= tokens.size() || !tokens.get(currentIndexHolder.index).getValue().equals(")")) {
                        throw new IllegalArgumentException("Missing closing parenthesis after function argument");
                    }

                    currentIndexHolder.index++; // Consume ')'
                    functionToken.setArgument(argumentToken);
                    return functionToken;
                } else {
                    throw new IllegalArgumentException("Function without parentheses");
                }
            }
            case PARENTHESIS -> {
                if (currentToken.getValue().equals("(")) {
                    currentIndexHolder.index++; // Consume '('
                    Token expressionToken = parseExpression(tokens, currentIndexHolder);

                    if (currentIndexHolder.index >= tokens.size() || !tokens.get(currentIndexHolder.index).getValue().equals(")")) {
                        throw new IllegalArgumentException("Missing closing parenthesis");
                    }

                    currentIndexHolder.index++; // Consume ')'
                    return expressionToken;
                } else {
                    throw new IllegalArgumentException("Unexpected token: " + currentToken.getValue());
                }
            }
            default -> throw new IllegalArgumentException("Unexpected token: " + currentToken.getValue());
        }
    }

    // Updated evaluate method
    public static double evaluate(Token node, boolean useRadians, double x) {
        if (node == null) {
            throw new IllegalArgumentException("Invalid syntax tree");
        }

        switch (node.getType()) {
            case CONSTANT -> {
                switch (node.getValue()) {
                    case "e" -> {
                        return Math.E;
                    }
                    case "π" -> {
                        return Math.PI;
                    }
                    default -> {
                        double value = Double.parseDouble(node.getValue());
                        if (Math.abs(value) < 1e-10) {
                            return 0.0; // Handle small floating-point inaccuracies
                        }
                        return value;
                    }
                }
            }
            case VARIABLE -> {
                // Return the value of x for variable tokens
                return x;
            }
            case OPERATOR -> {
                double leftValue = evaluate(node.getLeft(), useRadians, x);
                double rightValue = evaluate(node.getRight(), useRadians, x);
                return switch (node.getValue()) {
                    case "+" -> leftValue + rightValue;
                    case "-" -> leftValue - rightValue;
                    case "×" -> leftValue * rightValue;
                    case "÷" -> leftValue / rightValue;
                    case "^" -> Math.pow(leftValue, rightValue);
                    default -> throw new IllegalArgumentException("Unknown operator: " + node.getValue());
                };
            }
            case FUNCTION -> {
                double argumentValue = evaluate(node.getArgument(), useRadians, x);
                if (!useRadians) {
                    argumentValue = Math.toRadians(argumentValue); // Convert degrees to radians
                }
                double result = switch (node.getValue()) {
                    case "sin" -> Math.sin(argumentValue);
                    case "cos" -> Math.cos(argumentValue);
                    case "tan" -> Math.tan(argumentValue);
                    case "log" -> Math.log10(argumentValue);
                    case "ln" -> Math.log(argumentValue);
                    case "√" -> Math.sqrt(argumentValue);
                    default -> throw new IllegalArgumentException("Unknown function: " + node.getValue());
                };

                return result;
            }
            default -> throw new IllegalArgumentException("Unexpected token type: " + node.getType());
        }
    }

    // Overloaded evaluate method for standard calculations (without x)
    public static double evaluate(Token node, boolean useRadians) {
        return evaluate(node, useRadians, 0); // x is not used in standard calculations
    }
}
