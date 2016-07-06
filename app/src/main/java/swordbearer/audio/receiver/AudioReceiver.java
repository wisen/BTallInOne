package swordbearer.audio.receiver;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import swordbearer.audio.NetConfig;

/**
 * Created by wisen on 2016-06-28.
 */
public class AudioReceiver implements Runnable {
    String LOG = "AudioReceiver";
    int port = NetConfig.CLIENT_PORT;// 鎺ユ敹鐨勭鍙?
    DatagramSocket socket;
    DatagramPacket packet;
    boolean isRunning = false;

    private byte[] packetBuf = new byte[1024];
    private int packetSize = 1024;

    /*
     * 寮€濮嬫帴鏀舵暟鎹?
     */
    public void startRecieving() {
        if (socket == null) {
            try {
                socket = new DatagramSocket(port);
                packet = new DatagramPacket(packetBuf, packetSize);
            } catch (SocketException e) {
            }
        }
        new Thread(this).start();
    }

    /*
     * 鍋滄鎺ユ敹鏁版嵁
     */
    public void stopRecieving() {
        isRunning = false;
    }

    /*
     * 閲婃斁璧勬簮
     */
    private void release() {
        if (packet != null) {
            packet = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    public void run() {
        // 鍦ㄦ帴鏀跺墠锛岃鍏堝惎鍔ㄨВ鐮佸櫒
        AudioDecoder decoder = AudioDecoder.getInstance();
        decoder.startDecoding();

        isRunning = true;
        try {
            while (isRunning) {
                socket.receive(packet);
                // Log.i(LOG, "鏀跺埌涓€涓寘..." + packet.getLength());
                // 姣忔帴鏀朵竴涓猆DP鍖咃紝灏变氦缁欒В鐮佸櫒锛岀瓑寰呰В鐮?
                decoder.addData(packet.getData(), packet.getLength());
            }

        } catch (IOException e) {
            Log.e(LOG, "RECIEVE ERROR!");
        }
        // 鎺ユ敹瀹屾垚锛屽仠姝㈣В鐮佸櫒锛岄噴鏀捐祫婧?
        decoder.stopDecoding();
        release();
        Log.e(LOG, "stop recieving");
    }
}

