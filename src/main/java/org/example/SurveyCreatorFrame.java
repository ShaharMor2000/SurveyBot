package org.example;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SurveyCreatorFrame extends JFrame {
    private SurveyBot bot;
    private ChatGptApiClient chatGptClient;
    //Mode selection
    private JRadioButton manualRadio = new JRadioButton("יצירה ידנית", true);
    private JRadioButton autoRadio = new JRadioButton("יצירה אוטומטית (ChatGPT)");

    //Delay in minutes before sending the survey
    private JTextField delayField = new JTextField("0", 5);

    //Automatic mode ChatGPT
    private JTextField topicField = new JTextField(20);
    private JSpinner numQuestionsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
    private JButton generateBtn = new JButton("צור שאלות מה-API");

    //Manual mode
    private JTextField[] questionFields = new JTextField[3];
    private JTextField[][] answerFields = new JTextField[3][4];
    private JButton createPollBtn = new JButton("שלח סקר");
    private JButton showResultsBtn = new JButton("הצג תוצאות");
    private JTextArea resultsArea = new JTextArea(10, 40);

    //Panels we enable/disable according to selected mode
    private JPanel autoPanel;
    private JPanel manualPanel;
    public SurveyCreatorFrame(SurveyBot bot, ChatGptApiClient chatGptClient) {
        super(" יצירת סקר SurveyBot");
        this.bot = bot;
        this.chatGptClient = chatGptClient;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        Font font = new Font("Arial", Font.PLAIN, 14);
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("RadioButton.font", font);
        UIManager.put("Spinner.font", font);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        titlePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JLabel title = new JLabel(" יצירת סקר חדש");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(title);
        contentPanel.add(titlePanel);
        contentPanel.add(Box.createVerticalStrut(10));

        //settings
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "הגדרות סקר",
                TitledBorder.RIGHT,
                TitledBorder.TOP
        ));
        settingsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        //Mode selection
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        modePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        ButtonGroup group = new ButtonGroup();
        group.add(manualRadio);
        group.add(autoRadio);
        modePanel.add(new JLabel("מצב יצירה: "));
        modePanel.add(manualRadio);
        modePanel.add(autoRadio);

        //Delay configuration
        JPanel delayPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        delayPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        delayPanel.add(new JLabel("עיכוב (דקות לפני שליחה):"));
        delayPanel.add(delayField);
        settingsPanel.add(modePanel);
        settingsPanel.add(delayPanel);
        contentPanel.add(settingsPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Automatic mode
        autoPanel = new JPanel();
        autoPanel.setLayout(new BoxLayout(autoPanel, BoxLayout.Y_AXIS));
        autoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "יצירה אוטומטית בעזרת ChatGPT",
                TitledBorder.RIGHT,
                TitledBorder.TOP
        ));
        autoPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JPanel autoRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        autoRow1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        autoRow1.add(new JLabel("נושא כללי:"));
        autoRow1.add(topicField);
        JPanel autoRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        autoRow2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        autoRow2.add(new JLabel("מספר שאלות:"));
        autoRow2.add(numQuestionsSpinner);
        autoRow2.add(generateBtn);
        JLabel autoHint = new JLabel("במצב זה הסקר ייבנה לפי נושא, במידה ותרצה לערוך את המלל יש לעבור למצב יצירה ידנית");
        autoHint.setForeground(new Color(120, 80, 80));
        autoPanel.add(autoRow1);
        autoPanel.add(autoRow2);
        autoPanel.add(autoHint);
        contentPanel.add(autoPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Manual mode panel
        manualPanel = new JPanel();
        manualPanel.setLayout(new BoxLayout(manualPanel, BoxLayout.Y_AXIS));
        manualPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "יצירה ידנית של שאלות ותשובות",
                TitledBorder.RIGHT,
                TitledBorder.TOP
        ));
        manualPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        for (int i = 0; i < 3; i++) {
            JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            qPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            qPanel.add(new JLabel("שאלה " + (i + 1) + ":"));
            questionFields[i] = new JTextField(25);
            qPanel.add(questionFields[i]);
            manualPanel.add(qPanel);

            JPanel aPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            aPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            aPanel.add(new JLabel("תשובות (עד 4):"));
            for (int j = 0; j < 4; j++) {
                answerFields[i][j] = new JTextField(8);
                aPanel.add(answerFields[i][j]);
            }
            manualPanel.add(aPanel);
            if (i < 2) {
                manualPanel.add(new JSeparator());
            }
        }
        contentPanel.add(manualPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        buttonsPanel.add(createPollBtn);
        buttonsPanel.add(showResultsBtn);
        contentPanel.add(buttonsPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "📊 תוצאות אחרונות",
                TitledBorder.RIGHT,
                TitledBorder.TOP
        ));
        contentPanel.add(resultsScroll);

        // Wrap everything in a scroll pane
        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        setContentPane(mainScroll);
        generateBtn.addActionListener(this::onGenerateFromApi);
        createPollBtn.addActionListener(this::onCreatePoll);
        showResultsBtn.addActionListener(this::onShowResults);
        manualRadio.addActionListener(e -> updateModeUI());
        autoRadio.addActionListener(e -> updateModeUI());

        // Initial mode manual creation
        updateModeUI();
        pack();
        setLocationRelativeTo(null);
    }
    // Enable/disable panels according to the selected mode
    private void updateModeUI() {
        boolean manual = manualRadio.isSelected();
        setPanelEnabledRecursive(manualPanel, manual);
        setPanelEnabledRecursive(autoPanel, !manual);
    }
    //Recursively enable/disable all child components of a container
    private void setPanelEnabledRecursive(Container container, boolean enabled) {
        if (container == null) return;
        for (Component c : container.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container child) {
                setPanelEnabledRecursive(child, enabled);
            }
        }
    }
    // Questions via API in automatic mode
    private void onGenerateFromApi(ActionEvent e) {
        if (!autoRadio.isSelected()) {
            JOptionPane.showMessageDialog(
                    this,
                    "כדי להשתמש ב-ChatGPT, בחרי 'יצירה אוטומטית (ChatGPT)'",
                    "הערה",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        String topic = topicField.getText().trim();
        int numQ = (int) numQuestionsSpinner.getValue();

        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "נא להזין נושא", "שגיאה", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!chatGptClient.checkBalance()) {
            JOptionPane.showMessageDialog(this, "לא ניתן להשתמש ב-API", "שגיאה", JOptionPane.ERROR_MESSAGE);
            return;
        }
        chatGptClient.clearHistory();
        List<SurveyQuestion> questions = chatGptClient.generateQuestionsFromTopic(topic, numQ);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "לא התקבלו שאלות מה-API", "שגיאה", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // manual fields with the generated questions
        for (int i = 0; i < 3; i++) {
            if (i < questions.size()) {
                SurveyQuestion q = questions.get(i);
                questionFields[i].setText(q.getText());
                for (int j = 0; j < 4; j++) {
                    if (j < q.getAnswers().size()) {
                        answerFields[i][j].setText(q.getAnswers().get(j).getText());
                    } else {
                        answerFields[i][j].setText("");
                    }
                }
            } else {
                questionFields[i].setText("");
                for (int j = 0; j < 4; j++) {
                    answerFields[i][j].setText("");
                }
            }
        }
    }
    // Survey in manual mode, questions from fields, in automatic mode create via ChatGPT, and send with the selected delay
    private void onCreatePoll(ActionEvent e) {
        try {
            int delay = Integer.parseInt(delayField.getText().trim());
            if (delay < 0) delay = 0;
            List<SurveyQuestion> questions;
            if (manualRadio.isSelected()) {
                questions = buildQuestionsFromFields();
                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "במצב ידני צריך לפחות שאלה אחת עם 2–4 תשובות",
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } else {
                String topic = topicField.getText().trim();
                int numQ = (int) numQuestionsSpinner.getValue();
                if (topic.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "במצב ChatGPT צריך להזין נושא",
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                if (!chatGptClient.checkBalance()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "לא ניתן להשתמש ב-API",
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                chatGptClient.clearHistory();
                questions = chatGptClient.generateQuestionsFromTopic(topic, numQ);
                if (questions.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "ChatGPT לא החזיר שאלות",
                            "שגיאה",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }
            bot.createSurveyFromSwing(questions, delay);
            JOptionPane.showMessageDialog(
                    this,
                    "הסקר נוצר ויישלח לקהילה בהתאם לעיכוב",
                    "הצלחה",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "עיכוב חייב להיות מספר שלם", "שגיאה", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "שגיאה", JOptionPane.ERROR_MESSAGE);
        }
    }
    //List of SurveyQuestion objects from the text fields
    private List<SurveyQuestion> buildQuestionsFromFields() {
        List<SurveyQuestion> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String qText = questionFields[i].getText().trim();
            if (qText.isEmpty()) continue;
            List<SurveyAnswer> answers = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                String aText = answerFields[i][j].getText().trim();
                if (!aText.isEmpty()) {
                    answers.add(new SurveyAnswer(j, aText));
                }
            }
            if (answers.size() < 2 || answers.size() > 4) {
                return new ArrayList<>();
            }
            result.add(new SurveyQuestion(i, qText, answers));
        }
        return result;
    }
    //Last survey results as text in the resultsArea.
    private void onShowResults(ActionEvent e) {
        resultsArea.setText(bot.getSurveyResultsText());
    }
}