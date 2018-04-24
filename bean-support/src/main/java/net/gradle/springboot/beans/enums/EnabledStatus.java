package net.gradle.springboot.beans.enums;

/**
 * 可用不可用状态的Status
 */
public enum EnabledStatus implements CodedEnum<Integer>{
    enabled(1), disabled(0);
    public int code;
    EnabledStatus(int code){this.code = code;}
    @Override
    public Integer getCode() {
        return code;
    }

    /**
     * 子类必须实现这个类
     * @param code
     * @return
     */
    public static EnabledStatus translate(Integer code){
        return code.equals(1) ? enabled : disabled;
    }
}
