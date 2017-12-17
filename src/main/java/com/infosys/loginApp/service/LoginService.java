package com.infosys.loginApp.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.common.ElasticSearchUtil;
import org.sunbird.common.exception.ProjectCommonException;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.models.util.ProjectUtil.EsIndex;
import org.sunbird.common.models.util.ProjectUtil.EsType;
import org.sunbird.common.models.util.datasecurity.DataMaskingService;
import org.sunbird.common.models.util.datasecurity.DecryptionService;
import org.sunbird.common.models.util.datasecurity.EncryptionService;
import org.sunbird.common.models.util.datasecurity.OneWayHashing;
import org.sunbird.common.request.Request;
import org.sunbird.common.responsecode.ResponseCode;
import org.sunbird.common.services.ProfileCompletenessService;
import org.sunbird.common.services.impl.ProfileCompletenessFactory;
import org.sunbird.dto.SearchDTO;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.loginApp.helper.DbInfo;
import com.infosys.loginApp.helper.Util;

@Service
public class LoginService {

	private EncryptionService encryptionService = org.sunbird.common.models.util.datasecurity.impl.ServiceFactory
			.getEncryptionServiceInstance(null);
	private CassandraOperation cassandraOperation = new CassandraOperationImpl();

	public boolean createUser(Request requestBody) {
		DbInfo usrDbInfo = Util.dbInfoMap.get(JsonKey.USER_DB);
		DbInfo addrDbInfo = Util.dbInfoMap.get(JsonKey.ADDRESS_DB);
		DbInfo eduDbInfo = Util.dbInfoMap.get(JsonKey.EDUCATION_DB);
		DbInfo jobProDbInfo = Util.dbInfoMap.get(JsonKey.JOB_PROFILE_DB);
		DbInfo usrOrgDb = Util.dbInfoMap.get(JsonKey.USR_ORG_DB);
		DbInfo orgDb = Util.dbInfoMap.get(JsonKey.ORG_DB);
		DbInfo usrExtIdDb = Util.dbInfoMap.get(JsonKey.USR_EXT_ID_DB);

		// System.out.println(usrDbInfo.getKeySpace()+" "+usrDbInfo.getTableName());

		Map<String, Object> requestMap = null;
		Map<String, Object> userMap = requestBody.getRequest();

		// checkPhoneUniqueness(userMap,JsonKey.CREATE);

		Map<String, Object> emailTemplateMap = new HashMap<>(userMap);

		/*
		 * if (userMap.containsKey(JsonKey.WEB_PAGES)) { SocialMediaType
		 * .validateSocialMedia((List<Map<String, String>>)
		 * userMap.get(JsonKey.WEB_PAGES)); }
		 */

		// ambiguos dont exactly know what it is
		// userMap.put(JsonKey.CREATED_BY, req.get(JsonKey.REQUESTED_BY));

		/*
		 * dont require to remove as these are not at all passed
		 * userMap.remove(JsonKey.ENC_EMAIL); userMap.remove(JsonKey.ENC_PHONE)
		 */

		if (userMap.containsKey(JsonKey.PROVIDER)
				&& !ProjectUtil.isStringNullOREmpty((String) userMap.get(JsonKey.PROVIDER))) {
			userMap.put(JsonKey.LOGIN_ID,
					(String) userMap.get(JsonKey.USERNAME) + "@" + (String) userMap.get(JsonKey.PROVIDER));
		} else {
			userMap.put(JsonKey.LOGIN_ID, (String) userMap.get(JsonKey.USERNAME));
		}

		// System.out.println(userMap.get(JsonKey.LOGIN_ID));

		emailTemplateMap.put(JsonKey.USERNAME, userMap.get(JsonKey.LOGIN_ID));

		if (null != userMap.get(JsonKey.LOGIN_ID)) {
			String loginId = "";
			try {
				loginId = encryptionService.encryptData((String) userMap.get(JsonKey.LOGIN_ID));
				// System.out.println(loginId);
			} catch (Exception e) {
				ProjectCommonException exception = new ProjectCommonException(
						ResponseCode.userDataEncryptionError.getErrorCode(),
						ResponseCode.userDataEncryptionError.getErrorMessage(),
						ResponseCode.SERVER_ERROR.getResponseCode());
			}

			// ****************************uncomment this
			// tomorrow*************************************

			/*
			 * Response resultFrUserName =
			 * cassandraOperation.getRecordsByProperty(usrDbInfo.getKeySpace(),
			 * usrDbInfo.getTableName(), JsonKey.LOGIN_ID, loginId); if
			 * (!(((List<Map<String, Object>>)
			 * resultFrUserName.get(JsonKey.RESPONSE)).isEmpty())) { ProjectCommonException
			 * exception = new ProjectCommonException(
			 * ResponseCode.userAlreadyExist.getErrorCode(),
			 * ResponseCode.userAlreadyExist.getErrorMessage(),
			 * ResponseCode.CLIENT_ERROR.getResponseCode()); }
			 */
		}

		// *****************************uncomment this
		// tomorrow****************************************

		userMap.put(JsonKey.ROOT_ORG_ID, JsonKey.DEFAULT_ROOT_ORG_ID);
		/*
		 * if (!ProjectUtil.isStringNullOREmpty((String)
		 * userMap.get(JsonKey.REGISTERED_ORG_ID))) { Response orgResponse = null; try {
		 * orgResponse = cassandraOperation.getRecordById(orgDb.getKeySpace(),
		 * orgDb.getTableName(), (String) userMap.get(JsonKey.REGISTERED_ORG_ID)); }
		 * catch (Exception e) { ProjectLogger.
		 * log("Exception occured while verifying regOrgId during create user : ", e);
		 * throw new ProjectCommonException(ResponseCode.invalidOrgId.getErrorCode(),
		 * ResponseCode.invalidOrgId.getErrorMessage(),
		 * ResponseCode.CLIENT_ERROR.getResponseCode()); } List<Map<String, Object>>
		 * responseList = (List<Map<String, Object>>) orgResponse.get(JsonKey.RESPONSE);
		 * String rootOrgId = ""; if (null != responseList && !(responseList.isEmpty()))
		 * { String orgId = (String) responseList.get(0).get(JsonKey.ID); Map<String,
		 * Object> orgMap = responseList.get(0); boolean isRootOrg = false; if (null !=
		 * orgMap.get(JsonKey.IS_ROOT_ORG)) { isRootOrg = (boolean)
		 * orgMap.get(JsonKey.IS_ROOT_ORG); } else { isRootOrg = false; } if (isRootOrg)
		 * { rootOrgId = orgId; } else { String channel = (String)
		 * orgMap.get(JsonKey.CHANNEL); if (!ProjectUtil.isStringNullOREmpty(channel)) {
		 * Map<String, Object> filters = new HashMap<>(); filters.put(JsonKey.CHANNEL,
		 * channel); filters.put(JsonKey.IS_ROOT_ORG, true); Map<String, Object>
		 * esResult = elasticSearchComplexSearch(filters,
		 * EsIndex.sunbird.getIndexName(), EsType.organisation.getTypeName()); if
		 * (Util.isNotNull(esResult) && esResult.containsKey(JsonKey.CONTENT) &&
		 * Util.isNotNull(esResult.get(JsonKey.CONTENT)) && !(((List<String>)
		 * esResult.get(JsonKey.CONTENT)).isEmpty())) { Map<String, Object> esContent =
		 * ((List<Map<String, Object>>) esResult.get(JsonKey.CONTENT)).get(0); rootOrgId
		 * = (String) esContent.get(JsonKey.ID); } else { throw new
		 * ProjectCommonException( ResponseCode.invalidRootOrgData.getErrorCode(),
		 * ProjectUtil .formatMessage(ResponseCode.invalidRootOrgData.getErrorMessage(),
		 * channel), ResponseCode.CLIENT_ERROR.getResponseCode()); } } else { rootOrgId
		 * = JsonKey.DEFAULT_ROOT_ORG_ID; } } userMap.put(JsonKey.ROOT_ORG_ID,
		 * rootOrgId); } else { throw new
		 * ProjectCommonException(ResponseCode.invalidOrgId.getErrorCode(),
		 * ResponseCode.invalidOrgId.getErrorMessage(),
		 * ResponseCode.CLIENT_ERROR.getResponseCode()); } } else { String provider =
		 * (String) userMap.get(JsonKey.PROVIDER); String rootOrgId =
		 * Util.getRootOrgIdFromChannel(provider); if
		 * (!ProjectUtil.isStringNullOREmpty(rootOrgId)) {
		 * userMap.put(JsonKey.ROOT_ORG_ID, rootOrgId); } }
		 */

		if (userMap.containsKey(JsonKey.ROLES)) {
			List<String> roles = (List<String>) userMap.get(JsonKey.ROLES);
			/*
			 * String msg = Util.validateRoles(roles); if
			 * (!msg.equalsIgnoreCase(JsonKey.SUCCESS)) { throw new
			 * ProjectCommonException(ResponseCode.invalidRole.getErrorCode(),
			 * ResponseCode.invalidRole.getErrorMessage(),
			 * ResponseCode.CLIENT_ERROR.getResponseCode()); }
			 */
			if (!roles.contains(ProjectUtil.UserRole.PUBLIC.getValue())) {
				roles.add(ProjectUtil.UserRole.PUBLIC.getValue());
				userMap.put(JsonKey.ROLES, roles);
			}
		} else {
			List<String> roles = new ArrayList<>();
			roles.add(ProjectUtil.UserRole.PUBLIC.getValue());
			userMap.put(JsonKey.ROLES, roles);
		}

		userMap.put(JsonKey.USER_ID, OneWayHashing.encryptVal((String) userMap.get(JsonKey.USERNAME)));
		userMap.put(JsonKey.ID, OneWayHashing.encryptVal((String) userMap.get(JsonKey.USERNAME)));

		userMap.put(JsonKey.CREATED_DATE, ProjectUtil.getFormattedDate());
		userMap.put(JsonKey.STATUS, ProjectUtil.Status.ACTIVE.getValue());

		if (!ProjectUtil.isStringNullOREmpty((String) userMap.get(JsonKey.PASSWORD))) {
			emailTemplateMap.put(JsonKey.TEMPORARY_PASSWORD, (String) userMap.get(JsonKey.PASSWORD));
			userMap.put(JsonKey.PASSWORD, OneWayHashing.encryptVal((String) userMap.get(JsonKey.PASSWORD)));
		} else {
			// create tempPassword
			String tempPassword = ProjectUtil.generateRandomPassword();
			userMap.put(JsonKey.PASSWORD, OneWayHashing.encryptVal(tempPassword));
			emailTemplateMap.put(JsonKey.TEMPORARY_PASSWORD, tempPassword);
		}

		requestMap = new HashMap<>();
		requestMap.putAll(userMap);
		// removeUnwanted(requestMap);
		Map<String, String> profileVisbility = new HashMap<>();
		for (String field : ProjectUtil.defaultPrivateFields) {
			profileVisbility.put(field, JsonKey.PRIVATE);
		}
		requestMap.put(JsonKey.PROFILE_VISIBILITY, profileVisbility);
		Response response = null;

		// **************uncomment this tomorrow********************
		/*
		 * try { response = cassandraOperation.insertRecord(usrDbInfo.getKeySpace(),
		 * usrDbInfo.getTableName(), requestMap); } catch (ProjectCommonException
		 * exception) { //sender().tell(exception, self()); //return;
		 * exception.printStackTrace(); }
		 

		response.put(JsonKey.USER_ID, userMap.get(JsonKey.ID));
		if (((String) response.get(JsonKey.RESPONSE)).equalsIgnoreCase(JsonKey.SUCCESS)) {
			if (userMap.containsKey(JsonKey.ADDRESS)) {
				List<Map<String, Object>> reqList = (List<Map<String, Object>>) userMap.get(JsonKey.ADDRESS);
				for (int i = 0; i < reqList.size(); i++) {
					Map<String, Object> reqMap = reqList.get(i);
					reqMap.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(i + 1));
					reqMap.put(JsonKey.CREATED_DATE, ProjectUtil.getFormattedDate());
					String encUserId = "";
					String encCreatedById = "";
					try {
						encUserId = encryptionService.encryptData((String) userMap.get(JsonKey.ID));
						encCreatedById = encryptionService.encryptData((String) userMap.get(JsonKey.CREATED_BY));
					} catch (Exception e) {
						ProjectCommonException exception = new ProjectCommonException(
								ResponseCode.userDataEncryptionError.getErrorCode(),
								ResponseCode.userDataEncryptionError.getErrorMessage(),
								ResponseCode.SERVER_ERROR.getResponseCode());
						// sender().tell(exception, self());
						// return;
					}
					// reqMap.put(JsonKey.CREATED_BY, encCreatedById);
					reqMap.put(JsonKey.USER_ID, encUserId);
					try {
						cassandraOperation.insertRecord(addrDbInfo.getKeySpace(), addrDbInfo.getTableName(), reqMap);
					} catch (Exception e) {
						ProjectLogger.log(e.getMessage(), e);
					}
				}
			}
			ProjectLogger.log("User insertation on DB started--.....");
			if (userMap.containsKey(JsonKey.EDUCATION)) {
				insertEducationDetails(userMap, addrDbInfo, eduDbInfo);
				ProjectLogger.log("User insertation for Education done--.....");
			}
			if (userMap.containsKey(JsonKey.JOB_PROFILE)) {
				insertJobProfileDetails(userMap, addrDbInfo, jobProDbInfo);
				ProjectLogger.log("User insertation for Job profile done--.....");
			}
			if (!ProjectUtil.isStringNullOREmpty((String) userMap.get(JsonKey.REGISTERED_ORG_ID))) {
				Response orgResponse = null;
				try {
					orgResponse = cassandraOperation.getRecordById(orgDb.getKeySpace(), orgDb.getTableName(),
							(String) userMap.get(JsonKey.REGISTERED_ORG_ID));
				} catch (Exception e) {
					ProjectLogger.log("Exception occured while verifying regOrgId during create user : ", e);
				}
				if (null != orgResponse
						&& (!((List<Map<String, Object>>) orgResponse.get(JsonKey.RESPONSE)).isEmpty())) {
					insertOrganisationDetails(userMap, usrOrgDb);
				} else {
					ProjectLogger.log("Reg Org Id :" + (String) userMap.get(JsonKey.REGISTERED_ORG_ID) + " for user id "
							+ userMap.get(JsonKey.ID) + " is not valid.");
				}
			}
			// update the user external identity data
			ProjectLogger.log("User insertation for extrenal identity started--.....");
			updateUserExtId(requestMap, usrExtIdDb);
			ProjectLogger.log("User insertation for extrenal identity completed--.....");
		}*/
		
		ProjectLogger.log("User created successfully.....");
	    //response.put(JsonKey.ACCESSTOKEN, accessToken);

		return false;
	}
	
	private void getUserProfile(String userId) {
	    ProjectLogger.log("get user profile method call started user Id : " + userId);
	    DbInfo userDbInfo = Util.dbInfoMap.get(JsonKey.USER_DB);
	    DbInfo addrDbInfo = Util.dbInfoMap.get(JsonKey.ADDRESS_DB);
	    DbInfo eduDbInfo = Util.dbInfoMap.get(JsonKey.EDUCATION_DB);
	    DbInfo jobProDbInfo = Util.dbInfoMap.get(JsonKey.JOB_PROFILE_DB);
	    DbInfo userSkillDbInfo = Util.dbInfoMap.get(JsonKey.USER_SKILL_DB);
	    EncryptionService service = org.sunbird.common.models.util.datasecurity.impl.ServiceFactory
	        .getEncryptionServiceInstance(null);
	    DecryptionService decService = org.sunbird.common.models.util.datasecurity.impl.ServiceFactory
	        .getDecryptionServiceInstance(null);
	    Response response = null;
	    List<Map<String, Object>> list = null;
	    try {
	      response = cassandraOperation.getRecordById(userDbInfo.getKeySpace(),
	          userDbInfo.getTableName(), userId);
	      list = (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
	      ProjectLogger.log("collecting user data to save user id : " + userId, LoggerEnum.INFO.name());
	    } catch (Exception e) {
	      ProjectLogger.log(e.getMessage(), e);
	    }

	    if (!(list.isEmpty())) {
	      Map<String, Object> map = list.get(0);
	      Response addrResponse;
	      list = null;
	      try {
	        ProjectLogger.log("collecting user address operation user Id : " + userId);
	        String encUserId = service.encryptData(userId);
	        addrResponse = cassandraOperation.getRecordsByProperty(addrDbInfo.getKeySpace(),
	            addrDbInfo.getTableName(), JsonKey.USER_ID, encUserId);
	        list = (List<Map<String, Object>>) addrResponse.getResult().get(JsonKey.RESPONSE);
	        ProjectLogger.log("collecting user address operation completed user Id : " + userId);
	      } catch (Exception e) {
	        ProjectLogger.log(e.getMessage(), e);
	      } finally {
	        if (null == list) {
	          list = new ArrayList<>();
	        }
	      }
	      map.put(JsonKey.ADDRESS, list);
	      list = null;
	      Response eduResponse = null;
	      try {
	        eduResponse = cassandraOperation.getRecordsByProperty(eduDbInfo.getKeySpace(),
	            eduDbInfo.getTableName(), JsonKey.USER_ID, userId);
	        list = (List<Map<String, Object>>) eduResponse.getResult().get(JsonKey.RESPONSE);
	      } catch (Exception e) {
	        ProjectLogger.log(e.getMessage(), e);
	      } finally {
	        if (null == list) {
	          list = new ArrayList<>();
	        }
	      }
	      for (Map<String, Object> eduMap : list) {
	        String addressId = (String) eduMap.get(JsonKey.ADDRESS_ID);
	        if (!ProjectUtil.isStringNullOREmpty(addressId)) {

	          Response addrResponseMap;
	          List<Map<String, Object>> addrList = null;
	          try {
	            addrResponseMap = cassandraOperation.getRecordById(addrDbInfo.getKeySpace(),
	                addrDbInfo.getTableName(), addressId);
	            addrList =
	                (List<Map<String, Object>>) addrResponseMap.getResult().get(JsonKey.RESPONSE);
	          } catch (Exception e) {
	            ProjectLogger.log(e.getMessage(), e);
	          } finally {
	            if (null == addrList) {
	              addrList = new ArrayList<>();
	            }
	          }
	          eduMap.put(JsonKey.ADDRESS, addrList.get(0));
	        }
	      }
	      map.put(JsonKey.EDUCATION, list);

	      Response jobProfileResponse;
	      list = null;
	      try {
	        ProjectLogger.log("collecting user jobprofile user Id : " + userId);
	        jobProfileResponse = cassandraOperation.getRecordsByProperty(jobProDbInfo.getKeySpace(),
	            jobProDbInfo.getTableName(), JsonKey.USER_ID, userId);
	        list = (List<Map<String, Object>>) jobProfileResponse.getResult().get(JsonKey.RESPONSE);
	        ProjectLogger.log("collecting user jobprofile collection completed userId : " + userId);
	      } catch (Exception e) {
	        ProjectLogger.log(e.getMessage(), e);
	      } finally {
	        if (null == list) {
	          list = new ArrayList<>();
	        }
	      }
	      for (Map<String, Object> eduMap : list) {
	        String addressId = (String) eduMap.get(JsonKey.ADDRESS_ID);
	        if (!ProjectUtil.isStringNullOREmpty(addressId)) {
	          Response addrResponseMap;
	          List<Map<String, Object>> addrList = null;
	          try {
	            addrResponseMap = cassandraOperation.getRecordById(addrDbInfo.getKeySpace(),
	                addrDbInfo.getTableName(), addressId);
	            addrList =
	                (List<Map<String, Object>>) addrResponseMap.getResult().get(JsonKey.RESPONSE);
	          } catch (Exception e) {
	            ProjectLogger.log(e.getMessage(), e);
	          } finally {
	            if (null == addrList) {
	              addrList = new ArrayList<>();
	            }
	          }
	          eduMap.put(JsonKey.ADDRESS, addrList.get(0));
	        }
	      }
	      map.put(JsonKey.JOB_PROFILE, list);
	      list = null;
	      List<Map<String, Object>> organisations = new ArrayList<>();
	      try {
	        Map<String, Object> reqMap = new HashMap<>();
	        reqMap.put(JsonKey.USER_ID, userId);
	        reqMap.put(JsonKey.IS_DELETED, false);
	        DbInfo orgUsrDbInfo = Util.dbInfoMap.get(JsonKey.USER_ORG_DB);
	        Response result = cassandraOperation.getRecordsByProperties(orgUsrDbInfo.getKeySpace(),
	            orgUsrDbInfo.getTableName(), reqMap);
	        list = (List<Map<String, Object>>) result.get(JsonKey.RESPONSE);
	        if (!(list.isEmpty())) {
	          for (Map<String, Object> tempMap : list) {
	            organisations.add(tempMap);
	          }
	        }
	      } catch (Exception e) {
	        ProjectLogger.log(e.getMessage(), e);
	      }
	      map.put(JsonKey.ORGANISATIONS, organisations);
	      Util.removeAttributes(map, Arrays.asList(JsonKey.PASSWORD));
	    } else {
	      ProjectLogger.log("User data not found to save to ES user Id : " + userId,
	          LoggerEnum.INFO.name());
	    }
	    if (!(((List<Map<String, String>>) response.getResult().get(JsonKey.RESPONSE)).isEmpty())) {
	      ProjectLogger.log("saving started user to es userId : " + userId, LoggerEnum.INFO.name());
	      Map<String, Object> map =
	          ((List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE)).get(0);

	      // save masked email and phone number
	      DataMaskingService maskingService =
	          org.sunbird.common.models.util.datasecurity.impl.ServiceFactory
	              .getMaskingServiceInstance(null);
	      String phone = (String) map.get(JsonKey.PHONE);
	      String email = (String) map.get(JsonKey.EMAIL);


	      if (!ProjectUtil.isStringNullOREmpty(phone)) {
	        map.put(JsonKey.ENC_PHONE, phone);
	        map.put(JsonKey.PHONE, maskingService.maskPhone(decService.decryptData(phone)));
	      }
	      if (!ProjectUtil.isStringNullOREmpty(email)) {
	        map.put(JsonKey.ENC_EMAIL, email);
	        map.put(JsonKey.EMAIL, maskingService.maskEmail(decService.decryptData(email)));
	      }

	      // add the skills column into ES
	      Response skillresponse = cassandraOperation.getRecordsByProperty(userSkillDbInfo.getKeySpace() , userSkillDbInfo.getTableName(), JsonKey.USER_ID , userId);
	      List<Map<String,Object>> responseList = (List<Map<String, Object>>) skillresponse.get(JsonKey.RESPONSE);
	      map.put(JsonKey.SKILLS , responseList);
	      ProfileCompletenessService profileService = ProfileCompletenessFactory.getInstance();
	      Map<String, Object> responsemap = profileService.computeProfile(map);
	      map.putAll(responsemap);
	      //TODO:Refactor the code for better understanding and based on modules
	      //Update private fields data to userProfileVisbility index and others to user index
	      Map<String, Object> profileVisibility =
	          (Map<String, Object>) map.get(JsonKey.PROFILE_VISIBILITY);
	      if (null != profileVisibility && !profileVisibility.isEmpty()) {
	        Map<String, Object> profileVisibilityMap = new HashMap<>();
	        for (String field : profileVisibility.keySet()) {
	          profileVisibilityMap.put(field, map.get(field));
	        }
	        insertDataToElastic(ProjectUtil.EsIndex.sunbird.getIndexName(),
	            ProjectUtil.EsType.userprofilevisibility.getTypeName(), userId, profileVisibilityMap);
	        com.infosys.loginApp.helper.UserUtility.updateProfileVisibilityFields(profileVisibilityMap, map);
	      }
	      insertDataToElastic(ProjectUtil.EsIndex.sunbird.getIndexName(),
	          ProjectUtil.EsType.user.getTypeName(), userId, map);
	      ProjectLogger.log("saving completed user to es userId : " + userId);
	    } else {
	      ProjectLogger.log("user data not found to save to ES userId : " + userId);
	    }

	  }
	
	private boolean insertDataToElastic(String index, String type, String identifier,
		      Map<String, Object> data) {
		    ProjectLogger
		        .log("making call to ES for type ,identifier ,data==" + type + " " + identifier + data);
		   /* if (type.equalsIgnoreCase(ProjectUtil.EsType.user.getTypeName())) {
		      // now calculate profile completeness and error filed and store it in ES
		      ProfileCompletenessService service = ProfileCompletenessFactory.getInstance();
		      Map<String, Object> responsemap = service.computeProfile(data);
		      data.putAll(responsemap);
		    }*/
		    String response = ElasticSearchUtil.createData(index, type, identifier, data);
		    ProjectLogger.log("Getting ES save response for type , identiofier==" + type + "  " + identifier
		        + "  " + response);
		    if (!ProjectUtil.isStringNullOREmpty(response)) {
		      ProjectLogger.log("User Data is saved successfully ES ." + type + "  " + identifier);
		      return true;
		    }
		    ProjectLogger.log("unbale to save the data inside ES with identifier " + identifier,
		        LoggerEnum.INFO.name());
		    return false;
		  }

	private void checkPhoneUniqueness(Map<String, Object> userMap, String opType) {
		String phone = (String) userMap.get(JsonKey.PHONE);
		if (!ProjectUtil.isStringNullOREmpty(phone)) {
			try {
				phone = encryptionService.encryptData(phone);
			} catch (Exception e) {
				ProjectLogger.log("Exception occured while encrypting phone number ", e);
			}
			Map<String, Object> filters = new HashMap<>();
			filters.put(JsonKey.ENC_PHONE, phone);
			Map<String, Object> map = new HashMap<>();
			map.put(JsonKey.FILTERS, filters);
			SearchDTO searchDto = Util.createSearchDto(map);
			Map<String, Object> result = ElasticSearchUtil.complexSearch(searchDto,
					ProjectUtil.EsIndex.sunbird.getIndexName(), ProjectUtil.EsType.user.getTypeName());
			List<Map<String, Object>> userMapList = (List<Map<String, Object>>) result.get(JsonKey.CONTENT);
			if (!userMapList.isEmpty()) {
				if (opType.equalsIgnoreCase(JsonKey.CREATE)) {
					throw new ProjectCommonException(ResponseCode.PhoneNumberInUse.getErrorCode(),
							ResponseCode.PhoneNumberInUse.getErrorMessage(),
							ResponseCode.CLIENT_ERROR.getResponseCode());
				} else {
					Map<String, Object> user = userMapList.get(0);
					if (!(((String) user.get(JsonKey.ID)).equalsIgnoreCase((String) userMap.get(JsonKey.ID)))) {
						throw new ProjectCommonException(ResponseCode.PhoneNumberInUse.getErrorCode(),
								ResponseCode.PhoneNumberInUse.getErrorMessage(),
								ResponseCode.CLIENT_ERROR.getResponseCode());
					}
				}
			}
		}
	}

	private static Map<String, Object> elasticSearchComplexSearch(Map<String, Object> filters, String index,
			String type) {

		SearchDTO searchDTO = new SearchDTO();
		searchDTO.getAdditionalProperties().put(JsonKey.FILTERS, filters);

		return ElasticSearchUtil.complexSearch(searchDTO, index, type);

	}

	/*
	 * public static void main(String args[]) { String string = "{}"; Request
	 * request; try { request = new ObjectMapper().readValue(string, Request.class);
	 * } catch (JsonParseException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (JsonMappingException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch (IOException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * // createUser(request); }
	 */

	private void insertEducationDetails(Map<String, Object> userMap, DbInfo addrDbInfo, DbInfo eduDbInfo) {

		List<Map<String, Object>> reqList = (List<Map<String, Object>>) userMap.get(JsonKey.EDUCATION);
		for (int i = 0; i < reqList.size(); i++) {
			Map<String, Object> reqMap = reqList.get(i);
			reqMap.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(i + 1));
			String addrId = null;
			Response addrResponse = null;
			if (reqMap.containsKey(JsonKey.ADDRESS)) {
				Map<String, Object> address = (Map<String, Object>) reqMap.get(JsonKey.ADDRESS);
				addrId = ProjectUtil.getUniqueIdFromTimestamp(i + 1);
				address.put(JsonKey.ID, addrId);
				address.put(JsonKey.CREATED_DATE, ProjectUtil.getFormattedDate());
				address.put(JsonKey.CREATED_BY, userMap.get(JsonKey.ID));
				try {
					addrResponse = cassandraOperation.insertRecord(addrDbInfo.getKeySpace(), addrDbInfo.getTableName(),
							address);
				} catch (Exception e) {
					ProjectLogger.log(e.getMessage(), e);
				}
			}
			if (null != addrResponse
					&& ((String) addrResponse.get(JsonKey.RESPONSE)).equalsIgnoreCase(JsonKey.SUCCESS)) {
				reqMap.put(JsonKey.ADDRESS_ID, addrId);
				reqMap.remove(JsonKey.ADDRESS);
			}
			try {
				reqMap.put(JsonKey.YEAR_OF_PASSING, ((BigInteger) reqMap.get(JsonKey.YEAR_OF_PASSING)).intValue());
			} catch (Exception ex) {
				ProjectLogger.log(ex.getMessage(), ex);
			}
			if (null != reqMap.get(JsonKey.PERCENTAGE)) {
				reqMap.put(JsonKey.PERCENTAGE, Double.parseDouble(String.valueOf(reqMap.get(JsonKey.PERCENTAGE))));
			}
			reqMap.put(JsonKey.CREATED_DATE, ProjectUtil.getFormattedDate());
			reqMap.put(JsonKey.CREATED_BY, userMap.get(JsonKey.ID));
			reqMap.put(JsonKey.USER_ID, userMap.get(JsonKey.ID));

			Set<String> keySet = reqMap.keySet();

			for (String key : keySet) {
				System.out.println(key + " " + reqMap.get(key));
			}

			
			  try { cassandraOperation.insertRecord(eduDbInfo.getKeySpace(),
			  eduDbInfo.getTableName(), reqMap); } catch (Exception e) {
			  ProjectLogger.log(e.getMessage(), e); }
			 
		}
	}

	private void insertJobProfileDetails(Map<String, Object> userMap, DbInfo addrDbInfo, DbInfo jobProDbInfo) {

		List<Map<String, Object>> reqList = (List<Map<String, Object>>) userMap.get(JsonKey.JOB_PROFILE);
		for (int i = 0; i < reqList.size(); i++) {
			Map<String, Object> reqMap = reqList.get(i);
			reqMap.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(i + 1));
			String addrId = null;
			Response addrResponse = null;
			if (reqMap.containsKey(JsonKey.ADDRESS)) {
				Map<String, Object> address = (Map<String, Object>) reqMap.get(JsonKey.ADDRESS);
				addrId = ProjectUtil.getUniqueIdFromTimestamp(i + 1);
				address.put(JsonKey.ID, addrId);
				address.put(JsonKey.CREATED_DATE, ProjectUtil.getFormattedDate());
				address.put(JsonKey.CREATED_BY, userMap.get(JsonKey.ID));
				try {
					addrResponse = cassandraOperation.insertRecord(addrDbInfo.getKeySpace(), addrDbInfo.getTableName(),
							address);
				} catch (Exception e) {
					ProjectLogger.log(e.getMessage(), e);
				}
			}
			if (null != addrResponse
					&& ((String) addrResponse.get(JsonKey.RESPONSE)).equalsIgnoreCase(JsonKey.SUCCESS)) {
				reqMap.put(JsonKey.ADDRESS_ID, addrId);
				reqMap.remove(JsonKey.ADDRESS);
			}
			reqMap.put(JsonKey.CREATED_DATE, ProjectUtil.getFormattedDate());
			reqMap.put(JsonKey.CREATED_BY, userMap.get(JsonKey.ID));
			reqMap.put(JsonKey.USER_ID, userMap.get(JsonKey.ID));

			Set<String> keySet = reqMap.keySet();

			for (String key : keySet) {
				System.out.println(key + " " + reqMap.get(key));
			}

			
			  try { cassandraOperation.insertRecord(jobProDbInfo.getKeySpace(),
			  jobProDbInfo.getTableName(), reqMap); } catch (Exception e) {
			  ProjectLogger.log(e.getMessage(), e); }
			 
		}

	}

	private void insertOrganisationDetails(Map<String, Object> userMap, DbInfo usrOrgDb) {

		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(1));
		reqMap.put(JsonKey.USER_ID, userMap.get(JsonKey.ID));
		reqMap.put(JsonKey.ORGANISATION_ID, userMap.get(JsonKey.REGISTERED_ORG_ID));
		reqMap.put(JsonKey.ORG_JOIN_DATE, ProjectUtil.getFormattedDate());
		reqMap.put(JsonKey.IS_DELETED, false);

		try {
			cassandraOperation.insertRecord(usrOrgDb.getKeySpace(), usrOrgDb.getTableName(), reqMap);
		} catch (Exception e) {
			ProjectLogger.log(e.getMessage(), e);
		}
	}

	private void updateUserExtId(Map<String, Object> requestMap, DbInfo usrExtIdDb) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(JsonKey.USER_ID, requestMap.get(JsonKey.USER_ID));
		/*
		 * update table for userName,phone,email,Aadhar No for each of these parameter
		 * insert a record into db for username update isVerified as true and for others
		 * param this will be false once verified will update this flag to true
		 */

		map.put(JsonKey.USER_ID, requestMap.get(JsonKey.ID));
		map.put(JsonKey.IS_VERIFIED, false);
		if (requestMap.containsKey(JsonKey.USERNAME)
				&& !(ProjectUtil.isStringNullOREmpty((String) requestMap.get(JsonKey.USERNAME)))) {
			map.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(1));
			map.put(JsonKey.EXTERNAL_ID, JsonKey.USERNAME);
			map.put(JsonKey.EXTERNAL_ID_VALUE, requestMap.get(JsonKey.USERNAME));
			map.put(JsonKey.IS_VERIFIED, true);

			reqMap.put(JsonKey.EXTERNAL_ID_VALUE, requestMap.get(JsonKey.USERNAME));
			List<Map<String, Object>> mapList = checkDataUserExtTable(map);
			if (mapList.isEmpty()) {
				updateUserExtIdentity(map, usrExtIdDb, JsonKey.INSERT);
			}
		}
		if (requestMap.containsKey(JsonKey.PHONE)
				&& !(ProjectUtil.isStringNullOREmpty((String) requestMap.get(JsonKey.PHONE)))) {
			map.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(1));
			map.put(JsonKey.EXTERNAL_ID, JsonKey.PHONE);
			map.put(JsonKey.EXTERNAL_ID_VALUE, requestMap.get(JsonKey.PHONE));

			if (!ProjectUtil.isStringNullOREmpty((String) requestMap.get(JsonKey.PHONE_VERIFIED))
					&& (boolean) requestMap.get(JsonKey.PHONE_VERIFIED)) {
				map.put(JsonKey.IS_VERIFIED, true);
			}
			reqMap.put(JsonKey.EXTERNAL_ID_VALUE, requestMap.get(JsonKey.PHONE));
			List<Map<String, Object>> mapList = checkDataUserExtTable(map);
			if (mapList.isEmpty()) {
				updateUserExtIdentity(map, usrExtIdDb, JsonKey.INSERT);
			} else {
				map.put(JsonKey.ID, mapList.get(0).get(JsonKey.ID));
				updateUserExtIdentity(map, usrExtIdDb, JsonKey.UPDATE);
			}
		}
		if (requestMap.containsKey(JsonKey.EMAIL)
				&& !(ProjectUtil.isStringNullOREmpty((String) requestMap.get(JsonKey.EMAIL)))) {
			map.put(JsonKey.ID, ProjectUtil.getUniqueIdFromTimestamp(1));
			map.put(JsonKey.EXTERNAL_ID, JsonKey.EMAIL);
			map.put(JsonKey.EXTERNAL_ID_VALUE, requestMap.get(JsonKey.EMAIL));

			if (!ProjectUtil.isStringNullOREmpty((String) requestMap.get(JsonKey.EMAIL_VERIFIED))
					&& (boolean) requestMap.get(JsonKey.EMAIL_VERIFIED)) {
				map.put(JsonKey.IS_VERIFIED, true);
			}
			reqMap.put(JsonKey.EXTERNAL_ID, requestMap.get(JsonKey.EMAIL));
			List<Map<String, Object>> mapList = checkDataUserExtTable(map);
			if (mapList.isEmpty()) {
				updateUserExtIdentity(map, usrExtIdDb, JsonKey.INSERT);
			} else {
				map.put(JsonKey.ID, mapList.get(0).get(JsonKey.ID));
				updateUserExtIdentity(map, usrExtIdDb, JsonKey.UPDATE);
			}
		}
	}

	private void updateUserExtIdentity(Map<String, Object> map, DbInfo usrExtIdDb, String opType) {
		try {
			if (JsonKey.INSERT.equalsIgnoreCase(opType)) {
				cassandraOperation.insertRecord(usrExtIdDb.getKeySpace(), usrExtIdDb.getTableName(), map);
			} else {
				cassandraOperation.updateRecord(usrExtIdDb.getKeySpace(), usrExtIdDb.getTableName(), map);
			}
		} catch (Exception e) {
			ProjectLogger.log(e.getMessage(), e);
		}

	}

	private List<Map<String, Object>> checkDataUserExtTable(Map<String, Object> map) {
		DbInfo usrExtIdDb = Util.dbInfoMap.get(JsonKey.USR_EXT_ID_DB);
		Map<String, Object> reqMap = new HashMap<>();
		reqMap.put(JsonKey.USER_ID, map.get(JsonKey.USER_ID));
		reqMap.put(JsonKey.EXTERNAL_ID_VALUE, map.get(JsonKey.EXTERNAL_ID_VALUE));
		Response response = null;
		List<Map<String, Object>> responseList = new ArrayList<>();
		try {
			response = cassandraOperation.getRecordsByProperties(usrExtIdDb.getKeySpace(), usrExtIdDb.getTableName(),
					reqMap);
		} catch (Exception ex) {
			ProjectLogger.log("Exception Occured while fetching data from user Ext Table in bulk upload", ex);
		}
		if (null != response) {
			responseList = (List<Map<String, Object>>) response.get(JsonKey.RESPONSE);
		}
		return responseList;
	}

}
