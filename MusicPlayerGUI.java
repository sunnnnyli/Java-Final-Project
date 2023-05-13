/*
Sunny Li
Final Project
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MusicPlayerGUI extends JFrame {
	private String title = "";
	private JLabel titleLabel;
	private JButton playButton, pauseButton, backButton, nextButton;
	private JProgressBar progressBar;
	private JPanel paintPanel;
	private Image image = null;

	private boolean play = true;
	private boolean pause = false;
	private boolean back = false;
	private boolean next = false;
	private boolean seek = false;
	private int jump;
	private boolean jobDone = true;
	private boolean loop = false;

	public MusicPlayerGUI() {
		super("Music Player");

		// Create title label
		titleLabel = new JLabel(title, SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

		// Create buttons
		playButton = new JButton("Play");
		pauseButton = new JButton("Pause");
		backButton = new JButton("Previous");
		nextButton = new JButton("Skip");

		// Set button colors
		playButton.setBackground(new Color(220, 220, 220));
		pauseButton.setBackground(new Color(220, 220, 220));
		backButton.setBackground(new Color(220, 220, 220));
		nextButton.setBackground(new Color(220, 220, 220));

		// Set button margins
		playButton.setMargin(new Insets(10, 20, 10, 20));
		pauseButton.setMargin(new Insets(10, 20, 10, 20));
		backButton.setMargin(new Insets(10, 20, 10, 20));
		nextButton.setMargin(new Insets(10, 20, 10, 20));

		// Create panel for buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(new Color(240, 240, 240));
		buttonPanel.setLayout(new GridLayout(1, 4, 10, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPanel.add(backButton);
		buttonPanel.add(playButton);
		buttonPanel.add(pauseButton);
		buttonPanel.add(nextButton);

		// Create progress bar
		progressBar = new JProgressBar(0, 100);
		progressBar.setForeground(new Color(47, 160, 255));
		progressBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		progressBar.setStringPainted(true);

		// Create paint panel
		paintPanel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;

				// Draw a radial gradient background
				GradientPaint gradientPaint = new GradientPaint(getWidth() / 2, getHeight() / 2, new Color(0x1E90FF), getWidth(), getHeight(), new Color(0x00008B));
				g2d.setPaint(gradientPaint);
				g2d.fillRect(0, 0, getWidth(), getHeight());

    			// Draw reflection of the image
				if (image != null) {
					BufferedImage reflection = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
					Graphics2D reflectionGraphics = reflection.createGraphics();
					reflectionGraphics.drawImage(image, null, null);
					reflectionGraphics.scale(1, -1);
					reflectionGraphics.translate(0, -image.getHeight(null));
					reflectionGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
					reflectionGraphics.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 0), 0, reflection.getHeight(), new Color(255, 255, 255, 255)));
					reflectionGraphics.fillRect(0, 0, reflection.getWidth(), reflection.getHeight() * 2);
					reflectionGraphics.dispose();
					g2d.drawImage(reflection, 0, image.getHeight(null), null);

    				// Draw image
					g2d.drawImage(image, 0, 0, null);
				}

				// Draw shadow of the image
				if (image != null) {
    				// Create shadow
					BufferedImage shadow = new BufferedImage(image.getWidth(null) + 10, image.getHeight(null) + 10, BufferedImage.TYPE_INT_ARGB);
					Graphics2D shadowGraphics = shadow.createGraphics();
					shadowGraphics.drawImage(image, 5, 5, null);
					shadowGraphics.dispose();

    				// Draw shadow
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					g2d.drawImage(shadow, 5, 5, null);

    				// Draw image
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
					g2d.drawImage(image, 0, 0, null);
				}
			}
		};

		// Add components to content pane using nested BorderLayout
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// Create panel for paint panel and button panel using nested BorderLayout
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(paintPanel, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);

		contentPane.add(titleLabel, BorderLayout.NORTH);
		contentPane.add(topPanel, BorderLayout.CENTER);
		contentPane.add(progressBar, BorderLayout.SOUTH);

		// Set window properties
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);

		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				play = true;
				pause = false;
				back = false;
				next = false;
				jobDone = false;
			}
		});

		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				play = false;
				pause = true;
				back = false;
				next = false;
				jobDone = false;
			}
		});

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				play = false;
				pause = false;
				back = true;
				next = false;
				jobDone = false;
			}
		});

		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				play = false;
				pause = false;
				back = false;
				next = true;
				jobDone = false;
			}
		});

		// Add listener for progress bar
		progressBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int mouseX = e.getX();
				int progressBarWidth = progressBar.getWidth();
				double percentage = (double)mouseX / (double)progressBarWidth;
				int value = (int)(percentage * progressBar.getMaximum());
				progressBar.setValue(value);
				jump = value;
				seek = true;
				jobDone = false;
			}
		});

	}

	public void updateImage(String name) {
		image = new ImageIcon(name).getImage();
		image = resizeImage(image, paintPanel.getWidth(), paintPanel.getHeight());
		paintPanel.repaint();
	}

	public void updateTitle(String name) {
		title = name;
		title = name.substring(0, name.length() - 4); // Remove the last 4 characters (".jpg")
		titleLabel.setText(title);
	}

	private Image resizeImage(Image originalImage, int panelWidth, int panelHeight) {
    	// Create a new blank image with the same dimensions as the panel
		Image resizedImage = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);

    	// Scale the original image to fit the width of the panel
		double scale = (double) panelWidth / (double) originalImage.getWidth(null);
		int newWidth = panelWidth;
		int newHeight = (int) (originalImage.getHeight(null) * scale);

    	// If the scaled height is greater than the panel height, scale the image to fit the height instead
		if (newHeight > panelHeight) {
			scale = (double) panelHeight / (double)originalImage.getHeight(null);
			newWidth = (int) (originalImage.getWidth(null) * scale);
			newHeight = panelHeight;
		}

    	// Use Graphics2D to draw the resized image onto the new blank image
		Graphics2D g2d = (Graphics2D) resizedImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(originalImage, (panelWidth - newWidth) / 2, (panelHeight - newHeight) / 2, newWidth, newHeight, null);
		g2d.dispose();

		return resizedImage;
	}

	public void updateProgressBar(long curMS, long fullMS) {
		double result = (double) curMS / fullMS;
		int val = (int) (result * 100);
		progressBar.setValue(val);
	}

	public boolean isPlay() {
		return play;
	}
	
	public boolean isPause() {
		return pause;
	}
	
	public boolean isBack() {
		return back;
	}
	
	public boolean isNext() {
		return next;
	}

	public boolean isSeek() {
		return seek;
	}

	public int getJump() {
		return jump;
	}
	
	public boolean jobFinished() {
		return jobDone;
	}
	
	public void finishedJob() {
		jobDone = true;
		seek = false;
	}
}
