package interfaceApplication;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.email.emailhost;
import common.java.email.mail;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.string.StringHelper;

/**
 * 邮箱操作
 * 
 *
 */
public class Email1 {
    private String appid = appsProxy.appidString();
    private GrapeTreeDBModel em;
    private GrapeDBSpecField gDbSpecField;

    public Email1() {
        em = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("Email"));
        em.descriptionModel(gDbSpecField);
    }

    /**
     * 新增邮箱host信息
     * 
     * @param Info
     * @return
     */
    public String AddEmail(String Info) {
        String tip = null;
        String result = rMsg.netMSG(100, "新增失败");
        String userid = "", password = "", smtp = "", pop3 = "";
        JSONObject object = JSONObject.toJSON(Info);
        if (object != null && object.size() > 0) {
            if (object.containsKey("userid")) {
                userid = object.getString("userid");
            }
            if (object.containsKey("password")) {
                password = object.getString("password");
            }
            if (object.containsKey("smtp")) {
                smtp = object.getString("smtp");
            }
            if (object.containsKey("pop3")) {
                pop3 = object.getString("pop3");
            }
            tip = emailhost.addHost(appid, userid, password, smtp, pop3);
        }
        return (tip != null) ? rMsg.netMSG(0, "新增成功") : result;
    }

    /**
     * 编辑邮箱host信息
     * 
     * @param id
     * @param Info
     * @return
     */
    public String UpdateEmail(String id, String Info) {
        String result = rMsg.netMSG(100, "修改失败");
        if (!StringHelper.InvaildString(id)) {
            return rMsg.netMSG(2, "id无效");
        }
        JSONObject object = JSONObject.toJSON(Info);
        if (object == null || object.size() <= 0) {
            return rMsg.netMSG(1, "无效参数");
        }
        boolean flag = emailhost.editHost(Integer.parseInt(id), object);
        return flag ? rMsg.netMSG(0, "修改成功") : result;
    }

    /**
     * 删除邮箱host信息
     * 
     * @param ids
     * @return
     */
    public String DeleteBatchEmail(String ids) {
        String result = rMsg.netMSG(100, "删除失败");
        if (!StringHelper.InvaildString(ids)) {
            return rMsg.netMSG(2, "id无效");
        }
        boolean flag = emailhost.removeHost(ids);
        return flag ? rMsg.netMSG(0, "删除成功") : result;
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
        em.eq("ownid", appid);
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
            em.where(condObj).eq("ownid", appid);
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
    public String SendEmail(int hostid, String emaiInfo) {
        List<String> attachments = new ArrayList<String>();
        String toUser = "", CC = "", mailBody = "", subject = "", attachment = "";
        if (!StringHelper.InvaildString(emaiInfo)) {
            return rMsg.netMSG(1, "无效邮件信息");
        }
        JSONObject object = JSONObject.toJSON(emaiInfo);
        if (object != null && object.size() > 0) {
            if (object.containsKey("to")) {
                toUser = object.getString("to"); //收件人
            }
            if (object.containsKey("CC")) {
                CC = object.getString("CC"); //抄送
            }
            if (object.containsKey("subject")) {
                subject = object.getString("subject"); //邮件主题
            }
            if (object.containsKey("body")) {
                mailBody = object.getString("body"); //邮件内容
            }
            if (object.containsKey("attachment")) {
                attachment = object.getString("attachment");
                attachments = String2List(attachment);
            }
            mail mails = mail.defaultEntity(hostid, toUser, CC, subject, mailBody, attachments);
            try {
                boolean flag = mails.send();
                return flag ? rMsg.netMSG(0, "邮件发送成功") : rMsg.netMSG(0, "邮件发送失败");
            } catch (Exception e) {
                nlogger.logout(e);
            }
        }
        return rMsg.netMSG(0, "邮件发送失败");
    }

    /**
     * 字符串转换为List
     * @param str
     * @return
     */
    private List<String> String2List(String str) {
        String[] value = null;
        List<String> list = null;
        if (StringHelper.InvaildString(str)) {
            list = new ArrayList<>();
            value = str.split(",");
            for (String string : value) {
                list.add(string);
            }
        }
        return list;
    }
}
