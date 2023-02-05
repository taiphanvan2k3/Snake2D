import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
//import java.util.Timer;

import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener {
	static final int SCREEN_WIDTH = 600;
	static final int SCREEN_HEIGHT = 600;

	// 1 đoạn trong game là 1 hình vuông 25x25
	static final int UNIT_SIZE = 25;
	static final int DELAY = 75;
	static final int GAME_UNITS = (SCREEN_HEIGHT * SCREEN_WIDTH) / UNIT_SIZE;
	int X[] = new int[GAME_UNITS];
	int Y[] = new int[GAME_UNITS];

	// initial thì rắn có 6 đoạn
	int bodyParts = 3;
	int applesEaten;
	boolean checkAppleEaten = false;
	int appleX, appleY;

	// Ban đầu cho rắn chạy về bên phải
	char direction = 'R';
	boolean running = false;
	Timer timer;
	Random random;
	Thread threadForRenderApple;

	// Thời gian chơi game:
	Thread threadForPlayingTime;
	int seconds, minutes;

	public GamePanel() {
		random = new Random();
		this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		this.setBackground(Color.black);
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		this.startGame();
	}

	public void startGame() {
		newApple();
		running = true;
		// Timer này của javaw.swing, DELAY ms
		timer = new Timer(DELAY, this);
		// gọi sự kiện bên trong phương thức actionPerform
		timer.start();
		threadForRenderApple = new Thread() {
			public void run() {
				while (true) {
					if (checkAppleEaten) {
						try {
							threadForRenderApple.sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						newApple();
					}
				}
			}
		};

		threadForPlayingTime = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						updatePlayingTime();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		threadForPlayingTime.start();
		threadForRenderApple.start();
	}

	private void restartGame() {
		running = true;
		bodyParts = 3;
		applesEaten = 0;
		minutes = seconds = 0;
		direction = 'R';
		X = new int[GAME_UNITS];
		Y = new int[GAME_UNITS];
		timer.start();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	// Ko phải phương thức override
	public void draw(Graphics g) {
		if (running) {
			// System.out.println(g.getColor());
			for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
				// Vẽ các đường thẳng để chia lưới panel
				g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
				g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
			}

			if (!checkAppleEaten) {
				g.setColor(Color.red);
				g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
			}

			for (int i = 0; i < bodyParts; i++) {
				if (i == 0) {
					g.setColor(Color.green);
					g.fillRect(X[i], Y[i], UNIT_SIZE, UNIT_SIZE);
				} else {
					g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
					g.fillRect(X[i], Y[i], UNIT_SIZE, UNIT_SIZE);
				}
			}

			g.setColor(Color.red);
			g.setFont(new Font("Ink Free", Font.BOLD, 40));
			FontMetrics metrics = getFontMetrics(g.getFont());
			String s = "Score: " + applesEaten;
			g.drawString(s, (SCREEN_WIDTH - metrics.stringWidth(s)) / 2, g.getFont().getSize());

		} else
			gameOver(g);
	}

	public void move() {
		// Cập nhật phần thân của con rắn
		for (int i = bodyParts; i > 0; i--) {
			X[i] = X[i - 1];
			Y[i] = Y[i - 1];
		}

		switch (direction) {
		// Cập nhật toạ độ đầu con rắn
		case 'U':
			Y[0] = Y[0] - UNIT_SIZE;
			break;
		case 'D':
			Y[0] = Y[0] + UNIT_SIZE;
			break;
		case 'L':
			X[0] = X[0] - UNIT_SIZE;
			break;
		case 'R':
			X[0] = X[0] + UNIT_SIZE;
			break;
		}
	}

	private void updatePlayingTime() {
		seconds++;
		if (seconds == 60) {
			seconds = 0;
			minutes++;
		}
		Graphics g = this.getGraphics();
		g.setColor(Color.blue);
		g.setFont(new Font("Ink Free", Font.BOLD, 30));
		FontMetrics metrics = getFontMetrics(g.getFont());
		String s = minutes + ":" + seconds;
		g.drawString(s, SCREEN_WIDTH * 3 / 4, g.getFont().getSize());
		;
	}

	public void newApple() {
		// Xác định vị trí của apple trên lưới
		// random.nextInt(SCREEN_WIDTH / UNIT_SIZE) trả về index trên mảng X
		appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
		appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
		checkAppleEaten = false;
	}

	public void checkApple() {
		if (X[0] == appleX && Y[0] == appleY) {
			bodyParts++;
			applesEaten++;
			checkAppleEaten = true;
		}
	}

	public void checkCollision() {
		// Kiểm tra nếu đầu va chạm với thân
		for (int i = bodyParts; i > 0; i--) {
			if ((X[0] == X[i]) && (Y[0] == Y[i])) {
				running = false;
			}
		}

		// kiểm tra đầu chạm với tường trái
		if (X[0] < 0)
			running = false;
		// kiểm tra đầu chạm với tường phải
		if (X[0] > SCREEN_WIDTH)
			running = false;
		// kiểm tra đầu chạm với tường trên
		if (Y[0] < 0)
			running = false;
		// kiểm tra đầu chạm với tường dưới
		if (Y[0] > SCREEN_HEIGHT)
			running = false;
		if (!running) {
			timer.stop();
		}
	}

	public void gameOver(Graphics g) {
		threadForPlayingTime.stop();
		// Score
		g.setColor(Color.red);
		g.setFont(new Font("Ink Free", Font.BOLD, 40));
		FontMetrics metrics1 = getFontMetrics(g.getFont());
		String s1 = "Score: " + applesEaten;
		g.drawString(s1, (SCREEN_WIDTH - metrics1.stringWidth(s1)) / 2, g.getFont().getSize());

		// Game over text
		g.setColor(Color.red);
		g.setFont(new Font("Ink Free", Font.BOLD, 75));
		FontMetrics metrics2 = getFontMetrics(g.getFont());
		String s2 = "Game Over";
		g.drawString(s2, (SCREEN_WIDTH - metrics2.stringWidth(s2)) / 2, (SCREEN_HEIGHT / 2));
		;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (running) {
			move();
			checkApple();
			checkCollision();
		}
		repaint();
	}

	public class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				if (direction != 'R')
					direction = 'L';
				break;
			case KeyEvent.VK_RIGHT:
				if (direction != 'L')
					direction = 'R';
				break;
			case KeyEvent.VK_UP:
				if (direction != 'D')
					direction = 'U';
				break;
			case KeyEvent.VK_DOWN:
				if (direction != 'U')
					direction = 'D';
				break;
			case KeyEvent.VK_ENTER:
				if (!running)
					restartGame();
			}
		}
	}
}
