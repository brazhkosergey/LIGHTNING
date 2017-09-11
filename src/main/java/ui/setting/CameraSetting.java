package ui.setting;

import ui.main.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CameraSetting extends JPanel {
    private static CameraSetting cameraSetting;
    private JPanel mainCameraSettingPanel;
    private Map<Integer,JTextField> textFieldMapAddressCamera;
    private CameraSetting() {
        this.setPreferredSize(new Dimension(1100, 600));
        textFieldMapAddressCamera = new HashMap<>();
        buildCameraSetting();
        this.setLayout(new BorderLayout());
        this.add(mainCameraSettingPanel, BorderLayout.CENTER);
    }

    public static CameraSetting getCameraSetting() {
        if (cameraSetting != null) {
            return cameraSetting;
        } else {
            cameraSetting = new CameraSetting();
            return cameraSetting;
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
            firstCameraTextField.setPreferredSize(new Dimension(300, 30));
            textFieldMapAddressCamera.put(i * 2 - 1,firstCameraTextField);
            cameraOneSetting.add(firstCameraLabel);
            cameraOneSetting.add(firstCameraTextField);

            JPanel cameraTwoSetting = new JPanel(new FlowLayout());
            JLabel secondCameraLabel = new JLabel("Камера " + (i * 2));
            secondCameraLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
            JTextField secondCameraTextField = new JTextField();
            secondCameraTextField.setPreferredSize(new Dimension(300, 30));
            textFieldMapAddressCamera.put(i*2,secondCameraTextField);
            cameraTwoSetting.add(secondCameraLabel);
            cameraTwoSetting.add(secondCameraTextField);

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
            inputPanel.add(Box.createRigidArea(new Dimension(150,20)));
            inputPanel.add(addImageLabel);
            inputPanel.add(Box.createRigidArea(new Dimension(50,20)));
            inputPanel.add(addImageButton);

            TitledBorder titleMainSetting = BorderFactory.createTitledBorder("Блок - " + i);
            titleMainSetting.setTitleJustification(TitledBorder.CENTER);
            titleMainSetting.setTitleFont((new Font("Comic Sans MS", Font.BOLD, 14)));
            titleMainSetting.setTitleColor(new Color(46, 139, 87));
            titleMainSetting.setBorder(new LineBorder(new Color(46, 139, 87), 2, true));
            blockPanel.setBorder(titleMainSetting);

            blockPanel.add(inputPanel);
//            blockPanel.add(fileChooser);
//            fileChooser.setVisible(false);
            mainCameraSettingPanel.add(blockPanel);
        }

        JButton saveButton = new JButton("Зберегти");
        saveButton.setFont(new Font("Comic Sans MS", Font.BOLD, 17));
        saveButton.addActionListener((e)->{
            for(Integer textFieldNumber:textFieldMapAddressCamera.keySet()){
                String text = textFieldMapAddressCamera.get(textFieldNumber).getText();
                if(text.length()>1){
                    MainFrame.camerasAddress.put(textFieldNumber,text);
                }
            }
            MainFrame.getMainFrame().showAllCameras();
        });

        mainCameraSettingPanel.add(Box.createRigidArea(new Dimension(1000,30)));
        mainCameraSettingPanel.add(saveButton);
    }

}
