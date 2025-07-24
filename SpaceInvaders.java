
/**
 * Sura Gaafar
 * Space Invaders
 * 
 * 
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SpaceInvaders extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int SHIP_WIDTH = 50;
    private static final int SHIP_HEIGHT = 30;
    private static final int MONSTER_WIDTH = 50;
    private static final int MONSTER_HEIGHT = 30;
    private static final int SHOT_WIDTH = 5;
    private static final int SHOT_HEIGHT = 10;
    private static final int SHOT_SPEED = 5;
    private static final int MONSTER_SPEED = 2;
    private static final int MONSTER_ROWS = 3;
    private static final int MONSTER_COLS = 10;
    private static final int SHIELD_WIDTH = 80;
    private static final int SHIELD_HEIGHT = 50;
    private static final int SHIELD_GAP = 50;

    private ImageIcon monsterIcon, shipIcon, shotIcon;
    private ArrayList<JLabel> monsters, playerShots, monsterShots, shields;
    private JLabel shipLabel, backgroundLabel, scoreLabel, livesLabel;
    private int score, lives, shipX, monsterDirection;
    private Timer timer;
    private Random random;

    public SpaceInvaders() {
        setTitle("Space Invaders");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        initializeGame();
        setupUI();
        setupListeners();

        setVisible(true);

        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGame();
                repaint();
            }
        });
        timer.start();
    }

    private void initializeGame() {
        // Load images
        monsterIcon = new ImageIcon("monster.png");
        shipIcon = new ImageIcon("ship.png");
        shotIcon = new ImageIcon("shot.png");

        // Initialize game variables
        monsters = new ArrayList<>();
        playerShots = new ArrayList<>();
        monsterShots = new ArrayList<>();
        shields = new ArrayList<>();
        score = 0;
        lives = 3;
        shipX = (WIDTH - SHIP_WIDTH) / 2;
        monsterDirection = 1;
        random = new Random();

        // Create monsters
        for (int row = 0; row < MONSTER_ROWS; row++) {
            for (int col = 0; col < MONSTER_COLS; col++) {
                JLabel monster = new JLabel(monsterIcon);
                monster.setBounds(100 + col * 60, 50 + row * 40, MONSTER_WIDTH, MONSTER_HEIGHT);
                monsters.add(monster);
            }
        }

        // Create shields
        for (int i = 0; i < 3; i++) {
            JLabel shield = new JLabel(new ImageIcon("shield.png"));
            shield.setBounds(150 + i * 250, 450, SHIELD_WIDTH, SHIELD_HEIGHT);
            shields.add(shield);
        }
    }

    private void setupUI() {
        // Background
        backgroundLabel = new JLabel(new ImageIcon("background.jpg"));
        backgroundLabel.setBounds(0, 0, WIDTH, HEIGHT);
        add(backgroundLabel);

        // Ship
        shipLabel = new JLabel(shipIcon);
        shipLabel.setBounds(shipX, HEIGHT - 100, SHIP_WIDTH, SHIP_HEIGHT);
        backgroundLabel.add(shipLabel);

        // Score
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setBounds(20, 20, 200, 30);
        scoreLabel.setForeground(Color.WHITE);
        backgroundLabel.add(scoreLabel);

        // Lives
        livesLabel = new JLabel("Lives: " + lives);
        livesLabel.setBounds(WIDTH - 120, 20, 100, 30);
        livesLabel.setForeground(Color.WHITE);
        backgroundLabel.add(livesLabel);

        // Monsters
        for (JLabel monster : monsters) {
            backgroundLabel.add(monster);
        }

        // Shields
        for (JLabel shield : shields) {
            backgroundLabel.add(shield);
        }
    }

    private void setupListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_LEFT) {
                    if (shipX > 0) {
                        shipX -= 10;
                        shipLabel.setLocation(shipX, shipLabel.getY());
                    }
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    if (shipX < WIDTH - SHIP_WIDTH) {
                        shipX += 10;
                        shipLabel.setLocation(shipX, shipLabel.getY());
                    }
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    firePlayerShot();
                }
            }
        });
    }

    private void updateGame() {
        // Move monsters
        for (JLabel monster : monsters) {
            monster.setLocation(monster.getX() + MONSTER_SPEED * monsterDirection, monster.getY());
        }

        // Check if monsters hit the wall
        boolean hitWall = false;
        for (JLabel monster : monsters) {
            if (monster.getX() <= 0 || monster.getX() >= WIDTH - MONSTER_WIDTH) {
                hitWall = true;
                break;
            }
        }
        if (hitWall) {
            monsterDirection *= -1;
            for (JLabel monster : monsters) {
                monster.setLocation(monster.getX(), monster.getY() + 20);
            }
        }

        // Move player shots
        for (JLabel shot : playerShots) {
            shot.setLocation(shot.getX(), shot.getY() - SHOT_SPEED);
        }

        // Move monster shots
        for (JLabel shot : monsterShots) {
            shot.setLocation(shot.getX(), shot.getY() + SHOT_SPEED);
        }

        // Check for collisions
        checkCollisions();

        // Monster shooting
        if (random.nextInt(100) < 2) { // Adjust the probability of shooting
            int randomMonsterIndex = random.nextInt(monsters.size());
            JLabel monster = monsters.get(randomMonsterIndex);
            fireMonsterShot(monster.getX() + MONSTER_WIDTH / 2, monster.getY() + MONSTER_HEIGHT);
        }
    }

    private void checkCollisions() {
        // Player shot vs monsters
        for (int i = 0; i < playerShots.size(); i++) {
            JLabel shot = playerShots.get(i);
            for (int j = 0; j < monsters.size(); j++) {
                JLabel monster = monsters.get(j);
                if (shot.getBounds().intersects(monster.getBounds())) {
                    score += 10;
                    scoreLabel.setText("Score: " + score);
                    remove(monster);
                    remove(shot);
                    monsters.remove(monster);
                    playerShots.remove(shot);
                    break;
                }
            }
        }

        // Monster shot vs player
        for (int i = 0; i < monsterShots.size(); i++) {
            JLabel shot = monsterShots.get(i);
            if (shot.getBounds().intersects(shipLabel.getBounds())) {
                lives--;
                livesLabel.setText("Lives: " + lives);
                remove(shot);
                monsterShots.remove(shot);
                if (lives == 0) {
                    endGame();
                }
                break;
            }
        }
    }

    private void firePlayerShot() {
        JLabel shot = new JLabel(shotIcon);
        shot.setBounds(shipX + SHIP_WIDTH / 2 - SHOT_WIDTH / 2, HEIGHT - 100 - SHOT_HEIGHT, SHOT_WIDTH, SHOT_HEIGHT);
        backgroundLabel.add(shot);
        playerShots.add(shot);
    }

    private void fireMonsterShot(int x, int y) {
        JLabel shot = new JLabel(shotIcon);
        shot.setBounds(x - SHOT_WIDTH / 2, y, SHOT_WIDTH, SHOT_HEIGHT);
        backgroundLabel.add(shot);
        monsterShots.add(shot);
    }

    private void endGame() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over!\nYour Score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpaceInvaders());
    }
}
