package com.lzx2005.system.order.utils;

import java.util.UUID;

/**
 * Created by john on 2017/5/22.
 */

public class SUID {

    private static long systemTimeMillis = 0l;
    private static long synchronizedNum = 0l;
    private static StringBuilder primaryUniqueKey;

    /**
     * @Title: getPUKey
     * @Description: 获得一个数据记录的主键Key<时间戳生成>
     * @return
     * @return String
     * @throws
     */
    @Deprecated
    public synchronized static String getPUKey() {
        primaryUniqueKey = new StringBuilder(25);
        long currentTimeMillis = System.currentTimeMillis();
        // 同步判定
        if (currentTimeMillis > systemTimeMillis) {
            synchronizedNum = 0l;
            systemTimeMillis = currentTimeMillis;
        } else {
            synchronizedNum++;
        }
        primaryUniqueKey.append(systemTimeMillis).append(synchronizedNum);
        return primaryUniqueKey.toString();
    }

    /**
     * @Title: getUUID
     * @Description: 生成32位编码
     * @return
     * @return String
     * @throws
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().trim().replaceAll("-", "");
    }

    public static void main(String[] args) {
        for (int i=0;i<=10;i++) {
            System.out.println(getUUID());

//----------------------------------------

        }
    }
}
