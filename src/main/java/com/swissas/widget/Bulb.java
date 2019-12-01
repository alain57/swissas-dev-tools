package com.swissas.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import com.intellij.ui.JBColor;
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
    private final Color onColor;
    private String currentState;
    private boolean blinkingCurrentOn;
    private final TimerTask timerTask;
    private int radius;
    private int border;

    Bulb(Color color){
        this.blinkingCurrentOn = false;
        this.onColor = color;
        this.currentState = OFF;
        Timer timer = new Timer("blink"/*NON-NLS*/);
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                Bulb.this.blinkingCurrentOn = !Bulb.this.blinkingCurrentOn;
                repaint();
            }
        };
        timer.scheduleAtFixedRate(this.timerTask, 1000, 1000);
    }

    void setRadiusAndBorder(int radius, int border){
        this.radius = radius;
        this.border = border;
    }

    void changeState(@NotNull String newState){
        if(!newState.equals(this.currentState)) {
            this.currentState = newState;
            if (this.currentState.equals(BLINKING)) {
                this.timerTask.run();
            } else {
                this.blinkingCurrentOn = false;
                this.timerTask.cancel();
            }
        }
    }
    
    @Override
    public Dimension getPreferredSize(){
        int size = (this.radius + this.border)*2;
        return new Dimension( size, size );
    }

    @Override
    public void paintComponent(Graphics g){
        g.setColor(JBColor.background());
        g.fillRect(0,0,getWidth(),getHeight());
        switch (this.currentState){
            case ON:
                g.setColor( this.onColor);
                g.fillOval(this.border, this.border,2* this.radius,2* this.radius);
                break;
            case BLINKING:
                g.setColor(this.blinkingCurrentOn ? this.onColor : this.onColor.darker().darker().darker());
                g.fillOval(this.border, this.border,2* this.radius,2* this.radius);
                break;
            default:
                g.setColor( this.onColor.darker().darker().darker() );
                g.fillOval(this.border, this.border,2* this.radius,2* this.radius);
                break;
        }
    }
}