package com.rhbgroup.dcpbo.user.common;

import java.util.List;

import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserUsergroupRepository extends JpaRepository<UserUsergroup, Integer> {

	UserUsergroup findOneByUserIdAndUserGroupId(Integer userId, Integer userGroupId);
	List<UserUsergroup> findAllByUserId(Integer userId);
	UserUsergroup findOneByUserId(int userId);
	List<UserUsergroup> findByUserGroupId(int userGroupId);

	@Query(value = "select x.userGroupId from UserUsergroup x where x.userId = :userId and x.status = 'A'")
	public List<Integer> findUserGroupIdListByUserId(@Param("userId") Integer userId);

	@Query(value = "select x.userId from UserUsergroup x where x.userGroupId in (:userGroupIds)")
	public List<Integer> findUserIdsByUsergroupIds(@Param("userGroupIds") List<Integer> userGroupId);

	@Query(value = "select * from tbl_bo_user_usergroup a where a.status != :status " +
			" and USER_GROUP_ID in (select id from tbl_bo_usergroup where GROUP_STATUS != :status )", nativeQuery = true	)
	public List<UserUsergroup> findAllByExcludeStatus(@Param("status") String status);
}
