package com.iot.mycontrol;

/**
 * @ClassName: StringOperate
 * @Description:
 * @Author: TangJW
 */
public class StringOperate {
    public String stringDel(String str_src,char delChar)
    {
        String result = "";
        for (int i = 0; i < str_src.length(); i++)
        {
            if (str_src.charAt(i) != delChar)
            {
                result += str_src.charAt(i);
            }
        }
        return result;
    }
}
