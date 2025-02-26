import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;

public class QuizApp extends JFrame {
    private ArrayList<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private JLabel questionLabel, timerLabel;
    private JButton[] optionButtons;
    private Timer timer;
    private int timeLeft = 10; // 10 seconds per question

    public QuizApp() {
        setTitle("Quiz Application");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        loadQuestions();

        questionLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel = new JLabel("Time Left: 10", SwingConstants.CENTER);
        optionButtons = new JButton[4];

        JPanel questionPanel = new JPanel(new GridLayout(2, 1));
        questionPanel.add(questionLabel);
        questionPanel.add(timerLabel);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].addActionListener(new OptionListener());
            optionsPanel.add(optionButtons[i]);
        }

        add(questionPanel, BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);

        // Start the quiz
        displayQuestion();
        startTimer();
    }

    private static class Question {
        String text;
        String[] options;
        int correctAnswer;

        Question(String text, String[] options, int correctAnswer) {
            this.text = text;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    private void loadQuestions() {
        questions = new ArrayList<>();
        questions.add(new Question(
            "What is the capital of France?",
            new String[]{"Paris", "London", "Berlin", "Madrid"},
            0
        ));
        questions.add(new Question(
            "Which planet is known as the Red Planet?",
            new String[]{"Earth", "Mars", "Jupiter", "Venus"},
            1
        ));
        questions.add(new Question(
            "What is 2 + 2?",
            new String[]{"3", "4", "5", "6"},
            1
        ));
        Collections.shuffle(questions); // Randomize questions
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            questionLabel.setText(q.text);
            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText(q.options[i]);
                optionButtons[i].setEnabled(true);
            }
            timeLeft = 10;
            timerLabel.setText("Time Left: " + timeLeft);
        } else {
            endQuiz();
        }
    }

    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time Left: " + timeLeft);
            if (timeLeft <= 0) {
                timer.stop();
                nextQuestion(false); // Time's up, no points
            }
        });
        timer.start();
    }

    private void nextQuestion(boolean isCorrect) {
        if (isCorrect) score++;
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
            startTimer();
        } else {
            endQuiz();
        }
    }

    private void endQuiz() {
        timer.stop();
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        JLabel resultLabel = new JLabel(
            "Quiz Over! Your Score: " + score + "/" + questions.size(),
            SwingConstants.CENTER
        );
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> {
            currentQuestionIndex = 0;
            score = 0;
            Collections.shuffle(questions);
            getContentPane().removeAll();
            setLayout(new BorderLayout(10, 10));
            add(questionLabel, BorderLayout.NORTH);
            add(new JPanel(new GridLayout(2, 2, 10, 10)) {{
                for (JButton btn : optionButtons) add(btn);
            }}, BorderLayout.CENTER);
            displayQuestion();
            startTimer();
            revalidate();
            repaint();
        });

        add(resultLabel, BorderLayout.CENTER);
        add(restartButton, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    private class OptionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            JButton clicked = (JButton) e.getSource();
            Question q = questions.get(currentQuestionIndex);
            boolean isCorrect = clicked.getText().equals(q.options[q.correctAnswer]);
            JOptionPane.showMessageDialog(
                QuizApp.this,
                isCorrect ? "Correct!" : "Wrong! Correct answer: " + q.options[q.correctAnswer]
            );
            for (JButton btn : optionButtons) btn.setEnabled(false); // Disable buttons after selection
            Timer delay = new Timer(1000, evt -> nextQuestion(isCorrect));
            delay.setRepeats(false);
            delay.start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizApp().setVisible(true));
    }
}
