package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface DeviceProfileRepository extends JpaRepository<DeviceProfile, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM DeviceProfile x WHERE x.deviceId = :deviceId")
    Integer deleteDeviceById(@Param("deviceId") String deviceId);

    @Query(value = "SELECT COUNT(x.deviceId) FROM DeviceProfile x WHERE x.deviceId = :deviceId")
    int findDeviceId(@Param("deviceId") String deviceId);

    @Query(value = "SELECT x FROM DeviceProfile x WHERE x.deviceId = :deviceId")
    DeviceProfile findByDeviceId(@Param("deviceId") String deviceId);

    @Query(value = "SELECT x FROM DeviceProfile x WHERE x.userId = :userId and x.deviceStatus = 'ACTIVE' ")
    List<DeviceProfile> findDeviceByUserIdAndActiveDevice(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE DeviceProfile x SET x.deviceStatus = :deviceStatus WHERE x.id = :deviceId")
    Integer updateDeviceStatus(@Param("deviceStatus") String deviceStatus, @Param("deviceId") Integer deviceId);
}
