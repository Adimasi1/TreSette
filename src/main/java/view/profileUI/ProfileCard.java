package view.profileUI;

import profile.UserProfile;
import profile.SelectedProfileHolder;
import view.common.ImageResources;
import utils.AudioManager;

import javax.swing.*;
import java.awt.*;

/**
 * A UI card that displays a single user profile.
 *
 * The card shows avatar, nickname and basic statistics, and exposes two
 * user actions via a {@link Listener} callback: select and delete.
 */
final class ProfileCard extends JPanel {
    interface Listener {
        void onSelect(UserProfile profile);
        void onDelete(UserProfile profile);
    }

    private final UserProfile profile;
    private final boolean selected;

    /**
     * Create a profile card for the given user
     *
     * @param profile the user profile to displa
     * @param listener call invoked when the user selects or deletes this profile
     */
    public ProfileCard(UserProfile profile, Listener listener){
        this.profile = profile;
        this.selected = (SelectedProfileHolder.get() != null && 
                         SelectedProfileHolder.get().getNickname().equals(profile.getNickname()));
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        setPreferredSize(new Dimension(600, 160));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        add(buildAvatarWrap(), BorderLayout.WEST);
        add(buildInfoBox(), BorderLayout.CENTER);
        add(buildActions(listener), BorderLayout.EAST);
    }

    private JComponent buildAvatarWrap(){
        JPanel avatarWrap = new JPanel(new BorderLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setBorder(BorderFactory.createEmptyBorder(0,0,0,16));
        avatarWrap.add(buildAvatar(profile.getAvatarPath()), BorderLayout.CENTER);
        return avatarWrap;
    }

    private JComponent buildInfoBox(){
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(0,4,0,12));
        JLabel name = ProfileUIStyle.createLabel(profile.getNickname(), ProfileUIStyle.FONT_NAME, Color.BLACK);
        int played = profile.getGamesPlayed();
        int won = profile.getGamesWon();
        int lost = profile.getGameLost();
        double rate = profile.getWinRate()*100.0;
        box.add(name);
        box.add(Box.createVerticalStrut(6));
        box.add(statLabel("Partite: "+ played));
        box.add(statLabel("Vittorie: "+ won));
        box.add(statLabel("Sconfitte: "+ lost));
        box.add(statLabel(String.format("Vittorie: %.1f%%", rate)));
        return box;
    }

    private JLabel statLabel(String t){
        return ProfileUIStyle.createLabel(t, ProfileUIStyle.FONT_STAT, Color.DARK_GRAY);
    }

    private JComponent buildActions(Listener listener){
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        JButton select = ProfileUIStyle.createButton("Seleziona", new Color(34,139,34), new Dimension(120,40), ProfileUIStyle.FONT_BTN_SMALL, Color.WHITE);
        JButton delete = ProfileUIStyle.createButton("Elimina", new Color(139,34,34), new Dimension(120,40), ProfileUIStyle.FONT_BTN_SMALL, Color.WHITE);
        select.addActionListener(new java.awt.event.ActionListener(){
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent e){
                AudioManager.playClick();
                if(listener!=null) listener.onSelect(profile);
            }
        });
        delete.addActionListener(new java.awt.event.ActionListener(){
            @Override 
            public void actionPerformed(java.awt.event.ActionEvent e){
                AudioManager.playClick();
                if(listener!=null) listener.onDelete(profile);
            }
        });
        col.add(select); col.add(Box.createVerticalStrut(6)); col.add(delete);
        return col;
    }

    private JComponent buildAvatar(String path){
        JLabel lbl;
        try {
            if (path != null) {
                var url = getClass().getResource(path.startsWith("/")?path:"/"+path);
                if (url != null) {
                    Image img = new ImageIcon(url).getImage().getScaledInstance(80,80,Image.SCALE_SMOOTH);
                    lbl = new JLabel(new ImageIcon(img));
                } else lbl = new JLabel("IMG");
            } else lbl = new JLabel("IMG");
        } catch(Exception ex) { lbl = new JLabel("IMG"); }
        lbl.setPreferredSize(new Dimension(80,80));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    @Override
    protected void paintComponent(Graphics g){
        Image wood = ImageResources.load(ImageResources.WOOD);
        if (wood != null) {
            g.drawImage(wood, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(133,94,66));
            g.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
        }
        if (selected) {
            g.setColor(new Color(255,255,180,130));
            g.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
        }
        super.paintComponent(g);
    }
}
