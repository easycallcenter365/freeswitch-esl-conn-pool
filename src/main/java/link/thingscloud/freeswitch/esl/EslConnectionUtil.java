package link.thingscloud.freeswitch.esl;

import com.alibaba.fastjson.JSON;
import link.thingscloud.freeswitch.esl.inbound.handler.InboundChannelHandler;
import link.thingscloud.freeswitch.esl.transport.CommandResponse;
import link.thingscloud.freeswitch.esl.transport.message.EslMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *  FreeSWITCH Esl-pool util methods.
 *  This class is exposed to external program to control your FreeSWITCH instance.
 * @author  easycallcenter365@gmail.com
 */
public class EslConnectionUtil  {

    protected static final Logger logger = LoggerFactory.getLogger(EslConnectionUtil.class);

    private static  final ConcurrentHashMap<String, EslConnectionPool> eslConnectionPools = new ConcurrentHashMap<>(200);

    public static void setEslExecuteTime(int timeout){
        InboundChannelHandler.setEslExecuteTimeout(timeout);
    }

    public synchronized static void initConnPool( List<FreeswitchNodeInfo> nodeList) {
        if (eslConnectionPools.size() != 0 || nodeList == null || nodeList.size() == 0) {
            return;
        }
        try {
            for (FreeswitchNodeInfo node : nodeList) {
                createEslConnectionPool(node);
            }
        } catch (Exception e) {
            logger.error("initConnPool error: {}", e.toString());
        }
    }


    /**
     *  Gets the default esl connection pool,
     *  which can only be used if there is only one FreeSWITCH instance.
     * @return
     */
    public static EslConnectionPool getDefaultEslConnectionPool(){
        if(eslConnectionPools.size() > 0) {
            for (Map.Entry<String, EslConnectionPool> entry : eslConnectionPools.entrySet()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     *  get esl connection pool by FreeSWITCH instance info.
     * @param host
     * @param port
     * @return
     */
    public static EslConnectionPool getEslConnectionPool(String host, int port){
        String hostKey = String.format("%s:%d", host, port);
        return eslConnectionPools.get(hostKey);
    }


    /**
     *  create esl connection pool for a  FreeSWITCH instance
     * @param nodeInfo
     * @return
     */
    private static EslConnectionPool createEslConnectionPool(FreeswitchNodeInfo nodeInfo){
        String host = nodeInfo.getHost();
        int port = nodeInfo.getPort();
        String pass = nodeInfo.getPass();
        int poolSize = nodeInfo.getPoolSize();
        String hostKey = String.format("%s:%d", host, port);
       EslConnectionPool connectionPool = eslConnectionPools.get(hostKey);
        if(null == connectionPool) {
            synchronized (hostKey.intern()) {
                connectionPool = eslConnectionPools.get(hostKey);
                if (null == connectionPool) {
                     connectionPool =  EslConnectionPool.createPool(poolSize, host, port , pass);
                    eslConnectionPools.put(hostKey,  connectionPool);
                }
            }
        }
        return connectionPool;
    }

    /**
     *  exec app
     * @param app
     * @param param
     * @param uuid
     */
    public static  String sendExecuteCommand(String app, String param, String uuid){
        if(eslConnectionPools.size() > 0) {
            return sendExecuteCommand(app, param, uuid, getDefaultEslConnectionPool());
        }
        return "No default EslConnectionPool found.";
    }

    /**
     *  exec app
     * @param app
     * @param param
     * @param uuid
     * @param eslConnectionPool
     */
    public static  String sendExecuteCommand(String app, String param, String uuid, EslConnectionPool eslConnectionPool){
        long startTime = System.currentTimeMillis();
        String traceId = uuid;
        EslConnectionDetail connection = eslConnectionPool.getConnection();
        long cost = System.currentTimeMillis() - startTime;
        logger.info("{} successfully borrow a esl connection , cost: {} mills.", traceId, cost);
        String response = "";
        try {
            long cmdStartTime = System.currentTimeMillis();
            CommandResponse cmdResponse  = connection.getRealConn().execute(
                    eslConnectionPool.getEslAddr(),
                    app,
                    param,
                    uuid
            );
            cost = System.currentTimeMillis() - cmdStartTime;
            response = JSON.toJSONString(cmdResponse);
            logger.info("{} [sendExecuteCommand] {}  cost: {} mills. ", traceId, app, cost);
            logger.info("{} [sendExecuteCommand] {} successfully get response:{}, from esl connection.", traceId, app, response);
        }catch ( Exception e )
        {
        }finally {
            eslConnectionPool.releaseOneConn(connection);
            cost = System.currentTimeMillis() - startTime;
            logger.info("{} successfully return  a esl connection to fs connPool, total cost: {} mills.", traceId, cost);
        }
        return  response;
    }

    /**
     *  exec async api
     * @param api
     * @param param
     * @return
     */
    public static  String sendAsyncApiCommand(String api, String param ) {
        if(eslConnectionPools.size() > 0) {
            return sendAsyncApiCommand(api, param, getDefaultEslConnectionPool());
        }
        return "No default EslConnectionPool found.";
    }

    /**
     * exec async api
     * @param api
     * @param param
     * @param eslConnectionPool
     * @return
     */
    public static  String sendAsyncApiCommand(String api, String param, EslConnectionPool eslConnectionPool){
        long startTime = System.currentTimeMillis();
        String traceId = param;
        String spacer = " ";
        if(param.contains(spacer)){
            traceId = param.split(spacer)[0];
        }
        EslConnectionDetail  connection = eslConnectionPool.getConnection();
        long cost = System.currentTimeMillis() - startTime;
        logger.info("{} successfully borrow a esl connection , cost: {} mills.", traceId, cost);
        String response = null;
        try {
            long cmdStartTime = System.currentTimeMillis();
            response = connection.getRealConn().sendAsyncApiCommand(
                    eslConnectionPool.getEslAddr(),
                    api,
                    param);
            cost = System.currentTimeMillis() - cmdStartTime;
            logger.info("{} [sendAsyncApiCommand] {}  cost: {} mills. ", traceId, api, cost);
            logger.info("{} [sendAsyncApiCommand] successfully get response:{}, from esl connection, cost: {} mills.", traceId,  response, cost);
        }catch ( Exception e )
        {
        }finally {
            eslConnectionPool.releaseOneConn(connection);
            cost = System.currentTimeMillis() - startTime;
            logger.info("{} successfully return  a esl connection to fs connPool, total cost: {} mills.", traceId, cost);
        }
        return response;
    }


    /**
     * exec sync api
     * @param api
     * @param param
     * @return
     */
    public static EslMessage sendSyncApiCommand(String api, String param){
        if(eslConnectionPools.size() > 0) {
            return sendSyncApiCommand(api, param, getDefaultEslConnectionPool());
        }
        logger.error("No default EslConnectionPool found.");
        return null;
    }

    /**
     * exec sync api
     * @param api
     * @param param
     * @param eslConnectionPool
     * @return
     */
    public static  EslMessage sendSyncApiCommand(String api, String param, EslConnectionPool eslConnectionPool){
        long startTime = System.currentTimeMillis();
        String traceId = param;
        String spacer = " ";
        if(param.contains(spacer)){
            traceId = param.split(spacer)[0];
        }
        EslConnectionDetail  connection = eslConnectionPool.getConnection();
        long cost = System.currentTimeMillis() - startTime;
        logger.info("{} successfully borrow a esl connection , cost: {} mills.", traceId, cost);
        EslMessage response = null;
        try {
            long cmdStartTime = System.currentTimeMillis();
            response = connection.getRealConn().sendSyncApiCommand(
                    eslConnectionPool.getEslAddr(),
                    api,
                    param);
            cost = System.currentTimeMillis() - cmdStartTime;
            logger.info("{} [sendSyncApiCommand] {}  cost: {} mills. ", traceId, api, cost);
            logger.info("{} [sendSyncApiCommand]  successfully get response:{}, from esl connection, cost: {} mills.", traceId,  response, cost);
        }catch ( Exception e )
        {
        }finally {
            eslConnectionPool.releaseOneConn(connection);
            cost = System.currentTimeMillis() - startTime;
            logger.info("{} successfully return  a esl connection to fs connPool, total cost: {} mills.", traceId, cost);
        }
        return response;
    }

}