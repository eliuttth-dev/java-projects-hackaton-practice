import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.sound.sampled.*;

public class Stopwatch extends JFrame {
    private Timer stopwatchTimer;
    private Timer countdownTimer;
    private double elapsedTime = 0;
    private double countdownTime = 0;
    private boolean isRunning = false;
    
    private JLabel stopwatchLabel;
    private JLabel timerLabel;
    private JTextField timerInput;
    private JTextArea lapsArea;
    private ArrayList<Double> lapTimes = new ArrayList<>();
    
    private DecimalFormat df = new DecimalFormat("0.00");

    public Stopwatch () {
        setTitle("Stopwatch & Timer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1));
        setSize(300, 400);

        JPanel stopwatchPanel = new JPanel();
        stopwatchPanel.setLayout(new FlowLayout());
        
        stopwatchLabel = new JLabel("00:00.00");
        stopwatchLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton resetButton = new JButton("Reset");
        JButton lapButton = new JButton("Lap");
        
        stopwatchPanel.add(stopwatchLabel);
        stopwatchPanel.add(startButton);
        stopwatchPanel.add(stopButton);
        stopwatchPanel.add(resetButton);
        stopwatchPanel.add(lapButton);
        
        // Timer Panel
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new FlowLayout());
        
        timerLabel = new JLabel("00:00.00");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        
        timerInput = new JTextField("10", 5); // Default 10 seconds
        JButton setTimerButton = new JButton("Set Timer");
        
        timerPanel.add(new JLabel("Seconds:"));
        timerPanel.add(timerInput);
        timerPanel.add(setTimerButton);
        timerPanel.add(timerLabel);
        
        // Laps Display
        lapsArea = new JTextArea(10, 20);
        lapsArea.setEditable(false);
        
        // Add panels to frame
        add(stopwatchPanel);
        add(timerPanel);
        add(new JScrollPane(lapsArea));
        
        // Stopwatch Timer
        stopwatchTimer = new Timer(10, e -> {
            elapsedTime += 0.01;
            updateStopwatchLabel();
        });
        
        // Action Listeners
        startButton.addActionListener(e -> {
            if (!isRunning) {
                stopwatchTimer.start();
                isRunning = true;
            }
        });
        
        stopButton.addActionListener(e -> {
            if (isRunning) {
                stopwatchTimer.stop();
                isRunning = false;
            }
        });
        
        resetButton.addActionListener(e -> {
            stopwatchTimer.stop();
            isRunning = false;
            elapsedTime = 0;
            updateStopwatchLabel();
            lapTimes.clear();
            lapsArea.setText("");
        });
        
        lapButton.addActionListener(e -> {
            if (isRunning) {
                lapTimes.add(elapsedTime);
                updateLapsDisplay();
            }
        });
        
        setTimerButton.addActionListener(e -> {
            try {
                countdownTime = Double.parseDouble(timerInput.getText());
                updateTimerLabel();
                if (countdownTimer != null && countdownTimer.isRunning()) {
                    countdownTimer.stop();
                }
                countdownTimer = new Timer(10, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        countdownTime -= 0.01;
                        updateTimerLabel();
                        if (countdownTime <= 0) {
                            countdownTimer.stop();
                            playSound();
                            JOptionPane.showMessageDialog(null, "Timer Finished!");
                        }
                    }
                });
                countdownTimer.start();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number");
            }
        });
    }
    
    private void updateStopwatchLabel() {
        int minutes = (int) (elapsedTime / 60);
        double seconds = elapsedTime % 60;
        stopwatchLabel.setText(String.format("%02d:%05.2f", minutes, seconds));
    }
    
    private void updateTimerLabel() {
        if (countdownTime < 0) countdownTime = 0;
        int minutes = (int) (countdownTime / 60);
        double seconds = countdownTime % 60;
        timerLabel.setText(String.format("%02d:%05.2f", minutes, seconds));
    }
    
    private void updateLapsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lapTimes.size(); i++) {
            int minutes = (int) (lapTimes.get(i) / 60);
            double seconds = lapTimes.get(i) % 60;
            sb.append("Lap ").append(i + 1).append(": ")
              .append(String.format("%02d:%05.2f", minutes, seconds))
              .append("\n");
        }
        lapsArea.setText(sb.toString());
    }
    
    private void playSound() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Stopwatch().setVisible(true);
        });
    }
}
