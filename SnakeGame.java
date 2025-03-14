import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame());
    }

    // Game constants
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private static final int GAME_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int GAME_HEIGHT = GRID_SIZE * CELL_SIZE;
    private static final int INITIAL_SPEED = 120;
    private static final int SPEED_INCREMENT = 10;
    private static final int MAX_SPEED = 50;

    // Game state
    private GamePanel gamePanel;
    private Timer gameTimer;
    private int score = 0;
    private int highScore = 0;
    private int currentSpeed = INITIAL_SPEED;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private JLabel statusLabel;
    private JButton restartButton;
    private JButton pauseButton;
    private JComboBox<String> difficultyComboBox;

    public SnakeGame() {
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Create game panel
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        
        // Create status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Score: 0 | High Score: 0");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        difficultyComboBox = new JComboBox<>(new String[] {"Easy", "Medium", "Hard"});
        difficultyComboBox.addActionListener(e -> changeDifficulty());
        
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> togglePause());
        
        restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());
        
        controlPanel.add(new JLabel("Difficulty:"));
        controlPanel.add(difficultyComboBox);
        controlPanel.add(pauseButton);
        controlPanel.add(restartButton);
        
        statusPanel.add(controlPanel, BorderLayout.EAST);
        
        // Add components to frame
        add(gamePanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Setup game timer
        gameTimer = new Timer(currentSpeed, e -> updateGame());
        
        // Setup key listener
        setupKeyListener();
        
        // Pack and display frame
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Start game
        startGame();
    }
    
    private void setupKeyListener() {
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isGameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        restartGame();
                    }
                    return;
                }
                
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause();
                    return;
                }
                
                if (isPaused) return;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if (gamePanel.getDirection() != Direction.DOWN)
                            gamePanel.setDirection(Direction.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if (gamePanel.getDirection() != Direction.UP)
                            gamePanel.setDirection(Direction.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if (gamePanel.getDirection() != Direction.RIGHT)
                            gamePanel.setDirection(Direction.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if (gamePanel.getDirection() != Direction.LEFT)
                            gamePanel.setDirection(Direction.RIGHT);
                        break;
                }
            }
        });
    }
    
    private void startGame() {
        gamePanel.initGame();
        score = 0;
        isGameOver = false;
        isPaused = false;
        updateStatusLabel();
        gameTimer.start();
        gamePanel.requestFocus();
    }
    
    private void restartGame() {
        gameTimer.stop();
        currentSpeed = INITIAL_SPEED;
        gameTimer.setDelay(currentSpeed);
        startGame();
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        pauseButton.setText(isPaused ? "Resume" : "Pause");
        if (isPaused) {
            gameTimer.stop();
        } else {
            gameTimer.start();
            gamePanel.requestFocus();
        }
    }
    
    private void changeDifficulty() {
        String difficulty = (String) difficultyComboBox.getSelectedItem();
        switch (difficulty) {
            case "Easy":
                currentSpeed = INITIAL_SPEED;
                break;
            case "Medium":
                currentSpeed = INITIAL_SPEED - SPEED_INCREMENT;
                break;
            case "Hard":
                currentSpeed = INITIAL_SPEED - (SPEED_INCREMENT * 2);
                break;
        }
        gameTimer.setDelay(currentSpeed);
    }
    
    private void updateGame() {
        if (isGameOver || isPaused) return;
        
        boolean result = gamePanel.updateGame();
        
        if (!result) {
            // Game over
            gameTimer.stop();
            isGameOver = true;
            updateStatusLabel();
            return;
        }
        
        if (gamePanel.isSnakeAteFood()) {
            score += 10;
            if (score > highScore) {
                highScore = score;
            }
            updateStatusLabel();
            
            if (score % 50 == 0 && currentSpeed > MAX_SPEED) {
                currentSpeed -= SPEED_INCREMENT;
                gameTimer.setDelay(currentSpeed);
            }
        }
    }
    
    private void updateStatusLabel() {
        if (isGameOver) {
            statusLabel.setText("Game Over! Score: " + score + " | High Score: " + highScore + " | Press SPACE to restart");
        } else {
            statusLabel.setText("Score: " + score + " | High Score: " + highScore);
        }
    }
    
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    
    // Game panel class
    private class GamePanel extends JPanel {
        private ArrayList<Point> snake;
        private Point food;
        private Direction direction;
        private boolean ateFood;
        private Random random;
        private Point specialFood;
        private int specialFoodTimer;
        private boolean specialFoodActive;
        
        public GamePanel() {
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            snake = new ArrayList<>();
            random = new Random();
        }
        
        public void initGame() {
            snake.clear();
            snake.add(new Point(GRID_SIZE / 2, GRID_SIZE / 2));
            snake.add(new Point(GRID_SIZE / 2 - 1, GRID_SIZE / 2));
            snake.add(new Point(GRID_SIZE / 2 - 2, GRID_SIZE / 2));
            
            direction = Direction.RIGHT;
            spawnFood();
            specialFoodActive = false;
            specialFoodTimer = 0;
            ateFood = false;
        }
        
        public Direction getDirection() {
            return direction;
        }
        
        public void setDirection(Direction direction) {
            this.direction = direction;
        }
        
        public boolean isSnakeAteFood() {
            return ateFood;
        }
        
        private void spawnFood() {
            while (true) {
                int x = random.nextInt(GRID_SIZE);
                int y = random.nextInt(GRID_SIZE);
                food = new Point(x, y);
                
                // Make sure food doesn't spawn on snake
                boolean onSnake = false;
                for (Point p : snake) {
                    if (p.x == x && p.y == y) {
                        onSnake = true;
                        break;
                    }
                }
                
                if (!onSnake) break;
            }
        }
        
        private void spawnSpecialFood() {
            while (true) {
                int x = random.nextInt(GRID_SIZE);
                int y = random.nextInt(GRID_SIZE);
                specialFood = new Point(x, y);
                
                boolean onSnake = false;
                for (Point p : snake) {
                    if (p.x == x && p.y == y) {
                        onSnake = true;
                        break;
                    }
                }
                
                if (!onSnake && (food.x != x || food.y != y)) break;
            }
            specialFoodActive = true;
            specialFoodTimer = 50;
        }
        
        public boolean updateGame() {
            ateFood = false;
            
            // Move snake
            Point head = snake.get(0);
            Point newHead = new Point(head);
            
            switch (direction) {
                case UP:
                    newHead.y--;
                    break;
                case DOWN:
                    newHead.y++;
                    break;
                case LEFT:
                    newHead.x--;
                    break;
                case RIGHT:
                    newHead.x++;
                    break;
            }
            
            // Check for collision with wall
            if (newHead.x < 0 || newHead.x >= GRID_SIZE || newHead.y < 0 || newHead.y >= GRID_SIZE) {
                return false;
            }
            
            // Check for collision with self
            for (int i = 1; i < snake.size(); i++) {
                if (newHead.x == snake.get(i).x && newHead.y == snake.get(i).y) {
                    return false;
                }
            }
            
            // Check for food
            if (newHead.x == food.x && newHead.y == food.y) {
                ateFood = true;
                spawnFood();
                
                // 20% chance to spawn special food
                if (random.nextInt(5) == 0 && !specialFoodActive) {
                    spawnSpecialFood();
                }
            } else if (specialFoodActive && newHead.x == specialFood.x && newHead.y == specialFood.y) {
                // Special food gives 30 points
                score += 20; // Additional 20 points (10 will be added in updateGame)
                specialFoodActive = false;
                ateFood = true;
            } else {
                // Remove tail if no food was eaten
                snake.remove(snake.size() - 1);
            }
            
            // Add new head
            snake.add(0, newHead);
            
            // Update special food timer
            if (specialFoodActive) {
                specialFoodTimer--;
                if (specialFoodTimer <= 0) {
                    specialFoodActive = false;
                }
            }
            
            repaint();
            return true;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw grid background
            for (int x = 0; x < GRID_SIZE; x++) {
                for (int y = 0; y < GRID_SIZE; y++) {
                    if ((x + y) % 2 == 0) {
                        g2d.setColor(new Color(0, 20, 0));
                    } else {
                        g2d.setColor(new Color(0, 30, 0));
                    }
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
            
            // Draw snake
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                
                if (i == 0) {
                    // Head
                    g2d.setColor(new Color(50, 205, 50));
                } else {
                    // Body with gradient color
                    float hue = 0.33f; // Green
                    float saturation = 1.0f - ((float) i / snake.size() * 0.7f);
                    float brightness = 0.9f - ((float) i / snake.size() * 0.5f);
                    g2d.setColor(Color.getHSBColor(hue, saturation, brightness));
                }
                
                g2d.fillRoundRect(p.x * CELL_SIZE + 1, p.y * CELL_SIZE + 1, 
                                CELL_SIZE - 2, CELL_SIZE - 2, 8, 8);
                
                // Add eyes to head
                if (i == 0) {
                    g2d.setColor(Color.BLACK);
                    int eyeSize = CELL_SIZE / 5;
                    
                    // Position eyes based on direction
                    int leftEyeX, leftEyeY, rightEyeX, rightEyeY;
                    
                    switch (direction) {
                        case UP:
                            leftEyeX = p.x * CELL_SIZE + CELL_SIZE / 4;
                            leftEyeY = p.y * CELL_SIZE + CELL_SIZE / 3;
                            rightEyeX = p.x * CELL_SIZE + 3 * CELL_SIZE / 4 - eyeSize;
                            rightEyeY = p.y * CELL_SIZE + CELL_SIZE / 3;
                            break;
                        case DOWN:
                            leftEyeX = p.x * CELL_SIZE + CELL_SIZE / 4;
                            leftEyeY = p.y * CELL_SIZE + 2 * CELL_SIZE / 3 - eyeSize;
                            rightEyeX = p.x * CELL_SIZE + 3 * CELL_SIZE / 4 - eyeSize;
                            rightEyeY = p.y * CELL_SIZE + 2 * CELL_SIZE / 3 - eyeSize;
                            break;
                        case LEFT:
                            // leftEyeX = p.x * CELL_SIZE + CELL_SIZE / 3;
                            // leftEyeY = p.y * CELL_SIZE + CELL_SIZE /3;
                            leftEyeX = p.x * CELL_SIZE + CELL_SIZE / 3;
                            leftEyeY = p.y * CELL_SIZE + CELL_SIZE / 4;
                            rightEyeX = p.x * CELL_SIZE + CELL_SIZE / 3;
                            rightEyeY = p.y * CELL_SIZE + 3 * CELL_SIZE / 4 - eyeSize;
                            break;
                        case RIGHT:
                            leftEyeX = p.x * CELL_SIZE + 2 * CELL_SIZE / 3 - eyeSize;
                            leftEyeY = p.y * CELL_SIZE + CELL_SIZE / 4;
                            rightEyeX = p.x * CELL_SIZE + 2 * CELL_SIZE / 3 - eyeSize;
                            rightEyeY = p.y * CELL_SIZE + 3 * CELL_SIZE / 4 - eyeSize;
                            break;
                        default:
                            leftEyeX = p.x * CELL_SIZE + CELL_SIZE / 3;
                            leftEyeY = p.y * CELL_SIZE + CELL_SIZE / 3;
                            rightEyeX = p.x * CELL_SIZE + 2 * CELL_SIZE / 3 - eyeSize;
                            rightEyeY = p.y * CELL_SIZE + CELL_SIZE / 3;
                    }
                    
                    g2d.fillOval(leftEyeX, leftEyeY, eyeSize, eyeSize);
                    g2d.fillOval(rightEyeX, rightEyeY, eyeSize, eyeSize);
                }
            }
            
            // Draw food
            g2d.setColor(Color.RED);
            g2d.fillOval(food.x * CELL_SIZE + 2, food.y * CELL_SIZE + 2, 
                          CELL_SIZE - 4, CELL_SIZE - 4);
            
            // Add shine to food
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(food.x * CELL_SIZE + CELL_SIZE / 3, food.y * CELL_SIZE + CELL_SIZE / 3, 
                          CELL_SIZE / 4, CELL_SIZE / 4);
            
            // Draw special food if active
            if (specialFoodActive) {
                // Rainbow effect for special food
                int hue = (int) (System.currentTimeMillis() % 3000) / 30;
                g2d.setColor(Color.getHSBColor(hue / 100.0f, 0.9f, 0.9f));
                g2d.fillOval(specialFood.x * CELL_SIZE, specialFood.y * CELL_SIZE, 
                              CELL_SIZE, CELL_SIZE);
                
                // Pulsating effect
                int pulseSize = 3 + (int) (Math.sin(System.currentTimeMillis() / 100.0) * 2);
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.fillOval(specialFood.x * CELL_SIZE + CELL_SIZE / 2 - pulseSize / 2, 
                              specialFood.y * CELL_SIZE + CELL_SIZE / 2 - pulseSize / 2, 
                              pulseSize, pulseSize);
            }
            
            // Draw game over or paused message
            if (isGameOver) {
                drawCenteredString(g2d, "Game Over!", GAME_WIDTH / 2, GAME_HEIGHT / 2 - 20, Color.WHITE);
                drawCenteredString(g2d, "Press SPACE to restart", GAME_WIDTH / 2, GAME_HEIGHT / 2 + 20, Color.WHITE);
            } else if (isPaused) {
                drawCenteredString(g2d, "Paused", GAME_WIDTH / 2, GAME_HEIGHT / 2, Color.WHITE);
            }
        }
        
        private void drawCenteredString(Graphics g, String text, int x, int y, Color color) {
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int textWidth = metrics.stringWidth(text);
            
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(x - textWidth/2 - 10, y - metrics.getHeight() + 5, textWidth + 20, metrics.getHeight() + 10, 10, 10);
            
            g.setColor(color);
            g.drawString(text, x - textWidth/2, y);
        }
    }
}