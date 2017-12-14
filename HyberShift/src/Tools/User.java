package Tools;

public class User {
	
    public String getIpSender() {
		return IpSender;
	}

	public void setIpSender(String ipSender) {
		IpSender = ipSender;
	}

	public int getVoicePort() {
		return voicePort;
	}

	public void setVoicePort(int voicePort) {
		this.voicePort = voicePort;
	}

	public int getVoiceFormat() {
		return voiceFormat;
	}

	public void setVoiceFormat(int voiceFormat) {
		this.voiceFormat = voiceFormat;
	}

	public int getVideoPort() {
		return videoPort;
	}

	public void setVideoPort() {
		this.videoPort = videoPortDefault +  10;
		videoPortDefault+=10;
	}

	public int getVideoFormat() {
		return videoFormat;
	}

	public void setVideoFormat() {
		this.voicePort = voicePortDefault +  10;
		voicePortDefault+=10;
	}
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private static int videoPortDefault = 1000;
	private static int voicePortDefault = 1000;
	
	private String name;
	private String IpSender;
    
	private int voicePort;
    private int voiceFormat;

    private int videoPort;
    private int videoFormat;
    
    User()
    {
    	voicePort = 0;
    	voiceFormat = 0;
    	videoPort = 0;
    	videoFormat = 0;
    }

}
