import javax.swing.JFrame;

public class GameFrame extends JFrame {

	private void initFrame() {
		this.setTitle("Snake");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		// fit Causes this Window to be sized to fit the preferred sizeand layouts of
		// its subcomponents
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
	}

	public GameFrame() {
		this.add(new GamePanel());
		this.initFrame();
	}
}
