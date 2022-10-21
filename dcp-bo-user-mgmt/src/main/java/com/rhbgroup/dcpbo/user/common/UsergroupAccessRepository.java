package com.rhbgroup.dcpbo.user.common;

import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.UsergroupAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface UsergroupAccessRepository extends JpaRepository<UsergroupAccess, Integer>{

    @Query("SELECT x from UsergroupAccess x WHERE x.userGroupId = :userGroupId")
    List<UsergroupAccess> findByUserGroupId(@Param("userGroupId") Integer userGroupId);

    @Query("SELECT x from UsergroupAccess x WHERE x.userGroupId = :userGroupId and x.status = 'A'")
    List<UsergroupAccess> findByUserGroupIdAndStatus(@Param("userGroupId") Integer userGroupId);

    @Query("SELECT x from UsergroupAccess x WHERE x.userGroupId = :userGroupId and x.status = 'A' and x.accessType in (:accessTypes)")
    List<UsergroupAccess> findByUserGroupIdAndStatusAndAccessType(@Param("userGroupId") Integer userGroupId, @Param("accessTypes") List<String> accessTypes);

    @Query(value = "select module_id from BO_usergroup_access where user_group_id = ?1", nativeQuery = true)
    List<Integer> findModuleIdByUsergroupid(Integer user_group_id);

    @Query(value = "select * from BO_usergroup_access where module_id = ?1 and user_group_id = ?2", nativeQuery = true)
    List<com.rhbgroup.dcpbo.common.model.UsergroupAccess> findByModuleIdAndUserGroupId(Integer module_id, Integer user_group_id);

    @Query(value = "  select A.USER_GROUP_ID, A.FUNCTION_ID,B.MODULE_ID,A.SCOPE_ID,A.ACCESS_TYPE,A.STATUS,A.CREATED_TIME,A.CREATED_BY,A.UPDATED_TIME,A.UPDATED_BY from TBL_BO_USERGROUP_ACCESS A JOIN\n" +
            "  (select * from TBL_BO_CONFIG_FUNCTION) B ON A.FUNCTION_ID = B.ID WHERE A.USER_GROUP_ID in (:userGroupId) and A.STATUS = 'A' ORDER BY B.module_id, A.function_id", nativeQuery = true)
    List<Object[]> findByUserGroupIdList(@Param("userGroupId") List<Integer> userGroupId);


    @Query(value = "SELECT x FROM UsergroupAccess x WHERE x.userGroupId = :userGroupId and x.functionId = :functionId")
    UsergroupAccess findByUserGroupIdAndFunctionId(@Param("userGroupId") Integer userGroupId,
                                   @Param("functionId") Integer functionId);

    @Query(value = "SELECT x FROM UsergroupAccess x WHERE x.userGroupId = :userGroupId and x.accessType = :accessType")
    List<UsergroupAccess> findByUserGroupIdAndAccessType(@Param("userGroupId") Integer userGroupId,
                                                   @Param("accessType") String accessType);

    @Query(value = "SELECT x FROM UsergroupAccess x WHERE x.userGroupId = :userGroupId and x.functionId = :functionId and x.accessType = :accessType")
    UsergroupAccess findByUserGroupIdAndFunctionIdAndAccessType(@Param("userGroupId") Integer userGroupId,
                                                   @Param("functionId") Integer functionId,
                                                   @Param("accessType") String accessType);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO TBL_BO_USERGROUP_ACCESS (USER_GROUP_ID, FUNCTION_ID, SCOPE_ID, ACCESS_TYPE, STATUS," +
                        " CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES (:userGroupId,:functionId,:scopeId,:accessType,:status," +
                        ":createdTime, :createdBy, :updatedTime, :updatedBy)", nativeQuery = true)
    Integer insert(@Param("userGroupId") String userGroupId, @Param("functionId") String functionId,
                   @Param("scopeId") String scopeId,
                   @Param("accessType") String accessType, @Param("status") String status,
                   @Param("createdTime") Timestamp createdTime, @Param("createdBy") String createdBy,
                   @Param("updatedTime") Timestamp updatedTime, @Param("updatedBy") String updatedBy);

    @Query(value = "SELECT * FROM TBL_BO_USERGROUP_ACCESS x where x.user_group_id = :usergroupId and "
            + "(:accessType is null or :accessType = '' or x.access_type = :accessType) and "
            + "(:functionId is null or :functionId = '' or x.function_id = :functionId)",
            nativeQuery = true)
    List<UsergroupAccess> findByUserGroupIdAndOptionalAccessTypeOrOptionalFunctionId(
                    @Param("usergroupId") Integer usergroupId,
                    @Param("accessType") String accessType,
                    @Param("functionId") Integer functionId
            );

    @Query(value = "select x.user_group_id from TBL_BO_USERGROUP_ACCESS x where x.function_id in (:functionIds) group by x.user_group_id",nativeQuery = true)
    public List<Integer> findUserGroupIdByFunctionId(@Param("functionIds") List<Integer> functionIds);

    @Query(value="select x.user_group_id from TBL_BO_USERGROUP_ACCESS x where x.access_type in (:accessTypes) group by x.user_group_id",nativeQuery = true)
    List<Integer> findUsergroupIdByAccessType(@Param("accessTypes") List<String> accessTypes);

}

