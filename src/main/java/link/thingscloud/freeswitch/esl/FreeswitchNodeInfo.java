package link.thingscloud.freeswitch.esl;


/**
 * FreeSWITCH节点信息
 * @author  easycallcenter365@gmail.com
 */
public class FreeswitchNodeInfo {

    private  String host = "127.0.0.1";
    private  int port = 8021;
    private String pass = "ClueCon";
    private  int maxLoad = 1000;
    private int currentLoad = 0;
    private int poolSize = 30;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
