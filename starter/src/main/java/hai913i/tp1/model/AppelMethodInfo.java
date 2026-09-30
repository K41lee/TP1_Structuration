package hai913i.tp1.model;

public record AppelMethodInfo(
        String callerMethodName,
        String targetClassName,
        String targetMethodName,
        String receiverType,
        int lineNumber,
        boolean isInternal
) {}