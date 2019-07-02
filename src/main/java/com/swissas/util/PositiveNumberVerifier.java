package com.swissas.util;

import javax.swing.*;

/**
 * A very basic verifier that checks that the component text is a positive integer
 * @author Tavan Alain
 */
public class PositiveNumberVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
        String text = ((JTextField) input).getText();
        int i;
        try {
            i = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            i = -1;
        }

        return i >= 0;
    }
}
