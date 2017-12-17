package com.infosys.loginApp.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.helper.ServiceFactory;

public class SocialMediaType {

  private static Map<String, String> mediaTypes = new HashMap<>();
  private static CassandraOperation cassandraOperation = ServiceFactory.getInstance();
  private static DbInfo mediaTypeDB = Util.dbInfoMap.get(JsonKey.MEDIA_TYPE_DB);

  public static Map<String, String> getMediaTypes() {
    if (null == mediaTypes || mediaTypes.isEmpty()) {
     updateCache();
    }
    return mediaTypes;
  }

  public void addMediaType(String id, String name) {
    Map<String, Object> queryMap = new HashMap<>();
    queryMap.put(JsonKey.ID, id);
    queryMap.put(JsonKey.NAME, name);
    Response response = cassandraOperation.insertRecord(mediaTypeDB.getKeySpace(),
        mediaTypeDB.getTableName(), queryMap);
    if (ResponseCode.success.getResponseCode() == response.getResponseCode().getResponseCode()) {
      mediaTypes.put(id, name);
    }
  }

  public static Response getMediaTypeFromDB() {
    Response response =
        cassandraOperation.getAllRecords(mediaTypeDB.getKeySpace(), mediaTypeDB.getTableName());
    return response;
  }

  @SuppressWarnings("unchecked")
  private static void updateCache() {
    Response response = getMediaTypeFromDB();
    Map<String, String> mediaMap = new HashMap<>();
    List<Map<String, Object>> list = ((List<Map<String, Object>>) response.get(JsonKey.RESPONSE));
    if (!list.isEmpty()) {
      for (Map<String, Object> data : list) {
        mediaMap.put((String) data.get(JsonKey.ID), (String) data.get(JsonKey.NAME));
      }
    }
    mediaTypes = mediaMap;
  }

  private static String validateMediaURL(String type, String url) {
    String pattern = "";
    if(ProjectUtil.isStringNullOREmpty(url)){
      return url;
    }
    switch (type) {
      case "fb":{
        if(url.contains("http")){
          pattern = "http(?:s)?:\\/\\/(?:www.)?facebook.com\\/(?:(?:\\w)*#!\\/)?(?:pages\\/)?(?:[?\\w\\-]*\\/)?(?:profile.php\\?id=(?=\\d.*))?([\\w\\-]*)?";
          if(!IsMatch(url, pattern)){
           url = ""; 
          }
        } else {
          if(url.contains("facebook.com/")){
            url = StringUtils.substringAfter(url, "facebook.com/");
          }
          url = "https://www.facebook.com/" + url;
        }
        return url;
      }
      case "twitter":{
        if(url.contains("http")){
          pattern = "http(?:s)?:\\/\\/(?:www.)?twitter\\.com\\/([a-zA-Z0-9_]+)";
          if(!IsMatch(url, pattern)){
            url = ""; 
           }
        } else {
          if(url.contains("twitter.com/")){
            url = StringUtils.substringAfter(url, "twitter.com/");
          }
          url = "https://twitter.com/" + url;
        }
        return url;
      }
      case "in":{
        if(url.contains("http")){
          pattern = "http(?:s)?:\\/\\/(?:www.)?linkedin+\\.[a-zA-Z0-9/~\\-_,&=\\?\\.;]+[^\\.,\\s<]";
          if(!IsMatch(url, pattern)){
            url = ""; 
           }
        } else {
          if(url.contains("linkedin.com/in/")){
            url = StringUtils.substringAfter(url, "linkedin.com/in/");
          }
          url = "https://www.linkedin.com/in/" + url;
        }
        return url;
      }
      case "blog": {
        pattern = "http(?:s)?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        if(!IsMatch(url, pattern)){
          url = "";
        }
        return url;
      }
      default: {
        pattern = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        if(!IsMatch(url, pattern)){
          url = ""; 
         }
        return url;
      }
    }
  }

  private static boolean IsMatch(String s, String pattern) {
    try {
      Pattern patt = Pattern.compile(pattern);
      Matcher matcher = patt.matcher(s);
      return matcher.matches();
    } catch (RuntimeException e) {
      return false;
    }
  }
  
  public static void validateSocialMedia(List<Map<String, String>> socialMediaList) {
    for(Map<String,String> socialMedia : socialMediaList){
      if(null == socialMedia || socialMedia.isEmpty()){
        throw new ProjectCommonException(ResponseCode.invalidWebPageData.getErrorCode(),
            ResponseCode.invalidWebPageData.getErrorMessage(),
            ResponseCode.CLIENT_ERROR.getResponseCode());
      }
      String mediaType = socialMedia.get(JsonKey.TYPE);
      //Checking in variable for media types
      if(!SocialMediaType.getMediaTypes().containsKey(mediaType)){
        //If not found pulling latest media types from cassandra to variable and checking again
        SocialMediaType.updateCache();
        if(!SocialMediaType.getMediaTypes().containsKey(mediaType)){
          throw new ProjectCommonException(ResponseCode.invalidMediaType.getErrorCode(),
              ResponseCode.invalidMediaType.getErrorMessage(),
              ResponseCode.CLIENT_ERROR.getResponseCode());
        } 
      }
      String mediaUrl = SocialMediaType.validateMediaURL(mediaType, socialMedia.get(JsonKey.URL));
      if(ProjectUtil.isStringNullOREmpty(mediaUrl)){
        throw new ProjectCommonException(ResponseCode.invalidWebPageUrl.getErrorCode(),
            ProjectUtil.formatMessage(ResponseCode.invalidWebPageUrl.getErrorMessage(), getMediaTypes().get(mediaType)),
            ResponseCode.CLIENT_ERROR.getResponseCode());
      }  else {
        socialMedia.put(JsonKey.URL, mediaUrl);
      }
    } 
  }
}

