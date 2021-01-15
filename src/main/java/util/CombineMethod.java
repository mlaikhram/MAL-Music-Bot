package util;

public enum CombineMethod {
    UNIFORM,    // each anime gets represented equally
    BALANCED,   // each user gets represented equally
    OVERLAP,    // only include anime that at least 2 people have watched
    WEIGHTED    // anime that has been watched by more people will be more likely to be selected (default)
}
