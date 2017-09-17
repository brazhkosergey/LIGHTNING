package entity;

public class MainPlayerClass {

    VideoPlayer videoPlayer;
    private boolean PLAY = false;


    public MainPlayerClass(VideoPlayer videoPlayer){
        this.videoPlayer = videoPlayer;
//        videoPlayer.setMainPlayerClass(this);
    }

    public boolean isPLAY() {
        return PLAY;
    }

    public void setPLAY(boolean PLAY) {
        this.PLAY = PLAY;
    }
}
