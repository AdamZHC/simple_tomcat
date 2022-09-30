package com.hit.adam.tomcat.http.reinforce;

import com.alibaba.fastjson.JSONObject;

public final class ReinforceJsonObjectWrapper implements ReinforceLocked{


    /**
     * 此参数表示JsonObject的锁定状态
     */
    private boolean locked = false;

    public ReinforceJsonObjectWrapper() {
        super();
    }

    public JSONObject parsePostData(String postData) {
        if(locked)
            throw new IllegalStateException("ReinforceJsonObject is locked,parse map is fail!");
        setLocked(true);
        return JSONObject.parseObject(postData);
    }


    /**
     * 返回JsonObject的锁定状态
     */
    public boolean isLocked() {

        return (this.locked);

    }
    /**
     * 设置JsonObject的锁定状态
     *
     * @param locked 锁定状态
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
