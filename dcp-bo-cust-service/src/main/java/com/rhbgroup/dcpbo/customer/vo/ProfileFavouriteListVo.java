package com.rhbgroup.dcpbo.customer.vo;

import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileFavouriteListVo implements BoData {

	List<ProfileFavouriteVo> favourites;

}
