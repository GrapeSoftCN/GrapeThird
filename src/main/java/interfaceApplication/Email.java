package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appsProxy;
import email.mail;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import nlogger.nlogger;
import security.codec;
import string.StringHelper;

/**
 * 邮箱操作
 * 
 *
 */
public class Email {
    private String pkString;
    private GrapeTreeDBModel em;
    private GrapeDBSpecField gDbSpecField;

    public Email() {
        em = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("Email"));
        em.descriptionModel(gDbSpecField);
        em.bindApp();
        pkString = em.getPk(); // 获取主键id
    }

    /**
     * 新增邮箱host信息
     * 
     * @param Info
     * @return
     */
    public String AddEmail(String Info) {
        int code = 99;
        String result = rMsg.netMSG(100, "添加失败");
        if (!StringHelper.InvaildString(Info)) {
            return rMsg.netMSG(1, "非法参数");
        }
        JSONObject object = JSONObject.toJSON(Info);
        if (object != null && object.size() > 0) {
            code = em.data(object).autoComplete().insertOnce() != null ? 0 : 99;
            result = code == 0 ? rMsg.netMSG(0, "新增成功") : result;
        }
        return result;
    }

    /**
     * 编辑邮箱host信息
     * 
     * @param id
     * @param Info
     * @return
     */
    public String UpdateEmail(String id, String Info) {
        int code = 99;
        String result = rMsg.netMSG(100, "修改失败");
        if (!StringHelper.InvaildString(id)) {
            return rMsg.netMSG(2, "id无效");
        }
        JSONObject object = JSONObject.toJSON(Info);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        code = em.eq("id", id).data(object).update() != null ? 0 : 99;
        result = code == 0 ? rMsg.netMSG(0, "修改成功") : result;
        return result;
    }

    /**
     * 删除邮箱host信息
     * 
     * @param ids
     * @return
     */
    public String DeleteBatchEmail(String ids) {
        int code = -1;
        String result = rMsg.netMSG(100, "删除失败");
        String[] value = null;
        if (!StringHelper.InvaildString(ids)) {
            return rMsg.netMSG(2, "id无效");
        }
        value = ids.split(",");
        em.or();
        for (String id : value) {
            em.eq(pkString, id);
        }
        code = em.deleteAll() != 0 ? 0 : -1;
        return (code == 0) ? rMsg.netMSG(0, "删除成功") : result;
    }

    /**
     * 分页显示邮箱host信息
     * 
     * @param idx
     * @param pageSize
     * @return
     */
    public String PageEmail(int idx, int pageSize) {
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        return rMsg.netPAGE(idx, pageSize, em.dirty().count(), em.page(idx, pageSize));
    }

    /**
     * 按条件分页显示邮箱host信息
     * 
     * @param idx
     * @param pageSize
     * @param RouteInfo
     * @return
     */
    public String PageByEmail(int idx, int pageSize, String RouteInfo) {
        String out = null;
        if (idx <= 0) {
            return rMsg.netMSG(false, "页码错误");
        }
        if (pageSize <= 0) {
            return rMsg.netMSG(false, "页长度错误");
        }
        JSONArray condObj = JSONArray.toJSONArray(RouteInfo);
        if (condObj != null && condObj.size() > 0) {
            em.where(condObj);
            out = rMsg.netPAGE(idx, pageSize, em.dirty().count(), em.page(idx, pageSize));
        } else {
            out = rMsg.netMSG(false, "无效条件");
        }
        return out;
    }

    /**
     * 发送邮件
     * 
     * @param sdkuser
     *            发送人邮箱id
     * @param emaiInfo
     *            邮件信息，整体编码：base64+特殊格式
     * @return
     */
    public String SendEmail(int sdkuser, String emaiInfo) {
        boolean flag = false;
        String smtpHost = "", sendUserName = "", sendUserPass = "";
        if (StringHelper.InvaildString(String.valueOf(sdkuser))) {
            return rMsg.netMSG(1, "非法id");
        }

        JSONObject object = JSONObject.toJSON(codec.DecodeFastJSON(emaiInfo));
        if (object == null || object.size() > 0) {
            return rMsg.netMSG(2, "非法参数");
        }

        object = get(sdkuser);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(3, "发件人信息处在不可用状态或者发件人信息不存在");
        }
        smtpHost = object.getString("smtp");
        sendUserName = object.getString("userid");
        sendUserPass = object.getString("password");

        if (StringHelper.InvaildString(smtpHost) && StringHelper.InvaildString(sendUserName) && StringHelper.InvaildString(sendUserPass)) {
            try {
                mail mails = mail.entity(smtpHost, sendUserName, sendUserPass, "", "", "", "", null);
                flag = mails.send();
            } catch (Exception e) {
                nlogger.logout(e);
                flag = false;
            }
        } else {
            return rMsg.netMSG(100, "发送人信息设置错误，邮件发送失败");
        }
        return flag ? rMsg.netMSG(0, "邮件发送成功") : rMsg.netMSG(0, "邮件发送失败");
    }

    /**
     * 获取发件人邮箱信息
     *    state  状态 ：0：可用状态；1：不可用状态
     * @param sdkUserID
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONObject get(int sdkUserID) {
        JSONObject object = null;
        String[] key = { "smtp", "userid", "password" };
        object = em.eq(pkString, sdkUserID).eq("state", 0).field(key).find();
        if (object != null && object.size() > 0) {
            for (String keyString : key) {
                if (!object.containsKey(keyString)) {
                    object.put(keyString, "");
                }
            }
        }
        return object;
    }
}
