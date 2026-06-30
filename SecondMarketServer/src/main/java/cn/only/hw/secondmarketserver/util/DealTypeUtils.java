package cn.only.hw.secondmarketserver.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class DealTypeUtils {

    public static final String EXPRESS_DELIVERY = "线上快递发货";

    public static final String FACE_TO_FACE = "线下面对面交易";

    private static final Set<String> EXPRESS_TYPES = new HashSet<>(Arrays.asList(
            EXPRESS_DELIVERY,
            "平台送货上门",
            "卖家送货上门"
    ));

    private static final Set<String> FACE_TO_FACE_TYPES = new HashSet<>(Arrays.asList(
            FACE_TO_FACE,
            "买家上门自提",
            "互换"
    ));

    private DealTypeUtils() {
    }

    public static String normalizeForRead(String dealType) {
        String normalized = normalizeKnownType(dealType);
        if (normalized != null) {
            return normalized;
        }
        return trimToNull(dealType);
    }

    public static String normalizeForSave(String dealType) {
        return normalizeKnownType(dealType);
    }

    public static boolean isFaceToFace(String dealType) {
        String normalizedDealType = trimToNull(dealType);
        return normalizedDealType != null && FACE_TO_FACE_TYPES.contains(normalizedDealType);
    }

    public static boolean requiresLogistics(String dealType) {
        return !isFaceToFace(dealType);
    }

    private static String normalizeKnownType(String dealType) {
        String normalizedDealType = trimToNull(dealType);
        if (normalizedDealType == null) {
            return null;
        }
        if (EXPRESS_TYPES.contains(normalizedDealType)) {
            return EXPRESS_DELIVERY;
        }
        if (FACE_TO_FACE_TYPES.contains(normalizedDealType)) {
            return FACE_TO_FACE;
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
