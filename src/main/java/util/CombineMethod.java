package util;

import java.util.HashMap;
import java.util.Map;

public enum CombineMethod {
    UNIFORM,   // each anime gets represented equally
    BALANCED,  // each user gets represented equally
    OVERLAP,   // only include anime that at least 2 users have watched, with increasing weight for anime watched by more users
    INTERSECT, // only include anime that all users have watched
    WEIGHTED;  // anime that has been watched by more users will be more likely to be selected (default)

    private static final Map<Integer, CombineMethod> intToTypeMap = new HashMap<>();
    static {
        for (int i = 0; i < CombineMethod.values().length; ++i) {
            intToTypeMap.put(i, CombineMethod.values()[i]);
        }
    }

    public static CombineMethod fromInt(int i) {
        CombineMethod type = intToTypeMap.get(Integer.valueOf(i));
        if (type == null)
            return CombineMethod.WEIGHTED;
        return type;
    }
}