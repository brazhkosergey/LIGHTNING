package ui.setting;

import entity.AddressSaver;
import ui.main.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraAddressSetting extends JPanel {


    private static CameraAddressSetting cameraAddressSetting;
    private JPanel mainCameraSettingPanel;
    private Map<Integer,JTextField> textFieldsIpAddressMap;
    private Map<Integer,JTextField> textFieldsUsernameMap;
    private Map<Integer,JTextField> textFieldsPasswordMap;

    private CameraAddressSetting() {
        this.setPreferredSize(new Dimension(1100, 600));
        textFieldsIpAddressMap = new HashMap<>();
        textFieldsUsernameMap = new HashMap<>();
        textFieldsPasswordMap = new HashMap<>();
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

            JLabel firstCameraUserNameLabel = new JLabel("Username");
            JTextField firstCameraUserNameTextField = new JTextField();
            firstCameraUserNameTextField.setPreferredSize(new Dimension(100,25));
            JLabel firstCameraPasswordLabel = new JLabel("Пароль");
            JTextField firstCameraPasswordTextField = new JTextField();
            firstCameraPasswordTextField.setPreferredSize(new Dimension(100,25));

            textFieldsIpAddressMap.put(i * 2 - 1,firstCameraTextField);
            textFieldsUsernameMap.put(i * 2 - 1,firstCameraUserNameTextField);
            textFieldsPasswordMap.put(i * 2 - 1,firstCameraPasswordTextField);

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

            JLabel secondCameraUserNameLabel = new JLabel("Username");
            JTextField secondCameraUserNameTextField = new JTextField();
            secondCameraUserNameTextField.setPreferredSize(new Dimension(100,25));

            JLabel secondCameraPasswordLabel = new JLabel("Пароль");
            JTextField secondCameraPasswordTextField = new JTextField();
            secondCameraPasswordTextField.setPreferredSize(new Dimension(100,25));

            textFieldsIpAddressMap.put(i*2,secondCameraTextField);
            textFieldsUsernameMap.put(i*2,secondCameraUserNameTextField);
            textFieldsPasswordMap.put(i*2,secondCameraPasswordTextField);

            cameraTwoSetting.add(secondCameraLabel);
            cameraTwoSetting.add(secondCameraTextField);
            cameraTwoSetting.add(secondCameraUserNameLabel);
            cameraTwoSetting.add(secondCameraUserNameTextField);
            cameraTwoSetting.add(secondCameraPasswordLabel);
            cameraTwoSetting.add(secondCameraPasswordTextField);

            cameraBlock.add(cameraOneSetting);
            cameraBlock.add(cameraTwoSetting);

            JLabel addImageLabel = new JLabel("Додати фонове зображення");
            addImageLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            JButton addImageButton = new JButton("Вибрати файл");
            addImageButton.addActionListener((e)->{
                JFileChooser fileChooser = new JFileChooser("Вибрати файл");
                fileChooser.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
                fileChooser.setApproveButtonText("Вибрати");
                int ret = fileChooser.showDialog(null, "Вибрати файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
//                    File file = fileChooser.getSelectedFile();
                    System.out.println("Была нажата кнопка открыть");
                }
            });
            inputPanel.add(cameraBlock);
            inputPanel.add(Box.createRigidArea(new Dimension(30,20)));
            inputPanel.add(addImageLabel);
            inputPanel.add(Box.createRigidArea(new Dimension(30,20)));
            inputPanel.add(addImageButton);

            TitledBorder titleMainSetting = BorderFactory.createTitledBorder("Блок - " + i);
            titleMainSetting.setTitleJustification(TitledBorder.CENTER);
            titleMainSetting.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 14)));
            titleMainSetting.setTitleColor(new Color(46, 139, 87));
            titleMainSetting.setBorder(new LineBorder(new Color(46, 139, 87), 2, true));
            blockPanel.setBorder(titleMainSetting);

            blockPanel.add(inputPanel);
            blockPanel.setMaximumSize(new Dimension(1100,130));
//            blockPanel.add(fileChooser);
//            fileChooser.setVisible(false);
            mainCameraSettingPanel.add(blockPanel);
        }

        JButton saveButton = new JButton("Зберегти");
        saveButton.setFont(new Font("Comic Sans MS", Font.BOLD, 17));
        saveButton.addActionListener((e)->{
            MainFrame.addressSaver.cleanSaver();
            saveAddressToMap();
            MainFrame.getMainFrame().showAllCameras();
        });

        mainCameraSettingPanel.add(Box.createRigidArea(new Dimension(900,20)));
        mainCameraSettingPanel.add(saveButton);
    }

    public void saveAddressToMap(){
        for(Integer textFieldNumber: textFieldsIpAddressMap.keySet()){
            String ipAddress = textFieldsIpAddressMap.get(textFieldNumber).getText();
            if(ipAddress.length()>3){
                String userName = textFieldsUsernameMap.get(textFieldNumber).getText();
                String password = textFieldsPasswordMap.get(textFieldNumber).getText();
                List<String> list = new ArrayList<>();
                list.add(ipAddress);
                list.add(userName);
                list.add(password);
                MainFrame.addressSaver.savePasswords(textFieldNumber,ipAddress,userName,password);
                MainFrame.camerasAddress.put(textFieldNumber,list);
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

    public void restoreAddresses(){
        MainFrame.addressSaver.setPasswordsToFields();
    }
}
