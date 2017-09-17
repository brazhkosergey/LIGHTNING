package ui.setting;

import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Setting extends JPanel {
    private static Setting setting;
    public JButton saveButton;
    public static JTextField timeTextField;
    public static JCheckBox checkBox;


    private Setting() {
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        this.setPreferredSize(new Dimension(1100, 580));
//        this.setMaximumSize(new Dimension(1100, 600));
        buildSetting();
    }

    public static Setting getSetting() {
        if (setting != null) {
            return setting;
        } else {
            setting = new Setting();
            return setting;
        }
    }

    private void buildSetting(){

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

        JPanel checkBoxPane = new JPanel(new FlowLayout());
        checkBox = new JCheckBox();
        JLabel checkBoxLabel = new JLabel("Фіксувати програмні спрацювання");
        checkBoxPane.add(checkBox);
        checkBoxPane.add(checkBoxLabel);

        JPanel timePane=new JPanel(new FlowLayout());
        timeTextField = new JTextField("30");
        timeTextField.setPreferredSize(new Dimension(40,25));
        JLabel textLabel = new JLabel("Час відео до та після в секундах");

        timePane.add(timeTextField);
        timePane.add(textLabel);

        saveButton = new JButton("Зберегти");
        saveButton.addActionListener((e)->{
            String text = timeTextField.getText();
            int i = Integer.parseInt(text);
            MainFrame.programWork = checkBox.isSelected();
            MainFrame.timeToSave = i;
            MainFrame.addressSaver.saveSetting(i,checkBox.isSelected());
            saveButton.setText("Збережено");
            saveButton.setForeground(new Color(46, 139, 87));
        });

        mainPanel.add(Box.createRigidArea(new Dimension(10,30)));
        mainPanel.add(checkBoxPane);
        mainPanel.add(Box.createRigidArea(new Dimension(10,10)));
        mainPanel.add(timePane);
        mainPanel.add(Box.createRigidArea(new Dimension(10,400)));
        mainPanel.add(saveButton);
        this.add(mainPanel);
    }

    public void setSetting(){
        MainFrame.addressSaver.setSetting();
    }
}
