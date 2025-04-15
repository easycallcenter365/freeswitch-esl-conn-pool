package link.thingscloud.freeswitch.esl;

public class CommonUtils {


	public static String getStackTraceString(StackTraceElement[] stackTraceElements){
		StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stackTraceElements.length; i++) {
            stringBuilder.append("ClassName:");
	        stringBuilder.append(stackTraceElements[i].getClassName());
			stringBuilder.append("\n FileName:");
			stringBuilder.append(stackTraceElements[i].getFileName());
			stringBuilder.append("\n LineNumber:");
			stringBuilder.append(stackTraceElements[i].getLineNumber());
			stringBuilder.append("\n MethodName:");
			stringBuilder.append(stackTraceElements[i].getMethodName());
          }
        return stringBuilder.toString();
	}

}