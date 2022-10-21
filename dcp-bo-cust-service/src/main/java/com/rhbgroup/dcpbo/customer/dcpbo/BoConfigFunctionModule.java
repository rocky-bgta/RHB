package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
public class BoConfigFunctionModule {

    @Id
    private Integer id;
    private String functionName;
    private Integer moduleId;
    private String moduleName;

}
