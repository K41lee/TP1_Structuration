package hai913i.tp1.model;

import java.util.List;

public record MethodeInfo(
        String name,
        int parameterCount,
        boolean isConstructor,
        List<AppelMethodInfo> calls
) {}