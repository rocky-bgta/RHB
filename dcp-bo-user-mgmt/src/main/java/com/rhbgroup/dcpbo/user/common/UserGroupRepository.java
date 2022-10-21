package com.rhbgroup.dcpbo.user.common;

import java.util.List;

import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface UserGroupRepository extends JpaRepository<Usergroup, Integer> {

	@Query("SELECT x from Usergroup x WHERE x.groupName like LOWER(CONCAT('%', :groupName, '%')) and x.groupStatus ='A'")
	List<Usergroup> findByGroupName(@Param("groupName") String groupName);

    @Query("SELECT count(x) from Usergroup x WHERE x.groupName = :usergroupName and x.groupStatus = :usergroupStatus")
    Integer findCountByGroupNameAndGroupStatus(@Param("usergroupName") String usergroupName, @Param("usergroupStatus") String usergroupStatus);

    @Query(value = "INSERT INTO TBL_BO_USERGROUP (GROUP_NAME, GROUP_STATUS, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)" +
            "OUTPUT inserted.id" +
            " VALUES (:groupName,:groupStatus,:createdTime, :createdBy, :updatedTime, :updatedBy) ", nativeQuery = true)
    List<Integer> insert(@Param("groupName") String groupName, @Param("groupStatus") String groupStatus,
                     @Param("createdTime") Timestamp createdTime, @Param("createdBy") String createdBy,
                     @Param("updatedTime") Timestamp updatedTime, @Param("updatedBy") String updatedBy);

	Usergroup findOneById(Integer id);

    @Query(value = "SELECT * from TBL_BO_USERGROUP x WHERE x.GROUP_NAME like LOWER(CONCAT('%', :keyword, '%')) and x.GROUP_STATUS = 'A'" , nativeQuery = true)
	public List<Usergroup> findDistinctByGroupNameContaining(@Param("keyword") String keyword);

    @Query(value = "SELECT * from TBL_BO_USERGROUP x WHERE x.GROUP_NAME like LOWER(CONCAT('%', :keyword, '%')) and x.group_status = 'A'"
            + " and x.ID in (SELECT x.user_group_id from TBL_BO_USERGROUP_ACCESS x where x.status = 'A')"
            + " order by x.GROUP_NAME offset :offset rows fetch next 10 rows only", nativeQuery = true)
    public List<Usergroup> getUsergroupContaining(@Param("keyword") String keyword, @Param("offset") Integer offset);

    @Query(value = "SELECT count(*) from TBL_BO_USERGROUP x WHERE x.GROUP_NAME like LOWER(CONCAT('%', :keyword, '%')) and x.group_status = 'A'"
            + " and x.ID in (SELECT x.user_group_id from TBL_BO_USERGROUP_ACCESS x where x.status = 'A')", nativeQuery = true)
    Integer getUsergroupCount(@Param("keyword") String keyword);

    @Query(value = "SELECT * from TBL_BO_USERGROUP x WHERE x.GROUP_NAME like LOWER(CONCAT('%', :keyword, '%')) and x.group_status = 'A'" +
            " and x.ID in (SELECT x.user_group_id from TBL_BO_USERGROUP_ACCESS x where x.status = 'A')", nativeQuery = true)
    public List<Usergroup> getUsergroupByKeyword(@Param("keyword") String keyword);

    @Query("SELECT x from Usergroup x WHERE x.groupStatus != :groupStatus")
    List<Usergroup> findAllByExcludeStatus(@Param("groupStatus") String groupStatus);
}