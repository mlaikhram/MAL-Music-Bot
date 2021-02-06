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
        CombineMethod type = intToTypeMap.get(i);
        if (type == null)
            return CombineMethod.WEIGHTED;
        return type;
    }

    public static String getInfoText() {
        return "Here's all of the combine methods I know:\n" +
                "\n" +
                "`0 (UNIFORM)`\n" +
                "Each anime gets represented equally\n" +
                "\n" +
                "`1 (BALANCED)`\n" +
                "Each user gets represented equally\n" +
                "\n" +
                "`2 (OVERLAP)`\n" +
                "Only include anime that at least 2 users have watched, with increased weight for anime watched by more users\n" +
                "\n" +
                "`3 (INTERSECT)`\n" +
                "Only include anime that all users have watched\n" +
                "\n" +
                "`4 (WEIGHTED)`\n" +
                "Anime that has been watched by more users will be more likely to be selected [Default Option]";
    }
}