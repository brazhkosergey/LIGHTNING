package ui.setting;

import org.apache.log4j.Logger;
import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Setting extends JPanel {
    private static Logger log = Logger.getLogger(Setting.class);

    private static Setting setting;
    private JLabel portLabel;
    private JTextField defaultPort;
    private JTextField defaultFolder;
    private JLabel currentFolder;

    public JButton saveButton;
    private JTextField timeTextField;
    private JCheckBox checkBox;

    private JPanel allSettingPane;
    private JPanel passwordPane;
    private JLabel wrongPasswordLabel;
    private JTextField passwordTextField;

    private Setting() {
        this.setPreferredSize(new Dimension(1120, 540));
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
        JPanel mainSettingPanel = new JPanel(new FlowLayout());
        mainSettingPanel.setBorder(BorderFactory.createEtchedBorder());
        mainSettingPanel.setPreferredSize(new Dimension(700, 530));

        JCheckBox testModeCheckBox = new JCheckBox(MainFrame.getBundle().getString("testmode"));
        testModeCheckBox.setPreferredSize(new Dimension(370, 30));
        testModeCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        testModeCheckBox.setSelected(false);

        JPanel lightWorkPane = new JPanel(new GridLayout(2, 1));
        lightWorkPane.setBorder(BorderFactory.createEtchedBorder());
        lightWorkPane.setPreferredSize(new Dimension(690, 70));

        JPanel checkBoxPane = new JPanel(new FlowLayout());
        checkBox = new JCheckBox();
        checkBox.setSelected(MainFrame.isProgramLightCatchWork());
        JLabel checkBoxLabel = new JLabel(MainFrame.getBundle().getString("programcatchcheckboxlabel"));
        checkBoxPane.add(checkBox);
        checkBoxPane.add(checkBoxLabel);

        JPanel timePane = new JPanel(new FlowLayout());
        timeTextField = new JTextField();
        timeTextField.setText(String.valueOf(MainFrame.getTimeToSave()));
        timeTextField.setPreferredSize(new Dimension(40, 25));
        JLabel textLabel = new JLabel(MainFrame.getBundle().getString("timetosavevideolabel"));
        timePane.add(timeTextField);
        timePane.add(textLabel);
        lightWorkPane.add(checkBoxPane);
        lightWorkPane.add(timePane);

        JPanel programLightCatchSettingPanel = new JPanel(new FlowLayout());
        programLightCatchSettingPanel.setBorder(BorderFactory.createEtchedBorder());
        programLightCatchSettingPanel.setPreferredSize(new Dimension(690, 230));

        JLabel headLabel = new JLabel(MainFrame.getBundle().getString("programcatchsettinglabel"));
        headLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headLabel.setFont(new Font(null, Font.BOLD, 17));
        headLabel.setPreferredSize(new Dimension(680, 30));

        JLabel lightSensitivityLabel = new JLabel(MainFrame.getBundle().getString("photosensitivitysettinglabel") + MainFrame.getColorLightNumber());
        lightSensitivityLabel.setPreferredSize(new Dimension(680, 30));

        JPanel whitePanel = new JPanel(new FlowLayout());
        whitePanel.setPreferredSize(new Dimension(680, 40));
        whitePanel.setBackground(Color.darkGray);
        whitePanel.setBorder(BorderFactory.createEtchedBorder());
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 37; i++) {
            stringBuilder.append(String.valueOf((char) 8623));
        }

        JLabel whiteColorLabel = new JLabel(stringBuilder.toString());
        whiteColorLabel.setPreferredSize(new Dimension(680, 30));
        whiteColorLabel.setFont(new Font(null, Font.ITALIC, 25));
        whiteColorLabel.setForeground(new Color(MainFrame.getColorLightNumber(), MainFrame.getColorLightNumber(), MainFrame.getColorLightNumber()));
        whitePanel.add(whiteColorLabel);

        JSlider lightSensitivitySlider = new JSlider();
        lightSensitivitySlider.setPreferredSize(new Dimension(680, 30));
        lightSensitivitySlider.setMinorTickSpacing(1);
        lightSensitivitySlider.setPaintTicks(true);
        lightSensitivitySlider.setMinimum(140);
        lightSensitivitySlider.setMaximum(230);
        lightSensitivitySlider.setValue(MainFrame.getColorLightNumber());
        lightSensitivitySlider.addChangeListener(e -> {
            int value = lightSensitivitySlider.getValue();
            whiteColorLabel.setForeground(new Color(value, value, value));
            lightSensitivityLabel.setText(MainFrame.getBundle().getString("photosensitivitysettinglabel") + value);
        });

        JLabel changeWhiteLabel = new JLabel(MainFrame.getBundle().getString("lightening") + MainFrame.getPercentDiffWhite() + " %");
        changeWhiteLabel.setPreferredSize(new Dimension(680, 30));

        JSlider sliderChangeWhite = new JSlider();
        sliderChangeWhite.setPreferredSize(new Dimension(680, 30));
        sliderChangeWhite.setMinorTickSpacing(1);
        sliderChangeWhite.setPaintTicks(true);
        sliderChangeWhite.setMinimum(2);
        sliderChangeWhite.setMaximum(9);
        sliderChangeWhite.setValue(MainFrame.getPercentDiffWhite());
        sliderChangeWhite.addChangeListener(e -> {
            changeWhiteLabel.setText(MainFrame.getBundle().getString("lightening") + sliderChangeWhite.getValue() + " %");
        });

        programLightCatchSettingPanel.add(headLabel);
        programLightCatchSettingPanel.add(lightSensitivityLabel);
        programLightCatchSettingPanel.add(whitePanel);
        programLightCatchSettingPanel.add(lightSensitivitySlider);
        programLightCatchSettingPanel.add(changeWhiteLabel);
        programLightCatchSettingPanel.add(sliderChangeWhite);

        JPanel otherSetting = new JPanel(new FlowLayout());
        otherSetting.setPreferredSize(new Dimension(690, 70));
        otherSetting.setBorder(BorderFactory.createEtchedBorder());

        JLabel opacityLabel = new JLabel(MainFrame.getBundle().getString("backimageopacitylabel") + MainFrame.getOpacitySetting() + " %");
        opacityLabel.setPreferredSize(new Dimension(680, 25));
        JSlider slider = new JSlider();
        slider.setPreferredSize(new Dimension(680, 30));
        slider.setMinorTickSpacing(2);
        slider.setPaintTicks(true);
        slider.setValue(MainFrame.getOpacitySetting());
        slider.addChangeListener(e -> {
            int value = slider.getValue();
            opacityLabel.setText(MainFrame.getBundle().getString("backimageopacitylabel") + value + " %");
        });

        otherSetting.add(opacityLabel);
        otherSetting.add(slider);

        JPanel countImageToShowPanel = new JPanel(new FlowLayout());
        countImageToShowPanel.setPreferredSize(new Dimension(690, 70));
        countImageToShowPanel.setBorder(BorderFactory.createEtchedBorder());
        JLabel countShowLabel = new JLabel(MainFrame.getBundle().getString("showframescountlabel") + MainFrame.getShowFramesPercent());
        countShowLabel.setPreferredSize(new Dimension(680, 25));
        JSlider countShowSlider = new JSlider();
        countShowSlider.setPreferredSize(new Dimension(680, 30));
        countShowSlider.setMinimum(1);
        countShowSlider.setMaximum(60);
        countShowSlider.setMinorTickSpacing(2);
        countShowSlider.setPaintTicks(true);
        countShowSlider.setValue(MainFrame.getShowFramesPercent());
        countShowSlider.addChangeListener(e -> {
            int value = countShowSlider.getValue();
            countShowLabel.setText(MainFrame.getBundle().getString("showframescountlabel") + value);
        });

        countImageToShowPanel.add(countShowLabel);
        countImageToShowPanel.add(countShowSlider);


        saveButton = new JButton();
        saveButton.setPreferredSize(new Dimension(150, 50));
        saveButton.setFont(new Font(null, Font.BOLD, 20));
        saveButton.addActionListener((e) -> {

            try{
                MainFrame.setTestMode(testModeCheckBox.isSelected());

                int value = countShowSlider.getValue();
                MainFrame.setShowFramesPercent(value);

                MainFrame.setProgramLightCatchWork(checkBox.isSelected());
                int changeWhitePercent = sliderChangeWhite.getValue();
                MainFrame.setPercentDiffWhite(changeWhitePercent);

                int lightSensitivity = lightSensitivitySlider.getValue();
                MainFrame.getMainFrame().setColorLightNumber(lightSensitivity);

                String text = timeTextField.getText();
                int countSecondsToSaveVideo = Integer.parseInt(text);

                if(countSecondsToSaveVideo<2){
                    countSecondsToSaveVideo = 2;
                    timeTextField.setText(String.valueOf(countSecondsToSaveVideo));
                }

                MainFrame.setCountSecondsToSaveVideo(countSecondsToSaveVideo);

                int opacity = slider.getValue();
                MainFrame.setOpacitySetting(opacity);

                int port = 9999;
                try {
                    port = Integer.valueOf(defaultPort.getText());
                } catch (Exception ignored) {
                }

                MainFrame.setPort(port);
                portLabel.setText("port сервера - " + port);

                String path = defaultFolder.getText();
                boolean mkdirs = false;
                if (path != null && path.length() > 2) {
                    File file = new File(path + "\\bytes\\");
                    try {
                        mkdirs = file.mkdirs();
                    } catch (Exception ignored) {
                    }
                }

                if (mkdirs) {
                    currentFolder.setText(path);
                    MainFrame.setPath(path);
                }

                MainFrame.addressSaver.saveSetting(countSecondsToSaveVideo, checkBox.isSelected(), changeWhitePercent, lightSensitivity, opacity, port, path);
                log.info("Настройки изменены. Время сохранения: " + countSecondsToSaveVideo +
                        ", Фиксируем програмные сработки: " + checkBox.isSelected() +
                        ", процент вспышки на изображении: " + changeWhitePercent +
                        ", чуствительность камеры: " + lightSensitivity +
                        ", прозрачность фона: " + opacity +
                        ", порт для ожидания сиграла аппаратной сработки: " + port +
                        ", путь к папке для сохранения данных: " + path +
                        ", тестовый режим: " + testModeCheckBox.isSelected() + ".");

                saveButton.setText(MainFrame.getBundle().getString("savedbutton"));
                MainFrame.showInformMassage(MainFrame.getBundle().getString("savedbutton"),new Color(46, 139, 87));
                saveButton.setForeground(new Color(46, 139, 87));
            } catch (Exception exc){
                log.error(exc.getMessage());
                MainFrame.showInformMassage("ERROR",Color.red);
            }
        });

        mainSettingPanel.add(lightWorkPane);
        mainSettingPanel.add(programLightCatchSettingPanel);
        mainSettingPanel.add(otherSetting);
        mainSettingPanel.add(countImageToShowPanel);
        mainSettingPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        mainSettingPanel.add(saveButton);

        JPanel folderSettingPanel = new JPanel(new FlowLayout());
        folderSettingPanel.setBorder(BorderFactory.createEtchedBorder());
        folderSettingPanel.setPreferredSize(new Dimension(380, 530));


        portLabel = new JLabel("port - " + MainFrame.getPort());
        portLabel.setPreferredSize(new Dimension(200, 25));
        defaultPort = new JTextField();
        defaultPort.setText(String.valueOf(MainFrame.getPort()));
        defaultPort.setPreferredSize(new Dimension(100, 25));

        JPanel pathPanel = new JPanel(new FlowLayout());
        pathPanel.setPreferredSize(new Dimension(375, 110));
        pathPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel addressSaverLabel = new JLabel(MainFrame.getBundle().getString("foldertosavevideo"));
        addressSaverLabel.setFont(new Font(null, Font.BOLD, 15));
        addressSaverLabel.setPreferredSize(new Dimension(370, 25));
        addressSaverLabel.setHorizontalAlignment(SwingConstants.CENTER);

        currentFolder = new JLabel(MainFrame.getPath());
        currentFolder.setHorizontalAlignment(SwingConstants.CENTER);
        currentFolder.setPreferredSize(new Dimension(370, 25));
        defaultFolder = new JTextField(MainFrame.getPath());//"C:\\ipCamera\\"
        defaultFolder.setPreferredSize(new Dimension(370, 25));

        pathPanel.add(addressSaverLabel);
        pathPanel.add(currentFolder);
        pathPanel.add(defaultFolder);

        folderSettingPanel.add(testModeCheckBox);
        folderSettingPanel.add(portLabel);
        folderSettingPanel.add(defaultPort);
        folderSettingPanel.add(pathPanel);

        allSettingPane = new JPanel(new FlowLayout());
        allSettingPane.add(mainSettingPanel);
        allSettingPane.add(folderSettingPanel);

        passwordPane = new JPanel(new FlowLayout());
        passwordPane.setPreferredSize(new Dimension(1000, 500));
        passwordPane.setBackground(Color.LIGHT_GRAY);
        JLabel passwordLabel = new JLabel(MainFrame.getBundle().getString("editpasswordlabel"));

        wrongPasswordLabel = new JLabel(MainFrame.getBundle().getString("wrongpasswordlabel"));
        wrongPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrongPasswordLabel.setPreferredSize(new Dimension(900, 30));
        wrongPasswordLabel.setVisible(false);

        passwordTextField = new JTextField();
        passwordTextField.setPreferredSize(new Dimension(150, 30));
        JButton passwordButton = new JButton(MainFrame.getBundle().getString("editpasswordbutton"));
        passwordButton.setPreferredSize(new Dimension(150, 30));
        passwordButton.addActionListener((e) -> {
            String passwordString = passwordTextField.getText();
            if (passwordString.length() > 1 && passwordString.compareTo(MainFrame.getPassword()) == 0) {
                passwordPane.setVisible(false);
                allSettingPane.setVisible(true);
            } else {
                wrongPasswordLabel.setVisible(true);
            }
        });

        passwordPane.add(passwordLabel);
        passwordPane.add(Box.createRigidArea(new Dimension(10, 30)));
        passwordPane.add(passwordTextField);
        passwordPane.add(passwordButton);
        passwordPane.add(wrongPasswordLabel);
        allSettingPane.setVisible(false);

        this.setBackground(Color.LIGHT_GRAY);
        this.add(passwordPane);
        this.add(allSettingPane);
    }

    public void reSetPassword() {
        passwordTextField.setText("");
        allSettingPane.setVisible(false);
        passwordPane.setVisible(true);
        wrongPasswordLabel.setVisible(false);
    }
}
