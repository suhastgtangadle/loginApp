package com.infosys.loginApp.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.PropertiesCache;
import org.sunbird.common.models.util.datasecurity.DecryptionService;
import org.sunbird.common.models.util.datasecurity.EncryptionService;
import org.sunbird.common.models.util.datasecurity.impl.ServiceFactory;

/**
 * This class is for utility methods for encrypting user data.
 * 
 * @author Amit Kumar
 *
 */
public class UserUtility {

  private UserUtility() {}

  private static List<String> userKeyToEncrypt = new ArrayList<>();
  private static List<String> addressKeyToEncrypt = new ArrayList<>();
  private static List<String> userKeyToDecrypt = new ArrayList<>();
  static {
    String userKey = PropertiesCache.getInstance().getProperty("userkey.encryption");
    userKeyToEncrypt = new ArrayList<>(Arrays.asList(userKey.split(",")));
    String addressKey = PropertiesCache.getInstance().getProperty("addresskey.encryption");
    addressKeyToEncrypt = new ArrayList<>(Arrays.asList(addressKey.split(",")));
    String userKeyDecrypt = PropertiesCache.getInstance().getProperty("userkey.decryption");
    userKeyToDecrypt = new ArrayList<>(Arrays.asList(userKeyDecrypt.split(",")));
  }


  public static Map<String, Object> encryptUserData(Map<String, Object> userMap) throws Exception {
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance(null);
    // Encrypt user basic info
    for (String key : userKeyToEncrypt) {
      if (userMap.containsKey(key)) {
        userMap.put(key, service.encryptData((String) userMap.get(key)));
      }
    }
    // Encrypt user address Info
    if (userMap.containsKey(JsonKey.ADDRESS)) {
      List<Map<String, Object>> addressList =
          (List<Map<String, Object>>) userMap.get(JsonKey.ADDRESS);
      for (Map<String, Object> map : addressList) {
        for (String key : addressKeyToEncrypt) {
          if (map.containsKey(key)) {
            map.put(key, service.encryptData((String) map.get(key)));
          }
        }
      }
    }
    return userMap;
  }

  public static List<Map<String, Object>> encryptUserAddressData(
      List<Map<String, Object>> addressList) throws Exception {
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance(null);
    // Encrypt user address Info
    for (Map<String, Object> map : addressList) {
      for (String key : addressKeyToEncrypt) {
        if (map.containsKey(key)) {
          map.put(key, service.encryptData((String) map.get(key)));
        }
      }
    }
    return addressList;
  }

  public static Map<String, Object> decryptUserData(Map<String, Object> userMap) {
    DecryptionService service = ServiceFactory.getDecryptionServiceInstance(null);
    // Decrypt user basic info
    for (String key : userKeyToEncrypt) {
      if (userMap.containsKey(key)) {
        userMap.put(key, service.decryptData((String) userMap.get(key)));
      }
    }

    // Decrypt user address Info
    if (userMap.containsKey(JsonKey.ADDRESS)) {
      List<Map<String, Object>> addressList =
          (List<Map<String, Object>>) userMap.get(JsonKey.ADDRESS);
      for (Map<String, Object> map : addressList) {
        for (String key : addressKeyToEncrypt) {
          if (map.containsKey(key)) {
            map.put(key, service.decryptData((String) map.get(key)));
          }
        }
      }
    }
    return userMap;
  }

  public static Map<String, Object> decryptUserDataFrmES(Map<String, Object> userMap) {
    DecryptionService service = ServiceFactory.getDecryptionServiceInstance(null);
    // Decrypt user basic info
    for (String key : userKeyToDecrypt) {
      if (userMap.containsKey(key)) {
        userMap.put(key, service.decryptData((String) userMap.get(key)));
      }
    }

    // Decrypt user address Info
    if (userMap.containsKey(JsonKey.ADDRESS)) {
      List<Map<String, Object>> addressList =
          (List<Map<String, Object>>) userMap.get(JsonKey.ADDRESS);
      for (Map<String, Object> map : addressList) {
        for (String key : addressKeyToEncrypt) {
          if (map.containsKey(key)) {
            map.put(key, service.decryptData((String) map.get(key)));
          }
        }
      }
    }
    return userMap;
  }

  public static List<Map<String, Object>> decryptUserAddressData(
      List<Map<String, Object>> addressList) {
    DecryptionService service = ServiceFactory.getDecryptionServiceInstance(null);
    // Decrypt user address info
    for (Map<String, Object> map : addressList) {
      for (String key : addressKeyToEncrypt) {
        if (map.containsKey(key)) {
          map.put(key, service.decryptData((String) map.get(key)));
        }
      }
    }
    return addressList;
  }

  public static Map<String, Object> encryptUserSearchFilterQueryData(Map<String, Object> map)
      throws Exception {
    Map<String, Object> filterMap = (Map<String, Object>) map.get(JsonKey.FILTERS);
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance(null);
    // Encrypt user basic info
    for (String key : userKeyToEncrypt) {
      if (filterMap.containsKey(key)) {
        if(key.equalsIgnoreCase(JsonKey.EMAIL)){
          filterMap.put(JsonKey.ENC_EMAIL, service.encryptData((String) filterMap.get(key)));
          filterMap.remove(JsonKey.EMAIL);
        } else if (key.equalsIgnoreCase(JsonKey.PHONE)){
          filterMap.put(JsonKey.PHONE, service.encryptData((String) filterMap.get(key)));
          filterMap.remove(JsonKey.PHONE);
        } else {
          filterMap.put(key, service.encryptData((String) filterMap.get(key)));
        }
      }
    }
    // Encrypt user address Info
    for (String key : addressKeyToEncrypt) {
      if ((filterMap).containsKey((JsonKey.ADDRESS + "." + key))) {
        filterMap.put((JsonKey.ADDRESS + "." + key),
            service.encryptData((String) filterMap.get(JsonKey.ADDRESS + "." + key)));
      }
    }

    return filterMap;
  }
  
  public static String encryptData(String data) throws Exception {
    EncryptionService service = ServiceFactory.getEncryptionServiceInstance(null);
    return service.encryptData(data);
  }
  
  public static Map<String, Object> updateProfileVisibilityFields(
      Map<String, Object> profileVisibility, Map<String, Object> mapToBeUpdated) {
    for (String field : profileVisibility.keySet()) {
      if ("dob".equalsIgnoreCase((String) field)) {
        mapToBeUpdated.put(field, null);
      } else if (profileVisibility.get(field) instanceof List) {
        mapToBeUpdated.put(field, new ArrayList<>());
      } else if (profileVisibility.get(field) instanceof Map) {
        mapToBeUpdated.put(field, new HashMap<>());
      } else if (profileVisibility.get(field) instanceof String) {
        mapToBeUpdated.put(field, "");
      } else {
        mapToBeUpdated.put(field, null);
      }

    }
    return mapToBeUpdated;
  }

}

