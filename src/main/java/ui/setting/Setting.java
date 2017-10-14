package ui.setting;

import ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Setting extends JPanel {
    private static Setting setting;
    private JLabel portLabel;
    private JTextField defaultPort;
    private JTextField defaultFolder;
    private JTextField defaultProfileName;
    private JLabel currentFolder;
    private JLabel currentProfileName;
    public JButton saveButton;
    private JTextField timeTextField;
    private JCheckBox checkBox;
    private JPanel allSettingPane;
    private JPanel passwordPane;

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

        JCheckBox testModeCheckBox = new JCheckBox("Тестовий режим ");
        testModeCheckBox.setPreferredSize(new Dimension(370, 30));
        testModeCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        testModeCheckBox.setSelected(false);

        JPanel lightWorkPane = new JPanel(new GridLayout(2, 1));
        lightWorkPane.setBorder(BorderFactory.createEtchedBorder());
        lightWorkPane.setPreferredSize(new Dimension(690, 70));

        JPanel checkBoxPane = new JPanel(new FlowLayout());
        checkBox = new JCheckBox();
        checkBox.setSelected(MainFrame.isProgramLightCatchWork());
        JLabel checkBoxLabel = new JLabel("Фіксувати програмні спрацювання");
        checkBoxPane.add(checkBox);
        checkBoxPane.add(checkBoxLabel);

        JPanel timePane = new JPanel(new FlowLayout());
        timeTextField = new JTextField();
        timeTextField.setText(String.valueOf(MainFrame.getTimeToSave()));
        timeTextField.setPreferredSize(new Dimension(40, 25));
        JLabel textLabel = new JLabel("Час відео ДО та ПІСЛЯ блискавки (в секундах)");
        timePane.add(timeTextField);
        timePane.add(textLabel);
        lightWorkPane.add(checkBoxPane);
        lightWorkPane.add(timePane);

        JPanel programLightCatchSettingPanel = new JPanel(new FlowLayout());
        programLightCatchSettingPanel.setBorder(BorderFactory.createEtchedBorder());
        programLightCatchSettingPanel.setPreferredSize(new Dimension(690, 230));

        JLabel headLabel = new JLabel("Налаштування програмного спрацьовування");
        headLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headLabel.setFont(new Font(null, Font.BOLD, 17));
        headLabel.setPreferredSize(new Dimension(680, 30));

        JLabel lightSensitivityLabel = new JLabel("Світлочутливість камери " + MainFrame.getColorLightNumber());
        lightSensitivityLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
        whiteColorLabel.setPreferredSize(new Dimension(670, 30));
        whiteColorLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
            lightSensitivityLabel.setText("Світлочутливість камери " + value);
        });


        JLabel changeWhiteLabel = new JLabel("Різниця кількості білого на кадрі - " + MainFrame.getPercentDiffWhite() + " %");
        changeWhiteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        changeWhiteLabel.setPreferredSize(new Dimension(680, 30));

        JSlider sliderChangeWhite = new JSlider();
        sliderChangeWhite.setPreferredSize(new Dimension(680, 30));
        sliderChangeWhite.setMinorTickSpacing(1);
        sliderChangeWhite.setPaintTicks(true);
        sliderChangeWhite.setMinimum(5);
        sliderChangeWhite.setMaximum(20);
        sliderChangeWhite.setValue(MainFrame.getPercentDiffWhite());
        sliderChangeWhite.addChangeListener(e -> {
            changeWhiteLabel.setText("Різниця кількості білого в кадрі - " + sliderChangeWhite.getValue() + " %");
        });

        programLightCatchSettingPanel.add(headLabel);
        programLightCatchSettingPanel.add(lightSensitivityLabel);
        programLightCatchSettingPanel.add(whitePanel);
        programLightCatchSettingPanel.add(lightSensitivitySlider);
        programLightCatchSettingPanel.add(changeWhiteLabel);
        programLightCatchSettingPanel.add(sliderChangeWhite);

        JPanel otherSetting = new JPanel(new FlowLayout());
        otherSetting.setPreferredSize(new Dimension(690, 100));
        otherSetting.setBorder(BorderFactory.createEtchedBorder());

        JLabel opacityLabel = new JLabel("Прозорість зображення що накладаемо на відео - " + MainFrame.getOpacitySetting() + " відсотків");
        opacityLabel.setPreferredSize(new Dimension(350, 30));
        opacityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JSlider slider = new JSlider();
        slider.setPreferredSize(new Dimension(680, 30));
        slider.setMinorTickSpacing(2);
        slider.setPaintTicks(true);
        slider.setValue(MainFrame.getOpacitySetting());
        slider.addChangeListener(e -> {
            int value = slider.getValue();
            opacityLabel.setText("Прозорість зображення що накладаемо відео - " + value + " відсотків");
        });

        otherSetting.add(opacityLabel);
        otherSetting.add(slider);

        JPanel countImageToSHowPanel = new JPanel(new FlowLayout());
        JLabel countImagesToShowLabel = new JLabel("Кількість кадрів що транслюемо ");


        JComboBox<Integer> comboBox = new JComboBox<>();
        comboBox.addItem(1);
        comboBox.addItem(5);
        comboBox.addItem(10);

        countImageToSHowPanel.add(countImagesToShowLabel);
        countImageToSHowPanel.add(comboBox);

        saveButton = new JButton("Зберегти");
        saveButton.setPreferredSize(new Dimension(150, 50));
        saveButton.setFont(new Font(null, Font.BOLD, 20));
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

            int port = 9999;
            try {
                port = Integer.valueOf(defaultPort.getText());
            } catch (Exception ignoge) {
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

            int selectedIndex = (int) comboBox.getSelectedItem();
            MainFrame.setShowImagePerSecond(selectedIndex);

            String profileName = defaultProfileName.getText();
            currentProfileName.setText(profileName);
            MainFrame.setProfileName(profileName);

            MainFrame.addressSaver.saveSetting(i, checkBox.isSelected(), changeWhitePercent, lightSensitivity, opacity, port, path, profileName);
            saveButton.setText("Збережено");
            saveButton.setForeground(new Color(46, 139, 87));
        });

        mainSettingPanel.add(lightWorkPane);
        mainSettingPanel.add(programLightCatchSettingPanel);
        mainSettingPanel.add(otherSetting);
        mainSettingPanel.add(Box.createRigidArea(new Dimension(690, 20)));
        mainSettingPanel.add(countImageToSHowPanel);
        mainSettingPanel.add(Box.createRigidArea(new Dimension(100, 20)));
        mainSettingPanel.add(saveButton);

        JPanel folderSettingPanel = new JPanel(new FlowLayout());
        folderSettingPanel.setBorder(BorderFactory.createEtchedBorder());
        folderSettingPanel.setPreferredSize(new Dimension(380, 530));


        portLabel = new JLabel("port сервера - " + MainFrame.getPort());
        portLabel.setPreferredSize(new Dimension(200, 25));
        defaultPort = new JTextField();//"9999"
        defaultPort.setText(String.valueOf(MainFrame.getPort()));
        defaultPort.setPreferredSize(new Dimension(100, 25));

        JPanel pathPanel = new JPanel(new FlowLayout());
        pathPanel.setPreferredSize(new Dimension(375, 110));
        pathPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel addressSaverLabel = new JLabel("Папка для файлів");
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

        JPanel profileNamePane = new JPanel(new FlowLayout());
        profileNamePane.setBorder(BorderFactory.createEtchedBorder());
        profileNamePane.setPreferredSize(new Dimension(375, 110));

        JLabel headProfileNameLabel = new JLabel("Ім'я профілю відео потоку");
        headProfileNameLabel.setFont(new Font(null, Font.BOLD, 15));
        headProfileNameLabel.setPreferredSize(new Dimension(370, 25));
        headProfileNameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        currentProfileName = new JLabel(MainFrame.getProfileName());
        currentProfileName.setHorizontalAlignment(SwingConstants.CENTER);
        currentProfileName.setPreferredSize(new Dimension(370, 25));

        defaultProfileName = new JTextField(MainFrame.getProfileName());
        defaultProfileName.setPreferredSize(new Dimension(370, 25));

        profileNamePane.add(headProfileNameLabel);
        profileNamePane.add(currentProfileName);
        profileNamePane.add(defaultProfileName);

        folderSettingPanel.add(testModeCheckBox);
        folderSettingPanel.add(portLabel);
        folderSettingPanel.add(defaultPort);
        folderSettingPanel.add(pathPanel);
        folderSettingPanel.add(profileNamePane);

        allSettingPane = new JPanel(new FlowLayout());
        allSettingPane.add(mainSettingPanel);
        allSettingPane.add(folderSettingPanel);

        passwordPane = new JPanel(new FlowLayout());
        passwordPane.setPreferredSize(new Dimension(1050,300));
        JLabel passwordLabel = new JLabel("Введіть пароль");
        JTextField passwordTextField = new JTextField();
        passwordTextField.setPreferredSize(new Dimension(150, 30));

        JLabel wrongPasswordLabel = new JLabel("Пароль не вірний. ВВЕДІТЬ 'PASS'");

        wrongPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wrongPasswordLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        wrongPasswordLabel.setPreferredSize(new Dimension(1000, 30));
        wrongPasswordLabel.setVisible(false);
        JButton passwordButton = new JButton("Вхід");
        passwordButton.setPreferredSize(new Dimension(150, 30));
        passwordButton.addActionListener((e) -> {

            String password = passwordTextField.getText();
            if (password.compareTo("PASS") == 0) {
                passwordPane.setVisible(false);
                allSettingPane.setVisible(true);
                wrongPasswordLabel.setVisible(false);
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

        this.add(passwordPane);
        this.add(allSettingPane);
    }

    public void setPassword() {
        passwordPane.setVisible(true);
        allSettingPane.setVisible(false);
    }
}
