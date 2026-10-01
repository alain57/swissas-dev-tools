package com.swissas.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.jetbrains.annotations.NotNull;

import static com.swissas.util.Constants.BLINKING;
import static com.swissas.util.Constants.OFF;
import static com.swissas.util.Constants.ON;

/**
 * The traffic light Bulb 
 * 
 * @author Tavan Alain
 */

class Bulb extends JPanel {
    private static final int BLINK_DELAY_MS = 1000;

    private final Color onColor;
    private final Timer blinkTimer;
    private String currentState;
    private boolean blinkingCurrentOn;
    private int radius;
    private int border;

    Bulb(Color color){
        this.blinkingCurrentOn = false;
        this.onColor = color;
        this.currentState = OFF;
        setOpaque(false);
        // a Swing timer only runs while blinking and lives on the EDT:
        // no extra (never stopped) thread per bulb anymore
        this.blinkTimer = new Timer(BLINK_DELAY_MS, e -> {
            this.blinkingCurrentOn = !this.blinkingCurrentOn;
            repaint();
        });
        this.blinkTimer.setRepeats(true);
    }

    void setRadiusAndBorder(int radius, int border){
        this.radius = radius;
        this.border = border;
    }

    void changeState(@NotNull String newState){
        if(!newState.equals(this.currentState)) {
            this.currentState = newState;
            if (BLINKING.equals(this.currentState)) {
                this.blinkingCurrentOn = true;
                this.blinkTimer.restart();
            } else {
                this.blinkingCurrentOn = false;
                this.blinkTimer.stop();
            }
            repaint();
        }
    }

    @Override
    public void removeNotify() {
        this.blinkTimer.stop();
        super.removeNotify();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (BLINKING.equals(this.currentState)) {
            this.blinkTimer.restart();
        }
    }

    @Override
    public Dimension getPreferredSize(){
        int size = (this.radius + this.border)*2;
        return new Dimension( size, size );
    }

    @Override
    public void paintComponent(Graphics g){
        switch (this.currentState) {
            case ON -> {
                g.setColor(this.onColor);
                g.fillOval(this.border, this.border, 2 * this.radius, 2 * this.radius);
            }
            case BLINKING -> {
                g.setColor(this.blinkingCurrentOn ? this.onColor : this.onColor.darker().darker().darker());
                g.fillOval(this.border, this.border, 2 * this.radius, 2 * this.radius);
            }
            default -> {
                g.setColor(this.onColor.darker().darker().darker());
                g.fillOval(this.border, this.border, 2 * this.radius, 2 * this.radius);
            }
        }
    }
}
