package com.example.recruitmentagent.constant;

public enum NextActionEnum {
    INTERVIEW,
    PHONE_SCREEN,
    REJECT,
    NEED_MORE_INFO;

    public static boolean isValid(String value) {
        if (value == null) return false;
        for (NextActionEnum action : values()) {
            if (action.name().equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }
}
