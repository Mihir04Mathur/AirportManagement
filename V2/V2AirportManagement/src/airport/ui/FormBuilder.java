package airport.ui;

import javax.swing.*;
import java.awt.*;

/** Builds vertical forms: label above field. */
public class FormBuilder {
    private final JPanel panel = new JPanel(new GridBagLayout());
    private int row = 0;

    public FormBuilder() { panel.setOpaque(false); }

    public JPanel add(String label, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.add(Theme.label(label), BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return addGroup(group);
    }

    public JPanel addGroup(JPanel group) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row++; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);
        panel.add(group, c);
        return group;
    }

    public JPanel build() { return panel; }
}
