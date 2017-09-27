package ui.setting;

import ui.main.MainFrame;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraAddressSetting extends JPanel {
    private static CameraAddressSetting cameraAddressSetting;
    private JPanel mainCameraSettingPanel;
    private Map<Integer, JTextField> textFieldsIpAddressMap;
    private Map<Integer, JTextField> textFieldsUsernameMap;
    private Map<Integer, JTextField> textFieldsPasswordMap;
    private Map<Integer, JLabel> labelMap;

    private CameraAddressSetting() {
        this.setPreferredSize(new Dimension(1100, 600));
        textFieldsIpAddressMap = new HashMap<>();
        textFieldsUsernameMap = new HashMap<>();
        textFieldsPasswordMap = new HashMap<>();
        labelMap = new HashMap<>();
        buildCameraSetting();
        this.setLayout(new BorderLayout());
        this.add(mainCameraSettingPanel, BorderLayout.CENTER);
    }

    public static CameraAddressSetting getCameraAddressSetting() {
        if (cameraAddressSetting != null) {
            return cameraAddressSetting;
        } else {
            cameraAddressSetting = new CameraAddressSetting();
            return cameraAddressSetting;
        }
    }

    private void buildCameraSetting() {
        mainCameraSettingPanel = new JPanel();
        mainCameraSettingPanel.setLayout(new BoxLayout(mainCameraSettingPanel, BoxLayout.Y_AXIS));
        for (int i = 1; i < 5; i++) {
            JPanel blockPanel = new JPanel(new FlowLayout());
            JPanel inputPanel = new JPanel(new FlowLayout());
            JPanel cameraBlock = new JPanel();
            cameraBlock.setLayout(new BoxLayout(cameraBlock, BoxLayout.Y_AXIS));

            JPanel cameraOneSetting = new JPanel(new FlowLayout());
            JLabel firstCameraLabel = new JLabel("Камера " + (i * 2 - 1));
            firstCameraLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            JTextField firstCameraTextField = new JTextField();
//            JTextField firstCameraTextField = new JTextField("http://195.235.198.107:3346/axis-cgi/mjpg/video.cgi?size=640x480");
            firstCameraTextField.setPreferredSize(new Dimension(250, 25));

            JLabel firstCameraUserNameLabel = new JLabel("Ім'я");
            JTextField firstCameraUserNameTextField = new JTextField();
            firstCameraUserNameTextField.setPreferredSize(new Dimension(100, 25));
            JLabel firstCameraPasswordLabel = new JLabel("Пароль");
            JTextField firstCameraPasswordTextField = new JTextField();
            firstCameraPasswordTextField.setPreferredSize(new Dimension(100, 25));

            textFieldsIpAddressMap.put(i * 2 - 1, firstCameraTextField);
            textFieldsUsernameMap.put(i * 2 - 1, firstCameraUserNameTextField);
            textFieldsPasswordMap.put(i * 2 - 1, firstCameraPasswordTextField);

            cameraOneSetting.add(firstCameraLabel);
            cameraOneSetting.add(firstCameraTextField);
            cameraOneSetting.add(firstCameraUserNameLabel);
            cameraOneSetting.add(firstCameraUserNameTextField);
            cameraOneSetting.add(firstCameraPasswordLabel);
            cameraOneSetting.add(firstCameraPasswordTextField);

            JPanel cameraTwoSetting = new JPanel(new FlowLayout());
            JLabel secondCameraLabel = new JLabel("Камера " + (i * 2));
            secondCameraLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            JTextField secondCameraTextField = new JTextField();
            secondCameraTextField.setPreferredSize(new Dimension(250, 25));

            JLabel secondCameraUserNameLabel = new JLabel("Ім'я");
            JTextField secondCameraUserNameTextField = new JTextField();
            secondCameraUserNameTextField.setPreferredSize(new Dimension(100, 25));

            JLabel secondCameraPasswordLabel = new JLabel("Пароль");
            JTextField secondCameraPasswordTextField = new JTextField();
            secondCameraPasswordTextField.setPreferredSize(new Dimension(100, 25));

            textFieldsIpAddressMap.put(i * 2, secondCameraTextField);
            textFieldsUsernameMap.put(i * 2, secondCameraUserNameTextField);
            textFieldsPasswordMap.put(i * 2, secondCameraPasswordTextField);

            cameraTwoSetting.add(secondCameraLabel);
            cameraTwoSetting.add(secondCameraTextField);
            cameraTwoSetting.add(secondCameraUserNameLabel);
            cameraTwoSetting.add(secondCameraUserNameTextField);
            cameraTwoSetting.add(secondCameraPasswordLabel);
            cameraTwoSetting.add(secondCameraPasswordTextField);

            cameraBlock.add(cameraOneSetting);
            cameraBlock.add(cameraTwoSetting);

            JLabel addImageLabel = new JLabel("Додати фонове зображення");
            labelMap.put(i, addImageLabel);
            addImageLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            addImageLabel.setPreferredSize(new Dimension(230, 30));

            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));

            JButton addImageButton = new JButton("Вибрати файл   ");
            int number = i;
            addImageButton.addActionListener((e) -> {
                JFileChooser fileChooser = new JFileChooser("Вибрати файл");
                fileChooser.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
                fileChooser.setApproveButtonText("Вибрати");
                int ret = fileChooser.showDialog(null, "Вибрати файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    BufferedImage bufferedImage = null;
                    try {
                        bufferedImage = ImageIO.read(file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    File imageFile = new File("C:\\ipCamera\\bytes\\" + number + ".jpg");
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }

                    InputStream is;
                    OutputStream os;
                    try {
                        imageFile.createNewFile();
                        is = new FileInputStream(file);
                        os = new FileOutputStream(imageFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                        is.close();
                        os.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    if (bufferedImage != null) {
                        MainFrame.addImage(bufferedImage, number);
                        MainFrame.creatorMap.get(number).setBufferedImageBack(bufferedImage);
                    }

                    String text = addImageLabel.getText();
                    if (text.compareTo("Зображення додане") == 0) {
                        addImageLabel.setText("Зображення оновлено");
                    } else {
                        addImageLabel.setText("Зображення додане");
                        addImageLabel.setForeground(new Color(46, 139, 87));
                    }
                }
            });
            JButton removeButton = new JButton("Видалити файл");
            removeButton.addActionListener((e) -> {
                File imageFile = new File("C:\\ipCamera\\bytes\\" + number + ".jpg");
                if (imageFile.exists()) {
                    imageFile.delete();
                }

                MainFrame.removeImageForBlock(number);

                addImageLabel.setText("Вибрати файл");
                addImageLabel.setForeground(Color.BLACK);

            });


            buttonPane.add(addImageButton);
            buttonPane.add(Box.createRigidArea(new Dimension(20, 10)));
            buttonPane.add(removeButton);

            inputPanel.add(cameraBlock);
            inputPanel.add(Box.createRigidArea(new Dimension(10, 20)));
            inputPanel.add(addImageLabel);
            inputPanel.add(Box.createRigidArea(new Dimension(10, 20)));
            inputPanel.add(buttonPane);

            TitledBorder titleMainSetting = BorderFactory.createTitledBorder("Блок - " + i);
            titleMainSetting.setTitleJustification(TitledBorder.CENTER);
            titleMainSetting.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 14)));
            titleMainSetting.setTitleColor(new Color(46, 139, 87));
            titleMainSetting.setBorder(new LineBorder(new Color(46, 139, 87), 2, true));
            blockPanel.setBorder(titleMainSetting);

            blockPanel.add(inputPanel);
            blockPanel.setMaximumSize(new Dimension(1100, 120));
            mainCameraSettingPanel.add(blockPanel);
        }

        JButton saveButton = new JButton("Зберегти");
        saveButton.setFont(new Font("Comic Sans MS", Font.BOLD, 17));
        saveButton.addActionListener((e) -> {
            MainFrame.addressSaver.cleanSaver();
            saveAddressToMap();
            MainFrame.getMainFrame().showAllCameras();
        });

        mainCameraSettingPanel.add(Box.createRigidArea(new Dimension(900, 20)));
        mainCameraSettingPanel.add(saveButton);

    }

    public void setHaveImage(int integer){
        if (labelMap.containsKey(integer)) {
            labelMap.get(integer).setText("Зображення додане");
            labelMap.get(integer).setForeground(new Color(46, 139, 87));
        }
    }

    public void saveAddressToMap() {
        for (Integer textFieldNumber : textFieldsIpAddressMap.keySet()) {
            String ipAddress = textFieldsIpAddressMap.get(textFieldNumber).getText();

            if (ipAddress.length() > 3) {
                String userName = textFieldsUsernameMap.get(textFieldNumber).getText();
                String password = textFieldsPasswordMap.get(textFieldNumber).getText();
                List<String> list = new ArrayList<>();
                list.add(ipAddress);
                list.add(userName);
                list.add(password);
                MainFrame.addressSaver.savePasswords(textFieldNumber, ipAddress, userName, password);
                MainFrame.camerasAddress.put(textFieldNumber, list);
            } else {
                MainFrame.camerasAddress.put(textFieldNumber, null);
            }
        }
    }

    public Map<Integer, JTextField> getTextFieldsIpAddressMap() {
        return textFieldsIpAddressMap;
    }

    public Map<Integer, JTextField> getTextFieldsUsernameMap() {
        return textFieldsUsernameMap;
    }

    public Map<Integer, JTextField> getTextFieldsPasswordMap() {
        return textFieldsPasswordMap;
    }
}
