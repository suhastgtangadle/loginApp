package com.infosys.loginApp.helper;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunbird.common.ElasticSearchUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.models.util.ProjectUtil.EsIndex;
import org.sunbird.common.models.util.ProjectUtil.EsType;
import org.sunbird.dto.SearchDTO;


public class Util {

	public static Map<String, DbInfo> dbInfoMap = new HashMap<>();
	private static final String KEY_SPACE_NAME = "sunbird";
	
	 static {
		 initializeDBProperty();
	 }
	 
	 public static void removeAttributes(Map<String, Object> map ,List<String> keys ){

	        if(null != map && null != keys) {
	            for (String key : keys) {
	                map.remove(key);
	            }
	        }
	    }
	 
	  private static Map<String , Object> elasticSearchComplexSearch(Map<String , Object> filters , String index , String type) {

	      SearchDTO searchDTO = new SearchDTO();
	      searchDTO.getAdditionalProperties().put(JsonKey.FILTERS , filters);

	      return ElasticSearchUtil.complexSearch(searchDTO , index,type);

	    }
	  
	  public static boolean isNotNull(Object obj){
	        return null != obj? true:false;
	    }
	  
	   public static String getRootOrgIdFromChannel(String channel) {
		      if (!ProjectUtil.isStringNullOREmpty(channel)) {
		        Map<String, Object> filters = new HashMap<>();
		        filters.put(JsonKey.CHANNEL, channel);
		        filters.put(JsonKey.IS_ROOT_ORG, true);
		        Map<String, Object> esResult = elasticSearchComplexSearch(filters,
		            EsIndex.sunbird.getIndexName(), EsType.organisation.getTypeName());
		        if (isNotNull(esResult) && esResult.containsKey(JsonKey.CONTENT)
		            && isNotNull(esResult.get(JsonKey.CONTENT))
		            && ((List) esResult.get(JsonKey.CONTENT)).size() > 0) {
		          Map<String, Object> esContent =
		              ((List<Map<String, Object>>) esResult.get(JsonKey.CONTENT)).get(0);
		          return (String) esContent.getOrDefault(JsonKey.ID, "");
		        }
		      }
		      return "";
		    }
	
	   public static String validateRoles(List<String> roleList){
		      Map<String,Object> roleMap = DataCacheHandler.getRoleMap();
		      if(null != roleMap && !roleMap.isEmpty()){
		        for (String role : roleList){
		          if(null == roleMap.get(role)){
		            return role+" is not a valid role.";
		          }
		        }
		      }else{
		        ProjectLogger.log("Roles are not cached.Please Cache it.");
		      }
		      return JsonKey.SUCCESS;
		    }
	
	
	 private static void initializeDBProperty() {
	      //setting db info (keyspace , table) into static map
	      //this map will be used during cassandra data base interaction.
	      //this map will have each DB name and it's corresponding keyspace and table name.
	      dbInfoMap.put(JsonKey.LEARNER_COURSE_DB, getDbInfoObject(KEY_SPACE_NAME,"user_courses"));
	      dbInfoMap.put(JsonKey.LEARNER_CONTENT_DB, getDbInfoObject(KEY_SPACE_NAME,"content_consumption"));
	      dbInfoMap.put(JsonKey.COURSE_MANAGEMENT_DB, getDbInfoObject(KEY_SPACE_NAME,"course_management"));
	      dbInfoMap.put(JsonKey.USER_DB, getDbInfoObject(KEY_SPACE_NAME,"user"));
	      dbInfoMap.put(JsonKey.USER_AUTH_DB, getDbInfoObject(KEY_SPACE_NAME,"user_auth"));
	      dbInfoMap.put(JsonKey.ORG_DB, getDbInfoObject(KEY_SPACE_NAME,"organisation"));
	      dbInfoMap.put(JsonKey.PAGE_MGMT_DB, getDbInfoObject(KEY_SPACE_NAME,"page_management"));
	      dbInfoMap.put(JsonKey.PAGE_SECTION_DB, getDbInfoObject(KEY_SPACE_NAME,"page_section"));
	      dbInfoMap.put(JsonKey.SECTION_MGMT_DB, getDbInfoObject(KEY_SPACE_NAME,"page_section"));
	      dbInfoMap.put(JsonKey.ASSESSMENT_EVAL_DB, getDbInfoObject(KEY_SPACE_NAME,"assessment_eval"));
	      dbInfoMap.put(JsonKey.ASSESSMENT_ITEM_DB, getDbInfoObject(KEY_SPACE_NAME,"assessment_item"));
	      dbInfoMap.put(JsonKey.ADDRESS_DB, getDbInfoObject(KEY_SPACE_NAME,"address"));
	      dbInfoMap.put(JsonKey.EDUCATION_DB, getDbInfoObject(KEY_SPACE_NAME,"user_education"));
	      dbInfoMap.put(JsonKey.JOB_PROFILE_DB, getDbInfoObject(KEY_SPACE_NAME,"user_job_profile"));
	      dbInfoMap.put(JsonKey.USR_ORG_DB, getDbInfoObject(KEY_SPACE_NAME,"user_org"));
	      dbInfoMap.put(JsonKey.USR_EXT_ID_DB, getDbInfoObject(KEY_SPACE_NAME,"user_external_identity"));

	      dbInfoMap.put(JsonKey.ORG_MAP_DB, getDbInfoObject(KEY_SPACE_NAME,"org_mapping"));
	      dbInfoMap.put(JsonKey.ORG_TYPE_DB, getDbInfoObject(KEY_SPACE_NAME,"org_type"));
	      dbInfoMap.put(JsonKey.ROLE, getDbInfoObject(KEY_SPACE_NAME,"role"));
	      dbInfoMap.put(JsonKey.MASTER_ACTION, getDbInfoObject(KEY_SPACE_NAME,"master_action"));
	      dbInfoMap.put(JsonKey.URL_ACTION, getDbInfoObject(KEY_SPACE_NAME,"url_action"));
	      dbInfoMap.put(JsonKey.ACTION_GROUP, getDbInfoObject(KEY_SPACE_NAME,"action_group"));
	      dbInfoMap.put(JsonKey.USER_ACTION_ROLE, getDbInfoObject(KEY_SPACE_NAME,"user_action_role"));
	      dbInfoMap.put(JsonKey.ROLE_GROUP, getDbInfoObject(KEY_SPACE_NAME,"role_group"));
	      dbInfoMap.put(JsonKey.USER_ORG_DB , getDbInfoObject(KEY_SPACE_NAME , "user_org"));
	      dbInfoMap.put(JsonKey.BULK_OP_DB , getDbInfoObject(KEY_SPACE_NAME , "bulk_upload_process"));
	      dbInfoMap.put(JsonKey.COURSE_BATCH_DB, getDbInfoObject(KEY_SPACE_NAME , "course_batch"));
	      dbInfoMap.put(JsonKey.COURSE_PUBLISHED_STATUS, getDbInfoObject(KEY_SPACE_NAME , "course_publish_status"));
	      dbInfoMap.put(JsonKey.REPORT_TRACKING_DB ,getDbInfoObject(KEY_SPACE_NAME , "report_tracking"));
	      dbInfoMap.put(JsonKey.BADGES_DB, getDbInfoObject(KEY_SPACE_NAME , "badge"));
	      dbInfoMap.put(JsonKey.USER_BADGES_DB, getDbInfoObject(KEY_SPACE_NAME , "user_badge"));
	      dbInfoMap.put(JsonKey.USER_NOTES_DB, getDbInfoObject(KEY_SPACE_NAME, "user_notes"));
	      dbInfoMap.put(JsonKey.MEDIA_TYPE_DB, getDbInfoObject(KEY_SPACE_NAME, "media_type"));
	      dbInfoMap.put(JsonKey.USER_SKILL_DB , getDbInfoObject(KEY_SPACE_NAME, "user_skills"));
	      dbInfoMap.put(JsonKey.SKILLS_LIST_DB , getDbInfoObject(KEY_SPACE_NAME, "skills"));
	      dbInfoMap.put(JsonKey.TENANT_PREFERENCE_DB , getDbInfoObject(KEY_SPACE_NAME, "tenant_preference"));
	      dbInfoMap.put(JsonKey.GEO_LOCATION_DB , getDbInfoObject(KEY_SPACE_NAME, "geo_location"));

	      dbInfoMap.put(JsonKey.CLIENT_INFO_DB, getDbInfoObject(KEY_SPACE_NAME, "client_info"));
	    }
	 
	 private static DbInfo getDbInfoObject(String keySpace, String table) {

	        DbInfo dbInfo = new DbInfo();

	        dbInfo.setKeySpace(keySpace);
	        dbInfo.setTableName(table);

	        return dbInfo;
	    }
	 
	 public static SearchDTO createSearchDto(Map<String , Object> searchQueryMap){
	        SearchDTO search = new SearchDTO();
	        if(searchQueryMap.containsKey(JsonKey.QUERY)){
	         search.setQuery((String) searchQueryMap.get(JsonKey.QUERY));
	        }
	        if(searchQueryMap.containsKey(JsonKey.FACETS)){
	            search.setFacets((List<Map<String , String>>) searchQueryMap.get(JsonKey.FACETS));
	        }
	        if(searchQueryMap.containsKey(JsonKey.FIELDS)){
	            search.setFields((List<String>) searchQueryMap.get(JsonKey.FIELDS));
	        }
	        if(searchQueryMap.containsKey(JsonKey.FILTERS)){
	            search.getAdditionalProperties().put(JsonKey.FILTERS,searchQueryMap.get(JsonKey.FILTERS));
	        }
	        if(searchQueryMap.containsKey(JsonKey.EXISTS)){
	            search.getAdditionalProperties().put(JsonKey.EXISTS, searchQueryMap.get(JsonKey.EXISTS));
	        }
	        if(searchQueryMap.containsKey(JsonKey.NOT_EXISTS)){
	            search.getAdditionalProperties().put(JsonKey.NOT_EXISTS, searchQueryMap.get(JsonKey.NOT_EXISTS));
	        }
	        if(searchQueryMap.containsKey(JsonKey.SORT_BY)){
	            search.getSortBy().putAll((Map<? extends String, ? extends String>) searchQueryMap.get(JsonKey.SORT_BY));
	        }
	        if(searchQueryMap.containsKey(JsonKey.OFFSET)){
	            if((searchQueryMap.get(JsonKey.OFFSET)) instanceof Integer ){
	                search.setOffset((int)searchQueryMap.get(JsonKey.OFFSET));
	            }else{
	                search.setOffset(((BigInteger) searchQueryMap.get(JsonKey.OFFSET)).intValue());
	            }
	        }
	        if(searchQueryMap.containsKey(JsonKey.LIMIT)){
	            if((searchQueryMap.get(JsonKey.LIMIT)) instanceof Integer ){
	                search.setLimit((int)searchQueryMap.get(JsonKey.LIMIT));
	            }else{
	                search.setLimit(((BigInteger) searchQueryMap.get(JsonKey.LIMIT)).intValue());
	            }
	        }
	        if(searchQueryMap.containsKey(JsonKey.GROUP_QUERY)){
	            search.getGroupQuery().addAll((Collection<? extends Map<String, Object>>) searchQueryMap.get(JsonKey.GROUP_QUERY));
	        }
	        return search;
	    }
	
	
}
