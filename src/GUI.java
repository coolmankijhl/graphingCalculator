package calculator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GUI {
    private boolean useRadians = true; // Default to radians
    private final List<Token> tokenList = new ArrayList<>();

    // Instance variables for graphing
    private JPanel graphPanel;
    private Token currentFunction;

    // User-specified graph bounds
    private double minX = -10;
    private double maxX = 10;
    private double minY = -10;
    private double maxY = 10;

    public void createGUI() {
        JFrame mainWindow = new JFrame("Calculator");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a settings menu
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");

        // Mode menu item
        final JMenuItem modeItem = new JMenuItem("Current Mode: Radians");
        modeItem.addActionListener(e -> {
            useRadians = !useRadians; // Toggle between radians and degrees
            modeItem.setText(useRadians ? "Current Mode: Radians" : "Current Mode: Degrees");
            if (graphPanel != null) {
                graphPanel.repaint();
            }
        });
        settingsMenu.add(modeItem);

        // Add separator
        settingsMenu.addSeparator();

        // Graph Bounds menu item
        JMenuItem boundsItem = new JMenuItem("Set Graph Bounds");
        boundsItem.addActionListener(e -> {
            setGraphBounds();
        });
        settingsMenu.add(boundsItem);

        // Add settings menu to menu bar
        menuBar.add(settingsMenu);
        mainWindow.setJMenuBar(menuBar);

        // Create a JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Create the standard calculator tab
        JPanel standardCalculatorPanel = createCalculatorPanel(false);

        // Create the graphing calculator tab
        JPanel graphingCalculatorPanel = createCalculatorPanel(true);

        // Add tabs to the tabbed pane
        tabbedPane.addTab("Standard Calculator", standardCalculatorPanel);
        tabbedPane.addTab("Graphing Calculator", graphingCalculatorPanel);

        // Add the tabbed pane to the main window
        mainWindow.add(tabbedPane);

        mainWindow.setSize(800, 600);            // Adjust window size
        mainWindow.setLocationRelativeTo(null);  // Center the window
        mainWindow.setVisible(true);
    }

    private JPanel createCalculatorPanel(boolean isGraphingCalculator) {
        // Create a main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and add the display field at the top
        JTextField display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.PLAIN, 24));
        display.setPreferredSize(new Dimension(0, 50));
        mainPanel.add(display, BorderLayout.NORTH);

        // Create the button panel
        JPanel buttonPanel = createButtonPanel(display, isGraphingCalculator);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        if (isGraphingCalculator) {
            // Create a panel for the graph
            graphPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    drawFunction(g);
                }
            };
            graphPanel.setPreferredSize(new Dimension(400, 400));
            graphPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            mainPanel.add(graphPanel, BorderLayout.EAST);
        }

        return mainPanel;
    }

    private JPanel createButtonPanel(JTextField display, boolean isGraphingCalculator) {
        String[][] buttonNames = {
            {"7", "8", "9", "×", "("},
            {"4", "5", "6", "÷", ")"},
            {"1", "2", "3", "+", "e"},
            {".", "0", "=", "-", "π"},
            {"cos", "sin", "tan", "^", "√"},
            {"✖", "⌫", "X", "Ans"} // clear, backspace, variable
        };

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Configure GridBagConstraints
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between buttons
        gbc.fill = GridBagConstraints.BOTH;   // Make buttons expand to fill space
        gbc.weightx = 1.0;                    // Equal horizontal space
        gbc.weighty = 1.0;                    // Equal vertical space
        gbc.ipadx = 20;                       // Increase button width

        // Add buttons to the button panel
        for (int row = 0; row < buttonNames.length; row++) {
            gbc.gridy = row; // Set the row position
            for (int col = 0; col < buttonNames[row].length; col++) {
                gbc.gridx = col; // Set the column position

                final String buttonName = buttonNames[row][col];
                JButton button = new JButton(buttonName);
                createActionListener(display, button, buttonName, isGraphingCalculator);
                buttonPanel.add(button, gbc);
            }
        }

        return buttonPanel;
    }

    public void createActionListener(JTextField display, JButton button, String buttonName, boolean isGraphingCalculator) {
        button.addActionListener(e -> {
            switch (buttonName) {
                case "log", "ln", "√", "cos", "sin", "tan" -> {
                    // Add function token and open parenthesis
                    tokenList.add(new Token(Token.TokenType.FUNCTION, buttonName, null, null));
                    tokenList.add(new Token(Token.TokenType.PARENTHESIS, "(", null, null));
                    updateDisplay(display);
                }
                case "^", "+", "-", "×", "÷" -> {
                    // Add operator token
                    tokenList.add(new Token(Token.TokenType.OPERATOR, buttonName, null, null));
                    updateDisplay(display);
                }
                case "✖" -> {
                    // Clear action
                    tokenList.clear();
                    display.setText("");
                    if (isGraphingCalculator) {
                        currentFunction = null;
                        graphPanel.repaint();
                    }
                }
                case "⌫" -> {
                    // Backspace action
                    if (!tokenList.isEmpty()) {
                        tokenList.remove(tokenList.size() - 1);
                        updateDisplay(display);
                    }
                }
                case "=" -> {
                    // Evaluate or plot
                    try {
                        concatenateConstants();
                        Token parseTree = Parser.parseExpression(tokenList, new IndexHolder(0));
                        if (isGraphingCalculator) {
                            plotFunction(parseTree);
                        } else {
                            double result = Parser.evaluate(parseTree, useRadians);
                            display.setText(Double.toString(result));
                        }
                        tokenList.clear();
                    } catch (Exception ex) {
                        display.setText("Error");
                        if (isGraphingCalculator) {
                            currentFunction = null;
                            graphPanel.repaint();
                        }
                    }
                }
                default -> {
                    // Handle constants, variables, parentheses
                    Token.TokenType type;
                    String tokenValue = buttonName;
                    switch (buttonName) {
                        case "(", ")" -> type = Token.TokenType.PARENTHESIS;
                        case "e" -> {
                            type = Token.TokenType.CONSTANT;
                            tokenValue = "e";
                        }
                        case "π" -> {
                            type = Token.TokenType.CONSTANT;
                            tokenValue = "π";
                        }
                        case "X" -> type = Token.TokenType.VARIABLE;
                        default -> type = Token.TokenType.CONSTANT;
                    }
                    tokenList.add(new Token(type, tokenValue, null, null));
                    updateDisplay(display);
                }
            }
        });
    }

    private void updateDisplay(JTextField display) {
        StringBuilder displayText = new StringBuilder();
        for (Token token : tokenList) {
            displayText.append(token.getValue());
        }
        display.setText(displayText.toString());
    }

    private void concatenateConstants() {
        List<Token> newTokenList = new ArrayList<>();
        StringBuilder concatenatedValue = new StringBuilder();
        for (Token token : tokenList) {
            if (token.getType() == Token.TokenType.CONSTANT && !token.getValue().equals("e") && !token.getValue().equals("π")) {
                concatenatedValue.append(token.getValue());
            } else {
                if (concatenatedValue.length() > 0) {
                    newTokenList.add(new Token(Token.TokenType.CONSTANT, concatenatedValue.toString(), null, null));
                    concatenatedValue.setLength(0);
                }
                newTokenList.add(token);
            }
        }
        if (concatenatedValue.length() > 0) {
            newTokenList.add(new Token(Token.TokenType.CONSTANT, concatenatedValue.toString(), null, null));
        }
        tokenList.clear();
        tokenList.addAll(newTokenList);
    }

    private void plotFunction(Token parseTree) {
        currentFunction = parseTree;
        graphPanel.repaint();
    }

    private void drawFunction(Graphics g) {
        if (currentFunction == null) return;

        Graphics2D g2d = (Graphics2D) g;
        // Enable anti-aliasing for smoother lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = graphPanel.getWidth();
        int height = graphPanel.getHeight();

        // Calculate scaling factors based on user-specified bounds
        double scalingFactorX = width / (maxX - minX);
        double scalingFactorY = height / (maxY - minY);

        // Draw grid lines
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));

        int tickCountX = 10; // Number of ticks on X-axis
        int tickCountY = 10; // Number of ticks on Y-axis

        double tickSpacingX = (maxX - minX) / tickCountX;
        double tickSpacingY = (maxY - minY) / tickCountY;

        // Vertical grid lines
        for (int i = 0; i <= tickCountX; i++) {
            double xValue = minX + i * tickSpacingX;
            int x = (int) ((xValue - minX) * scalingFactorX);
            g2d.drawLine(x, 0, x, height);
        }

        // Horizontal grid lines
        for (int i = 0; i <= tickCountY; i++) {
            double yValue = minY + i * tickSpacingY;
            int y = (int) (height - (yValue - minY) * scalingFactorY);
            g2d.drawLine(0, y, width, y);
        }

        // Draw axes
        g2d.setColor(Color.GRAY);
        // X-axis
        int zeroY = (int) (height - ((0 - minY) * scalingFactorY));
        g2d.drawLine(0, zeroY, width, zeroY);
        // Y-axis
        int zeroX = (int) ((0 - minX) * scalingFactorX);
        g2d.drawLine(zeroX, 0, zeroX, height);

        // Draw ticks on axes
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // X-axis ticks and labels
        for (int i = 0; i <= tickCountX; i++) {
            double xValue = minX + i * tickSpacingX;
            int x = (int) ((xValue - minX) * scalingFactorX);
            g2d.drawLine(x, zeroY - 5, x, zeroY + 5);
            g2d.drawString(String.format("%.1f", xValue), x - 10, zeroY + 15);
        }

        // Y-axis ticks and labels
        for (int i = 0; i <= tickCountY; i++) {
            double yValue = minY + i * tickSpacingY;
            int y = (int) (height - ((yValue - minY) * scalingFactorY));
            g2d.drawLine(zeroX - 5, y, zeroX + 5, y);
            g2d.drawString(String.format("%.1f", yValue), zeroX + 5, y + 5);
        }

        // Plot the function with increased sampling rate
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));

        double xIncrement = (maxX - minX) / (width * 5); // 5 samples per pixel
        double prevX = minX;
        double prevY;

        try {
            prevY = Parser.evaluate(currentFunction, useRadians, prevX);
        } catch (Exception e) {
            prevY = Double.NaN;
        }

        for (double x = minX + xIncrement; x <= maxX; x += xIncrement) {
            double y;
            try {
                y = Parser.evaluate(currentFunction, useRadians, x);
            } catch (Exception e) {
                y = Double.NaN;
            }

            if (!Double.isNaN(prevY) && !Double.isNaN(y) && y >= minY && y <= maxY && prevY >= minY && prevY <= maxY) {
                int pixelX1 = (int) ((prevX - minX) * scalingFactorX);
                int pixelY1 = (int) (height - ((prevY - minY) * scalingFactorY));

                int pixelX2 = (int) ((x - minX) * scalingFactorX);
                int pixelY2 = (int) (height - ((y - minY) * scalingFactorY));

                if (Math.abs(pixelY1 - pixelY2) < height) {
                    g2d.drawLine(pixelX1, pixelY1, pixelX2, pixelY2);
                }
            }

            prevX = x;
            prevY = y;
        }
    }

    private void setGraphBounds() {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JTextField minXField = new JTextField(Double.toString(minX));
        JTextField maxXField = new JTextField(Double.toString(maxX));
        JTextField minYField = new JTextField(Double.toString(minY));
        JTextField maxYField = new JTextField(Double.toString(maxY));

        panel.add(new JLabel("Min X:"));
        panel.add(minXField);
        panel.add(new JLabel("Max X:"));
        panel.add(maxXField);
        panel.add(new JLabel("Min Y:"));
        panel.add(minYField);
        panel.add(new JLabel("Max Y:"));
        panel.add(maxYField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Set Graph Bounds", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double newMinX = Double.parseDouble(minXField.getText());
                double newMaxX = Double.parseDouble(maxXField.getText());
                double newMinY = Double.parseDouble(minYField.getText());
                double newMaxY = Double.parseDouble(maxYField.getText());

                if (newMinX >= newMaxX || newMinY >= newMaxY) {
                    JOptionPane.showMessageDialog(null, "Minimum values must be less than maximum values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                minX = newMinX;
                maxX = newMaxX;
                minY = newMinY;
                maxY = newMaxY;

                if (graphPanel != null) {
                    graphPanel.repaint();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter valid numerical values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
