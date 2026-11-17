import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Moodscape {

    static final String FILE_NAME = "moodscape.txt";

    static class JournalEntry {
        String mood;
        String title;
        String note;
        String date;

        JournalEntry(String mood, String title, String note) {
            this.mood = mood;
            this.title = title;
            this.note = note;
            this.date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
            );
        }

        public String toString() {
            return mood + " - " + title;
        }
    }

    static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes());
    }

    static String decode(String text) {
        return new String(Base64.getDecoder().decode(text));
    }

    static void saveToFile(DefaultListModel<JournalEntry> model) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < model.size(); i++) {
                JournalEntry entry = model.getElementAt(i);
                writer.println(
                    encode(entry.mood) + "|" +
                    encode(entry.title) + "|" +
                    encode(entry.note) + "|" +
                    encode(entry.date)
                );
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan jurnal.");
        }
    }

    static void loadFromFile(DefaultListModel<JournalEntry> model) {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|", -1);

                if (data.length == 4) {
                    try {
                        JournalEntry entry = new JournalEntry(
                            decode(data[0]),
                            decode(data[1]),
                            decode(data[2])
                        );
                        entry.date = decode(data[3]);
                        model.addElement(entry);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Data jurnal dilewati.");
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal membaca file jurnal.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Moodscape - Personal Journal");
            frame.setSize(780, 560);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            Color lavender = new Color(225, 216, 241);
            Color background = new Color(250, 247, 252);
            Color dark = new Color(75, 65, 90);

            JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
            mainPanel.setBackground(background);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

            // Header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(lavender);

            JLabel titleLabel = new JLabel("  Moodscape");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 25));
            titleLabel.setForeground(dark);

            JLabel subtitleLabel = new JLabel("A little space for your feelings  ");
            subtitleLabel.setForeground(dark);

            headerPanel.add(titleLabel, BorderLayout.WEST);
            headerPanel.add(subtitleLabel, BorderLayout.EAST);

            // Model untuk menyimpan daftar jurnal
            DefaultListModel<JournalEntry> journalModel = new DefaultListModel<>();
            loadFromFile(journalModel);

            // Tab untuk memisahkan halaman
            JTabbedPane tabbedPane = new JTabbedPane();

            // Halaman New Journal
            JPanel formPanel = new JPanel(new GridLayout(0, 1, 8, 8));
            formPanel.setBackground(background);
            formPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));

            JLabel moodLabel = new JLabel("How are you feeling today?");
            String[] moods = {"Happy", "Calm", "Sad", "Tired", "Anxious"};
            JComboBox<String> moodBox = new JComboBox<>(moods);

            JLabel titleInputLabel = new JLabel("Journal title:");
            JTextField titleField = new JTextField();

            JLabel noteLabel = new JLabel("Write about your day:");
            JTextArea noteArea = new JTextArea(6, 25);
            noteArea.setLineWrap(true);
            noteArea.setWrapStyleWord(true);
            JScrollPane noteScroll = new JScrollPane(noteArea);

            JButton saveButton = new JButton("Save Journal");
            saveButton.setBackground(lavender);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(background);
            buttonPanel.add(saveButton);

            formPanel.add(moodLabel);
            formPanel.add(moodBox);
            formPanel.add(titleInputLabel);
            formPanel.add(titleField);
            formPanel.add(noteLabel);
            formPanel.add(noteScroll);
            formPanel.add(buttonPanel);

            // Halaman Journal History
            JPanel historyPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            historyPanel.setBackground(background);
            historyPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JList<JournalEntry> journalList = new JList<>(journalModel);
            JScrollPane listScroll = new JScrollPane(journalList);

            JTextArea detailArea = new JTextArea();
            detailArea.setEditable(false);
            detailArea.setLineWrap(true);
            detailArea.setWrapStyleWord(true);
            detailArea.setText("Select a journal to see its details.");
            JScrollPane detailScroll = new JScrollPane(detailArea);

            JPanel detailContainer = new JPanel(new BorderLayout(5, 5));
            detailContainer.add(new JLabel("Journal Details"), BorderLayout.NORTH);
            detailContainer.add(detailScroll, BorderLayout.CENTER);

            JButton deleteButton = new JButton("Delete Selected");
            JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            deletePanel.add(deleteButton);
            detailContainer.add(deletePanel, BorderLayout.SOUTH);

            historyPanel.add(listScroll);
            historyPanel.add(detailContainer);

            tabbedPane.addTab("New Journal", formPanel);
            tabbedPane.addTab("Journal History", historyPanel);

            // Event Listener: menyimpan jurnal
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String title = titleField.getText().trim();
                    String note = noteArea.getText().trim();
                    String mood = moodBox.getSelectedItem().toString();

                    if (title.isEmpty() || note.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Please complete the title and journal note.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        journalModel.addElement(new JournalEntry(mood, title, note));
                        saveToFile(journalModel);

                        titleField.setText("");
                        noteArea.setText("");

                        JOptionPane.showMessageDialog(
                            frame,
                            "Your journal has been saved!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            });

            // Event Listener: menampilkan detail jurnal
            journalList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        JournalEntry entry = journalList.getSelectedValue();

                        if (entry != null) {
                            detailArea.setText(
                                "Date: " + entry.date +
                                "\nMood: " + entry.mood +
                                "\nTitle: " + entry.title +
                                "\n\n" + entry.note
                            );
                        }
                    }
                }
            });

            // Event Listener: menghapus jurnal
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = journalList.getSelectedIndex();

                    if (index == -1) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Select a journal first.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        int confirm = JOptionPane.showConfirmDialog(
                            frame,
                            "Delete this journal?",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION
                        );

                        if (confirm == JOptionPane.YES_OPTION) {
                            journalModel.remove(index);
                            saveToFile(journalModel);
                            detailArea.setText("Select a journal to see its details.");
                        }
                    }
                }
            });

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(tabbedPane, BorderLayout.CENTER);

            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

}