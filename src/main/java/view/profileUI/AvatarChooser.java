package view.profileUI;

import javax.swing.*;
import java.awt.*;
import view.common.ImageResources;
import view.LayoutConstant;

/**
 * Avatar chooser dialog. It shows the different avatars in the UI.
 */
public final class AvatarChooser {
    private AvatarChooser() {}

    /**
     * Return a copy of the available avatar resource paths.
     * The returned array is a defensive copy and can be safely modified.
     * @return array of avatar resource paths
     */
    public static String[] list() {
        return LayoutConstant.AVATARS.clone();
    }

    /**
     * Show a modal dialog that allows the user to choose an avatar.
     *
     * The dialog is blocked until the user selects an avatar or cancels. 
     * Returns the selected resource path
     * or {@code null} if the user cancelled.
     *
     * @param parent component used to parent the dialog
     * @return selected avatar resource path or {@code null}
     */
    public static String choose(Component parent) {
        final String[] selected = { null };
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Seleziona Avatar", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        for (String path : LayoutConstant.AVATARS) {
            Image img = ImageResources.load(path);
            if (img == null) continue;
            img = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JButton b = new JButton(new ImageIcon(img));
            b.setPreferredSize(new Dimension(88, 88));
            b.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    selected[0] = path;
                    dialog.dispose();
                }
            });
            row.add(b);
        }

        JButton annulla = new JButton("Annulla");
        annulla.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dialog.dispose();
            }
        });

        JPanel south = new JPanel(new FlowLayout());
        south.add(annulla);
        dialog.add(new JLabel("Scegli il tuo avatar:", SwingConstants.CENTER), BorderLayout.NORTH);
        dialog.add(row, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return selected[0];
    }
}
