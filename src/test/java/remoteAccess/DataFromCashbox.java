package remoteAccess;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс используется, если нужно выполнить какие-то команды на кассе, например reaboot или date
 */

public class DataFromCashbox {
    private static final int CONNECTION_TIMEOUT = 100000;
    private static final int BUFFER_SIZE = 1024;

    private static Session sshSessionCahbox;

    public List<String> executeListCommand(String commandInput) {
        List<String> lines = new ArrayList<>();
        try {
            String cmd = new String(commandInput.getBytes(), "Cp866");
            Channel channel = initChannel(cmd, sshSessionCahbox);//commandInput, sshSessionCahbox);
            InputStream in = channel.getInputStream();
            channel.connect();
            String dataFromChannel = getDataFromChannel(channel, in);
            lines.addAll(Arrays.asList(dataFromChannel.split("\n")));
            channel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    public void disconnectSession() {
        sshSessionCahbox.disconnect();
    }

    public void initSession(String host, String username, int port, String password) {
        Session session = null;
        JSch jsch;

        try {
            jsch = new JSch();
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig(config);
            session.connect(CONNECTION_TIMEOUT);
            System.out.println("Connected to host: "+ host + " under " + username + " user.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sshSessionCahbox = session;
    }

    private Channel initChannel(String commands, Session session) throws JSchException {
        Channel channel = session.openChannel("exec");

        ChannelExec channelExec = (ChannelExec) channel;
        channelExec.setCommand(commands);
        channelExec.setInputStream(null);
        channelExec.setErrStream(System.err);
        return channel;
    }

    private String getDataFromChannel(Channel channel, InputStream in) throws IOException {
        StringBuilder result = new StringBuilder();
        byte[] tmp = new byte[BUFFER_SIZE];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp,  0, BUFFER_SIZE);
                if (i < 0) {
                    break;
                }
                result.append(new String(tmp, 0, i, "Cp866"));
            }
            if (channel.isClosed()) {
                int exitStatus = channel.getExitStatus();
                System.out.println("exit-status: " + exitStatus);
                break;
            }
            trySleep();
        }
        return result.toString();
    }

    private void trySleep() {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

