package view.profileUI;

import view.common.ImageResources;
import javax.swing.*;
import java.awt.*;

/**
 * Overlay dialog used to create a new user profile.
 *
 * The overlay is shown on top of the parent ProfilesPanel using the
 * pane glass layer. It collects a nickname and an optional avatar
 * and delegates creation to the configured {@link ProfilesAdapter}.
 */
class ProfileCreateOverlay {
    private final ProfilesPanel parentPanel;
    private final ProfilesAdapter port;
    private final Runnable onCreatedRefresh;
    private String selectedAvatarPath;

    /**
     * Create a new overlay instance.
     *
     * @param parentPanel the profiles panel that will host the overlay
     * @param port adapter used to perform create, delete and other operations
     * @param onCreatedRefresh optional callback executed after a creation to refresh the UI
     */
    
    public ProfileCreateOverlay(ProfilesPanel parentPanel, ProfilesAdapter port, Runnable onCreatedRefresh) {
        this.parentPanel = parentPanel;
        this.port = port;
        this.onCreatedRefresh = onCreatedRefresh;
    }

    /**
     * Display the overlay on the parent panel's glass pane.
     * If the parent's root pane cannot be found the method returns silently.
     */
    public void show() {
        JRootPane rootPane = SwingUtilities.getRootPane(parentPanel);
        if (rootPane == null) {
            return;
        }

        JComponent glassPane = prepareGlassPane(rootPane);
        JPanel darkOverlayPanel = createDarkOverlay();

        JTextField nicknameField = new JTextField(18);
        selectedAvatarPath = null;

        JPanel woodPanel = createWoodBackgroundPanel();
        woodPanel.add(buildCreateHeader(), BorderLayout.NORTH);
        woodPanel.add(buildCenterContent(nicknameField), BorderLayout.CENTER);
        woodPanel.add(buildActionButtons(rootPane, nicknameField), BorderLayout.SOUTH);
        addEscapeToClose(woodPanel, rootPane);

        darkOverlayPanel.add(woodPanel, new GridBagConstraints());
        glassPane.removeAll();
        glassPane.add(darkOverlayPanel, BorderLayout.CENTER);
        glassPane.setVisible(true);
        glassPane.revalidate();
        glassPane.repaint();

        SwingUtilities.invokeLater(() -> nicknameField.requestFocusInWindow());
    }

    private JComponent prepareGlassPane(JRootPane rootPane) {
        JComponent glassPane = (JComponent) rootPane.getGlassPane();
        if (!(glassPane.getLayout() instanceof BorderLayout)) {
            glassPane.setLayout(new BorderLayout());
        }
        return glassPane;
    }

    private JPanel createDarkOverlay() {
        JPanel overlayPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g;
                graphics.setColor(new Color(0, 0, 0, 140));
                graphics.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        overlayPanel.setOpaque(false);
        return overlayPanel;
    }

    private JPanel createWoodBackgroundPanel() {
        JPanel panel = new JPanel() {
            private final Image woodImage = ImageResources.load(ImageResources.WOOD);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D graphics = (Graphics2D) g;
                if (woodImage != null) {
                    graphics.drawImage(woodImage, 0, 0, getWidth(), getHeight(), this);
                }
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(640, 420));
        return panel;
    }

    private JPanel buildCreateHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Tre Sette");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 34));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.BLACK);

        JLabel subtitleLabel = new JLabel("Nuovo Profilo");
        subtitleLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.BLACK);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        return headerPanel;
    }

    private JPanel buildCenterContent(JTextField nicknameField) {
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel nicknameRowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        nicknameRowPanel.setOpaque(false);
        nicknameField.setFont(new Font("Serif", Font.PLAIN, 16));
        nicknameRowPanel.add(new JLabel("Nickname:"));
        nicknameRowPanel.add(nicknameField);
        contentPanel.add(nicknameRowPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel avatarLabel = new JLabel("Seleziona Avatar:");
        avatarLabel.setFont(new Font("Serif", Font.PLAIN, 16));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarLabel.setForeground(Color.BLACK);
        contentPanel.add(avatarLabel);
        contentPanel.add(Box.createVerticalStrut(6));

        JPanel avatarGridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        avatarGridPanel.setOpaque(false);
        populateAvatarGrid(avatarGridPanel);
        contentPanel.add(avatarGridPanel);
        return contentPanel;
    }

    private void populateAvatarGrid(JPanel avatarGridPanel) {
        for (String path : AvatarChooser.list()) {
            Image avatarBaseImage = ImageResources.load(path);
            if (avatarBaseImage == null) {
                continue;
            }

            Image avatarScaledImage = avatarBaseImage.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            JButton avatarButton = new JButton(new ImageIcon(avatarScaledImage));
            avatarButton.setPreferredSize(new Dimension(78, 78));
            avatarButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));

            avatarButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectedAvatarPath = path;

                    for (Component component : avatarGridPanel.getComponents()) {
                        if (component instanceof JButton buttonInGrid) {
                            buttonInGrid.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
                        }
                    }

                    avatarButton.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 3, true));
                }
            });

            avatarGridPanel.add(avatarButton);
        }
    }

    private JPanel buildActionButtons(JRootPane rootPane, JTextField nicknameField) {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        buttonsPanel.setOpaque(false);

        JButton createButton = new JButton("Crea");
        styleActionButton(createButton, new Color(34, 139, 34));
        createButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                handleCreateProfile(rootPane, nicknameField);
            }
        });

        JButton cancelButton = new JButton("Annulla");
        styleActionButton(cancelButton, new Color(139, 34, 34));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hideOverlay(rootPane);
            }
        });

        buttonsPanel.add(createButton);
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
    }

    private void styleActionButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        button.setFont(new Font("Serif", Font.BOLD, 16));
    }

    private void handleCreateProfile(JRootPane rootPane, JTextField nicknameField) {
        String nickname = nicknameField.getText().trim();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(parentPanel, "Nickname vuoto");
            return;
        }

        boolean created = port.createProfile(nickname, selectedAvatarPath);
        if (created) {
            hideOverlay(rootPane);
            if (onCreatedRefresh != null) {
                onCreatedRefresh.run();
            }
        } else {
            JOptionPane.showMessageDialog(parentPanel, "Creazione fallita");
        }
    }

    private void addEscapeToClose(JComponent component, JRootPane rootPane) {
        String actionKey = "escCloseOverlay";
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), actionKey);
        component.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hideOverlay(rootPane);
            }
        });
    }

    private void hideOverlay(JRootPane rootPane) {
        if (rootPane == null) {
            return;
        }
        JComponent glassPane = (JComponent) rootPane.getGlassPane();
        glassPane.setVisible(false);
        glassPane.removeAll();
    }
}
