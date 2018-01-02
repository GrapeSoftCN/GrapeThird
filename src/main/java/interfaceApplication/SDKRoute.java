package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.string.StringHelper;
/**
 * 第三方平台操作
 *
 */
public class SDKRoute {
    private String pkString;
    private GrapeTreeDBModel sdkRoute;
    private GrapeDBSpecField gDbSpecField;

    public SDKRoute() {

        sdkRoute = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("SDKRoute"));
        sdkRoute.descriptionModel(gDbSpecField);
        sdkRoute.bindApp();
        pkString = sdkRoute.getPk(); // 获取主键id
    }

    /**
     * 新增第三方平台
     * 
     * @param Info
     * @return
     */
    public String AddRoute(String Info) {
        int code = 99;
        String result = rMsg.netMSG(100, "添加失败");
        if (!StringHelper.InvaildString(Info)) {
            return rMsg.netMSG(1, "非法参数");
        }
        JSONObject object = JSONObject.toJSON(Info);
        if (object != null && object.size() > 0) {
            code = sdkRoute.data(object).autoComplete().insertOnce() != null ? 0 : 99;
            result = code == 0 ? rMsg.netMSG(0, "新增成功") : result;
        }
        return result;
    }

    /**
     * 编辑第三方平台
     * 
     * @param id
     * @param Info
     * @return
     */
    public String UpdateRoute(String id, String Info) {
        int code = 99;
        String result = rMsg.netMSG(100, "修改失败");
        if (!StringHelper.InvaildString(id)) {
            return rMsg.netMSG(2, "id无效");
        }
        JSONObject object = JSONObject.toJSON(Info);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        code = sdkRoute.eq("id", id).data(object).update() != null ? 0 : 99;
        result = code == 0 ? rMsg.netMSG(0, "修改成功") : result;
        return result;
    }

    /**
     * 删除第三方平台
     * 
     * @param ids
     * @return
     */
    public String DeleteBatchRoute(String ids) {
        int code = -1;
        String result = rMsg.netMSG(100, "删除失败");
        String[] value = null;
        if (!StringHelper.InvaildString(ids)) {
            return rMsg.netMSG(2, "id无效");
        }
        value = ids.split(",");
        sdkRoute.or();
        for (String id : value) {
            sdkRoute.eq(pkString, id);
        }
        code = sdkRoute.deleteAll() != 0 ? 0 : -1;
        return (code == 0) ? rMsg.netMSG(0, "删除成功") : result;
    }

    /**
     * 分页显示第三方平台
     * @param idx
     * @param pageSize
     * @return
     */
    public String PageRoute(int idx, int pageSize) {
        long count = 0;
        JSONArray array = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        try {
            array = sdkRoute.page(idx, pageSize);
            count = sdkRoute.count();
        } catch (Exception e) {
            count = 0;
            array = null;
        }
        return rMsg.netPAGE(idx, pageSize, count, array);
    }

    /**
     * 按条件分页显示第三方平台信息
     * @param idx
     * @param pageSize
     * @param RouteInfo
     * @return
     */
    public String PageByRoute(int idx, int pageSize, String RouteInfo) {
        String out = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        JSONArray condObj = JSONArray.toJSONArray(RouteInfo);
        if (condObj != null && condObj.size() > 0) {
            sdkRoute.where(condObj);
            out = rMsg.netPAGE(idx, pageSize, sdkRoute.dirty().count(), sdkRoute.page(idx, pageSize));
        } else {
            out = rMsg.netMSG(false, "无效条件");
        }
        return out;
    }
}
