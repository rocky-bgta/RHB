package com.rhbgroup.dcpbo.user.common;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rhbgroup.dcpbo.user.info.model.bo.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT name FROM User x WHERE x.id = :id")
    String findNameById(@Param("id") Integer id);

    @Query("SELECT x FROM User x WHERE x.username LIKE CONCAT('%', :keyword, '%')")
	List<User> findByUsernamePattern(@Param("keyword") String keyword);

    @Query("SELECT x FROM User x WHERE x.name LIKE CONCAT('%', :keyword, '%')")
	List<User> findByNamePattern(@Param("keyword") String keyword);

	public User findOneByUsername(String username);

	@Query("SELECT x FROM User x WHERE x.username = :username AND x.userStatusId IN ('A', 'I', 'L')")
	public List<User> findByUsername(@Param("username") String username);

	public User findById(Integer id);

	@Query(value ="SELECT x FROM User x WHERE " +
			" ( x.username LIKE CONCAT('%', :keyword, '%') OR x.name LIKE CONCAT('%', :keyword, '%')) AND " +
			" ( 0 = :statusExist OR ( x.userStatusId in (:status))) AND " +
			" ( 0 = :departmentExist OR ( x.userDepartmentId in (:departmentId))) AND " +
			" ( 0 = :fromDateExist OR ( x.createdTime >= (:fromTimestamp))) AND " +
			" ( 0 = :toDateExist OR ( x.createdTime < (:toTimestamp))) " +
			" ORDER BY ISNUMERIC(x.name), x.name, ISNUMERIC(x.username), x.username")
	List<User> findByCustomParamSortDefault(@Param("keyword") String keyword,
								 @Param("statusExist") Integer statusExist, @Param("status") List<String> status,
								 @Param("departmentExist") Integer departmentExist, @Param("departmentId") List<Integer> departmentId,
								 @Param("fromDateExist") Integer fromDateExist, @Param("fromTimestamp") Timestamp fromTimestamp,
								 @Param("toDateExist") Integer toDateExist, @Param("toTimestamp") Timestamp toTimestamp
								 );

	@Query(value ="SELECT x FROM User x WHERE " +
			" ( x.username LIKE CONCAT('%', :keyword, '%') OR x.name LIKE CONCAT('%', :keyword, '%')) AND " +
			" ( 0 = :statusExist OR ( x.userStatusId in (:status))) AND " +
			" ( 0 = :departmentExist OR ( x.userDepartmentId in (:departmentId))) AND " +
			" ( 0 = :fromDateExist OR ( x.createdTime >= (:fromTimestamp))) AND " +
			" ( 0 = :toDateExist OR ( x.createdTime < (:toTimestamp))) " +
			" ORDER BY x.createdTime DESC")
	List<User> findByCustomParamSortCreated(@Param("keyword") String keyword,
											@Param("statusExist") Integer statusExist, @Param("status") List<String> status,
											@Param("departmentExist") Integer departmentExist, @Param("departmentId") List<Integer> departmentId,
											@Param("fromDateExist") Integer fromDateExist, @Param("fromTimestamp") Timestamp fromTimestamp,
											@Param("toDateExist") Integer toDateExist, @Param("toTimestamp") Timestamp toTimestamp
	);

	@Query(value ="SELECT x FROM User x WHERE " +
			" ( x.username LIKE CONCAT('%', :keyword, '%') OR x.name LIKE CONCAT('%', :keyword, '%')) AND " +
			" ( 0 = :statusExist OR ( x.userStatusId in (:status))) AND " +
			" ( 0 = :departmentExist OR ( x.userDepartmentId in (:departmentId))) AND " +
			" ( 0 = :fromDateExist OR ( x.createdTime >= (:fromTimestamp))) AND " +
			" ( 0 = :toDateExist OR ( x.createdTime < (:toTimestamp))) " +
			" ORDER BY x.updatedTime DESC")
	List<User> findByCustomParamSortUpdated(@Param("keyword") String keyword,
											@Param("statusExist") Integer statusExist, @Param("status") List<String> status,
											@Param("departmentExist") Integer departmentExist, @Param("departmentId") List<Integer> departmentId,
											@Param("fromDateExist") Integer fromDateExist, @Param("fromTimestamp") Timestamp fromTimestamp,
											@Param("toDateExist") Integer toDateExist, @Param("toTimestamp") Timestamp toTimestamp
	);
}

