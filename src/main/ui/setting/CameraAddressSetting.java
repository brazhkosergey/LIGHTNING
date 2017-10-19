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
        this.setPreferredSize(new Dimension(1100, 540));
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
        JButton saveButton = new JButton(MainFrame.getBundle().getString("startcameras"));
        saveButton.setFont(new Font(null, Font.BOLD, 17));
        saveButton.addActionListener((e) -> {
            MainFrame.addressSaver.cleanSaver();
            saveAddressToMap();
            MainFrame.getMainFrame().showAllCameras();
        });
        mainCameraSettingPanel = new JPanel();
        mainCameraSettingPanel.setLayout(new BoxLayout(mainCameraSettingPanel, BoxLayout.Y_AXIS));
        for (int i = 1; i < 5; i++) {
            JPanel blockPanel = new JPanel(new FlowLayout());
            JPanel inputPanel = new JPanel(new FlowLayout());
            JPanel cameraBlock = new JPanel();
            cameraBlock.setLayout(new BoxLayout(cameraBlock, BoxLayout.Y_AXIS));

            JPanel cameraOneSetting = new JPanel(new FlowLayout());
            JLabel firstCameraLabel = new JLabel(MainFrame.getBundle().getString("cameraword") + (i * 2 - 1));
            firstCameraLabel.setFont(new Font(null, Font.BOLD, 15));
            JTextField firstCameraTextField = new JTextField();
            firstCameraTextField.setPreferredSize(new Dimension(320, 20));

            JLabel firstCameraUserNameLabel = new JLabel(MainFrame.getBundle().getString("username"));
            JTextField firstCameraUserNameTextField = new JTextField();
            firstCameraUserNameTextField.setPreferredSize(new Dimension(100, 20));
            JLabel firstCameraPasswordLabel = new JLabel(MainFrame.getBundle().getString("password"));
            JTextField firstCameraPasswordTextField = new JTextField();
            firstCameraPasswordTextField.setPreferredSize(new Dimension(100, 20));
            textFieldsIpAddressMap.put(i * 2 - 1, firstCameraTextField);
            textFieldsUsernameMap.put(i * 2 - 1, firstCameraUserNameTextField);
            textFieldsPasswordMap.put(i * 2 - 1, firstCameraPasswordTextField);
            cameraOneSetting.add(firstCameraLabel);
            cameraOneSetting.add(firstCameraTextField);
            cameraOneSetting.add(Box.createRigidArea(new Dimension(5, 20)));
            cameraOneSetting.add(firstCameraUserNameLabel);
            cameraOneSetting.add(firstCameraUserNameTextField);
            cameraOneSetting.add(firstCameraPasswordLabel);
            cameraOneSetting.add(firstCameraPasswordTextField);

            JPanel cameraTwoSetting = new JPanel(new FlowLayout());
            JLabel secondCameraLabel = new JLabel(MainFrame.getBundle().getString("cameraword") + (i * 2));
            secondCameraLabel.setFont(new Font(null, Font.BOLD, 15));
            JTextField secondCameraTextField = new JTextField();
            secondCameraTextField.setPreferredSize(new Dimension(320, 20));

            JLabel secondCameraUserNameLabel = new JLabel(MainFrame.getBundle().getString("username"));
            JTextField secondCameraUserNameTextField = new JTextField();
            secondCameraUserNameTextField.setPreferredSize(new Dimension(100, 20));

            JLabel secondCameraPasswordLabel = new JLabel(MainFrame.getBundle().getString("password"));
            JTextField secondCameraPasswordTextField = new JTextField();
            secondCameraPasswordTextField.setPreferredSize(new Dimension(100, 20));
            textFieldsIpAddressMap.put(i * 2, secondCameraTextField);
            textFieldsUsernameMap.put(i * 2, secondCameraUserNameTextField);
            textFieldsPasswordMap.put(i * 2, secondCameraPasswordTextField);

            cameraTwoSetting.add(secondCameraLabel);
            cameraTwoSetting.add(secondCameraTextField);
            cameraTwoSetting.add(Box.createRigidArea(new Dimension(5, 20)));
            cameraTwoSetting.add(secondCameraUserNameLabel);
            cameraTwoSetting.add(secondCameraUserNameTextField);
            cameraTwoSetting.add(secondCameraPasswordLabel);
            cameraTwoSetting.add(secondCameraPasswordTextField);

            cameraBlock.add(cameraOneSetting);
            cameraBlock.add(cameraTwoSetting);

            JLabel addImageLabel = new JLabel(MainFrame.getBundle().getString("selectimage"));
            labelMap.put(i, addImageLabel);
            addImageLabel.setFont(new Font(null, Font.BOLD, 15));
            addImageLabel.setPreferredSize(new Dimension(210, 20));

            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));

            JButton addImageButton = new JButton(MainFrame.getBundle().getString("selectimagefilebutton"));
            int number = i;
            addImageButton.addActionListener((e) -> {
                JFileChooser fileChooser = new JFileChooser(MainFrame.getBundle().getString("selectimagefilebutton"));
                fileChooser.setFont(new Font(null, Font.BOLD, 12));
                fileChooser.setApproveButtonText(MainFrame.getBundle().getString("selectimagefilebutton"));
                int ret = fileChooser.showDialog(null, MainFrame.getBundle().getString("selectimagefilebutton"));
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    BufferedImage bufferedImage = null;
                    try {
                        bufferedImage = ImageIO.read(file);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    File imageFile = new File(MainFrame.getDefaultPath() + "\\buff\\" + number + ".jpg");
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

                    addImageLabel.setText(MainFrame.getBundle().getString("imagehadadded"));
                    addImageLabel.setForeground(new Color(46, 139, 87));

                }
            });

            JButton removeButton = new JButton(MainFrame.getBundle().getString("deleteimagefilebutton"));//
            removeButton.addActionListener((e) -> {
                File imageFile = new File("C:\\LIGHTNING_STABLE\\buff\\" + number + ".jpg");
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                MainFrame.removeImageForBlock(number);
                addImageLabel.setText(MainFrame.getBundle().getString("selectimage"));
                addImageLabel.setForeground(Color.BLACK);
            });

            buttonPane.add(addImageButton);
            buttonPane.add(Box.createRigidArea(new Dimension(20, 5)));
            buttonPane.add(removeButton);

            inputPanel.add(cameraBlock);
            inputPanel.add(Box.createRigidArea(new Dimension(5, 5)));
            inputPanel.add(addImageLabel);
            inputPanel.add(Box.createRigidArea(new Dimension(5, 5)));
            inputPanel.add(buttonPane);

            TitledBorder titleMainSetting = BorderFactory.createTitledBorder(MainFrame.getBundle().getString("cameragroup") + i);
            titleMainSetting.setTitleJustification(TitledBorder.CENTER);
            titleMainSetting.setTitleFont((new Font(null, Font.BOLD, 14)));
            titleMainSetting.setTitleColor(new Color(46, 139, 87));
            titleMainSetting.setBorder(new LineBorder(new Color(46, 139, 87), 1, true));
            blockPanel.setBorder(titleMainSetting);

            blockPanel.add(inputPanel);
            blockPanel.setMaximumSize(new Dimension(1100, 80));
            mainCameraSettingPanel.add(blockPanel);

            if (i == 4) {
                JPanel audioPane = new JPanel(new FlowLayout());
                TitledBorder titleAudio = BorderFactory.createTitledBorder(MainFrame.getBundle().getString("soundtitle"));
                titleAudio.setTitleJustification(TitledBorder.CENTER);
                titleAudio.setTitleFont((new Font(null, Font.BOLD, 14)));
                titleAudio.setTitleColor(new Color(46, 139, 87));
                titleAudio.setBorder(new LineBorder(new Color(46, 139, 87), 1, true));
                audioPane.setBorder(titleAudio);
                JLabel addressAudioLabel = new JLabel(MainFrame.getBundle().getString("rtspaddress"));
                JTextField addressAudioTextField = new JTextField();
                textFieldsIpAddressMap.put(null, addressAudioTextField);
                addressAudioTextField.setPreferredSize(new Dimension(250, 25));
                audioPane.add(addressAudioLabel);
                audioPane.add(addressAudioTextField);
                audioPane.add(Box.createRigidArea(new Dimension(220, 10)));
                audioPane.add(saveButton);
                mainCameraSettingPanel.add(audioPane);
            }
        }
    }

    public void setHaveImage(int integer) {
        if (labelMap.containsKey(integer)) {
            labelMap.get(integer).setText(MainFrame.getBundle().getString("imagehadadded"));
            labelMap.get(integer).setForeground(new Color(46, 139, 87));
        }
    }

    public void saveAddressToMap() {
        for (Integer textFieldNumber : textFieldsIpAddressMap.keySet()) {
            if (textFieldNumber != null) {
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
            } else {
                String ipAddress = textFieldsIpAddressMap.get(textFieldNumber).getText();
                if (ipAddress.length() > 3) {
                    if (ipAddress.length() > 1) {
                        List<String> list = new ArrayList<>();
                        list.add(ipAddress);
                        MainFrame.camerasAddress.put(null, list);
                        MainFrame.addressSaver.savePasswords(0, ipAddress, null, null);
                    } else {
                        MainFrame.camerasAddress.put(null, new ArrayList<>());
                    }
                }
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
