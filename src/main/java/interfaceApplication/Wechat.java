package interfaceApplication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import apps.appIns;
import apps.appsProxy;
import database.DBHelper;
import database.db;
import security.codec;
import string.StringHelper;
import thirdsdk.wechatHelper;
import thirdsdk.wechatModel;

/**
 * 推送消息到微信，支持图文和文本
 * 支持保存素材至微信
 *
 */
public class Wechat {
    private String appid = appsProxy.appidString();
    private wechatHelper wechatHelper;

    /**
     * 发送文本消息
     * 
     * @param wechatServID
     *            第三方用户id
     * @param info
     *            待发送的文本消息
     * @return
     */
    public String SendText(int wechatServID, String info) {
        JSONObject object = null;
        long code = 0;
        String result = rMsg.netMSG(100, "发送失败");
        wechatHelper = new wechatHelper(wechatServID);
        if (StringHelper.InvaildString(info)) {
            JSONObject contents = wechatModel.toall.text(info);
            object = wechatHelper.send2all(0, contents, wechatModel.MSGTYPE_TEXT);
        }
        if (object != null && object.size() > 0) {
            code = Long.parseLong(object.getString("errcode"));
            result = (code == 0) ? rMsg.netMSG(0, "发送成功") : rMsg.netMSG(new Long(code).intValue(), object.getString("errmsg"));
        }
        return result;
    }

    /**
     * 推送文章到微信
     * 
     * @param sdkUserId
     * @param newsData
     * @return
     */
    public String pushArcToWechat(String sdkUserId, String newsData) {
        int code = 99;
        JSONArray tempArray = null;
        JSONArray DataArray = getContents(newsData);
        JSONObject ContentArray = getMpNews(sdkUserId, DataArray);
        if (ContentArray != null && ContentArray.size() != 0) {
            if (ContentArray.containsKey("errorcode")) {
                return ContentArray.toJSONString();
            }
            if (ContentArray.containsKey("mpnews")) {
                tempArray = ContentArray.getJsonArray("mpnews");
                if (tempArray != null && tempArray.size() > 0) {
                    code = pushMpNews(sdkUserId, tempArray);
                }
            }
            if (ContentArray.containsKey("text")) {
                tempArray = ContentArray.getJsonArray("text");
                if (tempArray != null && tempArray.size() != 0) {
                    code = pushNews(sdkUserId, tempArray);
                }
            }
        }

        return rMsg.netMSG(code, (code == 0 ? "文章推送到微信成功" : "文章推送失败，请重试"));
    }

    /**
     * 推送图文消息
     * 
     * @param sdkUserId
     * @param array
     * @return
     */
    private int pushMpNews(String sdkUserId, JSONArray array) {
        JSONObject Obj = new JSONObject();
        int code = 99;
        int errcode = 0;
        if (array != null && array.size() != 0) {
            wechatHelper weHelper = getHelper(sdkUserId);
            Obj = uploadMaterial(sdkUserId, array);
            if (Obj != null && Obj.size() > 0) {
                Obj = weHelper.send2all(0, wechatModel.toall.mpNews(Obj.getString("media_id")), wechatModel.MSGTYPE_MPNEWS);
                if (Obj.containsKey("errcode")) {
                    errcode = Integer.parseInt(Obj.getString("errcode"));
                }
                code = (errcode == 0) ? 0 : 99;
            }
        }
        return code;
    }

    private int pushNews(String sdkUserId, JSONArray array) {
        int code = 99;
        int errcode = 0;
        JSONObject obj = new JSONObject(), temp = new JSONObject();
        wechatHelper weHelper = getHelper(sdkUserId);
        if (weHelper != null) {
            for (Object object : array) {
                obj = (JSONObject) object;
                temp = weHelper.send2all(0, wechatModel.toall.text(obj.getString("content")), wechatModel.MSGTYPE_TEXT);
                if (temp.containsKey("errcode")) {
                    errcode = Integer.parseInt(temp.getString("errcode"));
                }
                code = (errcode == 0) ? 0 : 99;
            }
        }
        return code;
    }

    /**
     * 保存文章数据至素材库
     * 
     * @param sdkUserId
     * @param newsData
     * @return
     */
    public String getNews(String sdkUserId, String newsData) {
        JSONObject ContentArray = null;
        JSONArray tempArray = null;
        JSONObject obj = null;
        JSONArray DataArray = getContents(newsData);
        if (DataArray != null && DataArray.size() > 0) {
            ContentArray = getMpNews(sdkUserId, DataArray);
        }
        if (ContentArray != null && ContentArray.size() != 0) {
            if (ContentArray.containsKey("errorcode")) {
                return ContentArray.toJSONString();
            }
            if (ContentArray.containsKey("mpnews")) {
                tempArray = ContentArray.getJsonArray("mpnews");
                if (tempArray != null && tempArray.size() > 0) {
                    obj = uploadMaterial(sdkUserId, tempArray);
                }
            }
        }
        return rMsg.netMSG(0, (obj != null && obj.size() > 0) ? "保存至素材库成功" : "保存至素材库失败");
    }

    /**
     * 获取微信图文新闻
     * 
     * @project GrapeWechat_temp
     * @package interfaceApplication
     * @file Wechat.java
     * 
     * @param id
     * @param DataArray
     * @return
     *
     */
    @SuppressWarnings("unchecked")
    private JSONObject getMpNews(String id, JSONArray DataArray) {
        JSONObject obj = new JSONObject();
        wechatModel.uploadArticle upload = new wechatModel.uploadArticle();
        String mainName = "", content = "", desp = "", mediaId = "", image = "", author = "", suffix = "";
        JSONObject object;
        JSONArray array = null;
        JSONArray Textarray = new JSONArray();
        if (DataArray != null && DataArray.size() != 0) {
            int l = DataArray.size();
            l = l > 8 ? 8 : l;
            for (int i = 0; i < l; i++) {
                object = (JSONObject) DataArray.get(i);
                if (object != null && object.size() != 0) {
                    mainName = object.containsKey("mainName") ? object.getString("mainName") : "";
                    author = object.containsKey("author") ? object.getString("author") : "";
                    content = object.containsKey("content") ? object.getString("content") : "";
                    suffix = object.containsKey("suffix") ? object.getString("suffix") : "";
                    String urls = "/WechatUploadMedia/uploadImages/getImageFromContent/" + id;
                    JSONObject postParam = new JSONObject("param", content);
                    appIns apps = appsProxy.getCurrentAppInfo();
                    content = (String) appsProxy.proxyCall(urls, postParam, apps);
                    content = content + suffix;
                    content = content.replaceAll("\\u201C", "\"");
                    content = content.replaceAll("\\u201D", "\"");
                    mainName = mainName.replace("\\u201C", "\"");
                    mainName = mainName.replaceAll("\\u201D", "\"");
                    desp = object.containsKey("desp") ? object.getString("desp") : "";
                    image = object.containsKey("image") ? object.getString("image") : "";
                    System.out.println(image);
                    image = image.equals("") ? (object.containsKey("thumbnail") ? object.getString("thumbnail") : image) : image;
                }
                if (StringHelper.InvaildString(image)) {
                    String temp = (String) appsProxy.proxyCall("/WechatUploadMedia/uploadImages/uploadmedia/" + id + "/" + image);
                    JSONObject media = JSONObject.toJSON(temp);
                    if (media.containsKey("errorcode")) {
                        return media;
                    }
                    if (media != null && media.size() > 0) {
                        mediaId = (media != null && media.size() != 0) ? media.getString("media_id") : "";
                        array = upload.newArticle(mediaId, author, mainName, content, "", desp, false).toArticleArray();
                    }
                } else {
                    Textarray.add(object);
                }
            }
            obj.put("mpnews", array);
            obj.put("text", Textarray);
        }
        return obj;
    }

    /**
     * 上传图文消息至素材库
     * 
     * @project GrapeWechatArticle
     * @package interfaceApplication
     * @file Article.java
     * 
     * @param appids
     * @param sdkUserId
     * @param array
     * @return
     *
     */
    private JSONObject uploadMaterial(String sdkUserId, JSONArray array) {
        JSONObject Obj = new JSONObject();
        if (array != null && array.size() != 0) {
            wechatHelper weHelper = getHelper(sdkUserId);
            if (weHelper != null) {
                Obj = weHelper.uploadArticles(array);
            }
        }
        return Obj;
    }

    private wechatHelper getHelper(String sdkUserId) {
        wechatHelper weHelper = null;
        String _appid = getwechatAppid(Integer.parseInt(sdkUserId), "appid");
        String _appsecret = getwechatAppid(Integer.parseInt(sdkUserId), "appsecret");
        if (StringHelper.InvaildString(_appid) || StringHelper.InvaildString(_appsecret)) {
            weHelper = new wechatHelper(_appid, _appsecret);
        }
        return weHelper;
    }

    /**
     * 获取appid，appsecret
     * 
     * @param appids
     * @param id
     * @param key
     * @return
     */
    private String getwechatAppid(int id, String key) {
        DBHelper helper = new DBHelper(appsProxy.configValue().getString("db"), "sdkuser");
        db db = helper.bind(appid);
        JSONObject object = db.eq("id", id).field("configString").find();
        String value = "";
        if (object != null && object.size() > 0) {
            if (object.containsKey("configstring")) {
                object = JSONObject.toJSON(object.getString("configstring"));
                if (object != null && object.size() > 0) {
                    if (object.containsKey(key)) {
                        value = object.getString(key);
                    }
                }
            }
        }
        return value;
    }

    /**
     * 根据文章id获取文章数据
     * 
     * @project GrapeWechatArticle
     * @package interfaceApplication
     * @file Article.java
     * 
     * @param contentID
     *            {appid:contentID,contentID}
     * @return
     *
     */
    private JSONArray getContents(String contentID) {
        String temp = "";
        if (StringHelper.InvaildString(contentID)) {
            temp = (String)appsProxy.proxyCall("/GrapeContent/Content/FindWechatArticle/" + contentID);
        }
        return ArticleEncode(temp);
    }

    /**
     * 获取到文章进行编码
     * 
     * @param ArticleInfo
     * @return
     */
    @SuppressWarnings("unchecked")
    private JSONArray ArticleEncode(String ArticleInfo) {
        JSONObject object;
        String temp;
        JSONArray array = null;
        if (StringHelper.InvaildString(ArticleInfo)) {
            array = JSONArray.toJSONArray(ArticleInfo);
            if (array != null && array.size() > 0) {
                int l = array.size();
                for (int i = 0; i < l; i++) {
                    object = (JSONObject) array.get(i);
                    if (object.containsKey("content")) {
                        temp = object.getString("content");
                        temp = codec.encodebase64(temp);
                        temp = codec.EncodeHtmlTag(temp);
                        object.put("content", temp);
                    }
                    if (object.containsKey("image")) {
                        temp = object.getString("image");
                        temp = codec.EncodeHtmlTag(temp);
                        temp = temp.replaceAll("\\\\", "@t");
                        object.put("image", temp);
                    }
                    if (object.containsKey("thumbnail")) {
                        temp = object.getString("thumbnail");
                        temp = codec.EncodeHtmlTag(temp);
                        object.put("thumbnail", temp);
                    }
                    array.set(i, object);
                }
            }
        }
        return array;
    }
}
