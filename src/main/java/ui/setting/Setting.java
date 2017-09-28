package ui.setting;

import ui.camera.CameraPanel;
import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Setting extends JPanel {
    private static Setting setting;
    public JButton saveButton;
    private JTextField timeTextField;
    private JCheckBox checkBox;

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

    private void buildSetting() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel checkBoxPane = new JPanel(new FlowLayout());
        JCheckBox testModeCheckBox = new JCheckBox("Тестовий режим ");
        testModeCheckBox.setSelected(false);

        checkBox = new JCheckBox();
        checkBox.setSelected(MainFrame.isProgramLightCatchWork());


        JLabel checkBoxLabel = new JLabel("Фіксувати програмні спрацювання");
        checkBoxPane.add(checkBox);
        checkBoxPane.add(checkBoxLabel);

        JLabel changeWhiteLabel = new JLabel("Різниця кількості білого на кадрі - " + MainFrame.getPercentDiffWhite() + " %");

        JSlider sliderChangeWhite = new JSlider();
        sliderChangeWhite.setPreferredSize(new Dimension(200, 25));
        sliderChangeWhite.setMinorTickSpacing(1);
        sliderChangeWhite.setPaintTicks(true);
        sliderChangeWhite.setMinimum(5);
        sliderChangeWhite.setMaximum(20);
        sliderChangeWhite.setValue(MainFrame.getPercentDiffWhite());
        sliderChangeWhite.addChangeListener(e -> {
            changeWhiteLabel.setText("Різниця кількості білого на кадрі - " + sliderChangeWhite.getValue() + " %");
        });

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            stringBuilder.append(String.valueOf((char) 8623));
        }

        JPanel whitePanel = new JPanel();
        whitePanel.setBackground(Color.darkGray);
        JLabel whiteColorLabel = new JLabel(stringBuilder.toString());
        whiteColorLabel.setFont(new Font(null, Font.ITALIC, 25));
        whiteColorLabel.setForeground(new Color(MainFrame.getColorLightNumber(), MainFrame.getColorLightNumber(), MainFrame.getColorLightNumber()));
        whitePanel.add(whiteColorLabel);
        JLabel lightSensitivityLabel = new JLabel("Світлочутливість камери " + MainFrame.getColorLightNumber());
        JSlider lightSensitivitySlider = new JSlider();
        lightSensitivitySlider.setPreferredSize(new Dimension(200, 25));
        lightSensitivitySlider.setMinorTickSpacing(1);
        lightSensitivitySlider.setPaintTicks(true);
        lightSensitivitySlider.setMinimum(140);
        lightSensitivitySlider.setMaximum(245);
        lightSensitivitySlider.setValue(MainFrame.getColorLightNumber());
        lightSensitivitySlider.addChangeListener(e -> {
            int value = lightSensitivitySlider.getValue();
            whiteColorLabel.setForeground(new Color(value, value, value));
            lightSensitivityLabel.setText("Світлочутливість камери " + value);
        });

        JPanel timePane = new JPanel(new FlowLayout());
        timeTextField = new JTextField();
        timeTextField.setText(String.valueOf(MainFrame.getTimeToSave()));
        timeTextField.setPreferredSize(new Dimension(40, 25));
        JLabel textLabel = new JLabel("Час відео до та після в секундах");

        timePane.add(timeTextField);
        timePane.add(textLabel);

        JLabel opacityLabel = new JLabel("Прозорість зображення що накладаемо відео - " + MainFrame.getOpacitySetting() + " відсотків");

        JSlider slider = new JSlider();
        slider.setPreferredSize(new Dimension(200, 25));
        slider.setMinorTickSpacing(2);
        slider.setPaintTicks(true);
        slider.setValue(MainFrame.getOpacitySetting());
        slider.addChangeListener(e -> {
            int value = slider.getValue();
            opacityLabel.setText("Прозорість зображення що накладаемо відео - " + value + " відсотків");
        });

        int v = MainFrame.getDoNotShowImages();
        int s2 = 100;
        if (v != 0) {
            s2 = 100 / v;
        }

        JLabel framesLabel = new JLabel("Транслюемо відео - " + s2 + "%");
        JSlider sliderFrames = new JSlider();
        sliderFrames.setPreferredSize(new Dimension(200, 25));
        sliderFrames.setMinorTickSpacing(1);
        sliderFrames.setPaintTicks(true);
        sliderFrames.setMinimum(0);
        sliderFrames.setMaximum(20);
        sliderFrames.setValue(MainFrame.getDoNotShowImages());
        sliderFrames.addChangeListener(e -> {
            int value = sliderFrames.getValue();
            int s = 100;
            if (value != 0) {
                s = 100 / value;
            }
            framesLabel.setText("Транслюемо відео - " + s + "%");
        });

        saveButton = new JButton("Зберегти");
        saveButton.addActionListener((e) -> {
            MainFrame.setTestMode(testModeCheckBox.isSelected());

            MainFrame.setProgramLightCatchWork(checkBox.isSelected());
            int changeWhitePercent = sliderChangeWhite.getValue();
            MainFrame.setPercentDiffWhite(changeWhitePercent);

            int lightSensitivity = lightSensitivitySlider.getValue();
            MainFrame.setColorLightNumber(lightSensitivity);

            String text = timeTextField.getText();
            int i = Integer.parseInt(text);
            MainFrame.getMainFrame().setCountSaveVideo(i);

            int opacity = slider.getValue();
            MainFrame.setOpacitySetting(opacity);

            int doNotShowFrames = sliderFrames.getValue();
            MainFrame.setQualityVideoLabel(doNotShowFrames);

            MainFrame.addressSaver.saveSetting(i, checkBox.isSelected(), changeWhitePercent, lightSensitivity, opacity, doNotShowFrames);
            saveButton.setText("Збережено");
            saveButton.setForeground(new Color(46, 139, 87));
        });

        mainPanel.add(Box.createRigidArea(new Dimension(10, 30)));
        mainPanel.add(testModeCheckBox);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 30)));
        mainPanel.add(checkBoxPane);
        mainPanel.add(changeWhiteLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(sliderChangeWhite);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(lightSensitivityLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(whitePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(lightSensitivitySlider);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(timePane);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 30)));
        mainPanel.add(opacityLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(slider);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 30)));
        mainPanel.add(framesLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        mainPanel.add(sliderFrames);
        mainPanel.add(Box.createRigidArea(new Dimension(10, 20)));
        mainPanel.add(saveButton);
        this.add(mainPanel);
    }
}
