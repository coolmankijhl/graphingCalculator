package calculator;

import javax.swing.JTextField;

public class GraphingCalculator {
    public static final JTextField display = new JTextField();

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.createGUI();
    }
}
