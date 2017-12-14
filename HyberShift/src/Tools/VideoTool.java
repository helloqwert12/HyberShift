package Tools;

import com.sun.media.rtp.RTPSessionMgr;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.InetAddress;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.FormatControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author KhangDang
 */
public class VideoTool implements ReceiveStreamListener {

    private Processor processor;
    private DataSource outDataSource;

    private RTPSessionMgr videoSession;
    private SendStream sendStream;

    private ReceiveStream receiveStream;
    private Player player;

    private User senderInfo;
    private User receiverInfo;

    public void senderInfo(User senderInfo) {
        this.senderInfo = senderInfo;
    }

    public void receiverInfo(User receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public void init() {
        try {
            videoSession = new RTPSessionMgr();
            videoSession.addReceiveStreamListener(this);

            SessionAddress localSessionAddress = new SessionAddress(
                    InetAddress.getByName(senderInfo.getIpSender()), senderInfo.getVideoPort());

            SessionAddress revSessionAddress = new SessionAddress(
                    InetAddress.getByName(receiverInfo.getIpSender()), receiverInfo.getVideoPort());

            videoSession.initSession(new SessionAddress(), null, 0.25, 0.5);
            videoSession.startSession(localSessionAddress, localSessionAddress, revSessionAddress, null);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void startMedia() {
        try {
            // bắt media stream từ soundcard
            MediaLocator locator = new MediaLocator("vfw://0");

            DataSource dataSource = Manager.createDataSource(locator);

            processor = Manager.createProcessor(dataSource);
            processor.configure();
            while (processor.getState() != Processor.Configured) {
                Thread.sleep(10);
            }

            processor.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW_RTP));
            TrackControl tracks[] = processor.getTrackControls();
            VideoFormat vf = new VideoFormat(VideoFormat.JPEG_RTP);

            int m = 0;
            Format f[] = tracks[0].getSupportedFormats();
            for (int j = 0; j < f.length; j++) {
                if (vf.matches(f[j])) {
                    m = 1;
                }
            }
            if (m == 0) {
                return;
            }
            ((FormatControl) tracks[0]).setFormat(vf);

            processor.realize();
            while (processor.getState() != Processor.Realized) {
                Thread.sleep(10);
            }

            outDataSource = processor.getDataOutput();

        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
    }

    public void send() {
        try {
            sendStream = videoSession.createSendStream(outDataSource, 0);
            sendStream.start();
            processor.start();

        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
    }

    public void stopMedia() {
        try {

            player.stop();
            player.deallocate();
            player.close();

            sendStream.stop();

            processor.stop();
            processor.deallocate();
            processor.close();

            videoSession.closeSession();
            videoSession.dispose();
        } catch (Exception e) {
            System.out.println("StopMedia : " + e.getMessage());
        }
    }

    @Override
    public void update(ReceiveStreamEvent rse) {
        try {
            if (rse instanceof NewReceiveStreamEvent) {
                receiveStream = rse.getReceiveStream();
                DataSource myDs = receiveStream.getDataSource();

                player = Manager.createRealizedPlayer(myDs);
                Component comp = player.getVisualComponent();
                Dimension d = comp.getSize();
                VideoFrame vframe = new VideoFrame();
                vframe.jPanel1.add(comp);
                vframe.setSize(d);
                vframe.pack();
                vframe.setVisible(true);
                player.start();
                vframe.show();
            }
        } catch (Exception e) {
            System.out.println("Update : " + e.getMessage());
        }
    }

}

class VideoFrame extends JFrame {

    JPanel jPanel1 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    FlowLayout flowLayout2 = new FlowLayout();

    public VideoFrame() {
        try {
            this.setTitle("Remote video");
            jbInit();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    void jbInit() throws Exception {
        this.getContentPane().setLayout(flowLayout1);
        jPanel1.setLayout(flowLayout2);
        this.getContentPane().add(jPanel1, null);
    }
}
