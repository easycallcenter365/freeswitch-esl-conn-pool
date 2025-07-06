package link.thingscloud.freeswitch.esl;

import java.util.Date;


public class UuidGenerator {

	  private static final Object syncRoot = new Object();
	  private static final String DATE_FORMAT = "yyMMddHHmmss";
	  private static final int MAX_COUNTER = 10000;
      private static long lastNumber = MAX_COUNTER;
      private static String timeStr = DateUtils.format(new Date(), DATE_FORMAT);
      private static final String CALL_NODE_NO = "esl";
      public static String getOneUuid()
      {
    	  synchronized (syncRoot)
          {
          	  String currentTimeStr = DateUtils.format(new Date(), DATE_FORMAT);
              if (!timeStr.equals(currentTimeStr))
              {
                  lastNumber = MAX_COUNTER;
                  timeStr = currentTimeStr;
              }
              lastNumber += 1;
              return timeStr + CALL_NODE_NO + lastNumber;
          }
      }
}
