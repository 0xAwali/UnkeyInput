package burpsuite;

import burp.api.montoya.persistence.PersistedObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static burpsuite.UnkeyInput.*;

public class Settings extends JPanel {

    PersistedObject persistence;
    public Settings(PersistedObject persist) {

        this.persistence = persist;
        // Set the layout to GridBagLayout for precise control
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(20, 5, 1, 5);

        // First row: Description Label
        String payloadDescription = "Enter your payload to use :";
        JLabel payloadDescriptionLabel = new JLabel(payloadDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 3; // Span across 3 columns
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        this.add(payloadDescriptionLabel, gbc);

        // First row: Label, TextField, Button
        JLabel label1 = new JLabel("Payload : ");
        JTextField payloadInput = new JTextField(persistence.getString(YOUR_PAYLOAD), 30); // Smaller width
        JButton saveBtn1 = new JButton("Save");

        // Add ActionListener to the first button
        saveBtn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String payload = payloadInput.getText();
                if (payload.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Payload cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setString(YOUR_PAYLOAD, payload);
                    JOptionPane.showMessageDialog(Settings.this, payload + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add components of the first row
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label1, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        this.add(payloadInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 1; // Row 1
        this.add(saveBtn1, gbc);

        // Second row: Description Label
        String matchDescription = "Enter your match :";
        JLabel timeoutDescriptionLabel = new JLabel(matchDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 2; // Row 2
        gbc.gridwidth = 3; // Span across 3 columns
        this.add(timeoutDescriptionLabel, gbc);

        // Second row: Label, TextField, Button
        JLabel label2 = new JLabel("Match : ");
        JTextField matchInput = new JTextField(persistence.getString(YOUR_MATCH), 30); // Smaller width
        JButton saveBtn2 = new JButton("Save");

        // Add ActionListener to the second button
        saveBtn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String match = matchInput.getText();
                if (match.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Match cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setString(YOUR_MATCH, match);
                    JOptionPane.showMessageDialog(Settings.this, match + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add components of the second row
        gbc.gridx = 0; // Column 0
        gbc.gridy = 3; // Row 3
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label2, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 3; // Row 3
        this.add(matchInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 3; // Row 3
        this.add(saveBtn2, gbc);

        // Third row: Description Label
        String thredsDescription = "Set number of threads :";
        JLabel thredsDescriptionLabel = new JLabel(thredsDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 4; // Row 4
        gbc.gridwidth = 3; // Span across 3 columns
        this.add(thredsDescriptionLabel, gbc);

        // Third row: Label, TextField, Button
        JLabel label3 = new JLabel("Threads : ");
        JTextField thredsInput = new JTextField(Integer.toString(persistence.getInteger(YOUR_THREAD)), 30); // Smaller width
        JButton saveBtn3 = new JButton("Save");

        // Add ActionListener to the third button
        saveBtn3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String threads = thredsInput.getText();
                if (threads.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Threads cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setInteger(YOUR_THREAD, Integer.parseInt(threads));
                    JOptionPane.showMessageDialog(Settings.this, threads + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add components of the third row
        gbc.gridx = 0; // Column 0
        gbc.gridy = 5; // Row 5
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label3, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 5; // Row 5
        this.add(thredsInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 5; // Row 5
        this.add(saveBtn3, gbc);

        // Fourth row: Description Label
        String headersDescription = "Set number of headers per request :";
        JLabel headersDescriptionLabel = new JLabel(headersDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 6; // Row 6
        gbc.gridwidth = 3; // Span across 3 columns
        this.add(headersDescriptionLabel, gbc);

        // Fourth row: Label, TextField, Button
        JLabel label4 = new JLabel("Headers : ");
        JTextField headersInput = new JTextField(Integer.toString(persistence.getInteger(YOUR_HEADERS_PER_REQUEST)), 30); // Smaller width
        JButton saveBtn4 = new JButton("Save");

        // Add ActionListener to the fourth button
        saveBtn4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String header = headersInput.getText();

                if (header.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Number of headers cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setInteger(YOUR_HEADERS_PER_REQUEST, Integer.parseInt(header));
                    JOptionPane.showMessageDialog(Settings.this, header + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add components of the fourth row
        gbc.gridx = 0; // Column 0
        gbc.gridy = 7; // Row 7
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label4, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 7; // Row 7
        this.add(headersInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 7; // Row 7
        this.add(saveBtn4, gbc);

        // Fifth row: Description Label
        String queryDescription = "Set number of query parameters per request :";
        JLabel queryDescriptionLabel = new JLabel(queryDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 8; // Row 8
        gbc.gridwidth = 3; // Span across 3 columns
        this.add(queryDescriptionLabel, gbc);

        // Fifth row: Label, TextField, Button
        JLabel label5 = new JLabel("Query : ");
        JTextField queryInput = new JTextField(Integer.toString(persistence.getInteger(YOUR_PARAMETERS_QUERY_PER_REQUEST)), 30); // Wider width for User-Agent
        JButton saveBtn5 = new JButton("Save");

        // Add ActionListener to the fifth button
        saveBtn5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryInput.getText();

                if (query.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Number of query parameters cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setInteger(YOUR_PARAMETERS_QUERY_PER_REQUEST, Integer.parseInt(query));
                    JOptionPane.showMessageDialog(Settings.this, query + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add components of the fifth row
        gbc.gridx = 0; // Column 0
        gbc.gridy = 9; // Row 9
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label5, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 9; // Row 9
        this.add(queryInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 9; // Row 9
        this.add(saveBtn5, gbc);

        // Sixth row: Description Label
        String cookieDescription = "Set number of cookie parameters per request :";
        JLabel cookieDescriptionLabel = new JLabel(cookieDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 10; // Row 10
        gbc.gridwidth = 3; // Span across 3 columns
        this.add(cookieDescriptionLabel, gbc);

        // Sixth row: Label, TextField, Button
        JLabel label6 = new JLabel("Cookie : ");
        JTextField cookieInput = new JTextField(Integer.toString(persistence.getInteger(YOUR_PARAMETERS_COOKIE_PER_REQUEST)), 30); // Wider width for proxy address
        JButton saveBtn6 = new JButton("Save");

        // Add ActionListener to the sixth button
        saveBtn6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cookie = cookieInput.getText();
                if (cookie.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Number of cookie parameters cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setInteger(YOUR_PARAMETERS_COOKIE_PER_REQUEST, Integer.parseInt(cookie));
                    JOptionPane.showMessageDialog(Settings.this, cookie + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add components of the sixth row
        gbc.gridx = 0; // Column 0
        gbc.gridy = 11; // Row 11
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label6, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 11; // Row 11
        this.add(cookieInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 11; // Row 11
        this.add(saveBtn6, gbc);

        // Seventh row: Description Label
        String bodyDescription = "Set number of body parameters per request :";
        JLabel bodyDescriptionLabel = new JLabel(bodyDescription);
        gbc.gridx = 0; // Column 0
        gbc.gridy = 12; // Row 12
        gbc.gridwidth = 3; // Span across 3 columns
        this.add(bodyDescriptionLabel, gbc);

        // Seventh row: Label, TextField, Button
        JLabel label7 = new JLabel("Body : ");
        JTextField bodyInput = new JTextField(Integer.toString(persistence.getInteger(YOUR_PARAMETERS_BODY_PER_REQUEST)), 30); // Wider width for cookie
        JButton saveBtn7 = new JButton("Save");

        // Add ActionListener to the seventh button
        saveBtn7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String body = bodyInput.getText();
                if (body.isEmpty()) {
                    JOptionPane.showMessageDialog(Settings.this, "Number of body parameters cannot be empty", "Unkey Input", JOptionPane.ERROR_MESSAGE);
                } else {
                    persistence.setInteger(YOUR_PARAMETERS_BODY_PER_REQUEST, Integer.parseInt(body));
                    JOptionPane.showMessageDialog(Settings.this, body + " saved", "Unkey Input", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        gbc.gridx = 0; // Column 0
        gbc.gridy = 13; // Row 13
        gbc.gridwidth = 1; // Reset gridwidth
        this.add(label7, gbc);

        gbc.gridx = 1; // Column 1
        gbc.gridy = 13; // Row 13
        this.add(bodyInput, gbc);

        gbc.gridx = 2; // Column 2
        gbc.gridy = 13; // Row 13
        this.add(saveBtn7, gbc);
    }
}