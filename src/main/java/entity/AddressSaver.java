package entity;

import ui.main.MainFrame;
import ui.setting.CameraAddressSetting;
import ui.setting.Setting;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "address")
public class AddressSaver {

    String [] arr = new String[24];
    int timeToSave = 0;
    boolean programWork = false;

    public void savePasswords(int numberOfCamera, String ipAddress,String username, String password){
        int ipAddressInt = numberOfCamera - 1;
        int userNameInt = numberOfCamera+7;
        int passwordInt = numberOfCamera+15;

        arr[ipAddressInt] = ipAddress;
        arr[userNameInt] = username;
        arr[passwordInt] = password;
        savePasswordSaverToFile();
    }

    public void saveSetting(int i,boolean programWork){
        timeToSave = i;
        this.programWork = programWork;
        savePasswordSaverToFile();
    }

    public void cleanSaver(){
        for(int i = 0;i<arr.length;i++){
            arr[i] = "";
        }
    }

    private void savePasswordSaverToFile() {
        String pathFile = "C:\\ipCamera\\bytes\\address.txt";
        File file = new File(pathFile);
        try {
            boolean ok = file.exists();
            if (!ok) {
                ok = file.createNewFile();
            }

            if (ok) {
                JAXBContext context = JAXBContext.newInstance(AddressSaver.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(this, file);
            }
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }

    public static AddressSaver restorePasswords() {
        String pathFile = "C:\\ipCamera\\bytes\\address.txt";
        File file = new File(pathFile);

        Object passwordsSaverObject = null;
        if (file.canRead()) {
            try {
                JAXBContext context = JAXBContext.newInstance(AddressSaver.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                passwordsSaverObject = unmarshaller.unmarshal(file);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        AddressSaver passwordSaver = null;
        if (passwordsSaverObject != null) {
            try {
                passwordSaver = (AddressSaver) passwordsSaverObject;
                System.out.println("Пароли восстановлены");
            } catch (ClassCastException e) {
                e.printStackTrace();
                System.out.println("Проблема во время восстановления цепочек");
            }
        }

        for(int i=1;i<5;i++){
            File imageFile = new File("C:\\ipCamera\\bytes\\"+i+".jpg");
            if(imageFile.exists()){
                BufferedImage bufferedImage=null;
                try {
                    bufferedImage = ImageIO.read(imageFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if(bufferedImage!=null){
                    MainFrame.addImage(bufferedImage,i);
                }
            }
        }

        if(passwordSaver!= null){
            return passwordSaver;
        }else {
            passwordSaver = new AddressSaver();
            return passwordSaver;
        }
    }

    public void setPasswordsToFields() {
        Map<Integer, JTextField> textFieldsIpAddressMap = CameraAddressSetting.getCameraAddressSetting().getTextFieldsIpAddressMap();
        for(Integer integer:textFieldsIpAddressMap.keySet()){
            textFieldsIpAddressMap.get(integer).setText(arr[integer-1]);
        }

        Map<Integer, JTextField> textFieldsUsernameMap = CameraAddressSetting.getCameraAddressSetting().getTextFieldsUsernameMap();
        for(Integer integer:textFieldsUsernameMap.keySet()){
            textFieldsUsernameMap.get(integer).setText(arr[integer+7]);
        }

        Map<Integer, JTextField> textFieldsPasswordMap = CameraAddressSetting.getCameraAddressSetting().getTextFieldsPasswordMap();
        for(Integer integer:textFieldsPasswordMap.keySet()){
            textFieldsPasswordMap.get(integer).setText(arr[integer+15]);
        }
    }

    public void setSetting(){
        MainFrame.timeToSave = timeToSave;
        MainFrame.programWork = programWork;
        Setting.checkBox.setSelected(programWork);
        Setting.timeTextField.setText(String.valueOf(timeToSave));
    }
}
