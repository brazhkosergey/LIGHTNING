package ui.setting;

import ui.camera.CameraPanel;
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
        this.setPreferredSize(new Dimension(1100, 550));
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

        JLabel opacityLabel = new JLabel("Прозорість зображення що накладаемо відео - "+30+" відсотків");

        JSlider slider = new JSlider();
        slider.setPreferredSize(new Dimension(200, 25));
        slider.setMinorTickSpacing(1);
        slider.setValue(30);
        slider.addChangeListener(e ->{
            System.out.println("Значение на слайдере изменилось на: " + slider.getValue());
            int value = slider.getValue();
            Float f =(float) value/100;
            CameraPanel.setOpacity(f);
            opacityLabel.setText("Прозорість зображення що накладаемо відео - "+value+" відсотків");
        });

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
        mainPanel.add(Box.createRigidArea(new Dimension(10,30)));
        mainPanel.add(opacityLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(10,10)));
        mainPanel.add(slider);
        mainPanel.add(Box.createRigidArea(new Dimension(10,300)));
        mainPanel.add(saveButton);
        this.add(mainPanel);
    }

    public void setSetting(){
        MainFrame.addressSaver.setSetting();
    }
}
