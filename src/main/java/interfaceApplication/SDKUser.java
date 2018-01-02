package interfaceApplication;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.check.checkHelper;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.string.StringHelper;

/**
 * 第三方用户操作
 *
 */
public class SDKUser {
    private String pkString;
    private GrapeTreeDBModel sdkUser;
    private GrapeDBSpecField gDbSpecField;

    public SDKUser() {
        sdkUser = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("SDKUser"));
        sdkUser.descriptionModel(gDbSpecField);
        sdkUser.bindApp();
        pkString = sdkUser.getPk(); // 获取主键id
    }

    /**
     * 新增第三方用户
     * 
     * @param Info
     * @return
     */
    public String AddSdkUser(String Info) {
        int code = 99;
        String result = rMsg.netMSG(100, "添加失败");
        if (!StringHelper.InvaildString(Info)) {
            return rMsg.netMSG(1, "非法参数");
        }
        JSONObject object = EscapeString(Info);
        if (object != null && object.size() > 0) {
            code = sdkUser.data(object).autoComplete().insertOnce() != null ? 0 : 99;
            result = code == 0 ? rMsg.netMSG(0, "新增成功") : result;
        }
        return result;
    }

    /**
     * 编辑第三方用户
     * 
     * @param id
     * @param Info
     * @return
     */
    public String UpdateSdkUser(String id, String Info) {
        int code = 99;
        String result = rMsg.netMSG(100, "修改失败");
        if (!StringHelper.InvaildString(id)) {
            return rMsg.netMSG(2, "id无效");
        }
        JSONObject object = EscapeString(Info);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        code = sdkUser.eq(pkString, id).data(object).update() != null ? 0 : 99;
        result = (code == 0) ? rMsg.netMSG(0, "修改成功") : result;
        return result;
    }

    /**
     * 删除第三方用户
     * 
     * @param ids
     * @return
     */
    public String DeleteBatchSdkUser(String ids) {
        int code = -1;
        String result = rMsg.netMSG(100, "删除失败");
         String[] value = null;
        if (!StringHelper.InvaildString(ids)) {
            return rMsg.netMSG(2, "id无效");
        }
         value = ids.split(",");
        if (value != null) {
            sdkUser.or();
            for (String id : value) {
                if (StringHelper.InvaildString(id)) {
                    if (ObjectId.isValid(id) && checkHelper.isInt(id)) {
                        sdkUser.eq(pkString, id);
                    }
                }
            }
            JSONArray cond = JSONArray.toJSONArray(sdkUser.condString());
            if (cond == null || cond.size() <= 0) {
                return rMsg.netMSG(2, "无效id");
            }
            code = sdkUser.deleteAll() != 0 ? 0 : -1;
        }
        return (code == 0) ? rMsg.netMSG(0, "删除成功") : result;
    }

    /**
     * 分页显示第三方平台信息
     * 
     * @param idx
     * @param pageSize
     * @return
     */
    public String PageSdkUser(int idx, int pageSize) {
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        return rMsg.netPAGE(idx, pageSize, sdkUser.dirty().count(), sdkUser.page(idx, pageSize));
    }

    /**
     * 按条件显示第三方分页信息
     * 
     * @param idx
     * @param pageSize
     * @param RouteInfo
     * @return
     */
    public String PageBySdkUser(int idx, int pageSize, String RouteInfo) {
        String out = null;
        long count = 0;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        try {
            JSONArray condObj = JSONArray.toJSONArray(RouteInfo);
            if (condObj != null && condObj.size() > 0) {
                sdkUser.where(condObj);
                JSONArray array = sdkUser.dirty().page(idx, pageSize);
                count = sdkUser.count();
                out = rMsg.netPAGE(idx, pageSize, count, array);
            } else {
                out = rMsg.netMSG(false, "无效条件");
            }
        } catch (Exception e) {
            nlogger.logout(e);
            out = rMsg.netPAGE(idx, pageSize, 0, new JSONArray());
        }
        return out;
    }
    
    /**
     * mysql中对json-string进行转义
     * @param object
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject EscapeString(String Info) {
        JSONObject object = JSONObject.toJSON(Info);
        if (object!=null && object.size() > 0) {
            if (object.containsKey("configstring")) {
                String configstring = object.getString("configstring");
                configstring = StringEscapeUtils.escapeJava(configstring);
                object.put("configstring", configstring);
            }
        }
        return object;
    }
}
