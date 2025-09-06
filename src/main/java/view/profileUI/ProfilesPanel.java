package view.profileUI;

import profile.UserProfile;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import utils.AudioManager;

/**
 * ProfilesPanel is a Swing component that displays existing user profiles
 * and provides controls to create, select or delete a profile.
 * The panel delegates the profile operations to a {@link ProfilesAdapter} instance,
 * in order to keep the UI decoupled from the ProfileService
 * and ProfileRepository
 */
public class ProfilesPanel extends JPanel {
    private final ProfilesAdapter port;
    private final Runnable onBack;
    private final Consumer<UserProfile> onProfileSelected;
    private static void noopProfile(UserProfile ignored) {}
    private JPanel listContainer;
    private JScrollPane scroll;

    /**
     * Create a ProfilesPanel bound to the provided port.
     *
    * @param port adapter used to interact with the profile service
     * @param onBack runnable called when the user requests to go back 
     * @param onProfileSelected callback executed after a profile is selected
     */
    public ProfilesPanel(ProfilesAdapter port, Runnable onBack, Consumer<UserProfile> onProfileSelected) {
        this.port = port;
        this.onBack = onBack;
        if (onProfileSelected == null) {
            this.onProfileSelected = ProfilesPanel::noopProfile;
        } else {
            this.onProfileSelected = onProfileSelected;
        }
        setOpaque(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(900,600));
        setMinimumSize(new Dimension(900,600));
        buildUI();
        refresh();
    }
    /**
     * Refresh the list of displayed profiles.
     * This operation rebuilds the cards shown in the panel.
     */
    public final void refresh() {
        List<UserProfile> profiles = port.listProfiles();
        listContainer.removeAll();
        if (profiles.isEmpty()) {
            JLabel empty = ProfileUIStyle.createLabel("Nessun profilo. Creane uno nuovo.", 
                                                    new Font("Serif", Font.ITALIC,18), Color.BLACK);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listContainer.add(empty);
        } else {
            for (UserProfile user : profiles) {
                listContainer.add(new ProfileCard(user, new ProfileCard.Listener() {
                    @Override 
                    public void onSelect(UserProfile profile) {
                        port.selectProfile(profile);
                        onProfileSelected.accept(profile);
                        refresh();
                    }
                    @Override 
                    public void onDelete(UserProfile profile) {
                        String message = "Eliminare '" + profile.getNickname() + "'?";
                        if (JOptionPane.showConfirmDialog(ProfilesPanel.this, message, "Conferma",
                                                 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            if (port.deleteProfile(profile)) refresh();
                        }
                    }
                }));
                listContainer.add(Box.createVerticalStrut(8));
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);
        listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBorder(BorderFactory.createEmptyBorder(10,40,10,8));
        scroll = new JScrollPane(listContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel title = ProfileUIStyle.createLabel("Tre Sette", ProfileUIStyle.FONT_TITLE, Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = ProfileUIStyle.createLabel("Gestione Profili", ProfileUIStyle.FONT_SUBTITLE, Color.BLACK);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(15));
        p.add(title);
        p.add(Box.createVerticalStrut(6));
        p.add(sub);
        p.add(Box.createVerticalStrut(10));
        return p;
    }

    private JComponent buildBottom() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        JButton create = ProfileUIStyle.createButton("Nuovo Profilo", new Color(34, 139, 34), 
                        new Dimension(140, 42), ProfileUIStyle.FONT_BTN_LARGE, Color.WHITE);
        JButton back = ProfileUIStyle.createButton("Indietro", new Color(139, 69, 19),
                        new Dimension(140, 42), ProfileUIStyle.FONT_BTN_LARGE, Color.WHITE);

        create.addActionListener(new java.awt.event.ActionListener(){
            @Override public void actionPerformed(java.awt.event.ActionEvent event){
                AudioManager.playClick();
                showCreateDialog();
            }
        });
        back.addActionListener(new java.awt.event.ActionListener(){
            @Override public void actionPerformed(java.awt.event.ActionEvent event){
                AudioManager.playClick();
                if (onBack != null) onBack.run();
            }
        });

        panel.add(create);
        panel.add(back);
        return panel;
    }


    // ---------- profile creation dialog trigger ----------
    private void showCreateDialog() { new ProfileCreateOverlay(this, port, this::refresh).show(); }
}
