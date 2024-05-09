/*
 * 
 * Build a snake game in Java - Quick and easy tutorial. (2023, January 27). YouTube. Devression. https://www.youtube.com/watch?v=S4lPjokjHWs. Accessed 8 May 2024 
 * 
 */

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import javax.swing.JPanel;

import javax.sound.sampled.*; // sound and file libraries yay! 
import java.io.File;
import java.io.IOException;

class GamePanel extends JPanel implements ActionListener{

    private double startUnixTime = System.currentTimeMillis() / 1000;
    private double currentUnixTime;
    private double timePlayed;
    
    private static final long serialVersionUID = 1L;
    
    static final int WIDTH = 1000;
    static final int HEIGHT = 1000;
    static final int UNIT_SIZE = 20;
    static final int NUMBER_OF_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // stores xy coordinates for the snake I believe
    final int x[] = new int[NUMBER_OF_UNITS];
    final int y[] = new int[NUMBER_OF_UNITS];
    
    // snake starts out this long
    int length = 5;
    int foodEaten;
    int foodX;
    int foodY;
    char direction = 'D';
    boolean running = false;
    Random random;
    Timer timer;

    File foodEatenSound = new File("/Users/name/Documents/JavaGUI/JavaGUI/src/apple-munch.wav");
    File gameOverSound = new File("/Users/name/Documents/JavaGUI/JavaGUI/src/negative_beeps.wav");
    
    GamePanel() {
        random = new Random(); 

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        play();
    }   
    
    public  void play() {
        addFood();
        running = true;
        
        timer = new Timer(80, this);
        timer.start();  
    }
    
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        draw(graphics);
    }
    
    public void move() {
        for (int i = length; i > 0; i--) {
            // moves snake by one unit each time
            x[i] = x[i-1];
            y[i] = y[i-1];
        }
        
        //directions!!
        if (direction == 'L') {
            x[0] = x[0] - UNIT_SIZE;
        } else if (direction == 'R') {
            x[0] = x[0] + UNIT_SIZE;
        } else if (direction == 'U') {
            y[0] = y[0] - UNIT_SIZE;
        } else {
            y[0] = y[0] + UNIT_SIZE;
        }   
    }
    
    public void checkFood() {
        if(x[0] == foodX && y[0] == foodY) {
            length++;
            foodEaten++;


            Thread audioThread = new Thread(() -> {
                try {
                    // Create an AudioInputStream from the .wav file
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(foodEatenSound);

                    // Get the audio format and create a DataLine.Info object
                    AudioFormat format = audioStream.getFormat();
                    DataLine.Info info = new DataLine.Info(Clip.class, format);

                    // Obtain and open the clip
                    Clip audioClip = (Clip) AudioSystem.getLine(info);
                    audioClip.open(audioStream);

                    // Play the audio clip
                    audioClip.start();

                    // Keep the audio thread running until the audio finishes playing
                    System.out.println("Playing audio...");
                    while (audioClip.isRunning()) {
                        Thread.sleep(100);
                    }

                    // Close the audio clip and audio stream
                    audioClip.close();
                    audioStream.close();
                    System.out.println("Playback completed.");
                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

            // had to learn threading for this - hope this doesn't spawn a million threads and crashes everything 
            audioThread.start();

            addFood();

        }
    }
    
    public void draw(Graphics graphics) {
        
        if (running) {
            graphics.setColor(new Color(210, 115, 90));
            graphics.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);
            
            graphics.setColor(Color.white);
            //commented below because I want a circular snake - it's not original but looks more realistic
            //graphics.fillRect(x[0], y[0], UNIT_SIZE, UNIT_SIZE);
            //that's the head, I'll add some eyes:
            graphics.fillOval(x[0], y[0], UNIT_SIZE, UNIT_SIZE);
            graphics.setColor(new Color(0, 0, 0));
            graphics.fillOval(x[0], y[0], UNIT_SIZE/10, UNIT_SIZE/10);

            for (int i = 1; i < length; i++) {
                graphics.setColor(new Color(40, 200, 150));
                //also commented because I want a circular snake
                //graphics.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                graphics.fillOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }
            
            graphics.setColor(Color.white);
            graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
            FontMetrics metrics = getFontMetrics(graphics.getFont());//WIDTH - metrics.stringWidth("Score: " + foodEaten)
            graphics.drawString("Score: " + foodEaten, WIDTH - metrics.stringWidth("Score: " + foodEaten) , graphics.getFont().getSize());

            graphics.setColor(Color.white);
            graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
            metrics = getFontMetrics(graphics.getFont());//(WIDTH - metrics.stringWidth("Time: " + getTimePlayed()))
            graphics.drawString("Time: " + getTimePlayed(),  0, graphics.getFont().getSize());

            
        
        } else {
            gameOver(graphics);
        }
    }
    
    public void addFood() {
        foodX = random.nextInt((int)(WIDTH / UNIT_SIZE))*UNIT_SIZE;
        foodY = random.nextInt((int)(HEIGHT / UNIT_SIZE))*UNIT_SIZE;

        

        
    }
    
    
    public void checkHit() {
        // crashes into the head into the body
        for (int i = length; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }
        
        // crashed into the wall
        if (x[0] < 0 || x[0] > WIDTH || y[0] < 0 || y[0] > HEIGHT) {
            running = false;
        }
        
        if(!running) {
            timer.stop();
        }
    }
    
    public void gameOver(Graphics graphics) {

        getTimePlayed();
        Thread audioThread = new Thread(() -> {
            try {
                // Create an AudioInputStream from the .wav file
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(gameOverSound);

                // Get the audio format and create a DataLine.Info object
                AudioFormat format = audioStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);

                // Obtain and open the clip
                Clip audioClip = (Clip) AudioSystem.getLine(info);
                audioClip.open(audioStream);

                // Play the audio clip
                audioClip.start();

                // Keep the audio thread running until the audio finishes playing
                System.out.println("Playing audio...");
                while (audioClip.isRunning()) {
                    Thread.sleep(100);
                }

                // Close the audio clip and audio stream
                audioClip.close();
                audioStream.close();
                System.out.println("Playback completed.");
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        // had to learn threading for this - hope this doesn't spawn a million threads and crashes everything 
        audioThread.start();
        graphics.setColor(Color.red);
        graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 50));
        FontMetrics metrics = getFontMetrics(graphics.getFont());
        graphics.drawString("Game Over! ", (WIDTH - metrics.stringWidth("Game Over! ")) / 2, HEIGHT / 2);

        //commented because I wanted to experiment with the above. never mind about this line - forgot it was needed oops.

        graphics.setColor(Color.white);
        graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
        metrics = getFontMetrics(graphics.getFont());//WIDTH - metrics.stringWidth("Score: " + foodEaten)
        graphics.drawString("Score: " + foodEaten, WIDTH - metrics.stringWidth("Score: " + foodEaten) , graphics.getFont().getSize());

        graphics.setColor(Color.white);
        graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
        metrics = getFontMetrics(graphics.getFont());//(WIDTH - metrics.stringWidth("Time: " + getTimePlayed()))
        graphics.drawString("Time: " + getTimePlayed(),  0, graphics.getFont().getSize());

    }
    
    public double getTimePlayed(){
        currentUnixTime = System.currentTimeMillis() / 1000d; //New! let's hope this works! very jank code I guess. Jank comments too!!!
        timePlayed = currentUnixTime- startUnixTime ; // cool, let's hope this isn't jank at all. Time stuff!

        return timePlayed;

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (running) {
            move();
            checkFood();
            checkHit();
        }
        repaint();
    }
    
    // too complex code that does switches for keypress stuff
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                    
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                    
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                    
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;      
            }
        }
    }
}

class GameFrame extends JFrame{

    private static final long serialVersionUID = 1L;

    GameFrame() {
        GamePanel panel = new GamePanel();
        this.add(panel);
        this.setTitle("snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}

public class SnakeGame {

    public static void main(String[] args) {
        new GameFrame();
    }

}


